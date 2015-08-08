package fr.adrean.Survivor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

public class ChestSpawner implements Listener {

	public Location location;
	public int id;
	public int delay;
	public ChestType type;
	private boolean enabled;
	private boolean opened;
	private Core plugin;
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param id
	 * @param delay
	 * @param type
	 */
	public ChestSpawner(int x, int y, int z, int id, int delay, ChestType type, Core core) {
		this.location = new Location(Core.world, x, y, z);
		this.id = id;
		this.delay = delay;
		this.type = type;
		this.plugin = core;
		Bukkit.getPluginManager().registerEvents(this, core);
	}
	
	public boolean enable() {
		if (this.enabled == true) return false;
		this.enabled = true;
		return true;
	}
	
	public boolean disable() {
		if (this.enabled == false) return false;
		this.enabled = false;
		return true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public Material getMaterial() {
		return getMaterial(this.type);
	}
	
	public void spawn() {
		this.opened = false;
		this.location.getBlock().setType(this.getMaterial());
		Chest b = (Chest) this.location.getBlock().getState();
		ArrayList<Integer> ia = new ArrayList<Integer>();
		for (int i = 0; i < b.getBlockInventory().getSize(); i++) {
			ia.add(i);
		}
		Collections.shuffle(ia);
		double rand = new Random().nextInt(4);
		for (int i = 0; i < 2 + (int) rand; i++) {
			b.getInventory().setItem((int) ia.toArray()[i], plugin.getRandomChestItem(this.type));
		}
		Location l = b.getLocation();
		//Spawn the Firework, get the FireworkMeta.
        //Firework fw = (Firework) l.getWorld().spawnEntity(l, EntityType.FIREWORK);
        //FireworkMeta fwm = fw.getFireworkMeta();
       
        //Our random generator
        Type type = Type.BALL;      
       
        //Get our random colours
        Color c1;
        Color c2;
        if (this.type == ChestType.NORMAL) {
        	c1 = Color.YELLOW;
        	c2 = Color.ORANGE;
        } else {
        	c1 = Color.RED;
        	c2 = Color.BLACK;
        }
        
        //Create our effect with this
        FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(c1).withFade(c2).with(type).trail(true).build();
        l.setX(l.getX() + 0.5);
        l.setY(l.getY() + 0.5);
        l.setZ(l.getZ() + 0.5);
        CustomEntityFirework_1_8_3_R02.spawn(l, effect);
        //Then apply the effect to the meta
        //fwm.addEffect(effect);
       
        //Generate some random power and set it
        //fwm.setPower(2);
       
        //Then apply this to our rocket
        //fw.setFireworkMeta(fwm);          
		return;
	}
	
	public void open(Player p) {
		if (this.opened) return;
		this.opened = true;
		Location l = this.location.clone();
	    Type type = Type.BALL;      
	    //Get our random colours
	    Color c1 = Color.GREEN;
	    Color c2 = Color.LIME;
	    if (this.type == ChestType.NORMAL) {
	    	p.setExp(p.getExp() + 0.10F);
	    	p.sendMessage("\u00a7a+10\u00a77 exp");
	    } else {
	    	p.setExp(p.getExp() + 0.50F);
	    	p.sendMessage("\u00a7a+50\u00a77 exp");
	    }
    	if (p.getExp() > 1) {
    		p.setLevel(p.getLevel() + (int) p.getExp());
    		p.setExp(p.getExp() - (int) p.getExp());
    	}
	    
	    //Create our effect with this
	    FireworkEffect effect = FireworkEffect.builder().flicker(true).withColor(c1).withFade(c2).with(type).trail(true).build();
	    l.setX(l.getX() + 0.5);
	    l.setY(l.getY() + 0.5);
	    l.setZ(l.getZ() + 0.5);
	    CustomEntityFirework_1_8_3_R02.spawn(l, effect);
		
	}
	
	public boolean deSpawn(boolean drop) {
		if (this.location.getBlock() == null || this.location.getBlock().isEmpty()) return false;
		if (!drop && this.location.getBlock().getType().equals(Material.CHEST) || this.location.getBlock().getType().equals(Material.TRAPPED_CHEST)) {
			Chest b = (Chest) this.location.getBlock().getState();
			b.getInventory().setContents(new ItemStack[b.getInventory().getSize()]);
		}
		this.location.getBlock().breakNaturally();
		return true;
	}

	public static ChestType getType(String name) {
		if (name.equalsIgnoreCase("magic")) return ChestType.MAGIC;
		if (name.equalsIgnoreCase("normal")) return ChestType.NORMAL;
		return null;
	}
	
	public static ChestType getType(Material material) {
		if (material.equals(Material.TRAPPED_CHEST)) return ChestType.MAGIC;
		if (material.equals(Material.CHEST)) return ChestType.NORMAL;
		return null;
	}
	
	public static Material getMaterial(ChestType type) {
		if (type.equals(ChestType.NORMAL)) return Material.CHEST;
		if (type.equals(ChestType.MAGIC)) return Material.TRAPPED_CHEST;
		return Material.AIR;
	}
	
	public enum ChestType {
		NORMAL,
		MAGIC
	}

	public void schedule() {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				if (enabled) {
					spawn();
				}
			}
		}, this.delay);		
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerOpenChest(InventoryOpenEvent e) {if (e.getPlayer() instanceof Player && e.getInventory().getHolder() instanceof CraftChest && ((CraftChest) e.getInventory().getHolder()).equals(this.location.getBlock().getState())) {
			open((Player) e.getPlayer());
		}
	}
	
}
