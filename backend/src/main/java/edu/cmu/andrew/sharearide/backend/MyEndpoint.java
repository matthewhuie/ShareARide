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

/**
 * MyEndpoint class contains all the API methods to interact with
 * MySql instance on Google cloud
 */
@Api (name = "shareARideApi", version = "v1", namespace = @ApiNamespace (
    ownerDomain = "backend.sharearide.andrew.cmu.edu",
    ownerName = "backend.sharearide.andrew.cmu.edu",
    packagePath = ""))
public class MyEndpoint {

  private static final Logger log = Logger.getLogger (MyEndpoint.class.getName ());
  private static final Calendar calendar = Calendar.getInstance ();

    /**
     * the reset method resets the users in the User table
     * and ends all old trips
     */

  @ApiMethod (name = "reset")
  public void reset () {
    List<UserBean> users = queryUser ("user_type!=''");
    for (UserBean ub : users) {
      ub.setUserType ("");
      ub.setLongitude (0);
      ub.setLatitude (0);
      updateUser (ub);
      endPreviousTrips (ub.getUserID ());
    }

    List<MessageBean> messages = queryMessage ("is_read=0");
    for (MessageBean mb : messages) {
      mb.setIs_read (1);
      updateMessage (mb);
    }
  }

    /**
     * updates the longitude and latitude of the user
     * in the User table
     * @param userID  user whose location needs to be updated
     * @param latitude new latitude
     * @param longitude new longitude
     * @return
     */

  @ApiMethod (name = "updateLocation")
  public UserBean updateLocation (@Named ("userID") int userID, @Named ("latitude") double latitude, @Named ("longitude") double longitude) {
    UserBean ub = queryUser ("user_id=" + userID).get(0);
    ub.setLatitude (latitude);
    ub.setLongitude (longitude);
    updateUser (ub);
    return ub;
  }

    /**
     * Get drivers who have the capacity for the current request API method
     *
     * @param numOfRiders number of riders in the current request
     * @return drivers met the requirement
     */
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

    /**
     * creates a new message for the user
     * @param userID user for whom the message is intended
     * @param message   message text
     * @param request_id  the request id for which the message is created
     */
    @ApiMethod (name = "createMessage")
    public void createMessage (@Named ("userID") int userID,@Named("message") String message,@Named("requestId") int request_id) {
        MessageBean mb = new MessageBean();
        mb.setMessage(message);
        mb.setRequest_id(request_id);
        mb.setUser_name(userID);
        mb.setIs_read(0);
        updateMessage(mb);
    }

    /**
     * checks if the userID has any messages for
     * it to act upon
     * @param userID User for which to check for messages
     * @return
     */

  @ApiMethod (name = "pollMessage")
  public MessageBean pollMessage (@Named ("userID") int userID) {
    return getMessage (userID);
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
                                       @Named ("destLong") double destLong,@Named("riders") int riders,
                                       @Named ("estimatedDist") double estiDist, @Named ("estimatedTime") double estiTime,@Named("estiFare") double estiFare) {
    RequestBean rb = new RequestBean (getPassenger (passenger).getUserID (), srcLat, srcLong, destLat, destLong,riders);
      rb.setDistanceEstimated(estiDist);
      rb.setEstimatedTime(estiTime);
      rb.setEstimatedFare(estiFare);
    int request_id = updateRequest (rb);
    MessageBean mb = new MessageBean ();
      System.out.println(request_id + "----result");
    if (request_id == -1)
      mb.setStatus (false);
    else {
      mb.setStatus (true);
      mb.setMessage ("New Request");
     // mb.setUser_name (passenger);
      mb.setRequest_id(request_id);
     // updateMessage (mb);
    }

    return mb;
  }

    /**
     * Set a request status as fulfilled in Request table by request id API method
     *
     * @param request_id current request id
     * @return request met the requirement
     */
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


    /**
     * Get a user record from User table by user id API method
     *
     * @param userID current user id
     * @return user met the requirement
     */
  @ApiMethod (name = "getUserByID")
  public UserBean getUserByID (@Named ("userID") int userID) {
    List<UserBean> users = queryUser ("user_id=" + userID);
    if (users != null && users.size () > 0) {
      return users.get (0);
    }
    return null;
  }

