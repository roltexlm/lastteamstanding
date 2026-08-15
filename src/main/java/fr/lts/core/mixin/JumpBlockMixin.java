package fr.lts.core.mixin;

import fr.lts.core.client.LtsCoreClient;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bloque le saut pendant la phase de placement (/lts tp, avant /lts start).
 *
 * <p>Le saut est déclenché côté client. On lit la phase de jeu depuis
 * {@link LtsCoreClient#getClientPhase()} qui est mise à jour via packet
 * custom du serveur. Cela fonctionne aussi en multi (le client reçoit la
 * phase via le packet).</p>
 */
@Mixin(LivingEntity.class)
public abstract class JumpBlockMixin {

    @Inject(method = "method_6043", at = @At("HEAD"), cancellable = true)
    private void lts$jump(CallbackInfo ci) {
        try {
            String phase = LtsCoreClient.getClientPhase();
            if ("PLACEMENT".equals(phase)) {
                ci.cancel();
            }
        } catch (Exception e) {
            // Ignore : le client n'est pas encore connecte.
        }
    }
}
