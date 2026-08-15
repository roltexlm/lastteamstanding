package fr.lts.core.mixin;

import fr.lts.core.client.LtsCoreClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Remplace le displayName des joueurs cote client par une version coloree
 * avec la vraie couleur hex de leur team LTS.
 *
 * <p>Cible Entity (class_1297) car getDisplayName est defini la, et non dans
 * PlayerEntity. On filtre ensuite pour ne modifier que les joueurs.</p>
 */
@Mixin(Entity.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "method_5756", at = @At("RETURN"), cancellable = true)
    private void lts$modifyDisplayName(CallbackInfoReturnable<Text> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof PlayerEntity)) {
            return;
        }
        int color = LtsCoreClient.getPlayerColor(self.getUuid());
        if (color >= 0) {
            String name = self.getEntityName();
            cir.setReturnValue(new LiteralText(name).styled(style -> style.withColor(color)));
        }
    }
}
