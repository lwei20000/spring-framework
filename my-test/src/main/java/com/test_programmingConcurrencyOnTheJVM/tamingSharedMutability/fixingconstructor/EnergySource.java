package com.test_programmingConcurrencyOnTheJVM.tamingSharedMutability.fixingconstructor;


import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EnergySource {
	private final long MAXLEVEL = 100;
	private long level = MAXLEVEL;  // EnergySource中的方法会被多个线程调用，所以私有类变量就成为一个非线程安全的共享可变变量
	//private boolean keepRunning = true;
	private static final ScheduledExecutorService replenishTimer = Executors.newScheduledThreadPool(10); // 线程池
	private ScheduledFuture<?> replenishTask;

	/**
	 * 构造函数私有化
	 */
	private EnergySource() {}

	private void init() {
		replenishTask = replenishTimer.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				replenish();
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * 静态工厂函数
	 */
	public static EnergySource create() {
		final EnergySource energySource = new EnergySource();
		energySource.init();
		return energySource;
	}

	public long getUnitsAvailable() { return level; }

	public boolean useEnergy(final long units) {
		if(units > 0 && level >= units) {
			level -= units;
			return true;
		}
		return false;
	}

	public void stopEnergySource() { replenishTask.cancel(false); }

	/**
	 * replenish大部分时间都是处于睡眠状态，但是本代码还死活得浪费一个线程在它身上。
	 * JVM通常只允许我们创建几千个线程
	 */
	private void replenish() {
			if(level < MAXLEVEL) level++;
	}
}
