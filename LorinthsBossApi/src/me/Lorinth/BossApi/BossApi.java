package me.Lorinth.BossApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.Lorinth.BossApi.BossInstance.HealthSection;
import me.Lorinth.BossApi.Abilities.Ability;
import me.Lorinth.BossApi.Abilities.Action;
import me.Lorinth.BossApi.Abilities.Heal;
import me.Lorinth.BossApi.Abilities.Ignite;
import me.Lorinth.BossApi.Abilities.Knockback;
import me.Lorinth.BossApi.Abilities.LaunchFireball;
import me.Lorinth.BossApi.Abilities.LaunchSnowball;
import me.Lorinth.BossApi.Abilities.Lightning;
import me.Lorinth.BossApi.Abilities.PotionEffectAction;
import me.Lorinth.BossApi.Abilities.Speak;
import me.Lorinth.BossApi.Abilities.Summon;
import me.Lorinth.BossApi.Abilities.TargetType;
import me.Lorinth.BossApi.Abilities.Teleport;
import me.Lorinth.BossApi.Abilities.TrueDamage;
import me.Lorinth.BossApi.Abilities.Wait;
import me.Lorinth.BossApi.Events.BossDeathEvent;
import me.Lorinth.BossApi.Events.BossSpawnEvent;
import me.Lorinth.BossApi.Tasks.SpawnTask;

import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

public class BossApi extends JavaPlugin implements Listener{

	private static BossApi instance;
    private SpawnTask spawnTask;
	public HashMap<String, Boss> bossNames = new HashMap<String, Boss>();
	public HashMap<BossInstance, Integer> bossEntities = new HashMap<BossInstance, Integer>();
	public HashMap<Integer, BossInstance> bossIds = new HashMap<Integer, BossInstance>();
	
	public HashMap<World, ArrayList<Integer>> worldBossIds = new HashMap<World, ArrayList<Integer>>();
	
	public HashMap<Projectile, Integer> projectiles = new HashMap<Projectile, Integer>();
	
	public ArrayList<Spawner> spawnerList = new ArrayList<Spawner>();
	public HashMap<String, Ability> abilityList = new HashMap<String, Ability>();
	
	ItemManager itemManager;
	
	File bossesFile;            ////
	File spawnersFile;
	File abilitiesFile;
	
    FileConfiguration bosses;   ////
    FileConfiguration spawners;
    FileConfiguration abilities;
 
    @Override
    public void onDisable() {
        // in here, we need to save all our yamls if we have not yet,
        //  this maybe also a critical part cause some methods you might have forgotten
        //  to use the saveYamls(); on some of your methods that uses bosses.set or *.set(path,value)
        //  so we will use saveYamls(); method here to auto save what we have done
    	
    	for(BossInstance bi : bossEntities.keySet()){
    		bi.bossEntity.remove();
    	}
    	
    }
 
    /*
     * in this firstRun(); method, we checked if each File that we initialized does not exists
     *  if it does not exists, we load the yaml located at your jar file, then save it in
     *  the File(/plugins/<pluginName>/*.yml)
     * only needed at onEnable()
     */
    private void firstRun() throws Exception {
        if(!bossesFile.exists()){                        // checks if the yaml does not exists
            bossesFile.getParentFile().mkdirs();         // creates the /plugins/<pluginName>/ directory if not found
            copy(getResource("Bosses.yml"), bossesFile); // copies the yaml from your jar to the folder /plugin/<pluginName>
        }
        if(!spawnersFile.exists()){
        	spawnersFile.getParentFile().mkdirs();
        	copy(getResource("Spawners.yml"), spawnersFile);
        }
        if(!abilitiesFile.exists()){
        	abilitiesFile.getParentFile().mkdirs();
        	copy(getResource("Abilities.yml"), abilitiesFile);
        }
    }
 
    /*
     * this copy(); method copies the specified file from your jar
     *     to your /plugins/<pluginName>/ folder
     */
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
 
