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

package edu.iu.dsc.tws.master.client;

import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.protobuf.Message;

import edu.iu.dsc.tws.common.config.Config;
import edu.iu.dsc.tws.common.net.tcp.Progress;
import edu.iu.dsc.tws.common.net.tcp.StatusCode;
import edu.iu.dsc.tws.common.net.tcp.request.ConnectHandler;
import edu.iu.dsc.tws.common.net.tcp.request.MessageHandler;
import edu.iu.dsc.tws.common.net.tcp.request.RRClient;
import edu.iu.dsc.tws.common.net.tcp.request.RequestID;
import edu.iu.dsc.tws.proto.network.Network;
import edu.iu.dsc.tws.proto.network.Network.ListWorkersRequest;
import edu.iu.dsc.tws.proto.network.Network.ListWorkersResponse;
import edu.iu.dsc.tws.rsched.bootstrap.WorkerNetworkInfo;

public class JobMasterClient extends Thread {
  private static final Logger LOG = Logger.getLogger(JobMasterClient.class.getName());

  private static Progress looper;
  private boolean stopLooper = false;

  private int workerID;
  private InetAddress workerIP;
  private int workerPort;
  private int numberOfWorkers;

  private String masterAddress;
  private int masterPort;

  private RRClient rrClient;
  private Pinger pinger;
  private WorkerController workerController;

  public JobMasterClient(Config config, String masterAddress, int masterPort,
                         int workerID, InetAddress workerIP, int workerPort, int numberOfWorkers) {
    this.masterAddress = masterAddress;
    this.masterPort = masterPort;
    this.workerID = workerID;
    this.workerIP = workerIP;
    this.workerPort = workerPort;
    this.numberOfWorkers = numberOfWorkers;
  }

  public void init() {
    looper = new Progress();

    ClientConnectHandler connectHandler = new ClientConnectHandler();
    rrClient = new RRClient(masterAddress, masterPort, null, looper, workerID, connectHandler);

    long interval = 1000;
    pinger = new Pinger(workerID, rrClient, interval);

    WorkerNetworkInfo thisWorker = new WorkerNetworkInfo(workerIP, workerPort, workerID);
    workerController = new WorkerController(thisWorker, numberOfWorkers, rrClient);

    Network.Ping.Builder pingBuilder = Network.Ping.newBuilder();
    rrClient.registerResponseHandler(pingBuilder, pinger);

    ListWorkersRequest.Builder listRequestBuilder = ListWorkersRequest.newBuilder();
    ListWorkersResponse.Builder listResponseBuilder = ListWorkersResponse.newBuilder();
    rrClient.registerResponseHandler(listRequestBuilder, workerController);
    rrClient.registerResponseHandler(listResponseBuilder, workerController);

    Network.WorkerStateChange.Builder stateChangeBuilder = Network.WorkerStateChange.newBuilder();
    Network.WorkerStateChangeResponse.Builder stateChangeResponseBuilder
        = Network.WorkerStateChangeResponse.newBuilder();

    ResponseMessageHandler responseMessageHandler = new ResponseMessageHandler();
    rrClient.registerResponseHandler(stateChangeBuilder, responseMessageHandler);
    rrClient.registerResponseHandler(stateChangeResponseBuilder, responseMessageHandler);

    rrClient.start();
    // we loop once to initialize things
    looper.loop();
    this.start();

    workerController.sendWorkerListRequest(ListWorkersRequest.RequestType.IMMEDIATE_RESPONSE);
    pinger.start();
  }

  public void close() {
    pinger.stopPinger();
    stopLooper = true;
    interrupt();
  }

  @Override
  public void run() {

    int loopCount = 5;
    long sleepTime = 100;

    while (!stopLooper) {
      for (int i = 0; i < loopCount; i++) {
        looper.loop();
      }

      try {
        sleep(sleepTime);
      } catch (InterruptedException e) {
        if (!stopLooper) {
          LOG.log(Level.WARNING, "JobMasterClient Thread sleep interrupted.", e);
        }
      }
    }

    // if there are any messages to receive in the buffer, get them
    // we assume there can be at most 3 messages
    looper.loop();
    looper.loop();
    looper.loop();
    rrClient.stop();
  }

