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
package edu.iu.dsc.tws.executor.comms.batch;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.comms.api.DataFlowOperation;
import edu.iu.dsc.tws.comms.api.MessageReceiver;
import edu.iu.dsc.tws.comms.core.TaskPlan;
import edu.iu.dsc.tws.comms.dfw.DataFlowGather;
import edu.iu.dsc.tws.comms.op.Communicator;
import edu.iu.dsc.tws.data.api.DataType;
import edu.iu.dsc.tws.executor.core.AbstractParallelOperation;
import edu.iu.dsc.tws.executor.core.EdgeGenerator;
import edu.iu.dsc.tws.executor.util.Utils;
import edu.iu.dsc.tws.task.api.IMessage;
import edu.iu.dsc.tws.task.api.TaskMessage;


public class GatherBatchOperation extends AbstractParallelOperation {
  private static final Logger LOG = Logger.getLogger(GatherBatchOperation.class.getName());
  private DataFlowGather op;

  public GatherBatchOperation(Config config, Communicator network, TaskPlan tPlan) {
    super(config, network, tPlan);
  }

  public void prepare(Set<Integer> srcs, int dest, EdgeGenerator e,
                      DataType dataType, String edgeName, Config config, TaskPlan taskPlan) {
    this.edgeGenerator = e;
    communicationEdge = e.generate(edgeName);
    op = new DataFlowGather(channel.getChannel(), srcs, dest, new FinalGatherReceiver(), 0, 0,
        config, Utils.dataTypeToMessageType(dataType), taskPlan, e.getIntegerMapping(edgeName));
    op.init(config, Utils.dataTypeToMessageType(dataType), taskPlan, communicationEdge);
  }

  @Override
  public boolean send(int source, IMessage message, int flags) {
    //LOG.info("Message : " + message.getContent());
    return op.send(source, message.getContent(), flags);
  }

  @Override
  public boolean progress() {
    return op.progress() && hasPending();
  }

  public boolean hasPending() {
    return !op.isComplete();
  }


  private class FinalGatherReceiver implements MessageReceiver {
    // lets keep track of the messages
    // for each task we need to keep track of incoming messages
    @Override
    public void init(Config cfg, DataFlowOperation operation,
                     Map<Integer, List<Integer>> expectedIds) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onMessage(int source, int path, int target, int flags, Object object) {
      // add the object to the map
      if (object instanceof List) {
        for (Object o : (List) object) {
          TaskMessage msg = new TaskMessage(o,
              edgeGenerator.getStringMapping(communicationEdge), target);
          outMessages.get(target).offer(msg);
          //    LOG.info("Source : " + source + ", Message : " + msg.getContent() + ", Target : "
          //        + target + ", Destination : " + destination);

        }
      }
      return true;

    }

    @Override
    public boolean progress() {
      return true;
    }
  }

}