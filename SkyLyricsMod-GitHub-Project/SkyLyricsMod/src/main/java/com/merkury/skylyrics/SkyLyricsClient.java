package com.merkury.skylyrics;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.text.Text;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class SkyLyricsClient implements ClientModInitializer {
    private static boolean enabled = true;
    private static List<String> lyrics = List.of("your lyrics in the sky", "a little closer to the stars");
    private static long startTime;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        startTime = System.currentTimeMillis();
        loadLyrics();
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.skylyrics.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_L, "category.skylyrics"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) {
                enabled = !enabled;
                if (client.player != null) client.player.sendMessage(
                        Text.literal("Sky Lyrics: " + (enabled ? "ON" : "OFF")), true);
            }
        });
        WorldRenderEvents.AFTER_ENTITIES.register(context -> renderLyrics(context.matrixStack(), context.camera().getPos()));
    }

    private static void loadLyrics() {
        Path file = MinecraftClient.getInstance().runDirectory.toPath().resolve("config/skylyrics.txt");
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.write(file, List.of("your lyrics in the sky", "a little closer to the stars"));
            }
            List<String> loaded = Files.readAllLines(file).stream().map(String::trim)
                    .filter(line -> !line.isEmpty()).limit(8).toList();
            if (!loaded.isEmpty()) lyrics = loaded;
        } catch (IOException ignored) { }
    }

    private static void renderLyrics(MatrixStack matrices, Vec3d cameraPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!enabled || client.world == null || client.player == null || matrices == null) return;

        float elapsed = (System.currentTimeMillis() - startTime) / 1000.0f;
        Vector3f forwardVector = new Vector3f(0, 0, -1).rotate(client.gameRenderer.getCamera().getRotation());
        Vector3f upVector = new Vector3f(0, 1, 0).rotate(client.gameRenderer.getCamera().getRotation());
        Vec3d forward = new Vec3d(forwardVector.x, forwardVector.y, forwardVector.z);
        Vec3d up = new Vec3d(upVector.x, upVector.y, upVector.z);
        Vec3d anchor = cameraPos.add(forward.multiply(30)).add(up.multiply(5));

        matrices.push();
        matrices.translate(anchor.x - cameraPos.x, anchor.y - cameraPos.y, anchor.z - cameraPos.z);
        matrices.multiply(client.gameRenderer.getCamera().getRotation());
        matrices.scale(-0.035f, -0.035f, 0.035f);

        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        TextRenderer renderer = client.textRenderer;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int count = lyrics.size();
        for (int i = 0; i < count; i++) {
            String line = lyrics.get(i);
            float drift = (float) Math.sin(elapsed * 0.45f + i * 0.8f) * 18.0f;
            float y = (i - (count - 1) / 2.0f) * 15.0f;
            int width = renderer.getWidth(line);
            float x = -width / 2.0f + drift;
            int alpha = 190 + (int) (45 * Math.sin(elapsed * 1.2f + i));
            int color = (Math.max(0, Math.min(255, alpha)) << 24) | 0xFFFFFF;
            renderer.draw(line, x, y, color, true, matrix, consumers,
                    TextRenderer.TextLayerType.SEE_THROUGH, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
        }
        consumers.draw();
        matrices.pop();
    }
}
