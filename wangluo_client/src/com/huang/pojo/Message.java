package com.huang.pojo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class Message implements Serializable {

    private String sender;

    private String getter;

    private byte[] content;

    private LocalDateTime sendTime;

    private String messageType;

    public Message () {
    }

    public Message(String sender, String getter, byte[] content, LocalDateTime sendTime, String messageType) {
        this.sender = sender;
        this.getter = getter;
        this.content = content;
        this.sendTime = sendTime;
        this.messageType = messageType;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getGetter() {
        return getter;
    }

    public void setGetter(String getter) {
        this.getter = getter;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
