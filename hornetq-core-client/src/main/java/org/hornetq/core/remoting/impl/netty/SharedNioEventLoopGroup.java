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
package org.hornetq.core.remoting.impl.netty;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;
import io.netty.util.concurrent.Promise;
import org.hornetq.core.client.impl.ClientSessionFactoryImpl;
import org.hornetq.utils.HornetQThreadFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="nmaurer@redhat.com">Norman Maurer</a>
 */
public class SharedNioEventLoopGroup extends NioEventLoopGroup
{
   private static SharedNioEventLoopGroup instance;

   private final AtomicReference<ScheduledFuture<?>> shutdown = new AtomicReference<ScheduledFuture<?>>();
   private final AtomicLong nioChannelFactoryCount = new AtomicLong();
   private final Promise<?> terminationPromise = ImmediateEventExecutor.INSTANCE.newPromise();

   private SharedNioEventLoopGroup(int numThreads, ThreadFactory factory)
   {
      super(numThreads, factory);
   }

   private static ClassLoader getThisClassLoader()
   {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return ClientSessionFactoryImpl.class.getClassLoader();
         }
      });
   }

   public static synchronized void forceShutdown()
   {
      if (instance != null)
      {
         instance.shutdown();
         instance.nioChannelFactoryCount.set(0);
         instance = null;
      }
   }

   public static synchronized SharedNioEventLoopGroup getInstance(int numThreads)
   {
      if (instance != null)
      {
         ScheduledFuture f = instance.shutdown.getAndSet(null);
         if (f != null)
         {
            f.cancel(false);
         }
      }
      else
      {
         instance = new SharedNioEventLoopGroup(numThreads, new HornetQThreadFactory("HornetQ-client-netty-threads", true, getThisClassLoader()));
      }
      instance.nioChannelFactoryCount.incrementAndGet();
      return instance;
   }

   @Override
   public Future<?> terminationFuture()
   {
      return terminationPromise;
   }

   @Override
   public Future<?> shutdownGracefully()
   {
      return shutdownGracefully(100, 3000, TimeUnit.MILLISECONDS);
   }

   @Override
   public Future<?> shutdownGracefully(final long l, final long l2, final TimeUnit timeUnit)
   {
      if (nioChannelFactoryCount.decrementAndGet() == 0)
      {
         shutdown.compareAndSet(null, next().scheduleAtFixedRate(new Runnable()
         {
            @Override
            public void run()
            {
               synchronized (SharedNioEventLoopGroup.class)
               {
                  if (shutdown.get() != null)
                  {
                     Future<?> future = SharedNioEventLoopGroup.super.shutdownGracefully(l, l2, timeUnit);
                     future.addListener(new FutureListener<Object>()
                     {
                        @Override
                        public void operationComplete(Future future) throws Exception
                        {
                           if (future.isSuccess())
                           {
                              terminationPromise.setSuccess(null);
                           }
                           else
                           {
                              terminationPromise.setFailure(future.cause());
                           }
                        }
                     });
                     instance = null;
                  }
               }
            }

         }, 10, 10, TimeUnit.SECONDS));
      }
      return terminationPromise;
   }
}
