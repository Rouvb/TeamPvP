package org.miraimc.teamPvP.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.PlayerUtil;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GameHandler {

    private final TeamPvP plugin;

    public GameHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void preparePlayerForGame(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        PlayerUtil.resetInventory(player, team.getLeader().equals(player.getUniqueId()));
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

    public void teleportTeamRandomWorld(Team team) {
        if (team == null || team.getLeader() == null) return;

        Player leader = Bukkit.getPlayer(team.getLeader());
        Location worldLocation = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations.lobby"));
        World world = worldLocation.getWorld();
        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");
        int safeRange = Math.max(16, plugin.getConfigFile().getInt("rtp.range"));

        plugin.getRtpManager().findSafeLocationAsync(world, safeRange, maxTries)
                .thenAccept(location -> {
                    if (location == null) {
                        leader.sendMessage(TextUtil.translate("&c안전한 위치를 찾지 못했습니다!"));
                        return;
                    }

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        leader.teleport(location);
                        team.getMembers().forEach(uuid -> {
                            if (!uuid.equals(leader.getUniqueId())) {
                                Player member = Bukkit.getPlayer(uuid);
                                if (member != null && member.isOnline()) {
                                    member.teleport(location);
                                }
                            }
                        });
                    });
                });
    }

    public void teleportToLeader(Player member) {
        Team team = plugin.getTeamManager().getTeam(member);
        if (team == null || team.getLeader() == null) return;

        Player leader = Bukkit.getPlayer(team.getLeader());
        member.teleport(leader.getLocation());
        member.sendMessage(TextUtil.translate("&a팀 리더에게 텔레포트 되었습니다."));
    }

    public void sendDeathMatchAlert() {
        org.miraimc.teamPvP.game.GameManager manager = plugin.getGameManager();
        manager.soundAll(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
        manager.messageAll(TextUtil.translate("&a데스 매치가 시작되었습니다!"));
        Title title = Title.title(
                Component.text(TextUtil.translate("&b&l데스 매치")),
                Component.text(TextUtil.translate("&f데스 매치가 시작되었습니다!")));
        manager.TitleAll(title);
    }

    public void teleportAllToDeathMatch() {
        List<Team> teams = plugin.getTeamManager().getTeams();
        for (Team team : teams) {
            if (team.getLeader() == null || team.getDeathMatchLocation() == null) continue;
            Set<UUID> membersId = team.getMembers();
            for (UUID uuid : membersId) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null || !player.isOnline()) continue;
                player.teleport(team.getDeathMatchLocation());
            }
        }
    }

    public void setSpectatorMode(Player player) {
        PlayerUtil.resetInventory(player, false);
    }

}
