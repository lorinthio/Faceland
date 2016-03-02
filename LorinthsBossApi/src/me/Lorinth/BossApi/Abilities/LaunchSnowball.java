package me.Lorinth.BossApi.Abilities;

import me.Lorinth.BossApi.BossApi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class LaunchSnowball extends Action{

	double damage;
	double speed;
	TargetType type;
	double radius;
	
	public LaunchSnowball(TargetType type, double damage, double speed){
		this.type = type;
		this.damage = damage;
		this.speed = speed;
	}
	
	public LaunchSnowball(TargetType type, double damage, double speed, double radius){
		this.type = type;
		this.damage = damage;
		this.speed = speed;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			final Snowball ball = ent.launchProjectile(Snowball.class);
			ball.setShooter(ent);
			Vector vec = getVelocity(ent, ent.getTarget());
			ball.setVelocity(new Vector(vec.getX(), 0, vec.getZ()));
			
			BossApi.getPlugin().projectiles.put(ball, (int) damage);
			
			SnowballTask task = new SnowballTask(ball, vec);
			task.runTaskTimer(BossApi.getPlugin(), 0, 1);
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : this.getNearbyCreatures(radius, ent)){
				final Snowball ball = ent.launchProjectile(Snowball.class);
				ball.setShooter(ent);
				Vector vec = getVelocity(ent, c);
				ball.setVelocity(new Vector(vec.getX(), 0, vec.getZ()));
				
				BossApi.getPlugin().projectiles.put(ball, (int) damage);
				
				SnowballTask task = new SnowballTask(ball, vec);
				task.runTaskTimer(BossApi.getPlugin(), 0, 1);
			}
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : this.getNearbyPlayers(radius, ent)){
				final Snowball ball = ent.launchProjectile(Snowball.class);
				ball.setShooter(ent);
				Vector vec = getVelocity(ent, p);
				ball.setVelocity(new Vector(vec.getX(), 0, vec.getZ()));
				
				BossApi.getPlugin().projectiles.put(ball, (int) damage);
				
				SnowballTask task = new SnowballTask(ball, vec);
				task.runTaskTimer(BossApi.getPlugin(), 0, 1);
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
