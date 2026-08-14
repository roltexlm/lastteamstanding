package fr.lts.core.restriction;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration des items et enchantements bannis du jeu.
 *
 * <p>Les items bannis sont retirés des loots et impossibles à crafter. Les
 * enchantements bannis sont filtrés de la table d'enchantement et de
 * l'enclume.</p>
 *
 * <h3>Items bannis</h3>
 * <ul>
 *   <li>Notch Apple (Enchanted Golden Apple)</li>
 *   <li>Shield</li>
 * </ul>
 *
 * <h3>Enchantements bannis</h3>
 * <ul>
 *   <li>Tous les enchantements au-dessus du niveau 3</li>
 *   <li>Fire Aspect (tous niveaux)</li>
 *   <li>Flame (tous niveaux)</li>
 *   <li>Channeling (tous niveaux)</li>
 *   <li>Thorns (tous niveaux)</li>
 *   <li>Infinity (tous niveaux)</li>
 *   <li>Knockback au-dessus du niveau 1</li>
 *   <li>Punch au-dessus du niveau 1</li>
 *   <li>Power au-dessus du niveau 1</li>
 *   <li>Quick Charge au-dessus du niveau 1</li>
 * </ul>
 */
public final class BannedItems {

    private BannedItems() {
    }

    // ----- Items bannis -----

    /**
     * Identifiants des items bannis (loot bloqué + craft bloqué).
     */
    public static final Set<Identifier> BANNED_ITEM_IDS = new HashSet<>(Arrays.asList(
        Registry.ITEM.getId(Items.ENCHANTED_GOLDEN_APPLE), // Notch Apple
        Registry.ITEM.getId(Items.SHIELD)
    ));

    /**
     * Vérifie si un item (par son Identifier) est banni.
     */
    public static boolean isItemBanned(Identifier itemId) {
        return BANNED_ITEM_IDS.contains(itemId);
    }

    // ----- Enchantements bannis -----

    /**
     * Enchantements totalement bannis (tous niveaux confondus).
     */
    public static final Set<Enchantment> FULLY_BANNED_ENCHANTS = new HashSet<>(Arrays.asList(
        Enchantments.FIRE_ASPECT,
        Enchantments.FLAME,
        Enchantments.CHANNELING,
        Enchantments.THORNS,
        Enchantments.INFINITY
    ));

    /**
     * Niveau maximum autorisé pour certains enchantements (au-dessus = banni).
     * Si un enchantement n'est pas dans cette map et n'est pas dans
     * {@link #FULLY_BANNED_ENCHANTS}, il n'y a pas de restriction de niveau.
     */
    public static final java.util.Map<Enchantment, Integer> MAX_LEVEL_ALLOWED = new java.util.HashMap<>();
    static {
        // Tous les enchantements : niveau max 3 (règle générale).
        // Les enchantements suivants ont une restriction plus stricte :
        MAX_LEVEL_ALLOWED.put(Enchantments.KNOCKBACK, 1);
        MAX_LEVEL_ALLOWED.put(Enchantments.PUNCH, 1);
        MAX_LEVEL_ALLOWED.put(Enchantments.POWER, 1);
        MAX_LEVEL_ALLOWED.put(Enchantments.QUICK_CHARGE, 1);
    }

    /** Niveau maximum général (pour les enchantements non listés individuellement). */
    public static final int GLOBAL_MAX_LEVEL = 3;

    /**
     * Vérifie si un enchantement à un niveau donné est banni.
     *
     * @param enchantment l'enchantement à vérifier.
     * @param level       le niveau appliqué.
     * @return {@code true} si l'enchantement est banni à ce niveau.
     */
    public static boolean isEnchantmentBanned(Enchantment enchantment, int level) {
        if (enchantment == null) {
            return false;
        }
        // Enchantements totalement bannis (tous niveaux).
        if (FULLY_BANNED_ENCHANTS.contains(enchantment)) {
            return true;
        }
        // Vérifie le niveau maximum autorisé.
        Integer maxLevel = MAX_LEVEL_ALLOWED.get(enchantment);
        if (maxLevel != null) {
            return level > maxLevel;
        }
        // Règle générale : niveau max 3.
        return level > GLOBAL_MAX_LEVEL;
    }

    /**
     * Vérifie si un enchantement est totalement banni (peu importe le niveau).
     */
    public static boolean isEnchantmentFullyBanned(Enchantment enchantment) {
        return FULLY_BANNED_ENCHANTS.contains(enchantment);
    }

    /**
     * Retourne le niveau maximum autorisé pour un enchantement, ou
     * {@link #GLOBAL_MAX_LEVEL} si pas de restriction spécifique.
     */
    public static int getMaxLevelAllowed(Enchantment enchantment) {
        Integer max = MAX_LEVEL_ALLOWED.get(enchantment);
        return max != null ? max : GLOBAL_MAX_LEVEL;
    }
}
