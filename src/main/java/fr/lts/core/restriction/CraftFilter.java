package fr.lts.core.restriction;

import fr.lts.core.LtsCore;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtre les recettes de craft au chargement pour retirer celles qui produisent
 * un item banni (Notch Apple, Shield).
 *
 * <p>Les recettes bannies ne sont pas retirées du RecipeManager directement
 * (l'API 1.17.1 ne le permet pas proprement au runtime). À la place, on
 * interceptera le résultat du craft au moment de la récupération par le joueur
 * (côté serveur, via un event de clic dans la table de craft).</p>
 *
 * <p>Pour l'instant, cette classe sert de référence des recettes à bloquer et
 * fournit une méthode utilitaire pour vérifier si le résultat d'une recette est
 * un item banni.</p>
 */
public final class CraftFilter {

    private static final Logger LOGGER = LtsCore.LOGGER;

    private CraftFilter() {
    }

    /**
     * Vérifie si une recette produit un item banni.
     */
    public static boolean isRecipeBanned(Recipe<?> recipe) {
        if (recipe == null || recipe.getOutput() == null) {
            return false;
        }
        Identifier itemId = Registry.ITEM.getId(recipe.getOutput().getItem());
        return BannedItems.isItemBanned(itemId);
    }
}
