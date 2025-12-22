package org.miraimc.teamPvP.lobby.menu;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.lobby.menu.button.border.DefaultBorderSizeButton;

public class GameSettingsMenu implements InventoryProvider {

    private final TeamPvP plugin;

    public GameSettingsMenu(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public static void open(Player player) {
        SmartInventory.builder()
                .id("gameSettingsMenu")
                .provider(new GameSettingsMenu(TeamPvP.getInstance()))
                .size(3, 9)
                .title("게임 설정")
                .manager(TeamPvP.getInstance().getInventoryManager())
                .build()
                .open(player);
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(1, 3, new DefaultBorderSizeButton(plugin).toClickableItem());
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }
}
