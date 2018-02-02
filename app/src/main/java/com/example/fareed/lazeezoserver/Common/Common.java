package com.example.fareed.lazeezoserver.Common;

import com.example.fareed.lazeezoserver.Model.Request;
import com.example.fareed.lazeezoserver.Model.User;


public class Common {
    public  static User currentUser;
    public  static Request currentRequest;

    public static String MODIFY="Update";
    public static String REMOVE="Remove";

    public static String convertCodeToStatus(String code){
        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "On My way";
        else
            return "Shipped";
    }
}
