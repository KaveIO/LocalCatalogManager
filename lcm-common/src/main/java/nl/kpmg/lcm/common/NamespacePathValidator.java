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
package nl.kpmg.lcm.common;

import nl.kpmg.lcm.common.validation.Notification;

/**
 *
 * @author shristov
 */
public class NamespacePathValidator {
  public void validate(String path, Notification notification) {
    if (path == null) {
      notification.addError("Error: Namesapce path is null");
      return;
    }
    String pattern = "^([a-zA-Z0-9_-]+(/[a-zA-Z0-9_-]+)*)?$";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher m = p.matcher(path);
    if (!m.matches()) {
      notification
          .addError("Error: Namesapce path is empty or it doesn't match the allowed symbols! "
              + "You can use only: a-z, A-Z, 0-9, '_', '-' and '/'");
    }
  }
}
