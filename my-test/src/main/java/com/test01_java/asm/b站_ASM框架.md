# **ASM框架**

**https://www.bilibili.com/video/BV1Tt411u7Zz?from=search&seid=9506151271103448400**

# 第一部分   基本资料

## 1.1、  官网地址

https://asm.ow2.io

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308104930588.png" alt="image-20210308104930588" style="zoom: 25%;" />

使用场景：

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308145453708.png" alt="image-20210308145453708" style="zoom:50%;" />

## 1.2、  学习路径指南

1. 理解class结构
2. 理解掌握JVM指令码执行过程
3. 理解操作数栈的原理
4. 理解局部变量表

# 第二部分   class字节码与jvm指令码的关系

- class结构解析
- JVM指令吗
- 线程栈
- 局部变量表

## 2.1、  一道面试题

**第一段代码：**

```java
package com.test;

public class PT00_ReadMe01 {
	public static void main(String[] args) {
		int i = 2;
		i = i++;
		System.out.println(i);
	}
}

// 输出 2
```

对应的字节码如下：

```java
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe01 {

  // compiled from: PT00_ReadMe01.java

  // access flags 0x1
  public <init>()V                                                // 默认构造函数
   L0
    LINENUMBER 3 L0                                               // 源码第3行
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe01; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0                                              // 源码第5行
    ICONST_2                                                     // 常量2入栈
    ISTORE 1                                                     // 把栈顶的值移到局部变量表的1号位置（0号位被args占据了）
   L1
    LINENUMBER 6 L1                                              // 源码第6行
    ILOAD 1                                                      // 把局部变量表的1号位置的值拷贝到操作数栈
    IINC 1 1                                                     // 在局部变量表中执行自增
    ISTORE 1                                                     // 把栈顶的值移到局部变量表的1号位置===>覆盖了
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    RETURN
   L4
    LOCALVARIABLE args [Ljava/lang/String; L0 L4 0                // 局部变量 args
    LOCALVARIABLE i I L1 L4 1                                     // 局部变量 i
    MAXSTACK = 2
    MAXLOCALS = 2                                                 // 局部变量表只有2个位置：args、i
}
```

**第二段代码：**

```java
package com.test;

public class PT00_ReadMe02 {
	public static void main(String[] args) {
		int i = 2;
		int b = i++;
		System.out.println(i);
	}
}

// 输出3
```

```java
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe02 {

  // compiled from: PT00_ReadMe02.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe02; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0                                              // 源码第5行
    ICONST_2                                                     // 常量2入栈
    ISTORE 1                                                     // 把栈顶的值移到局部变量表的1号位置（0号位被args占据了）
   L1
    LINENUMBER 6 L1                                              // 源码第6行
    ILOAD 1                                                      // 把局部变量表的1号位置的值拷贝到操作数栈
    IINC 1 1                                                     // 在局部变量表中执行自增
    ISTORE 2                                                     // 把栈顶的值移到局部变量表的2号位置 ===>没有覆盖
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    RETURN
   L4
    LOCALVARIABLE args [Ljava/lang/String; L0 L4 0                // 局部变量args
    LOCALVARIABLE i I L1 L4 1                                     // 局部变量i
    LOCALVARIABLE b I L2 L4 2                                     // 局部变量b
    MAXSTACK = 2
    MAXLOCALS = 3                                                // 局部变量表只有3个位置：args、i、b
}
```

> 比较：
>
> 根本原因在于局部变量表的长度不一样：
>
> 上面是：args、i
>
> 下面是：args、i、b
>
> 第6行代码执行后，是把栈顶的元素2写到了【下标=2】的b中，并未覆盖【下标=1】的i中。
>
> 然后打印的时候，【下标=1】的i被打印出来了。



>  结论：
>
> **1、自增不走栈，直接走cpu指令，在局部变量表中进行**
>
> 2、输出的时候，把操作数栈的数据打印出来了。
>
> 3、++i的话会输出3：因为代码会是先自增、在iload、再istore
>
> 4、第二段代码打印3的原因：操作数栈有三个位置，第三个是给b，最后的指令是ISTORE 2（不是ISTORE 1），所以不会覆盖。

**栈帧结构：**操作数栈、局部变量表是两段连续的内存空间。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308101454066.png" alt="image-20210308101454066" style="zoom: 33%; " />

> 在执行jvm指令码的过程中，是用一条线程来执行的，一个线程对应一个栈，每一个方法对应一个栈帧。
>
> 当程序进入到某个方法后，激活对应栈帧之后，会分配两个连续的内存空间：操作数栈、局部变量表。
>
> 操作数栈的大小早就固定了的。例子中的操作数栈事2slot。
>
> 局部变量表的大小在进入当前栈帧之后也是固定的，局部变量表有下标。

## 2.2、  字节码概述

### 1、操作数栈空间的计算

> 操作数栈和局部变量表的容量在开始的时候就计算好了。
>
> 一个指令最多的参数，就是操作数栈的大小

```java
// 案例一

package com.test;

public class PT00_ReadMe01 {
	public static void main(String[] args) {
		int i = 2;                                         // 这条指令：需要一个栈空间
		i = i++;                                           // 这条指令：不需要
		System.out.println(i);                             // 这条指令：需要两个 - 静态out、"hello"
	}
}

// 所以上面代码的操作数栈空间 MAXSTACK=2
```

