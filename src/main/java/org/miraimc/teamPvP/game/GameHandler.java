package org.miraimc.teamPvP.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.*;

public class GameHandler {

    private final TeamPvP plugin;
    private final Map<Team, Location> randomTeleportLocations = new HashMap<>();

    public GameHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void preparePlayerForGame(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        plugin.getProfileHandler().resetInventory(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.sendMessage(TextUtil.translate("&a게임이 시작되었습니다!"));
        Title title = Title.title(
                Component.text(TextUtil.translate("&b&lTeam PvP")),
                Component.text(TextUtil.translate("&f게임이 시작되었습니다."))
        );
        plugin.getGameManager().TitleAll(title);
    }

    public void getRandomTeleportLocations() {
        Location worldLocation = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations.game"));
        World world = worldLocation.getWorld();
        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");
        int safeRange = Math.max(16, plugin.getConfigFile().getInt("rtp.range"));

        List<Team> teams = plugin.getTeamManager().getTeams();
        for (Team team : teams) {
            plugin.getRtpManager().findSafeLocationAsync(world, safeRange, maxTries)
                    .thenAccept(location -> {
                        if (location == null) {
                            plugin.getLogger().warning("Can't find safe location for " + team);
                            return;
                        }
                        randomTeleportLocations.put(team, location);
                    });
        }
    }

    public void teleportAllToGameWorld() {
        List<Team> teams = plugin.getTeamManager().getTeams();
        for (Team team : teams) {
            Set<UUID> membersId = team.getMembers();
            for (UUID uuid : membersId) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.teleport(randomTeleportLocations.get(team));
                }
            }
        }
    }

    public void teleportToLeader(Player member) {
        Team team = plugin.getTeamManager().getTeam(member);
        if (team == null || team.getLeader() == null) return;

        Player leader = Bukkit.getPlayer(team.getLeader());
        member.teleport(leader.getLocation());
    }

    public void sendDeathMatchAlert() {
        GameManager manager = plugin.getGameManager();
        manager.soundAll(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
        manager.messageAll(TextUtil.translate("&a데스매치가 시작되었습니다!"));
        Title title = Title.title(
                Component.text(TextUtil.translate("&b&l데스매치")),
                Component.text(TextUtil.translate("&f데스매치가 시작되었습니다!")));
        manager.TitleAll(title);
    }

    public void teleportAllToDeathMatch() {
        List<Team> teams = plugin.getTeamManager().getTeams();
        for (Team team : teams) {
            if (team.getLeader() == null || team.getDeathMatchLocation() == null) continue;
            Set<UUID> membersId = team.getMembers();
            for (UUID uuid : membersId) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null || !player.isOnline()) continue;
                player.teleport(team.getDeathMatchLocation());
            }
        }
    }

    public void handleEndGame(Team winnerTeam) {
        if (winnerTeam == null) {
            String[] messages = {
              TextUtil.LONG_LINE,
              "",
              TextUtil.translate("&b&l게임 종료!"),
              TextUtil.translate("&f 승리 팀: &b없음"),
              "",
              TextUtil.LONG_LINE,
            };
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(messages));
        } else {
            String[] messages = {
                    TextUtil.LONG_LINE,
                    "",
                    TextUtil.translate("&b&l게임 종료!"),
                    TextUtil.translate("&f 승리 팀: &b" + winnerTeam.getPrefix() + winnerTeam.getDisplayName()),
                    TextUtil.translate("&f 플레이어: &b" + plugin.getTeamHandler().parseTeamMembers(winnerTeam)),
                    "",
                    TextUtil.LONG_LINE,
            };
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(messages));
        }
    }
}
