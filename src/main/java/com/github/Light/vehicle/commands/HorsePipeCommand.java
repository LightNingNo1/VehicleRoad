package com.github.Light.vehicle.commands;

import com.github.Light.vehicle.items.HorsePipe;
import com.github.Light.vehicle.VehicleOnRoad;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class HorsePipeCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private final HorsePipe horsePipe;

    public HorsePipeCommand(JavaPlugin plugin, VehicleOnRoad eventProcessor) {
        this.plugin = plugin;
        this.horsePipe = new HorsePipe(plugin, eventProcessor);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            // 玩家直接使用命令
            if (!player.hasPermission("horsepipe.give")) {
                player.sendMessage(ChatColor.RED + "你没有权限使用该指令！");
                return true;
            }
            giveHorsePipe(player);
            return true;
        }

        // 控制台使用命令
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "用法: /horsepipe <玩家名>");
            return true;
        }

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "找不到玩家: " + args[0]);
            return true;
        }

        giveHorsePipe(target);
        sender.sendMessage(ChatColor.GREEN + "已将唤马竹筒给予玩家 " + target.getName());
        return true;
    }

    private void giveHorsePipe(Player player) {
        player.getInventory().addItem(horsePipe.createEmptyPipe());
        player.sendMessage(ChatColor.GREEN + "已获得唤马竹筒！");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partialPlayerName = args[0].toLowerCase();
            // 获取所有在线玩家并筛选匹配的名字
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                String playerName = player.getName();
                if (playerName.toLowerCase().startsWith(partialPlayerName)) {
                    completions.add(playerName);
                }
            }
        }
        
        return completions;
    }
}
