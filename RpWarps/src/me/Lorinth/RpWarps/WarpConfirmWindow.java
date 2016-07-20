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
		inv = Bukkit.createInventory(null, 27, ChatColor.BLACK + "" + ChatColor.WHITE +"★" + warp.name);
		
		ItemStack warpItem = new ItemStack(Material.NETHER_STAR);
		ItemMeta meta = warpItem.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Warp Now!");
		warpItem.setItemMeta(meta);

        ItemStack cancelItem = new ItemStack(Material.COAL);
        meta = cancelItem.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + "Cancel");
        cancelItem.setItemMeta(meta);

        ItemStack forgetItem = new ItemStack(Material.REDSTONE);
        meta = forgetItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Delete Warp");
        forgetItem.setItemMeta(meta);
		
		
		inv.setItem(12, warpItem);
		inv.setItem(14, cancelItem);
		inv.setItem(26, forgetItem);
		
		play.openInventory(inv);
	}
	
	void handleClickEvent(InventoryClickEvent e){
        if (!(e.getInventory().getName().startsWith(ChatColor.BLACK + "" + ChatColor.WHITE + "★"))) {
            return;
        }
        if(e.getSlot() == 12){
            play.closeInventory();
            warp.teleport(play);
            play.sendMessage(ChatColor.GREEN + "You have warped to " + warp.name + ChatColor.GREEN + "!");
        }
        if(e.getSlot() == 14){
            play.closeInventory();
            }
        if(e.getSlot() == 26){
            play.closeInventory();
            profile.knownWarps.remove(warp.ID);
            play.sendMessage(ChatColor.YELLOW + "You have forgotten the warp " + warp.name + ChatColor.YELLOW + "!");
        }
	}
	
}
