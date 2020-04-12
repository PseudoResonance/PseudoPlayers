package io.github.pseudoresonance.pseudoplayers.commands;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.Config;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.Chat.Errors;
import io.github.pseudoresonance.pseudoapi.bukkit.data.Backend;
import io.github.pseudoresonance.pseudoapi.bukkit.data.Data;
import io.github.pseudoresonance.pseudoapi.bukkit.data.FileBackend;
import io.github.pseudoresonance.pseudoapi.bukkit.data.SQLBackend;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class PlaytopSC implements SubCommandExecutor {
	
	private static final long CACHE_EXPIRY = 300000;
	
	private static LinkedHashMap<String, Long> playtimeCache = null;
	private static long lastCache = 0;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.playtop")) {
			Backend b = Data.getGlobalBackend();
			final int page;
			if (args.length > 0) {
				try {
					page = Integer.valueOf(args[0]);
				} catch (NumberFormatException e) {
					PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NOT_A_NUMBER, args[0]);
					return false;
				}
			} else
				page = 1;
			if (System.currentTimeMillis() - lastCache > CACHE_EXPIRY) {
				PseudoPlayers.plugin.doAsync(() -> {
					if (b instanceof FileBackend) {
						FileBackend fb = (FileBackend) b;
						File folder = new File(fb.getFolder(), "Players");
						try {
							LinkedHashMap<String, Long> map = new LinkedHashMap<String, Long>();
							for (File f : folder.listFiles()) {
								if (f.isFile() && f.getName().endsWith(".yml")) {
									String uuid = f.getName().substring(0, f.getName().length() - 4);
									Player p = Bukkit.getPlayer(UUID.fromString(uuid));
									YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
									Object o = c.get("playtime");
									if (o instanceof BigInteger || o instanceof Long || o instanceof Integer) {
										long playtime = 0;
										String name = "";
										if (o instanceof BigInteger)
											playtime = ((BigInteger) o).longValueExact();
										else if (o instanceof Long)
											playtime = (Long) o;
										else
											playtime = (Integer) o;
										if (p != null) {
											Object jl = c.get("lastjoinleave");
											Timestamp joinLeaveTS = null;
											if (jl instanceof Date) {
												joinLeaveTS = new Timestamp(((Date) jl).getTime());
											}
											if (joinLeaveTS != null) {
												long joinLeave = joinLeaveTS.getTime();
												long diff = System.currentTimeMillis() - joinLeave;
												playtime += diff;
											}
											name = p.getDisplayName();
										} else {
											String nick = c.getString("nickname");
											if (nick == null || nick.length() == 0) {
												name = c.getString("username");
											} else {
												name = nick;
											}
										}
										map.put(name, playtime);
									}
								}
							}
							lastCache = System.currentTimeMillis();
							playtimeCache = map.entrySet().stream()
					        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
					        .collect(Collectors.toMap(
					                Map.Entry::getKey, 
					                Map.Entry::getValue, 
					                (x,y)-> {throw new AssertionError();},
					                LinkedHashMap::new
					        ));
						} catch (SecurityException e) {
							PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "No permission to access: " + folder.getAbsolutePath());
						}
					} else if (b instanceof SQLBackend) {
						SQLBackend sb = (SQLBackend) b;
						DataSource data = sb.getDataSource();
						try (Connection c = data.getConnection()) {
							try (PreparedStatement ps = c.prepareStatement("SELECT IFNULL(nickname, username) AS 'name',CAST((IF (online=1, TIMESTAMPDIFF(MICROSECOND, LASTJOINLEAVE, NOW()), 0)/1000 + playtime) AS UNSIGNED) AS onlinetime FROM `" + sb.getPrefix() + "Players` ORDER BY onlinetime DESC;")) {
								try (ResultSet rs = ps.executeQuery()) {
									playtimeCache = new LinkedHashMap<String, Long>();
									while (rs.next()) {
										Object o = rs.getObject(2);
										long playtime = 0;
										if (o instanceof BigInteger || o instanceof Long || o instanceof Integer) {
											if (o instanceof BigInteger)
												playtime = ((BigInteger) o).longValueExact();
											else if (o instanceof Long)
												playtime = (Long) o;
											else
												playtime = (Integer) o;
										}
										String name = rs.getString(1);
										playtimeCache.put(name, playtime);
									}
									lastCache = System.currentTimeMillis();
								} catch (SQLException e) {
									PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error when getting total playtime from table: " + sb.getPrefix() + "Players in database: " + sb.getName());
									PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
									PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_playtop"));
									return;
								}
							} catch (SQLException e) {
								PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error when preparing statement in database: " + sb.getName());
								PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_playtop"));
								return;
							}
						} catch (SQLException e) {
							PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error while accessing database: " + sb.getName());
							PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_playtop"));
							return;
						}
					} else {
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_playtop"));
						return;
					}
					int total = (playtimeCache.size() - 1) / 10 + 1;
					int usrPage = page;
					if (usrPage > total || usrPage <= 0) {
						usrPage = 1;
					}
					ArrayList<String> messages = new ArrayList<String>();
					messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.playtop_page") + Config.borderColor + "---===");
					Iterator<Entry<String, Long>> iter = playtimeCache.entrySet().iterator();
					for (int i = 0; i < (usrPage - 1) * 10; i++)
						iter.next();
					for (int i = 1; i <= 10; i++) {
						if (iter.hasNext()) {
							Entry<String, Long> entry = iter.next();
							messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.playtop_entry", (usrPage - 1) * 10 + i, entry.getKey() + Config.descriptionColor, Config.commandColor + LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - entry.getValue()), false, ChronoUnit.SECONDS, ChronoUnit.YEARS) + Config.descriptionColor));
						} else
							break;
					}
					messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoapi.page_number", usrPage, total) + Config.borderColor + "---===");
					Chat.sendMessage(sender, messages);
				});
				return true;
			} else {
				int total = (playtimeCache.size() - 1) / 10 + 1;
				int usrPage = page;
				if (usrPage > total || usrPage <= 0) {
					usrPage = 1;
				}
				ArrayList<String> messages = new ArrayList<String>();
				messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.playtop_page") + Config.borderColor + "---===");
				Iterator<Entry<String, Long>> iter = playtimeCache.entrySet().iterator();
				for (int i = 0; i < (usrPage - 1) * 10; i++)
					iter.next();
				for (int i = 1; i <= 10; i++) {
					if (iter.hasNext()) {
						Entry<String, Long> entry = iter.next();
						messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.playtop_entry", (usrPage - 1) * 10 + i, entry.getKey() + Config.descriptionColor, Config.commandColor + LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - entry.getValue()), false, ChronoUnit.SECONDS, ChronoUnit.YEARS) + Config.descriptionColor));
					} else
						break;
				}
				messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoapi.page_number", usrPage, total) + Config.borderColor + "---===");
				Chat.sendMessage(sender, messages);
				return true;
			}
		} else
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_playtop"));
		return false;
	}

}
