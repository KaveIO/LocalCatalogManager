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
package nl.kpmg.lcm.server;

import nl.kpmg.lcm.common.NamespacePathValidator;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import nl.kpmg.lcm.common.validation.Notification;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author shristov
 */
@RunWith(DataProviderRunner.class)
public class NamespacePathValidatorTest {

  @Test
  @UseDataProvider("provideCorrectPathAndErrorMessage")
  public void testCorrectNamespace(String testPath, String errorMessage) {
    NamespacePathValidator validator = new NamespacePathValidator();
    Notification notification = new Notification();
    validator.validate(testPath, notification);

    Assert.assertFalse(errorMessage, notification.hasErrors());
  }


  @Test
  @UseDataProvider("provideWrongPathAndErrorMessage")
  public void testWrongNamespaceLeadingSlash(String testPath, String errorMessage) {
    NamespacePathValidator validator = new NamespacePathValidator();
    Notification notification = new Notification();
    validator.validate(testPath, notification);

    Assert.assertTrue(errorMessage, notification.hasErrors());
  }

  @DataProvider
  public static Object[][] provideWrongPathAndErrorMessage() {
    return new Object[][] { {"//////", "Namespace with empty path elements passed!"},
        {"kpmg/lcm/test/", "Namespace with trailing slash passed!"},
        {"/kpmg/lcm/test", "Namespace with Leading slash passed!"},
        {"kpm.g", "Namespace with '.' symbol passed!"},
        {"kpm^g", "Namespace with '^' symbol passed!"},
        {"kpm@g", "Namespace with '@' symbol passed!"}};
  }

  @DataProvider
  public static Object[][] provideCorrectPathAndErrorMessage() {
    return new Object[][] { {"kpmg", "Correct namespace didn't pass!"},
        {"kpmg/lcm/test", "Correct namespace didn't pass!"}};
  }
}
