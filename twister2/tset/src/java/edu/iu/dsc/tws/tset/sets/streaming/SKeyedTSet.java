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


package edu.iu.dsc.tws.tset.sets.streaming;

import java.util.Collections;

import edu.iu.dsc.tws.api.comms.structs.Tuple;
import edu.iu.dsc.tws.api.compute.nodes.ICompute;
import edu.iu.dsc.tws.api.tset.fn.TFunction;
import edu.iu.dsc.tws.tset.env.StreamingTSetEnvironment;
import edu.iu.dsc.tws.tset.fn.MapCompute;
import edu.iu.dsc.tws.tset.fn.MapIterCompute;
import edu.iu.dsc.tws.tset.ops.MapToTupleIterOp;
import edu.iu.dsc.tws.tset.ops.MapToTupleOp;

/**
 * Attaches a key to the oncoming data.
 *
 * @param <K> key type
 * @param <V> data (value) type
 */
public class SKeyedTSet<K, V> extends StreamingTupleTSetImpl<K, V> {
  private TFunction<Tuple<K, V>, ?> mapToTupleFunc;

  public SKeyedTSet(StreamingTSetEnvironment tSetEnv, MapCompute<Tuple<K, V>, ?> mapFn,
                    int parallelism) {
    super(tSetEnv, "skeyed", parallelism);
    this.mapToTupleFunc = mapFn;
  }

  public SKeyedTSet(StreamingTSetEnvironment tSetEnv, MapIterCompute<Tuple<K, V>, ?> mapFn,
                    int parallelism) {
    super(tSetEnv, "skeyed", parallelism);
    this.mapToTupleFunc = mapFn;
  }

  @Override
  public ICompute getINode() {

    if (mapToTupleFunc instanceof MapCompute) {
      return new MapToTupleOp<>((MapCompute<Tuple<K, V>, ?>) mapToTupleFunc, this,
          Collections.emptyMap());
    } else if (mapToTupleFunc instanceof MapIterCompute) {
      return new MapToTupleIterOp<>((MapIterCompute<Tuple<K, V>, ?>) mapToTupleFunc, this,
          Collections.emptyMap());
    }

    throw new RuntimeException("Unknown map function passed to keyed tset" + mapToTupleFunc);

  }

  @Override
  public SKeyedTSet<K, V> setName(String name) {
    rename(name);
    return this;
  }
}