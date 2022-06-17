package com.saasovation.collaboration.application;

import org.springframework.transaction.annotation.Transactional;

public class ForumService {

    // ...

    @Transactional
    public Discussion startExclusiveForumWithDiscussion(
        String aTenantId,
        String aCreatorId,
        String aModeratorId,
        String aForumSubject,
        String aForumDescription,
        String aDiscussionSubject,
        String anExclusiveOwner
    ) {
        Tenant tenant = new Tenant(aTenantId);

        Forum forum = forumRepository.exclusiveForumOfOwner(tenant, anExclusiveOwner);

        // 멱등성을 위한 코드
        if (forum == null) {
            forum = this.startForum(
                tenant,
                aCreatorId,
                aModeratorId,
                aForumSubject,
                aForumDescription,
                anExclusiveOwner
            );
        }

        Discussion discussion = discussionRepository.exclusiveDiscussionOfOwner(tenant, anExclusiveOwner);

        // 멱등성을 위한 코드
        if (discussion == null) {
            Author author = collaboratorService.authorFrom(tenant, aModeratorId);

            discussion = forum.startDiscussion(
                forumNavigationService,
                    author,
                    aDiscussionSubject
            );

            discussionRepository.add(discussion);
        }

        return discussion;
    }

    // ...

}
