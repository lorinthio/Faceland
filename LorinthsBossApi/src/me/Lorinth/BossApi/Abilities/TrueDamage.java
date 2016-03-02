package me.Lorinth.BossApi.Abilities;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class TrueDamage extends Action{
	TargetType type;
	double damage_amount;
	double radius;
	
	public TrueDamage(TargetType t, double amount){
		type = t;
		damage_amount = amount;
	}
	
	public TrueDamage(TargetType t, double amount, double radius){
		type = t;
		damage_amount = amount;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			LivingEntity targ = ent.getTarget();
			targ.setHealth(Math.max(0, targ.getHealth() - damage_amount));
			
		}
		else if(type == TargetType.Self){
			ent.setHealth(Math.max(0, ent.getHealth() - damage_amount));
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
				p.setHealth(Math.max(0, p.getHealth() - damage_amount));
			}
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : getNearbyCreatures(radius, ent)){
				c.setHealth(Math.max(0, c.getHealth() - damage_amount));
			}
		}
	}
}
