package io.github.pseudoresonance.pseudoplayers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;

public class ReloadSC implements SubCommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.reload")) {
			try {
				PseudoPlayers.plugin.reloadConfig();
			} catch (Exception e) {
				PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.GENERIC);
				return false;
			}
			PseudoPlayers.getConfigOptions().reloadConfig();
			PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoapi.config_reloaded"));
			return true;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoapi.permission_reload_config"));
			return false;
		}
	}

}
