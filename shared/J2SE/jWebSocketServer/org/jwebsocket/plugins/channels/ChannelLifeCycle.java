package org.jwebsocket.plugins.channels;

public interface ChannelLifeCycle {
    
  void init();

  void start();
  
  void suspend();

  void stop();
}
