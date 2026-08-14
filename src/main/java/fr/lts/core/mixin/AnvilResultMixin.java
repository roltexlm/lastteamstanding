package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bloque la récupération du résultat d'une anvil si l'item contient un
 * enchantement banni (ou si c'est un item banni).
 *
 * <p>Intercepte {@link ScreenHandler#onSlotClick} et vérifie si le slot
 * cliqué est le slot de résultat d'un {@link ForgingScreenHandler}. Si oui
 * et que le résultat est banni, on annule le clic.</p>
 */
@Mixin(ScreenHandler.class)
public abstract class AnvilResultMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void lts$onSlotClick(int slotIndex, int button, SlotActionType actionType,
                                  PlayerEntity player, CallbackInfo ci) {
        if (slotIndex < 0) return;

        ScreenHandler self = (ScreenHandler) (Object) this;
        if (!(self instanceof ForgingScreenHandler)) {
            return;
        }

        ForgingScreenHandler forging = (ForgingScreenHandler) self;
        if (slotIndex >= forging.slots.size()) {
            return;
        }

        Slot slot = forging.getSlot(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack result = slot.getStack();
        if (result.isEmpty()) {
            return;
        }

        // Vérifie si c'est le slot de résultat (index 2 = OUTPUT_SLOT_INDEX).
        // On vérifie aussi si l'item est banni.
        if (slotIndex == 2 && BannedItems.isItemStackBanned(result)) {
            // Bloque les actions qui prennent l'item du slot de résultat.
            if (actionType == SlotActionType.PICKUP
                    || actionType == SlotActionType.PICKUP_ALL
                    || actionType == SlotActionType.QUICK_MOVE
                    || actionType == SlotActionType.SWAP
                    || actionType == SlotActionType.CLONE) {
                ci.cancel();
            }
        }
    }
}
