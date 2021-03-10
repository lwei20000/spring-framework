package com.test_java.aqs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 *
 */
public class MyReentrantLock implements Lock {

	// Our internal helper class
	private static class Sync extends AbstractQueuedSynchronizer {
		private static final long serialVersionUID = 1L;

		// Acquires the lock if state is zero
		public boolean tryAcquire(int acquires) {
			assert acquires == 1; // Otherwise unused
			if (compareAndSetState(0, 1)) {
				// 如果成功了，就把拥有者线程设置程当前线程
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			} else if (Thread.currentThread().equals(getExclusiveOwnerThread())) {
				//
				setState(getState() + 1);
				System.out.println(Thread.currentThread().getName()+", tryAcquire2 ==" +getState());
				return true;
			}
			return false;
		}

		// Releases the lock by setting state to zero
		protected boolean tryRelease(int releases) {
			assert releases == 1; // Otherwise unused

			// 判断是不是独占
			if (!isHeldExclusively())  //
				// 如果不是独占，就直接抛错
				throw new IllegalMonitorStateException();

			setState(getState()-1);
			System.out.println(Thread.currentThread().getName()+", tryAcquire2 ==" +getState());

			// 如果state为0
			if(getState() == 0) {
				// 拥有者线程设置为null
				setExclusiveOwnerThread(null);
			}
			return true;
		}

		// Reports whether in locked state
		public boolean isLocked() {
			return getState() != 0;
		}

		public boolean isHeldExclusively() {
			// a data race, but safe due to out-of-thin-air guarantees
			return getExclusiveOwnerThread() == Thread.currentThread();
		}

		// Provides a Condition
		public Condition newCondition() {
			return new ConditionObject();
		}

		// Deserializes properly
		private void readObject(ObjectInputStream s)
				throws IOException, ClassNotFoundException {
			s.defaultReadObject();
			setState(0); // reset to unlocked state
		}
	}


	// The sync object does all the hard work. We just forward to it.
	private final Sync sync = new Sync();

	public void lock()              { sync.acquire(1); }
	public boolean tryLock()        { return sync.tryAcquire(1); }
	public void unlock()            { sync.release(1); }
	public Condition newCondition() { return sync.newCondition(); }
	public boolean isLocked()       { return sync.isLocked(); }
	public boolean isHeldByCurrentThread() {
		return sync.isHeldExclusively();
	}
	public boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}
	public boolean tryLock(long timeout, TimeUnit unit)
			throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}
}
