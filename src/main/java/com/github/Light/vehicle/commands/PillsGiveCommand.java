package com.github.Light.vehicle.commands;

import com.github.Light.vehicle.items.Pills;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PillsGiveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 权限检查
        if (sender instanceof Player player && !player.hasPermission("givepills.use")) {
            sender.sendMessage(ChatColor.RED + "你没有权限使用该命令！");
            return true;
        }

        // 参数数量检查
        if (args.length < 2 || args.length > 3) {
            sendUsage(sender);
            return true;
        }

        // 获取目标玩家
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "找不到玩家: " + args[0]);
            return true;
        }

        // 处理数量参数
        int amount = 1;
        if (args.length == 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount < 1 || amount > 64) {
                    sender.sendMessage(ChatColor.RED + "数量必须在1-64之间！");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "无效的数量: " + args[2]);
                sender.sendMessage(ChatColor.GRAY + "数量必须是1-64之间的整数");
                return true;
            }
        }

        // 检查玩家背包空间
        if (target.getInventory().firstEmpty() == -1) {
            sender.sendMessage(ChatColor.RED + "目标玩家背包已满！");
            return true;
        }

        // 创建丹药
        ItemStack pill = switch (args[1].toLowerCase()) {
            case "base_health" -> Pills.createBaseHealthPill(amount);
            case "base_speed" -> Pills.createBaseSpeedPill(amount);
            case "base_jump" -> Pills.createBaseJumpPill(amount);
            case "base_hunger" -> Pills.createHungerConsPill(amount);
            case "potent_health" -> Pills.createHealthPill(amount);
            case "potent_speed" -> Pills.createSpeedPill(amount);
            case "potent_jump" -> Pills.createJumpPill(amount);
            case "potent_hunger" -> Pills.createHungerPill(amount);
            default -> null;
        };

        if (pill == null) {
            sender.sendMessage(ChatColor.RED + "无效的丹药类型: " + args[1]);
            sendUsage(sender);
            return true;
        }

        // 给予丹药
        target.getInventory().addItem(pill);
        
        // 发送成功消息
        target.sendMessage(ChatColor.GREEN + "已获得 " + pill.getItemMeta().getDisplayName() + 
                ChatColor.GREEN + " * " + amount);
        
        // 如果执行者不是目标玩家，发送确认消息
        if (!(sender instanceof Player) || !((Player) sender).equals(target)) {
            sender.sendMessage(ChatColor.GREEN + "已将 " + pill.getItemMeta().getDisplayName() + 
                    ChatColor.GREEN + " * " + amount + " 给予 " + target.getName());
        }
        
        return true;
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "用法: /givepill <玩家> <类型> [数量]");
        sender.sendMessage(ChatColor.GRAY + "基础丹药类型:");
        sender.sendMessage(ChatColor.GRAY + "  base_health - 生命提升");
        sender.sendMessage(ChatColor.GRAY + "  base_speed  - 速度提升");
        sender.sendMessage(ChatColor.GRAY + "  base_jump   - 跳跃提升");
        sender.sendMessage(ChatColor.GRAY + "  base_hunger - 能耗提升");
        sender.sendMessage(ChatColor.GRAY + "强效丹药类型:");
        sender.sendMessage(ChatColor.GRAY + "  potent_health - 强效生命提升");
        sender.sendMessage(ChatColor.GRAY + "  potent_speed  - 强效速度提升");
        sender.sendMessage(ChatColor.GRAY + "  potent_jump   - 强效跳跃提升");
        sender.sendMessage(ChatColor.GRAY + "  potent_hunger - 强效能耗提升");
        sender.sendMessage(ChatColor.GRAY + "数量: 1-64 之间的整数");
    }
}

