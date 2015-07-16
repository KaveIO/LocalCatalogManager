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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author mhoekstra
 */
public class Configuration {
    private static String DEFAULT_FILENAME = "application.properties";
    private final Properties prop = new Properties();

    public Configuration() throws IOException {
	InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_FILENAME);
        if (input == null) {
            throw new IOException("Can't find default configuration file");
        }
        prop.load(input);
    }

    public Configuration(String filePath) throws IOException {
        InputStream input = new FileInputStream(filePath);
        prop.load(input);
    }


    public final String getServerName() {
        return prop.getProperty("lcm.server.name");
    }

    public final String getServerPort() {
        return prop.getProperty("lcm.server.port");
    }

    public final String getServerStorage() {
        return prop.getProperty("lcm.server.storage");
    }
}
