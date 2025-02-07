package com.github.Light.vehicle;

import com.github.Light.vehicle.commands.PillsTabCompleter;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.Light.vehicle.listeners.HorsePipeListener;
import com.github.Light.vehicle.commands.HorsePipeCommand;
import com.github.Light.vehicle.data.VehicleDataManager;
import com.github.Light.vehicle.items.Pills;
import com.github.Light.vehicle.commands.PillsGiveCommand;

public final class Vehicle extends JavaPlugin {
    private VehicleDataManager dataManager;
    private VehicleOnRoad eventProcessor;

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();

        // 初始化数据管理器
        this.dataManager = new VehicleDataManager(this);

        // 创建事件处理器
        this.eventProcessor = new VehicleOnRoad(this, dataManager);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(eventProcessor, this);
        getServer().getPluginManager().registerEvents(new HorsePipeListener(this, eventProcessor), this);

        // 注册指令
        HorsePipeCommand horsePipeCommand = new HorsePipeCommand(this, eventProcessor);
        getCommand("horsepipe").setExecutor(horsePipeCommand);
        getCommand("horsepipe").setTabCompleter(horsePipeCommand);
        getCommand("givepill").setExecutor(new PillsGiveCommand());
        getCommand("givepill").setTabCompleter(new PillsTabCompleter());

        // 注册丹药配方
        Pills.registerRecipes(this);
    }

    @Override
    public void onDisable() {
        // 保存所有数据
        if (dataManager != null) {
            dataManager.saveAllData();
        }
    }
}
