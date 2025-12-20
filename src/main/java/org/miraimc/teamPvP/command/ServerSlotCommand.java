package org.miraimc.teamPvP.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.util.ServerSlotUtil;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("setslot|setserverslot|serverslot")
@CommandPermission("miraismp.admin")
public class ServerSlotCommand extends BaseCommand {

    @Default
    public void onSetSlot(Player sender, int amount) {
        Bukkit.setMaxPlayers(amount);
        ServerSlotUtil.setMaxPlayers(amount);
        sender.sendMessage(TextUtil.translate("&aSuccessfully set the server slot to &a" + amount + "&a."));
    }

}
