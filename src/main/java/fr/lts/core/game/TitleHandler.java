package fr.lts.core.game;

import fr.lts.core.team.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.UUID;

/**
 * Affiche des titles (texte plein écran) aux joueurs à des moments clés :
 *
 * <ul>
 *   <li>Au /lts tp : "La partie commence dans quelques instants"</li>
 *   <li>Au /lts start : "Bonne chance !"</li>
 *   <li>À la fin : équipe gagnante + joueurs vivants/morts</li>
 * </ul>
 */
public final class TitleHandler {

    private TitleHandler() {
    }

    /**
     * Affiche un title à tous les joueurs connectés.
     *
     * @param server    le serveur.
     * @param title     le titre principal (peut être null).
     * @param subtitle  le sous-titre (peut être null).
     * @param fadeIn    durée d'apparition en ticks.
     * @param stay      durée d'affichage en ticks.
     * @param fadeOut   durée de disparition en ticks.
     */
    public static void showTitle(MinecraftServer server, Text title, Text subtitle,
                                  int fadeIn, int stay, int fadeOut) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Times (fade in, stay, fade out)
            player.networkHandler.sendPacket(new TitleS2CPacket(fadeIn, stay, fadeOut));
            // Title
            if (title != null) {
                player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
            }
            // Subtitle
            if (subtitle != null) {
                player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subtitle));
            }
        }
    }

    /**
     * Title au moment du /lts tp.
     */
    public static void showTpTitle(MinecraftServer server) {
        showTitle(server,
            new LiteralText("La partie commence dans quelques instants")
                .formatted(Formatting.GOLD),
            null,
            10, 100, 20);
    }

    /**
     * Title au moment du /lts start.
     */
    public static void showStartTitle(MinecraftServer server) {
        showTitle(server,
            new LiteralText("Bonne chance !").formatted(Formatting.GREEN),
            null,
            10, 60, 20);
    }

    /**
     * Title de fin de partie avec l'équipe gagnante.
     *
     * @param server     le serveur.
     * @param winner     la team gagnante (peut être null si ex aequo).
     * @param isExAequo  true si ex aequo (plusieurs teams gagnantes).
     */
    public static void showVictoryTitle(MinecraftServer server, Team winner, boolean isExAequo) {
        if (winner == null) {
            showTitle(server,
                new LiteralText("Fin de partie").formatted(Formatting.WHITE),
                new LiteralText("Ex aequo !").formatted(Formatting.YELLOW),
                20, 200, 20);
            return;
        }

        // Titre : "Victoire de la team <couleur>"
        Formatting color = TeamUIHandler.getFormattingForColor(winner.getColor());
        Text title = new LiteralText("Victoire de la team ")
            .append(new LiteralText(winner.getColor().getDisplayName()).formatted(color));

        // Sous-titre : liste des joueurs vivants et morts
        LiteralText subtitle = new LiteralText("");
        boolean first = true;
        for (UUID playerId : winner.getMembers()) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerId);
            String name = player != null ? player.getEntityName() : "???";
            boolean alive = player != null && !player.isSpectator();

            if (!first) {
                subtitle.append(new LiteralText(", ").formatted(Formatting.GRAY));
            }
            first = false;

            if (alive) {
                subtitle.append(new LiteralText(name).formatted(color));
            } else {
                subtitle.append(new LiteralText("§m" + name + "§r").formatted(Formatting.GRAY));
            }
        }

        if (isExAequo) {
            subtitle.append(new LiteralText(" (ex aequo)").formatted(Formatting.YELLOW));
        }

        showTitle(server, title, subtitle, 20, 300, 20);
    }
}
