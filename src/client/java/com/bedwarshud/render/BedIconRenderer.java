package com.bedwarshud.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public final class BedIconRenderer {
    private static final ResourceLocation BED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("bedwarshud", "textures/gui/bed.png");
    private static final ResourceLocation RED_X_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("bedwarshud", "textures/gui/red_x.png");

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
            int overlaySize = Math.max(1, Math.round(size * redXScale));
            int overlayX = x + (size - overlaySize) / 2;
            int overlayY = y + (size - overlaySize) / 2;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    RED_X_TEXTURE,
                    overlayX,
                    overlayY,
                    0.0F,
                    0.0F,
                    overlaySize,
                    overlaySize,
                    16,
                    16
            );
        }
    }
}
