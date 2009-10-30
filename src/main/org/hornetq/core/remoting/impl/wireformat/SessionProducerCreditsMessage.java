/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.core.remoting.impl.wireformat;

import org.hornetq.core.remoting.spi.HornetQBuffer;
import org.hornetq.utils.DataConstants;
import org.hornetq.utils.SimpleString;

/**
 * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
 * 
 */
public class SessionProducerCreditsMessage extends PacketImpl
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   private int credits;
   
   private SimpleString address;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public SessionProducerCreditsMessage(final int credits, final SimpleString address)
   {
      super(SESS_PRODUCER_CREDITS);

      this.credits = credits;
      
      this.address = address;
   }

   public SessionProducerCreditsMessage()
   {
      super(SESS_PRODUCER_CREDITS);
   }

   // Public --------------------------------------------------------

   public int getCredits()
   {
      return credits;
   }
   
   public SimpleString getAddress()
   {
      return address;
   }
   
//   public boolean isRequiresConfirmations()
//   {
//      return false;
//   }

   @Override
   public void encodeBody(final HornetQBuffer buffer)
   {
      buffer.writeInt(credits);
      buffer.writeSimpleString(address);
   }

   @Override
   public void decodeBody(final HornetQBuffer buffer)
   {
      credits = buffer.readInt();
      address = buffer.readSimpleString();
   }

   public int getRequiredBufferSize()
   {
      int size = BASIC_PACKET_SIZE + DataConstants.SIZE_INT + SimpleString.sizeofString(address);

      return size;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
