package fr.lts.core;

import fr.lts.core.command.LtsCommands;
import fr.lts.core.game.GameTimer;
import fr.lts.core.game.PlayerDeathHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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

        // One-life (hardcore hybride) :
        // - ALLOW_DEATH : mémorise la position de mort + compteur de kills
        //   (appelé au moment des dégâts fatals, avant la mort effective).
        // - AFTER_RESPAWN : repasse immédiatement en spectateur à la position
        //   de mort (le joueur ne rejoue jamais, peu importe le mode).
        ServerPlayerEvents.ALLOW_DEATH.register(PlayerDeathHandler::onAllowDeath);
        ServerPlayerEvents.AFTER_RESPAWN.register(PlayerDeathHandler::onAfterRespawn);

        // Timer serveur : met a jour le scoreboard (temps restant + kills)
        // et verifie la fin de partie (timer ecoule ou derniere team).
        ServerTickEvents.END_SERVER_TICK.register(GameTimer::onServerTick);

        LOGGER.info("[LTS] Coeur de jeu prêt.");
    }
}
