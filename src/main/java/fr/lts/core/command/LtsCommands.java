package fr.lts.core.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Enregistrement de toutes les commandes {@code /lts ...}.
 *
 * <p>Structure :</p>
 * <pre>
 * /lts team size &lt;1|2|3|4&gt;
 * /lts team assign &lt;player&gt; &lt;color&gt;
 * /lts team random [players...]
 * /lts team list
 * /lts team clear
 * /lts team remove &lt;player&gt;
 * /lts tp
 * /lts start
 * /lts stop
 * /lts hardcore easy|vanilla
 * </pre>
 *
 * <p>Toutes les commandes sont réservées aux OP (permission level 2). Aucune
 * commande n'est exposée aux joueurs non-OP : l'HUD (timer + kills) est le
 * seul canal vers eux.</p>
 */
public final class LtsCommands {

    private LtsCommands() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register(
            literal("lts")
                .requires(src -> src.hasPermissionLevel(2))
                .then(TeamCommands.register())
                .then(TpCommand.register())
                .then(StartCommand.register())
                .then(StopCommand.register())
                .then(HardcoreCommand.register())
        );
    }
}
