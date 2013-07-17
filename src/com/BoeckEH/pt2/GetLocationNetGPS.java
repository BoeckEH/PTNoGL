package com.BoeckEH.pt2;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

public class GetLocationNetGPS {

	Context mContext;
	
	private GetLocationNetGPS(Context mContext)
	{
		this.mContext = mContext;
	}
	
	private double[] GetLoc() {
        LocationManager lm = (LocationManager)  mContext.getSystemService(Context.LOCATION_SERVICE);  
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;
        
        for (int i=providers.size()-1; i>=0; i--) {
                l = lm.getLastKnownLocation(providers.get(i));
                if (l != null) break;
        }
        
        double[] gps = new double[2];
        if (l != null) {
                gps[0] = l.getLatitude();
                gps[1] = l.getLongitude();
        }
        return gps;
	}
}
