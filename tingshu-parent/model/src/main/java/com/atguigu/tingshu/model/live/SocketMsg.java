package com.atguigu.tingshu.model.live;

import lombok.Data;
import lombok.Getter;

/**
 * 这里我们就不能使用简单的文本消息进行消息的发送了，我们使用json进行消息的发送。
 */
@Data
public class SocketMsg {

    @Getter
    public enum MsgTypeEnum {

        HEART_BEAT("0","心跳信息"),
        PUBLIC_MSG("1","公共聊天消息"),
        JOIN_CHAT("2","加入直播间"),
        CLOSE_SOCKET("3","退出直播间"),
        TOKEN_INVALID("-1","token无效"),
        ;

        private String code;
        private String data;

        MsgTypeEnum(String code, String data) {
            this.code = code;
            this.data = data;
        }

    }

    //直播房间id
    private Long liveRoomId;

    //消息类型
    private String msgType;

    //消息内容
    private Object msgContent;

    //发送者
    private FromUser fromUser;

    //时间戳
    private String time;
}