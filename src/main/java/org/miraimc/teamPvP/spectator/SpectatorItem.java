package org.miraimc.teamPvP.spectator;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.util.ItemBuilder;

public class SpectatorItem {

    public static final ItemStack SPECTATOR_MENU = createSpectatorMenuItem();

    private static ItemStack createSpectatorMenuItem() {
        return new ItemBuilder(Material.COMPASS)
                .displayName(Lang.SPECTATOR_ITEM_SPECTATOR_MENU.getMessage())
                .hideFlags()
                .build();
    }
}
