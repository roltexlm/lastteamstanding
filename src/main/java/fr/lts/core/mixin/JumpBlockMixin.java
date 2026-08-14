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
 * <p>Le saut est d\u00e9clench\u00e9 c\u00f4t\u00e9 client, puis le mouvement est envoy\u00e9 au
 * serveur. Il faut donc bloquer c\u00f4t\u00e9 client. On v\u00e9rifie la phase via
 * LtsState (qui est accessible c\u00f4t\u00e9 client car le mod est charg\u00e9 des deux
 * c\u00f4t\u00e9s).</p>
 */
@Mixin(LivingEntity.class)
public abstract class JumpBlockMixin {

    @Inject(method = "method_6043", at = @At("HEAD"), cancellable = true)
    private void lts$jump(CallbackInfo ci) {
        try {
            GameState state = LtsState.getGameService().getState();
            if (state.getPhase() == GamePhase.PLACEMENT) {
                ci.cancel();
            }
        } catch (Exception e) {
            // LtsState non initialise ou c\u00f4t\u00e9 client sans serveur : ignore.
        }
    }
}
