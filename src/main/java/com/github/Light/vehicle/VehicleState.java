package com.github.Light.vehicle;

import org.bukkit.World;

public class VehicleState {
    // 速度倍率
    private double speedMultiplier;
    private double hungerValue;
    private double hungerMultiplier;
    private double consumptionMultiplier;
    // 基础属性
    private double baseSpeed;
    //上次喂食时间
    private long lastFeedTime;
    private static final double HUNGER_DECREASE_RATE = 0.00125; // 每秒饥饿值减少率
    private static final double DISTANCE_HUNGER_RATE = 0.2; // 每100米饥饿值减少量
    //基础丹药升级次数 <3
    private int healthPillUses = 0;
    private int speedPillUses = 0;
    private int jumpPillUses = 0;
    private int hungryPillUses = 0;
    //进阶丹药升级次数 <1
    private boolean SpeedUp = false;
    private boolean JumpUp = false;
    private boolean HealthUp = false;
    private boolean HungerUp = false;
    

    public VehicleState(double baseSpeed,  World world) {
        this.speedMultiplier = 1.0;
        this.baseSpeed = baseSpeed;
        this.hungerValue = 90.0; // 初始饥饿值
        this.hungerMultiplier = 0.151; // 初始饥饿速度修正
        this.lastFeedTime = world.getFullTime();
        this.consumptionMultiplier = 1.0;
    }

    // 获取当前速度倍率
    public double getRoadSpeedMultiplier() {
        return speedMultiplier;
    }

    // 设置新的速度倍率
    public void setSpeedMultiplier(double multiplier) {
        this.speedMultiplier = multiplier;
    }

    // 获取基础速度
    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public void setConsumptionMultiplier(double consumptionMultiplier) {
        this.consumptionMultiplier = consumptionMultiplier;
    }

    // 计算当前实际速度
    public double getCurrentSpeed() {
        return baseSpeed * (speedMultiplier + hungerMultiplier);
    }
    //获取能耗比
    public double getConsumptionMultiplier(){
        return consumptionMultiplier;
    }

    //更新饥饿值
    public void updateHunger(World world) {
        long currentTime = world.getFullTime();
        long timeDiff = currentTime - lastFeedTime;
        if(timeDiff < 0)
        {
            lastFeedTime = currentTime;
            return;
        }
        double timeDecrease = Math.min(hungerValue * 0.15 + 10.0 ,timeDiff * HUNGER_DECREASE_RATE * consumptionMultiplier);

        hungerValue = Math.max(0 ,hungerValue - timeDecrease);

        // 更新最后喂食时间为当前时间
        lastFeedTime = currentTime;
    }

    //处理移动消耗的饥饿值
    public void consumeHungerByDistance(double distance) {
        // 根据公式：每移动100m，饥饿值减少0.2*消耗系数
        double distanceDecrease = (distance / 100.0) * DISTANCE_HUNGER_RATE * consumptionMultiplier;
        hungerValue = Math.max(0, Math.min(200, hungerValue - distanceDecrease));
    }

    public double getHungerMultiplier(){return hungerMultiplier;}

    public void setHungerMultiplier(double multiplier) {
        this.hungerMultiplier = multiplier;
    }

    //喂食
    public void feed(double amount, World world) {
        // 增加饥饿值
        this.hungerValue = Math.min(200.0, this.hungerValue + amount);
        // 更新最后喂食时间
        this.lastFeedTime = world.getFullTime();
    }

    // Getter方法
    public double getHungerValue() {
        return hungerValue;
    }

    // 设置存储的状态
    public void setStoredState(double hungerValue, long lastFeedTime) {
        this.hungerValue = hungerValue;
        this.lastFeedTime = lastFeedTime;
    }

    // 获取最后喂食时间
    public long getLastFeedTime() {
        return lastFeedTime;
    }

    public int getHealthPillUses() { return healthPillUses; }
    public int getSpeedPillUses() { return speedPillUses; }
    public int getJumpPillUses() { return jumpPillUses; }
    public int getHungerPillUses() { return hungryPillUses; }

    public boolean isSpeedUp() { return SpeedUp; }
    public boolean isJumpUp() { return JumpUp; }
    public boolean isHealthUp() { return HealthUp; }
    public boolean isHungerUp() { return HungerUp; }

    public void setSpeedUp(boolean SpeedUp) { this.SpeedUp = SpeedUp; }
    public void setJumpUp(boolean JumpUp) { this.JumpUp = JumpUp; }
    public void setHealthUp(boolean HealthUp) { this.HealthUp = HealthUp; }
    public void setHungerUp(boolean HungerUp) { this.HungerUp = HungerUp; }

    public void incrementHealthPillUses() { healthPillUses++; }
    public void incrementSpeedPillUses() { speedPillUses++; }
    public void incrementJumpPillUses() { jumpPillUses++; }
    public void incrementHungryPillUses() { hungryPillUses++; }

    public void setPillUses(int speed, int health, int jump, int hungry, boolean speedUp, boolean healthUp, boolean jumpUp, boolean hungerUp) {
        this.speedPillUses = speed;
        this.healthPillUses = health;
        this.jumpPillUses = jump;
        this.hungryPillUses = hungry;
        this.SpeedUp = speedUp;
        this.HealthUp = healthUp;
        this.JumpUp = jumpUp;
        this.HungerUp = hungerUp;
    }
}
