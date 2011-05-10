package carnero.cgeo.mfmap;

import java.util.ArrayList;

import org.mapsforge.android.maps.ItemizedOverlay;

import carnero.cgeo.cgBase;
import carnero.cgeo.cgCoord;
import carnero.cgeo.cgSettings;
import carnero.cgeo.cgeodetail;
import carnero.cgeo.cgeonavigate;
import carnero.cgeo.cgeopopup;
import carnero.cgeo.cgeowaypoint;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;

//public class MfMapOverlay extends Overlay {
//
//	@Override
//	protected void drawOverlayBitmap(Canvas canvas, Point drawPosition,
//			Projection projection, byte drawZoomLevel) {
//		// TODO Auto-generated method stub
//
//	}
//
//}

public class MfMapOverlay extends ItemizedOverlay<MfOverlayItem> {

	private ArrayList<MfOverlayItem> items = new ArrayList<MfOverlayItem>();
	private Context context = null;
	private Boolean fromDetail = false;
	private ProgressDialog waitDialog = null;

	public MfMapOverlay(Context contextIn, Drawable markerIn, Boolean fromDetailIn) {
		super(boundCenterBottom(markerIn));
		populate();

		context = contextIn;
		fromDetail = fromDetailIn;
	}
	
	protected void updateItems(MfOverlayItem item) {
		ArrayList<MfOverlayItem> itemsPre = new ArrayList<MfOverlayItem>();
		itemsPre.add(item);
		
		updateItems(itemsPre);
	}

	protected void updateItems(ArrayList<MfOverlayItem> itemsPre) {
		if (itemsPre == null) {
			return;
		}

		for (MfOverlayItem item : itemsPre) {
			item.setMarker(boundCenterBottom(item.getMarker()));
		}

		items.clear();
		
		if (itemsPre.size() > 0) {
			items = (ArrayList<MfOverlayItem>) itemsPre.clone();
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

			if (waitDialog == null) {
				waitDialog = new ProgressDialog(context);
				waitDialog.setMessage("loading details...");
				waitDialog.setCancelable(false);
			}
			waitDialog.show();

			MfOverlayItem item = items.get(index);
			cgCoord coordinate = item.getCoord();

			if (coordinate.type != null && coordinate.type.equalsIgnoreCase("cache") == true && coordinate.geocode != null && coordinate.geocode.length() > 0) {
				Intent popupIntent = new Intent(context, cgeopopup.class);

				popupIntent.putExtra("fromdetail", fromDetail);
				popupIntent.putExtra("geocode", coordinate.geocode);

				context.startActivity(popupIntent);
			} else if (coordinate.type != null && coordinate.type.equalsIgnoreCase("waypoint") == true && coordinate.id != null && coordinate.id > 0) {
				Intent popupIntent = new Intent(context, cgeowaypoint.class);

				popupIntent.putExtra("waypoint", coordinate.id);
				popupIntent.putExtra("geocode", coordinate.geocode);

				context.startActivity(popupIntent);
			} else {
				waitDialog.dismiss();
				return false;
			}

			waitDialog.dismiss();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.onTap: " + e.toString());
		}

		return false;
	}

//	@Override
//	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
//		super.draw(canvas, mapView, false);
//	}

	@Override
	public MfOverlayItem createItem(int index) {
		try {
			return items.get(index);
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.createItem: " + e.toString());
		}

		return null;
	}

	@Override
	public int size() {
		try {
			return items.size();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.size: " + e.toString());
		}

		return 0;
	}

	public void infoDialog(int index) {
		final MfOverlayItem item = items.get(index);
		final cgCoord coordinate = item.getCoord();

		if (coordinate == null) {
			Log.e(cgSettings.tag, "cgMapOverlay:infoDialog: No coordinates given");
			return;
		}

		try {
			AlertDialog.Builder dialog = new AlertDialog.Builder(context);
			dialog.setCancelable(true);

			if (coordinate.type.equalsIgnoreCase("cache")) {
				dialog.setTitle("cache");

				String cacheType;
				if (cgBase.cacheTypesInv.containsKey(coordinate.typeSpec) == true) {
					cacheType = cgBase.cacheTypesInv.get(coordinate.typeSpec);
				} else {
					cacheType = cgBase.cacheTypesInv.get("mystery");
				}

				dialog.setMessage(Html.fromHtml(item.getTitle()) + "\n\ngeocode: " + coordinate.geocode.toUpperCase() + "\ntype: " + cacheType);
				if (fromDetail == false) {
					dialog.setPositiveButton("detail", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							Intent cachesIntent = new Intent(context, cgeodetail.class);
							cachesIntent.putExtra("geocode", coordinate.geocode.toUpperCase());
							context.startActivity(cachesIntent);

							dialog.cancel();
						}
					});
				} else {
					dialog.setPositiveButton("navigate", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int id) {
							cgeonavigate navigateActivity = new cgeonavigate();

							cgeonavigate.coordinates = new ArrayList<cgCoord>();
							cgeonavigate.coordinates.add(coordinate);

							Intent navigateIntent = new Intent(context, navigateActivity.getClass());
							navigateIntent.putExtra("latitude", coordinate.latitude);
							navigateIntent.putExtra("longitude", coordinate.longitude);
							navigateIntent.putExtra("geocode", coordinate.geocode.toUpperCase());
							context.startActivity(navigateIntent);
							dialog.cancel();
						}
					});
				}
			} else {
				dialog.setTitle("waypoint");

				String waypointType;
				if (cgBase.cacheTypesInv.containsKey(coordinate.typeSpec) == true) {
					waypointType = cgBase.waypointTypes.get(coordinate.typeSpec);
				} else {
					waypointType = cgBase.waypointTypes.get("waypoint");
				}

				dialog.setMessage(Html.fromHtml(item.getTitle()) + "\n\ntype: " + waypointType);
				dialog.setPositiveButton("navigate", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int id) {
						cgeonavigate navigateActivity = new cgeonavigate();

						cgeonavigate.coordinates = new ArrayList<cgCoord>();
						cgeonavigate.coordinates.add(coordinate);

						Intent navigateIntent = new Intent(context, navigateActivity.getClass());
						navigateIntent.putExtra("latitude", coordinate.latitude);
						navigateIntent.putExtra("longitude", coordinate.longitude);
						navigateIntent.putExtra("geocode", coordinate.name);

						context.startActivity(navigateIntent);
						dialog.cancel();
					}
				});
			}

			dialog.setNegativeButton("dismiss", new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});

			AlertDialog alert = dialog.create();
			alert.show();
		} catch (Exception e) {
			Log.e(cgSettings.tag, "cgMapOverlay.infoDialog: " + e.toString());
		}
	}
}
