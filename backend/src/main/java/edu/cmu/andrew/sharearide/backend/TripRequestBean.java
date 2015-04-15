package edu.cmu.andrew.sharearide.backend;

public class TripRequestBean {

    private int tripId;
    private ArrayList<RequestBean> requests = new ArrayList<>();

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public ArrayList<RequestBean> getRequests() {
        return requests;
    }

  public int getRequestId () {
    return requestId;
  }

  public void setRequestId (int requestId) {
    this.requestId = requestId;
  }

}