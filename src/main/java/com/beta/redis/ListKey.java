package com.beta.redis;

public class ListKey extends BasePrefix {
    public ListKey(String prefix) {
        super(prefix);
    }

    public static OrderKey getListKey= new OrderKey("listKey");
}
