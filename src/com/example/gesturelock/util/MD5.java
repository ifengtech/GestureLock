package com.example.gesturelock.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
	public static String md5(String input){
        String result = input;
        if(input != null) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            try {
				md.update(input.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            BigInteger hash = new BigInteger(1, md.digest());
            result = hash.toString(16);
            if ((result.length() % 2) != 0) {
                result = "0" + result;
            } 
            }catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    public static boolean compareMD5String(String ss1, String ss2) {
        if (ss1 == null || ss2 == null){
            return false;
        } else {
            if (ss1.length() != ss2.length()) {
                return false;
            } else {
                for (int i=0; i<ss1.length(); i++) {
                    if (ss1.charAt(i) != ss2.charAt(i)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
