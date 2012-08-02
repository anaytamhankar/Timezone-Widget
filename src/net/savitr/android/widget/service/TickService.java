package net.savitr.android.widget.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;

import net.savitr.android.widget.IConstants;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.widget.RemoteViews;

public class TickService extends Service {
	// some member variables
	private Thread t = null;
	private Hashtable<Integer, WidgetData> dataStore = null;

	public TickService() {
		dataStore = new Hashtable<Integer, WidgetData>();
		t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						final long currentTime = System.currentTimeMillis();
						final Date currentDate = new Date(currentTime);
						final long sleepTime = (60 - currentDate.getSeconds()) * 1000;

						final AppWidgetManager manager = AppWidgetManager
								.getInstance(TickService.this);

						// here update all widgets
						synchronized (dataStore) {
							for (Integer widgetId : dataStore.keySet()) {
								final WidgetData widgetData = dataStore
										.get(widgetId);
								updateWidget(manager, widgetId,
										widgetData, currentDate);
							}
						}
						Thread.sleep(sleepTime);
					}
				} catch (Exception e) {
				}
			}

		});
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		final Bundle extras = intent.getExtras();
		if (extras != null) {
			final boolean show24HourClockFlag = extras.getBoolean(
					IConstants.KEY_24_HOUR_CLOCK_FLAG, false);
			final String timezone = extras.getString(IConstants.KEY_TIMEZONE);
			final Integer appWidgetId = extras.getInt(
					IConstants.KEY_APP_WIDGET_ID, 0);
			final WidgetData widgetData = new WidgetData(show24HourClockFlag,
					timezone);

			synchronized (dataStore) {
				dataStore.put(appWidgetId, widgetData);
			}

			if (!t.isAlive()) {
				t.start();
			}

			// finally force update here
			final AppWidgetManager manager = AppWidgetManager
					.getInstance(TickService.this);
			final long currentTime = System.currentTimeMillis();
			final Date currentDate = new Date(currentTime);
			updateWidget(manager, appWidgetId, widgetData, currentDate);
		}
	}

	private void updateWidget(AppWidgetManager manager, Integer widgetId, WidgetData widgetData, Date currentDate) {
		// first create remote view
		RemoteViews view = null;
		view = new RemoteViews(TickService.this.getPackageName(), net.savitr.android.widget.R.layout.main);
		
		// first get the text for current time
		final Spanned timeString = getTimeString(currentDate, widgetData);
		view.setTextViewText(net.savitr.android.widget.R.id.time_str,
				timeString);
		view.setTextViewText(net.savitr.android.widget.R.id.timezone_str,
				widgetData.getDisplayTimezone());
		manager.updateAppWidget(widgetId, view);
	}

	private Spanned getTimeString(Date currentDate, WidgetData widgetData) {
		SimpleDateFormat dateFormat = null;
		SimpleDateFormat ampmFormat = new SimpleDateFormat("aa");
		if (widgetData.is24HourFlagset()) {
			dateFormat = new SimpleDateFormat("HH:mm");
		} else {
			dateFormat = new SimpleDateFormat("KK:mm");
		}
		if (widgetData.getTimezone() != null) {
			dateFormat.setTimeZone(TimeZone.getTimeZone(widgetData
					.getTimezone()));
			ampmFormat.setTimeZone(TimeZone.getTimeZone(widgetData
					.getTimezone()));
		}
		if(widgetData.is24HourFlagset()) {
			return Html.fromHtml(dateFormat.format(currentDate));
		}
		else {
			return Html.fromHtml(dateFormat.format(currentDate) + " <small>"
					+ ampmFormat.format(currentDate) + "</small>");
		}
	}

	public class WidgetData {
		// member variables
		private boolean show24HourFlag = false;
		private String timezone = null;

		public WidgetData(boolean show24HourFlag, String timezone) {
			this.show24HourFlag = show24HourFlag;
			this.timezone = timezone;
		}

		public boolean is24HourFlagset() {
			return show24HourFlag;
		}

		public String getTimezone() {
			return timezone;
		}
		
		public String getDisplayTimezone() {
			return formatString(timezone);
		}

		private String formatString(String element) {
			if(element == null) {
				return null;
			}
			final String temp = element.substring(element.lastIndexOf('/') + 1);
			return temp.replace('_', ' ');
		}
	}
}
