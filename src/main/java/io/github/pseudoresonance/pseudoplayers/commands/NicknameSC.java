package io.github.pseudoresonance.pseudoplayers.commands;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Message.Errors;
import io.github.pseudoresonance.pseudoapi.bukkit.messaging.PluginMessenger;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;

public class NicknameSC implements SubCommandExecutor {

	Pattern colorPattern = Pattern.compile("&[0-9a-f]", Pattern.CASE_INSENSITIVE);
	Pattern formattingPattern = Pattern.compile("&[l-o]", Pattern.CASE_INSENSITIVE);
	Pattern obfuscatePattern = Pattern.compile("&[k]", Pattern.CASE_INSENSITIVE);
	Pattern colorFormattingPattern = Pattern.compile("&[0-9a-fl-o]", Pattern.CASE_INSENSITIVE);
	Pattern colorObfuscatePattern = Pattern.compile("&[0-9a-fk]", Pattern.CASE_INSENSITIVE);
	Pattern formattingObfuscatePattern = Pattern.compile("&[l-ok]", Pattern.CASE_INSENSITIVE);
	Pattern asciiPattern = Pattern.compile("[^ -~]");
	Pattern allPattern = Pattern.compile("&[0-9a-frl-ok]", Pattern.CASE_INSENSITIVE);

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.nickname")) {
			if (args.length == 0) {
				PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Valid subcommands are: set, reset, setplayer, view");
				return false;
			} else if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("set")) {
					if (sender instanceof Player) {
						if (sender.hasPermission("pseudoplayers.nickname.set")) {
							if (args.length == 1) {
								PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a new nickname!");
								return false;
							} else {
								String nickname = "";
								for (int i = 1; i < args.length; i++) {
									nickname += (i != args.length - 1) ? args[i] + " " : args[i];
								}
								if (!sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = formattingPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = obfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorFormattingPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorObfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = formattingObfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = allPattern.matcher(nickname);
									nickname = match.replaceAll("");
								}
								if (!sender.hasPermission("pseudoplayers.nickname.special")) {
									Matcher match = asciiPattern.matcher(nickname);
									nickname = match.replaceAll("");
									nickname = nickname.trim();
								}
								if (nickname.length() > 100)
									nickname = nickname.substring(0, 100);
								if (nickname.length() <= 0) {
									PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a valid nickname!");
									return false;
								}
								nickname = ChatColor.translateAlternateColorCodes('&', nickname);
								PlayerDataController.setPlayerSetting(((Player) sender).getUniqueId().toString(), "nickname", nickname);
								((Player) sender).setDisplayName(nickname);
								ArrayList<Object> data = new ArrayList<Object>();
								data.add(((Player) sender).getUniqueId().toString());
								data.add(nickname);
								PluginMessenger.sendToBungee(((Player) sender), "displayname", data);
								PseudoPlayers.message.sendPluginMessage(sender, "Your nickname has been set to " + nickname);
								return true;
							}
						} else {
							PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "change your nickname!");
							return false;
						}
					} else {
						PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please use setplayer instead of set!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("reset")) {
					if (args.length == 1) {
						if (sender instanceof Player) {
							PlayerDataController.setPlayerSetting(((Player) sender).getUniqueId().toString(), "nickname", null);
							((Player) sender).setDisplayName(null);
							ArrayList<Object> data = new ArrayList<Object>();
							data.add(((Player) sender).getUniqueId().toString());
							PluginMessenger.sendToBungee(((Player) sender), "resetdisplayname", data);
							PseudoPlayers.message.sendPluginMessage(sender, "Your nickname has been reset!");
							return true;
						} else {
							PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a player!");
							return false;
						}
					} else {
						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							PseudoPlayers.message.sendPluginError(sender, Errors.NOT_ONLINE, args[1]);
							return false;
						} else {
							String uuid = p.getUniqueId().toString();
							PlayerDataController.setPlayerSetting(uuid, "nickname", null);
							p.setDisplayName(null);
							ArrayList<Object> data = new ArrayList<Object>();
							data.add(uuid);
							PluginMessenger.sendToBungee(p, "resetdisplayname", data);
							PseudoPlayers.message.sendPluginMessage(p, "Your nickname has been reset!");
							PseudoPlayers.message.sendPluginMessage(sender, p.getName() + "'s nickname has been reset!");
							return true;
						}
					}
				} else if (args[0].equalsIgnoreCase("setplayer")) {
					if (sender.hasPermission("pseudoplayers.nickname.set.others")) {
						if (args.length == 1) {
							PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a player and a new nickname!");
							return false;
						} else if (args.length == 2) {
							PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a new nickname!");
							return false;
						} else {
							Player p = Bukkit.getPlayer(args[1]);
							if (p == null) {
								PseudoPlayers.message.sendPluginError(sender, Errors.NOT_ONLINE, args[1]);
								return false;
							} else {
								String uuid = p.getUniqueId().toString();
								String nickname = "";
								for (int i = 2; i < args.length; i++) {
									nickname += (i != args.length - 1) ? args[i] + " " : args[i];
								}
								if (!sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = formattingPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = obfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorFormattingPattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = colorObfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = formattingObfuscatePattern.matcher(nickname);
									nickname = match.replaceAll("");
								} else if (!sender.hasPermission("pseudoplayers.nickname.colors") && !sender.hasPermission("pseudoplayers.nickname.formatting") && !sender.hasPermission("pseudoplayers.nickname.obfuscated")) {
									Matcher match = allPattern.matcher(nickname);
									nickname = match.replaceAll("");
								}
								if (!sender.hasPermission("pseudoplayers.nickname.special")) {
									Matcher match = asciiPattern.matcher(nickname);
									nickname = match.replaceAll("");
									nickname = nickname.trim();
								}
								if (nickname.length() > 100)
									nickname = nickname.substring(0, 100);
								if (nickname.length() <= 0) {
									PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a valid nickname!");
									return false;
								}
								nickname = ChatColor.translateAlternateColorCodes('&', nickname);
								PlayerDataController.setPlayerSetting(uuid, "nickname", nickname);
								p.setDisplayName(nickname);
								ArrayList<Object> data = new ArrayList<Object>();
								data.add(uuid);
								data.add(nickname);
								PluginMessenger.sendToBungee(p, "displayname", data);
								PseudoPlayers.message.sendPluginMessage(p, "Your nickname has been set to " + nickname);
								PseudoPlayers.message.sendPluginMessage(sender, p.getName() + "'s nickname has been set to " + nickname);
								return true;
							}
						}
					} else {
						PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "change other players' nicknames!");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("view")) {
					if (args.length == 1) {
						if (sender instanceof Player) {
							if (sender.hasPermission("pseudoplayers.nickname.view")) {
								Object o = PlayerDataController.getPlayerSetting(((Player) sender).getUniqueId().toString(), "nickname");
								if (o == null) {
									PseudoPlayers.message.sendPluginMessage(sender, "Your don't have a nickname");
								} else {
									PseudoPlayers.message.sendPluginMessage(sender, "Your nickname is " + o);
								}
								return true;
							} else {
								PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "view your nickname!");
								return false;
							}
						} else {
							PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a player!");
							return false;
						}
					} else {
						if (sender.hasPermission("pseudoplayers.nickname.view.others")) {
							String uuid = PlayerDataController.getUUID(args[1]);
							Player p = Bukkit.getPlayer(args[1]);
							if (p == null && uuid != null) {
								Object o = PlayerDataController.getPlayerSetting(uuid, "nickname");
								if (o == null) {
									PseudoPlayers.message.sendPluginMessage(sender, PlayerDataController.getName(uuid) + " does not have a nickname");
								} else {
									PseudoPlayers.message.sendPluginMessage(sender, PlayerDataController.getName(uuid) + "'s nickname is " + o);
								}
								return true;
							} else if (p == null && uuid == null) {
								PseudoPlayers.message.sendPluginError(sender, Errors.NEVER_JOINED, args[1]);
								return false;
							} else {
								Object o = PlayerDataController.getPlayerSetting(uuid, "nickname");
								if (o == null) {
									PseudoPlayers.message.sendPluginMessage(sender, PlayerDataController.getName(uuid) + " does not have a nickname");
								} else {
									PseudoPlayers.message.sendPluginMessage(sender, PlayerDataController.getName(uuid) + "'s nickname is " + o);
								}
								return true;
							}
						} else {
							PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "view other players' nicknames!");
							return false;
						}
					}
				} else {
					PseudoPlayers.message.sendPluginError(sender, Errors.CUSTOM, "Valid subcommands are: set, reset, setplayer, view");
					return false;
				}
			}
			return false;
		} else {
			PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "change your nickname!");
			return false;
		}
	}

}
