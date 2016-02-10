package stellarium.compat.calendarapi;

import java.util.List;

import foxie.calendar.api.DayTimeDescriptor;
import foxie.calendar.api.ICalendarProvider;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import stellarium.StellarSky;
import stellarium.stellars.StellarManager;

public class StellarCalendarProvider implements ICalendarProvider {
	
	private long currentTick;
	private StellarManager manager;
	private double skyTick;
	private int day, year;
	
	public StellarCalendarProvider(long currentTick) {
		this.currentTick = currentTick;
		this.manager = StellarSky.getManager();
		this.skyTick = (long) manager.getSkyTime(currentTick);
		this.day = (int) Math.floor(this.skyTick / manager.day);
		this.year = (int) Math.floor(this.day / manager.year);
	}

	public StellarCalendarProvider() {
		this(0);
	}

	@Override
	public double getDaysInYear() {
		return manager.year;
	}

	@Override
	public int getDaysInYear(int year) {
		return (int) (Math.floor(manager.year*(year+1)) - Math.floor(manager.year*year));
	}

	@Override
	public long getTicksPerYear(int year) {
		return (int) (Math.floor(manager.year*manager.day*(year+1))
				- Math.floor(manager.year*manager.day*year));
	}

	@Override
	public double getTicksPerYear() {
		return manager.year * manager.day;
	}

	@Override
	public long getTime() {
		return this.currentTick;
	}

	@Override
	public int getDay() {
		return this.day;
	}

	@Override
	public ICalendarProvider setDay(int newDay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScaledDay() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setScaledDay(int newDay) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMonth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setMonth(int newMonth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScaledMonth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setScaledMonth(int newMonth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getYear() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setYear(int newYear) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getHour() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setHour(int newHour) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScaledHour() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setScaledHour(int newHour) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinute() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setMinute(int newMinutes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScaledMinute() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setScaledMinute(int newMinute) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getScaledSecond() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setScaledSecond(int newSecond) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSecond() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider setSecond(int newSecond) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addScaledSeconds(int seconds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addSeconds(int seconds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addScaledMinutes(int minutes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addMinutes(int minutes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addHours(int hours) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addDays(int days) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addMonths(int months) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider addYears(int years) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getDaysInMonth(int month) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDaysInMonth(int month, int year) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ICalendarProvider create(World world) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider create(WorldProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider create(long time) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNumberOfMonths() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String[] getListOfMonthsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICalendarProvider copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void apply(World world) {
		world.setWorldTime(this.currentTick);
	}

	@Override
	public List<DayTimeDescriptor> getDayTimeDescriptors(int tolerance) {
		// TODO Auto-generated method stub
		return null;
	}

}
