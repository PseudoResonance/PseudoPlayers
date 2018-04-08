package io.github.pseudoresonance.pseudospawners.events;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import io.github.pseudoresonance.pseudoapi.bukkit.Message.Errors;
import io.github.pseudoresonance.pseudospawners.ConfigOptions;
import io.github.pseudoresonance.pseudospawners.PseudoSpawners;
import io.github.pseudoresonance.pseudospawners.SpawnerSettings;

public class BlockPlaceEH implements Listener {
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack is = e.getItemInHand();
		Player p = e.getPlayer();
		if (is.getType() == Material.MOB_SPAWNER) {
			Block b = e.getBlockPlaced();
			ItemMeta im = is.getItemMeta();
			if (im.hasDisplayName()) {
				String name = im.getDisplayName();
				name = name.replace(" Spawner", "");
				String entityName = ChatColor.stripColor(name);
				EntityType entity;
				try {
					entity = ConfigOptions.getEntity(entityName);
				} catch (IllegalArgumentException ex) {
					PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "That spawner is invalid!");
					e.setCancelled(true);
					return;
				}
				if (p.hasPermission("pseudospawners.override")) {
					try {
						if (b.getType() == Material.MOB_SPAWNER) {
							CreatureSpawner s = (CreatureSpawner) b.getState();
							s.setSpawnedType(entity);
							s.update();
							if (im.hasLore()) {
								setData(p, b, im.getLore());
							}
							return;
						} else {
							PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "You are not looking at a spawner!");
							return;
						}
					} catch (Exception ex) {
						PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "Minecraft disallows that entity type!");
						return;
					}
				} else {
					for (EntityType et : ConfigOptions.spawnable) {
						if (et == entity) {
							try {
								if (b.getType() == Material.MOB_SPAWNER) {
									if (p.hasPermission("pseudospawners.spawner." + entity.toString().toLowerCase())) {
										CreatureSpawner s = (CreatureSpawner) b.getState();
										s.setSpawnedType(entity);
										s.update();
										if (im.hasLore()) {
											setData(p, b, im.getLore());
										}
									} else {
										PseudoSpawners.message.sendPluginError(p, Errors.NO_PERMISSION, "You do not have permission to use that spawner!");
										e.setCancelled(true);
									}
									return;
								} else {
									PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "You are not looking at a spawner!");
									return;
								}
							} catch (Exception ex) {
								PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "Minecraft disallows that entity type!");
								return;
							}
						}
					}
				}
				PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, "That spawner is disabled!");
				e.setCancelled(true);
			}
		}
	}
	
	public boolean isEgg(ItemStack is) {
		Material m = is.getType();
		if (m == Material.MONSTER_EGGS) {
			return true;
		} else {
			return false;
		}
	}
	
	private void setData(Player p, Block b, List<String> lore) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String line : lore) {
			String strip = ChatColor.stripColor(line);
			String[] split = strip.split(": ");
			map.put(split[0], split[1]);
		}
		String msg = SpawnerSettings.setSettings(b, map);
		if (!msg.equals("")) {
			PseudoSpawners.message.sendPluginError(p, Errors.CUSTOM, msg);
		}
	}

}