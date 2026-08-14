package fr.lts.core.game;

/**
 * État mutable de la partie (phase, mode hardcore, taille de map, timer, etc.).
 *
 * <p>Cet objet est volontairement simple (POJO thread-confined au thread
 * serveur principal) et ne contient aucune logique Minecraft. La logique de
 * téléportation/état joueur vit dans {@code GameService}.</p>
 */
public final class GameState {

    private GamePhase phase = GamePhase.LOBBY;
    private HardcoreMode hardcore = HardcoreMode.EASY;

    /** Taille de map courante (en blocs côté). Calculée au tp. */
    private int mapSizeBlocks = 0;

    /**
     * Compteur indiquant si {@code /lts tp} a déjà été appelé au moins une fois
     * alors qu'aucune team n'était active. Permet de forcer le tp au second
     * appel consécutif (comportement demandé : 1er appel = erreur, 2e = force).
     */
    private boolean tpForceNext = false;

    /**
     * Nombre de kills total dans la partie (pour l'affichage HUD).
     */
    private int killCount = 0;

    // ----- Timer -----

    /**
     * Durée totale d'une partie : 2h33, soit 9214 secondes
     * (2x3600 + 33x60 + 34 = 9214s).
     *
     * <p>1 semaine in-game (7x20min) = 2h20 = 8400s, à 2h33 exactement (9214s).</p>
     */
    public static final long GAME_DURATION_TICKS = 9214L * 20L; // 184 280 ticks

    private long startTimeTicks = -1L; // -1 = partie non démarrée

    /** Nombre de teams actives au moment du /lts start (pour éviter la victoire instantanée en solo). */
    private int initialTeamsCount = 0;

    /** Si le PvP est activé (s active à 1h de jeu). */
    private boolean pvpEnabled = false;

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    public HardcoreMode getHardcore() {
        return hardcore;
    }

    public void setHardcore(HardcoreMode hardcore) {
        this.hardcore = hardcore;
    }

    public int getMapSizeBlocks() {
        return mapSizeBlocks;
    }

    public void setMapSizeBlocks(int mapSizeBlocks) {
        this.mapSizeBlocks = mapSizeBlocks;
    }

    public boolean isTpForceNext() {
        return tpForceNext;
    }

    public void setTpForceNext(boolean tpForceNext) {
        this.tpForceNext = tpForceNext;
    }

    public int getKillCount() {
        return killCount;
    }

    public void incrementKillCount() {
        this.killCount++;
    }

    public long getStartTimeTicks() {
        return startTimeTicks;
    }

    public void setStartTimeTicks(long startTimeTicks) {
        this.startTimeTicks = startTimeTicks;
    }

    public int getInitialTeamsCount() {
        return initialTeamsCount;
    }

    public void setInitialTeamsCount(int initialTeamsCount) {
        this.initialTeamsCount = initialTeamsCount;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    /**
     * Réinitialise l'état à sa valeur initiale (LOBBY, easy, timer à -1, etc.).
     * La taille de map et le kill count sont aussi reset.
     */
    public void reset() {
        this.phase = GamePhase.LOBBY;
        this.hardcore = HardcoreMode.EASY;
        this.mapSizeBlocks = 0;
        this.tpForceNext = false;
        this.killCount = 0;
        this.startTimeTicks = -1L;
        this.initialTeamsCount = 0;
        this.pvpEnabled = false;
    }
}
