package org.miraimc.teamPvP.config;

import org.miraimc.teamPvP.TeamPvP;

import java.util.List;

public enum Lang {

    // ===== Unit =====
    UNIT_HOUR("unit.hour"),
    UNIT_MINUTE("unit.minute"),
    UNIT_SECOND("unit.second"),

    // ===== General =====
    NONE("none"),

    // ===== Game Errors =====
    GAME_ERROR_START_CONDITION("game.error.start_condition"),

    // ===== Game Status =====
    GAME_STATUS_COUNTDOWN_MESSAGE("game.status.countdown.message"),
    GAME_STATUS_COUNTDOWN_TITLE("game.status.countdown.title"),
    GAME_STATUS_COUNTDOWN_SUBTITLE("game.status.countdown.subtitle"),

    GAME_STATUS_STARTED_MESSAGE("game.status.started.message"),
    GAME_STATUS_STARTED_TITLE("game.status.started.title"),
    GAME_STATUS_STARTED_SUBTITLE("game.status.started.subtitle"),

    GAME_STATUS_DEATHMATCH_ALERT("game.status.death_match.alert"),
    GAME_STATUS_DEATHMATCH_MESSAGE("game.status.death_match.message"),
    GAME_STATUS_DEATHMATCH_TITLE("game.status.death_match.title"),
    GAME_STATUS_DEATHMATCH_SUBTITLE("game.status.death_match.subtitle"),

    GAME_END_MESSAGE("game.end.message"),
    GAME_END_TITLE("game.end.title"),
    GAME_END_SUBTITLE("game.end.subtitle"),

    GAME_INVINCIBILITY("game.invincibility"),

    // ===== Game Death =====
    GAME_DEATH_LEADER_NATURAL_TITLE("game.death.leader_natural.title"),
    GAME_DEATH_LEADER_NATURAL_SUBTITLE("game.death.leader_natural.subtitle"),
    GAME_DEATH_MEMBER_NATURAL_TITLE("game.death.member_natural.title"),
    GAME_DEATH_MEMBER_NATURAL_SUBTITLE("game.death.member_natural.subtitle"),
    GAME_DEATH_LEADER_ABSORPTION_TITLE("game.death.leader_absorption.title"),
    GAME_DEATH_LEADER_ABSORPTION_SUBTITLE("game.death.leader_absorption.subtitle"),
    GAME_DEATH_MEMBER_ABSORPTION_TITLE("game.death.member_absorption.title"),
    GAME_DEATH_MEMBER_ABSORPTION_SUBTITLE("game.death.member_absorption.subtitle"),
    GAME_DEATH_SPECTATOR_TITLE("game.death.spectator_mode.title"),
    GAME_DEATH_SPECTATOR_SUBTITLE("game.death.spectator_mode.subtitle"),

    // ===== Team Command =====
    TEAM_COMMAND_HELP("team.command.help"),

    // ===== Team Errors =====
    TEAM_ERROR_NO_TEAM("team.error.no_team"),
    TEAM_ERROR_UNABLE_ACCESS("team.error.unable_access"),
    TEAM_ERROR_NOT_FOUND("team.error.not_found"),
    TEAM_ERROR_ONLY_LEADER("team.error.only_leader"),

    // ===== Team Status =====
    TEAM_STATUS_NEW_LEADER("team.status.new_leader"),
    TEAM_STATUS_LEAVE("team.status.leave"),
    TEAM_STATUS_MEMBER_JOINED("team.status.member_joined"),
    TEAM_STATUS_MEMBER_LEFT("team.status.member_left"),

    // ===== Team Chat =====
    TEAM_CHAT_CHANNEL_GLOBAL("team.chat.channel.global"),
    TEAM_CHAT_CHANNEL_TEAM("team.chat.channel.team"),
    TEAM_CHAT_TOGGLE_CHANNEL("team.chat.toggle_channel"),
    TEAM_CHAT_MESSAGE_FORMAT("team.chat.message_format");

    private final String path;

    Lang(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return TeamPvP.getInstance().getLangFile().getString(path);
    }

    public List<String> getMessageList() {
        return TeamPvP.getInstance().getLangFile().getStringList(path);
    }
}
