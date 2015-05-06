package edu.cmu.andrew.sharearide.backend;

/**
 * Created by Aditi on 4/18/2015.
 * this bean corresponds to the columns in the Message table
 * in the MySql google instance
 */
public class MessageBean {

    private boolean status;
    private int user_name;
    private String message;
    private int message_id;
    private int is_read;
    private int request_id;


    /**
     * gets the Request_id column value
     * @return
     */
    public int getRequest_id() {
        return request_id;
    }

    /**
     * sets the Request_id column value
     * @param request_id
     */

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    /**
     * gets the user_name column value from the message table
     * @return
     */

    public int getUser_name() {
        return user_name;
    }

    /**
     * sets the user_name column value for  the message table
     * @param user_name
     */

    public void setUser_name(int user_name) {
        this.user_name = user_name;
    }

    /**
     * gets the message text from the Message table
     * @return
     */


    public String getMessage() {
        return message;
    }

    /**
     * sets the message text for the message table
     * @param message
     */

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * gets the message_id column value from the message table
     * @return
     */
    public int getMessage_id() {
        return message_id;
    }


    /**
     * sets the message_id column from the message table
     * @param message_id
     */
    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    /**
     * gets the value of is_read column in the message table
     * @return
     */

    public int isIs_read() {
        return is_read;
    }

    /**
     * sets the value of the is_read column in the message table
     * @param is_read
     */

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    /**
     * to check the status of the message
     * @return
     */

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
