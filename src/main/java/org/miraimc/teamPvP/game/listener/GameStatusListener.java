package org.miraimc.teamPvP.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.event.GameDeathMatchEvent;
import org.miraimc.teamPvP.game.event.GameStartEvent;
import org.miraimc.teamPvP.profile.Profile;

public class GameStatusListener implements Listener {

    private final TeamPvP plugin;

    public GameStatusListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = plugin.getProfileManager().getProfile(player);
            if (profile == null) continue;
            plugin.getGameHandler().preparePlayerForGame(player);
        }

        plugin.getGameHandler().teleportAllToGameWorld();
    }

    @EventHandler
    public void onGameDeathMatch(GameDeathMatchEvent event) {
        plugin.getGameHandler().sendDeathMatchAlert();
        plugin.getGameHandler().teleportAllToDeathMatch();
    }
}