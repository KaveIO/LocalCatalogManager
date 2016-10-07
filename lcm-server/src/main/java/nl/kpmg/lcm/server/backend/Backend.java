/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
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

import nl.kpmg.lcm.server.backend.exception.BackendException;
import java.io.InputStream;

import nl.kpmg.lcm.server.data.MetaData;

import org.apache.metamodel.data.DataSet;

/**
 *
 * @author mhoekstra
 */
public interface Backend {

  /**
   * Method to gather information necessary for the dataset manipulation from the metadata.
   *
   * @param metadata should contain above all valid URI.
   * @return information whether URI points to existing resource, and its properties.
   * @throws BackendException if the URI is not valid or it is not possible to reach the resource.
   */
  DataSetInformation gatherDataSetInformation(MetaData metadata) throws BackendException;

  /**
   * Method to store some content on a data storage backend.
   *
   * @param metadata {@link MetaData} with URI of the data.
   * @param content {@link InputStream} that should be stored.
   * @throws BackendException if the URI is not valid or it is not possible to reach the storage.
   */
  void store(MetaData metadata, InputStream content) throws BackendException;

  /**
   * Method to read some content from a data storage backend.
   *
   * @param metadata {@link MetaData} with URI of the data.
   * @return {@link DataSet} with the data to be read.
   * @throws BackendException if the URI is not valid or it is not possible to reach the storage.
   */
  DataSet read(MetaData metadata) throws BackendException;

  /**
   * Method to delete some content on a data storage backend.
   *
   * @param metadata {@link MetaData} with URI of the data.
   * @return true if delete is successful, false otherwise.
   * @throws BackendException if the URI is not valid or it is not possible to reach the storage.
   */
  boolean delete(MetaData metadata) throws BackendException;
}
