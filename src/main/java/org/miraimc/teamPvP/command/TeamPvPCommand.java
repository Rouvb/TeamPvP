package org.miraimc.teamPvP.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameSettings;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;
import org.popcraft.chunky.api.ChunkyAPI;

@CommandAlias("teampvp")
public class TeamPvPCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamPvPCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start|시작")
    @CommandPermission("teampvp.host")
    public void onStart(Player sender) {
        Team team = plugin.getTeamManager().getWinnerTeam();
        if (team != null) {
            sender.sendMessage(Lang.GAME_ERROR_START_CONDITION.getMessage());
            return;
        }

        ChunkyAPI chunky = plugin.getChunky();
        if (chunky != null && chunky.isRunning("world")) {
            chunky.onGenerationProgress(event -> {
                if (event.world().equalsIgnoreCase("world")) {
                    float progress = event.progress();
                    sender.sendActionBar(Lang.GAME_ERROR_WORLD_GENERATING_ACTION_BAR.getMessage()
                                    .replace("{progress}", String.format("%.2f", progress))
                    );
                }
            });

            chunky.onGenerationComplete(event -> {
                if (event.world().equalsIgnoreCase("world")) {
                    sender.sendMessage(Lang.GAME_ERROR_WORLD_GENERATING_DONE.getMessage());
                    sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    Bukkit.getScheduler().runTask(plugin, () -> plugin.getGameManager().startCountdown());
                }
            });

            sender.sendMessage(Lang.GAME_ERROR_WORLD_GENERATING_GENERATING.getMessage());
            return;
        }

        plugin.getGameManager().startCountdown();
    }

    @Subcommand("end|stop|종료|중지")
    @CommandPermission("teampvp.host")
    public void onEnd(Player sender, @Optional String team) {
        if (team == null) {
            plugin.getGameManager().endGame(null);
        }

        plugin.getGameManager().endGame(plugin.getTeamManager().getTeam(team));
    }

    @Subcommand("reload")
    @CommandPermission("teampvp.admin")
    public void onReload(Player sender) {
        plugin.getConfigFile().reload();
        plugin.getScoreboardFile().reload();
        plugin.getTeamsFile().reload();
        plugin.getLangFile().reload();

        sender.sendMessage(TextUtil.translate("&aPlugin has been reloaded."));
    }

    @Subcommand("build")
    @CommandPermission("teampvp.admin")
    public void onBuild(Player sender) {
        Profile profile = plugin.getProfileManager().getProfile(sender);
        if (profile == null) {
            sender.sendMessage(TextUtil.translate("&cYou do not have a profile!"));
            return;
        }

        profile.setBuildMode(!profile.isBuildMode());
        sender.sendMessage(TextUtil.translate("&aBuild mode set to &e" + profile.isBuildMode() + "&a."));
    }

    @Subcommand("debug")
    @CommandPermission("teampvp.admin")
    public void onDebug(Player sender, @Optional OnlinePlayer target) {
        Player targetPlayer;
        if (target == null) {
            targetPlayer = sender;
        } else {
            targetPlayer = target.getPlayer();
        }

        Profile profile = plugin.getProfileManager().getProfile(targetPlayer);
        Game game = plugin.getGame();
        GameSettings gameSettings = plugin.getGameSettings();
        Team team = plugin.getTeamManager().getTeam(targetPlayer);

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        sender.sendMessage(TextUtil.translate("&b&lLocations:"));
        sender.sendMessage(TextUtil.translate("&f Lobby: &b" + plugin.getConfigFile().getString("game.locations.lobby")));
        sender.sendMessage(TextUtil.translate("&f Game: &b" + plugin.getConfigFile().getString("game.locations.game")));
        sender.sendMessage(TextUtil.translate("&f Death Match: &b" + plugin.getConfigFile().getString("game.locations.death_match")));

        if (profile != null) {
            sender.sendMessage(" ");
            sender.sendMessage(TextUtil.translate("&b&lProfile Info:"));
            sender.sendMessage(TextUtil.translate("&f Status: &b" + profile.getStatus().name()));
            sender.sendMessage(TextUtil.translate("&f Build Mode: &b" + profile.isBuildMode()));
        }
        
        if (game != null) {
            sender.sendMessage(" ");
            sender.sendMessage(TextUtil.translate("&b&lGame Info:"));
            sender.sendMessage(TextUtil.translate("&f Status: &b" + game.getGameStatus().name()));
            sender.sendMessage(TextUtil.translate("&f Duration: &b" + game.getGameTime()));
            sender.sendMessage(TextUtil.translate("&f Invincibility Time: &b" + gameSettings.getInvincibilityTime()));
            sender.sendMessage(TextUtil.translate("&f Countdown Time: &b" + gameSettings.getCountdownTime()));
            sender.sendMessage(TextUtil.translate("&f Death Match Time: &b" + gameSettings.getDeathMatchTime()));
        }

        if (team != null) {
            sender.sendMessage(" ");
            sender.sendMessage(TextUtil.translate("&b&lTeam Info:"));
            sender.sendMessage(TextUtil.translate("&f Team: &b" + team.getName()));
            sender.sendMessage(TextUtil.translate("&f Team Display Name: &b" + team.getColor() + team.getDisplayName()));
            sender.sendMessage(TextUtil.translate("&f Team Prefix: &b" + team.getPrefix()));
            sender.sendMessage(TextUtil.translate("&f Team Leader: &b" + team.getLeader()));
            sender.sendMessage(TextUtil.translate("&f Team Weight: &b" + team.getWeight()));
            sender.sendMessage(TextUtil.translate("&f Team Members: &b" + plugin.getTeamHandler().parseTeamMembers(team)));
        }

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }
}
