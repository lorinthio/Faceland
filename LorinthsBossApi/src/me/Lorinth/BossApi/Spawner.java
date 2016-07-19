package me.Lorinth.BossApi;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Spawner {
    private final Spawner spawner;
    private BossApi api;
    private int spawnTaskId = 0;

    public BossInstance bossInstance;
	public Boss boss;
	public long respawnDelay;
    public int resetCount = 0;
	public double maxDistance;
	public double maxDistSqr;
	public Location loc;

	public String key;

	public Spawner(String key, BossApi api, Boss boss, Location loc, long delay, double distance) {
		System.out.println("Loading, " + key);
		this.key = key;
		this.api = api;
		this.boss = boss;
		this.loc = loc;
		this.respawnDelay = delay;
		this.maxDistance = distance;
		this.maxDistSqr = ((distance + 1.0D) * (distance + 1.0D));
		this.spawner = this;
	}

	public void save(String key)
	{
		File spawnersFile = new File(api.getDataFolder(), "Spawners.yml");
		FileConfiguration spawners = new YamlConfiguration();
		
		try {
			spawners.load(spawnersFile);
		} catch (IOException | InvalidConfigurationException e1) {
			// TODO Auto-generated catch block
			System.out.println("Cannot find file Spawners.yml");
			
			e1.printStackTrace();
		}
		
		spawners.set(key + ".Location.world", this.loc.getWorld().getName());
		spawners.set(key + ".Location.x", Integer.valueOf(this.loc.getBlockX()));
		spawners.set(key + ".Location.y", Integer.valueOf(this.loc.getBlockY()));
		spawners.set(key + ".Location.z", Integer.valueOf(this.loc.getBlockZ()));
		spawners.set(key + ".Boss", this.boss.key);
		spawners.set(key + ".RespawnDelay", Long.valueOf(this.respawnDelay));
		spawners.set(key + ".MaxDistance", Double.valueOf(this.maxDistance));
		
		try
		{
			spawners.save(spawnersFile);
		}
		catch (IOException e)
		{
			System.out.println("Failed to save spawner...");
			
			e.printStackTrace();
		}
	}
    public BossInstance getBoss() {
        return this.bossInstance;
    }

    public double getRadius() {
        return this.maxDistance;
    }

    public void setResetCount(int amount) {
        if (amount > 3) {
            killBoss();
            spawnBoss();
            amount = 0;
        }
        this.resetCount = amount;
    }

    public int getResetCount() {
        return  this.resetCount;
    }

    public boolean isInChunk(Chunk chunk) {
        return this.loc.getChunk() == chunk;
    }

    public void spawnBoss() {
        this.bossInstance = this.boss.spawn(this.loc, true);
        this.bossInstance.spawner = this.spawner;
    }

    public void killBoss() {
        api.bossEntities.remove(this.getBoss().bossEntity);
        this.getBoss().bossEntity.remove();
    }

    public void bossDied() {
        this.spawnTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable() {
            public void run() {
                if (spawner.loc.getChunk().isLoaded()) {
                    spawner.spawnBoss();
                }
            }
        }, this.respawnDelay * 20L);
    }

    public boolean isSpawnTaskRunning() {
        return Bukkit.getScheduler().isCurrentlyRunning(this.spawnTaskId);
    }

    public void stopSpawning() {
        this.resetCount = 0;
        if (getBoss() != null) {
            if (getBoss().mount != null) {
                getBoss().mount.bossEntity.remove();
            }
            killBoss();
        }
    }
}