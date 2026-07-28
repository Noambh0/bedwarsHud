package com.bedwarshud.config;

public final class BedWarsHudConfig {
    public boolean hudEnabled = true;
    public float hudScale = 1.0F;
    public int hudXOffset = 0;
    public int hudYOffset = 8;
    public int iconSize = 18;
    public float redXSize = 1.0F;
    public boolean showOnlyWhilePlayingBedWars = true;

    public void sanitize() {
        hudScale = clamp(hudScale, 0.5F, 3.0F);
        hudXOffset = clamp(hudXOffset, -10_000, 10_000);
        hudYOffset = clamp(hudYOffset, -10_000, 10_000);
        iconSize = clamp(iconSize, 8, 64);
        redXSize = clamp(redXSize, 0.5F, 2.0F);
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
