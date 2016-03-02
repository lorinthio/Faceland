package me.Lorinth.BossApi.Abilities;

import me.Lorinth.BossApi.Boss;
import me.Lorinth.BossApi.BossApi;

import org.bukkit.World;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;

public class Summon extends Action{

	EntityType type;
	Integer count;
	double radius;
	
	Boss b;
	String bossname;
	
	public Summon(EntityType type, Integer number){
		this.type = type;
		count = number;
	}
	
	public Summon(String bossname, Integer count){
		this.bossname = bossname;
		this.count = count;
	}

	@Override
	public void execute(Creature ent){
		World w = ent.getWorld();
		for(int i=0; i<count; i++){
			if(type != null){
				w.spawnEntity(ent.getLocation(), type);
			}
			else{
				if(b == null){
					b = BossApi.getPlugin().bossNames.get(bossname);
				}
				//System.out.println("Trying to spawn, " + b.name);
				b.spawn(ent.getLocation(), false);
			}
		}
	}
	
}
