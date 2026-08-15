package fr.lts.core.network;

import fr.lts.core.team.TeamColor;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Packet custom pour synchroniser les couleurs hex des teams de chaque joueur
 * vers les clients. Permet d afficher les vraies couleurs (24 couleurs
 * daltonien-friendly) au lieu des 16 couleurs Formatting de Minecraft.
 */
public final class TeamColorPacket {

    public static final Identifier PACKET_ID = new Identifier("lts-core", "team_colors");

    private TeamColorPacket() {
    }

    /**
     * Envoie les couleurs des teams de tous les joueurs a un client.
     *
     * @param playerColors map UUID du joueur -> couleur hex (int RGB).
     */
    public static void sendToPlayer(ServerPlayerEntity player,
                                     Map<UUID, Integer> playerColors) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(playerColors.size());
        for (Map.Entry<UUID, Integer> entry : playerColors.entrySet()) {
            buf.writeUuid(entry.getKey());
            buf.writeInt(entry.getValue());
        }
        ServerPlayNetworking.send(player, PACKET_ID, buf);
    }

    /**
     * Recupere les couleurs des teams de tous les joueurs connectes.
     */
    public static Map<UUID, Integer> collectPlayerColors(MinecraftServer server) {
        Map<UUID, Integer> colors = new HashMap<>();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Recupere la team LTS du joueur depuis le TeamService.
            try {
                fr.lts.core.team.Team team = fr.lts.core.LtsState.getTeamService()
                    .getTeamOf(player.getUuid());
                if (team != null && team.getColor() != null) {
                    int[] rgb = team.getColor().getRgb();
                    int color = (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
                    colors.put(player.getUuid(), color);
                }
            } catch (Exception e) {
                // Ignore.
            }
        }
        return colors;
    }

    /**
     * Broadcast les couleurs de tous les joueurs a tous les clients.
     */
    public static void broadcast(MinecraftServer server) {
        Map<UUID, Integer> colors = collectPlayerColors(server);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            sendToPlayer(player, colors);
        }
    }
}
