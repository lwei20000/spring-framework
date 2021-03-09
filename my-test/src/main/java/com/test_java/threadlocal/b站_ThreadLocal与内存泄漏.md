

# **ThreadLocal**

[**https://www.bilibili.com/video/BV1N741127FH?p=11**](https://www.bilibili.com/video/BV1N741127FH?p=11)

---

# 第一部分   代码案例

**例子一**

```java
package com.test.threadlocal;

public class MyDemo01 {
	private String content;
	private String getContent() {
		return content;
	}
	private void setContent(String content) {
		this.content = content;
	}
	public static void main(String[] args) {
		final MyDemo01 demo = new MyDemo01();
		for (int i = 0; i < 5; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					demo.setContent(Thread.currentThread().getName()+"的数据");
					//System.out.println("-----");
					System.out.println(Thread.currentThread().getName() + "--->" +demo.getContent());
				}
			});
			thread.setName("线程" +i);
			thread.start();
		}
	}
}
```

输出：线程取到了其它线程的数据。

```
线程0--->线程1的数据
线程3--->线程3的数据
线程2--->线程2的数据
线程1--->线程1的数据
线程4--->线程4的数据
```

**例子二**

```java
package com.test.threadlocal;

public class MyDemo02 {
	ThreadLocal<String> t1 = new ThreadLocal<String>();
	private String getContent() {
		return t1.get();
	}
	private void setContent(String content) {
		t1.set(content);
	}
	public static void main(String[] args) {
		final MyDemo02 demo = new MyDemo02();
		for (int i = 0; i < 5; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					demo.setContent(Thread.currentThread().getName()+"的数据");
					//System.out.println("-----------------");
					System.out.println(Thread.currentThread().getName() + "--->" +demo.getContent());
				}
			});
			thread.setName("线程" +i);
			thread.start();
		}
	}
}
```

 输出：线程取到对应线程的数据

```
线程0--->线程0的数据
线程3--->线程3的数据
线程2--->线程2的数据
线程1--->线程1的数据
线程4--->线程4的数据
```

# 第二部分   ThreadLocal的内部结构

## 2.1、  常见的误解

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308110636413.png" alt="image-20210308110636413" style="zoom: 33%;" />

​                               

如果不去看源码的话，可能会猜测ThreadLocal是这样子设计的：

1. 每个ThreadLocal都创建一个Map，线程作为key，存储的局部变量作为value。
2. 这样就达到各个线程的局部变量隔离的效果。这是最简单的设计，JDK早起的threadlocal确实是这样设计的，但是现在已经不是了。

## 2.2、  现在的设计

JDK后面优化了设计方案，在JDK8中对ThreadLocal进行了重新设计：

1. 每个Thread线程内部都有一个Map和线程的变量副本value。
2. Map里面存储的key为ThreadLocal对象，value为线程的变量副本。
3. Thread内的Map是由ThreadLocal维护的，由ThreadLocal负责向Map获取和设置线程的变量值。
4. 对于不同的线程，每次获得副本值时，别的线程并不能获取到当前线程的副本值，形成了副本的隔离，互不干扰。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308110828441.png" alt="image-20210308110828441" style="zoom:33%;" />

**区别：**

1. 上面是由ThreadLocal维护这个ThreadLocalMap。下面是由Thread维护ThreadLocalMap。

**好处：**

1. 每个Map存储的Entry数量变少。

   因为上面map中key是Thread，有多少线程就有多少Entry。下面的key是ThreadLocal，实际的开发过程中，ThreadLocal的数量肯定远远少于线程的数量。这样也尽量避免了hash冲突的问题。

2. 当Thread销毁的时候，ThreadLocalMap也会随之销毁，减少了内存的使用。

# 第三部分   ThreadLocal的核心方法源码

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308110930618.png" alt="image-20210308110930618" style="zoom:33%;" />

## 3.1、  Set方法

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210308111001467.png" alt="image-20210308111001467" style="zoom:33%;" />

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210308111039947.png" alt="image-20210308111039947" style="zoom:33%;" />

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111107306.png" alt="image-20210308111107306" style="zoom:33%;" />

 总结：

1.  首先获取当前线程，并根据当前线程获取一个MAP。
2. 如果获取的MAP不为空，则将参数设置到MAP中（当前ThreadLocal的引用作为key）
3. 如果map为空，则给当前线程创建map，并设置初始值。

## 3.2、  get方法

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111136280.png" alt="image-20210308111136280" style="zoom:33%;" />

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111208129.png" alt="image-20210308111208129" style="zoom:33%;" />

 **总结：**

1. 首先获取当前线程，根据当前线程获取一个map。
2. 如果获取到的map不为空，则在map中以ThreadLocal的引用作为key在map中取得对应的entry，否则转到4步。
3. 如果e不为空则返回e.value，否则转到4步
4. Map为空或者e为空，则通过initialValue函数获取初始值value，然后用ThreadLocal的引用和value作为firstkey和firstvalue创建一个信的map。

总之：**线获取当前线程的map，如果存在则返回值，不存在则创建并返回初始值。**

## 3.3、  remove方法

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111412322.png" alt="image-20210308111412322" style="zoom:33%;" />

总结：

1. 首先获取当前想成，并根据当前线程获取一个map
2. 如果获取的map不为空，则删除当threadlocal对象对应的entry。

## 3.4、  initialValue方法

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111450771.png" alt="image-20210308111450771" style="zoom:33%;" />

总结：

1. 这个方法是一个延迟调用方法，从上面的代码我们知道，在set方法还未调用而先调用了get方法时才会执行，并切执行一个。
2. 这个方法缺省实现时返回一个null
3. 如果想要一个除null之外的初始值，可以重写此方法。（备注：该方法是一个protected的方法，显然是为了让子类覆盖而设计的）

# 第四部分   TheadLocalMap的基本结构

## 4.1、  ThreadLocalMap基本结构

TheadLocalMap是TheadLocal的内部类，它没有实现map接口，它以独立的方式实现了map的功能，其内部的entry也是独立实现的。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210309120103868.png" alt="image-20210309120103868" style="zoom: 50%;" />![image-20210309120245235](https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210309120245235.png)<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210309120103868.png" alt="image-20210309120103868" style="zoom: 50%;" />![image-20210309120245235](https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210309120245235.png)

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111839331.png" alt="image-20210308111839331" style="zoom:33%;" />

 说明：

1. Theadlocal中提供的set、get、remove方法的底层实现都是通过map的方法完成的。
2. Entry继承了WeakReference弱引用，其目的是将Theadlocal对象的生命周期和线程生命周期解绑。
3.  Map里面的key是Theadlocal，并不是thread，这点要记住。 

## 4.2、ThreadLocalMap成员变量

```java
static class ThreadLocalMap {

        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         * 
         * Entry继承WeakReference，并且用ThreadLocal作为key。
         * 如果key为null（entry.get() == null）,意味着key不再被引用，
         * 因此这个时候entry也可以从table中清除。
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         * 初始容量 - 必须是2的整次幂
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two.
         * 存放数据的table，Entry类的定义会在下面分析。
         * 同样，数组长度必须是2的整数幂。
         */
        private Entry[] table;

        /**
         * The number of entries in the table.
         * 数组里面entrys的个数，可以用于判断table当前使用量是否超过阀值
         */
        private int size = 0;

        /**
         * The next size value at which to resize.
         * 运行扩容的阀值，表使用量大于它的时候进行扩容
         */
        private int threshold; // Default to 0



        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * Get the entry associated with key.  This method
         * itself handles only the fast path: a direct hit of existing
         * key. It otherwise relays to getEntryAfterMiss.  This is
         * designed to maximize performance for direct hits, in part
         * by making this method readily inlinable.
         *
         * @param  key the thread local object
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * Version of getEntry method for use when key is not found in
         * its direct hash slot.
         *
         * @param  key the thread local object
         * @param  i the table index for key's hash code
         * @param  e the entry at table[i]
         * @return the entry associated with key, or null if no such
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null)
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * Set the value associated with key.
         *
         * @param key the thread local object
         * @param value the value to be set
         */
        private void set(ThreadLocal<?> key, Object value) {

            // We don't use a fast path as with get() because it is at
            // least as common to use set() to create new entries as
            // it is to replace existing ones, in which case, a fast
            // path would fail more often than not.

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);

            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    e.value = value;
                    return;
                }

                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            tab[i] = new Entry(key, value);
            int sz = ++size;
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * Remove the entry for key.
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len-1);
            for (Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * Replace a stale entry encountered during a set operation
         * with an entry for the specified key.  The value passed in
         * the value parameter is stored in the entry, whether or not
         * an entry already exists for the specified key.
         *
         * As a side effect, this method expunges all stale entries in the
         * "run" containing the stale entry.  (A run is a sequence of entries
         * between two null slots.)
         *
         * @param  key the key
         * @param  value the value to be associated with key
         * @param  staleSlot index of the first stale entry encountered while
         *         searching for key.
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value,
                                       int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;

            // Back up to check for prior stale entry in current run.
            // We clean out whole runs at a time to avoid continual
            // incremental rehashing due to garbage collector freeing
            // up refs in bunches (i.e., whenever the collector runs).
            int slotToExpunge = staleSlot;
            for (int i = prevIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = prevIndex(i, len))
                if (e.get() == null)
                    slotToExpunge = i;

            // Find either the key or trailing null slot of run, whichever
            // occurs first
            for (int i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                // If we find key, then we need to swap it
                // with the stale entry to maintain hash table order.
                // The newly stale slot, or any other stale slot
                // encountered above it, can then be sent to expungeStaleEntry
                // to remove or rehash all of the other entries in run.
                if (k == key) {
                    e.value = value;

                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // Start expunge at preceding stale entry if it exists
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // If we didn't find stale entry on backward scan, the
                // first stale entry seen while scanning for key is the
                // first still present in the run.
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // If key not found, put new entry in stale slot
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // If there are any other stale entries in run, expunge them
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * Expunge a stale entry by rehashing any possibly colliding entries
         * lying between staleSlot and the next null slot.  This also expunges
         * any other stale entries encountered before the trailing null.  See
         * Knuth, Section 6.4
         *
         * @param staleSlot index of slot known to have null key
         * @return the index of the next null slot after staleSlot
         * (all between staleSlot and this slot will have been checked
         * for expunging).
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;

            // expunge entry at staleSlot
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;

            // Rehash until we encounter null
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len);
                 (e = tab[i]) != null;
                 i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;

                        // Unlike Knuth 6.4 Algorithm R, we must scan until
                        // null because multiple entries could have been stale.
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * Heuristically scan some cells looking for stale entries.
         * This is invoked when either a new element is added, or
         * another stale one has been expunged. It performs a
         * logarithmic number of scans, as a balance between no
         * scanning (fast but retains garbage) and a number of scans
         * proportional to number of elements, that would find all
         * garbage but would cause some insertions to take O(n) time.
         *
         * @param i a position known NOT to hold a stale entry. The
         * scan starts at the element after i.
         *
         * @param n scan control: {@code log2(n)} cells are scanned,
         * unless a stale entry is found, in which case
         * {@code log2(table.length)-1} additional cells are scanned.
         * When called from insertions, this parameter is the number
         * of elements, but when from replaceStaleEntry, it is the
         * table length. (Note: all this could be changed to be either
         * more or less aggressive by weighting n instead of just
         * using straight log n. But this version is simple, fast, and
         * seems to work well.)
         *
         * @return true if any stale entries have been removed.
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                if (e != null && e.get() == null) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ( (n >>>= 1) != 0);
            return removed;
        }

        /**
         * Re-pack and/or re-size the table. First scan the entire
         * table removing stale entries. If this doesn't sufficiently
         * shrink the size of the table, double the table size.
         */
        private void rehash() {
            expungeStaleEntries();

            // Use lower threshold for doubling to avoid hysteresis
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * Double the capacity of the table.
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * Expunge all stale entries in the table.
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
```



# 第五部分   内存泄漏与weakReference的关系

有些程序员在使用ThreadLocal的过程中会发现有内存泄漏的情况发生，就猜测这个内存泄漏跟Entry中使用了弱引用的key有关系。这个理解其实是不对的。我们先来回顾一下这个问题中设计到的几个名词概念，在来分析问题。

## 5.1、  内存泄漏相关概念

- Memory overflow：内存溢出，指没有足够的内存提供申请者使用。
- Memory leak：内存泄漏，指程序中已经动态分配的堆内存由于某种原因程序未释放或我无法释放，造成系统内存的浪费，导致程序运行速度减慢甚至系统崩溃等严重后果。内存泄漏的堆积将导致内存溢出。

## 5.2、  弱引用相关概念

略

## 5.3、  如果ThreadLoaclMap中的key使用强引用

假设ThreadLocalMap中的key使用了强引用，那么会出现内存泄漏吗？

此时ThreadLocal的内存图（实线表示强引用）如下：

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210309151742584.png" alt="image-20210309151742584" style="zoom:50%;" />

结论：就算是Entry使用了强引用，也无法避免消亡的线程对应的entry出现内存泄漏。

## 5.4、  Threadlocal出现内存泄漏的真实原因

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308111916928.png" alt="image-20210308111916928" style="zoom: 50%;" /> 

## 5.5、  为什么使用弱应用

根据刚才的分析，我们知道了：无论ThreadLocalMap中的key使用那种类型的引用都无法避免内存泄漏，也就是内存泄漏跟使用弱应用没有任何关系。要避免内存泄漏，有两种方式：

1. 使用完TheadLocal，调用remove方法删除对应的Entry。
2. 使用完ThreadLocal，当前Thread也随之运行结束

相对于第一种方式，第二种方式显然不好控制，特别是使用线程池的时候，线程结束时不会销毁的而是放入了池中。只能走第二中方式，也就是说，只要记得在使用完ThreadLocal及时的调用remove，无论key时强引用还是弱引用都不会有问题。

那么，为什么这里我们要使用弱应用呢？实际上，在ThreadLocalMap中的set/getEntry方法中，会对key未null（也就是ThreadLocal为null）进行判断，如果为null的话，是会对value设置为null的。

这就意味着使用完threadLocal，CurrentThread依然运行的前提下，就算忘记调用remove方法，**弱应用比强应用可以多一层保障**：弱引用的ThreadLoca会被回收，对应的value在下一次ThreadLoacalMap调用set/get/remove中的任意一个方法的时候都会被清除，从而避免了内存泄漏。

**总结：**

1. 内存泄漏的真正原因：

   - 使用完Threadlocal没有删除对应的Entry。

   - Map的生命周期与线程是一样的长。

2. 无论ThreadLocalMap中的key使用的是强引用还是弱引用，都无法避免内存泄漏的出现。

3. 真正要解决内存泄漏的方式是两种：

   - 使用完Threadlocal，调用remove方法删除对应的Entry。

   - 使用完ThreadLocal，当前Thread也随之运行结束。

##  5.5、  哈希冲突的解决

hash冲突的解决是Map中的一个重要内容。我们以hash冲突的解决为线索，来研究一下ThreadLocalMap的核心源码。

（1）首先从TheadLocal的set方法入手

```java
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            // 调用了ThreadLocalMap的set方法
            map.set(this, value);
        else
            createMap(t, value);
    }


    void createMap(Thread t, T firstValue) {
        // 调用ThreadLocalMap的构造方法
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }
```

这段代码有两个地方分别涉及到ThreadLocalMap的两个方法，我们接着分析这两个方法。

（2）ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) 

