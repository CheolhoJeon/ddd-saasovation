package com.saasovation.agilepm.infrastructure.messaging;

import com.saasovation.agilepm.application.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

public class DiscussionStartedListener extends ExchangeListener {

    @Autowired
    private ProductService productService;

    // ...
    @Override
    protected String exchangeName() {
        return Exchanges.COLLABORATION_EXCHANGE_NAME;
    }

    @Override
    protected String [] listensToEvents() {
        return new String[] {
            "com.saasovation.collaboration.domain.model.forum.DiscussionStarted"
        };
    }

    @Override
    protected void filteredDispatch(String aType, String aTextMessage) {
        NotificationReader reader = new NotificatinoReader(aTextMessage);

        String tenantId = reader.eventStringValue("tenant.id");
        String productId = reader.eventStringValue("exclusiveOwner");
        String discussionId = reader.eventStringValue("discussionId.id");

        // 애플리케이션 서비스 호출
        productService.initiateDiscussion(
            new InitiateDiscussionCommand(tenantId, productId, discussionId)
        );
    }

    // ...
}
