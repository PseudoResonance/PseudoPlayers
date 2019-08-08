package io.github.pseudoresonance.pseudoplayers.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.pseudoresonance.pseudoapi.bukkit.messaging.PluginMessenger;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.ServerPlayerDataController;
import io.github.pseudoresonance.pseudoplayers.PseudoPlayers;

public class PlayerJoinLeaveL implements Listener {
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		String uuid = e.getPlayer().getUniqueId().toString();
		Object nicknameO = PlayerDataController.getPlayerSetting(uuid, "nickname").join();
		if (nicknameO instanceof String) {
			String nickname = (String) nicknameO;
			e.getPlayer().setDisplayName(nickname);
			ArrayList<Object> data = new ArrayList<Object>();
			data.add(uuid);
			data.add(nickname);
			Bukkit.getScheduler().scheduleSyncDelayedTask(PseudoPlayers.plugin, new Runnable() {
				public void run() {
					PluginMessenger.sendToBungee(e.getPlayer(), "displayname", data);
				}
			}, 1);
		}
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		HashMap<String, Object> settings = new HashMap<String, Object>();
		Player p = e.getPlayer();
		Location l = p.getLocation();
		String location = l.getWorld().getUID().toString() + "," + l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		UUID u = p.getUniqueId();
		String uuid = u.toString();
		settings.put("logoutLocation", location);
		ServerPlayerDataController.setPlayerSettings(uuid, settings);
	}

}
