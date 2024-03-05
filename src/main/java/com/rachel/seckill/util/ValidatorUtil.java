package com.rachel.seckill.util;

import org.thymeleaf.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
    private static Pattern mobilePattern = Pattern.compile("1\\d{10}");

    public static boolean isMobile(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        Matcher matcher = mobilePattern.matcher(str);
        return matcher.matches();
    }

//    public static void main(String[] args) {
//        String s1 = "17382123456";
//        String s2 = "1234567890";
//        System.out.println(isMobile(s1));
//        System.out.println(isMobile(s2));
//    }
}
