package org.miraimc.tailChasing.team;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.util.LocationUtil;
import org.miraimc.tailChasing.util.TextUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Team {

    private final TailChasing plugin;

    private String name;
    private String displayName;
    private String prefix;
    private String color;
    private UUID leader;
    private Location deathMatchLocation;
    private Set<UUID> members;

    public Team(TailChasing plugin, String name, String displayName, String prefix, String color) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = displayName;
        this.prefix = TextUtil.translate(prefix);
        this.color = TextUtil.translate(color);
        this.leader = null;
        this.deathMatchLocation = LocationUtil.deserializeLocation(plugin.getTeamsFile().getString("teams." + name + ".death_match_locations"));
        this.members = new HashSet<>();
    }

    public void ensureLeader(UUID candidate) {
        if (leader == null) {
            leader = candidate;
        }
    }
}
