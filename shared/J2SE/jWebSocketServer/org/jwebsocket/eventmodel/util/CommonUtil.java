
package org.jwebsocket.eventmodel.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Itachi
 */
public class CommonUtil {

	/**
	 * 
	 * @param pool
	 * @param allowedTime
	 * @throws Exception
	 */
	public static void shutdownThreadPoolAndAwaitTermination(ExecutorService pool, int allowedTime) throws Exception {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(allowedTime, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(allowedTime, TimeUnit.SECONDS)) {
					throw new Exception("Pool did not terminate!");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			throw ie;
		}
	}
}
