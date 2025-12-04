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
            case "lobby", "game" -> plugin.getConfigFile().set(path, LocationUtil.serializeLocation(location));
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
}
