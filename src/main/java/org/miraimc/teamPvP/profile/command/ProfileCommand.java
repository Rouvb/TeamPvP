package org.miraimc.teamPvP.profile.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("profile")
@CommandPermission("teampvp.admin")
public class ProfileCommand extends BaseCommand {

    private final TeamPvP plugin;

    public ProfileCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Subcommand("bypass|build|buildmode")
    public void onBypass(Player sender) {
        Profile profile = plugin.getProfileManager().getProfile(sender);
        if (profile == null) {
            sender.sendMessage(TextUtil.translate("&cYou do not have a profile!"));
            return;
        }

        profile.setBuildMode(!profile.isBuildMode());
        sender.sendMessage(TextUtil.translate("&aBuild mode set to &e" + profile.isBuildMode() + "&a."));
    }
}
