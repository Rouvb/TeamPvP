package org.miraimc.teamPvP.spectator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.List;
import java.util.stream.Collectors;

public class SpectatorListener implements Listener {

    private final TeamPvP plugin;

    public SpectatorListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack clickedItem = event.getItem();
        if (clickedItem != null) {
            if (clickedItem.isSimilar(SpectatorItem.SPECTATOR_MENU)) {
                List<Player> alivePlayers = Bukkit.getOnlinePlayers().stream()
                        .filter(p -> TeamPvP.getInstance().getProfileManager().getProfile(p).getStatus() == ProfileStatus.PLAYING)
                        .collect(Collectors.toList());
                SpectatorMenu.open(player, alivePlayers);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile.getStatus() == ProfileStatus.SPECTATING) {
            String message = event.getMessage();
            Bukkit.getOnlinePlayers().stream()
                            .filter(p -> plugin.getProfileManager().getProfile(p).getStatus() == ProfileStatus.SPECTATING)
                            .forEach(p -> p.sendMessage(TextUtil.translate("&7[관전자] " + player.getName() + ": " + message)));
            event.setCancelled(true);
        }
    }
}
