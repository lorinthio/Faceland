package me.Lorinth.BossApi.Abilities;

import me.Lorinth.BossApi.BossApi;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Ignite extends Action{

	TargetType type;
	double duration;
	double damagePerTick;
	double radius;
	
	public Ignite(TargetType type, double duration){
		this.type = type;
		this.duration = duration;
		this.damagePerTick = damagePerTick;
	}
	
	public Ignite(TargetType type, double radius, double duration){
		this.type = type;
		this.radius = radius;
		this.duration = duration;
		this.damagePerTick = damagePerTick;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			LivingEntity living = ent.getTarget();
			living.setFireTicks((int) (duration * 20));
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
				p.setFireTicks((int) (duration * 20));
			}
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : getNearbyCreatures(radius, ent)){
				c.setFireTicks((int) (duration * 20));
			}
		}
		else if(type == TargetType.Self){
			ent.setFireTicks((int) (duration * 20));
		}
	}
	
}
