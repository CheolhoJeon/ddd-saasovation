package com.saasovation.agilepm.infrastructure.messaging;

import com.saasovation.agilepm.application.ProductService;
import com.saasovation.common.port.adapter.messaging.rabbitmq.ExchangeListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Properties;

public class ProductDiscussionRequestedListener extends ExchangeListener {
    // ...

    private static final String COMMAND = "com.saasovation.collaboration.discussion.CreateExclusiveDiscussion";

    @Autowired
    private ProductService productService;

    @Override
    protected String exchangeName() {
        return Exchanges.AGILEPM_EXCHANGE_NAME;
    }

    @Override
    protected String [] listensToEvents() {
        return new String[] {
            "com.saasovation.agilepm.domain.model.product.ProductCreated",
            "com.saasovation.agilepm.domain.model.product.ProductDiscussionRequested",
        };
    }

    @Override
    protected void filteredDispatch(String aType, String aTextMessage) {
        NotificationReader reader = new NotificationReader(aTextMessage);

        // requestingDiscussion의 값이 거짓이라면 이벤트 무시
        // 그렇지 않은 경우, 이벤트의 상태로부터 CreateExclusiveDiscussion 커맨드를 만들어서 협업 컨텍스트의 메시지 익스체인지로 보냄
        if (!reader.eventBooleanValue("requestingDiscussion")) {
            return;
        }

        String tenantId = reader.eventStringValue("tenantId.id");
        String productId = reader.eventStringValue("product.id");

        productService.startDiscussionInitiation(
            new StartDiscussionInitiationCommand(tenantId, productId)
        );

        // 협업 컨텍스트로 커맨드를 보낸다
        Properties parameters = this.parametersFrom(reader);
        PropertiesSerializer serializer = PropertiesSerializer.instance();

        String serialization = serializer.serialize(parameters);
        String commandId = this.commandIdFrom(parameters);

        this.messageProducer()
            .send(
                serialization,
                MessageParameters.durableTextParameters(
                    COMMAND, commandId, new Date()
                )
            )
            .close();
    }

    // ...
}
