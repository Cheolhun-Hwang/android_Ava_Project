package com.hch.hooney.avaappproject.SupportTool;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AvaCode {
    public static String createCode(){
        String currentDateandTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return currentDateandTime+"-"+randomCode(10);
    }

    public static String randomCode(int length){
        String code = "";
        for(int num = 0; num < length ; num++){
            int random = (int)(Math.random()*9);
            code+=random;
        }
        return code;
    }
}
