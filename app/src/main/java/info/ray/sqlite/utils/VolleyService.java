package info.ray.sqlite.utils;

/**
 * Created by raza on 8/14/18.
 */

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raza on 5/25/18.
 */
public class VolleyService {

    IResult mResultCallback = null;
    Context mContext;
    String TAG="VolleyService";

    public VolleyService(IResult resultCallback, Context context){
        mResultCallback = resultCallback;
        mContext = context;
    }


    public void postDataVolley(final String requestType, String url, final JSONObject sendObj){
        try {
            RequestQueue queue = Volley.newRequestQueue(mContext);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d(TAG,"Success");
                    try {
                        //JSONArray jsonRresponse = new JSONArray(response);
                        if(mResultCallback != null) {
                            mResultCallback.notifySuccess(requestType, response);
                        }
                    } catch (Exception exception){
                        Log.e("Error: ", exception.toString());
                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG,"Failure");
                    if(mResultCallback != null)
                        mResultCallback.notifyError(requestType,error);


                }
            }){
                //Code to send parameters to server , we can send as many params as we want
                @Override
                protected Map getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    try {
                        for (int i = 0; i < sendObj.names().length(); i++) {
                             Log.v(TAG, "key = " + sendObj.names().getString(i) + " value = " + sendObj.get(sendObj.names().getString(i)));
                            params.put(sendObj.names().getString(i), sendObj.get(sendObj.names().getString(i)).toString());
                        }
                    }
                    catch(JSONException je){
                        Log.e(TAG,je.toString());
                    }
                    return params;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    3,
                    1));

            queue.add(stringRequest);

        }catch(Exception e){
            Log.e(TAG,e.toString());
        }
    }


}


