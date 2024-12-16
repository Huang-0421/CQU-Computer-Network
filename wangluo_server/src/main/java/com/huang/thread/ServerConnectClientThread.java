package com.huang.thread;

import com.huang.pojo.Message;
import com.huang.pojo.MessageType;
import com.huang.utils.MyException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class ServerConnectClientThread extends Thread {

    private Socket socket;
    private String userId;
    private String onlineUser;
    private ConcurrentHashMap<String, List<String>> chatRecords; // 存储与其他客户端的聊天记录

    public ServerConnectClientThread(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
        this.chatRecords = new ConcurrentHashMap<>(); // 初始化聊天记录容器
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUserId() {
        return userId;
    }

    public void run() {
        boolean flag = true;
        while (flag) {
            try {
                //获取请求
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message messageRequest = (Message) ois.readObject();
                Message messageResponse = new Message();

                switch (messageRequest.getMessageType()) {
                    case MessageType.MESSAGE_GET_ONLINE_FRIEND:
                        onlineUser = ManageClientsThread.getOnlineUser(userId);
                        //生成响应消息
                        messageResponse.setMessageType(MessageType.MESSAGE_GET_ONLINE_FRIEND);
                        messageResponse.setContent(onlineUser.getBytes());
                        messageResponse.setGetter(messageRequest.getSender());
                        //  返回给客户端
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(messageResponse);
                        break;
                    case MessageType.MESSAGE_IS_FILE:
                    case MessageType.MESSAGE_COMMON_MSG:
                        //此时是“我”给对面发消息
                        if (messageRequest.getMessageType().equals(MessageType.MESSAGE_COMMON_MSG)) {
                            messageResponse.setMessageType(MessageType.MESSAGE_COMMON_MSG);
                            //先获取我与对面的聊天记录
                            List<String> historyList = chatRecords.computeIfAbsent(messageRequest.getGetter(), k -> new ArrayList<>());
                            //在“我”中新增我的聊天记录
                            String string = new String(messageRequest.getContent());
                            historyList.add("我: " + string);
                        } else {
                            messageResponse.setMessageType(MessageType.MESSAGE_IS_FILE);
                        }
                        //  获取 收取人的 socket
                        ServerConnectClientThread serverConnectClientThread = ManageClientsThread.getServerConnectClientThread(messageRequest.getGetter());
                        messageResponse.setSender(messageRequest.getSender());
                        messageResponse.setGetter(messageRequest.getGetter());
                        messageResponse.setContent(messageRequest.getContent());
                        Socket socket1 = serverConnectClientThread.getSocket();
                        new ObjectOutputStream(socket1.getOutputStream()).writeObject(messageResponse);
                        break;
                    //在“我”中新增对方的聊天记录
                    case MessageType.MESSAGE_ADD_HISTOAY:
                        //先获取我与对面的聊天记录
                        List<String> historyList = chatRecords.computeIfAbsent(messageRequest.getSender(), k -> new ArrayList<>());
                        //在“我”中新增对方的聊天记录
                        String string = new String(messageRequest.getContent());
                        historyList.add(messageRequest.getSender() + ": " + string);
                        break;
                    case MessageType.MESSAGE_GET_HISTOAY:
                        //生成响应消息
                        messageResponse.setMessageType(MessageType.MESSAGE_GET_HISTOAY);
                        //找到我与对方的聊天记录
                        List<String> historyList1 = chatRecords.computeIfAbsent(messageRequest.getGetter(), k -> new ArrayList<>());
                        StringBuilder string1 = new StringBuilder();
                        //遍历这个容器
                        for (String item : historyList1) {
                            string1.append(item).append("\n");
                        }
                        messageResponse.setContent(string1.toString().getBytes());
                        messageResponse.setGetter(messageRequest.getSender());
                        //  返回给客户端
                        new ObjectOutputStream(socket.getOutputStream()).writeObject(messageResponse);
                        break;
                    //申请创建群聊
                    case MessageType.MESSAGE_ADD_GROUP:
                        String members = new String(messageRequest.getContent());
                        String[] membersArray = members.split(",");
                        String groupName = membersArray[0];
                        List<String> memberList = new ArrayList<>();
                        for (int i = 1; i < membersArray.length; i++) {
                            memberList.add(membersArray[i].trim());
                        }
                        ManageClientsThread.addGroup(groupName, memberList);
                        break;
                    //获取群聊列表
                    case MessageType.MESSAGE_GET_GROUP:
                        String userName = messageRequest.getSender();
                        String groupList = ManageClientsThread.getGroup(userName);
                        //将这个列表返还给客户端
                        //生成响应消息
                        messageResponse.setMessageType(MessageType.MESSAGE_GET_GROUP);
                        messageResponse.setContent(groupList.getBytes());
                        messageResponse.setGetter(userName);
                        //  返回给客户端
                        new ObjectOutputStream(socket.getOutputStream()).writeObject(messageResponse);
                        break;
                    //群消息
                    case MessageType.MESSAGE_GROUP_MSG:
                        //  获取群中人员列表
                        List<String> memberListSend = ManageClientsThread.getGroupMember(messageRequest.getGetter());
                        // 根据列表找到对应人员的线程，并分别发送消息
                        for (String item : memberListSend) {
                            if(item.equals(messageRequest.getSender())) {
                                continue;
                            }
                            Socket socketnew = ManageClientsThread.getServerConnectClientThread(item).getSocket();
                            messageResponse.setMessageType(MessageType.MESSAGE_GROUP_MSG);
                            messageResponse.setSender(messageRequest.getSender());
                            messageResponse.setGetter(item);
                            String content_origin = new String(messageRequest.getContent());
                            String content_add = messageRequest.getGetter() + ":" + content_origin;
                            messageResponse.setContent(content_add.getBytes());
                            new ObjectOutputStream(socketnew.getOutputStream()).writeObject(messageResponse);
                        }
                        break;
                    case MessageType.MESSAGE_CLIENT_EXIT:
                        //  将该线程从集合中移除
                        ManageClientsThread.removeServerConnectClientThread(messageRequest.getSender());
                        //  关闭socket
                        socket.close();
                        System.out.println("客户端[" + userId + "]退出了");
                        flag = false;
                        break;

                }
//                if (MessageType.MESSAGE_GET_ONLINE_FRIEND.equals(messageRequest.getMessageType())) {
//
//                } else if (MessageType.MESSAGE_CLIENT_EXIT.equals(messageRequest.getMessageType())) {
//                    //  将该线程从集合中移除
//                    ManageClientsThread.removeServerConnectClientThread(messageRequest.getSender());
//                    //  关闭socket
//                    socket.close();
//                    System.out.println("客户端[" + userId + "]退出了");
//                    break;
//                } else if (MessageType.MESSAGE_TO_ALL_MSG.equals(messageRequest.getMessageType())) {
//                    //  获取在线用户
//                    String onlineUser = ManageClientsThread.getOnlineUser();
//                    String[] onlineUsers = onlineUser.split(" ");
//                    if (onlineUsers == null || onlineUsers.length == 0) {
//                        System.out.println("没有在线用户");
//                    }
//                    for (int i = 0; i < onlineUsers.length; i++) {
//                        if (!onlineUsers[i].equals(messageRequest.getSender())) {
//                            //  获取 收取人的 socket
//                            ServerConnectClientThread serverConnectClientThread = ManageClientsThread.getServerConnectClientThread(onlineUsers[i]);
//                            Socket socket = serverConnectClientThread.getSocket();
//                            new ObjectOutputStream(socket.getOutputStream()).writeObject(messageRequest);
//                        }
//                    }
//                } else {
//                    System.out.println("先不处理");
//                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.print("这里出错");
            }
        }
    }
}