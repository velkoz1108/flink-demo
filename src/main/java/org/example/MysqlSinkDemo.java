package org.example;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class MysqlSinkDemo {
    private static String URL = "jdbc:mysql://localhost:3306/my_schema?characterEncoding=utf8&useSSL=false";
    private static String USER = "root";
    private static String PASSWORD = "Eden1992";

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        //数据输入
        DataStreamSource<String> inputData = environment.socketTextStream("localhost", 9000);
        //数据处理
        DataStream<Person> streaming = inputData.map(data -> {
            System.out.println("input data ---> " + data);
            String[] arr = data.split(",");
            //TODO data validation
            return new Person(arr[0], Integer.valueOf(arr[1]), arr[2]);
        });
        //数据下沉
        streaming.addSink(new MySqlSink());

        environment.execute("Mysql Sink");

    }

    static class Person {
        private String name;
        private Integer age;
        private String address;

        public Person() {
        }

        public Person(String name, Integer age, String address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", address='" + address + '\'' +
                    '}';
        }
    }

    static class MySqlSink extends RichSinkFunction<Person> {
        private Connection connection;
        private PreparedStatement preparedStatement;

        @Override
        public void open(Configuration parameters) throws Exception {
            System.out.println("[MysqlSink Open ... ]");
            super.open(parameters);
            //important
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            preparedStatement = connection.prepareStatement("replace INTO person (name, age, address) VALUES (?, ?, ?)");
        }

        @Override
        public void invoke(Person value, Context context) throws Exception {
            System.out.println("[MysqlSink invoke ... ]");

            preparedStatement.setString(1, value.getName());
            preparedStatement.setInt(2, value.getAge());
            preparedStatement.setString(3, value.getAddress());

            boolean result = preparedStatement.execute();
            System.out.println("[MysqlSink invoke ... ] result --> " + result);
        }

        @Override
        public void close() throws Exception {
            System.out.println("[MysqlSink Close ... ]");
            preparedStatement.close();
            connection.close();
        }
    }
}
