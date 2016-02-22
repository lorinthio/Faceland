package me.Lorinth.RpWarps;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.configuration.file.YamlConfiguration;
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
				p.sendMessage(ChatColor.GREEN + "You have learned the location, " + w.name);
			}
			else{
				p.sendMessage(ChatColor.RED + "Your ");
			}
		}
	}
	
}
