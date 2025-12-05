package org.miraimc.teamPvP.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.GameStatus;

import java.util.List;
import java.util.UUID;

public class TeamHandler {

    private final TeamPvP plugin;

    public TeamHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void handleTeam(Team newTeam, Player player) {
        TeamManager manager = plugin.getTeamManager();
        Team currentTeam = plugin.getTeamManager().getTeam(player);

        if (!player.isOnline() && currentTeam != null) {
            manager.removeMember(currentTeam, player);
            return;
        }

        // 게임 상태 확인
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            if (currentTeam != null) {
                UUID leaderId = currentTeam.getLeader();
                if (leaderId == null || !leaderId.equals(player.getUniqueId())) {
                    manager.removeMember(currentTeam, player);
                    if (newTeam != null) {
                        manager.addMember(newTeam, player);
                    }
                } else if (newTeam != null) {
                    manager.mergeTeams(currentTeam, newTeam);
                }
            }
            return;
        }

        if (currentTeam != null) {
            manager.removeMember(currentTeam, player);
        }

        if (newTeam != null) {
            manager.addMember(newTeam, player);
        }
    }

    public String parseTeamMembers(Team team) {
        if (team == null || team.getMembers() == null) {
            return ChatColor.GRAY + "None";
        }

        List<String> teamMembers = team.getMembers().stream()
                .map(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return team.getColor() + (player != null ? player.getName() : "알 수 없음");
                })
                .toList();
        return String.join(ChatColor.GRAY + ", ", teamMembers);
    }
}
