package me.Lorinth.BossApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import me.Lorinth.BossApi.Abilities.Ability;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class BossInstance {

	Boss b;
	LivingEntity ent;
	BossInstance mount;
	
	private HealthSection section = HealthSection.max;
	
	double effectRadius = 2;
	double effectCount = 10;
	int effectData = 0;
	public Effect particle;
	
	Random r = new Random();
	
	public HashMap<HealthSection, ArrayList<Ability>> onHit = new HashMap<HealthSection, ArrayList<Ability>>();
	public HashMap<HealthSection, ArrayList<Ability>> whenHit = new HashMap<HealthSection, ArrayList<Ability>>();
	public HashMap<HealthSection, ArrayList<Ability>> onEnter = new HashMap<HealthSection, ArrayList<Ability>>();
	public ArrayList<String> onCooldown = new ArrayList<String>();
	
	public enum HealthSection{
		lowest, low, medium, high, highest, max
	}
	
	public BossInstance(Boss b, LivingEntity ent){
		this.b = b;
		this.ent = ent;
	}
	
	public void StartParticles(){
		if(particle != null){
			ParticleTask task = new ParticleTask(ent, particle, effectRadius, effectCount, effectData);
			task.runTaskTimer(BossApi.getPlugin(), 0, 5);
		}
	}
	
	public void dealtDamage(){
		onHit();
	}
	
	public void onDamage(){
		double percent = 100 * (ent.getHealth() / ent.getMaxHealth());
		
		boolean change = false;
		
		switch(section){
		case max:
			if(percent < 100){
				section = HealthSection.highest;
				change = true;
			}
		case highest:
			if(percent <= 80){
				section = HealthSection.high;
				change = true;
			}
			break;
		case high:
			if(percent <= 60){
				section = HealthSection.medium;
				change = true;
			}
			break;
		case medium:
			if(percent <= 40){
				section = HealthSection.low;
				change = true;
			}
			break;
		case low:
			if(percent <= 20){
				section = HealthSection.lowest;
				change = true;
			}
			break;
		case lowest:
			break;
		default:
			break;
		}
		
		whenHit();
		
		if(change){
			onEnterNewHealthSection(section);
		}
	}
	
	public void onHit(){
		HealthSection sec = section;
		if(sec == HealthSection.max){
			sec = HealthSection.highest;
		}
		try{
			for(Ability a : onHit.get(sec)){
				if(!onCooldown.contains(a.name)){
					if(r.nextDouble() * 100 < a.chance){
						a.Cast((Creature) ent);
						onCooldown.add(a.name);
						Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable(){
	
							@Override
							public void run() {
								onCooldown.remove(a.name);
							}
							
						}, (long) (a.cooldown * 20));
						break;
					}
				}
			}
		}
		catch(NullPointerException e){
			//No phase
		}
	}
	
	public void whenHit(){
		HealthSection sec = section;
		if(sec == HealthSection.max){
			sec = HealthSection.highest;
		}
		
		try{
			for(Ability a : whenHit.get(section)){
				if(!onCooldown.contains(a.name)){
					if(r.nextDouble() * 100 < a.chance){
						a.Cast((Creature) ent);
						onCooldown.add(a.name);
						Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable(){
	
							@Override
							public void run() {
								onCooldown.remove(a.name);
							}
							
						}, (long) (a.cooldown * 20));
						break;
					}
				}
			}
		}
		catch(NullPointerException e){
			//No Phase
		}
	}
	
	public void TookDamage(){
		whenHit();
		onDamage();
	}
	
	public void onEnterNewHealthSection(HealthSection section){
		//System.out.println(onEnter.toString());
		try{
			for(Ability a : this.onEnter.get(section)){
				a.Cast((Creature) ent);
			}
		}
		catch(NullPointerException e){
			//No onEnter
		}
	}
	
}
