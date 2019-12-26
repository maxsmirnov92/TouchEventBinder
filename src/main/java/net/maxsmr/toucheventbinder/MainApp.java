package net.maxsmr.toucheventbinder;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import javafx.application.Application;
import javafx.stage.Stage;
import net.maxsmr.commonutils.logger.BaseLogger;
import net.maxsmr.commonutils.logger.holder.BaseLoggerHolder;
import net.maxsmr.toucheventbinder.util.Slf4Logger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    static {
        BaseLoggerHolder.initInstance(() -> new BaseLoggerHolder(true) {

            @Override
            protected BaseLogger createLogger(@NotNull String className) {
                if (BuildConfig.IS_LOGGING_ENABLED) {
                    return new Slf4Logger(LoggerFactory.getLogger(className));
                } else {
                    return null;
                }
            }
        });
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainAppDelegate.INSTANCE.start(stage);
    }

    @Override
    public void stop() throws Exception {
        MainAppDelegate.INSTANCE.stop();
        super.stop();
    }

    private static boolean checkIfRunning() {
        boolean alreadyRunning;

        try {
            JUnique.acquireLock(BuildConfig.APP_ID);
            alreadyRunning = false;
        } catch (AlreadyLockedException e) {
            alreadyRunning = true;
        }

        return alreadyRunning;
    }

    public static void main(String[] args) {
        if (checkIfRunning()) {
            System.exit(0);
            return;
        }
        launch(args);
    }
}
