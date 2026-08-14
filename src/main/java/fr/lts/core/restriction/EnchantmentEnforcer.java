package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameService;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;

/**
 * Applique le bannissement des enchantements en scannant périodiquement les
 * items des joueurs pendant une partie en cours.
 *
 * <p>Retire les enchantements bannis des items équipés/portés :
 * <ul>
 *   <li>Enchantements totalement bannis (Fire Aspect, Flame, Channeling,
 *       Thorns, Infinity) — retirés quel que soit le niveau.</li>
 *   <li>Enchantements au-dessus du niveau autorisé (ex: niveau max 3 général,
 *       Knockback/Punch/Power/Quick Charge max 1).</li>
 * </ul>
 *
 * <p>Couvre tous les cas : table d'enchantement, enclume, loot enchanté,
 * trade. Moins élégant qu'une interception fine mais robuste en 1.17.1 (pas
 * d'API Fabric pour les enchantements).</p>
 */
public final class EnchantmentEnforcer {

    /** Intervalle de vérification en ticks (toutes les 2 secondes). */
    private static final int CHECK_INTERVAL = 40;
    private static int tickCounter = 0;

    private EnchantmentEnforcer() {
    }

    /**
     * À appeler à chaque tick serveur.
     */
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
            scanInventory(player);
        }
    }

    /**
     * Scanne l'inventaire d'un joueur et retire les enchantements bannis de
     * chaque item.
     */
    private static void scanInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        boolean modified = false;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty() || !stack.hasEnchantments()) {
                continue;
            }
            if (cleanEnchantments(stack)) {
                modified = true;
            }
        }

        // Équipement (main principale, main secondaire, armure).
        for (net.minecraft.util.Hand hand : net.minecraft.util.Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (!stack.isEmpty() && stack.hasEnchantments()) {
                if (cleanEnchantments(stack)) {
                    modified = true;
                }
            }
        }

        for (int i = 0; i < 4; i++) {
            ItemStack armor = player.getInventory().getStack(36 + i);
            if (!armor.isEmpty() && armor.hasEnchantments()) {
                if (cleanEnchantments(armor)) {
                    modified = true;
                }
            }
        }

        if (modified) {
            inventory.markDirty();
            player.currentScreenHandler.sendContentUpdates();
        }
    }

    /**
     * Retire les enchantements bannis d'un ItemStack.
     *
     * @return {@code true} si au moins un enchantement a été retiré.
     */
    private static boolean cleanEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(stack);
        boolean removed = false;

        for (Map.Entry<Enchantment, Integer> entry : new java.util.HashMap<>(enchants).entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();
            if (BannedItems.isEnchantmentBanned(ench, level)) {
                enchants.remove(ench);
                removed = true;
            }
        }

        if (removed) {
            // Reconstruit les enchantements restants sur l'item.
            net.minecraft.nbt.NbtList enchantList = new net.minecraft.nbt.NbtList();
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                net.minecraft.nbt.NbtCompound tag = new net.minecraft.nbt.NbtCompound();
                tag.putString("id", net.minecraft.util.registry.Registry.ENCHANTMENT.getId(entry.getKey()).toString());
                tag.putShort("lvl", (short) (entry.getValue() & 0xFFFF));
                enchantList.add(tag);
            }
            net.minecraft.nbt.NbtCompound nbt = stack.getOrCreateNbt();
            if (enchantList.isEmpty()) {
                nbt.remove("Enchantments");
                if (nbt.isEmpty()) {
                    stack.setNbt(null);
                }
            } else {
                nbt.put("Enchantments", enchantList);
            }
        }

        return removed;
    }
}
