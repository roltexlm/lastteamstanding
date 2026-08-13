package fr.lts.core.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.context.CommandContext;
import fr.lts.core.LtsState;
import fr.lts.core.game.GameService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Commande {@code /lts start}.
 *
 * <p>Débloque les joueurs stunés (lève immobilité + invincibilité) et lance le
 * timer. Erreur si les joueurs n'ont pas été placés au préalable via
 * {@code /lts tp}.</p>
 */
public final class StartCommand {

    private StartCommand() {
    }

    public static LiteralCommandNode<ServerCommandSource> register() {
        return literal("start")
            .executes(StartCommand::execute)
            .build();
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        GameService game = LtsState.getGameService();
        GameService.StartResult result = game.start(ctx.getSource().getMinecraftServer());

        if (result.success) {
            ctx.getSource().sendFeedback(new LiteralText(result.message), true);
            return 1;
        }
        ctx.getSource().sendError(new LiteralText(result.message));
        return 0;
    }
}
