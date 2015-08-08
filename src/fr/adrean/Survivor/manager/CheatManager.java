package fr.adrean.Survivor.manager;

import java.util.ArrayList;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import fr.adrean.BlueCore.BlueCore;
import fr.adrean.Survivor.Click;
import fr.adrean.Survivor.Core;

public class CheatManager implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerCheat(PlayerInteractEvent e) {
		if (Core.playerLastClick.containsKey(e.getPlayer().getUniqueId()) && (System.currentTimeMillis() - Core.playerLastClick.get(e.getPlayer().getUniqueId()) < 500)) {
			if (Core.playerClicks.containsKey(e.getPlayer().getUniqueId())) {
				Core.playerClicks.get(e.getPlayer().getUniqueId()).add(Click.get(e.getAction()));
				ArrayList<Click> a = new ArrayList<Click>();
				a = Core.playerClicks.get(e.getPlayer().getUniqueId());
				if (a.size() >= Core.cheatSequence.length) {
					int diff = a.size() - Core.cheatSequence.length;
					Click c;
					for (int i = 0; i < Core.cheatSequence.length; i++) {
						c = a.get(i + diff);
						if (c != Core.cheatSequence[i]) {
							return;
						}
					}
					Core.playerClicks.remove(e.getPlayer().getUniqueId());
					Core.playerLastClick.remove(e.getPlayer().getUniqueId());
					if (e.getPlayer().getFoodLevel() >= 10) {
						BlueCore.boost(e.getPlayer(), 2);
						e.getPlayer().setFoodLevel(e.getPlayer().getFoodLevel() - 2);
					}
				}
			} else {
				ArrayList<Click> clicks = new ArrayList<Click>();
				clicks.add(Click.get(e.getAction()));
				Core.playerClicks.put(e.getPlayer().getUniqueId(), clicks);
			}
		} else {
			ArrayList<Click> clicks = new ArrayList<Click>();
			clicks.add(Click.get(e.getAction()));
			Core.playerClicks.put(e.getPlayer().getUniqueId(), clicks);
		}
		Core.playerLastClick.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
	}
}
