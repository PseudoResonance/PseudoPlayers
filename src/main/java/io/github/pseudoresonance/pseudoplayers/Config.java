package io.github.pseudoresonance.pseudoplayers;

import org.bukkit.configuration.file.FileConfiguration;

import io.github.pseudoresonance.pseudoapi.bukkit.PseudoPlugin;
import io.github.pseudoresonance.pseudoapi.bukkit.data.PluginConfig;

public class Config extends PluginConfig {

	public static int firstJoinTimeDifference = 7;
	public static int joinLeaveTimeDifference = 30;
	public static String teleportationFormat = "tp @p {x} {y} {z}";
	
	public static boolean aggressiveCommands = false;
	
	public void reloadConfig() {
		FileConfiguration fc = PseudoPlayers.plugin.getConfig();
		firstJoinTimeDifference = PluginConfig.getInt(fc, "FirstJoinTimeDifference", firstJoinTimeDifference);
		joinLeaveTimeDifference = PluginConfig.getInt(fc, "JoinLeaveTimeDifference", joinLeaveTimeDifference);
		teleportationFormat = PluginConfig.getString(fc, "TeleportationFormat", teleportationFormat);
		
		aggressiveCommands = PluginConfig.getBoolean(fc, "AggressiveCommands", aggressiveCommands);
		if (aggressiveCommands)
			PseudoPlayers.registerPCommand();
		else
			PseudoPlayers.unregisterPCommand();
	}
	
	public Config(PseudoPlugin plugin) {
		super(plugin);
	}

}