```java
// 案例二

package com.test;

public class PT00_ReadMe03 {
	public static void main(String[] args) {
		int i = 2;                                         // 这条指令：需要一个栈空间
		i = i++;                                           // 这条指令：不需要
		System.out.println(i);                             // 这条指令：需要2个 - 静态out、"hello"
		test("a", "b", "c", "d");                          // 这条指令：需要4个
	}

  // 静态方法
	public static void test(String a, String b, String c, String d) {}
}

// 所以上面代码的操作数栈空间 MAXSTACK=4

------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe03 {

  // compiled from: PT00_ReadMe03.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe03; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 6 L1
    ILOAD 1
    IINC 1 1
    ISTORE 1
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    LDC "a"
    LDC "b"
    LDC "c"
    LDC "d"
    INVOKESTATIC com/test/asm/PT00_ReadMe03.test (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V       // 静态方法不需要对象作为参数进行调用
   L4
    LINENUMBER 9 L4
    RETURN
   L5
    LOCALVARIABLE args [Ljava/lang/String; L0 L5 0
    LOCALVARIABLE i I L1 L5 1
    MAXSTACK = 4
    MAXLOCALS = 2

  // access flags 0x9
  public static test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 11 L0
    RETURN
   L1
    LOCALVARIABLE a Ljava/lang/String; L0 L1 0                                           // 局部变量 a
    LOCALVARIABLE b Ljava/lang/String; L0 L1 1                                           // 局部变量 b
    LOCALVARIABLE c Ljava/lang/String; L0 L1 2                                           // 局部变量 b
    LOCALVARIABLE d Ljava/lang/String; L0 L1 3                                           // 局部变量 d
    MAXSTACK = 0
    MAXLOCALS = 4                                                                        // 局部变量表一共是4个
}
------------------------------------------------------------------------------------------------------------         
```

```java
// 案例三

package com.test;

public class PT00_ReadMe04 {
	public static void main(String[] args) {
		int i = 2;                                         // 这条指令：需要一个栈空间
		i = i++;                                           // 这条指令：不需要
		System.out.println(i);                             // 这条指令：需要2个 - 静态out、"hello"
		new PT00_ReadMe04().test("a", "b", "c", "d");      // 这条指令：需要5个 = this、a、b、c、d
	}

  // 非静态方法
	public void test(String a, String b, String c, String d) {}
}

// 所以上面代码的操作数栈空间 MAXSTACK=5

------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe04 {

  // compiled from: PT00_ReadMe04.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe04; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 6 L1
    ILOAD 1
    IINC 1 1
    ISTORE 1
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    NEW com/test/asm/PT00_ReadMe04                                                      // 新建对象
    DUP                                                                                 // 复制到操作数栈
    INVOKESPECIAL com/test/asm/PT00_ReadMe04.<init> ()V
    LDC "a"
    LDC "b"
    LDC "c1"
    LDC "d"
    INVOKEVIRTUAL com/test/asm/PT00_ReadMe04.test (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L4
    LINENUMBER 9 L4
    RETURN
   L5
    LOCALVARIABLE args [Ljava/lang/String; L0 L5 0
    LOCALVARIABLE i I L1 L5 1
    MAXSTACK = 5
    MAXLOCALS = 2

  // access flags 0x1
  public test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 11 L0
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe04; L0 L1 0                              // 局部变量this
    LOCALVARIABLE a Ljava/lang/String; L0 L1 1                                           // 局部变量 a
    LOCALVARIABLE b Ljava/lang/String; L0 L1 2                                           // 局部变量 b
    LOCALVARIABLE c Ljava/lang/String; L0 L1 3                                           // 局部变量 c
    LOCALVARIABLE d Ljava/lang/String; L0 L1 4                                           // 局部变量 d
    MAXSTACK = 0
    MAXLOCALS = 5                                                                       // 局部变量表一共是5个
}
------------------------------------------------------------------------------------------------------------
```

```java
// 案例四

package com.test;

public class PT00_ReadMe04 {
	public static void main(String[] args) {
		int i = 2;                                         // 这条指令：需要一个栈空间
		i = i++;                                           // 这条指令：不需要
		System.out.println(i);                             // 这条指令：需要2个 - 静态out、"hello"
		new PT00_ReadMe04().test("a", "b", "c"+1, "d");    // 这条指令：需要5个 = this、a、b、c、d 
	}

	public void test(String a, String b, String c, String d) {}
}

//该例子虽然是["c" + 1]，但是还是5个：因为被编译器优化掉了
```

编译器进行了优化

![image-20210308103627151](https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308103627151.png)



如果不想被优化：

```java
package com.test;

public class PT00_ReadMe04 {
	public static void main(String[] args) {
		int i = 2;
		i = i++;
		System.out.println(i);
		new PT00_ReadMe04().test("a", "c".equals("b")+"",  "b", "d");  // 5个：this、a、c、b、""
    new PT00_ReadMe04().test("a", "b", "c".equals("b")+"", "d");   // 6个：this、a、b、c、b、""（equals执行之后释放了）
    new PT00_ReadMe04().test("a", "b", "d", "c".equals("b")+"");   // 7个：this、a、b、d、c、b、""
	}

	public void test(String a, String b, String c, String d) {}
}
```

> 栈在刚开始执行的时候都是空的。
>
> 当执行一条指令的时候，会把值放到栈顶去。

### 2、局部变量表的计算

> 局部变量表也是在一开始就计算好了的。
>
> 它的计算方式是：看方法中有多少个局部变量。

