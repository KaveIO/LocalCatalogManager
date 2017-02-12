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

package nl.kpmg.lcm.server.backend.metadata;

import nl.kpmg.lcm.server.data.metadata.MetaData;
import nl.kpmg.lcm.server.data.metadata.Wrapper;

import org.apache.metamodel.csv.CsvConfiguration;
import org.apache.metamodel.util.FileHelper;

/**
 *
 * @author Stoyan Hristov<shristov@intracol.com>
 */
@Wrapper
public class CsvMetaData extends TabularMetaData {

  public CsvMetaData(MetaData metaData) {
    super(metaData);
  }

  public CsvMetaData() {
    super();
  }

  public CsvConfiguration getConfiguration() {
    int columnNameLine = CsvConfiguration.DEFAULT_COLUMN_NAME_LINE;
    String encoding = FileHelper.DEFAULT_ENCODING;
    char separatorChar = CsvConfiguration.DEFAULT_SEPARATOR_CHAR;
    char quoteChar = CsvConfiguration.DEFAULT_QUOTE_CHAR;
    char escapeChar = CsvConfiguration.DEFAULT_ESCAPE_CHAR;

    CsvConfigurationDescriptor csvDescriptor = new CsvConfigurationDescriptor(getMetaData());
    if (csvDescriptor.getColumnNameLine() != null) {
      columnNameLine = csvDescriptor.getColumnNameLine();
    }
    if (csvDescriptor.getEncoding() != null) {
      encoding = csvDescriptor.getEncoding();
    }
    if (csvDescriptor.getSeparatorChar() != null) {
      separatorChar = csvDescriptor.getSeparatorChar();
    }
    if (csvDescriptor.getQuoteChar() != null) {
      quoteChar = csvDescriptor.getQuoteChar();
    }
    if (csvDescriptor.getEscapeChar() != null) {
      escapeChar = csvDescriptor.getEscapeChar();
    }

    CsvConfiguration csvConfiguration =
        new CsvConfiguration(columnNameLine, encoding, separatorChar, quoteChar, escapeChar);
    return csvConfiguration;
  }
}
