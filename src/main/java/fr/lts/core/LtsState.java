package fr.lts.core;

import fr.lts.core.game.GameService;
import fr.lts.core.team.TeamService;

/**
 * Accès global aux services du mod ( singleton serveur).
 *
 * <p>Initialisé au démarrage du serveur (via
 * {@link fr.lts.core.LtsCore#onInitialize()}), accessible partout. Thread-safe
 * par construction : un seul thread serveur principal accède en écriture/lecture.</p>
 */
public final class LtsState {

    private static GameService gameService;

    private LtsState() {
    }

    public static void init() {
        TeamService teamService = new TeamService();
        gameService = new GameService(teamService);
    }

    public static GameService getGameService() {
        if (gameService == null) {
            throw new IllegalStateException("LtsState non initialisé. Le serveur a-t-il démarré ?");
        }
        return gameService;
    }

    public static TeamService getTeamService() {
        return getGameService().getTeamService();
    }
}
