package fr.lts.core.game;

import fr.lts.core.team.Team;
import fr.lts.core.team.TeamService;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Service de logique de partie : téléportation des équipes, stun/invincibilité
 * des joueurs pendant le placement, démarrage (libération), arrêt (reset +
 * spectateur).
 *
 * <p>Travaille sur un {@link MinecraftServer} donné. Toutes les opérations
 * doivent être appelées depuis le thread serveur principal.</p>
 */
public class GameService {

    private final TeamService teamService;
    private final GameState state = new GameState();
    private final Random random = new Random();

    public GameService(TeamService teamService) {
        this.teamService = teamService;
    }

    public GameState getState() {
        return state;
    }

    public TeamService getTeamService() {
        return teamService;
    }

    // ----- /lts tp -----

    /**
     * Téléporte les teams actives sur la map et stune les joueurs.
     *
     * <p>Comportement :</p>
     * <ul>
     *   <li>Si aucune team active et que {@code force} est {@code false} :
     *       erreur "Aucune team active". L'appelant doit mémoriser que le
     *       prochain appel doit forcer.</li>
     *   <li>Sinon : calcule la taille de map proportionnelle au nombre de
     *       joueurs actifs, place les teams sur la map, téléporte chaque team
     *       sur un point (membres sur le même bloc), et stune les joueurs
     *       (immobiles + invincibles, vision libre).</li>
     *   <li>Les joueurs sans team ne sont pas téléportés et restent
     *       spectateurs.</li>
     * </ul>
     *
     * @param server serveur cible.
     * @param force  {@code true} pour forcer même sans team active (2e appel
     *               consécutif).
     * @return résultat du tp (succès/échec + message).
     */
    public TpResult teleportTeams(MinecraftServer server, boolean force) {
        List<Team> activeTeams = teamService.getActiveTeams();

        if (activeTeams.isEmpty() && !force) {
            return TpResult.error("Aucune team active. Tapez /lts tp une seconde fois pour forcer.");
        }

        // Compter le nombre de joueurs actifs (dans une team) pour la taille
        // de map.
        int activePlayers = 0;
        for (Team t : activeTeams) {
            activePlayers += t.size();
        }
        if (activePlayers == 0) {
            // Force mais rien à téléporter.
            state.setMapSizeBlocks(MapPlacement.computeMapSize(0));
            return TpResult.error("Aucun joueur à téléporter (teams vides).");
        }

        int mapSize = MapPlacement.computeMapSize(activePlayers);
        state.setMapSizeBlocks(mapSize);

        ServerWorld world = server.getOverworld();
        if (world == null) {
            return TpResult.error("Le monde n'est pas disponible.");
        }

        // Placement des teams. On utilise les teams actives non vides ; si
        // toutes sont vides (cas force), on n'a rien à faire.
        List<Team> teamsToPlace = activeTeams;
        List<int[]> spawnPoints = MapPlacement.computeTeamSpawnPoints(
            teamsToPlace.size(), mapSize, random);

        for (int i = 0; i < teamsToPlace.size(); i++) {
            Team team = teamsToPlace.get(i);
            int[] xz = spawnPoints.get(i);
            int x = xz[0];
            int z = xz[1];
            // Hauteur : surface du monde à cette colonne.
            int y = world.getTopY(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

            for (UUID playerId : team.getMembers()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                teleportTo(player, world, x + 0.5, y, z + 0.5);
                stunPlayer(player);
            }
        }

        state.setPhase(GamePhase.PLACEMENT);
        state.setTpForceNext(false);
        return TpResult.success(mapSize, teamsToPlace.size(), activePlayers);
    }

    private void teleportTo(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
        // Téléportation réseau-safe : on met le joueur en position puis on
        // synchronise.
        player.teleport(world, x, y, z, player.getYaw(), player.getPitch());
    }

    // ----- Stun / un-stun -----

    /**
     * Stune un joueur : immobile (vitesse 0) + invincible, mais vision libre
     * (pas en spectateur, on garde la gamemode courante mais on neutralise
     * les déplacements).
     *
     * <p>Implémentation : on passe le mouvement à 0 via l'attribut
     * {@code GENERIC_MOVEMENT_SPEED}, on marque invulnérable, et on gèle la
     * position en interne. Le joueur peut regarder autour de lui.</p>
     */
    private void stunPlayer(ServerPlayerEntity player) {
        player.setInvulnerable(true);
        // Vitesse de déplacement = 0. On stocke l'ancienne valeur via
        // l'attribut directement (la valeur de base).
        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            // Applique un modificateur multiplicatif à 0 pour immobiliser.
            // On ne remplace pas la base pour pouvoir restaurer ensuite.
            speed.setBaseValue(0.0);
        }
        // Nourriture au max pour éviter la régénération/recoil, et santé max.
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel(5.0F);
        player.setHealth(player.getMaxHealth());
        player.clearActiveItem();
    }

