package fr.lts.core.team;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère l'affichage des équipes via les scoreboard teams vanilla.
 *
 * <p>Quand un joueur est assigné à une team LTS, il est aussi placé dans la
 * scoreboard team vanilla correspondante avec la couleur de l'équipe. Cela
 * gère automatiquement :</p>
 * <ul>
 *   <li>Le nametag au-dessus du joueur (couleur du pseudo)</li>
 *   <li>Le TAB (couleur du pseudo dans la liste des joueurs)</li>
 *   <li>Le chat (couleur du pseudo dans les messages)</li>
 * </ul>
 *
 * <p>Pour la tête de skin 2D devant le pseudo, il faudra un rendu custom côté
 * client (mixin sur EntityRenderer.renderLabelIfPresent) — à implémenter
 * plus tard.</p>
 */
public final class TeamUIHandler {

    /** Préfixe des noms de scoreboard teams LTS. */
    private static final String TEAM_PREFIX = "lts_";

    /** Map des couleurs TeamColor -> Formatting Minecraft. */
    private static final Map<TeamColor, Formatting> COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(TeamColor.ORANGE, Formatting.GOLD);
        COLOR_MAP.put(TeamColor.BLEU_CIEL, Formatting.AQUA);
        COLOR_MAP.put(TeamColor.VERT_MENTHE, Formatting.GREEN);
        COLOR_MAP.put(TeamColor.JAUNE, Formatting.YELLOW);
        COLOR_MAP.put(TeamColor.BLEU, Formatting.BLUE);
        COLOR_MAP.put(TeamColor.VERMILLON, Formatting.GOLD);
        COLOR_MAP.put(TeamColor.ROSE, Formatting.LIGHT_PURPLE);
        COLOR_MAP.put(TeamColor.BORDEAUX, Formatting.DARK_RED);
        COLOR_MAP.put(TeamColor.BLEU_TOL, Formatting.BLUE);
        COLOR_MAP.put(TeamColor.CYAN_TOL, Formatting.AQUA);
        COLOR_MAP.put(TeamColor.VERT_TOL, Formatting.GREEN);
        COLOR_MAP.put(TeamColor.JAUNE_TOL, Formatting.YELLOW);
        COLOR_MAP.put(TeamColor.ROUGE_SOMBRE, Formatting.RED);
        COLOR_MAP.put(TeamColor.VIOLET, Formatting.DARK_PURPLE);
        COLOR_MAP.put(TeamColor.GRIS_TOL, Formatting.GRAY);
        COLOR_MAP.put(TeamColor.MAGENTA_VIBRANT, Formatting.GOLD);
        COLOR_MAP.put(TeamColor.BLEU_CLAIR_VIBRANT, Formatting.AQUA);
        COLOR_MAP.put(TeamColor.VERT_VIBRANT, Formatting.DARK_GREEN);
        COLOR_MAP.put(TeamColor.JAUNE_CITRON, Formatting.GREEN);
        COLOR_MAP.put(TeamColor.BLEU_NUIT, Formatting.DARK_BLUE);
        COLOR_MAP.put(TeamColor.ROSE_POURPRE, Formatting.RED);
        COLOR_MAP.put(TeamColor.MAUVE, Formatting.YELLOW);
        COLOR_MAP.put(TeamColor.INDIGO, Formatting.DARK_BLUE);
        COLOR_MAP.put(TeamColor.VERT_D_EAU, Formatting.DARK_AQUA);
    }

    private TeamUIHandler() {
    }

    /**
     * Assigne un joueur à la scoreboard team correspondant à sa team LTS.
     *
     * @param server le serveur.
     * @param player le joueur.
     * @param color  la couleur de sa team LTS.
     */
    public static void assignToScoreboardTeam(MinecraftServer server,
                                               ServerPlayerEntity player,
                                               TeamColor color) {
        Scoreboard scoreboard = server.getScoreboard();
        String teamName = TEAM_PREFIX + color.name();

        // Crée la scoreboard team si elle n'existe pas.
        Team scoreboardTeam = scoreboard.getTeam(teamName);
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.addTeam(teamName);
            // Ne pas set la couleur de la scoreboard team : le mixin
            // PlayerDisplayNameMixin s'occupe d'afficher la vraie couleur
            // hex cote client. Si on set une couleur Formatting ici, elle
            // ecrase la couleur hex du getDisplayName.
            scoreboardTeam.setColor(Formatting.WHITE);
            // Affiche le nametag toujours (même à travers les murs).
            scoreboardTeam.setShowFriendlyInvisibles(false);
            scoreboardTeam.setFriendlyFireAllowed(false);
        }

        // Retire le joueur de son ancienne team.
        scoreboard.clearPlayerTeam(player.getEntityName());

        // Ajoute le joueur à la nouvelle team.
        scoreboard.addPlayerToTeam(player.getEntityName(), scoreboardTeam);
    }

    /**
     * Retire un joueur de sa scoreboard team LTS.
     */
    public static void removeFromScoreboardTeam(MinecraftServer server,
                                                 ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        scoreboard.clearPlayerTeam(player.getEntityName());
    }

    /**
     * Nettoie toutes les scoreboard teams LTS (au /lts stop).
     */
    public static void clearAllScoreboardTeams(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        // Récupère une copie des noms de teams pour éviter la
        // ConcurrentModificationException.
        java.util.List<String> teamNames = new java.util.ArrayList<>();
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith(TEAM_PREFIX)) {
                teamNames.add(team.getName());
            }
        }
        for (String name : teamNames) {
            scoreboard.removeTeam(scoreboard.getTeam(name));
        }
    }
}
