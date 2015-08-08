package fr.adrean.Survivor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import fr.adrean.Survivor.ChestSpawner.ChestType;
import fr.adrean.Survivor.manager.ActionBarManager;
import fr.adrean.Survivor.manager.CheatManager;
import fr.adrean.Survivor.manager.OfflinePlayerManager;
import fr.adrean.Survivor.manager.PlayerManager;
import fr.adrean.Survivor.manager.ResourceManager;
import fr.adrean.Survivor.manager.ScoreboardManager;
import fr.adrean.Survivor.territory.Ferme;
import fr.adrean.Survivor.territory.Mine;
import fr.adrean.Survivor.territory.Moulin;

@SuppressWarnings("deprecation")
public class Core extends JavaPlugin {
	public boolean a = true;
	public static String version = "0.0.1";
	public static ArrayList<ChestSpawner> chestSpawners = new ArrayList<ChestSpawner>();
	public static ArrayList<ItemStack> chestItems = new ArrayList<ItemStack>();
	public static ArrayList<ItemStack> magicChestItems = new ArrayList<ItemStack>();
	public static ItemStack[] randomChestItems = new ItemStack[0];
	public static ItemStack[] randomMagicChestItems = new ItemStack[0];
	public static EntityType[] randomMobs = new EntityType[0];
	public static ActionBarManager manager = null;
	public static HashMap<String, Location> playerTPs = new HashMap<String, Location>();
	public static ArrayList<String> auraSlotMessageCooldown = new ArrayList<String>();
	public static HashMap<String, String> rename = new HashMap<String, String>();
	public static World world;
	public static HashMap<String, ItemStack> customItems = new HashMap<String, ItemStack>();
	public static Core plugin;
	public static HashMap<UUID, Long> playerSounds = new HashMap<UUID, Long>();
	public static Click[] cheatSequence = new Click[] { Click.LEFT, Click.RIGHT, Click.LEFT, Click.RIGHT, Click.LEFT, Click.LEFT, Click.LEFT, Click.RIGHT };
	public static HashMap<UUID, Long> playerLastClick = new HashMap<UUID, Long>();
	public static HashMap<UUID, ArrayList<Click>> playerClicks = new HashMap<UUID, ArrayList<Click>>();
	public static ArrayList<TradeRequest> playerTradeRequests = new ArrayList<TradeRequest>();
	