  public boolean sendWorkerStartingMessage() {
    Network.WorkerStateChange workerStateChange = Network.WorkerStateChange.newBuilder()
        .setWorkerID(workerID)
        .setNewState(Network.WorkerState.STARTING)
        .setIp(workerIP.getHostAddress())
        .setPort(workerPort)
        .build();

    RequestID requestID = rrClient.sendRequest(workerStateChange);
    if (requestID == null) {
      LOG.severe("Could not send Worker Starting message.");
      return false;
    }

    LOG.info("Sent the Worker Starting message with requestID: \n" + workerStateChange);
    return true;
  }

  public boolean sendWorkerRunningMessage() {
    Network.WorkerStateChange workerStateChange = Network.WorkerStateChange.newBuilder()
        .setWorkerID(workerID)
        .setNewState(Network.WorkerState.RUNNING)
        .build();

    RequestID requestID = rrClient.sendRequest(workerStateChange);
    if (requestID == null) {
      LOG.severe("Could not send Worker Running message.");
      return false;
    }

    LOG.info("Sent the Worker Running message: \n" + workerStateChange);
    return true;
  }

  public boolean sendWorkerCompletedMessage() {
    Network.WorkerStateChange workerStateChange = Network.WorkerStateChange.newBuilder()
        .setWorkerID(workerID)
        .setNewState(Network.WorkerState.COMPLETED)
        .build();

    RequestID requestID = rrClient.sendRequest(workerStateChange);
    if (requestID == null) {
      LOG.severe("Could not send Worker Completed message.");
      return false;
    }

    LOG.info("Sent the Worker Completed message: \n" + workerStateChange);
    return true;
  }

  class ResponseMessageHandler implements MessageHandler {

    @Override
    public void onMessage(RequestID id, int workerId, Message message) {

      if (message instanceof Network.WorkerStateChangeResponse) {
        LOG.info("Received a WorkerStateChange response from the master. \n" + message);
      } else {
        LOG.warning("Received message unrecognized. \n" + message);
      }

    }
  }

  public class ClientConnectHandler implements ConnectHandler {
    @Override
    public void onError(SocketChannel channel) {

    }

    @Override
    public void onConnect(SocketChannel channel, StatusCode status) {
    }

    @Override
    public void onClose(SocketChannel channel) {

    }
  }

  public static void simulateClient(String masterAddress, int masterPort, int workerID,
                                    int numberOfWorkers) {

    InetAddress workerIP = WorkerController.convertStringToIP("149.165.150.81");
    int workerPort = 10000 + (int) (Math.random() * 10000);

    JobMasterClient client = new JobMasterClient(null, masterAddress, masterPort,
        workerID, workerIP, workerPort, numberOfWorkers);
    client.init();

    client.sendWorkerStartingMessage();

    // wait 500ms
    sleeeep(500);

    client.sendWorkerRunningMessage();

    List<WorkerNetworkInfo> workerList = client.workerController.getWorkerList();
    WorkerController.printWorkers(workerList);

    // wait 2000ms
    sleeeep(2000);

    workerList = client.workerController.waitForAllWorkersToJoin(2000);
    WorkerController.printWorkers(workerList);

    client.sendWorkerCompletedMessage();
//    sleeeep(500);

    client.close();

    System.out.println("all messaging done. waiting before finishing.");

    sleeeep(500);
  }

  public static void sleeeep(long duration) {

    try {
      Thread.sleep(duration);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void loop(long duration) {

    long start = System.currentTimeMillis();

    while (duration > (System.currentTimeMillis() - start)) {
      looper.loop();
    }
  }

  public static void main(String[] args) {

    Logger.getLogger("edu.iu.dsc.tws.common.net.tcp").setLevel(Level.SEVERE);

    String masterAddress = "localhost";
    int masterPort = 11111;
    int workerID = 0;
    int numberOfWorkers = 1;

    if (args.length == 1) {
      numberOfWorkers = Integer.parseInt(args[0]);
    }

    if (args.length == 2) {
      numberOfWorkers = Integer.parseInt(args[0]);
      workerID = Integer.parseInt(args[1]);
    }

    simulateClient(masterAddress, masterPort, workerID, numberOfWorkers);

  }


}
