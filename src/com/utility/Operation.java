package com.utility;

/**
 * Created by Anurita on 10/18/2016.
 */
public enum Operation {

    GET,
    PUT,
    DELETE,
    OTHER;

    public static Operation fromValue(String value) {
        try{
            return valueOf(value.toUpperCase()) ;
        } catch (Exception ex){
            return OTHER;
        }
    }
}
