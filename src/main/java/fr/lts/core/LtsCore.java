package fr.lts.core;

import fr.lts.core.command.LtsCommands;
import fr.lts.core.game.PlayerDeathHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Point d'entrée principal du mod Last Team Standing - Core.
 *
 * <p>Le mod gère : les teams (24 pré-créées), le timer de partie, la
 * téléportation des équipes, les états joueurs (immobile/invincible), les
 * commandes {@code /lts ...} et l'affichage des vainqueurs.</p>
 */
public class LtsCore implements ModInitializer {
    public static final String MOD_ID = "lts-core";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[LTS] Initialisation du coeur de jeu Last Team Standing...");

        // Initialise les services partagés (teams + game state).
        LtsState.init();

        // Enregistrement des commandes /lts ...
        CommandRegistrationCallback.EVENT.register(LtsCommands::register);

        // One-life : à la mort d'un joueur en partie, passage en spectateur
        // (mode easy) ou bannissement vanilla (mode vanilla).
        ServerLivingEntityEvents.AFTER_DEATH.register(PlayerDeathHandler::onAfterDeath);

        LOGGER.info("[LTS] Coeur de jeu prêt.");
    }
}
