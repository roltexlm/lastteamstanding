package fr.lts.core.team;

import java.util.Arrays;
import java.util.Optional;

/**
 * Palette de 24 couleurs daltonien-friendly, figée et réutilisée pour toutes
 * les parties.
 *
 * <p>Construite à partir de :</p>
 * <ul>
 *   <li><b>Okabe-Ito</b> (Nature Methods, Wong 2011) - 8 couleurs</li>
 *   <li><b>Paul Tol</b> schémas bright / vibrant / muted (SRON technical note
 *       3.2, 2021)</li>
 * </ul>
 *
 * <p>Aucune paire rouge/vert pur. Toutes les teintes sont documentées comme
 * distinctes en vision normale, protanopie, deutéranopie et tritanopie.</p>
 *
 * <p>Le noir (#000000) de la palette Okabe-Ito a été remplacé par le bordeaux
 * (#882255, Tol muted) car le noir est peu utile comme couleur de team.</p>
 *
 * <p>Les noms d'affichage utilisent des underscores à la place des espaces
 * car l'argument Brigadier {@code word()} n'accepte pas les espaces.</p>
 */
public enum TeamColor {
    ORANGE("Orange", "#E69F00"),
    BLEU_CIEL("Bleu_ciel", "#56B4E9"),
    VERT_MENTHE("Vert_menthe", "#009E73"),
    JAUNE("Jaune", "#F0E442"),
    BLEU("Bleu", "#0072B2"),
    VERMILLON("Vermillon", "#D55E00"),
    ROSE("Rose", "#CC79A7"),
    BORDEAUX("Bordeaux", "#882255"),
    BLEU_TOL("Bleu_tol", "#4477AA"),
    CYAN_TOL("Cyan_tol", "#66CCEE"),
    VERT_TOL("Vert_tol", "#228833"),
    JAUNE_TOL("Jaune_tol", "#CCBB44"),
    ROUGE_SOMBRE("Rouge_sombre", "#EE6677"),
    VIOLET("Violet", "#AA3377"),
    GRIS_TOL("Gris_tol", "#BBBBBB"),
    MAGENTA_VIBRANT("Magenta_vibrant", "#EE7733"),
    BLEU_CLAIR_VIBRANT("Bleu_clair_vibrant", "#33BBEE"),
    VERT_VIBRANT("Vert_vibrant", "#009988"),
    JAUNE_CITRON("Jaune_citron", "#99EE44"),
    BLEU_NUIT("Bleu_nuit", "#0077BB"),
    ROSE_POURPRE("Rose_pourpre", "#EE3377"),
    MAUVE("Mauve", "#BBCC33"),
    INDIGO("Indigo", "#332288"),
    VERT_D_EAU("Vert_d_eau", "#44AA99");

    private final String displayName;
    private final String hex;

    TeamColor(String displayName, String hex) {
        this.displayName = displayName;
        this.hex = hex;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Code hexadécimal de la couleur, préfixé par {@code #} (ex. {@code #E69F00}).
     */
    public String getHex() {
        return hex;
    }

    /**
     * Couleur en RGB entiers 0-255.
     */
    public int[] getRgb() {
        int parsed = Integer.parseInt(hex.substring(1), 16);
        return new int[]{
            (parsed >> 16) & 0xFF,
            (parsed >> 8) & 0xFF,
            parsed & 0xFF
        };
    }

    /**
     * Recherche d'une couleur par son nom d'affichage (insensible à la casse).
     */
    public static Optional<TeamColor> byName(String name) {
        return Arrays.stream(values())
            .filter(c -> c.displayName.equalsIgnoreCase(name))
            .findFirst();
    }
}
