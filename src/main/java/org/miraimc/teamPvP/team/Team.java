package org.miraimc.teamPvP.team;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Team {

    private final TeamPvP plugin;

    private String name;
    private String displayName;
    private String prefix;
    private String color;
    private UUID leader;
    private Location deathMatchLocation;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> aliveMembers = new HashSet<>();

    public Team(TeamPvP plugin, String name, String displayName, String prefix, String color) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = displayName;
        this.prefix = TextUtil.translate(prefix);
        this.color = TextUtil.translate(color);
        this.leader = null;
        this.deathMatchLocation = LocationUtil.deserializeLocation(plugin.getTeamsFile().getString("teams." + name + ".death_match_location"));
    }

    public void ensureLeader(UUID candidate) {
        if (leader == null) {
            leader = candidate;
        }
    }
}
