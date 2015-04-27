package edu.cmu.andrew.sharearide.backend;

/**
 * Created by Aditi on 4/18/2015.
 */
public class MessageBean {

    private boolean status;
    private int user_name;
    private String message;
    private int message_id;
    private int is_read;
    private int request_id;

    public int getRequest_id() {
        return request_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public int getUser_name() {
        return user_name;
    }

    public void setUser_name(int user_name) {
        this.user_name = user_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessage_id() {
        return message_id;
    }

    public void setMessage_id(int message_id) {
        this.message_id = message_id;
    }

    public int isIs_read() {
        return is_read;
    }

    public void setIs_read(int is_read) {
        this.is_read = is_read;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }



}