```java
        /**
         * firstKey：本ThreadLocal实例（this）
         * firstValue：要保存的线程本地变量
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            // 初始化table
            table = new Entry[INITIAL_CAPACITY];
            // 计算索引（重点代码）
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            // 设置值
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            // 设置阀值
            setThreshold(INITIAL_CAPACITY);
        }
```

构造函数首先创建了一个长度为16的Entry数组，然后计算出firstKey对应的索引，然后存储到table中，并设置size和threshold。

**重点分析**：

```java
int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
```

a、关于firstKey.threadLocalHashCode：

```java
private final int threadLocalHashCode = nextHashCode();


private static int nextHashCode() {
    return nextHashCode.getAndAdd(HASH_INCREMENT);
}


// AtomicInteger 是一个提供院子操作的Integer类，通过线程安全的方式操作加减，适合高并发情况下的使用
private static AtomicInteger nextHashCode = new AtomicInteger();


// 特殊的hash值：这个值与斐波那契数列（黄金分割数）有关，其主要目的就是为了让哈希码能均匀分股在2的n次方的数组里，也就是Entry[] table中，这样尽量避免hash冲突
private static final int HASH_INCREMENT = 0x61c88647;
```

b、关于(INITIAL_CAPACITY - 1)

计算hash的时候里面采用了hashCode & （size - 1）的算法，这相当于取模运算hashCode % size的一个更高效的实现，正是因为这种算法，我们要求size必须是2的整数次幂，这也能保证在索引不越界的前提下，使得hash发生冲突的次数减小。

