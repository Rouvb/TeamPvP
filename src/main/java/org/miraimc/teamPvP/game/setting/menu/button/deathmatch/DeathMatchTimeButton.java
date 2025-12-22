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
import org.miraimc.teamPvP.util.TimeFormatUtil;

public class DeathMatchTimeButton extends GameSettingsButton {
    public DeathMatchTimeButton(TeamPvP plugin) {
        super(buildItem(plugin), event -> {
            Player player = (Player) event.getWhoClicked();
            player.closeInventory();

            ConversationFactory factory = new ConversationFactory(plugin)
                    .withFirstPrompt(new DeathMatchTimePrompt(plugin))
                    .withLocalEcho(false)
                    .withEscapeSequence("cancel")
                    .withTimeout(30);

            Conversation conversation = factory.buildConversation(player);
            conversation.begin();
        });
    }

    private static ItemStack buildItem(TeamPvP plugin) {
        int deathMatchTime = plugin.getGameSettings().getDeathMatchTime();

        return new ItemBuilder(Material.DIAMOND_SWORD)
                .displayName(TextUtil.translate("&b&l데스 매치 시작 시간"))
                .appendLoreLine(TextUtil.translate("&7게임 시작 후 데스 매치가 진행될 시간을 설정합니다."))
                .appendLoreLine(TextUtil.translate("&7※ 초 단위로 입력해주세요"))
                .appendLoreLine("")
                .appendLoreLine(TextUtil.translate("&f현재 설정: &b" + TimeFormatUtil.formatSeconds(deathMatchTime) + " &7(" + deathMatchTime + ")"))
                .hideFlags()
                .build();
    }
}
