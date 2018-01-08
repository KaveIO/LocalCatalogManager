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

public class TestResult {

  public enum TestCode {
    INACCESSIBLE, ACCESIBLE
  };

  private String message;
  private TestCode code;

  public TestResult() {}

  public TestResult(String message, TestCode code) {
    this.message = message;
    this.code = code;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @return the code
   */
  public TestCode getCode() {
    return code;
  }

  /**
   * @param message the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * @param code the code to set
   */
  public void setCode(TestCode code) {
    this.code = code;
  }
}