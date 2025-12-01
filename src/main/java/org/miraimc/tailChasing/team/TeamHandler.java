package org.miraimc.tailChasing.team;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.game.GameStatus;
import org.miraimc.tailChasing.util.TextUtil;

import java.util.List;

public class TeamHandler {

    private final TailChasing plugin;

    public TeamHandler(TailChasing plugin) {
        this.plugin = plugin;
    }

    public void handleTeam(Team newTeam, Player player) {
        TeamManager manager = plugin.getTeamManager();
        Team currentTeam = plugin.getTeamManager().getTeam(player);

        if (!player.isOnline()) {
            manager.removeMember(currentTeam, player);
        }

        // 게임 상태 확인
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            if (currentTeam != null) {
                if (!currentTeam.getLeader().equals(player.getUniqueId())) {
                    manager.removeMember(currentTeam, player);
                    if (newTeam != null) {
                        manager.addMember(newTeam, player);
                    }
                } else {
                    manager.mergeTeams(currentTeam, newTeam);
                }
            }
            return;
        }

        // 로비 상태일 때
        if (currentTeam != null) {
            manager.removeMember(currentTeam, player);
        }

        if (newTeam != null) {
            manager.addMember(newTeam, player);
        } else {
            player.sendMessage(TextUtil.translate("&a팀에서 떠났습니다!"));
        }
    }

    public String parseTeamMembers(Team team) {
        List<String> teamMembers = team.getMembers().stream()
                .map(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    return team.getColor() + (player != null ? player.getName() : "알 수 없음");
                })
                .toList();
        return String.join(ChatColor.GRAY + ", ", teamMembers);
    }
}
