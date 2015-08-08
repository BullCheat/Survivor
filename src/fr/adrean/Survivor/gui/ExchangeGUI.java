package fr.adrean.Survivor.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.Exchange;
import fr.adrean.Survivor.ResourceType;

public class ExchangeGUI extends GUI {
	
	private Player otherPlayer;
	private Player p;
	private Exchange e;
	//private Core plugin;

	public ExchangeGUI(Player player, Exchange exchange, Core plugin, Player oplayer) {
		super(player, 9*6, "Échange - " + oplayer.getName(), plugin, player);
		this.p = player;
		this.e = exchange;
		//this.plugin = plugin;
		this.otherPlayer = oplayer;
		update();
	}

	@Override
	public void update() {
		//Background
		for (byte a = 0; a < 6; a++) {
			for (byte b = 0; b < 9; b++) {
				this.set(a, b, getGreyPane());
			}
		}
		for (int a = 0; a < 2; a++ ) {
			byte dec;
			if (a == 0) dec = 1; else dec = 5;
			//ItemStacks
			for (byte i = 0; i < 3; i++) {
				for (byte j = 0; j < 3; j++) {
					this.set((byte) (i + 1), (byte) (j + dec), getIStack((byte) (i*3 + j), a == 0));
				}
			}
			//Nether star
			this.set((byte) 2, (byte) (a*8), get(ResourceType.XP, a == 0));
			//Or Cuir Blé
			for (int i = 0; i < 3; i++) {
				this.set((byte) 5, (byte) (dec + i), get(i == 0 ? ResourceType.GOLD : i == 1 ? ResourceType.LEATHER : ResourceType.WHEAT, a == 0));
			}
			//Boutons accepter/refuser
			for (int i = 0; i < 2; i++) {
				this.set((byte) (a*2 + i), (byte) 4, getButton(a == 0));
			}
		}
	}

	private ItemStack getButton(boolean b) {
		ItemStack pane = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)(b ? 13 : 14));
		ItemMeta meta = pane.getItemMeta();
		meta.setDisplayName(b ? "§aAccepter la transaction" : "§cRefuser la transaction");
		pane.setItemMeta(meta);
		return pane;
	}

	private ItemStack get(ResourceType type, boolean b) {
		int num = e.get(type, b ? p : otherPlayer);
		ItemStack stack = new ItemStack(type.getMaterial(), Integer.min(num, 64));
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + num + " " + type.getDisplayName(num > 1));
		stack.setItemMeta(meta);
		return stack;
	}

	private ItemStack getIStack(byte i, boolean b) {
		return e.getItemStack(i, b ? p : otherPlayer);
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public ExchangeGUI getOtherGUI() {
		return e.getGUI(otherPlayer);
	}
	
	@Override
	@EventHandler
	public void onPlayerCloseGUI(InventoryCloseEvent e) {
		if (this.disabled) return;
		if (e.getInventory().hashCode() == this.hashCode()) {
			this.e.abort();
		}
	}
	
	@EventHandler
	public void onPlayerCloseIt(InventoryCloseEvent e) {
		
	}
}
