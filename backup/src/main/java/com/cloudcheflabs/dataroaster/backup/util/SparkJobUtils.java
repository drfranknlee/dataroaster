package com.cloudcheflabs.dataroaster.backup.util;

import com.cloudcheflabs.dataroaster.backup.component.SparkRunner;
import org.apache.commons.io.IOUtils;
import org.apache.spark.sql.Row;
import org.joda.time.DateTime;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class SparkJobUtils {

    public static String getYearMonthDayPath(String basePath, DateTime dt)
    {
        String path = "";
        path += basePath;
        path += "/" + SparkRunner.DateDirPath.Year.getDirPath() + dt.getYear();
        path += "/" + SparkRunner.DateDirPath.Month.getDirPath() + String.format("%02d", dt.getMonthOfYear());
        path += "/" + SparkRunner.DateDirPath.Day.getDirPath() + String.format("%02d", dt.getDayOfMonth());

        return path;
    }

    public static String yyyyMMddWithHyphen(DateTime dt)
    {
        return dt.getYear() + "-" + String.format("%02d", dt.getMonthOfYear()) + "-" + String.format("%02d", dt.getDayOfMonth());
    }

    public static String getYearMonthDayHourPath(String basePath, DateTime dt)
    {
        String path = "";
        path += basePath;
        path += "/" + SparkRunner.DateDirPath.Year.getDirPath() + dt.getYear();
        path += "/" + SparkRunner.DateDirPath.Month.getDirPath() + String.format("%02d", dt.getMonthOfYear());
        path += "/" + SparkRunner.DateDirPath.Day.getDirPath() + String.format("%02d", dt.getDayOfMonth());
        path += "/" + SparkRunner.DateDirPath.Hour.getDirPath() + String.format("%02d", dt.getHourOfDay());

        return path;
    }

    public static long convertStringToLong(String value)
    {
        if(value != null)
        {
            if(value.toLowerCase().equals("null"))
            {
                return -1L;
            }
            else
            {
                try {
                    return Long.valueOf(value);
                }catch (NumberFormatException e)
                {
                    return -1L;
                }
            }
        }
        else
        {
            return -1L;
        }
    }


    public static Map<String, Object> getMapFromMap(Map<String, Object> map, String key)
    {
        if(map.containsKey(key))
        {
            return (Map<String, Object>) map.get(key);
        }
        else
        {
            return null;
        }
    }

    public static List<String> getStringListFromMap(Map<String, Object> map, String key)
    {
        if(map.containsKey(key))
        {
            return (List<String>) map.get(key);
        }
        else
        {
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getMapListFromMap(Map<String, Object> map, String key)
    {
        if(map.containsKey(key))
        {
            return (List<Map<String, Object>>) map.get(key);
        }
        else
        {
            return null;
        }
    }


    public static String getStringFromMap(Map<String, Object> map, String key)
    {
        if(map.containsKey(key))
        {
            return (String) map.get(key);
        }
        else
        {
            return null;
        }
    }

    public static String getStringFromRow(Row row, int index)
    {
        if(row.isNullAt(index))
        {
            return null;
        }
        else
        {
            return row.getString(index);
        }
    }


    public static long convertIntToLongFromMap(Map<String, Object> map, String key)
    {
        if(map.containsKey(key))
        {
            Object obj = map.get(key);
            if(obj instanceof Integer)
            {
                return new Long((int)obj).longValue();
            }
            else if(obj instanceof Long)
            {
                return (Long) obj;
            }
            else
            {
                throw new RuntimeException("Value Type is not int | long...");
            }
        }
        else
        {
            return -1L;
        }
    }

    public static Long extractDigitsFromString(String str)
    {
        if(str == null)
        {
            return -1L;
        }

        String digits = str.replaceAll("\\D+","");

        if(digits.trim().equals(""))
        {
            return -1L;
        }

        return Long.valueOf(digits);
    }

    public static String encodeAsBase64(String input)
    {
        final byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        final String encoded = Base64.getEncoder().encodeToString(inputBytes);

        return encoded;
    }

    public static String decodeBase64EncodedString(String encodedString)
    {
        final byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }


    public static String convertTimeToFormattedString(long time, String format)
    {
        Date date = new Date(time);
        SimpleDateFormat df = new SimpleDateFormat(format);
        String dateText = df.format(date);

        return dateText;
    }

    public static String fileToString(String filePath) {
        try (InputStream inputStream = new ClassPathResource(filePath).getInputStream())  {
            return IOUtils.toString(inputStream);
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

    public static Map<String, String> getFileContentListFromClasspath(String pathDir, String extension)
    {
        List<String> files = readFileNamesFromClasspath(pathDir, extension);

        Map<String, String> map = new HashMap<>();

        files.stream().forEach(f -> {
            String path = pathDir + "/" + f;

            String fileContent = fileToString(path);

            map.put(f, fileContent);
        });

        return map;
    }


    private static List<String> readFileNamesFromClasspath(String pathDir, String extension)
    {
        List<String> fileNames = new ArrayList<>();

        ClassLoader cl = new SparkJobUtils().getClass().getClassLoader();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
        try {

            Resource[] resources = resolver.getResources("classpath*:" + pathDir + "/*." + extension);
            for (Resource resource : resources) {
                fileNames.add(resource.getFilename());
            }

            return fileNames;
        }catch (IOException e)
        {
            e.printStackTrace();
        }

        return fileNames;
    }
}
