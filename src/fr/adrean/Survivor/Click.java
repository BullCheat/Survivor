package fr.adrean.Survivor;

import org.bukkit.event.block.Action;

public enum Click {
	LEFT,
	RIGHT;
	public static Click get(Action action) {
		switch (action) {
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				return LEFT;
			default:
				return RIGHT;
		}
	}
}
