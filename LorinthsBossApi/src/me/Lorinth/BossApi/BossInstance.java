package me.Lorinth.BossApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import me.Lorinth.BossApi.Abilities.Ability;
import me.Lorinth.BossApi.Tasks.ParticleTask;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

public class BossInstance {
	Boss b;
	LivingEntity bossEntity;
	BossInstance mount;

    public Spawner spawner = null;
	
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
		this.bossEntity = ent;
	}
	
	public void StartParticles(){
		if(particle != null){
			ParticleTask task = new ParticleTask(bossEntity, particle, effectRadius, effectCount, effectData);
			task.runTaskTimer(BossApi.getPlugin(), 0, 5);
		}
	}
	
	public void dealtDamage(){
		onHit();
	}
	
	public void onDamage(){
		double percent = 100 * (bossEntity.getHealth() / bossEntity.getMaxHealth());
		
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
			for(final Ability a : onHit.get(sec)){
				if(!onCooldown.contains(a.name)){
					if(r.nextDouble() * 100 < a.chance){
						a.Cast((Creature) bossEntity);
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
			for(final Ability a : whenHit.get(section)){
				if(!onCooldown.contains(a.name)){
					if(r.nextDouble() * 100 < a.chance){
						a.Cast((Creature) bossEntity);
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

    public LivingEntity getBossEntity() {
        return bossEntity;
    }
	
	public void onEnterNewHealthSection(HealthSection section){
		//System.out.println(onEnter.toString());
		try{
			for(Ability a : this.onEnter.get(section)){
				a.Cast((Creature) bossEntity);
			}
		}
		catch(NullPointerException e){
			//No onEnter
		}
	}
	
}
