package fr.lts.core.client;

import fr.lts.core.network.LtsNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Point d'entrée client.
 *
 * <p>Gère l'affichage HUD : temps restant + nombre de kills, reçus via packet
 * custom du serveur. Affiché uniquement pendant une partie (quand l'état a
 * été reçu au moins une fois).</p>
 *
 * <p>Stocke aussi la phase de jeu côté client (pour le JumpBlockMixin).</p>
 */
public class LtsCoreClient implements ClientModInitializer {

    /** État reçu du serveur (mis à jour par packet). -1 = pas de partie. */
    private static long remainingSeconds = -1;
    private static int kills = 0;
    /** Phase de jeu côté client (LOBBY, PLACEMENT, RUNNING, ENDED). */
    private static String clientPhase = "LOBBY";

    @Override
    public void onInitializeClient() {
        // Reçoit l'état du jeu (timer + kills + phase) du serveur.
        ClientPlayNetworking.registerGlobalReceiver(LtsNetworking.STATE_PACKET_ID,
            (client, handler, buf, responseSender) -> {
                long remaining = buf.readLong();
                int k = buf.readInt();
                String phase = buf.readString();
                client.execute(() -> {
                    remainingSeconds = remaining;
                    kills = k;
                    clientPhase = phase;
                });
            });

        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(MatrixStack matrixStack, float tickDelta) {
        // N'affiche rien si pas de partie ou timer écoulé.
        if (remainingSeconds < 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int windowWidth = client.getWindow().getScaledWidth();
        int y = 4;

        // Timer : format HH:MM:SS
        String timeStr = formatTime(remainingSeconds);
        Text timerText = new LiteralText(timeStr).formatted(Formatting.GOLD);
        int timerWidth = textRenderer.getWidth(timerText);
        textRenderer.drawWithShadow(matrixStack, timerText, windowWidth - timerWidth - 4, y, 0xFFFFFF);
        y += 12;

        // Kills
        Text killsText = new LiteralText("⚔ " + kills).formatted(Formatting.RED);
        int killsWidth = textRenderer.getWidth(killsText);
        textRenderer.drawWithShadow(matrixStack, killsText, windowWidth - killsWidth - 4, y, 0xFFFFFF);
    }

    /**
     * Réinitialise l'état client (quand la partie s'arrête).
     */
    public static void resetState() {
        remainingSeconds = -1;
        kills = 0;
        clientPhase = "LOBBY";
    }

    /**
     * Retourne la phase de jeu côté client.
     */
    public static String getClientPhase() {
        return clientPhase;
    }

    /**
     * Formate un nombre de secondes en HH:MM:SS (ou MM:SS si < 1h).
     */
    private static String formatTime(long totalSeconds) {
        int seconds = (int) totalSeconds;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%d:%02d", minutes, secs);
    }
}
