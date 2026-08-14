package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameService;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

/**
 * Vérifie périodiquement que les joueurs ne portent pas d'armure avec un
 * enchantement banni.
 *
 * <p>Si une armure équipée a un enchantement banni, elle est retirée du slot
 * d'armure et remise dans l'inventaire du joueur (si plein, jetée au sol).</p>
 *
 * <p>L'enchantement reste sur l'item (visible), mais ne peut pas être porté.</p>
 */
public final class ArmorEnchantChecker {

    private static final int CHECK_INTERVAL = 20; // 1 seconde
    private static int tickCounter = 0;

    private ArmorEnchantChecker() {
    }

    public static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        GameService game = LtsState.getGameService();
        if (game.getState().getPhase() != GamePhase.RUNNING) {
            return;
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            checkArmor(player);
        }
    }

    private static void checkArmor(ServerPlayerEntity player) {
        // Utilise getEquippedStack pour accéder aux 4 slots d'armure.
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        }) {
            ItemStack armor = player.getEquippedStack(slot);
            if (armor.isEmpty() || !armor.hasEnchantments()) {
                continue;
            }
            if (hasBannedEnchant(armor)) {
                // Retire l'armure du slot.
                player.equipStack(slot, ItemStack.EMPTY);
                // Remet dans l'inventaire (ou jette au sol si plein).
                PlayerInventory inv = player.getInventory();
                if (!inv.insertStack(armor)) {
                    player.dropItem(armor, false);
                }
                inv.markDirty();
                player.currentScreenHandler.sendContentUpdates();
            }
        }
    }

    private static boolean hasBannedEnchant(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        for (Map.Entry<Enchantment, Integer> e : enchants.entrySet()) {
            if (BannedItems.isEnchantmentBanned(e.getKey(), e.getValue())) {
                return true;
            }
        }
        return false;
    }
}
