# 将程序打包

```shell
mvn clean install -DskipTests
```

# 开启server

```shell
nc -l 9000
```

nc即netcat，[查看详细参数](https://www.cnblogs.com/xiangtingshen/p/10909077.html)

# 添加一个job

```shell
./flink run -c org.example.SocketWindowWordCount /Users/twang/IdeaProjects/flink-demo/target/original-flink-demo-1.0-SNAPSHOT.jar --port 9000 --host 127.0.0.1
```

# 测试另外一个job

```shell
 ./flink run -c org.example.SocketTextStreamWordCount /Users/twang/IdeaProjects/flink-demo/target/original-flink-demo-1.0-SNAPSHOT.jar 127.0.0.1 9000
```

# 输入数据

在nc打开的server端输入数据并回车发送

```shell
$ nc -l 9000

hello
world
eden
twang

```

# 查看flink日志

```shell
$ tail -fn 20 xxx.local.out

(hello,1)
(world,1)
(eden,1)
(twang,1)

```

# mysql sink

## 数据处理

```shell
//数据处理
DataStream<Person> streaming = inputData.map(data -> {
    System.out.println("input data ---> " + data);
    String[] arr = data.split(",");
    //TODO data validation
    return new Person(arr[0], Integer.valueOf(arr[1]), arr[2]);
});
```

## 定义mysql sink

```shell

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
```

## 添加sink

```shell
//数据下沉
streaming.addSink(new MySqlSink());
```
## 开启nc
```shell
nc -l
```

## 添加任务
```shell
./flink run -c org.example.MysqlSinkDemo /Users/twang/IdeaProjects/flink-demo/target/flink-demo-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 输入数据
```shell
$ nc -l
wangtao,29,shanghai

```

### 查看mysql数据

```shell
select * from person;
```

id|name|age|address
----:|:-----:|---:|:-----:
1|eden|28|shanghai
2|wangtao|29|kunshan
3|wang|20|sh
4|qq|18|ks

### troubleshooting

```shell
Caused by: java.sql.SQLException: No suitable driver found for jdbc:mysql://localhost:3306/my_schema?characterEncoding=utf8&useSSL=false
```

没有加载driver class

```shell
Class.forName("com.mysql.cj.jdbc.Driver");
```

或者没有将mysql connector打包到jar包中

```shell
 <plugin>
        <artifactId>maven-assembly-plugin</artifactId>
        <configuration>
            <archive>
                <manifest>
                    <addClasspath>true</addClasspath>
                    <!--下面必须指定好主类-->
                    <mainClass>org.example.MysqlSinkDemo</mainClass>
                </manifest>
            </archive>

            <descriptorRefs>
                <descriptorRef>jar-with-dependencies</descriptorRef>
            </descriptorRefs>
        </configuration>
        <executions>
            <execution>
                <id>make-my-jar-with-dependencies</id>
                <phase>package</phase>
                <goals>
                    <goal>single</goal>
                </goals>
            </execution>
        </executions>
</plugin>
```


