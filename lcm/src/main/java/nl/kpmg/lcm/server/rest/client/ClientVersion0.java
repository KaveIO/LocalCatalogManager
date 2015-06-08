package nl.kpmg.lcm.server.rest.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import nl.kpmg.lcm.server.metadata.MetaData;
import nl.kpmg.lcm.server.metadata.storage.file.MetaDataDaoImpl;
import org.apache.commons.lang.NotImplementedException;

/**
 *
 * @author mhoekstra
 */

@Path("client/v0")
public class ClientVersion0 {
    
    /**
     * Placeholder method for calls to a non endpoint. 
     *
     * @return String "ok" as niceness
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIndex() {
        return "ok";
    }
    
    /**
     * Get local meta data overview
     * 
     * @return 
     */
    @GET
    @Path("local")
    @Produces({"application/json"})
    public Map getLocalMetaDataOverview() {
        HashMap result = new HashMap();
        
        MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl();
        for (MetaData metadata : metaDataDaoImpl.getAll()) {
            HashMap details = new HashMap();
            details.put("url", "http://localhost:8080/client/v0/local/" + metadata.getName() + "/" + metadata.getVersionNumber());
            result.put(metadata.getName(), details);
        }
        return result;
    }
    
    /**
     * Create a new metadata item.
     *
     * @param metaDataName
     * @param metaData
     * @return String "ok" if call is successful 
     */
    @PUT
    @Path("local/{metaDataName}")
    @Consumes({"application/json"})
    @Produces(MediaType.TEXT_PLAIN)
    public String putLocalMetaData(
            @PathParam("metaDataName") String metaDataName,
            MetaData metaData) {
        
        metaData.setName(metaDataName);
        MetaDataDaoImpl metaDataDaoImpl = new MetaDataDaoImpl();
        metaDataDaoImpl.persist(metaData);
        
        return "ok";
    }

    /**
     * Get meta data of the head version of a specific meta data set.
     * 
     * @param metaDataName
     * @return 
     */
    @GET
    @Path("local/{metaDataName}")
    @Produces({"application/json"})
    public String getLocalMetaData(
            @PathParam("metaDataName") String metaDataName) {
        throw new NotImplementedException();
    }        
  
    /**
     *  Delete the entire meta data set from the LCM
     * 
     * @param metaDataName
     * @return 
     */
    @DELETE
    @Path("local/{metaDataName}")
    @Produces({"application/json"})
    public String deleteLocalMetaData(
            @PathParam("metaDataName") String metaDataName) {
        throw new NotImplementedException();
    }
    
    /**
     * Place a fetch request for given data to be stored in a local data store. 
     * 
     * @param metaDataName
     * @param metaData
     * @return 
     */
    @POST
    @Path("local/{metaDataName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public String postLocalMetaData(
            @PathParam("metaDataName") String metaDataName, 
            MetaData metaData) {
        throw new NotImplementedException();
    }
    
    /**
     * Get meta data of a specific version of a specific meta data set
     * 
     * @param metaDataName
     * @param version
     * @return 
     */
    @GET
    @Path("local/{metaDataName}/{version}")
    @Produces({"application/json"})
    public String getLocalMetaDataByVersion(
            @PathParam("metaDataName") String metaDataName, 
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }        
    
    /**
     * Delete a version of meta data about a data-set
     * 
     * @param metaDataName
     * @param version
     * @return 
     */
    @DELETE
    @Path("local/{metaDataName}/{version}")
    @Produces({"application/json"})
    public String deleteLocalMetaDataByVersion(
            @PathParam("metaDataName") String metaDataName, 
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }
    
    /**
     * Place a fetch request for given data to be stored in a local data store. 
     * 
     * @param metaDataName
     * @param version
     * @param metaData
     * @return 
     */
    @POST
    @Path("local/{metadata}/{version}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public String postLocalMetaDataByVersion(
            @PathParam("metaDataName") String metaDataName, 
            @PathParam("version") String version, 
            MetaData metaData) {
        
        // Sample code to unmarshall the metaData. not clear if this will do the trick.
        // MetaData readValue;
        // try {
        //    JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        //    ObjectMapper context = jacksonJsonProvider.getContext(MetaData.class);
        //    readValue = context.readValue(bla, MetaData.class);
        // } catch (IOException ex) {
        //    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        // }
        
        throw new NotImplementedException();
    }
    
    
    /**
     * Get an overview of the connected remote LCM's
     *
     * @return String "ok" as niceness
     */
    @GET
    @Path("remote")
    @Produces({"application/json"})
    public String getRemote() {
        throw new NotImplementedException();
    }

