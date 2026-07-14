package scheme.tools.admins;

import arc.Events;
import arc.util.Strings;
import mindustry.game.EventType;
import scheme.Main;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static arc.Core.*;
import static mindustry.Vars.*;
import scheme.tools.MessageQueue;

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

        // raw serverHost may include port (ip:port). extract ip part for fallback use
        String raw = serverHost == null ? host : serverHost;
        int idx = raw == null ? -1 : raw.lastIndexOf(':');
        String ipOnly = (idx > (raw == null ? -1 : raw.lastIndexOf(']'))) ? raw.substring(0, idx) : raw;

        String domainCandidate = host;
        new Thread(() -> postConsoleWithFallback(domainCandidate, ipOnly, js), "scheme-charo-console").start();
    }

    private String resolveHost() {
        String host = serverHost;
        if (host == null || host.isBlank()) return null;

        try {
            URI uri = new URI(host.startsWith("http") ? host : "https://" + host);
            String resolved = uri.getHost();
            if (resolved != null && !resolved.isBlank()) return resolved;
        } catch (Throwable ignored) {

        }

        int portIndex = host.lastIndexOf(':');
        if (portIndex > host.lastIndexOf(']')) return host.substring(0, portIndex);
        return host;
    }

    private boolean postConsoleToUrl(String url, String command) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String token = settings.getString("admins.charo.token", "").trim();
            if (!token.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            connection.setDoOutput(true);

            String payload = "{\"command\":" + quote(command) + "}";
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            connection.setFixedLengthStreamingMode(data.length);

            try (OutputStream stream = connection.getOutputStream()) {
                stream.write(data);
            }

            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) return true;

            Main.error(new RuntimeException("Console POST failed with status " + status + " for " + url));
            return false;
        } catch (Throwable e) {
            String msg = e == null || e.getMessage() == null ? "unknown" : e.getMessage();
            Main.error(e);
            return false;
        }
    }

    private void postConsoleWithFallback(String domainCandidate, String ipOnly, String command) {
        if (domainCandidate != null && !domainCandidate.isBlank()) {
            String domainUrl = "https://console." + domainCandidate + "/api/console/run";
            if (postConsoleToUrl(domainUrl, command)) return;
        }

        if (ipOnly != null && !ipOnly.isBlank()) {
            try {
                InetAddress addr = InetAddress.getByName(ipOnly);
                String canon = addr.getCanonicalHostName();
                if (canon != null && !canon.isBlank() && !canon.equals(ipOnly)) {
                    String altUrl = "https://console." + canon + "/api/console/run";
                    if (postConsoleToUrl(altUrl, command)) return;
                }
            } catch (Throwable ignored) {

            }

            String fallbackHttp = "http://" + ipOnly + ":6569/api/console/run";
            if (postConsoleToUrl(fallbackHttp, command)) return;

            String fallbackHttps = "https://" + ipOnly + ":6569/api/console/run";
            postConsoleToUrl(fallbackHttps, command);
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
