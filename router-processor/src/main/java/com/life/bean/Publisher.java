package com.life.bean;

import com.life.common.IPublisher;
import com.life.manager.MessageQueueManager;

public class Publisher implements IPublisher {
    private String channelId;

    public Publisher(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public void registerPublisher(String channelId) {
        this.channelId = channelId;
        MessageQueueManager.getInstance().registerPublisher(channelId, this);
    }

    @Override
    public void unRegisterPublisher(String channelId) {
        MessageQueueManager.getInstance().unRegisterPublisher(channelId, this);

    }

    @Override
    public String getCurrentChannelId() {
        return this.channelId;
    }

    @Override
    public void publishMessage(Message message) {
        MessageQueueManager.getInstance().publishMessage(channelId, message);
    }
}
