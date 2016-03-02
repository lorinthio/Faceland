package me.Lorinth.BossApi.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Action {

	public void execute(Creature ent){
		
	}
	
	public List<Player> getNearbyPlayers(double radius, Creature ent){
		List<Player> players = new ArrayList<Player>();
		for(Entity e : ent.getNearbyEntities(radius, radius, radius)){
			if(e instanceof Player){
				if(((Player)e).getGameMode() == GameMode.SURVIVAL){
					players.add((Player) e);
				}
			}
		}
		return players;
	}
	
	public List<Creature> getNearbyCreatures(double radius, Creature ent){
		List<Creature> creatures = new ArrayList<Creature>();
		for(Entity e : ent.getNearbyEntities(radius, radius, radius)){
			if(e instanceof Creature){
				creatures.add((Creature) e);
			}
		}
		
		creatures.remove(ent);
		
		return creatures;
	}
	
}
