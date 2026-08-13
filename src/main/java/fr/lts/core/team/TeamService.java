package fr.lts.core.team;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service central de gestion des 24 teams.
 *
 * <p>Responsabilités :</p>
 * <ul>
 *   <li>Créer et maintenir les 24 teams pré-existantes (une par couleur de
 *       {@link TeamColor}).</li>
 *   <li>Stocke la {@code teamSize} courante (utilisé uniquement pour le random
 *       assign).</li>
 *   <li>Assignation manuelle ({@link #assign}), sans contrainte de taille.</li>
 *   <li>Random assign {@link #randomAssign} : tire N teams parmi les 24,
 *       remplit dans l'ordre selon {@code teamSize}, et le surplus va grossir
 *       une team plus petite (jamais une plus grosse).</li>
 *   <li>Recherche de la team d'un joueur, reset, liste des teams actives.</li>
 * </ul>
 *
 * <p>Ce service ne dépend pas de Minecraft : il manipule uniquement des UUIDs
 * et des données de team. La logique de téléportation/état joueur vit ailleurs
 * (GameService).</p>
 */
public class TeamService {

    /** Nombre maximum de teams dans le jeu. */
    public static final int MAX_TEAMS = 24;

    /** Taille d'équipe par défaut utilisée pour le random assign. */
    public static final int DEFAULT_TEAM_SIZE = 2;

    private final List<Team> teams = new ArrayList<>();
    private final Map<UUID, Team> playerTeams = new HashMap<>();

    private int teamSize = DEFAULT_TEAM_SIZE;

    public TeamService() {
        TeamColor[] colors = TeamColor.values();
        // TeamColor contient exactement MAX_TEAMS couleurs.
        for (int i = 0; i < MAX_TEAMS && i < colors.length; i++) {
            teams.add(new Team(i, colors[i]));
        }
    }

    // ----- Configuration -----

    /**
     * Définit la taille d'équipe utilisée pour le random assign.
     *
     * @param size 1, 2, 3 ou 4.
     * @throws IllegalArgumentException si la taille n'est pas dans {1,2,3,4}.
     */
    public void setTeamSize(int size) {
        if (size < 1 || size > 4) {
            throw new IllegalArgumentException(
                "La taille d'équipe doit être 1, 2, 3 ou 4 (reçu: " + size + ")");
        }
        this.teamSize = size;
    }

    public int getTeamSize() {
        return teamSize;
    }

    // ----- Accès aux teams -----

    /**
     * Liste immuable des 24 teams.
     */
    public List<Team> getAllTeams() {
        return Collections.unmodifiableList(teams);
    }

    /**
     * Team par couleur. {@code null} si la couleur n'existe pas (ne devrait
     * pas arriver puisque les 24 sont pré-créées à partir de l'enum).
     */
    public Team getTeam(TeamColor color) {
        for (Team t : teams) {
            if (t.getColor() == color) {
                return t;
            }
        }
        return null;
    }

    /**
     * Team par nom d'affichage de couleur (insensible à la casse).
     */
    public Team getTeam(String colorName) {
        return TeamColor.byName(colorName)
            .map(this::getTeam)
            .orElse(null);
    }

    /**
     * Team d'un joueur, ou {@code null} s'il n'en a pas.
     */
    public Team getTeamOf(UUID playerId) {
        return playerTeams.get(playerId);
    }

    /**
     * Teams actives (utilisées dans la partie courante), dans l'ordre.
     */
    public List<Team> getActiveTeams() {
        List<Team> active = new ArrayList<>();
        for (Team t : teams) {
            if (t.isActive()) {
                active.add(t);
            }
        }
        return active;
    }

    /**
     * Teams actives et non vides (donc avec au moins un joueur assigné).
     */
    public List<Team> getPopulatedTeams() {
        List<Team> populated = new ArrayList<>();
        for (Team t : teams) {
            if (t.isActive() && !t.isEmpty()) {
                populated.add(t);
            }
        }
        return populated;
    }

    // ----- Assignation manuelle -----

    /**
     * Assigne manuellement un joueur à une team, sans contrainte de taille.
     * La team est marquée comme active.
     *
     * @param playerId  joueur à assigner.
     * @param color     couleur de la team cible (parmi les 24).
     * @return {@code true} si l'assignation a changé quelque chose.
     * @throws IllegalArgumentException si la couleur n'existe pas.
     */
    public boolean assign(UUID playerId, TeamColor color) {
        Team target = getTeam(color);
        if (target == null) {
            throw new IllegalArgumentException("Couleur inconnue: " + color);
        }

        // Retirer le joueur de sa team actuelle s'il en a une.
        Team previous = playerTeams.remove(playerId);
        if (previous != null) {
            previous.removeMember(playerId);
        }

        boolean added = target.addMember(playerId);
        playerTeams.put(playerId, target);
        return added;
    }

    /**
     * Retire un joueur de sa team.
     *
     * @return {@code true} si le joueur était dans une team et a été retiré.
     */
    public boolean remove(UUID playerId) {
        Team team = playerTeams.remove(playerId);
        if (team == null) {
            return false;
        }
        return team.removeMember(playerId);
    }

    // ----- Random assign -----

    /**
     * Assigne aléatoirement les joueurs donnés à des teams tirées parmi les
     * 24, en respectant la {@code teamSize} courante.
     *
     * <p>Algorithme :</p>
     * <ol>
     *   <li>Tire au hasard le nombre de teams nécessaires parmi les 24
     *       (inactive et non encore tirée).</li>
     *   <li>Remplit les teams dans l'ordre, chaque team recevant
     *       {@code teamSize} joueurs.</li>
     *   <li>S'il reste des joueurs (surplus) et qu'on est au-delà de la
     *       dernière team pleine, on ajoute le surplus à une team déjà pleine
     *       — de préférence la plus petite à ce moment-là (jamais une plus
     *       grosse).</li>
     * </ol>
     *
     * <p>Les joueurs déjà assignés à une team avant l'appel sont retirés de
     * leur team précédente avant la redistribution.</p>
     *
     * @param players joueurs à répartir. Ne doit pas dépasser
     *                {@code MAX_TEAMS * teamSize} (sinon il y aura des teams
     *                au-delà de la taille cible).
     */
    public RandomAssignResult randomAssign(Collection<UUID> players) {
        List<UUID> toAssign = new ArrayList<>(players);
        Collections.shuffle(toAssign);

        // Retirer chaque joueur de sa team actuelle.
        for (UUID p : toAssign) {
            Team previous = playerTeams.remove(p);
            if (previous != null) {
                previous.removeMember(p);
            }
        }

        // Teams candidates : inactives (non encore utilisées) parmi les 24.
        List<Team> candidates = new ArrayList<>();
        for (Team t : teams) {
            if (!t.isActive()) {
                candidates.add(t);
            }
        }
        Collections.shuffle(candidates);

        int total = toAssign.size();
        if (total == 0) {
            return new RandomAssignResult(0, 0, List.of());
        }

        int teamsNeeded = (total + teamSize - 1) / teamSize; // arrondi supérieur
        int teamsAvailable = Math.min(teamsNeeded, candidates.size());

        List<Team> used = new ArrayList<>(candidates.subList(0, teamsAvailable));

        // Remplissage dans l'ordre : chaque team reçoit teamSize joueurs.
        int idx = 0;
        for (Team team : used) {
            team.activate();
            int count = Math.min(teamSize, total - idx);
            for (int i = 0; i < count && idx < total; i++) {
                UUID p = toAssign.get(idx++);
                team.addMember(p);
                playerTeams.put(p, team);
            }
        }

        // Surplus : joueurs restants après avoir rempli toutes les teams à
        // teamSize. On les ajoute à la team la plus petite courante (jamais à
        // une plus grosse), en bouclant.
        int surplus = total - idx;
        if (surplus > 0) {
            for (int i = 0; i < surplus; i++) {
                UUID p = toAssign.get(idx++);
                Team smallest = smallestActiveTeam(used);
                smallest.addMember(p);
                playerTeams.put(p, smallest);
            }
        }

        return new RandomAssignResult(total, used.size(), used);
    }

    /**
     * Renvoie la team active la plus petite parmi celles données (en cas
     * d'égalité, la première rencontrée).
     */
    private Team smallestActiveTeam(List<Team> among) {
        Team smallest = null;
        for (Team t : among) {
            if (smallest == null || t.size() < smallest.size()) {
                smallest = t;
            }
        }
        return smallest;
    }

    // ----- Reset -----

    /**
     * Réinitialise toutes les teams à leur état initial (vides et inactives),
     * oublie toutes les assignations de joueurs, et remet la teamSize à sa
     * valeur par défaut.
     */
    public void reset() {
        for (Team t : teams) {
            t.reset();
        }
        playerTeams.clear();
        teamSize = DEFAULT_TEAM_SIZE;
    }

    // ----- Résultat random -----

    /**
     * Résultat d'un random assign.
     *
     * @param totalPlayers    nombre de joueurs répartis.
     * @param teamsUsed       nombre de teams utilisées.
     * @param usedTeams       les teams utilisées, dans l'ordre de remplissage.
     */
    public static final class RandomAssignResult {
        public final int totalPlayers;
        public final int teamsUsed;
        public final List<Team> usedTeams;

        public RandomAssignResult(int totalPlayers, int teamsUsed, List<Team> usedTeams) {
            this.totalPlayers = totalPlayers;
            this.teamsUsed = teamsUsed;
            this.usedTeams = Collections.unmodifiableList(new ArrayList<>(usedTeams));
        }
    }
}
