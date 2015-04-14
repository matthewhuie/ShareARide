/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package edu.cmu.andrew.sharearide.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.utils.SystemProperty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  @ApiMethod (name = "getAvailableDrivers")
  public MyBean getAvailableDrivers () {
    return query ("SELECT * FROM User WHERE user_type='Driver'",
        new String []{"user_name","longitude","latitude"});
  }

  @ApiMethod (name = "userLogin")
  public MyBean userLogin (@Named("username") String username,
                           @Named("secret") String secret,
                           @Named("longitude") float longitude,
                           @Named("latitude") float latitude) {
    MyBean query = query ("SELECT * FROM User WHERE user_name='" + username + "'",
        new String []{"secret","longitude","latitude"});
    MyBean response = new MyBean ();
    String[] result = query.getData().split (",");
    if (result.length == 0 || ! secret.equals (result[0])) {
      response.setData ("false");
    } else {
      response.setData ("true");
    }

    return response;
  }

  private MyBean query (String query, String... columns) {
    MyBean response = new MyBean ();
    String url = null;
    try {
      if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
        Class.forName ("com.mysql.jdbc.GoogleDriver");
        url = "jdbc:google:mysql://vivid-art-90101:ridesharing/ridesharing?user=cmu&password=cmu";
      } else {
        Class.forName ("com.mysql.jdbc.Driver");
        url = "jdbc:mysql://173.194.242.26:3306/ridesharing?user=cmu&password=cmu";
      }
        Connection conn = DriverManager.getConnection (url);
      try {
        Statement statement = conn.createStatement ();
        ResultSet rs = statement.executeQuery (query);
        while (rs.next ()) {
          for (String s : columns) {
            response.appendData (rs.getString (s));
          }
          response.appendData ("|");
        }
      } finally {
        conn.close();
      }
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      response.setData (sw.toString());
    }

    return response;
  }

}
