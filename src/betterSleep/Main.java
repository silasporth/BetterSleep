package betterSleep;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends JavaPlugin implements Listener {

    private Set<Player> sleepingPlayers;
    private int percentage;
    private boolean mode;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();

        sleepingPlayers = new HashSet<>();
        percentage = getConfig().getInt("percentage");
        mode = getConfig().getBoolean("SwitchMode");
    }

    @EventHandler
    public void onPlayerBedEnter(final PlayerBedEnterEvent event){
        sleepingPlayers.add(event.getPlayer());
        bedMessage(event.getPlayer(), "entered");


        if(!enoughPlayers()) {
            return;
        }

        getServer().getScheduler().cancelTasks(this);
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            if(!enoughPlayers()) {
                return;
            }
            getServer().getWorlds().get(0).setTime(0);
            getServer().getWorlds().get(0).setStorm(false);
            getServer().broadcastMessage("§aNight successfully skipped!");

            getServer().getWorlds().get(0).getPlayers().forEach((player) -> {
                if(player.isSleeping()){
                    player.wakeup(false);
                }
            });
        }, 101);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        sleepingPlayers.remove(event.getPlayer());
        if(getServer().getWorlds().get(0).getTime()>12000){
            bedMessage(event.getPlayer(), "left");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(command.getName().equalsIgnoreCase("bettersleep")) {
            switch (args.length) {
                case 0:
                    sender.sendMessage("BetterSleep " + getDescription().getVersion());
                    return true;
                case 1:
                    if (args[0].equalsIgnoreCase("pct")) {
                        sender.sendMessage("Percentage: " + percentage);
                        return true;
                    }else if(args[0].equalsIgnoreCase("mode")) {
                        String stringMode;
                        if(!mode){
                            stringMode = "\"percentage\"";
                        }else{
                            stringMode = "\"playercount\"";
                        }
                        sender.sendMessage("Mode: " + stringMode);
                        return true;
                    }else{
                        sender.sendMessage("§4§lPlease enter a valid command!");}
                        return false;
                case 2:
                    if (args[0].equalsIgnoreCase("pct")) {
                        try {
                            percentage = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§4§lPlease input a valid Number");
                            return false;
                        }
                        if (percentage > 100 || percentage < 0) {
                            sender.sendMessage("§4§lThe number must be between 100 and 0");
                            return false;
                        }

                        getConfig().set("percentage", percentage);
                        saveConfig();

                        sender.sendMessage("Sleeping percentage: " + percentage + "%");
                        return true;

                    }else if(args[0].equalsIgnoreCase("mode")){
                        if(args[1].equalsIgnoreCase("percentage")){
                            mode = false;
                        }else if(args[1].equalsIgnoreCase("playercount")){
                            mode = true;
                        }else{
                            sender.sendMessage("§4§lPlease enter percentage or playercount!");
                            return false;
                        }
                        getConfig().set("SwitchMode", mode);
                        saveConfig();

                        String stringMode;
                        if(!mode){
                            stringMode = "\"percentage\"";
                        }else{
                            stringMode = "\"playercount\"";
                        }

                        sender.sendMessage("Set Mode to " + stringMode);
                        return true;
                    }
                default:
                    sender.sendMessage("§4§lToo many Arguments!");
                    return false;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tabComplete0 = new ArrayList<String>();
        List<String> tabComplete1_0 = new ArrayList<String>();
        List<String> tabComplete1_1 = new ArrayList<String>();

        tabComplete0.add("pct");
        tabComplete0.add("mode");
        tabComplete1_0.add("25");
        tabComplete1_0.add("50");
        tabComplete1_0.add("100");
        tabComplete1_1.add("percentage");
        tabComplete1_1.add("playercount");

        if(args.length == 1){
            return tabComplete0;
        }
        if(args.length == 2){
            if(args[0].equalsIgnoreCase("pct")) {
                return tabComplete1_0;
            }else if(args[0].equalsIgnoreCase("mode")){
                return tabComplete1_1;
            }
        }
        return null;
    }

    private boolean enoughPlayers(){
        return sleepingPlayers.size() / getServer().getWorlds().get(0).getPlayers().size() >= (percentage / 100d);
    }

    private void bedMessage(Player player, String string){
        double d = sleepingPlayers.size() / getServer().getWorlds().get(0).getPlayers().size()*100;
        String output;
        if(!mode){
            output = "(" + Integer.toString((int)Math.round(d)) + "%/" + percentage + "%)";
        }else{
            output = "(" + sleepingPlayers.size() + "/" + (int)(percentage/100d*getServer().getWorlds().get(0).getPlayers().size()) + ")";
        }

        getServer().broadcastMessage("§e" + player.getName() + " " + string + " the bed. " + output);
    }
}
