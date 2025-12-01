package org.miraimc.tailChasing.profile.listener;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.tailChasing.TailChasing;

public class ProfileListener implements Listener {

    private final TailChasing plugin;

    public ProfileListener(TailChasing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().addProfile(player);


        FastBoard board = new FastBoard(player);
        plugin.getBoards().put(player.getUniqueId(), board);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileManager().removeProfile(player);

        FastBoard board = plugin.getBoards().get(player.getUniqueId());
        if (board != null) {
            board.delete();
        }
    }
}
