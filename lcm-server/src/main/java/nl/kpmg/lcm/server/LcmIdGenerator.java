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
package nl.kpmg.lcm.server;

import nl.kpmg.lcm.common.configuration.ServerConfiguration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Date;
import java.util.Random;

/**
 *
 * @author shristov
 */
public class LcmIdGenerator {

  public static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public static final String LOWER = UPPER.toLowerCase();

  public static final String DIGITS = "0123456789";

  public static final int LCM_ID_MAX_LENGTH = 32;

  public static final int APPLICATION_NAME_MAX_LENGTH = 12;

  private int counter = 0;

  private ApplicationContext parentContext =
        new ClassPathXmlApplicationContext(new String[] {"application-context-server.xml"});

  private ServerConfiguration configuration = parentContext.getBean(ServerConfiguration.class);


  public String generateRandomAlphanum() {
    Random random = new Random();
    String alphanum = UPPER + LOWER + DIGITS;
    char[] symbols = alphanum.toCharArray();
    StringBuilder str = new StringBuilder();

    for (int i = counter; i < LCM_ID_MAX_LENGTH; i++) {
      str.append(symbols[random.nextInt(symbols.length)]);
    }

    return str.toString();
  }

  public String generateLcmId() {
    StringBuilder str = new StringBuilder();

    String applicationName = configuration.getApplicationName();
    if (applicationName.length() > APPLICATION_NAME_MAX_LENGTH) {
      applicationName = applicationName.substring(0, APPLICATION_NAME_MAX_LENGTH);
    }
    counter = applicationName.length();
    str.append(applicationName + "-");

    String unixTimestamp = Long.toString(new Date().getTime());
    counter = counter + unixTimestamp.length();
    str.append(unixTimestamp + "-");

    String randomAlphanum = generateRandomAlphanum();
    str.append(randomAlphanum);

    return str.toString();
  }
}
