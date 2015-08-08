package fr.adrean.Survivor;

import org.bukkit.ChatColor;

public enum ShopType {
	MINER("Mineur"),
	FARMER("Fermier"),
	FORGER("Forgeron");
	private String name;
	ShopType(String s) {
		this.name = s;
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	public static ShopType getByName(String string) {
		string = ChatColor.stripColor(string);
		for (ShopType t : ShopType.values()) {
			if (t.toString().equalsIgnoreCase(string) || t.name().equalsIgnoreCase(string)) {
				return t;
			}
		}
		return null;
	}
}
