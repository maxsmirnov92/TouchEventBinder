package net.maxsmr.toucheventbinder.util;

import javafx.application.Platform;
import net.maxsmr.tasksutils.runnable.WrappedRunnable;

public class RunLaterExceptionHandler implements WrappedRunnable.ExceptionHandler {

    @Override
    public void onRunnableCrash(Throwable throwable) {
        Platform.runLater(() -> {
            throw new RuntimeException("An exception occurred: " + throwable.getMessage(), throwable);
        });
    }
}
