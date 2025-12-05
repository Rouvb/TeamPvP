package org.miraimc.teamPvP.team.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.ChatChannel;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
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
                "&b/team info &7- &f팀 정보를 확인합니다.",
                "&b/team list &7- &f팀 목록을 확인합니다.",
                "&b/team join &7- &f팀에 참가합니다.",
                "&b/team leave &7- &f팀에서 탈퇴합니다.",
                "&b/team leader &7- &f팀 리더를 변경합니다.",
                "&b/team chat &7- &f채팅 채널을 변경합니다.",
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
            String message = String.format(" %s%s &7(%d): %s",
                    team.getPrefix(),
                    team.getDisplayName(),
                    team.getMembers().size(),
                    plugin.getTeamHandler().parseTeamMembers(team));
            sender.sendMessage(TextUtil.translate(message));
        }
        sender.sendMessage(TextUtil.LONG_LINE);
    }

    @Subcommand("join|참가")
    public void onJoin(Player sender) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 해당 명령어를 사용할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getRandomTeam();
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀을 찾을 수 없습니다!"));
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("forcejoin|강제참가")
    public void onForceJoin(Player sender, String teamName) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 해당 명령어를 사용할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀을 찾을 수 없습니다!"));
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("leave|탈퇴")
    public void onLeave(Player sender) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 해당 명령어를 사용할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        team.getMembers().remove(sender.getUniqueId());
    }

    @Subcommand("info|정보")
    public void onInfo(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        String leaderName = plugin.getProfileHandler().getPlayerName(team.getLeader());

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&b&l팀 정보:")));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 팀: " + team.getPrefix() + " " + team.getDisplayName())));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 리더: " + team.getColor() + leaderName)));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 멤버 (" + team.getMembers().size() + "): " + plugin.getTeamHandler().parseTeamMembers(team))));
        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }

    @Subcommand("leader|리더")
    public void onLeader(Player sender, OnlinePlayer target) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(TextUtil.translate("&c게임이 진행 중일 때는 해당 명령어를 사용할 수 없습니다!"));
            return;
        }

        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null || !team.getMembers().contains(target.getPlayer().getUniqueId())) {
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

    @Subcommand("chat|채팅")
    public void onChat(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        Profile profile = plugin.getProfileManager().getProfile(sender);
        if (team == null) {
            profile.setChatChannel(ChatChannel.GLOBAL);
            sender.sendMessage(TextUtil.translate("&c팀에 소속되어 있지 않습니다!"));
            return;
        }

        if (profile.getChatChannel() == ChatChannel.GLOBAL) {
            profile.setChatChannel(ChatChannel.TEAM);
            sender.sendMessage(TextUtil.translate("&f채팅 채널을 &b팀 채팅&f으로 변경했습니다."));
            return;
        }
        if (profile.getChatChannel() == ChatChannel.TEAM) {
            profile.setChatChannel(ChatChannel.GLOBAL);
            sender.sendMessage(TextUtil.translate("&f채팅 채널을 &b전채 채팅&f으로 변경했습니다."));
        }
    }
}
