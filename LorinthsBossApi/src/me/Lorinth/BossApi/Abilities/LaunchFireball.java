package me.Lorinth.BossApi.Abilities;

import me.Lorinth.BossApi.BossApi;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;

public class LaunchFireball extends Action{

	double damage;
	double speed;
	TargetType type;
	double radius;
	
	public LaunchFireball(TargetType type, double damage, double speed){
		this.type = type;
		this.damage = damage;
		this.speed = speed;
	}
	
	public LaunchFireball(TargetType type, double damage, double speed, double radius){
		this.type = type;
		this.damage = damage;
		this.speed = speed;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		//System.out.println("Fireball has target, " + type.toString());
		
		if(type == TargetType.Target){
			SmallFireball ball = ent.launchProjectile(SmallFireball.class);
			ball.setIsIncendiary(false);
			ball.setShooter(ent);
			ball.setVelocity(getVelocity(ent, ent.getTarget()));
			
			BossApi.getPlugin().projectiles.put(ball, (int) damage);
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : this.getNearbyCreatures(radius, ent)){
				SmallFireball ball = ent.launchProjectile(SmallFireball.class);
				ball.setIsIncendiary(false);
				ball.setShooter(ent);
				ball.setVelocity(getVelocity(ent, c));
				
				BossApi.getPlugin().projectiles.put(ball, (int) damage);
			}
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : this.getNearbyPlayers(radius, ent)){
				SmallFireball ball = ent.launchProjectile(SmallFireball.class);
				ball.setIsIncendiary(false);
				ball.setShooter(ent);
				ball.setVelocity(getVelocity(ent, p));
				
				BossApi.getPlugin().projectiles.put(ball, (int) damage);
			}
		}
		
	}
	
	public Vector getVelocity(LivingEntity from, LivingEntity to){
		try{
			Vector vec = to.getLocation().toVector().subtract(from.getLocation().toVector());
			vec = vec.normalize();
			vec = vec.multiply(new Vector(speed, speed, speed));
			return vec;
		}
		catch(NullPointerException e){
			Vector vec =  from.getLocation().getDirection();
			vec = vec.normalize();
			vec = vec.multiply(new Vector(speed, speed, speed));
			return vec;
		}
	}
	
}
