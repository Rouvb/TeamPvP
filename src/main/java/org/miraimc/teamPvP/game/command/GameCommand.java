package org.miraimc.teamPvP.game.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("game")
@CommandPermission("teampvp.admin")
public class GameCommand extends BaseCommand {

    private final TeamPvP plugin;

    public GameCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onDefault(CommandSender sender) {
        String[] helpLines = {
                TextUtil.LONG_LINE,
                "&b/game world <world>",
                "&b/game setlocation lobby",
                "&b/game setlocation world",
                "&b/game deathmatch add",
                "&b/game deathmatch remove <index>",
                "&b/game setbordersize <int>",
                "&b/game setcountdown <int>",
                "&b/game setdeathmatchtime <int>",
                TextUtil.LONG_LINE
        };
        for (String line : helpLines) {
            sender.sendMessage(TextUtil.translate(line));
        }
    }

    @Subcommand("setlocation")
    public void onSetLocation(Player sender, String locationType) {
        Location location = sender.getLocation();
        String path = "game.locations." + locationType;

        switch (locationType.toLowerCase()) {
            case "lobby" -> plugin.getConfigFile().set(path, LocationUtil.serializeLocation(location));
            case "world" -> plugin.getConfigFile().set(path, LocationUtil.serializeLocation(location));
            default -> {
                sender.sendMessage(TextUtil.translate("&cInvalid location type!"));
                return;
            }
        }
        sender.sendMessage(TextUtil.translate("&aSuccessfully set location &e" + locationType + "&a!"));
    }

    @Subcommand("setbodersize")
    public void onSetBorder(Player sender, int size) {
        plugin.getGame().setBorderSize(size);
        plugin.getConfigFile().set("game.border_size", size);
        sender.sendMessage(TextUtil.translate("&aSuccessfully set border &e" + size + "&a!"));
    }

    @Subcommand("setcountdown")
    public void onSetCountdown(Player sender, int countdown) {
        plugin.getGame().setCountdownTime(countdown);
        plugin.getConfigFile().set("game.countdown_time", countdown);
        sender.sendMessage(TextUtil.translate("&aSuccessfully set countdown &e" + countdown + "&a!"));
    }

    @Subcommand("setdeathmatchtime")
    public void onSetDeathMatchTime(Player sender, int deathmatchTime) {
        plugin.getGame().setDeathMatchTime(deathmatchTime);
        plugin.getConfigFile().set("game.death_match_time", deathmatchTime);
        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }

    @Subcommand("world")
    public void onWorld(Player sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(TextUtil.translate(TextUtil.translate("&cWorld not found!")));
            return;
        }

        sender.teleport(world.getSpawnLocation());
        sender.sendMessage(TextUtil.translate("&aSuccessfully set world &e" + worldName + "&a!"));
    }

    @Subcommand("debug")
    public void onDebug(Player sender, @Optional OnlinePlayer target) {
        Player targetPlayer;
        if (target == null) {
            targetPlayer = sender;
        } else {
            targetPlayer = target.getPlayer();
        }

        Team team = plugin.getTeamManager().getTeam(targetPlayer);
        Profile profile = plugin.getProfileManager().getProfile(targetPlayer);

        targetPlayer.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        targetPlayer.sendMessage(TextUtil.translate("&b&lLocations:"));
        targetPlayer.sendMessage(TextUtil.translate("&fLobby: &b" + plugin.getConfigFile().getString("game.locations.lobby")));
        targetPlayer.sendMessage(TextUtil.translate("&fGame: &b" + plugin.getConfigFile().getString("game.locations.game")));

        if (profile != null) {
            targetPlayer.sendMessage(TextUtil.translate("&b&lProfile Info:"));
            targetPlayer.sendMessage(TextUtil.translate("&fStatus: &b" + profile.getStatus().name()));
            targetPlayer.sendMessage(TextUtil.translate("&fBuild Mode: &b" + profile.isBuildMode()));
        }

        if (team != null) {
            targetPlayer.sendMessage(TextUtil.translate("&b&lTeam Info:"));
            targetPlayer.sendMessage(TextUtil.translate(" &fTeam: &b" + team.getName()));
            targetPlayer.sendMessage(TextUtil.translate(" &fTeam Display Name: &b" + team.getColor() + team.getDisplayName()));
            targetPlayer.sendMessage(TextUtil.translate(" &fTeam Prefix: &b" + team.getPrefix()));
            targetPlayer.sendMessage(TextUtil.translate(" &fTeam Leader: &b" + team.getLeader()));
            targetPlayer.sendMessage(TextUtil.translate(" &fTeam Members: &b" + plugin.getTeamHandler().parseTeamMembers(team)));
        }

        targetPlayer.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }
}
