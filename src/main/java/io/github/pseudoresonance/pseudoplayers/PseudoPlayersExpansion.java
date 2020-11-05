package io.github.pseudoresonance.pseudoplayers;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import io.github.pseudoresonance.pseudoapi.bukkit.language.LanguageManager;
import io.github.pseudoresonance.pseudoapi.bukkit.playerdata.PlayerDataController;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PseudoPlayersExpansion extends PlaceholderExpansion {
	
	private Plugin plugin;
	
	public PseudoPlayersExpansion(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getIdentifier() {
		return plugin.getName().toLowerCase();
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, String identifier) {
		switch (identifier) {
		case "first_join":
			if (player != null) {
				Object firstJoinO = PlayerDataController.getPlayerSetting(player.getUniqueId().toString(), "firstjoin");
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
						firstJoinTime = LanguageManager.getLanguage(player).formatDateTime(firstJoinTS);
					} else {
						firstJoinTime = LanguageManager.getLanguage(player).formatTimeAgo(firstJoinTS, ChronoUnit.SECONDS, ChronoUnit.DAYS);
					}
				} else
					firstJoinTime = LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_unknown");
				return firstJoinTime;
			} else {
				return LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_not_player");
			}
		case "online_since":
			if (player != null) {
				Object joinLeaveO = PlayerDataController.getPlayerSetting(player.getUniqueId().toString(), "lastjoinleave");
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
						joinLeaveTime = LanguageManager.getLanguage(player).formatDateTime(joinLeaveTS);
					} else {
						joinLeaveTime = LanguageManager.getLanguage(player).formatTimeAgo(joinLeaveTS, false, ChronoUnit.SECONDS, ChronoUnit.DAYS);
					}
				} else
					joinLeaveTime = LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_unknown");
				return joinLeaveTime;
			} else {
				return LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_not_player");
			}
		case "playtime":
			if (player != null) {
				Object playtimeO = PlayerDataController.getPlayerSetting(player.getUniqueId().toString(), "playtime");
				long playtime = 0;
				if (playtimeO instanceof BigInteger || playtimeO instanceof Long || playtimeO instanceof Integer) {
					if (playtimeO instanceof BigInteger)
						playtime = ((BigInteger) playtimeO).longValueExact();
					else if (playtimeO instanceof Long)
						playtime = (Long) playtimeO;
					else
						playtime = (Integer) playtimeO;
					Object o = PlayerDataController.getPlayerSetting(player.getUniqueId().toString(), "lastjoinleave").join();
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
					return LanguageManager.getLanguage(player).formatTimeAgo(new Timestamp(System.currentTimeMillis() - playtime), false, ChronoUnit.SECONDS, ChronoUnit.YEARS);
				}
				return LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_unknown");
			} else {
				return LanguageManager.getLanguage(player).getMessage("pseudoplayers.placeholder_not_player");
			}
		default:
			return "";
	}
	}

}
