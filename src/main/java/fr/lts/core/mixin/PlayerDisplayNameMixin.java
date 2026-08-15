package fr.lts.core.mixin;

import fr.lts.core.client.LtsCoreClient;
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
 * <p>Le displayName est utilise pour le nametag au-dessus du joueur, le TAB,
 * et le chat. En le modifiant cote client, on couvre les 3 d un coup.</p>
 *
 * <p>Utilise remap = false car getDisplayName est heritee de Entity et le
 * refmap de Loom 0.9 ne la resout pas correctement en intermediary.</p>
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerDisplayNameMixin {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, remap = false)
    private void lts$modifyDisplayName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        int color = LtsCoreClient.getPlayerColor(self.getUuid());
        if (color >= 0) {
            String name = self.getEntityName();
            cir.setReturnValue(new LiteralText(name).styled(style -> style.withColor(color)));
        }
    }
}
