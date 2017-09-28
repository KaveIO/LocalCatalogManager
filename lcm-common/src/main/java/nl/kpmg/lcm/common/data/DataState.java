/*
  * Copyright 2017 KPMG N.V. (unless otherwise stated).
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
 */
package nl.kpmg.lcm.common.data;

/**
 *
 * @author shristov
 */
public class DataState {
  // state "ATTACHED" is used when the data exists and it is valid and attached to the metadata that
  // describes it
  public static final String ATTACHED = "ATTACHED";
  // state "DETACHED" is used when the metadata describes certain data that does not exist
  public static final String DETACHED = "DETACHED";
  // state "INVALID" is used when the data exists but it is not valid. Nevertheless it is attached
  // to the metadata that describes it.
  public static final String INVALID = "INVALID";
}
