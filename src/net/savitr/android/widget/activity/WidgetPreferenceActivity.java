package net.savitr.android.widget.activity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import net.savitr.android.widget.IConstants;
import net.savitr.android.widget.service.TickService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

public class WidgetPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(net.savitr.android.widget.R.xml.widget_preference);

		final String[] timezone_string_values = getTimezones();
		final List<String> valuesList = Arrays.asList(timezone_string_values);
		final String[] time_string = formatTimezones(timezone_string_values);

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		final CheckBoxPreference show24HourClockPref = (CheckBoxPreference) findPreference(IConstants.PREF_24_HOUR_CLOCK_FLAG);
		if (prefs.getBoolean(IConstants.PREF_24_HOUR_CLOCK_FLAG, false)) {
			show24HourClockPref.setChecked(true);
		}
		final ListPreference timezonePreference = (ListPreference) findPreference(IConstants.PREF_TIMEZONE);
		if (prefs.getString(IConstants.PREF_TIMEZONE, null) != null) {
			timezonePreference.setSummary(time_string[valuesList.indexOf(prefs
					.getString(IConstants.PREF_TIMEZONE, null))]);
		} else {
			timezonePreference
					.setSummary(net.savitr.android.widget.R.string.summary_select_timezone);
		}

		timezonePreference.setEntries(time_string);
		timezonePreference.setEntryValues(timezone_string_values);

		timezonePreference
			.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					timezonePreference.setSummary(time_string[valuesList
							.indexOf(newValue)]);
					return true;
				}
			});

	}

	private String[] getTimezones() {
		final String[] retval = TimeZone.getAvailableIDs();
		Arrays.sort(retval, new Comparator<String>() {

			@Override
			public int compare(String object1, String object2) {
				final String obj1 = formatString(object1);
				final String obj2 = formatString(object2);
				return obj1.compareTo(obj2);
			}
		});
		return retval;
	}

	private String[] formatTimezones(String[] data) {
		final String[] retval = new String[data.length];
		int index = 0;
		for (String item : data) {
			retval[index] = formatString(item);
			index++;
		}
		return retval;
	}

	private String formatString(String element) {
		final String temp = element.substring(element.lastIndexOf('/') + 1);
		return temp.replace('_', ' ');
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent launchIntent = getIntent();
			Bundle extras = launchIntent.getExtras();
			Integer appWidgetId = 0;
			if (extras != null) {
				appWidgetId = extras.getInt(
						AppWidgetManager.EXTRA_APPWIDGET_ID,
						AppWidgetManager.INVALID_APPWIDGET_ID);
			}
			final SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			final boolean show24HourClockFlag = prefs.getBoolean(
					IConstants.PREF_24_HOUR_CLOCK_FLAG, false);
			final String timezone = prefs.getString(IConstants.PREF_TIMEZONE,
					null);
			final String backgroundColor = prefs.getString(
					IConstants.PREF_BACKGROUND_COLOR, null);
			final Intent intent = new Intent(this, TickService.class);
			intent.putExtra(IConstants.KEY_24_HOUR_CLOCK_FLAG,
					show24HourClockFlag);
			intent.putExtra(IConstants.KEY_TIMEZONE, timezone);
			intent.putExtra(IConstants.KEY_APP_WIDGET_ID, appWidgetId);
			this.startService(intent);

			// now store the values per appWidgetId so that when call to enabled
			// is made, things work out
			// smoothly
			final SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(IConstants.PREF_24_HOUR_CLOCK_FLAG
					+ IConstants.PREF_KEY_SEPARATOR + appWidgetId,
					show24HourClockFlag);
			editor.putString(IConstants.PREF_TIMEZONE
					+ IConstants.PREF_KEY_SEPARATOR + appWidgetId, timezone);
			editor.putString(IConstants.PREF_BACKGROUND_COLOR
					+ IConstants.PREF_KEY_SEPARATOR + appWidgetId,
					backgroundColor);
			editor.commit();

			/* Important step to start widget */
			final Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
					appWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
