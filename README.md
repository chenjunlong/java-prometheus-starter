# java-prometheus-starter
prometheus registered client

``` 
<dependency>
  <groupId>com.github.chenjunlong</groupId>
  <artifactId>java-prometheus-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

增加一个java-prometheus-starter.properties配置文件 
```
#暴露给Prometheus的端口，
port=8001
#job_name
app.name=spring-boot-example
#java-prometheus-reload暴露的接口地址
reload.register.url=10.10.21.15:9091
```

在主函数启动时加载PrometheusLoader
```
public static void main(String[] args) throws Exception {
    PrometheusLoader.initialize();
    SpringApplication.run(AnnotationRateLimitBootStrapApplication.class, args);
}
```
