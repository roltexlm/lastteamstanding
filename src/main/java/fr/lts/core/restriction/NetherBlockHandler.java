package fr.lts.core.restriction;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Bloque l accès au Nether pendant une partie LTS.
 *
 * <p>Si un joueur arrive dans le Nether pendant RUNNING/PLACEMENT, il est
 * téléporté en retour vers l overworld (au spawn par défaut).</p>
 */
public final class NetherBlockHandler {

    private NetherBlockHandler() {
    }

    public static void register() {
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
            NetherBlockHandler::onPlayerChangeWorld);
    }

    private static void onPlayerChangeWorld(ServerPlayerEntity player,
                                            ServerWorld origin,
                                            ServerWorld destination) {
        try {
            GameState state = LtsState.getGameService().getState();
            if (state.getPhase() != GamePhase.RUNNING && state.getPhase() != GamePhase.PLACEMENT) {
                return;
            }
            // Si le joueur arrive dans le Nether, le renvoyer a l overworld.
            if (destination.getRegistryKey() == World.NETHER) {
                ServerWorld overworld = player.server.getOverworld();
                if (overworld != null) {
                    player.teleport(overworld, player.getX(), player.getY(), player.getZ(),
                        player.getYaw(), player.getPitch());
                }
            }
        } catch (IllegalStateException e) {
            // LtsState non initialise : ignore.
        }
    }
}
