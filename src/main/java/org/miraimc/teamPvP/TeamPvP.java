package org.miraimc.teamPvP;

import co.aikar.commands.PaperCommandManager;
import fr.minuskube.inv.InventoryManager;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.miraimc.teamPvP.command.ServerSlotCommand;
import org.miraimc.teamPvP.command.TeamPvPCommand;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameHandler;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.game.command.GameCommand;
import org.miraimc.teamPvP.game.listener.GameCountdownListener;
import org.miraimc.teamPvP.game.listener.GameListener;
import org.miraimc.teamPvP.listener.ServerListener;
import org.miraimc.teamPvP.placeholder.TeamPvPExpansion;
import org.miraimc.teamPvP.profile.ProfileHandler;
import org.miraimc.teamPvP.profile.ProfileManager;
import org.miraimc.teamPvP.lobby.listener.LobbyListener;
import org.miraimc.teamPvP.profile.listener.ProfileListener;
import org.miraimc.teamPvP.profile.listener.ProfilePreventionListener;
import org.miraimc.teamPvP.profile.listener.WorldPreventionListener;
import org.miraimc.teamPvP.rtp.RTPListener;
import org.miraimc.teamPvP.scoreboard.ScoreboardManager;
import org.miraimc.teamPvP.spectator.SpectatorHandler;
import org.miraimc.teamPvP.spectator.listener.SpectatorListener;
import org.miraimc.teamPvP.team.TeamHandler;
import org.miraimc.teamPvP.team.TeamManager;
import org.miraimc.teamPvP.team.command.TeamAdminCommand;
import org.miraimc.teamPvP.team.command.TeamCommand;
import org.miraimc.teamPvP.team.listener.TeamChatListener;
import org.miraimc.teamPvP.team.listener.TeamListener;
import org.miraimc.teamPvP.util.BasicConfigFile;
import org.miraimc.teamPvP.world.WorldManager;
import org.popcraft.chunky.api.ChunkyAPI;

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
    private SpectatorHandler spectatorHandler;
    private ScoreboardManager scoreboardManager;
    private InventoryManager inventoryManager;
    private ChunkyAPI chunky;
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
        loadChunky();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TeamPvP...");
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
        spectatorHandler = new SpectatorHandler(this);
        scoreboardManager = new ScoreboardManager(this);
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

    private void registerListeners() {
        getLogger().info("Registering Listeners...");
        getServer().getPluginManager().registerEvents(new ProfileListener(this), this);
        getServer().getPluginManager().registerEvents(new ProfilePreventionListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldPreventionListener(this), this);
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new GameCountdownListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamListener(this), this);
        getServer().getPluginManager().registerEvents(new TeamChatListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerListener(this), this);
        getServer().getPluginManager().registerEvents(new RTPListener(this), this);
        getServer().getPluginManager().registerEvents(new SpectatorListener(this), this);
    }

    private void registerCommands() {
        getLogger().info("Registering Commands...");
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new GameCommand(this));
        commandManager.registerCommand(new TeamPvPCommand(this));
        commandManager.registerCommand(new TeamCommand(this));
        commandManager.registerCommand(new TeamAdminCommand(this));
        commandManager.registerCommand(new ServerSlotCommand());
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

    public void loadChunky() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Chunky")) return;
        chunky = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
    }
}
