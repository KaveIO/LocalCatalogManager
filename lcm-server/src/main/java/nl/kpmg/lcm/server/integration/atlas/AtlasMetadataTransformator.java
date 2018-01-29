/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.server.integration.atlas;

import nl.kpmg.lcm.common.data.DataFormat;
import nl.kpmg.lcm.common.data.Storage;
import nl.kpmg.lcm.common.data.metadata.MetaData;
import nl.kpmg.lcm.server.backend.metadata.ColumnDescription;
import nl.kpmg.lcm.server.backend.metadata.TabularMetaData;

import org.apache.commons.lang3.StringUtils;
import org.apache.metamodel.schema.ColumnType;
import org.apache.metamodel.schema.ColumnTypeImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shristov
 */
// Compatible with sandbox 2.6.3
@Service
public class AtlasMetadataTransformator {

  public MetaData transform(Map entity, Storage storage) throws TransformationException {
    return transform(entity, "atlas", storage);
  }

  public MetaData transform(Map mainEntity, String path, Storage storage)
      throws TransformationException {
    TabularMetaData metadata = new TabularMetaData();

    Map entityItem = (Map) mainEntity.get("entity");
    String tableTypeName = (String) entityItem.get("typeName");
    if (!tableTypeName.equals("hive_table")) {
      throw new TransformationException("Transformation is available only from \"hive_table\" type");
    }

    Map atribues = (Map) entityItem.get("attributes");
    String tableName = (String) atribues.get("name");
    metadata.setName(tableName);
    metadata.getData().setPath(path);
    // TODO find sutable storage on the base of atlas configuration
    String dataFormat = DataFormat.HIVE;
    String dataUri = dataFormat + "://" + storage.getName() + "/" + tableName;
    List<String> uriList = new ArrayList();
    uriList.add(dataUri);
    metadata.getData().setUri(uriList);

    Map referencedEntities = (Map) mainEntity.get("referredEntities");
    List<ColumnDescription> columns = buildColumns(atribues, referencedEntities);
    metadata.getTableDescription(dataUri).setColumns(columns);

    setAtlasOriginData(entityItem, metadata, mainEntity);

    return metadata.getMetaData();
  }

  private List<ColumnDescription> buildColumns(Map atribues, Map referencedEntities)
      throws TransformationException {
    List<ColumnDescription> columns = new ArrayList();
    List<Map> originalColumns = (List) atribues.get("columns");
    for (Map column : originalColumns) {
      String guid = (String) column.get("guid");

      String columnTypeName = (String) column.get("typeName");
      if (columnTypeName.equals("hive_column")) {
        Map columnMap = (Map) referencedEntities.get(guid);
        Map columnAttributes = (Map) columnMap.get("attributes");
        String type = (String) columnAttributes.get("type");
        String size = null;
        if (type.contains("(")) {
          String[] typeArr = StringUtils.split(type, "(");
          if (typeArr.length != 2) {
            throw new TransformationException("Illegal type structure: " + type);
          }

          type = typeArr[0];
          size = typeArr[1].replace("(", "");
          size = size.replace(")", "");
        }

        String columnName = (String) columnAttributes.get("name");
        ColumnType columnType = matchColumnType(type.toUpperCase());
        ColumnDescription description = new ColumnDescription(columnName, columnType);
        if (size != null) {
          setSize(columnType, size, description);
        }
        columns.add(description);
      }
    }

    return columns;
  }

  private void setAtlasOriginData(Map entityItem, TabularMetaData metadata, Map mainEntity) {
    String metadataGuid = (String) entityItem.get("guid");
    metadata.getAtlasMetadata().setGuid(metadataGuid);
    metadata.getAtlasMetadata().setBody(mainEntity);
  }

  private void setSize(ColumnType columnType, String size, ColumnDescription description)
      throws TransformationException {
    try {
      if (columnType.equals(ColumnType.DECIMAL)) {
        String[] sizeArr = StringUtils.split(size, ",");
        if (sizeArr.length != 2) {
          throw new TransformationException("Illegal Desimal size structure: " + size);
        }
        size = sizeArr[0];

        if (!StringUtils.isNumeric(sizeArr[1])) {
          throw new TransformationException("Presicion must be numeric: " + sizeArr[1]);
        }
        description.setPrecision(Integer.parseInt(sizeArr[1]));

      }

      if (!StringUtils.isNumeric(size)) {
        throw new TransformationException("Size must be numeric: " + size);
      }
      description.setSize(Integer.parseInt(size));
    } catch (NumberFormatException nfe) {
      throw new TransformationException("Unable to parse size value: " + nfe.getMessage());
    }
  }

  private ColumnType matchColumnType(String value) {
    ColumnType columnType = null;
    if (value != null) {
      // Add bellow all custom types that are not mached by metamodel.
      if (value.equalsIgnoreCase("INT")) {
        value = "INTEGER";
      }
      columnType = ColumnTypeImpl.valueOf(value);
    }
    return columnType;
  }
}
