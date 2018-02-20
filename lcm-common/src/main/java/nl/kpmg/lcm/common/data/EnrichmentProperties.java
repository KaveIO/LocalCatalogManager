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
package nl.kpmg.lcm.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
public class EnrichmentProperties {

  private Map descriptionMap;

  public EnrichmentProperties(Map descriptionMap) {
    if (descriptionMap == null) {
      this.descriptionMap = new HashMap();
      return;
    }

    this.descriptionMap = descriptionMap;
  }

  public EnrichmentProperties() {
    this.descriptionMap = new HashMap();
  }

  private Map getEnrichmentSection() {
    return descriptionMap;
  }

  /**
   * @return the cronExpression
   */
  public String getCronExpression() {
    return (String) getEnrichmentSection().get("cron-expression");
  }

  /**
   * @param cronExpression the cronExpression to set
   */
  public void setCronExpression(String cronExpression) {
    getEnrichmentSection().put("cron-expression", cronExpression);
  }

  /**
   * @return the collected
   */
  private void setProperty(String propertyName, boolean flag) {
    if (getEnrichmentSection().get("collected-properties") == null) {
      descriptionMap.put("collected-properties", new ArrayList());
    }

    ArrayList list = (ArrayList) getEnrichmentSection().get("collected-properties");
    HashSet propertiesSet = new HashSet(list);
    if (flag) {
      propertiesSet.add(propertyName);
    } else {
      propertiesSet.remove(propertyName);
    }

    List updatedList = new ArrayList(propertiesSet);
    getEnrichmentSection().put("collected-properties", updatedList);
  }



  /**
   * @param collected the collected to set
   */
  private boolean getProperty(String propertyName) {
    if (getEnrichmentSection().get("collected-properties") == null) {
      descriptionMap.put("collected-properties", new ArrayList());
    }

    ArrayList list = (ArrayList) getEnrichmentSection().get("collected-properties");
    HashSet propertiesSet = new HashSet(list);

    return propertiesSet.contains(propertyName);
  }

  /**
   * @return whether data accessibility will be added to the metadata during the enrichment process
   */
  public boolean getAccessibility() {
    return getProperty("accessibility");
  }

  /**
   * @param flag defines whether data accessibility will be added to the metadata during the
   *        enrichment process
   */
  public void setAccessibility(boolean flag) {
    setProperty("accessibility", flag);
  }

  /**
   * @return whether data size will be added to the metatadata during the enrichment process
   */
  public boolean getSize() {
    return getProperty("size");
  }

  /**
   * @param flag defines whether data size will be added to the metatadata during the enrichment
   *        process
   */
  public void setSize(boolean flag) {
    setProperty("size", flag);
  }

  /**
   * @return whether data structure will be added to the metatadata during the enrichment process
   */
  public boolean getStructure() {
    return getProperty("structure");
  }

  /**
   * @param flag defines whether data structure will be added to the metatadata during the
   *        enrichment process
   */
  public void setStructure(boolean flag) {
    setProperty("structure", flag);
  }

  /**
   * @return whether data items(i.e rows for SQL, objects for json etc...) count will be added to
   *         the metatadata during the enrichment process
   */
  public boolean getItemsCount() {
    return getProperty("items-count");
  }

  /**
   * @param flag defines whether data items count(i.e rows for SQL, objects for json etc...) will be
   *        added to the metatadata during the enrichment process
   */
  public void setItemCount(boolean flag) {
    setProperty("items-count", flag);
  }

  public static EnrichmentProperties createDefaultEnrichmentProperties() {
    EnrichmentProperties enrichment = new EnrichmentProperties(new HashMap());
    enrichment.setItemCount(true);
    enrichment.setSize(true);
    enrichment.setStructure(true);
    enrichment.setAccessibility(true);

    return enrichment;
  }

  public static EnrichmentProperties createDataExistingEnrichmentProperties() {
    EnrichmentProperties enrichment = new EnrichmentProperties(new HashMap());
    enrichment.setItemCount(false);
    enrichment.setSize(false);
    enrichment.setStructure(false);
    enrichment.setAccessibility(true);

    return enrichment;
  }
}
