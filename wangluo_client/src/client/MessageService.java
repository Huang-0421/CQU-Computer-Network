package client;

import com.huang.pojo.Message;
import com.huang.pojo.MessageType;
import utils.MyException;

import java.io.*;
import java.net.Socket;

/**
 * @description: 消息服务
 * @author: Snow
 * @date: 2024/6/27
 * **************************************************
 * 修改记录(时间--修改人--修改说明):
 */
public class MessageService {
    private UserClientService userClientService;

    public MessageService(UserClientService userClientService) {
        this.userClientService = userClientService;
    }
    /** 发消息给某人 */
    public void sendMessageToOne(String content, String getter, String sender) {
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_COMMON_MSG);
        message.setSender(sender);
        message.setGetter(getter);
        message.setContent(content.getBytes());
        try {
            //  获取当前线程的 socket
            Socket socket = userClientService.getSocket();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendFile(String getter, String sender, File file){
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_IS_FILE);
        message.setSender(sender);
        message.setGetter(getter);
        message.setContent(convertFileToByteArray(file));
        try {
            //  获取当前线程的 socket
            Socket socket = userClientService.getSocket();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
            System.out.println("已发送文件");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public byte[] convertFileToByteArray(File file) {
        byte[] fileBytes = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            // 获取文件的大小
            long fileLength = file.length();

            // 创建一个字节数组来存储文件内容
            fileBytes = new byte[(int) fileLength];

            // 读取文件内容到字节数组中
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }
    /** 群发消息 */
    public void sendMessageToAll(String content, String sender, String groupName) {
        Message message = new Message();
        message.setMessageType(MessageType.MESSAGE_GROUP_MSG);
        message.setSender(sender);
        message.setGetter(groupName);
        message.setContent(content.getBytes());
        try {
            //  获取当前线程的 socket
            Socket socket = userClientService.getSocket();
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

