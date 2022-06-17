package com.saasovation.agilepm.model.product;

public class ProductDiscussionRequested implements DomainEvent {
    // ...

    public ProductDiscussionRequested(
        TenantId aTenantId,
        ProductId aProductId,
        ProductOwnerId aProductOwnerId,
        String aName,
        String aDescription,
        boolean isRequestingDiscussion
    ) {
        // ...
    }

    // ...
}
