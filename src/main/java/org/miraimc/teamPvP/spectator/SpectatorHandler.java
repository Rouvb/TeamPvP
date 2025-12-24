package org.miraimc.teamPvP.spectator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileChatStatus;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.profile.event.ProfileStatusEvent;
import org.miraimc.teamPvP.spectator.item.SpectatorItem;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.VisibilityUtil;

import java.util.List;
import java.util.stream.Collectors;

public class SpectatorHandler {

    private final TeamPvP plugin;

    public SpectatorHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    private void checkEndGame() {
        Team winnerTeam = plugin.getTeamManager().getWinnerTeam();
        if (winnerTeam != null) {
            plugin.getGameManager().endGame(winnerTeam);
        }
    }

    public void handleSpectator(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            team.getAliveMembers().remove(player.getUniqueId());
        }

        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile != null) {
            profile.setStatus(ProfileStatus.SPECTATING);
            profile.setProfileChatStatus(ProfileChatStatus.SPECTATOR);
            Bukkit.getPluginManager().callEvent(new ProfileStatusEvent(profile));
        }

        Title title = Title.title(
                Component.text(Lang.GAME_DEATH_SPECTATOR_TITLE.getMessage()),
                Component.text(Lang.GAME_DEATH_SPECTATOR_SUBTITLE.getMessage())
        );
        player.showTitle(title);

        VisibilityUtil.updatePlayerVisibility(player, ProfileStatus.SPECTATING);
        plugin.getProfileHandler().resetInventory(player);
        player.getInventory().setItem(4, SpectatorItem.SPECTATOR_MENU);
        player.getInventory().setHeldItemSlot(4);
        checkEndGame();
    }

    public List<Player> getSpectators() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(p -> TeamPvP.getInstance().getProfileManager().getProfile(p).getStatus() == ProfileStatus.SPECTATING)
                .collect(Collectors.toList());
    }
}
