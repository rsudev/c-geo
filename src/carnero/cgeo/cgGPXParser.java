package carnero.cgeo;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import org.xml.sax.Attributes;

public class cgGPXParser {
	private cgBase base = null;
	private ArrayList<cgCache> caches = null;

	private cgCache cache = new cgCache();
	private cgTrackable trackable = new cgTrackable();
	private cgLog log = new cgLog();
	private boolean htmlShort = true;
	private boolean htmlLong = true;


	public cgGPXParser(cgBase baseIn, ArrayList<cgCache> cachesIn) {
		base = baseIn;
		caches = cachesIn;
	}

	public boolean parse(File file, int version) {
		if (caches == null) return false;
		if (file == null) return false;

		String ns = null;
		if (version == 11) ns = "http://www.topografix.com/GPX/1/1"; // GPX 1.1
		else ns = "http://www.topografix.com/GPX/1/0"; // GPX 1.0

		String nsGC = "http://www.groundspeak.com/cache/1/0";

		final RootElement root = new RootElement(ns, "gpx");
		final Element waypoint = root.getChild(ns, "wpt");

		// waypoint - attributes
		waypoint.setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				try {
					if (attrs.getIndex("lat") > -1) cache.latitude = new Double(attrs.getValue("lat"));
					if (attrs.getIndex("lon") > -1) cache.longitude = new Double(attrs.getValue("lon"));
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse waypoint's latitude and/or longitude.");
				}
			}
		});

		// waypoint
		waypoint.setEndElementListener(new EndElementListener() {
			public void end() {
				if (cache.latitude != null && cache.longitude != null && cache.name != null && cache.name.length() > 0) {
					cache.latitudeString = base.formatCoordinate(cache.latitude, "lat", true);
					cache.longitudeString = base.formatCoordinate(cache.longitude, "lon", true);
					cache.inventoryUnknown = cache.inventory.size();
					cache.reason = 1;
					cache.updated = new Date().getTime();
					cache.detailedUpdate = new Date().getTime();
					cache.detailed = true;

					caches.add(cache); // add cache to list
				}

				htmlShort = true;
				htmlLong = true;

				cache = new cgCache();
			}
		});

		// waypoint.time
		waypoint.getChild(ns, "time").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
				try {
					cache.hidden = base.dateGPXIn.parse(body.trim());
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse cache date: " + e.toString());
				}
			}
        });

		// waypoint.name
		waypoint.getChild(ns, "name").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
				final String content = Html.fromHtml(body).toString().trim();
				cache.name = content;
				if (cache.name.substring(0, 2).equalsIgnoreCase("GC") == true) {
					cache.geocode = cache.name.toUpperCase();
				}
			}
        });

		// waypoint.desc
		waypoint.getChild(ns, "desc").setEndTextElementListener(new EndTextElementListener(){
            public void end(String body) {
				final String content = Html.fromHtml(body).toString().trim();
				cache.shortdesc = content;
			}
        });

		// waypoints.cache
		final Element gcCache = waypoint.getChild(nsGC, "cache");

		gcCache.setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				try {
					if (attrs.getIndex("id") > -1) cache.cacheid = attrs.getValue("id");
					if (attrs.getIndex("archived") > -1) {
						final String at = attrs.getValue("archived").toLowerCase();
						if (at.equals("true")) cache.archived = true;
						else cache.archived = false;
					}
					if (attrs.getIndex("available") > -1) {
						final String at = attrs.getValue("available").toLowerCase();
						if (at.equals("true")) cache.disabled = false;
						else cache.disabled = true;
					}
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse cache attributes.");
				}
			}
		});

		// waypoint.cache.name
		gcCache.getChild(nsGC, "name").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				final String content = Html.fromHtml(body).toString().trim();
				cache.name = content;
			}
        });

		// waypoint.cache.owner
		gcCache.getChild(nsGC, "owner").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				final String content = Html.fromHtml(body).toString().trim();
				cache.owner = content;
			}
        });

		// waypoint.cache.type
		gcCache.getChild(nsGC, "type").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				final String content = base.cacheTypes.get(body.toLowerCase());
				cache.type = content;
			}
        });

		// waypoint.cache.container
		gcCache.getChild(nsGC, "container").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				final String content = body.toLowerCase();
				cache.size = content;
			}
        });

		// waypoint.cache.difficulty
		gcCache.getChild(nsGC, "difficulty").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				try {
					cache.difficulty = new Float(body);
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse difficulty: " + e.toString());
				}
			}
        });

		// waypoint.cache.terrain
		gcCache.getChild(nsGC, "terrain").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				try {
					cache.terrain = new Float(body);
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse terrain: " + e.toString());
				}
			}
        });

		// waypoint.cache.country
		gcCache.getChild(nsGC, "country").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				if (cache.location == null || cache.location.length() == 0) cache.location = body.trim();
				else cache.location = cache.location + ", " + body.trim();
			}
        });

		// waypoint.cache.state
		gcCache.getChild(nsGC, "state").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				if (cache.location == null || cache.location.length() == 0) cache.location = body.trim();
				else cache.location = body.trim() + ", " + cache.location;
			}
        });

		// waypoint.cache.encoded_hints
		gcCache.getChild(nsGC, "encoded_hints").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				cache.hint = body.trim();
			}
        });

		// waypoint.cache.short_description
		gcCache.getChild(nsGC, "short_description").setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				try {
					if (attrs.getIndex("html") > -1) {
						final String at = attrs.getValue("html").toLowerCase();
						if (at.equals("false")) htmlShort = false;
					}
				} catch (Exception e) {
					// nothing
				}
			}
		});

		gcCache.getChild(nsGC, "short_description").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				if (htmlShort == false) cache.shortdesc = Html.fromHtml(body).toString();
				else cache.shortdesc = body;
			}
        });

		// waypoint.cache.long_description
		gcCache.getChild(nsGC, "long_description").setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				try {
					if (attrs.getIndex("html") > -1) {
						final String at = attrs.getValue("html").toLowerCase();
						if (at.equals("false")) htmlLong = false;
					}
				} catch (Exception e) {
					// nothing
				}
			}
		});

		gcCache.getChild(nsGC, "long_description").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				if (htmlLong == false) cache.description = Html.fromHtml(body).toString();
				else cache.description = body;
			}
        });

		// waypoint.cache.travelbugs
		final Element gcTBs = gcCache.getChild(nsGC, "travelbugs");

		// waypoint.cache.travelbugs.travelbug
		gcTBs.getChild(nsGC, "travelbug").setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				trackable = new cgTrackable();

				try {
					if (attrs.getIndex("ref") > -1) trackable.geocode = attrs.getValue("ref").toUpperCase();
				} catch (Exception e) {
					// nothing
				}
			}
		});

		// waypoint.cache.travelbug
		final Element gcTB = gcTBs.getChild(nsGC, "travelbug");

		gcTB.setEndElementListener(new EndElementListener() {
			public void end() {
				if (trackable.geocode != null && trackable.geocode.length() > 0 && trackable.name != null && trackable.name.length() > 0) {
					cache.inventory.add(trackable);
				}
			}
		});

		// waypoint.cache.travelbugs.travelbug.name
		gcTB.getChild(nsGC, "name").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				String content = Html.fromHtml(body).toString();
				trackable.name = content;
			}
        });

		// waypoint.cache.logs
		final Element gcLogs = gcCache.getChild(nsGC, "logs");

		// waypoint.cache.log
		final Element gcLog = gcLogs.getChild(nsGC, "log");

		gcLog.setStartElementListener(new StartElementListener() {
			public void start(Attributes attrs) {
				log = new cgLog();

				try {
					if (attrs.getIndex("id") > -1) log.id = Integer.parseInt(attrs.getValue("id"));
				} catch (Exception e) {
					// nothing
				}
			}
		});

		gcLog.setEndElementListener(new EndElementListener() {
			public void end() {
				if (log.log != null && log.log.length() > 0) {
					cache.logs.add(log);
				}
			}
		});

		// waypoint.cache.logs.log.date
		gcLog.getChild(nsGC, "date").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				try {
					log.date = base.dateGPXIn.parse(body.trim()).getTime();
				} catch (Exception e) {
					Log.w(cgSettings.tag, "Failed to parse log date: " + e.toString());
				}
			}
        });

		// waypoint.cache.logs.log.type
		gcLog.getChild(nsGC, "type").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				final String content = body.trim().toLowerCase();
				if (base.logTypes0.containsKey(content) == true) log.type = base.logTypes0.get(content);
				else log.type = 4;
			}
        });

		// waypoint.cache.logs.log.finder
		gcLog.getChild(nsGC, "finder").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				String content = Html.fromHtml(body).toString();
				log.author = content;
			}
        });

		// waypoint.cache.logs.log.finder
		gcLog.getChild(nsGC, "text").setEndTextElementListener(new EndTextElementListener() {
            public void end(String body) {
				String content = Html.fromHtml(body).toString();
				log.log = content;
			}
        });

		try {
            Xml.parse(new FileInputStream(file), Xml.Encoding.UTF_8, root.getContentHandler());
			
			return true;
        } catch (Exception e) {
            Log.e(cgSettings.tag, "Cannot parse .gpx file " + file.getAbsolutePath() + " as GPX " + version + ": " + e.toString());
        }

		return false;
	}
}