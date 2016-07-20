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
            if (s.bossInstance == null) {
                // Just flat out ignore attempts to spawn in unloaded chunks lol
                if (!s.loc.getChunk().isLoaded()) {
                    continue;
                }
                // Uses the time the boss was last slain to determine when it should be spawned again
                // bossDeathTime defaults to when the server starts. If it is never set, or the monster
                // is killed by a command, it will still spawn a boss, because the timer just wasn't
                // updated when it died!
                long msToSpawn = (s.respawnDelay * 1000) - (System.currentTimeMillis() - s.bossDeathTime);
                if (msToSpawn > 0) {
                    continue;
                }
                // Kills boss (with .remove) and spawns a new one, 'resetting' it.
                s.killBoss();
                s.spawnBoss();
            } else {
                // If for SOME reason, the boss instance isn't set but the boss exists, kill it and try later
                if (!s.bossInstance.getBossEntity().isValid()) {
                    s.setBoss(null);
                    continue;
                }
                // Allowing the option to set a -1 radius to ignore radius checks
                if (s.getRadius() == -1) {
                    continue;
                }
                // A 100% accurate way of resetting bosses after they are reset 3 times!
                if (s.loc.distance(s.getBoss().getBossEntity().getLocation()) > s.getRadius()) {
                    s.getBoss().getBossEntity().teleport(s.loc);
                    s.setResetCount(s.getResetCount() + 1);
                }
            }
        }
	}
	
}
