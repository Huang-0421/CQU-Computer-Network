package com.huang.pojo;

import java.io.Serializable;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class User implements Serializable {

    private String userId;

    public User() {
    }

    public User(String userId, String password) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}