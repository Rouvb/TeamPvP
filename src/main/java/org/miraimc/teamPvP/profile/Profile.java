package org.miraimc.teamPvP.profile;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Profile {

    private UUID uuid;
    private ProfileStatus status;
    private boolean buildMode;

    public Profile(UUID uuid, ProfileStatus status) {
        this.uuid = uuid;
        this.status = status;
        this.buildMode = false;
    }

}