```java
// 案例一 -- INVOKESTATIC

package com.test;

public class PT00_ReadMe03 {
	public static void main(String[] args) { 
		int i = 2;                                         
		i = i++;                                           
		System.out.println(i);                             
		test("a", "b", "c", "d");                          
	}

  // 静态方法
	public static void test(String a, String b, String c, String d) {}
}


------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe03 {

  // compiled from: PT00_ReadMe03.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe03; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 6 L1
    ILOAD 1
    IINC 1 1
    ISTORE 1
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    LDC "a"
    LDC "b"
    LDC "c"
    LDC "d"
    INVOKESTATIC com/test/asm/PT00_ReadMe03.test (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V              // 静态方法不需要对象作为参数进行调用
   L4
    LINENUMBER 9 L4
    RETURN
   L5
    LOCALVARIABLE args [Ljava/lang/String; L0 L5 0                                       // 局部变量 args
    LOCALVARIABLE i I L1 L5 1                                                            // 局部变量 i
    MAXSTACK = 4                                                                         // 操作数栈：a、b、c、d
    MAXLOCALS = 2                                                                        // 局部变量表一共是2个slot

  // access flags 0x9
  public static test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 11 L0
    RETURN
   L1
    LOCALVARIABLE a Ljava/lang/String; L0 L1 0                                           // 局部变量 a
    LOCALVARIABLE b Ljava/lang/String; L0 L1 1                                           // 局部变量 b
    LOCALVARIABLE c Ljava/lang/String; L0 L1 2                                           // 局部变量 b
    LOCALVARIABLE d Ljava/lang/String; L0 L1 3                                           // 局部变量 d
    MAXSTACK = 0
    MAXLOCALS = 4                                                                        // 局部变量表一共是4个slot
}
------------------------------------------------------------------------------------------------------------   
```

```java
// 案例二 -- INVOKEVIRTUAL

package com.test;

public class PT00_ReadMe04 {
	public static void main(String[] args) {
		int i = 2;                                         // 这条指令：需要一个栈空间
		i = i++;                                           // 这条指令：不需要
		System.out.println(i);                             // 这条指令：需要2个 - 静态out、"hello"
		new PT00_ReadMe04().test("a", "b", "c", "d");      // 这条指令：需要5个 = this、a、b、c、d
	}

  // 非静态方法
	public void test(String a, String b, String c, String d) {}
}

// 所以上面代码的操作数栈空间 MAXSTACK=5

------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe04 {

  // compiled from: PT00_ReadMe04.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe04; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 6 L1
    ILOAD 1
    IINC 1 1
    ISTORE 1
   L2
    LINENUMBER 7 L2
    GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
    ILOAD 1
    INVOKEVIRTUAL java/io/PrintStream.println (I)V
   L3
    LINENUMBER 8 L3
    NEW com/test/asm/PT00_ReadMe04                                                      // 新建对象
    DUP                                                                                 // 复制到操作数栈
    INVOKESPECIAL com/test/asm/PT00_ReadMe04.<init> ()V
    LDC "a"
    LDC "b"
    LDC "c1"
    LDC "d"
    INVOKEVIRTUAL com/test/asm/PT00_ReadMe04.test (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L4
    LINENUMBER 9 L4
    RETURN
   L5
    LOCALVARIABLE args [Ljava/lang/String; L0 L5 0
    LOCALVARIABLE i I L1 L5 1
    MAXSTACK = 5                                                                         // 操作数栈5个  ：this、a、b、c、d
    MAXLOCALS = 2                                                                        // 局部变量表2个：args i

  // access flags 0x1
  public test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 11 L0
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe04; L0 L1 0                              // 局部变量 this
    LOCALVARIABLE a Ljava/lang/String; L0 L1 1                                           // 局部变量 a
    LOCALVARIABLE b Ljava/lang/String; L0 L1 2                                           // 局部变量 b
    LOCALVARIABLE c Ljava/lang/String; L0 L1 3                                           // 局部变量 c
    LOCALVARIABLE d Ljava/lang/String; L0 L1 4                                           // 局部变量 d
    MAXSTACK = 0
    MAXLOCALS = 5                                                                        // 局部变量表一共是5个
}
------------------------------------------------------------------------------------------------------------
说明：
1、INVOKEVIRTUAL指令必须传递this。而上面的INVOKESTATIC指令是不需要this的。所以这个例子会多一个this局部变量表。
```

```java
// 案例三 -- 作用域01/02

package com.test.asm;

public class PT00_ReadMe05 {
	public static void main(String[] args) {
		int i = 2;

		int a = 1;
	}

	public void test(String a, String b, String c, String d) {}
}

------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe05 {

  // compiled from: PT00_ReadMe05.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe05; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 5 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 7 L1
    ICONST_1
    ISTORE 2
   L2
    LINENUMBER 8 L2
    RETURN
   L3
    LOCALVARIABLE args [Ljava/lang/String; L0 L3 0
    LOCALVARIABLE i I L1 L3 1
    LOCALVARIABLE a I L2 L3 2
    MAXSTACK = 1
    MAXLOCALS = 3

  // access flags 0x1
  public test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 10 L0
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe05; L0 L1 0
    LOCALVARIABLE a Ljava/lang/String; L0 L1 1
    LOCALVARIABLE b Ljava/lang/String; L0 L1 2
    LOCALVARIABLE c Ljava/lang/String; L0 L1 3
    LOCALVARIABLE d Ljava/lang/String; L0 L1 4
    MAXSTACK = 0
    MAXLOCALS = 5
}
------------------------------------------------------------------------------------------------------------
```

```java
// 案例四 -- 作用域02/02

package com.test.asm;

public class PT00_ReadMe05 {
	public static void main(String[] args) {
		{
			int i = 2;
		} // 释放了
		{
			int a = 1;
		}
	}

	public void test(String a, String b, String c, String d) {}
}

------------------------------------------------------------------------------------------------------------
// class version 52.0 (52)
// access flags 0x21
public class com/test/asm/PT00_ReadMe05 {

  // compiled from: PT00_ReadMe05.java

  // access flags 0x1
  public <init>()V
   L0
    LINENUMBER 3 L0
    ALOAD 0
    INVOKESPECIAL java/lang/Object.<init> ()V
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe05; L0 L1 0
    MAXSTACK = 1
    MAXLOCALS = 1

  // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 6 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 9 L1
    ICONST_1
    ISTORE 1
   L2
    LINENUMBER 11 L2
    RETURN
   L3
    LOCALVARIABLE args [Ljava/lang/String; L0 L3 0
    MAXSTACK = 1
    MAXLOCALS = 2                                                           // 这里的局部变量表是2，比上面少一个的原因是：作用域过了会释放

  // access flags 0x1
  public test(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
   L0
    LINENUMBER 13 L0
    RETURN
   L1
    LOCALVARIABLE this Lcom/test/asm/PT00_ReadMe05; L0 L1 0
    LOCALVARIABLE a Ljava/lang/String; L0 L1 1
    LOCALVARIABLE b Ljava/lang/String; L0 L1 2
    LOCALVARIABLE c Ljava/lang/String; L0 L1 3
    LOCALVARIABLE d Ljava/lang/String; L0 L1 4
    MAXSTACK = 0
    MAXLOCALS = 5
}
------------------------------------------------------------------------------------------------------------
```

