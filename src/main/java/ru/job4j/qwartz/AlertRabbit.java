package ru.job4j.qwartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(RabbitProperties.class).build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(11)
                    .withRepeatCount(3);
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    public static class RabbitProperties implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            File file = new File("rabbit.properties");
            List<String> result = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                br.lines().forEach(result::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(result);
    }
}
}
