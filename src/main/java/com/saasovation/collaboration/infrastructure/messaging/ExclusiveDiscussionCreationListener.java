package com.saasovation.collaboration.infrastructure.messaging;

import com.saasovation.common.port.adapter.messaging.rabbitmq.ExchangeListener;
import org.springframework.beans.factory.annotation.Autowired;

public class ExclusiveDiscussionCreationListener extends ExchangeListener {

    @Autowired
    private ForumService forumService;

    // ...

    @Override
    protected void filteredDispatch(String aType, String aTextMessage) {
        NotificationReader reader = new NotificationReader(aTextMessage);

        String tenantId = reader.eventStringValue("tenantId");
        String exclusiveOwnerId = reader.eventStringValue("exclusiveOwnerId");
        String forumSubject = reader.eventStringValue("forumTitle");
        String forumDescription = reader.eventStringValue("forumDescription");
        String discussionSubject = reader.eventStringValue("discussionSubject");
        String creatorId = reader.eventStringValue("creatorId");
        String moderatorId = reader.eventStringValue("moderatorId");

        // 관리 컨텍스트에서 전달된 커맨드를 수신하면 애플리케이션 서비스인 ForumSerivce를 호출
        forumService.startExclusiveForumWithDiscussion(
            tenantId,
            creatorId,
            moderatorId,
            forumSubject,
            forumDescription,
            discussionSubject,
            exclusiveOwnerId
        );
    }

}
