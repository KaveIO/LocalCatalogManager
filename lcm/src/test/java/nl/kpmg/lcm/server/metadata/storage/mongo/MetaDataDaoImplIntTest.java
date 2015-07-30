package nl.kpmg.lcm.server.metadata.storage.mongo;

import nl.kpmg.lcm.server.data.dao.mongo.MetaDataDaoImpl;

import java.net.UnknownHostException;
import java.util.List;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.DaoException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author kos
 */


public class MetaDataDaoImplIntTest {
    
     @Test
     public void testConstruction() throws DaoException, UnknownHostException {
         MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("probablyDoesntExist","collectionname");
     }
     
     @Test
     public void testGetAllReturnsEmptyListOnEmptyDatabase() throws DaoException, UnknownHostException {
         MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("probablyDoesntExist","collectionname");
         List<MetaData> all = metaDataDaoImpl.getAll();
         
         assertEquals(0, all.size());
     }
     
//     @Test
//     public void makeAndPersistMetaDataFirst() throws StorageException {
//         MetaData metadata = new MetaData();
//         MetaData testMetaData = new MetaData();
//         metadata.put("name", "Ben");
//         metadata.put("version",10);
//         metadata.put("description", "man");
//         MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("local","mycollection");
//         metaDataDaoImpl.persist(metadata);
//         testMetaData = metaDataDaoImpl.getByName("Mark");
//         
//         assertEquals("Mark",testMetaData.getName() );
//         //System.out.print(testMetaData.getName());
//         System.out.println(testMetaData.getName());
//         System.out.println(testMetaData);
//     }
     
     @Test
     public void getByNameAndGetByVersion() throws DaoException, UnknownHostException {
         MetaData metadata1 = new MetaData();
         MetaData metadata2 = new MetaData();
         MetaData metadata3 = new MetaData();
         MetaData metadata4 = new MetaData();
         MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("local","mycollection");
         metadata1 = metaDataDaoImpl.getByName("Mark");
         metadata2 = metaDataDaoImpl.getByNameAndVersion("Mark","8");
         metadata3 = metaDataDaoImpl.getByNameAndVersion("Ben","8");
         metadata4 = metaDataDaoImpl.getByNameAndVersion("Lou","1");
     }
     
//     @Test
//     public void makeAndPersistMetaDataMultiple() throws StorageException {
//         MetaData metadata1 = new MetaData();
//         MetaData metadata2 = new MetaData();
//         MetaData metadata3 = new MetaData();
//         MetaData testMetaData = new MetaData();
//         metadata1.put("name", "Mark");
//         metadata2.put("name", "Tom");
//         metadata3.put("name", "Tom");
//         metadata3.put("version", 5);
//         MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl("local","mycollection");
//         metaDataDaoImpl.persist(metadata1);
//         metaDataDaoImpl.persist(metadata2);
//         metaDataDaoImpl.persist(metadata3);
//         testMetaData = metaDataDaoImpl.getByName("Mark");
//         //System.out.print(testMetaData.getName());
//         //System.out.println(testMetaData.getName());
//         //System.out.println(testMetaData.getVersionNumber());
//         testMetaData = metaDataDaoImpl.getByName("Tom");
//         //System.out.println(testMetaData.getName());
//         //System.out.println(testMetaData.getVersionNumber());
//         List<MetaData> listOfMetaData = metaDataDaoImpl.getAll();
//         MetaData currentData;
//         for (MetaData loopMetaData : listOfMetaData){
//             currentData = loopMetaData;
//         }
//
//     }
}
