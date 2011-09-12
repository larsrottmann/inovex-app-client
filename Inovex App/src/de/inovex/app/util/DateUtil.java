package de.inovex.app.util;

import java.util.Date;

public class DateUtil {

	public static String getFormattedTimeSaldo(Date startDate, Date endDate) {
		long diffTime = endDate.getTime() - startDate.getTime();
		int timeInSeconds = (int) diffTime / 1000;
		int hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		int minutes = timeInSeconds / 60;
		String totalTime = (hours<10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes);
		return totalTime;
	}
}
