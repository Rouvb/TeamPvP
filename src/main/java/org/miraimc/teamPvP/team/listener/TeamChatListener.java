package org.miraimc.teamPvP.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.profile.ChatChannel;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;

public class TeamChatListener implements Listener {

    private final TeamPvP plugin;

    public TeamChatListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = plugin.getProfileManager().getProfile(player);

        if (profile.getChatChannel().equals(ChatChannel.TEAM)) {
            Team team = plugin.getTeamManager().getTeam(player);
            if (team == null) {
                profile.setChatChannel(ChatChannel.GLOBAL);
                player.sendMessage(Lang.TEAM_CHAT_TOGGLE_CHANNEL.getMessage()
                        .replace("{channel}", "전체 채널"));
                event.setCancelled(true);
            } else {
                plugin.getTeamManager().sendMessage(
                        team,
                        Lang.TEAM_CHAT_MESSAGE_FORMAT.getMessage()
                                .replace("{team_color}", team.getColor())
                                .replace("{player}", player.getName())
                                .replace("{message}", event.getMessage())
                );
                event.setCancelled(true);
            }
        }
    }
}
