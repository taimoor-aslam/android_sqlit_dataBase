package info.ray.sqlite.utils;

/**
 * Created by raza on 8/14/18.
 */

import com.android.volley.VolleyError;


public interface IResult {
    public void notifySuccess(String requestType, String response);
    public void notifyError(String requestType, VolleyError error);
}
