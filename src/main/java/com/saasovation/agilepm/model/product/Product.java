package com.saasovation.agilepm.model.product;

public class Product extends ConcurrencySafeEnity {

    // ...

    // ProductService.newProductWith()에 의해 호출되며
    // 협업 옵션이 사전에 구입되었다면, DiscussionAvailability.REQUESTED가 전달됨
    public Product(
        TenantId aTenantId,
        ProductId aProductId,
        ProductOwnerId aProductOwnerId,
        String aName,
        String aDescription,
        // READY, ADD_ON_NOT_ENABLED, NOT_REQUESTED, REQUESTED
        DiscussionAvailability aDiscussionAvailability
    ) {
        this();

        this.setTenantId(aTenantId);
        this.setProdctId(aProductId);
        this.setProductOwnerId(aProductOwnerId);
        this.setName(aName);
        this.setDescription(aDescription);

        this.setDiscussion(ProductDiscussion.fromAvailability(aDiscussionAvailability));

        // 도메인 이벤트를 발행
        DomainEventPublisher
            .instance()
            .publish(new ProductCreated(
               this.tenantId(),
               this.productId(),
               this.productOwnerId(),
               this.name(),
               this.description(),
               // True이면 장기 실행 프로세스 시작
               // 단, false라고 해서 이벤트가 발행되지 않는 것이 아님
               // false이면 이벤트가 무시될 뿐임
               this.discussion.availability().isRequested()
            ));
    }

    // Product만 생성된 후에, 따로 토론을 요청할 때 호출
    public void requestDiscussion(DiscussionAvailability aDiscussionAvailability) {
        if (!this.discussion().availability().isReady()) {
            this.setDiscussion(ProductDiscussion.fromAvailability(aDiscussionAvailability));
        }

        DomainEventPublisher
            .instance()
            // ProductDiscussionRequested는 ProductCreated와 동일한 속성을 가지며,
            // 두 이벤트 모두 장기 실행 프로세스의 트리거로서 활용됨
            .publish(new ProductDiscussionRequested(
                this.tenantId(),
                this.productId(),
                this.productOwnerId(),
                this.name(),
                this.description(),
                // 마찬가지로, True이면 장기 실행 프로세스 시작
                this.discussion.availability().isRequested()
            ));
    }

    public void initiateDiscussion(DiscussionDescriptor aDescriptor) {
        if (aDescriptor == null) {
            throw new IllegalArgumentException("The descriptor must not be null.");
        }

        if (this.discussion().availability().isRequested()) {
            this.setDiscussion(this.discussion().nowReady(aDescriptor));

            DomainEventPublisher
                .instance()
                .publish(
                    new ProductDiscussionInitiated(
                        this.tenantId(),
                        this.productId(),
                        this.discussion()
                    )
                );
        }
    }

    public void failDiscussionInitiation() {
        if (!this.discussion().availability().isReady()) {
            this.setDiscussionInitiationId(null);

            this.setDiscussion(ProductDiscussion.fromAvailability(DiscussionAvailability.FAILED));
        }
    }

    // ...

}
