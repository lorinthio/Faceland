package me.Lorinth.BossApi.Loot;

import java.util.HashMap;
import java.util.Random;

import net.minecraft.server.v1_10_R1.NBTTagCompound;
import net.minecraft.server.v1_10_R1.NBTTagList;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class LootTable {

	private HashMap<ItemStack, Double> table = new HashMap<ItemStack, Double>();
	private Random r = new Random();
	
	public void AddItem(ItemStack i, Double d){
		table.put(removeAttributes(i), d);
	}
	
	public LootTable(){
		//Empty Constructor
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack getDrop(){
		Double roll = r.nextDouble() * 100;
		double cur = 0;
		
		ItemStack item = new ItemStack(0);
		
		for(ItemStack i : table.keySet()){
			cur += table.get(i);
			if(cur > roll){
				item = i;
			}
		}
		
		return item;
	}
	
	private static ItemStack removeAttributes(ItemStack i) {
        if(i == null) {
            return i;
        }
        if(i.getType() == Material.BOOK_AND_QUILL) {
            return i;
        }
        ItemStack item = i.clone();
        net.minecraft.server.v1_10_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if(!nmsStack.hasTag()) {
            tag = new NBTTagCompound();
            nmsStack.setTag(tag);
        }
        else {
            tag = nmsStack.getTag();
        }
        NBTTagList am = new NBTTagList();
        tag.set("AttributeModifiers", am);
        nmsStack.setTag(tag);
        return CraftItemStack.asCraftMirror(nmsStack);
    }
	
}
