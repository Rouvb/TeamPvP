package org.miraimc.teamPvP.profile.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.PlayerUtil;
import org.miraimc.teamPvP.util.TextUtil;

public class LobbyListener implements Listener {

    private final TeamPvP plugin;

    public LobbyListener(TeamPvP plugin) {
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
