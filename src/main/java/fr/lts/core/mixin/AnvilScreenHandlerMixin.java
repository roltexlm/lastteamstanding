package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Empêche de récupérer le résultat d'un craft d'anvil si l'item produit
 * contient un enchantement banni (ou si c'est un item banni).
 *
 * <p>Intercepte {@link ForgingScreenHandler#onTakeOutput(PlayerEntity, ItemStack)}
 * avant que le résultat ne soit pris. Si l'item est banni, on annule l'action
 * (les ressources ne sont pas consommées).</p>
 */
@Mixin(ForgingScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    protected void lts$onTakeOutput(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (stack != null && !stack.isEmpty() && BannedItems.isItemStackBanned(stack)) {
            ci.cancel();
        }
    }
}
