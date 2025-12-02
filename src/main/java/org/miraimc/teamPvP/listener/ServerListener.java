package org.miraimc.teamPvP.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.miraimc.teamPvP.TeamPvP;

public class ServerListener implements Listener {

    private final TeamPvP plugin;

    public ServerListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerLoaded(ServerLoadEvent event) {
        plugin.getGameManager().loadLocation("lobby");
        plugin.getGameManager().loadLocation("game");
        plugin.getTeamManager().loadAllTeams();
    }
}
