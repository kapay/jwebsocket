package org.jwebsocket.channel;

public interface ChannelLifeCycle {
  void init();

  void start();

  void stop();
}
