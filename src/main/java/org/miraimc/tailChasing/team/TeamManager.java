package org.miraimc.tailChasing.team;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class TeamManager {

    private final List<Team> teams = new ArrayList<>();

    private final TailChasing plugin;

    public TeamManager(TailChasing plugin) {
        this.plugin = plugin;
    }

    public Team getTeam(String teamName) {
        return teams.stream()
                .filter(team -> team.getName().equalsIgnoreCase(teamName))
                .findFirst()
                .orElse(null);
    }

    public Team getTeam(Player player) {
        return teams.stream()
                .filter(team -> team.getMembers().contains(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public void loadAllTeams() {
        teams.clear();
        ConfigurationSection section = plugin.getTeamsFile().getConfigurationSection("teams");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            String key = "teams." + name + ".";
            String id = plugin.getTeamsFile().getString(key + "id");
            String displayName = plugin.getTeamsFile().getString(key + "display_name");
            String prefix = plugin.getTeamsFile().getString(key + "prefix");
            String color = plugin.getTeamsFile().getString(key + "color");
            createTeam(id, displayName, prefix, color);
        }
    }

    public boolean createTeam(String teamName, String teamDisplayName, String teamPrefix, String teamColor) {
        if (getTeam(teamName) != null) return false;
        Team newTeam = new Team(plugin, teamName, teamDisplayName, teamPrefix, teamColor);
        teams.add(newTeam);
        return true;
    }

    public void removeTeam(String teamName) {
        teams.removeIf(team -> team.getName().equalsIgnoreCase(teamName));
    }

    public void addMember(Team team, Player player) {
        team.getMembers().add(player.getUniqueId());
        team.ensureLeader(player.getUniqueId());
        sendMessage(team, TextUtil.translate("{team_prefix} {player} &f님이 팀에 합류했습니다!")
                .replace("{team_prefix}", team.getPrefix())
                .replace("{player}", player.getName()));
    }

    public void removeMember(Team team, Player player) {
        team.getMembers().remove(player.getUniqueId());
        sendMessage(team, TextUtil.translate("{team_prefix} {player} &f님이 팀에서 떠났습니다!")
                .replace("{team_prefix}", team.getPrefix())
                .replace("{player}", player.getName()));

        // 리더였을 경우 자동 교체
        if (team.getLeader() != null && team.getLeader().equals(player.getUniqueId())) {
            UUID newLeader = team.getMembers().stream().findAny().orElse(null);
            team.setLeader(newLeader);
        }
    }

    public void mergeTeams(Team source, Team target) {
        target.getMembers().addAll(source.getMembers());
        source.getMembers().clear();
        source.setLeader(null);

        Bukkit.broadcastMessage(TextUtil.translate("{team_prefix} {team_display_name} &f팀이 {new_team_prefix} {new_team_display_name} &f팀으로 흡수되었습니다!")
                .replace("{team_prefix}", source.getPrefix())
                .replace("{team_display_name}", source.getDisplayName())
                .replace("{new_team_prefix}", target.getPrefix())
                .replace("{new_team_display_name}", target.getDisplayName()));
    }

    public void sendMessage(Team team, String message) {
        team.getMembers().forEach(memberId -> {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(TextUtil.translate(message));
            }
        });
    }
}
