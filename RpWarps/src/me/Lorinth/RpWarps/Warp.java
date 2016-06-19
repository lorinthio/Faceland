package me.Lorinth.RpWarps;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Warp {

	String ID;
	String name;
	Material mat = Material.ENDER_PEARL;
	Location loc;
	String owner; //UUID
	boolean ServerOwned = false;
	boolean isAccessPoint = false;
	RpWarpsMain main;
	
	ItemStack display;
	
	public Warp(RpWarpsMain main){
		this.main = main;
	}
	
	@Override
	public String toString(){
		String line = "";
		
		line = "{ID: " + ID + ", Name: " + ChatColor.stripColor(name) + ", Material: " + mat.toString() + ", Owner: " + owner + ", Location:" + loc.toString() + "}";
		
		if(ServerOwned){
			line = "ServerWarp" + line;
		}
		else{
			line = "PlayerWarp" + line;
		}
		
		return line;
	}
	
	public Warp(RpWarpsMain main, String ID, Location loc, Player p){
		this.main = main;
		this.ID = ID;
		this.name = "<Unnamed>";
		this.loc = loc;
		owner = p.getUniqueId().toString();
		
		p.sendMessage(ChatColor.GREEN + "Warp Created!");
		p.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/quickwarp name <name> " + ChatColor.GREEN + "to name it");
		p.sendMessage(ChatColor.GREEN + "Use " + ChatColor.WHITE + "/quickwarp icon " + ChatColor.GREEN + "to change the icon");
		if(p.hasPermission("LRWarps.convert")){
			p.sendMessage(ChatColor.GOLD + "Use " + ChatColor.WHITE + "/quickwarp convert " + ChatColor.GOLD + "to convert this to a server warp");
		}
		else if(p.hasPermission("LRWarps.admin")){
			p.sendMessage(ChatColor.GOLD + "Use " + ChatColor.WHITE + "/quickwarp setaccess " + ChatColor.GOLD + "to convert this to a server warp");
		}
		p.playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1, 1);

		makeDisplayItem();
	}
	
	boolean isOwnedByPlayer(Player p){
		if(!ServerOwned){
			if(owner.equalsIgnoreCase(p.getUniqueId().toString())){
				return true;
			}
			if(p.hasPermission("LRWarps.admin")){
				return true;
			}
		}
		else{
			if(p.hasPermission("LRWarps.admin")){
				return true;
			}
		}
		
		p.sendMessage(ChatColor.RED + "You cannot edit this warp point!");
		return false;
	}
	
	void makeDisplayItem(){
		if(mat == Material.AIR){
			mat = Material.ENDER_PEARL;
		}
		display = new ItemStack(mat);
		ItemMeta meta = display.getItemMeta();
		meta.setDisplayName(name);
		List<String> lore = new ArrayList<String>();
		if(ServerOwned){
			lore.add(ChatColor.WHITE + "Owner: " + ChatColor.BLUE + "Faceland!");
		}
		else{
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(owner));
			lore.add(ChatColor.WHITE + "Owner: " + ChatColor.GRAY + p.getName());
			lore.add(ChatColor.WHITE + "World: " + ChatColor.GRAY + loc.getWorld().getName());
			lore.add(ChatColor.WHITE + "Location: "  + ChatColor.GRAY + "("  +loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
		}
		
		meta.setLore(lore);
		display.setItemMeta(meta);
	}
	
	public void SetServerOwned(boolean set){
		ServerOwned = set;
	}
	
	public void teleport(Player p){
		p.teleport(loc.clone().add(0.5, 1, 0.5));
	}
	
	public void Save(){
		if(isAccessPoint){
			String prefix = "AccessPoints.";
			main.warpsYml.set(prefix + ID + ".Location.World", loc.getWorld().getName());
			main.warpsYml.set(prefix + ID + ".Location.X", loc.getX());
			main.warpsYml.set(prefix + ID + ".Location.Y", loc.getY());
			main.warpsYml.set(prefix + ID + ".Location.Z", loc.getZ());
		}
		else{
			String prefix = "PlayerWarps.";
			if(ServerOwned){
				prefix = "ServerWarps.";
			}
			
			main.warpsYml.set(prefix + ID + ".Owner", owner);
			main.warpsYml.set(prefix + ID + ".Name", name);
			main.warpsYml.set(prefix + ID + ".Material", mat.toString());
			main.warpsYml.set(prefix + ID + ".Location.World", loc.getWorld().getName());
			main.warpsYml.set(prefix + ID + ".Location.X", loc.getX());
			main.warpsYml.set(prefix + ID + ".Location.Y", loc.getY());
			main.warpsYml.set(prefix + ID + ".Location.Z", loc.getZ());
		}
		
		
	}
	
}
