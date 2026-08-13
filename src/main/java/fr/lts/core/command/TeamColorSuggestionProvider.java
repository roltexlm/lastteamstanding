package fr.lts.core.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.lts.core.team.TeamColor;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

/**
 * Fournisseur de suggestions pour les noms de couleurs de team.
 *
 * <p>Permet l'autocomplétion (Tab) sur l'argument {@code <color>} des commandes
 * comme {@code /lts team assign <player> <color>}.</p>
 */
public final class TeamColorSuggestionProvider implements SuggestionProvider<ServerCommandSource> {

    public static final TeamColorSuggestionProvider INSTANCE = new TeamColorSuggestionProvider();

    private TeamColorSuggestionProvider() {
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context,
                                                          SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (TeamColor color : TeamColor.values()) {
            String name = color.getDisplayName();
            if (name.toLowerCase().startsWith(remaining)) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }
}
