package com.bedwarshud.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BedIconRenderer {
    private static final Identifier BED_TEXTURE =
            Identifier.fromNamespaceAndPath("bedwarshud", "textures/gui/bed.png");
    private static final int DESTROYED_LINE_COLOR = 0xFFFF2020;

    private BedIconRenderer() {
    }

    public static void draw(GuiGraphics graphics, int x, int y, int size, int color, boolean destroyed,
                            float redXScale) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                BED_TEXTURE,
                x,
                y,
                0.0F,
                0.0F,
                size,
                size,
                16,
                16,
                color
        );

        if (destroyed) {
            drawDestroyedLine(graphics, x, y, size, redXScale);
        }
    }

    private static void drawDestroyedLine(GuiGraphics graphics, int x, int y, int size, float scale) {
        int thickness = Math.max(1, Math.round(size / 14.0F * scale));
        for (int offset = 0; offset < size; offset++) {
            int lineX = x + offset;
            int lineY = y + size - 1 - offset;
            graphics.fill(
                    lineX,
                    Math.max(y, lineY - thickness + 1),
                    Math.min(x + size, lineX + thickness),
                    lineY + 1,
                    DESTROYED_LINE_COLOR
            );
        }
    }
}
