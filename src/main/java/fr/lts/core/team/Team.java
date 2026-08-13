package fr.lts.core.team;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Représente une des 24 teams du jeu.
 *
 * <p>Les 24 teams sont pré-créées au démarrage du mod, chacune avec une couleur
 * fixe de la palette {@link TeamColor}. Une team est {@link #active} si elle est
 * utilisée dans la partie courante (a au moins un joueur, ou a été activée
 * manuellement via {@code /lts team assign}).</p>
 *
 * <p>Les membres sont stockés par UUID pour rester robustes face aux
 * reconnexions de joueurs.</p>
 */
public final class Team {

    private final int index;
    private final TeamColor color;
    private final Set<UUID> members = new LinkedHashSet<>();
    private boolean active;

    public Team(int index, TeamColor color) {
        this.index = index;
        this.color = color;
        this.active = false;
    }

    public int getIndex() {
        return index;
    }

    public TeamColor getColor() {
        return color;
    }

    /**
     * Membres de la team (UUIDs), non modifiables directement.
     */
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public int size() {
        return members.size();
    }

    public boolean isActive() {
        return active;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    /**
     * Ajoute un joueur à la team et marque la team comme active.
     *
     * @return {@code true} si le joueur a été ajouté (n'était pas déjà présent).
     */
    public boolean addMember(UUID playerId) {
        boolean added = members.add(playerId);
        if (added) {
            active = true;
        }
        return added;
    }

    /**
     * Retire un joueur de la team. Ne désactive pas la team (reste active tant
     * que la partie est en cours, voir {@link TeamService} pour le reset).
     *
     * @return {@code true} si le joueur était présent et a été retiré.
     */
    public boolean removeMember(UUID playerId) {
        return members.remove(playerId);
    }

    /**
     * Active explicitement la team (ex. via assignation manuelle).
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Réinitialise la team à son état initial : vide et inactive.
     */
    public void reset() {
        members.clear();
        active = false;
    }

    @Override
    public String toString() {
        return color.getDisplayName() + " (" + members.size() + " membres)";
    }
}
