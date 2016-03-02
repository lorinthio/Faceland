package me.lorinth.mounts;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MountWindow {

	private LorinthsMountsMain main;
	private Player player;
	private Inventory inv;
	private List<Mount> mounts;
	
	public MountWindow(LorinthsMountsMain main, List<Mount> mounts, Player player){
		this.main = main;
		this.mounts = mounts;
		this.player = player;
		
		createWindow();
		showPlayer();
	}
	
	public void createWindow(){
		String name = main.convertToMColors(main.windowName.replace("<name>", player.getDisplayName()));
		
		inv = Bukkit.getServer().createInventory(null, 27, name);
		int slot = 0;
		for(Mount mount : mounts){
			inv.setItem(slot, mount.getDisplayItem());
			slot += 1;
		}
	}
	
	public void showPlayer(){
		player.openInventory(inv);
	}
	
	public void handleClick(InventoryClickEvent event){
		try{
			Mount m = mounts.get(event.getSlot());
			if(m != null && !main.mountCooldowns.contains(player)){
				if(main.notify){
					player.sendMessage(ChatColor.GREEN + "[Mounts] : You have mounted, " + m.getName());
				}
				
				main.activeHorses.put(player, m.spawn(player));
				player.closeInventory();
				
				main.mountCooldowns.add(player);
				Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable(){

					@Override
					public void run() {
						main.mountCooldowns.remove(player);
					}
					
				}, main.cooldownDelay);
			}
		}
		catch(IndexOutOfBoundsException error){
			//pass
		}
		event.setCancelled(true);
			
	}
	
}
