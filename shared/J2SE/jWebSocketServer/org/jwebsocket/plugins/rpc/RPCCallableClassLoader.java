//	---------------------------------------------------------------------------
//	jWebSocket - RPC PlugIn
//	Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
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
package org.jwebsocket.plugins.rpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

/**
 * ...
 * @author Quentin Ambard
 */
public class RPCCallableClassLoader {

	private Class mRpcCallableClass;
	private RPCCallable mRpcCallableInstance;
	private Map<String, List<Method>> mMethods = new FastMap<String, List<Method>>();

	public RPCCallableClassLoader(Class aRpcCallableClass, RPCCallable aRpcCallableInstance) {
		this.mRpcCallableClass = aRpcCallableClass;
		this.mRpcCallableInstance = aRpcCallableInstance;
	}

	public void addMethod(String aMethodName, Method aMethod) {
		if (!mMethods.containsKey(aMethodName)) {
			mMethods.put(aMethodName, new ArrayList<Method>());
		}
		mMethods.get(aMethodName).add(aMethod);
	}

	public Class getRpcCallableClass() {
		return mRpcCallableClass;
	}

	public RPCCallable getRpcCallableInstanceGenerator() {
		return mRpcCallableInstance;
	}

	public boolean hasMethod(String aMethod) {
		return mMethods.containsKey(aMethod);
	}

	public List<Method> getMethods(String aMethodName) {
		return mMethods.get(aMethodName);
	}
}
