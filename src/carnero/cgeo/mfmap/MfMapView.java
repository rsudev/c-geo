package carnero.cgeo.mfmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewMode;
import org.mapsforge.android.maps.Projection;

import android.content.Context;
import android.util.AttributeSet;

public class MfMapView extends MapView {

	public MfMapView(Context context, MapViewMode mapViewMode) {
		super(context, mapViewMode);
		// TODO Auto-generated constructor stub
	}

	public MfMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MfMapView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public int getLatitudeSpan() {
		
		Projection proj = getProjection();
		GeoPoint upperLeft = proj.fromPixels(0, 0);
		
		// check if we can calculate
		if (upperLeft == null)
			return 0;
		
		GeoPoint lowerRight = proj.fromPixels(this.getWidth()-1, this.getHeight()-1);
		
		return Math.abs(upperLeft.getLatitudeE6()-lowerRight.getLatitudeE6());
	}

	public int getLongitudeSpan() {
		Projection proj = getProjection();
		GeoPoint upperLeft = proj.fromPixels(0, 0);
		
		// check if we can calculate
		if (upperLeft == null)
			return 0;
		
		GeoPoint lowerRight = proj.fromPixels(this.getWidth()-1, this.getHeight()-1);
		
		return Math.abs(upperLeft.getLongitudeE6()-lowerRight.getLongitudeE6());
	}
}
