package com.bedwarshud.mixin;

import com.bedwarshud.BedWarsHudClient;
import com.bedwarshud.config.BedWarsHudConfig;
import com.bedwarshud.model.BedState;
import com.bedwarshud.model.BedWarsTeam;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Uses vanilla's secondary player-name line for the marker. This preserves all
 * of vanilla's distance scaling, visibility, sneaking and line-of-sight rules.
 */
@Mixin(AvatarRenderer.class)
abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void bedwarshud$addBedStatus(
            Avatar player,
            AvatarRenderState state,
            float tickDelta,
            CallbackInfo callbackInfo
    ) {
        if (BedWarsHudClient.configManager() == null) {
            return;
        }

        BedWarsHudConfig config = BedWarsHudClient.configManager().get();
        if (!config.nametagIconsEnabled) {
            return;
        }
        if (config.showOnlyWhilePlayingBedWars && !BedWarsHudClient.tracker().isInBedWars()) {
            return;
        }

        BedWarsTeam.fromPlayerTeam(player.getTeam()).ifPresent(team -> {
            Component marker = Component.literal("▰").withStyle(team.formatting());
            if (BedWarsHudClient.tracker().state(team) == BedState.DESTROYED) {
                marker = Component.empty()
                        .append(marker)
                        .append(Component.literal(" ✕").withStyle(ChatFormatting.RED));
            }
            state.scoreText = marker;
        });
    }
}