    /*
     * in here, each of the FileConfigurations loaded the contents of yamls
     *  found at the /plugins/<pluginName>/*yml.
     * needed at onEnable() after using firstRun();
     * can be called anywhere if you need to reload the yamls.
     */
    public void loadYamls() {
        try {
            bosses.load(bossesFile); //loads the contents of the File to its FileConfiguration
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            spawners.load(spawnersFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            abilities.load(abilitiesFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    /*
     * save all FileConfigurations to its corresponding File
     * optional at onDisable()
     * can be called anywhere if you have *.set(path,value) on your methods
     */
    public void saveYamls() {
        try {
            bosses.save(bossesFile); //saves the FileConfiguration to its File
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            spawners.save(spawnersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            abilities.save(abilitiesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void onEnable(){
		instance = this;
		
		itemManager = new ItemManager(this);
        spawnTask = new SpawnTask(this);
		
		bossesFile = new File(getDataFolder(), "Bosses.yml");
		spawnersFile = new File(getDataFolder(), "Spawners.yml");
		abilitiesFile = new File(getDataFolder(), "Abilities.yml");
		 
        try {
            firstRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        // and we declare the FileConfigurations using YamlConfigurations and
        // then we just use loadYamls(); method
        // this is the critical part, this is needed cause if we do not use this,
        // it will read from the yml located at your jar, not in /plugins/<pluginName>/*yml.
        bosses = new YamlConfiguration();
        spawners = new YamlConfiguration();
        abilities = new YamlConfiguration();
        
        for(World w : Bukkit.getWorlds()){
        	this.worldBossIds.put(w, new ArrayList<Integer>());
        }
		spawnTask.runTaskTimer(this,
				20L * 60, // Start timer after 60s
				20L * 10 // Run it every 10s
		);
        
        loadYamls();
        loadData();
        
        Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	public void ReloadPlugin(){
		onDisable();
		
		bossNames = new HashMap<String, Boss>();
		bossEntities = new HashMap<BossInstance, Integer>();
		bossIds = new HashMap<Integer, BossInstance>();
		
		worldBossIds = new HashMap<World, ArrayList<Integer>>();
		
		projectiles = new HashMap<Projectile, Integer>();
		
		spawnerList = new ArrayList<Spawner>();
		abilityList = new HashMap<String, Ability>();
		
		onEnable();
	}
	
	public void loadData(){
		loadAbilities();
		loadBosses();
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){

			@Override
			public void run() {
				loadSpawners();
			}
			
		}, 20);
		
	}
	
	private void loadSpawners() {
		for(String key : this.spawners.getConfigurationSection("").getKeys(false)){
			//Bukkit.getConsoleSender().sendMessage("Spawner ID: " + key);
			Boss b = this.bossNames.get(this.spawners.get(key + ".Boss"));
			//Bukkit.getConsoleSender().sendMessage("has boss," + boss.name);
			double delay = spawners.getDouble(key + ".RespawnDelay");
			double distance = spawners.getDouble(key + ".MaxDistance");
			
			
			World w = Bukkit.getWorld(spawners.getString(key + ".Location.world"));
			double x, y, z;
			x = spawners.getDouble(key + ".Location.x");
			y = spawners.getDouble(key + ".Location.y");
			z= spawners.getDouble(key + ".Location.z");
			Location loc = new Location(w, x, y, z);
			
			//Bukkit.getConsoleSender().sendMessage("location," + loc.toString());
			
			Spawner s = new Spawner(key, this, b, loc, (long) delay, distance);
            addSpawner(s);
		}
	}

	Ability getAbility(String a){
		try{
			return abilityList.get(a).clone();
		}
		catch(NullPointerException e){
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + a + ", doesn't exist");
			return null;
		}
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	private void loadBosses() {
		//Bukkit.getConsoleSender().sendMessage(abilityList.toString());
		for(String key : bosses.getConfigurationSection("").getKeys(false)){
			//Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[BossApi] : Loading Boss, " + key);
			
			EntityType type = EntityType.fromName(bosses.getString(key + ".type").replace("_", "").toLowerCase());
			String name = bosses.getString(key + ".name");
			double hp = bosses.getDouble(key + ".hp");
			double damage = bosses.getDouble(key + ".damage");
			double movespeed = bosses.getDouble(key + ".move_speed");
			
			
			Boss b = new Boss(type, key, convertToMColors(name), hp, damage, movespeed);
			
			if(bosses.getConfigurationSection(key).getKeys(false).contains("LootTable")){
				b.lootTable = bosses.getString(key + ".LootTable");
			}
			if(bosses.getConfigurationSection(key).getKeys(false).contains("Particles")){
				//System.out.println(key + " has particles");
				for(String key2 : bosses.getConfigurationSection(key + ".Particles").getKeys(false)){
					if(key2.equalsIgnoreCase("radius")){
						b.effectRadius = bosses.getDouble(key + ".Particles." + key2);
					}
					else if(key2.equalsIgnoreCase("count")){
						b.effectCount = bosses.getDouble(key + ".Particles." + key2);
					}
					else if(key2.equalsIgnoreCase("data")){
						b.effectData = bosses.getInt(key + ".Particles." + key2);
					}
					else if(key2.equalsIgnoreCase("effect")){
						b.particle = Effect.getByName(bosses.getString(key + ".Particles." + key2));
						//System.out.println(bosses.getString(key + ".Particles." + key2));
					}
				}
			}
			
			if(bosses.getConfigurationSection(key).getKeys(false).contains("exp_reward")){
				b.expReward = bosses.getInt(key + ".exp_reward");
			}
			if(bosses.getConfigurationSection(key).getKeys(false).contains("Mount")){
				b.mountName = bosses.getString(key + ".Mount.Name");
				b.removeMountOnDeath = bosses.getBoolean(key + ".Mount.RemoveOnDeath");
			}
			if(bosses.getConfigurationSection(key).getKeys(false).contains("Equipment")){
				for(String key2 : bosses.getConfigurationSection(key + ".Equipment").getKeys(false)){
					if(key2.equalsIgnoreCase("held")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.held = new ItemStack((int)o);
						}
						else if(o instanceof String){
							b.held = itemManager.getItem((String)o).clone();
						}
					}
					if(key2.equalsIgnoreCase("off")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.off = new ItemStack((int)o);
						}
						else if(o instanceof String){
							b.off = itemManager.getItem((String)o).clone();
						}
					}
					else if(key2.equalsIgnoreCase("helm")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.helm = new ItemStack((int)o);
						}
						else if(o instanceof String){
							if(((String) o).contains("player_")){
								String playername = ((String) o).replace("player_", "");
								ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
								SkullMeta sm = (SkullMeta) head.getItemMeta();
					        	sm.setOwner(playername.trim());
					        	head.setItemMeta(sm);
								b.helm = head;
							}
							else{
								b.helm = itemManager.getItem((String)o).clone();
							}
						}
					}
					else if(key2.equalsIgnoreCase("chest")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.chest = new ItemStack((int)o);
						}
						else if(o instanceof String){
							b.chest = itemManager.getItem((String)o).clone();
						}		
					}
					else if(key2.equalsIgnoreCase("legs")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.legs = new ItemStack((int)o);
						}
						else if(o instanceof String){
							b.legs = itemManager.getItem((String)o).clone();
						}
					}
					else if(key2.equalsIgnoreCase("feet")){
						Object o = bosses.get(key + ".Equipment." + key2);
						if(o instanceof Number){
							b.feet = new ItemStack((int)o);
						}
						else if(o instanceof String){
							b.feet = itemManager.getItem((String)o).clone();
						}
					}
				}
			}
			
			for(String key2 : bosses.getConfigurationSection(key + ".Abilities").getKeys(false)){
				if(key2.equalsIgnoreCase("OnHit")){
					HashMap<HealthSection, ArrayList<Ability>> onHit = new HashMap<HealthSection, ArrayList<Ability>>();
					for(HealthSection h : HealthSection.values()){
						onHit.put(h, new ArrayList<Ability>());
					}
					for(String key3 : bosses.getConfigurationSection(key + ".Abilities.OnHit").getKeys(false)){
						if(key3.equalsIgnoreCase("Phase1")){
							List<String> phase1 = bosses.getStringList(key + ".Abilities.OnHit.Phase1");
							ArrayList<Ability> phase1Abilities = new ArrayList<Ability>();
							for(String abilityName : phase1){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase1Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnHit.Phase1, " + b.name);
								}
							}
							onHit.put(HealthSection.highest, phase1Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase2")){
							List<String> phase2 = bosses.getStringList(key + ".Abilities.OnHit.Phase2");
							ArrayList<Ability> phase2Abilities = new ArrayList<Ability>();
							for(String abilityName : phase2){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase2Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnHit.Phase2, " + b.name);
								}
							}
							onHit.put(HealthSection.high, phase2Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase3")){
							List<String> phase3 = bosses.getStringList(key + ".Abilities.OnHit.Phase3");
							ArrayList<Ability> phase3Abilities = new ArrayList<Ability>();
							for(String abilityName : phase3){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase3Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnHit.Phase3, " + b.name);
								}
							}
							onHit.put(HealthSection.medium, phase3Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase4")){
							List<String> phase4 = bosses.getStringList(key + ".Abilities.OnHit.Phase4");
							ArrayList<Ability> phase4Abilities = new ArrayList<Ability>();
							for(String abilityName : phase4){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase4Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnHit.Phase4, " + b.name);
								}
							}
							onHit.put(HealthSection.low, phase4Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase5")){
							List<String> phase5 = bosses.getStringList(key + ".Abilities.OnHit.Phase5");
							ArrayList<Ability> phase5Abilities = new ArrayList<Ability>();
							for(String abilityName : phase5){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase5Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnHit.Phase5, " + b.name);
								}
							}
							onHit.put(HealthSection.lowest, phase5Abilities);
						}
						b.onHit = onHit;
					}
				}
				else if(key2.equalsIgnoreCase("WhenHit")){
					HashMap<HealthSection, ArrayList<Ability>> whenHit = new HashMap<HealthSection, ArrayList<Ability>>();
					for(HealthSection h : HealthSection.values()){
						whenHit.put(h, new ArrayList<Ability>());
					}
					for(String key3 : bosses.getConfigurationSection(key + ".Abilities.WhenHit").getKeys(false)){
						if(key3.equalsIgnoreCase("Phase1")){
							List<String> phase1 = bosses.getStringList(key + ".Abilities.WhenHit.Phase1");
							ArrayList<Ability> phase1Abilities = new ArrayList<Ability>();
							for(String abilityName : phase1){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase1Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for WhenHit.Phase1, " + b.name);
								}
							}
							whenHit.put(HealthSection.highest, phase1Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase2")){
							List<String> phase2 = bosses.getStringList(key + ".Abilities.WhenHit.Phase2");
							ArrayList<Ability> phase2Abilities = new ArrayList<Ability>();
							for(String abilityName : phase2){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase2Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for WhenHit.Phase2, " + b.name);
								}
							}
							whenHit.put(HealthSection.high, phase2Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase3")){
							List<String> phase3 = bosses.getStringList(key + ".Abilities.WhenHit.Phase3");
							ArrayList<Ability> phase3Abilities = new ArrayList<Ability>();
							for(String abilityName : phase3){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase3Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for WhenHit.Phase3, " + b.name);
								}
							}
							whenHit.put(HealthSection.medium, phase3Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase4")){
							List<String> phase4 = bosses.getStringList(key + ".Abilities.WhenHit.Phase4");
							ArrayList<Ability> phase4Abilities = new ArrayList<Ability>();
							for(String abilityName : phase4){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase4Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for WhenHit.Phase4, " + b.name);
								}
							}
							whenHit.put(HealthSection.low, phase4Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase5")){
							List<String> phase5 = bosses.getStringList(key + ".Abilities.WhenHit.Phase5");
							ArrayList<Ability> phase5Abilities = new ArrayList<Ability>();
							for(String abilityName : phase5){
								String[] words = abilityName.split(" ");
								Ability a = getAbility(words[0]);
								if(a != null){
									a.chance = Double.parseDouble(words[1].replace("%", ""));
									phase5Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for WhenHit.Phase5, " + b.name);
								}
							}
							whenHit.put(HealthSection.lowest, phase5Abilities);
						}
						b.whenHit = whenHit;
					}
				}
				else if(key2.equalsIgnoreCase("OnNewPhase")){
					HashMap<HealthSection, ArrayList<Ability>> onEnter = new HashMap<HealthSection, ArrayList<Ability>>();
					for(HealthSection h : HealthSection.values()){
						onEnter.put(h, new ArrayList<Ability>());
					}
					for(String key3 : bosses.getConfigurationSection(key + ".Abilities.OnNewPhase").getKeys(false)){
						if(key3.equalsIgnoreCase("Phase1")){
							List<String> phase1 = bosses.getStringList(key + ".Abilities.OnNewPhase.Phase1");
							ArrayList<Ability> phase1Abilities = new ArrayList<Ability>();
							for(String abilityName : phase1){
								Ability a = getAbility(abilityName);
								if(a != null){
									phase1Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnNewPhase.Phase1, " + b.name);
								}
							}
							onEnter.put(HealthSection.highest, phase1Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase2")){
							List<String> phase2 = bosses.getStringList(key + ".Abilities.OnNewPhase.Phase2");
							ArrayList<Ability> phase2Abilities = new ArrayList<Ability>();
							for(String abilityName : phase2){
								Ability a = getAbility(abilityName);
								if(a != null){
									phase2Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnNewPhase.Phase2, " + b.name);
								}
							}
							onEnter.put(HealthSection.high, phase2Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase3")){
							List<String> phase3 = bosses.getStringList(key + ".Abilities.OnNewPhase.Phase3");
							ArrayList<Ability> phase3Abilities = new ArrayList<Ability>();
							for(String abilityName : phase3){
								Ability a = getAbility(abilityName);
								if(a != null){
									phase3Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnNewPhase.Phase3, " + b.name);
								}
							}
							onEnter.put(HealthSection.medium, phase3Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase4")){
							List<String> phase4 = bosses.getStringList(key + ".Abilities.OnNewPhase.Phase4");
							ArrayList<Ability> phase4Abilities = new ArrayList<Ability>();
							for(String abilityName : phase4){
								Ability a = getAbility(abilityName);
								if(a != null){
									phase4Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnNewPhase.Phase4, " + b.name);
								}
							}
							onEnter.put(HealthSection.low, phase4Abilities);
						}
						else if(key3.equalsIgnoreCase("Phase5")){
							List<String> phase5 = bosses.getStringList(key + ".Abilities.OnNewPhase.Phase5");
							ArrayList<Ability> phase5Abilities = new ArrayList<Ability>();
							for(String abilityName : phase5){
								Ability a = getAbility(abilityName);
								if(a != null){
									phase5Abilities.add(a);
								}
								else{
									Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "The ability, " + abilityName + ", doesn't exist for OnNewPhase.Phase5, " + b.name);
								}
							}
							onEnter.put(HealthSection.lowest, phase5Abilities);
						}
						
					}
					b.onEnter = onEnter;
					//Bukkit.getConsoleSender().sendMessage(onEnter.toString());
				}	
			}
			
			this.bossNames.put(key, b);
		}
	}

	private void loadAbilities() {
		for(String key : abilities.getConfigurationSection("").getKeys(false)){
			ArrayList<Action> actions = turnArgumentsIntoActions(abilities.getStringList(key + ".Actions"));
			double cooldown = abilities.getDouble(key + ".Cooldown");
			Ability ability = new Ability(key, actions, cooldown);
			this.abilityList.put(key, ability);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(commandLabel.equalsIgnoreCase("boss")){
				if(args.length == 0){
					p.sendMessage(ChatColor.UNDERLINE + "Commands");
					p.sendMessage(ChatColor.GOLD + "/boss list");
					p.sendMessage(ChatColor.GOLD + "/boss spawn <name>");
					p.sendMessage(ChatColor.GOLD + "/boss spawner create <bossname> <spawnername> <cooldown> <maxDistance>");
				}
				else{
					if(args[0].equalsIgnoreCase("list")){
						p.sendMessage(ChatColor.UNDERLINE + "Bosses");
						for(String name : bossNames.keySet()){
							p.sendMessage(ChatColor.GOLD + "- " + name);
						}
					}
					else if(args[0].equalsIgnoreCase("reload")){
						ReloadPlugin();
						p.sendMessage(ChatColor.GREEN + "Boss Plugin Reloaded!");
					}
					else if(args[0].equalsIgnoreCase("spawn")){
						Boss b = this.bossNames.get(args[1]);
						if(b != null){
							b.spawn(p.getLocation(), false);
							p.sendMessage(ChatColor.GREEN + "Boss was spawned!");
						}
						else{
							p.sendMessage(ChatColor.RED + "Boss was not found!");
						}
					}
					
					if(args.length >= 2){
						try{
							if(args[0].equalsIgnoreCase("spawner") && args[1].equalsIgnoreCase("create")){
								if(args.length == 6){
									Boss b = this.bossNames.get(args[2]);
									String spawnName = args[3];
									double cd = Double.parseDouble(args[4]);
									double dist = Double.parseDouble(args[5]);
									Spawner s = new Spawner(spawnName, this, b, p.getLocation(), (long) cd, dist);
                                    addSpawner(s);
									s.spawnBoss();
									s.save(spawnName);
								}
								else{
									p.sendMessage(ChatColor.GOLD + "/boss spawner create <bossname> <spawnername> <cooldown> <maxDistance>");
								}
							}
						}
						catch(NullPointerException e){
							p.sendMessage(ChatColor.RED + "There is no boss by the name, " + args[2]);
						}
					}
				}
			}
		}
		return false;
	}
	
	private ArrayList<Action> turnArgumentsIntoActions(List<String> stringList) {
		ArrayList<Action> actions = new ArrayList<Action>();
		for(String line : stringList){
			actions.add(createAction(line));
		}
		return actions;
	}
	
	private Action createAction(String line){
		String[] words = line.split(" ");
		String command = words[0];
		if(command.equalsIgnoreCase("Heal")){
			boolean targetB = false, amountB = false;
			TargetType type = TargetType.Self;
			double amount = 10;
			double radius = 5;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					targetB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("amount:")){
					amountB = true;
					amount = Double.parseDouble(arg.replace("amount:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				
			}
			
			if(targetB && amountB){
				Heal h = new Heal(type, amount);
				return h;
			}
		}
		else if(command.equalsIgnoreCase("Ignite")){
			boolean targetB = false, durationB = false;
			double duration=1, radius=1;
			TargetType type = TargetType.Target;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					targetB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("duration:")){
					durationB = true;
					duration = Double.parseDouble(arg.replace("duration:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				
			}
			
			if(targetB && durationB){
				if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
					Ignite i = new Ignite(type, radius, duration);
					return i;
				}
				else{
					Ignite i = new Ignite(type, duration);
					return i;
				}
			}
		}
		else if(command.equalsIgnoreCase("Knockback")){
			boolean targetB = false;
			double horiz = 1, vert = 1, radius=5;
			TargetType type = TargetType.Target;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					targetB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("h:")){
					horiz = Double.parseDouble(arg.replace("h:", ""));
				}
				else if(arg.startsWith("v:")){
					vert = Double.parseDouble(arg.replace("v:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				
			}
			
			if(targetB){
				if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
					Knockback k = new Knockback(horiz, vert);
					return k;
				}
				else{
					Knockback k = new Knockback(type, radius, horiz, vert);
					return k;
				}
			}
		}
		else if(command.equalsIgnoreCase("LaunchFireball")){
			
			boolean damageB = false, speedB = false, targetB = false, radiusB = false;
			double damage=5, speed = 1, radius = 5;
			TargetType type = TargetType.Target;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("damage:")){
					damageB = true;
					damage = Double.parseDouble(arg.replace("damage:", ""));
				}
				else if(arg.startsWith("target:")){
					targetB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("radius:")){
					radiusB = true;
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				else if(arg.startsWith("speed:")){
					speedB = true;
					speed = Double.parseDouble(arg.replace("speed:", ""));
				}
				
			}
			
			if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
				if(damageB && speedB && targetB && radiusB){
					LaunchFireball fire = new LaunchFireball(type, damage, speed, radius);
					return fire;
				}
			}
			else if(damageB && speedB && targetB){
				LaunchFireball fire = new LaunchFireball(type, damage, speed);
				return fire;
			}
		}
		else if(command.equalsIgnoreCase("LaunchSnowball")){
			boolean damageB = false, speedB = false, targetB = false, radiusB = false;
			double damage=5, speed = 1, radius = 5;
			TargetType type = TargetType.Target;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("damage:")){
					damageB = true;
					damage = Double.parseDouble(arg.replace("damage:", ""));
				}
				else if(arg.startsWith("speed:")){
					speedB = true;
					speed = Double.parseDouble(arg.replace("speed:", ""));
				}
				else if(arg.startsWith("target:")){
					targetB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("radius:")){
					radiusB = true;
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
			}
			if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
				if(damageB && speedB && targetB && radiusB){
					LaunchSnowball snow = new LaunchSnowball(type, damage, speed, radius);
					return snow;
				}
			}
			if(damageB && speedB){
				LaunchSnowball snow = new LaunchSnowball(type, damage, speed);
				return snow;
			}
		}
		else if(command.equalsIgnoreCase("Lightning")){
			boolean typeB=false, damageB=false;
			TargetType type = TargetType.Target;
			double damage = 1, radius = 5;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("damage:")){
					damageB = true;
					damage = Double.parseDouble(arg.replace("damage:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				else if(arg.startsWith("target:")){
					typeB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
			}
			
			if(damageB && typeB){
				if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
					Lightning light = new Lightning(type, damage, radius);
					return light;
				}
				else{
					Lightning light = new Lightning(type, damage);
					return light;
				}
			}
		}
		else if(command.equalsIgnoreCase("PotionEffect")){
			boolean typeB=false, durationB=false, intensityB=false, pTypeB=false;
			TargetType type = TargetType.Target;
			PotionEffectType pType = PotionEffectType.POISON;
			double duration = 1, intensity = 1, radius = 1;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					typeB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("duration:")){
					durationB = true;
					duration = Double.parseDouble(arg.replace("duration:", ""));
				}
				else if(arg.startsWith("intensity:")){
					intensityB = true;
					intensity = Double.parseDouble(arg.replace("intensity:", ""));
				}
				else if(arg.startsWith("type:")){
					pTypeB = true;
					pType = PotionEffectType.getByName(arg.replace("type:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
			}
			
			if(typeB && durationB && intensityB && pTypeB){
				if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
					PotionEffectAction act = new PotionEffectAction(pType, duration, intensity, type, radius);
					return act;
				}
				else{
					PotionEffectAction act = new PotionEffectAction(pType, duration, intensity, type);
					return act;
				}
			}
		}
		else if(command.equalsIgnoreCase("Speak")){
			boolean typeB = false, messageB = false;
			double radius = 10;
			TargetType type = TargetType.Target;
			String message = "";
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					typeB = true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
				else if(arg.startsWith("message:")){
					messageB = true;
					message = arg.replace("message:", "");
					message = message.replace("_", " ");
					message = this.convertToMColors(message);
				}
			}
			
			if(typeB && messageB){
				if(type == TargetType.AoePlayers){
					Speak s = new Speak(message, radius);
					return s;
				}
				else{
					Speak s = new Speak(message, type);
					return s;
				}
			}	
		}
		else if(command.equalsIgnoreCase("Summon")){
			boolean typeB = false, countB = false, bossB = false;
			Integer count = 1;
			EntityType type = EntityType.ZOMBIE;
			String bossname = "";
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("type:")){
					typeB = true;
					type = EntityType.valueOf(arg.replace("type:", ""));
				}
				else if(arg.startsWith("count:")){
					countB = true;
					count = Integer.parseInt(arg.replace("count:", ""));
				}
				else if(arg.startsWith("name:")){
					bossB = true;
					bossname = arg.replace("name:", "");
				}
			}
			
