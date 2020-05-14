package com.incountry.residence.sdk.http.mocks;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class FakeHttpServer {
    private HttpServer server;

    public FakeHttpServer(String response, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(response, responseCode));
    }

    public FakeHttpServer(List<String> responseList, int responseCode, int port) throws IOException {
        server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/", new FakeHandler(responseList, responseCode));
    }

    public void start() {
        server.start();
    }

    public void stop(int delay) {
        server.stop(delay);
    }

    private static class FakeHandler implements HttpHandler {
        LinkedList<String> responseList;
        String response;
        int responseCode;

        FakeHandler(String response, int responseCode) {
            this.response = response;
            this.responseCode = responseCode;
        }

        FakeHandler(List<String> responseList, int responseCode) {
            this.responseList = new LinkedList<>(responseList);
            this.responseCode = responseCode;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String currentResponse = null;
            if (response != null) {
                currentResponse = response;
            } else if (responseList != null && !responseList.isEmpty()) {
                currentResponse = responseList.getFirst();
                if (responseList.size() > 1) {
                    responseList.removeFirst();
                }
            }
            byte[] bytes = currentResponse != null ? currentResponse.getBytes(StandardCharsets.UTF_8) : new byte[0];
            exchange.sendResponseHeaders(responseCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}
