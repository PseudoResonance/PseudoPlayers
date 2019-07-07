package io.github.pseudoresonance.pseudoplayers.commands;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.Message.Errors;
import io.github.pseudoresonance.pseudoapi.bukkit.utils.Utils;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoAPI;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;

public class PingSC implements SubCommandExecutor {

	private Class<?> craftPlayerClass = null;
	private Class<?> entityPlayerClass = null;
	private Method handleMethod = null;
	private Field pingField = null;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("pseudoplayers.ping")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					try {
						Player p = (Player) sender;
						if (craftPlayerClass == null)
							craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Utils.getBukkitVersion() + ".entity.CraftPlayer");
						if (entityPlayerClass == null)
							entityPlayerClass = Class.forName("net.minecraft.server." + Utils.getBukkitVersion() + ".EntityPlayer");
						Object craftPlayer = craftPlayerClass.cast(p);
						if (handleMethod == null)
							handleMethod = craftPlayerClass.getMethod("getHandle");
						Object entityPlayer = handleMethod.invoke(craftPlayer);
						if (pingField == null)
							pingField = entityPlayerClass.getField("ping");
						int ping = pingField.getInt(entityPlayer);
						PseudoAPI.message.sendPluginMessage(sender, "Your Ping: " + ping + "ms");
						return true;
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
						e.printStackTrace();
						PseudoAPI.message.sendPluginError(sender, Errors.CUSTOM, "There was an error while getting your ping! Please contact an administrator!");
						return false;
					}
				} else {
					PseudoAPI.message.sendPluginError(sender, Errors.CUSTOM, "Please specify a player whose ping to view!");
					return false;
				}
			} else {
				if (sender.hasPermission("pseudoplayers.ping.others")) {
					Player p = Bukkit.getPlayer(args[0]);
					if (p != null) {
						try {
							if (craftPlayerClass == null)
								craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + Utils.getBukkitVersion() + ".entity.CraftPlayer");
							if (entityPlayerClass == null)
								entityPlayerClass = Class.forName("net.minecraft.server." + Utils.getBukkitVersion() + ".EntityPlayer");
							Object craftPlayer = craftPlayerClass.cast(p);
							if (handleMethod == null)
								handleMethod = craftPlayerClass.getMethod("getHandle");
							Object entityPlayer = handleMethod.invoke(craftPlayer);
							if (pingField == null)
								pingField = entityPlayerClass.getField("ping");
							int ping = pingField.getInt(entityPlayer);
							PseudoAPI.message.sendPluginMessage(sender, p.getName() + "'s Ping: " + ping + "ms");
							return true;
						} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
							e.printStackTrace();
							PseudoAPI.message.sendPluginError(sender, Errors.CUSTOM, "There was an error while getting " + p.getName() + "'s ping! Please contact an administrator!");
							return false;
						}
					} else {
						PseudoPlayers.message.sendPluginError(sender, Errors.NOT_ONLINE, args[0]);
						return false;
					}
				} else {
					PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "get another player's ping!");
					return false;
				}
			}
		} else {
			PseudoPlayers.message.sendPluginError(sender, Errors.NO_PERMISSION, "get your ping!");
			return false;
		}
	}

}
