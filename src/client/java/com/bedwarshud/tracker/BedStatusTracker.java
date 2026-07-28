package com.bedwarshud.tracker;

import com.bedwarshud.model.BedState;
import com.bedwarshud.model.BedWarsTeam;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.PlayerScoreEntry;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BedStatusTracker {
    private static final Pattern TEAM_STATUS = Pattern.compile(
            "(?i)(?:^|\\s)(red|blue|green|yellow|aqua|cyan|white|pink|gray|grey)\\s*[:\\-]?\\s*(.+)$");
    private static final Pattern BED_WARS = Pattern.compile("(?i)bed\\s*wars");
    private static final Pattern BED_DESTROYED_MESSAGE = Pattern.compile(
            "(?i)\\b(red|blue|green|yellow|aqua|cyan|white|pink|gray|grey)\\s+bed\\b.*\\b(?:destroyed|broken)\\b");

    private final EnumMap<BedWarsTeam, BedState> states = new EnumMap<>(BedWarsTeam.class);
    private boolean inBedWars;
    private int ticksUntilRefresh;

    public BedStatusTracker() {
        reset();
    }

    public void tick(Minecraft client) {
        if (ticksUntilRefresh-- > 0) {
            return;
        }
        ticksUntilRefresh = 4;

        if (client.level == null || client.getConnection() == null) {
            reset();
            return;
        }

        Scoreboard scoreboard = client.level.getScoreboard();
        Objective sidebar = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (sidebar == null) {
            reset();
            return;
        }

        String title = plain(sidebar.getDisplayName());
        StringBuilder visibleText = new StringBuilder(title);
        EnumMap<BedWarsTeam, BedState> next = new EnumMap<>(states);

        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(sidebar)) {
            PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            Component line = entry.display() != null ? entry.display() : Component.literal(entry.owner());
            Component decorated = PlayerTeam.formatNameForTeam(team, line);
            String text = plain(decorated);
            visibleText.append('\n').append(text);
            parseTeamLine(text, next);
        }

        inBedWars = BED_WARS.matcher(visibleText).find();
        states.putAll(next);
    }

    public boolean isInBedWars() {
        return inBedWars;
    }

    /**
     * Hypixel announces bed destructions as game messages. Reading that client-visible
     * message is more reliable than waiting for every sidebar variant to update.
     */
    public void handleGameMessage(Component message) {
        Matcher matcher = BED_DESTROYED_MESSAGE.matcher(plain(message));
        if (matcher.find()) {
            BedWarsTeam team = teamFromName(matcher.group(1));
            if (team != null) {
                states.put(team, BedState.DESTROYED);
                inBedWars = true;
            }
        }
    }

    public BedState state(BedWarsTeam team) {
        return states.getOrDefault(team, BedState.UNKNOWN);
    }

    public Map<BedWarsTeam, BedState> snapshot() {
        return Map.copyOf(states);
    }

    private void parseTeamLine(String text, EnumMap<BedWarsTeam, BedState> target) {
        String normalized = text.replace('✔', '✓').trim();
        Matcher matcher = TEAM_STATUS.matcher(normalized);
        if (!matcher.find()) {
            return;
        }

        String teamName = matcher.group(1).toLowerCase(Locale.ROOT);
        String status = matcher.group(2).toLowerCase(Locale.ROOT);
        BedWarsTeam team = teamFromName(teamName);

        if (team == null) {
            return;
        }

        if (status.contains("✕") || status.contains("×") || status.contains("dead")
                || status.contains("destroyed")
                || status.matches(".*\\b[0-9]+\\b.*")) {
            target.put(team, BedState.DESTROYED);
        } else if (status.contains("✓") || status.contains("alive")) {
            target.put(team, BedState.ALIVE);
        }
    }

    private static BedWarsTeam teamFromName(String teamName) {
        return switch (teamName.toLowerCase(Locale.ROOT)) {
            case "red" -> BedWarsTeam.RED;
            case "blue" -> BedWarsTeam.BLUE;
            case "green" -> BedWarsTeam.GREEN;
            case "yellow" -> BedWarsTeam.YELLOW;
            case "aqua", "cyan" -> BedWarsTeam.AQUA;
            case "white" -> BedWarsTeam.WHITE;
            case "pink" -> BedWarsTeam.PINK;
            case "gray", "grey" -> BedWarsTeam.GRAY;
            default -> null;
        };
    }

    private static String plain(Component component) {
        return component.getString().replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }

    private void reset() {
        inBedWars = false;
        ticksUntilRefresh = 0;
        for (BedWarsTeam team : BedWarsTeam.values()) {
            states.put(team, BedState.UNKNOWN);
        }
    }
}
