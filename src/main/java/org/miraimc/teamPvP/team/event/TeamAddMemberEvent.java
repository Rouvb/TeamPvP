package org.miraimc.teamPvP.team.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.team.Team;

public class TeamAddMemberEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final Team team;

    public TeamAddMemberEvent(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
