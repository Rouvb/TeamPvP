package org.miraimc.teamPvP.profile.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.profile.Profile;

public class ProfileStatusEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    @Getter private final Profile profile;

    public ProfileStatusEvent(Profile profile) {
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
