package edu.cmu.andrew.utilities;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;

/**
 * Created by Aditi on 4/19/2015.
 */
public class EndPointManager {

    private static ShareARideApi myApiService = null;

    public static ShareARideApi getEndpointInstance() {
        if (myApiService == null) {
            ShareARideApi.Builder builder = new ShareARideApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null).setRootUrl("https://vivid-art-90101.appspot.com/_ah/api/");

            myApiService = builder.build();
            Log.i("API Backend Connection: ", myApiService.toString());

        }
        Log.i("myapi exist: ", myApiService.toString());
        return myApiService;

    }

}
