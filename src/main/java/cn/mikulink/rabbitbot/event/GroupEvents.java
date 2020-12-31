package cn.mikulink.rabbitbot.event;


import cn.mikulink.rabbitbot.service.ImageService;
import cn.mikulink.rabbitbot.service.RabbitBotService;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 群相关事件
 * @author: MikuLink
 * @date: 2021/1/1 1:42
 **/
@Component
public class GroupEvents extends SimpleListenerHost {
    private static final Logger logger = LoggerFactory.getLogger(GroupEvents.class);

    @Autowired
    private ImageService imageService;
    @Autowired
    private RabbitBotService rabbitBotService;


    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        logger.error("RecallEvent Error:{}", exception.getMessage());
    }


    /**
     * 群成员主动加群事件
     *
     * @param event 消息事件
     * @return 监听状态 详见 ListeningStatus
     */
    @NotNull
    @EventHandler(priority = Listener.EventPriority.NORMAL)
    public ListeningStatus onMemberJoinGroup(@NotNull MemberJoinEvent.Active event) {
        Group group = event.getGroup();
        User sender = event.getMember();
        logger.info("{新增群员_主动加群} userId:{},userNick:{},groupId:{},groupName:{}", sender.getId(), sender.getNick(), group.getId(), group.getName());
        groupMemberJoinMsg(group, sender);
        return ListeningStatus.LISTENING; // 表示继续监听事件
    }

    /**
     * 群成员被邀请加群事件
     *
     * @param event 消息事件
     * @return 监听状态 详见 ListeningStatus
     */
    @NotNull
    @EventHandler(priority = Listener.EventPriority.NORMAL)
    public ListeningStatus onMemberJoinGroup(@NotNull MemberJoinEvent.Invite event) {
        Group group = event.getGroup();
        User sender = event.getMember();
        logger.info("{新增群员_被邀请加群} userId:{},userNick:{},groupId:{},groupName:{}", sender.getId(), sender.getNick(), group.getId(), group.getName());
        groupMemberJoinMsg(group, sender);
        return ListeningStatus.LISTENING; // 表示继续监听事件
    }


    //群成员主动加群，被邀请加群事件业务
    private void groupMemberJoinMsg(Group group, User sender) {
        //获取头像
        String qlogoLocalPath = imageService.getQLogoCq(sender.getId());
        //上传头像
        Image miraiImage = rabbitBotService.uploadMiraiImage(qlogoLocalPath);

        //返回消息
        Message result = MessageUtils.newChain();
        result = result.plus("").plus(miraiImage).plus("\n");
        result = result.plus("[").plus(sender.getNick()).plus("]\n");
        result = result.plus("兔叽增员中。。。");
        group.sendMessage(result);
    }


    /**
     * 群成员主动离群事件
     *
     * @param event 消息事件
     * @return 监听状态 详见 ListeningStatus
     */
    @NotNull
    @EventHandler(priority = Listener.EventPriority.NORMAL)
    public ListeningStatus onMemberLeaveGroup(@NotNull MemberLeaveEvent.Quit event) {
        Group group = event.getGroup();
        User sender = event.getMember();
        logger.info("{群员离群_主动离群} userId:{},userNick:{},groupId:{},groupName:{}", sender.getId(), sender.getNick(), group.getId(), group.getName());
        groupMemberLeaveMsg(group, sender);
        return ListeningStatus.LISTENING; // 表示继续监听事件
    }

    /**
     * 群成员被踢出群事件
     *
     * @param event 消息事件
     * @return 监听状态 详见 ListeningStatus
     */
    @NotNull
    @EventHandler(priority = Listener.EventPriority.NORMAL)
    public ListeningStatus onMemberLeaveGroup(@NotNull MemberLeaveEvent.Kick event) {
        Group group = event.getGroup();
        User sender = event.getMember();
        logger.info("{群员离群_被踢出群} userId:{},userNick:{},groupId:{},groupName:{}", sender.getId(), sender.getNick(), group.getId(), group.getName());
        groupMemberLeaveMsg(group, sender);
        return ListeningStatus.LISTENING; // 表示继续监听事件
    }

    //群成员主动离群，被踢出群事件业务
    private void groupMemberLeaveMsg(Group group, User sender) {
        //获取头像
        String qlogoLocalPath = imageService.getQLogoCq(sender.getId());
        //上传头像
        Image miraiImage = rabbitBotService.uploadMiraiImage(qlogoLocalPath);

        //返回消息
        Message result = MessageUtils.newChain();
        result = result.plus("").plus(miraiImage).plus("\n");
        result = result.plus("[").plus(sender.getNick()).plus("]\n");
        result = result.plus("兔叽-1");
        group.sendMessage(result);
    }
}