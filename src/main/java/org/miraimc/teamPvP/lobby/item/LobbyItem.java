package org.miraimc.teamPvP.lobby.item;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.miraimc.teamPvP.util.ItemBuilder;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.ArrayList;
import java.util.List;

public class LobbyItem {

    public static final ItemStack TEAM_SELECTOR = new ItemBuilder(Material.COMPASS)
            .displayName(TextUtil.translate("&b&lJoin Team")).build();

    public static final ItemStack GAME_INFO_BOOK = new ItemBuilder(getGameInfoBook())
            .clearLore().build();

    public static final ItemStack GAME_SETTINGS = new ItemBuilder(Material.REPEATER)
            .displayName(TextUtil.translate("&b&lGame Settings")).build();

    public static final ItemStack GAME_START = new ItemBuilder(Material.CLOCK)
            .displayName(TextUtil.translate("&b&lStart Game")).build();

    private static ItemStack getGameInfoBook() {
        ItemStack book = new ItemBuilder(Material.WRITTEN_BOOK)
                .displayName(TextUtil.translate("&b&l게임 설명")).build();

        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("게임 설명");
        meta.setAuthor("Rouvb");
        meta.pages(getPages());
        book.setItemMeta(meta);

        return book;
    }

    private static List<Component> getPages() {
        List<Component> pages = new ArrayList<>();
        pages.add(ColorParser.of(
                "&0&l게임 구성&r\n\n"
                + "● 게임은 &cRed&r, &9Blue&r, &eYellow, &aGreen&r 총 네 개의 팀으로 진행됩니다.\n\n"
                + "● 각 팀에는 반드시 한 명의 팀장이 존재합니다."
                )
                .legacy().build());
        pages.add(ColorParser.of(
                "&0&l진행 방식&r\n\n"
                + "● 게임 시작 후 5분 동안은 무적입니다.\n\n"
                + "● 게임이 진행되는 동안 월드보더가 줄어듭니다.\n\n"
                + "● 1시간이 지나면 데스매치가 시작됩니다.\n\n"
                + "● 최종 생존 팀이 우승합니다."
                )
                .legacy().build());
        pages.add(ColorParser.of(
                "&0&l팀 설명&r\n\n"
                + "● 같은 팀원끼리는 공격할 수 없습니다.\n\n"
                + "● 팀원이 처치당하면 상대 팀으로 흡수됩니다.\n\n"
                + "● 팀장이 처치당하면 팀 전체가 흡수됩니다.\n\n"
                + "● 팀장 체력: &c20❤&r\n"
                + "● 팀원 체력: &c10❤&r\n"
                + "● 흡수된 팀원 체력: &c5❤&r"
                )
                .legacy().build());
        pages.add(ColorParser.of(
            "&0&l명령어 안내&r\n\n"
                    + "● /team info - 현재 팀 확인하기\n"
                    + "● /team join - 랜덤 팀 배정받기\n"
                    + "● /team leave - 팀에서 떠나기\n"
                    + "● /team leader - 팀장 위임하기\n"
                    + "● /team chat - 팀 채팅 채널 사용하기"
                    )
                    .legacy().build());
        pages.add(ColorParser.of(
            "&0&l추가 안내&r\n\n"
                    + "게임 설정에 따라 무적 시간, 게임 시간 등은 변경될 수 있습니다."
                    )
                    .legacy().build());

        return pages;
    }
}