    /**
     * checks if user exists in the user table
     * against user and password and if exists sets the user type
     * and current location
     * @param username
     * @param secret
     * @param latitude
     * @param longitude
     * @param userType
     * @return
     */

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

    /**
     *
     * @param driverUsername
     * @return
     */

  @ApiMethod (name = "startTrip")
  public TripBean startTrip (@Named ("driver_username") String driverUsername) {
    return null;
  }

    /**
     * gets the message from the user
     * and marks it as read
     * @param userID
     * @return
     */

    private MessageBean getMessage (int userID) {
    List<MessageBean> al = new ArrayList<> ();
    al = queryMessage ("user_name=" + userID + " AND is_read=0");

    if (al.size () > 0) {
      MessageBean mb = al.get (0);
      mb.setIs_read (1);
      updateMessage (mb);
      return mb;
    }
    return null;
  }

    /**
     * Get a passenger record from User table by user id
     *
     * @param userId current user id
     * @return user met the requirement
     */
  private UserBean getPassenger (String userId) {
    return getUser (userId);
  }

    /**
     * Get a user record from User table by user id
     *
     * @param userId current user id
     * @return user met the requirement
     */
  private UserBean getUser (String userId) {
    UserBean user = new UserBean ();
    List<UserBean> users = queryUser ("user_name='" + userId + "'");
    if (users != null && users.size () > 0) {
      user = users.get (0);
    }
    return user;
  }

    /**
     * Get a user record from User table
     *
     * @param where the pre-condition of the query
     * @return user met the requirement
     */
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

    /**
     * Get a trip record from Trip table API method
     *
     * @param driverId the driverid for the current trip
     * @return trip met the requirement
     */
  @ApiMethod (name = "getTrip")
  public TripBean getTrip (@Named ("driverId") int driverId) {
    TripBean trip = new TripBean ();
    List<TripBean> trips = queryTrip ("driver_user_id='" + driverId + "' AND is_ended=0");
    if (trips != null && trips.size () > 0) {
      return trips.get (0);
    }
    return null;
  }

    /**
     * Perform taxi searching business logic, insert a trip request, then
     * change the request status as fulfilled
     *
     * @param request_id current request id has been fulfilled
     * @param minDriverID driver assigned for the request
     * @param numOfRiders number of riders in the current request
     */
  @ApiMethod (name = "taxiSearching")
  public void taxiSearching (@Named ("request_id") int request_id,
                             @Named ("minDriverID") int minDriverID,
                             @Named ("numOfRiders") int numOfRiders) {
    int tripId = getTrip (minDriverID).getTripId ();
    updateTripRequest(tripId, request_id);
    fulfillRequest(request_id);
  }

    /**
     * ends all trips by marking them
     * as ended and not active
     * @param driverId
     */

  @ApiMethod (name = "endPreviousTrips")
  public void endPreviousTrips (@Named ("driverId") int driverId) {
    List<TripBean> trips = queryTrip ("driver_user_id='" + driverId + "' AND is_ended=0");
    if (trips != null && trips.size () > 0) {
      for (TripBean tb : trips) {
        tb.setHasEnded (1);
        tb.setIsActive (0);
        updateTrip (tb);
      }
    }
  }

    /**
     * Set driver's availability upon change
     *
     * @param driverId driver's id
     */
  @ApiMethod (name = "endTrip")
  public void endTrip (@Named ("driverId") int driverId) {
    TripBean tb = getTrip (driverId);
    tb.setIsActive (0);
    tb.setHasEnded (1);
    updateTrip (tb);
  }

    /**
     * Get a trip record from Trip table
     *
     * @param where the pre-condition of the query
     * @return trip met the requirement
     */
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

    /**
     * Get a request record from Request table API method
     *
     * @param request_id request id info
     * @return request met the requirement
     */
  @ApiMethod (name = "getRequest")
  public RequestBean getRequest (@Named ("request_id") int request_id) {
    List<RequestBean> requests = queryRequest ("request_id=" + request_id);
    if (requests != null && requests.size () > 0) {
      return requests.get (0);
    }
    return null;
  }

    /**
     * adds updated fare to the old fare
     * to store cumulative
     * @param requestID
     * @param fareToAdd
     * @return
     */

