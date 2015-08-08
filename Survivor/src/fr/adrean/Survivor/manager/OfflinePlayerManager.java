package fr.adrean.Survivor.manager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.adrean.Survivor.Core;

public class OfflinePlayerManager implements Listener {
	
	public static Core core;
	
	public static void addMessage(OfflinePlayer p, String msg) {
		if (p.isOnline()) {
			p.getPlayer().sendMessage(msg);
		} else {
			List<String> a = new ArrayList<String>();
			if (core.getConfig().getStringList("offlinemsg." + p.getUniqueId().toString()) != null) {
				a = core.getConfig().getStringList("offlinemsg." + p.getUniqueId().toString());
			}
			core.getConfig().set("offlinemsg."+ p.getUniqueId().toString(), a);
			core.saveConfig();
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (core.getConfig().contains("offlinemsg." + e.getPlayer().getUniqueId().toString())) {
			for (String s : core.getConfig().getStringList("offlinemsg." + e.getPlayer().getUniqueId().toString())) {
				e.getPlayer().sendMessage(s);
			}
		}
		
	}
	
	
}
