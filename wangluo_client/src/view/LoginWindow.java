package view;

import utils.MyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow {

    public static void main(String[] args) {
        // 创建并显示登录窗口
        SwingUtilities.invokeLater(() -> new LoginWindow().createLoginWindow());
    }

    public void createLoginWindow() {
        // 创建JFrame窗口
        JFrame frame = new JFrame("登录");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150); // 设置窗口大小

        // 创建面板并设置布局
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2, 10, 10)); // 网格布局，3行2列，间距为10

        // 创建组件
        JLabel usernameLabel = new JLabel("用户名:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("密码:");

        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("登录");
        // 设置标签居中对齐
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 将组件添加到面板中
        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel()); // 占位符
        panel.add(loginButton);

        // 设置窗口内容面板
        frame.add(panel);

        // 监听登录按钮的点击事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] password = passwordField.getPassword();
                // 将密码转换为字符串
                String passwordStr = new String(password);

                // 如果密码正确就进入页面
                if (passwordStr.equals("123456")) {
                    try {
                        new ChatWindow(username);
                        frame.setVisible(false);
                    } catch (MyException ex) {
                        ex.printStackTrace();
                    }

                    // 可以打开其他窗口或执行其他操作
                } else {
                    JOptionPane.showMessageDialog(frame, "用户名或密码错误！", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 设置窗口显示
        frame.setLocationRelativeTo(null); // 窗口居中
        frame.setVisible(true);
    }
}
