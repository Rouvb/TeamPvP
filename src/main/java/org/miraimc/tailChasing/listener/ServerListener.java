package org.miraimc.tailChasing.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.miraimc.tailChasing.TailChasing;

public class ServerListener implements Listener {

    private final TailChasing plugin;

    public ServerListener(TailChasing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoaded(ServerLoadEvent event) {
        plugin.getGameManager().loadLocation("lobby");
        plugin.getGameManager().loadLocation("game");
        plugin.getTeamManager().loadAllTeams();
    }
}
