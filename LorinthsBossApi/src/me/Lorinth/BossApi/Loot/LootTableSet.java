package me.Lorinth.BossApi.Loot;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootTableSet {

	public List<LootTable> set = new ArrayList<LootTable>();
	
	public LootTableSet(){
		//
	}
	
	public void addTable(LootTable table){
		set.add(table);
	}
	
	public List<ItemStack> getLoot(){
		List<ItemStack> items = new ArrayList<ItemStack>();
		for(LootTable table : set){
			ItemStack drop = table.getDrop();
			if(drop.getType() != Material.AIR){
				items.add(table.getDrop());
			}
			
			
		}
		return items;
	}
	
}
