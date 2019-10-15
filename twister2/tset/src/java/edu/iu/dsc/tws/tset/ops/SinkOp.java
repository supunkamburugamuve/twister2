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
package edu.iu.dsc.tws.tset.ops;

import java.util.Set;

import edu.iu.dsc.tws.api.compute.IMessage;
import edu.iu.dsc.tws.api.compute.TaskContext;
import edu.iu.dsc.tws.api.compute.modifiers.Closable;
import edu.iu.dsc.tws.api.compute.modifiers.Collector;
import edu.iu.dsc.tws.api.compute.modifiers.IONames;
import edu.iu.dsc.tws.api.compute.nodes.IComputableSink;
import edu.iu.dsc.tws.api.config.Config;
import edu.iu.dsc.tws.api.dataset.DataPartition;
import edu.iu.dsc.tws.api.tset.fn.SinkFunc;
import edu.iu.dsc.tws.tset.sets.BaseTSet;

/**
 * Creates a sink using {@link SinkFunc}. Can produce a single {@link DataPartition} from the
 * ID of the origin {@link BaseTSet}
 *
 * @param <T> data type
 */
public class SinkOp<T> extends BaseOp implements IComputableSink<T>, Closable, Collector {
  private static final long serialVersionUID = -9398832570L;

  private SinkFunc<T> sink;
  private IONames collectible; // key of the data partition this op produces

  public SinkOp() {
  }

  public SinkOp(SinkFunc<T> sink, BaseTSet origin, Set<String> receivableNames) {
    super(origin, receivableNames);
    this.sink = sink;
    this.collectible = IONames.declare(origin.getId());
  }

  @Override
  public void prepare(Config cfg, TaskContext ctx) {
    this.updateTSetContext(cfg, ctx);
    sink.prepare(gettSetContext());
  }

  @Override
  public boolean execute(IMessage<T> message) {
    sink.add(message.getContent());
    return true;
  }

  @Override
  public void close() {
    sink.close();
  }

  /**
   * returns the collected data partition only when it matches the provided name
   */
  @Override
  public DataPartition<?> get(String name) {
    if (collectible.size() == 1 && name.equals(collectible.iterator().next())) {
      return sink.get();
    }
    throw new RuntimeException("Unable to get the partition with key: " + name);
  }

  @Override
  public IONames getCollectibleNames() {
    return collectible;
  }
}
