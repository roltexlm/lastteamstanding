package fr.lts.core.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.context.CommandContext;
import fr.lts.core.LtsState;
import fr.lts.core.game.GameService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Commande {@code /lts stop}.
 *
 * <p>Arrête la partie : coupe l'affichage du title (TODO UI), retire les teams
 * des joueurs, les passe en spectateur, reset les options (team size,
 * assignations, hardcore, timer).</p>
 */
public final class StopCommand {

    private StopCommand() {
    }

    public static LiteralCommandNode<ServerCommandSource> register() {
        return literal("stop")
            .executes(StopCommand::execute)
            .build();
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        GameService game = LtsState.getGameService();
        GameService.StopResult result = game.stop(ctx.getSource().getServer());

        if (result.success) {
            ctx.getSource().sendFeedback(new LiteralText(result.message), true);
            return 1;
        }
        ctx.getSource().sendError(new LiteralText(result.message));
        return 0;
    }
}
