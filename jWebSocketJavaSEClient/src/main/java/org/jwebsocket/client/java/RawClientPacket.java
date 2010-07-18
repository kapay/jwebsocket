//  ---------------------------------------------------------------------------
//  jWebSocket - Raw Data Packet Implementation
//  Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.client.java;

import java.io.UnsupportedEncodingException;

import org.jwebsocket.api.WebSocketMessage;
import org.jwebsocket.api.WebSocketPacket;

/**
 * Implements the low level data packets which are interchanged between
 * client and server. Data packets do not have a special format at this
 * communication level.
 * @author puran
 * @version $Id:$
 */
public class RawClientPacket implements WebSocketPacket {
    public static final int FRAMETYPE_UTF8 = 0;
    public static final int FRAMETYPE_BINARY = 1;

    private byte[] data = null;
    private int frameType = FRAMETYPE_UTF8;
    
    private WebSocketMessage message;
    /**
     * Base constructor 
     * @param socketMessage the low level socket message
     */
    public RawClientPacket(WebSocketMessage socketMessage) {
       this.message = socketMessage; 
       setString(message.getText());
       setByteArray(message.getByteData());
    }

    @Override
    public void setByteArray(byte[] aByteArray) {
      data = aByteArray;
    }

    @Override
    public void setString(String aString) {
      data = aString.getBytes();
    }

    @Override
    public void setString(String aString, String aEncoding)
      throws UnsupportedEncodingException {
      data = aString.getBytes(aEncoding);
    }

    @Override
    public void setUTF8(String aString) {
      try {
        data = aString.getBytes("UTF-8");
      } catch (UnsupportedEncodingException ex) {
        // ignore exception here
      }
    }

    @Override
    public void setASCII(String aString) {
      try {
        data = aString.getBytes("US-ASCII");
      } catch (UnsupportedEncodingException ex) {
        // ignore exception here
      }
    }

    @Override
    public byte[] getByteArray() {
      return data;
    }

    @Override
    public String getString() {
      return new String(data);
    }

    @Override
    public String getString(String aEncoding)
      throws UnsupportedEncodingException {
      return new String(data, aEncoding);
    }

    @Override
    public String getUTF8() {
      try {
        return new String(data, "UTF-8");
      } catch (UnsupportedEncodingException ex) {
        return null;
      }
    }

    @Override
    public String getASCII() {
      try {
        return new String(data, "US-ASCII");
      } catch (UnsupportedEncodingException ex) {
        return null;
      }
    }

    /**
     * @return the frameType
     */
    @Override
    public int getFrameType() {
      return frameType;
    }

    /**
     * @param frameType the frameType to set
     */
    @Override
    public void setFrameType(int aFrameType) {
      this.frameType = aFrameType;
    }

}
