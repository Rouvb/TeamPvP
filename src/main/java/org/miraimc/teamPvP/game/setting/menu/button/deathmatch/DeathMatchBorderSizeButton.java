package org.miraimc.teamPvP.game.setting.menu.button.deathmatch;

import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.setting.menu.button.GameSettingsButton;
import org.miraimc.teamPvP.util.ItemBuilder;
import org.miraimc.teamPvP.util.TextUtil;

import java.text.NumberFormat;

public class DeathMatchBorderSizeButton extends GameSettingsButton {

    public DeathMatchBorderSizeButton(TeamPvP plugin) {
        super(buildItem(plugin), event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            ConversationFactory factory = new ConversationFactory(plugin)
                    .withFirstPrompt(new DeathMatchBorderSizePrompt(plugin))
                    .withLocalEcho(false)
                    .withEscapeSequence("cancel")
                    .withTimeout(30);

            Conversation conversation = factory.buildConversation(player);
            conversation.begin();
        });
    }

    private static ItemStack buildItem(TeamPvP plugin) {
        int size = plugin.getGameSettings().getDeathMatchBorderSize();
        int half = size / 2;

        return new ItemBuilder(Material.RED_GLAZED_TERRACOTTA)
                .displayName(TextUtil.translate("&b&l데스 매치 월드 보더 크기"))
                .appendLoreLine(TextUtil.translate("&7데스 매치까지 축소될 보더 크기를 설정합니다."))
                .appendLoreLine(TextUtil.translate("&7※ 지름 기준으로 적용됩니다"))
                .appendLoreLine("")
                .appendLoreLine(TextUtil.translate("&f현재 설정: &b" + NumberFormat.getInstance().format(size) + " &7(±" + half + ")"))
                .build();
    }
}
