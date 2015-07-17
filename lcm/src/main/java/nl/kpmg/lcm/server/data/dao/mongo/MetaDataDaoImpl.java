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
package nl.kpmg.lcm.server.data.dao.mongo;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.logging.Logger;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import java.util.List;
import java.util.Set;

import nl.kpmg.lcm.server.data.dao.DaoException;

/**
 * Implementation of a file based MetaData DAO.
 */
public class MetaDataDaoImpl implements MetaDataDao {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(MetaDataDaoImpl.class.getName());
    

    private static MongoClient mongoClient;
    private static DB db;
    
    private static DBCursor cursor;
    private static DBCollection dBCollection;
    private List<DBObject> myList;
    /**
     * @param storagePath The path where the metaData is stored
     * @throws UnknownHostException 
     * @throws StorageException when the storagePath doesn't exist
     */
    public MetaDataDaoImpl(final String databaseName, final String collectionName) throws DaoException, UnknownHostException {
       mongoClient = new MongoClient( "localhost" );
       db = mongoClient.getDB(databaseName);
       dBCollection = db.getCollection(collectionName);
       myList = null;
    
       
       

        if (dBCollection.count() == 0) {
            LOGGER.severe(String.format(
                    "The database %s is empty.", databaseName));
        }
    }   
        
    @Override
    public List<MetaData> getAll() {
       LinkedList<MetaData> result = new LinkedList(); 
       MetaData metaData;
       Set<String> allFieldNames;
       cursor = dBCollection.find();
       try {
            while(cursor.hasNext()) {
                metaData = new MetaData();
                allFieldNames = cursor.next().keySet();
                for (String fieldName : allFieldNames){
                    metaData.put(fieldName,cursor.curr().get(fieldName));
                }
                result.add(metaData);
            }    
        } finally {
            cursor.close();
        }
       return result;
    }   

    @Override
    public MetaData getByName(String name) {
        MetaData result = new MetaData();
        
        BasicDBObject nquery;
        Set<String> allFieldNames;
        BasicDBObject sortByVersion = new BasicDBObject("version",-1);
        nquery = new BasicDBObject("name",name);
        cursor = dBCollection.find(nquery).sort(sortByVersion);
        if (cursor.count() > 0){
           allFieldNames = cursor.next().keySet();
                for (String fieldName : allFieldNames){
                    result.put(fieldName,cursor.curr().get(fieldName));
                } 
        }
        return result;
    }

    @Override
    public MetaData getByNameAndVersion(String name, String versionNumber) {
      MetaData result = new MetaData();
        int verNum = Integer.valueOf(versionNumber);
        BasicDBObject nquery;
        Set<String> allFieldNames;
        nquery = new BasicDBObject("name",name).append("version",verNum);
     
        cursor = dBCollection.find(nquery);
        if (cursor.count() > 0){
            allFieldNames = cursor.next().keySet();
                for (String fieldName : allFieldNames){
                    result.put(fieldName,cursor.curr().get(fieldName));
                } 
            
        }
     return result;
    }

    @Override
    public void persist(final MetaData metadata) {
        BasicDBObject nquery;
        BasicDBObject myobject;
        int newVersion;
        String name = metadata.getName();
        nquery = new BasicDBObject("name",name);
        myobject = new BasicDBObject();
        
        Set<String> allFieldNames;
        if (dBCollection.find(nquery).count() == 0){
            newVersion = 1;
            allFieldNames = metadata.keySet();
            for (String fieldName : allFieldNames){
              myobject.put(fieldName, metadata.get(fieldName));
            }
            dBCollection.insert(myobject.append("version", newVersion));
        }
        else {
            cursor = dBCollection.find(nquery);
            cursor.sort(new BasicDBObject("version",-1));
            newVersion = (int) cursor.next().get("version") + 1;
            allFieldNames = metadata.keySet();
            for (String fieldName : allFieldNames){
              myobject.put(fieldName, metadata.get(fieldName));
            }
            dBCollection.insert(myobject.append("version",newVersion));
            
        }
    }
    

    @Override
    public void delete(MetaData metadata) {
       BasicDBObject nquery;
       nquery = new BasicDBObject("name",metadata.getName());
       dBCollection.remove(nquery);
    }


}
