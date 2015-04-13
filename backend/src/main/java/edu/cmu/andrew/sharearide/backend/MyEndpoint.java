/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package edu.cmu.andrew.sharearide.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  /**
   * A simple endpoint method that takes a name and says Hi back
   */
  @ApiMethod (name = "getQuery")
  public MyBean getQuery (@Named ("query") String query) {
    MyBean response = new MyBean ();
    String url = null;
    try {
        Class.forName("com.mysql.jdbc.GoogleDriver");
        url = "jdbc:google:mysql://vivid-art-90101:ridesharing/ridesharing";
      Connection conn = DriverManager.getConnection (url, "ridesharing", "ridesharing");
      try {
        response.setData (conn.createStatement().executeQuery(query).getString (1));
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      e.printStackTrace ();
    }

    return response;
  }

}
