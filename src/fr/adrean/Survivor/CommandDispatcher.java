package fr.adrean.Survivor;

import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.server.v1_8_R3.EntityVillager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.adrean.Survivor.ChestSpawner.ChestType;
import fr.adrean.Survivor.territory.Ferme;
import fr.adrean.Survivor.territory.Mine;
import fr.adrean.Survivor.territory.Moulin;
import fr.adrean.Survivor.territory.Territory;

public class CommandDispatcher implements CommandExecutor {
	private Core plugin;
	public static String usage = "/survivor <chest>";
	
	public CommandDispatcher(Core plugin) {
		this.plugin = plugin;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) {
		if (cmd.getName().equals("survivor")) {
			if (args.length == 0) {
				sender.sendMessage("\u00a7aSurvivor v" + Core.version + "\n\u00a7c" + usage);
				return true;
			}
			
			//Tp
			if (args[0].equalsIgnoreCase("getteleporteritem")) {
				ItemStack is = new ItemStack(Material.HOPPER_MINECART);
				ItemMeta m = is.getItemMeta();
				m.setDisplayName("\u00a76Téléporteur");
				is.setItemMeta(m);
				if (!(sender instanceof Player)) 
				{
					sender.sendMessage("\u00a7cNo console !");
					return true;
				}
				((Player) sender).getInventory().addItem(is);
				sender.sendMessage("\u00a7aPop !");
				return true;
			}
			
			//Chest
			if (args[0].equalsIgnoreCase("chest")) {
				if (args.length == 1) {
					sender.sendMessage(plugin.getConfig().getString("messages.command.chest.args.0"));
					return true;
					
				} 
				if (args[1].equalsIgnoreCase("list")) {
					sender.sendMessage("\u00a76ID - TYPE - X Y Z");
					for (ChestSpawner c : Core.chestSpawners) {
						sender.sendMessage("\u00a7a" + c.id + " \u00a76 - " + (c.type == ChestType.NORMAL ? "\u00a7a" : "\u00a7c") + c.type.toString() + "\u00a76 - \u00a7a" + (int) Math.floor(c.location.getX()) + " " + (int) Math.floor(c.location.getY()) + " " + (int) Math.floor(c.location.getZ()));
					}
					return true;
					
				} 
				if (!args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove")) {
					sender.sendMessage(plugin.getConfig().getString("messages.command.chest.args.1"));
					return true;
					
				} 
				if (!(sender instanceof Player)) {
					sender.sendMessage(plugin.getConfig().getString("messages.command.chest.noconsole"));
					return true;
					
				}
				Player p = (Player) sender;
				if (args[1].equalsIgnoreCase("remove")) {
					if (args.length < 3) {
						sender.sendMessage("\u00a7c/survivor chest remove < under | cursor | id | <x y z> >");
						return true;
					}
					Location loc = null;
					if (args[2].equals("~") || args[2].equalsIgnoreCase("under")) {
						loc = p.getLocation();
						loc.setY(loc.getY() - 1);
					} else if (args[2].equals("cursor")) {
						Block b = p.getTargetBlock((Set<Material>)null, 64);
						if (b != null && ChestSpawner.getType(b.getType()) != null) {
							loc = b.getLocation();
						} else {
							sender.sendMessage("\u00a7cVous devez pointer un ChestSpawner !");
							return true;
						}
					} else if (args.length == 3) {
						try {
							int id = Integer.parseInt(args[2]);
							for (ChestSpawner c : Core.chestSpawners) {
								if (c.id == id) {
									loc = c.location;
									break;
								}
							}
							if (loc == null) {
								sender.sendMessage("\u00a7cL'id du ChestSpawner est invalide !");
								return true;
							}
						} catch (Exception e) {
							sender.sendMessage("\u00a7c/survivor chest remove < under | cursor | id | <x y z> >");
							return true;
						}
					} else if (args.length == 5) {
						try {
							int x = Integer.parseInt(args[2]);
							int y = Integer.parseInt(args[3]);
							int z = Integer.parseInt(args[4]);
							loc = new Location(Core.world, x, y, z);
						} catch (Exception e) {
							sender.sendMessage("\u00a7c/survivor chest remove < under | cursor | id | <x y z> >");
						}
					}
					if (loc == null) {
						sender.sendMessage("\u00a7cErreur interne");
					}
					for (ChestSpawner c : Core.chestSpawners) {
						if (c.location.equals(loc)) {
							this.plugin.getConfig().set("chestspawners." + c.id, null);
							this.plugin.saveConfig();
							sender.sendMessage("\u00a7aLe spawner n°" + c.id + "\u00a7aa, \u00a7ae" + (int) Math.floor(c.location.getX()) + " " + (int) Math.floor(c.location.getY()) + " " + (int) Math.floor(c.location.getZ()) + "\u00a7a de type \u00a7e" + c.type + "\u00a7a avec un délai de \u00a7e" + c.delay + " \u00a7aticks a bien été supprimé !");
							this.plugin.reloadChestSpawners();
							return true;
						}
					}
					sender.sendMessage("\u00a7cChestSpawner invalide !");
					return true;
				}
				if (args[1].equalsIgnoreCase("add")) {
					if (args.length < 4) {
						sender.sendMessage("\u00a7c/survivor chest add <type> <delayInTicks> [cursor]");
						return true;
					}
					try {
						int delay = Integer.parseInt(args[3]);
						ChestType type = ChestSpawner.getType(args[2]);
						int i = 1;
						if (this.plugin.getConfig().getConfigurationSection("chestspawners") != null) {
							for (String key : this.plugin.getConfig().getConfigurationSection("chestspawners").getKeys(false)) {
								try {
									 if (Integer.parseInt(key) > i) i = Integer.parseInt(key);
								} catch (ArithmeticException e){}
							}
						}
						i = i+1;
						Location l; 
						if (args.length > 4 && args[4].equalsIgnoreCase("cursor")) {
							Block b = p.getTargetBlock((Set<Material>)null, 64);
							if (b != null && !b.getType().equals(Material.AIR)) {
								l = b.getLocation();
							} else {
								sender.sendMessage("\u00a7cVous devez pointer un bloc !");
								return true;
							}
						} else {
							l = p.getLocation();
						}
						this.plugin.getConfig().set("chestspawners." + i + ".x", (int) Math.floor(l.getX()));
						this.plugin.getConfig().set("chestspawners." + i + ".y", (int) Math.floor(l.getY()));
						this.plugin.getConfig().set("chestspawners." + i + ".z", (int) Math.floor(l.getZ()));
						this.plugin.getConfig().set("chestspawners." + i + ".delay", delay * 20);
						this.plugin.getConfig().set("chestspawners." + i + ".type", type.toString());
						this.plugin.saveConfig();
						this.plugin.reloadChestSpawners();
						
					} catch (ArithmeticException e) {
						sender.sendMessage("\u00a7cDélai invalide !");
					}
				}
				return true;
			}
			return true;
			
		}
		if (cmd.getName().equals("randomtp")) {
			if (!sender.isOp()) { sender.sendMessage("\u00a7cVous devez être OP pour exécuter cette commande !"); return true; }
			if (!(sender instanceof Player)) { sender.sendMessage("\u00a7cVous devez être un joueur pour exécuter cette commande !"); return true; }
			Player p = (Player) sender;
			Location l = Core.getAvailableSpawnLocation(p.getLocation());
			p.sendMessage("\u00a7aTéléporté en \u00a7e" + l.getX() + " " + l.getY() + " " + l.getZ());
			p.teleport(l);
			return true;
		}
		if (cmd.getName().equals("speed")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("\u00a7cVous devez être un joueur pour exécuter cette commande !");
				return true;
			}
			if (!sender.isOp()) { sender.sendMessage("\u00a7cVous devez être OP pour exécuter cette commande !"); return true; }
			if (args.length < 1) {
				sender.sendMessage("\u00a7c/speed 0-10");
				return true;
			}
			Player p = (Player) sender;
			try {
				p.setFlySpeed((float) Double.parseDouble(args[0]) / 10);
				sender.sendMessage("\u00a7aSuccess");
			} catch (Exception e) {
				sender.sendMessage(e.getMessage());
				sender.sendMessage("\u00a7cNombre invalide");
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("svterritory")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("\u00a7cNo console!");
				return true;
			}
			Player p = (Player) sender;
			if (args.length == 0) {
				sender.sendMessage("/svterritory <add | list | tp>");
				return true;
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length < 3 || Core.getTerritoryClass(args[2]) == null) {
					sender.sendMessage("/svterritory add <name> <Mine | Ferme | Moulin>");
					return true;
				}
				Class<?> t = Core.getTerritoryClass(args[2]);
				Territory t1 = null;
				if (t.equals(Mine.class)) {
					if (Mine.mines.containsKey(args[1])) {
						sender.sendMessage("\u00a7cCette mine existe déjà !");
					} else {
						t1 = Mine.get(plugin, args[1]);
					}
				} else if (t.equals(Ferme.class)) {
					if (Ferme.fermes.containsKey(args[1])) {
						sender.sendMessage("\u00a7cCette ferme existe déjà !");
					} else {
						t1 = Ferme.get(plugin, args[1]);
					}
				} else {
					if (Moulin.moulins.containsKey(args[1])) {
						sender.sendMessage("\u00a7cCe moulin existe déjà !");
					} else {
						t1 = Moulin.get(plugin, args[1]);
					}
				}
				CraftVillager v = (CraftVillager) p.getWorld().spawnEntity(p.getLocation(), EntityType.VILLAGER);
				t1.setPNJ(v);
				v.setCustomName(t1.getName());
				v.setCustomNameVisible(true);
				v.setCanPickupItems(false);
				v.setRemoveWhenFarAway(false);
				v.setProfession(Profession.LIBRARIAN);
				EntityVillager nmsVillager = v.getHandle();
				Core.overwriteVillagerAI(nmsVillager);
				v.setHandle(nmsVillager);
				Core.rename.put(p.getName(), args[1]);
				p.sendMessage("\u00a7aEntrez le nouveau nom pour ce terrain :");
				return true;
			} else if (args[0].equalsIgnoreCase("tp")) {
				if (args.length < 2) {
					sender.sendMessage("/svterritory tp <territoryid>");
					return true;
				}
				if (!Territory.territories.containsKey(args[1])) {
					sender.sendMessage("\u00a7cID invalide !");
					return true;
				}
				Villager c = Territory.territories.get(args[1]).getPNJ(true);
				if (c == null) {
					p.sendMessage("\u00a7cUne erreur est survenue, le PNJ est introuvable !");
				} else {
					p.teleport(c);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length < 2) {
					sender.sendMessage("/svterritory remove <territoryid>");
					return true;
				}
				if (!Territory.territories.containsKey(args[1])) {
					sender.sendMessage("\u00a7cID invalide !");
					return true;
				}
				Territory.territories.get(args[1]).delete();
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				for (Entry<String, Territory> e : Territory.territories.entrySet()) {
					sender.sendMessage(e.getValue().getID() + " " + e.getValue().getName());
				}
				return true;
			}
		}
		if (cmd.getName().equalsIgnoreCase("svshop")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("IG only!");
				return true;
			}
			if (args.length == 0) {
				sender.sendMessage("/svshop <type>");
			} else {
				String name = args[0];
				name = ChatColor.translateAlternateColorCodes('&', name);
				ShopType t = ShopType.getByName(name);
				if (t == null) {
					sender.sendMessage("§cType invalide !");
				} else {
					CraftVillager v = (CraftVillager) ((Player)sender).getWorld().spawnEntity(((Player)sender).getLocation(), EntityType.VILLAGER);
					v.setCustomName(name);
					v.setCustomNameVisible(true);
					v.setCanPickupItems(false);
					v.setRemoveWhenFarAway(false);
					v.setProfession(Profession.FARMER);
					EntityVillager nmsVillager = v.getHandle();
					Core.overwriteVillagerAI(nmsVillager);
					v.setHandle(nmsVillager);
					sender.sendMessage("§aSuccess !");
				}
			}
			return true;
				
		}
		/*if (cmd.getName().equals("set")) {
			if (args.length < 2) {
				sender.sendMessage("/set relDir relDis");
				return true;
			}
			Location l = new Location(Core.world, 0, 3, 0);
			float yaw = ((Player) sender).getLocation().getYaw();
			Point a = new Point(new DegreeCoordinate(47.646742), new DegreeCoordinate(-2.080897));
			Point b = new Point(new DegreeCoordinate(Double.parseDouble(args[0])), new DegreeCoordinate(Double.parseDouble(args[1])));
			double bearing = EarthCalc.getBearing(a, b);
			int dst = (int) EarthCalc.getDistance(a, b);
			l.setYaw((float) bearing + 155);
			((Player) sender).teleport(l);
			Location la = Core.getBlockAtDistance((Player) sender, dst, false);
			la.setX(la.getX() + 0.5);
			la.setZ(la.getZ() + 0.5);
			la.setYaw(yaw);
			((Player) sender).teleport(la);
			return true;
		}*/
		sender.sendMessage(cmd.getName());
		return false;
	}

}
