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

package nl.kpmg.lcm.server.backend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.common.validation.Notification;

import org.junit.Test;

import java.io.File;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class FilePathValidatorTest {

  @Test
  public final void testValidatePositive() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("test/file.csv");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertFalse(notification.hasErrors());
  }

  @Test
  public final void testValidateNegativeSameLevel() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("file.csv");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertTrue(notification.hasErrors());
  }

  @Test
  public final void testValidateNegative1() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("test/../../file.csv");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertTrue(notification.hasErrors());
  }

  @Test
  public final void testValidateNegative2() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("~/file.csv");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertTrue(notification.hasErrors());
  }



  @Test
  public final void testValidateNegative3() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("/etc/passwd");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertTrue(notification.hasErrors());
  }

  @Test
  public final void testValidateNegative4() {
    // This false are located in the directory of execution
    File baseDir = new File("test/");
    File file = new File("google.com");

    Notification notification = new Notification();
    FilePathValidator.validate(baseDir, file, notification);

    assertTrue(notification.hasErrors());
  }  
}
