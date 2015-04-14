package edu.cmu.andrew.sharearide.backend;

public class UserBean {

  private int userID;
  private String userName;
  private String secret;
  private String firstName;
  private String lastName;
  private int phoneNumber;
  private String email;
  private double longitude;
  private double latitude;
  private String userType;

  public int getUserID () {
    return userID;
  }

  public void setUserID (int userID) {
    this.userID = userID;
  }

  public String getUserName () {
    return userName;
  }

  public void setUserName (String userName) {
    this.userName = userName;
  }

  public String getSecret () {
    return secret;
  }

  public void setSecret (String secret) {
    this.secret = secret;
  }

  public String getFirstName () {
    return firstName;
  }

  public void setFirstName (String firstName) {
    this.firstName = firstName;
  }

  public String getLastName () {
    return lastName;
  }

  public void setLastName (String lastName) {
    this.lastName = lastName;
  }

  public int getPhoneNumber () {
    return phoneNumber;
  }

  public void setPhoneNumber (int phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getEmail () {
    return email;
  }

  public void setEmail (String email) {
    this.email = email;
  }

  public double getLongitude () {
    return longitude;
  }

  public void setLongitude (double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude () {
    return latitude;
  }

  public void setLatitude (double latitude) {
    this.latitude = latitude;
  }

  public String getUserType () {
    return userType;
  }

  public void setUserType (String userType) {
    this.userType = userType;
  }

}