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

import fr.adrean.Survivor.manager.ResourceManager;
import fr.adrean.Survivor.territory.Ferme;
import fr.adrean.Survivor.territory.Mine;
import fr.adrean.Survivor.territory.Moulin;
import fr.adrean.Survivor.territory.Territory;

public class PlayerTerritoryGUI extends TerritoryGUI {

	private PlayerTerritoryGUI(InventoryHolder owner, int size, String title, Player p) {
		super(owner, size, title, null, p);
	}
	
	public PlayerTerritoryGUI(Territory t, Player p) {
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
		}.runTaskTimer(territory.getPlugin(), 0, 10);
	}
	

	@Override
	public void update() {
		this.set((byte) 1, (byte) 4, getNetherStar());
		this.set((byte) 2, (byte) 2, getGreenWool());
		//this.set((byte) 2, (byte) 6, getRedWool());
		this.set((byte) 2, (byte) 6, getInfoBook());
		this.set((byte) 3, (byte) 8, getAdminButton());
		for (byte x = 4; x < 6; x++) {
			for (byte y = 0; y < 9; y++) {
				this.set(x, y, getGreyPane());
			}
		}
		long total = territory.getWaitingXP(false);
		if (total > 64*9) throw new IllegalArgumentException(total + " > 64*9");
		ItemStack is = new ItemStack(territory.getXPMaterial());
		ItemMeta meta = is.getItemMeta();
		long x = territory.getWaitingXP(false);
		meta.setDisplayName("\u00a7c" + x + " \u00a7a" + (x > 1 ? territory.getXPMaterialName()[1] : territory.getXPMaterialName()[0]) +" en attente");
		ArrayList<String> lore = new ArrayList<String>();
		if (territory.getOwner() != null && territory.getOwner().equals(player)) {
			lore.add("\u00a77Cliquez pour récupérer");	
		} else {
			lore.add("\u00a77Capturez ce territoire pour récupérer");
		}
		meta.setLore(lore);
		is.setItemMeta(meta);
		byte a = 4, b = 4;
		b += (byte) (this.territory.getMaxWaitingXp() / 2);
		a -= (byte) (this.territory.getMaxWaitingXp() / 2);
		for (this.hashCode(); a <= b; a++) {
			if (total > 0) {
			is.setAmount((int) (total > 64 ? 64 : total));
				this.set((byte) 5, a, is.clone());
			} else {
				this.set((byte) 5, a, null);
			}
			total -= 64;
		}
	}


	/*private ItemStack getOptionsButton() {
		if (territory.getOwner() == null || !territory.getOwner().equals(player)) return null;
		else {
			ItemStack button = new ItemStack(Material.DIODE);
			ItemMeta meta = button.getItemMeta();
			meta.setDisplayName("�6Options");
			button.setItemMeta(meta);
			return button;
		}
	}*/

	private ItemStack getAdminButton() {
		if (!player.isOp()) return null;
		ItemStack diode = new ItemStack(Material.DIODE);
		ItemMeta meta = diode.getItemMeta();
		meta.setDisplayName("\u00a74Administration");
		diode.setItemMeta(meta);
		return diode;
		
	}
	
	
	private ItemStack getGreenWool() {
		ItemStack wool = new ItemStack(Material.WOOL, 1, (short) 8);
		ItemMeta meta = wool.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		if (territory.getOwner() == null) {
			meta.setDisplayName("\u00a7aObtenir ce territoire");
			lore.add("\u00a76Clic droit + sneak \u00a77pour obtenir ce territoire");
			lore.add("\u00a77Coût : \u00a7c" + territory.getSurclaimCost() + " \u00a77niveau" + (territory.getSurclaimCost() > 1 ? "x" : "") + " d'expérience");
		} else if (territory.getOwner().equals(player)) {
			wool.setDurability((short) 5);
			meta.setDisplayName("\u00a7aAjouter de l'expérience");
			lore.add("\u00a76Clic gauche \u00a77pour ajouter \u00a761 \u00a77niveau d'expérience");
			lore.add("\u00a76Clic droit \u00a77pour ajouter \u00a765 \u00a77niveaux d'expérience");
			lore.add("\u00a76Clic droit + sneak \u00a77pour ajouter \u00a7610 \u00a77niveaux d'expérience");
		} else {
			meta.setDisplayName("\u00a7aEnvahir ce territoire");
			lore.add("\u00a76Clic droit + sneak \u00a77pour \u00a7cenvahir\u00a77 ce territoire");
			lore.add("\u00a77Coût : \u00a7c" + territory.getSurclaimCost() + "\u00a77 niveaux d'expérience");
		}
		meta.setLore(lore);
		wool.setItemMeta(meta);
		return wool;
	}
	/*
	@SuppressWarnings("unused")
	private ItemStack getRedWool() {
		ItemStack wool;
		if (territory.getOwner() == null || !territory.getOwner().equals(player)) {
			wool = new ItemStack(Material.WOOL, 1, (short) 8);
			ItemMeta meta = wool.getItemMeta();
			meta.setDisplayName("\u00a77Ce territoire ne vous appartient pas");
			wool.setItemMeta(meta);
		} else {
			wool = new ItemStack(Material.WOOL, 1, (short) 14);
			ArrayList<String> lore = new ArrayList<String>();
			ItemMeta meta = wool.getItemMeta();
			meta.setDisplayName("\u00a7cRetirer de l'exp�rience");
			lore.add("�6Clic gauche �7pour retirer �61 �7niveau d'exp�rience");
			lore.add("�6Clic droit �7pour retirer �65 �7niveaux d'exp�rience");
			lore.add("�6Clic droit + sneak �7pour retirer �610 �7niveaux d'exp�rience");
			
			meta.setLore(lore);
			wool.setItemMeta(meta);
		}
		return wool;
	}*/
	
	@EventHandler
	public void onPlayerClick(InventoryClickEvent e) {
		if ((e.getClickedInventory() == null || e.getClickedInventory().hashCode() != this.hashCode()) && (e.getInventory() == null || e.getInventory().hashCode() != this.hashCode())) return;
		if (e.getSlot() == getSlot(2, 2)) {
			handleGreenWoolClick(e.isLeftClick(), e.isShiftClick());
		} /*else if (e.getSlot() == getSlot(2, 6)) {
			handleRedWoolClick(e.isLeftClick(), e.isShiftClick());
		} */else if (e.getSlot() >= getSlot(5, 0) && e.getSlot() <= getSlot(5, 8)) {
			if (e.getCurrentItem() != null && e.getCurrentItem().getType() == territory.getXPMaterial()) {
				handleXPClick();
			}
		} else if (e.getSlot() == getSlot(3, 8)) {
			if (player.isOp()) player.closeInventory();
			if (player.isOp()) player.openInventory(new AdminTerritoryGUI(territory, player));
		}
	}

	private void handleXPClick() {
		if (player.equals(territory.getOwner())) {
			int res = (int) this.territory.getWaitingXP(false);
			if (res > 0) {
				yes();
				if (this.territory instanceof Mine) {
					ResourceManager.addGold(player, res);
				} else if (this.territory instanceof Ferme) {
					ResourceManager.addLeather(player, res);
				} else if (this.territory instanceof Moulin) {
					ResourceManager.addWheat(player, res);
				}
				territory.resetWaitingXP();
				this.update();
			}
		} else {
			no();
			player.sendMessage("§cVous devez capturer ce territoire afin d'obtenir les productions associées.");
		}
		
	}

	private void handleGreenWoolClick(boolean leftClick, boolean shiftClick) {
		if (territory.getOwner() != null && territory.getOwner().equals(player)) {
			byte cost = (byte) (leftClick ? 1 : !shiftClick ? 5 : 10);
			if (player.getLevel() < 1) {
				no();
				player.sendMessage("\u00a76Vous n'avez pas assez d'expérience !");
			} else if (territory.getXP() >= territory.getMaxXP()) {
				no();
				player.sendMessage("\u00a7cVous ne pouvez pas placer plus d'XP sur ce territoire !");
			} else {
				yes();
				cost = (byte) (cost > player.getLevel() ? player.getLevel() : cost);
				if (territory.getXP() + cost >= territory.getMaxXP()) {
					cost = (byte) (territory.getMaxXP() - territory.getXP());
				}
				territory.increaseXP(cost);
				player.setLevel(player.getLevel() - cost);
				player.sendMessage("\u00a7aVous avez bien ajouté \u00a76" + cost + "\u00a7a niveau" + (cost > 1 ? "x" : "") + " d'expérience sur ce territoire.");
			}
		} else if (shiftClick && !leftClick) {
			byte cost = territory.getSurclaimCost();
			if (player.getLevel() < cost) {
				no();
				player.sendMessage("\u00a76Vous avez besoin de \u00a7c" + (cost - player.getLevel()) + "\u00a76 niveau" + (cost - player.getLevel() > 1 ? "x" : "") + " supplémentaires !");
			} else {
				yes();
				player.sendMessage("\u00a7aVous avez bien acheté le territoire \u00a76" + this.territory.getName() + "\u00a7a pour \u00a76" + cost + "\u00a7a niveaux d'expérience.");
				player.setLevel(player.getLevel() - cost);
				territory.setOwner(player);
			}
			
		} else {
			no();
			if (territory.getOwner() == null) {
				player.sendMessage("\u00a76Clic droit + sneak \u00a77pour obtenir ce territoire");
			} else {
				player.sendMessage("\u00a76Clic droit + sneak \u00a77pour \u00a7cenvahir\u00a77 ce territoire");
			}
		}
		this.update();
	}
	
	/*@SuppressWarnings("unused")
	private void handleRedWoolClick(boolean leftClick, boolean shiftClick) 
	{
		if (territory.getOwner() != null && territory.getOwner().equals(player)) {
			byte cost = (byte) (leftClick ? 1 : !shiftClick ? 5 : 10);
			if (territory.getXP(false) < 1) {
				player.sendMessage("�cImpossible de retirer d'avantage d'exp�rience.");
			} else {
				cost = (byte) (cost > territory.getXP(false) ? territory.getXP(false) : cost);
				territory.decreaseXP(cost);
				player.setLevel(player.getLevel() + cost);
				player.sendMessage("�aVous avez bien retir� �6" + cost + "�a niveau" + (cost > 1 ? "x" : "") + " d'exp�rience de cet emplacement.");
			}
		}
		this.update();
	}*/
	
 
}
