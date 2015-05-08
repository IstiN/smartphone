/**
 * 
 */
package mobi.wrt.android.smartcontacts.utils;

import android.text.format.DateUtils;

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
	
	public static String humanFriendlyDateHeader(Long date) {
		// today
		Date today = new Date();
		
		// how much time since (ms)
		Long duration = today.getTime() - date;
		
		long second = 1000;
		long minute = second * 60;
		long hour = minute * 60;
		long day = hour * 24;
		
		if (duration < day) {
			return ContextHolder.get().getString(R.string.call_log_header_today);
		}
		if (duration > day && duration < day * 2l) {
			return ContextHolder.get().getString(R.string.call_log_header_yesterday);
		}

        return ContextHolder.get().getString(R.string.call_log_header_other);
	}

}
