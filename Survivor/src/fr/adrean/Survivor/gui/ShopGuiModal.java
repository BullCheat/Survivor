package fr.adrean.Survivor.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.ShopType;
import fr.adrean.Survivor.manager.ResourceManager;

public class ShopGuiModal extends GUI {

	ShopType type;
	Player player;
	Core plugin;
	ItemStack item;
	
	private ShopGuiModal(InventoryHolder owner, int size, String title, Core plugin, Player p) {
		super(owner, size, title, plugin, p);
		this.plugin = plugin;
	}
	public ShopGuiModal(ShopType t, Player p, Core core, ItemStack item) {
		super(null, 9 * 6, t.toString(), core, p);
		this.type = t;
		this.player = p;
		this.plugin = core;
		this.item = item;
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
		for (byte a = 1; a < 4; a++) {
			for (byte b = 1; b < 4; b++) {
				this.set(a, b, null);
			}
		}
		this.set((byte) 2, (byte) 2, item);
		this.set((byte) 1, (byte) 5, getGold());
		this.set((byte) 1, (byte) 6, getLeather());
		this.set((byte) 1, (byte) 7, getNetherStar());
		this.set((byte) 3, (byte) 7, getGreenWool());
		this.set((byte) 3, (byte) 5, getRedWool());

	}
	

	private ItemStack getRedWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 14);
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName("\u00a7cAnnuler l'achat");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("\u00a77Annuler et revenir au menu précédent");
		meta.setLore(lore);
		wool.setItemMeta(meta);
		return wool;
	}
	
	private ItemStack getGreenWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 5);
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName("\u00a7aValider l'achat");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("\u00a77Acheter cet item");
		meta.setLore(lore);
		wool.setItemMeta(meta);
		return wool;
	}
	private ItemStack getGold() {
		int g = ResourceManager.getGold(player);
		ItemStack gold = new ItemStack(Material.GOLD_INGOT, Math.min(1, g));
		ItemMeta meta =	gold.getItemMeta();
		meta.setDisplayName("\u00a7c" + (g == 0 ? "\u00a7aPas de" : g) + " \u00a7alingot" + (g > 1 ? "s" : "") +" d'or");
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

}
