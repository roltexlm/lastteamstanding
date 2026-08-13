package fr.lts.core.game;

import fr.lts.core.LtsState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire de mort et respawn : implémente le one-life (hardcore hybride).
 *
 * <p>Peu importe le mode (EASY ou VANILLA), le comportement est identique :</p>
 * <ol>
 *   <li>Le joueur subit des dégâts fatals → sa position de mort est mémorisée
 *       (via {@code ALLOW_DEATH}, appelé avant la mort effective).</li>
 *   <li>Quand il clique pour respawn, il est immédiatement repassé en
 *       <b>spectateur</b> et téléporté à l'endroit exact de sa mort
 *       (via {@code AFTER_RESPAWN}).</li>
 * </ol>
 *
 * <p>Ainsi le joueur ne rejoue jamais (one-life) mais peut regarder le reste
 * de la partie depuis sa position de mort. Le bannissement vanilla est
 * neutralisé car on intercepte le respawn avant qu'il ne s'applique.</p>
 *
 * <p>La texture hardcore des cœurs est activée via le flag {@code hardcore}
 * du monde (voir {@link GameService#setHardcore}), indépendamment d'ici.</p>
 */
public final class PlayerDeathHandler {

    /** Position de mort de chaque joueur (x, y, z), pour le téléport de respawn. */
    private static final Map<UUID, double[]> DEATH_POSITIONS = new HashMap<>();

    private PlayerDeathHandler() {
    }

    /**
     * Appelé quand un joueur subit des dégâts fatals (avant la mort effective).
     * Mémorise la position de mort et incrémente le compteur de kills.
     *
     * @return {@code true} pour laisser la mort se produire (le joueur meurt,
     *         puis géré au respawn).
     */
    public static boolean onAllowDeath(ServerPlayerEntity player,
                                       DamageSource damageSource,
                                       float damageAmount) {
        GameService game = LtsState.getGameService();
        GameState state = game.getState();

        // On n'agit que pendant la partie en cours.
        if (state.getPhase() != GamePhase.RUNNING) {
            return true;
        }

        // Mémorise la position de mort pour le respawn → spectateur.
        DEATH_POSITIONS.put(player.getUuid(),
            new double[]{player.getX(), player.getY(), player.getZ()});

        // Compter le kill (pour l'affichage HUD).
        state.incrementKillCount();

        // Laisse la mort se produire : le joueur mourra, puis au respawn
        // on le repassera en spectateur à cette position.
        return true;
    }

    /**
     * Appelé après le respawn d'un joueur. Si le joueur était marqué comme
     * mort (one-life), on le repasse immédiatement en spectateur et on le
     * téléporte à sa position de mort.
     *
     * @param oldPlayer l'ancienne instance du joueur (avant respawn).
     * @param newPlayer la nouvelle instance du joueur (après respawn).
     * @param alive     si l'ancien joueur est encore vivant.
     */
    public static void onAfterRespawn(ServerPlayerEntity oldPlayer,
                                      ServerPlayerEntity newPlayer,
                                      boolean alive) {
        GameService game = LtsState.getGameService();
        GameState state = game.getState();

        // On n'agit que pendant la partie en cours.
        if (state.getPhase() != GamePhase.RUNNING) {
            return;
        }

        UUID id = newPlayer.getUuid();
        double[] pos = DEATH_POSITIONS.remove(id);
        if (pos == null) {
            // Pas de mort enregistrée : on ne fait rien (pas un one-life).
            return;
        }

        // Repasse en spectateur et téléporte à la position de mort.
        newPlayer.changeGameMode(GameMode.SPECTATOR);
        newPlayer.teleport(newPlayer.getServerWorld(), pos[0], pos[1], pos[2],
            newPlayer.getYaw(), newPlayer.getPitch());
    }
}
