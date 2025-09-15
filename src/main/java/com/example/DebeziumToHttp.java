package com.example;

import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.format.Json;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class DebeziumToHttp {

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void startDebezium() {
        Properties props = new Properties();

        // Configuração do Debezium para MySQL
        props.setProperty("name", "mysql-connector");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "debezium");
        props.setProperty("database.password", "debeziumpw");
        props.setProperty("database.server.id", "184054");
        props.setProperty("database.server.name", "meu-mysql");
        props.setProperty("database.include.list", "testdb");
        props.setProperty("table.include.list", "testdb.users");
        props.setProperty("topic.prefix", "meuapp-mysql");
        props.setProperty("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory");
        props.setProperty("schema.history.internal.file.filename", "/tmp/schema-history.dat");
        props.setProperty("offset.storage.file.filename", "data/offsets.dat");

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
                .using(props)
                .notifying(event -> {
                    String payload = event.value();
                    System.out.println("Debezium EVENT: " + payload);

                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/logs"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(payload))
                            .build();

                    try {
                        client.send(request, HttpResponse.BodyHandlers.ofString());
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                })
                .build();

        // Inicia o engine em background
        executor.submit(() -> {
            try {
                engine.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}