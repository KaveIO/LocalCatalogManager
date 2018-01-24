/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.integration.atlas;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.common.data.metadata.MetaDataWrapper;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
@Service
public class AtlasMetadataTransformator {

    public MetaData transform(Map entity, Storage storage) {
        return transform(entity, storage, "atlas");
    }

    public MetaData transform(Map entity, Storage storage, String  path) {

        MetaDataWrapper   metadata = new MetaDataWrapper();

       Map entityItem = (Map)entity.get("entity");
       String typeName = (String)entityItem.get("typeName");
       if(!typeName.equals("hive_table")) {
           throw new IllegalArgumentException("Transformation is available only from \"hibe_table\" type");
       }

       Map atribues = (Map)entityItem.get("attributes");
       String name = (String)atribues.get("name");
       metadata.setName(name);
       metadata.getData().setPath(path);

       String dataFormat = DataFormat.HIVE;
       String dataUri = dataFormat + "://" + storage.getName() + "/" +  name;
       List<String> uriList = new ArrayList();
       uriList.add(dataUri);
       metadata.getData().setUri(uriList);


       //TODO fill all the columns/type
       return metadata.getMetaData();
    }
}
