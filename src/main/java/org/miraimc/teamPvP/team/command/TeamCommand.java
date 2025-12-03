package org.miraimc.teamPvP.team.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.PlayerUtil;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.List;

@CommandAlias("team|팀")
public class TeamCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(CommandSender sender) {
        String[] helpLines = {
                TextUtil.LONG_LINE,
                "&b&l팀 명령어 도움말:",
                "&b/team info &7 - &f팀 정보를 확인합니다.",
                "&b/team list &7 - &f팀 목록을 확인합니다.",
                "&b/team join &7 - &f팀에 참가합니다.",
                "&b/team leave &7 - &f팀에서 탈퇴합니다.",
                TextUtil.LONG_LINE
        };
        for (String line : helpLines) {
            sender.sendMessage(TextUtil.translate(line));
        }
    }

    @Subcommand("list|목록")
    public void onList(Player sender) {
        List<Team> teams = plugin.getTeamManager().getTeams();

        sender.sendMessage(TextUtil.LONG_LINE);
        sender.sendMessage(TextUtil.translate("&b&l팀 목록:"));
        for (Team team : teams) {
            String message = String.format("%s %s &7(%d)",
                    team.getPrefix(),
                    team.getDisplayName(),
                    team.getMembers().size());
            sender.sendMessage(TextUtil.translate(message));
        }
        sender.sendMessage(TextUtil.LONG_LINE);
    }

    @Subcommand("join|참가")
    public void onJoin(Player sender) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 팀에 참가할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getRandomTeam();
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c해당 팀을 찾을 수 없습니다!"));
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("forcejoin|강제참가")
    public void onForceJoin(Player sender, String teamName) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 팀에 참가할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c해당 팀을 찾을 수 없습니다!"));
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("leave|탈퇴")
    public void onLeave(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 팀에서 나갈 수 없습니다!"));
            return;
        }

        team.getMembers().remove(sender.getUniqueId());
        sender.sendMessage(TextUtil.translate("&c팀에서 성공적으로 탈퇴했습니다!"));
    }

    @Subcommand("info|정보")
    public void onInfo(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        String leaderName = PlayerUtil.getPlayerName(team.getLeader());

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&b&l팀 정보:")));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 팀: " + team.getPrefix() + " " + team.getDisplayName())));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 리더: " + team.getColor() + leaderName)));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 멤버 (" + team.getMembers().size() + "): " + plugin.getTeamHandler().parseTeamMembers(team))));
        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }

    @Subcommand("leader|리더")
    public void onLeader(Player sender, OnlinePlayer target) {
        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        if (!team.getLeader().equals(sender.getUniqueId())) {
            sender.sendMessage(TextUtil.translate("&c팀의 리더만 새로운 리더를 결정할 수 있습니다!"));
        } else {
            team.setLeader(target.getPlayer().getUniqueId());
            sender.sendMessage(TextUtil.translate("&f팀의 리더가 &6{leader} &f님으로 결정되었습니다!")
                    .replace("{leader}", target.getPlayer().getName())
            );
        }
    }

    @Subcommand("setdeathmatch")
    @CommandPermission("teampvp.admin")
    public void onDeathMatchAdd(Player sender, String teamName) {
        Location location = sender.getLocation();
        plugin.getTeamsFile().set("teams." + teamName + ".death_match_location", LocationUtil.serializeLocation(location));
        sender.sendMessage(TextUtil.translate("&aSuccessfully added death match location for &e" + teamName + "&a."));
    }
}