# 第三部分   ASM

## 3.1、  基于ASM编辑JVM字节码

### 3.1.1    ASM设计模式

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308104632879.png" alt="image-20210308104632879" style="zoom: 33%;" />

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210308104658234.png" alt="image-20210308104658234" style="zoom:33%;" />

 

### 3.1.2    代码

```java
	import jdk.internal.org.objectweb.asm.ClassReader;
	import jdk.internal.org.objectweb.asm.util.TraceClassVisitor;
	import java.io.IOException;
	import java.io.PrintWriter;
	
	public class AsmReader {
	    public static void main(String[] args) throws IOException {
	        ClassReader reader = new ClassReader("java.lang.String");                        // 读取java.lang.String类
	        TraceClassVisitor visit = new TraceClassVisitor(new PrintWriter(System.out));    // 
	        reader.accept(visit,0);
	    }
	}
```

### 3.1.3    字节码指令

操作码=Opcode===Opcode.java（ASM）

# 第四部分 附录：字节码指令表

## 4.1 Constants 常量相关

| 十进制 | 操作码 | 助记符      | 含义                                                         |
| ------ | ------ | ----------- | ------------------------------------------------------------ |
| 00     | 0x00   | nop         | 什么都不做                                                   |
| 01     | 0x01   | aconst_null | 把 null 推到操作数栈                                         |
| 02     | 0x02   | iconst_m1   | 把 int 常量 –1 推到操作数栈                                  |
| 03     | 0x03   | iconst_0    | 把 int 常量 0 推到操作数栈                                   |
| 04     | 0x04   | iconst_1    | 把 int 常量 1 推到操作数栈                                   |
| 05     | 0x05   | iconst_2    | 把 int 常量 2 推到操作数栈                                   |
| 06     | 0x06   | iconst_3    | 把 int 常量 3 推到操作数栈                                   |
| 07     | 0x07   | iconst_4    | 把 int 常量 4 推到操作数栈                                   |
| 08     | 0x08   | iconst_5    | 把 int 常量 5 推到操作数栈                                   |
| 09     | 0x09   | lconst_0    | 把 long 常量 0 推到操作数栈                                  |
| 10     | 0x0A   | lconst_1    | 把 long 常量 1 推到操作数栈                                  |
| 11     | 0x0B   | fconst_0    | 把 float 常量 0 推到操作数栈                                 |
| 12     | 0x0C   | fconst_1    | 把 float 常量 1 推到操作数栈                                 |
| 13     | 0x0D   | fconst_2    | 把 float 常量 2 推到操作数栈                                 |
| 14     | 0x0E   | dconst_0    | 把 double 常量 0 推到操作数栈                                |
| 15     | 0x0F   | dconst_1    | 把 double 常量 1 推到操作数栈                                |
| 16     | 0x10   | bipush      | 把单字节常量（-128~127）推到操作数栈                         |
| 17     | 0x11   | sipush      | 把 short 常量（-32768~32767）推到操作数栈                    |
| 18     | 0x12   | ldc         | 把常量池中的int，float，String型常量取出并推到操作数栈顶     |
| 19     | 0x13   | ldc_w       | 把常量池中的int，float，String型常量取出并推到操作数栈顶（宽索引） |
| 20     | 0x14   | ldc2_w      | 把常量池中的long，double型常量取出并推到操作数栈顶（宽索引） |



## 4.2 Loads 加载相关

