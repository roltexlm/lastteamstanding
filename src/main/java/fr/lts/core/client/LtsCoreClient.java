package fr.lts.core.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Point d'entrée client. L'UI (nametag custom, TAB, chat, HUD timer, affichage
 * des vainqueurs) nécessite du rendu côté client. Sera implémenté dans une
 * étape ultérieure.
 */
public class LtsCoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // TODO: HUD (timer + kills), nametag, TAB, chat, affichage vainqueurs.
    }
}
