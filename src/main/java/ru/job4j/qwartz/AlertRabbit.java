package ru.job4j.qwartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.TimeZone;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = Rabbit.class.getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static Connection getConnection() {
        Properties config = getProperties();
        Connection connection;
        try {
            Class.forName(config.getProperty("driver-class-name"));
            connection = DriverManager.getConnection(
                    config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return connection;
    }

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            int interval = Integer.parseInt(getProperties()
                    .getProperty("rabbit.interval"));
            data.put("interval", interval);
            data.put("connection", getConnection());
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here...");
            try (PreparedStatement ps = ((Connection) context
                    .getJobDetail()
                    .getJobDataMap()
                    .get("connection")).
                    prepareStatement(
                            "insert into rabbit (created_date) values (?)")) {
                LocalDateTime jobTime = LocalDateTime
                        .ofInstant(Instant.ofEpochMilli(context.getFireTime().getTime()),
                                TimeZone.getDefault().toZoneId());
                ps.setTimestamp(1, Timestamp.valueOf(jobTime));
                System.out.printf("Rabbit runs here with frequency: %s seconds",
                        context.getJobDetail()
                                .getJobDataMap()
                                .get("interval"));
                ps.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
