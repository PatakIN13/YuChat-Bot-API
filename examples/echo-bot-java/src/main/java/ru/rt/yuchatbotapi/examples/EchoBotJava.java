package ru.rt.yuchatbotapi.examples;

import ru.rt.yuchatbotapi.java.YuChatBotJavaClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Пример echo-бота на Java.
 *
 * Настройка: скопируйте bot.properties.example в bot.properties и укажите токен бота.
 */
public class EchoBotJava {

    private static final Properties props = new Properties();

    static {
        File f = new File("bot.properties");
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Warning: could not read bot.properties: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String token = env("YUCHAT_BOT_TOKEN", "yuchat.bot.token");
        if (token == null) {
            System.err.println("Set YUCHAT_BOT_TOKEN env or yuchat.bot.token in bot.properties");
            System.exit(1);
        }

        String baseUrl = env("YUCHAT_BASE_URL", "yuchat.base.url");
        if (baseUrl == null) baseUrl = "https://yuchat.ai";

        YuChatBotJavaClient bot = new YuChatBotJavaClient(token, baseUrl, 3, 1000L);

        System.out.println("Echo bot (Java) started. Polling for updates...");

        // Простой polling-цикл через Java API
        long offset = 0;
        while (true) {
            try {
                var updates = bot.updates.getUpdates(offset, 100).join();

                for (var update : updates) {
                    if (update.getNewChatMessage() != null) {
                        var msg = update.getNewChatMessage();
                        System.out.println("Received: " + msg.getText());

                        bot.messages.send(
                            msg.getWorkspaceId(),
                            msg.getChatId(),
                            "Echo: " + msg.getText()
                        ).join();
                    }
                    offset = update.getUpdateId() + 1;
                }

                Thread.sleep(500);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private static String env(String name, String prop) {
        String val_ = System.getenv(name);
        if (val_ != null) return val_;
        return props.getProperty(prop);
    }
}
