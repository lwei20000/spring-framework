# Java虚拟机之字节码专题---常量池类型汇总

https://www.bilibili.com/video/BV1V541147WW

> 

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

