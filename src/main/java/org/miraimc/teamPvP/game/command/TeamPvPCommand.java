package org.miraimc.teamPvP.game.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("teampvp")
public class TeamPvPCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamPvPCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start|시작")
    @CommandPermission("teampvp.start")
    public void onStart(Player sender) {
        sender.sendMessage(TextUtil.translate("&a게임이 곧 시작됩니다."));
        plugin.getGameManager().startCountdown();
    }

    @Subcommand("reload")
    @CommandPermission("teampvp.admin")
    public void onReload(Player sender) {
        plugin.getConfigFile().reload();
        plugin.getScoreboardFile().reload();
        plugin.getTeamsFile().reload();

        sender.sendMessage(TextUtil.translate("&aPlugin has been reloaded."));
    }
}
