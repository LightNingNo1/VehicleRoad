package com.github.Light.vehicle.data;

import com.github.Light.vehicle.VehicleState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class VehicleDataManager {
    private final JavaPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    public VehicleDataManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "vehicle_data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            plugin.saveResource("vehicle_data.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void saveVehicleState(Entity vehicle, VehicleState state) {
        String vehicleId = vehicle.getUniqueId().toString();
        data.set(vehicleId + ".hungerValue", state.getHungerValue());
        data.set(vehicleId + ".lastFeedTime", state.getLastFeedTime());
        data.set(vehicleId + ".baseSpeed", state.getBaseSpeed());
        data.set(vehicleId + ".consumptionMultiplier", state.getConsumptionMultiplier());
        data.set(vehicleId + ".baseHealthPillUses", state.getHealthPillUses());
        data.set(vehicleId + ".baseSpeedPillUses", state.getSpeedPillUses());
        data.set(vehicleId + ".baseJumpPillUses", state.getJumpPillUses());
        data.set(vehicleId + ".baseHungerPillUses", state.getHungerPillUses());
        data.set(vehicleId + ".HealthPillUses", state.isHealthUp());
        data.set(vehicleId + ".SpeedPillUses", state.isSpeedUp());
        data.set(vehicleId + ".JumpPillUses", state.isJumpUp());
        data.set(vehicleId + ".HungerPillUses", state.isHungerUp());
        
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存坐骑数据: " + e.getMessage());
        }
    }

    public VehicleState loadVehicleState(Entity vehicle, org.bukkit.World world) {
        String vehicleId = vehicle.getUniqueId().toString();
        if (!data.contains(vehicleId)) {
            return null;
        }

        double hungerValue = data.getDouble(vehicleId + ".hungerValue", 90.0);
        long lastFeedTime = data.getLong(vehicleId + ".lastFeedTime", world.getFullTime());
        int healthPillUses = data.getInt(vehicleId + ".baseHealthPillUses", 0);
        int speedPillUses = data.getInt(vehicleId + ".baseSpeedPillUses", 0);
        int jumpPillUses = data.getInt(vehicleId + ".baseJumpPillUses", 0);
        int hungerPillUses = data.getInt(vehicleId + ".baseHungerPillUses", 0);
        boolean healthUp = data.getBoolean(vehicleId + ".HealthPillUses", false);
        boolean speedUp = data.getBoolean(vehicleId + ".SpeedPillUses", false);
        boolean jumpUp = data.getBoolean(vehicleId + ".JumpPillUses", false);
        boolean hunger = data.getBoolean(vehicleId + ".HungerPillUses", false);
        double consumptionMultiplier = data.getDouble(vehicleId + ".consumptionMultiplier", 1.0);
        double speed = data.getDouble(vehicleId + ".baseSpeed");

        
        VehicleState state = new VehicleState(speed, world);
        state.setStoredState(hungerValue, lastFeedTime);
        state.setConsumptionMultiplier(consumptionMultiplier);
        state.setPillUses(speedPillUses, healthPillUses, jumpPillUses, hungerPillUses, speedUp, healthUp, jumpUp, hunger);
        
        return state;
    }

    public void removeVehicleData(Entity vehicle) {
        String vehicleId = vehicle.getUniqueId().toString();
        data.set(vehicleId, null);
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法删除坐骑数据: " + e.getMessage());
        }
    }

    public void saveAllData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("无法保存坐骑数据: " + e.getMessage());
        }
    }
} 