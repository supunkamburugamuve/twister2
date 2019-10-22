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
package org.apache.beam.runners.twister2.utils;

import javax.annotation.Nullable;

import org.apache.beam.runners.core.SideInputReader;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.values.PCollectionView;

public class Twister2SideInputReader implements SideInputReader {

  public Twister2SideInputReader()
  @Nullable
  @Override
  public <T> T get(PCollectionView<T> view, BoundedWindow window) {
    return null;
  }

  @Override
  public <T> boolean contains(PCollectionView<T> view) {
    return false;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
}
