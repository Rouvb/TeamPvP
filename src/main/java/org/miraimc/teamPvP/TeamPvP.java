package org.miraimc.teamPvP;

import co.aikar.commands.PaperCommandManager;
import fr.mrmicky.fastboard.FastBoard;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameHandler;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.game.command.GameCommand;
import org.miraimc.teamPvP.command.TeamPvPCommand;
import org.miraimc.teamPvP.game.listener.GameListener;
import org.miraimc.teamPvP.listener.ServerListener;
import org.miraimc.teamPvP.profile.ProfileManager;
import org.miraimc.teamPvP.profile.command.ProfileCommand;
import org.miraimc.teamPvP.profile.listener.LobbyListener;
import org.miraimc.teamPvP.profile.listener.LobbyPreventionListener;
import org.miraimc.teamPvP.profile.listener.ProfileListener;
import org.miraimc.teamPvP.rtp.RTPManager;
import org.miraimc.teamPvP.scoreboard.ScoreboardManager;
import org.miraimc.teamPvP.team.TeamHandler;
import org.miraimc.teamPvP.team.TeamManager;
import org.miraimc.teamPvP.team.command.TeamCommand;
import org.miraimc.teamPvP.team.listener.TeamListener;
import org.miraimc.teamPvP.util.BasicConfigFile;
import org.miraimc.teamPvP.world.WorldManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public final class TeamPvP extends JavaPlugin {

    @Getter private static TeamPvP instance;
    private BasicConfigFile configFile, teamsFile, scoreboardFile;
    private WorldManager worldManager;
    private Game game;
    private ProfileManager profileManager;
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
        loadScoreboardUpdater();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TeamPvP...");
        worldManager.resetSurvivalWorlds();
    }

    private void loadFiles() {
        getLogger().info("Loading Files...");
        configFile = new BasicConfigFile(this, "config.yml", true);
        teamsFile = new BasicConfigFile(this, "teams.yml", true);
        scoreboardFile = new BasicConfigFile(this, "scoreboard.yml", true);
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
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyPreventionListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
    }

    private void registerCommands() {
        getLogger().info("Registering Commands...");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new ProfileCommand(this));
        commandManager.registerCommand(new GameCommand(this));
        commandManager.registerCommand(new TeamPvPCommand(this));
        commandManager.registerCommand(new TeamCommand(this));
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
