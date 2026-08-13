package fr.lts.core.game;

/**
 * Mode hardcore hybride du jeu.
 *
 * <p>Dans les deux cas, le jeu est en "one life" (une seule vie, pas de
 * respawn). La différence porte sur la difficulté du PVE :</p>
 * <ul>
 *   <li>{@link #EASY} : difficulté easy + one-life (hardcore hybride, PVE peu
 *       hostile).</li>
 *   <li>{@link #VANILLA} : difficulté vanilla hardcore (PVE normal).</li>
 * </ul>
 */
public enum HardcoreMode {
    EASY,
    VANILLA
}
