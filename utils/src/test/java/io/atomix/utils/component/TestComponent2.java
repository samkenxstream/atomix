/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atomix.utils.component;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test component.
 */
@Component(TestComponent2Config.class)
public class TestComponent2 implements TestComponentInterface2, Managed<TestComponent2Config> {
  @Dependency
  private TestComponent3 component3;

  @Override
  public CompletableFuture<Void> start(TestComponent2Config config) {
    assertNotNull(component3);
    assertNotNull(config);
    assertEquals("2", config.getValue());
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> stop() {
    return CompletableFuture.completedFuture(null);
  }
}