package com.saasovation.agilepm.application;

import com.saasovation.agilepm.model.product.Product;
import com.saasovation.agilepm.model.product.ProductDiscussionRequestTimedOut;
import com.saasovation.common.domain.model.process.ProcessId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOwnerRepository productOwnerRepository;

    @Autowired
    private ProcessTrackerRepository processTrackerRepository;

    //...

    // 제품(Product) 생성과 동시에 토론(Discussion)을 '함께' 요청하는 오퍼레이션
    @Transactional
    public String newProductWithDiscussion(NewProductCommand aCommand) {
        // Product 생성자를 호출
        return this.newProductWith(
                aCommand.getTenantId(),
                aCommand.getProductOwnerId(),
                aCommand.getName(),
                aCommand.getDescription(),
                // 토론 생성이 가능한지에 대한 여부를 나타내는 프로퍼티를 반환
                this.requestDiscussionIfAvailable()
        );
    }

    @Transactional
    public void initiateDiscussion(InitiateDiscussionCommand aCommand) {
        Product product = productRepository.productOfId(
                new TenantId(aCommand.getTenantId()),
                new ProductId(aCommand.getProductId())
        );

        if (product == null) {
            throw new IllegalStateException(
                "Unknown product of tenant id: "
                + aCommand.getTenantId()
                + "and product id: "
                + aCommand.getProductId());
        }

        product.initiateDiscussion(new DiscussionDescriptor(aCommand.getDiscussionId()));

        timeContrainedProcessTracker tracker =
                this.processTrackerRepository.trackerOfProcessId(ProcessId.existingProcessId(product.discussionInitiationId()));

        // 이 시점 이후론 재시도나 타임아웃을 알리기 위해 트래커를 사용하지 않음. 프로세스는 끝남
        tracker.completed();
    }

    @Transactional
    public void startDiscussionInitiation(StartDiscussionInitiationCommand aCommand) {
        Product product = productRepository.productOfId(
            new TenantId(aCommand.getTenantId()),
            new ProductId(aCommand.getProductId())
        );

        if (product == null) {
            throw new IllegalStateException(
                "Unknown product of tenant id: "
                + aCommand.getTenantId()
                + "and product id: "
                + aCommand.getProductId());
        }

        String timedOutEventName = ProductDiscussionRequestTimedOut.class.getName();

        // 프로세스 트래커 생성
        TimeConstrainedProcessTracker tracker = new TimeConstrainedProcessTracker(
            product.tenantId().id(),
            ProcessId.newProcessId(),
            "Create discussion for product: " + product.name(),
            new Date(),
            5L * 60L * 1000L, // 5분 마다 재시도한다
            3, // 총 3회 재시도한다
            timedOutEventName
        );

        // 트래커를 영속화시킴
        processTrackerRepository.add(tracker);

        product.setDiscussionInitiationId(tracker.processId().id());
    }

    @Transactional
    public void checkForTimedOutProcesses() {
        Collection<TimeConstrainedProcessTracker> trackers = processTrackerRepository.allTimedOut();

        for (TimeConstrainedProcessTracker tracker : trackers) {
            // 프로세스의 재시도나 타임아웃이 필요한지 여부를 확인하고, 만약 확인된 경우에는 ProcessTimedOut의 서브클래스를 발행함
            tracker.informationProcessTimedOut();
        }
    }

    @Transactional
    public void timeOutProductDiscussionRequest(TimeOutProductDiscussionRequestCommand aCommand) {
        ProcessId processId = ProcessId.existingProcessId(aCommand.getProcessId());

        TenantId tenantId = new TenantId(aCommand.getTenantId());

        Product product = productRepository.productOfDiscussionInitiationId(tenantId, processId);

        // 실패 메일 전송
        this.sendEmailForTimedOutProcess(product);

        // Discussion 생성 실패에 따른 Product의 적절한 상태를 가질 수 있도록 메서드 호출
        product.failDiscussionInitiation();
    }

    @Transactional
    public void retryProductDiscussionRequest(RetryProductDiscussionRequestCommand aCommand) {
        ProcessId processId = ProcessId.existingProcessId(aCommand.getProcessId());

        TenantId tenantId = new TenantId(aCommand.getTenantId());

        Product product = productRepository.productOfDiscussionInitiationId(tenantId, processId);

        if (product == null) {
            throw new IllegalStateException(
                "Unknown product of tenant id: "
                + aCommand.getTenantId()
                + "and product id: "
                + aCommand.getProductId());
        }

        // ProductDiscussionRequested 이벤트를 다시 발행
        this.requestProductDiscussion(new RequestProductDiscussionCommand(aCommand.getTenantId(), product.productId().id()));
    }

    // ...

}





















