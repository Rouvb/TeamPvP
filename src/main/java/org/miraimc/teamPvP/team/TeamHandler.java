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

        if (!player.isOnline()) {
            if (currentTeam != null) {
                manager.removeMember(currentTeam, player);
            }
        }

        GameStatus gameStatus = plugin.getGame().getGameStatus();

        // 로비 상태: 자유롭게 팀 변경 가능
        if (gameStatus == GameStatus.LOBBY) {
            if (currentTeam != null) {
                manager.removeMember(currentTeam, player);
            }
            if (newTeam != null) {
                manager.addMember(newTeam, player);
            }
            return;
        }

        // 게임 중 또는 데스매치: 특수 처리
        if (gameStatus == GameStatus.PLAYING || gameStatus == GameStatus.DEATH_MATCH) {
            // 현재 팀이 없으면 아무 처리도 하지 않음
            if (currentTeam == null) {
                return;
            }

            UUID leaderId = currentTeam.getLeader();
            boolean isLeader = leaderId != null && leaderId.equals(player.getUniqueId());

            if (isLeader && newTeam != null) {
                // 리더가 다른 팀으로 이동: 팀 병합
                plugin.getLogger().info("Leader " + player.getName() + " moving. Merging " +
                        currentTeam.getName() + " into " + newTeam.getName());
                manager.mergeTeams(currentTeam, newTeam);
            } else if (!isLeader) {
                // 일반 팀원: 단순 이동
                manager.removeMember(currentTeam, player);
                if (newTeam != null) {
                    manager.addMember(newTeam, player);
                }
            }
        }
    }

    public String parseTeamMembers(Team team) {
        if (team == null || team.getMembers() == null) {
            return ChatColor.GRAY + "None";
        }

        List<String> teamMembers = team.getAliveMembers().stream()
                .map(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return team.getColor() + (player != null ? player.getName() : "알 수 없음");
                })
                .toList();
        return String.join(ChatColor.GRAY + ", ", teamMembers);
    }
}
