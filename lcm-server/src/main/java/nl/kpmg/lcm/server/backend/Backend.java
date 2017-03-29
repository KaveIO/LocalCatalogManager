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

import nl.kpmg.lcm.server.data.ContentIterator;
import nl.kpmg.lcm.server.data.Data;
import nl.kpmg.lcm.server.data.EnrichmentProperties;
import nl.kpmg.lcm.server.data.ProgressIndicationFactory;
import nl.kpmg.lcm.server.data.metadata.MetaData;

import org.apache.metamodel.data.DataSet;

/**
 *
 * @author mhoekstra
 */
public interface Backend {

  /**
   * Method to gather information necessary for the dataset manipulation.
   *
   * @return information whether URI points to existing resource, and its properties.
   */
  public MetaData enrichMetadata(EnrichmentProperties properties);


  /**
   * Method to store some content on a data storage backend.
   *
   * @param forceOverwrite - if set to true the @content is stored no mater if it already exists.
   *        When set to false the @content is stored only if it does not exist.
   * @param content {@link ContentIterator} that should be stored.
   */
  public void store(ContentIterator content, DataTransformationSettings transformationSettings,
      boolean forceOverwrite);

  /**
   * Method to read some content from a data storage backend.
   *
   * @return {@link DataSet} with the data to be read.
   */
  public Data read();

  /**
   * Method to delete some content on a data storage backend.
   *
   * @return true if delete is successful, false otherwise.
   */
  public boolean delete();

  /**
   * Method finalize is used to free all underlaying resources Call it when you are done with the
   * backend.
   */
  public void free();

  /*
   * ProgressIndicationFactory is used to produce indication messages about the progress of the
   * transfer.
   */
  public void setProgressIndicationFactory(ProgressIndicationFactory progressIndicationFactory);
}
