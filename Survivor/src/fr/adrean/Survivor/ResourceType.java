package fr.adrean.Survivor;

import org.bukkit.Material;

public enum ResourceType {
	XP(0, Material.NETHER_STAR, "niveau d'expérience", "niveaux d'expérience"),
	WHEAT(1, Material.WHEAT, "blé", "blés"),
	LEATHER(2, Material.LEATHER, "cuir", "cuirs"),
	GOLD(3, Material.GOLD_INGOT, "lingot d'or", "lingots d'or");
	
	private int id;
	private Material material;
	private String singular;
	private String plural;
	
	private ResourceType(int i, Material m, String s, String p) {
		this.id = i;
		this.material = m;
		this.singular = s;
		this.plural = p;
	}
	
	public int getID() {
		return this.id;
	}

	public Material getMaterial() {
		return material;
	}
	
	public String getDisplayName(boolean plural) {
		return plural ? this.plural : this.singular;
	}
}
