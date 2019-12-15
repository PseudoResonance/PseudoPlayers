package io.github.pseudoresonance.pseudoplayers.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.Chat.Errors;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class ReloadLocalizationSC implements SubCommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.reloadlocalization")) {
			try {
				LanguageManager.copyDefaultPluginLanguageFiles(PseudoPlayers.plugin, false);
			} catch (Exception e) {
				PseudoPlayers.plugin.getChat().sendPluginError(sender, Errors.GENERIC);
				return false;
			}
			PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoapi.localization_reloaded"));
			return true;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoapi.permission_reload_localization"));
			return false;
		}
	}

}
