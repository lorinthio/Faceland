package me.Lorinth.BossApi.Abilities;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.util.Vector;

import me.Lorinth.BossApi.BossApi;

public class LaunchShulkerBullet extends Action{

	double damage;
	TargetType type;
	double radius;

	public LaunchShulkerBullet(TargetType type, double damage){
		this.type = type;
		this.damage = damage;
	}

	public LaunchShulkerBullet(TargetType type, double damage, double radius){
		this.type = type;
		this.damage = damage;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		
		if(type == TargetType.Target){
			ShulkerBullet bullet = ent.launchProjectile(ShulkerBullet.class);
			bullet.setVelocity(new Vector(0, 0, 0));
			bullet.setShooter(ent);
			bullet.setTarget(ent.getTarget());
			
			BossApi.getPlugin().projectiles.put(bullet, (int) damage);
		}
		else if(type == TargetType.AoeCreatures){
			for(Creature c : this.getNearbyCreatures(radius, ent)){
                ShulkerBullet bullet = ent.launchProjectile(ShulkerBullet.class);
                bullet.setVelocity(new Vector(0, 0, 0));
                bullet.setShooter(ent);
                bullet.setTarget(c);
				
				BossApi.getPlugin().projectiles.put(bullet, (int) damage);
			}
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : this.getNearbyPlayers(radius, ent)){
                ShulkerBullet bullet = ent.launchProjectile(ShulkerBullet.class);
                bullet.setVelocity(new Vector(0, 0, 0));
                bullet.setShooter(ent);
                bullet.setTarget(p);

                BossApi.getPlugin().projectiles.put(bullet, (int) damage);
			}
		}
		
	}
	
}
