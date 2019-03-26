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

package edu.iu.dsc.tws.api.tset.sets;


import edu.iu.dsc.tws.api.task.ComputeConnection;
import edu.iu.dsc.tws.api.tset.Source;
import edu.iu.dsc.tws.api.tset.TSetEnv;
import edu.iu.dsc.tws.api.tset.ops.SourceOp;
import edu.iu.dsc.tws.common.config.Config;

public abstract class SourceTSet<T> extends BatchBaseTSet<T> {
  protected Source<T> source;

  public SourceTSet(Config cfg, TSetEnv tSetEnv) {
    super(cfg, tSetEnv);
  }

  @Override
  public boolean baseBuild() {
    tSetEnv.getTSetBuilder().getTaskGraphBuilder().
        addSource(getName(), new SourceOp<T>(source), parallel);
    return true;
  }

  @Override
  public void buildConnection(ComputeConnection connection) {
    throw new IllegalStateException("Build connections should not be called on a TSet");
  }

  @Override
  public SourceTSet<T> setName(String n) {
    this.name = n;
    return this;
  }
}
