package io.github.pseudoresonance.pseudoplayers.commands;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import io.github.pseudoresonance.pseudoapi.bukkit.utils.Utils;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;
import io.github.pseudoresonance.pseudoapi.bukkit.Chat;
import io.github.pseudoresonance.pseudoapi.bukkit.PseudoAPI;
import io.github.pseudoresonance.pseudoapi.bukkit.SubCommandExecutor;
import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoapi.bukkit.messaging.PluginMessenger;
import io.github.pseudoresonance.pseudoapi.bukkit.messaging.PluginMessengerListener;

public class PingSC implements SubCommandExecutor, PluginMessengerListener {
	
	private static final long TIMEOUT_PERIOD = 1000;

	private static Class<?> craftPlayerClass = null;
	private static Class<?> entityPlayerClass = null;
	private static Method handleMethod = null;
	private static Field pingField = null;
	
	private static ConcurrentHashMap<String, PingRequest> pingRequest = new ConcurrentHashMap<String, PingRequest>();
	
	private static BukkitRunnable requestClearTimer = null;
	
	public static void setup() {
		if (requestClearTimer == null || requestClearTimer.isCancelled()) {
			requestClearTimer = new BukkitRunnable() {
				@Override
				public void run() {
					Iterator<Entry<String, PingRequest>> iterator = pingRequest.entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<String, PingRequest> entry = iterator.next();
						if (System.nanoTime() - entry.getValue().requestTime > TIMEOUT_PERIOD * 1000000) {
							pingRequest.remove(entry.getKey());
							PseudoPlayers.plugin.doSync(() -> {
								CommandSender sender = entry.getValue().requestingSender;
								if (sender == null) {
									Player p = Bukkit.getPlayer(entry.getValue().requestingPlayer);
									if (p != null)
										if (entry.getValue().requestingPlayer.toString().equals(entry.getValue().requestedPlayer))
											PseudoAPI.plugin.getChat().sendPluginMessage(p, LanguageManager.getLanguage(p).getMessage("pseudoplayers.your_ping", entry.getValue().spigotPing));
										else
											PseudoAPI.plugin.getChat().sendPluginMessage(p, LanguageManager.getLanguage(p).getMessage("pseudoplayers.players_ping", entry.getValue().requestedName, entry.getValue().spigotPing));
								} else
									PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.players_ping", entry.getValue().requestedName, entry.getValue().spigotPing));
							});
						}
					}
				}
			};
			requestClearTimer.runTaskTimerAsynchronously(PseudoPlayers.plugin, 20, 20);
		}
	}

	@Override
	public void onMessageReceived(Player p, String subchannel, byte[] data) {
		if (subchannel.equals("ping")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(data);
			in.readUTF();
			in.readUTF();
			int ping = in.readInt();
			String requestUuid = in.readUTF();
			PingRequest req = pingRequest.remove(requestUuid);
			if (req != null) {
				CommandSender sender = req.requestingSender;
				if (sender == null) {
					Player pl = Bukkit.getPlayer(req.requestingPlayer);
					if (pl != null)
						if (req.requestingPlayer.toString().equals(req.requestedPlayer))
							PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.your_ping", ping));
						else
							PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.players_ping", req.requestedName, ping));
				} else
					PseudoAPI.plugin.getChat().sendPluginMessage(sender, LanguageManager.getLanguage(sender).getMessage("pseudoplayers.players_ping", req.requestedName, ping));
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("pseudoplayers.ping")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					try {
						Player p = (Player) sender;
						int ping = getPlayerPing(p, sender, false);
						if (ping >= 0)
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
							int ping = getPlayerPing(p, sender, false);
							if (ping >= 0)
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

	private static int getPlayerPing(Player p, CommandSender sender, boolean forceSpigot) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
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
		if (io.github.pseudoresonance.pseudoapi.bukkit.Config.bungeeEnabled && !forceSpigot) {
			ArrayList<Object> data = new ArrayList<Object>();
			String requestUUID = UUID.randomUUID().toString();
			data.add(p.getUniqueId().toString());
			data.add(requestUUID);
			PluginMessenger.sendToBungee(p, "ping", data);
			if (sender instanceof Player)
				pingRequest.put(requestUUID, new PingRequest(p.getUniqueId().toString(), p.getDisplayName(), ((Player) sender).getUniqueId(), ping));
			else
				pingRequest.put(requestUUID, new PingRequest(p.getUniqueId().toString(), p.getDisplayName(), sender, ping));
			return -1;
		}
		return ping;
	}
	
	private static class PingRequest {

		public final long requestTime;
		public final String requestedPlayer;
		public final String requestedName;
		public final UUID requestingPlayer;
		public final CommandSender requestingSender;
		public final int spigotPing;
		
		public PingRequest(String requestedPlayer, String requestedName, UUID requestingPlayer, int ping) {
			requestTime = System.nanoTime();
			this.requestedPlayer = requestedPlayer;
			this.requestedName = requestedName;
			this.requestingPlayer = requestingPlayer;
			requestingSender = null;
			spigotPing = ping;
		}
		
		public PingRequest(String requestedPlayer, String requestedName, CommandSender requestingSender, int ping) {
			requestTime = System.nanoTime();
			this.requestedPlayer = requestedPlayer;
			this.requestedName = requestedName;
			this.requestingSender = requestingSender;
			requestingPlayer = null;
			spigotPing = ping;
		}
	}

}
