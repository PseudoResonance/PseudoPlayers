package io.github.pseudoresonance.pseudoplayers;

import org.bukkit.plugin.RegisteredServiceProvider;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.CommandDescription;
import io.github.pseudoresonance.pseudoapi.bukkit.HelpSC;
import io.github.pseudoresonance.pseudoapi.bukkit.MainCommand;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoAPI;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoPlugin;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoUpdater;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.Column;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.ServerPlayerDataController;
import io.github.pseudoresonance.pseudoplayers.commands.ReloadSC;
import io.github.pseudoresonance.pseudoplayers.commands.ResetLocalizationSC;
import io.github.pseudoresonance.pseudoplayers.commands.ResetSC;
import io.github.pseudoresonance.pseudoplayers.commands.NicknameSC;
import io.github.pseudoresonance.pseudoplayers.commands.PingSC;
import io.github.pseudoresonance.pseudoplayers.commands.PlayerSC;
import io.github.pseudoresonance.pseudoplayers.commands.ReloadLocalizationSC;
import io.github.pseudoresonance.pseudoplayers.completers.NicknameTC;
import io.github.pseudoresonance.pseudoplayers.completers.PlayerTC;
import io.github.pseudoresonance.pseudoplayers.completers.PseudoPlayersTC;
import io.github.pseudoresonance.pseudoplayers.listeners.PlayerJoinLeaveL;

public class PseudoPlayers extends PseudoPlugin {

	public static PseudoPlugin plugin;

	private static MainCommand mainCommand;
	private static HelpSC helpSubCommand;
	private static PingSC pingSubCommand;

	private static PlayerSC playerSubCommand;
	private static PlayerTC playerTabCompleter;

	private static Config config;

	public static Object economy = null;
	public static Object chat = null;
	
	public void onLoad() {
		PseudoUpdater.registerPlugin(this);
	}

	public void onEnable() {
		super.onEnable();
		this.saveDefaultConfig();
		plugin = this;
		ServerPlayerDataController.addColumn(new Column("logoutLocation", "VARCHAR(225)", "NULL"));
		PlayerDataController.addColumn(new Column("nickname", "VARCHAR(100)", "NULL"));
		config = new Config(this);
		config.updateConfig();
		playerSubCommand = new PlayerSC();
		playerTabCompleter = new PlayerTC();
		config.reloadConfig();
		initVault();
		mainCommand = new MainCommand(plugin);
		helpSubCommand = new HelpSC(plugin);
		pingSubCommand = new PingSC();
		initializeCommands();
		initializeTabcompleters();
		initializeSubCommands();
		initializeListeners();
		setCommandDescriptions();
		PseudoAPI.registerConfig(config);
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	public static Config getConfigOptions() {
		return PseudoPlayers.config;
	}

	private void initVault() {
		if (getServer().getPluginManager().getPlugin("Vault") != null) {
			try {
				RegisteredServiceProvider<?> economyProvider = getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.economy.Economy"));
				if (economyProvider != null)
					economy = economyProvider.getProvider();
				else
					getChat().sendConsolePluginError(Chat.Errors.CUSTOM, LanguageManager.getLanguage().getMessage("pseudoplayers.error_no_vault_economy_loaded"));
			} catch (ClassNotFoundException e) {
				getChat().sendConsolePluginError(Chat.Errors.CUSTOM, LanguageManager.getLanguage().getMessage("pseudoplayers.error_no_vault_loaded"));
			}
			try {
				RegisteredServiceProvider<?> chatProvider = getServer().getServicesManager().getRegistration(Class.forName("net.milkbowl.vault.chat.Chat"));
				if (chatProvider != null)
					chat = chatProvider.getProvider();
				else
					getChat().sendConsolePluginError(Chat.Errors.CUSTOM, LanguageManager.getLanguage().getMessage("pseudoplayers.error_no_vault_chat_loaded"));
			} catch (ClassNotFoundException e) {
				getChat().sendConsolePluginError(Chat.Errors.CUSTOM, LanguageManager.getLanguage().getMessage("pseudoplayers.error_no_vault_loaded"));
			}
		} else
			getChat().sendConsolePluginError(Chat.Errors.CUSTOM, LanguageManager.getLanguage().getMessage("pseudoplayers.error_no_vault_loaded"));
	}

	private void initializeCommands() {
		this.getCommand("pseudoplayers").setExecutor(mainCommand);
		this.getCommand("player").setExecutor(playerSubCommand);
		this.getCommand("ping").setExecutor(pingSubCommand);
		this.getCommand("nickname").setExecutor(new NicknameSC());
	}
	
	public static void registerPCommand() {
		plugin.registerDynamicCommand("p", playerSubCommand, playerTabCompleter);
	}
	
	public static void unregisterPCommand() {
		plugin.unregisterDynamicCommand("p");
	}

	private void initializeSubCommands() {
		subCommands.put("help", helpSubCommand);
		subCommands.put("reload", new ReloadSC());
		subCommands.put("reloadlocalization", new ReloadLocalizationSC());
		subCommands.put("reset", new ResetSC());
		subCommands.put("resetlocalization", new ResetLocalizationSC());
		subCommands.put("player", playerSubCommand);
		subCommands.put("ping", pingSubCommand);
	}

	private void initializeTabcompleters() {
		this.getCommand("pseudoplayers").setTabCompleter(new PseudoPlayersTC());
		this.getCommand("player").setTabCompleter(playerTabCompleter);
		this.getCommand("nickname").setTabCompleter(new NicknameTC());
	}

	private void initializeListeners() {
		getServer().getPluginManager().registerEvents(new PlayerJoinLeaveL(), this);
	}

	private void setCommandDescriptions() {
		commandDescriptions.add(new CommandDescription("pseudoplayers", "pseudoplayers.pseudoplayers_help", ""));
		commandDescriptions.add(new CommandDescription("pseudoplayers help", "pseudoplayers.pseudoplayers_help_help", ""));
		commandDescriptions.add(new CommandDescription("pseudoplayers reload", "pseudoplayers.pseudoplayers_reload_help", "pseudoplayers.reload"));
		commandDescriptions.add(new CommandDescription("pseudoplayers reloadlocalization", "pseudoplayers.pseudoplayers_reloadlocalization_help", "pseudoplayers.reloadlocalization"));
		commandDescriptions.add(new CommandDescription("pseudoplayers reset", "pseudoplayers.pseudoplayers_reset_help", "pseudoplayers.reset"));
		commandDescriptions.add(new CommandDescription("pseudoplayers resetlocalization", "pseudoplayers.pseudoplayers_resetlocalization_help", "pseudoplayers.resetlocalization"));
		commandDescriptions.add(new CommandDescription("nickname", "pseudoplayers.pseudoplayers_nickname_help", "pseudoplayers.nickname", false));
		commandDescriptions.add(new CommandDescription("ping", "pseudoplayers.pseudoplayers_ping_help", "pseudoplayers.ping"));
		commandDescriptions.add(new CommandDescription("player", "pseudoplayers.pseudoplayers_player_help", "pseudoplayers.view"));
	}

}