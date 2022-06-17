package com.saasovation.agilepm.model.product;

import java.io.Serializable;

public class ProductDiscussion implements Serializable {

    // ...

    public static ProductDiscussion fromAvailability(DiscussionAvailablity anAvailability) {
        if (anAvailability.isReady()) {
            throw new IllegalArgumentException("Cannot be created ready.");
        }

        DiscussionDecriptor descriptor = new DiscussionDescriptor(
            DiscussionDescriptor(DiscussionDescriptor.UNDEFIEND_ID)
        );

        return new ProductDiscussion(descriptor, anAvailability);
    }

    // ...

}
