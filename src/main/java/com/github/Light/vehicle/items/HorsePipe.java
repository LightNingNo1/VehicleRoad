package com.github.Light.vehicle.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import com.github.Light.vehicle.VehicleOnRoad;
import com.github.Light.vehicle.VehicleState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

/**
 * 唤马竹筒物品类
 * 用于管理唤马竹筒的创建、存储和验证等功能
 */
public class HorsePipe {
    // 物品显示名称
    private static final String DISPLAY_NAME = ChatColor.GOLD + "唤马竹筒";
    // 插件实例
    private final JavaPlugin plugin;
    // 用于存储坐骑UUID的键
    private final NamespacedKey horseKey;
    private final NamespacedKey entityTypeKey;
    private final NamespacedKey horseDataKey;
    private final NamespacedKey horseInventoryKey;
    private final NamespacedKey pipeIdKey;  // 用于存储竹筒的唯一ID
    private final NamespacedKey tamingDataKey;
    private final NamespacedKey hungerValueKey;
    private final NamespacedKey lastFeedTimeKey;
    private final NamespacedKey PillTimesKey;
    private final NamespacedKey ConsumptionMultiplier;
    private final VehicleOnRoad eventProcessor;

    /**
     * 构造函数
     * @param plugin 插件实例，用于创建NamespacedKey
     * @param eventProcessor EventProcessor实例，用于获取VehicleState
     */
    public HorsePipe(JavaPlugin plugin, VehicleOnRoad eventProcessor) {
        this.plugin = plugin;
        this.eventProcessor = eventProcessor;
        this.horseKey = new NamespacedKey(plugin, "stored_horse");
        this.entityTypeKey = new NamespacedKey(plugin, "entity_type");
        this.horseDataKey = new NamespacedKey(plugin, "horse_data");
        this.horseInventoryKey = new NamespacedKey(plugin, "horse_inventory");
        this.pipeIdKey = new NamespacedKey(plugin, "pipe_id");
        this.tamingDataKey = new NamespacedKey(plugin, "taming_data");
        this.hungerValueKey = new NamespacedKey(plugin, "hunger_value");
        this.PillTimesKey = new NamespacedKey(plugin, "pill_times");
        this.lastFeedTimeKey = new NamespacedKey(plugin, "last_feed_time");
        this.ConsumptionMultiplier = new NamespacedKey(plugin, "consumption_multiplier");
    }

    /**
     * 创建一个空的唤马竹筒
     * @return 创建的唤马竹筒物品
     */
    public ItemStack createEmptyPipe() {
        ItemStack pipe = new ItemStack(Material.BAMBOO);
        ItemMeta meta = pipe.getItemMeta();
        if (meta != null) {
            // 生成唯一ID
            String pipeId = UUID.randomUUID().toString();
            meta.getPersistentDataContainer().set(pipeIdKey, PersistentDataType.STRING, pipeId);

            meta.setDisplayName(DISPLAY_NAME);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "右键点击坐骑进行捕获");
            meta.setLore(lore);

            // 添加无属性的附魔效果
            meta.addEnchant(Enchantment.UNBREAKING, 3, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.setCustomModelData(76);

            pipe.setItemMeta(meta);
        }
        return pipe;
    }

    /**
     * 检查物品是否为唤马竹筒
     * @param item 要检查的物品
     * @return 如果是唤马竹筒返回true，否则返回false
     */
    public boolean isHorsePipe(ItemStack item) {
        if (item == null || item.getType() != Material.BAMBOO)
            return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && DISPLAY_NAME.equals(meta.getDisplayName());
    }

