package Tools;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MXLogger {


    private int errorNum;
    private int warningNum;
    private Logger logger;

    public MXLogger(Level level) {
        logger = Logger.getLogger("MxLogger");
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LogFormatter());
        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);

        errorNum = 0;
        warningNum = 0;
    }



    public void severe(String msg, Location location) {
        errorNum += 1;
        logger.severe("Error at " + location.toString() + " with message: "+ msg);
    }

    public void severe(String msg) {
        errorNum += 1;
        logger.severe("Error with message: "+ msg);
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
    }

    public void warning(String msg, Location location) {
        warningNum += 1;
        logger.warning("Warning at " + location.toString() + " with message: "+ msg);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void fine(String msg) {
        logger.fine(msg);
    }

    public void fine(String msg, Location location) {
        logger.fine(msg + " At " + location.toString());
    }

    public void finer(String msg) {
        logger.finer(msg);
    }

    public void finest(String msg) {
        logger.finest(msg);
    }

    public int getErrorNum() {
        return errorNum;
    }

}
