package me.Lorinth.BossApi;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleTask extends BukkitRunnable {

	double radius;
	double count;
	Effect effect;
	int data;
	LivingEntity ent;
	
	Random r = new Random();
	
	public ParticleTask(LivingEntity ent, Effect particle, double effectRadius, double effectCount, int data) {
		this.ent = ent;
		effect = particle;
		count = effectCount;
		radius = effectRadius;
		this.data = data;
	}

	@Override
	public void run() {
		if(!ent.isDead() && ent.isValid()){
			World w = ent.getWorld();
			w.spigot().playEffect(ent.getLocation(), effect, r.nextInt(1000), data, 0.1F, 0.1F, 0.1F, 0.05F, (int) count, 32);
		}
		else{
			this.cancel();
		}
	}
	
}
