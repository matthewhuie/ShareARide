package edu.cmu.andrew.sharearide.backend;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * class that abstracts the business logic
 * calls the google app engine endpoint methods
 */

public class AbstractFactory {
    Calendar calendar = Calendar.getInstance();

    /**
     * authenticates the user and against password
     * and if authenticated returns true and false otherwise
     * @return
     */
    public boolean authenticateUser(String userName) {
        boolean isValidUser = false;
        return isValidUser;
    }

    /**
     * returns all the drivers that are available right now
     * and which satisfies the number of riders and time criteria
     * @param currentTime
     * @return
     */
    public List<UserBean> getAvailableDrivers(Timestamp currentTime,int numOfRiders) {
        List<UserBean> drivers = new ArrayList<>();

        return drivers;
    }

    public UserBean getDriver(String userId){
        return getUser(userId);
    }

    public UserBean getPassenger(String userId){
        return getUser(userId);
    }

    /**
     * gets the driver record corresponding to the user ID
     * @param userId
     * @return
     */

    public UserBean getUser(String userId) {
        UserBean driver = new UserBean();

        return driver;
    }

    /**
     * updates the driver with the current new location
     * @param latitude
     * @param longitude
     * @return
     */
    public boolean updateDriverLocation(double latitude,double longitude,String userId) {
        UserBean driver =  getDriver(userId);
        boolean isupdated = false;

        return isupdated;
    }


    /**
     * gets the total num of riders for the trip
     * @param tripId
     * @return
     */

    public int getRidersForTrip(int tripId){
        int result = 0 ;


        return result;
    }

    /**
     * updates the fare and passenger rating for the request
     * @param fare
     * @param passRating
     * @param requestId
     * @return
     */
    public boolean updateFareRatingPass(double fare,float passRating,int requestId){
    //update the request table row with the new fare and rating
        RequestBean rb = new RequestBean(requestId,fare,passRating);
        boolean result = false;

        return result;
    }

    /**
     * updates the driver rating for the request
     * @param passRating
     * @param requestId
     * @return
     */
    public boolean updateDriverRating(float passRating,int requestId){
        //update the request table row with the new fare and rating
        RequestBean rb = new RequestBean(requestId,passRating);
        boolean result = false;

        return result;
    }

    /**
     * associates the trip to request
     * @param tripId
     * @param requestId
     * @return
     */

    public boolean addRequestToTrip(int tripId,int requestId){
        boolean result = false;
        //insert in the trip request table

        return result;
    }



}