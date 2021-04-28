package com.incountry.residence.sdk.http.mocks;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
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
    private final FakeHandler handler;

    public FakeHttpServer(String response, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        handler = new FakeHandler(response, responseCode);
        server.createContext("/", handler);

    }

    public FakeHttpServer(String response, int responseCode, int port, String path) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress("localhost", port), 0);
        handler = new FakeHandler(response, responseCode);
        server.createContext(path, handler);
    }

    public FakeHttpServer(List<String> responseList, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        handler = new FakeHandler(responseList, responseCode);
        server.createContext("/", handler);
    }

    public FakeHttpServer(String response, List<Integer> respCodeList, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        handler = new FakeHandler(response, respCodeList);
        server.createContext("/", handler);
    }

    public FakeHttpServer(String response, int responseCode, int port, int sleepTimeoutInSeconds) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        handler = new FakeHandler(response, responseCode, sleepTimeoutInSeconds);
        server.createContext("/", handler);
    }

    public Headers getLastRequestHeaders() {
        return handler.lastRequestHeaders;
    }

    public String getLastRequestBody() {
        return handler.lastRequestBody;
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
        Headers lastRequestHeaders;
        String lastRequestBody;

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
            lastRequestHeaders = exchange.getRequestHeaders();
            lastRequestBody = IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8.name());
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
            if (exchange.getRequestURI().toString().equals("/attachments/file_id")) {
                exchange.getResponseHeaders().set("Content-disposition", "attachment; filename*=UTF-8''filename.txt");
            }
            exchange.sendResponseHeaders(currentCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
