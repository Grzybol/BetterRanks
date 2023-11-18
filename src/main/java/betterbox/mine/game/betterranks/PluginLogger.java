package betterbox.mine.game.betterranks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class PluginLogger {

    private final Logger logger;
    private FileHandler fileHandler;
    public boolean debugBool = false;
    public void debugBboolStateChange(){
        debugBool=!debugBool;
    }

    public PluginLogger(BetterRanks plugin, String folderPath) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date date = new Date();
        this.logger = Logger.getLogger(plugin.getName());
        this.logger.setUseParentHandlers(false);
        createLogDirectory(folderPath);

        try {
            String logFilePath = folderPath + "/logs/" + plugin.getName() + "_"+formatter.format(date)+".log";
            fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new CustomFormatter(plugin.getName()));
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLogDirectory(String folderPath) {
        File logDir = new File(folderPath, "logs");
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
        log(Level.CONFIG, "[DEBUG] " + message);
    }

    private void log(Level level, String message) {
        if (level != Level.CONFIG || debugBool)
        logger.log(level, "[" + level + "] " + message);
    }

    static class CustomFormatter extends Formatter {
        private final String pluginName;

        public CustomFormatter(String pluginName) {
            this.pluginName = pluginName;
        }

        @Override
        public String format(LogRecord record) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(record.getMillis()));
            return "[" + pluginName + "][" + timeStamp + "] " + formatMessage(record) + "\n";
        }
    }
}
