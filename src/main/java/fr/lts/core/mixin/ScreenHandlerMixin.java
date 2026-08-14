package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepte les clics dans les containers (coffres, tables de craft, etc.)
 * pour empêcher le joueur de récupérer un item banni.
 *
 * <p>L'item reste visible dans le slot (avec son fond normal, le fond rouge
 * sera ajouté plus tard côté client), mais le joueur ne peut pas le prendre
 * dans son inventaire.</p>
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    /**
     * Intercepte {@link ScreenHandler#onSlotClick(int, int, SlotActionType, PlayerEntity)}.
     *
     * <p>Si le slot cliqué contient un item banni et que l'action consiste à
     * récupérer l'item (PICKUP, PICKUP_ALL, QUICK_MOVE, SWAP), on annule le
     * clic en ne faisant rien.</p>
     */
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void lts$onSlotClick(int slotIndex, int button, SlotActionType actionType,
                                  PlayerEntity player, CallbackInfo ci) {
        if (slotIndex < 0) {
            return;
        }

        ScreenHandler self = (ScreenHandler) (Object) this;
        if (slotIndex >= self.slots.size()) {
            return;
        }

        Slot slot = self.getSlot(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) {
            return;
        }

        Identifier itemId = Registry.ITEM.getId(stack.getItem());
        if (!BannedItems.isItemBanned(itemId)) {
            return;
        }

        // L'item dans le slot est banni. On bloque les actions qui permettent
        // de récupérer l'item dans l'inventaire du joueur.
        switch (actionType) {
            case PICKUP:       // clic normal (prendre/poser)
            case PICKUP_ALL:   // double-clic pour récupérer tous les items similaires
            case QUICK_MOVE:   // shift+clic (transfert rapide)
            case SWAP:         // touche numérique (1-9)
            case CLONE:        // molette en créatif
                // Si le joueur a le curseur vide (essaie de prendre l'item),
                // on bloque. S'il a un item en main et essaie de poser, on
                // laisse faire (le slot reste banni).
                if (player.getInventory().getMainHandStack().isEmpty()
                        && player.currentScreenHandler.getCursorStack().isEmpty()) {
                    ci.cancel();
                    return;
                }
                // Shift+click depuis l'inventaire du joueur vers le container :
                // on laisse faire (ne concerne pas l'item banni).
                break;
            case THROW:        // clic droit + touche Q (jeter)
                // On laisse le joueur jeter l'item (ça le retire du slot).
                break;
            default:
                break;
        }
    }
}
