package org.miraimc.teamPvP.lobby.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.lobby.item.LobbyItem;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.profile.event.ProfileAddEvent;
import org.miraimc.teamPvP.util.TextUtil;

public class LobbyListener implements Listener {

    private final TeamPvP plugin;

    public LobbyListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    private void giveLobbyItems(Player player) {
        player.getInventory().setItem(3, LobbyItem.TEAM_SELECTOR);
        player.getInventory().setItem(5, LobbyItem.GAME_INFO_BOOK);
    }

    @EventHandler()
    public void onProfileAdd(ProfileAddEvent event) {
        if (event.getProfile().getStatus() == ProfileStatus.WAITING) {
            Player player = event.getPlayer();

            plugin.getProfileHandler().resetInventory(player);
            player.getInventory().setHeldItemSlot(4);
            giveLobbyItems(player);
            Location location = plugin.getGame().getLobbyLocation();

            if (location == null) {
                player.sendMessage(TextUtil.translate("&cYou must specify a lobby location!"));
                return;
            }

            player.teleport(location);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile.getStatus() == ProfileStatus.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack clickedItem = event.getItem();
        if (clickedItem != null) {
            if (clickedItem.isSimilar(LobbyItem.TEAM_SELECTOR)) {
                Bukkit.getServer().dispatchCommand(player, "team join");
            } else if (clickedItem.isSimilar(LobbyItem.GAME_INFO_BOOK)) {
                player.openBook(LobbyItem.GAME_INFO_BOOK);
            }
        }
    }
}
