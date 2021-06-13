package com.test_programmingConcurrencyOnTheJVM.tamingSharedMutability.originalcode;


public class EnergySource {
	private final long MAXLEVEL = 100;
	private long level = MAXLEVEL;  // EnergySource中的方法会被多个线程调用，所以私有类变量就成为一个非线程安全的共享可变变量
	private boolean keepRunning = true;

	/**
	 * 构造函数中启动了线程
	 * （1）
	 * 构造函数违反了类不变式（class invariant）：一个精心设计的类在其自身没有恢复到有效状态之前，它的任何方法应该都是不能访问的。
	 * 而本构造函数可以导致在构造函数还没有完成之前就有其他线程调用replenish()
	 * （2）
	 * Thread类的start函数会自动插入一个内存栅栏（start函数是synchronized的。），
	 * 于是Thread类的对象在EnergySource实例化完成之前就脱离了其控制范围。
	 */
	public EnergySource() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				replenish();
			}
		}).start();
	}

	public long getUnitsAvailable() {
		return level;
	}

	public boolean useEnergy(final long units) {
		if(units > 0 && level >= units) {
			level -= units;
			return true;
		}
		return false;
	}

	public void stopEnergySource() {
		keepRunning = false;
	}

	/**
	 * replenish大部分时间都是处于睡眠状态，但是本代码还死活得浪费一个线程在它身上。
	 * JVM通常只允许我们创建几千个线程
	 */
	private void replenish() {
		while (keepRunning) {
			if(level < MAXLEVEL) level++;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {}
		}
	}
}
