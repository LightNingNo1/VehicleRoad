package com.github.Light.vehicle;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;
import com.github.Light.vehicle.config.SpeedConfig;
import com.github.Light.vehicle.data.VehicleDataManager;
import com.github.Light.vehicle.items.Pills;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.EnumSet;
import java.util.Random;

/**
 * 主要的事件处理类
 * 处理所有与坐骑速度相关的事件
 */
public class VehicleOnRoad implements Listener {
    // 存储所有坐骑的状态
    private final Map<Entity, VehicleState> vehicleStates = new HashMap<>();
    private final SpeedConfig speedConfig;
    private final VehicleDataManager dataManager;

    // 定义可用的坐骑类型
    private static final Set<EntityType> VALID_MOUNT_TYPES = EnumSet.of(
            EntityType.HORSE,
            EntityType.DONKEY,
            EntityType.MULE,
            EntityType.STRIDER
    );

    public VehicleOnRoad(JavaPlugin plugin, VehicleDataManager dataManager) {
        this.speedConfig = new SpeedConfig(plugin);
        this.dataManager = dataManager;
    }

    /**
     * 处理玩家移动事件
     * 当玩家骑乘坐骑移动时触发
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = player.getVehicle();

        if (vehicle == null || !VALID_MOUNT_TYPES.contains(vehicle.getType())) {
            return;
        }

        VehicleState state = initializeVehicleStateIfNeeded((LivingEntity)vehicle, player.getWorld());
        
        // 获取移动距离
        double distance = event.getFrom().distance(event.getTo());

        // 更新饥饿值
        state.updateHunger(player.getWorld());
        state.consumeHungerByDistance(distance);

        // 更新显示信息
        displayVehicleStatus(player, (LivingEntity)vehicle, state);

        // 处理移动速度
        handleVehicleMovement((LivingEntity) vehicle);
    }

    private void displayVehicleStatus(Player player, LivingEntity vehicle, VehicleState state) {
        double speed = vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue() * 43;
        double hunger = state.getHungerValue();
        
        // 构建状态栏
        StringBuilder status = new StringBuilder();
        status.append(ChatColor.GOLD).append("饥饿值: ");
        
        // 添加饥饿值颜色指示
        if (hunger >= 150) {
            status.append(ChatColor.GREEN);
        } else if (hunger >= 75) {
            status.append(ChatColor.YELLOW);
        } else if (hunger >= 30) {
            status.append(ChatColor.GOLD);
        } else {
            status.append(ChatColor.RED);
        }
        
        status.append(String.format("%.1f", hunger))
              .append(ChatColor.GOLD).append(" 速度: ")
              .append(ChatColor.WHITE)
              .append(String.format("%.2f", speed))
              .append(ChatColor.GOLD).append(" 格/秒");

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(status.toString()));
    }

    /**
     * 处理坐骑移动逻辑
     * 更新速度并显示信息
     */
    private void handleVehicleMovement(LivingEntity vehicle) {
        // 获取相关方块
        Block block = vehicle.getLocation().getBlock();
        Block underBlock1 = block.getRelative(0, -1, 0);
        Block underBlock2 = block.getRelative(0, -2, 0);

        // 获取道路加速倍率
        double roadSpeedMultiplier = calculateSpeedMultiplier(block, underBlock1, underBlock2);

        // 获取饥饿值影响的速度倍率
        double HungerMultiplier = calculateHungerMultiplier(vehicleStates.get(vehicle).getHungerValue());

        // 更新速度
        updateVehicleSpeed(vehicle, roadSpeedMultiplier, HungerMultiplier);
    }

    /**
     * 更新坐骑速度
     * 仅当新的速度倍率与当前不同时更新
     */
    private void updateVehicleSpeed(LivingEntity vehicle, double roadSpeedMultiplier, double HungerMultiplier) {
        VehicleState state = vehicleStates.get(vehicle);
        if (state.getRoadSpeedMultiplier() != roadSpeedMultiplier || state.getHungerMultiplier() != HungerMultiplier) {
            state.setSpeedMultiplier(roadSpeedMultiplier);
            state.setHungerMultiplier(HungerMultiplier);
            vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .setBaseValue(state.getCurrentSpeed());
        }
    }

    /**
     * 计算速度倍率
     * 基于坐骑所在的方块类型
     */
    private double calculateSpeedMultiplier(Block block, Block underBlock1, Block underBlock2) {
        double multiplier = 1.0;

        // 泥土路径提供一级加速
        if (block.getType() == Material.DIRT_PATH) {
            multiplier += speedConfig.getSpeedLevel1();
        }
        // 混凝土粉末提供二级加速
        if (isConcretePowder(underBlock1.getType()) || isConcretePowder(underBlock2.getType())) {
            multiplier += speedConfig.getSpeedLevel2();
        }

        return multiplier;
    }

