package fr.lts.core.mixin;

import fr.lts.core.LtsState;
import fr.lts.core.game.GamePhase;
import fr.lts.core.game.GameState;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Empêche le portail du Nether de fonctionner pendant une partie LTS.
 *
 * <p>Intercepte {@link NetherPortalBlock#onEntityCollision} et l annule si
 * la partie est en cours. Le portail reste visible mais ne téléporte pas
 * le joueur.</p>
 */
@Mixin(NetherPortalBlock.class)
public abstract class NetherPortalBlockMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void lts$onEntityCollision(BlockState state, World world, BlockPos pos,
                                        Entity entity, CallbackInfo ci) {
        if (world.isClient) {
            return;
        }
        try {
            GameState gameState = LtsState.getGameService().getState();
            if (gameState.getPhase() == GamePhase.RUNNING
                    || gameState.getPhase() == GamePhase.PLACEMENT) {
                ci.cancel();
            }
        } catch (IllegalStateException e) {
            // LtsState non initialise : ignore.
        }
    }
}
