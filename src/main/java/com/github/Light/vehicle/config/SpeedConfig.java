package com.github.Light.vehicle.config;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * 速度配置管理类
 * 用于从config.yml中读取和管理速度相关的配置
 */
public class SpeedConfig {
    // 速度等级1的加成值（用于泥土路径）
    private final double speedLevel1;
    // 速度等级2的加成值（用于混凝土粉末）
    private final double speedLevel2;

    /**
     * 从插件配置文件中加载速度配置
     * @param plugin 插件实例
     */
    public SpeedConfig(JavaPlugin plugin) {
        this.speedLevel1 = plugin.getConfig().getDouble("speed-level-1", 0.25);
        this.speedLevel2 = plugin.getConfig().getDouble("speed-level-2", 0.75);
    }

    public double getSpeedLevel1() {
        return speedLevel1;
    }

    public double getSpeedLevel2() {
        return speedLevel2;
    }
}
