package betterbox.mine.game.betterranks;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PluginLogger {

    private final String prefix;
    private final Logger logger;

    public PluginLogger(BetterRanks plugin) {
        this.logger = plugin.getLogger();
        this.prefix = "[" + plugin.getName() + "]";
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
        // Możesz dodać warunek, aby logować komunikaty debugowania tylko wtedy, gdy jest włączony tryb debugowania.
        log(Level.INFO, "[DEBUG] " + message);
    }

    private void log(Level level, String message) {
        logger.log(level, prefix + " " + message);
    }
}
