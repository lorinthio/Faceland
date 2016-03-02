package me.Lorinth.BossApi.Events;

import me.Lorinth.BossApi.BossInstance;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BossDeathEvent extends Event{
	
	private BossInstance bi;
	private Entity ent;
	private World world;
	
	private boolean cancelled;
	private static final HandlerList handlers = new HandlerList();
	
	public BossDeathEvent(BossInstance bi, Entity ent){
		this.bi = bi;
		this.ent = ent;
		world = ent.getWorld();
	}
	
	public BossInstance getBossInstance(){
		return bi;
	}
	
	public Entity getLivingEntity(){
		return ent;
	}
	
	public Location getLocation(){
		return ent.getLocation();
	}
	
	public World getWorld(){
		return world;
	}
	
    public boolean isCancelled() {
        return cancelled;
    }
 
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
}
