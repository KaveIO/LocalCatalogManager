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

/**
 * When the MetaData does not contains information how the data to be interpreted then
 * DataTransformationSettings must be used.
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class DataTransformationSettings {
  private static final int DEFAULT_DECIMAL_PRECISION = 10;
  private static final int DEFAULT_VARCHAR_SIZE = 255;
  private static final int DEAFULT_MAXIMUM_INSERTED_RECORDS_PER_QUERY = 10000;

  private Integer maximumInsertedRecordsPerQuery = DEAFULT_MAXIMUM_INSERTED_RECORDS_PER_QUERY;
  private Integer varCharSize = DEFAULT_VARCHAR_SIZE;
  private Integer decimalPrecision = DEFAULT_DECIMAL_PRECISION;

  /**
   *
   * @return the maximumInsertedRecordsPerQuery
   */
  Integer getMaximumInsertedRecordsPerQuery() {
    return maximumInsertedRecordsPerQuery;
  }

  /**
   * As usually big amounts of data is transmitted it must be stored by chunks. This method defines
   * the maximum size of the chunk specified with count of inserted records per one insertion. If
   * noting is set explicitly by this method then the default value will be used which is
   * {@value #DEAFULT_MAXIMUM_INSERTED_RECORDS_PER_QUERY}
   *
   * @param maximumInsertedRecordsPerQuery
   */
  public void setMaximumInsertedRecordsPerQuery(Integer maximumInsertedRecordsPerQuery) {
    this.maximumInsertedRecordsPerQuery = maximumInsertedRecordsPerQuery;
  }

  /**
   * @return the varchar Size
   *
   */
  Integer getVarCharSize() {
    return varCharSize;
  }

  /**
   * In case varchar size is missing in the metadata this value will be used for constructing
   * destination table.
   *
   * This setting will be taken in account only if destination is SQL like data sources.
   *
   * If noting is set explicitly by this method then the default value will be used which is
   * {@value #DEFAULT_VARCHAR_SIZE}
   *
   * @param varCharSize It could be up to 65535.
   */
  public void setVarCharSize(Integer varCharSize) {
    this.varCharSize = varCharSize;
  }

  /**
   * @return the decimalPrecision
   */
  Integer getDecimalPrecision() {
    return decimalPrecision;
  }

  /**
   * In case decimal precision is missing in the metadata this value will be used for constructing
   * the destination table.
   *
   * This setting will be taken in account only if destination is SQL like data sources.
   *
   * If noting is set explicitly by this method then the default value will be used which is
   * {@value #DEFAULT_DECIMAL_PRECISION}
   *
   * @param decimalPrecision the precision value
   */
  public void setDecimalPrecision(Integer decimalPrecision) {
    this.decimalPrecision = decimalPrecision;
  }
}
