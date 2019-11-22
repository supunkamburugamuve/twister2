//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.


package edu.iu.dsc.tws.tset.links.batch;

import edu.iu.dsc.tws.api.compute.OperationNames;
import edu.iu.dsc.tws.api.compute.graph.Edge;
import edu.iu.dsc.tws.tset.env.BatchTSetEnvironment;
import edu.iu.dsc.tws.tset.sets.batch.CachedTSet;
import edu.iu.dsc.tws.tset.sets.batch.PersistedTSet;

public class DirectTLink<T> extends BatchIteratorLink<T> {

  public DirectTLink(BatchTSetEnvironment tSetEnv, int sourceParallelism) {
    super(tSetEnv, "direct", sourceParallelism);
  }

  public DirectTLink(BatchTSetEnvironment tSetEnv, String name, int sourceParallelism) {
    super(tSetEnv, name, sourceParallelism);
  }

  @Override
  public DirectTLink<T> setName(String name) {
    rename(name);
    return this;
  }

  @Override
  public Edge getEdge() {
    return new Edge(getId(), OperationNames.DIRECT, getMessageType());
  }

  @Override
  public CachedTSet<T> lazyCache() {
    return (CachedTSet<T>) super.lazyCache();
  }

  @Override
  public CachedTSet<T> cache() {
    return (CachedTSet<T>) super.cache();
  }

  @Override
  public PersistedTSet<T> persist() {
    return (PersistedTSet<T>) super.persist();
  }

  @Override
  public PersistedTSet<T> lazyPersist() {
    return (PersistedTSet<T>) super.lazyPersist();
  }
}