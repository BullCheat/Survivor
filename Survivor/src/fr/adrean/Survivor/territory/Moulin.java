package fr.adrean.Survivor.territory;

import java.util.HashMap;

import org.bukkit.Material;

import fr.adrean.Survivor.Core;

public class Moulin extends Territory {
	public static HashMap<String, Moulin> moulins = new HashMap<String, Moulin>();

	private Moulin(Core core, String id) {
		super(core, id);
		secsPerProduct = new short[] { 1000, 500, 250, 120, 60 };
		secsPerXP = new short[] { 144*60, 60*60, 29*60, 13*60, 6*60 };
	}
	
	public static Moulin get(Core core, String id) 
	{
		if (!moulins.containsKey(id)) {
			moulins.put(id, new Moulin(core, id));
		}
		return moulins.get(id);
	}
	
	@Override
	public Material getXPMaterial() 
	{
		return Material.WHEAT;
	}
	
	@Override
	public String[] getXPMaterialName() {
		return new String[] {"blé", "blés", "de blé"};
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
