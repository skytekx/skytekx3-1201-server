package twilightforest.util;

import java.time.Month;
import java.time.MonthDay;

/**
 * Seasonal checks. 4.8 makes this a Beanification component; a static helper is enough here.
 */
public final class HolidayEvent {

	private HolidayEvent() {
	}

	public static boolean isHalloweenWeek() {
		MonthDay now = MonthDay.now();
		return now.isAfter(MonthDay.of(Month.OCTOBER, 19)) && now.isBefore(MonthDay.of(Month.NOVEMBER, 4));
	}
}
