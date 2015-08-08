package fr.adrean.Survivor.territory;

import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.gui.AdminTerritoryGUI;
import fr.adrean.Survivor.gui.GUI;
import fr.adrean.Survivor.gui.PlayerTerritoryGUI;
import fr.adrean.Survivor.manager.OfflinePlayerManager;

public abstract class Territory implements Listener {
	
	private String name;
	private String id;
	private Core plugin;
	private OfflinePlayer owner;
	private byte level;
	private byte xp;
	protected short[] secsPerProduct;
	protected short[] secsPerXP;
	public static HashMap<String, Territory> territories = new HashMap<String, Territory>();
	private long lastWaitingXPClear;
	private long lastXPChange;
	private UUID pnjUUID;
	private UUID lvlStandUUID;
	private UUID xpStandUUID;
	private UUID nameStandUUID;
	//String region;
	
	
	protected Territory(Core core, String id) {
		this.setPlugin(core);
		this.id = id;
		reload();
		if (getName() == null || getName().equals("")) {
			setName(id + (int) (Math.random()*1000));
		} else {
			save();
		}
		territories.put(this.getID(), this);
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	
	private void save() {
		getPlugin().getConfig().set("territory." + id + ".name", name);
		getPlugin().getConfig().set("territory." + id + ".owner", owner != null ? owner.getUniqueId().toString() : null);
		getPlugin().getConfig().set("territory." + id + ".level", level);
		getPlugin().getConfig().set("territory." + id + ".xp", xp);
		getPlugin().getConfig().set("territory." + id +".lastwaitingxpclear", lastWaitingXPClear);
		getPlugin().getConfig().set("territory." + id +".lastxpchange", lastXPChange);
		if (pnjUUID != null) {
			getPlugin().getConfig().set("territory." + id + ".pnj", pnjUUID.toString());
		}
		if (lvlStandUUID != null) {
			getPlugin().getConfig().set("territory." + id + ".lvlStand", lvlStandUUID.toString());
		}
		if (xpStandUUID != null) {
			getPlugin().getConfig().set("territory." + id + ".xpStand", xpStandUUID.toString());
		}
		if (nameStandUUID != null) {
			getPlugin().getConfig().set("territory." + id + ".nameStand", nameStandUUID.toString());
		}
		getPlugin().getConfig().set("territory." + id + ".type", this.getClass().getName());
		getPlugin().saveConfig();
	}
	
	private void reload() {
		this.name = getPlugin().getConfig().getString("territory." + id + ".name");
		this.level = (byte) getPlugin().getConfig().getInt("territory." + id + ".level");
		if (this.level < 1) this.level = 1;
		this.xp = (byte) getPlugin().getConfig().getInt("territory." + id + ".xp");
		this.lastWaitingXPClear = getPlugin().getConfig().getLong("territory." + id +".lastwaitingxpclear");
		this.lastXPChange = getPlugin().getConfig().getLong("territory." + id +".lastxpchange");
		if (this.lastWaitingXPClear == 0) this.lastWaitingXPClear = System.currentTimeMillis();
		if (this.lastXPChange == 0) this.lastXPChange = System.currentTimeMillis();
		try {
			this.owner = Bukkit.getOfflinePlayer(UUID.fromString(getPlugin().getConfig().getString("territory." + id + ".owner")));
		} catch (Exception e) {
			this.owner = null;
		}
		String pnjUUID = getPlugin().getConfig().getString("territory." + id +".pnj");
		if (pnjUUID != null) {
			this.pnjUUID = UUID.fromString(pnjUUID);
		}
		String lvlStandUUID = getPlugin().getConfig().getString("territory." + id +".lvlStand");
		if (lvlStandUUID != null) {
			this.lvlStandUUID = UUID.fromString(lvlStandUUID);
		}
		String xpStandUUID = getPlugin().getConfig().getString("territory." + id +".xpStand");
		if (xpStandUUID != null) {
			this.xpStandUUID = UUID.fromString(xpStandUUID);
		}
		String nameStandUUID = getPlugin().getConfig().getString("territory." + id +".nameStand");
		if (nameStandUUID != null) {
			this.nameStandUUID = UUID.fromString(nameStandUUID);
		}
		updateLvlStand();
		updateNameStand();
		new BukkitRunnable() {
			@Override
			public void run() {
				updateXPStand();
				save();
			}
		}.runTaskTimer(plugin, 0, 100);
		this.getPNJ(true).setCustomNameVisible(false);
		this.getPNJ(true).setCustomName("");
	}
	
	private void updateNameStand() {
		if (getPNJ(true) == null) return;
		Location l = this.getPNJ(true).getEyeLocation();
		l.setY(l.getY() - 0.75);
		ArmorStand nameStand  = this.getNameStand();
		if (nameStand == null) nameStand = (ArmorStand) Core.world.spawnEntity(l, EntityType.ARMOR_STAND);
		this.setNameStand(nameStand);
		nameStand.setCustomName(ChatColor.GOLD + this.getName());
		triggerStand(nameStand);
		
	}

	private void updateXPStand() {
		if (getPNJ(true) == null) return;
		Location l = this.getPNJ(true).getEyeLocation();
		l.setY(l.getY() - 0.5);
		ArmorStand xpStand  = this.getXPStand();
		if (xpStand == null) xpStand = (ArmorStand) Core.world.spawnEntity(l, EntityType.ARMOR_STAND);
		this.setXPStand(xpStand);
		xpStand.setCustomName("§aExpérience : §6" + this.getXP());
		triggerStand(xpStand);
		
	}

	private void updateLvlStand() {
		if (getPNJ(true) == null) return;
		Location l = this.getPNJ(true).getEyeLocation();
		l.setY(l.getY() - 0.25);
		ArmorStand lvlStand  = this.getLvlStand();
		if (lvlStand == null) lvlStand = (ArmorStand) Core.world.spawnEntity(l, EntityType.ARMOR_STAND);
		this.setLvlStand(lvlStand);
		lvlStand.setCustomName("§aNiveau : §6" + this.level);
		triggerStand(lvlStand);
	}
	
	private void triggerStand(ArmorStand stand) {
		stand.setCustomNameVisible(true);
		stand.setGravity(false);
		stand.setCanPickupItems(false);
		stand.setMaximumNoDamageTicks(Integer.MAX_VALUE);
		stand.setNoDamageTicks(Integer.MAX_VALUE);
		stand.setVisible(false);
		stand.setSmall(true);
		stand.setRemoveWhenFarAway(false);
	}

	public void delete() {
		getPlugin().getConfig().set("territory." + this.getID(), null);
		getPlugin().saveConfig();
		if (this.getPNJ(true) != null) this.getPNJ(true).remove();
		if (this.getLvlStand() != null) this.getLvlStand().remove();
		if (this.getXPStand() != null) this.getXPStand().remove();
		if (this.getNameStand() != null) this.getNameStand().remove();
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload");
	}
	
	public String getID() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
		ArmorStand stand = this.getNameStand();
		if (stand != null) {
			stand.setCustomName("\u00a76" + name);
		}
		save();
	}
	
