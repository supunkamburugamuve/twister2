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
package edu.iu.dsc.tws.api.compute.executor;

import edu.iu.dsc.tws.api.exceptions.Twister2Exception;
import edu.iu.dsc.tws.api.faulttolerance.Fault;
import edu.iu.dsc.tws.api.faulttolerance.FaultAcceptable;

public interface IExecutor extends FaultAcceptable {
  /**
   * Execute the specific plan
   * @return true if accepted
   */
  boolean execute();

  /**
   * Execute the specific plan
   * @return true if accepted
   */
  boolean execute(boolean close);

  /**
   * Asynchronously execute a plan, One need to call progress on the execution object returned to
   * continue the execution
   * @return an execution or null if not accepted
   */
  IExecution iExecute();

  /**
   * Wait for the execution to complete
   * @return true if successful
   */
  boolean closeExecution();

  /**
   * Terminate the executor
   */
  void close();

  /**
   * We are notifying that there is an error in the system and we need to terminate the current
   * execution.
   * @param fault the error code
   */
  @Override
  default void onFault(Fault fault) throws Twister2Exception {
  }

  /**
   * Get the execution plan associated with this executor
   * @return the execution plan
   */
  ExecutionPlan getExecutionPlan();
}
