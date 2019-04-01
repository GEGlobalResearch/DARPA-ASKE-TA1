/*  
                      Similarity  Parameter Calculator 
                                (SPC)
                Interactive Program to solve for flow variables
                          A Java Applet

                     Version 1.1b   - 20 Jan 09

                         Written by Tom Benson
                       NASA Glenn Research Center

>                              NOTICE
>This software is in the Public Domain.  It may be freely copied and used in
>non-commercial products, assuming proper credit to the author is given.  IT
>MAY NOT BE RESOLD.  If you want to use the software for commercial
>products, contact the author.
>No copyright is claimed in the United States under Title 17, U. S. Code.
>This software is provided "as is" without any warranty of any kind, either
>express, implied, or statutory, including, but not limited to, any warranty
>that the software will conform to specifications, any implied warranties of
>merchantability, fitness for a particular purpose, and freedom from
>infringement, and any warranty that the documentation will conform to the
>program, or any warranty that the software will be error free.
>In no event shall NASA be liable for any damages, including, but not
>limited to direct, indirect, special or consequential damages, arising out
>of, resulting from, or in any way connected with this software, whether or
>not based on warranty, contract, tort or otherwise, whether or not injury
>was sustained by persons or property or otherwise, and whether or not loss
>was sustained from, or arose out of the results of, or use of, the software
>or services provided hereunder.

                correction for calculation of nu in metric units

Edited:  AJV May 21, 2009

*/

import java.awt.*;
import java.lang.Math ;

