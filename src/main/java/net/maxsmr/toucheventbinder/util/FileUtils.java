package net.maxsmr.toucheventbinder.util;

import net.maxsmr.commonutils.data.StringUtils;
import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static net.maxsmr.commonutils.data.StreamUtils.readStringsFromInputStream;
import static net.maxsmr.commonutils.data.SymbolConstKt.NEXT_LINE;

// duplicate methods from FileHelper due to Android classes usage
public class FileUtils {

    private final static BaseLogger logger = BaseLoggerHolder.getInstance().getLogger(FileUtils.class);

    public static boolean isFileCorrect(File file) {
        return isFileExists(file) && file.length() > 0;
    }

    public static boolean isFileExists(String fileName, String parentPath) {

        if (StringUtils.isEmpty(fileName) || fileName.contains("/")) {
            return false;
        }

        if (StringUtils.isEmpty(parentPath)) {
            return false;
        }

        File f = new File(parentPath, fileName);
        return f.exists() && f.isFile();
    }

    public static boolean isFileExists(File file) {
        return file != null && isFileExists(file.getAbsolutePath());
    }

    public static boolean isFileExists(String filePath) {
        if (!StringUtils.isEmpty(filePath)) {
            File f = new File(filePath);
            return (f.exists() && f.isFile());
        }
        return false;
    }


    @NotNull
    public static List<String> readStringsFromFile(File file) {

        List<String> lines = new ArrayList<>();

        if (!isFileCorrect(file)) {
            logger.e("incorrect file: " + file);
            return lines;
        }

        if (!file.canRead()) {
            logger.e("can't read from file: " + file);
            return lines;
        }

        try {
            return readStringsFromInputStream(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            logger.e("an IOException occurred", e);
            return lines;
        }
    }

    @Nullable
    public static String readStringFromFile(File file) {
        List<String> strings = readStringsFromFile(file);
        return !strings.isEmpty() ? StringUtils.join(NEXT_LINE, strings) : null;
    }
}
