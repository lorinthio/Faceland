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

public class Spawner
{
	public Boss b;
	public Entity ent;
	public long respawnDelay;
	public double maxDistance;
	public double maxDistSqr;
	public Location loc;
	Chunk c;
	boolean loaded = false;
	final Spawner spawner;
	public BossInstance bi;
	private BossApi api;
	private int spawnTaskId = 0;
	private int distTaskId = 0;
	
	public String key;

	public Spawner(String key, BossApi api, Boss b, Location loc, long delay, double distance)
	{
		System.out.println("Loading, " + key);
		this.key = key;
		this.api = api;
		this.b = b;
		this.loc = loc;
		this.c = loc.getChunk();
		this.loaded = c.isLoaded();
		this.respawnDelay = delay;
		this.maxDistance = distance;
		this.maxDistSqr = ((distance + 1.0D) * (distance + 1.0D));
		this.spawner = this;

		spawn();
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
		spawners.set(key + ".Boss", this.b.key);
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

	public void unLoad(boolean respawn)
	{
		//System.out.println("Unloading, " + key);
		Bukkit.getScheduler().cancelTask(this.distTaskId);
		distTaskId = 0;
		Bukkit.getScheduler().cancelTask(this.spawnTaskId);
		spawnTaskId = 0;
		if(respawn){
			if(ent != null){
				notifyDeath((Creature) this.ent);
			}
			else{
				this.spawnTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable()
				{
					public void run()
					{
						spawner.spawn();
					}
				}, this.respawnDelay * 20L);
			}
		}
		else{
			if(ent != null){
				if(bi != null){
					if(bi.mount != null){
						bi.mount.ent.remove();
					}
				}
				
				this.ent.remove();
				bi = null;
			}
		}
		this.loaded = false;
	}

	public void reLoad()
	{
		//System.out.println("loading, " + key);
		
		if(bi != null){
			if(bi.mount != null){
				bi.mount.ent.remove();
			}
			bi.ent.remove();
			
			
		}
		else if(ent != null){
			ent.remove();
		}
		
		
		this.loaded = true;
		if(this.spawnTaskId != 0){
			Bukkit.getScheduler().cancelTask(spawnTaskId);
			spawnTaskId = 0;
		}
		spawn();
	}

	public boolean notifyDeath(Creature ent)
	{
		//System.out.println("Death notified, " + key);
		try
		{
			if (ent == this.ent)
			{
				//System.out.println("Entity was spawner entity!");
				Bukkit.getScheduler().cancelTask(this.distTaskId);
				distTaskId = 0;
				
				this.bi = null;
				this.spawnTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable()
				{
					public void run()
					{
						spawner.spawn();
					}
				}, this.respawnDelay * 20L);
				
				return true;
			}
		}
		catch (NullPointerException localNullPointerException) {
			
			System.out.println("*****ERROR for spawner, " + key + "*****");
			//System.out.println(ent.toString());
			//System.out.println(bi.toString());
		}
		return false;
	}

	public void spawn()
	{	
		if (this.loaded && loc.getChunk().isLoaded())
		{
			boolean playerFound = false;
			
			Entity arrow = loc.getWorld().spawn(loc, Arrow.class);
			for(Entity ent : arrow.getNearbyEntities(30, 30, 30)){
				if(ent instanceof Player){
					playerFound = true;
				}
			}
			arrow.remove();
			
			if(playerFound){
				
				//System.out.println("Player in Range!");
				
				if(bi != null){
					if(bi.mount != null){
						bi.mount.ent.remove();
					}
					bi.ent.remove();
				}
				else if(ent != null){
					ent.remove();
				}
				
				this.bi = this.b.spawn(this.loc, true);
				this.ent = this.bi.ent;
	
				final BossInstance bI = this.bi;
				final Location spawnerLoc = this.loc;
				this.distTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(BossApi.getPlugin(), new Runnable()
				{
					public void run()
					{
						if (bI.ent.isValid() && !bI.ent.isDead())
						{
							double distance = bI.ent.getLocation().distanceSquared(spawnerLoc);
							if (distance > maxDistSqr)
							{
								ent.teleport(loc);
								((Creature)ent).setTarget(null);
								
								if(bi != null){
									BossInstance last = bi;
									BossInstance mount = bi.mount;
									while(mount != null && last != null){
										if(mount.ent.isValid() && !mount.ent.isDead()){
											mount.ent.teleport(loc);
											if(mount.ent instanceof Creature){
												((Creature)mount.ent).setTarget(null);
											}
											mount.ent.setPassenger(last.ent);
										}
										last = mount;
										mount = mount.mount;
										
									}
								}
							}
						}
						else
						{
							//System.out.println("Valid = " + ent.isValid() + ", Dead = " + ent.isDead());
							
							Spawner.this.spawner.notifyDeath((Creature) bI.ent);
						}
					}
				}, 100L, 100L);
			}
			else{
				spawnTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable()
				{
					public void run()
					{
						Spawner.this.spawn();
					}
				}, 200L);
			}
		}
		else
		{
			spawnTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable()
			{
				public void run()
				{
					Spawner.this.spawn();
				}
			}, 200L);
		}
	}
}