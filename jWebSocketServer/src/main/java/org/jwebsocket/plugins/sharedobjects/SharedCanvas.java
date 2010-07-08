/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.plugins.sharedobjects;

import javolution.util.FastList;

/**
 *
 * @author aschulze
 */
public class SharedCanvas extends BaseSharedObject {

	private FastList cmds = new FastList();

	public void addCmd(String aCmd) {
		 cmds.add(aCmd);
	}

	public void removeCmd(int aIndex) {
		 cmds.remove(aIndex);
	}
}
