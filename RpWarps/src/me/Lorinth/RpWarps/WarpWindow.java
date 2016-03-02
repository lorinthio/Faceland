package me.Lorinth.RpWarps;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class WarpWindow {

	Inventory inv;
	Player play;
	RpWarpsMain main;
	HashMap<Integer, Warp> windowWarps = new HashMap<Integer, Warp>();
	PlayerWarpProfile profile;
	ItemStack blank;
	
	public WarpWindow(Player p, RpWarpsMain main){
		this.main = main;
		play = p;
		profile = main.profiles.get(p);
		createBlank();
		createWindow();
	}
	
	void createBlank(){
		blank = new ItemStack(Material.STAINED_GLASS_PANE);
		ItemMeta meta = blank.getItemMeta();
		meta.setDisplayName(" ");
		blank.setItemMeta(meta);
		MaterialData data = blank.getData();
		data.setData(DyeColor.BLACK.getData());
		blank.setData(data);
	}
	
	void createWindow(){
		play.sendMessage(ChatColor.GREEN + "Opening teleport menu!");
		inv = Bukkit.createInventory(null, 54, ChatColor.WHITE + "QuickWarp!");
		
		int pos = 0;
		
		for(Warp w : main.serverWarps){
			inv.setItem(pos, w.display);
			windowWarps.put(pos, w);
			pos += 1;
		}
		if(pos <8){
			pos = 9;
		}
		else if(pos < 17){
			pos = 18;
		}
		
		for(int i=pos; i<pos+9; i++){
			inv.setItem(i, blank);
		}
		
		pos += 9;
		
		ArrayList<String> remove = new ArrayList<String>();
		
		for(String id : profile.knownWarps){
			Warp w = main.playerWarps.get(id);
			try{
				inv.setItem(pos, w.display);
				windowWarps.put(pos, w);
				pos += 1;
			}
			catch(NullPointerException e){
				remove.add(id);
			}
		}
		profile.knownWarps.removeAll(remove);
		
		play.openInventory(inv);
	}
	
	void HandleClickEvent(InventoryClickEvent event){
		try{
			if(inv.getItem(event.getSlot()).getType() != Material.STAINED_GLASS_PANE){
				play.closeInventory();
				
				Warp w = windowWarps.get(event.getSlot());
				if(w.ServerOwned){
					w.teleport(play);
					play.sendMessage(ChatColor.GREEN + "You have warped to " + w.name + ChatColor.GREEN +"!");
			
				}
				else{
					main.playerConfirms.put(play, new WarpConfirmWindow(main, play, w, profile));
				}
			}
		}
		catch(NullPointerException e){
			//NO ITEM CLICKED
		}
		catch(ArrayIndexOutOfBoundsException e){
			//Outside Window
		}
	}
	
}
