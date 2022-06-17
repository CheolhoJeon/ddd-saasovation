package com.saasovation.agilepm.model.product;

import com.saasovation.common.domain.model.process.ProcessId;
import com.saasovation.common.domain.model.process.ProcessTimedOut;

public class ProductDiscussionRequestTimedOut extends ProcessTimedOut {

    public ProductDiscussionRequestTimedOut(
        String aTenantId,
        ProcessId aProcessId,
        int aTotalRetriesPermitted,
        int aRetryCount
    ) {
        super(aTenantId, aProcessId, aTotalRetriesPermitted, aRetryCount);
    }

}
