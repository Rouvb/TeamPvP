package org.miraimc.teamPvP.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.rtp.RTPManager;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.VisibilityUtil;

import java.util.*;

public class GameHandler {

    private final TeamPvP plugin;

    public GameHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void preparePlayerForGame(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) {
            handleSpectator(player);
            Location gameLocation = plugin.getGame().getGameLocation();
            if (gameLocation != null && gameLocation.getWorld() != null) {
                Location highestLocation = gameLocation.getWorld()
                        .getHighestBlockAt(gameLocation)
                        .getLocation()
                        .add(0, 1, 0);
                player.teleport(highestLocation);
            }
            return;
        }

        plugin.getProfileManager().getProfile(player).setStatus(ProfileStatus.PLAYING);
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

    public void teleportAllToGameWorld() {
        List<Team> teams = plugin.getTeamManager().getTeams();

        for (Team team : teams) {
            UUID leaderId = team.getLeader();
            if (leaderId == null) continue;

            Player leader = Bukkit.getPlayer(leaderId);
            if (leader == null || !leader.isOnline()) continue;

            // Teleport leader to random location
            String worldName = "world";
            int range = plugin.getConfigFile().getInt("rtp.range");
            RTPManager.teleportPlayer(leader, worldName, range);

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

    public void handleSpectator(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile != null) {
            profile.setStatus(ProfileStatus.SPECTATING);
        }

        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            team.getAliveMembers().remove(player.getUniqueId());
        }

        Title title = Title.title(
                Component.text(Lang.GAME_DEATH_SPECTATOR_TITLE.getMessage()),
                Component.text(Lang.GAME_DEATH_SPECTATOR_SUBTITLE.getMessage())
        );
        player.showTitle(title);

        VisibilityUtil.updatePlayerVisibility(player, ProfileStatus.SPECTATING);
        plugin.getProfileHandler().resetInventory(player);
    }

    /**
     * 플레이어가 죽었을 경우를 담당하는 메서드
     * @param victim
     * @param killer
     */
    public void handleDeath(Player victim, @Nullable Player killer) {
        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        if (victimTeam == null) return;

        Game game = plugin.getGame();
        GameStatus gameStatus = game.getGameStatus();

        // 데스매치 중이면 관전자로 전환
        if (gameStatus == GameStatus.DEATH_MATCH) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                victim.spigot().respawn();
                handleSpectator(victim);
            }, 4L);
            return;
        }

        // 게임 중이 아니면 무시
        if (gameStatus != GameStatus.PLAYING) {
            return;
        }

        boolean isLeader = victimTeam.getLeader() != null &&
                victimTeam.getLeader().equals(victim.getUniqueId());

        // 킬러가 있고 다른 팀인 경우
        if (killer != null && !killer.equals(victim)) {
            Team killerTeam = plugin.getTeamManager().getTeam(killer);

            // 다른 팀에게 죽은 경우
            if (killerTeam != null && !killerTeam.equals(victimTeam)) {
                if (isLeader) {
                    // 리더가 죽으면 팀 전체 흡수
                    handleLeaderDeath(victimTeam, killerTeam, victim);
                } else {
                    // 일반 팀원만 흡수
                    handleMemberDeath(victim, victimTeam, killerTeam);
                }
                return;
            }
        }

        // 자살 또는 환경 데미지로 사망한 경우
        handleNaturalDeath(victim, victimTeam, isLeader);
    }

    /**
     * 리더가 죽었을 경우
     * @param victimTeam
     * @param killerTeam
     * @param leader
     */
    private void handleLeaderDeath(Team victimTeam, Team killerTeam, Player leader) {
        plugin.getLogger().info("Leader " + leader.getName() + " died. Merging team " +
                victimTeam.getName() + " into " + killerTeam.getName());

        // 흡수될 팀원들의 UUID 저장
        Set<UUID> absorbedMembers = new HashSet<>(victimTeam.getMembers());

        // 팀 전체를 킬러 팀으로 병합
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getTeamManager().mergeTeams(victimTeam, killerTeam);

            // 새 팀 리더 위치로 텔레포트 및 체력 설정
            UUID newLeaderId = killerTeam.getLeader();
            if (newLeaderId != null) {
                Player newLeader = Bukkit.getPlayer(newLeaderId);
                if (newLeader != null && newLeader.isOnline()) {
                    for (UUID memberId : killerTeam.getMembers()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null && member.isOnline()) {
                            member.teleport(newLeader.getLocation());

                            // 흡수된 팀원들은 체력 10칸으로 설정
                            if (absorbedMembers.contains(memberId)) {
                                setReducedHealth(member);
                            }
                        }
                    }
                }
            }
        }, 4L);
    }

    /**
     * 일반 멤버가 상대팀에게 죽었을 경우
     * @param victim
     * @param victimTeam
     * @param killerTeam
     */
    private void handleMemberDeath(Player victim, Team victimTeam, Team killerTeam) {
        plugin.getLogger().info("Member " + victim.getName() + " died. Moving from " +
                victimTeam.getName() + " to " + killerTeam.getName());

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // 기존 팀에서 제거하고 새 팀에 추가
            plugin.getTeamManager().removeMember(victimTeam, victim);
            plugin.getTeamManager().addMember(killerTeam, victim);

            // 체력 감소
            setReducedHealth(victim);

            // 새 팀 리더 위치로 텔레포트
            UUID newLeaderId = killerTeam.getLeader();
            if (newLeaderId != null) {
                Player newLeader = Bukkit.getPlayer(newLeaderId);
                if (newLeader != null && newLeader.isOnline()) {
                    victim.teleport(newLeader.getLocation());
                }
            }
        }, 4L);
    }

    /**
     * 자연사로 죽었을 경우 리더는 랜덤 위치, 일반 팀원은 리더의 위치로 텔레포트
     * @param victim
     * @param team
     * @param isLeader
     */
    private void handleNaturalDeath(Player victim, Team team, boolean isLeader) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (isLeader) {
                // 리더는 랜덤 위치로
                String worldName = "world";
                int range = plugin.getConfigFile().getInt("rtp.range");
                RTPManager.teleportPlayer(victim, worldName, range);
            } else {
                // 일반 팀원은 리더 위치로
                UUID leaderId = team.getLeader();
                if (leaderId != null) {
                    Player leader = Bukkit.getPlayer(leaderId);
                    if (leader != null && leader.isOnline()) {
                        victim.teleport(leader.getLocation());
                    }
                }
            }
        }, 4L);
    }

    private void setReducedHealth(Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(10.0);
            player.setHealth(10.0);
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

        List<String> messages = Lang.GAME_END_MESSAGE.getMessageList();
        List<String> formattedMessages = new ArrayList<>();
        for (String message : messages) {
            formattedMessages.add(message
                    .replace("{team_color}", teamColor)
                    .replace("{team_display_name}", teamDisplayName)
            );
        }

        Bukkit.getOnlinePlayers().forEach(player -> {
            formattedMessages.forEach(player::sendMessage);
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.1F, 1.0F);
        });
    }
}