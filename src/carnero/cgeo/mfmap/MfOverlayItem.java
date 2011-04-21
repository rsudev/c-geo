package carnero.cgeo.mfmap;

import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.OverlayItem;

import carnero.cgeo.cgCoord;

public class MfOverlayItem extends OverlayItem {
	private cgCoord coordinate;

	public MfOverlayItem(cgCoord coordinate) {
		super(new GeoPoint((int)(coordinate.latitude * 1e6), (int)(coordinate.longitude * 1e6)), coordinate.name, "");

		this.coordinate = coordinate;
	}

	public cgCoord getCoord() {
		return this.coordinate;
	}
}
