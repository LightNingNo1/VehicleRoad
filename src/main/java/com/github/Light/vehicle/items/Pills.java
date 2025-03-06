package com.github.Light.vehicle.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class Pills {

    public static final String BASE_HEALTH_PILL = "坐骑提升丹药-生命";
    public static final String BASE_SPEED_PILL = "坐骑提升丹药-速度";
    public static final String BASE_JUMP_PILL = "坐骑提升丹药-跳跃";
    public static final String BASE_HUNGER_CONS_PILL = "坐骑提升丹药-能耗";
    //高级
    public static final String HEALTH_PILL = "强效坐骑提升丹药-生命";
    public static final String SPEED_PILL = "强效提升丹药-速度";
    public static final String JUMP_PILL = "强效提升丹药-跳跃";
    public static final String HUNGER_CONS_PILL = "强效提升丹药-能耗";


    public static ItemStack createBaseHealthPill(int amount) {
        ItemStack pill = new ItemStack(Material.GLISTERING_MELON_SLICE, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + BASE_HEALTH_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑最大生命值");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用3次");
        meta.setLore(lore);
        meta.setCustomModelData(76);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createBaseSpeedPill(int amount) {
        ItemStack pill = new ItemStack(Material.SUGAR, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + BASE_SPEED_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑移动速度");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用3次");
        meta.setLore(lore);
        meta.setCustomModelData(76);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createBaseJumpPill(int amount) {
        ItemStack pill = new ItemStack(Material.RABBIT_FOOT, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + BASE_JUMP_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑跳跃能力");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用3次");
        meta.setLore(lore);
        meta.setCustomModelData(76);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createHungerConsPill(int amount) {
        ItemStack pill = new ItemStack(Material.GOLDEN_CARROT, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + BASE_HUNGER_CONS_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "减少坐骑食物消耗");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用3次");
        meta.setLore(lore);
        meta.setCustomModelData(76);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createHealthPill(int amount) {
        ItemStack pill = new ItemStack(Material.GLISTERING_MELON_SLICE, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.RED + HEALTH_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑最大生命值");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用1次");
        meta.setLore(lore);
        meta.setCustomModelData(77);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createSpeedPill(int amount) {
        ItemStack pill = new ItemStack(Material.SUGAR, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.RED + SPEED_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑移动速度");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用1次");
        meta.setLore(lore);
        meta.setCustomModelData(77);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createJumpPill(int amount) {
        ItemStack pill = new ItemStack(Material.RABBIT_FOOT,amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.RED + JUMP_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "提升坐骑跳跃能力");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用1次");
        meta.setLore(lore);
        meta.setCustomModelData(77);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static ItemStack createHungerPill(int amount) {
        ItemStack pill = new ItemStack(Material.GOLDEN_CARROT, amount);
        ItemMeta meta = pill.getItemMeta();
        meta.setDisplayName(ChatColor.RED + HUNGER_CONS_PILL);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "右键点击坐骑使用");
        lore.add(ChatColor.GRAY + "减少坐骑食物消耗");
        lore.add(ChatColor.GRAY + "每匹坐骑最多使用1次");
        meta.setLore(lore);
        meta.setCustomModelData(77);

        // 添加无属性的附魔效果
        meta.addEnchant(Enchantment.UNBREAKING, 3, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        pill.setItemMeta(meta);
        return pill;
    }

    public static boolean isVehiclePill(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String name = item.getItemMeta().getDisplayName();
        return name.contains(BASE_HEALTH_PILL) ||
               name.contains(BASE_SPEED_PILL) ||
               name.contains(BASE_JUMP_PILL) ||
                name.contains(BASE_HUNGER_CONS_PILL) ||
                name.contains(HEALTH_PILL) ||
                name.contains(SPEED_PILL) ||
                name.contains(JUMP_PILL) ||
                name.contains(HUNGER_CONS_PILL);
    }

    public static void registerRecipes(JavaPlugin plugin) {
        // 生命提升丹药配方
        NamespacedKey healthKey = new NamespacedKey(plugin, "health_pill");
        ShapedRecipe healthRecipe = new ShapedRecipe(healthKey, createBaseHealthPill(1));
        healthRecipe.shape("HNH", "WAW", "HNH");
        healthRecipe.setIngredient('H', Material.HAY_BLOCK);
        healthRecipe.setIngredient('N', Material.NETHER_WART);
        healthRecipe.setIngredient('A', Material.GOLDEN_APPLE);
        healthRecipe.setIngredient('W', Material.GLISTERING_MELON_SLICE);
        
        // 速度提升丹药配方
        NamespacedKey speedKey = new NamespacedKey(plugin, "speed_pill");
        ShapedRecipe speedRecipe = new ShapedRecipe(speedKey, createBaseSpeedPill(1));
        speedRecipe.shape("HNH", "SAS", "HNH");
        speedRecipe.setIngredient('H', Material.HAY_BLOCK);
        speedRecipe.setIngredient('S', Material.SUGAR);
        speedRecipe.setIngredient('N', Material.NETHER_WART);
        speedRecipe.setIngredient('A', Material.GOLDEN_APPLE);
        
        // 跳跃提升丹药配方
        NamespacedKey jumpKey = new NamespacedKey(plugin, "jump_pill");
        ShapedRecipe jumpRecipe = new ShapedRecipe(jumpKey, createBaseJumpPill(1));
        jumpRecipe.shape("HNH", "RAN", "HNH");
        jumpRecipe.setIngredient('H', Material.HAY_BLOCK);
        jumpRecipe.setIngredient('R', Material.RABBIT_FOOT);
        jumpRecipe.setIngredient('N', Material.NETHER_WART);
        jumpRecipe.setIngredient('A', Material.GOLDEN_APPLE);

        //能耗减少丹药配方
        NamespacedKey hungryKey = new NamespacedKey(plugin, "hunger_pill");
        ShapedRecipe hungryRecipe = new ShapedRecipe(hungryKey, createHungerConsPill(1));
        hungryRecipe.shape("HNH", "CAC", "HNH");
        hungryRecipe.setIngredient('H', Material.HAY_BLOCK);
        hungryRecipe.setIngredient('C', Material.GOLDEN_CARROT);
        hungryRecipe.setIngredient('A', Material.GOLDEN_APPLE);
        hungryRecipe.setIngredient('N', Material.NETHER_WART);

        
        // 注册配方
        plugin.getServer().addRecipe(healthRecipe);
        plugin.getServer().addRecipe(speedRecipe);
        plugin.getServer().addRecipe(jumpRecipe);
    }
}
