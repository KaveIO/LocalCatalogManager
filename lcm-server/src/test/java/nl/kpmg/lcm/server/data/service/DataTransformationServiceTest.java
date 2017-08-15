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
package nl.kpmg.lcm.server.data.service;

import static org.junit.Assert.assertTrue;

import nl.kpmg.lcm.common.data.DataFormat;

import org.junit.Test;

/**
 *
 * @author shristov
 */
public class DataTransformationServiceTest {

  private static DataTransformationService service = new DataTransformationService();

  @Test
  public void testCsvToCsv() {
    boolean result = service.isTransformationAdmissible(DataFormat.CSV, DataFormat.CSV);
    assertTrue(result);
  }

  @Test
  public void testCsvToHive() {
    boolean result = service.isTransformationAdmissible(DataFormat.CSV, DataFormat.HIVE);
    assertTrue(result);
  }

  @Test
  public void testHiveToHive() {
    boolean result = service.isTransformationAdmissible(DataFormat.HIVE, DataFormat.CSV);
    assertTrue(result);
  }

  @Test
  public void testHiveToFile() {
    boolean result = service.isTransformationAdmissible(DataFormat.HIVE, DataFormat.FILE);
    assertTrue(result);
  }

  @Test
  public void testFileToHive() {
    boolean result = service.isTransformationAdmissible(DataFormat.HIVE, DataFormat.FILE);
    assertTrue(result);
  }
}
