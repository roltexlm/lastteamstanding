package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Empêche de récupérer le résultat d'un craft d'anvil si l'item produit
 * contient un enchantement banni (ou si c'est un item banni).
 *
 * <p>Le joueur voit le résultat, mais ne peut pas le prendre (les ressources
 * ne sont pas consommées).</p>
 */
@Mixin(ForgingScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Shadow
    protected Inventory output;

    @Shadow
    protected Inventory input;

    /**
     * Intercepte {@link ForgingScreenHandler#canTakeOutput(PlayerEntity, boolean)}.
     * Si le slot de résultat contient un item avec un enchantement banni (ou
     * un item banni), on retourne false pour bloquer la prise.
     */
    @Inject(method = "canTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void lts$canTakeOutput(PlayerEntity player, boolean present,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (output == null) return;
        ItemStack result = output.getStack(0);
        if (result != null && !result.isEmpty() && BannedItems.isItemStackBanned(result)) {
            cir.setReturnValue(false);
        }
    }
}