			if(typeB && countB){
				Summon s = new Summon(type, count);
				return s;
			}
			else if(countB && bossB){
				Summon s = new Summon(bossname, count);
				return s;
			}
		}
		else if(command.equalsIgnoreCase("Teleport")){
			boolean xB=false, yB=false, zB=false;
			double x = 0, y = 0, z = 0;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("x:")){
					x = Double.parseDouble(arg.replace("x:", ""));
				}
				else if(arg.startsWith("y:")){
					y = Double.parseDouble(arg.replace("y:", ""));
				}
				else if(arg.startsWith("z:")){
					z = Double.parseDouble(arg.replace("z:", ""));
				}
			}
			
			if(xB && yB && zB){
				Location loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
				Teleport t = new Teleport(loc);
				return t;
			}
		}
		else if(command.equalsIgnoreCase("TrueDamage")){
			boolean typeB = false, damageB = false;
			TargetType type = TargetType.Target;
			double damage = 1, radius = 5;
			
			for(int i = 1; i<words.length; i++){
				String arg = words[i];
				
				if(arg.startsWith("target:")){
					typeB=true;
					type = TargetType.valueOf(arg.replace("target:", ""));
				}
				else if(arg.startsWith("damage:")){
					damageB = true;
					damage = Double.parseDouble(arg.replace("damage:", ""));
				}
				else if(arg.startsWith("radius:")){
					radius = Double.parseDouble(arg.replace("radius:", ""));
				}
			}
			
			if(typeB && damageB){
				if(type == TargetType.AoeCreatures || type == TargetType.AoePlayers){
					TrueDamage td = new TrueDamage(type, damage, radius);
					return td;
				}
				else{
					TrueDamage td = new TrueDamage(type, damage);
					return td;
				}
				
			}
		}
		else if(command.equalsIgnoreCase("Wait")){
			double delay = Double.parseDouble(words[1].replace("delay:", ""));
			Wait w = new Wait(delay);
			return w;
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[BossAPI] : Error reading action... ");
		Bukkit.getConsoleSender().sendMessage(line);
		return new Wait(0);
		
	}

	public static final BossApi getPlugin() {
		return instance;
	}
	
	public String convertToMColors(String line){
		return line.replaceAll("&", "§");
	}
	
	public boolean isBoss(Entity ent){
		if(ent instanceof Creature){
			try{
				if(this.worldBossIds.get(ent.getWorld()).contains(ent.getEntityId())){
					return true;
				}
				return false;
			}
			catch(NullPointerException e){
				return false;
			}
		}
		return false;
		
	}
	
	@EventHandler
	public void OnBossDeathEvent(EntityDeathEvent e){
		if(isBoss(e.getEntity())){
			Entity ent = e.getEntity();
			BossInstance bi = bossIds.get(ent.getEntityId());
			
			if(bi.b.removeMountOnDeath){
				bi.mount.bossEntity.remove();
				Bukkit.getPluginManager().callEvent(new BossDeathEvent(bi.mount, bi.mount.bossEntity));
			}
			
			Bukkit.getPluginManager().callEvent(new BossDeathEvent(bi, ent));
			
			ArrayList<Integer> worldIds = worldBossIds.get(ent.getWorld());
			worldIds.remove((Object)ent.getEntityId());
			worldBossIds.put(ent.getWorld(), worldIds);
			bossEntities.remove(bossIds.get(ent.getEntityId()));
			bossIds.remove(ent.getEntityId());

			if(bi.b.expReward != 0){
				e.setDroppedExp(bi.b.expReward);
			}
		}
	}

	@EventHandler
	public void onBossDeath(BossDeathEvent e){
        e.getBossInstance().spawner.bossDied();
        itemManager.dropItems(e);
	}
	
	@EventHandler
	public void OnBossSpawn(BossSpawnEvent e){
		//System.out.println("************Got boss spawn Event");
		
		BossInstance bi = e.getBossInstance();
		LivingEntity ent = e.getLivingEntity();
		
		ArrayList<Integer> worldIds = worldBossIds.get(e.getWorld());
		if(worldIds == null){
			worldIds = new ArrayList<Integer>();
		}
		worldIds.add(ent.getEntityId());
		worldBossIds.put(e.getWorld(), worldIds);
		bossEntities.put(bi, ent.getEntityId());
		bossIds.put(ent.getEntityId(), bi);
		
		//Bukkit.getConsoleSender().sendMessage(bossInstance.boss.name + " was spawned at location, " + e.getLivingEntity().getLocation().toString());
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnBossDamaged(EntityDamageByEntityEvent e){
		//WhenHit Check - boss takes damage
		if(isBoss(e.getEntity())){
			this.bossIds.get(e.getEntity().getEntityId()).TookDamage();
			
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onBossDamage(EntityDamageByEntityEvent e){
		//OnHit Check - Boss deals damage
		if(isBoss(e.getDamager())){
			BossInstance bi = bossIds.get(e.getDamager().getEntityId());
			e.setDamage(bi.b.damage);
			bi.onHit();
		}
		else if(e.getDamager() instanceof Projectile){
			if(projectiles.containsKey(e.getDamager())){
				e.setDamage(projectiles.get(e.getDamager()));
				projectiles.remove(e.getDamager());
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onChunkUnload(ChunkUnloadEvent e){
		for (Spawner s : spawnerList) {
            if (!s.isInChunk(e.getChunk())) {
                continue;
            }
            Bukkit.getLogger().info("UNLOADED CHUNK WITH SPAWNER IN IT!");
            s.killBoss();
            s.setResetCount(0);
        }
	}

    public void addSpawner(Spawner s) {
        this.spawnerList.add(s);
    }

    public ArrayList<Spawner> getSpawners() {
        return this.spawnerList;
    }
	
	public List<String> convertToMColors(List<String> lore) {
		List<String> newLore = new ArrayList<String>();
		for(String s : lore){
			newLore.add(this.convertToMColors(s));
		}
		return newLore;
	}
}
