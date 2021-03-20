# Java虚拟机之字节码专题---常量池类型汇总

> https://www.bilibili.com/video/BV1V541147WW
>
> http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.lcmp

> # 使用External Tools反编译字节码
>
> <img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210318134551424.png" alt="image-20210318134551424" style="zoom:33%;" />
>
> /Library/Java/JavaVirtualMachines/jdk1.8.0_271.jdk/Contents/Home

>  # vim 查看16进制字节码
>
> Classfile /Users/weiliang/IdeaProjects/spring-framework/my-test/build/classes/java/main/com/test_java/bytecode/HelloWorld.class
>
> vim HelloWorld.class
>
> :%!xxd -g 1



[toc]

**对应具体类型分析如下：** 

## 01、[0x07=07] CONSTANT_Class_info

> 功能： 表示类或接口
> 格式：
>
> CONSTANT_Class_info {
>     u1 tag;
>     u2 name_index;
> }
> 其中：tag 值为7，表示一个 CONSTANT_Class_info 类型
> name_index， 必须是对常量池的一个有效索引。 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构， 代表一个有效的类或接口二进制名称的内部形式。

## 02、[0x09=09] CONSTANT_Fieldref_info

> 功能： 表示字段字面量
> 格式：
>
> CONSTANT_Fieldref_info {
>     u1 tag;     //9
>     u2 class_index; //CONSTANT_Fieldref_info 结构的 class_index 项的类型既可以是类也可以是接口。
>     u2 name_and_type_index;
> }
> class_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Class_info 结构，表示一个类或接口，当前字段或方法是这个类或接口的成员。
> name_and_type_index: 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是 CONSTANT_NameAndType_info 结构，它表示当前字段或方法的名字和描述符。

## 03、[0x0a=10] CONSTANT_Methodref_info

> 功能： 表示方法符号引用
> 格式：
> CONSTANT_Methodref_info {
>     u1 tag;   //10
>     u2 class_index; //CONSTANT_Methodref_info 结构的 class_index 项的类型必须是类（不能是接口）。
>     u2 name_and_type_index;
> }

## 04、[0x0b=11] CONSTANT_InterfaceMethodref_info

> 功能： 表示接口方法符号引用
> 格式：
> CONSTANT_InterfaceMethodref_info {
>     u1 tag;    //11
>     u2 class_index; //CONSTANT_InterfaceMethodref_info 结构的class_index 项的类型必须是接口
> （不能是类）。 
>     u2 name_and_type_index;
> }

## 05、[0x08=08] CONSTANT_String_info

> 功能： 表示方法符号引用
> 格式：
> CONSTANT_String_info {
>     u1 tag;    //8
>     u2 string_index;
> }
> string_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，表示一组 Unicode 码点序列，这组 Unicode 码点序列最终会被初始化为一个 String 对象

## 06、[0x03=03] CONSTANT_Integer_info、CONSTANT_Float_info

> 功能： 表示表示 4 字节的整型、浮点型字面量
> 格式：
> CONSTANT_Integer_info {
>     u1 tag;  //3
>     u4 bytes; 
> }
> CONSTANT_Integer_info 结构的 bytes 项表示 int 常量的值，按照 Big-Endian的顺序存储。
>
> CONSTANT_Float_info {
>     u1 tag;  //4
>     u4 bytes;
> }
> CONSTANT_Float_info 结构的 bytes 项按照 IEEE 754 单精度浮点格式.表示 float 常量的值，按照 Big-Endian 的顺序存储。

## 07、[0x05=05] CONSTANT_Long_info 、CONSTANT_Double_info

> 功能：表示 8 字节（long 和 double）的数值常量
> 注1
>     在Class 文件的常量池中，所有的 8 字节的常量都占两个表成员（项）的空间。如果一个 CONSTANT_Long_info 或 CONSTANT_Double_info 结构的项在常量池中的索引为 n，则常量池中下一个有效的项的索引为 n+2， 此时常量池中索引为 n+1 的项有效但必须被认为不可用。
>
> 格式：
> CONSTANT_Long_info {
>     u1 tag;   //5
>     u4 high_bytes;
>     u4 low_bytes;
> }
> CONSTANT_Long_info 结构中的无符号的 high_bytes 和 low_bytes 项用于共同表
> 示 long 型常量，构造形式为((long) high_bytes << 32) + low_bytes，high_bytes 和 low_bytes 都按照 Big-Endian 顺序存储。
>
> CONSTANT_Double_info {
>     u1 tag;   //6
>     u4 high_bytes;
>     u4 low_bytes;
> }
> CONSTANT_Double_info 结构中的 high_bytes 和 low_bytes 共同按照 IEEE 754
> 双精度浮点格式 表示 double 常量的值。 high_bytes 和 low_bytes 都按照 Big-Endian 顺序存储。

