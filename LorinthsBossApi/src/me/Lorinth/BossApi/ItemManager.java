package me.Lorinth.BossApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import me.Lorinth.BossApi.Events.BossDeathEvent;
import me.Lorinth.BossApi.Loot.LootTable;
import me.Lorinth.BossApi.Loot.LootTableSet;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

public class ItemManager {

	HashMap<String, ItemStack> itemList = new HashMap<String, ItemStack>();
	HashMap<String, LootTable> tables = new HashMap<String, LootTable>();
	HashMap<String, LootTableSet> sets = new HashMap<String, LootTableSet>();
	
	BossApi api;
	
	File itemsFile;
	FileConfiguration items;
	
	File lootTableFile;
	FileConfiguration lootTables;
	
	public ItemManager(BossApi main){
		api = main;
		LoadData();
	}
	
	void LoadData(){
		itemsFile = new File(api.getDataFolder(), "Items.yml");
		lootTableFile = new File(api.getDataFolder(), "LootTables.yml");
		
		try {
			firstRun();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		items = new YamlConfiguration();
		lootTables = new YamlConfiguration();
		
		loadYamls();
		loadItems();
		loadTables();
	}
	
	@SuppressWarnings("deprecation")
	private void loadItems(){
		for(String key : items.getConfigurationSection("").getKeys(false)){
			ItemStack item = new ItemStack(0);
			boolean isPotion = false;
			
			Integer i = items.getInt(key + ".Id");
			if(i == 0){
				if(items.getString(key + ".Id").equalsIgnoreCase("potion")){
					i = Material.POTION.getId();
					isPotion = true;
				}
			}
			Integer d = items.getInt(key + ".Data");
			String name = items.getString(key + ".Display");
			List<String> lore = items.getStringList(key + ".Lore");
			
			if(isPotion){
				item = new ItemStack(Material.POTION);
				item.setDurability((short)(int)d);
				
			}
			else{
				item = new ItemStack(Material.getMaterial(i));
				MaterialData data = item.getData();
				data.setData(d.byteValue());
				item.setData(data);
				
				//Set Meta
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(api.convertToMColors(name));
				meta.setLore(api.convertToMColors(lore));
				item.setItemMeta(meta);
			}
			//System.out.println(i + ":" + d + ", " + name);
			
			//Set Data
			
			
			//Store item by key
			itemList.put(key, item);
		}
	}
	
	private void loadTables(){
		for(String key : lootTables.getConfigurationSection("").getKeys(false)){
			ProcessTable(key);
		}
	}
	
	void ProcessTable(String s){
		for(String key : lootTables.getConfigurationSection(s).getKeys(false)){
			if(lootTables.get(s + "." + key) instanceof Number){
				tables.put(s, MakeLootTable(s));
			}
			else{
				sets.put(s, MakeLootSet(s));
			}
		}
	}
	
	LootTable MakeLootTable(String key){
		LootTable lt = new LootTable();
		for(String key2 : lootTables.getConfigurationSection(key).getKeys(false)){
			lt.AddItem(getItem(key2), lootTables.getDouble(key + "." + key2));
		}
		return lt;
	}
	
	LootTableSet MakeLootSet(String key){
		LootTableSet set = new LootTableSet();
		for(String key2 : lootTables.getConfigurationSection(key).getKeys(false)){
			set.addTable(MakeLootTable(key + "." + key2));
		}
		return set;
	}
	
	ItemStack getItem(String key){
		return itemList.get(key);
	}
	
	private void loadYamls(){
		try{
			items.load(itemsFile);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		try{
			lootTables.load(lootTableFile);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void firstRun() throws Exception {
        if(!itemsFile.exists()){                        // checks if the yaml does not exists
            itemsFile.getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            copy(api.getResource("Items.yml"), itemsFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
        if(!lootTableFile.exists()){
        	lootTableFile.getParentFile().mkdirs();
        	copy(api.getResource("LootTables.yml"), lootTableFile);
        }
    }
	
	private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public Object getTable(String s){
		if(tables.containsKey(s)){
			return tables.get(s);
		}
		else{
			if(sets.containsKey(s)){
				return sets.get(s);
			}
		}
		return null;
	}

	public void dropItems(BossDeathEvent e) {
		try{
			String table = e.getBossInstance().b.lootTable;
			if(!table.equalsIgnoreCase("")){
				Object t = getTable(table);
				if(t != null){
					if(t instanceof LootTable){
						ItemStack drop = ((LootTable) t).getDrop();
						if(drop.getType() != Material.AIR){
							e.getWorld().dropItem(e.getLocation(), drop.clone());
						}
					}
					else if(t instanceof LootTableSet){
						for(ItemStack i : ((LootTableSet) t).getLoot()){
							if(i.getType() != Material.AIR){
								e.getWorld().dropItem(e.getLocation(), i.clone());
							}
						}
					}
				}
			}
		}
		catch(NullPointerException error){
			
		}
	}
	
}
