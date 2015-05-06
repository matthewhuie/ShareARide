package edu.cmu.andrew.utilities;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;

/**
 * Created by Aditi on 4/19/2015.
 *  singleton class to create the google cloud endpoint instance
 *  to ensure that there is always one instance of the same
 */
public class EndPointManager {

    private static ShareARideApi myApiService = null;
    //google endpoint URL
    private static final String ENDPOINT_URL = "https://vivid-art-90101.appspot.com/_ah/api/";

    public static ShareARideApi getEndpointInstance() {
        if (myApiService == null) {
            ShareARideApi.Builder builder = new ShareARideApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null).setRootUrl(ENDPOINT_URL);

            myApiService = builder.build();
            Log.i("API Backend Connection: ", myApiService.toString());

        }
        Log.i("myapi exist: ", myApiService.toString());
        return myApiService;

    }

}
