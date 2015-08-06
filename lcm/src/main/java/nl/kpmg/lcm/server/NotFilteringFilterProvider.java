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
package nl.kpmg.lcm.server;

import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/**
 * Helper class to avoid some of the nastiness Jackson and SecurityEntityFilteringFeature provide.
 *
 * We use the SecurityEntityFilteringFeature for our role bases resource filtering. Somehow
 * this requires in combination with Jackson for a PropertyFilter to be set in some cases.
 * This is all good, however we currently have no intention of using this feature. This class
 * is a hacky catch-all that just acts as a filter provider. It does not filter anything.
 *
 * @author mhoekstra
 */
public class NotFilteringFilterProvider extends FilterProvider {

    private static final PropertyFilter notFilteringFilter = SimpleBeanPropertyFilter.serializeAllExcept("");

    @Override
    public BeanPropertyFilter findFilter(Object filterId) {
        throw new UnsupportedOperationException("Access to deprecated filters not supported");
    }

    @Override
    public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
        return notFilteringFilter;
    }
}
