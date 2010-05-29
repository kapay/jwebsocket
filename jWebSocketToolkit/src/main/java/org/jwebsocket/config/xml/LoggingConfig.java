package org.jwebsocket.config.xml;

import org.jwebsocket.config.Config;
import org.jwebsocket.kit.WebSocketRuntimeException;

/**
 * Configuration for logging
 * User: puran
 *
 * @version $Id:$
 */
public class LoggingConfig implements Config {

    private final String appender;
    private final String pattern;
    private final String level;
    private final String filename;

    /**
     * Costrutor
     *
     * @param appender the logging appender
     * @param pattern  logging pattern
     * @param level    the level of logging
     * @param filename the log file name
     */
    public LoggingConfig(String appender, String pattern, String level, String filename) {
        this.appender = appender;
        this.pattern = pattern;
        this.level = level;
        this.filename = filename;
    }

    public String getAppender() {
        return appender;
    }

    public String getPattern() {
        return pattern;
    }

    public String getLevel() {
        return level;
    }

    public String getFilename() {
        return filename;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() {
        if ((appender != null && appender.length() > 0)
                && (pattern != null && pattern.length() > 0)
                && (level != null && level.length() > 0)
                && (filename != null && filename.length() > 0)) {
            return;
        }
        throw new WebSocketRuntimeException(
                "Missing one of the engine configuration, please check your configuration file");
    }
}
