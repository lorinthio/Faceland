package me.Lorinth.BossApi.Abilities;

import org.bukkit.Location;
import org.bukkit.entity.Snowball;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SnowballTask extends BukkitRunnable{

	Snowball ball;
	Vector vec;
	Location loc;
	
	public SnowballTask(Snowball ball, Vector direction){
		this.ball = ball;
		this.vec = direction;
		ball.setVelocity(vec);
	}
	
	@Override
	public void run() {
		if(!ball.isDead()){
			ball.setVelocity(vec);
		}
		else{
			this.cancel();
		}
	}

}
