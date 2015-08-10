package fr.adrean.Survivor.manager;

import java.util.ArrayList;
import java.util.logging.Level;

import mkremins.fanciful.FancyMessage;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import fr.adrean.BlueCore.BlueCore;
import fr.adrean.Survivor.ChestSpawner;
import fr.adrean.Survivor.Core;
import fr.adrean.Survivor.Core.AuraType;
import fr.adrean.Survivor.Exchange;
import fr.adrean.Survivor.ShopType;
import fr.adrean.Survivor.TradeRequest;
import fr.adrean.Survivor.gui.ShopGUI;
import fr.adrean.Survivor.territory.Territory;

public class PlayerManager implements Listener {

	Core plugin;
	
	public PlayerManager(Core plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		if (!e.getPlayer().isOp()) return;
		if (e.getMessage().equals("/fly")) {
			e.getPlayer().setAllowFlight(true);
			e.setCancelled(true);
		}
		if (e.getMessage().equalsIgnoreCase("/g")) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			if (p.getGameMode() == GameMode.CREATIVE) {
				p.setGameMode(GameMode.ADVENTURE);
				p.sendMessage("\u00a7aVous avez bien été switché en gamemode \u00a76aventure\u00a7a !");
			} else {
				p.setGameMode(GameMode.CREATIVE);
				p.sendMessage("\u00a7aVous avez bien été switché en gamemode \u00a76créatif\u00a7a !");
			}
		} else if (e.getMessage().startsWith("/rename")) {
			e.setCancelled(true);
			if (e.getMessage().split(" ").length < 2) {
				e.getPlayer().sendMessage("/rename <name>");
				return;
			}
			if (e.getPlayer().getNearbyEntities(1, 1, 1).size() != 1) {
				e.getPlayer().sendMessage("Vous devez être à moins d'un bloc d'une seule entité !");
				return;
			}
			e.getPlayer().getNearbyEntities(1, 1, 1).get(0).setCustomName(e.getMessage().split(" ")[1].replaceAll("&", "\u00a7"));
			e.getPlayer().getNearbyEntities(1, 1, 1).get(0).setCustomNameVisible(true);
			e.getPlayer().sendMessage(e.getPlayer().getNearbyEntities(1, 1, 1).get(0).getUniqueId().toString() + " est maintenant " + e.getPlayer().getNearbyEntities(1, 1, 1).get(0).getCustomName());
		} else if (e.getMessage().equalsIgnoreCase("/noai")) {
			e.setCancelled(true);
			if (e.getPlayer().getNearbyEntities(1, 1, 1).size() != 1) {
				e.getPlayer().sendMessage("Vous devez être à moins d'un bloc d'un seul villageois !");
				return;
			}
			if (!(e.getPlayer().getNearbyEntities(1, 1, 1).get(0) instanceof CraftVillager)) {
				e.getPlayer().sendMessage("Ce n'est pas un villageois !");
				return;
			}
			Core.overwriteVillagerAI(((CraftVillager) e.getPlayer().getNearbyEntities(1, 1, 1).get(0)).getHandle());
			e.getPlayer().sendMessage("Success !");
		} else if (e.getMessage().equalsIgnoreCase("/tpentity")) {
			e.setCancelled(true);
			if (e.getPlayer().getNearbyEntities(1, 1, 1).size() != 1) {
				e.getPlayer().sendMessage("Vous devez être à moins d'un bloc d'un seul villageois !");
				return;
			}
			e.getPlayer().getNearbyEntities(1, 1, 1).get(0).teleport(e.getPlayer());
			e.getPlayer().sendMessage("Success !");
		} else if (e.getMessage().equalsIgnoreCase("/entityinfo")) {
			e.setCancelled(true);
			if (e.getPlayer().getNearbyEntities(1, 1, 1).size() != 1) {
				e.getPlayer().sendMessage("Vous devez être à moins d'un bloc d'un seul villageois !");
				return;
			}
			Entity en = e.getPlayer().getNearbyEntities(1, 1, 1).get(0);
			e.getPlayer().sendMessage(en.getUniqueId().toString() + " (" + en.getEntityId() + ")\n" + en.getTicksLived() + " ticks\n" + en.getType().toString() );
		} else if (e.getMessage().equals("/a")) {
			plugin.a = !plugin.a;	
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		e.setJoinMessage("\u00a78[\u00a7a+\u00a78] " + p.getName());
		p.setGameMode(p.isOp() ? GameMode.CREATIVE : GameMode.ADVENTURE);
        //p.teleport(Core.getAvailableSpawnLocation(p.getWorld().getWorldBorder().getCenter()));
        p.setAllowFlight(true);
		ScoreboardManager.update(p);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		e.setQuitMessage("\u00a78[\u00a7c-\u00a78] " + p.getName());
	}
	
