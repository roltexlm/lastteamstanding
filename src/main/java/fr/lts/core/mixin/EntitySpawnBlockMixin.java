package fr.lts.core.mixin;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Empêche le spawn des evokers et des horses pendant une partie LTS.
 *
 * <p>Intercepte {@link ServerWorld#spawnEntity(Entity)} et annule si l'entité
 * est un Evoker ou un Horse ( EntityType.EVOKER ou EntityType.HORSE ) et que
 * la partie est en cours.</p>
 */
@Mixin(ServerWorld.class)
public abstract class EntitySpawnBlockMixin {

    @Inject(method = "spawnEntity", at = @At("HEAD"), cancellable = true)
    private void lts$spawnEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        try {
            GameState state = LtsState.getGameService().getState();
            if (state.getPhase() != GamePhase.RUNNING && state.getPhase() != GamePhase.PLACEMENT) {
                return;
            }
            EntityType<?> type = entity.getType();
            if (type == EntityType.EVOKER || type == EntityType.HORSE) {
                cir.setReturnValue(false);
            }
        } catch (IllegalStateException e) {
            // LtsState non initialise : ignore.
        }
    }
}
