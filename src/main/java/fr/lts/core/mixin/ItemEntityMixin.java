package fr.lts.core.mixin;

import fr.lts.core.restriction.BannedItems;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bloque le ramassage au sol des items bannis.
 *
 * <p>Quand un joueur marche sur un item au sol (coffre cassé, drop), si
 * l'item est banni (Notch Apple, Shield, livre enchanté banni, item avec
 * enchant banni), le ramassage est annulé. L'item reste au sol.</p>
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Shadow
    public abstract ItemStack getStack();

    /**
     * Intercepte {@link ItemEntity#onPlayerCollision(PlayerEntity)}.
     * Si l'item est banni, on annule le pickup.
     */
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void lts$onPlayerCollision(PlayerEntity player, CallbackInfo ci) {
        ItemStack stack = this.getStack();
        if (BannedItems.isItemStackBanned(stack)) {
            ci.cancel();
        }
    }
}