	public void setOwner(OfflinePlayer owner) {
		if (this.owner != null) {
			OfflinePlayerManager.addMessage(this.owner, "\u00a7cVotre territoire \u00a76" + this.getName() + "\u00a7c a été envahi !");
			//if (this.getXP() > 0) OfflinePlayerManager.addXP(this.owner, (float) this.getXP());
			if (owner != null) {
				OfflinePlayerManager.addMessage(owner, "\u00a7aVous avez envahi le territoire \u00a76" + this.getName() + "\u00a7a !");
			}
		}
		this.owner = owner;
		save();
	}
	public OfflinePlayer getOwner() {
		return this.owner;
	}
	
	public void setLevel(byte lvl) {
		if (lvl < 1) lvl = 1;
		else if (lvl > 5) lvl = 5;
		this.level = lvl;
		save();
		updateLvlStand();
	}

	public void increaseLevel() {
		if (this.level < this.getMaxLevel()) {
			this.setLevel((byte) (this.getLevel() + 1));
			save();
		}
	}
	
	public void decreaseLevel() {
		if (this.level < 2) return;
		this.setLevel((byte) (this.getLevel() - 1));
		save();
	}
	
	public byte getLevel() {
		return this.level;
	}
	
	public byte getXP() {
		return (byte) getXP(false);
	}

	public long getXP(boolean i) {
		long a = (((System.currentTimeMillis()
				- lastXPChange) / 1000) / 
				(long) secsPerXP[
				                 this.level - 1]);
		if (i) return a;
		long b = xp - a;
		if (!i && b > 49) b = 50;
		if (!i && b < 0) b = 0;
		return b;
	}
	
	public void increaseXP(byte b) {
		if (this.getXP() < this.getMaxXP()) {
			this.xp = (byte) (getXP() + b);
			this.lastXPChange = this.lastXPChange + ((((System.currentTimeMillis() - lastXPChange) / 1000) / (long) secsPerXP[this.level - 1]) * this.secsPerXP[this.level - 1] * 1000);
			save();
			updateXPStand();
		}
	}
	
	public void decreaseXP(byte b) {
		if (this.getXP() > 0) {
			this.xp = (byte) (getXP() - b);
			this.lastXPChange = this.lastXPChange + ((((System.currentTimeMillis() - lastXPChange) / 1000) / (long) secsPerXP[this.level - 1]) * this.secsPerXP[this.level - 1] * 1000);
			save();
			updateXPStand();
		}
	}
	
