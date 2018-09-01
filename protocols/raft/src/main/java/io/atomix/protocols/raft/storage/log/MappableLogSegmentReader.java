/*
 * Copyright 2018-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.protocols.raft.storage.log;

import io.atomix.protocols.raft.storage.log.index.RaftLogIndex;
import io.atomix.utils.serializer.Namespace;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Mappable log segment reader.
 */
class MappableLogSegmentReader<E> implements RaftLogReader<E> {
  private final RaftLogSegment<E> segment;
  private final FileChannel channel;
  private final int maxEntrySize;
  private final RaftLogIndex index;
  private final Namespace namespace;
  private RaftLogReader<E> reader;

  MappableLogSegmentReader(
      FileChannel channel,
      RaftLogSegment<E> segment,
      int maxEntrySize,
      RaftLogIndex index,
      Namespace namespace) {
    this.channel = channel;
    this.segment = segment;
    this.maxEntrySize = maxEntrySize;
    this.index = index;
    this.namespace = namespace;
    this.reader = new FileChannelLogSegmentReader<>(channel, segment, maxEntrySize, index, namespace);
  }

  /**
   * Converts the reader to a mapped reader using the given buffer.
   *
   * @param buffer the mapped buffer
   */
  void map(ByteBuffer buffer) {
    if (!(reader instanceof MappedLogSegmentReader)) {
      RaftLogReader<E> reader = this.reader;
      this.reader = new MappedLogSegmentReader<>(buffer, segment, maxEntrySize, index, namespace);
      this.reader.reset(reader.getNextIndex());
      reader.close();
    }
  }

  /**
   * Converts the reader to an unmapped reader.
   */
  void unmap() {
    if (reader instanceof MappedLogSegmentReader) {
      RaftLogReader<E> reader = this.reader;
      this.reader = new FileChannelLogSegmentReader<>(channel, segment, maxEntrySize, index, namespace);
      this.reader.reset(reader.getNextIndex());
      reader.close();
    }
  }

  @Override
  public long getCurrentIndex() {
    return reader.getCurrentIndex();
  }

  @Override
  public Indexed<E> getCurrentEntry() {
    return reader.getCurrentEntry();
  }

  @Override
  public long getNextIndex() {
    return reader.getNextIndex();
  }

  @Override
  public boolean hasNext() {
    return reader.hasNext();
  }

  @Override
  public Indexed<E> next() {
    return reader.next();
  }

  @Override
  public void reset() {
    reader.reset();
  }

  @Override
  public void reset(long index) {
    reader.reset(index);
  }

  @Override
  public void close() {
    reader.close();
    try {
      channel.close();
    } catch (IOException e) {
      throw new RaftIOException(e);
    } finally {
      segment.closeReader(this);
    }
  }
}