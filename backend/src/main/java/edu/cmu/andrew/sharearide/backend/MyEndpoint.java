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
import java.util.logging.Logger;

import javax.inject.Named;

@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  private static final Logger log = Logger.getLogger(MyEndpoint.class.getName());

  @ApiMethod (name = "getAvailableDrivers")
  public List<UserBean> getAvailableDrivers () {
    return queryUser ("user_type='Driver'");
  }

  private List<UserBean> queryUser (String where) {
    ArrayList<UserBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM User WHERE " + where);
      while (rs.next ()) {
        UserBean ub = new UserBean ();
        ub.setUserID (rs.getInt (1));
        ub.setUserName (rs.getString (2));
        ub.setSecret (rs.getString (3));
        ub.setFirstName (rs.getString (4));
        ub.setLastName (rs.getString (5));
        ub.setPhoneNumber (rs.getInt (6));
        ub.setEmail (rs.getString (7));
        ub.setLongitude (rs.getDouble (8));
        ub.setLatitude (rs.getDouble (9));
        ub.setUserType (rs.getString (10));
        al.add (ub);
      }
      disconnect (conn);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return al;
  }

  private List<TripBean> queryTrip (String where) {
    ArrayList<TripBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Trip WHERE " + where);
      while (rs.next ()) {
        TripBean ub = new TripBean ();
        ub.setTripId (rs.getInt (1));
        ub.setDriverUserId (rs.getInt (2));
        ub.setNumOfRiders (rs.getInt (3));
        ub.setActive (rs.getBoolean (4));
        ub.setHasEnded (rs.getBoolean (5));
        al.add (ub);
      }
      disconnect (conn);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return al;
  }

  private List<RequestBean> queryRequest (String where) {
    ArrayList<RequestBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Request WHERE " + where);
      while (rs.next ()) {
        RequestBean ub = new RequestBean ();
        ub.setRequestId (rs.getInt (1));
        ub.setPassUserId (rs.getInt (2));
        ub.setSrcLongitude (rs.getDouble (3));
        ub.setSrcLatitude (rs.getDouble (4));
        ub.setDstLongitude (rs.getDouble (5));
        ub.setDstLatitude (rs.getDouble (6));
        ub.setFare (rs.getDouble (7));
        ub.setLatestTime (rs.getTimestamp (8));
        ub.setPassRating (rs.getInt (9));
        ub.setDriverRating (rs.getInt (10));
        ub.setStartTime (rs.getTimestamp (11));
        ub.setEndTime (rs.getTimestamp (12));
        ub.setServed (rs.getBoolean (13));
        al.add (ub);
      }
      disconnect (conn);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return al;
  }

  private List<TripRequestBean> queryTripRequest (String where) {
    ArrayList<TripRequestBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Trip_Request WHERE " + where);
      while (rs.next ()) {
        TripRequestBean ub = new TripRequestBean ();
        ub.setTripId (rs.getInt (1));
        ub.setRequestId (rs.getInt (2));
        al.add (ub);
      }
      disconnect (conn);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
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
