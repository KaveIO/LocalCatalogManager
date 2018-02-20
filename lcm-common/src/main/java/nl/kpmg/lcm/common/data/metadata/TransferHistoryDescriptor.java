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
package nl.kpmg.lcm.common.data.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author shristov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferHistoryDescriptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferHistoryDescriptor.class);
  private MetaData metaData;

  public static final int MAX_SIZE_OF_TRANSFER_HISTORY = 10;

  public TransferHistoryDescriptor(MetaData metaData) {
    this.metaData = metaData;
  }

  public final List<String> getTransferHistory() {
    if (this.metaData.getInnerMap().containsKey(getSectionName())) {
      return (List<String>) this.metaData.getInnerMap().get(getSectionName());
    }
    return null;
  }

  public final void setTransferHistory(final List<String> transferHistory) {
    this.metaData.getInnerMap().put(getSectionName(), transferHistory);
  }

  public final void addSourceLcmId(final String sourceLcmId) {
    List<String> list = getTransferHistory();
    if(list == null){
        list = new LinkedList<>();
    }
    if (list.size() == MAX_SIZE_OF_TRANSFER_HISTORY) {
      list.remove(0);
    }
    list.add(sourceLcmId);
    setTransferHistory(list);
  }

  public String getSectionName() {
    return "transfer-history";
  }
}
