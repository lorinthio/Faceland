package me.Lorinth.BossApi.Abilities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;

public class Speak extends Action{

	TargetType type;
	String message;
	double radius;
	
	public Speak(String line, TargetType type){
		this.type = type;
		message = line;
	}
	
	public Speak(String line, double radius){
		type = TargetType.AoePlayers;
		message = line;
		this.radius = radius;
	}
	
	@Override
	public void execute(Creature ent){
		if(type == TargetType.Target){
			if(ent.getTarget() instanceof Player){
				((Player)ent.getTarget()).sendMessage(message);
			}
		}
		else if(type == TargetType.AoePlayers){
			for(Player p : getNearbyPlayers(radius, ent)){
				p.sendMessage(message);
			}
		}
		else if(type == TargetType.Self){
			Bukkit.getConsoleSender().sendMessage(message);
		}
	}
	
}
