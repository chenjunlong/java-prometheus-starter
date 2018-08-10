package com.github.jpstarter;

import com.github.jpstarter.utils.NetUtils;
import com.google.common.net.HostAndPort;
import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import com.orbitz.consul.model.agent.ImmutableRegistration;
import com.orbitz.consul.model.agent.Registration;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * Created by chenjunlong on 2018/8/9.
 */
public class PrometheusConsulRegister {

    private static Logger logger = LoggerFactory.getLogger(PrometheusConsulRegister.class);

    private static boolean initialized = false;

    private final String FILE_NAME = "java-prometheus-starter.properties";
    private final String HOST;
    private final int PORT;
    private final String APP_ID;
    private final String APP_NAME;
    private String[] APP_TAGS = {};
    private final String CONSUL_HOST;
    private final int CONSUL_PORT;

    public PrometheusConsulRegister() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        if (null == in) {
            throw new FileNotFoundException("classpath not found" + FILE_NAME);
        }
        Properties properties = new Properties();
        properties.load(in);

        HOST = NetUtils.getLocalHost();

        String port = properties.getProperty("port");
        PORT = (null == port || "".equals(port)) ? 1234 : Integer.parseInt(port);

        APP_ID = properties.getProperty("app.id");
        if (null == APP_ID || "".equals(APP_ID)) {
            throw new IllegalArgumentException("app.id is empty");
        }

        APP_NAME = properties.getProperty("app.name");
        if (null == APP_NAME || "".equals(APP_NAME)) {
            throw new IllegalArgumentException("app.name is empty");
        }

        String appTags = properties.getProperty("app.tags");
        if (null != appTags || !"".equals(appTags)) {
            APP_TAGS = appTags.split(",");
        }

        CONSUL_HOST = properties.getProperty("consul.host");
        if (null == CONSUL_HOST || "".equals(CONSUL_HOST)) {
            throw new IllegalArgumentException("consul.host is empty");
        }

        String consulPort = properties.getProperty("consul.port");
        if (null == consulPort || "".equals(consulPort)) {
            throw new IllegalArgumentException("consul.port is empty");
        }
        CONSUL_PORT = Integer.parseInt(consulPort);
    }


    public void start() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(HOST, PORT);
        HTTPServer httpServer = new HTTPServer(addr, CollectorRegistry.defaultRegistry, true);
        DefaultExports.initialize();
        logger.info("PrometheusConsulRegister HTTPServer started ...");

        Consul consul = Consul.builder()
                .withHostAndPort(HostAndPort.fromParts(CONSUL_HOST, CONSUL_PORT))
                .build();
        AgentClient agentClient = consul.agentClient();
        String http = "http://" + HOST + ":" + PORT + "/metrics";
        Registration.RegCheck single = Registration.RegCheck.http(http, 20);
        ImmutableRegistration.Builder builder = ImmutableRegistration.builder()
                .check(single)
                .address(HOST)
                .port(PORT)
                .name(APP_NAME)
                .id(APP_ID);
        if (APP_TAGS.length > 0) {
            builder.addTags(APP_TAGS);
        }
        Registration reg = builder.build();
        agentClient.register(reg);
        logger.info("PrometheusConsulRegister Consul Registered ...");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                httpServer.stop();
                logger.info("PrometheusConsulRegister HTTPServer stop complete ...");

                agentClient.deregister(APP_ID);
                logger.info("PrometheusConsulRegister Consul deregister complete ...");
            }
        }));
    }

    public static synchronized void initialize() {
        try {
            if (!initialized) {
                new PrometheusConsulRegister().start();
                initialized = true;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
