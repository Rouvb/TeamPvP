package org.miraimc.teamPvP.game.setting.menu.button;

import fr.minuskube.inv.ClickableItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GameSettingsButton {

    private final ItemStack item;
    private final Consumer<InventoryClickEvent> onClick;

    public GameSettingsButton(ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.item = item;
        this.onClick = onClick;
    }

    public ClickableItem toClickableItem() {
        return ClickableItem.of(item, onClick::accept);
    }
}
