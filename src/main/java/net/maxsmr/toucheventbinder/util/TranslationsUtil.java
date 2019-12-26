package net.maxsmr.toucheventbinder.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

public class TranslationsUtil {

    @NotNull
    private final ResourceBundle resourceBundle;

    private static TranslationsUtil sInstance;

    private TranslationsUtil() {
        resourceBundle = ResourceBundle.getBundle("strings", Locale.getDefault());
    }

    public static synchronized TranslationsUtil getInstance() {
        synchronized (TranslationsUtil.class) {
            if (sInstance == null) {
                sInstance = new TranslationsUtil();
            }
            return sInstance;
        }
    }

    @NotNull
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public String getString(@NotNull String key) {
        return resourceBundle.getString(key);
    }

    public String formatString(@NotNull String key, Object... args) {
        return String.format(getString(key), args);
    }

}
