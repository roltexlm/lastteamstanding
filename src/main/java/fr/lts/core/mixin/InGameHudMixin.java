package fr.lts.core.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Force l'affichage des cœurs hardcore en permanence (texture y=45 au lieu
 * de y=0).
 *
 * <p>En vanilla, le client affiche les cœurs hardcore uniquement si le monde
 * est marqué {@code hardcore}. Ce mixin intercepte {@link InGameHud#drawHeart}
 * et remplace la coordonnée {@code v} (y dans la texture icons.png) par 45
 * (ligne des cœurs hardcore), peu importe le flag du monde.</p>
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    /**
     * Modifie le paramètre {@code v} (3e paramètre int après x, y) de
     * {@code drawHeart} pour forcer la texture hardcore (v=45).
     *
     * <p>La signature de drawHeart est :
     * {@code drawHeart(MatrixStack, HeartType, int x, int y, int v, boolean, boolean)}</p>
     */
    @ModifyVariable(method = "drawHeart", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static int lts$forceHardcoreHearts(int v) {
        return 45;
    }
}
