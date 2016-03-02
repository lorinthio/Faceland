package me.Lorinth.RpWarps;

import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpConfirmWindow {

	Inventory inv;
	Player play;
	RpWarpsMain main;
	Warp warp;
	PlayerWarpProfile profile;
	
	public WarpConfirmWindow(RpWarpsMain main, Player play, Warp w, PlayerWarpProfile profile){
		this.play = play;
		this.main = main;
		this.warp = w;
		this.profile = profile;
		
		makeInventory();
	}
	
	void makeInventory(){
		inv = Bukkit.createInventory(null, 27, warp.name);
		
		ItemStack warpItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta meta = warpItem.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Warp!");
		warpItem.setItemMeta(meta);
		
		ItemStack forgetItem = new ItemStack(Material.BARRIER);
		meta = forgetItem.getItemMeta();
		meta.setDisplayName(ChatColor.RED + "Forget!");
		forgetItem.setItemMeta(meta);
		
		
		inv.setItem(12, warpItem);
		inv.setItem(14, forgetItem);
		
		play.openInventory(inv);
	}
	
	void handleClickEvent(InventoryClickEvent e){
		try{
			if(e.getSlot() == 12){
				play.closeInventory();
				warp.teleport(play);
				play.sendMessage(ChatColor.GREEN + "You have warped to, " + warp.name);
			}
			if(e.getSlot() == 14){
				play.closeInventory();
				profile.knownWarps.remove(warp.ID);
				play.sendMessage(ChatColor.RED + "You have forgotten, " + warp.name);
			}
		}
		catch(NullPointerException error){
			//NO ITEM CLICKED
		}
		catch(ArrayIndexOutOfBoundsException error){
			//Outside Window
		}
	}
	
}
