package com.bedwarshud.model;

import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum BedWarsTeam {
    RED("Red", ChatFormatting.RED, 0xFFFF5555, List.of("red", "r")),
    BLUE("Blue", ChatFormatting.BLUE, 0xFF5555FF, List.of("blue", "b")),
    GREEN("Green", ChatFormatting.GREEN, 0xFF55FF55, List.of("green", "g")),
    YELLOW("Yellow", ChatFormatting.YELLOW, 0xFFFFFF55, List.of("yellow", "y")),
    AQUA("Aqua", ChatFormatting.AQUA, 0xFF55FFFF, List.of("aqua", "cyan", "a")),
    WHITE("White", ChatFormatting.WHITE, 0xFFFFFFFF, List.of("white", "w")),
    PINK("Pink", ChatFormatting.LIGHT_PURPLE, 0xFFFF55FF, List.of("pink", "p")),
    GRAY("Gray", ChatFormatting.GRAY, 0xFFAAAAAA, List.of("gray", "grey", "s"));

    private final String displayName;
    private final ChatFormatting formatting;
    private final int argb;
    private final List<String> aliases;

    BedWarsTeam(String displayName, ChatFormatting formatting, int argb, List<String> aliases) {
        this.displayName = displayName;
        this.formatting = formatting;
        this.argb = argb;
        this.aliases = aliases;
    }

    public String displayName() {
        return displayName;
    }

    public int argb() {
        return argb;
    }

    public ChatFormatting formatting() {
        return formatting;
    }

    public static Optional<BedWarsTeam> fromPlayerTeam(Team team) {
        if (team == null) {
            return Optional.empty();
        }

        ChatFormatting color = team.getColor();
        for (BedWarsTeam candidate : values()) {
            if (candidate.formatting == color) {
                return Optional.of(candidate);
            }
        }

        if (!(team instanceof PlayerTeam playerTeam)) {
            return Optional.empty();
        }

        String name = playerTeam.getName().toLowerCase(Locale.ROOT);
        for (BedWarsTeam candidate : values()) {
            if (candidate.aliases.stream().anyMatch(alias ->
                    name.equals(alias) || name.startsWith(alias + "_") || name.endsWith("_" + alias))) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
