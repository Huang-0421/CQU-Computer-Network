package com.huang.thread;

import com.huang.utils.MyException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class ManageClientsThread {

    private static ConcurrentHashMap<String, ServerConnectClientThread> hm = new ConcurrentHashMap();
    private static ConcurrentHashMap<String, List<String>> members = new ConcurrentHashMap();

    // 添加子线程
    public static void addServerConnectClientThread(String uniqueId, ServerConnectClientThread serverConnectClientThread) throws MyException {
        if(hm.containsKey(uniqueId)){
            throw new MyException("用户名重复");
        }
        System.out.println(serverConnectClientThread.getSocket().toString());
        hm.put(uniqueId, serverConnectClientThread);
    }

    // 获取子线程
    public static ServerConnectClientThread getServerConnectClientThread(String uniqueId){
        return hm.get(uniqueId);
    }

    //获取在线列表
    public static String getOnlineUser(String userId) {
        StringBuilder sb = new StringBuilder();
        for (String uniqueId : hm.keySet()) {
            if (uniqueId.equals(userId)) {
                continue;
            }
            sb.append(uniqueId).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    //添加群聊
    public static void addGroup(String groupName, List<String> members_in){
        members.put(groupName, members_in);
    }

    //获取该人参与的群聊
    public static String getGroup(String userName){
        StringBuilder sb = new StringBuilder();
        Set<String> keys = members.keySet();
        for(String key : keys){
            List<String> list = members.get(key);
            if(list.contains(userName)){
                sb.append(key).append(",");
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    //获取该群聊中的人
    public static List<String> getGroupMember(String groupName){
         return members.get(groupName);
    }
    // 删除指定在线用户
    public static void removeServerConnectClientThread(String uniqueId) {
        hm.remove(uniqueId);
    }
}

