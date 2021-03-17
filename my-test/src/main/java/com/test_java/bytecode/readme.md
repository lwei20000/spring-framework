# Java虚拟机之字节码专题---常量池类型汇总

常量类型	值	描述
CONSTANT_Class_info	7	表示类或接口
CONSTANT_Fieldref_info	9	字段信息表
CONSTANT_Methodref_info	10	方法
CONSTANT_InterfaceMethodref_info	11	接口方法
CONSTANT_String_info	8	java.lang.String 类型的常量对象
CONSTANT_Integer_info	3	整型字面量
CONSTANT_Float_info	4	浮点型字面量
CONSTANT_Long_info	5	长整型字面量
CONSTANT_Double_info	6	双精度型字面量
CONSTANT_NameAndType_info	12	名称和类型表
CONSTANT_Utf8_info	1	utf-8 编码的字符串
CONSTANT_MethodHandle_info	15	方法句柄表
CONSTANT_MethodType_info	16	方法类型表
CONSTANT_InvokeDynamic_info	18	动态方法调用点

**对应具体类型分析如下：** 

## 1、CONSTANT_Class_info

> 功能： 表示类或接口
> 格式：
>
> CONSTANT_Class_info {
>     u1 tag;
>     u2 name_index;
> }
> 其中：tag 值为7，表示一个 CONSTANT_Class_info 类型
> name_index， 必须是对常量池的一个有效索引。 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构， 代表一个有效的类或接口二进制名称的内部形式。

## 2、CONSTANT_Fieldref_info

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
> ————————————————
> 版权声明：本文为CSDN博主「极客消息」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
> 原文链接：https://blog.csdn.net/yy_xiaozhu/article/details/81751112

## 3、CONSTANT_Methodref_info

> 功能： 表示方法符号引用
> 格式：
> CONSTANT_Methodref_info {
>     u1 tag;   //10
>     u2 class_index; //CONSTANT_Methodref_info 结构的 class_index 项的类型必须是类（不能是接口）。
>     u2 name_and_type_index;
> }

## 4、CONSTANT_InterfaceMethodref_info

> 功能： 表示接口方法符号引用
> 格式：
> CONSTANT_InterfaceMethodref_info {
>     u1 tag;    //11
>     u2 class_index; //CONSTANT_InterfaceMethodref_info 结构的class_index 项的类型必须是接口
> （不能是类）。 
>     u2 name_and_type_index;
> }

## 5、CONSTANT_String_info

> 功能： 表示方法符号引用
> 格式：
> CONSTANT_String_info {
>     u1 tag;    //8
>     u2 string_index;
> }
> string_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，表示一组 Unicode 码点序列，这组 Unicode 码点序列最终会被初始化为一个 String 对象

## 6、CONSTANT_Integer_info、CONSTANT_Float_info

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

## 7、CONSTANT_Long_info 、CONSTANT_Double_info

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

## 8、CONSTANT_NameAndType_info

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

## 9、CONSTANT_Utf8_info

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

## 10、CONSTANT_MethodHandle_info

> 功能：表示方法句柄
> 格式：
> CONSTANT_MethodHandle_info {
>     u1 tag;  //15
>     u1 reference_kind;
>     u2 reference_index;
> }
> reference_kind 项的值必须在 1 至 9 之间（包括 1 和 9），它决定了方法句柄的类型。方法句柄类型的值表示方法句柄的字节码行为。

## 11、CONSTANT_MethodType_info

> 功能：表示方法类型
> 格式：
> CONSTANT_MethodType_info {
>     u1 tag;   //16
>     u2 descriptor_index;
> }
> descriptor_index 项的值必须是对常量池的有效索引， 常量池在该索引处的项必须是CONSTANT_Utf8_info 结构，表示方法的描述符。

## 12、CONSTANT_InvokeDynamic_info

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