## 08、[0x0c=12] CONSTANT_NameAndType_info

> 功能：表示字段或方法(描述其名称和类型)
> 格式：
> CONSTANT_NameAndType_info {
>     u1 tag;  //12
>     u2 name_index;
>     u2 descriptor_index;
> }
>
> name_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，这个结构要么表示特殊的方法名<init>，要么表示一个有效的字段或方法的非限定名（Unqualified Name）。
>
> descriptor_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，这个结构表示一个有效的字段描述符 或方法描述符。

## 09、[0x01=01] CONSTANT_Utf8_info

> 功能：表示字段或方法
> 格式：
> CONSTANT_Utf8_info {
>     u1 tag;  //1
>     u2 length;
>     u1 bytes[length];
> }
> length 项的值指明了 bytes[]数组的长度（注意，不能等同于当前结构所表示的String 对象的长度）， CONSTANT_Utf8_info 结构中的内容是以 length 属性确定长度而不是以 null 作为字符串的终结符。
> 如果 length 的值为 0x00, 则没有 bytes[length]。
>
> bytes[]是表示字符串值的byte数组， bytes[]数组中每个成员的byte值都不会是0，
> 也不在 0xf0 至 0xff 范围内。

## 10、[0x0f=15] CONSTANT_MethodHandle_info

> 功能：表示方法句柄
> 格式：
> CONSTANT_MethodHandle_info {
>     u1 tag;  //15
>     u1 reference_kind;
>     u2 reference_index;
> }
> reference_kind 项的值必须在 1 至 9 之间（包括 1 和 9），它决定了方法句柄的类型。方法句柄类型的值表示方法句柄的字节码行为。

## 11、[0x0a=16] CONSTANT_MethodType_info

> 功能：表示方法类型
> 格式：
> CONSTANT_MethodType_info {
>     u1 tag;   //16
>     u2 descriptor_index;
> }
> descriptor_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，表示方法的描述符。

## 12、[0x12=18] CONSTANT_InvokeDynamic_info

> 功能：表示方法类型
> 格式：
> CONSTANT_InvokeDynamic_info {
>     u1 tag;  //18
>     u2 bootstrap_method_attr_index;
>     u2 name_and_type_index;
> }
>
> bootstrap_method_attr_index  项的值必须是对当前 Class 文件中引导方法表的 bootstrap_methods[]数组的有效索引。
>
> name_and_typ



## 附件：例子 - HelloWorld

> 源代码

```java
package com.test_java.bytecode;
public class HelloWorld {
	public static void main(String[] args) {
		System.out.println("hello world");
	}
}
```

> javap反编译字节码结构

```
/Library/Java/JavaVirtualMachines/jdk1.8.0_271.jdk/Contents/Home/bin/javap -verbose com.test_java.bytecode.HelloWorld
Classfile /Users/weiliang/IdeaProjects/spring-framework/my-test/build/classes/java/main/com/test_java/bytecode/HelloWorld.class
  Last modified 2021-3-18; size 579 bytes
  MD5 checksum e3d1b2e061530ed81477b876d1066895
  Compiled from "HelloWorld.java"
public class com.test_java.bytecode.HelloWorld
  minor version: 0
  major version: 52
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #6.#20         // java/lang/Object."<init>":()V
   #2 = Fieldref           #21.#22        // java/lang/System.out:Ljava/io/PrintStream;
   #3 = String             #23            // hello world
   #4 = Methodref          #24.#25        // java/io/PrintStream.println:(Ljava/lang/String;)V
   #5 = Class              #26            // com/test_java/bytecode/HelloWorld
   #6 = Class              #27            // java/lang/Object
   #7 = Utf8               <init>
   #8 = Utf8               ()V
   #9 = Utf8               Code
  #10 = Utf8               LineNumberTable
  #11 = Utf8               LocalVariableTable
  #12 = Utf8               this
  #13 = Utf8               Lcom/test_java/bytecode/HelloWorld;
  #14 = Utf8               main
  #15 = Utf8               ([Ljava/lang/String;)V
  #16 = Utf8               args
  #17 = Utf8               [Ljava/lang/String;
  #18 = Utf8               SourceFile
  #19 = Utf8               HelloWorld.java
  #20 = NameAndType        #7:#8          // "<init>":()V
  #21 = Class              #28            // java/lang/System
  #22 = NameAndType        #29:#30        // out:Ljava/io/PrintStream;
  #23 = Utf8               hello world
  #24 = Class              #31            // java/io/PrintStream
  #25 = NameAndType        #32:#33        // println:(Ljava/lang/String;)V
  #26 = Utf8               com/test_java/bytecode/HelloWorld
  #27 = Utf8               java/lang/Object
  #28 = Utf8               java/lang/System
  #29 = Utf8               out
  #30 = Utf8               Ljava/io/PrintStream;
  #31 = Utf8               java/io/PrintStream
  #32 = Utf8               println
  #33 = Utf8               (Ljava/lang/String;)V
{
  public com.test_java.bytecode.HelloWorld();
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:
        line 3: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       5     0  this   Lcom/test_java/bytecode/HelloWorld;

  public static void main(java.lang.String[]);
    descriptor: ([Ljava/lang/String;)V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=1, args_size=1
         0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
         3: ldc           #3                  // String hello world
         5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
         8: return
      LineNumberTable:
        line 5: 0
        line 6: 8
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       9     0  args   [Ljava/lang/String;
}
SourceFile: "HelloWorld.java"

Process finished with exit code 0
                           
```

