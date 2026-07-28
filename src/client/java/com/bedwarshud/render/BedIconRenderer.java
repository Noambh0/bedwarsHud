package com.bedwarshud.render;

import com.bedwarshud.model.BedState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BedIconRenderer {
    private static final Identifier BED_TEXTURE =
            Identifier.fromNamespaceAndPath("bedwarshud", "textures/gui/bed.png");
    private static final Identifier BED_DESTROYED_TEXTURE =
            Identifier.fromNamespaceAndPath("bedwarshud", "textures/gui/red_slash.png");
    private static final Identifier ELIMINATED_TEXTURE =
            Identifier.fromNamespaceAndPath("bedwarshud", "textures/gui/red_x.png");
    private static final Identifier TEAM_ARROW_TEXTURE =
            Identifier.fromNamespaceAndPath("bedwarshud", "textures/gui/team_arrow.png");
    private static final int TEXTURE_SIZE = 64;

    private BedIconRenderer() {
    }

    public static void draw(GuiGraphics graphics, int x, int y, int size, int color, BedState state,
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
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                color
        );

        Identifier overlay = switch (state) {
            case BED_DESTROYED -> BED_DESTROYED_TEXTURE;
            case ELIMINATED -> ELIMINATED_TEXTURE;
            default -> null;
        };
        if (overlay != null) {
            int overlaySize = Math.max(1, Math.round(size * redXScale));
            int overlayX = x + (size - overlaySize) / 2;
            int overlayY = y + (size - overlaySize) / 2;
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    overlay,
                    overlayX,
                    overlayY,
                    0.0F,
                    0.0F,
                    overlaySize,
                    overlaySize,
                    TEXTURE_SIZE,
                    TEXTURE_SIZE
            );
        }
    }

    public static void drawTeamArrow(GuiGraphics graphics, int centerX, int bottomY, int size) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEAM_ARROW_TEXTURE,
                centerX - size / 2,
                bottomY - size,
                0.0F,
                0.0F,
                size,
                size,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );
    }
}
