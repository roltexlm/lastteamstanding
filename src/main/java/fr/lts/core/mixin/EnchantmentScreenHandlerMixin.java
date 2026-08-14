package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Empêche d'appliquer un enchantement banni via la table d'enchantement.
 *
 * <p>Le joueur voit les enchantements proposés (probabilités vanilla
 * intactes), mais s'il clique sur un choix qui contient un enchantement
 * banni, l'action est annulée (retourne false, les niveaux et le lapis
 * ne sont pas consommés).</p>
 */
@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin {

    /**
     * Intercepte {@link EnchantmentScreenHandler#onButtonClick(PlayerEntity, int)}.
     * Si l'enchantement sélectionné (via l'index id) est banni, on annule.
     */
    @Inject(method = "onButtonClick", at = @At("HEAD"), cancellable = true)
    public void lts$onButtonClick(PlayerEntity player, int id,
                                   CallbackInfoReturnable<Boolean> cir) {
        EnchantmentScreenHandler self = (EnchantmentScreenHandler) (Object) this;
        int[] enchIds = self.enchantmentId;
        int[] enchLevels = self.enchantmentLevel;
        if (id < 0 || id >= enchIds.length) {
            return;
        }
        int enchRawId = enchIds[id];
        int enchLevel = enchLevels[id];
        if (enchRawId < 0) {
            return;
        }
        Enchantment enchantment = Registry.ENCHANTMENT.get(enchRawId);
        if (enchantment != null && BannedItems.isEnchantmentBanned(enchantment, enchLevel)) {
            cir.setReturnValue(false);
        }
    }
}
