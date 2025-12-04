package org.miraimc.teamPvP.team;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Getter
public class TeamManager {

    private final List<Team> teams = new ArrayList<>();

    private final TeamPvP plugin;

    public TeamManager(TeamPvP plugin) {
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

    public Team getRandomTeam() {
        int minSize = teams.stream()
                .mapToInt(team -> team.getMembers().size())
                .min()
                .orElse(Integer.MAX_VALUE);

        List<Team> candidates = teams.stream()
                .filter(team -> team.getMembers().size() == minSize)
                .toList();

        return candidates.isEmpty() ? null :
                candidates.get(new Random().nextInt(candidates.size()));
    }

    public void loadTeam(String teamId) {
        String path = "teams." + teamId + ".";
        String displayName = plugin.getTeamsFile().getString(path + "display_name");
        String prefix = plugin.getTeamsFile().getString(path + "prefix");
        String color = plugin.getTeamsFile().getString(path + "color");
        int weight = plugin.getTeamsFile().getInt(path + "weight");
        createTeam(teamId, displayName, prefix, color, weight);
        plugin.getLogger().info("Team " + teamId + " loaded.");
    }

    public void loadAllTeams() {
        teams.clear();
        ConfigurationSection section = plugin.getTeamsFile().getConfigurationSection("teams");
        if (section == null) return;

        for (String name : section.getKeys(false)) {
            loadTeam(name);
        }
    }

    public boolean createTeam(String teamName, String teamDisplayName, String teamPrefix, String teamColor, int weight) {
        if (getTeam(teamName) != null) return false;
        Team newTeam = new Team(plugin, teamName, teamDisplayName, teamPrefix, teamColor, weight);
        teams.add(newTeam);
        return true;
    }

    public Team getWinnerTeam() {
        List<Team> aliveTeams = teams.stream()
                .filter(team -> !team.getAliveMembers().isEmpty())
                .toList();

        return aliveTeams.size() == 1 ? aliveTeams.get(0) : null;
    }

    public void removeTeam(String teamName) {
        teams.removeIf(team -> team.getName().equalsIgnoreCase(teamName));
    }

    public void addMember(Team team, Player player) {
        team.getMembers().add(player.getUniqueId());
        team.getAliveMembers().add(player.getUniqueId());
        team.ensureLeader(player.getUniqueId());
    }

    public void removeMember(Team team, Player player) {
        team.getMembers().remove(player.getUniqueId());
        team.getAliveMembers().remove(player.getUniqueId());

        // 리더였을 경우 자동 교체
        if (team.getLeader() != null && team.getLeader().equals(player.getUniqueId())) {
            UUID newLeader = team.getMembers().stream().findAny().orElse(null);
            team.setLeader(newLeader);
        }
    }

    public void mergeTeams(Team source, Team target) {
        target.getMembers().addAll(source.getMembers());
        target.getAliveMembers().addAll(source.getAliveMembers());
        source.getMembers().clear();
        source.getAliveMembers().clear();
        source.setLeader(null);

        Bukkit.broadcastMessage(TextUtil.translate("{team_prefix}{team_display_name} &f팀이 {new_team_prefix}{new_team_display_name} &f팀으로 흡수되었습니다!")
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
