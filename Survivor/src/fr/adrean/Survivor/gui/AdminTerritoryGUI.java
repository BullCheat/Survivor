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
import fr.adrean.Survivor.territory.Territory;

public class AdminTerritoryGUI extends TerritoryGUI {

	private AdminTerritoryGUI(InventoryHolder owner, int size, String title, Core plugin, Player p) {
		super(owner, size, title, plugin, p);
	}
	
	public AdminTerritoryGUI(Territory t, Player p) {
		super(null, 9 * 6, t.getName(), t.getPlugin(), p);
		this.territory = t;
		this.player = p;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (disabled) {
					this.cancel();
					return;
				}
				update();
			}
		}.runTaskTimer(territory.getPlugin(), 0, 2);
	}

	@Override
	public void update() {
		for (byte a = 0; a < 6; a++) {
			for (byte b = 0; b < 9; b++) {
				this.set(a, b, getGreyPane());
			}
		}
		this.set((byte) 1, (byte) 4, this.getInfoBook());
		this.set((byte) 2, (byte) 1, this.getIncreaseLevelWool());
		this.set((byte) 2, (byte) 2, this.getEnderEye());
		this.set((byte) 2, (byte) 3, this.getDecreaseLevelWool());
		this.set((byte) 4, (byte) 1, this.getIncreaseXPWool());
		this.set((byte) 4, (byte) 2, this.getNetherStar());
		this.set((byte) 4, (byte) 3, this.getDecreaseXPWool());
		this.set((byte) 2, (byte) 5, this.getNametag());
		this.set((byte) 2, (byte) 6, this.getFreeTerrainButton());
		this.set((byte) 2, (byte) 7, this.getSurclaimButton());
		this.set((byte) 4, (byte) 6, this.getDeleteTerrainButton());
	}
	

	@EventHandler
	public void onPlayerClick(InventoryClickEvent e) {
		if ((e.getClickedInventory() == null || e.getClickedInventory().hashCode() != this.hashCode()) && (e.getInventory() == null || e.getInventory().hashCode() != this.hashCode())) return;
		if (e.getSlot() == getSlot(2, 1)) {
			territory.increaseLevel();
			update();
		} else if (e.getSlot() == getSlot(2, 3)) {
			territory.decreaseLevel();
			update();
		} else if (e.getSlot() == getSlot(4, 1)) {
			handleIncreaseXP(e.isLeftClick(), e.isShiftClick());
		} else if (e.getSlot() == getSlot(4, 3)) {
			handleDecreaseXP(e.isLeftClick(), e.isShiftClick());
		} else if (e.getSlot() == getSlot(2, 5)) {
			handleNameTagClick();
		} else if (e.getSlot() == getSlot(2, 6)) {
			territory.setOwner(null);
		} else if (e.getSlot() == getSlot(2, 7)) {
			territory.setOwner(player);
		} else if (e.getSlot() == getSlot(4, 6)) {
			handleDeleteClick();
		}
	}


	private void handleDeleteClick() {
		
		player.closeInventory();
		player.sendMessage("/svterritory remove " + territory.getID());
		
	}

	private void handleNameTagClick() {
		Core.rename.put(player.getName(), territory.getID());
		player.sendMessage("\u00a7aEntrez le nouveau nom pour ce terrain :");
		player.closeInventory();
	}

	private void handleIncreaseXP(boolean leftClick, boolean shiftClick) {
		if (leftClick) {
			territory.increaseXP((byte) 1);
		} else if (shiftClick) {
			territory.increaseXP((byte) 10);
		} else {
			territory.increaseXP((byte) 5);
		}
		update();
		
	}


	private void handleDecreaseXP(boolean leftClick, boolean shiftClick) {
		if (leftClick) {
			territory.decreaseXP((byte) 1);
		} else if (shiftClick) {
			territory.decreaseXP((byte) 10);
		} else {
			territory.decreaseXP((byte) 5);
		}
		update();
		
	}

	private ItemStack getDeleteTerrainButton() {
		ItemStack barrier = new ItemStack(Material.BARRIER);
		ItemMeta meta = barrier.getItemMeta();
		meta.setDisplayName("\u00a74Supprimer le terrain");
		barrier.setItemMeta(meta);
		return barrier;
	}

	private ItemStack getEnderEye() {
		ItemStack eye = new ItemStack(Material.EYE_OF_ENDER, territory.getLevel());
		ItemMeta meta = eye.getItemMeta();
		meta.setDisplayName("\u00a76Niveau " + territory.getLevel());
		eye.setItemMeta(meta);
		return eye;
	}

	private ItemStack getIncreaseLevelWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 5);
		String color = "\u00a7a";
		if (territory.getLevel() >= territory.getMaxLevel()) {
			wool.setDurability((short) 8);
			color = "\u00a77";
		}
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName(color + "Augmenter d'un niveau");
		wool.setItemMeta(meta);
		return wool;
	}
	
	private ItemStack getDecreaseLevelWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 14);
		String color = "\u00a7c";
		if (territory.getLevel() <= 1) {
			wool.setDurability((short) 8);
			color = "\u00a77";
		}
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName(color + "Diminuer d'un niveau");
		wool.setItemMeta(meta);
		return wool;		
	}

	private ItemStack getIncreaseXPWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 5);
		String color = "\u00a7a";
		ArrayList<String> lore = new ArrayList<String>();
		if (territory.getXP() >= territory.getMaxXP()) {
			wool.setDurability((short) 8);
			color = "\u00a77";
		} else {
			lore.add("\u00a76Clic gauche \u00a77pour ajouter \u00a761 \u00a77niveau d'expérience");
			lore.add("\u00a76Clic droit \u00a77pour ajouter \u00a765 \u00a77niveaux d'expérience");
			lore.add("\u00a76Clic droit + sneak \u00a77pour ajouter \u00a7610 \u00a77niveaux d'expérience");
		}
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName(color + "Ajouter de l'expérience");
		meta.setLore(lore);
		wool.setItemMeta(meta);
		return wool;
	}
	
	private ItemStack getDecreaseXPWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 14);
		String color = "\u00a7c";
		ArrayList<String> lore = new ArrayList<String>();
		if (territory.getXP() <= 0) {
			wool.setDurability((short) 8);
			color = "\u00a77";
		} else {
			lore.add("\u00a76Clic gauche \u00a77pour retirer \u00a761 \u00a77niveau d'expérience");
			lore.add("\u00a76Clic droit \u00a77pour retirer \u00a765 \u00a77niveaux d'expérience");
			lore.add("\u00a76Clic droit + sneak \u00a77pour retirer \u00a7610 \u00a77niveaux d'expérience");
		}
		ItemMeta meta = wool.getItemMeta();
		meta.setDisplayName(color + "Retirer de l'expérience");
		meta.setLore(lore);
		wool.setItemMeta(meta);
		return wool;		
	}
	
	private ItemStack getNametag() {
		ItemStack nt = new ItemStack(Material.NAME_TAG);
		ItemMeta meta = nt.getItemMeta();
		meta.setDisplayName("\u00a76Renommer le terrain");
		nt.setItemMeta(meta);		
		return nt;
	}
	
	private ItemStack getFreeTerrainButton() {
		ItemStack dye = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = dye.getItemMeta();
		if (territory.getOwner() == null) {
			dye.setDurability((short) 10);
			meta.setDisplayName("\u00a7aLe terrain est libre");
		} else {
			meta.setDisplayName("\u00a7aLibérer le terrain");
		}
		dye.setItemMeta(meta);		
		return dye;
	}
	
	private ItemStack getSurclaimButton() {
		ItemStack dye = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = dye.getItemMeta();
		if (player.equals(territory.getOwner())) {
			dye.setDurability((short) 13);
			meta.setDisplayName("\u00a7cCe terrain vous appartient");
		} else {
			meta.setDisplayName("\u00a7cSurclaim le terrain");
		}
		dye.setItemMeta(meta);		
		return dye;
	}

}
