package ru.job4j.grabber.utils;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;

public class HabrCareerDateTimeParserTest {
    @Test
    public void distance3d() {
        String string = "2024-02-21T12:41:09+03:00";
        LocalDateTime expected =
                LocalDateTime.of(2024, 2, 21,  12,  41, 9, 0);
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime result = parser.parse(string);
        Assertions.assertEquals(expected, result);
    }
}
