package fr.lts.core.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Packets custom pour synchroniser l'état du jeu (timer + kills) du serveur
 * vers les clients.
 *
 * <p>Évite d'utiliser le scoreboard (qui provoque l'affichage vanilla de la
 * sidebar) : on envoie directement les valeurs au client, qui les affiche dans
 * le HUD custom.</p>
 */
public final class LtsNetworking {

    public static final Identifier STATE_PACKET_ID = new Identifier("lts-core", "state");

    private LtsNetworking() {
    }

    /**
     * Envoie l'état du jeu (temps restant en secondes + kills du joueur) à un
     * joueur.
     */
    public static void sendGameState(ServerPlayerEntity player, long remainingSeconds, int kills) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeLong(remainingSeconds);
        buf.writeInt(kills);
        ServerPlayNetworking.send(player, STATE_PACKET_ID, buf);
    }

    /**
     * Envoie l'état du jeu à tous les joueurs connectés.
     */
    public static void broadcastGameState(MinecraftServer server, long remainingSeconds,
                                            java.util.Map<java.util.UUID, Integer> killsByPlayer) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            int kills = killsByPlayer.getOrDefault(player.getUuid(), 0);
            sendGameState(player, remainingSeconds, kills);
        }
    }
}
