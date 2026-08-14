package fr.lts.core.game;

import fr.lts.core.LtsCore;
import fr.lts.core.LtsState;
import fr.lts.core.network.LtsNetworking;
import fr.lts.core.team.Team;
import fr.lts.core.team.TeamService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Timer serveur : envoie l'état du jeu (temps restant + kills par joueur) aux
 * clients via un packet custom, et vérifie la fin de partie (timer écoulé ou
 * une seule team restante).
 *
 * <p>Doit être appelé à chaque tick serveur (via
 * {@code ServerTickEvents.END_SERVER_TICK} enregistré dans {@link fr.lts.core.LtsCore}).</p>
 */
public final class GameTimer {

    private static final org.apache.logging.log4j.Logger LOGGER = LtsCore.LOGGER;

    private GameTimer() {
    }

    /**
     * À appeler à chaque tick serveur. Envoie l'état aux clients et vérifie la
     * fin de partie.
     */
    public static void onServerTick(MinecraftServer server) {
        GameService game = LtsState.getGameService();
        GameState state = game.getState();

        // Ne fait rien si la partie n'est pas en cours.
        if (state.getPhase() != GamePhase.RUNNING) {
            return;
        }

        long remainingTicks = game.getRemainingTicks(server);
        long remainingSeconds = Math.max(0, remainingTicks / 20L);

        // Récupère les kills par joueur depuis le scoreboard (lts_kills).
        Map<UUID, Integer> killsByPlayer = collectKills(server);

        // Envoie l'état à tous les clients via packet custom.
        LtsNetworking.broadcastGameState(server, remainingSeconds, killsByPlayer);

        // Active le PvP après 1h de jeu (3600s).
        long elapsedSeconds = GameState.GAME_DURATION_TICKS / 20L - remainingSeconds;
        if (!state.isPvpEnabled() && elapsedSeconds >= 3600L) {
            state.setPvpEnabled(true);
            applyGlowingToAll(server);
            broadcastMessage(server, "§cLe PvP est désormais activé !");
        }

        // Vérifie la fin de partie : timer écoulé.
        if (remainingTicks <= 0) {
            game.endByTimer(server);
            return;
        }

        // Vérifie la fin de partie : une seule team (ou aucune) encore vivante.
        checkLastTeamStanding(server, game, state);
    }

    /**
     * Collecte les kills par joueur depuis le scoreboard lts_kills.
     */
    private static Map<UUID, Integer> collectKills(MinecraftServer server) {
        Map<UUID, Integer> kills = new HashMap<>();
        // Le scoreboard lts_kills contient les scores par nom de joueur.
        net.minecraft.scoreboard.Scoreboard scoreboard = server.getScoreboard();
        net.minecraft.scoreboard.ScoreboardObjective killsObj = scoreboard.getObjective("lts_kills");
        if (killsObj != null) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                String name = player.getEntityName();
                if (scoreboard.getKnownPlayers().contains(name)) {
                    kills.put(player.getUuid(),
                        scoreboard.getPlayerScore(name, killsObj).getScore());
                } else {
                    kills.put(player.getUuid(), 0);
                }
            }
        }
        return kills;
    }

    /**
     * Vérifie s'il ne reste qu'une seule team (ou aucune) avec au moins un
     * joueur vivant. Si oui, déclenche la fin de partie.
     */
    /**
     * Applique l'effet Glowing infini à tous les joueurs connectés.
     */
    private static void applyGlowingToAll(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(
                net.minecraft.entity.effect.StatusEffects.GLOWING,
                999999,
                0, false, false, true));
        }
    }

    private static void broadcastMessage(MinecraftServer server, String message) {
        net.minecraft.text.Text text = new net.minecraft.text.LiteralText(message);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(text, false);
        }
    }

    private static void checkLastTeamStanding(MinecraftServer server, GameService game, GameState state) {
        TeamService ts = game.getTeamService();
        List<Team> activeTeams = ts.getActiveTeams();

        // Compte les teams qui ont encore au moins un joueur non-spectateur.
        int aliveTeams = 0;
        Team winner = null;
        for (Team team : activeTeams) {
            if (hasAliveMember(server, team)) {
                aliveTeams++;
                winner = team;
            }
        }

            aliveTeams, state.getInitialTeamsCount(), activeTeams.size());
        // Ne déclenche la victoire que si on avait au moins 2 teams au
        // départ (sinon victoire instantanée en solo).
        if (aliveTeams <= 1 && state.getInitialTeamsCount() >= 2) {
            game.endByVictory(server, winner);
        }
    }

    /**
     * Vérifie si une team a au moins un joueur non-spectateur (donc vivant).
     */
    private static boolean hasAliveMember(MinecraftServer server, Team team) {
        for (UUID playerId : team.getMembers()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            if (player == null) continue;
            if (!player.isSpectator()) {
                return true;
            }
        }
        return false;
    }
}
