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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  // https://apis-explorer.appspot.com/apis-explorer/?base=https://vivid-art-90101.appspot.com/_ah/api#p/shareARideApi/v1/

  private static final Logger log = Logger.getLogger (MyEndpoint.class.getName ());
  private static final Calendar calendar = Calendar.getInstance ();

  //change!!!
  @ApiMethod (name = "getAvailableDrivers")
  public List<UserBean> getAvailableDrivers (@Named ("numOfRiders") int numOfRiders) {
    int maxInCurr = 4 - numOfRiders;
    List<Integer> validDriverIds = new ArrayList<> ();
    List<UserBean> drivers = queryUser ("user_type='Driver'");
    List<UserBean> validDrivers = new ArrayList<> ();
    for (UserBean driver : drivers) {
      int driver_user_id = driver.getUserID ();
      List<TripBean> validTrips = queryTrip ("driver_user_id=" + driver_user_id + " AND num_riders<=" + maxInCurr + " AND is_ended=0");
      if (validTrips != null && validTrips.size () > 0) {
        TripBean validTrip = validTrips.get (0);
        validDriverIds.add (validTrip.getDriverUserId ());
      }
    }

    for (Integer validDriverId : validDriverIds) {
      List<UserBean> validDriversById = queryUser ("user_id=" + validDriverId);
      if (validDriversById != null && validDriversById.size () > 0) {
        UserBean validDriver = validDriversById.get (0);
        validDrivers.add (validDriver);
      }
    }

    return validDrivers;
  }


  @ApiMethod (name = "pollMessage")
  public MessageBean pollMessage (@Named ("userName") String userName) {
    MessageBean mb = getMessage (userName);
    return mb;
  }

  /**
   * adds a new request in the request table and returns true for successful inserts
   *
   * @param passenger
   * @param srcLat
   * @param srcLong
   * @param destLat
   * @param destLong
   * @return
   */
  @ApiMethod (name = "createNewRequest")
  public MessageBean createNewRequest (@Named ("passenger") String passenger, @Named ("srcLat") double srcLat,
                                       @Named ("srcLong") double srcLong, @Named ("destLat") double destLat,
                                       @Named ("destLong") double destLong, @Named ("riders") int riders) {
    //Date startTime = calendar.getTime ();

    RequestBean rb = new RequestBean (getPassenger (passenger).getUserID (), srcLat, srcLong, destLat, destLong, riders);
    int result = updateRequest (rb);
    MessageBean mb = new MessageBean ();
    System.out.println (result + "----result");
    if (result == 0)
      mb.setStatus (false);
    else {
      mb.setStatus (true);
      mb.setMessage ("New Request");
      mb.setUser_name (passenger);
      mb.setRequest_id (getPrimaryKey ());
      updateMessage (mb);
    }

    return mb;
  }

  @ApiMethod (name = "fulfillRequest")
  public RequestBean fulfillRequest (@Named ("request_id") int request_id) {

    RequestBean rb = getRequest (request_id);
    if (rb != null) {
      rb.setServed (1);
      updateRequest (rb);
      return rb;
    }
    return null;
  }


  @ApiMethod (name = "getUser")
  public UserBean getUserDetails (@Named ("userName") String userName) {
    UserBean ub = getUser (userName);
    return ub;
  }

  @ApiMethod (name = "userLogin")
  public UserBean userLogin (
      @Named ("username") String username,
      @Named ("secret") String secret,
      @Named ("latitude") double latitude,
      @Named ("longitude") double longitude,
      @Named ("user_type") String userType) {
    List<UserBean> users = queryUser ("user_name='" + username + "' AND secret='" + secret + "'");
    if (users != null && users.size () > 0) {
      UserBean ub = users.get (0);
      ub.setLatitude (latitude);
      ub.setLongitude (longitude);
      ub.setUserType (userType);
      updateUser (ub);
      return ub;
    }
    return null;
  }

  @ApiMethod (name = "startTrip")
  public TripBean startTrip (@Named ("driver_username") String driverUsername) {
    return null;
  }

  private MessageBean getMessage (String userName) {
    MessageBean mb = new MessageBean ();
    List<MessageBean> al = new ArrayList<> ();
    al = queryMessage ("user_name='" + userName + "'");

    if (al.size () > 0) {
      mb = al.get (0);
    }
    return mb;
  }

  private UserBean getPassenger (String userId) {
    return getUser (userId);
  }

  private UserBean getUser (String userId) {
    UserBean user = new UserBean ();
    List<UserBean> users = queryUser ("user_name='" + userId + "'");
    if (users != null && users.size () > 0) {
      user = users.get (0);
    }
    return user;
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

  @ApiMethod (name = "getTrip")
  public TripBean getTrip (@Named ("driverId") int driverId) {
    TripBean trip = new TripBean ();
    List<TripBean> trips = queryTrip ("driver_user_id='" + driverId + "' AND is_ended=0");
    if (trips != null && trips.size () > 0) {
      trip = trips.get (0);
    }
    return trip;
  }

  @ApiMethod (name = "endTrip")
  public void endTrip (@Named ("driverId") int driverId) {
    TripBean tb = getTrip (driverId);
    tb.setIsActive (0);
    tb.setHasEnded (1);
    updateTrip (tb);
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
        ub.setIsActive (rs.getInt (4));
        ub.setHasEnded (rs.getInt (5));
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


  @ApiMethod (name = "getRequest")
  public RequestBean getRequest (@Named ("request_id") int request_id) {
    RequestBean request = new RequestBean ();
    List<RequestBean> requests = queryRequest ("request_id=" + request_id);
    if (requests != null && requests.size () > 0) {
      request = requests.get (0);
    }
    return request;
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
        ub.setServed (rs.getInt (13));
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

  private List<MessageBean> queryMessage (String where) {
    ArrayList<MessageBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Message WHERE " + where);
      while (rs.next ()) {
        MessageBean mb = new MessageBean ();
        mb.setUser_name (rs.getString (1));
        mb.setMessage (rs.getString (2));
        mb.setMessage_id (rs.getInt (3));
        mb.setIs_read (rs.getBoolean (4));
        al.add (mb);
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


  private int updateUser (UserBean ub) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO User (user_id, user_name, secret, first_name, last_name," +
          "phone_number, email, longitude, latitude, user_type) VALUES (" + ub.getUserID () + ", \"" +
          ub.getUserName () + "\", \"" + ub.getSecret () + "\", \"" + ub.getFirstName () + "\", \"" +
          ub.getLastName () + "\", " + ub.getPhoneNumber () + ", \"" + ub.getEmail () + "\", " +
          ub.getLongitude () + ", " + ub.getLatitude () + ", \"" + ub.getUserType () + "\") " +
          "ON DUPLICATE KEY UPDATE user_name=VALUES(user_name), " +
          "secret=VALUES(secret), first_name=VALUES(first_name), last_name=VALUES(last_name)," +
          "phone_number=VALUES(phone_number), email=VALUES(email), longitude=VALUES(longitude)," +
          "latitude=VALUES(latitude), user_type=VALUES(user_type)");
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return result;
  }

  private int updateRequest (RequestBean rb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Request (request_id, pass_user_id, source_longitude, source_latitude, " +
          "dest_longitude, dest_latitude, fare, latest_time, pass_rating, driver_rating, start_time, end_time," +
          "is_served, estimated_distance) " +
          "VALUES (" + rb.getRequestId () + ", " + rb.getPassUserId () + ", " + rb.getSrcLongitude () +
          ", " + rb.getSrcLatitude () + ", " + rb.getDstLongitude () + ", " + rb.getDstLatitude () +
          ", " + rb.getFare () + ", " + rb.getLatestTime () + ", " + rb.getPassRating () + ", " +
          rb.getDriverRating () + ", now(), " + rb.getEndTime () + ", " +
          rb.isServed () + ", " + rb.getDistanceEstimated () + ") " +
          "ON DUPLICATE KEY UPDATE pass_user_id=VALUES(pass_user_id), " +
          "source_longitude=VALUES(source_longitude), source_latitude=VALUES(source_latitude), dest_longitude=VALUES(dest_longitude), " +
          "dest_latitude=VALUES(dest_latitude), fare=VALUES(fare), latest_time=VALUES(latest_time), pass_rating=VALUES(pass_rating), " +
          "driver_rating=VALUES(driver_rating), start_time=VALUES(start_time), end_time=VALUES(end_time), is_served=VALUES(is_served), " +
          "estimated_distance=VALUES(estimated_distance)");
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }
    return result;
  }


  @ApiMethod (name = "updateTrip")
  public TripBean updateTrip (@Named ("driverId") int driverId, @Named ("numOfRiders") int numOfRiders) {
    TripBean tb = getTrip (driverId);
    if (tb == null) {
      tb = new TripBean ();
      tb.setDriverUserId (driverId);
    }
    int currRider = tb.getNumOfRiders ();
    tb.setNumOfRiders (currRider + numOfRiders);
    updateTrip (tb);
    return tb;
  }

  private int updateTrip (TripBean tb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Trip (trip_id, driver_user_id, num_riders, is_active, is_ended) " +
          "VALUES (" + tb.getTripId () + ", " + tb.getDriverUserId () + ", " + tb.getNumOfRiders () +
          ", " + tb.getIsActive () + ", " + tb.getHasEnded () + ") " +
          "ON DUPLICATE KEY UPDATE driver_user_id=VALUES(driver_user_id), " +
          "num_riders=VALUES(num_riders), is_active=VALUES(is_active), is_ended=VALUES(is_ended)");
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }
    return result;
  }


  @ApiMethod (name = "updateTripRequest")
  public TripRequestBean updateTripRequest (@Named ("tripId") int tripId, @Named ("requestId") int requestId) {
    TripRequestBean trb = new TripRequestBean (tripId, requestId);
    updateTripRequest (trb);
    return null;
  }

  private int getPrimaryKey () {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT LAST_INSERT_ID()");
      while (rs.next ()) {

        return rs.getInt (0);
      }
    } catch (Exception e) {

    }
    return result;
  }

  private int updateTripRequest (TripRequestBean trb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Trip_Request (trip_id, request_id) " +
          "VALUES (" + trb.getTripId () + ", " + trb.getRequestId () + ")");
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return result;
  }

  private int updateMessage (MessageBean mb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Message (user_name, message, message_id, is_read)" +
          " VALUES (\"" + mb.getUser_name () + "\", \"" +
          mb.getMessage () + "\", \"" + mb.getMessage_id () + "\", \"" + mb.isIs_read () + "\")" +
          "ON DUPLICATE KEY UPDATE user_name=VALUES(user_name), " +
          "message=VALUES(message), message_id=VALUES(message_id), is_read=VALUES(is_read)");

    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return result;
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
