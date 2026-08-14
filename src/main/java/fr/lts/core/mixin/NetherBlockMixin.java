package fr.lts.core.mixin;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bloque l'accès au Nether pendant une partie LTS.
 *
 * <p>Intercepte {@link ServerPlayerEntity#moveToWorld(ServerWorld)} et annule
 * si la destination est le Nether et que la partie est en cours.</p>
 */
@Mixin(ServerPlayerEntity.class)
public abstract class NetherBlockMixin {

    @Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
    private void lts$moveToWorld(ServerWorld destination, CallbackInfoReturnable<Boolean> cir) {
        try {
            GameState state = LtsState.getGameService().getState();
            if (state.getPhase() != GamePhase.RUNNING && state.getPhase() != GamePhase.PLACEMENT) {
                return;
            }
            // Bloque si la destination est le Nether.
            if (destination.getRegistryKey() == World.NETHER) {
                cir.setReturnValue(false);
            }
        } catch (IllegalStateException e) {
            // LtsState non initialise : ignore.
        }
    }
}
