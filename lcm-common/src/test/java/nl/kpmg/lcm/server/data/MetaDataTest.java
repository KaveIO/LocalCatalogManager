/*
 * Copyright 2015 KPMG N.V. (unless otherwise stated).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.kpmg.lcm.server.data;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 *
 * @author mhoekstra
 */
public class MetaDataTest {

    private final Method setMethod;

    private final Method getMethod;

    public MetaDataTest() throws NoSuchMethodException {
        setMethod = MetaData.class.getDeclaredMethod("set", String.class, Object.class);
        setMethod.setAccessible(true);

        getMethod = MetaData.class.getDeclaredMethod("get", String.class);
        getMethod.setAccessible(true);
    }


    @Test
    public void testGetReturnNullOnNoValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String actual;

        MetaData metaData = new MetaData();

        actual = (String) getMethod.invoke(metaData, "notExistingKey");

        assertNull(actual);
    }

    @Test
    public void testGetReturnFirstLevelValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String expected = "value";
        String actual;

        MetaData metaData = new MetaData();
        metaData.set("firstLevelKey", expected);

        actual = (String) getMethod.invoke(metaData, "firstLevelKey");

        assertEquals(expected, actual);
    }

    @Test
    public void testGetReturnSecondLevelValue() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final String expected = "value";
        String actual;

        MetaData metaData = new MetaData();
        metaData.set("firstLevelKey", new HashMap() {{ put("secondLevelKey", expected); }});

        actual = (String) getMethod.invoke(metaData, "firstLevelKey.secondLevelKey");

        assertEquals(expected, actual);
    }



    @Test
    public void testSetFirstLevelAttributes() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String expected = "value";
        String actual;

        MetaData metaData = new MetaData();

        setMethod.invoke(metaData, "firstLevelKey", expected);

        actual = (String) metaData.get("firstLevelKey");

        assertEquals(expected, actual);
    }

    @Test
    public void testSetSecondLevelAttributes() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String expected = "value";
        String actual;

        MetaData metaData = new MetaData();

        setMethod.invoke(metaData, "firstLevelKey.secondLevelKey", expected);

        actual = (String) ((Map) metaData.get("firstLevelKey")).get("secondLevelKey");

        assertEquals(expected, actual);
    }

    @Test
    public void testSetMulitpleSecondLevelAttributes() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        String expected1 = "value1";
        String expected2 = "value2";
        String actual;

        MetaData metaData = new MetaData();

        setMethod.invoke(metaData, "firstLevelKey.secondLevelKey", expected1);
        setMethod.invoke(metaData, "firstLevelKey.secondSecondLevelKey", expected2);

        Object get = metaData.get("firstLevelKey");

        assertEquals(expected1, (String) ((Map) metaData.get("firstLevelKey")).get("secondLevelKey"));
        assertEquals(expected2, (String) ((Map) metaData.get("firstLevelKey")).get("secondSecondLevelKey"));
    }
}
