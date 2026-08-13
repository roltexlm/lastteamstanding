package fr.lts.core.game;

import fr.lts.core.LtsState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

/**
 * Gestionnaire de mort des joueurs : implémente le one-life du mode hardcore.
 *
 * <p>Lorsqu'un joueur meurt pendant que la partie est en cours
 * ({@link GamePhase#RUNNING}) :</p>
 * <ul>
 *   <li>En mode {@link HardcoreMode#EASY} : le joueur est passé en
 *       spectateur (il ne peut plus jouer, mais n'est pas banni).</li>
 *   <li>En mode {@link HardcoreMode#VANILLA} : le comportement est géré par
 *       le hardcore vanilla du monde (bannissement automatique), donc on ne
 *       fait rien ici.</li>
 * </ul>
 *
 * <p>Ce handler est appelé via l'événement {@code ServerLivingEvents.AFTER_DEATH}
 * de Fabric API, enregistré dans {@link fr.lts.core.LtsCore}.</p>
 */
public final class PlayerDeathHandler {

    private PlayerDeathHandler() {
    }

    /**
     * À appeler après la mort d'une entité vivante. Ne fait quelque chose que
     * si l'entité est un joueur et que la partie est en cours.
     */
    public static void onAfterDeath(LivingEntity entity, DamageSource source) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) entity;

        GameService game = LtsState.getGameService();
        GameState state = game.getState();

        // On n'agit que pendant la partie en cours.
        if (state.getPhase() != GamePhase.RUNNING) {
            return;
        }

        // Compter le kill (pour l'affichage HUD).
        state.incrementKillCount();

        // En mode EASY, le one-life = passage en spectateur (pas de ban).
        if (state.getHardcore() == HardcoreMode.EASY) {
            // S'assurer que le respawn n'a pas déjà eu lieu ; on passe en
            // spectateur au prochain tick via le gamemode.
            player.changeGameMode(GameMode.SPECTATOR);
        }
        // En mode VANILLA, le hardcore du monde bannit automatiquement.
    }
}
