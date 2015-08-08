package fr.adrean.Survivor.gui;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.adrean.Survivor.Core;

public abstract class GUI extends CraftInventoryCustom implements Listener {

	static ArrayList<Integer> guis = new ArrayList<Integer>();
	boolean disabled = false;
	
	public GUI(InventoryHolder owner, int size, String title, Core plugin, final Player p) {
		super(owner, size, title);
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public abstract void update();
	
	public void set(byte x, byte y, ItemStack i) 
	{
		this.setItem(getSlot(x, y), i);
	}
	
	public byte getSlot(byte x, byte y) {
		return (byte) (x * 9 + y);
	}
	
	public void disable() {
		this.disabled = true;	
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerClickInventory(InventoryClickEvent e) {
		if (this.disabled) return;
		if ((e.getClickedInventory() != null && e.getClickedInventory().hashCode() == this.hashCode()) || (e.getInventory() != null && e.getInventory().hashCode() == this.hashCode())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerCloseGUI(InventoryCloseEvent e) {
		if (e.getInventory().hashCode() == this.hashCode()) {
			this.disable();
		}
	}

	public byte getSlot(int x, int y) {
		return this.getSlot((byte) x, (byte) y);
	}
	
	public ItemStack getGreyPane() {
		ItemStack pane =  new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName("");
		pane.setItemMeta(meta);
		return pane;
	}


}
