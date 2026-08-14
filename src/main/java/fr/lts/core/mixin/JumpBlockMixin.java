package fr.lts.core.mixin;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bloque le saut pendant la phase de placement (/lts tp, avant /lts start).
 *
 * <p>En 1.17.1, le saut des joueurs n'est pas un attribut mais une méthode
 * {@link LivingEntity#jump()}. On l'annule si la partie est en phase
 * PLACEMENT et que l'entité est un joueur.</p>
 */
@Mixin(LivingEntity.class)
public abstract class JumpBlockMixin {

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void lts$jump(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.world.isClient) {
            return;
        }
        if (!(self instanceof net.minecraft.server.network.ServerPlayerEntity)) {
            return;
        }
        try {
            GameState state = LtsState.getGameService().getState();
            if (state.getPhase() == GamePhase.PLACEMENT) {
                ci.cancel();
            }
        } catch (IllegalStateException e) {
            // LtsState non initialise : ignore.
        }
    }
}
