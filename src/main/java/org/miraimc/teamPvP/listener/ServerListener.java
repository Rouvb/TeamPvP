package org.miraimc.teamPvP.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.popcraft.chunky.api.ChunkyAPI;

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
        preGenerateChunks();
        serverLoaded = true;
    }

    @EventHandler
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (!serverLoaded) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "서버가 로드 중입니다.");
        }
    }

    private void preGenerateChunks() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Chunky")) return;
        ChunkyAPI chunky = plugin.getChunky();
        int rtpRange = plugin.getConfigFile().getInt("rtp.range");
        chunky.startTask("world", "square", 0, 0, rtpRange, rtpRange, "concentric");
        chunky.onGenerationComplete(event -> plugin.getLogger().info("Chunk generation completed."));
    }
}
