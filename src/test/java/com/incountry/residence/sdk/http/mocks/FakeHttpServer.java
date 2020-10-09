package com.incountry.residence.sdk.http.mocks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class FakeHttpServer {

    private static final Logger LOG = LogManager.getLogger(FakeHttpServer.class);
    private final HttpServer server;

    public FakeHttpServer(String response, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(response, responseCode));
    }

    public FakeHttpServer(String response, int responseCode, int port, String path) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress("localhost", port), 0);
        server.createContext(path, new FakeHandler(response, responseCode));
    }

    public FakeHttpServer(List<String> responseList, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(responseList, responseCode));
    }

    public FakeHttpServer(String response, List<Integer> respCodeList, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(response, respCodeList));
    }

    public FakeHttpServer(String response, int responseCode, int port, int sleepTimeoutInSeconds) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(response, responseCode, sleepTimeoutInSeconds));
    }

    public void start() {
        server.start();
    }

    public void stop(int delay) {
        server.stop(delay);
    }

    private static class FakeHandler implements HttpHandler {
        LinkedList<Integer> respCodeList;
        LinkedList<String> responseList;
        String response;
        Integer responseCode;
        Integer sleepTimeout;

        FakeHandler(String response, int responseCode) {
            this.response = response;
            this.responseCode = responseCode;
        }

        FakeHandler(List<String> responseList, int responseCode) {
            this.responseList = new LinkedList<>(responseList);
            this.responseCode = responseCode;
        }

        FakeHandler(String response, List<Integer> respCodeList) {
            this.response = response;
            this.respCodeList = new LinkedList<>(respCodeList);
        }

        FakeHandler(String response, int responseCode, int sleepTimeoutInSeconds) {
            this.response = response;
            this.responseCode = responseCode;
            this.sleepTimeout = sleepTimeoutInSeconds;
        }

        @SuppressWarnings("java:S2925")
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (sleepTimeout != null) {
                try {
                    Thread.sleep(sleepTimeout * 1000);
                } catch (InterruptedException ex) {
                    LOG.error(ex.getMessage());
                }
            }
            String currentResponse = null;
            if (response != null) {
                currentResponse = response;
            } else if (responseList != null && !responseList.isEmpty()) {
                currentResponse = responseList.getFirst();
                if (responseList.size() > 1) {
                    responseList.removeFirst();
                }
            }
            Integer currentCode = 200;
            if (responseCode != null) {
                currentCode = responseCode;
            } else if (respCodeList != null && !respCodeList.isEmpty()) {
                currentCode = respCodeList.getFirst();
                if (respCodeList.size() > 1) {
                    respCodeList.removeFirst();
                }
            }
            byte[] bytes = currentResponse != null ? currentResponse.getBytes(StandardCharsets.UTF_8) : new byte[0];
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(currentCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
