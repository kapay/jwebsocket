package org.jwebsocket.api;

import java.util.List;

public interface EngineConfiguration extends Configuration {
    String getJar();
    int getPort();
    int getTimeout();
    int getMaxFrameSize();
    List<String> getAllowedDomains();
    
}