    private double calculateHungerMultiplier(double hungerValue) {
        if (hungerValue >= 0 && hungerValue <= 30) {
            return -0.25;
        } else if (hungerValue > 30 && hungerValue <= 50) {
            return -0.15;
        } else if (hungerValue > 50 && hungerValue <= 75) {
            return 0.0;
        } else if (hungerValue > 75 && hungerValue <= 200) {
            return 0.15;
        }
        return 0.0;
    }

    /**
     * 检查方块是否为混凝土粉末
     */
    private boolean isConcretePowder(Material material) {
        return material.name().endsWith("CONCRETE_POWDER");
    }

    /**
     * 处理实体死亡事件
     * 清理已死亡坐骑的状态
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        vehicleStates.remove(entity);
        // 删除已死亡坐骑的存储数据
        dataManager.removeVehicleData(entity);
    }

    /**
     * 处理玩家退出事件
     * 保存坐骑状态但不重置
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity && vehicleStates.containsKey(vehicle)) {
            VehicleState state = vehicleStates.get(vehicle);
            state.updateHunger(vehicle.getWorld());
            
            // 保存状态到文件
            dataManager.saveVehicleState(vehicle, state);
            
            ((LivingEntity) vehicle).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .setBaseValue(state.getBaseSpeed());
        }
    }

    /**
     * 重置坐骑速度
     * @param vehicle 需要重置速度的坐骑
     */
    public void resetVehicleSpeed(LivingEntity vehicle) {
        if (vehicleStates.containsKey(vehicle)) {
            VehicleState state = vehicleStates.get(vehicle);
            vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                    .setBaseValue(state.getBaseSpeed());
            vehicleStates.remove(vehicle);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        
        if (!VALID_MOUNT_TYPES.contains(entity.getType())) {
            return;
        }

        org.bukkit.inventory.ItemStack itemInHand = player.getInventory().getItemInMainHand();
        
        // 检查是否是丹药
        if (Pills.isVehiclePill(itemInHand)) {
            event.setCancelled(true);  // 阻止玩家坐上坐骑
            handlePillUse(player, (LivingEntity)entity, itemInHand);
            return;
        }

        // 原有的喂食逻辑...
        if (itemInHand.getType() == Material.AIR) {
            return;
        }

        if (entity instanceof LivingEntity) {
            Feed((LivingEntity) entity, itemInHand);
        }
    }

    private void handlePillUse(Player player, LivingEntity vehicle, ItemStack pill) {

        VehicleState state = initializeVehicleStateIfNeeded(vehicle, vehicle.getWorld());
        String pillName = pill.getItemMeta().getDisplayName();

        Random random = new Random();
        
        if (pillName.contains(Pills.BASE_HEALTH_PILL)) {
            if (state.getHealthPillUses() >= 3) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到生命提升次数上限！");
                return;
            }
            //生命值
            double currentMaxHealth = vehicle.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            vehicle.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth * (1.0 + (0.167 + 0.045 * random.nextGaussian())));
            state.incrementHealthPillUses();
            player.sendMessage(ChatColor.GREEN + "成功使用生命提升丹药！");
        } 
        else if (pillName.contains(Pills.BASE_SPEED_PILL)) {
            if (state.getSpeedPillUses() >= 3) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到速度提升次数上限！");
                return;
            }
            //速度
            double Speed = state.getBaseSpeed() * (1.0 + (0.167 + 0.045 * random.nextGaussian()));
            state.setBaseSpeed(Speed);
            vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(state.getCurrentSpeed());
            state.incrementSpeedPillUses();
            player.sendMessage(ChatColor.GREEN + "成功使用速度提升丹药！");
        }
        else if (pillName.contains(Pills.BASE_JUMP_PILL)) {
            if (state.getJumpPillUses() >= 3) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到跳跃提升次数上限！");
                return;
            }
            // 跳跃强度
            double Jump = vehicle.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).getBaseValue() * (1.0 + (0.167 + 0.045 * random.nextGaussian()));
            vehicle.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(Jump);
            state.incrementJumpPillUses();
            player.sendMessage(ChatColor.GREEN + "成功使用跳跃提升丹药！");
        }
        else if (pillName.contains(Pills.BASE_HUNGER_CONS_PILL)) {
            if(state.getHungerPillUses() >= 3) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到能耗升级次数上限");
                return;
            }
            double consumptionMultiplier = state.getConsumptionMultiplier() * (1.0 - (0.083 + 0.045 * random.nextGaussian())) ;
            state.setConsumptionMultiplier(consumptionMultiplier);
            state.incrementHungryPillUses();
            player.sendMessage(ChatColor.GREEN + "成功使用能耗升级丹药！");
        }
        else if (pillName.contains(Pills.SPEED_PILL)) {
            if (state.isSpeedUp()) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到强效速度提升次数上限！");
                return;
            }
            //速度提升 -- 强效
            double Speed = state.getBaseSpeed() * (1.0 + (0.167 + 0.045 * random.nextGaussian()));
            state.setBaseSpeed(Speed);
            vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(state.getCurrentSpeed());
            state.setSpeedUp(true);
            player.sendMessage(ChatColor.GREEN + "成功使用强效速度提升丹药！");
        }
        else if (pillName.contains(Pills.JUMP_PILL)) {
            if (state.isJumpUp()) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到强效跳跃提升次数上限！");
                return;
            }
            //跳跃强度 -- 强效
            double Jump = vehicle.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).getBaseValue() * (1.0 + (0.167 + 0.045 * random.nextGaussian()));
            vehicle.getAttribute(Attribute.GENERIC_JUMP_STRENGTH).setBaseValue(Jump);
            state.setJumpUp(true);
            player.sendMessage(ChatColor.GREEN + "成功使用强效跳跃提升丹药！");
        }
        else if (pillName.contains(Pills.HEALTH_PILL)) {
            if (state.isHealthUp()) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到强效生命提升次数上限！");
                return;
            }
            //生命值提升 -- 强效
            double currentMaxHealth = vehicle.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
            vehicle.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth * (1.0 + (0.167 + 0.045 * random.nextGaussian())));
            state.setHealthUp(true);
            player.sendMessage(ChatColor.GREEN + "成功使用强效生命提升丹药！");
        }
        else if (pillName.contains(Pills.HUNGER_CONS_PILL)) {
            if(state.isHungerUp()) {
                player.sendMessage(ChatColor.RED + "该坐骑已达到强效能耗升级次数上限");
                return;
            }
            double consumptionMultiplier = state.getConsumptionMultiplier() * (1.0 - (0.083 + 0.045 * random.nextGaussian())) ;
            state.setConsumptionMultiplier(consumptionMultiplier);
            state.setHungerUp(true);
            player.sendMessage(ChatColor.GREEN + "成功使用强效能耗升级丹药！");
        }

        // 使用后消耗一个丹药
        pill.setAmount(pill.getAmount() - 1);
        
        // 保存状态
        dataManager.saveVehicleState(vehicle, state);

    }

    public void Feed(LivingEntity vehicle, org.bukkit.inventory.ItemStack food) {
        if (!vehicleStates.containsKey(vehicle) || !VALID_MOUNT_TYPES.contains(vehicle.getType())) {
            return;
        }

        World world = vehicle.getWorld();
        VehicleState state = vehicleStates.get(vehicle);

        // 根据食物类型增加饥饿值
        if(vehicle instanceof Strider && food.getType() == Material.WARPED_FUNGUS) {
            state.feed(10.0, world);
        } else if(!(vehicle instanceof Strider)) {
            switch (food.getType()) {
                case WHEAT -> state.feed(10.0, world);
                case HAY_BLOCK -> state.feed(90.0, world);
            }
        }

        // 播放粒子效果
        vehicle.getWorld().spawnParticle(org.bukkit.Particle.HEART,
                vehicle.getLocation().add(0, 1, 0), 3);
    }

    @EventHandler
    public void onVehicleEnter(org.bukkit.event.vehicle.VehicleEnterEvent event) {
        Entity vehicle = event.getVehicle();

        if (!VALID_MOUNT_TYPES.contains(vehicle.getType())) {
            return;
        }

        World world = vehicle.getWorld();

        VehicleState state = initializeVehicleStateIfNeeded((LivingEntity) vehicle, world);

        state.updateHunger(world);
    }

    @EventHandler
    public void onVehicleExit(org.bukkit.event.vehicle.VehicleExitEvent event) {
        Entity vehicle = event.getVehicle();
        
        // 只处理有效的坐骑类型
        if (!VALID_MOUNT_TYPES.contains(vehicle.getType())) {
            return;
        }

        // 如果坐骑有状态，则保存当前状态
        if (vehicleStates.containsKey(vehicle)) {
            VehicleState state = vehicleStates.get(vehicle);
            state.updateHunger(vehicle.getWorld());
            
            // 保存状态到文件
            dataManager.saveVehicleState(vehicle, state);
            
            // 下马时重置速度为基础值
            if (vehicle instanceof LivingEntity livingEntity) {
                livingEntity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                        .setBaseValue(state.getBaseSpeed());
            }
        }
    }


    // 初始化坐骑状态
    public void initializeVehicleState(LivingEntity vehicle, VehicleState state) {
        vehicleStates.put(vehicle, state);
        // 保存状态到文件
        dataManager.saveVehicleState(vehicle, state);
    }

    public VehicleState initializeVehicleStateIfNeeded(LivingEntity vehicle, World world) {
        if (!vehicleStates.containsKey(vehicle)) {
            VehicleState state = dataManager.loadVehicleState(vehicle, world);
            if(state == null) {
                double baseSpeed = vehicle.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
                state = new VehicleState(baseSpeed, world);
            }
            vehicleStates.put(vehicle, state);
            // 保存状态到文件
            dataManager.saveVehicleState(vehicle, state);
        }
        return vehicleStates.get(vehicle);
    }
}
