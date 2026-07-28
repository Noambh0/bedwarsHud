package com.bedwarshud.tracker;

import com.bedwarshud.BedWarsHudClient;
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
            "(?i)(?:^|\\s)(red|blue|green|yellow|aqua|cyan|white|pink|gray|grey)\\s*[:\\-]?\\s*(.*)$");
    private static final Pattern BED_WARS = Pattern.compile("(?i)bed\\s*wars");
    private static final Pattern BED_DESTROYED_MESSAGE = Pattern.compile(
            "(?i)\\bbed\\s+destruction\\b.*?\\b"
                    + "(red|blue|green|yellow|aqua|cyan|white|pink|gray|grey)"
                    + "(?:['’]s)?\\s+bed\\b");
    private static final Pattern TEAM_ELIMINATED_MESSAGE = Pattern.compile(
            "(?i)\\bteam\\s+eliminated\\b.*?\\b"
                    + "(red|blue|green|yellow|aqua|cyan|white|pink|gray|grey)"
                    + "\\s+team\\b");
    private static final Pattern PLAYER_COUNT = Pattern.compile("\\b(\\d+)\\b");

    private final EnumMap<BedWarsTeam, BedState> states = new EnumMap<>(BedWarsTeam.class);
    private boolean inBedWars;
    private boolean supportedMode;
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
        EnumMap<BedWarsTeam, BedState> observed = new EnumMap<>(BedWarsTeam.class);

        for (PlayerScoreEntry entry : scoreboard.listPlayerScores(sidebar)) {
            PlayerTeam team = scoreboard.getPlayersTeam(entry.owner());
            Component line = entry.display() != null ? entry.display() : Component.literal(entry.owner());
            Component decorated = PlayerTeam.formatNameForTeam(team, line);
            String text = plain(decorated);
            visibleText.append('\n').append(text);
            parseTeamLine(text, observed);
        }

        boolean detectedBedWars = BED_WARS.matcher(visibleText).find();
        if (!detectedBedWars) {
            if (inBedWars) {
                resetStates();
            }
            inBedWars = false;
            supportedMode = false;
            return;
        }

        if (!inBedWars) {
            resetStates();
        }
        inBedWars = true;

        // Solo and Doubles are the only regular modes with all eight Bed Wars teams.
        // Lobbies and 2/4-team modes never expose eight team-status rows.
        if (observed.size() == BedWarsTeam.values().length) {
            supportedMode = true;
        } else {
            supportedMode = false;
        }

        // A broken bed cannot become alive again during the same match. This also
        // prevents a one-tick-old scoreboard line from undoing a chat-message update.
        observed.forEach((team, observedState) -> {
            BedState currentState = states.get(team);
            if (currentState != BedState.ELIMINATED
                    && (observedState == BedState.ELIMINATED
                    || currentState != BedState.BED_DESTROYED
                    || observedState == BedState.BED_DESTROYED)) {
                states.put(team, observedState);
            }
        });
    }

    public boolean isInBedWars() {
        return inBedWars && supportedMode;
    }

    /**
     * Hypixel announces bed destructions as game messages. Reading that client-visible
     * message is more reliable than waiting for every sidebar variant to update.
     */
    public void handleGameMessage(Component message) {
        String text = plain(message);
        Matcher eliminatedMatcher = TEAM_ELIMINATED_MESSAGE.matcher(text);
        if (eliminatedMatcher.find()) {
            BedWarsTeam team = teamFromName(eliminatedMatcher.group(1));
            if (team != null) {
                states.put(team, BedState.ELIMINATED);
                BedWarsHudClient.LOGGER.info("Detected {} team elimination from Hypixel message", team);
            }
            return;
        }

        Matcher destroyedMatcher = BED_DESTROYED_MESSAGE.matcher(text);
        if (destroyedMatcher.find()) {
            BedWarsTeam team = teamFromName(destroyedMatcher.group(1));
            if (team != null) {
                states.put(team, BedState.BED_DESTROYED);
                inBedWars = true;
                BedWarsHudClient.LOGGER.info("Detected {} bed destruction from Hypixel message", team);
            }
        } else if (text.toLowerCase(Locale.ROOT).contains("bed destruction")) {
            BedWarsHudClient.LOGGER.warn("Could not identify team in Bed Wars message: {}", text);
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

        Matcher playerCount = PLAYER_COUNT.matcher(status);
        if (status.isBlank() || status.contains("✕") || status.contains("×") || status.contains("dead")
                || status.contains("eliminated")) {
            target.put(team, BedState.ELIMINATED);
        } else if (playerCount.find()) {
            int remainingPlayers = Integer.parseInt(playerCount.group(1));
            target.put(team, remainingPlayers > 0 ? BedState.BED_DESTROYED : BedState.ELIMINATED);
        } else if (status.contains("destroyed")) {
            target.put(team, BedState.BED_DESTROYED);
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
        supportedMode = false;
        ticksUntilRefresh = 0;
        resetStates();
    }

    private void resetStates() {
        for (BedWarsTeam team : BedWarsTeam.values()) {
            states.put(team, BedState.UNKNOWN);
        }
    }
}
