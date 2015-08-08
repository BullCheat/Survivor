package fr.adrean.Survivor.manager;

import org.bukkit.OfflinePlayer;

import fr.adrean.Survivor.Core;

public class ResourceManager {
	public static Core core;

	public static void addWheat(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".wheat";
		core.getConfig().set(a, core.getConfig().getInt(a) + i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static void removeWheat(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".wheat";
		core.getConfig().set(a, core.getConfig().getInt(a) - i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static int getWheat(OfflinePlayer p) {
		String a = "resources." + p.getUniqueId().toString() + ".wheat";
		return core.getConfig().getInt(a);
	}

	public static void addGold(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".gold";
		core.getConfig().set(a, core.getConfig().getInt(a) + i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static void removeGold(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".gold";
		core.getConfig().set(a, core.getConfig().getInt(a) - i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static int getGold(OfflinePlayer p) {
		String a = "resources." + p.getUniqueId().toString() + ".gold";
		return core.getConfig().getInt(a);
	}

	public static void addLeather(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".leather";
		core.getConfig().set(a, core.getConfig().getInt(a) + i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static void removeLeather(OfflinePlayer p, int i) {
		String a = "resources." + p.getUniqueId().toString() + ".leather";
		core.getConfig().set(a, core.getConfig().getInt(a) - i);
		core.saveConfig();
		ScoreboardManager.update(p);
	}
	
	public static int getLeather(OfflinePlayer p) {
		String a = "resources." + p.getUniqueId().toString() + ".leather";
		return core.getConfig().getInt(a);
	}
	
}
