package fr.lts.core.restriction;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

/**
 * Bloque l'usage (clic droit) et l'attaque (clic gauche) des items bannis.
 *
 * <p>Covers :
 * <ul>
 *   <li>Clic droit sur un item banni (manger une Notch Apple, utiliser un Shield).</li>
 *   <li>Attaquer une entité avec un item qui a un enchantement banni
 *       (ex: épée avec Fire Aspect).</li>
 *   <li>Utiliser un livre enchanté banni.</li>
 * </ul>
 *
 * <p>Les items restent dans l'inventaire du joueur (s'ils y sont déjà), mais
 * ne peuvent pas être utilisés.</p>
 */
public final class BannedItemUseHandler {

    private BannedItemUseHandler() {
    }

    /**
     * Enregistre les callbacks d'usage et d'attaque.
     */
    public static void register() {
        // Bloque le clic droit sur un item banni.
        UseItemCallback.EVENT.register(BannedItemUseHandler::onUseItem);

        // Bloque l'attaque avec un item banni/enchanté illégalement.
        AttackEntityCallback.EVENT.register(BannedItemUseHandler::onAttackEntity);
    }

    private static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (BannedItems.isItemStackBanned(stack)) {
            // Annule l'usage : l'item ne peut pas être consommé/activé.
            return TypedActionResult.fail(stack);
        }
        return TypedActionResult.pass(stack);
    }

    private static net.minecraft.util.ActionResult onAttackEntity(
            PlayerEntity player, World world, Hand hand,
            net.minecraft.entity.Entity entity, EntityHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        if (BannedItems.isItemStackBanned(stack)) {
            // Annule l'attaque si l'item en main a un enchantement banni.
            return net.minecraft.util.ActionResult.FAIL;
        }
        return net.minecraft.util.ActionResult.PASS;
    }
}
