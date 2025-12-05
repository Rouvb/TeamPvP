package org.miraimc.teamPvP.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.team.event.TeamAddMemberEvent;
import org.miraimc.teamPvP.team.event.TeamRemoveMemberEvent;
import org.miraimc.teamPvP.util.TextUtil;

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

        plugin.getTeamManager().sendMessage(
                team,
                TextUtil.translate("{team_prefix}{player} &f님이 팀에 합류했습니다.")
                        .replace("{team_prefix}", team.getPrefix())
                        .replace("{player}", player.getName())
        );
    }

    @EventHandler
    public void onTeamMemberLeave(TeamRemoveMemberEvent event) {
        Team team = event.getTeam();
        Player player = event.getPlayer();

        player.sendMessage(TextUtil.translate("&c팀에서 떠났습니다."));
        plugin.getTeamManager().sendMessage(
                team,
                TextUtil.translate("{team_prefix}{player} &f님이 팀에서 떠났습니다.")
                        .replace("{team_prefix}", team.getPrefix())
                        .replace("{player}", player.getName())
        );
    }
}
