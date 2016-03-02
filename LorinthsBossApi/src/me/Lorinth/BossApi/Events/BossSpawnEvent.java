package me.Lorinth.BossApi.Events;

import me.Lorinth.BossApi.BossInstance;

import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BossSpawnEvent extends Event{
		
	private BossInstance bi;
	private LivingEntity ent;
	private World world;
	
	private boolean cancelled;
	private static final HandlerList handlers = new HandlerList();
	
	public BossSpawnEvent(BossInstance bi, LivingEntity ent){
		this.bi = bi;
		this.ent = ent;
		world = ent.getWorld();
	}
	
	public BossInstance getBossInstance(){
		return bi;
	}
	
	public LivingEntity getLivingEntity(){
		return ent;
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
