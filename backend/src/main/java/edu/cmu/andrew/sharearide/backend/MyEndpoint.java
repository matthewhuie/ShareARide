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
  public List<UserBean> getAvailableDrivers () {
    return queryUser ("user_type='Driver'");
  }

  /**@ApiMethod (name = "userLogin")
  public UserBean userLogin (@Named("username") String username,
                           @Named("secret") String secret,
                           @Named("userType") String userType,
                           @Named("longitude") float longitude,
                           @Named("latitude") float latitude) {
    UserBean query = queryUser ("user_name='" + username + "'");
    UserBean response = new UserBean ();
    String[] result = query.getData().split (",");
    if (result.length == 0 || ! secret.equals (result[0])) {
      response.setData ("false");
    } else {
      response.setData ("true");
    }

    return response;
  }*/

  @ApiMethod (name = "userLogout")
  public UserBean userLogout (@Named("username") String username) {
    return null;
  }

  public List<UserBean> queryUser (@Named("where") String where) {
    ArrayList<UserBean> al = new ArrayList<UserBean> ();
    try (Connection conn = connect ()) {
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM User WHERE" + where);
      while (rs.next ()) {
        UserBean ub = new UserBean ();
        ub.setUserID (rs.getInt (0));
        ub.setUserName (rs.getString (1));
        ub.setSecret (rs.getString (2));
        ub.setFirstName (rs.getString (3));
        ub.setLastName (rs.getString (4));
        ub.setPhoneNumber (rs.getInt (5));
        ub.setEmail (rs.getString (6));
        ub.setLongitude (rs.getDouble (7));
        ub.setLatitude (rs.getDouble (8));
        ub.setUserType (rs.getString (9));
        al.add (ub);
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
