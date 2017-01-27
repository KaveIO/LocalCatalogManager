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

package nl.kpmg.lcm.server.backend.metatadata;

import nl.kpmg.lcm.server.data.meatadata.MetaData;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;

import java.util.Map;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
public class CsvMetaData extends TabularMetaData {

  public CsvMetaData(MetaData metaData) {
    super(metaData);
  }


  public CsvConfiguration getConfiguration() {
    // TODO these options must be dynamically loaded from metada opject
    // However, until the metadata is not refactored they will be in this way.
    int columnNameLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
    String encoding = FileHelper.DEFAULT_ENCODING;
    char separatorChar = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;
    char quoteChar = CsvConfiguration.DEFAULT_QUOTE_CHAR;
    char escapeChar = CsvConfiguration.DEFAULT_ESCAPE_CHAR;

    // !TODO
    // When you refactoring MetaData and objects around it
    // keep in mind that validation must be done to all imput data
    // for example this is valid scenario
    // "column-name-line": "kdlfjhsadjkfh"
    Map dataOptions = getDataOptions();
    if (dataOptions != null) {
      if (dataOptions.containsKey("column-name-line")) {
        columnNameLine = (int) dataOptions.get("column-name-line");
      }
      if (dataOptions.containsKey("encoding")) {
        encoding = (String) dataOptions.get("encoding");
      }
      if (dataOptions.containsKey("separator-char")) {
        separatorChar = (char) dataOptions.get("separator-char");
      }
      if (dataOptions.containsKey("quote-char")) {
        quoteChar = (char) dataOptions.get("quote-char");
      }
      if (dataOptions.containsKey("escape-char")) {
        escapeChar = (char) dataOptions.get("escape-char");
      }
    }

    CsvConfiguration csvConfiguration =
        new CsvConfiguration(columnNameLine, encoding, separatorChar, quoteChar, escapeChar);
    return csvConfiguration;
  }
}
