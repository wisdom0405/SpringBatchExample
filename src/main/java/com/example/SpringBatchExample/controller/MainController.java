package com.example.SpringBatchExample.controller;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
// “/first” 경로로 요청이 들어온다면, 배치 1을 실행하도록 설정하겠습니다.
// 이때 배치 실행에 대한 판단 값도 함께 넘겨줘야 중복 실행 및 실행 스케줄을 확인할 수 있습니다.
public class MainController {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public MainController(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    @GetMapping("/first")
    public String firstApi(@RequestParam("value") String value) throws Exception { // Batch 작업 중 예외 발생 시 예외 던질 수 있도록 throws 처리
        // 특정한 파라미터가 들어오면, Batch 파라미터로 함께 넘겨줄 수 있도록 함 (특정일자에만 실행시키기 위함)
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("date", value)
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("firstJob"), jobParameters);


        return "ok";
    }
}