package org.jwebsocket.plugins.channel;

public interface ChannelLifeCycle {
  void init();

  void start();

  void stop();
}
