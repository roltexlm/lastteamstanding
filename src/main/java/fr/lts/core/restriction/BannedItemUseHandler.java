package fr.lts.core.restriction;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.TridentItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Map;

/**
 * Bloque précisément les usages interdits :
 *
 * <ul>
 *   <li>Manger/utiliser une Notch Apple ou un Shield (clic droit).</li>
 *   <li>Attaquer avec une épée/hache/trident qui a un enchantement banni.</li>
 *   <li>Tirer avec un arc/crossbow/trident qui a un enchantement banni.</li>
 * </ul>
 *
 * <p>Les enchantements restent sur les items (probabilités vanilla intactes),
 * mais le joueur ne peut pas les utiliser s'ils sont illégaux.</p>
 */
public final class BannedItemUseHandler {

    private BannedItemUseHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register(BannedItemUseHandler::onUseItem);
        AttackEntityCallback.EVENT.register(BannedItemUseHandler::onAttackEntity);
    }

    /**
     * Bloque le clic droit sur :
     * - Notch Apple (manger)
     * - Shield (bloquer)
     * - Arc/crossbow/trident avec enchant banni (tirer)
     */
    private static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Items totalement bannis (Notch Apple, Shield).
        if (isBannedItem(stack)) {
            return TypedActionResult.fail(stack);
        }

        // Armes à distance avec enchant banni.
        if (hasBannedEnchantAndIsRangedWeapon(stack)) {
            return TypedActionResult.fail(stack);
        }

        return TypedActionResult.pass(stack);
    }

    /**
     * Bloque l'attaque (clic gauche) avec une arme de mêlée qui a un
     * enchantement banni (épée, hache, trident).
     */
    private static net.minecraft.util.ActionResult onAttackEntity(
            PlayerEntity player, World world, Hand hand,
            net.minecraft.entity.Entity entity, EntityHitResult hitResult) {
        ItemStack stack = player.getStackInHand(hand);
        if (hasBannedEnchant(stack)) {
            return net.minecraft.util.ActionResult.FAIL;
        }
        return net.minecraft.util.ActionResult.PASS;
    }

    // ----- Utilitaires -----

    private static boolean isBannedItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registry.ITEM.getId(stack.getItem());
        return BannedItems.isItemBanned(id);
    }

    private static boolean hasBannedEnchant(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Map<net.minecraft.enchantment.Enchantment, Integer> enchants =
            EnchantmentHelper.get(stack);
        for (Map.Entry<net.minecraft.enchantment.Enchantment, Integer> e : enchants.entrySet()) {
            if (BannedItems.isEnchantmentBanned(e.getKey(), e.getValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si l'item est une arme à distance (arc, crossbow, trident) ET
     * a un enchantement banni.
     */
    private static boolean hasBannedEnchantAndIsRangedWeapon(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof RangedWeaponItem)
            && !(stack.getItem() instanceof BowItem)
            && !(stack.getItem() instanceof CrossbowItem)
            && !(stack.getItem() instanceof TridentItem)) {
            return false;
        }
        return hasBannedEnchant(stack);
    }
}
