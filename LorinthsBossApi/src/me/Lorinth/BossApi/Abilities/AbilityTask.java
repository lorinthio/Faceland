package me.Lorinth.BossApi.Abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.Lorinth.BossApi.BossApi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.scheduler.BukkitRunnable;

public class AbilityTask extends BukkitRunnable {

	final Creature caster;
	List<Action> actions;
	
	HashMap<Integer, List<Action>> actionsAtDelay = new HashMap<Integer, List<Action>>();
	boolean hasDelay = false;
	
	public AbilityTask(List<Action> actions2, Creature ent){
		this.actions = actions2;
		caster = ent;
		
		checkDelays();
	}
	
	public void checkDelays(){
		hasDelay = false;
		for(Action a : actions){
			//System.out.println(a.toString());
			if(a instanceof Wait){
				//System.out.println("Found wait");
				hasDelay = true;
			}
		}
		
		if(hasDelay){
			double delay = 0;
			double newDelay = 0;
			List<Action> actionSet = new ArrayList<Action>();
			for(Action a : actions){
				if(a instanceof Wait){
					newDelay += ((Wait) a).delay * 20;
					if(newDelay != delay){
						actionsAtDelay.put((int) delay, actionSet);
						actionSet = new ArrayList<Action>();
					}
					delay = newDelay;
				}
				else{
					actionSet.add(a);
				}
			}
			actionsAtDelay.put((int) delay, actionSet);
			
			//System.out.println(actionsAtDelay.toString());
		}
	}
	
	@Override
	public void run() {
		if(hasDelay){
			for(final Integer time : actionsAtDelay.keySet()){
				if(!caster.isDead()){
					if(time == 0){
						for(Action a : actionsAtDelay.get(time)){
							a.execute(caster);
						}
					}
					else{
						Bukkit.getScheduler().scheduleSyncDelayedTask(BossApi.getPlugin(), new Runnable(){
	
							@Override
							public void run() {
								if(!caster.isDead()){
									for(Action a : actionsAtDelay.get(time)){
										a.execute(caster);
									}
								}
							}
							
						}, time);
					}
				}
			}
		}
		else{
			if(!caster.isDead()){
				for(Action a : actions){
					a.execute(caster);
				}
			}
		}
		
	}

	
	
}
