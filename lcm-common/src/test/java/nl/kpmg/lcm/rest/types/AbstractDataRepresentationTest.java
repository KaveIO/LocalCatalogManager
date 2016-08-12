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
package nl.kpmg.lcm.rest.types;

import org.junit.Test;

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Link;

/**
 *
 * @author mhoekstra
 */
public class AbstractDataRepresentationTest {

  @Test
  public void testSerialization() throws JsonProcessingException {
    TestModel testModel = new TestModel();
    testModel.setValue("value");

    ConcreteTestDataRepresentation concreteTestDataRepresentation =
        new ConcreteTestDataRepresentation();
    concreteTestDataRepresentation.setItem(testModel);

    ObjectMapper objectMapper = new ObjectMapper();
    String value = objectMapper.writeValueAsString(testModel);

    assertNotNull(value);
  }

  @Test
  public void testGetLinks() {
    // Baseline, no links
    ConcreteTestDataRepresentation concreteTestDataRepresentation =
        new ConcreteTestDataRepresentation();
    List<Link> actual = concreteTestDataRepresentation.getLinks();
    assertNotNull(actual);
    assertEquals(0, actual.size());

    // One link in the regular attribute
    concreteTestDataRepresentation = new ConcreteTestDataRepresentation();
    concreteTestDataRepresentation.setLinks(Arrays.asList(Link.valueOf("<http://www.google.com>")));
    actual = concreteTestDataRepresentation.getLinks();
    assertNotNull(actual);
    assertEquals(1, actual.size());

    // One link via the injected method
    concreteTestDataRepresentation = new ConcreteTestDataRepresentation();
    concreteTestDataRepresentation
        .setInjectedLinks(Arrays.asList(Link.valueOf("<http://www.google.com>")));
    actual = concreteTestDataRepresentation.getLinks();
    assertNotNull(actual);
    assertEquals(1, actual.size());

    // Combination from attribute and the injected method
    concreteTestDataRepresentation = new ConcreteTestDataRepresentation();
    concreteTestDataRepresentation.setLinks(Arrays.asList(Link.valueOf("<http://www.google.com>")));
    concreteTestDataRepresentation
        .setInjectedLinks(Arrays.asList(Link.valueOf("<http://www.google.com>")));
    actual = concreteTestDataRepresentation.getLinks();
    assertNotNull(actual);
    assertEquals(2, actual.size());
  }
}
