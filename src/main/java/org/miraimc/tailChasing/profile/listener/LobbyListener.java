package org.miraimc.tailChasing.profile.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.util.LocationUtil;
import org.miraimc.tailChasing.util.PlayerUtil;
import org.miraimc.tailChasing.util.TextUtil;

public class LobbyListener implements Listener {

    private final TailChasing plugin;

    public LobbyListener(TailChasing plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerUtil.resetInventory(player, false);
        player.getInventory().setHeldItemSlot(4);

        Location lobbyLocation = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations.lobby"));
        if (lobbyLocation == null) {
            player.sendMessage(TextUtil.translate("&cYou must specify a lobby location!"));
            return;
        }

        player.teleport(lobbyLocation);
    }

}
