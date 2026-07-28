package com.bedwarshud;

import com.bedwarshud.config.ConfigManager;
import com.bedwarshud.render.HudRenderer;
import com.bedwarshud.tracker.BedStatusTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BedWarsHudClient implements ClientModInitializer {
    public static final String MOD_ID = "bedwarshud";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final BedStatusTracker TRACKER = new BedStatusTracker();
    private static ConfigManager configManager;

    @Override
    public void onInitializeClient() {
        configManager = new ConfigManager(LOGGER);
        Minecraft client = Minecraft.getInstance();
        HudRenderer renderer = new HudRenderer(client, TRACKER, () -> configManager.get());

        ClientTickEvents.END_CLIENT_TICK.register(TRACKER::tick);
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "bed_status"),
                renderer::render
        );

        LOGGER.info("Bed Wars HUD initialized");
    }

    public static BedStatusTracker tracker() {
        return TRACKER;
    }

    public static ConfigManager configManager() {
        return configManager;
    }
}
