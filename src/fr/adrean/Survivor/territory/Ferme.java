package fr.adrean.Survivor.territory;

import java.util.HashMap;

import org.bukkit.Material;

import fr.adrean.Survivor.Core;

public class Ferme extends Territory {
	public static HashMap<String, Ferme> fermes = new HashMap<String, Ferme>();

	private Ferme(Core core, String id) {
		super(core, id);
		secsPerProduct = new short[] { 1000, 500, 250, 120, 60 };
		secsPerXP = new short[] { 144*60, 60*60, 29*60, 13*60, 6*60 };
	}
	
	public static Ferme get(Core core, String id) 
	{
		if (!fermes.containsKey(id)) {
			fermes.put(id, new Ferme(core, id));
		}
		return fermes.get(id);
	}
	
	@Override
	public Material getXPMaterial() 
	{
		return Material.LEATHER;
	}
	
	@Override
	public String[] getXPMaterialName() {
		return new String[] {"cuir", "cuirs", "de cuir"};
	}
	
	@Override
	public byte getMaxWaitingXp() {
		byte[] b = new byte[] {
				3,
				3,
				5,
				5,
				7
		};
		return b[this.getLevel() - 1];
	}
	
	@Override
	public byte getMaxXP() {
		byte[] b = new byte[] {
				20,
				20,
				25,
				30,
				40
		};
		return b[this.getLevel() - 1];
	}
	
	@Override
	public byte getMaxLevel() {
		return (byte) Math.min(secsPerProduct.length, secsPerXP.length);
	}
}
