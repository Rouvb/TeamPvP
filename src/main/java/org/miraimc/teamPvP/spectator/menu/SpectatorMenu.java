package org.miraimc.teamPvP.spectator.menu;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.ItemBuilder;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SpectatorMenu implements InventoryProvider {

    private final List<Player> alivePlayers;

    public static void open(Player player, List<Player> alivePlayers) {
        SmartInventory.builder()
                .id("spectatorMenu")
                .provider(new SpectatorMenu(alivePlayers))
                .size(4, 9)
                .title(Lang.SPECTATOR_MENU_TITLE.getMessage())
                .manager(TeamPvP.getInstance().getInventoryManager())
                .build()
                .open(player);
    }

    public SpectatorMenu(List<Player> alivePlayers) {
        this.alivePlayers = alivePlayers;
    }

    @Override
    public void init(Player spectator, InventoryContents contents) {
        Pagination pagination = contents.pagination();
        pagination.setItemsPerPage(27);

        pagination.setItems(buildItems(spectator));
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        addPageButtons(spectator, contents, pagination);
    }


    private ClickableItem[] buildItems(Player spectator) {
        return alivePlayers.stream()
                .sorted(Comparator.comparing(
                        target -> {
                            Team team = TeamPvP.getInstance().getTeamManager().getTeam(target);
                            return team != null ? team.getWeight() : 0;
                        },
                        Comparator.reverseOrder()
                ))
                .map(target -> {
                    Team team = TeamPvP.getInstance().getTeamManager().getTeam(target);
                    String teamDisplayName = team != null ? team.getDisplayName() : "";
                    String teamColor = team != null ? team.getColor() : "";
                    String teamPrefix = team != null ? team.getPrefix() : "";

                    ItemStack head = new ItemBuilder(Material.PLAYER_HEAD)
                            .displayName(Lang.SPECTATOR_MENU_ITEM_TARGET_NAME.getMessage()
                                    .replace("{player}", target.getName())
                                    .replace("{team_display_name}", teamDisplayName)
                                    .replace("{team_color}", teamColor)
                                    .replace("{team_prefix}", teamPrefix)
                            )
                            .lore(Lang.SPECTATOR_MENU_ITEM_TARGET_LORE.getMessageList().stream()
                                    .map(s -> s
                                            .replace("{player}", target.getName())
                                            .replace("{team_display_name}", teamDisplayName)
                                            .replace("{team_color}", teamColor)
                                            .replace("{team_prefix}", teamPrefix)
                                    )
                                    .collect(Collectors.toList())
                            ).build();
                    SkullMeta meta = (SkullMeta) head.getItemMeta();
                    meta.setOwningPlayer(target);
                    head.setItemMeta(meta);

                    return ClickableItem.of(head, e -> {
                        spectator.closeInventory();
                        spectator.teleport(target.getLocation());
                        spectator.playSound(spectator.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1.0F, 1.0F);
                    });
                })
                .toArray(ClickableItem[]::new);
    }


    private void addPageButtons(Player player, InventoryContents contents, Pagination pagination) {
        for (int col = 0; col < 9; col++) {
            contents.set(0, col, ClickableItem.empty(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .displayName(TextUtil.translate("&f"))
                    .build()
            ));
        }

        contents.set(0, 0, ClickableItem.of(new ItemBuilder(Material.ARROW)
                        .displayName(Lang.SPECTATOR_MENU_ITEM_PREV_PAGE.getMessage())
                        .build(),
                e -> {
                    if (pagination.isFirst()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    } else {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                        contents.inventory().open(player, pagination.previous().getPage());
                    }
                }
        ));

        contents.set(0, 8, ClickableItem.of(new ItemBuilder(Material.ARROW)
                        .displayName(Lang.SPECTATOR_MENU_ITEM_NEXT_PAGE.getMessage())
                        .build(),
                e -> {
                    if (pagination.isLast()) {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                    } else {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
                        contents.inventory().open(player, pagination.next().getPage());
                    }
                }
        ));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        List<Player> currentAlive = Bukkit.getOnlinePlayers().stream()
                .filter(p -> TeamPvP.getInstance().getProfileManager().getProfile(p).getStatus() == ProfileStatus.PLAYING)
                .collect(Collectors.toList());

        if (!currentAlive.containsAll(alivePlayers) || !alivePlayers.containsAll(currentAlive)) {
            alivePlayers.clear();
            alivePlayers.addAll(currentAlive);
            contents.pagination().setItems(buildItems(player));
        }
    }
}