	@EventHandler
	public void onBlockPlaced(BlockPlaceEvent e) {
		if (e.getPlayer() != null && !e.getPlayer().isOp()) {
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBroken(BlockBreakEvent e) {
	//	e.getPlayer().sendMessage(e.getBlock().getType() + "" + e.getBlock().getData() + e.getBlock());
		if (e.getPlayer() != null && !e.getPlayer().isOp()) {
			e.setCancelled(true);
			return;
		}
		if (e.getBlock().getType().equals(Material.CHEST) || e.getBlock().getType().equals(Material.TRAPPED_CHEST)) {
			for (final ChestSpawner c : Core.chestSpawners) {
				if (c.location.equals(e.getBlock().getLocation())) {
					c.schedule();
					break;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChestInteract(InventoryCloseEvent e) {
		if (e.getInventory().getType().equals(InventoryType.CHEST)) {
			Inventory i = e.getInventory();
			if (i.getHolder() instanceof CraftChest) {
				Chest block = (Chest) i.getHolder();
				for (final ChestSpawner c : Core.chestSpawners) {
					if (c.location.equals(block.getLocation())) {
						for (ItemStack is : i.getContents()) {
							if (is == null) continue;
							if (is.getType().equals(Material.AIR)) continue;
							return;
						}
						new BukkitRunnable() {
							@Override
							public void run() {
								c.deSpawn(true);
								c.schedule();
							}
						}.runTaskLater(this.plugin, 20);
						return;
					}
				} 
			}
		}
		 
	}
	
	/**
	 * 
	 * Empêche de passer par le death screen
	 * @param e
	 */
	@EventHandler
    public void onPlayerDamageReceive(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            
            if((damaged.getHealth() - e.getDamage()) <= 0) {
                PlayerInventory i = damaged.getInventory();
                for (ItemStack s : i.getContents()) {
                	if (s != null && s.getType() != Material.AIR && Core.getAuraTypeByMaterial(s.getType()) != AuraType.NOTHING && Math.random() > 0.75) {
	                	Location l = damaged.getLocation();
	                	l.setX(l.getX() + (Math.random() * 3 - 1.5));
	                	l.setY(l.getY() + (Math.random() / 2));
	                	l.setZ(l.getZ() + (Math.random() * 3 - 1.5));
	                	l.getWorld().dropItemNaturally(l, s);
                	}
                }
                for (ItemStack s : damaged.getInventory().getArmorContents()) {
	                if (s != null && s.getType() != Material.AIR && Math.random() > 0.75) {
		            	Location l = damaged.getLocation();
		            	l.setX(l.getX() + (Math.random() * 3 - 1.5));
		            	l.setY(l.getY() + (Math.random() / 2));
		            	l.setZ(l.getZ() + (Math.random() * 3 - 1.5));
		            	l.getWorld().dropItemNaturally(l, s);
	                }
                }
                ItemStack[] ni = new ItemStack[i.getSize()];
                for (int a = 0; a < ni.length; a++) {
                	ni[a] = null;
                }
                i.setContents(ni);
                ItemStack[] nia = new ItemStack[] {null, null, null, null};
                i.setArmorContents(nia);
                damaged.playSound(damaged.getLocation(), Sound.NOTE_PLING, 2, 2);
            }
            	
            
          /*  if(e.getDamager() instanceof Player) {
                Player damager = (Player) e.getDamager();
 
            }*/
        } else if (e.getEntity() instanceof Villager) {
        	if (e.getEntity().getCustomName() != null && ShopType.getByName(e.getEntity().getCustomName()) != null) {
        		e.setCancelled(true);
        	}
        }
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(final PlayerRespawnEvent e) {
        new BukkitRunnable() {

			@Override
			public void run() {
	        	e.getPlayer().teleport(Core.getAvailableSpawnLocation());
	        	e.getPlayer().setMaxHealth(40);
	        	e.getPlayer().setHealth(40);
			}
        }.runTaskLater(plugin, 1);
	}
	
	@EventHandler
	public void PlayerDamagedCommit(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			if (p.getItemInHand() != null && p.getItemInHand().getType() != Material.AIR && p.getItemInHand().getAmount() > 0) 
			{
				Material m = p.getItemInHand().getType();
				int i = -1;
				if (m == Material.WOOD_SWORD)
					i += 3;
				else if (m == Material.STONE_SWORD)
					i += 5;
				else if (m == Material.IRON_SWORD)
					i += 6;
				else if (m == Material.GOLD_SWORD)
					i += 7;
				else if (m == Material.DIAMOND_SWORD)
					i += 8;
				else if (m == Material.WOOD_AXE)
					i += 2;
				else if (m == Material.STONE_AXE)
					i += 4;
				else if (m == Material.IRON_AXE)
					i += 5;
				else if (m == Material.GOLD_AXE)
					i += 6;
				else if (m == Material.DIAMOND_AXE)
					i += 7;
				e.setDamage(e.getDamage() + i);
				if (e.getDamage() <= 1) {
					e.setCancelled(true);
				}
			} else {
				e.setCancelled(true);
			}
		}
		if (e.getEntity() instanceof Player && ((e.getDamager() instanceof Player) || ( e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player))) {
			Player damaged = (Player) e.getEntity();
			Player damager = null;
			if (e.getDamager() instanceof Player) {
				damager = (Player) e.getDamager();
			} else {
				damager = (Player) (((Arrow) e.getDamager()).getShooter());
			}
			if (damaged.getHealth() - e.getDamage() <= 0) {
				damaged.setHealth(0);
				float xp = damaged.getLevel() + damaged.getExp();
				float xplost = 0;
				if (xp > 1) {
					xp--;
					damager.setLevel(damager.getLevel() + 1);
					damaged.setLevel((int) xp);
					damaged.setExp(xp - (int) xp);
					xplost = 1;
				} else {
					xplost = damaged.getExp();
					float newxp = damager.getLevel() + damager.getExp() + xplost;
					damaged.setExp(0);
					damager.setLevel((int)newxp);
					damager.setExp(newxp - (int) newxp);
				}
				int wheat = Core.getMax(ResourceManager.getWheat(damaged), 10, 2);
				int leather = Core.getMax(ResourceManager.getLeather(damaged), 10, 2);
				int gold = Core.getMax(ResourceManager.getGold(damaged), 10, 2);
				ResourceManager.removeWheat(damaged, wheat);
				ResourceManager.removeLeather(damaged, leather);
				ResourceManager.removeGold(damaged, gold);
				ResourceManager.addWheat(damager, wheat);
				ResourceManager.addLeather(damager, leather);
				ResourceManager.addGold(damager, gold);
				damaged.sendMessage(Core.getLostString(wheat, leather, gold, xplost));
				damager.sendMessage("A" +Core.getLostString(wheat, leather, gold, xplost));
				damager.playSound(damaged.getLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
				damager.playSound(damaged.getLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
				final Player p = damager; 
				new BukkitRunnable() {
					public void run() {
						p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 1, 1);
					}
				}.runTaskLater(plugin, 2);
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Player p = e.getPlayer();
			if (p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.HOPPER_MINECART)) { // Hand = Hopper
				if (p.getItemInHand().hasItemMeta()) {
					ItemMeta meta = p.getItemInHand().getItemMeta();
					if (meta.getDisplayName().equals("\u00a76Téléporteur")) {
						e.setCancelled(true);
						net.minecraft.server.v1_8_R3.ItemStack stack = CraftItemStack.asNMSCopy(p.getItemInHand());
						NBTTagCompound tag = stack.getTag();
						if (tag.hasKey("xCoord") && tag.hasKey("yCoord") && tag.hasKey("zCoord")) {
							try {
								Core.teleportDelay(p, new Location(Core.world, tag.getInt("xCoord") + 0.5, tag.getInt("yCoord") + 0.5, tag.getInt("zCoord") + 0.5));
								if (p.getItemInHand().getAmount() < 2) {
									p.setItemInHand(null);
								} else {
									p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
								}
							} catch (Exception e1) {
								plugin.getLogger().log(Level.SEVERE, "ERREUR ITEMNBTTAG !!!!!");
								e1.printStackTrace();
								p.sendMessage("\u00a7cOups, petite erreur...");
								ItemStack i = p.getItemInHand();
								i = new ItemStack(i.getType(), i.getAmount());
								ItemMeta m = i.getItemMeta();
								m.setDisplayName("\u00a76Téléporteur");
								i.setItemMeta(m);
								p.setItemInHand(i);
							}
						} else {
							Location l = p.getLocation();
							tag.setInt("xCoord", l.getBlockX());
							tag.setInt("yCoord", l.getBlockY());
							tag.setInt("zCoord", l.getBlockZ());
							stack.setTag(tag);
							ItemStack is = CraftItemStack.asCraftMirror(stack);
							is.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
							ItemMeta ismeta = is.getItemMeta();
							ismeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							is.setItemMeta(ismeta);
							if (is.getAmount() > 1) {
								ItemStack o = new ItemStack(is.getType(), is.getAmount() - 1);
								ItemMeta m = o.getItemMeta();
								m.setDisplayName("\u00a76Téléporteur");
								o.setItemMeta(m);
								p.setItemInHand(is);
								p.getInventory().addItem(o);
							} else {
								p.setItemInHand(is);
							}
							p.sendMessage("\u00a7aPosition sauvegardée !");
							p.playSound(p.getLocation(), Sound.FIRE_IGNITE, 1, 1);
						}
					} else {
						p.sendMessage(meta.getDisplayName());
					}
				}
			} else if (p.getItemInHand() != null && p.getItemInHand().getType().equals(Material.BUCKET)) { //Hand = Bucket
				p.setItemInHand(null);
			} /*aura*/else if (p.getItemInHand() != null && Core.getAuraTypeByMaterial(p.getItemInHand().getType()) != null && Core.getAuraTypeByMaterial(p.getItemInHand().getType()).getEffectType() != null) {
				if (p.getInventory().getHeldItemSlot() == 8) {
					e.getPlayer().getInventory().addItem(p.getItemInHand().clone());
					e.getPlayer().setItemInHand(null);
					e.getPlayer().updateInventory();
					e.setCancelled(true);
				} else if (e.getPlayer().getInventory().getItem(8) == null || e.getPlayer().getInventory().getItem(8).getType() == Material.AIR || Core.getAuraTypeByMaterial(e.getPlayer().getInventory().getItem(8).getType()) == AuraType.NOTHING) {
					e.getPlayer().getInventory().setItem(8, e.getPlayer().getItemInHand().clone());
					e.getPlayer().setItemInHand(null);
					e.getPlayer().updateInventory();
					e.setCancelled(true);
				}
			} /*no hopperminecart*/else if ((p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) && p.getInventory().getHeldItemSlot() == 8) {
				int i = 0;
				boolean success = false;
				for (ItemStack is : p.getInventory().getContents()) {
					if (is != null && Core.getAuraTypeByMaterial(is.getType()) != null) {
						p.getInventory().setItem(8, is.clone());
						p.getInventory().setItem(i, null);
						p.updateInventory();
						success = true;
						break;
					}
					i++;
				}
				if (!success) {
					p.sendMessage("\u00a7cVous ne possédez pas d'aura dans votre inventaire !");
				}
			}
			/*anti hoe*/if (e.getClickedBlock() != null && (e.getClickedBlock().getType() == Material.DIRT || e.getClickedBlock().getType() == Material.GRASS) && (p.getItemInHand() != null && (Core.getAuraTypeByMaterial(p.getItemInHand().getType()) != null))) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerConsume(final PlayerItemConsumeEvent e) {
		if (e.getItem().getType().equals(Material.MILK_BUCKET)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (e.getPlayer().getItemInHand().getType().equals(Material.BUCKET)) {
						e.getPlayer().setItemInHand(null);
					}
				}
			}.runTaskLater(plugin, 1);
		}
	}
	
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {
		ItemStack is = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		if (is != null && is.getType() == Material.BOW) {
			ItemStack nis = e.getPlayer().getInventory().getItem(e.getNewSlot());
			if (nis == null || nis.getType() != Material.BOW) {
				Core.manager.setBowMessage(e.getPlayer(), "");
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	//AntiHopperMove
	public void onPlayerClick(InventoryClickEvent e) {
		if (e.getSlot() == 8 && e.getSlotType() == SlotType.QUICKBAR) {
			e.setCancelled(true);
			((Player) e.getWhoClicked()) .updateInventory();
			if (e.getCursor() != null && e.getCursor().getType() != Material.AIR)
				e.getWhoClicked().getWorld().dropItemNaturally(e.getWhoClicked().getLocation(), e.getCursor().clone());
				e.setCursor(null);
		}
	}
	
	@EventHandler
	//AntiDropHopper
	public void onPlayerDrop(PlayerDropItemEvent e) {
		AuraType a = Core.getAuraTypeByMaterial(e.getItemDrop().getItemStack().getType());
		if (a != null && a.getEffectType() == null) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	//RenameTerritory
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		if (e.getPlayer().isOp() && Core.rename.containsKey(e.getPlayer().getName())) {
			Territory t = Territory.territories.get(Core.rename.get(e.getPlayer().getName()));
			Core.rename.remove(e.getPlayer().getName());
			if (t == null) {
				e.getPlayer().sendMessage("\u00a7cLe terrain n'existe plus ! \u00a7a:'| ?");
			} else {
				t.setName(e.getMessage().replaceAll("&", "\u00a7"));
				e.getPlayer().sendMessage("\u00a7aSuccès !");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	//ShopManager
	public void onPlayerOpenPNJ(PlayerInteractAtEntityEvent e) {
		if (e.getRightClicked() instanceof CraftVillager) {
			e.setCancelled(true);
			ShopType t = ShopType.getByName(e.getRightClicked().getCustomName());
			if (t != null) {
				e.getPlayer().openInventory(new ShopGUI(t, e.getPlayer(), plugin));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerIntentExchange(PlayerInteractAtEntityEvent e) {
		if (!e.getPlayer().isSneaking()) return;
		Player rc = (Player) e.getRightClicked();
		if (e.getRightClicked() instanceof Player) {
			for (TradeRequest tr : new ArrayList<TradeRequest>(Core.playerTradeRequests)) {
				if (tr.isValid(e.getPlayer().getUniqueId(), rc.getUniqueId())) {
					new Exchange(e.getPlayer(), rc, plugin).openGUIs();
					return;
				}
			}
			Core.playerTradeRequests.add(new TradeRequest(e.getPlayer().getUniqueId(), rc.getUniqueId(), 5000));
			BlueCore.getFancyName(new FancyMessage("Demande envoyée à ").color(ChatColor.GREEN), rc, e.getPlayer()).send(e.getPlayer());;
			BlueCore.getFancyName(new FancyMessage("Vous avez reçu une demande de ").color(ChatColor.GOLD), e.getPlayer(), rc).send(rc);;
		}
	}
	
}
