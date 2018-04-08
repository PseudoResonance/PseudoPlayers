package io.github.pseudoresonance.pseudoplayers.commands;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
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

public class PlayerSC implements SubCommandExecutor {

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player) || sender.hasPermission("pseudoplayers.view")) {
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
			List<Object> messages = new ArrayList<Object>();
			messages.add(ConfigOptions.border + "===---" + ConfigOptions.title + name + " Details" + ConfigOptions.border + "---===");
			Object firstJoinO = PlayerDataController.getPlayerSetting(uuid, "firstjoin");
			String firstJoinTime = "";
			if (firstJoinO != null) {
				Timestamp firstJoinTS = new Timestamp(System.currentTimeMillis());
				if (firstJoinO instanceof Timestamp) {
					firstJoinTS = (Timestamp) firstJoinO;
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
			if (Bukkit.getServer().getPlayer(name) != null)
				messages.add(ConfigOptions.description + "Online " + joinLeaveTime);
			else {
				messages.add(ConfigOptions.description + "Offline " + joinLeaveTime);
				if (sender.hasPermission("pseudoplayers.view.logoutlocation")) {
					Object logoutLocationO = PlayerDataController.getPlayerSetting(uuid, "logoutLocation");
					if (logoutLocationO != null) {
						if (logoutLocationO instanceof String) {
							String s = (String) logoutLocationO;
							String[] split = s.split(",");
							if (split.length >= 4)
								messages.add(new ElementBuilder(new ChatElement(ConfigOptions.description + "Logout Location: "), new ChatElement(ConfigOptions.command + "World: " + split[0] + " X: " + split[1] + " Y: " + split[2] + " Z: " + split[3], new ChatComponent(ComponentType.SUGGEST_COMMAND, "/tp @p " + split[1] + " " + split[2] + " " + split[3]), new ChatComponent(ComponentType.SHOW_TEXT, ConfigOptions.description + "Click to teleport to coordinates"))).build());
						}
					}
				}
			}
			Message.sendMessage(sender, messages);
			return true;
		} else {
			PseudoAPI.message.sendPluginError(sender, Errors.NO_PERMISSION, "view player details!");
		}
		return false;
	}

}
