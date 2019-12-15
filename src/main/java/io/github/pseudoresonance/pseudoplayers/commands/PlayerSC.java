package io.github.pseudoresonance.pseudoplayers.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.Config;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.ServerPlayerDataController;
import io.github.pseudoresonance.pseudoapi.bukkit.utils.ChatComponent;
import io.github.pseudoresonance.pseudoapi.bukkit.utils.ChatComponent.ComponentType;
import io.github.pseudoresonance.pseudoapi.bukkit.utils.ChatElement;
import io.github.pseudoresonance.pseudoapi.bukkit.utils.ElementBuilder;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class PlayerSC implements SubCommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.view")) {
			boolean online = false;
			String uuid;
			String name;
			String nickname;
			if (args.length == 0) {
				if (sender instanceof Player) {
					uuid = ((Player) sender).getUniqueId().toString();
					name = ((Player) sender).getName();
				} else {
					PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_a_player_view_details"));
					return false;
				}
			} else {
				String pUuid = PlayerDataController.getUUID(args[0]);
				if (pUuid != null) {
					name = PlayerDataController.getName(pUuid);
					if (sender instanceof Player) {
						if (((Player) sender).getUniqueId().toString().equalsIgnoreCase(pUuid))
							uuid = pUuid;
						else {
							if (sender.hasPermission("pseudoplayers.view.others"))
								uuid = pUuid;
							else {
								PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_view_player_details_others"));
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
			}
			Player player = Bukkit.getServer().getPlayer(name);
			if (player != null) {
				online = true;
				nickname = player.getDisplayName();
			} else {
				Object nicknameO = PlayerDataController.getPlayerSetting(uuid, "nickname").join();
				if (nicknameO instanceof String) {
					nickname = (String) nicknameO;
				} else {
					nickname = "";
				}
			}
			List<Object> messages = new ArrayList<Object>();
			if (nickname != "")
				messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_details", nickname + Config.titleColor) + Config.borderColor + "---===");
			else
				messages.add(Config.borderColor + "===---" + Config.titleColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_details", name) + Config.borderColor + "---===");
			if (sender.hasPermission("pseudoplayers.view.uuid"))
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_uuid", Config.commandColor + uuid));
			if (sender.hasPermission("pseudoplayers.view.username"))
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_username", Config.commandColor + name));
			boolean onlineNetwork = false;
			if (online)
				onlineNetwork = true;
			else {
				Object onlineO = PlayerDataController.getPlayerSetting(uuid, "online").join();
				if (onlineO instanceof Boolean) {
					onlineNetwork = (Boolean) onlineO;
				}
			}
			Object firstJoinO = PlayerDataController.getPlayerSetting(uuid, "firstjoin").join();
			String firstJoinTime = "";
			if (firstJoinO != null) {
				Timestamp firstJoinTS = new Timestamp(System.currentTimeMillis());
				if (firstJoinO instanceof Timestamp) {
					firstJoinTS = (Timestamp) firstJoinO;
				}
				if (firstJoinO instanceof Date) {
					firstJoinTS = new Timestamp(((Date) firstJoinO).getTime());
				}
				LocalDate firstJoinDate = firstJoinTS.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				long firstJoinDays = ChronoUnit.DAYS.between(firstJoinDate, Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate());
				if (firstJoinDays >= io.github.pseudoresonance.pseudoplayers.Config.firstJoinTimeDifference) {
					firstJoinTime = LanguageManager.getLanguage(sender).formatDateTime(firstJoinTS);
				} else {
					firstJoinTime = LanguageManager.getLanguage(sender).formatTimeAgo(firstJoinTS, ChronoUnit.SECONDS, ChronoUnit.DAYS);
				}
			} else
				firstJoinTime = LanguageManager.getLanguage(sender).getMessage("pseudoplayers.unknown");
			messages.add(Config.descriptionColor + "First Joined: " + Config.commandColor + firstJoinTime);
			Object joinLeaveO = PlayerDataController.getPlayerSetting(uuid, "lastjoinleave").join();
			String joinLeaveTime = "";
			if (joinLeaveO != null) {
				Timestamp joinLeaveTS = new Timestamp(System.currentTimeMillis());
				if (joinLeaveO instanceof Timestamp) {
					joinLeaveTS = (Timestamp) joinLeaveO;
				}
				if (joinLeaveO instanceof Date) {
					joinLeaveTS = new Timestamp(((Date) joinLeaveO).getTime());
				}
				LocalDate joinLeaveDate = joinLeaveTS.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				long joinLeaveDays = ChronoUnit.DAYS.between(joinLeaveDate, Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate());
				if (joinLeaveDays >= io.github.pseudoresonance.pseudoplayers.Config.joinLeaveTimeDifference) {
					joinLeaveTime = LanguageManager.getLanguage(sender).getMessage("pseudoplayers.time_since", Config.commandColor + LanguageManager.getLanguage(sender).formatDateTime(joinLeaveTS));
				} else {
					joinLeaveTime = LanguageManager.getLanguage(sender).getMessage("pseudoplayers.time_for", Config.commandColor + LanguageManager.getLanguage(sender).formatTimeAgo(joinLeaveTS, false, ChronoUnit.SECONDS, ChronoUnit.DAYS));
				}
			} else
				joinLeaveTime = "Unknown";
			if (onlineNetwork)
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.time_online", joinLeaveTime));
			else
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.time_offline", joinLeaveTime));
			if (sender.hasPermission("pseudoplayers.view.playtime")) {
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
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_playtime", Config.commandColor + LanguageManager.getLanguage(sender).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtime), false, ChronoUnit.SECONDS, ChronoUnit.YEARS)));
			}
			if (online) {
				if (sender.hasPermission("pseudoplayers.view.location")) {
					Location loc = Bukkit.getServer().getPlayer(name).getLocation();
					String world = loc.getWorld().getName();
					String worldId = loc.getWorld().getUID().toString();
					String x = String.valueOf(loc.getBlockX());
					String y = String.valueOf(loc.getBlockY());
					String z = String.valueOf(loc.getBlockZ());
					String tpCommand = io.github.pseudoresonance.pseudoplayers.Config.teleportationFormat;
					tpCommand = tpCommand.replaceAll("\\{world\\}", worldId);
					tpCommand = tpCommand.replaceAll("\\{x\\}", x);
					tpCommand = tpCommand.replaceAll("\\{y\\}", y);
					tpCommand = tpCommand.replaceAll("\\{z\\}", z);
					messages.add(new ElementBuilder(new ChatElement(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_location") + " "), new ChatElement(Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.location_format", world, x, y, z), new ChatComponent(ComponentType.SUGGEST_COMMAND, "/" + tpCommand), new ChatComponent(ComponentType.SHOW_TEXT, Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.click_to_teleport")))).build());
				}
			} else {
				if (sender.hasPermission("pseudoplayers.view.logoutlocation")) {
					Object logoutLocationO = ServerPlayerDataController.getPlayerSetting(uuid, "logoutLocation").join();
					if (logoutLocationO != null) {
						if (logoutLocationO instanceof String) {
							String s = (String) logoutLocationO;
							String[] split = s.split(",");
							if (split.length >= 4) {
								String tpCommand = io.github.pseudoresonance.pseudoplayers.Config.teleportationFormat;
								World world = PseudoPlayers.plugin.getServer().getWorld(UUID.fromString(split[0]));
								String worldName = "";
								if (world != null) {
									worldName = world.getName();
									tpCommand = tpCommand.replaceAll("\\{world\\}", split[0]);
									tpCommand = tpCommand.replaceAll("\\{x\\}", split[2]);
									tpCommand = tpCommand.replaceAll("\\{y\\}", split[3]);
									tpCommand = tpCommand.replaceAll("\\{z\\}", split[4]);
									messages.add(new ElementBuilder(new ChatElement(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_logout_location") + " "), new ChatElement(Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.location_format", worldName, split[2], split[3], split[4]), new ChatComponent(ComponentType.SUGGEST_COMMAND, "/" + tpCommand), new ChatComponent(ComponentType.SHOW_TEXT, Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.click_to_teleport")))).build());
								} else {
									worldName = split[1];
									messages.add(new ElementBuilder(new ChatElement(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_logout_location") + " "), new ChatElement(Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.location_format", worldName, split[2], split[3], split[4]))).build());
								}
							}
						}
					}
				}
			}
			if (online) {
				if (sender.hasPermission("pseudoplayers.view.server"))
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.online_on", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.this_server")));
			} else if (onlineNetwork) {
				if (sender.hasPermission("pseudoplayers.view.server")) {
					Object lastServerO = PlayerDataController.getPlayerSetting(uuid, "lastserver").join();
					if (lastServerO instanceof String) {
						String lastServer = (String) lastServerO;
						messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.online_on", Config.commandColor + lastServer));
					}
				}
			} else {
				if (sender.hasPermission("pseudoplayers.view.lastserver")) {
					Object lastServerO = PlayerDataController.getPlayerSetting(uuid, "lastserver").join();
					if (lastServerO instanceof String) {
						String lastServer = (String) lastServerO;
						messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.last_online_on", Config.commandColor + lastServer));
					}
				}
			}
			if (PseudoPlayers.economy != null) {
				if (sender.hasPermission("pseudoplayers.view.balance")) {
					OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid));
					double bal = 0.0;
					String formatBal = "";
					try {
						Class<?> c = Class.forName("net.milkbowl.vault.economy.Economy");
						if (c.isInstance(PseudoPlayers.economy)) {
							Method balanceM = c.getMethod("getBalance", OfflinePlayer.class);
							Object balO = balanceM.invoke(PseudoPlayers.economy, op);
							if (balO instanceof Double) {
								bal = (Double) balO;
								Method formatM = c.getMethod("format", double.class);
								Object finalO = formatM.invoke(PseudoPlayers.economy, bal);
								if (finalO instanceof String) {
									formatBal = (String) finalO;
								}
							}
						}
					} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
						boolean exit = false;
						if (e instanceof InvocationTargetException) {
							if (e.getCause() != null) {
								if (e.getCause() instanceof RuntimeException) {
									exit = true;
								}
							}
						}
						if (!exit) {
							formatBal = LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error");
							PseudoPlayers.plugin.getChat().sendPluginError(Bukkit.getConsoleSender(), Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_getting_balance", PlayerDataController.getName(uuid)));
							e.printStackTrace();
						}
					}
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_balance", Config.commandColor + formatBal));
				}
			}
			if (sender.hasPermission("pseudoplayers.view.ip")) {
				if (online)
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_ip", Config.commandColor + Bukkit.getServer().getPlayer(name).getAddress().getAddress().getHostAddress()));
				else {
					Object ipO = PlayerDataController.getPlayerSetting(uuid, "ip").join();
					if (ipO instanceof String) {
						String ip = (String) ipO;
						if (!ip.equals("0.0.0.0")) {
							messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_ip", Config.commandColor + ip));
						}
					}
				}
			}
			if (sender.hasPermission("pseudoplayers.view.gamemode") && online) {
				GameMode gm = Bukkit.getServer().getPlayer(name).getGameMode();
				String mode = gm.toString();
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_gamemode", Config.commandColor + mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase()));
			}
			if (sender.hasPermission("pseudoplayers.view.health") && online) {
				AttributeInstance max = Bukkit.getServer().getPlayer(name).getAttribute(Attribute.GENERIC_MAX_HEALTH);
				int health = (int) Math.round(Bukkit.getServer().getPlayer(name).getHealth());
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_health", Config.commandColor + health, ((int) Math.round(max.getValue()))));
			}
			if (sender.hasPermission("pseudoplayers.view.hunger") && online) {
				int food = Bukkit.getServer().getPlayer(name).getFoodLevel();
				float sat = Bukkit.getServer().getPlayer(name).getSaturation();;
				messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_hunger", Config.commandColor + food, 20, sat));
			}
			if (sender.hasPermission("pseudoplayers.view.op") && online) {
				boolean op = Bukkit.getServer().getPlayer(name).isOp();
				if (op)
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_op", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.true")));
				else
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_op", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.false")));
			}
			if (Bukkit.getPluginManager().getPlugin("PseudoUtils") != null) {
				if (Bukkit.getPluginManager().getPlugin("PseudoUtils").isEnabled() && online) {
					if (sender.hasPermission("pseudoplayers.view.god")) {
						Object godO = ServerPlayerDataController.getPlayerSetting(uuid, "godMode").join();
						boolean god = false;
						if (godO instanceof Boolean) {
							god = (Boolean) godO;
						}
						if (god)
							messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_god_mode", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.enabled")));
						else
							messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_god_mode", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.disabled")));
					}
				}
			}
			if (sender.hasPermission("pseudoplayers.view.fly") && online) {
				boolean fly = Bukkit.getServer().getPlayer(name).getAllowFlight();
				if (fly) {
					boolean isFly = Bukkit.getServer().getPlayer(name).isFlying();
					if (isFly)
						messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_fly_mode", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.enabled_flying")));
					else
						messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_fly_mode", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.enabled")));
				}
				else
					messages.add(Config.descriptionColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.player_fly_mode", Config.commandColor + LanguageManager.getLanguage(sender).getMessage("pseudoplayers.disabled")));
			}
			Chat.sendMessage(sender, messages);
			return true;
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_view_player_details"));
		}
		return false;
	}

}
