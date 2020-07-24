package info.ray.sqlite.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;


/**
 * Created by raza on 8/18/18.
 */

public class Utils {


    /*
     * Following URL are used for Localhost and IP changes
     */
    public static String SERVER_HOME_URL="http://192.168.43.219/sql_data_base/index.php";



    /**
     * Returns Internet State.
     *
     * @param context
     * @return True if Internet is connected, False otherwise
     */
    public static boolean isOnline(Context context) {

        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connMgr.getNetworkCapabilities(connMgr.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                // current code
                final NetworkInfo wifi = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                final NetworkInfo mobile = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                if (wifi.isAvailable() && wifi.isConnected()) {
                    return true;
                }
                if (mobile != null) {
                    if (mobile.isAvailable() && mobile.isConnected()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }


}
