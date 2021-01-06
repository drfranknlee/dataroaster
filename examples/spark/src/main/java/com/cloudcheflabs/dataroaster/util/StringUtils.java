package com.cloudcheflabs.dataroaster.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

public class StringUtils {

    public static String fileToString(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream())  {
            return IOUtils.toString(inputStream);
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

    public static String addZeroToMonthOrDay(int monthOrDay) {
        return String.format("%02d", monthOrDay);
    }
}
