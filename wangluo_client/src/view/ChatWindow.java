package view;

import client.MessageService;
import client.UserClientService;
import com.huang.pojo.Message;
import com.huang.pojo.MessageType;
import com.huang.pojo.User;
import utils.MyException;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class ChatWindow {
    private JFrame frame;
    private JList<String> userList;
    private JList<String> groupList;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton sendButton1;
    private DefaultListModel<String> listModel;
    private DefaultListModel<String> groupListModel;
    private Map<String, JTextArea> chatWindows;
    private String currentUser = null;
    private String currentGroup = null;
    private List<String> onlineUsers;
    private List<String> onlineGroups;

    /**
     * 客户端用户服务
     */
    private UserClientService userClientService = new UserClientService();
    private MessageService messageService;

    public ChatWindow(String userName) throws MyException {
        // 先建立连接，将连接放入线程池
        userClientService.checkUser(userName);
        messageService = new MessageService(userClientService);
        /*--------下面进入聊天界面------------*/
        // 绘图
        frame = new JFrame("PartyChat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);
        frame.setLocation(100, 100);
        frame.setLayout(new BorderLayout());
        // 添加窗口关闭监听器
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 在此处处理关闭事件
                System.out.println("Window is closing!");
                // 向服务器发送关闭请求
                try {
                    userClientService.exit();
                } catch (MyException ex) {
                    throw new RuntimeException(ex);
                }
                System.exit(0); // 确保程序退出
            }
        });
        JPanel leftbig = new JPanel();
        leftbig.setPreferredSize(new Dimension(150, frame.getHeight()));
        leftbig.setLayout(new BorderLayout());

        // 左侧面板：在线用户
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        // 创建“发起群聊”按钮并添加监听器
        JButton startGroupChatButton = new JButton("发起群聊");
        startGroupChatButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 处理群聊发起事件
                startGroupChat();
            }
        });

        // 将发起群聊按钮添加到左侧面板的上部
        leftPanel.add(startGroupChatButton, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 点击用户时，切换聊天窗口
        userList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedUser = userList.getSelectedValue();
                if (selectedUser != null) {
                    try {
                        switchChatWindow(selectedUser);
                    } catch (MyException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        JScrollPane userScrollPane = new JScrollPane(userList);
        leftPanel.add(userScrollPane, BorderLayout.CENTER);

        JPanel leftGroup = new JPanel();
        leftGroup.setLayout(new BorderLayout());

        // 群聊列表
        groupListModel = new DefaultListModel<>();
        groupList = new JList<>(groupListModel);
        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // 点击群聊时，切换聊天窗口
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedGroup = groupList.getSelectedValue();
                if (selectedGroup != null) {
                    try {
                        switchGroupWindow(selectedGroup);
                    } catch (MyException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        JScrollPane groupScrollPane = new JScrollPane(groupList);
        leftGroup.add(groupScrollPane, BorderLayout.CENTER);

        leftbig.add(leftPanel, BorderLayout.NORTH);
        leftbig.add(leftGroup, BorderLayout.SOUTH);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftPanel, leftGroup);
        splitPane.setResizeWeight(0.5);  // 让上下部分平均分配空间
        splitPane.setDividerLocation(frame.getHeight() / 2);  // 初始分隔位置
        leftbig.add(splitPane, BorderLayout.CENTER);
        frame.add(leftbig, BorderLayout.WEST);

        // 右侧面板：聊天窗口
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        rightPanel.add(chatScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        inputField = new JTextField();

        // 创建一个容器来放置按钮
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // 按钮右对齐
        sendButton = new JButton("发送");
        sendButton1 = new JButton("上传文件");
        buttonPanel.add(sendButton);
        buttonPanel.add(sendButton1);
        // 按钮点击事件
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        // 监听回车键事件
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();  // 按回车键时发送消息
                }
            }
        });
        // 上传文件按钮事件
        sendButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadFile();
            }
        });
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        frame.add(rightPanel, BorderLayout.CENTER);

        // 初始化聊天窗口
        chatWindows = new HashMap<>();
        //currentUser = null;
        switchChatWindow(null);
        // 启动一个线程监听服务器发来消息
        startAllocate();
        // 启动一个线程请求在线用户列表和群聊列表
        startOnlineUserUpdater();
        // 显示窗口
        frame.setVisible(true);
    }

    // 群聊发起操作
    private void startGroupChat() {
        JDialog groupChatDialog = new JDialog(frame, "发起群聊", true); // true表示模态对话框
        groupChatDialog.setLayout(new BorderLayout());
        groupChatDialog.setSize(400, 300);
        groupChatDialog.setLocationRelativeTo(frame); // 使对话框居中显示
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        // 在线人员列表

        String[] onlineUsers1 = onlineUsers.toArray(new String[onlineUsers.size()]);
        JList<String> userList = new JList<>(onlineUsers1);
        userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // 允许多选
        JScrollPane userListScrollPane = new JScrollPane(userList);
        topPanel.add(new JLabel("选择群聊成员："), BorderLayout.NORTH);
        topPanel.add(userListScrollPane, BorderLayout.CENTER);

        // 创建群聊名称输入框
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JTextField groupNameField = new JTextField();
        bottomPanel.add(new JLabel("输入群聊名称："), BorderLayout.NORTH);
        bottomPanel.add(groupNameField, BorderLayout.CENTER);

        // 创建按钮面板，包含确认和取消按钮
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确认");
        JButton cancelButton = new JButton("取消");

        // 确认按钮的处理
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String groupName = groupNameField.getText().trim();
                if (groupName.isEmpty()) {
                    JOptionPane.showMessageDialog(groupChatDialog, "请输入群聊名称！", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 获取选中的在线人员
                List<String> selectedUsers = userList.getSelectedValuesList();
                if (selectedUsers.isEmpty()) {
                    JOptionPane.showMessageDialog(groupChatDialog, "请选择至少一位成员！", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // 创建群聊,把群聊名放开头
                StringBuilder members = new StringBuilder();
                members.append(groupName).append(", ");
                for (String user : selectedUsers) {
                    members.append(user).append(", ");
                }
                User me = userClientService.getUser();
                members.append(me.getUserId());
                try {
                    userClientService.addGroupRqt(members.toString());
                } catch (MyException ex) {
                    throw new RuntimeException(ex);
                }
                // 关闭对话框
                groupChatDialog.dispose();
            }
        });

        // 取消按钮的处理
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                groupChatDialog.dispose();
            }
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        // 创建一个新的面板，用来容纳bottomPanel和buttonPanel
        JPanel bottomContainerPanel = new JPanel();
        bottomContainerPanel.setLayout(new BoxLayout(bottomContainerPanel, BoxLayout.Y_AXIS)); // 纵向排列
        bottomContainerPanel.add(bottomPanel);  // 添加群聊名称输入框面板
        bottomContainerPanel.add(buttonPanel);  // 添加确认/取消按钮面板

        // 将新的容器面板添加到对话框的南部
        groupChatDialog.add(topPanel, BorderLayout.CENTER);
        groupChatDialog.add(bottomContainerPanel, BorderLayout.SOUTH); // 只有这个面板被添加到南部

        // 添加各个面板到对话框中
        groupChatDialog.add(topPanel, BorderLayout.CENTER);
        groupChatDialog.add(bottomContainerPanel, BorderLayout.SOUTH);

        // 显示对话框
        groupChatDialog.setVisible(true);
    }
    // 切换聊天窗口
    private void switchChatWindow(String selectedUser) throws MyException {
        currentUser = selectedUser;
        currentGroup = null;
        if (currentUser == null) {
            return;
        }
        // 如果用户没有聊天窗口，则创建一个新的
        if (!chatWindows.containsKey(currentUser)) {
            JTextArea newChatArea = new JTextArea();
            newChatArea.setText("这是["+ currentUser + "]的聊天窗口\n");  // 设置初始聊天内容
            newChatArea.setEditable(true);
            chatWindows.put(currentUser, newChatArea);  // 将新窗口添加到 chatWindows
        }
        // 获取当前用户的聊天窗口
        chatArea.setText(chatWindows.get(currentUser).getText());
        // 请求聊天记录
        //userClientService.getHistoryRqt(currentUser);
    }
    // 切换群聊窗口
    private void switchGroupWindow(String selectedGroup) throws MyException {
        currentGroup = selectedGroup;
        currentUser = null;
        if (currentGroup == null) {
            return;
        }
        if (!chatWindows.containsKey(currentGroup)) {
            JTextArea newChatArea = new JTextArea();
            newChatArea.setText("这是["+ currentGroup + "]的聊天窗口\n");  // 设置初始聊天内容
            newChatArea.setEditable(true);
            chatWindows.put(currentGroup, newChatArea);  // 将新窗口添加到 chatWindows
        }
        // 获取当前群聊的聊天窗口
        chatArea.setText(chatWindows.get(currentGroup).getText());
    }
    // 发送消息
    private void sendMessage() {
        String message = inputField.getText();
        if(currentUser == null && currentGroup == null){
            return;
        }
        if (!message.trim().isEmpty()) {
            String chatHistory = chatArea.getText();
            chatHistory += "我: " + message + "\n";

            chatArea.setText(chatHistory);
            inputField.setText("");
            //发送消息到服务器
            //String content, String getter, String sender
            if(currentUser != null){
                chatWindows.get(currentUser).setText(chatHistory);
                messageService.sendMessageToOne(message, currentUser, userClientService.getUser().getUserId());
            }else if(currentGroup != null){
                chatWindows.get(currentGroup).setText(chatHistory);
                messageService.sendMessageToAll(message, userClientService.getUser().getUserId(), currentGroup);
            }
        }
    }
    // 上传文件
    private void uploadFile() {
        if(currentGroup != null){
            JOptionPane.showMessageDialog(frame, "群聊中禁止上传文件！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 这里可以打开文件对话框，选择文件，发送文件内容
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            // 获取选择的文件
            File selectedFile = fileChooser.getSelectedFile();
            messageService.sendFile(currentUser, userClientService.getUser().getUserId(), selectedFile);
        }
    }
    // 启动线程持续接收在线用户列表并更新UI
    private void startOnlineUserUpdater() {
        new Thread(() -> {
            while (true) {
                try {
                    //不断发出用户列表请求
                    userClientService.getListRqt();
                    Thread.sleep(2000);
                    userClientService.getGroupRqt();
                } catch (MyException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    // 启动线程持续接收群聊列表并更新UI
    private void startGroupUpdater() {
        new Thread(() -> {
            while (true) {
                try {
                    //不断发出群聊列表请求
                    userClientService.getGroupRqt();
                    Thread.sleep(20000);
                } catch (MyException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    // 更新在线用户列表
    private void updateOnlineUserList(List<String> onlineUsers) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (String user : onlineUsers) {
                listModel.addElement(user);
            }
        });
    }
    //更新群聊列表
    private void updateGroupList(List<String> groups) {
        SwingUtilities.invokeLater(() -> {
            groupListModel.clear();
            for (String group : groups) {
                groupListModel.addElement(group);
            }
        });
    }
    private void updateChatWindow(Message message) {
        // 假设 message 对象有 getSender() 和 getContent() 方法来获取消息的发送者和内容
        String sender = message.getSender();//这个sender一定是用户
        String content = new String(message.getContent(), StandardCharsets.UTF_8);
        JTextArea hideChatArea = chatWindows.get(sender);
        if(hideChatArea == null){
            hideChatArea = new JTextArea();
            hideChatArea.append("这是["+ sender + "]的聊天窗口\n");
            hideChatArea.append(sender + ": " + content + "\n");
            chatWindows.put(sender, hideChatArea);
            return;
        }
        hideChatArea.append(sender + ": " + content + "\n");
        chatWindows.put(sender, hideChatArea);
        chatArea.setText(hideChatArea.getText());
    }

    private void updateGroupChatWindow(Message message) {
        // 假设 message 对象有 getSender() 和 getContent() 方法来获取消息的发送者和内容
        String sender = message.getSender();//sender是发出人
        String content_received = new String(message.getContent(), StandardCharsets.UTF_8);

        String[] parts = content_received.split(":", 2);  // 使用冒号分割，最多分为两部分
        String groupName = parts[0];  // 获取 getter
        String content = parts[1];

        JTextArea hideChatArea = chatWindows.get(groupName);
        if(hideChatArea == null){
            hideChatArea = new JTextArea();
            hideChatArea.append("这是["+ groupName + "]的聊天窗口\n");
            hideChatArea.append(sender + ": " + content + "\n");
            chatWindows.put(groupName, hideChatArea);
            return;
        }
        hideChatArea.append(sender + ": " + content + "\n");
        chatWindows.put(groupName, hideChatArea);
        chatArea.setText(hideChatArea.getText());
    }

    private void updateHistoryWindow(String history) {
        // 更新聊天区域
        chatArea.append(history);
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 保持聊天框滚动到底部
    }
    private void startAllocate() {
        new Thread(() -> {
            while (true) {
                // 接收服务器发来的消息
                Message receivedMessage = userClientService.receive();
                if (receivedMessage != null) {
                    switch (receivedMessage.getMessageType()) {
                        //更新用户列表
                        case MessageType.MESSAGE_GET_ONLINE_FRIEND:
                            onlineUsers = userClientService.receiveOnlineUsers(receivedMessage);
                            if (onlineUsers != null && !onlineUsers.isEmpty()) {
                                updateOnlineUserList(onlineUsers);
                            }
                            break;
                        case MessageType.MESSAGE_GET_GROUP:
                            onlineGroups = userClientService.receiveOnlineGroups(receivedMessage);
                            if (onlineGroups != null && !onlineGroups.isEmpty()) {
                                updateGroupList(onlineGroups);
                            }
                            break;
                        //返回历史记录
                        case MessageType.MESSAGE_GET_HISTOAY:
                            String history = new String(receivedMessage.getContent(), StandardCharsets.UTF_8);
                            updateHistoryWindow(history);
                            break;
                        //普通消息
                        case MessageType.MESSAGE_COMMON_MSG:
                            //向服务器发送更新聊天记录请求
                            //userClientService.addHistoryRqt(receivedMessage);
                            //更新聊天面板
                            updateChatWindow(receivedMessage);
                            break;
                        case MessageType.MESSAGE_GROUP_MSG:
                            //更新聊天面板
                            updateGroupChatWindow(receivedMessage);
                            break;
                        //文件消息
                        case MessageType.MESSAGE_IS_FILE:
                            // 获取文件字节内容
                            byte[] fileBytes = receivedMessage.getContent();
                            updateFileWindow(fileBytes);
                            break;
                    }
                }
            }
        }).start();
    }
    public void updateFileWindow(byte[] fileBytes) {
        // 通过文件字节判断文件类型
        String fileExtension = getFileExtension(fileBytes);
        String defaultFileName = "received_file" + fileExtension;

        // 创建一个对话框，提示用户接收文件
        int choice = JOptionPane.showConfirmDialog(null,
                "您是否接收这个文件？",
                "接收文件",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        // 如果用户选择 "是"（YES）
        if (choice == JOptionPane.YES_OPTION) {
            // 打开文件保存对话框，让用户选择保存路径
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择保存路径");

            // 设置为可以保存文件
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // 只允许选择目录

            // 显示对话框并等待用户选择
            int result = fileChooser.showSaveDialog(null);

            if (result == JFileChooser.APPROVE_OPTION) {
                // 获取用户选择的目录路径
                File saveDirectory = fileChooser.getSelectedFile();

                // 创建目标文件
                File savedFile = new File(saveDirectory, defaultFileName);

                // 调用方法保存文件
                saveFile(fileBytes, savedFile);

                JOptionPane.showMessageDialog(null,
                        "文件保存成功！保存路径：" + savedFile.getAbsolutePath(),
                        "文件接收成功",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "文件接收已取消", "操作取消", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "文件接收已取消", "操作取消", JOptionPane.WARNING_MESSAGE);
        }
    }
    // 判断文件类型并返回扩展名
    private String getFileExtension(byte[] fileBytes) {
        if (fileBytes.length < 4) {
            return ".dat";  // 默认扩展名
        }
        // 根据字节判断文件类型
        if (fileBytes[0] == (byte) 0xFF && fileBytes[1] == (byte) 0xD8 && fileBytes[2] == (byte) 0xFF) {
            return ".jpg";  // JPEG 图片
        } else if (fileBytes[0] == (byte) 0x89 && fileBytes[1] == (byte) 0x50 &&
                fileBytes[2] == (byte) 0x4E && fileBytes[3] == (byte) 0x47) {
            return ".png";  // PNG 图片
        } else if (fileBytes[0] == (byte) 0x47 && fileBytes[1] == (byte) 0x49 &&
                fileBytes[2] == (byte) 0x46 && fileBytes[3] == (byte) 0x38) {
            return ".gif";  // GIF 图片
        } else if (fileBytes[0] == (byte) 0x25 && fileBytes[1] == (byte) 0x50 &&
                fileBytes[2] == (byte) 0x44 && fileBytes[3] == (byte) 0x46) {
            return ".pdf";  // PDF 文档
        }

        // 如果不能确定文件类型，返回一个默认扩展名
        return ".dat";
    }
    // 保存文件到指定路径
    private void saveFile(byte[] fileBytes, File file) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(fileBytes); // 写入字节数组
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "文件保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

}

