package com.life.manager;

import com.life.bean.Channel;
import com.life.bean.Message;
import com.life.common.IPublisher;
import com.life.common.ISubscriber;
import com.life.processor_utils.ObjectUtils;

import java.util.HashMap;
import java.util.List;

public class MessageQueueManager {

    private static final HashMap<String, Channel> channels = new HashMap<>();

    private static class MessageQueueManagerHolder {
        private final static MessageQueueManager messageQueueManager = new MessageQueueManager();
    }

    public static MessageQueueManager getInstance() {
        return MessageQueueManagerHolder.messageQueueManager;
    }

    /**
     * 注册发布者
     *
     * @param channelId 频道
     * @param publisher 发布者
     */
    public void registerPublisher(String channelId, IPublisher publisher) {
        Channel channel = channels.get(channelId);
        if (ObjectUtils.isNotEmpty(channel)) {
            channel.addPublisher(publisher);
        } else {
            channel = new Channel();
            channel.addPublisher(publisher);
            channels.put(channelId, channel);
        }
    }

    /**
     * 注销发布者
     *
     * @param channelId 频道
     * @param publisher 订阅者
     */
    public void unRegisterPublisher(String channelId, IPublisher publisher) {
        Channel channel = channels.get(channelId);
        if (ObjectUtils.isNotEmpty(channel)) {
            channel.removePublisher(publisher);
        }
    }

    /**
     * 注册订阅者
     *
     * @param channelId  String
     * @param subscriber ISubscriber
     */
    public void registerSubscriber(String channelId, ISubscriber subscriber) {
        Channel channel = channels.get(channelId);
        if (ObjectUtils.isNotEmpty(channel)) {
            channel.addSubscriber(subscriber);
        } else {
            channel = new Channel();
            channel.addSubscriber(subscriber);
            channels.put(channelId, channel);

        }
    }

    /**
     * 注销订阅者
     *
     * @param channelId  String
     * @param subscriber ISubscriber
     */
    public void unRegisterSubscriber(String channelId, ISubscriber subscriber) {
        Channel channel = channels.get(channelId);
        if (ObjectUtils.isNotEmpty(channel)) {
            channel.removeSubscriber(subscriber);
        }
    }

    // 发布消息给指定的频道的所有订阅者
    public void publishMessage(String channelId, Message message) {
        Channel channel = channels.get(channelId);
        if (ObjectUtils.isNotEmpty(channel)) {
            List<ISubscriber> subscriberList = channel.getSubscriberList();
            for (ISubscriber subscriber : subscriberList) {
                subscriber.receiveMessage(message);
            }
        }
    }
}
