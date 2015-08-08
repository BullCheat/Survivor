package fr.adrean.Survivor;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class FloatingText {
	
	long duration;
	BukkitTask task = null;
	ArmorStand stand;
	
	public FloatingText(Location location, String text, int duration, Core plugin) {
		this.duration = duration;
		this.stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
		this.stand.setCustomNameVisible(true);
		this.stand.setCustomName(text);
		this.stand.setGravity(false);
		this.stand.setCanPickupItems(false);
		this.stand.setMaximumNoDamageTicks(Integer.MAX_VALUE);
		this.stand.setNoDamageTicks(Integer.MAX_VALUE);
		this.stand.setVisible(false);
		this.stand.setSmall(true);
		this.stand.setRemoveWhenFarAway(false);
		
		if (duration < Integer.MAX_VALUE) {
			task = new BukkitRunnable() {
				@Override
				public void run() {
					decrease();
					if (getDuration() < 1) {
						delete();
					}
				}
			}.runTaskTimer(plugin, 1, 1);
		}
	}
	
	public void decrease() {
		this.duration--;
	}
	public long getDuration() {
		return this.duration;
	}
	public void delete() {
		task.cancel();
		this.stand.remove();
	}
}