public class SPCal extends java.applet.Applet 
	{
   	 double gama,alt,ts0,ps0,vel,q0,lng ;
   	 double tt0,pt0, mu, nu, rey ;
   	 double rgas, rho0, rho, a0, lrat, mach ;
   	 int lunits,inparam,ther,prs, planet ;
   	 int mode ;

   	 In in ;

   	 public void init() 
		{
     		 setLayout(new GridLayout(1,1,0,0)) ;

     		 setDefaults () ;

     		 in = new In(this) ;

     		 add(in) ;

     		 computeFlow() ;
  		}
 
  	public Insets insets() 
		{
     		 return new Insets(10,10,10,10) ;
  		}

  	public void setDefaults() 
		{
     		 inparam = 0 ;
     		 lunits = 0;
     		 mode = 0 ;
     		 planet = 0 ;

     		 alt = 0.0 ;
     		 vel = 500. ;
     		 lng = 1.0 ;
  		}

  	public void computeFlow() 
		{
     		 double trat ;
 
     		 if (planet == 0) // Earth  standard day
			{          /* ft2/sec2 R */
        		 rgas = 1716. ;                
        		 gama = 1.4 ;

        		if (alt <= 36152.)            // Troposphere
				{
          			 ts0 = 518.688 - 3.56 * alt/1000. ;
          			 ps0 = 2116. * Math.pow(ts0/518.688,5.256) ;
        			}
        		if (alt >= 36152. && alt <= 82345.)   // Stratosphere
				{
          			 ts0 = 389.98 ;
          			 ps0 = 2116. * .2236 *
                 		   Math.exp((36152.-alt)/(53.35*389.98)) ;
        			}
        		if (alt >= 82345. && alt <= 155348.) 
				{          
          			 ts0 = 389.98 + 1.645 * (alt-82345.)/1000. ;
          			 ps0 = 2116. *.02456 * Math.pow(ts0/389.98,-11.388) ;
        			}
     			}

     		 if (planet == 1)   // Mars - curve fit of orbiter data
			{                /* ft2/sec2 R */
        		 rgas = 1149. ;                
        		 gama = 1.29 ;

        		 if (alt <= 22960.) 
				{
          			 ts0 = 434.02 - .548 * alt/1000. ;
          			 ps0 = 14.62 * Math.pow(2.71828,-.00003 * alt) ;
        			}
        		if (alt > 22960.) 
				{
          			 ts0 = 449.36 - 1.217 * alt/1000. ;
          			 ps0 = 14.62 * Math.pow(2.71828,-.00003 * alt) ;
        			}
     			}
 		 // speed of sound
     		 rho0 = ps0 / rgas / ts0 ;
     		 a0 = Math.sqrt(gama*rgas*ts0) ;  // feet /sec
     		 a0 = a0 * 60.0 / 88. ;   // mph
                       // compute either mach or velocity 
     		 if (inparam == 0) mach = vel/a0 ;
     		 if (inparam == 1) vel = mach * a0 ;

     		 q0 = gama / 2.0*mach*mach*ps0 ;
     		 trat = (1.0 + .5 * (gama - 1.0) * mach * mach) ;
     		 tt0 = ts0 * trat ;
     		 pt0 = ps0 * Math.pow(trat, (gama/(gama-1.0))) ;
 
//     mu = .0000020886*.1716*690.6/(ts0 + 199.)*Math.pow(ts0/491.6,1.5) ;
     		 mu = .000000362*717.408/(ts0 + 198.72)*Math.pow(ts0/518.688,1.5) ;
     		 nu = mu / rho0 ;

     		 rey = vel * 88.0 / 60.0 * lng / nu ;

		 loadOut() ;

     		 if (mode == 0) loadInpt() ;
  		}
 
  	public void loadOut() 
		{
     		 String outvel,outlng,outprs,outtmp,outden,outmu,outnu ;

		 outlng = " ft" ;
     		 if (lunits == 1) outlng = " m" ;
     		 outvel = " mph" ;
     		 if (lunits == 1) outvel = " m/sec" ;
     		 outprs = " psf" ;
     		 if (lunits == 1) outprs = " kPa" ;
     		 outtmp = " R" ;
     		 if (lunits == 1) outtmp = " K" ;
     		 outden = " slug/ft^3" ;
     		 if (lunits == 1) outden = " kg/m^3 " ;
     		 outmu = " lb-s/ft^2" ;
     		 if (lunits == 1) outmu = " N-s/m^2" ;
      		 outnu = " ft^2/s" ;
     		 if (lunits == 1) outnu = " m^2/s" ;

     		 if (lunits == 0) 
			{
        		in.dn.o3.setText(String.valueOf(filter0(a0)) + outvel);
        		in.dn.o4.setText(String.valueOf(filter0(vel)) + outvel);
        		in.dn.o6.setText(String.valueOf(filter0(ps0)) + outprs);
        		in.dn.o7.setText(String.valueOf(filter0(ts0)) + outtmp);
        		in.dn.o8.setText(String.valueOf(filter9(rho0)) + outden);
        		in.dn.o9.setText(String.valueOf(filter0(pt0)) + outprs);
        		in.dn.o10.setText(String.valueOf(filter0(tt0)) + outtmp);
        		in.dn.o11.setText(String.valueOf(filter0(q0)) + outprs);
        		in.dn.o12.setText(String.valueOf(filter9(mu)) + outmu);
        		in.dn.o13.setText(String.valueOf(filter9(nu)) + outnu);
     			}
     		 if (lunits == 1) 
			{
        		in.dn.o3.setText(String.valueOf(filter0(a0  * .447)) + outvel);
        		in.dn.o4.setText(String.valueOf(filter0(vel * .447)) + outvel);
        		in.dn.o6.setText(String.valueOf(filter3(ps0 * 4.448/.3048/.3048/1000.)) + outprs) ;
        		in.dn.o7.setText(String.valueOf(filter0(ts0 * .55555)) + outtmp) ;
        		in.dn.o8.setText(String.valueOf(filter9(rho0 * 515.4)) + outden) ;
        		in.dn.o9.setText(String.valueOf(filter3(pt0 * 4.448/.3048/.3048/1000.)) + outprs) ;
        		in.dn.o10.setText(String.valueOf(filter0(tt0 * .55555)) + outtmp) ;
        		in.dn.o11.setText(String.valueOf(filter3(q0 * 4.448/.3048/.3048/1000.)) + outprs) ;
        		in.dn.o12.setText(String.valueOf(filter9(mu * 47.879)) + outmu) ;
        		in.dn.o13.setText(String.valueOf(filter9(nu / 10.7639)) + outnu) ;
     			}
 
     		 in.dn.o5.setText(String.valueOf(filter3(mach))) ;
     		 in.dn.o14.setText(String.valueOf(filter0(rey))) ;
  		}

  	public void loadInpt() 
		{
     		if (lunits == 0) 
			{
         		in.up.o1.setText(String.valueOf(filter0(alt))) ;
         		in.up.o3.setText(String.valueOf(filter3(lng))) ;
         		if (inparam == 0) 
				{
            			in.up.o2.setText(String.valueOf(filter0(vel))) ;
         			}
         		if (inparam == 1) 
				{
            			in.up.o2.setText(String.valueOf(filter3(mach))) ;
         			}
     			}
     		if (lunits == 1) 
			{
         		in.up.o1.setText(String.valueOf(filter0(alt*.3048))) ;
         		in.up.o3.setText(String.valueOf(filter3(lng * .3048))) ;
         		if (inparam == 0) 
				{
            			in.up.o2.setText(String.valueOf(filter0(vel*.447))) ;
         			}
         		if (inparam == 1) 
				{
            			in.up.o2.setText(String.valueOf(filter3(mach))) ;
         			}
     			}
  		}

  	public int filter0(double inumbr) 
		{
     		//  integer output
       		float number ;
       		int intermed ;

       		intermed = (int) (inumbr) ;
       		number = (float) (intermed);
       		return intermed ;
  		}
 
  	public float filter3(double inumbr) 
		{
     		//  output only to .001
       		float number ;
       		int intermed ;
  
       		intermed = (int) (inumbr * 1000.) ;
       		number = (float) (intermed / 1000. );
       		return number ;
  		}

  	public float filter5(double inumbr) 
		{
     		//  output only to .00001
       		float number ;
       		int intermed ;
  
       		intermed = (int) (inumbr * 100000.) ;
       		number = (float) (intermed / 100000. );
       		return number ;
  		}

  	public float filter9(double inumbr) 
		{
     		//  output only to .000000001
       		float number ;
       		int intermed ;
  
       		intermed = (int) (inumbr * 1000000000.) ;
       		number = (float) (intermed / 1000000000. );
       		return number ;
  		}

class In extends Panel 
	{
     	SPCal outerparent ;
     	Titl titl ;
     	Up up ;
     	Dn dn ;

     	In (SPCal target) 
		{                           
        	outerparent = target ;
        	setLayout(new GridLayout(3,1,5,5)) ;
		setBackground(Color.white);

        	titl = new Titl(outerparent) ;
        	up = new Up(outerparent) ;
        	dn = new Dn(outerparent) ;

        	add(titl) ;
        	add(up) ;
        	add(dn) ;
     		}

     	public Insets insets() 
		{
        	return new Insets(5,5,0,0) ;
     		}

     	class Titl extends Panel 
		{
        	Label la,lb;
        	Choice untch ;
		Choice choplan;

        	Titl (SPCal target) 
			{                           
            		outerparent = target;
	    		setLayout(new GridLayout(3,4,0,0)) ;

            		la = new Label("Similarity Parameter", Label.RIGHT) ;
            		la.setForeground(Color.black) ;

			lb = new Label("Calculator", Label.LEFT);
			lb.setForeground(Color.black);

            		untch = new Choice() ;
            		untch.addItem("Imperial") ;
            		untch.addItem("Metric");
            		untch.select(0) ;

			choplan = new Choice();
			choplan.addItem("Earth");
			choplan.addItem("Mars");
			choplan.select(0);

            		add(new Label(" ", Label.CENTER)) ;
            		add(la) ;
            		add(lb) ;
            		add(new Label(" ", Label.CENTER)) ;

            		add(new Label(" ", Label.CENTER)) ;
            		add(new Label("Units: ", Label.RIGHT)) ;
            		add(untch) ;
            		add(new Label(" ", Label.CENTER)) ;

            		add(new Label(" ", Label.CENTER)) ;
			add(new Label("Planet: ", Label.RIGHT));
			add(choplan);
            		add(new Label(" ", Label.CENTER)) ;
        		}

        	public boolean action(Event evt, Object arg) 
			{
			if(evt.target instanceof Choice)
				{ 
				this.handleProb(arg);
			  	return true;
					}
			else return false;
			}

		public void handleProb(Object obj)
			{
		 	lunits  = untch.getSelectedIndex() ;
			planet = choplan.getSelectedIndex();

		 	if (lunits == 0) // English units labels
				{  
                	 	up.l1u.setText("feet") ;
                	 	up.l3u.setText("feet") ;
                	 	if (inparam == 0) 
					{
                     		 	up.l2u.setText("mph") ;
                			}
                	 	if (inparam == 1) 
					{
                     		 	up.l2u.setText(" ") ;
                			}
            			}
            	 	if (lunits == 1) // Metric units labels
				{  
                	 	up.l1u.setText("meters") ;
                	 	up.l3u.setText("meters") ;
                	 	if (inparam == 0) 
					{
                     		 	up.l2u.setText("m/sec") ;
                			} 
                		if (inparam == 1) 
					{
                     		 	up.l2u.setText(" ") ;
                			}
            			}
 
            	 	mode = 0 ;
            	 	computeFlow() ;
        		}
     		}

	class Up extends Panel
		{
        	TextField o1,o2,o3 ;
        	Label l1,l1u,l2u,l3,l3u ;
        	Label la,lb,lc,ld ;
        	Choice inptch ;
		Button calculate;
	

        	Up (SPCal target) 
			{                           
            		outerparent = target ;
            		setLayout(new GridLayout(6,4,5,5)) ;
    
            		la = new Label("Input", Label.LEFT) ;
            		la.setForeground(Color.red) ;

			lc = new Label("Please Input Altitude,", Label.RIGHT);
			lc.setForeground(Color.red);

            		ld = new Label("Speed, and Length Scale", Label.LEFT) ;
            		ld.setForeground(Color.red) ;

            		l1 = new Label("Altitude", Label.RIGHT) ;
            		l1u = new Label(" feet ", Label.LEFT) ;
            		o1 = new TextField() ;
            		o1.setBackground(Color.white) ;
            		o1.setForeground(Color.black) ;

            		l2u = new Label("mph", Label.LEFT) ;
            		o2 = new TextField() ;
            		o2.setBackground(Color.white) ;
            		o2.setForeground(Color.black) ;
 
            		l3 = new Label("Length", Label.RIGHT) ;
            		l3u = new Label("feet", Label.LEFT) ;
            		o3 = new TextField() ;
            		o3.setBackground(Color.white) ;
            		o3.setForeground(Color.black) ;
 
            		inptch = new Choice() ;
            		inptch.addItem("Speed") ;
            		inptch.addItem("Mach");
            		inptch.select(0) ;

            		calculate = new Button("Calculate!");
	    		calculate.setBackground(Color.red);
	    		calculate.setForeground(Color.white);
            		
			add(la) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;

            		add(new Label(" ", Label.RIGHT)) ;
            		add(lc) ;
            		add(ld) ;
            		add(new Label(" ", Label.RIGHT)) ;

            		add(new Label(" ", Label.RIGHT)) ;            		
			add(l1) ;
            		add(o1) ;
            		add(l1u) ;

            		add(new Label(" ", Label.RIGHT)) ;
            		add(inptch) ;
            		add(o2) ;
            		add(l2u) ;

            		add(new Label(" ", Label.RIGHT)) ;
            		add(l3) ;
            		add(o3) ;
            		add(l3u) ;

	    		add(new Label(" ", Label.RIGHT));
	    		add(new Label(" ", Label.RIGHT));
  	    		add(calculate);
	    		add(new Label(" ", Label.RIGHT));
        		}

        	public Insets insets() 
			{
           		return new Insets(5,5,5,5) ;
        		}

        	public boolean action(Event evt, Object arg) 
			{
            		if(evt.target instanceof Choice) 
				{
               			this.handleProb(arg) ;
               			return true ;
            			}

	    		if(evt.target instanceof Button)  //Sets button action command
				{
		 		this.handleText(evt);
		 		return true;
				}

            		else return false ;
        		}
 
        	public void handleProb(Object obj) 
			{

            		inparam = inptch.getSelectedIndex() ;

            		if (lunits == 0)  // English units labels
				{
                		if (inparam == 0) 
					{
                     			l2u.setText("mph") ;
                			}
                		if (inparam == 1) 
					{
                     			l2u.setText(" ") ;
                			}
            			}
            		if (lunits == 1)   // Metric units labels
				{
                		if (inparam == 0) 
					{
                     			l2u.setText("m/sec") ;
                			} 
                		if (inparam == 1) 
					{
                     			l2u.setText(" ") ;
                			}
            			}
 
            		mode = 0 ;
            		computeFlow() ;
        		}

        	public void handleText(Event evt) 
			{
            		Double V1,V2,V3 ;
            		double v1,v2,v3 ;

            		V1 = Double.valueOf(o1.getText()) ;
            		v1 = V1.doubleValue() ;
            		V2 = Double.valueOf(o2.getText()) ;
            		v2 = V2.doubleValue() ;
            		V3 = Double.valueOf(o3.getText()) ;
            		v3 = V3.doubleValue() ;

            		if (lunits == 0) 
				{
                		if (v1 < 0.0) 
					{
                   			v1 = 0.0 ;
                   			o1.setText(String.valueOf(filter0(v1))) ;
                			}
                		if (v1 >150000.0) 
					{
                   			v1 = 150000.0 ;
                   			o1.setText(String.valueOf(filter0(v1))) ;
                			}
                		if (inparam == 0) 
					{
                   			if (v2 < 0.0) 
						{
                      				v2 = 0.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			if (v2 >17600.0) 
						{
                      				v2 = 17600.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			vel = v2 ;
                			}
                		if (inparam == 1) 
					{
                   			if (v2 < 0.0) 
						{
                      				v2 = 0.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			if (v2 >25.0) 
						{
                      				v2 = 25.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			mach = v2 ;
                			}
                		if (v3 < 0.001) 
					{
                   			v3 = 0.001 ;
                   			o3.setText(String.valueOf(filter0(v3))) ;
                			}
                		if (v3 >100.0) 
					{
                   			v3 = 100.0 ;
                   			o3.setText(String.valueOf(filter0(v3))) ;
                			}
                		alt = v1 ;
                		lng = v3 ;
            			}
            		if (lunits == 1) 
				{
                		if (v1 < 0.0) 
					{
                   			v1 = 0.0 ;
                   			o1.setText(String.valueOf(filter0(v1))) ;
                			}
                		if (v1 >45700.0) 
					{
                   			v1 = 45700.0 ;
                   			o1.setText(String.valueOf(filter0(v1))) ;
                			}
                		if (inparam == 0) 
					{
                   			if (v2 < 0.0) 
						{
                      				v2 = 0.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			if (v2 >7867.0) 
						{
                      				v2 = 7867.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			vel = v2 / .447 ;
                			}
                		if (inparam == 1) 
					{
                   			if (v2 < 0.0) 
						{
                      				v2 = 0.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			if (v2 >25.0) 
						{
                      				v2 = 25.0 ;
                      				o2.setText(String.valueOf(filter0(v2))) ;
                   				}
                   			mach = v2 ;
                			}
                		if (v3 < 0.0001) 
					{
                   			v3 = 0.0001 ;
                   			o3.setText(String.valueOf(filter0(v3))) ;
                			}
                		if (v3 >30.0) 
					{
                   			v3 = 30.0 ;
                   			o3.setText(String.valueOf(filter0(v3))) ;
                			}
                		alt = v1 / .3048 ;
                		lng = v3 / .3048 ;
            			}

            		mode = 1;
	    		computeFlow();
        		}
     		}

	class Dn extends Panel 
		{
        	SPCal outerparent ;
        	TextField o3,o4,o5,o6,o7,o8,o9,o10,o11,o12,o13,o14 ;
        	Label l3,l4,l5,l6,l7,l8,l9,l10,l11,l12,l13,l14 ;
        	Label lb,lc,lv,lsm ;

        	Dn (SPCal target) 
			{
            		outerparent = target ;
            		setLayout(new GridLayout(6,6,0,0)) ;
    
            		lb = new Label("Output:", Label.LEFT) ;
            		lb.setForeground(Color.blue) ;

            		lc = new Label("Compressibility", Label.CENTER) ;
            		lc.setForeground(Color.blue) ;

            		lv = new Label("Viscosity", Label.CENTER) ;
            		lv.setForeground(Color.blue) ;

			lsm = new Label("Speed/Mach Number", Label.CENTER);
			lsm.setForeground(Color.blue);

            		l3 = new Label("Speed of Sound", Label.CENTER) ;
            		o3 = new TextField() ;
            		o3.setBackground(Color.black) ;
            		o3.setForeground(Color.yellow) ;

            		l4 = new Label("Speed", Label.CENTER) ;
            		o4 = new TextField() ;
            		o4.setBackground(Color.black) ;
            		o4.setForeground(Color.yellow) ;
 
            		l5 = new Label("Mach #", Label.CENTER) ;
            		o5 = new TextField() ;
            		o5.setBackground(Color.blue) ;
            		o5.setForeground(Color.white) ;
 
            		l6 = new Label("P static", Label.CENTER) ;
            		o6 = new TextField() ;
            		o6.setBackground(Color.black) ;
            		o6.setForeground(Color.yellow) ;
 
            		l7 = new Label("T static", Label.CENTER) ;
            		o7 = new TextField() ;
            		o7.setBackground(Color.black) ;
            		o7.setForeground(Color.yellow) ;
 
            		l8 = new Label("Density", Label.CENTER) ;
            		o8 = new TextField() ;
            		o8.setBackground(Color.black) ;
            		o8.setForeground(Color.yellow) ;
 
            		l9 = new Label("P total", Label.CENTER) ;
            		o9 = new TextField() ;
            		o9.setBackground(Color.black) ;
            		o9.setForeground(Color.yellow) ;
 
            		l10 = new Label("T total", Label.CENTER) ;
            		o10 = new TextField() ;
            		o10.setBackground(Color.black) ;
            		o10.setForeground(Color.yellow) ;
 
            		l11 = new Label("Dynamic Press", Label.CENTER) ;
            		o11 = new TextField() ;
            		o11.setBackground(Color.black) ;
            		o11.setForeground(Color.yellow) ;
 
            		l12 = new Label("Dynamic Coef.", Label.CENTER) ;
            		o12 = new TextField() ;
            		o12.setBackground(Color.black) ;
            		o12.setForeground(Color.yellow) ;
 
            		l13 = new Label("Kinematic Coef.", Label.CENTER) ;
            		o13 = new TextField() ;
            		o13.setBackground(Color.black) ;
            		o13.setForeground(Color.yellow) ;
 
            		l14 = new Label("Reynolds #", Label.CENTER) ;
            		o14 = new TextField() ;
            		o14.setBackground(Color.blue) ;
            		o14.setForeground(Color.white) ;

			add(lb) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(new Label(" ", Label.RIGHT)) ;
			
			add(lsm);
            		add(new Label(" ", Label.RIGHT)) ;            		
			add(lc) ;
            		add(new Label(" ", Label.RIGHT)) ;
            		add(lv) ;
			add(new Label(" ", Label.RIGHT)) ;

            		add(l4) ;
            		add(o4) ;
            		add(l6) ;
            		add(o6) ;
            		add(l8) ;
            		add(o8) ;

            		add(l3) ;
            		add(o3) ;
            		add(l9) ;
            		add(o9) ;
            		add(l12) ;
            		add(o12) ;

            		add(l11) ;
            		add(o11) ;
            		add(l7) ;
            		add(o7) ;
            		add(l13) ;
            		add(o13) ;

            		add(l5) ;
            		add(o5) ;
            		add(l10) ;
            		add(o10) ;
            		add(l14) ;
            		add(o14) ;
        		}
    		}
  	}
}
