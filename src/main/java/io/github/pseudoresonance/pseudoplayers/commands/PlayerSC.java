package io.github.pseudoresonance.pseudoplayers.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.ChatComponent;
import io.github.pseudoresonance.pseudoapi.bukkit.ChatElement;
import io.github.pseudoresonance.pseudoapi.bukkit.ComponentType;
import io.github.pseudoresonance.pseudoapi.bukkit.ConfigOptions;
import io.github.pseudoresonance.pseudoapi.bukkit.ElementBuilder;
import io.github.pseudoresonance.pseudoapi.bukkit.Message;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoAPI;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.Utils;
import io.github.pseudoresonance.pseudoapi.bukkit.Message.Errors;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class PlayerSC implements SubCommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.view")) {
			boolean online = false;
			String uuid;
			String name;
			if (args.length == 0) {
				if (sender instanceof Player) {
					uuid = ((Player) sender).getUniqueId().toString();
					name = ((Player) sender).getName();
				} else {
					PseudoAPI.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a player to view details on!");
					return false;
				}
			} else {
				String pUuid = PlayerDataController.getUUID(args[0]);
				if (pUuid != null) {
					name = PlayerDataController.getName(pUuid);
					if (sender instanceof Player) {
						if (((Player) sender).getName().equalsIgnoreCase(name))
							uuid = pUuid;
						else {
							if (sender.hasPermission("pseudoplayers.view.others"))
								uuid = pUuid;
							else {
								PseudoAPI.message.sendPluginError(sender, Errors.NO_PERMISSION, "view other player's details!");
								return false;
							}
						}
					} else {
						uuid = pUuid;
					}
				} else {
					PseudoAPI.message.sendPluginError(sender, Errors.NEVER_JOINED, args[0]);
					return false;
				}
			}
			if (Bukkit.getServer().getPlayer(name) != null)
				online = true;
			List<Object> messages = new ArrayList<Object>();
			messages.add(ConfigOptions.border + "===---" + ConfigOptions.title + name + " Details" + ConfigOptions.border + "---===");
			if (sender.hasPermission("pseudoplayers.view.uuid"))
				messages.add(ConfigOptions.description + "UUID: " + ConfigOptions.command + uuid);
			Object firstJoinO = PlayerDataController.getPlayerSetting(uuid, "firstjoin");
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
				if (firstJoinDays >= io.github.pseudoresonance.pseudoplayers.ConfigOptions.firstJoinTimeDifference) {
					firstJoinTime = new SimpleDateFormat(io.github.pseudoresonance.pseudoplayers.ConfigOptions.firstJoinTimeFormat).format(firstJoinTS);
				} else {
					long diff = System.currentTimeMillis() - firstJoinTS.getTime();
					if (diff < 0) {
						diff = 0 - diff;
					}
					firstJoinTime = Utils.millisToHumanFormat(diff) + " ago";
				}
			} else
				firstJoinTime = "Unknown";
			messages.add(ConfigOptions.description + "First Joined: " + ConfigOptions.command + firstJoinTime);
			Object joinLeaveO = PlayerDataController.getPlayerSetting(uuid, "lastjoinleave");
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
				if (joinLeaveDays >= io.github.pseudoresonance.pseudoplayers.ConfigOptions.joinLeaveTimeDifference) {
					joinLeaveTime = "Since: " + ConfigOptions.command + new SimpleDateFormat(io.github.pseudoresonance.pseudoplayers.ConfigOptions.joinLeaveTimeFormat).format(joinLeaveTS);
				} else {
					long diff = System.currentTimeMillis() - joinLeaveTS.getTime();
					if (diff < 0) {
						diff = 0 - diff;
					}
					joinLeaveTime = "For: " + ConfigOptions.command + Utils.millisToHumanFormat(diff);
				}
			} else
				joinLeaveTime = "Unknown";
			if (online)
				messages.add(ConfigOptions.description + "Online " + joinLeaveTime);
			else
				messages.add(ConfigOptions.description + "Offline " + joinLeaveTime);
			if (sender.hasPermission("pseudoplayers.view.playtime")) {
				Object playtimeO = PlayerDataController.getPlayerSetting(uuid, "playtime");
				long playtime = 0;
				if (playtimeO instanceof BigInteger || playtimeO instanceof Long) {
					if (playtimeO instanceof BigInteger)
						playtime = ((BigInteger) playtimeO).longValueExact();
					else
						playtime = (Long) playtimeO;
				}
				if (online) {
					Object o = PlayerDataController.getPlayerSetting(uuid, "lastjoinleave");
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
				messages.add(ConfigOptions.description + "Playtime: " + ConfigOptions.command + Utils.millisToHumanFormat(playtime));
			}
			if (online) {
				if (sender.hasPermission("pseudoplayers.view.location")) {
					Location loc = Bukkit.getServer().getPlayer(name).getLocation();
					String world = loc.getWorld().getName();
					String x = String.valueOf(loc.getBlockX());
					String y = String.valueOf(loc.getBlockY());
					String z = String.valueOf(loc.getBlockZ());
					String tpCommand = io.github.pseudoresonance.pseudoplayers.ConfigOptions.teleportationFormat;
					tpCommand = tpCommand.replaceAll("\\{world\\}", world);
					tpCommand = tpCommand.replaceAll("\\{x\\}", x);
					tpCommand = tpCommand.replaceAll("\\{y\\}", y);
					tpCommand = tpCommand.replaceAll("\\{z\\}", z);
					messages.add(new ElementBuilder(new ChatElement(ConfigOptions.description + "Location: "), new ChatElement(ConfigOptions.command + "World: " + world + " X: " + x + " Y: " + y + " Z: " + z, new ChatComponent(ComponentType.SUGGEST_COMMAND, "/" + tpCommand), new ChatComponent(ComponentType.SHOW_TEXT, ConfigOptions.description + "Click to teleport to coordinates"))).build());
				}
			} else {
				if (sender.hasPermission("pseudoplayers.view.logoutlocation")) {
					Object logoutLocationO = PlayerDataController.getPlayerSetting(uuid, "logoutLocation");
					if (logoutLocationO != null) {
						if (logoutLocationO instanceof String) {
							String s = (String) logoutLocationO;
							String[] split = s.split(",");
							if (split.length >= 4) {
								String tpCommand = io.github.pseudoresonance.pseudoplayers.ConfigOptions.teleportationFormat;
								tpCommand = tpCommand.replaceAll("\\{world\\}", split[0]);
								tpCommand = tpCommand.replaceAll("\\{x\\}", split[1]);
								tpCommand = tpCommand.replaceAll("\\{y\\}", split[2]);
								tpCommand = tpCommand.replaceAll("\\{z\\}", split[3]);
								messages.add(new ElementBuilder(new ChatElement(ConfigOptions.description + "Logout Location: "), new ChatElement(ConfigOptions.command + "World: " + split[0] + " X: " + split[1] + " Y: " + split[2] + " Z: " + split[3], new ChatComponent(ComponentType.SUGGEST_COMMAND, "/" + tpCommand), new ChatComponent(ComponentType.SHOW_TEXT, ConfigOptions.description + "Click to teleport to coordinates"))).build());
							}
						}
					}
				}
			}
			if (PseudoPlayers.economy != null) {
				if (sender.hasPermission("pseudoplayers.view.balance")) {
					OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid));
					double bal = 0.0;
					String formatBal = "$0";
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
							PseudoPlayers.message.sendPluginError(Bukkit.getConsoleSender(), Errors.CUSTOM, "An error has occurred while getting the balance of: " + PlayerDataController.getName(uuid) + "! Please ensure Vault is up to date, and if so, report this to the author!");
							e.printStackTrace();
						}
					}
					messages.add(ConfigOptions.description + "Balance: " + ConfigOptions.command + formatBal);
				}
			}
			if (sender.hasPermission("pseudoplayers.view.ip")) {
				if (online)
					messages.add(ConfigOptions.description + "IP: " + ConfigOptions.command + Bukkit.getServer().getPlayer(name).getAddress().getAddress().getHostAddress());
				else {
					Object ipO = PlayerDataController.getPlayerSetting(uuid, "ip");
					if (ipO instanceof String) {
						String ip = (String) ipO;
						if (!ip.equals("0.0.0.0")) {
							messages.add(ConfigOptions.description + "IP: " + ConfigOptions.command + ip);
						}
					}
				}
			}
			if (sender.hasPermission("pseudoplayers.view.gamemode") && online) {
				GameMode gm = Bukkit.getServer().getPlayer(name).getGameMode();
				String mode = gm.toString();
				messages.add(ConfigOptions.description + "Gamemode: " + ConfigOptions.command + mode.substring(0, 1).toUpperCase() + mode.substring(1).toLowerCase());
			}
			if (sender.hasPermission("pseudoplayers.view.health") && online) {
				AttributeInstance max = Bukkit.getServer().getPlayer(name).getAttribute(Attribute.GENERIC_MAX_HEALTH);
				int health = (int) Math.round(Bukkit.getServer().getPlayer(name).getHealth());
				messages.add(ConfigOptions.description + "Health: " + ConfigOptions.command + health + "/" + ((int) Math.round(max.getValue())));
			}
			if (sender.hasPermission("pseudoplayers.view.hunger") && online) {
				int food = Bukkit.getServer().getPlayer(name).getFoodLevel();
				float sat = Bukkit.getServer().getPlayer(name).getSaturation();;
				messages.add(ConfigOptions.description + "Hunger: " + ConfigOptions.command + food + "/20 (+" + sat + " saturation)");
			}
			if (sender.hasPermission("pseudoplayers.view.op") && online) {
				boolean op = Bukkit.getServer().getPlayer(name).isOp();
				if (op)
					messages.add(ConfigOptions.description + "OP: " + ConfigOptions.command + "True");
				else
					messages.add(ConfigOptions.description + "OP: " + ConfigOptions.command + "False");
			}
			if (Bukkit.getPluginManager().getPlugin("PseudoUtils") != null) {
				if (Bukkit.getPluginManager().getPlugin("PseudoUtils").isEnabled() && online) {
					if (sender.hasPermission("pseudoplayers.view.god")) {
						Object godO = PlayerDataController.getPlayerSetting(uuid, "godMode");
						boolean god = false;
						if (godO instanceof Boolean) {
							god = (Boolean) godO;
						}
						if (god)
							messages.add(ConfigOptions.description + "God Mode: " + ConfigOptions.command + "Enabled");
						else
							messages.add(ConfigOptions.description + "God Mode: " + ConfigOptions.command + "Disabled");
					}
				}
			}
			if (sender.hasPermission("pseudoplayers.view.fly") && online) {
				boolean fly = Bukkit.getServer().getPlayer(name).getAllowFlight();
				if (fly) {
					boolean isFly = Bukkit.getServer().getPlayer(name).isFlying();
					if (isFly)
						messages.add(ConfigOptions.description + "Fly Mode: " + ConfigOptions.command + "Enabled (Flying)");
					else
						messages.add(ConfigOptions.description + "Fly Mode: " + ConfigOptions.command + "Enabled");
				}
				else
					messages.add(ConfigOptions.description + "Fly Mode: " + ConfigOptions.command + "Disabled");
			}
			Message.sendMessage(sender, messages);
			return true;
		} else {
			PseudoAPI.message.sendPluginError(sender, Errors.NO_PERMISSION, "view player details!");
		}
		return false;
	}

}
