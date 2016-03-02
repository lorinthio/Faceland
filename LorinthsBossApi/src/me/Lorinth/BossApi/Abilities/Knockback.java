package me.Lorinth.BossApi.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Knockback extends Action{

	TargetType type;
	double horiz;
	double vertical;
	double radius;
	
	//Single target
	public Knockback(double horizontalStrength, double verticalStrength){
		this.type = TargetType.Target;
		horiz = horizontalStrength;
		vertical = verticalStrength;
	}
	
	public Knockback(TargetType type, double radius, double horizontalStrength, double verticalStrength){
		this.radius = radius;
		this.type = type;
		horiz = horizontalStrength;
		vertical = verticalStrength;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			LivingEntity targ = ent.getTarget();
			Vector vec = getVelocity(ent, targ);
			targ.setVelocity(vec);
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
				p.setVelocity(getVelocity(ent, p));
			}
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : getNearbyCreatures(radius, ent)){
				c.setVelocity(getVelocity(ent, c));
			}
		}
		
	}
	
	public Vector getVelocity(LivingEntity from, LivingEntity to){
		Vector vec = to.getLocation().toVector().subtract(from.getLocation().toVector());
		vec = vec.normalize();
		vec.setY(1);
		vec = vec.multiply(new Vector(horiz, vertical, horiz));
		return vec;
	}
	
}
