package io.github.pseudoresonance.pseudoplayers.listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;

public class PlayerJoinLeaveL implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent e) {
		PlayerDataController.setPlayerSetting(e.getPlayer().getUniqueId().toString(), "ip", e.getAddress().getHostAddress());
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent e) {
		HashMap<String, Object> settings = new HashMap<String, Object>();
		Player p = e.getPlayer();
		Location l = p.getLocation();
		String location = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		UUID u = p.getUniqueId();
		String uuid = u.toString();
		settings.put("logoutLocation", location);
		settings.put("ip", p.getAddress().getAddress().getHostAddress());
		PlayerDataController.setPlayerSettings(uuid, settings);
	}

}
