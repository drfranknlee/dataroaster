package com.cloudcheflabs.dataroaster.cli.util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class FileUtils {

    public static InputStream readFile(String filePath) {
        try {
            return new FileInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream readFileFromClasspath(String filePath) {
        try {
            return new ClassPathResource(filePath).getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fileToString(String filePath, boolean fromClasspath) {
        try {
            return fromClasspath ? IOUtils.toString(readFileFromClasspath(filePath)) :
                    IOUtils.toString(readFile(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stringToFile(String string, String path, boolean executable) {
        try{
            Writer output = new BufferedWriter(new FileWriter(path));
            output.write(string);
            output.close();
            new File(path).setExecutable(executable);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static void createDirectory(String directoryPath) {
        try {
            org.apache.commons.io.FileUtils.forceMkdir(new File(directoryPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteDirectory(String directoryPath) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException e) {
        }
    }
}
