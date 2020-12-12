package com.cloudcheflabs.dataroaster.backup.component;

/**
 * Created by mykidong on 2017-11-27.
 */
public interface SparkRunner {

    public static enum CriterionDate {
        NOW, NULL;
    }

    public static enum DateType {
        DAY, HOUR
    }

    public static enum DateDirPath
    {
        Year("year="), Month("month="), Day("day="), Hour("hour=");

        private String dirPath;

        private DateDirPath(String dirPath)
        {
            this.dirPath = dirPath;
        }

        public String getDirPath()
        {
            return this.dirPath;
        }
    }


    int run(String[] args) throws Exception;

}
