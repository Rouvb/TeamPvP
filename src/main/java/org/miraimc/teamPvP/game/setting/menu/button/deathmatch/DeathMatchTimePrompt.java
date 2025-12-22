package org.miraimc.teamPvP.game.setting.menu.button.deathmatch;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.setting.menu.GameSettingsMenu;
import org.miraimc.teamPvP.util.TextUtil;
import org.miraimc.teamPvP.util.TimeFormatUtil;

public class DeathMatchTimePrompt extends StringPrompt {

    private final TeamPvP plugin;

    public DeathMatchTimePrompt(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getPromptText(@NotNull ConversationContext context) {
        return TextUtil.translate(
                TextUtil.LONG_LINE + "\n" +
                        TextUtil.translate("&b&l데스 매치 시작 시간 설정\n") +
                        TextUtil.translate("&f 게임 시작 후 데스 매치가 진행될 시간을 입력해주세요.\n") +
                        TextUtil.translate("&f 초 단위로 입력해야합니다.\n") +
                        TextUtil.translate("&c cancel&f(을)를 입력하면 설정 진행을 취소합니다.\n") +
                        TextUtil.LONG_LINE
        );
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String input) {
        Player player = (Player) context.getForWhom();

        try {
            int deathMatchTime = Integer.parseInt(input);
            int minDeathMatchTime = Math.max(deathMatchTime, 1);
            plugin.getGameSettings().setDeathMatchTime(minDeathMatchTime);

            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(TextUtil.translate("&a데스 매치 시작 시간이 &e" + TimeFormatUtil.formatSeconds(minDeathMatchTime) + " &a로 설정되었습니다."));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                GameSettingsMenu.open(player);
            });
        } catch (NumberFormatException e) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(TextUtil.translate("&c잘못된 값입니다. 숫자만 입력해주세요."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            });
            return this;
        }

        return END_OF_CONVERSATION;
    }
}
