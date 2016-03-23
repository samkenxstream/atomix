/*
 * Copyright 2015 the original author or authors.
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
package io.atomix.group;

import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.util.Assert;
import io.atomix.group.connection.Connection;
import io.atomix.group.tasks.MemberTaskQueue;
import io.atomix.group.tasks.TaskQueueController;
import io.atomix.group.util.Submitter;

/**
 * A {@link DistributedGroup} member representing a member of the group controlled by a local
 * or remote process.
 *
 * @author <a href="http://github.com/kuujo>Jordan Halterman</a>
 */
public class GroupMember {
  protected volatile long index;
  protected final String memberId;
  protected final Address address;
  protected final MembershipGroup group;
  private final GroupProperties properties;
  final TaskQueueController tasks;
  private final Connection connection;

  GroupMember(GroupMemberInfo info, MembershipGroup group, Submitter submitter) {
    this(info, group, submitter, null);
  }

  GroupMember(GroupMemberInfo info, MembershipGroup group, Submitter submitter, TaskQueueController tasks) {
    this.index = info.index();
    this.memberId = info.memberId();
    this.address = info.address();
    this.group = Assert.notNull(group, "group");
    this.properties = new GroupProperties(memberId, group);
    this.connection = new Connection(memberId, address, group.connections);
    if (tasks != null) {
      this.tasks = tasks;
    } else {
      this.tasks = new TaskQueueController(new MemberTaskQueue(memberId, group, submitter));
    }
  }

  /**
   * Returns the member ID.
   * <p>
   * The member ID is guaranteed to be unique across the cluster. Depending on how the member was
   * constructed, it may be a user-provided identifier or an automatically generated {@link java.util.UUID}.
   *
   * @return The member ID.
   */
  public String id() {
    return memberId;
  }

  /**
   * Returns the member address.
   *
   * @return The member address.
   */
  public Address address() {
    return address;
  }

  /**
   * Returns the member version.
   *
   * @return The member version.
   */
  public long version() {
    return index;
  }

  /**
   * Updates the member index.
   *
   * @param index The updated member index.
   * @return The group member.
   */
  GroupMember setIndex(long index) {
    this.index = index;
    return this;
  }

  /**
   * Returns the member properties.
   *
   * @return The member properties.
   */
  public GroupProperties properties() {
    return properties;
  }

  /**
   * Returns a direct connection to the member.
   *
   * @return A direct connection to the member.
   */
  public Connection connection() {
    return connection;
  }

  /**
   * Returns the member's task queue.
   *
   * @return The member's task queue.
   */
  public MemberTaskQueue tasks() {
    return (MemberTaskQueue) tasks.queue();
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof GroupMember && ((GroupMember) object).memberId.equals(memberId);
  }

  @Override
  public String toString() {
    return String.format("%s[id=%s, address=%s]", getClass().getSimpleName(), memberId, address);
  }

}
