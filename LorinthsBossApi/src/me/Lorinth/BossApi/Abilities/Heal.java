package me.Lorinth.BossApi.Abilities;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Heal extends Action{

	TargetType type;
	double heal_amount;
	double radius;
	
	public Heal(TargetType t, double amount){
		type = t;
		heal_amount = amount;
	}
	
	public Heal(TargetType t, double amount, double radius){
		type = TargetType.AoeCreatures;
		heal_amount = amount;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			LivingEntity targ = ent.getTarget();
			if (targ.getHealth() > 0) {
                targ.setHealth(Math.min(targ.getMaxHealth(), targ.getHealth() + heal_amount));
            }
			return;
		}

		if(type == TargetType.Self){
            if (ent.getHealth() > 0) {
                ent.setHealth(Math.min(ent.getMaxHealth(), ent.getHealth() + heal_amount));
            }
            return;
		}

		if(type == TargetType.AoeCreatures){
			for(Creature c : getNearbyCreatures(radius, ent)){
                if (c.getHealth() > 0) {
                    c.setHealth(Math.min(c.getMaxHealth(), c.getHealth() + heal_amount));
                }
			}
            return;
		}

		if(type == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
                if (p.getHealth() > 0) {
                    p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + heal_amount));
                }
			}
		}
		
	}
	
}
