package org.miraimc.teamPvP.team.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.team.Team;

public class TeamMergeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    @Getter private final Team sourceTeam;
    @Getter private final Team targetTeam;

    public TeamMergeEvent(Team sourceTeam, Team targetTeam) {
        this.sourceTeam = sourceTeam;
        this.targetTeam = targetTeam;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
