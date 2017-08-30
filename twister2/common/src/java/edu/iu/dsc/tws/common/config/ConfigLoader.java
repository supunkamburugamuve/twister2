//
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
package edu.iu.dsc.tws.common.config;

import java.util.Map;

public final class ConfigLoader {

  private ConfigLoader() {
  }

  private static Config loadDefaults(String twister2Home, String configPath) {
    return Config.newBuilder(true)
        .put(Key.TWISTER2_HOME, twister2Home)
        .put(Key.TWISTER2_CONF, configPath)
        .build();
  }

  static Config loadConfig(String file) {
    Map<String, Object> readConfig = ConfigReader.loadFile(file);
    return addFromFile(readConfig);
  }

  static Config addFromFile(Map<String, Object> readConfig) {
    return Config.newBuilder().putAll(readConfig).build();
  }

  /**
   * Loads raw configurations from files under the twister2Home and configPath. The returned config
   * must be converted to either local or cluster mode to trigger pattern substitution of wildcards
   * tokens.
   */
  public static Config loadConfig(String twister2Home, String configPath,
                                  String releaseFile, String overrideConfigFile) {
    Config defaultConfig = loadDefaults(twister2Home, configPath);
    Config localConfig = Config.toLocalMode(defaultConfig); //to token-substitute the conf paths

    Config.Builder cb = Config.newBuilder()
        .putAll(defaultConfig)
        .putAll(loadConfig(Context.clientConfigurationFile(localConfig)))
        .putAll(loadConfig(Context.taskConfigurationFile(localConfig)))
        .putAll(loadConfig(Context.resourceSchedulerConfigurationFile(localConfig)))
        .putAll(loadConfig(Context.stateManagerConfigurationFile(localConfig)))
        .putAll(loadConfig(Context.uploaderConfigurationFile(localConfig)))
        .putAll(loadConfig(releaseFile))
        .putAll(loadConfig(overrideConfigFile));
    return cb.build();
  }

  /**
   * Loads raw configurations using the default configured twister2Home and configPath on
   * the cluster. The returned config must be converted to either local or cluster
   * mode to trigger pattern substitution of wildcards tokens.
   */
  public static Config loadClusterConfig() {
    Config defaultConfig = loadDefaults(
        Key.TWISTER2_CLUSTER_HOME.getDefaultString(), Key.TWISTER2_CLUSTER_CONF.getDefaultString());
    Config clusterConfig = Config.toClusterMode(defaultConfig); //to token-substitute the conf paths

    Config.Builder cb = Config.newBuilder()
        .putAll(defaultConfig)
        .putAll(loadConfig(Context.clientConfigurationFile(clusterConfig)))
        .putAll(loadConfig(Context.taskConfigurationFile(clusterConfig)))
        .putAll(loadConfig(Context.resourceSchedulerConfigurationFile(clusterConfig)))
        .putAll(loadConfig(Context.stateManagerConfigurationFile(clusterConfig)))
        .putAll(loadConfig(Context.uploaderConfigurationFile(clusterConfig)));

    return cb.build();
  }

  public static Config loadComponentConfig(String filePath) {
    Config defaultConfig = loadDefaults(
        Key.TWISTER2_CLUSTER_HOME.getDefaultString(), Key.TWISTER2_CLUSTER_CONF.getDefaultString());
    Config clusterConfig = Config.toClusterMode(defaultConfig); //to token-substitute the conf paths

    Config.Builder cb = Config.newBuilder()
        .putAll(defaultConfig)
        .putAll(loadConfig(filePath));
    return cb.build();
  }
}
