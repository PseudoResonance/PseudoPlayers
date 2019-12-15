package io.github.pseudoresonance.pseudoplayers.commands;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.pseudoresonance.pseudoapi.bukkit.utils.Utils;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.Config;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoAPI;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;

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
						PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.your_ping", ping));
						return true;
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
						e.printStackTrace();
						PseudoAPI.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_while_getting_ping"));
						return false;
					}
				} else {
					PseudoAPI.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_specify_player_ping"));
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
							PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.players_ping", p.getDisplayName(), ping));
							return true;
						} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
							e.printStackTrace();
							PseudoAPI.plugin.getChat().sendPluginError(sender, Chat.Errors.CUSTOM, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.error_while_getting_ping_others", p.getDisplayName()));
							return false;
						}
					} else {
						PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NOT_ONLINE, args[0]);
						return false;
					}
				} else {
					PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_get_ping_others"));
					return false;
				}
			}
		} else {
			PseudoPlayers.plugin.getChat().sendPluginError(sender, Chat.Errors.NO_PERMISSION, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.permission_get_ping"));
			return false;
		}
	}

}