	public long getWaitingXP(boolean infinite) {
		long a = (((System.currentTimeMillis() - lastWaitingXPClear) / 1000) / (long) secsPerProduct[this.level - 1]);
		if (!infinite && a > getMaxWaitingXp() * 64) {
			return getMaxWaitingXp() * 64;
		}
		return a;
	}
	
	public void resetWaitingXP() {
		this.lastWaitingXPClear = this.lastWaitingXPClear + (this.getWaitingXP(true) * this.secsPerProduct[this.level - 1] * 1000);
		save();
	}

	public int getXPProdDelay() {
		return this.secsPerProduct[this.level - 1];
	}
	public int getXPConsDelay() {
		return this.secsPerXP[this.level - 1];
	}
	public String getXPProdPerHour() {
		float f = 3600F / (float) getXPProdDelay();
		if ((int) f == f) {
			return String.valueOf((int) f);
		} else {
			return String.valueOf(Math.floor(f * 10) / 10);
		}
	}
	public String getXPConsPerHour() {
		float f = 3600F / (float) getXPConsDelay();
		if ((int) f == f) {
			return String.valueOf((int) f);
		} else {
			return String.valueOf(Math.floor(f * 10) / 10);
		}
	}
	

	/*protected static Territory get(Core core, String id) {
		if (!territories.containsKey(id)) {
			territories.put(id, new Territory(core, id));
		}
		return territories.get(id);
	}*/

	public abstract Material getXPMaterial();
	
	public abstract String[] getXPMaterialName();

	public abstract byte getMaxWaitingXp();
	
	public abstract byte getMaxXP();
	
	public abstract byte getMaxLevel();

	/**
	 * @return the plugin
	 */
	public Core getPlugin() {
		return plugin;
	}

	/**
	 * @param plugin the plugin to set
	 */
	private void setPlugin(Core plugin) {
		this.plugin = plugin;
	}

	public byte getSurclaimCost() {
		byte b = (byte) Math.floor(this.getXP() * 1.5F);
		if (b < 10) b = 10;
		if (this.getXP() == 0) {
			b = 1;
		} else if (this.getOwner() == null) {
			b = this.getXP();
		}
		return b;
	}
	
	public void setPNJ(CraftVillager v) {
		if (v == null) {
			this.pnjUUID = null;
		} else {
			this.pnjUUID = v.getUniqueId();
		}
		save();
	}
	
	public Villager getPNJ(boolean deep) {
		if (this.pnjUUID == null) return null;
		Entity e = Core.getEntity(this.pnjUUID, deep);
		if (e instanceof Villager) {
			return (Villager) e;
		} else {
			return null;
		}
				
	}

	@EventHandler
	public void onPlayerRightClickPNJ(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() != null && e.getRightClicked() instanceof CraftVillager && this.getPNJ(false) != null) {
			e.setCancelled(true);
			if (e.getRightClicked().getUniqueId().equals(this.pnjUUID)) {
				GUI i;
				if (e.getPlayer().isOp() && e.getPlayer().isSneaking()) {
					i = new AdminTerritoryGUI(this, e.getPlayer());
				} else {
					i = new PlayerTerritoryGUI(this, e.getPlayer());
				}
				e.getPlayer().openInventory(i);
			}
		}
	}
	
	@EventHandler
	public void onPNJReceiveDamage(EntityDamageEvent e) {
		if (e.getEntity().getUniqueId().equals(this.pnjUUID)) {
			e.setCancelled(true);
		}
	}
	
	public ArmorStand getLvlStand() {
		if (this.lvlStandUUID == null)	return null;
		Entity e = Core.getEntity(lvlStandUUID, true);
		if (e instanceof ArmorStand) return (ArmorStand) e;
		return null;
	}
	
	public void setLvlStand(ArmorStand as) {
		if (as == null) this.lvlStandUUID = null;
		else this.lvlStandUUID = as.getUniqueId();
	}
	
	public ArmorStand getXPStand() {
		if (this.xpStandUUID == null)	return null;
		Entity e = Core.getEntity(xpStandUUID, true);
		if (e instanceof ArmorStand) return (ArmorStand) e;
		return null;
	}
	
	public void setXPStand(ArmorStand as) {
		if (as == null) this.xpStandUUID = null;
		else this.xpStandUUID = as.getUniqueId();
	}
	
	public ArmorStand getNameStand() {
		if (this.nameStandUUID == null)	return null;
		Entity e = Core.getEntity(nameStandUUID, true);
		if (e instanceof ArmorStand) return (ArmorStand) e;
		return null;
	}
	
	public void setNameStand(ArmorStand as) {
		if (as == null) this.nameStandUUID = null;
		else this.nameStandUUID = as.getUniqueId();
	}
}
