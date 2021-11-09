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