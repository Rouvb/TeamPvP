package org.miraimc.teamPvP.profile.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.profile.Profile;

public class ProfileAddEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final Player player;
    @Getter private final Profile profile;

    public ProfileAddEvent(Player player, Profile profile) {
        this.player = player;
        this.profile = profile;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
