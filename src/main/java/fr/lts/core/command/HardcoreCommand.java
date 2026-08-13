package fr.lts.core.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.context.CommandContext;
import fr.lts.core.LtsState;
import fr.lts.core.game.GameService;
import fr.lts.core.game.HardcoreMode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Commande {@code /lts hardcore easy|vanilla}.
 *
 * <p>Bascule entre hardcore easy (one-life + difficulté easy, PVE peu hostile)
 * et hardcore vanilla (one-life + difficulté vanilla).</p>
 *
 * <p>La difficulté effective du monde est appliquée ailleurs (la commande ne
 * fait que stocker la préférence ; l'application au monde se fera dans une
 * étape ultérieure avec gestion de la one-life via ban-on-death spectateur).</p>
 */
public final class HardcoreCommand {

    private HardcoreCommand() {
    }

    public static LiteralCommandNode<ServerCommandSource> register() {
        return literal("hardcore")
            .then(literal("easy")
                .executes(ctx -> set(ctx, HardcoreMode.EASY)))
            .then(literal("vanilla")
                .executes(ctx -> set(ctx, HardcoreMode.VANILLA)))
            .build();
    }

    private static int set(CommandContext<ServerCommandSource> ctx, HardcoreMode mode) {
        GameService game = LtsState.getGameService();
        game.setHardcore(mode);
        ctx.getSource().sendFeedback(
            new LiteralText("Mode hardcore défini sur : " + mode.name().toLowerCase() + "."), false);
        return 1;
    }
}
