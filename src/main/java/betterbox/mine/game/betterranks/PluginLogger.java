package betterbox.mine.game.betterranks;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class PluginLogger {

    private final Logger logger;
    private FileHandler fileHandler;

    public PluginLogger(BetterRanks plugin) {
        this.logger = Logger.getLogger(plugin.getName());
        this.logger.setUseParentHandlers(false);
        createLogDirectory(plugin);

        try {
            String logFilePath = plugin.getDataFolder().getAbsolutePath() + "/logs/" + plugin.getName() + ".log";
            fileHandler = new FileHandler(logFilePath, true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLogDirectory(BetterRanks plugin) {
        File logDir = new File(plugin.getDataFolder(), "logs");
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warn(String message) {
        log(Level.WARNING, message);
    }

    public void severe(String message) {
        log(Level.SEVERE, message);
    }

    public void debug(String message) {
        log(Level.INFO, "[DEBUG] " + message);
    }

    private void log(Level level, String message) {
        logger.log(level, message);
    }
}
