/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.handler.codec.memcache.binary;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FullMemcacheMessageRequestTest {

    private EmbeddedChannel channel;

    @Before
    public void setup() {
        channel = new EmbeddedChannel(
                new BinaryMemcacheRequestEncoder(),
                new BinaryMemcacheRequestDecoder(),
                new BinaryMemcacheObjectAggregator(1024));
    }

    @After
    public void teardown() {
        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeDecode() {
        ByteBuf key = Unpooled.wrappedBuffer("key".getBytes(CharsetUtil.UTF_8));
        ByteBuf content = Unpooled.wrappedBuffer("content".getBytes(CharsetUtil.UTF_8));
        ByteBuf extras = Unpooled.wrappedBuffer("extras".getBytes(CharsetUtil.UTF_8));
        FullBinaryMemcacheRequest req = new DefaultFullBinaryMemcacheRequest(key, extras, content);
        assertTrue(channel.writeOutbound(req));
        // header + content
        assertEquals(2, channel.outboundMessages().size());
        assertTrue(channel.writeInbound(channel.readOutbound(), channel.readOutbound()));

        FullBinaryMemcacheRequest read = channel.readInbound();
        assertEquals("key", read.key().toString(CharsetUtil.UTF_8));
        assertEquals("content", read.content().toString(CharsetUtil.UTF_8));
        assertEquals("extras", read.extras().toString(CharsetUtil.UTF_8));
        read.release();
    }
}