| 十进制 | 操作码 | 助记符  | 含义                                            |
| ------ | ------ | ------- | ----------------------------------------------- |
| 21     | 0x15   | iload   | 把 int 型局部变量推到操作数栈                   |
| 22     | 0x16   | lload   | 把 long 型局部变量推到操作数栈                  |
| 23     | 0x17   | fload   | 把 float 型局部变量推到操作数栈                 |
| 24     | 0x18   | dload   | 把 double 型局部变量推到操作数栈                |
| 25     | 0x19   | aload   | 把引用型局部变量推到操作数栈                    |
| 26     | 0x1A   | iload_0 | 把局部变量第 1 个 int 型局部变量推到操作数栈    |
| 27     | 0x1B   | iload_1 | 把局部变量第 2 个 int 型局部变量推到操作数栈    |
| 28     | 0x1C   | iload_2 | 把局部变量第 3 个 int 型局部变量推到操作数栈    |
| 29     | 0x1D   | iload_3 | 把局部变量第 4 个 int 型局部变量推到操作数栈    |
| 30     | 0x1E   | lload_0 | 把局部变量第 1 个 long 型局部变量推到操作数栈   |
| 31     | 0x1F   | lload_1 | 把局部变量第 2 个 long 型局部变量推到操作数栈   |
| 32     | 0x20   | lload_2 | 把局部变量第 3 个 long 型局部变量推到操作数栈   |
| 33     | 0x21   | lload_3 | 把局部变量第 4 个 long 型局部变量推到操作数栈   |
| 34     | 0x22   | fload_0 | 把局部变量第 1 个 float 型局部变量推到操作数栈  |
| 35     | 0x23   | fload_1 | 把局部变量第 2 个 float 型局部变量推到操作数栈  |
| 36     | 0x24   | fload_2 | 把局部变量第 3 个 float 型局部变量推到操作数栈  |
| 37     | 0x25   | fload_3 | 把局部变量第 4 个 float 型局部变量推到操作数栈  |
| 38     | 0x26   | dload_0 | 把局部变量第 1 个 double 型局部变量推到操作数栈 |
| 39     | 0x27   | dload_1 | 把局部变量第 2 个 double 型局部变量推到操作数栈 |
| 40     | 0x28   | dload_2 | 把局部变量第 3 个 double 型局部变量推到操作数栈 |
| 41     | 0x29   | dload_3 | 把局部变量第 4 个 double 型局部变量推到操作数栈 |
| 42     | 0x2A   | aload_0 | 把局部变量第 1 个引用型局部变量推到操作数栈     |
| 43     | 0x2B   | aload_1 | 把局部变量第 2 个引用型局部变量推到操作数栈     |
| 44     | 0x2C   | aload_2 | 把局部变量第 3 个引用型局部变量推到操作数栈     |
| 45     | 0x2D   | aload_3 | 把局部变量第 4 个引用 型局部变量推到操作数栈    |
| 46     | 0x2E   | iaload  | 把 int 型数组指定索引的值推到操作数栈           |
| 47     | 0x2F   | laload  | 把 long 型数组指定索引的值推到操作数栈          |
| 48     | 0x30   | faload  | 把 float 型数组指定索引的值推到操作数栈         |
| 49     | 0x31   | daload  | 把 double 型数组指定索引的值推到操作数栈        |
| 50     | 0x32   | aaload  | 把引用型数组指定索引的值推到操作数栈            |
| 51     | 0x33   | baload  | 把 boolean或byte型数组指定索引的值推到操作数栈  |
| 52     | 0x34   | caload  | 把 char 型数组指定索引的值推到操作数栈          |
| 53     | 0x35   | saload  | 把 short 型数组指定索引的值推到操作数栈         |

## 4.3 Store 存储相关

| 十进制 | 操作码 | 助记符   | 含义                                              |
| ------ | ------ | -------- | ------------------------------------------------- |
| 54     | 0x36   | istore   | 把栈顶 int 型数值存入指定局部变量                 |
| 55     | 0x37   | lstore   | 把栈顶 long 型数值存入指定局部变量                |
| 56     | 0x38   | fstore   | 把栈顶 float 型数值存入指定局部变量               |
| 57     | 0x39   | dstore   | 把栈顶 double 型数值存入指定局部变量              |
| 58     | 0x3A   | astore   | 把栈顶引用型数值存入指定局部变量                  |
| 59     | 0x3B   | istore_0 | 把栈顶 int 型数值存入第 1 个局部变量              |
| 60     | 0x3C   | istore_1 | 把栈顶 int 型数值存入第 2 个局部变量              |
| 61     | 0x3D   | istore_2 | 把栈顶 int 型数值存入第 3 个局部变量              |
| 62     | 0x3E   | istore_3 | 把栈顶 int 型数值存入第 4 个局部变量              |
| 63     | 0x3F   | lstore_0 | 把栈顶 long 型数值存入第 1 个局部变量             |
| 64     | 0x40   | lstore_1 | 把栈顶 long 型数值存入第 2 个局部变量             |
| 65     | 0x41   | lstore_2 | 把栈顶 long 型数值存入第 3 个局部变量             |
| 66     | 0x42   | lstore_3 | 把栈顶 long 型数值存入第 4 个局部变量             |
| 67     | 0x43   | fstore_0 | 把栈顶 float 型数值存入第 1 个局部变量            |
| 68     | 0x44   | fstore_1 | 把栈顶 float 型数值存入第 2 个局部变量            |
| 69     | 0x45   | fstore_2 | 把栈顶 float 型数值存入第 3 个局部变量            |
| 70     | 0x46   | fstore_3 | 把栈顶 float 型数值存入第 4 个局部变量            |
| 71     | 0x47   | dstore_0 | 把栈顶 double 型数值存入第 1 个局部变量           |
| 72     | 0x48   | dstore_1 | 把栈顶 double 型数值存入第 2 个局部变量           |
| 73     | 0x49   | dstore_2 | 把栈顶 double 型数值存入第 3 个局部变量           |
| 74     | 0x4A   | dstore_3 | 把栈顶 double 型数值存入第 4 个局部变量           |
| 75     | 0x4B   | astore_0 | 把栈顶 引用 型数值存入第 1 个局部变量             |
| 76     | 0x4C   | astore_1 | 把栈顶 引用 型数值存入第 2 个局部变量             |
| 77     | 0x4D   | astore_2 | 把栈顶 引用 型数值存入第 3 个局部变量             |
| 78     | 0x4E   | astore_3 | 把栈顶 引用 型数值存入第 4 个局部变量             |
| 79     | 0x4F   | iastore  | 把栈顶 int 型数值存入数组指定索引位置             |
| 80     | 0x50   | lastore  | 把栈顶 long 型数值存入数组指定索引位置            |
| 81     | 0x51   | fastore  | 把栈顶 float 型数值存入数组指定索引位置           |
| 82     | 0x52   | dastore  | 把栈顶 double 型数值存入数组指定索引位置          |
| 83     | 0x53   | aastore  | 把栈顶 引用 型数值存入数组指定索引位置            |
| 84     | 0x54   | bastore  | 把栈顶 boolean or byte 型数值存入数组指定索引位置 |
| 85     | 0x55   | castore  | 把栈顶 char 型数值存入数组指定索引位置            |
| 86     | 0x56   | sastore  | 把栈顶 short 型数值存入数组指定索引位置           |



## 4.4 Stack 栈相关

