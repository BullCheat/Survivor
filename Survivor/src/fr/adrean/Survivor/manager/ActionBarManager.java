package fr.adrean.Survivor.manager;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.adrean.Survivor.Core;

public class ActionBarManager {

	private HashMap<String, String> tpMessages = new HashMap<String, String>();
	private HashMap<String, String> auraMessages = new HashMap<String, String>();
	private HashMap<String, String> bowMessages = new HashMap<String, String>();
	private HashMap<String, ArrayList<String>> infoMessages = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> warningMessages = new HashMap<String, ArrayList<String>>();
	
	public ActionBarManager(Core plugin) {
		Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			
			@Override
			public void run() {
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					String p = player.getName();
					if (warningMessages.containsKey(p)) {
						ArrayList<String> al = warningMessages.get(p);
						if (al == null || al.size() < 1) {
							warningMessages.remove(p);
						} else {
							sendActionBar(player, al.get(0));
							al.remove(0);
							warningMessages.remove(p);
							if (al.size() > 0) {
								warningMessages.put(p, al);
							}
							continue;
						}
					}
					if (infoMessages.containsKey(p)) {
						ArrayList<String> al = infoMessages.get(p);
						if (al == null || al.size() < 1) {
							infoMessages.remove(p);
						} else {
							sendActionBar(player, al.get(0));
							al.remove(0);
							infoMessages.remove(p);
							if (al.size() > 0) {
								infoMessages.put(p, al);
							}
							continue;
						}
					}
					if (tpMessages.containsKey(p)) {
						sendActionBar(player, tpMessages.get(p));
						tpMessages.remove(p);
						continue;
					}
					if (auraMessages.containsKey(p)) {
						sendActionBar(player, auraMessages.get(p));
						auraMessages.remove(p);
						continue;
					}
					if (bowMessages.containsKey(p)) {
						String msg= bowMessages.get(p);
						sendActionBar(player, msg);
						bowMessages.remove(p);
						continue;
					}
				}
				
			}
		}, 1L, 1L);
	}
	
	public void setBowMessage(Player player, String msg) {
		String p = player.getName();
		if (this.bowMessages.containsKey(p)) {
			this.bowMessages.remove(p);
		}
		if (msg != null) {
			this.bowMessages.put(p, msg);
		}
	}
	
	public void setTPMessage(Player player, String msg) {
		String p = player.getName();
		if (this.tpMessages.containsKey(p)) {
			this.tpMessages.remove(p);
		}
		if (msg != null) {
			this.tpMessages.put(p, msg);
		}
	}
	
	public void setAuraMessage(Player player, String msg) {
		String p = player.getName();
		if (this.auraMessages.containsKey(p)) {
			this.auraMessages.remove(p);
		}
		if (msg != null) {
			this.auraMessages.put(p, msg);
		}
	}

	public void addInfoMessage(Player player, String msg) {
		ArrayList<String> al;
		String p = player.getName();
		if (this.infoMessages.containsKey(p)) {
			al = this.infoMessages.get(p);
			this.infoMessages.remove(p);
		} else {
			al = new ArrayList<String>();
		}
		al.add(msg);
		this.infoMessages.put(p, al);
	}
	
	public void addWarningMessage(Player player, String msg) {
		ArrayList<String> al;
		String p = player.getName();
		if (this.warningMessages.containsKey(p)) {
			al = this.warningMessages.get(p);
			this.warningMessages.remove(p);
		} else {
			al = new ArrayList<String>();
		}
		al.add(msg);
		this.warningMessages.put(p, al);
	}

	public static void sendActionBar(Player player, String message){
		if (!(player instanceof CraftPlayer)) return;
        CraftPlayer p = (CraftPlayer) player;
        //if (p.getHandle().playerConnection.networkManager.getVersion() != 47) return; // Don't run if player is not on 1.8
        IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(ppoc);
    }
	
}
