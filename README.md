# java-prometheus-starter
prometheus consul registered client

``` 
<dependency>
  <groupId>com.github.chenjunlong</groupId>
  <artifactId>java-prometheus-starter</artifactId>
  <version>1.1</version>
</dependency>
```

增加一个java-prometheus-starter.properties配置文件 
```
#暴露给Prometheus的端口，
port=8001
#consul service id
app.id=1001
#consul service name
app.name=spring-boot-example
#consul service tags
app.tags=
#consul 地址
consul.host=xx.xx.xx.xx
#consul 端口
consul.port=8500
```

在主函数启动时加载PrometheusConsulRegister
```
public static void main(String[] args) throws Exception {
    PrometheusConsulRegister.initialize();
    SpringApplication.run(AnnotationRateLimitBootStrapApplication.class, args);
}
```
