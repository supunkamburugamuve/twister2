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
package edu.iu.dsc.tws.task.cdfw;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.ByteString;

import edu.iu.dsc.tws.api.compute.graph.ComputeGraph;
import edu.iu.dsc.tws.api.util.KryoSerializer;
import edu.iu.dsc.tws.proto.system.job.CDFWJobAPI;
import edu.iu.dsc.tws.proto.system.job.JobAPI;

public final class DataFlowGraph {
  // the data flow graph
  private ComputeGraph graph;

  // resources for cpu
  private int cpu;

  // the ram required
  private int ramMegaBytes;

  // the disk required
  private double diskGigaBytes;

  // number of workers requested
  private int workers;

  // the job configurations
  private DafaFlowJobConfig dafaFlowJobConfig = new DafaFlowJobConfig();

  // input names to this
  private List<CDFWJobAPI.Input> inputs = new ArrayList<>();

  // output names to this dataflow
  //private List<String> outputs = new ArrayList<>();

  // input names to this
  private List<CDFWJobAPI.Output> outputs = new ArrayList<>();

  private KryoSerializer kryoSerializer;

  // name to be used
  private String graphName;

  //schedule plan which has the workers to run the dataflow
  private CDFWJobAPI.CDFWSchedulePlan cdfwSchedulePlans;

  private DataFlowGraph(String name, ComputeGraph g) {
    this.graph = g;
    this.kryoSerializer = new KryoSerializer();
    this.graphName = name;
  }

  public static DataFlowGraph newSubGraphJob(String name, ComputeGraph g) {
    return new DataFlowGraph(name, g);
  }

  public CDFWJobAPI.CDFWSchedulePlan getCdfwSchedulePlans() {
    return cdfwSchedulePlans;
  }

  public DataFlowGraph setCdfwSchedulePlans(CDFWJobAPI.CDFWSchedulePlan cdfwSchedulePlan) {
    this.cdfwSchedulePlans = cdfwSchedulePlan;
    return this;
  }

  public DataFlowGraph setCpu(int c) {
    this.cpu = c;
    return this;
  }

  public String getGraphType() {
    return graphType;
  }

  public DataFlowGraph setGraphType(String graphtype) {
    this.graphType = graphtype;
    return this;
  }

  private String graphType;

  public DataFlowGraph setRamMegaBytes(int ram) {
    this.ramMegaBytes = ram;
    return this;
  }

  public DataFlowGraph setDiskGigaBytes(double disk) {
    this.diskGigaBytes = disk;
    return this;
  }

  public ComputeGraph getGraph() {
    return graph;
  }

  public int getCpu() {
    return cpu;
  }

  public int getRamMegaBytes() {
    return ramMegaBytes;
  }

  public double getDiskGigaBytes() {
    return diskGigaBytes;
  }

  public DataFlowGraph addDataFlowJobConfig(DafaFlowJobConfig jobConfig) {
    this.dafaFlowJobConfig.putAll(jobConfig);
    return this;
  }

  public DafaFlowJobConfig getDafaFlowJobConfig() {
    return dafaFlowJobConfig;
  }

  public int getWorkers() {
    return workers;
  }

  public DataFlowGraph setWorkers(int w) {
    this.workers = w;
    return this;
  }

 /* public DataFlowGraph addInput(String g, String input) {
    inputs.add(CDFWJobAPI.Input.newBuilder().setParentGraph(g).setName(input).build());
    return this;
  }*/

  /*public DataFlowGraph addOutput(String name) {
    outputs.add(name);
    return this;
  }

  public List<String> getOutputs() {
    return outputs;
  }*/

  public List<CDFWJobAPI.Input> getInputs() {
    return inputs;
  }

  public String getGraphName() {
    return graphName;
  }

  public DataFlowGraph addInput(String g, String input, String taskname) {
    inputs.add(CDFWJobAPI.Input.newBuilder()
        .setParentGraph(g).setName(input).setTaskname(taskname).build());
    return this;
  }

  public DataFlowGraph addOutput(String output, String taskname) {
    outputs.add(CDFWJobAPI.Output.newBuilder().setName(output).setTaskname(taskname).build());
    return this;
  }

  public List<CDFWJobAPI.Output> getOutputs() {
    return outputs;
  }

  public CDFWJobAPI.SubGraph build() {
    JobAPI.Config.Builder configBuilder = JobAPI.Config.newBuilder();

    if (graphName == null) {
      throw new RuntimeException("A name should be specified");
    }

    dafaFlowJobConfig.forEach((key, value) -> {
      byte[] objectByte = kryoSerializer.serialize(value);
      configBuilder.putConfigByteMap(key, ByteString.copyFrom(objectByte));
    });
    byte[] graphBytes = kryoSerializer.serialize(graph);

    //Construct the CDFWJob object to be sent to the CDFW Driver
    return CDFWJobAPI.SubGraph.newBuilder()
        .setName(graphName)
        .setConfig(configBuilder)
        .setGraphSerialized(ByteString.copyFrom(graphBytes))
        .setInstances(workers)
        .setCdfwScheduleplan(cdfwSchedulePlans)
        .addAllOutputs(outputs)
        .addAllInputs(inputs)
        .build();
  }
}
