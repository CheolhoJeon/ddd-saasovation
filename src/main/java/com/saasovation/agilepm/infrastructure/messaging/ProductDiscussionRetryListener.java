package com.saasovation.agilepm.infrastructure.messaging;

import com.saasovation.agilepm.model.product.ProductDiscussionRequestTimedOut;
import com.saasovation.common.port.adapter.messaging.rabbitmq.ExchangeListener;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductDiscussionRetryListener extends ExchangeListener {
    // ...

    @Autowired
    private ProcessService processService;

    // ...

    @Override
    protected String exchangeName() {
        return Exchanges.AGILEPM_EXCHANGE_NAME;
    }

    @Override
    protected String [] listensToEvents() {
        return new String[] {
            "com.saasovation.agilepm.domain.model.product.ProductDiscussionRequestTimedOut",
        };
    }

    @Override
    protected void filteredDispatch(String aType, String aTextMessage) {
        Notification notification = NotificationSerializer.instance().deserialize(aTextMessage, Notification.class);

        ProductDiscussionRequestTimedOut event = notification.event();

        // 완전히 타임아웃되었는가?
        if (event.hasFullyTimedOut()) {
            // 타임아웃 시 처리해야할 로직 요청
            productService.timeOutProductDiscussionRequest(
                new TimeOutProductDiscussionRequestCommand(
                    event.tenantId(),
                    event.processId().id(),
                    event.occurredOn()
                )
            );
        } else {
            // 재시도에 필요한 로직 요청
            productService.retryProductDiscussionRequest(
                new RetryProductDiscussionRequestCommand(
                    event.tenantId(),
                    event.processId().id()
                )
            );
        }
    }

    // ...
}
