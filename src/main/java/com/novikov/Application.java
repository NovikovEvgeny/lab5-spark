package com.novikov;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple3;

import java.util.Objects;

import static org.apache.spark.sql.functions.*;

public class Application {
    private static final String COUNT = "count";

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected hdfs host name and logs dir path");
        }

        SparkSession session = SparkSession.builder()
                .master("local")
                .appName("lab5")
                .getOrCreate();

        JavaRDD<ApacheAccessLog> data = session.read().textFile("hdfs://" + args[0] + ":9000" + args[1] + "/NASA_access_log_Jul95")
                .javaRDD()
                .map(ApacheAccessLog::parseFromLogLine)
                .filter(Objects::nonNull);

        Dataset<Row> dataSet = session.createDataFrame(data, ApacheAccessLog.class);

        taskOneDataFrame(dataSet, "hdfs://" + args[0] + ":9000/task1");
        taskTwoDataFrame(dataSet, "hdfs://" + args[0] + ":9000/task2");
        taskThreeDataFrame(dataSet, "hdfs://" + args[0] + ":9000/task3");

//        taskOneJavaRDD(data, "hdfs://" + args[0] + ":9000/task1");
//        taskTwoJavaRDD(data, "hdfs://" + args[0] + ":9000/task2");
    }

    private static void taskOneDataFrame(Dataset<Row> dataSet, String outputDir) {
        dataSet.filter(col(ApacheAccessLog.RESPONSE_CODE).between(500, 599))
                .groupBy(ApacheAccessLog.ENDPOINT)
                .count()
                .select(ApacheAccessLog.ENDPOINT, COUNT)
                .coalesce(1)
                .toJavaRDD()
                .saveAsTextFile(outputDir);
    }

    private static void taskTwoDataFrame(Dataset<Row> dataSet, String outputDir) {
        dataSet.groupBy(ApacheAccessLog.METHOD, ApacheAccessLog.RESPONSE_CODE, ApacheAccessLog.DATE_STRING)
                .count()
                .filter(col(COUNT).geq(10))
                .select(ApacheAccessLog.DATE_STRING, ApacheAccessLog.METHOD, ApacheAccessLog.RESPONSE_CODE, COUNT)
                .sort(ApacheAccessLog.DATE_STRING, ApacheAccessLog.METHOD, ApacheAccessLog.RESPONSE_CODE)
                .coalesce(1)
                .toJavaRDD()
                .saveAsTextFile(outputDir);
    }

    private static void taskThreeDataFrame(Dataset<Row> dataSet, String outputDir) {
        dataSet.filter(col(ApacheAccessLog.RESPONSE_CODE).between(400, 599))
                .groupBy(window(to_date(col(ApacheAccessLog.DATE_STRING), ApacheAccessLog.OUT_DATE_FORMAT), "1 week", "1 day"))
                .count()
                .select(date_format(col("window.start"), ApacheAccessLog.OUT_DATE_FORMAT),
                        date_format(col("window.end"), ApacheAccessLog.OUT_DATE_FORMAT),
                        col(COUNT))
                .coalesce(1)
                .toJavaRDD()
                .saveAsTextFile(outputDir);
    }

    private static void taskOneJavaRDD(JavaRDD<ApacheAccessLog> data, String outputDir) {
        data.filter(logRow -> logRow.getIntResponseCode() >= 500 && logRow.getIntResponseCode() <= 599)
                .mapToPair(logRow -> new Tuple2<>(logRow.getEndpoint(), 1))
                .reduceByKey((a, b) -> a + b)
                .coalesce(1)
                .saveAsTextFile(outputDir);
    }

    private static void taskTwoJavaRDD(JavaRDD<ApacheAccessLog> data, String outputDir) {
        data.mapToPair(logRow -> new Tuple2<>(new Tuple3<>(logRow.getDateString(), logRow.getMethod(), logRow.getResponseCode()), 1))
                .reduceByKey((a, b) -> a + b)
                .filter(f -> f._2() >= 10)
                .coalesce(1)
                .saveAsTextFile(outputDir);
    }
}