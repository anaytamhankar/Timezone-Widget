package net.savitr.android.widget;

import net.savitr.android.widget.service.TickService;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimezoneWidget extends AppWidgetProvider {
	@Override
	public void onEnabled(Context context) {
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(
				context, TimezoneWidget.class));
		for (int appWidgetId : appWidgetIds) {
			final boolean show24HourClockFlag = prefs.getBoolean(
					IConstants.PREF_24_HOUR_CLOCK_FLAG
							+ IConstants.PREF_KEY_SEPARATOR + appWidgetId,
					false);
			final String timezone = prefs.getString(IConstants.PREF_TIMEZONE
					+ IConstants.PREF_KEY_SEPARATOR + appWidgetId, null);
			final Intent intent = new Intent(context, TickService.class);
			intent.putExtra(IConstants.KEY_24_HOUR_CLOCK_FLAG,
					show24HourClockFlag);
			intent.putExtra(IConstants.KEY_TIMEZONE, timezone);
			intent.putExtra(IConstants.KEY_APP_WIDGET_ID, appWidgetId);
			context.startService(intent);
		}
	}
}