	@Override
	public void onEnable() {
		plugin = this;
		world = Bukkit.getWorld("world");
		ScoreboardManager.core = this;
		ResourceManager.core = this;
		OfflinePlayerManager.core = this;
		CommandDispatcher cd = new CommandDispatcher(this);
		getCommand("survivor").setExecutor(cd);
		getCommand("randomtp").setExecutor(cd);
		getCommand("speed").setExecutor(cd);
		getCommand("set").setExecutor(cd);
		getCommand("svterritory").setExecutor(cd);
		getCommand("svshop").setExecutor(cd);
		initCustomItems();
		initRandoms();
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerManager(this), this);
		pm.registerEvents(new CheatManager(), this);
		if (getConfig().get("chestspawners") == null) {
			getConfig().set("chestspawners", "");
		}
		saveConfig();
		reloadChestSpawners();
		initAuraChecker();
		manager = new ActionBarManager(this);
		replaceChests();
		initInventoryChecker();
		ScoreboardManager.update(Bukkit.getOnlinePlayers());
		initTerritories();
		//initGPS();
		initVillagerSoundStopper();
	}



	private void initVillagerSoundStopper() {
		ProtocolLibrary.getProtocolManager().addPacketListener(
		          new PacketAdapter(this, ConnectionSide.SERVER_SIDE, Packets.Server.NAMED_SOUND_EFFECT) {
		            @Override
		            public void onPacketSending(PacketEvent event) {
		                String soundName = event.getPacket().getStrings().read(0);
		                if ("mob.villager.idle".equals(soundName)) {
		                    event.setCancelled(true);
		                }
		            }
		        });
		
	}



	private void initCustomItems() {
		try {
			ConfigurationSection cs = getConfig().getConfigurationSection("customitems");
			if (cs == null) return;
			for (String key : cs.getKeys(false)) {
				ConfigurationSection c = cs.getConfigurationSection(key);
				Material m = Material.getMaterial(c.getString("type"));
				if (!c.contains("type")) {
					getLogger().log(Level.WARNING, "CustomItem " + key + " has no type ?!");
					continue;
				} else if (Material.getMaterial(c.getString("type")) == null || Material.getMaterial(c.getString("type")) == Material.AIR) {
					getLogger().log(Level.WARNING, "CustomItem " + key + " has an invalid type ("+ c.getString("key") +") ?!");
					continue;
				} else {
					ItemStack is = new ItemStack(m);
					if (c.contains("meta")) {
						is.setDurability((short) c.getInt("meta"));
					}
					ItemMeta im = is.getItemMeta();
					if (c.contains("name")) {
						im.setDisplayName(c.getString("name"));
					}
					if (c.contains("lore")) {
						im.setLore(c.getStringList("lore"));
					}
					is.setItemMeta(im);
					if (c.contains("enchants")) {
						for (String e : c.getConfigurationSection("enchants").getKeys(false)) {
							if (Enchantment.getByName(e) == null) {
								getLogger().log(Level.WARNING, "CustomItem " + key + " has an invalid enchantment (" + e + ") !");
								continue;
							} else {
								is.addEnchantment(Enchantment.getByName(e), c.getInt("enchants." + e));
							}
						}
					}
					customItems.put(key, is);
				}
			}
		} catch (Throwable t) {
			getLogger().log(Level.SEVERE, "Kestafoutupépé ? Yauneerreurdanlécustomiteme !");
			t.printStackTrace();
		}
		
	}


	private void initTerritories() {
		try {
			for (String s : getConfig().getConfigurationSection("territory").getKeys(false)) {
				String a = getConfig().getString("territory." + s + ".type");
				if (a.equals(Mine.class.getName())) {
					Mine.get(this, s);
				} else if (a.equals(Ferme.class.getName())) {
					Ferme.get(this, s);
				} else if (a.equals(Moulin.class.getName())) {
					Moulin.get(this, s);
				}
			}
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Pas de territoires !");
			e.printStackTrace();
		}
		
	}


	@Override
	public void onDisable() {
		for (ChestSpawner c : chestSpawners) {
			c.disable();
		}
		this.getServer().clearRecipes();
	}


	private void initInventoryChecker() {
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					checkSlots(p);
				}
			}
		}, 1L, 1L);
		
	}
	
	private void initAuraChecker() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				for (Player p : Bukkit.getOnlinePlayers()) {
					Inventory i = p.getInventory();
					if (i.getItem(8) == null || i.getItem(8).getType() == Material.AIR || getAuraTypeByMaterial(i.getItem(8).getType()) == AuraType.NOTHING) {
						ItemStack is = i.getItem(8);
						if (is == null || is.getType() != AuraType.NOTHING.getMaterial()) {
							is = new ItemStack(AuraType.NOTHING.getMaterial());
						}
						ItemMeta im = is.getItemMeta();
						if (!is.hasItemMeta()) {
							im.setDisplayName("\u00a76Vous n'avez pas d'aura");
							ArrayList<String> lore = new ArrayList<String>();
							lore.add("\u00a77Vous ne pouvez placer que des auras dans cette case !");
							im.setLore(lore);
							is.setItemMeta(im);
							i.setItem(8, is);
						}
					}
					if (getAuraTypeByMaterial(i.getItem(8).getType()) == null || getAuraTypeByMaterial(i.getItem(8).getType()) == AuraType.NOTHING) {
						if (p.hasPotionEffect(AuraType.HEALTH_BOOST.getEffectType())) p.removePotionEffect(AuraType.HEALTH_BOOST.getEffectType());
						if (p.hasPotionEffect(AuraType.RESISTANCE.getEffectType())) p.removePotionEffect(AuraType.RESISTANCE.getEffectType());
						if (p.hasPotionEffect(AuraType.SPEED.getEffectType())) p.removePotionEffect(AuraType.SPEED.getEffectType());
						if (p.hasPotionEffect(AuraType.STRENGTH.getEffectType())) p.removePotionEffect(AuraType.STRENGTH.getEffectType());
					} else {
						AuraType aura = getAuraTypeByMaterial(i.getItem(8).getType());
						net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(i.getItem(8));
						NBTTagCompound tag = stack.getTag();
						if (tag == null) {
							tag = new NBTTagCompound();
						}
						if (tag.hasKey("ticks")) {
							tag.setInt("ticks", tag.getInt("ticks") - 1);
							if (tag.getInt("ticks") <= 0) {
								i.setItem(8, null);
								continue;
							}
						} else {
							tag.setInt("ticks", aura.getDelay());
						}
						stack.setTag(tag);
						ItemStack is = CraftItemStack.asCraftMirror(stack);
						is.setDurability((short) (is.getType().getMaxDurability() -
								Math.floor(is.getType().getMaxDurability() / 
										(1 /
												((double) tag.getInt("ticks") / 
														(double) aura.getDelay())))));
						i.setItem(8, is);
						manager.setAuraMessage(p, "\u00a76Aura de " + aura.getName() + " (\u00a7b" + (tag.getInt("ticks") >= 1200 ? tag.getInt("ticks") / 1200 + "m" : "") + (tag.getInt("ticks") % 1200 < 200 ? "0" : "") + (tag.getInt("ticks") % 1200)/20 + "s\u00a76)");
						if (aura.getEffectType() != null && !p.hasPotionEffect(aura.getEffectType())) {
							p.addPotionEffect(new PotionEffect(aura.getEffectType(), tag.getInt("ticks"), aura.getPotionLevel()));
							if (aura != AuraType.HEALTH_BOOST && p.hasPotionEffect(AuraType.HEALTH_BOOST.getEffectType())) p.removePotionEffect(AuraType.HEALTH_BOOST.getEffectType());
							if (aura != AuraType.RESISTANCE && p.hasPotionEffect(AuraType.RESISTANCE.getEffectType())) p.removePotionEffect(AuraType.RESISTANCE.getEffectType());
							if (aura != AuraType.SPEED && p.hasPotionEffect(AuraType.SPEED.getEffectType())) p.removePotionEffect(AuraType.SPEED.getEffectType());
							if (aura != AuraType.STRENGTH && p.hasPotionEffect(AuraType.STRENGTH.getEffectType())) p.removePotionEffect(AuraType.STRENGTH.getEffectType());
						}
					}
				}
			}
		}, 1L, 1L);
	}
	
	private void initRandoms() {
		ConfigurationSection cs = getConfig().getConfigurationSection("probabilities.chest");
		if (cs != null) {
			try {
				Set<String> keys = cs.getKeys(false);
				ArrayList<ItemStack> items = new ArrayList<ItemStack>();
				for (int i = 0; i < keys.size(); i++) {
					try {
						String key = (String) keys.toArray()[i];
						ConfigurationSection c = cs.getConfigurationSection(key);
						for (int i1 = 0; i1 < c.getInt("prob"); i1++) {
							ItemStack is = getIStack(c.getString("type"), c.getInt("qt"));
							//getLogger().log(Level.INFO, is.toString());
							items.add(removeAttributes(is));
						}
					} catch (Exception e) {
						getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation d'une probabilité chest");
						getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
						e.printStackTrace();
					}
				}
				randomChestItems = items.toArray(new ItemStack[items.size()]);
			} catch(Exception e) {
				getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation des probabilités chest");
				getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
				e.printStackTrace();
			}
		}
		cs = getConfig().getConfigurationSection("probabilities.magicchest");
		if (cs != null) {
			try {
				Set<String> keys = cs.getKeys(false);
				ArrayList<ItemStack> items = new ArrayList<ItemStack>();
				for (int i = 0; i < keys.size(); i++) {
					try {
						String key = (String) keys.toArray()[i];
						ConfigurationSection c = cs.getConfigurationSection(key);
						for (int i1 = 0; i1 < c.getInt("prob"); i1++) {
							items.add(removeAttributes(getIStack(c.getString("type"), c.getInt("qt"))));
						}
					} catch (Exception e) {
						getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation d'une probabilité magicchest");
						getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
						e.printStackTrace();
					}
				}
				randomMagicChestItems = items.toArray(new ItemStack[items.size()]);
			} catch(Exception e) {
				getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation des probabilités magicchest");
				getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
				e.printStackTrace();
			}
		}
		cs = getConfig().getConfigurationSection("probabilities.randommobs");
		if (cs != null) {
			try {
				Set<String> keys = cs.getKeys(false);
				ArrayList<EntityType> monsters = new ArrayList<EntityType>();
				for (int i = 0; i < keys.size(); i++) {
					try {
						String key = (String) keys.toArray()[i];
						ConfigurationSection c = cs.getConfigurationSection(key);
						for (int i1 = 0; i1 < c.getInt("prob"); i1++) {
							monsters.add(EntityType.fromName(key));
						}
					} catch (Exception e) {
						getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation d'une probabilité mobs");
						getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
						e.printStackTrace();
					}
				}
				randomMobs = monsters.toArray(new EntityType[monsters.size()]);
			} catch(Exception e) {
				getLogger().log(Level.SEVERE, "Erreur lors de l'initialisation des probabilités mobs");
				getLogger().log(Level.SEVERE, "============== STACKTRACE ==============");
				e.printStackTrace();
			}
		}
	}
	
	
	private ItemStack getIStack(String s, int qt) {
		if (customItems.containsKey(s)) 
		{
			ItemStack is = customItems.get(s);
			if (qt < 1) qt = 1; 
			is.setAmount(qt);
			return is;
		} else {
			return null;
		}
		/*
		if (s.equalsIgnoreCase("TELEPORTER")) {
			ItemStack is = new ItemStack(Material.HOPPER_MINECART, qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("\u00a76Téléporteur");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		if (s.equalsIgnoreCase("CUSTOM_HORSE")) {
			ItemStack is = new ItemStack(Material.SADDLE, qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("\u00a76Cheval");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		if (s.equalsIgnoreCase("AURA_HEALTH")) {
			ItemStack is = new ItemStack(AuraType.HEALTH_BOOST.getMaterial(), qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(name != null && name.length() > 0 ? name : "\u00a76Aura de vie");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		if (s.equalsIgnoreCase("AURA_STRENGTH")) {
			ItemStack is = new ItemStack(AuraType.STRENGTH.getMaterial(), qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(name != null && name.length() > 0 ? name : "\u00a76Aura de force");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		if (s.equalsIgnoreCase("AURA_RESISTANCE")) {
			ItemStack is = new ItemStack(AuraType.RESISTANCE.getMaterial(), qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(name != null && name.length() > 0 ? name : "\u00a76Aura de résistance");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		if (s.equalsIgnoreCase("AURA_SPEED")) {
			ItemStack is = new ItemStack(AuraType.SPEED.getMaterial(), qt, meta);
			ItemMeta im = is.getItemMeta();
			im.setDisplayName(name != null && name.length() > 0 ? name : "\u00a76Aura de vitesse");
			im.setLore(lores);
			is.setItemMeta(im);
			return is;
		}
		try {
			ItemStack is = new ItemStack(Material.getMaterial(s), qt, meta); 
			if (name != null && name.length() > 0) {
				ItemMeta im = is.getItemMeta();
				im.setDisplayName(name);
				im.setLore(lores);
				is.setItemMeta(im);
			}
			return is;
		} catch (Exception e) {
			e.printStackTrace();
			getLogger().log(Level.SEVERE, "Can't find " + s);
		}
		return null;*/
	}

	public void replaceChests() {
		for (ChestSpawner c : chestSpawners) {
			c.deSpawn(false);
			c.spawn();
		}
		
	}

	public ArrayList<ChestSpawner> reloadChestSpawners() {
		for (ChestSpawner c : chestSpawners) {
			c.disable();
		}
		chestSpawners.clear();
		if (getConfig().getConfigurationSection("chestspawners") == null) return chestSpawners;
		for (String key : getConfig().getConfigurationSection("chestspawners").getKeys(false)) {
			ConfigurationSection c = getConfig().getConfigurationSection("chestspawners." + key);
			chestSpawners.add(new ChestSpawner(c.getInt("x"),
					 c.getInt("y"), 
					 c.getInt("z"), 
					 Integer.parseInt(key), 
					 c.getInt("delay"), 
					 ChestSpawner.getType(c.getString("type")),
					 this));
		}
		for (ChestSpawner c : chestSpawners) {
			c.enable();
		}
		return chestSpawners;
	}

	public static Location getAvailableSpawnLocation(Location location) {
		int max = (int) world.getWorldBorder().getSize() - 16;
		Location spawn = world.getWorldBorder().getCenter();
		int x = (int) (Math.floor(spawn.getX()) + Math.floor(Math.random() * max - max/2));
		int y = 3;
		int z = (int) (Math.floor(spawn.getZ()) + Math.floor(Math.random() * max - max/2));
		Location loc = new Location(world, x, y, z);
		while (loc.getBlock().getLightFromSky() < 4 && y < 140) {
			y++;
			loc = new Location(world, x, y, z);
		}
		
		if (y >= 140) {
			loc = getAvailableSpawnLocation(location);
		} else if (loc.distanceSquared(location) < 256) {
			loc = getAvailableSpawnLocation(location);
		}
		return loc;
	}
	
	public ItemStack getRandomChestItem(ChestType type) {
		try {
			if (type.equals(ChestType.NORMAL))
					return randomChestItems[new Random().nextInt(randomChestItems.length)];
				return randomMagicChestItems[new Random().nextInt(randomMagicChestItems.length)];
		} catch (java.lang.IllegalArgumentException e) {
			getLogger().log(Level.WARNING, "Pas d'items valides en configuration pour les coffres de type " + type.toString());
			return null;
		}
	
	}
	
	public static boolean soundOk(OfflinePlayer p) {
		if (!playerSounds.containsKey(p.getUniqueId())) {
			playerSounds.put(p.getUniqueId(), System.currentTimeMillis());
			return true;
		} else {
			if (System.currentTimeMillis() - playerSounds.get(p.getUniqueId()) >= 5000) {
				playerSounds.put(p.getUniqueId(), System.currentTimeMillis());
				return true;
			} else {
				return false;
			}
		}
	}
	
	public static void teleportDelay(final Player p, final Location l) {
		if (soundOk(p)) {
			p.playSound(p.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0F, 1.0F);
			p.playSound(l, Sound.ENDERDRAGON_DEATH, 1.0F, 1.0F);
		}
		new BukkitRunnable() {
			int i = 100;
			float d = 3.35F;
			@Override
			public void run() {
				if (i <= 0) {
					this.cancel();
				}
				if (i % 50 == 0) {
					Location o = l.clone();
					o.setY(o.getY() + 3);
				    o.getWorld().playEffect(o, Effect.ENDER_SIGNAL, 4);
				    o = p.getLocation().clone();
					o.setY(o.getY() + 3);
				    o.getWorld().playEffect(o, Effect.ENDER_SIGNAL, 4);
				}
				if (i == 0) {
				    p.getWorld().playSound(p.getLocation(), Sound.ENDERMAN_TELEPORT, 5.0F, 1.0F);
				    l.getWorld().playSound(l, Sound.ENDERMAN_TELEPORT, 5.0F, 1.0F);
					p.teleport(l);
					manager.setTPMessage(p, "\u00a7aTéléportation effectuée !");
					if (playerTPs.containsKey(p.getName())) {
						playerTPs.remove(p.getName());
					}
				} else {
					if (!playerTPs.containsKey(p.getName())) {
						playerTPs.put(p.getName(), p.getLocation());
					}
					if (playerTPs.get(p.getName()).distanceSquared(p.getLocation()) > 1) {
						playerTPs.remove(p.getName());
						manager.setTPMessage(p, "\u00a7cVous ne devez pas bouger pendant la téléportation !");
						this.cancel();
						net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(new ItemStack(Material.HOPPER_MINECART));
						NBTTagCompound tag = new NBTTagCompound();
						tag.setInt("xCoord", l.getBlockX());
						tag.setInt("yCoord", l.getBlockY());
						tag.setInt("zCoord", l.getBlockZ());
						stack.setTag(tag);
						ItemStack is = CraftItemStack.asCraftMirror(stack);
						is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
						ItemMeta m = is.getItemMeta();
						m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
						m.setDisplayName("\u00a76Téléporteur");
						is.setItemMeta(m);
						if (p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) {
							p.setItemInHand(is);
						} else {
							p.getInventory().addItem(is);
						}
						p.removePotionEffect(PotionEffectType.CONFUSION);
						return;
					}
					if (i == 90) {
						((CraftPlayer) p).getHandle().addEffect(new MobEffect(9, 200, 5));
					}
					StringBuilder s = new StringBuilder();
					s.append("\u00a72");
					for (int a = 0; a < (100 - (i - i % d)) / d; a++) {
						s.append("=");
					}
					s.append("\u00a74");
					for (int a = 0; a < (i - i % d) / d; a++) {
						s.append("=");
					}
						manager.setTPMessage(p, s.toString());
				}
				i--;
			}
			
		}.runTaskTimer(Bukkit.getPluginManager().getPlugin("Survivor"), 0L, 1L);
	}
	public enum AuraType {
		  HEALTH_BOOST ("vie", Material.WOOD_HOE, PotionEffectType.HEALTH_BOOST, 0, 2400),
		  RESISTANCE ("resistance", Material.STONE_HOE, PotionEffectType.DAMAGE_RESISTANCE, 0, 2400),
		  STRENGTH ("force", Material.IRON_HOE, PotionEffectType.INCREASE_DAMAGE, 0, 2400),
		  SPEED ("vitesse", Material.DIAMOND_HOE, PotionEffectType.SPEED, 0, 2400),
		  NOTHING (null, Material.POWERED_MINECART, null, 0, 0);
		   
		  private Material material = Material.AIR;
		  private PotionEffectType potionEffectType;
		  private int strength;
		  private int delay;
		  private String name;
		  
		  AuraType(String name, Material m, PotionEffectType pet, int strength, int delay){
		    this.material = m;
		    this.potionEffectType = pet;
		    this.strength = strength;
		    this.delay = delay;
		    this.name = name;
		  }
		   
		  public Material getMaterial(){
		    return this.material;
		  }
		  
		  public PotionEffectType getEffectType() {
			  return this.potionEffectType;
		  }
		  
		  public int getPotionLevel() {
			  return this.strength;
		  }
		  
		  public int getDelay() {
			  return this.delay;
		  }
		  
		  public String getName() {
			  return this.name;
		  }
		  
		}
	
	public static AuraType getAuraTypeByMaterial(Material m) {
		if (m == null) return null;
		if (m.equals(AuraType.HEALTH_BOOST.getMaterial()))
			return AuraType.HEALTH_BOOST;
		 else if (m.equals(AuraType.RESISTANCE.getMaterial())) 
			return AuraType.RESISTANCE;
		 else if (m.equals(AuraType.STRENGTH.getMaterial()))
			 return AuraType.STRENGTH;
		 else if (m.equals(AuraType.SPEED.getMaterial()))
			 return AuraType.SPEED;
		 else if (m.equals(AuraType.NOTHING.getMaterial()))
			 return AuraType.NOTHING;
		 else return null;
		
	}
	
	public static ItemStack removeAttributes(ItemStack item) {
	    if (!MinecraftReflection.isCraftItemStack(item)) {
	        item = MinecraftReflection.getBukkitItemStack(item);
	    }
	    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
	    compound.put(NbtFactory.ofList("AttributeModifiers"));
	    return item;
	}
	
	public static void checkSlots(Player p) {
		Inventory i = p.getInventory();
		if (i.getItem(8) != null && i.getItem(8).getType() != Material.AIR && getAuraTypeByMaterial(i.getItem(8).getType()) == null) {
			if (!auraSlotMessageCooldown.contains(p.getName())) {
				final String name = p.getName();
				auraSlotMessageCooldown.add(name);
				Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Survivor"), new Runnable() {
					@Override
					public void run() {
						auraSlotMessageCooldown.remove(name);
					}
				}, 15000);
				p.sendMessage("\u00a7cVous ne pouvez placer que des auras dans cette case !");
			}
			boolean ok = false;
			ItemStack o = i.getItem(8);
			int it = 0;
			for (ItemStack is : i.getContents()) {
				if (it != 8 && (is == null || is.getType() == Material.AIR)) {
					ok = true;
					is = new ItemStack(o.getType(), o.getAmount(), o.getDurability());
					is.setData(o.getData());
					is.setItemMeta(o.getItemMeta());
					i.setItem(it, is);
					break;
				}
				it++;
			}
			if (!ok) {
				p.getWorld().dropItemNaturally(p.getLocation(), o.clone());
			}
			i.setItem(8, null);
		}
		int in = 0;
		boolean modified = false;
		ItemStack[] newis = p.getInventory().getContents().clone();
		for (ItemStack is : p.getInventory().getContents()) {
			if (is != null && getAuraTypeByMaterial(is.getType()) == AuraType.NOTHING && in != 8) {
				modified = true;
				newis[in] = null;
			} else if ((is == null || getAuraTypeByMaterial(is.getType()) == null) && in == 8) {
				modified = true;
				newis[in] = new ItemStack(AuraType.NOTHING.getMaterial());
			}
			in++;
		}
		if (modified) {
			p.getInventory().setContents(newis);
			p.updateInventory();
		}
		/*int it = 0;
		boolean ok = true;
		ItemStack[] isa = i.getContents().clone();
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == Material.GOLD_HOE && it != 8) {
				isa[it] = null;
				ok = false;
			}
		}
		if (!ok) {
			i.setContents(isa);
		}*/
		if (p.getItemInHand() != null && p.getItemInHand().getType() == Material.BOW) {
			short amount = getMaterialCountInInventory(p.getInventory(), Material.ARROW);
			String text;
			if (amount < 1) {
				text = "\u00a7cVous n'avez pas de flèches.";
			} else {
				text = "\u00a76Vous avez \u00a7a" + amount + " \u00a76flèche" + (amount > 1 ? "s" : "") + ".";
			}
			manager.setBowMessage(p, text);
		}
		
	}
	
	public static boolean hasFreeSpace(Inventory i) {
		for (ItemStack is : i.getContents()) {
			if (is == null || is.getType() == Material.AIR) {
				return true;
			}
		}
		return false;
	}
	
	public static short getMaterialCountInInventory(Inventory i, Material m) {
		short count = 0;
		for (ItemStack is : i.getContents()) {
			if (is != null && is.getType() == m) {
				count += is.getAmount();
			}
		}
		return count;
	}
	
	public static void overwriteVillagerAI(EntityVillager ev) {
		NBTTagCompound compound = new NBTTagCompound();
		ev.c(compound);
		compound.setByte("NoAI", (byte) 1);
		ev.f(compound);
	}


	public static Class<?> getTerritoryClass(String s) {
		if (s.equalsIgnoreCase("mine")) {
			return Mine.class;
		} else if (s.equalsIgnoreCase("ferme")) {
			return Ferme.class;
		} else if (s.equalsIgnoreCase("moulin")) {
			return Moulin.class;
		}
		return null;
	}


	public static Entity getEntity(UUID u, boolean deep) {
		if (deep) {
			for (Entity e : world.getEntities()) {
				if (e.getUniqueId().equals(u)) {
					return e;
				}
			}
		} else {
			for (Chunk chunk : world.getLoadedChunks()) {
	            for (Entity entity : chunk.getEntities()) {
	                if (entity.getUniqueId().equals(u))
	                    return entity;
	            }
	        }
		}
		return null;
	}
	
	public static int getMax(int value, int max, int divider) {
		if (value >= max * divider) {
			return max;
		} else {
			return (int) value / divider;
		}
	}



	public static String getLostString(int wheat, int leather, int gold, float xp) {
		if (wheat > 0 || leather > 0 || gold > 0 || xp > 0) {
			StringBuilder s = new StringBuilder();
			if (wheat > 0) {
				s.append("§c" + -wheat + " §6blé" + (wheat > 1 ? "s" : ""));
				s.append("\n");
			}
			if (leather > 0) {
				s.append("§c" + -leather + " §6cuir" + (leather > 1 ? "s" : ""));
				s.append("\n");
			}
			if (gold > 0) {
				s.append("§c" + -gold + " §6lingot" + (gold > 1 ? "s" : "") + " d'or");
				s.append("\n");
			}
			if (xp > 0) {
				s.append("§c" + (xp == (int) xp ? (int) -xp : -xp) + " §6niveau" + (xp > 1 ? "x" : "") + " d'expérience");
				s.append("\n");
			}
			return s.toString();
		} else {
			return null;
		}
	}



	public static Location getAvailableSpawnLocation() {
		return getAvailableSpawnLocation(world.getSpawnLocation());
	}

}