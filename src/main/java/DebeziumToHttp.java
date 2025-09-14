import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.format.Json;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class DebeziumToHttp {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty("name", "mysql-connector");
        props.setProperty("connector.class", "io.debezium.connector.mysql.MySqlConnector");
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "3306");
        props.setProperty("database.user", "debezium");
        props.setProperty("database.password", "debeziumpw");
        props.setProperty("database.server.id", "184054");
        props.setProperty("database.server.name", "dbserver1");
        props.setProperty("database.include.list", "testdb");
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", "/tmp/dbhistory.dat");

        DebeziumEngine<ChangeEvent<String, String>> engine = DebeziumEngine.create(Json.class)
            .using(props)
            .notifying(record -> {
                sendToHttp(record.value());
            })
            .build();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                engine.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void sendToHttp(String json) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/logs"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}