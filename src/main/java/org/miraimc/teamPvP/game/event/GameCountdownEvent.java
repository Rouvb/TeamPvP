package org.miraimc.teamPvP.game.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.game.Game;

public class GameCountdownEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    @Getter private final Game game;

    public GameCountdownEvent(Game game) {
        this.game = game;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
