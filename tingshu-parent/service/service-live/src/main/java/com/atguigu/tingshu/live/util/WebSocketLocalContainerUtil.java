package com.atguigu.tingshu.live.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.atguigu.tingshu.model.live.FromUser;
import com.atguigu.tingshu.model.live.SocketMsg;
import jakarta.websocket.Session;
import org.joda.time.DateTime;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketLocalContainerUtil {

    //建立用户id与websocket Session的对应关系容器，通过用户id能够获取Session会话信息
    private static Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    //建立用户id与用户基本信息对应关系容器，通过用户id能够获取用户基本信息
    private static Map<Long, FromUser> userMap = new ConcurrentHashMap<>();

    //建立直播间id与用户id列表的对应关系容器，通过直播间能够获取全部直播间用户列表
    private static Map<Long, List<Long>> liveRoomIdToUserListMap = new ConcurrentHashMap<>();

    /**
     * 添加用户Id 与用户的信息
     *
     * @param userId
     * @param fromUser
     */
    public static void addFromUser(Long userId, FromUser fromUser) {
        WebSocketLocalContainerUtil.userMap.put(userId, fromUser);
    }

    /**
     * 删除用户信息
     *
     * @param userId
     */
    public static void removeFromUser(Long userId) {
        WebSocketLocalContainerUtil.userMap.remove(userId);
    }

    /**
     * 根据用户Id 获取用户信息
     *
     * @param userId
     * @return
     */
    public static FromUser getFromUser(Long userId) {
        return WebSocketLocalContainerUtil.userMap.get(userId);
    }

    /**
     * 添加用户Id 与 会话的关系
     *
     * @param userId
     * @param session
     */
    public static void addSession(Long userId, Session session) {
        WebSocketLocalContainerUtil.sessionMap.put(userId, session);
    }

    /**
     * 删除会话
     *
     * @param userId
     */
    public static void removeSession(Long userId) {
        WebSocketLocalContainerUtil.sessionMap.remove(userId);
    }

    /**
     * 获取会话
     *
     * @param userId
     * @return
     */
    public static Session getSession(Long userId) {
        return WebSocketLocalContainerUtil.sessionMap.get(userId);
    }

    /**
     * 删除该房间的用户
     *
     * @param liveRoomId
     * @param userId
     */
    public static void removeUserIdToLiveRoom(Long liveRoomId, Long userId) {
        WebSocketLocalContainerUtil.liveRoomIdToUserListMap.get(liveRoomId).remove(userId);
    }

    /**
     * 获取当前直播间的所有用户Id
     *
     * @param liveRoomId
     * @return
     */
    public static List<Long> getLiveRoomUserIdList(Long liveRoomId) {
        return WebSocketLocalContainerUtil.liveRoomIdToUserListMap.get(liveRoomId);
    }

    /**
     * 获取直播间的人数
     *
     * @param liveRoomId
     * @return
     */
    public static Integer getLiveRoomUserNum(Long liveRoomId) {
        return WebSocketLocalContainerUtil.liveRoomIdToUserListMap.get(liveRoomId).size();
    }

    /**
     * @param liveRoomId
     * @param userId
     */
    public static void addUserIdToLiveRoom(Long liveRoomId, Long userId) {
        //  先获取到房间中的用户Id列表
        List<Long> userIdList = WebSocketLocalContainerUtil.liveRoomIdToUserListMap.get(liveRoomId);
        //  如果当前直播间没有人
        if (CollectionUtils.isEmpty(userIdList)) {
            //  创建集合
            userIdList = new ArrayList<>();
            //  将这个人添加到直播间
            userIdList.add(userId);
            WebSocketLocalContainerUtil.liveRoomIdToUserListMap.put(liveRoomId, userIdList);
        } else {
            //  记录第二个以后进直播间的人；
            userIdList.add(userId);
        }
    }

    /**
     * 构建发生消息对象
     * @param liveRoomId
     * @param fromUser
     * @param msgTypeEnum
     * @param msgContent
     * @return
     */
    public static SocketMsg buildSocketMsg(Long liveRoomId, FromUser fromUser, SocketMsg.MsgTypeEnum msgTypeEnum, Object msgContent){
        //  创建对象
        SocketMsg socketMsg = new SocketMsg();
        socketMsg.setMsgContent(msgContent);
        socketMsg.setLiveRoomId(liveRoomId);
        socketMsg.setMsgType(msgTypeEnum.getCode());
        socketMsg.setFromUser(fromUser);
        socketMsg.setTime(new DateTime().toString("HH:mm:ss"));
        //  返回数据
        return socketMsg;
    }

    /**
     * 封装发送消息方法.
     * @param socketMsg
     */
    public static void sendMsg(SocketMsg socketMsg){
        //  判断当前是否发送消息。
        if (StringUtils.isEmpty(socketMsg.getMsgContent())) return;
        //  获取到当前直播间的用户Id集合
        List<Long> userIdList = liveRoomIdToUserListMap.get(socketMsg.getLiveRoomId());
        //  判断集合
        if (!CollectionUtils.isEmpty(userIdList)){
            //  循环遍历这个集合
            for (Long userId : userIdList) {
                //  执行一个异步发送消息功能.
                Session session = getSession(userId);
                if (null != session){
                    //  异步发送消息方法。
                    session.getAsyncRemote().sendText(JSON.toJSONString(socketMsg, SerializerFeature.DisableCircularReferenceDetect));
                }
            }
        }
    }
}