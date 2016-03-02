package me.Lorinth.RpWarps;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerWarpProfile {

	List<String> knownWarps = new ArrayList<String>();
	RpWarpsMain main;
	Player p;

	public PlayerWarpProfile(Player p, RpWarpsMain main){
		this.p = p;
		this.main = main;
		String uuid = p.getUniqueId().toString();
		try{
			if(main.playersYml.getConfigurationSection("Players").getKeys(false).contains(uuid)){
				knownWarps = main.playersYml.getStringList("Players." + uuid);
			}
		}
		catch(NullPointerException e){
			//File doesn't exist yet
		}
	}
	
	public void Save(){
		main.playersYml.set("Players." + p.getUniqueId().toString(), knownWarps);
	}
	
	public void AddWarp(Warp w){
		if(w.ServerOwned || w.isAccessPoint){
			return;
		}
		
		if(!knownWarps.contains(w.ID)){
			if(knownWarps.size() < 36){
				knownWarps.add(w.ID);
				p.sendMessage(ChatColor.GREEN + "This location has been memorized and added to your list!");
				p.sendMessage(ChatColor.GREEN + "You now have access to" + w.name + ChatColor.GREEN + "!");
			}
			else{
                p.sendMessage(ChatColor.RED + "You have memorized the maximum number of locations!");
			}
		}
	}
	
}
