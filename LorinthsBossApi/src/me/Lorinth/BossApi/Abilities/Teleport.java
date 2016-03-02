package me.Lorinth.BossApi.Abilities;

import org.bukkit.Location;
import org.bukkit.entity.Creature;

public class Teleport extends Action{

	Location loc;
	
	public Teleport(Location loc){
		this.loc = loc;
	}
	
	@Override
	public void execute(Creature ent){
		//Entity doesn't change worlds
		loc.setWorld(ent.getWorld());
		ent.teleport(loc);
	}
	
}
