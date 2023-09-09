package com.life.common;

import com.life.bean.Message;

public interface ISubscriber {

    void unRegisterSubscriber(String channelId);

    String getCurrentChannelId();

    void receiveMessage(Message message);
}
