package com.huang;

import com.huang.pojo.User;
import com.huang.thread.ManageClientsThread;
import com.huang.thread.ServerConnectClientThread;
import com.huang.utils.MyException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        new Main();
    }

    private ServerSocket serverSocket = null;

    public Main() {
        try {
            // 监听 8888 端口
            serverSocket = new ServerSocket(8888);
            while (true) {
                try {
                    // 有客户端建立连接时放行
                    Socket socket = serverSocket.accept();
                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                    // 获取客户端传来的 user 验证其身份
                    User user = (User) ois.readObject();

                    // 创建一个线程和客户端保持通信
                    System.out.println("客户端[" + user.getUserId() + "]加入");
                    //并创建一个聊天记录容器
                    ServerConnectClientThread serverThread = new ServerConnectClientThread(socket, user.getUserId());

                    // 把该线程对象放入客户端集合中,进行管理
                    ManageClientsThread.addServerConnectClientThread(user.getUserId(), serverThread);

                    // 启动线程
                    serverThread.start();

                } catch (IOException e) {
                    // 处理接收客户端连接异常（如端口被占用等），记录日志并继续等待下一个客户端连接
                    System.err.println("连接错误: " + e.getMessage());
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    // 处理客户端传送的数据格式错误
                    System.err.println("接收的数据格式错误: " + e.getMessage());
                    e.printStackTrace();
                } catch (MyException e) {
                    // 处理重复登录异常
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // 服务器启动时的异常（例如端口无法绑定）
            System.err.println("服务器启动失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("关闭服务器时发生错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}