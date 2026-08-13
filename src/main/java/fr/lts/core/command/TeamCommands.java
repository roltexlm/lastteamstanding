package fr.lts.core.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.lts.core.LtsState;
import fr.lts.core.team.Team;
import fr.lts.core.team.TeamColor;
import fr.lts.core.team.TeamService;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Sous-commandes {@code /lts team ...} : size, assign, random, list, clear,
 * remove.
 *
 * <p>{@code team size} ne contraint que le random ; l'assignation manuelle
 * ignore la taille et autorise des teams à effectif supérieur.</p>
 */
public final class TeamCommands {

    private static final SimpleCommandExceptionType UNKNOWN_COLOR =
        new SimpleCommandExceptionType(new LiteralText("Couleur inconnue."));

    private TeamCommands() {
    }

    public static com.mojang.brigadier.tree.LiteralCommandNode<ServerCommandSource> register() {
        return literal("team")
            .then(literal("size")
                .then(argument("size", IntegerArgumentType.integer(1, 4))
                    .executes(TeamCommands::setSize)))
            .then(literal("assign")
                .then(argument("player", EntityArgumentType.player())
                    .then(argument("color", StringArgumentType.word())
                        .executes(TeamCommands::assign))))
            .then(literal("random")
                .executes(ctx -> randomAssign(ctx, null))
                .then(argument("players", EntityArgumentType.players())
                    .executes(ctx -> randomAssign(ctx, new ArrayList<>(EntityArgumentType.getPlayers(ctx, "players"))))))
            .then(literal("list")
                .executes(TeamCommands::list))
            .then(literal("clear")
                .executes(TeamCommands::clear))
            .then(literal("remove")
                .then(argument("player", EntityArgumentType.player())
                    .executes(TeamCommands::remove)))
            .build();
    }

    // ----- size -----

    private static int setSize(CommandContext<ServerCommandSource> ctx) {
        int size = IntegerArgumentType.getInteger(ctx, "size");
        LtsState.getTeamService().setTeamSize(size);
        ctx.getSource().sendFeedback(
            new LiteralText("Taille d'équipe (random) définie à " + size + "."), false);
        return 1;
    }

    // ----- assign -----

    private static int assign(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        String colorName = StringArgumentType.getString(ctx, "color");

        TeamService ts = LtsState.getTeamService();
        TeamColor color = TeamColor.byName(colorName).orElse(null);
        if (color == null) {
            throw UNKNOWN_COLOR.create();
        }

        ts.assign(target.getUuid(), color);
        Team team = ts.getTeam(color);

        Formatting fmt = formatFor(color);
        LiteralText line = new LiteralText(target.getEntityName() + " assigné à la team ");
        line.append(new LiteralText(color.getDisplayName()).formatted(fmt));
        line.append(new LiteralText(" (" + team.size() + " membres)."));
        ctx.getSource().sendFeedback(line, false);
        return 1;
    }

    // ----- random -----

    private static int randomAssign(CommandContext<ServerCommandSource> ctx, List<ServerPlayerEntity> players)
        throws CommandSyntaxException {
        TeamService ts = LtsState.getTeamService();

        List<UUID> ids;
        if (players == null) {
            // Tous les joueurs connectés.
            ids = new ArrayList<>();
            for (ServerPlayerEntity p : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                ids.add(p.getUuid());
            }
        } else {
            ids = new ArrayList<>();
            for (ServerPlayerEntity p : players) {
                ids.add(p.getUuid());
            }
        }

        TeamService.RandomAssignResult res = ts.randomAssign(ids);
        ctx.getSource().sendFeedback(
            new LiteralText("Random assign : " + res.totalPlayers + " joueurs répartis dans "
                + res.teamsUsed + " teams (taille " + ts.getTeamSize() + ")."),
            false);
        return 1;
    }

    // ----- list -----

    private static int list(CommandContext<ServerCommandSource> ctx) {
        TeamService ts = LtsState.getTeamService();
        List<Team> active = ts.getActiveTeams();

        if (active.isEmpty()) {
            ctx.getSource().sendFeedback(new LiteralText("Aucune team active."), false);
            return 0;
        }

        ctx.getSource().sendFeedback(
            new LiteralText("Teams actives (" + active.size() + ") :"), false);

        for (Team team : active) {
            Formatting fmt = formatFor(team.getColor());
            MutableText line = new LiteralText("  " + team.getColor().getDisplayName()).formatted(fmt);
            line.append(new LiteralText(" : " + team.size() + " membres").formatted(Formatting.WHITE));
            ctx.getSource().sendFeedback(line, false);
        }
        return active.size();
    }

    // ----- clear -----

    private static int clear(CommandContext<ServerCommandSource> ctx) {
        LtsState.getTeamService().reset();
        ctx.getSource().sendFeedback(
            new LiteralText("Toutes les teams ont été réinitialisées."), false);
        return 1;
    }

    // ----- remove -----

    private static int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "player");
        boolean removed = LtsState.getTeamService().remove(target.getUuid());
        if (removed) {
            ctx.getSource().sendFeedback(
                new LiteralText(target.getEntityName() + " retiré de sa team."), false);
            return 1;
        }
        ctx.getSource().sendError(new LiteralText(target.getEntityName() + " n'était dans aucune team."));
        return 0;
    }

    // ----- Utilitaires -----

    /**
     * Tente de mapper une TeamColor vers un Formatting Minecraft pour le rendu
     * en chat/nametag. Les 24 couleurs n'ont pas toutes d'équivalent direct
     * dans la palette Formatting (16 couleurs), donc on retombe sur WHITE ou
     * la plus proche. Ce mapping est approximatif ; le rendu final se fera via
     * l'hex custom côté client (UI à implémenter).
     */
    private static Formatting formatFor(TeamColor color) {
        switch (color) {
            case ORANGE:
            case VERMILLON:
            case MAGENTA_VIBRANT:
                return Formatting.GOLD;
            case BLEU_CIEL:
            case CYAN_TOL:
            case BLEU_CLAIR_VIBRANT:
                return Formatting.AQUA;
            case VERT_MENTHE:
            case VERT_TOL:
            case VERT_VIBRANT:
            case VERT_D_EAU:
            case JAUNE_CITRON:
                return Formatting.GREEN;
            case JAUNE:
            case JAUNE_TOL:
                return Formatting.YELLOW;
            case BLEU:
            case BLEU_TOL:
            case BLEU_NUIT:
            case INDIGO:
                return Formatting.BLUE;
            case ROSE:
            case ROUGE_SOMBRE:
            case ROSE_POURPRE:
                return Formatting.RED;
            case VIOLET:
                return Formatting.LIGHT_PURPLE;
            case GRIS_TOL:
                return Formatting.GRAY;
            case BORDEAUX:
            case MAUVE:
            default:
                return Formatting.WHITE;
        }
    }
}
