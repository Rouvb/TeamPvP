package org.miraimc.teamPvP.team.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.team.event.TeamAddMemberEvent;
import org.miraimc.teamPvP.team.event.TeamRemoveMemberEvent;

public class TeamListener implements Listener {

    private final TeamPvP plugin;

    public TeamListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            plugin.getTeamManager().removeMember(team, player);
        }
    }

    @EventHandler
    public void onTeamMemberJoin(TeamAddMemberEvent event) {
        Team team = event.getTeam();
        Player player = event.getPlayer();

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
        plugin.getTeamManager().sendMessage(
                team,
                Lang.TEAM_STATUS_MEMBER_JOINED.getMessage()
                        .replace("{team_display_name}", team.getDisplayName())
                        .replace("{team_prefix}", team.getPrefix())
                        .replace("{team_color}", team.getColor())
                        .replace("{player}", player.getName())
        );
    }

    @EventHandler
    public void onTeamMemberLeave(TeamRemoveMemberEvent event) {
        Team team = event.getTeam();
        Player player = event.getPlayer();

        player.sendMessage(Lang.TEAM_STATUS_LEAVE.getMessage());
        plugin.getTeamManager().sendMessage(
                team,
                Lang.TEAM_STATUS_MEMBER_LEFT.getMessage()
                        .replace("{team_display_name}", team.getDisplayName())
                        .replace("{team_prefix}", team.getPrefix())
                        .replace("{team_color}", team.getColor())
                        .replace("{player}", player.getName())
        );
    }
}