  @ApiMethod (name = "updateFare")
  public RequestBean updateFare (@Named ("request_id") int requestID, @Named ("fareToAdd") double fareToAdd) {
    RequestBean rb = getRequest (requestID);
    if (rb != null) {
      rb.setFare (rb.getFare () + fareToAdd);
      updateRequest (rb);
      return rb;
    }
    return null;
  }

    /**
     * adds new distance to
     * original distance and new time to original
     * time
     * @param requestID
     * @param distanceToAdd
     * @param timeToAdd
     * @return
     */

  @ApiMethod (name = "updateDistanceTime")
  public RequestBean updateDistanceTime (@Named ("request_id") int requestID,
                                         @Named ("distanceToAdd") double distanceToAdd,
                                         @Named ("timeToAdd") double timeToAdd) {
    RequestBean rb = getRequest (requestID);
    if (rb != null) {
      rb.setActualDistance (rb.getActualDistance () + distanceToAdd);
      rb.setActualDuration (rb.getActualDuration () + timeToAdd);
      updateRequest (rb);
      return rb;
    }
    return null;
  }


    /**
     * updates the request with the end time as current time
     * @param requestID
     * @return
     */
    @ApiMethod (name = "updateEndTime")
    public RequestBean updateEndTime (@Named ("request_id") int requestID) {
        RequestBean rb = getRequest (requestID);
        if (rb != null) {
            rb.setEndTime(""+calendar.getTimeInMillis());
            updateRequest (rb);
            return rb;
        }
        return null;
    }

    /**
     * Get a request record from Request table
     *
     * @param where the pre-condition of the query
     * @return request met the requirement
     */
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
        ub.setPassRating (rs.getFloat (8));
        ub.setDriverRating (rs.getFloat (9));
        ub.setStartTime (rs.getString (10));
        ub.setEndTime (rs.getString (11));
        ub.setServed (rs.getInt (12));
        ub.setDistanceEstimated (rs.getDouble (13));
        ub.setNumOfRiders (rs.getInt (14));
        ub.setEstimatedTime (rs.getDouble (15));
        ub.setActualDistance (rs.getDouble (16));
        ub.setEstimatedFare (rs.getDouble (17));
        ub.setActualDuration (rs.getDouble (18));
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

