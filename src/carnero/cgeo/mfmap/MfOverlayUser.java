package carnero.cgeo.mfmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import carnero.cgeo.R;
import carnero.cgeo.cgUser;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class MfOverlayUser extends OverlayItem {
	private Context context = null;
	private cgUser user = null;

	public MfOverlayUser(Context contextIn, cgUser userIn) {
		super(new GeoPoint((int)(userIn.latitude * 1e6), (int)(userIn.longitude * 1e6)), userIn.username, "");

		context = contextIn;
		user = userIn;
	}

	@Override
	public Drawable getMarker() {
		Drawable marker = null;
		
		if (user != null && user.located != null && user.located.getTime() >= (System.currentTimeMillis() - (20 * 60 * 1000))) {
			marker = context.getResources().getDrawable(R.drawable.user_location_active);
		} else {
			marker = context.getResources().getDrawable(R.drawable.user_location);
		}

		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		marker.setAlpha(190);
		setMarker(marker);

		return marker;
	}

	public cgUser getUser() {
		return user;
	}
}
