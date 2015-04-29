package edu.cmu.andrew.utilities;

/**
 * Created by Deepak on 4/28/2015.
 */
public class PricingAlgorithm {
    static final float baseCharge = 1.35f;
    static final float safetyFee = 1.0f;
    static final float costPerMileAlone = 1.10f;
    static final float costPerMinuteAlone = 0.22f;
    static final float costPerMileSharing = 1.50f;
    static final float costPerMinuteSharing = 0.35f;
    static final float discountFeePerMile = 0.50f ;
    static final float discountFeePerMinute = 0.08f;

    public static double calcMaximumPrice (double distance, double time){
        double price = (distance * costPerMileAlone) + (time * costPerMinuteAlone) + baseCharge + safetyFee;
        return price;
    }

    public static double calcTripSegmentPrice (TripSegment trip){

        double price = 0;

        if (trip.getRequests().size() == 1)
            price = (trip.getDistance() * costPerMileAlone) + (trip.getDuration() * costPerMinuteAlone);
        else
            price = ((trip.getDistance() * costPerMileSharing) + (trip.getDuration() * costPerMinuteSharing)) / trip.getRequests().size();

        return price;
    }

    public static double calcFinalPrice (double estDistance, double estTime, double actDistance, double actTime){

        double price = safetyFee + baseCharge - ((actDistance - estDistance) * discountFeePerMile) - ((actTime - estTime) * discountFeePerMinute);
        return price;
    }
}
