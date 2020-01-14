package io.github.pseudoresonance.pseudoplayers.commands;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;

public class ResetSC implements SubCommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.reset")) {
			try {
				File conf = new File(PseudoPlayers.plugin.getDataFolder(), "config.yml");
				conf.delete();
				PseudoPlayers.plugin.saveDefaultConfig();
				PseudoPlayers.plugin.reloadConfig();
			} catch (Exception e) {
				PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.GENERIC);
				return false;
			}
			PseudoPlayers.getConfigOptions().reloadConfig();
			PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoapi.config_reset"));
			return true;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoapi.permission_reset_config"));
			return false;
		}
	}

}
