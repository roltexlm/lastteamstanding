# lts-core

Coeur du mode de jeu **Last Team Standing** pour Minecraft Java Edition **1.17.1** (Fabric).

## Périmètre actuel (itération 1 : teams)

- **24 teams pré-créées**, chacune avec une couleur fixe de la palette daltonien-friendly
  (Okabe-Ito + Paul Tol). Voir [`TeamColor`](src/main/java/fr/lts/core/team/TeamColor.java).
- **Commandes `/lts`** (OP uniquement, permission level 2) :

| Commande | Description |
|----------|-------------|
| `/lts team size <1\|2\|3\|4>` | Taille d'équipe utilisée pour le random uniquement. |
| `/lts team assign <player> <color>` | Attribution manuelle, sans contrainte de taille. |
| `/lts team random [players...]` | Random : tire N teams parmi 24, remplit dans l'ordre selon `size`, surplus → équipe plus petite. |
| `/lts team list` | Liste les teams actives. |
| `/lts team clear` | Réinitialise toutes les teams. |
| `/lts team remove <player>` | Retire un joueur de sa team. |
| `/lts tp` | Téléporte les teams actives (placement étalé), stune les joueurs. 2e appel consécutif force même sans team. |
| `/lts start` | Débloque les joueurs placés + lance le timer. Erreur si pas de `tp` au préalable. |
| `/lts stop` | Arrête : retire les teams, passe en spectateur, reset les options. |
| `/lts hardcore easy\|vanilla` | Bascule du mode hardcore hybride. |

- **Pas de commande pour les non-OP** : seul l'HUD (timer + kills) leur est visible (à implémenter).

## Build

```bash
./gradlew build
```

Nécessite JDK 16+.

## Versions

- Minecraft 1.17.1
- Yarn `1.17.1+build.65`
- Fabric Loader `0.12.5`
- Fabric API `0.37.2+1.17`
- Loom `0.9-SNAPSHOT`

## À venir (itérations suivantes)

- HUD : timer restant + nombre de kills (côté client).
- Nametag custom (tête 2D + pseudo couleur), TAB, chat.
- Affichage des vainqueurs (title + joueurs alignés), gestion ex aequo.
- One-life : ban-on-death → spectateur (hardcore easy / vanilla).
- Items bannis + enchantements bannis (mod séparé `lts-restriction` possible).
