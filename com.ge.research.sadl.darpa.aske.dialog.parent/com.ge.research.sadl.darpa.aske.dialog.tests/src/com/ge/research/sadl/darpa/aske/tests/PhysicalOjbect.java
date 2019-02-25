package com.ge.research.sadl.darpa.aske.tests;

/**
 * Class to compute various properties of a physical object
 * @author 200005201
 *
 */
public class PhysicalOjbect {
	/**
	 * Method to computer the force on an object of constant mass. 
	 * @param mass --  mass of the object
	 * @param massUnit -- unit of mass value
	 * @param acceleration -- acceleration of the object
	 * @param accelerationUnit -- unit of acceleration
	 * @return -- force on the object
	 * @throws MissingUnitsException
	 */
	public float force(float mass, String massUnit, float acceleration, String accelerationUnit) throws MissingUnitsException {
		if (massUnit == null || accelerationUnit == null) {
			throw new MissingUnitsException("Units cannot be null");
		}
		if (massUnit != null && massUnit.equalsIgnoreCase("kg")) {
			if (accelerationUnit != null) {
				if (accelerationUnit.equals("m/sec2")) {
					return mass * acceleration;
				}
			}
		}
		throw new MissingUnitsException("Mass must be in 'kg', acceleration in 'm/sec2'");
	}
}
