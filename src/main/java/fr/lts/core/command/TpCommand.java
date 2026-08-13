package fr.lts.core.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.lts.core.LtsState;
import fr.lts.core.game.GameService;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Commande {@code /lts tp}.
 *
 * <p>Téléporte les teams actives sur la map et stune les joueurs. Si aucune
 * team n'est active, la première exécution renvoie une erreur et arme un flag
 * "forcer au prochain appel" ; la seconde exécution (consécutive) force le tp
 * même sans team active (au cas où l'opérateur veut quand même lancer).</p>
 */
public final class TpCommand {

    private TpCommand() {
    }

    public static LiteralCommandNode<ServerCommandSource> register() {
        return literal("tp")
            .executes(TpCommand::execute)
            .build();
    }

    private static int execute(com.mojang.brigadier.context.CommandContext<ServerCommandSource> ctx) {
        GameService game = LtsState.getGameService();
        boolean force = game.getState().isTpForceNext();

        GameService.TpResult result = game.teleportTeams(ctx.getSource().getServer(), force);

        if (result.success) {
            ctx.getSource().sendFeedback(new LiteralText(result.message), true);
            game.getState().setTpForceNext(false);
        } else {
            // Si la cause est l'absence de team active, armer le flag pour
            // forcer au prochain appel. On détecte via le message, ou on
            // délègue : la 1re fois sans team => on arme le flag.
            if (!force && result.message.contains("Aucune team active")) {
                game.getState().setTpForceNext(true);
            } else {
                // Reset du flag si on était en force et qu'il n'y a quand même
                // rien à faire, pour éviter un blocage.
                game.getState().setTpForceNext(false);
            }
            ctx.getSource().sendError(new LiteralText(result.message));
        }
        return result.success ? 1 : 0;
    }
}
