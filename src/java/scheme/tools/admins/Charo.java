package scheme.tools.admins;

import arc.Events;
import arc.util.Strings;
import mindustry.game.EventType;
import scheme.Main;
import scheme.ui.dialogs.AdminsConfigDialog;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Charo extends SlashJs {

    private static final long COMMAND_DELAY_MS = 250L;

    private final Object sendLock = new Object();
    private long lastCommandSentAt = 0L;
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

        new Thread(() -> {
            waitForCommandDelay();
            postConsoleWithFallback(host, js);
        }, "scheme-charo-console").start();
    }

    private void waitForCommandDelay() {
        synchronized (sendLock) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastCommandSentAt;
            if (elapsed < COMMAND_DELAY_MS) {
                try {
                    Thread.sleep(COMMAND_DELAY_MS - elapsed);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            lastCommandSentAt = System.currentTimeMillis();
        }
    }

    private String resolveHost() {
        if (serverHost == null || serverHost.isBlank()) return null;

        try {
            URI uri = new URI(serverHost.startsWith("http") ? serverHost : "https://" + serverHost);
            return uri.getHost();
        } catch (Exception ignored) {
            return null;
        }
    }

    private boolean postConsoleToUrl(String url, String command) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            String token = settings.getString("charotoken", "").trim();
            if (token.isEmpty() && AdminsConfigDialog.charoToken != null) {
                token = AdminsConfigDialog.charoToken.trim();
            }
            if (!token.isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + token);
            }
            connection.setDoOutput(true);

            String payload = "{\"command\": \"js " + quote(command) + "\"}";
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
            Main.error(e);
            return false;
        }
    }

    private void postConsoleWithFallback(String host, String command) {
        if (host == null || host.isBlank()) return;

        String ip = host;
        if (!isIpAddress(ip)) {
            try {
                ip = InetAddress.getByName(host).getHostAddress();
            } catch (Exception ignored) {
                return;
            }
        }

        if (postConsoleToUrl("http://" + ip + ":6569/api/console/run", command)) {
            return;
        }

        if ("134.255.232.108".equals(ip)) {
            postConsoleToUrl("https://console.charo.qzz.io/api/console/run", command);
        }
    }

    private boolean isIpAddress(String str) {
        if (str == null || str.isBlank()) return false;
        if (str.matches("^\\d+(\\.\\d+)*$")) return true;
        if (str.contains(":")) return true;
        return false;
    }

    private String quote(String value) {
        StringBuilder builder = new StringBuilder();
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
        return builder.toString();
    }
}