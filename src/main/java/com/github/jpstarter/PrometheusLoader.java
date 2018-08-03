package com.github.jpstarter;

import com.github.jpstarter.utils.HttpClient;
import com.github.jpstarter.utils.NetUtils;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by chenjunlong on 2018/7/31.
 */
public class PrometheusLoader {

    private static Logger logger = LoggerFactory.getLogger(PrometheusLoader.class);

    private final String FILE_NAME = "java-prometheus-starter.properties";
    private final String HOST;
    private final int PORT;
    private final String APP_NAME;
    private final String PROMETHEUS_REGISTER_URL;

    public PrometheusLoader() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(FILE_NAME);
        if (null == in) {
            throw new FileNotFoundException("classpath not found" + FILE_NAME);
        }
        Properties properties = new Properties();
        properties.load(in);

        String port = properties.getProperty("port");
        PORT = (null == port || "".equals(port)) ? 1234 : Integer.parseInt(port);
        APP_NAME = properties.getProperty("app.name");
        if (null == APP_NAME || "".equals(APP_NAME)) {
            throw new IllegalArgumentException("app.name is empty");
        }
        PROMETHEUS_REGISTER_URL = properties.getProperty("reload.register.url");
        if (null == PROMETHEUS_REGISTER_URL || "".equals(PROMETHEUS_REGISTER_URL)) {
            throw new IllegalArgumentException("reload.register.url is empty");
        }
        HOST = NetUtils.getLocalHost();
        register("http://" + PROMETHEUS_REGISTER_URL + "/register");
    }

    private void register(String registerURL) throws IOException {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("appName", APP_NAME);
        requestParams.put("host", HOST);
        requestParams.put("port", String.valueOf(PORT));
        String resp = HttpClient.doPost(registerURL, requestParams);
        logger.info("register resp:{}", resp);
    }

    public void start() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(HOST, PORT);
        HTTPServer httpServer = new HTTPServer(addr, CollectorRegistry.defaultRegistry, true);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                httpServer.stop();
                logger.info("PrometheusLoader stop complete ...");
            }
        }));
        DefaultExports.initialize();
        logger.info("PrometheusLoader started complete ...");
    }

    public static void initialize() {
        try {
            new PrometheusLoader().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
