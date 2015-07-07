package nl.kpmg.lcm.server.metadata.storage.mongodb;

import java.util.List;

import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.MetaDataDao;


public class MongoDbDaoImpl implements MetaDataDao {

	@Override
	public List<MetaData> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaData getByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MetaData getByNameAndVersion(String name, String version) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void persist(MetaData metadata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(MetaData metadata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStoragePath(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getStoragePath() {
		// TODO Auto-generated method stub
		return null;
	}

}
