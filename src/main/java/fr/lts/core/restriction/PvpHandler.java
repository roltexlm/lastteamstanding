package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * Bloque le PvP entre joueurs tant que le PvP n'est pas activé (avant 1h de
 * jeu).
 *
 * <p>Quand {@link GameState#isPvpEnabled()} est false, toute attaque d'un
 * joueur sur un autre joueur est annulée.</p>
 */
public final class PvpHandler {

    private PvpHandler() {
    }

    public static void register() {
        AttackEntityCallback.EVENT.register(PvpHandler::onAttackEntity);
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world,
                                                Hand hand, Entity entity,
                                                EntityHitResult hitResult) {
        if (world.isClient) {
            return ActionResult.PASS;
        }
        // Ne bloque que pendant la partie en cours.
        GameState state;
        try {
            state = LtsState.getGameService().getState();
        } catch (IllegalStateException e) {
            return ActionResult.PASS;
        }
        if (state.getPhase() != GamePhase.RUNNING) {
            return ActionResult.PASS;
        }
        // Si le PvP est activé, on laisse faire.
        if (state.isPvpEnabled()) {
            return ActionResult.PASS;
        }
        // Si l'entité attaquée est un joueur, on bloque.
        if (entity instanceof PlayerEntity) {
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
}
