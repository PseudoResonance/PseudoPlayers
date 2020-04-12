package io.github.pseudoresonance.pseudoplayers.commands;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

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
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class PlaytimeSC implements SubCommandExecutor {
	
	private static final long CACHE_EXPIRY = 300000;
	
	private static long playtimeCache = 0L;
	private static long lastCache = 0;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.playtime")) {
				Backend b = Data.getGlobalBackend();
				if (args.length == 0) {
					if (System.currentTimeMillis() - lastCache <= CACHE_EXPIRY) {
						PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.server_playtime", LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtimeCache), false, ChronoUnit.SECONDS, ChronoUnit.YEARS)));
						return true;
					}
					PseudoPlayers.plugin.doAsync(() -> {
						if (b instanceof FileBackend) {
							long playtime = 0;
							FileBackend fb = (FileBackend) b;
							File folder = new File(fb.getFolder(), "Players");
							try {
								for (File f : folder.listFiles()) {
									if (f.isFile() && f.getName().endsWith(".yml")) {
										String uuid = f.getName().substring(0, f.getName().length() - 4);
										Player p = Bukkit.getPlayer(UUID.fromString(uuid));
										YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
										Object o = c.get("playtime");
										if (o instanceof BigInteger || o instanceof Long || o instanceof Integer) {
											if (o instanceof BigInteger)
												playtime += ((BigInteger) o).longValueExact();
											else if (o instanceof Long)
												playtime += (Long) o;
											else
												playtime += (Integer) o;
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
											}
										}
									}
								}
							} catch (SecurityException e) {
								PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "No permission to access: " + folder.getAbsolutePath());
							}
							playtimeCache = playtime;
							lastCache = System.currentTimeMillis();
							PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.server_playtime", LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtime), false, ChronoUnit.SECONDS, ChronoUnit.YEARS)));
							return;
						} else if (b instanceof SQLBackend) {
							SQLBackend sb = (SQLBackend) b;
							DataSource data = sb.getDataSource();
							try (Connection c = data.getConnection()) {
								try (PreparedStatement ps = c.prepareStatement("SELECT CAST(SUM(IF (online=1, TIMESTAMPDIFF(MICROSECOND, LASTJOINLEAVE, NOW()), 0)/1000 + playtime) AS UNSIGNED) AS totaltime FROM `" + sb.getPrefix() + "Players`;")) {
									try (ResultSet rs = ps.executeQuery()) {
										if (rs.next()) {
											Object o = rs.getObject(1);
											long playtime = 0;
											if (o instanceof BigInteger || o instanceof Long || o instanceof Integer) {
												if (o instanceof BigInteger)
													playtime = ((BigInteger) o).longValueExact();
												else if (o instanceof Long)
													playtime = (Long) o;
												else
													playtime = (Integer) o;
											}
											playtimeCache = playtime;
											lastCache = System.currentTimeMillis();
											PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.server_playtime", LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtime), false, ChronoUnit.SECONDS, ChronoUnit.YEARS)));
											return;
										}
									} catch (SQLException e) {
										PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error when getting total playtime from table: " + sb.getPrefix() + "Players in database: " + sb.getName());
										PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
									}
								} catch (SQLException e) {
									PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error when preparing statement in database: " + sb.getName());
									PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
								}
							} catch (SQLException e) {
								PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "Error while accessing database: " + sb.getName());
								PseudoPlayers.plugin.getChat().sendConsolePluginError(Errors.CUSTOM, "SQLError " + e.getErrorCode() + ": (State: " + e.getSQLState() + ") - " + e.getMessage());
							}
						}
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_total_playtime"));
						return;
					});
				} else {
					boolean online = false;
					String uuid;
					String name;
					String pUuid = PlayerDataController.getUUID(args[0]);
					if (pUuid != null) {
						name = PlayerDataController.getName(pUuid);
						if (sender instanceof Player) {
							if (((Player) sender).getUniqueId().toString().equalsIgnoreCase(pUuid))
								uuid = pUuid;
							else {
								if (sender.hasPermission("pseudoplayers.playtime.others"))
									uuid = pUuid;
								else {
									PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_playtime_others"));
									return false;
								}
							}
						} else {
							uuid = pUuid;
						}
					} else {
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NEVER_JOINED, args[0]);
						return false;
					}
					Player player = Bukkit.getServer().getPlayer(name);
					if (player != null) {
						online = true;
						name = player.getDisplayName();
					} else {
						Object nicknameO = PlayerDataController.getPlayerSetting(uuid, "nickname").join();
						if (nicknameO instanceof String) {
							name = (String) nicknameO;
						}
					}
					boolean onlineNetwork = false;
					if (online)
						onlineNetwork = true;
					else {
						Object onlineO = PlayerDataController.getPlayerSetting(uuid, "online").join();
						if (onlineO instanceof Boolean) {
							onlineNetwork = (Boolean) onlineO;
						}
					}
					Object playtimeO = PlayerDataController.getPlayerSetting(uuid, "playtime").join();
					long playtime = 0;
					if (playtimeO instanceof BigInteger || playtimeO instanceof Long || playtimeO instanceof Integer) {
						if (playtimeO instanceof BigInteger)
							playtime = ((BigInteger) playtimeO).longValueExact();
						else if (playtimeO instanceof Long)
							playtime = (Long) playtimeO;
						else
							playtime = (Integer) playtimeO;
					}
					if (onlineNetwork) {
						Object o = PlayerDataController.getPlayerSetting(uuid, "lastjoinleave").join();
						Timestamp joinLeaveTS = null;
						if (o instanceof Timestamp) {
							joinLeaveTS = (Timestamp) o;
						}
						if (o instanceof Date) {
							joinLeaveTS = new Timestamp(((Date) o).getTime());
						}
						if (joinLeaveTS != null) {
							long joinLeave = joinLeaveTS.getTime();
							long diff = System.currentTimeMillis() - joinLeave;
							playtime += diff;
						}
					}
					PseudoPlayers.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_playtime_specific", name + Config.textColor, LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtime), false, ChronoUnit.SECONDS, ChronoUnit.YEARS)));
					return true;
				}
			return false;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_playtime"));
		}
		return false;
	}

}