| 十进制 | 操作码 | 助记符  | 含义                                                         |
| ------ | ------ | ------- | ------------------------------------------------------------ |
| 87     | 0x57   | pop     | 把栈顶数值弹出（非long，double数值）                         |
| 88     | 0x58   | pop2    | 把栈顶的一个long或double值弹出，或弹出2个其他类型数值        |
| 89     | 0x59   | dup     | 复制栈顶数值并把数值入栈                                     |
| 90     | 0x5A   | dup_x1  | 复制栈顶数值并将两个复制值压入栈顶                           |
| 91     | 0x5B   | dup_x2  | 复制栈顶数值并将三个（或两个）复制值压入栈顶                 |
| 92     | 0x5C   | dup2    | 复制栈顶一个（long 或double 类型的)或两个（其它）数值并将复制值压入栈顶 |
| 93     | 0x5D   | dup2_x1 | dup_x1 指令的双倍版本                                        |
| 94     | 0x5E   | dup2_x2 | dup_x2 指令的双倍版本                                        |
| 95     | 0x5F   | swap    | 把栈顶端的两个数的值交换（数值不能是long 或double 类型< td >的） |



## 4.5 Math 运算相关

  Java 虚拟机在处理浮点数运算时，不会抛出任何运行时异常，当一个操作产生溢出时，将会使用有符号的无穷大来表示，如果某个操作结果没有明确的数学定义的话，将会使用 NaN 值来表示。所有使用 NaN 值作为操作数的算术操作，结果都会返回 NaN。

| 十进制 | 操作码 | 助记符 | 含义                                           |
| ------ | ------ | ------ | ---------------------------------------------- |
| 96     | 0x60   | iadd   | 把栈顶两个 int 型数值相加并将结果入栈          |
| 97     | 0x61   | ladd   | 把栈顶两个 long 型数值相加并将结果入栈         |
| 98     | 0x62   | fadd   | 把栈顶两个 float 型数值相加并将结果入栈        |
| 99     | 0x63   | dadd   | 把栈顶两个 double 型数值相加并将结果入栈       |
| 100    | 0x64   | isub   | 把栈顶两个 int 型数值相减并将结果入栈          |
| 101    | 0x65   | lsub   | 把栈顶两个 long 型数值相减并将结果入栈         |
| 102    | 0x66   | fsub   | 把栈顶两个 float 型数值相减并将结果入栈        |
| 103    | 0x67   | dsub   | 把栈顶两个 double 型数值相减并将结果入栈       |
| 104    | 0x68   | imul   | 把栈顶两个 int 型数值相乘并将结果入栈          |
| 105    | 0x69   | lmul   | 把栈顶两个 long 型数值相乘并将结果入栈         |
| 106    | 0x6A   | fmul   | 把栈顶两个 float 型数值相乘并将结果入栈        |
| 107    | 0x6B   | dmul   | 把栈顶两个 double 型数值相乘并将结果入栈       |
| 108    | 0x6C   | idiv   | 把栈顶两个 int 型数值相除并将结果入栈          |
| 109    | 0x6D   | ldiv   | 把栈顶两个 long 型数值相除并将结果入栈         |
| 110    | 0x6E   | fdiv   | 把栈顶两个 float 型数值相除并将结果入栈        |
| 111    | 0x6F   | ddiv   | 把栈顶两个 double 型数值相除并将结果入栈       |
| 112    | 0x70   | irem   | 把栈顶两个 int 型数值模运算并将结果入栈        |
| 113    | 0x71   | lrem   | 把栈顶两个 long 型数值模运算并将结果入栈       |
| 114    | 0x72   | frem   | 把栈顶两个 float 型数值模运算并将结果入栈      |
| 115    | 0x73   | drem   | 把栈顶两个 double 型数值模运算并将结果入栈     |
| 116    | 0x74   | ineg   | 把栈顶 int 型数值取负并将结果入栈              |
| 117    | 0x75   | lneg   | 把栈顶 long 型数值取负并将结果入栈             |
| 118    | 0x76   | fneg   | 把栈顶 float 型数值取负并将结果入栈            |
| 119    | 0x77   | dneg   | 把栈顶 double 型数值取负并将结果入栈           |
| 120    | 0x78   | ishl   | 把 int 型数左移指定位数并将结果入栈            |
| 121    | 0x79   | lshl   | 把 long 型数左移指定位数并将结果入栈           |
| 122    | 0x7A   | ishr   | 把 int 型数右移指定位数并将结果入栈（有符号）  |
| 123    | 0x7B   | lshr   | 把 long 型数右移指定位数并将结果入栈（有符号） |
| 124    | 0x7C   | iushr  | 把 int 型数右移指定位数并将结果入栈（无符号）  |
| 125    | 0x7D   | lushr  | 把 long 型数右移指定位数并将结果入栈（无符号） |
| 126    | 0x7E   | iand   | 把栈顶两个 int 型数值 按位与 并将结果入栈      |
| 127    | 0x7F   | land   | 把栈顶两个 long 型数值 按位与 并将结果入栈     |
| 128    | 0x80   | ior    | 把栈顶两个 int 型数值 按位或 并将结果入栈      |
| 129    | 0x81   | lor    | 把栈顶两个 long 型数值 按或与 并将结果入栈     |
| 130    | 0x82   | ixor   | 把栈顶两个 int 型数值 按位异或 并将结果入栈    |
| 131    | 0x83   | lxor   | 把栈顶两个 long 型数值 按位异或 并将结果入栈   |
| 132    | 0x84   | iinc   | 把指定 int 型增加指定值                        |



## 4.6 Conversions 转换相关

  类型转换指令可以将两种不同的数值类型进行相互转换，这些转换操作一般用于实现用户代码中的显示类型转换操作。
  Java 虚拟机直接支持（即转换时无需显示的转换指令）小范围类型向大范围类型的安全转换，但在处理窄化类型转换时，必须显式使用转换指令来完成。

