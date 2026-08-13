package fr.lts.core.game;

import fr.lts.core.LtsState;
import fr.lts.core.team.Team;
import fr.lts.core.team.TeamService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;

import java.util.List;

/**
 * Timer serveur : met à jour le temps restant et le nombre de kills dans le
 * scoreboard (objectifs {@code lts_timer} et {@code lts_kills}), vérifie la fin
 * de partie (timer écoulé ou une seule team restante).
 *
 * <p>Doit être appelé à chaque tick serveur (via
 * {@code ServerTickEvents.END_SERVER_TICK} enregistré dans {@link fr.lts.core.LtsCore}).</p>
 */
public final class GameTimer {

    /** Nom de l'objectif scoreboard pour le temps restant (en secondes). */
    public static final String TIMER_OBJECTIVE = "lts_timer";

    /** Nom de l'objectif scoreboard pour le nombre de kills. */
    public static final String KILLS_OBJECTIVE = "lts_kills";

    /** Entrée fictive utilisée comme holder dans le scoreboard. */
    private static final String TIMER_HOLDER = "LTS Timer";
    private static final String KILLS_HOLDER = "Kills";

    private GameTimer() {
    }

    /**
     * À appeler à chaque tick serveur. Met à jour le scoreboard et vérifie la
     * fin de partie.
     */
    public static void onServerTick(MinecraftServer server) {
        GameService game = LtsState.getGameService();
        GameState state = game.getState();

        // Ne fait rien si la partie n'est pas en cours.
        if (state.getPhase() != GamePhase.RUNNING) {
            return;
        }

        Scoreboard scoreboard = server.getScoreboard();

        // Met à jour le temps restant dans le scoreboard.
        long remainingTicks = game.getRemainingTicks(server);
        long remainingSeconds = Math.max(0, remainingTicks / 20L);

        updateObjective(scoreboard, TIMER_OBJECTIVE, TIMER_HOLDER, (int) remainingSeconds);

        // Met à jour le nombre de kills.
        updateObjective(scoreboard, KILLS_OBJECTIVE, KILLS_HOLDER, state.getKillCount());

        // Vérifie la fin de partie : timer écoulé.
        if (remainingTicks <= 0) {
            game.endByTimer(server);
            return;
        }

        // Vérifie la fin de partie : une seule team (ou aucune) encore vivante.
        checkLastTeamStanding(server, game, state);
    }

    /**
     * Met à jour (ou crée) un objectif dummy avec une valeur entière.
     */
    private static void updateObjective(Scoreboard scoreboard, String name, String holder, int value) {
        ScoreboardObjective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(name,
                ScoreboardCriterion.DUMMY,
                new LiteralText(name),
                ScoreboardCriterion.RenderType.INTEGER);
        }
        // Score du holder.
        scoreboard.getPlayerScore(holder, objective).setScore(value);
    }

    /**
     * Vérifie s'il ne reste qu'une seule team (ou aucune) avec au moins un
     * joueur vivant. Si oui, déclenche la fin de partie.
     */
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

        if (aliveTeams <= 1) {
            game.endByVictory(server, winner);
        }
    }

    /**
     * Vérifie si une team a au moins un joueur non-spectateur (donc vivant).
     */
    private static boolean hasAliveMember(MinecraftServer server, Team team) {
        for (java.util.UUID playerId : team.getMembers()) {
            net.minecraft.server.network.ServerPlayerEntity player =
                server.getPlayerManager().getPlayer(playerId);
            if (player == null) continue;
            if (!player.isSpectator()) {
                return true;
            }
        }
        return false;
    }
}
