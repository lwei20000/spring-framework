package com.test_officialweb.t15_Formatter;

import com.test_officialweb.t14_BeanWapper.bean.People;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.datetime.DateFormatter;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class IocTest {

	public static void main(String[] args) throws Exception {
		DateFormatter dateFormatter = new DateFormatter();
		dateFormatter.setIso(DateTimeFormat.ISO.DATE);
		System.out.println(dateFormatter.print(new Date(), Locale.CHINA));
		System.out.println(dateFormatter.parse("2020-03-26", Locale.CHINA));
		// 程序打印：
		// 2020-03-26
		// Thu Mar 26 08:00:00 CST 2020
	}
}