package org.miraimc.teamPvP.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.ChatChannel;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

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
                player.sendMessage(TextUtil.translate("&f채팅 채널을 &b전채 채팅&f으로 변경했습니다."));
                event.setCancelled(true);
            } else {
                plugin.getTeamManager().sendMessage(
                        team,
                        TextUtil.translate("&b&l[팀 채팅] {team_color}{player}&7: &f{message}")
                                .replace("{team_color}", team.getColor())
                                .replace("{player}", player.getName())
                                .replace("{message}", event.getMessage())
                );
                event.setCancelled(true);
            }
        }
    }
}
