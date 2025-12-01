package org.miraimc.tailChasing.game.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.util.TextUtil;

@CommandAlias("tailchasing|꼬리잡기")
public class TailChasingCommand extends BaseCommand {

    private final TailChasing plugin;

    public TailChasingCommand(TailChasing plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start|시작")
    @CommandPermission("tailchasing.start")
    public void onStart(Player sender) {
        sender.sendMessage(TextUtil.translate("&a게임이 곧 시작됩니다."));
        plugin.getGameManager().startCountdown();
    }

    @Subcommand("reload")
    @CommandPermission("tailchasing.admin")
    public void onReload(Player sender) {
        plugin.getConfigFile().reload();
        plugin.getScoreboardFile().reload();
        plugin.getTeamsFile().reload();

        sender.sendMessage(TextUtil.translate("&aPlugin has been reloaded."));
    }
}
