package windTurbine;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.math.plot.Plot2DPanel;
import org.math.plot.plotObjects.BaseLabel;

/**
 * This class runs a simulation of a wind turbine, ultimately calculating its electrical efficiency
 * in terms of the power coefficient. A {@code WindTurbine} object is created, and its power coefficient
 * is then computed and displayed. 
 * 
 * @author Mitchell Keeley, Chaitanya Varier
 * @version 05/19/2016
 */

public class TurbineSimulation {
	
	private static WindTurbine windTurbine;
	private static final double lambdaMax = 20.75;
	private static final double lambdaMin = 0.25;
	private static final double dlambda = 0.25;
	private static final double radius = 3.0;
	private static final int numBlades = 3;
	private static final double tipSpeedRatio = 6.0;
	private static boolean printLambdaVsCp = true;
	
	public static void main (String args[]) {
		
		/* Create a HAWT wind turbine with a blade radius of 3.0 m,
		 * three blades and a max tip speed ratio of 6.0.
		 */
		
		windTurbine = new WindTurbine(radius, numBlades, tipSpeedRatio);
		
		double powerCoefficient = windTurbine.getPowerCoefficient();
		
		System.out.printf("\nThe electrical efficiency of the HAWT wind turbine with radius " +
				"%.2f m,\n%d blades and a max tip speed ratio " +
				"of %.2f is: %.2f%%.",
				radius,numBlades,tipSpeedRatio,powerCoefficient);
		
		if (printLambdaVsCp)
			printLambdaVsCp(radius, numBlades, lambdaMin, lambdaMax, dlambda);
		
	}
	
	/**
	 * Displays the tip speed ratio (lambda) vs. coefficient of power (C<sub>p</sub>) curve for
	 * a given turbine radius, number of blades, minimum lambda, maximum lambda and a lambda step size 
	 * for determining the number of points to plot on the graph.
	 * 
	 * @param radius The radius of the turbine in m.
	 * @param numBlades	The number of blades of the turbine.
	 * @param lambdaMin	The starting lambda value for the graph.
	 * @param lambdaMax	The final lambda value for the graph.
	 * @param dlambda 	The step size in lambda, indicating the resolution and number of points on the
	 * graph.
	 */
	public static void printLambdaVsCp (double radius, int numBlades, double lambdaMin, double lambdaMax, double dlambda) {
		
		int lambdaCount = (int) ((lambdaMax-lambdaMin)/dlambda)+1;
		
		double[] lambdaArr = new double[lambdaCount];
		double[] powerCoefficientArr = new double[lambdaCount];
		double[] powerCoefficientArrNoTip = new double[lambdaCount];
		int iter = 0;
		
		for (double lambdaVar=lambdaMin; lambdaVar<lambdaMax+dlambda; lambdaVar+=dlambda) {
			
			windTurbine = new WindTurbine(radius, numBlades, lambdaVar);
			double powerCoefficient = windTurbine.getPowerCoefficient();
			double powerCoefficientNoTip = windTurbine.getPowerCoefficientNoTip();
			
			lambdaArr[iter] = lambdaVar;
			powerCoefficientArr[iter] = powerCoefficient;
			powerCoefficientArrNoTip[iter] = powerCoefficientNoTip;
			iter++;
			
		}
		
		Font plotFont = new Font(Font.MONOSPACED,Font.PLAIN,12);
		
		//-----------Create lambda vs. C_p plot-----------
		
		// Create a PlotPanel
		Plot2DPanel plot1 = new Plot2DPanel();
		
		// Add a line plot and labels to the PlotPanel
		plot1.addLinePlot("With Tip Loss",Color.BLUE, lambdaArr, powerCoefficientArr);
		// Add a line plot and labels to the PlotPanel
		plot1.setFixedBounds(1,0, 50.0);
		plot1.setFixedBounds(0,lambdaMin, lambdaMax);
		plot1.setAxisLabels("lambda", "C_p (%)");
		plot1.getAxis(0).setLabelPosition(0.5, -0.1);
		plot1.getAxis(0).setLabelFont(plotFont);
		plot1.getAxis(1).setLabelPosition(-0.15,0.5);
		plot1.getAxis(1).setLabelFont(plotFont);
		BaseLabel title1 = new BaseLabel("lambda vs. C_p",Color.BLACK,0.5, 1.1);
		title1.setFont(plotFont);
		plot1.addPlotable(title1);
		
		// Put the PlotPanel in a JFrame, as a JPanel
		JFrame frame1 = new JFrame("Output 1");
		frame1.setSize(1024, 576);
		frame1.setContentPane(plot1);
		frame1.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame1.setVisible(true);
		
	}

}
