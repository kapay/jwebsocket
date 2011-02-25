//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.eventmodel.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.eventmodel.api.IWebSocketSecureObject;
import org.jwebsocket.eventmodel.exception.InvalidExecutionTime;
import org.jwebsocket.eventmodel.exception.NotAuthorizedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

/**
 *
 * @author kyberneees
 */
public class CommonUtil {

	/**
	 * Takes a specific IP address and a range using the IP/Netmask (e.g. 192.168.1.0/24 or 202.24.0.0/14).
	 *
	 * @param ipAddress the address to check
	 * @param ipAddressRange the range of addresses that most contain the IP address
	 * @return true if the IP address is in the range of addresses.
	 */
	public static boolean isIpAddressInRange(String ipAddress, String ipAddressRange) {
		int nMaskBits = 0;

		if (ipAddress.indexOf('/') > 0) {
			String[] addressAndMask = StringUtils.split(ipAddress, "/");
			ipAddress = addressAndMask[0];
			nMaskBits = Integer.parseInt(addressAndMask[1]);
		}

		InetAddress requiredAddress = parseAddress(ipAddressRange);
		InetAddress remoteAddress = parseAddress(ipAddress);

		if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
			throw new IllegalArgumentException("IP Address in expression must be the same type as "
					+ "version returned by request!");
		}

		if (nMaskBits == 0) {
			return remoteAddress.equals(requiredAddress);
		}

		byte[] remAddr = remoteAddress.getAddress();
		byte[] reqAddr = requiredAddress.getAddress();

		int oddBits = nMaskBits % 8;
		int nMaskBytes = nMaskBits / 8 + (oddBits == 0 ? 0 : 1);
		byte[] mask = new byte[nMaskBytes];

		Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte) 0xFF);

		if (oddBits != 0) {
			int finalByte = (1 << oddBits) - 1;
			finalByte <<= 8 - oddBits;
			mask[mask.length - 1] = (byte) finalByte;
		}

		for (int i = 0; i < mask.length; i++) {
			if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
				return false;
			}
		}

		return true;
	}

	private static InetAddress parseAddress(String address) {
		try {
			return InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Failed to parse the address '" + address + "'!", e);
		}
	}

	/**
	 * Shutdown a ThreadPool and await for termination
	 *
	 * @param pool the ThreadPool
	 * @param allowedTime the time to await for termination
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
				throw new InvalidExecutionTime("Threads uses more time for execution than allowed!");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
			throw ie;
		}
	}

	public static void checkSecurityRestrictions(IWebSocketSecureObject secureObject, WebSocketConnector aConnector) throws Exception {
		//Leaving if the security checks are not enabled
		if (!secureObject.isSecurityEnabled()) {
			return;
		}

		//NOT '!' operator flag
		boolean exclusion = false;

		//Temporal variables
		Iterator<String> iterator;
		String value;
		boolean role_authorized = false, ip_authorized = false, 
				user_authorized = false, user_match = false, stop = false;

		//Processing ip addresses restrictions
		if (secureObject.getIpAddresses().size() > 0) {
			iterator = secureObject.getIpAddresses().iterator();
			while (iterator.hasNext()) {
				value = iterator.next();
				
				if (!value.equals("all")) {
					exclusion = (value.startsWith("!")) ? true : false;
					value = (exclusion) ? value.substring(1) : value;

					if (CommonUtil.isIpAddressInRange(aConnector.getRemoteHost().getHostAddress(), value)) {
						ip_authorized = (exclusion) ? false : true;
						break;
					}
				} else {
					ip_authorized = true;
					break;
				}
			}
			if (!ip_authorized) {
				throw new NotAuthorizedException("Your IP address '"
						+ aConnector.getRemoteHost().getHostAddress()
						+ "' is not authorized to execute the operation!");
			}
		}

		//Processing users
		if (secureObject.getUsers().size() > 0) {
			if (null != SecurityContextHolder.getContext().getAuthentication()
					&& SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
				//Getting the username
				Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
				String username;
				if (principal instanceof UserDetails) {
					username = ((UserDetails) principal).getUsername();
				} else {
					username = principal.toString();
				}

				iterator = secureObject.getUsers().iterator();
				while (iterator.hasNext()) {
					value = iterator.next();	//Required USER
					
					if (!value.equals("all")) {
						exclusion = (value.startsWith("!")) ? true : false;
						value = (exclusion) ? value.substring(1) : value;

						if (value.equals(username)) {
							user_match = true;
							if (!exclusion) {
								user_authorized = true;
								break;
							}
						}
					} else {
						user_match = true;
						user_authorized = true;
						break;
					}
				}
			}
			//Not authorized!
			if (!user_authorized && user_match || secureObject.getRoles().isEmpty()) {
				throw new NotAuthorizedException("Invalid credentials to execute the operation!");
			}
		}

		//Processing roles restrictions
		if (secureObject.getRoles().size() > 0) {
			if (null != SecurityContextHolder.getContext().getAuthentication()
					&& SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
				Collection<GrantedAuthority> connector_roles = SecurityContextHolder.getContext().
						getAuthentication().getAuthorities();

				Iterator<GrantedAuthority> user_roles = connector_roles.iterator();
				GrantedAuthority role;
				while (user_roles.hasNext()) {
					//Required ROLES iteration
					iterator = secureObject.getRoles().iterator();
					while (iterator.hasNext() && !stop) {
						role = user_roles.next();	//User ROLE
						value = iterator.next();	//Required ROLE
						
						if (!value.equals("all")) {
							exclusion = (value.startsWith("!")) ? true : false;
							value = (exclusion) ? value.substring(1) : value;
							
							if (value.equals(role.getAuthority())) {
								if (!exclusion) {
									role_authorized = true; //Authorized!
								}
								stop = true;
							}
						} else {
							role_authorized = true;
							stop = true;
						}
					}
					if (stop) {
						break;
					}
				}
			}
			//Not authorized!
			if (!role_authorized) {
				throw new NotAuthorizedException("Invalid credentials to execute the operation!");
			}
		}
	}
}
