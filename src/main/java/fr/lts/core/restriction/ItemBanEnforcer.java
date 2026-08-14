package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameService;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * Applique le bannissement des items en scannant périodiquement les
 * inventaires des joueurs pendant une partie en cours.
 *
 * <p>Cette approche couvre tous les cas : loot de coffres, craft, trade,
 * pickup au sol. Si un joueur obtient un item banni (Notch Apple, Shield),
 * il est retiré de son inventaire à la prochaine vérification.</p>
 *
 * <p>Moins élégant qu'une interception fine (mixin), mais robuste et simple
 * pour la 1.17.1.</p>
 */
public final class ItemBanEnforcer {

    /** Intervalle de vérification en ticks (toutes les 2 secondes = 40 ticks). */
    private static final int CHECK_INTERVAL = 40;
    private static int tickCounter = 0;

    private ItemBanEnforcer() {
    }

    /**
     * À appeler à chaque tick serveur. Vérifie les inventaires toutes les
     * {@link #CHECK_INTERVAL} ticks pendant une partie en cours.
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
     * Scanne l'inventaire d'un joueur et retire les items bannis.
     */
    private static void scanInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        boolean removed = false;

        // Inventaire principal + hotbar.
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            Identifier itemId = Registry.ITEM.getId(stack.getItem());
            if (BannedItems.isItemBanned(itemId)) {
                inventory.setStack(i, ItemStack.EMPTY);
                removed = true;
            }
        }

        // Armure (slots 36 à 39 dans l'inventaire vanilla).
        for (int i = 0; i < 4; i++) {
            ItemStack armor = inventory.getStack(36 + i);
            if (armor.isEmpty()) {
                continue;
            }
            Identifier itemId = Registry.ITEM.getId(armor.getItem());
            if (BannedItems.isItemBanned(itemId)) {
                inventory.setStack(36 + i, ItemStack.EMPTY);
                removed = true;
            }
        }

        // Main secondaire (off-hand).
        ItemStack offHand = player.getOffHandStack();
        if (!offHand.isEmpty()) {
            Identifier itemId = Registry.ITEM.getId(offHand.getItem());
            if (BannedItems.isItemBanned(itemId)) {
                player.setStackInHand(net.minecraft.util.Hand.OFF_HAND, ItemStack.EMPTY);
                removed = true;
            }
        }

        if (removed) {
            player.getInventory().markDirty();
            // Resync le joueur.
            player.currentScreenHandler.sendContentUpdates();
        }
    }
}