    /**
     * 将坐骑信息存储到竹筒中
     */
    public ItemStack storeHorse(LivingEntity mount) {
        ItemStack pipe = createEmptyPipe();
        ItemMeta meta = pipe.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(horseKey, PersistentDataType.STRING, mount.getUniqueId().toString());
            container.set(entityTypeKey, PersistentDataType.STRING, mount.getType().name());

            // 获取并存储饥饿值信息
            VehicleState state = eventProcessor.initializeVehicleStateIfNeeded(mount, mount.getWorld());
            if (state != null) {
                container.set(hungerValueKey, PersistentDataType.DOUBLE, state.getHungerValue());
                container.set(lastFeedTimeKey, PersistentDataType.LONG, state.getLastFeedTime());

                // 在lore中显示饥饿值
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GOLD + "饥饿值: " + String.format("%.1f", state.getHungerValue()));

                //存储丹药使用次数
                StringBuilder PillTimes = new StringBuilder();
                //速度，生命，跳跃，能耗
                PillTimes.append(String.format("%d,%d,%d,%d,%b,%b,%b,%b",
                        state.getSpeedPillUses(),
                        state.getHealthPillUses(),
                        state.getJumpPillUses(),
                        state.getHungerPillUses(),
                        state.isSpeedUp(),
                        state.isHealthUp(),
                        state.isJumpUp(),
                        state.isHungerUp()
                        ));
                container.set(PillTimesKey, PersistentDataType.STRING, PillTimes.toString());

                //储存能耗
                container.set(ConsumptionMultiplier, PersistentDataType.DOUBLE, state.getConsumptionMultiplier());

                // 存储坐骑数据
                if (mount instanceof AbstractHorse abstractHorse) {
                    StringBuilder mountData = new StringBuilder();
                    double baseSpeed = state.getBaseSpeed();
                    mountData.append(String.format("%f,%d,%f,%f",
                            abstractHorse.getJumpStrength(),
                            abstractHorse.getDomestication(),
                            abstractHorse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                            baseSpeed
                    ));

                    // 如果是普通马，存储颜色和样式
                    if (mount instanceof Horse horse) {
                        mountData.append(String.format(",%s,%s",
                                horse.getColor().name(),
                                horse.getStyle().name()
                        ));
                    }

                    container.set(horseDataKey, PersistentDataType.STRING, mountData.toString());

                    // 存储物品栏
                    StringBuilder inventoryData = new StringBuilder();
                    if (abstractHorse.getInventory() != null) {
                        ItemStack saddle = abstractHorse.getInventory().getSaddle();
                        ItemStack armor = null;

                        // 只有普通马可以装备马铠
                        if (mount instanceof Horse horse) {
                            armor = horse.getInventory().getArmor();
                        }

                        // 存储鞍具
                        if (saddle != null) {
                            inventoryData.append(itemToBase64(saddle));
                        }
                        inventoryData.append(";");

                        // 存储马铠（如果有）
                        if (armor != null) {
                            inventoryData.append(itemToBase64(armor));
                        }

                        container.set(horseInventoryKey, PersistentDataType.STRING, inventoryData.toString());
                    }
                } else if (mount instanceof Strider strider) {
                    // 存储炽足兽的特殊数据
                    StringBuilder mountData = new StringBuilder();
                    mountData.append(String.format("%f",
                            strider.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
                    ));
                    container.set(horseDataKey, PersistentDataType.STRING, mountData.toString());

                    // 存储鞍具
                    if (strider.hasSaddle()) {
                        container.set(horseInventoryKey, PersistentDataType.STRING,
                                itemToBase64(new ItemStack(Material.SADDLE)));
                    }
                }

                // 存储驯服状态
                if (mount instanceof Tameable tameable) {
                    String tamingData = tameable.isTamed() + ";" +
                            (tameable.getOwner() != null ? tameable.getOwner().getUniqueId().toString() : "");
                    container.set(tamingDataKey, PersistentDataType.STRING, tamingData);
                }

                // 存储当前生命值
                container.set(
                        new NamespacedKey(plugin, "current_health"),
                        PersistentDataType.DOUBLE,
                        mount.getHealth()
                );

                // 更新物品说明
                lore.add(ChatColor.GRAY + "包含已捕获的坐骑");

                // 如果坐骑有自定义名称，显示在第二行
                String customName = mount.getCustomName();
                if (customName != null && !customName.isEmpty()) {
                    lore.add(ChatColor.GOLD + "名称: " + customName);
                }

                // 添加坐骑类型信息
                String mountTypeName = switch (mount.getType()) {
                    case HORSE -> "马";
                    case DONKEY -> "驴";
                    case MULE -> "骡子";
                    case STRIDER -> "炽足兽";
                    default -> "未知坐骑";
                };
                lore.add(ChatColor.YELLOW + "种类: " + mountTypeName);

                // 显示基础速度而不是当前速度
                double baseSpeed = state.getBaseSpeed();
                lore.add(ChatColor.WHITE + "速度: " + String.format("%.2f", baseSpeed * 43) + " 格/秒");

                // 显示当前生命值
                double currentHealth = mount.getHealth();
                lore.add(ChatColor.RED + "当前生命值: " + String.format("%.1f", currentHealth));

                // 显示跳跃能力（仅对马匹类型显示）
                if (mount instanceof AbstractHorse horse) {
                    double jumpStrength = horse.getJumpStrength();
                    String jumpLevel = getJumpLevel(jumpStrength);
                    double jumpHeight = calculateJumpHeight(jumpStrength);
                    lore.add(ChatColor.AQUA + "跳跃能力: " + jumpLevel + String.format(" (%.1f格)", jumpHeight));
                }

                lore.add(ChatColor.GRAY + "速度升级:" + ChatColor.BLUE + getLevel(state.getSpeedPillUses()) + ChatColor.GRAY + "|" +
                        ChatColor.GOLD + (state.isSpeedUp() ? "★" : "☆"));
                lore.add(ChatColor.GRAY + "跳跃升级:" + ChatColor.BLUE + getLevel(state.getJumpPillUses()) + ChatColor.GRAY + "|" +
                        ChatColor.GOLD + (state.isJumpUp() ? "★" : "☆"));
                lore.add(ChatColor.GRAY + "生命升级:" + ChatColor.BLUE + getLevel(state.getHealthPillUses()) + ChatColor.GRAY + "|" +
                        ChatColor.GOLD + (state.isHealthUp() ? "★" : "☆"));
                lore.add(ChatColor.GRAY + "能耗升级:" + ChatColor.BLUE + getLevel(state.getHungerPillUses()) + ChatColor.GRAY + "|" +
                        ChatColor.GOLD + (state.isHungerUp() ? "★" : "☆"));

                lore.add(ChatColor.GRAY + "右键方块释放坐骑");
                lore.add(ChatColor.GRAY + "Shift+右键解除绑定");
                meta.setLore(lore);

                meta.setCustomModelData(77);

                // 添加无属性的附魔效果
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                pipe.setItemMeta(meta);

                // 在完全存储后，移除坐骑的 VehicleState
                eventProcessor.resetVehicleSpeed(mount);
            }
        }
        return pipe;
    }

    /**
     * 检查竹筒是否存储了坐骑
     * @param pipe 要检查的竹筒
     * @return 如果存储了坐骑返回true，否则返回false
     */
    public boolean hasStoredHorse(ItemStack pipe) {
        if (!isHorsePipe(pipe)) return false;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(horseKey, PersistentDataType.STRING);
    }

    /**
     * 获取存储在竹筒中的坐骑UUID
     * @param pipe 要获取UUID的竹筒
     * @return 坐骑的UUID，如果没有则返回null
     */
    public UUID getStoredHorseUUID(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        String uuidStr = meta.getPersistentDataContainer().get(horseKey, PersistentDataType.STRING);
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    /**
     * 获取存储的实体类型
     */
    public EntityType getStoredEntityType(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        String typeStr = meta.getPersistentDataContainer().get(entityTypeKey, PersistentDataType.STRING);
        return typeStr != null ? EntityType.valueOf(typeStr) : null;
    }

    /**
     * 获取存储的马匹数据
     */
    public String getHorseData(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(horseDataKey, PersistentDataType.STRING);
    }

    public String getHorsePillData(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(PillTimesKey, PersistentDataType.STRING);
    }

    public Double getConsumptionMultiplier(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(ConsumptionMultiplier, PersistentDataType.DOUBLE);
    }

    /**
     * 获取存储的马匹物品栏数据
     */
    public String getHorseInventory(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(horseInventoryKey, PersistentDataType.STRING);
    }

    /**
     * 获取竹筒的唯一ID
     */
    public String getPipeId(ItemStack pipe) {
        if (!isHorsePipe(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(pipeIdKey, PersistentDataType.STRING);
    }

    /**
     * 获取存储的驯服状态数据
     */
    public String getTamingData(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(tamingDataKey, PersistentDataType.STRING);
    }

    /**
     * 获取存储的当前生命值
     */
    public Double getCurrentHealth(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(
                new NamespacedKey(plugin, "current_health"),
                PersistentDataType.DOUBLE
        );
    }

    /**
     * 获取存储的饥饿值
     */
    public Double getStoredHungerValue(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(hungerValueKey, PersistentDataType.DOUBLE);
    }

    /**
     * 获取存储的最后喂食时间
     */
    public Long getStoredLastFeedTime(ItemStack pipe) {
        if (!hasStoredHorse(pipe)) return null;
        ItemMeta meta = pipe.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(lastFeedTimeKey, PersistentDataType.LONG);
    }

    /**
     * 将ItemStack转换为Base64字符串
     */
    private String itemToBase64(ItemStack item) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 将Base64字符串转换为ItemStack
     */
    public ItemStack base64ToItem(String base64) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算跳跃高度（以方块为单位）
     */
    private double calculateJumpHeight(double jumpStrength) {
        // Minecraft的跳跃高度计算公式
        return -0.1817584952 * Math.pow(jumpStrength, 3) + 3.689713992 * Math.pow(jumpStrength, 2) + 2.128599134 * jumpStrength - 0.343930367;
    }

    /**
     * 获取跳跃等级描述
     */
    private String getJumpLevel(double jumpStrength) {
        if (jumpStrength >= 0.9) return "★★★★★";
        if (jumpStrength >= 0.7) return "★★★★☆";
        if (jumpStrength >= 0.5) return "★★★☆☆";
        if (jumpStrength >= 0.3) return "★★☆☆☆";
        return "★☆☆☆☆";
    }
    //升级显示
    private String getLevel(int times){
        return switch(times){
            case 0 -> "☆☆☆";
            case 1 -> "★☆☆";
            case 2 -> "★★☆";
            case 3 -> "★★★";
            default -> "error";
        };

    }

    // 添加 getter 方法
    public NamespacedKey getHorseKey() { return horseKey; }
    public NamespacedKey getEntityTypeKey() { return entityTypeKey; }
    public NamespacedKey getHorseDataKey() { return horseDataKey; }
    public NamespacedKey getHorseInventoryKey() { return horseInventoryKey; }
    public NamespacedKey getPillTimesKey() { return PillTimesKey; }
    public NamespacedKey getTamingDataKey() { return tamingDataKey; }
    public NamespacedKey getHungerValueKey() { return hungerValueKey; }
    public NamespacedKey getLastFeedTimeKey() { return lastFeedTimeKey; }
}
