package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Retire périodiquement les evokers et horses du monde pendant une partie.
 *
 * <p>Couvre tous les cas : spawn naturel, génération de structure (manoirs),
 * /summon, oeufs de spawn. Les entités sont supprimées du monde.</p>
 *
 * <p>Le mixin EntitySpawnBlockMixin reste en place pour bloquer les spawns
 * artificiels (/summon, oeufs) immédiatement, mais ce handler retire aussi
 * les entités déjà présentes (ex: evokers dans les manoirs).</p>
 */
public final class MobCleanupHandler {

    private static final int CHECK_INTERVAL = 100; // 5 secondes
    private static int tickCounter = 0;

    private MobCleanupHandler() {
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(MobCleanupHandler::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return;
        }
        tickCounter = 0;

        GameState state;
        try {
            state = LtsState.getGameService().getState();
        } catch (IllegalStateException e) {
            return;
        }

        if (state.getPhase() != GamePhase.RUNNING && state.getPhase() != GamePhase.PLACEMENT) {
            return;
        }

        // Parcourt tous les mondes et retire les evokers et horses.
        for (ServerWorld world : server.getWorlds()) {
            for (Entity entity : world.getEntitiesByType(EntityType.EVOKER)) {
                entity.discard();
            }
            for (Entity entity : world.getEntitiesByType(EntityType.HORSE)) {
                entity.discard();
            }
        }
    }
}
