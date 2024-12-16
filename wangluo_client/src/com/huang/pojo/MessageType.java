package com.huang.pojo;

/**
 * @description:
 * @author: Snow
 * @date: 2024/6/24
 * **************************************************
 * 修改记录(时间--修改人--修改说明):
 */
public interface MessageType {

    /** 这是文件 */
    String MESSAGE_IS_FILE = "1";

    /** 登录失败 */
    String MESSAGE_GET_HISTOAY = "2";

    /** 普通消息 */
    String MESSAGE_COMMON_MSG = "3";

    /** 群发消息 */
    String MESSAGE_ADD_HISTOAY = "4";

    /** 要求在线用户列表 */
    String MESSAGE_GET_ONLINE_FRIEND = "5";

    /** 创建群聊请求 */
    String MESSAGE_ADD_GROUP = "6";

    /** 客户端请求退出 */
    String MESSAGE_CLIENT_EXIT = "7";

    /** 客户端请求群聊列表 */
    String MESSAGE_GET_GROUP = "8";

    /** 群发消息*/
    String MESSAGE_GROUP_MSG = "9";
}
