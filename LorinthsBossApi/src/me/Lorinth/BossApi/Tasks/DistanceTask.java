package me.Lorinth.BossApi.Tasks;

import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import me.Lorinth.BossApi.BossApi;
import me.Lorinth.BossApi.BossInstance;
import me.Lorinth.BossApi.Spawner;

public class DistanceTask extends BukkitRunnable {

    private BossApi api;
    private BossInstance bossInstance;

	public DistanceTask(BossApi api) {
        this.api = api;

	}

	@Override
	public void run() {
        for (Spawner s : api.getSpawners()) {
            World w = s.loc.getWorld();
            Arrow scanner = (Arrow) w.spawnEntity(s.loc, EntityType.ARROW);
            for (Entity e : scanner.getNearbyEntities(s.getRadius(), s.getRadius(), s.getRadius())) {
                if (api.isBoss(e)) {
                    if (s.getBoss() == bossInstance.getBossEntity()) {
                        e.teleport(s.loc);
                        s.setResetCount(s.getResetCount() + 1);
                    }
                    return;
                }
            }
        }
	}
	
}
