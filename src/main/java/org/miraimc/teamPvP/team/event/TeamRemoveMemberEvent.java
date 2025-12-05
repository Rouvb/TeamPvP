package org.miraimc.teamPvP.team.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.team.Team;

public class TeamRemoveMemberEvent extends Event {

    private static HandlerList HANDLERS = new HandlerList();
    @Getter private final Player player;
    @Getter private final Team team;

    public TeamRemoveMemberEvent(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
