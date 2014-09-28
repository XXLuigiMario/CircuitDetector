package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.util.Utility;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles all the commands of the plugin
 */
public class CommandManager implements CommandExecutor {
	
	public CommandManager(){

    }
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName();
        if(cmd.equalsIgnoreCase("cd")){
            if(!(sender instanceof Player)){
                sender.sendMessage("You must be a player to use this command.");
                return true;
            }

            Player player = (Player) sender;

            if(args.length < 1)
                return false;

            String arg = args[0];

            if(arg.equalsIgnoreCase("help")){
                Utility.sendMessage(player, "Help");
                player.sendMessage(ChatColor.AQUA + "/cd log " + ChatColor.BLUE + "| Enables logging");
                player.sendMessage(ChatColor.AQUA + "/cd unlog  " + ChatColor.BLUE + "| Disables logging");
                player.sendMessage(ChatColor.AQUA + "/cd list  " + ChatColor.BLUE + "| Gets violators");
                player.sendMessage(ChatColor.AQUA + "/cd kill <x> <y> <z>  " + ChatColor.BLUE + "| Kills any redstone circuit at XYZ by destroying it");
                player.sendMessage(ChatColor.AQUA + "/cd setthreshold <int>  " + ChatColor.BLUE + "| Sets threshold of pulse repetition to automatically destroy redstone circuits. Default: 0");
                return true;
            }
            // This command enables logging
            else if(arg.equalsIgnoreCase("log")){
                CircuitDetector.logging.put(player.getName(), true);
                Utility.sendMessage(player, "Logging enabled.");
                return true;
            }
            // This command disables logging
            else if(arg.equalsIgnoreCase("unlog")){
            	CircuitDetector.logging.put(player.getName(), false);
                Utility.sendMessage(player, "Logging disabled.");
                return true;
            }
            // This command lists all violations
            else if(arg.equalsIgnoreCase("list")){
            	Utility.sendMessage(player, "Violators:");
            	
                if(CircuitDetector.violations.size() == 0){ // Check if there are no violations
                    player.sendMessage(ChatColor.BLUE + "No violations.");
                    return true;
                }
                else{
                    int c = 1;
                    for(Location loc : CircuitDetector.violations.keySet()){
                        Block b = loc.getBlock();
                        player.sendMessage(ChatColor.BLUE + "" + c + ". " + ChatColor.AQUA + "\"" + b.getType().name() + "\" at " + ChatColor.GRAY + Utility.formatLocation(loc)
                                + ChatColor.DARK_RED + " x" + CircuitDetector.violations.get(loc)); // Format the violation
                        c++;
                    }
                }
                return true;
            }
            
            // This command kills known redstone circuits
            // Requires 3 integer coordinates
            else if(arg.equalsIgnoreCase("kill")){
                if(args.length < 4)
                    return false;
                int x = 0;
                int y = 0;
                int z = 0;
                
                try{
	                x = Integer.parseInt(args[1]);
	                y = Integer.parseInt(args[2]);
	                z = Integer.parseInt(args[3]);
                } catch (NumberFormatException nfe){
                	Utility.sendMessage(player, "Coordinates must be integers.");
                	return true;
                }
                
                Location loc = new Location(player.getWorld(), x, y, z);
                Block b = loc.getBlock();

                if(!Utility.isRedstone(b)){ // Check if the target block is redstone before nuking it
                	Utility.sendMessage(player, "Block is not redstone related.");
                    return true;
                }

                Utility.destroyCircuit(b, true); // Destroy the circuit using the recursive method

                Utility.sendMessage(player, "Circuit starting at " + Utility.formatLocation(loc) + " was destroyed."); // Let the user know
                return true;
            }
            
            // Sets the threshold that a circuit must meet before being destroyed automatically. Defaults to 0.
            else if(arg.equalsIgnoreCase("setthreshold")){
                if(args.length < 2){
                    return false;
                }
                
                int threshold = 0;
                
                try{
                	threshold = Integer.parseInt(args[1]);
                }catch(NumberFormatException nfe){
                	Utility.sendMessage(player, "Threshold must be an integer.");
                	return true;
                }
                
                if(threshold < 0){
                	Utility.sendMessage(player, "Threshold cannot be negative.");
                    return true;
                }
                else if(threshold == 0){
                	Utility.sendMessage(player, "Set to monitor mode. Will not auto-destroy clocks.");
                    CircuitDetector.threshold = 0;
                    return true;
                }
                else{
                	Utility.sendMessage(player, "Threshold set to " + threshold + ".");
                	CircuitDetector.threshold = threshold;
                    return true;
                }
            }
        }
        return false;
    }
}
