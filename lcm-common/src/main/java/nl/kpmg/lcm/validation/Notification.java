/*
 * Copyright 2016 KPMG N.V. (unless otherwise stated).
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

package nl.kpmg.lcm.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class Notification {

  private final List<Error> errors = new ArrayList<>();

  public void addError(String message, Exception e) {
    errors.add(new Error(message, e));
  }

  public String errorMessage() {
    return errors.stream().map(e -> e.message).collect(Collectors.joining(", "));
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }

  private static class Error {

    String message;
    Exception cause;

    private Error(String message, Exception cause) {
      this.message = message;
      this.cause = cause;
    }
  }

}