    /**
     * Attach a remote LCM to your local LCM.
     * 
     * @param remoteLcmName
     * @return 
     */
    @PUT
    @Path("remote/{remote_lcm_name}")
    @Produces({"application/json"})
    public String putRemoteLcmConnection(
            @PathParam("remote_lcm_name") String remoteLcmName) {
        throw new NotImplementedException();
    }
    
    /**
     * Get remote meta data overview
     * 
     * @param remoteLcmName
     * @return 
     */
    @GET
    @Path("remote/{remote_lcm_name}")
    @Produces({"application/json"})
    public String getRemoteMetaDataOverview(
            @PathParam("remote_lcm_name") String remoteLcmName) {
        throw new NotImplementedException();
    }
    
    /**
     * Get meta data of the head version of a specific meta data set
     * 
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @return 
     */
    @GET
    @Path("remote/{remote_lcm_name}/{remote_meta_data}")
    @Produces({"application/json"})
    public String getRemoteMetaData(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName) {
        throw new NotImplementedException();
    }
    
    /**
     * Get meta data of a specific version of a specific meta data set
     * 
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @param version
     * @return 
     */
    @GET
    @Path("remote/{remote_lcm_name}/{remote_meta_data}/{version}")
    @Produces({"application/json"})
    public String getRemoteMetaDataByVersion(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName,
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }
    
    /**
     *  Attach the remote meta-data to your own LCM, This causes the meta data to become available in like local meta-data. 
     * 
     * @param remoteLcmName
     * @param remoteMetaDataName
     * @param version
     * @return 
     */
    @POST
    @Path("remote/{remote_lcm_name}/{remote_meta_data}")
    @Produces({"application/json"})
    public String postRemoteAttachMetaData(
            @PathParam("remote_lcm_name") String remoteLcmName,
            @PathParam("remote_meta_data") String remoteMetaDataName,
            @PathParam("version") String version) {
        throw new NotImplementedException();
    }
    
    /**
     * Get an overview of the connected storage handlers.
     *
     * @return 
     */
    @GET
    @Path("storage")
    @Produces({"application/json"})
    public String getStorage() {
        throw new NotImplementedException();
    }
    
    /**
     * Get an overview of the connected storage handlers.
     *
     * @param storageHandlerName
     * @return 
     */
    @GET
    @Path("storage/{storage_handler_name}")
    @Produces({"application/json"})
    public String getStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }
    
    /**
     * Delete a specific storage handler.
     *
     * @param storageHandlerName
     * @return 
     */
    @DELETE
    @Path("storage/{storage_handler_name}")
    @Produces({"application/json"})
    public String deleteStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }
    
    /**
     * Add a specific storage handler.
     *
     * @param storageHandlerName
     * @return 
     */
    @PUT
    @Path("storage/{storage_handler_name}")
    @Produces({"application/json"})
    public String putStorageHandler(
            @PathParam("storage_handler_name") String storageHandlerName) {
        throw new NotImplementedException();
    }

    /**
     * Get a list of all the commands running 
     *
     * @return 
     */
    @GET
    @Path("commands")
    @Produces({"application/json"})
    public String getCommands() {
        throw new NotImplementedException();
    }
    
    /**
     * Get information about a specific command
     *
     * @param commandId
     * @return 
     */
    @GET
    @Path("commands/{command_id}")
    @Produces({"application/json"})
    public String getCommand(
            @PathParam("command_id") String commandId) {
        throw new NotImplementedException();
    }
    
    /**
     * Unschedule a command 
     *
     * @param commandId
     * @return 
     */
    @DELETE
    @Path("commands/{command_id}")
    @Produces({"application/json"})
    public String deleteCommand(
            @PathParam("command_id") String commandId) {
        throw new NotImplementedException();
    }
}
