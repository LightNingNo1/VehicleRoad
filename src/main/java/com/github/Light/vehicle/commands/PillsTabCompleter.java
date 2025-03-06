package com.github.Light.vehicle.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PillsTabCompleter implements TabCompleter {
    private final List<String> pillTypes = List.of(
            "base_health", "base_speed", "base_jump", "base_hunger",
            "potent_health", "potent_speed", "potent_jump", "potent_hunger"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // 第一个参数：在线玩家名字
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            // 第二个参数：丹药类型
            return pillTypes.stream()
                    .filter(type -> type.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 3) {
            // 第三个参数：显示<amount>作为提示
            completions.add("<amount>");
            return completions;
        }

        return completions;
    }
} 