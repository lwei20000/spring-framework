package com.test30_aop.d1_reflect;

import java.lang.annotation.ElementType;

/**
 * 测试反射Class.forname()
 *
 * @Auther: weiliang
 * @Date: 2024/11/5 17:08
 * @Description:
 */
// 下面这个注解是：gradle编译项目时还对于泛型有校验，但是我本身就是不需要校验的。
// 可以在类上加个@SuppressWarnings({"rawtypes", "unchecked"})
@SuppressWarnings({"rawtypes", "unchecked"})
public class Test01 {
	public static void main(String[] args) throws ClassNotFoundException {

		User user = new User();

		// 问题01：一个类在内存中只有一个Class对象
		Class c1 = Class.forName("com.test30_aop.d1_reflect.User");
		Class c2 = Class.forName("com.test30_aop.d1_reflect.User");
		Class c3 = Class.forName("com.test30_aop.d1_reflect.User");
		System.out.println(c1.hashCode());
		System.out.println(c2.hashCode());
		System.out.println(c3.hashCode());

		// 问题02：几种获取Class对象的方法：
		Class c4 = user.getClass();  // 方式1：通过实例获取Class对象
		Class c5 = Class.forName("com.test30_aop.d1_reflect.User");  // 方式2：通过Class.forName("")
		Class c6 = User.class; // 方式3：通过User.class获取
		Class c7 = Integer.class; // 方式3：基本内置类型的包装类都有一个TYPE属性
		Class c8 = Integer.TYPE; // 方式4：基本内置类型的包装类都有一个TYPE属性
		System.out.println(c7);
		System.out.println(c8);

		// 问题03：所有类型的class对象
		Class c01 = Object.class; // 类
		Class c02 = Comparable.class; // 接口
		Class c03 = String[].class; // 一纬数组
		Class c04 = int[][].class; // 二维数组
		Class c05 = Override.class; // 注解
		Class c06 = ElementType.class; // 枚举
		Class c07 = Integer.class; // 基本数据类型的包装类型
		Class c08 = void.class; // void
		Class c09 = Class.class; // Class类本身
		System.out.println(c01);
		System.out.println(c02);
		System.out.println(c03);
		System.out.println(c04);
		System.out.println(c05);
		System.out.println(c06);
		System.out.println(c07);
		System.out.println(c08);
		System.out.println(c09);







	}
}

class User {
	private String name;
}
