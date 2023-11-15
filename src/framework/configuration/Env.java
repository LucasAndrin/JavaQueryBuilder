package framework.configuration;

import io.github.cdimascio.dotenv.Dotenv;

public class Env {

    private static Dotenv instance;

    private static Dotenv getInstance() {
        if (instance == null) {
            refresh();
        }
        return instance;
    }

    public static void refresh() {
        instance = Dotenv.load();
    }

    public static String get(String key) {
        Dotenv env = getInstance();
        return env.get(key);
    }

    public static String get(String key, String defaultValue) {
        Dotenv env = getInstance();
        return env.get(key, defaultValue);
    }
}
