package com.bedwarshud.render;

import com.bedwarshud.config.BedWarsHudConfig;
import com.bedwarshud.model.BedState;
import com.bedwarshud.model.BedWarsTeam;
import com.bedwarshud.tracker.BedStatusTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;

public final class HudRenderer {
    private static final int GAP = 5;

    private final Minecraft client;
    private final BedStatusTracker tracker;
    private final java.util.function.Supplier<BedWarsHudConfig> config;

    public HudRenderer(Minecraft client, BedStatusTracker tracker,
                       java.util.function.Supplier<BedWarsHudConfig> config) {
        this.client = client;
        this.tracker = tracker;
        this.config = config;
    }

    public void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        BedWarsHudConfig settings = config.get();
        if (!settings.hudEnabled || client.level == null) {
            return;
        }
        if (settings.showOnlyWhilePlayingBedWars && !tracker.isInBedWars()) {
            return;
        }

        int iconSize = Math.max(1, Math.round(settings.iconSize * settings.hudScale));
        int gap = Math.max(1, Math.round(GAP * settings.hudScale));
        int count = BedWarsTeam.values().length;
        int totalWidth = count * iconSize + (count - 1) * gap;
        int startX = (graphics.guiWidth() - totalWidth) / 2 + settings.hudXOffset;
        int y = settings.hudYOffset;

        BedWarsTeam[] teams = BedWarsTeam.values();
        for (int index = 0; index < teams.length; index++) {
            BedWarsTeam team = teams[index];
            int x = startX + index * (iconSize + gap);
            boolean destroyed = tracker.state(team) == BedState.DESTROYED;
            BedIconRenderer.draw(graphics, x, y, iconSize, team.argb(), destroyed, settings.redXSize);
        }
    }
}

