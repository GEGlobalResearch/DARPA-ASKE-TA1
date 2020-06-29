package windTurbine;

/**
 * This class is meant for creating {@code WindTurbine} objects, effectively modeling a wind turbine
 * with a given radius, number of blades and tip speed ratio.<br><br>
 * This class also provides the necessary methods to
 * compute the efficiency of a HAWT wind turbine iteratively in terms of power coefficient, C<sub>p</sub>,
 * using blade element momentum theory.
 * 
 * @author Mitchell Keeley, Chaitanya Varier
 * @version 05/19/2016
 */

public class WindTurbine {
	
	// Class variables
	private static final double PI = Math.PI;
	private static final double e = Math.E;
	private static final double DIFF_THRESHOLD = 1e-15;
	public static final double dr = 0.05;   // Radius increment in m
	
	// Instance variables
	private int B = 3;
	private double R = 3.0;
	private double lambda_R = 6.0;
	private double dlambda_r;
	private double Omega; // Blade rotational speed in rad/s, giving lambda at blade tip (max lambda)
	
	/**
	 * Constructor for instantiating {@code WindTurbine} objects, which model wind turbines
	 * with a given radius, number of blades and tip speed ratio.
 	 * 
	 * @param radius			The radius of the turbine in m.
	 * @param numBlades			The number of blades of the turbine.
	 * @param tipSpeedRatio		The tip speed ratio of the turbine at the blade tip.
	 */
	
	public WindTurbine(double radius, int numBlades, double tipSpeedRatio) {
		R = radius;
		B = numBlades;
		lambda_R = tipSpeedRatio;
		Omega = lambda_R/R;
		dlambda_r = Omega*dr;
	}
	
	/**
	 * Calculates the coefficient of power, C<sub>p</sub>, of the wind turbine instantiated by its
	 * respective {@code WindTurbine} object, using momentum theory.
	 * 
	 * @return Coefficient of power, C<sub>p</sub>, expressed in terms of % of total input wind power.
	 */

	public double getPowerCoefficient () {
		
		// Let C_Ptot be the running sum for the coefficient of power
		double C_Ptot = 0;
		double lambda_r;
		double phi;
			
		double alpha;
		double C_L;
		double C_D;
		double C_t;
		double C_n;
		double sigmaPrime;
		double a_next;
		double aPrime_next;
		double aPrime;
		double a;
		double F_tilde;
		double f;
		double F;
		
		System.out.println("------------Intermediary Values-------------");
		System.out.println("radial position(m)\ta\t\t\taPrime");

		for (double radius = dr; radius <= R; radius += dr) {

			//---------Calculate a and aPrime at current radial position-----------

			// Step 1: Guess a (axial induction factor) and aPrime (angular induction factor)
			lambda_r = Omega*radius;
			phi = 0;
			
			// Look up C_d(alpha) and C_l(alpha) from the Coefficients class
			sigmaPrime = B * getChordLength(radius) /(2*PI*radius); // Calculate local solidity
			a_next = 1./3;
			aPrime_next = 0.0;
			aPrime = 1;
			a = 1;
			F_tilde = 0;
			

			// Step 2: Allow a and aPrime to converge to their true values (within the difference threshold)
			while (Math.abs(a_next-a) > DIFF_THRESHOLD && Math.abs(aPrime_next-aPrime) > DIFF_THRESHOLD) {
				
				/* Set the current values of a and aPrime to the values of a and aPrime calculated
				 * in the previous iteration.
				 */
				a = a_next;
				aPrime = aPrime_next;
				
				// Calculate angle between reference plane and relative wind
				phi = Math.atan((1-a)/(lambda_r*(1+aPrime)));
				
				// Calculate angle of attack, defined as angle between chord line and relative wind
				alpha = phi - getTwist(radius);
				
				// Look up C_d(alpha) and C_l(alpha)
				C_L = Coefficients.getCoefficientLift(alpha);
				C_D = Coefficients.getCoefficientDrag(alpha);
				C_t = C_L*Math.sin(phi) - C_D*Math.cos(phi);
				C_n = C_L*Math.cos(phi) + C_D*Math.sin(phi);
				
				// Calculate tip loss correction factor
				f = B*(R-radius)/(2*radius*Math.sin(phi));
				F = 2/PI*Math.acos(Math.pow(e, -f));
				F_tilde = F*Math.max(1.0, (1-a/4*(5-3*a))/(1-a));
				
				/* Cancel the effect of the tip loss correction factor 
				 * if the current segment is at the end of the blade
				 */
				if (Math.abs(R-radius)<1e-6 || Double.isNaN(F_tilde)) {
					F_tilde = 1;
					F = 1;
				}
				
				// Calculate the new values of a and aPrime
				a_next = 1/(4*F_tilde*(Math.sin(phi))*(Math.sin(phi))/(C_n*sigmaPrime) + 1);
				aPrime_next = 1/(4*F*(Math.sin(phi))*(Math.cos(phi))/(C_t*sigmaPrime) - 1);
			}
			
			System.out.printf("%.2f\t\t\t" + "%.6f\t\t" + "%.6f\n", radius, a_next, aPrime_next);
			

			//--------Calculate dC_P and add it to the running sum for the power coefficient---------
			
			C_Ptot += 8/(lambda_R*lambda_R)*F_tilde*aPrime*(1-a)*lambda_r*lambda_r*lambda_r*dlambda_r;
			
		}
		
		//--------Return the total sum of all the C_P contributions from each blade segment---------
		
		return C_Ptot*100;

	}
	
