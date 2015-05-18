/**
 * 
 */
package mobi.wrt.android.smartcontacts.utils;

import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;

import by.istin.android.xcore.ContextHolder;
import by.istin.android.xcore.utils.StringUtil;
import mobi.wrt.android.smartcontacts.R;


/**
 * The Class TwitterUtil.
 *
 * @author Uladzimir_Klyshevich
 */
public class HumanTimeUtil {


	/**
	 * Twitter human friendly date.
	 *
	 * @return the string
	 */
	public static CharSequence humanFriendlyDate(Long created) {
		return DateUtils.getRelativeTimeSpanString(created,
				System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS,
				DateUtils.FORMAT_ABBREV_RELATIVE);
	}

	private static final Time TIME = new Time();

	public static String humanFriendlyDateHeader(Long date) {
		long currentTime = System.currentTimeMillis();
		TIME.set(date);
		int startDay = Time.getJulianDay(date, TIME.gmtoff);
		TIME.set(currentTime);
		int currentDay = Time.getJulianDay(currentTime, TIME.gmtoff);
		int difference = Math.abs(currentDay - startDay);
		if (difference == 0) {
			return ContextHolder.get().getString(R.string.call_log_header_today);
		}
		if (difference == 1) {
			return ContextHolder.get().getString(R.string.call_log_header_yesterday);
		}
        return ContextHolder.get().getString(R.string.call_log_header_other);
	}

}
