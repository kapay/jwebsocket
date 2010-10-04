package org.jwebsocket.client.plugins.rpc;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RPCCallable {
}
