package com.github.Light.vehicle.listeners;

import com.github.Light.vehicle.items.HorsePipe;
import com.github.Light.vehicle.VehicleOnRoad;
import com.github.Light.vehicle.VehicleState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 唤马竹筒的事件监听器
 * 处理与唤马竹筒相关的所有交互事件
 */
public class HorsePipeListener implements Listener {
    private final JavaPlugin plugin;
    private final HorsePipe horsePipe;
    private final VehicleOnRoad eventProcessor;

    // 定义可以被捕获的坐骑类型
    private static final Set<EntityType> VALID_MOUNTS = Set.of(
            EntityType.HORSE,  // 马
            EntityType.DONKEY, // 驴
            EntityType.MULE,   // 骡子
            EntityType.STRIDER // 炽足兽
    );

    private static final String PIPE_ID_TAG = "bound_pipe_id";

    /**
     * 构造函数
     * @param plugin 插件实例
     * @param eventProcessor EventProcessor 实例
     */
    public HorsePipeListener(JavaPlugin plugin, VehicleOnRoad eventProcessor) {
        this.plugin = plugin;
        this.horsePipe = new HorsePipe(plugin, eventProcessor);
        this.eventProcessor = eventProcessor;
    }

    /**
     * 处理玩家与实体交互的事件
     * 主要用于捕获坐骑和解除绑定
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (!horsePipe.isHorsePipe(item)) return;
        event.setCancelled(true);

        // 检查是否为有效坐骑类型
        if (!VALID_MOUNTS.contains(target.getType())) {
            player.sendMessage(ChatColor.RED + "该物品只能捕获坐骑！");
            return;
        }

        // 处理Shift+右键解绑
        if (player.isSneaking()) {
            // 检查坐骑是否已经绑定了竹筒
            if (target.getPersistentDataContainer().has(
                    new NamespacedKey(plugin, PIPE_ID_TAG),
                    PersistentDataType.STRING)) {
                String boundPipeId = target.getPersistentDataContainer().get(
                        new NamespacedKey(plugin, PIPE_ID_TAG),
                        PersistentDataType.STRING);
                String currentPipeId = horsePipe.getPipeId(item);

                if (boundPipeId.equals(currentPipeId)) {
                    // 移除绑定关系
                    target.getPersistentDataContainer().remove(
                            new NamespacedKey(plugin, PIPE_ID_TAG)
                    );
                    // 给玩家一个新的空竹筒
                    player.getInventory().setItemInMainHand(horsePipe.createEmptyPipe());
                    player.sendMessage(ChatColor.GREEN + "已解除竹筒与坐骑的绑定！");
                    return;  // 重要：解绑后直接返回，不执行捕获逻辑
                }
            }
            return;  // 如果是Shift+右键但没有匹配的绑定，也直接返回
        }

        // 检查坐骑是否已经绑定了其他竹筒
        if (target.getPersistentDataContainer().has(
                new NamespacedKey(plugin, PIPE_ID_TAG),
                PersistentDataType.STRING)) {
            String boundPipeId = target.getPersistentDataContainer().get(
                    new NamespacedKey(plugin, PIPE_ID_TAG),
                    PersistentDataType.STRING);
            String currentPipeId = horsePipe.getPipeId(item);

            if (!boundPipeId.equals(currentPipeId)) {
                player.sendMessage(ChatColor.RED + "这个坐骑已经与其他唤马竹筒绑定！");
                return;
            }
        }

        // 检查与坐骑的距离
        if (player.getLocation().distance(target.getLocation()) > 2) {
            player.sendMessage(ChatColor.RED + "距离太远了！");
            return;
        }

        // 存储坐骑
        if (!horsePipe.hasStoredHorse(item)) {
            // 存储坐骑信息
            ItemStack newPipe = horsePipe.storeHorse((LivingEntity) target, player);
            player.getInventory().setItemInMainHand(newPipe);

            // 在存储完成后，移除坐骑的 VehicleState
            eventProcessor.resetVehicleSpeed((LivingEntity) target);

            target.remove();
            player.sendMessage(ChatColor.GREEN + "成功将坐骑存入竹筒！");
        }
    }

    /**
     * 处理玩家与方块交互的事件
     * 主要用于释放坐骑
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // 如果是唤马竹筒，阻止放置
        if (horsePipe.isHorsePipe(item)) {
            event.setCancelled(true);

            // 如果没有方块或没有物品，直接返回
            if (!event.hasBlock() || !event.hasItem()) return;

            // 以下是释放坐骑的逻辑
            if (!horsePipe.hasStoredHorse(item)) return;

            // 释放坐骑
            Location spawnLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
            EntityType entityType = horsePipe.getStoredEntityType(item);
            String horseData = horsePipe.getHorseData(item);
            String pipeId = horsePipe.getPipeId(item);// 获取竹筒ID
            UUID vehicleId = horsePipe.getStoredHorseUUID(item);

            if (entityType != null) {
                // 保留原有竹筒，而不是给一个新的
                ItemStack emptyPipe = item.clone();
                ItemMeta meta = emptyPipe.getItemMeta();
                if (meta != null) {
                    // 清除存储的坐骑数据，但保留竹筒ID
                    meta.getPersistentDataContainer().remove(horsePipe.getHorseKey());
                    meta.getPersistentDataContainer().remove(horsePipe.getEntityTypeKey());
                    meta.getPersistentDataContainer().remove(horsePipe.getHorseDataKey());
                    meta.getPersistentDataContainer().remove(horsePipe.getHorseInventoryKey());

                    // 更新描述
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "右键点击坐骑进行捕获");
                    meta.setLore(lore);
                    emptyPipe.setItemMeta(meta);
                }
                player.getInventory().setItemInMainHand(emptyPipe);

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    World world = spawnLoc.getWorld();
                    if (world != null) {
                        Entity entity = world.spawnEntity(spawnLoc, entityType);
                        
                        // 设置坐骑的UUID
                        if (vehicleId != null) {
                            try {
                                // 使用反射设置实体的UUID
                                Object craftEntity = entity.getClass().getMethod("getHandle").invoke(entity);
                                java.lang.reflect.Field uuidField = craftEntity.getClass().getSuperclass().getDeclaredField("uuid");
                                uuidField.setAccessible(true);
                                uuidField.set(craftEntity, vehicleId);
                            } catch (Exception e) {
                                plugin.getLogger().warning("无法设置坐骑UUID: " + e.getMessage());
                            }
                        }

                        // 恢复驯服状态和主人
                        String tamingData = horsePipe.getTamingData(item);
                        if (tamingData != null && entity instanceof Tameable tameable) {
                            String[] data = tamingData.split(";");
                            if (data.length >= 2) {
                                boolean isTamed = Boolean.parseBoolean(data[0]);
                                tameable.setTamed(isTamed);
                                if (isTamed && !data[1].isEmpty()) {
                                    UUID ownerUUID = UUID.fromString(data[1]);
                                    tameable.setOwner(plugin.getServer().getOfflinePlayer(ownerUUID));
                                }
                            }
                        }

                        // 恢复竹筒绑定
                        if (pipeId != null) {
                            entity.getPersistentDataContainer().set(
                                    new NamespacedKey(plugin, PIPE_ID_TAG),
                                    PersistentDataType.STRING,
                                    pipeId
                            );
                        }

                        // 恢复其他属性
                        if (entity instanceof AbstractHorse horse && horseData != null) {
                            String[] data = horseData.split(",");
                            if (data.length >= 4) {  // 现在有4个基本属性
                                horse.setJumpStrength(Double.parseDouble(data[0]));
                                horse.setDomestication(Integer.parseInt(data[1]));
                                horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Double.parseDouble(data[2]));

                                // 恢复当前生命值
                                Double currentHealth = horsePipe.getCurrentHealth(item);
                                if (currentHealth != null) {
                                    horse.setHealth(Math.min(currentHealth, horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
                                }

                                // 恢复基础速度
                                horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Double.parseDouble(data[3]));

                                if (horse instanceof Horse regularHorse && data.length >= 6) {  // 颜色和样式现在是第5、6个属性
                                    regularHorse.setColor(Horse.Color.valueOf(data[4]));
                                    regularHorse.setStyle(Horse.Style.valueOf(data[5]));
                                }
                            }

                            // 恢复物品栏
                            String inventoryData = horsePipe.getHorseInventory(item);
                            if (inventoryData != null && !inventoryData.isEmpty()) {
                                String[] items = inventoryData.split(";");
                                if (items.length >= 1 && !items[0].isEmpty()) {
                                    ItemStack saddle = horsePipe.base64ToItem(items[0]);
                                    if (saddle != null) {
                                        horse.getInventory().setSaddle(saddle);
                                    }
                                }
                                if (items.length >= 2 && !items[1].isEmpty() && horse instanceof Horse regularHorse) {
                                    ItemStack armor = horsePipe.base64ToItem(items[1]);
                                    if (armor != null) {
                                        regularHorse.getInventory().setArmor(armor);
                                    }
                                }
                            }
                        } else if (entity instanceof Strider strider && horseData != null) {
                            // 恢复炽足兽的属性
                            strider.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Double.parseDouble(horseData));
                            strider.setHealth(strider.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());

                            // 恢复鞍具
                            String inventoryData = horsePipe.getHorseInventory(item);
                            if (inventoryData != null && !inventoryData.isEmpty()) {
                                ItemStack saddle = horsePipe.base64ToItem(inventoryData);
                                if (saddle != null) {
                                    strider.setSaddle(true);
                                }
                            }
                        }

                        // 恢复饥饿值状态
                        if (entity instanceof LivingEntity livingEntity) {
                            Double storedHunger = horsePipe.getStoredHungerValue(item);
                            Long storedLastFeedTime = horsePipe.getStoredLastFeedTime(item);
                            Double consumptionMultiplier = horsePipe.getConsumptionMultiplier(item);
                            String[] PillData = horsePipe.getHorsePillData(item).split(",");//速度，生命，跳跃，能耗
                            int[] PillTimes = new int[4];
                            boolean[] PotentPillTimes = new boolean[4];
                            for(int i=0; i<4;i++) {
                                PillTimes[i] = Integer.parseInt(PillData[i]);
                                PotentPillTimes[i] = Boolean.parseBoolean(PillData[i + 4]);
                            }
                            if (storedHunger != null && storedLastFeedTime != null) {
                                VehicleState state = new VehicleState(
                                        livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue(),
                                        world
                                );
                                state.setPillUses(PillTimes[0],PillTimes[1],PillTimes[2],PillTimes[3],
                                        PotentPillTimes[0],PotentPillTimes[1],PotentPillTimes[2],PotentPillTimes[3]);
                                state.setConsumptionMultiplier(consumptionMultiplier);
                                state.setStoredState(storedHunger, storedLastFeedTime);
                                eventProcessor.initializeVehicleState(livingEntity, state);
                            }
                        }


                        player.sendMessage(ChatColor.GREEN + "坐骑已释放！");
                    }
                });
            }
        }
    }

}