| 十进制 | 操作码 | 助记符 | 含义                            |
| ------ | ------ | ------ | ------------------------------- |
| 133    | 0x85   | i2l    | 把栈顶 int 强转 long 并入栈     |
| 134    | 0x86   | i2f    | 把栈顶 int 强转 float 并入栈    |
| 135    | 0x87   | i2d    | 把栈顶 int 强转 double 并入栈   |
| 136    | 0x88   | l2i    | 把栈顶 long 强转 int 并入栈     |
| 137    | 0x89   | l2f    | 把栈顶 long 强转 float 并入栈   |
| 138    | 0x8A   | l2d    | 把栈顶 long 强转 double 并入栈  |
| 139    | 0x8B   | f2i    | 把栈顶 float 强转 int 并入栈    |
| 140    | 0x8C   | f2l    | 把栈顶 float 强转 long 并入栈   |
| 141    | 0x8D   | f2d    | 把栈顶 float 强转 double 并入栈 |
| 142    | 0x8E   | d2i    | 把栈顶 double 强转 int 并入栈   |
| 143    | 0x8F   | d2l    | 把栈顶 double 强转 long 并入栈  |
| 144    | 0x90   | d2f    | 把栈顶 double 强转 float 并入栈 |
| 145    | 0x91   | i2b    | 把栈顶 int 强转 byte 并入栈     |
| 146    | 0x92   | i2c    | 把栈顶 int 强转 char 并入栈     |
| 147    | 0x93   | i2s    | 把栈顶 int 强转 short 并入栈    |



## 4.7 Comparisons 比较相关

| 十进制 | 操作码 | 助记符    | 含义                                                         |
| ------ | ------ | --------- | ------------------------------------------------------------ |
| 148    | 0x94   | lcmp      | 比较栈顶两long 型数值大小，并将结果（1，0，-1）压入栈顶      |
| 149    | 0x95   | fcmpl     | 比较栈顶两float 型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为“NaN”时，将-1 压入栈顶 |
| 150    | 0x96   | fcmpg     | 比较栈顶两float 型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为“NaN”时，将1 压入栈顶 |
| 151    | 0x97   | dcmpl     | 比较栈顶两double 型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为“NaN”时，将-1 压入栈顶 |
| 152    | 0x98   | dcmpg     | 比较栈顶两double 型数值大小，并将结果（1，0，-1）压入栈顶；当其中一个数值为“NaN”时，将1 压入栈顶 |
| 153    | 0x99   | ifeq      | 当栈顶 int 型数值等于0时，跳转                               |
| 154    | 0x9A   | ifne      | 当栈顶 int 型数值不等于0时，跳转                             |
| 155    | 0x9B   | iflt      | 当栈顶 int 型数值小于0时，跳转                               |
| 156    | 0x9C   | ifge      | 当栈顶 int 型数值大于等于0时，跳转                           |
| 157    | 0x9D   | ifgt      | 当栈顶 int 型数值大于0时，跳转                               |
| 158    | 0x9E   | ifle      | 当栈顶 int 型数值小于等于0时，跳转                           |
| 159    | 0x9F   | if_icmpeq | 比较栈顶两个 int 型数值，等于0时，跳转                       |
| 160    | 0xA0   | if_icmpne | 比较栈顶两个 int 型数值，不等于0时，跳转                     |
| 161    | 0xA1   | if_icmplt | 比较栈顶两个 int 型数值，小于0时，跳转                       |
| 162    | 0xA2   | if_icmpge | 比较栈顶两个 int 型数值，大于等于0时，跳转                   |
| 163    | 0xA3   | if_icmpgt | 比较栈顶两个 int 型数值，大于0时，跳转                       |
| 164    | 0xA4   | if_icmple | 比较栈顶两个 int 型数值，小于等于0时，跳转                   |
| 165    | 0xA5   | if_acmpeq | 比较栈顶两个 引用 型数值，相等时跳转                         |
| 166    | 0xA6   | if_acmpne | 比较栈顶两个 引用 型数值，不相等时跳转                       |



## 4.8 Control 控制相关

  控制转移指令可以让 Java 虚拟机有条件或无条件地从指定的位置指令而不是控制转移指令的下一条指令继续执行程序，从概念模型上理解，可以认为控制转移指令就是在有条件或无条件地修改 PC 寄存器的值。

| 十进制 | 操作码 | 助记符       | 含义                                                         |
| ------ | ------ | ------------ | ------------------------------------------------------------ |
| 167    | 0xA7   | goto         | 无条件分支跳转                                               |
| 168    | 0xA8   | jsr          | 跳转至指定16 位offset（bit） 位置，并将jsr 下一条指令地址压入栈顶 |
| 169    | 0xA9   | ret          | 返回至局部变量指定的index 的指令位置（一般与jsr，jsr_w联合使用） |
| 170    | 0xAA   | tableswitch  | 用于switch 条件跳转，case 值连续（可变长度指令）             |
| 171    | 0xAB   | lookupswitch | 用于switch 条件跳转，case 值不连续（可变长度指令）           |
| 172    | 0xAC   | ireturn      | 结束方法，并返回一个int 类型数据                             |
| 173    | 0xAD   | lreturn      | 从当前方法返回 long                                          |
| 174    | 0xAE   | freturn      | 从当前方法返回 float                                         |
| 175    | 0xAF   | dreturn      | 从当前方法返回 double                                        |
| 176    | 0xB0   | areturn      | 从当前方法返回 对象引用                                      |
| 177    | 0xB1   | return       | 从当前方法返回 void                                          |



## 4.9 references 引用、方法、异常、同步相关

