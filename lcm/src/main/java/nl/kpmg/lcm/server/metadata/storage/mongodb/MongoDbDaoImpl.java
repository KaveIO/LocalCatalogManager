package nl.kpmg.lcm.server.metadata.storage.mongodb;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import nl.kpmg.lcm.server.data.MetaData;
import nl.kpmg.lcm.server.data.dao.MetaDataDao;


public class MongoDbDaoImpl implements MetaDataDao {

	@Autowired
	private MongoTemplate mongoTemplate;

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
	public void update(MetaData metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void persist(MetaData metadata) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(MetaData metadata) {
		// TODO Auto-generated method stub

	}


}
