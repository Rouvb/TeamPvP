package org.miraimc.teamPvP.profile.listener;

import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.util.VisibilityUtil;

public class ProfileListener implements Listener {

    private final TeamPvP plugin;

    public ProfileListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Profile profile;

        if (!plugin.getGame().getGameStatus().equals(GameStatus.LOBBY)) {
            profile = plugin.getProfileManager().addProfile(player, ProfileStatus.SPECTATING);
        } else {
            profile = plugin.getProfileManager().addProfile(player, ProfileStatus.WAITING);
        }

        FastBoard board = new FastBoard(player);
        plugin.getBoards().put(player.getUniqueId(), board);

        VisibilityUtil.updatePlayerVisibility(player, profile.getStatus());
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

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile != null) {
            VisibilityUtil.updatePlayerVisibility(player, profile.getStatus());
        }
    }
}
