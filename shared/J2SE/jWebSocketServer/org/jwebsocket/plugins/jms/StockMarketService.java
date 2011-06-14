//	---------------------------------------------------------------------------
//	jWebSocket
//	Copyright (c) 2011, Innotrade GmbH - jWebSocket.org, Alexander Schulze
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.jms;

/**
 * 
 * @author Johannes Smutny
 */
import org.springframework.jms.core.support.JmsGatewaySupport;

public class StockMarketService extends JmsGatewaySupport implements
		JMSStockMarketDataProducer {

	public StockMarketService() {
		//this.getJmsTemplate().setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
		//this.getJmsTemplate().setSessionTransacted(false);
	}

	public void sendStockMarketData() {
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}

			getJmsTemplate().convertAndSend("test.queue", "company:ticker:price");
		}
	}
}
