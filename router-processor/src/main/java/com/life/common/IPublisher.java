package com.life.common;

import com.life.bean.Message;

public interface IPublisher {

    void registerPublisher(String channelId);

    void unRegisterPublisher(String channelId);

    String getCurrentChannelId();

    void publishMessage(Message message);
}
