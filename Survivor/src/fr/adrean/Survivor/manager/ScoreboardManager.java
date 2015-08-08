package fr.adrean.Survivor.manager;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import fr.adrean.Survivor.Core;

public class ScoreboardManager {
	public static Core core;
	
	public static void update(Player p) {
		Objective o;
		Scoreboard s;
		if (p.getScoreboard() != null && p.getScoreboard().getObjective(DisplaySlot.SIDEBAR) != null && p.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName() != null && p.getScoreboard().getObjective(DisplaySlot.SIDEBAR).getName().equals("Resources")) {
			s = p.getScoreboard();
			o = s.getObjective(DisplaySlot.SIDEBAR);
			o.setDisplayName("\u00a7aRessources");
		} else {
			s = Bukkit.getScoreboardManager().getNewScoreboard();
			o = s.registerNewObjective("Resources", "dummy");
			o.setDisplayName("\u00a7aRessources");
			o.setDisplaySlot(DisplaySlot.SIDEBAR);
			p.setScoreboard(s);
		}
		Score wheat = o.getScore("\u00a76Blé :");
		Score gold = o.getScore("\u00a76Or :");
		Score leather = o.getScore("\u00a76Cuir :");
		wheat.setScore(ResourceManager.getWheat(p));
		gold.setScore(ResourceManager.getGold(p));
		leather.setScore(ResourceManager.getLeather(p));
		
	}
	
	public static void update(OfflinePlayer p) {
		if (p.isOnline()) update(p.getPlayer());
	}

	public static void update(Collection<? extends Player> players) {
		for (Player p : players) {
			update(p);
		}
	}
}
