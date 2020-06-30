/**
 * Copyright 2018 General Electric Company
 */
package com.ge.research.answer.java;

/**
 * Class to provide temperature conversions between different units
 * @author 200005201
 *
 */
public class TemperatureConversion {
	/**
	 * Method to convert from F to C
	 * @param tf -- temperature in degrees F
	 * @return -- temperature in degrees C
	 */
	public float convertFtoC(float tf) {
		return (float) ((tf - 32.0) * 9.0 / 5.0);
	}
	
	/**
	 * Method to convert from C to F
	 * @param tc -- temperature in degrees C
	 * @return -- temperature in degrees F
	 */
	public float convertCtoF(float tc) {
		return tc * 5 / 9 + 32;
	}
}
