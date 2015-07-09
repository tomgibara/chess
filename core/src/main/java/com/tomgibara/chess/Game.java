package com.tomgibara.chess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO add support for more tags
public class Game {

	private static final Pattern COMMA = Pattern.compile(",");
	private static final Pattern DOT = Pattern.compile("\\.");
	private static final Pattern COLON = Pattern.compile(":");
	private static final List<Integer> NULL_ROUND = Collections.emptyList();
	private static final Pattern DATE = Pattern.compile("((?:[1-9][0-9]{3})|(?:\\?{4}))\\.((?:[0-9]{2})|(?:\\?{2}))\\.((?:[0-9]{2})|(?:\\?{2}))");
	private static final Date ANY_DATE = new Date();
	private static final ThreadLocal<Calendar> localCalendar = new ThreadLocal<Calendar>() {
		protected Calendar initialValue() {
			//TODO how to nail down calendar
			return Calendar.getInstance();
		};
	};

	private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy.mm.dd");
		}
	};

	static final String TAG_EVENT = "Event";
	static final String TAG_SITE = "Site";
	static final String TAG_DATE = "Date";
	static final String TAG_ROUND = "Round";
	static final String TAG_WHITE = "White";
	static final String TAG_BLACK = "Black";
	static final String TAG_RESULT = "Result";
	
	public enum Result {
		WHITE_WON,
		BLACK_WON,
		DRAW,
		OTHER;
		
		public static Result from(String str) {
			if (str == null) throw new IllegalArgumentException("null str");
			switch (str) {
			case "1-0"     : return WHITE_WON;
			case "0-1"     : return BLACK_WON;
			case "1/2-1/2" : return DRAW;
			case "*"       : return OTHER;
			default        : throw new IllegalArgumentException("Unknown game result: " + str);
			}
		}
	}
	
	public final Map<String, String> tags;
	public final Sequence sequence;
	private Date date = null;
	private Site site;
	private List<Integer> round;
	private List<PlayerName> white;
	private List<PlayerName> black;
	private Result result;
	
	Game(Map<String, String> tags, Sequence sequence) {
		this.tags = Collections.unmodifiableMap(tags);
		this.sequence = sequence.immutable();
	}

	public String event() {
		return tags.get(TAG_EVENT);
	}
	
	public Site site() {
		if (site == null) {
			String str = tags.get(TAG_SITE);
			if (str == null) return null;
			site = Site.from(str);
		}
		return site;
	}
	
	public Date date() {
		if (date == null) {
			String str = tags.get(TAG_DATE);
			if (str == null) {
				date = ANY_DATE;
			} else try {
				date = Date.from(str);
			} catch (IllegalArgumentException e) {
				date = ANY_DATE;
			}
		}
		return date;
	}
	
	public List<Integer> round() {
		if (round == null) {
			String str = tags.get(TAG_ROUND);
			if (str == null || str.isEmpty()) {
				round = NULL_ROUND;
			} else try {
				String[] split = DOT.split(str);
				if (split.length == 1) {
					round = Collections.singletonList(Integer.parseInt(split[0]));
				} else {
					Integer[] ints = new Integer[split.length];
					for (int i = 0; i < ints.length; i++) {
						ints[i] = Integer.parseInt(split[i]);
					}
					round = Collections.unmodifiableList(Arrays.asList(ints));
				}
			} catch (NumberFormatException e) {
				//TODO log somehow
				round = NULL_ROUND;
			}
		}
		return round;
	}
	
	public List<PlayerName> white() {
		if (white == null) {
			white = colour(TAG_WHITE);
		}
		return white;
	}
	
	public List<PlayerName> black() {
		if (black == null) {
			black = colour(TAG_BLACK);
		}
		return black;
	}
	
	public Result result() {
		if (result == null) {
			String str = tags.get(TAG_RESULT);
			if (str == null) return null;
			result = Result.from(str);
		}
		return result;
	}
	
	private List<PlayerName> colour(String tagName) {
		String str = tags.get(tagName);
		if (str == null || str.isEmpty()) return Collections.emptyList();
		String[] split = COLON.split(str);
		try {
			if (split.length == 1) return Collections.singletonList(PlayerName.from(split[0]));
			PlayerName[] names = new PlayerName[split.length];
			for (int i = 0; i < names.length; i++) {
				names[i] = PlayerName.from(split[i]);
			}
			return Collections.unmodifiableList(Arrays.asList(names));
		} catch (IllegalArgumentException e) {
			return Collections.emptyList();
		}
	}
	
	// inner classes
	
	public static class Site {

		private static Site from(String str) {
			String[] parts = COMMA.split(str);
			//TODO need to analyze how to handle this
			if (parts.length != 3) throw new IllegalArgumentException();
			return new Site(norm(parts[0]), norm(parts[1]), norm(parts[2]));
		}
		
		private static String norm(String str) {
			return str.trim();
		}
		
		public final String city;
		public final String region;
		public final String countryCode;

		private Site(String city, String region, String countryCode) {
			this.city = city;
			this.region = region;
			this.countryCode = countryCode;
		}
		
		@Override
		public int hashCode() {
			return city.hashCode() + region.hashCode() + countryCode.hashCode();
		}
		
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Site)) return false;
			Site that = (Site) obj;
			if (!this.city.equals(that.city)) return false;
			if (!this.region.equals(that.region)) return false;
			if (!this.countryCode.equals(that.countryCode)) return false;
			return true;
		}
		
	}
	
	public static class Date {
		
		private final int year;
		private final int month;
		private final int day;
		
		private static final Date from(String str) {
			//TODO how to handle errors
			Matcher m = DATE.matcher(str);
			if (!m.matches()) {
				throw new IllegalArgumentException();
			}
			String yearStr = m.group(1);
			String monthStr = m.group(2);
			String dayStr = m.group(3);
			int year = yearStr.equals("????") ? 0 : Integer.parseInt(yearStr);
			int month = monthStr.equals("??") ? 0 : Integer.parseInt(monthStr);
			int day = dayStr.equals("??") ? 0 : Integer.parseInt(dayStr);

			if (month > 12) throw new IllegalArgumentException();
			if (day > 31) throw new IllegalArgumentException();
			
			if (year == 0 && month != 0 || month == 0 && day != 0) {
				throw new IllegalArgumentException();
			}
			
			if (day == 0) {
				return new Date(year, month, day);
			}

			Calendar c = localCalendar.get();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month - 1);
			c.set(Calendar.DATE, day);
			return new Date(c);
		}
		
		private Date() {
			this(0,0,0);
		}

		private Date(Calendar c) {
			this(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
		}
		
		private Date(int year, int month, int day) {
			this.year = year;
			this.month = month;
			this.day = day;
		}
		
		public int year() { return year; }

		public int month() { return month; }
		
		public int day() { return day; }
		
		@Override
		public int hashCode() {
			return year + 31 * ( month + 31 * day);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Date)) return false;
			Date that = (Date) obj;
			return
					this.year == that.year &&
					this.month == that.month &&
					this.day == that.day;
		}
		@Override
		public String toString() {
			return
				new StringBuilder(10)
				.append(year == 0 ? "????" : String.format("%04d", year))
				.append('.')
				.append(month == 0 ? "??" : String.format("%02d", month))
				.append('.')
				.append(day == 0 ? "??" : String.format("%02d", day))
				.toString();
		}
		
	}
	
	public final static class PlayerName {
		
		private static PlayerName from(String str) {
			String[] parts = COMMA.split(str);
			return parts.length == 1 ? new PlayerName("", norm(parts[0])) : new PlayerName(norm(parts[1]), norm(parts[0]));
		}
		
		private static String norm(String str) {
			return str.trim();
		}
		
		public final String firstName;
		public final String lastName;

		private PlayerName(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
		
		@Override
		public int hashCode() {
			return firstName.hashCode() + lastName.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (!(obj instanceof Site)) return false;
			Site that = (Site) obj;
			if (!this.firstName.equals(that.city)) return false;
			if (!this.lastName.equals(that.region)) return false;
			return true;
		}
		
		@Override
		public String toString() {
			if (lastName.isEmpty()) return firstName;
			return lastName + ", " + firstName;
		}
		
	}

}
