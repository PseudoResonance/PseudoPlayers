package io.github.pseudoresonance.pseudoplayers.commands;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.messaging.PluginMessenger;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
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
				PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.INVALID_SUBCOMMAND, "'set', 'reset', 'setplayer', 'view'");
				return false;
			} else if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("set")) {
					if (sender instanceof Player) {
						if (sender.hasPermission("pseudoplayers.nickname.set")) {
							if (args.length == 1) {
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_new_nickname"));
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
								}
								nickname = nickname.trim();
								if (nickname.length() > 100)
									nickname = nickname.substring(0, 100);
								if (nickname.length() <= 0) {
									PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_valid_nickname"));
									return false;
								}
								nickname = ChatColor.translateAlternateColorCodes('&', nickname);
								PlayerDataController.setPlayerSetting(((Player) sender).getUniqueId().toString(), "nickname", nickname);
								((Player) sender).setDisplayName(nickname);
								ArrayList<Object> data = new ArrayList<Object>();
								data.add(((Player) sender).getUniqueId().toString());
								data.add(nickname);
								PluginMessenger.sendToBungee(((Player) sender), "displayname", data);
								PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.your_nickname_set", nickname));
								return true;
							}
						} else {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_change_nickname"));
							return false;
						}
					} else {
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_use_setplayer"));
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
							PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.your_nickname_reset"));
							return true;
						} else {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_a_player"));
							return false;
						}
					} else {
						Player p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NOT_ONLINE, args[1]);
							return false;
						} else {
							String uuid = p.getUniqueId().toString();
							PlayerDataController.setPlayerSetting(uuid, "nickname", null);
							p.setDisplayName(null);
							ArrayList<Object> data = new ArrayList<Object>();
							data.add(uuid);
							PluginMessenger.sendToBungee(p, "resetdisplayname", data);
							PseudoPlayers.plugin.getChat().sendPluginMessage(p, LanguageManager.getLanguage(p).getMessage("pseudoplayers.your_nickname_reset"));
							PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_nickname_reset", p.getName()));
							return true;
						}
					}
				} else if (args[0].equalsIgnoreCase("setplayer")) {
					if (sender.hasPermission("pseudoplayers.nickname.set.others")) {
						if (args.length == 1) {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_a_player_new_nickname"));
							return false;
						} else if (args.length == 2) {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_new_nickname"));
							return false;
						} else {
							Player p = Bukkit.getPlayer(args[1]);
							if (p == null) {
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NOT_ONLINE, args[1]);
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
									PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_valid_nickname"));
									return false;
								}
								nickname = ChatColor.translateAlternateColorCodes('&', nickname);
								PlayerDataController.setPlayerSetting(uuid, "nickname", nickname);
								p.setDisplayName(nickname);
								ArrayList<Object> data = new ArrayList<Object>();
								data.add(uuid);
								data.add(nickname);
								PluginMessenger.sendToBungee(p, "displayname", data);
								PseudoPlayers.plugin.getChat().sendPluginMessage(p, LanguageManager.getLanguage(p).getMessage("pseudoplayers.your_nickname_set", nickname));
								PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_nickname_set", p.getName(), nickname));
								return true;
							}
						}
					} else {
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_change_nickname_others"));
						return false;
					}
				} else if (args[0].equalsIgnoreCase("view")) {
					if (args.length == 1) {
						if (sender instanceof Player) {
							if (sender.hasPermission("pseudoplayers.nickname.view")) {
								Object o = PlayerDataController.getPlayerSetting(((Player) sender).getUniqueId().toString(), "nickname").join();
								if (o == null) {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.you_no_nickname"));
								} else {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.your_nickname_is", o));
								}
								return true;
							} else {
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_view_nickname"));
								return false;
							}
						} else {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_a_player"));
							return false;
						}
					} else {
						if (sender.hasPermission("pseudoplayers.nickname.view.others")) {
							String uuid = PlayerDataController.getUUID(args[1]);
							Player p = Bukkit.getPlayer(args[1]);
							if (p == null && uuid != null) {
								Object o = PlayerDataController.getPlayerSetting(uuid, "nickname").join();
								if (o == null) {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_no_nickname", PlayerDataController.getName(uuid)));
								} else {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_nickname_is", PlayerDataController.getName(uuid), o));
								}
								return true;
							} else if (p == null && uuid == null) {
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NEVER_JOINED, args[1]);
								return false;
							} else {
								Object o = PlayerDataController.getPlayerSetting(uuid, "nickname").join();
								if (o == null) {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_no_nickname", PlayerDataController.getName(uuid)));
								} else {
									PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_nickname_is", PlayerDataController.getName(uuid), o));
								}
								return true;
							}
						} else {
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_view_nickname_others"));
							return false;
						}
					}
				} else {
					PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.INVALID_SUBCOMMAND, "'set', 'reset', 'setplayer', 'view'");
					return false;
				}
			}
			return false;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_change_nickname"));
			return false;
		}
	}

}
