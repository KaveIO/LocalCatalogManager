/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.kpmg.lcm.common.client;

import nl.kpmg.lcm.common.configuration.ClientConfiguration;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

/**
 *
 * @author shristov
 */
public class HttpClientFactory {

  private HttpAuthenticationFeature credentials;
  private ClientConfiguration configuration;
  private String baseUrl;

  public HttpClientFactory(ClientConfiguration configuration, HttpAuthenticationFeature credentials) {
    this.credentials = credentials;
    this.configuration =  configuration;
    baseUrl  = String.format("%s://%s:%s/", "http", configuration.getTargetHost(),
        configuration.getUnsafeTargetPort());
  }

  public Client createClient(){
       ClientBuilder builder = ClientBuilder.newBuilder();
       return builder.register(credentials).build();
  }

  public WebTarget getTarget(String fetchUrl) {
      return createClient().target(baseUrl + fetchUrl);
  }
}
