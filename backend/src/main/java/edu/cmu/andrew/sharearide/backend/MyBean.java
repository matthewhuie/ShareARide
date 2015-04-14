package edu.cmu.andrew.sharearide.backend;

/**
 * The object model for the data we are sending through endpoints
 */
public class MyBean {

  private String myData = "";

  public String getData () {
    return myData;
  }

  public void setData (String data) {
    myData = data;
  }

  public void appendData (String data) {
    if (! myData.equals ("")) myData += ",";
    myData += data;
  }
}