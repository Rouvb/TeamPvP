package org.miraimc.teamPvP.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.GameStatus;
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

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        GameStatus gameStatus = plugin.getGame().getGameStatus();
        Player player = event.getPlayer();

        if (gameStatus != GameStatus.LOBBY) {
            if (!player.hasPermission("teampvp.admin")) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "게임이 진행 중일 때는 접속할 수 없습니다.");
            }
        } else {
            if (plugin.getChunky().isRunning("world")) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "월드가 생성 중입니다.");
            }
        }
    }

    private void preGenerateChunks() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Chunky")) return;
        ChunkyAPI chunky = plugin.getChunky();
        int rtpRange = plugin.getGameSettings().getRtpRange();
        chunky.startTask("world", "square", 0, 0, rtpRange, rtpRange, "concentric");
        chunky.onGenerationComplete(event -> plugin.getLogger().info("Chunk generation completed."));
    }
}