    /**
     * Get a tripreuqest record from TripRequest table
     *
     * @param where the pre-condition of the query
     * @return triprequest met the requirement
     */
  private List<TripRequestBean> queryTripRequest (String where) {
    ArrayList<TripRequestBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Trip_Request WHERE " + where);
      while (rs.next ()) {
        TripRequestBean ub = new TripRequestBean ();
        ub.setTripId(rs.getInt(1));
        ub.setRequestId(rs.getInt(2));
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

    /**
     * Get a unread row from Message table for certain user
     *
     * @param where the pre-condition of the query
     * @return message to be read
     */
  private List<MessageBean> queryMessage (String where) {
    ArrayList<MessageBean> al = new ArrayList<> ();
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      ResultSet rs = statement.executeQuery ("SELECT * FROM Message WHERE " + where);
      while (rs.next ()) {
        MessageBean mb = new MessageBean ();
        mb.setUser_name (rs.getInt (1));
        mb.setMessage (rs.getString (2));
        mb.setMessage_id (rs.getInt (3));
        mb.setIs_read (rs.getInt (4));
        mb.setRequest_id (rs.getInt (5));
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

    /**
     * Insert or update a row in User table
     *
     * @param ub current UserBean
     * @return record successful or not indicator
     */
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

    /**
     * Insert or update a row in Request table
     *
     * @param rb current RequestBean
     * @return record successful or not indicator
     */
  private int updateRequest (RequestBean rb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Request (request_id, pass_user_id, source_longitude, source_latitude, " +
          "dest_longitude, dest_latitude, fare, pass_rating, driver_rating, start_time, end_time," +
          "is_served, estimated_distance,num_riders,estimated_time,actual_distance,estimated_fare,actual_duration) " +
          "VALUES (" + rb.getRequestId () + ", " + rb.getPassUserId () + ", " + rb.getSrcLongitude () +
          ", " + rb.getSrcLatitude () + ", " + rb.getDstLongitude () + ", " + rb.getDstLatitude () +
          ", " + rb.getFare () + "," + rb.getPassRating () + ", " +
          rb.getDriverRating () + ", " +calendar.getTimeInMillis()+"," + rb.getEndTime () + ", " +
          rb.isServed () + ", " + rb.getDistanceEstimated () + ", " + rb.getNumOfRiders () + ", " + rb.getEstimatedTime() +
          "," + rb.getActualDistance()+"," + rb.getEstimatedFare() +", " + rb.getActualDuration () + ") " +
          "ON DUPLICATE KEY UPDATE pass_user_id=VALUES(pass_user_id), " +
          "source_longitude=VALUES(source_longitude), source_latitude=VALUES(source_latitude), dest_longitude=VALUES(dest_longitude), " +
          "dest_latitude=VALUES(dest_latitude), fare=VALUES(fare),pass_rating=VALUES(pass_rating), " +
          "driver_rating=VALUES(driver_rating), start_time=VALUES(start_time), end_time=VALUES(end_time), is_served=VALUES(is_served), " +
          "estimated_distance=VALUES(estimated_distance), " + "num_riders=VALUES(num_riders)," + "estimated_time=VALUES(estimated_time),"
              + "actual_distance=VALUES(actual_distance)," + "estimated_fare=VALUES(estimated_fare)," + "actual_duration=VALUES(actual_duration)",  Statement.RETURN_GENERATED_KEYS);

        ResultSet rs = statement.getGeneratedKeys();
        rs.next();
        result = rs.getInt(1);
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }
    return result;
  }

    /**
     * Insert or update a row in Trip table API method
     */
  @ApiMethod (name = "updateTrip")
  public TripBean updateTrip (@Named ("driverId") int driverId, @Named ("numOfRiders") int numOfRiders) {
    TripBean tb = getTrip(driverId);
    if (tb == null) tb = new TripBean (driverId);
    int currRider = tb.getNumOfRiders();
    tb.setNumOfRiders(currRider+numOfRiders);
    updateTrip (tb);
    return tb;
  }

    /**
     * Insert or update a row in Trip table
     *
     * @param tb current TripBean
     * @return record successful or not indicator
     */
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

    /**
     * Insert a row into TripRequest table API method
     */
  @ApiMethod (name = "updateTripRequest")
  public TripRequestBean updateTripRequest (@Named ("tripId") int tripId, @Named ("requestId") int requestId) {
    TripRequestBean trb = new TripRequestBean (tripId, requestId);
    updateTripRequest (trb);
    return null;
  }

    private int getPrimaryKey(){
        int result = -1;
        try{
            Connection conn = connect ();
            Statement statement = conn.createStatement ();
            ResultSet rs = statement.executeQuery ("SELECT LAST_INSERT_ID()");
            while (rs.next ()) {

               return rs.getInt(0);
            }
        }catch(Exception e){

        }
        return result;
    }

    /**
     * Insert a row into TripRequest table
     *
     * @param trb current TripRequestBean
     * @return record successful or not indicator
     */
  private int updateTripRequest (TripRequestBean trb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Trip_Request (trip_id, request_id) " +
          "VALUES (" + trb.getTripId() + ", " + trb.getRequestId() + ")");
    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return result;
  }


    /**
     * updates the message table with values
     * that need to be updated
     * @param mb
     * @return
     */
  private int updateMessage (MessageBean mb) {
    int result = -1;
    try {
      Connection conn = connect ();
      Statement statement = conn.createStatement ();
      result = statement.executeUpdate ("INSERT INTO Message (user_name, message, message_id, is_read, Request_id)" +
          " VALUES (" + mb.getUser_name () + ", \"" + mb.getMessage() + "\", " + mb.getMessage_id() + ", " +
          mb.isIs_read() + ", " + mb.getRequest_id () + ")" +
          "ON DUPLICATE KEY UPDATE user_name=VALUES(user_name), " +
          "message=VALUES(message), is_read=VALUES(is_read), Request_id=VALUES(Request_id)");

    } catch (Exception e) {
      StringWriter sw = new StringWriter ();
      PrintWriter pw = new PrintWriter (sw);
      e.printStackTrace (pw);
      log.severe (sw.toString ());
    }

    return result;
  }

    /**
     * helper method to connect to the MySql instance
     * on Google cloud
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */


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
