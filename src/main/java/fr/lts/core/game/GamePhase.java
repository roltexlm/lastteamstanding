package fr.lts.core.game;

/**
 * Phase courante de la partie.
 */
public enum GamePhase {
    /** Phase d'accueil : configuration des teams, pas de jeu en cours. */
    LOBBY,

    /** Les équipes ont été téléportées et stunées, en attente du /lts start. */
    PLACEMENT,

    /** La partie est en cours : timer lancé, joueurs libérés. */
    RUNNING,

    /** La partie est terminée : affichage des vainqueurs ou reset effectué. */
    ENDED
}
