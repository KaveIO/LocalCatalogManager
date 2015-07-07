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
package nl.kpmg.lcm.server.mongodb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.file.MetaDataDaoImpl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.mongodb.Mongo;

/**
 *
 * @author venkateswarlub
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/application-context-dao.xml"})
public class MongoDbSpringIntTest {
    
           
    @Autowired
    private final Mongo mongo = null;
    
    @Autowired
    private final MongoTemplate mongoTemplate = null;
    
    @Test
    public void testMongoPersist() {    	
     	MongoOperations mo = (MongoOperations) mongoTemplate;
    	MongoDbUser user = new MongoDbUser("user1","password123");
     	mo.save(user);
     	
     	List<MongoDbUser> listUser = mo.findAll(MongoDbUser.class);
    	System.out.println(" Number of users = " + listUser.size());
    	System.out.println(" users = " + listUser.toString());
    	assertEquals(listUser.get(0).getUser(),"user1");
    	assertEquals(listUser.get(0).getPassword(),"password123");
     	
    }
       
}
