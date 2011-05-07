package carnero.cgeo.mfmap;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mapsforge.android.maps.ItemizedOverlay;
import carnero.cgeo.R;
import carnero.cgeo.cgSettings;
import carnero.cgeo.cgUser;
import carnero.cgeo.cgeodetail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MfUsersOverlay extends ItemizedOverlay<MfOverlayUser> {

	private ArrayList<MfOverlayUser> items = new ArrayList<MfOverlayUser>();
	private Context context = null;
	private final Pattern patternGeocode = Pattern.compile("^(GC[A-Z0-9]+)(\\: ?(.+))?$", Pattern.CASE_INSENSITIVE);

	public MfUsersOverlay(Context contextIn, Drawable markerIn) {
		super(boundCenterBottom(markerIn));
		populate();

		context = contextIn;
	}

	protected void updateItems(MfOverlayUser item) {
		ArrayList<MfOverlayUser> itemsPre = new ArrayList<MfOverlayUser>();
		itemsPre.add(item);
		
		updateItems(itemsPre);
	}

	protected void updateItems(ArrayList<MfOverlayUser> itemsPre) {
		if (itemsPre == null) {
			return;
		}

		for (MfOverlayUser item : itemsPre) {
			item.setMarker(boundCenterBottom(item.getMarker()));
		}

		items.clear();
		
		if (itemsPre.size() > 0) {
			items = (ArrayList<MfOverlayUser>) itemsPre.clone();
		}
		
//		setLastFocusedIndex(-1); // to reset tap during data change
		populate();
	}

	@Override
	protected boolean onTap(int index) {
		try {
			if (items.size() <= index) {
				return false;
			}

			final MfOverlayUser item = items.get(index);
			final cgUser user = item.getUser();

			// set action
			String action = null;
			String geocode = null;
			final Matcher matcherGeocode = patternGeocode.matcher(user.action.trim());

			if (user.action.length() == 0 || user.action.equalsIgnoreCase("pending")) {
				action = "Looking around";
			} else if (user.action.equalsIgnoreCase("tweeting")) {
				action = "Tweeting";
			} else if (matcherGeocode.find() == true) {
				if (matcherGeocode.group(1) != null) {
					geocode = matcherGeocode.group(1).trim().toUpperCase();
				}
				if (matcherGeocode.group(3) != null) {
					action = "Heading to " + geocode + " (" + matcherGeocode.group(3).trim() + ")";
				} else {
					action = "Heading to " + geocode;
				}
			} else {
				action = user.action;
			}

			// set icon
			int icon = -1;
			if (user.client.equalsIgnoreCase("c:geo") == true) {
				icon = R.drawable.client_cgeo;
			} else if (user.client.equalsIgnoreCase("preCaching") == true) {
				icon = R.drawable.client_precaching;
			} else if (user.client.equalsIgnoreCase("Handy Geocaching") == true) {
				icon = R.drawable.client_handygeocaching;
			}

			final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			if (icon > -1) {
				dialog.setIcon(icon);
			}
			dialog.setTitle(user.username);
			dialog.setMessage(action);
			dialog.setCancelable(true);
			if (geocode != null && geocode.length() > 0) {
				dialog.setPositiveButton(geocode + "?", new cacheDetails(geocode));
			}
			dialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			AlertDialog alert = dialog.create();
			alert.show();

			return true;
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.onTap: " + e.toString());
		}

		return false;
	}

	@Override
	public MfOverlayUser createItem(int index) {
		try {
			return items.get(index);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.createItem: " + e.toString());
		}

		return null;
	}

	@Override
	public int size() {
		try {
			return items.size();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgUsersOverlay.size: " + e.toString());
		}

		return 0;
	}

	private class cacheDetails implements DialogInterface.OnClickListener {

		private String geocode = null;

		public cacheDetails(String geocodeIn) {
			geocode = geocodeIn;
		}

		public void onClick(DialogInterface dialog, int id) {
			if (geocode != null) {
				Intent detailIntent = new Intent(context, cgeodetail.class);
				detailIntent.putExtra("geocode", geocode);
				context.startActivity(detailIntent);
			}

			dialog.cancel();
		}
	}
}
