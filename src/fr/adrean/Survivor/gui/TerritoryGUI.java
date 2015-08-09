package fr.adrean.Survivor.gui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.territory.Territory;

public abstract class TerritoryGUI extends GUI {


	protected Territory territory;
	protected Player player;
	protected long lastno;
	protected long lastyes;
	
	protected TerritoryGUI(InventoryHolder owner, int size, String title, Core plugin, Player p) {
		super(owner, size, title, plugin, p);
		
	}
	
	protected ItemStack getInfoBook() {
		ItemStack book = new ItemStack(Material.BOOK);
		ItemMeta meta = book.getItemMeta();
		meta.setDisplayName("\u00a76" + territory.getName());
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("\u00a77Niveau : \u00a7a" + territory.getLevel());
		if (territory.getOwner() == null) {
			lore.add("\u00a77Cet emplacement est libre !");
		} else {
			if (territory.getOwner().getUniqueId().equals(this.player.getUniqueId())) {
				lore.add("\u00a77Cet emplacement vous appartient");
			} else {
				lore.add("\u00a77Propriétaire : \u00a7a" + territory.getOwner().getName());
			}
		}
		lore.add("\u00a77Production " + territory.getXPMaterialName()[2] + " par heure : \u00a7a" + territory.getXPProdPerHour());
		meta.setLore(lore);
		book.setItemMeta(meta);
		return book;
	}
	
	protected ItemStack getNetherStar() {
		ItemStack star = new ItemStack(Material.NETHER_STAR);
		star.setAmount((int) territory.getXP());
		ItemMeta meta = star.getItemMeta();
		meta.setDisplayName("\u00a76" + territory.getXP() + " niveaux d'expérience");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("\u00a77Niveau d'expérience maximum : \u00a7a" + territory.getMaxXP());
		lore.add("\u00a77Consommation d'XP par heure : \u00a7a" + territory.getXPConsPerHour());
		meta.setLore(lore);
		star.setItemMeta(meta);
		return star;
	}

	public void yes() {
		if (System.currentTimeMillis() - lastyes > 1000) {
			player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
			lastyes = System.currentTimeMillis();
		}
	}
	public void no() {
		if (System.currentTimeMillis() - lastno > 1000) {
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
			lastno = System.currentTimeMillis();
		}
	}

}
