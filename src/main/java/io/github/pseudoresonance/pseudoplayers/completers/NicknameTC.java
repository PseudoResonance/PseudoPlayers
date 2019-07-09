package io.github.pseudoresonance.pseudoplayers.completers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class NicknameTC implements TabCompleter {

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> possible = new ArrayList<String>();
		if (args.length == 1) {
			if (sender.hasPermission("pseudoplayers.nickname.set")) {
				possible.add("set");
				possible.add("reset");
			}
			if (sender.hasPermission("pseudoplayers.nickname.view")) {
				possible.add("view");
			}
			if (sender.hasPermission("pseudoplayers.nickname.set.others")) {
				possible.add("setplayer");
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
			if (args[0].equalsIgnoreCase("reset") || args[0].equalsIgnoreCase("setplayer")) {
				if (sender.hasPermission("pseudoplayers.nickname.set.others")) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						possible.add(p.getName());
					}
				}
			} else if (args[0].equalsIgnoreCase("view")) {
				if (sender.hasPermission("pseudoplayers.nickname.view.others")) {
					for (Player p : Bukkit.getOnlinePlayers()) {
						possible.add(p.getName());
					}
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
