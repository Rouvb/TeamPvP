package org.miraimc.teamPvP.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.miraimc.teamPvP.TeamPvP;

public class ServerListener implements Listener {

    private final TeamPvP plugin;
    private boolean serverLoaded;

    public ServerListener(TeamPvP plugin) {
        this.plugin = plugin;
        this.serverLoaded = false;
    }

    @EventHandler
    public void onServerLoaded(ServerLoadEvent event) {
        plugin.getGameManager().setLocation("lobby");
        plugin.getGameManager().setLocation("game");
        plugin.getGameManager().setLocation("death_match");
        plugin.getTeamManager().loadAllTeams();
        serverLoaded = true;
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (!serverLoaded) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "서버가 로드 중입니다.");
        }
    }
}
