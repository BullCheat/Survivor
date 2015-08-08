package fr.adrean.Survivor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.adrean.Survivor.gui.ExchangeGUI;

public class Exchange {

	Player[] players;
	ItemStack[] [] items;
	int[] [] resources;
	Core plugin;
	ExchangeGUI[] guis;
	
	public Exchange(Player clicker, Player clicked, Core plugin) {
		guis = new ExchangeGUI[2];
		players = new Player[] { clicker, clicked };
		items = new ItemStack[players.length] [9];
		for (int i = 0; i < items.length; i++) {
			ItemStack[] is = items[i];
			for (int j = 0; j < is.length; j++) {
				items[i][j] = null;
			}
		}
		resources = new int[players.length] [ResourceType.values().length];
		for (int i = 0; i < resources.length; i++) {
			int[] is = resources[i];
			for (int j = 0; j < is.length; j++) {
				resources[i][j] = 0;
			}
		}
		guis[getPlayerID(clicker)] = new ExchangeGUI(clicker, this, plugin, clicked);
		guis[getPlayerID(clicked)] = new ExchangeGUI(clicked, this, plugin, clicker);
		
	}
	
	public void add(ResourceType type, int i, Player p) {
		this.resources[getPlayerID(p)] [type.getID()] += i;
	}
	
	public void remove(ResourceType type, int i, Player p) {
		byte pid = getPlayerID(p);
		this.resources[pid] [type.getID()] -= i;
		if (this.resources[pid] [type.getID()] < 0)
			this.resources[pid] [type.getID()] = 0;
	}
	
	public int get(ResourceType type, Player p) {
		return this.resources[getPlayerID(p)][type.getID()];
	}
	
	public ItemStack getItemStack(byte b, Player p) {
		byte id = getPlayerID(p);
		if (b >= items[id].length) {
			throw new IndexOutOfBoundsException("You can't get an ItemStack which is more than " + items.length + "! (max index" + (items.length - 1) + "), requested " + b);
		}
		return items[id] [b];
	}
	
	public void setItemStack(ItemStack s, byte b, Player p) {
		if (b >= items.length) {
			throw new IndexOutOfBoundsException("You can't set an ItemStack which is more than " + items.length + "! (max index" + (items.length - 1) + "), requested " + b);
		}
		items[getPlayerID(p)] [b] = s;
	}
	
	private byte getPlayerID(Player p) {
		for (byte i = 0; i < players.length; i++) {
			if (players[i].equals(p)) {
				return i;
			}
		}
		throw new IllegalArgumentException("Trying to non-participating player " + p.getName());
	}

	public void openGUIs() {
		for (ExchangeGUI gui : this.guis) {
			gui.getPlayer().openInventory(gui);
		}
	}
	
	public ExchangeGUI getGUI(Player p) {
		return guis[getPlayerID(p)];
	}

}
