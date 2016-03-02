package me.Lorinth.BossApi.Abilities;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectAction extends Action{

	TargetType target;
	PotionEffectType type;
	double duration;
	double intensity;
	double radius;
	
	public PotionEffectAction(PotionEffectType type, double duration2, double intensity2, TargetType targ){
		this.type = type;
		this.duration = duration2;
		this.intensity = intensity2;
		target = targ;
	}
	
	public PotionEffectAction(PotionEffectType type, double duration, double intensity, TargetType targ, double radius){
		target = targ;
		this.type = type;
		this.duration = duration;
		this.intensity = intensity;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		if(target == TargetType.Self){
			ent.addPotionEffect(new PotionEffect(type, (int) (20*duration), (int) intensity, false, true), true);
		}
		else if(target == TargetType.Target){
			ent.getTarget().addPotionEffect(new PotionEffect(type, (int) (20*duration), (int) intensity, false, true), true);
		}
		else if(target == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
				p.addPotionEffect(new PotionEffect(type, (int) (20*duration), (int) intensity, false, true), true);
			}
		}
		else if(target == TargetType.AoeCreatures){
			for (Creature c : getNearbyCreatures(radius, ent)){
				c.addPotionEffect(new PotionEffect(type, (int) (20*duration), (int) intensity, false, true), true);
			}
		}
	}
	
}
