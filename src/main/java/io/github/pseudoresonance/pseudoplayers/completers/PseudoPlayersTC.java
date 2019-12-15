package io.github.pseudoresonance.pseudoplayers.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;

public class PseudoPlayersTC implements TabCompleter {

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> possible = new ArrayList<String>();
		if (args.length == 1) {
			possible.add("help");
			if (sender.hasPermission("pseudoplayers.reload")) {
				possible.add("reload");
			}
			if (sender.hasPermission("pseudoplayers.reset")) {
				possible.add("reset");
			}
			if (sender.hasPermission("pseudoplayers.reloadlocalization")) {
				possible.add("reloadlocalization");
			}
			if (sender.hasPermission("pseudoplayers.resetlocalization")) {
				possible.add("resetlocalization");
			}
			if (sender.hasPermission("pseudoplayers.ping")) {
				possible.add("ping");
			}
			if (sender.hasPermission("pseudoplayers.view")) {
				possible.add("player");
			}
			if (args[0].equalsIgnoreCase("")) {
				return possible;
			} else {
				List<String> checked = new ArrayList<String>();
				for (String check : possible) {
					if (check.toLowerCase().startsWith(args[0].toLowerCase())) {
						checked.add(check);
					}
				}
				return checked;
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("player")) {
				if (sender.hasPermission("pseudoplayers.view.others")) {
					possible.addAll(PlayerDataController.getNames());
				}
			}
			if (args[1].equalsIgnoreCase("")) {
				return possible;
			} else {
				List<String> checked = new ArrayList<String>();
				for (String check : possible) {
					if (check.toLowerCase().startsWith(args[1].toLowerCase())) {
						checked.add(check);
					}
				}
				return checked;
			}
		}
		return null;
	}

}