	public double getPowerCoefficientNoTip () {
		
		// Let C_Ptot be the running sum for the coefficient of power
		double C_Ptot = 0;
		double lambda_r;
		double phi;
			
		double alpha;
		double C_L;
		double C_D;
		double C_t;
		double C_n;
		double sigmaPrime;
		double a_next;
		double aPrime_next;
		double aPrime;
		double a;
		double F_tilde;
		double f;
		double F;

		
		System.out.println("------------Intermediary Values-------------");
		System.out.println("radial position(m)\ta\t\t\taPrime");

		for (double radius = dr; radius <= R; radius += dr) {

			//---------Calculate a and aPrime at current radial position-----------

			// Step 1: Guess a (axial induction factor) and aPrime (angular induction factor)
			lambda_r = Omega*radius;
			phi = 0;
			
			// Look up C_d(alpha) and C_l(alpha) from the Coefficients class
			sigmaPrime = B * getChordLength(radius) /(2*PI*radius); // Calculate local solidity
			a_next = 1./3;
			aPrime_next = 0.0;
			aPrime = 1;
			a = 1;
			

			// Step 2: Allow a and aPrime to converge to their true values (within the difference threshold)
			while (Math.abs(a_next-a) > DIFF_THRESHOLD && Math.abs(aPrime_next-aPrime) > DIFF_THRESHOLD) {
				
				/* Set the current values of a and aPrime to the values of a and aPrime calculated
				 * in the previous iteration.
				 */
				a = a_next;
				aPrime = aPrime_next;
				
				// Calculate angle between reference plane and relative wind
				phi = Math.atan((1-a)/(lambda_r*(1+aPrime)));
				
				// Calculate angle of attack, defined as angle between chord line and relative wind
				alpha = phi - getTwist(radius);
				
				// Look up C_d(alpha) and C_l(alpha)
				C_L = Coefficients.getCoefficientLift(alpha);
				C_D = Coefficients.getCoefficientDrag(alpha);
				C_t = C_L*Math.sin(phi) - C_D*Math.cos(phi);
				C_n = C_L*Math.cos(phi) + C_D*Math.sin(phi);
				
				// Calculate the new values of a and aPrime
				a_next = 1/(4*(Math.sin(phi))*(Math.sin(phi))/(C_n*sigmaPrime) + 1);
				aPrime_next = 1/(4*(Math.sin(phi))*(Math.cos(phi))/(C_t*sigmaPrime) - 1);
			}
			
			System.out.printf("%.2f\t\t\t" + "%.6f\t\t" + "%.6f\n", radius, a_next, aPrime_next);
			

			//--------Calculate dC_P and add it to the running sum for the power coefficient---------
			
			C_Ptot += 8/(lambda_R*lambda_R)*aPrime*(1-a)*lambda_r*lambda_r*lambda_r*dlambda_r;
			
		}
		
		//--------Return the total sum of all the C_P contributions from each blade segment---------
		
		return C_Ptot*100;

	}
	
	/**
	 * Returns the chord length of the wind turbine airfoil at a particular radial 
	 * position along the blade.
	 * 
	 * @param pos The radial position of the current blade segment in m.
	 * @return The chord length at the current blade segment in m.
	 */
	private double getChordLength (double pos) {

		return R/15-3.0/50*pos;

	}

	/**
	 * Returns the local pitch angle (theta<sub>p</sub>), or twist of the 
	 * wind turbine airfoil at a particular radial position along the blade.
	 * The pitch angle is defined as the angle between the chord line and the reference plane.
	 * 
	 * @param pos The radial position of the current blade segment in m.
	 * @return The local pitch angle at the current blade segment in radians.
	 */
	private double getTwist (double pos) {

		return 0.02*3/R*Math.log(pos)/Math.log(0.3);
		
	}

}