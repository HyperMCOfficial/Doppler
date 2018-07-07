# Doppler
A Bukkit plugin that integrates third-party servers into MineNation

#INFO:

Doesn't work anymore due to new system, updated version will come out at some point when i have time. :)

# About
The Doppler system was developed for servers that are not hosted on the MineNation platform. In order to maintain the security of MineNation, the Doppler server was designed to provide a bridge between regular Minecraft servers and our database. This allows MineNation to add any third party server without having to worry about them decompiling code to find our database info.

# Setup
To create a third party server it requires a doppler apikey, you can get your doppler apikey in the MineNation lobby using the command /Doppler. Then go download our plugin from the spigotmc page (https://www.spigotmc.org/resources/doppler.53592/) or compile it yourself from this GitHub repository, and start your server. Once the server is started it will generate a config file, and you will have to go into that config file and paste your Doppler apikey. Now you need to set your server into offline mode on the server.properties by setting online-mode to false. The last step is to set bungee-cord to true in your spigot.yml and it's that easy!
