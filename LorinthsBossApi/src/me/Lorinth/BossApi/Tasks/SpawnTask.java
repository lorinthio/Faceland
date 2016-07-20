package me.Lorinth.BossApi.Tasks;

import org.bukkit.scheduler.BukkitRunnable;

import me.Lorinth.BossApi.BossApi;
import me.Lorinth.BossApi.Spawner;

public class SpawnTask extends BukkitRunnable {

    private BossApi api;

	public SpawnTask(BossApi api) {
        this.api = api;

	}

	@Override
	public void run() {
        for (Spawner s : api.getSpawners()) {
            if (s.getBoss().getBossEntity().isValid()) {
                if (s.getRadius() == -1) {
                    continue;
                }
                if (s.loc.distance(s.getBoss().getBossEntity().getLocation()) > s.getRadius()) {
                    s.getBoss().getBossEntity().teleport(s.loc);
                    s.setResetCount(s.getResetCount() + 1);
                }
            } else {
                if (s.respawnDelay < System.currentTimeMillis() - s.bossDeathTime) {
                    continue;
                }
                if (!s.loc.getChunk().isLoaded()) {
                    continue;
                }
                s.killBoss();
                s.spawnBoss();
            }
        }
	}
	
}
