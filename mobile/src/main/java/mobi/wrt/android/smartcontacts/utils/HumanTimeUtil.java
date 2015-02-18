/**
 * 
 */
package mobi.wrt.android.smartcontacts.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import by.istin.android.xcore.utils.StringUtil;


/**
 * The Class TwitterUtil.
 *
 * @author Uladzimir_Klyshevich
 */
public class HumanTimeUtil {

	/** The Constant OVER_A_YEAR_AGO. */
	private static final String OVER_A_YEAR_AGO = "over_a_year_ago";

	/** The Constant DAYS_AGO. */
	private static final String DAYS_AGO = "days_ago";

	/** The Constant TODAY. */
	private static final String TODAY = "today";

	/** The Constant YESTERDAY. */
	private static final String YESTERDAY = "yesterday";

	/** The Constant WEEK. */
	private static final String WEEK = "week";

	/** The Constant WEEK. */
	private static final String OLDER = "older";

	/** The Constant HOURS_AGO. */
	private static final String HOURS_AGO = "hours_ago";

	/** The Constant ABOUT_1_HOUR_AGO. */
	private static final String ABOUT_1_HOUR_AGO = "about_1_hour_ago";

	/** The Constant MINUTES_AGO. */
	private static final String MINUTES_AGO = "minutes_ago";

	/** The Constant ABOUT_1_MINUTE_AGO. */
	private static final String ABOUT_1_MINUTE_AGO = "about_1_minute_ago";

	/** The Constant SECONDS_AGO. */
	private static final String SECONDS_AGO = "seconds_ago";

	/** The Constant RIGHT_NOW. */
	private static final String RIGHT_NOW = "right_now";

	/** The Constant TAG. */
	private static final String TAG = HumanTimeUtil.class.getSimpleName();

	/**
	 * Twitter human friendly date.
	 *
	 * @return the string
	 */
	public static String humanFriendlyDate(Long created) {
		// today
		Date today = new Date();

		// how much time since (ms)
		Long duration = today.getTime() - created;

		long second = 1000;
		long minute = second * 60;
		long hour = minute * 60;
		long day = hour * 24;

		if (duration < second * 7l) {
			return StringUtil.getStringResource(RIGHT_NOW);
		}

		if (duration < minute) {
			int n = (int) Math.floor(duration / second);
			return n + " " + StringUtil.getStringResource(SECONDS_AGO);
		}

		if (duration < minute * 2l) {
			return StringUtil.getStringResource(ABOUT_1_MINUTE_AGO);
		}

		if (duration < hour) {
			int n = (int) Math.floor(duration / minute);
			return n + " " + StringUtil.getStringResource(MINUTES_AGO);
		}

		if (duration < hour * 2l) {
			return StringUtil.getStringResource(ABOUT_1_HOUR_AGO);
		}

		if (duration < day) {
			int n = (int) Math.floor(duration / hour);
			return n + " " + StringUtil.getStringResource(HOURS_AGO);
		}
		if (duration > day && duration < day * 2l) {
			return StringUtil.getStringResource(YESTERDAY);
		}

		if (duration < day * 365l) {
			int n = (int) Math.floor(duration / day);
			return n + " " + StringUtil.getStringResource(DAYS_AGO);
		} else {
			return StringUtil.getStringResource(OVER_A_YEAR_AGO);
		}
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
			return StringUtil.getStringResource(TODAY);
		}
		if (duration > day && duration < day * 2l) {
			return StringUtil.getStringResource(YESTERDAY);
		}

		if (duration > day && duration < day * 8l) {
			return StringUtil.getStringResource(WEEK);
		}

        return StringUtil.getStringResource(OLDER);
	}

}
