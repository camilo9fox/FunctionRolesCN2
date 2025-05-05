package com.function.events;

import com.function.model.Rol;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class EventGridPublisher {
    private final String topicEndpoint;
    private final String accessKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private static final Logger LOGGER = Logger.getLogger(EventGridPublisher.class.getName());

    public EventGridPublisher() {
        this.topicEndpoint = "https://cn2-g7-topic.eastus2-1.eventgrid.azure.net/api/events";
        this.accessKey = "2nwi65UeMUy64n7PO4ERmuauQaJLyFffT4xwO8Yu529H8QSxfjf8JQQJ99BEACHYHv6XJ3w3AAABAZEGcgVF";

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        out.value(value != null ? formatter.format(value) : null);
                    }

                    @Override
                    public LocalDateTime read(JsonReader in) throws IOException {
                        String datetime = in.nextString();
                        return datetime != null ? LocalDateTime.parse(datetime, formatter) : null;
                    }
                })
                .create();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void publishRoleCreated(Rol rol) {
        publishEvent("RoleCreated", new RoleEvent(rol));
    }

    public void publishRoleUpdated(Rol rol) {
        publishEvent("RoleUpdated", new RoleEvent(rol));
    }

    public void publishRoleDeleted(Long roleId) {
        publishEvent("RoleDeleted", new RoleEvent(roleId));
    }

    public void publishRoleRetrieved(Rol rol) {
        publishEvent("RoleRetrieved", new RoleEvent(rol));
    }

    public void publishRolesRetrieved(List<Rol> roles) {
        publishEvent("RolesRetrieved", new RolesRetrievedEvent(roles));
    }

    private void publishEvent(String eventType, Object data) {
        Map<String, Object> event = new HashMap<>();
        event.put("id", UUID.randomUUID().toString());
        event.put("subject", eventType);
        event.put("eventType", "com.function.roles." + eventType);
        event.put("data", data);
        event.put("eventTime", OffsetDateTime.now().toString());
        event.put("dataVersion", "1.0");
        event.put("topic", "");

        try {
            String jsonBody = gson.toJson(new Object[] { event });

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(topicEndpoint))
                    .header("aeg-sas-key", accessKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                LOGGER.log(Level.SEVERE, "Error al publicar evento. Status: {0}, Body: {1}",
                        new Object[] { response.statusCode(), response.body() });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Interrupción al publicar evento", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al publicar evento", e);
        }
    }
}
