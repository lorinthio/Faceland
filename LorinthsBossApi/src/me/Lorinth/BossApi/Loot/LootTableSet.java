package me.Lorinth.BossApi.Loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
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