> 二进制字节码结构

## 例子：运行时内存

https://www.bilibili.com/video/BV13Z4y1s7TH

https://www.bilibili.com/video/BV1w54y1X7Sq

### 1）原始java代码

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318213126963.png" alt="image-20210318213126963" style="zoom:50%;" />

> 局部变量a=10
>
> 局部变量b=shot最大值+1 // 注意：存放位置

### 2）编译后的字节码文件

```
略
```



### 3）常量池载入运行时常量池

> 1. 方法的字节码是存放在**方法区**
> 2. 运行时常量池是属于方法区的一个部分
> 3. 比较小的数字（10）是与字节码存放在一起的。一旦超过了short.Max_Value=32767的范围就是放在运行时常量池中。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318213309863.png" alt="image-20210318213309863" style="zoom:50%;" />

### 4）方法字节码载入方法区

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210318213733806.png" alt="image-20210318213733806" style="zoom:50%;" />

### 5）main线程开始运行，分配栈帧内存

> 1. 程序运行之前在栈中分配局部变量表+操作数栈两部分空间。这个是当前线程栈帧的内存大小。
> 2. 执行引擎区读取方法区中的一个个指令开始执行。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318212935814.png" alt="image-20210318212935814" style="zoom:50%;" />

> 1. bipush 10  #把10压入栈中（10在short范围内，所以不用从常量池中取）
> 2. istore 1    #栈顶数据弹出，存入到局部变量表的1号槽位。（0号是args占用了）

### <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318214217338.png" alt="image-20210318214217338" style="zoom:50%;" />



> 

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318214404934.png" alt="image-20210318214404934" style="zoom:50%;" />

> 

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318215644139.png" alt="image-20210318215644139" style="zoom:50%;" />

> 计算int c = a + b;   不能在局部变量表中进行，而是要在操作数栈上进行。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318214532861.png" alt="image-20210318214532861" style="zoom:50%;" />

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210318214714146.png" alt="image-20210318214714146" style="zoom:50%;" />

<img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210318214857494.png" alt="image-20210318214857494" style="zoom:50%;" />

 <img src="/Users/weiliang/Library/Application Support/typora-user-images/image-20210318214923177.png" alt="image-20210318214923177" style="zoom:50%;" />

> 下面是执行System.out.println(c);
>
> 1. get static #4   // 先到运行时常量池中找到成员变量的引用，他是一个System.out，然后到堆中找到它的对象。把对象的引用放入到操作数栈。
> 2. iload_3    // 打印函数需要的参数c

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318215238746.png" alt="image-20210318215238746" style="zoom:50%;" />

 <img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318215335654.png" alt="image-20210318215335654" style="zoom:50%;" />

> invokevirtual #5 
>
> 1. **main栈**的栈顶元素32768，传递给**println栈**的新的栈，放到栈底。
> 2. println方法执行完成之后，**println栈**弹出，然后把**main栈**中的的两个元素都清除掉。这时候system.out.println(c)就执行完毕。
> 3. **main栈**也执行完毕，栈帧被清除。

<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318215357659.png" alt="image-20210318215357659" style="zoom:50%;" />



<img src="https://cdn.jsdelivr.net/gh/lwei20000/pic/image-20210318220640868.png" alt="image-20210318220640868" style="zoom:50%;" />

#完毕

## 



























