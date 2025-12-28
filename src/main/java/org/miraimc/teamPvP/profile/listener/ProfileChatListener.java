package org.miraimc.teamPvP.profile.listener;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileChatStatus;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
import xyz.refinedev.phoenix.Phoenix;

public class ProfileChatListener implements Listener {

    private final TeamPvP plugin;

    public ProfileChatListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player);

        if (plugin.getServer().getPluginManager().isPluginEnabled("Phoenix")) {
            Phoenix phoenix = Phoenix.getInstance();
            if (phoenix.getProfileHandler().getProfile(player.getUniqueId()).isMuted()) {
                player.sendMessage(ColorParser.of("&cYou are currently muted.").build());
                event.setCancelled(true);
                return;
            }

            if (phoenix.getFilterHandler().isFiltered(event.getMessage())) {
                event.setCancelled(true);
                return;
            }
        }

        if (profile.getProfileChatStatus() == ProfileChatStatus.TEAM) {
            Team team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                profile.setProfileChatStatus(ProfileChatStatus.GLOBAL);
                player.sendMessage(Lang.PROFILE_CHAT_TOGGLE_CHANNEL.getMessage()
                        .replace("{channel}", "전체 채널"));
                event.setCancelled(true);
            } else {
                plugin.getTeamManager().sendMessage(
                        team,
                        Lang.PROFILE_CHAT_TEAM_CHAT.getMessage()
                                .replace("{team_color}", team.getColor())
                                .replace("{player}", player.getName())
                                .replace("{message}", event.getMessage())
                );
                event.setCancelled(true);
            }
        } else if (profile.getProfileChatStatus() == ProfileChatStatus.SPECTATOR) {
            if (profile.getStatus() == ProfileStatus.SPECTATING) {
                String message = event.getMessage();
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> plugin.getProfileManager().getProfile(p).getStatus() == ProfileStatus.SPECTATING)
                        .forEach(p -> p.sendMessage(ColorParser.of(Lang.PROFILE_CHAT_SPECTATOR_CHAT.getMessage())
                                .with("player", player.getName())
                                .with("message", message)
                                .papi(p)
                                .legacy()
                                .build()
                        ));
                event.setCancelled(true);
            }
        }
    }
}
