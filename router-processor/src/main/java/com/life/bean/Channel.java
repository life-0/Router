package com.life.bean;

import com.life.common.IPublisher;
import com.life.common.ISubscriber;

import java.util.ArrayList;
import java.util.List;

public class Channel {
    private String channelId;
    private final List<ISubscriber> subscriberList = new ArrayList<>();
    private final List<IPublisher> publisherList = new ArrayList<>();

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public void addSubscriber(ISubscriber subscriber) {
        subscriberList.add(subscriber);
    }

    public void removeSubscriber(ISubscriber subscriber) {
        subscriberList.remove(subscriber);
    }

    public List<ISubscriber> getSubscriberList() {
        return subscriberList;
    }

    public void addPublisher(IPublisher publisher) {
        publisherList.add(publisher);
    }

    public void removePublisher(IPublisher publisher) {
        publisherList.remove(publisher);
    }

    public List<IPublisher> getPublisherList() {
        return publisherList;
    }

    @Override
    public String toString() {
        return "Channel{" +
                "channelId='" + channelId + '\'' +
                ", subscriberList=" + subscriberList +
                ", publisherList=" + publisherList +
                '}';
    }
}
