package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Empêche de récupérer le résultat d'un craft si l'item produit est banni
 * (ex: Shield).
 *
 * <p>Le joueur voit la recette, voit le résultat dans le slot, mais ne peut
 * pas le prendre (clic annulé). Les ingrédients ne sont pas consommés.</p>
 */
@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {

    /**
     * Intercepte {@link CraftingResultSlot#takeStack(int)}.
     * Si le résultat est un item banni, on annule la prise.
     */
    @Inject(method = "takeStack", at = @At("HEAD"), cancellable = true)
    private void lts$takeStack(int amount, CallbackInfoReturnable<ItemStack> cir) {
        CraftingResultSlot self = (CraftingResultSlot) (Object) this;
        ItemStack result = self.getStack();
        if (result != null && !result.isEmpty()) {
            Identifier id = Registry.ITEM.getId(result.getItem());
            if (BannedItems.isItemBanned(id)) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }
}
