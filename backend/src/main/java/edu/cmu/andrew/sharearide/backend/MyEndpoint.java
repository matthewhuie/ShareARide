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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  @ApiMethod (name = "getAvailableDrivers")
  public List<RSBean> getAvailableDrivers () {
    return query ("SELECT * FROM User WHERE user_type='Driver'",
        new String []{"user_name","longitude","latitude"});
  }

  @ApiMethod (name = "userLogin")
  public RSBean userLogin (@Named("username") String username,
                           @Named("secret") String secret,
                           @Named("userType") String userType,
                           @Named("longitude") float longitude,
                           @Named("latitude") float latitude) {
    RSBean query = query ("SELECT * FROM User WHERE user_name='" + username + "'",
        new String []{"secret","longitude","latitude"}).get (0);
    RSBean response = new RSBean ();
    String[] result = query.getData().split (",");
    if (result.length == 0 || ! secret.equals (result[0])) {
      response.setData ("false");
    } else {
      response.setData ("true");
    }

    return response;
  }

  @ApiMethod (name = "userLogout")
  public RSBean userLogout (@Named("username") String username) {
    return null;
  }

  private List<RSBean> query (String query, String... columns) {
    ArrayList<RSBean> al = new ArrayList<RSBean> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery (query);
      while (rs.next ()) {
        RSBean response = new RSBean ();
        for (String s : columns) {
          response.setData (response.getData () + " " + rs.getString (s));
        }
        al.add (response);
      }
      disconnect (conn);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
    }

    return al;
  }

  private Connection connect () throws ClassNotFoundException, SQLException {
    String url = null;
    Connection conn = null;
    if (SystemProperty.environment.value () == SystemProperty.Environment.Value.Production) {
      Class.forName ("com.mysql.jdbc.GoogleDriver");
      url = "jdbc:google:mysql://vivid-art-90101:ridesharing/ridesharing?user=cmu&password=cmu";
    } else {
      Class.forName ("com.mysql.jdbc.Driver");
      url = "jdbc:mysql://173.194.242.26:3306/ridesharing?user=cmu&password=cmu";
    }
    conn = DriverManager.getConnection (url);

    return conn;
  }

  private void disconnect (Connection conn) throws SQLException {
    conn.close ();
  }
}