| 十进制 | 操作码 | 助记符          | 含义                                                         |
| ------ | ------ | --------------- | ------------------------------------------------------------ |
| 178    | 0xB2   | getstatic       | 获取指定类的静态域，并将其值压入栈顶                         |
| 179    | 0xB3   | putstatic       | 为类的静态域赋值                                             |
| 180    | 0xB4   | getfield        | 获取指定类的实例域（对象的字段值），并将其值压入栈顶         |
| 181    | 0xB5   | putfield        | 为指定的类的实例域赋值                                       |
| 182    | 0xB6   | invokevirtual   | 调用对象的实例方法，根据对象的实际类型进行分派（虚方法分派），是Java语言中最常见的方法分派方式。 |
| 183    | 0xB7   | invokespecial   | 调用一些需要特殊处理的实例方法，包括实例初始化方法（）、私有方法和父类方法。这三类方法的调用对象在编译时就可以确定。 |
| 184    | 0xB8   | invokestatic    | 调用静态方法                                                 |
| 185    | 0xB9   | invokeinterface | 调用接口方法调，它会在运行时搜索一个实现了这个接口方法的对象，找出适合的方法进行调用。 |
| 186    | 0xBA   | invokedynamic   | 调用动态链接方法（该指令是指令是Java SE 7 中新加入的）。用于在运行时动态解析出调用点限定符所引用的方法，并执行该方法，前面4条调用指令的分派逻辑都固化在Java虚拟机内部，而invokedynamic指令的分派逻辑是由用户所设定的引导方法决定的。 |
| 187    | 0xBB   | new             | 创建一个对象，并将其引用值压入栈顶                           |
| 188    | 0xBC   | newarray        | 创建一个指定原始类型（如int、float、char……）的数组，并将其引用值压入栈顶 |
| 189    | 0xBD   | anewarray       | 创建一个引用型（如类，接口，数组）的数组，并将其引用值压入栈顶 |
| 190    | 0xBE   | arraylength     | 获得数组的长度值并压入栈顶                                   |
| 191    | 0xBF   | athrow          | 将栈顶的异常直接抛出。Java程序中显式抛出异常的操作（throw语句）都由athrow指令来实现，并且，在Java虚拟机中，处理异常（catch语句）不是由字节码指令来实现的，而是采用异常表来完成的。 |
| 192    | 0xC0   | checkcast       | 检验类型转换，检验未通过将抛出ClassCastException             |
| 193    | 0xC1   | instanceof      | 检验对象是否是指定的类的实例，如果是将1 压入栈顶，否则将0 压入栈顶 |
| 194    | 0xC2   | monitorenter    | 获取对象的monitor，用于同步块或同步方法                      |
| 195    | 0xC3   | monitorexit     | 释放对象的monitor，用于同步块或同步方法                      |

  Java 虚拟机可以支持方法级的同步和方法内部一段指令序列的同步，这两种同步结构都是使用管程（Monitor）来支持的。
  **方法级的同步是隐式的，即无须通过字节码指令来控制，它实现在方法调用和返回操作之中。**虚拟机可以从方法常量池的方法表结构中的 ACC_SYNCHRONIZED 方法标志得知一个方法是否声明为同步方法。当方法调用时，调用指令将会检查方法的 ACC_SYNCHRONIZED 访问标志是否被设置，如果设置了，执行线程就要求先成功持有管程，然后才能执行方法，最后当方法完成（无论是正常完成还是非正常完成）时释放管程。在方法执行期间，执行线程持有了管程，其他任何线程都无法再获取到同一个管程。如果一个同步方法执行期间抛出了异常，并且在方法内部无法处理此异常，那么这个  同步方法所持有的管程将在异常抛到同步方法之外时自动释放。
  **同步一段指令集序列通常是由Java语言中的synchronized语句块来表示的，Java虚拟机的指令集中有monitorenter和monitorexit两条指令来支持synchronized关键字的语义**
  编译器必须确保无论方法通过何种方式完成，方法中调用过的每条monitorenter指令都必须执行其对应的monitorexit指令，而无论这个方法是正常结束还是异常结束。



## 4.10 Extended 扩展相关

| 十进制 | 操作码 | 助记符         | 含义                                                         |
| ------ | ------ | -------------- | ------------------------------------------------------------ |
| 196    | 0xC4   | wide           | 扩展访问局部变量表的索引宽度                                 |
| 197    | 0xC5   | multianewarray | 创建指定类型和指定维度的多维数组（执行该指令时，操作栈中必须包含各维度的长度值），并将其引用值压入栈顶 |
| 198    | 0xC6   | ifnull         | 为 null 时跳转                                               |
| 199    | 0xC7   | ifnonnull      | 非 null 时跳转                                               |
| 200    | 0xC8   | goto_w         | 无条件跳转（宽索引）                                         |
| 201    | 0xC9   | jsr_w          | 跳转指定32bit偏移位置，并将jsr_w下一条指令地址入栈           |



## 4.11 Reserved 保留指令

| 十进制 | 操作码 | 助记符     | 含义                           |
| ------ | ------ | ---------- | ------------------------------ |
| 202    | 0xCA   | breakpoint | 调试时的断点                   |
| 254    | 0xFE   | impdep1    | 用于在特定硬件中使用的语言后门 |
| 255    | 0xFF   | impdep2    | 用于在特定硬件中使用的语言后门 |

# 第五部分 参考和学习

    [JVM规范 Java SE8官方文档](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fdocs.oracle.com%2Fjavase%2Fspecs%2Fjvms%2Fse8%2Fhtml%2Findex.html)
    [JVM规范中《操作码助记符表》](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fdocs.oracle.com%2Fjavase%2Fspecs%2Fjvms%2Fse8%2Fhtml%2Fjvms-7.html)
    [JVM规范中《JVM指令集》介绍（包括操作码对应的操作数）](https://www.oschina.net/action/GoToLink?url=https%3A%2F%2Fdocs.oracle.com%2Fjavase%2Fspecs%2Fjvms%2Fse8%2Fhtml%2Fjvms-4.html%23jvms-4.10.1.9)
  《Java虚拟机规范》
  《深入理解Java虚拟机》
  《实战Java虚拟机》