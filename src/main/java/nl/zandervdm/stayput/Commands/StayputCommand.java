package nl.zandervdm.stayput.Commands;

import nl.zandervdm.stayput.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StayputCommand implements CommandExecutor {

    protected Main plugin;

    public StayputCommand(Main plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if(strings.length == 0){
            sendInfoMessage(commandSender);
            return true;
        }

        String executedCommand = strings[0];

        if(executedCommand.equals("reload")){
            if(!commandSender.hasPermission("stayput.admin")){
                this.sendMessage(commandSender, "You don't have permission to execute this command.");
                return true;
            }
            this.plugin.reloadConfig();
            this.plugin.setupConfig();
            this.sendMessage(commandSender, "Config has been reloaded!");
            return true;
        } else if(executedCommand.equals("tp") && strings.length > 2) {
            if(!commandSender.hasPermission("stayput.tp")){
                this.sendMessage(commandSender, "You don't have permission to execute this command.");
                return true;
            }
            Player player = this.plugin.getServer().getPlayerExact(strings[1]);
            if(player == null) {
                this.sendMessage(commandSender, "No player with the name " + strings[1] + " online?");
                return true;
            }
            World world = this.plugin.getServer().getWorld(strings[2]);
            if(world == null) {
                this.sendMessage(commandSender, "No world with the name " + strings[2] + " found?");
                return true;
            }

            // Find the previous spot of the user in this world
            Location location = this.plugin.getPositionRepository().getPreviousLocation(player, world);
            if (location != null) {
                location.getWorld().getChunkAtAsync(location).whenComplete((chunk, ex) -> {
                    if (chunk != null) {
                        Location toLocation = this.plugin.getTeleport().makeLocationSafe(location);
                        if (player.teleport(toLocation)) {
                            this.sendMessage(commandSender, "Teleported " + player.getName() + " to their last location in world " + world.getName());
                        } else {
                            this.sendMessage(commandSender, "Error while trying to teleport " + player.getName() + " to " + toLocation);
                        }
                    }
                    if (ex != null) {
                        this.sendMessage(commandSender, ex.getMessage());
                        ex.printStackTrace();
                    }
                });
            } else {
                this.sendMessage(commandSender, player.getName() + " has no last location in world " + world.getName());
            }
        }

        return false;
    }

    protected void sendInfoMessage(CommandSender commandSender){
        this.sendMessage(commandSender, "Available commands:");
        this.sendMessage(commandSender, "/stayput tp <player> <world> - Teleport a player to their last location in a world");
        this.sendMessage(commandSender, "/stayput reload - Reloads the config files");
    }

    protected void sendMessage(CommandSender commandSender, String message){
        commandSender.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "StayPut" + ChatColor.GRAY + "] " + message);
    }
}