（3）ThreadLocalMap中的set方法

```java
         private void set(ThreadLocal<?> key, Object value) {
           
            Entry[] tab = table;
            int len = tab.length;
           
            // 计算索引（重点代码：刚才分析过了）
            int i = key.threadLocalHashCode & (len-1);
            
            /**
             * 使用线性探测法查找元素（重点代码）
             * 大致思路：利用上面去的的索引，从tab中取得索引位。
             */
            for (Entry e = tab[i]; 
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();

                // ThreadLocal对应的key存在，直接覆盖之前的值
                if (k == key) {
                    e.value = value;
                    return;
                }

                // key为null，但是值不为null，说明之前的ThreadLocal对象已经被回收了。
                // 当前数组中的Entry是一个陈旧（stale）的元素
                if (k == null) {
                    // 用新元素替换陈旧的元素，这个方法进行了不少的垃圾清理动作，防止内存泄漏
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            // ThreadLocal对应的key不存在并且没有找到陈旧的元素，则在空元素的位置创建一个新的Entry。
            tab[i] = new Entry(key, value);
            int sz = ++size;
           
            // cleanSomeSlots用于清除e.get() == null的元素，
            // 这种数据key关联的对象已经被回收，所以这个Entry(table[index])可以被设置为null。
            // 如果没有清除任何entry，并且当前使用量达到了负载因子所定义（长度的2/3），那么运行rehash（执行一次全表的扫描清理工作）
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }


        /**
         * 取得环形数组的下一个索引
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }
```

**代码执行流程：**

1. 首先还是根据key计算出索引，然后查找位置上的Entry。
2. 若是Entry已经存在并且key等于传入的key，那么这时候直接给这个Entry赋新的value值。
3. 若是Entry存在，但是key为null，则调用replaceStaleEntry来更换这个key为空的Entry。
4. 不段循环检测，知道遇到为null的地方。这时候要是还没有在循环过程中return，那么就在这个null的位置新建一个Entry，并且插入，同时size增加1
5. 最后调用cleanSomeSlots，清理key为null的Entry，最后返回是否清理了Entry，接下来判断size是否>=thresfole达到了rehash的条件，达到的话就呼调用rehash函数执行一次全表的扫描清理。

**重点分析：**

该方法一次探测下一个地址，知道有空的地址后插入，若整个空间都找不到空余的地址，则产生溢出。

锯割例子，加入当前table长度为16，也就是说如果计算出来的key的hash值为14，如果table[14]上已经有值，并且其key与当前的key不一致，那么就发生了hash冲突，这个时候将14+1=15，取得table[15]进行判断，这个时候如果还是冲突会回到0，取得table[0]，以此类推，知道可以插入。

按照上面的描述，可以吧entry[] table堪称一个环形数组。

















 

 