package org.miraimc.teamPvP.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.team.Team;

import java.util.*;

public class GameHandler {

    private final TeamPvP plugin;

    public GameHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void preparePlayerForGame(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            plugin.getGameManager().setSpectatorMode(player);
            return;
        }

        plugin.getProfileHandler().resetInventory(player);
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.sendMessage(Lang.GAME_STATUS_STARTED_MESSAGE.getMessage());
        Title title = Title.title(
                Component.text(Lang.GAME_STATUS_STARTED_TITLE.getMessage()),
                Component.text(Lang.GAME_STATUS_STARTED_SUBTITLE.getMessage())
        );
        plugin.getGameManager().TitleAll(title);
    }

    public void teleportPlayerToRandomLocation(Player player) {
        List<Location> availableLocations = plugin.getRtpManager().getLocations();

        if (availableLocations.isEmpty()) {
            plugin.getLogger().warning("No available RTP locations for " + player.getName());
            return;
        }

        Location randomLocation = availableLocations.remove(0);

        if (player != null && player.isOnline()) {
            player.teleport(randomLocation);
            plugin.getLogger().info("Teleported " + player.getName() + " to " + randomLocation);
        }
    }


    public void teleportAllToGameWorld() {
        List<Team> teams = plugin.getTeamManager().getTeams();

        for (Team team : teams) {
            UUID leaderId = team.getLeader();
            if (leaderId == null) continue;

            Player leader = Bukkit.getPlayer(leaderId);
            if (leader == null || !leader.isOnline()) continue;

            // Teleport leader to random location
            teleportPlayerToRandomLocation(leader);

            // Teleport members to leader location
            for (UUID uuid : team.getMembers()) {
                if (uuid.equals(leaderId)) continue;
                Player member = Bukkit.getPlayer(uuid);
                if (member != null && member.isOnline()) {
                    member.teleport(leader.getLocation());
                }
            }
        }
    }


    public void sendDeathMatchAlert() {
        GameManager manager = plugin.getGameManager();
        manager.soundAll(Sound.ENTITY_ENDER_DRAGON_AMBIENT);
        manager.messageAll(Lang.GAME_STATUS_DEATHMATCH_MESSAGE.getMessage());
        Title title = Title.title(
                Component.text(Lang.GAME_STATUS_DEATHMATCH_TITLE.getMessage()),
                Component.text(Lang.GAME_STATUS_DEATHMATCH_SUBTITLE.getMessage()));
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
        String teamColor = winnerTeam != null ? winnerTeam.getColor() : "";
        String teamDisplayName = winnerTeam != null ? winnerTeam.getDisplayName() : Lang.NONE.getMessage();

        Title title = Title.title(
                Component.text(Lang.GAME_END_TITLE.getMessage()
                        .replace("{team_color}", teamColor)
                        .replace("{team_display_name}", teamDisplayName)
                ),
                Component.text(Lang.GAME_END_SUBTITLE.getMessage()
                        .replace("{team_color}", teamColor)
                        .replace("{team_display_name}", teamDisplayName)
                )
        );

        String message = Lang.GAME_END_MESSAGE.getMessage()
                .replace("{team_color}", teamColor)
                .replace("{team_display_name}", teamDisplayName);

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(message);
            player.showTitle(title);
        });
    }
}