    /**
     * Lève le stun d'un joueur : restaure la vitesse de déplacement vanilla et
     * retire l'invincibilité.
     */
    private void unstunPlayer(ServerPlayerEntity player) {
        player.setInvulnerable(false);
        EntityAttributeInstance speed = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            // Vitesse de marche vanilla : 0.1 (EntityAttributes.GENERIC_MOVEMENT_SPEED
            // default value is 0.1).
            speed.setBaseValue(0.10000000149011612D);
        }
    }

    // ----- /lts start -----

    /**
     * Démarre la partie : lève le stun des joueurs placés et lance le timer.
     *
     * @return erreur si la phase n'est pas {@link GamePhase#PLACEMENT}.
     */
    public StartResult start(MinecraftServer server) {
        if (state.getPhase() != GamePhase.PLACEMENT) {
            return StartResult.error("Joueurs non placés. Utilisez /lts tp d'abord.");
        }

        for (Team team : teamService.getActiveTeams()) {
            for (UUID playerId : team.getMembers()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                unstunPlayer(player);
            }
        }

        state.setPhase(GamePhase.RUNNING);
        state.setStartTimeTicks(server.getOverworld().getTime());
        return StartResult.success();
    }

    // ----- /lts stop -----

    /**
     * Arrête la partie : retire les teams des joueurs, passe les joueurs en
     * spectateur, reset les options (team size, assignations, hardcore, timer).
     *
     * @return résultat du stop.
     */
    public StopResult stop(MinecraftServer server) {
        // Passer les joueurs placés/en jeu en spectateur et lever le stun.
        for (Team team : teamService.getActiveTeams()) {
            for (UUID playerId : team.getMembers()) {
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                unstunPlayer(player);
                player.setGameMode(GameMode.SPECTATOR);
            }
        }

        // TODO: couper l'affichage du title des vainqueurs (UI pas encore implémentée).

        teamService.reset();
        state.reset();
        return StopResult.success();
    }

    // ----- Hardcore -----

    /**
     * Change le mode hardcore. Ne modifie pas la difficulté du monde ici (fait
     * ailleurs ou via commande vanilla) : on stocke juste la préférence.
     */
    public void setHardcore(HardcoreMode mode) {
        state.setHardcore(mode);
    }

    // ----- Résultats -----

    public static final class TpResult {
        public final boolean success;
        public final String message;
        public final int mapSize;
        public final int teamsPlaced;
        public final int playersPlaced;

        private TpResult(boolean success, String message, int mapSize, int teamsPlaced, int playersPlaced) {
            this.success = success;
            this.message = message;
            this.mapSize = mapSize;
            this.teamsPlaced = teamsPlaced;
            this.playersPlaced = playersPlaced;
        }

        static TpResult error(String message) {
            return new TpResult(false, message, 0, 0, 0);
        }

        static TpResult success(int mapSize, int teamsPlaced, int playersPlaced) {
            return new TpResult(true,
                String.format("Teams téléportées : %d teams, %d joueurs, map %d×%d.",
                    teamsPlaced, playersPlaced, mapSize, mapSize),
                mapSize, teamsPlaced, playersPlaced);
        }
    }

    public static final class StartResult {
        public final boolean success;
        public final String message;

        private StartResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static StartResult error(String message) {
            return new StartResult(false, message);
        }

        static StartResult success() {
            return new StartResult(true, "Partie démarrée. Bonne chance !");
        }
    }

    public static final class StopResult {
        public final boolean success;
        public final String message;

        private StopResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        static StopResult success() {
            return new StopResult(true, "Partie arrêtée. Teams retirées, joueurs en spectateur, options reset.");
        }
    }

    /**
     * Calcule le temps restant en ticks (positif si partie en cours, négatif
     * si écoulé).
     */
    public long getRemainingTicks(MinecraftServer server) {
        if (state.getStartTimeTicks() < 0) {
            return GameState.GAME_DURATION_TICKS;
        }
        long elapsed = server.getOverworld().getTime() - state.getStartTimeTicks();
        return GameState.GAME_DURATION_TICKS - elapsed;
    }
}
