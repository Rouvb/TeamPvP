package org.miraimc.teamPvP;

import co.aikar.commands.PaperCommandManager;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.miraimc.teamPvP.command.TeamPvPCommand;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameHandler;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.game.command.GameCommand;
import org.miraimc.teamPvP.game.listener.GameListener;
import org.miraimc.teamPvP.game.listener.GameStatusListener;
import org.miraimc.teamPvP.listener.ServerListener;
import org.miraimc.teamPvP.placeholder.TeamPvPExpansion;
import org.miraimc.teamPvP.profile.ProfileHandler;
import org.miraimc.teamPvP.profile.ProfileManager;
import org.miraimc.teamPvP.profile.listener.LobbyListener;
import org.miraimc.teamPvP.profile.listener.ProfileListener;
import org.miraimc.teamPvP.profile.listener.ProfilePreventionListener;
import org.miraimc.teamPvP.profile.listener.WorldPreventionListener;
import org.miraimc.teamPvP.rtp.RTPManager;
import org.miraimc.teamPvP.scoreboard.ScoreboardManager;
import org.miraimc.teamPvP.team.TeamHandler;
import org.miraimc.teamPvP.team.TeamManager;
import org.miraimc.teamPvP.team.command.TeamAdminCommand;
import org.miraimc.teamPvP.team.command.TeamCommand;
import org.miraimc.teamPvP.team.listener.TeamChatListener;
import org.miraimc.teamPvP.team.listener.TeamListener;
import org.miraimc.teamPvP.util.BasicConfigFile;
import org.miraimc.teamPvP.world.WorldManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class TeamPvP extends JavaPlugin {

    @Getter private static TeamPvP instance;
    private BasicConfigFile configFile, teamsFile, scoreboardFile, langFile;
    private WorldManager worldManager;
    private Game game;
    private ProfileManager profileManager;
    private ProfileHandler profileHandler;
    private TeamManager teamManager;
    private TeamHandler teamHandler;
    private GameManager gameManager;
    private GameHandler gameHandler;
    private RTPManager rtpManager;
    private ScoreboardManager scoreboardManager;
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling TeamPvP...");
        loadFiles();
        importWorlds();
        loadManagers();
        setupWorlds();
        registerListeners();
        registerCommands();
        registerPlaceholder();
        loadScoreboardUpdater();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TeamPvP...");
        rtpManager.getRtpTask().cancel();
    }

    private void loadFiles() {
        getLogger().info("Loading Files...");
        configFile = new BasicConfigFile(this, "config.yml", true);
        teamsFile = new BasicConfigFile(this, "teams.yml", false);
        scoreboardFile = new BasicConfigFile(this, "scoreboard.yml", true);
        langFile = new BasicConfigFile(this, "lang.yml", true);
    }

    private void importWorlds() {
        worldManager = new WorldManager(this);
        worldManager.loadWorlds();
    }

    private void setupWorlds() {
        worldManager.setGameRules();
        worldManager.setBorder();
    }

    private void loadManagers() {
        getLogger().info("Loading Managers...");
        game = new Game(this, GameStatus.LOBBY);
        profileManager = new ProfileManager();
        profileHandler = new ProfileHandler(this);
        teamManager = new TeamManager(this);
        teamHandler = new TeamHandler(this);
        gameManager = new GameManager(this);
        gameHandler = new GameHandler(this);
        rtpManager = new RTPManager(this);
        scoreboardManager = new ScoreboardManager(this);
    }

    private void registerListeners() {
        getLogger().info("Registering Listeners...");
        getServer().getPluginManager().registerEvents(new ProfileListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfilePreventionListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldPreventionListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new GameStatusListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
    }

    private void registerCommands() {
        getLogger().info("Registering Commands...");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new GameCommand(this));
        commandManager.registerCommand(new TeamPvPCommand(this));
        commandManager.registerCommand(new TeamCommand(this));
        commandManager.registerCommand(new TeamAdminCommand(this));
    }

    private void registerPlaceholder() {
        getLogger().info("Registering Placeholder...");
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new TeamPvPExpansion(this).register();
        }
    }

    private void loadScoreboardUpdater() {
        getLogger().info("Loading Scoreboard Updater...");
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (FastBoard board : boards.values()) {
                scoreboardManager.updateScoreboard(board);
            }
        }, 0, 20L);
    }
}
