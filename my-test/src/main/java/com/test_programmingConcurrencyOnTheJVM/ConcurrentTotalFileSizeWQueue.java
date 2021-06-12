package com.test_programmingConcurrencyOnTheJVM;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

// p47
public class ConcurrentTotalFileSizeWQueue {
	private ExecutorService service;
	final private BlockingQueue<Long> fileSizes = new ArrayBlockingQueue<Long>(500); // 阻塞队列
	final AtomicLong pendingFileVisits = new AtomicLong();

	private void startExploreDir(final File file) {
		pendingFileVisits.incrementAndGet();
		service.execute(new Runnable() {
			@Override
			public void run() {
				exploreDir(file);
			}
		});
	}

	private void exploreDir(final File file) {
		long fileSize = 0;
		if(file.isFile())
			fileSize = file.length();
		else {
			final File[] children = file.listFiles();
			if(children != null) {
				for (final File child : children) {
					if (child.isFile())
						fileSize += child.length();
					else
						startExploreDir(child);
				}
			}
		}
		try {
			fileSizes.put(fileSize);  // put是一个阻塞函数
		} catch (Exception ex) {
			throw new RuntimeException();
		}

		pendingFileVisits.decrementAndGet();
	}

	private long getTotalSizeOfFile(final String fileName) {
		service = Executors.newFixedThreadPool(100);
		long totalSize = 0;
		try {
			startExploreDir(new File(fileName));
			while (pendingFileVisits.get() > 0 || fileSizes.size() > 0) {
				final Long size = fileSizes.poll(10, TimeUnit.SECONDS);
				totalSize += size;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			service.shutdown();
		}
		return totalSize;
	}

	public static void main(final String[] args) throws InterruptedException {
		final long start = System.nanoTime();
		final long total = new ConcurrentTotalFileSizeWQueue().getTotalSizeOfFile(/**args[0]*/ "/usr");
		final long end = System.nanoTime();
		System.out.println("Total Size: " + total);
		System.out.println("Time taken: " + (end - start) / 1.0e9);
	}
}
