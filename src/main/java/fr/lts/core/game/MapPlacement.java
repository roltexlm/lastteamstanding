package fr.lts.core.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Calcul de la taille de map proportionnelle au nombre de joueurs, et
 * placement aléatoire étalé et cohérent des teams sur la map.
 *
 * <p>Règles :</p>
 * <ul>
 *   <li>Taille de map de référence : 1008×1008 blocs pour 24 joueurs (soit
 *       42 blocs par joueur sur un côté, ou encore une map dont la surface
 *       vaut {@code 1008^2 / 24 = 42 336} blocs² par joueur).</li>
 *   <li>La taille de map est proportionnelle au nombre de joueurs (en
 *       surface), pour garder une densité cohérente peu importe le remplissage
 *       de la partie.</li>
 *   <li>Les teams sont placées sur la map de façon étalée : on découpe la map
 *       en {@code nTeams} zones égales (grille la plus carrée possible) et on
 *       place une team au centre de chaque zone avec un petit offset aléatoire.
 *       Tous les membres d'une team sont sur le même bloc.</li>
 * </ul>
 */
public final class MapPlacement {

    /** Taille de map de référence (côté en blocs) pour 24 joueurs. */
    public static final int REFERENCE_MAP_SIZE = 1008;

    /** Nombre de joueurs de référence. */
    public static final int REFERENCE_PLAYER_COUNT = 24;

    private MapPlacement() {
    }

    /**
     * Calcule la taille de map (côté en blocs) proportionnelle au nombre de
     * joueurs.
     *
     * <p>On conserve la densité de joueurs de la référence : la surface est
     * proportionnelle au nombre de joueurs. Le côté vaut donc
     * {@code referenceSize * sqrt(playerCount / referencePlayerCount)}.</p>
     *
     * <p>Arrondi au multiple de 16 le plus proche (alignement chunks), avec un
     * minimum de 64 blocs pour rester jouable même à très peu de joueurs.</p>
     *
     * @param playerCount nombre de joueurs concernés (actifs).
     */
    public static int computeMapSize(int playerCount) {
        if (playerCount <= 0) {
            return REFERENCE_MAP_SIZE;
        }
        double ratio = (double) playerCount / REFERENCE_PLAYER_COUNT;
        double side = REFERENCE_MAP_SIZE * Math.sqrt(ratio);
        int rounded = (int) Math.round(side / 16.0) * 16;
        return Math.max(64, rounded);
    }

    /**
     * Calcule les positions de spawn de chaque team, étalées sur la map.
     *
     * <p>La map est centrée sur l'origine (0,0). On découpe la map en une
     * grille de {@code cols × rows} cellules (la grille la plus carrée possible
     * pour {@code nTeams} cellules) et on place chaque team au centre de sa
     * cellule, avec un petit offset aléatoire pour éviter un placement trop
     * rigide. Toutes les positions sont dans le demi-plan y >= 64 (surface).</p>
     *
     * @param nTeams   nombre de teams à placer.
     * @param mapSize  côté de la map en blocs.
     * @param random   source d'aléatoire.
     * @return liste de {@code nTeams} positions [x, z], dans l'ordre (team 0
     *         ... team n-1).
     */
    public static List<int[]> computeTeamSpawnPoints(int nTeams, int mapSize, Random random) {
        List<int[]> points = new ArrayList<>(nTeams);
        if (nTeams <= 0) {
            return points;
        }

        int[] dims = bestGridDimensions(nTeams);
        int cols = dims[0];
        int rows = dims[1];

        double half = mapSize / 2.0;
        double cellWidth = (double) mapSize / cols;
        double cellHeight = (double) mapSize / rows;

        // Offset aléatoire max : 15% de la taille de cellule, pour rester dans
        // la cellule tout en cassant la régularité.
        double jitterRatio = 0.15;

        int idx = 0;
        for (int r = 0; r < rows && idx < nTeams; r++) {
            for (int c = 0; c < cols && idx < nTeams; c++) {
                double centerX = -half + (c + 0.5) * cellWidth;
                double centerZ = -half + (r + 0.5) * cellHeight;

                double jitterX = (random.nextDouble() * 2 - 1) * cellWidth * jitterRatio;
                double jitterZ = (random.nextDouble() * 2 - 1) * cellHeight * jitterRatio;

                int x = (int) Math.round(centerX + jitterX);
                int z = (int) Math.round(centerZ + jitterZ);
                points.add(new int[]{x, z});
                idx++;
            }
        }
        return points;
    }

    /**
     * Calcule les dimensions (cols, rows) de la grille la plus carrée possible
     * pouvant contenir au moins {@code nTeams} cellules.
     *
     * <p>On cherche le couple (cols, rows) avec cols * rows >= nTeams et cols,
     * rows aussi proches que possible, en privilégiant cols >= rows (map plutôt
     * large).</p>
     */
    static int[] bestGridDimensions(int nTeams) {
        if (nTeams <= 0) {
            return new int[]{1, 1};
        }
        int side = (int) Math.ceil(Math.sqrt(nTeams));
        int cols = side;
        int rows = (int) Math.ceil((double) nTeams / cols);
        // Garantir cols * rows >= nTeams (déjà vrai par construction).
        while (cols * rows < nTeams) {
            cols++;
        }
        return new int[]{cols, rows};
    }
}
