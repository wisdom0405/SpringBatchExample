package com.example.SpringBatchExample.batch;

import com.example.SpringBatchExample.entity.AfterEntity;
import com.example.SpringBatchExample.entity.BeforeEntity;
import com.example.SpringBatchExample.repository.AfterRepository;
import com.example.SpringBatchExample.repository.BeforeRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Job;

import java.util.Map;


@Configuration
public class FirstBatch {

    //  Spring Batch가 Job 실행 이력, Job 상태, Step 상태 등을 저장하고 관리하는 인터페이스
    private final JobRepository jobRepository;
    // 트랜잭션 관리자 Spring Batch는 각 Step이 트랜잭션 단위로 처리되므로 필수입니다.
    private final PlatformTransactionManager platformTransactionManager;

    private final BeforeRepository beforeRepository;
    private final AfterRepository afterRepository;

    public FirstBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, BeforeRepository beforeRepository, AfterRepository afterRepository) {

        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.beforeRepository = beforeRepository;
        this.afterRepository = afterRepository;
    }

    // Job 정의
    @Bean
    public Job firstJob() { // Job : Batch 작업단위. 여러 Step을 구성할 수 있다.

        System.out.println("first job");

        // JobBuilder :  Job을 생성하기 위한 빌더 클래스
        return new JobBuilder("firstJob", jobRepository) // Job에 대한이름, 작업 트래킹을 위한 Job Repository (작업이 진행되면 Spring Batch가 알아서 이 작업이 진행되는지 meta_db에 저장함)
                .start(firstStep()) // Job의 첫번째 Step을 지정
//                .next() // 이렇게 다음 step 정의가능
                .build();
    }

    // 순서 생각 및 Step 정의
    // Job을 정의했지만, 실제 배치 처리는 Job 아래에 존재하는 하나의 Step에서 수행되게 됩니다.
    // 따라서 Step에서 읽기 -> 처리 -> 쓰기 과정을 구상해야 하며, Step을 등록하기 위한 @Bean을 등록
    @Bean
    public Step firstStep(){
        return new StepBuilder("firstStep", jobRepository)
                // chunk : 읽기 -> 처리 -> 쓰기 작업은 청크 단위로 진행되는데, 대량의 데이터를 얼만큼 끊어서 처리할지에 대한 값으로 적당한 값을 선정해야 한다.
                // 너무 작으면 I/O 처리가 많아지고 오버헤드 발생, 너무 크면 적재 및 자원 사용에 대한 비용과 실패 부담이 커짐
                .<BeforeEntity, AfterEntity> chunk(10, platformTransactionManager)
                .reader(beforeReader())
                .processor(middleProcessor())
                .writer(afterWriter())
                .build();
    }

    @Bean
    // RepositoryItemReader : Spring Batch에서 제공하는 ItemReader 구현체
    // Spring Data Repository(JPA, Mongo 등)를 이용해 데이터를 읽어오는 컴포넌트
    public RepositoryItemReader<BeforeEntity> beforeReader() {

        return new RepositoryItemReaderBuilder<BeforeEntity>()
                .name("beforeReader") // Spring Batch에서 사용할 리더 이름
                .pageSize(10) // 데이터를 한 번에 10개씩 페이징
                .methodName("findAll") // Repository에서 호출할 메서드명입니다. 여기서는 beforeRepository.findAll()
                .repository(beforeRepository) // 실제 데이터베이스와 연결된 JPA Repository
                .sorts(Map.of("id", Sort.Direction.ASC)) // 페이징 처리를 위해 필수적인 정렬 조건 (여기선 id 오름차순)
                .build();
    }

    @Bean
    // Process : 읽어온 데이터를 처리하는 Process (큰 작업을 수행하지 않을 경우 생략 가능, 지금과 같이 단순 이동은 사실 필요 없음)
    public ItemProcessor<BeforeEntity, AfterEntity> middleProcessor() {

        return new ItemProcessor<BeforeEntity, AfterEntity>() {

            @Override
            public AfterEntity process(BeforeEntity item) throws Exception {

                AfterEntity afterEntity = new AfterEntity();
                afterEntity.setUsername(item.getUsername());

                return afterEntity;
            }
        };
    }

    @Bean
    // Write : AfterEntity에 처리한 결과를 저장하는 Writer
    public RepositoryItemWriter<AfterEntity> afterWriter() {

        return new RepositoryItemWriterBuilder<AfterEntity>()
                .repository(afterRepository)
                .methodName("save")
                .build();
    }
}