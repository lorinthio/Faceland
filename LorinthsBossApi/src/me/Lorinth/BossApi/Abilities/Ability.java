package me.Lorinth.BossApi.Abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;

public class Ability {
	
	public double chance;
	public double cooldown;
	List<Action> actions = new ArrayList<Action>();
	public String name;
	
	public Ability(double chance, String name, List<Action> actions, double cooldown){
		this.chance = chance;
		this.name = name;
		this.cooldown = cooldown;
		this.actions = actions;
	}
	
	public Ability(String name, List<Action> actions, double cooldown){
		chance = 10;
		this.name = name;
		this.cooldown = cooldown;
		this.actions = actions;
	}
	
	public void Cast(final Creature ent){
		AbilityTask task = new AbilityTask(actions, ent);
		task.run();
	}
	
	public Ability clone(){
		return new Ability(chance, name, actions, cooldown);
	}

}
