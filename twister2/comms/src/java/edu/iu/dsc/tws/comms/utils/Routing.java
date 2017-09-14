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
package edu.iu.dsc.tws.comms.utils;

import java.util.List;

public class Routing {
  private final List<Integer> receivingIds;
  private final List<Integer> sendingIds;

  public Routing(List<Integer> receivingIds, List<Integer> sendingIds) {
    this.receivingIds = receivingIds;
    this.sendingIds = sendingIds;
  }

  public List<Integer> getReceivingIds() {
    return receivingIds;
  }

  public List<Integer> getSendingIds() {
    return sendingIds;
  }
}
