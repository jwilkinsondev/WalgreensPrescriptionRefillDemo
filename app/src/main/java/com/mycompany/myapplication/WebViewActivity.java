package com.mycompany.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;


public class WebViewActivity extends AppCompatActivity {
    public static final String LANDING_URL = "https://services-qa.walgreens.com/api/util/mweb5url";
    private static final String TAG = WebViewActivity.class.getSimpleName();
    private RequestQueue queue = null;
    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the coordinates from the intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(MainActivity.EXTRA_LATITUDE, 0);
        longitude = intent.getDoubleExtra(MainActivity.EXTRA_LONGITUDE, 0);

        // https://github.com/ogrebgr/android_volley_examples
        queue = Volley.newRequestQueue(this.getApplicationContext());

        // build post data for http request
        JSONObject postDataJSON = new JSONObject();
        try {
            postDataJSON.put("apiKey", getString(R.string.api_key));
            postDataJSON.put("affId", getString(R.string.affiliate_id)); //don't have an affId yet
            postDataJSON.put("transaction", "refillByScan");
            postDataJSON.put("act", "mweb5Url");
            postDataJSON.put("view", "mweb5UrlJSON");
            postDataJSON.put("devinf", "Android,2.3.3");
            postDataJSON.put("appver", "3.1");
        } catch (JSONException e){
            Log.e(TAG, "error building json request", e);
        }

        JsonObjectRequest landingRequest = new JsonObjectRequest(Method.POST, LANDING_URL, postDataJSON, landingSuccessListener(), errorListener()){};

        queue.add(landingRequest);

        setContentView(R.layout.activity_web_view);
    }

    private Response.Listener<JSONObject> landingSuccessListener(){
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // handle the response from the landing url
                final String landingUrl, token;
                try {
                    landingUrl = response.getString("landingUrl");
                    token = response.getString("token");
                }catch (JSONException e) {
                    Log.e(TAG, String.format("error parsing landing JSON response: %s", response), e);
                    return;
                }
                Response.Listener<String> responseSuccessListenner = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String lines[] = response.split("\\r?\\n");

                        WebView myWebView = (WebView) findViewById(R.id.webview);
                        myWebView.getSettings().setJavaScriptEnabled(true);

                        //http://stackoverflow.com/a/25556224
                        myWebView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                // we don't want to intercept http or https urls
                                if (url.startsWith("htttp:") || url.startsWith("https")) {
                                    return false;
                                }

                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                startActivity( intent );
                                return true;
                            }
                        });

                        myWebView.loadDataWithBaseURL(landingUrl, response, "text/html", "utf-8", "");

                    }
                };

                try{
                    // build refill JSON
                    final JSONObject refillDataJSON = new JSONObject();
                    refillDataJSON.put("affId", getString(R.string.affiliate_id));
                    refillDataJSON.put("token", token);
                    refillDataJSON.put("lat", Double.toString(latitude));
                    refillDataJSON.put("lng", Double.toString(longitude));
                    refillDataJSON.put("devinf", "Android,2.3.3");
                    refillDataJSON.put("appver", "3.1");
                    refillDataJSON.put("act", "chkExpRx");
                    refillDataJSON.put("appId", "refillByScan");
                    refillDataJSON.put("rxNo", "0373128-59382");
                    refillDataJSON.put("appCallBackScheme", "refillByScan://handleControl");
                    refillDataJSON.put("appCallBackAction", "callBackAction");
                    refillDataJSON.put("trackingId", "TRACKING_ID");

                    StringRequest refillRequest = new StringRequest(Method.POST, landingUrl, responseSuccessListenner, errorListener()) {
                        @Override
                        public byte[] getBody()throws AuthFailureError{
                            // using a byte array so that we can use StringRequest instead of JSON since we are expecting html back not JSON
                            return refillDataJSON.toString().getBytes();
                        }
                    };
                    queue.add(refillRequest);
                } catch (JSONException e) {
                    Log.e(TAG, "error building refill request JSON", e);
                    return;
                }
            }
        };
    }

    private Response.ErrorListener errorListener(){
        return new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                // if there is an error from the landing url
                Log.e(TAG, error.getMessage());
            }
        };
    }

}
