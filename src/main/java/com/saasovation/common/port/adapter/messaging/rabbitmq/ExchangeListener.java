package com.saasovation.common.port.adapter.messaging.rabbitmq;

import antlr.debug.MessageListener;

import java.util.Queue;

public abstract class ExchangeListener {

    private MessageConsumer messageConsumer;
    private Queue queue;

    public ExchangeListener() {
        super();

        this.attachQueue();
    }

    protected abstract String exchangeName();

    protected abstract void filteredDispatch(String aType, String aTextMessage);

    // 받고자 하는 알림 타입의 String[]을 응답
    protected abstract String[] listensToEvents();

    protected String queueName() {
        return this.getClass().getSimpleName();
    }

    private void attachQueue() {
        Exchange exchange = Exchange.fanOutInstance(
            ConnecttionSettings.instance(),
            this.exchangeName(),
            true
        );

        this.queue = Queue.individualExchangeSubscriberInstance(exchange, this.exchageName() + "." + this.queueName());
    }

    private Queue queue() {
        return this.queue;
    }

    private void registerConsumer() {
        this.messageConsumer = MessageConsumer.instance(this.queue(), false);

        this.messageConsumer.receiveOnly(this.listensToEvents(), new MessageListener(MessageListener.Type.Text)) {
            @Override
            public void handleMessage(
                String aType,
                String aMessageId,
                Date aTimestamp,
                String aTextMessage,
                long aDeliveryTag,
                boolean isRedelivery
            ) throw Exception {
                filteredDispatch(aType, aTextMessage);
            }
        }
    }

}
