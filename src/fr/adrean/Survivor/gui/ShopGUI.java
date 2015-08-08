package fr.adrean.Survivor.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.ShopType;
import fr.adrean.Survivor.manager.ResourceManager;

public class ShopGUI extends GUI {

	ShopType type;
	Player player;
	Core plugin;
	
	private ShopGUI(InventoryHolder owner, int size, String title, Core plugin, Player p) {
		super(owner, size, title, plugin, p);
		this.plugin = plugin;
	}
	
	public ShopGUI(ShopType t, Player p, Core core) {
		super(null, 9 * 6, t.toString(), core, p);
		this.type = t;
		this.player = p;
		this.plugin = core;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (disabled) {
					this.cancel();
					return;
				}
				update();
			}
		}.runTaskTimer(core, 0, 20);
	}

	@Override
	public void update() {
		for (byte a = 0; a < 6; a++) {
			for (byte b = 0; b < 9; b++) {
				this.set(a, b, getGreyPane());
			}
		}
		for (byte a = 3; a < 5; a++) {
			for (byte b = 0; b < 9; b++) {
				this.set(a, b, null);
			}
		}

		this.set((byte) 1, (byte) 1, getNetherStar());
		this.set((byte) 1, (byte) 3, getWheat());
		this.set((byte) 1, (byte) 5, getLeather());
		this.set((byte) 1, (byte) 7, getGold());
		this.set((byte) 3, (byte) 0, getItem());
	}

	private ItemStack getItem() {
		ItemStack sword = new ItemStack(Material.WOOD_SWORD);
		ItemMeta meta = sword.getItemMeta();
		meta.setDisplayName("\u00a74Épée en bois \u00a77(\u00a761 or\u00a77)");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("\u00a77Attaque : \u00a761,5");
		meta.setLore(lore);
		sword.setItemMeta(meta);
		return Core.removeAttributes(sword);
	}

	private ItemStack getGold() {
		int g = ResourceManager.getGold(player);
		ItemStack gold = new ItemStack(Material.GOLD_INGOT, Math.min(1, g));
		ItemMeta meta =	gold.getItemMeta();
		meta.setDisplayName("\u00a7c" + (g == 0 ? "Pas de" : g) + " \u00a7alingot" + (g > 1 ? "s" : "") +" d'or");
		gold.setItemMeta(meta);
		return gold;
	}

	private ItemStack getLeather() {
		int l = ResourceManager.getLeather(player);
		ItemStack leather = new ItemStack(Material.LEATHER, Math.min(1, l));
		ItemMeta meta =	leather.getItemMeta();
		meta.setDisplayName("\u00a7c" + (l == 0 ? "\u00a7aPas de" : l) + " \u00a7acuir" + (l > 1 ? "s" : ""));
		leather.setItemMeta(meta);
		return leather;
	}

	private ItemStack getNetherStar() {
		int x = player.getLevel();
		ItemStack star = new ItemStack(Material.NETHER_STAR, Math.min(1, x));
		ItemMeta meta =	star.getItemMeta();
		meta.setDisplayName("\u00a7a" + (x == 0 ? "\u00a76Pas de" : x) + " \u00a76niveau" + (player.getLevel() != 1 ? "x" : "") + " d'expérience");
		star.setItemMeta(meta);
		return star;
	}

	private ItemStack getWheat() {
		int l = ResourceManager.getWheat(player);
		ItemStack wheat = new ItemStack(Material.WHEAT, Math.min(1, l));
		ItemMeta meta =	wheat.getItemMeta();
		meta.setDisplayName("\u00a7c" + (l == 0 ? "\u00a7aPas de" : l) + " \u00a7ablé" + (l > 1 ? "s" : ""));
		wheat.setItemMeta(meta);
		return wheat;
	}


	@EventHandler
	public void onPlayerClick(InventoryClickEvent e) {
		if ((e.getClickedInventory() == null || e.getClickedInventory().hashCode() != this.hashCode()) && (e.getInventory() == null || e.getInventory().hashCode() != this.hashCode())) return;
		if (e.getSlot() >= getSlot(3, 0) && e.getSlot() <= getSlot(4, 8)) {
			if (e.getCurrentItem() != null) {
				if (e.getInventory().getItem(e.getSlot()) != null) {
					e.getWhoClicked().closeInventory();
					ItemStack is = e.getCurrentItem().clone();
					e.getWhoClicked().openInventory(new ShopGuiModal(type, player, plugin, is));
				}
			}
		}
	}

}
