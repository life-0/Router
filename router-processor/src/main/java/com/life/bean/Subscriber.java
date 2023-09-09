package com.life.bean;

import com.life.common.ISubscriber;
import com.life.manager.MessageQueueManager;

public class Subscriber implements ISubscriber {
    private String channelId;

    public Subscriber(String channelId) {
        this.channelId = channelId;
        registerSubscriber(channelId);
    }

    private void registerSubscriber(String channelId) {
        this.channelId = channelId;
        MessageQueueManager.getInstance().registerSubscriber(channelId, this);
    }

    @Override
    public void unRegisterSubscriber(String channelId) {
        MessageQueueManager.getInstance().unRegisterSubscriber(channelId, this);
    }

    @Override
    public String getCurrentChannelId() {
        return this.channelId;
    }

    @Override
    public void receiveMessage(Message message) {

    }
}
