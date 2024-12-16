package client;

import com.huang.pojo.Message;
import com.huang.pojo.MessageType;
import com.huang.pojo.User;
import utils.MyException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class UserClientService {

    private User user = new User();
    Properties properties = new Properties();
    private Socket socket;
    String ip = "";
    int port = 0;

    public void checkUser(String userId) throws MyException {
        try (InputStream input = new FileInputStream("src/config.properties")) {
            // 加载配置文件
            properties.load(input);

            // 获取配置文件中的 IP 和端口
            ip = properties.getProperty("server.ip", "127.0.0.1");  // 默认值为 127.0.0.1
            port = Integer.parseInt(properties.getProperty("server.port", "8888"));  // 默认端口为 8888
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        user.setUserId(userId);
        boolean b = false;
        try {
            //  与服务器建立连接
            socket = new Socket(InetAddress.getByName(ip), port);
            //  发送user对象
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(user);

        } catch (Exception e) {
            throw new MyException("建立连接失败");
        }
    }

    // 客户端请求获取在线用户列表
    public void getListRqt() throws MyException {
        //  发送一个 message 对象到服务端
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_GET_ONLINE_FRIEND);
        message.setSender(user.getUserId());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            throw new MyException("请求在线用户列表失败");
        }
    }
    // 客户端请求获取群聊列表
    public void getGroupRqt() throws MyException {
        //  发送一个 message 对象到服务端
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_GET_GROUP);
        message.setSender(user.getUserId());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            throw new MyException("请求群聊列表失败");
        }
    }
    public void addGroupRqt(String members) throws MyException {
        //  发送一个 message 对象到服务端
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_ADD_GROUP);
        message.setContent(members.getBytes());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            throw new MyException("请求创建群聊失败");
        }
    }

    // 获取用户列表
    public List<String> receiveOnlineUsers(Message messageResponse)  {
        String str = new String(messageResponse.getContent(), StandardCharsets.UTF_8);
        return Arrays.asList(str.split(","));
    }
    // 获取群聊列表
    public List<String> receiveOnlineGroups(Message messageResponse){
        String str = new String(messageResponse.getContent(), StandardCharsets.UTF_8);
        return Arrays.asList(str.split(","));
    }

    public Message receive() {
        try {
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    // 退出
    public void exit() throws MyException {
        //  发送一个 message 对象到服务端
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_CLIENT_EXIT);
        message.setSender(user.getUserId());
        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            oos.flush();
        } catch (Exception e) {
            throw new MyException("退出异常");
        }
    }

    public User getUser() {
        return user;
    }

    public Socket getSocket() {
        return socket;
    }

}

