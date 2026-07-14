package scheme.tools.admins;

import arc.Events;
import arc.util.Strings;
import mindustry.game.EventType;
import scheme.Main;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static mindustry.Vars.*;

public class Charo extends SlashJs {

    private String serverHost;

    public Charo() {
        Events.on(EventType.ClientServerConnectEvent.class, event -> serverHost = event.ip + ":" + event.port);
    }

    @Override
    public String keyName() {
        return "charo";
    }

    @Override
    protected void send(String command, Object... args) {
        if (unusable()) return;

        String js = Strings.format(command, args);
        String host = resolveHost();
        if (host == null || host.isBlank()) {
            ui.showInfoFade("@admins.notavailable");
            return;
        }

        String url = "https://console." + host + "/api/console/run";
        new Thread(() -> postConsole(url, js), "scheme-charo-console").start();
    }

    private String resolveHost() {
        String host = serverHost;
        if (host == null || host.isBlank()) return null;

        try {
            URI uri = new URI(host.startsWith("http") ? host : "https://" + host);
            String resolved = uri.getHost();
            if (resolved != null && !resolved.isBlank()) return resolved;
        } catch (Throwable ignored) {
            // fall back to the raw value below
        }

        int portIndex = host.lastIndexOf(':');
        if (portIndex > host.lastIndexOf(']')) return host.substring(0, portIndex);
        return host;
    }

    private void postConsole(String url, String command) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            String payload = "{\"command\":" + quote(command) + "}";
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(data.length);

            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(data);
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                Main.error(new RuntimeException("Console POST failed with status " + status));
            }
        } catch (Throwable e) {
            Main.error(e);
        }
    }

    private String quote(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(ch);
            }
        }
        builder.append('"');
        return builder.toString();
    }
}
