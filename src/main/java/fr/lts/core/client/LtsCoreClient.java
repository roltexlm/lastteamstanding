package fr.lts.core.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Point d'entrée client.
 *
 * <p>Gère l'affichage HUD : temps restant + nombre de kills, lus depuis le
 * scoreboard (objectifs {@code lts_timer} et {@code lts_kills} mis à jour par
 * le serveur). Affiché uniquement pendant une partie (quand le scoreboard
 * LTS existe).</p>
 */
public class LtsCoreClient implements ClientModInitializer {
    public static final String TIMER_OBJECTIVE = "lts_timer";
    public static final String KILLS_OBJECTIVE = "lts_kills";
    private static final String TIMER_HOLDER = "LTS Timer";
    private static final String KILLS_HOLDER = "Kills";

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onHudRender(MatrixStack matrixStack, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        Scoreboard scoreboard = client.player.getScoreboard();
        if (scoreboard == null) {
            return;
        }

        // Ne rend que si les objectifs LTS existent (partie en cours).
        ScoreboardObjective timerObj = scoreboard.getObjective(TIMER_OBJECTIVE);
        ScoreboardObjective killsObj = scoreboard.getObjective(KILLS_OBJECTIVE);
        if (timerObj == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int windowWidth = client.getWindow().getScaledWidth();
        int y = 4;

        // Timer : format HH:MM:SS
        int seconds = getScore(scoreboard, TIMER_HOLDER, timerObj);
        String timeStr = formatTime(seconds);
        Text timerText = new LiteralText("⏱ " + timeStr).formatted(Formatting.GOLD);
        int timerWidth = textRenderer.getWidth(timerText);
        textRenderer.drawWithShadow(matrixStack, timerText, windowWidth - timerWidth - 4, y, 0xFFFFFF);
        y += 12;

        // Kills : lit le score du joueur local dans lts_kills
        if (killsObj != null && client.player != null) {
            String playerName = client.player.getEntityName();
            int kills = getScore(scoreboard, playerName, killsObj);
            Text killsText = new LiteralText("⚔ " + kills).formatted(Formatting.RED);
            int killsWidth = textRenderer.getWidth(killsText);
            textRenderer.drawWithShadow(matrixStack, killsText, windowWidth - killsWidth - 4, y, 0xFFFFFF);
        }
    }

    private int getScore(Scoreboard scoreboard, String holder, ScoreboardObjective objective) {
        if (!scoreboard.getKnownPlayers().contains(holder)) {
            return 0;
        }
        return scoreboard.getPlayerScore(holder, objective).getScore();
    }

    /**
     * Formate un nombre de secondes en HH:MM:SS (ou MM:SS si < 1h).
     */
    private static String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }
}
