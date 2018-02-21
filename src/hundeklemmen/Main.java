package hundeklemmen;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//PREFIX: ChatColor.DARK_GRAY+"["+ChatColor.AQUA+ChatColor.BOLD+"Mine"+ChatColor.WHITE+ChatColor.BOLD+"Nation"+ChatColor.DARK_GRAY+"] "

public class Main extends JavaPlugin implements Listener {

	public static String prefix = "&8[&b&lPlayerHost&8]";
	static Process ps;
	public Socket clientSocket;
	public DataOutputStream outToServer;
	public BufferedReader inFromServer;
	public BufferedReader inFromUser;
	public String apikey;
	public boolean verified = false;
	public Plugin plugin = this;
	//public static List<String> redTeam = new ArrayList<String>();
	
	@Override
	public void onEnable() {
		//registerCommands();
		registerListeners();
		Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {

			@Override
            public void run() {
            	try {        
        			  clientSocket = new Socket("85.191.216.71", 1337);
        			  outToServer = new DataOutputStream(clientSocket.getOutputStream());
        			  inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        			  sendtest();
        			  while(true) {
        				  recieveMessage(inFromServer.readLine());
        			  }
        			  //System.out.println(inFromServer.readLine());
            	} catch (UnknownHostException e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
	            	getLogger().warning("Timed out trying to read from MineNation");
	            	plugin.getServer().shutdown();
    			} catch(java.net.SocketTimeoutException e) {
    				//e.printStackTrace();
	            	getLogger().warning("Timed out trying to read from MineNation");
	            	plugin.getServer().shutdown();
    			} catch(SocketException e) {
    				//e.printStackTrace();
	            	getLogger().warning("Lost connection to MineNation!");
	            	plugin.getServer().shutdown();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
	            	getLogger().warning("An error in the socket connection to MineNation");
	            	plugin.getServer().shutdown();
    			} 
        	}
			
			
        });
		createConfig();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
		    public void run() {
		    	UpdatePlayerCount();
		    }
		},2000L,2000L);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onDisable() {
		if(!clientSocket.isClosed()) {
			getLogger().warning("Disconnecting from MineNation");
			JSONObject json = new JSONObject();
			json.put("type", "closeserver");
			json.put("apikey", apikey);
			sendServerMessage(json.toString());
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
    			public void run() {
    				try {
    					clientSocket.close();
    				} catch (IOException e) {}
    			}
    		}, 1 * 20);
		}
	};
	
	
	public void reconnectsocket() {
		getLogger().warning("Disconnected from MineNation");
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
            	try {        
            		clientSocket = new Socket("85.191.216.71", 1337);
	      			outToServer = new DataOutputStream(clientSocket.getOutputStream());
	      			inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	      			sendtest();
	      		    while (true){
	      		    	recieveMessage(inFromServer.readLine());
	      		    }
    	        } catch (UnknownHostException e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
	            	getLogger().warning("Timed out trying to read from MineNation");
	            	plugin.getServer().shutdown();
    			} catch(java.net.SocketTimeoutException e) {
    				//e.printStackTrace();
	            	getLogger().warning("Timed out trying to read from MineNation");
	            	plugin.getServer().shutdown();
    			} catch(SocketException e) {
    				//e.printStackTrace();
	            	getLogger().warning("Lost connection to MineNation!");
	            	plugin.getServer().shutdown();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				//e.printStackTrace();
	            	getLogger().warning("An error in the socket connection to MineNation");
	            	plugin.getServer().shutdown();
    			} 
        	}
        }); 
		System.out.println("Connected to server");
	}
	@SuppressWarnings("unchecked")
	public void recieveMessage(String data) {
		if(isJson(data)) {
			try {
				JSONParser parser = new JSONParser();
				//System.out.println("FROM SERVER (json): " + data);
				JSONObject json = (JSONObject) parser.parse(data);
				//getLogger().info("Type: "+json.get("type").toString());
				String type = json.get("type").toString();
				if(type.equalsIgnoreCase("verifyapikeyrespond")) {
					String respond = json.get("respond").toString();
					if(respond.equalsIgnoreCase("true")) {
						verified = true;
						getLogger().info("Api-Key verified");
						plugin.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
		        			public void run() {
		        				getLogger().info("Connecting to MineNation");
								JSONObject json2 = new JSONObject();
								json2.put("type", "connectserver");
								json2.put("apikey", apikey);
								json2.put("port", plugin.getServer().getPort());
								sendServerMessage(json2.toString());
		        			}
		        		}, 1 * 1);
					} else if(respond.equalsIgnoreCase("false")) {
						verified = false;
						getLogger().info("Api-Key not verified");
						getLogger().info("Stopping server");
						plugin.getServer().shutdown();
					}
				} else if(type.equalsIgnoreCase("verifyapikeyrespond")) {
					String respond = json.get("respond").toString();
					if(respond.equalsIgnoreCase("true")) {
						getLogger().info("Server connected to MineNation");
						UpdatePlayerCount();
					} else {
						getLogger().info("Server failed while connecting to MineNation");
					}
				} else if(type.equalsIgnoreCase("connectserverrespond")) {
					String respond = json.get("respond").toString();
					if(respond.equalsIgnoreCase("true")) {
						getLogger().info("Successfully connected to MineNation");
					} else {
						getLogger().info("Connection failed to MineNation");
						getLogger().info("Stopping server");
						plugin.getServer().shutdown();
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		//} else {
			//System.out.println("FROM SERVER (plain): " + data);
		}
	}
	public void sendServerMessage(String data) {
		  try {
			 if(clientSocket.isClosed()) {
				 reconnectsocket();
			 } else {
				 outToServer.writeUTF(data.toString());
				 outToServer.flush();
			 }
		} catch(SocketException e) {
			//e.printStackTrace();
            reconnectsocket();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void sendtest() {
		JSONObject json = new JSONObject();
		json.put("type", "test");
		sendServerMessage(json.toString());
	}
	
	@SuppressWarnings("unchecked")
	public void UpdatePlayerCount() {
		JSONObject json = new JSONObject();
		json.put("type", "updateplayers");
		json.put("apikey", apikey);
		json.put("players", Bukkit.getServer().getOnlinePlayers().size());
		sendServerMessage(json.toString());
	}
	
	public boolean isJson(String data) {
		boolean isvalid = false;
		JSONParser parser = new JSONParser();
		try {
			@SuppressWarnings("unused")
			JSONObject json = (JSONObject) parser.parse(data);
			isvalid = true;
		} catch (ParseException e) {
			isvalid = false;
		}
		return isvalid;
	}

	
	@SuppressWarnings({ "unchecked", "unused" })
	private void createConfig() {
	    try {
	        if (!getDataFolder().exists()) {
	            getDataFolder().mkdirs();
	        }
	        File file = new File(getDataFolder(), "config.yml");
	        if (!file.exists()) {
	            getLogger().info("Config.yml not found, creating!");
	            saveDefaultConfig();
            	getLogger().warning("No apikey entered in config, stopping server!");
            	plugin.getServer().shutdown();
	        } else {
	            getLogger().info("Config.yml found, loading!");
	            String configapikey = plugin.getConfig().getString("api-key").toString();
	            if(!configapikey.equals("none")||configapikey != null||configapikey != "") {
	            	apikey = configapikey;
	        		JSONObject json = new JSONObject();
	        		json.put("type", "verifykey");
	        		json.put("key", configapikey);
	        		plugin.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	        			public void run() {
	        				sendServerMessage(json.toString()); 
	        				getLogger().info("Checking Api-Key");
	        			}
	        		}, 20L);
	        		//sendtest();
	            } else {
	            	getLogger().warning("No apikey entered in config, stopping server!");
	            	plugin.getServer().shutdown();
	            }
	        }
	    } catch (Exception e) {
	       // e.printStackTrace();

	    }

	}
	
	//private void registerCommands() {
    //    getCommand("reset").setExecutor(this);
	//}
    @EventHandler
    public void onplayerjoin(PlayerJoinEvent event) {
    	UpdatePlayerCount();
    }
    @EventHandler
    public void onplayerleave(PlayerQuitEvent event) {
    	UpdatePlayerCount();
    }
    
	private void registerListeners() {
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	
	public static String color(String in) {
		return ChatColor.translateAlternateColorCodes('&', in);
	}
	
	
};
