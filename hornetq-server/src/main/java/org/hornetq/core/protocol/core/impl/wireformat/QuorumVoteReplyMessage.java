/*
 * Copyright 2005-2014 Red Hat, Inc.
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
package org.hornetq.core.protocol.core.impl.wireformat;

import org.hornetq.api.core.HornetQBuffer;
import org.hornetq.api.core.SimpleString;
import org.hornetq.core.protocol.core.impl.PacketImpl;
import org.hornetq.core.server.cluster.qourum.QuorumVoteHandler;
import org.hornetq.core.server.cluster.qourum.Vote;

public class QuorumVoteReplyMessage extends PacketImpl
{
   private SimpleString handler;
   private Vote vote;
   private HornetQBuffer voteBuffer;

   public QuorumVoteReplyMessage(SimpleString handler, Vote vote)
   {
      super(QUORUM_VOTE_REPLY);
      this.handler = handler;
      this.vote = vote;
   }

   public QuorumVoteReplyMessage()
   {
      super(QUORUM_VOTE_REPLY);
   }

   public Vote getVote()
   {
      return vote;
   }

   public SimpleString getHandler()
   {
      return handler;
   }

   @Override
   public boolean isResponse()
   {
      return true;
   }

   @Override
   public void encodeRest(HornetQBuffer buffer)
   {
      super.encodeRest(buffer);
      buffer.writeSimpleString(handler);
      vote.encode(buffer);
   }

   @Override
   public void decodeRest(HornetQBuffer buffer)
   {
      super.decodeRest(buffer);
      handler = buffer.readSimpleString();
      voteBuffer = buffer.readSlice(buffer.readableBytes());
   }

   public void decodeRest(QuorumVoteHandler voteHandler)
   {
      vote = voteHandler.decode(voteBuffer);
   }
}
