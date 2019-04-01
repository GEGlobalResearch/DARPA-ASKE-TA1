/*
                           ShockModeler
                Interactive Multiple Shock Wave Program
                          A Java Applet

     Program to perform two dimensional analysis of supersonic flow
        past wedges including oblique and normal shock conditions

                     Version 1.3a   - 18 Feb 11

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

      New Test : 
               * expand display for portal
               * expand range for hypersonics
               * re-organize graphics and inputs
                 evolve into new InletSim
                     re-organize modules
                                                     TJB 12 Apr 06

                 put in Prandtl-Meyer limits on centered expansion
                 increase number of significant figures in output

                                                     TJB 18 Feb 11
*/

import java.awt.*;
import java.lang.Math ;

public class Mshock extends java.applet.Applet {

   double gama, mach0, ang1, ang2, dist, machlo, machhi, xlong ;
   final double convdr = 3.14515926/180.;

   static double angr, delmax, gamma ;
   static double ang1mn, ang1mx, ang2mn, ang2mx, distmn, distmx ;
   int prob, nramps, nshocks, nslip ;
              //  flow parameters
   static double[] turning = new double[21] ;
   static double[] mach1   = new double[21] ;
   static double[] mach2   = new double[21] ;
   static double[] prat    = new double[21] ;
   static double[] trat    = new double[21] ;
   static double[] ptrat   = new double[21] ;
   static double[] rhorat  = new double[21] ;
   static double[] defl    = new double[21] ;
   static double[] shkang  = new double[21] ;
   static double[] pspo    = new double[21] ;
   static double[] tsto    = new double[21] ;
   static double[] ptpto   = new double[21] ;
   static double[] rsro    = new double[21] ;
   static boolean[] detach = new boolean[21] ;
              //  wedge geometry
   static double[] ang = new double[3] ;
   static int[] wfamily = new int[3] ;
   static double[] winter = new double[3] ;
   static double[] wxbgn = new double[3] ;
   static double[] wxnd = new double[3] ;
   static double[] wybgn = new double[3] ;
   static double[] wynd = new double[3] ;
   static double[] wslope = new double[3] ;
              // shock geometry
   static double[] sang = new double[21] ;
   static int[] sfamily = new int[21] ;
   static double[] sinter = new double[21] ;
   static double[] sxbgn = new double[21] ;
   static double[] sxnd = new double[21] ;
   static double[] sybgn = new double[21] ;
   static double[] synd = new double[21] ;
   static double[] sslope = new double[21] ;
              // expansion geometry
   static double[] expang = new double[21] ;
   static int[] efamily = new int[21] ;
   static double[] einter = new double[21] ;
   static double[] exbgn = new double[21] ;
   static double[] exnd = new double[21] ;
   static double[] eybgn = new double[21] ;
   static double[] eynd = new double[21] ;
   static double[] eslope = new double[21] ;
              // slip line geometry
   static double[] slinter = new double[10] ;
   static double[] slxbgn = new double[10] ;
   static double[] slxnd = new double[10] ;
   static double[] slybgn = new double[10] ;
   static double[] slynd = new double[10] ;
   static double[] slslope = new double[10] ;
              //  plot data 
   static double fact ;
   int xt,yt,sldloc ;

   Solver solve;
   Viewer view ;
   Num num ;
   Image offscreenImg ;
   Graphics offsGg ;

   public void init() {
     boolean which = false ;
     solve = new Solver() ;
 
     offscreenImg = createImage(this.size().width,
                      this.size().height) ;
     offsGg = offscreenImg.getGraphics() ;
 
     setLayout(new GridLayout(2,1,5,5)) ;

     solve.setDefaults () ;

     view = new Viewer(this) ;
     num = new Num(this) ;

     add(view) ;
     add(num) ;
 
     solve.comPute() ;
     view.start() ;
  }
 
  public float filter3(double inumbr) {
     //  output only to .001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 1000.) ;
       number = (float) (intermed / 1000. );
       return number ;
  }

  public float filter5(double inumbr) {
     //  output only to .00001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 100000.) ;
       number = (float) (intermed / 100000. );
       return number ;
  }


  public void getGeomOblq (double xi, double yi, int fam, int index) {
          // oblique shock geometry
       sfamily[index] = fam ;
       sslope[index]  = fam*Math.tan(convdr * sang[index]) ;
       sxbgn[index]   = xi ;
       sybgn[index]   = yi ;
       sinter[index]  = yi - sslope[index] * xi ;
       sxnd[index]    = xlong ;
       synd[index]    = sxnd[index]*sslope[index] + sinter[index] ;
       return;
  }

  public void getGeomExpan(double xi, double yi, int fam, int prev, int index) {
          // expansion fan geometry
       efamily[index] = fam ;
       eslope[index]  = fam*Math.tan(convdr *(expang[index]
                          -Math.abs(turning[prev]))) ;
       exbgn[index]   = xi ;
       eybgn[index]   = yi ;
       einter[index]  = yi - eslope[index] * xi ;
       exnd[index]    = xlong ;
       eynd[index]    = exnd[index]*eslope[index] + einter[index] ;
       return;
  }

  public void getGeomNorm (double xi, double yi, double ye, 
                             int fam, int index) {
          // normal shock geometry
       sfamily[index] = fam ;
       sslope[index]  = 0.0 ;
       sxbgn[index]   = xi ;
       sybgn[index]   = yi ;
       sinter[index]  = yi ;
       sxnd[index]    = xi ;
       synd[index]    = ye ;
       return;
  }
 
  public void getGeomSlip (double xi, double yi, double angle,
                             int index) {
          // slip line geometry
       slxbgn[index]   = xi ;
       slybgn[index]   = yi ;
       slslope[index]  = Math.tan(convdr*angle) ;
       slinter[index]  = yi  - slslope[index] * xi ;;
       slxnd[index]    = xlong ;
       slynd[index]    = slslope[index] * slxnd[index] + slinter[index] ;
       return;
  }

  public void getGeomRamp () {
          // wedge geometry
       int i ;
 
       xlong = 100. ;
       wfamily[0] = 1 ;
       wslope[0]  = 0.0 ;
       wxbgn[0]   = -10. ;
       wybgn[0]   = 1.0 ;
       winter[0]  = 0.0 ;
       if (ang[1] >= 0.0) {
          wxnd[0]    = xlong ;
          wynd[0]    = 1.0 ;
       }
       else {
          wxnd[0]    = 2.0 ;
          wynd[0]    = 1.0 ;
       }

       if (prob == 2) {
    // limits for crossing ramps
         xlong = dist / (Math.tan(convdr * ang[1])+ Math.tan(convdr * ang[2])) ;
         for (i=1; i<=2; ++i) {
            wfamily[i] = 3 - 2*i ;
            wslope[i]  = wfamily[i] * Math.tan(convdr * ang[i]) ;
            wxbgn[i]   = 2. ;
            wybgn[i]   = 1.0 + (i-1) * dist ;
            winter[i]  = wybgn[i] - wslope[i] * wxbgn[i] ;
            wxnd[i]    = xlong ;                                 
            wynd[i]    = wxnd[i] * wslope[i] + winter[i] ;
         }
       }
       else{
         wfamily[1] = 1 ;
         wslope[1]  = Math.tan(convdr * ang[1]) ;
         wxbgn[1]   = 2.0 ;
         wybgn[1]   = 1.0 ;
         winter[1]  = wybgn[1] - wslope[1] * wxbgn[1] ;
         wxnd[1]    = xlong ;
         wynd[1]    = wxnd[1] * wslope[1] + winter[1];

         wfamily[2] = 1 ;
         wslope[2]  = Math.tan(convdr * (ang[1] + ang[2])) ;
         wxbgn[2]   = wxbgn[1] + dist ;
         wybgn[2]   = wxbgn[2] * wslope[1] + winter[1];
         winter[2]  = wybgn[2] - wslope[2] * wxbgn[2] ;
         wxnd[2]    = xlong ;
         wynd[2]    = wxnd[2] * wslope[2] + winter[2] ;
       }
 
       return;
  }

  public void getAnglim (double machin, double gam) {
       double a1, ab1, ac1, sints, msq, mfor, thmx, cotd ;

       msq = machin * machin ;
       mfor = msq * msq ;

       a1 = 2.0 * gam * mfor ;
       ab1 = 4.0 * msq - (gam + 1.0) * mfor ;
       ac1 = -(2.0 + (gam + 1.0) * msq) ;
       sints = (-ab1 + Math.sqrt(Math.pow(ab1,2.0)-4.0*a1*ac1))/(2.0*a1) ;
       thmx = Math.asin(Math.sqrt(sints)) ;

       cotd = Math.tan(thmx)*(((gam+1.0)*msq)/    
                (2.0*(msq * sints - 1.0))-1.0);
       delmax = (Math.atan(1.0/cotd))/convdr ;
 
       return;
  }

  public void getShkang (double machin, double gam, double delr, int index) {
        // Iterate to get Shock Angle given Wedge Angle.
      double cotd,mst,gp1,number ;    
      double theto,thetn,delo,deln,deriv ;

      gp1 = gam + 1.0 ;
      theto = Math.asin(1.0/machin) ; 
      delo = 0.0 ;
      thetn = theto + 3.0 * convdr ;
      while (Math.abs(delr - delo) > .0001) {
         mst = machin * Math.sin(thetn) ;
         cotd = Math.tan(thetn)*((gp1*machin*machin)/    
                (2.0*(mst * mst - 1.0))-1.0);
         deln = Math.atan(1.0/cotd) ;
         deriv = (deln-delo)/(thetn-theto) ;
         delo = deln ;
         theto = thetn ;
         thetn = theto + (delr-delo)/deriv ;
      }

      number = theto / convdr ;
      if (machin < 1.00001) number = 90.0 ;
      shkang[index] = number ;
      return ;
  }

  public void getOblq (double machin, double gam,
                        int upstrm, int index) {
          // NACA 1135 - oblique shock relations.
       double mst, gm1, gp1, msq, m2sq, cotd ;

       mst = machin * Math.sin(shkang[index]*convdr) ;
       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       prat[index] = (2.0*gam*mst*mst - gm1)/gp1 ;
       rhorat[index] = (gp1*mst*mst)/(gm1*mst*mst + 2.0) ;
       trat[index] = (2.0*gam*mst*mst - gm1) * (gm1*mst*mst + 2.0)
                  /(mst*mst*Math.pow(gp1,2.0)) ; 
       ptrat[index] = (Math.pow(((gp1*mst*mst)/(gm1*mst*mst+2.0)),(gam/gm1)))
               * Math.pow((gp1/(2.0*gam*mst*mst - gm1)),(1.0/gm1)) ;
       m2sq = ((msq * mst * mst * Math.pow(gp1,2.0)) +
              (-4.0 * (mst*mst  - 1.0) * (gam*mst*mst + 1.0))) /
              ((2.0*gam*mst*mst - gm1) * (gm1*mst*mst + 2.0)) ;
       mach2[index] = Math.sqrt(m2sq) ;
       cotd = Math.tan(shkang[index]*convdr)*((gp1*msq)/    
                (2.0*(mst * mst - 1.0))-1.0);
       defl[index] = (Math.atan(1.0/cotd))/convdr ;
       mach1[index] = machin ;

       pspo[index] = pspo[upstrm]*prat[index] ;
       tsto[index] = tsto[upstrm]*trat[index] ;
       rsro[index] = rsro[upstrm]*rhorat[index] ;
       ptpto[index] = ptpto[upstrm]*ptrat[index] ;
  
       return;
  }

  public void getNorm (double machin, double gam, 
                         int upstrm, int index) {
          // NACA 1135 - normal shock relations.
       double gm1, gp1, msq, m2sq ;

       if (machin < 1.0) return ;

       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       prat[index] = (2.0*gam*msq - gm1)/gp1 ;
       rhorat[index] = (gp1*msq)/(gm1*msq + 2.0) ;
       trat[index] = prat[index] / rhorat[index] ;
       ptrat[index] = (Math.pow(rhorat[index],(gam/gm1)))
               * (Math.pow((1.0/prat[index]),(1.0/gm1))) ;
       m2sq = msq / (prat[index] * rhorat[index]) ;
       mach2[index] = Math.sqrt(m2sq) ;
       defl[index] = 0.0 ;
       mach1[index] = machin ;
       shkang[index] = 90.0 ;
       sang[index] = 90.0 ;
       detach[index] = true ;

       pspo[index] = pspo[upstrm]*prat[index] ;
       tsto[index] = tsto[upstrm]*trat[index] ;
       rsro[index] = rsro[upstrm]*rhorat[index] ;
       ptpto[index] = ptpto[upstrm]*ptrat[index] ;
 
       return;
  }

  public void getExpan (double machin, double gam, double delr,
                        int upstrm, int index) {
          // Centered Prandtl-Meyer Expansion
       double msq, msm1, gm1, gp1, fac1, fac2 ;
       double numax, nuo, nur, nun, m2sm1o, m2sm1n, deriv ;

       msq  = machin * machin ;
       msm1 = msq - 1.0;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;
       numax = 1.5707*(Math.sqrt(gp1/gm1) - 1.0) ;

//  limit calculations to M= 50
       numax = 124. * convdr ;

       nuo = Math.sqrt(gp1/gm1) * Math.atan( Math.sqrt(gm1*msm1/gp1)) 
             - Math.atan(Math.sqrt(msm1)) ;
       shkang[index] =  Math.asin(1.0/machin)/convdr  ;
       if (machin < 1.00001) shkang[index] = 90.0 ;

       nur = nuo - delr;
       if (nur < numax ) {

          m2sm1o = msm1;      // iterate for downstream mach 
          m2sm1n = msm1+.1 ;
          while (Math.abs(nur - nuo) > .0001) {
            nun = Math.sqrt(gp1/gm1) * Math.atan(Math.sqrt(gm1*m2sm1n/gp1)) 
                 - Math.atan(Math.sqrt(m2sm1n));
            deriv = (nun-nuo)/(m2sm1n-m2sm1o) ;
            nuo = nun ;
            m2sm1o = m2sm1n ;
            m2sm1n = m2sm1o + (nur-nuo)/deriv ;
          }

          mach2[index] = Math.sqrt(m2sm1o + 1.0) ;
          expang[index] = (Math.asin(1.0/mach2[index]) +delr)/convdr ;

          fac1 = 1.0 + .5*gm1*msq;
          fac2 = 1.0 + .5*gm1*(m2sm1o+1.0);
       }
       else {
          mach2[index] = 50.0 ;
          expang[index] = (Math.asin(1.0/mach2[index]) +delr)/convdr ;
          fac1 = 1.0 + .5*gm1*msq;
          fac2 = 1.0 + .5*gm1*(mach2[index]*mach2[index]);
       }

       prat[index] = Math.pow((fac1/fac2),(gam/gm1));
       rhorat[index] = Math.pow((fac1/fac2),(1.0/gm1));
       trat[index] = fac1/fac2 ;
       ptrat[index] = 1.0;                   /* Isentropic */
       defl[index] = delr/convdr ;
       mach1[index] = machin ;

       pspo[index] = pspo[upstrm]*prat[index] ;
       tsto[index] = tsto[upstrm]*trat[index] ;
       rsro[index] = rsro[upstrm]*rhorat[index] ;
       ptpto[index] = ptpto[upstrm]*ptrat[index] ;
  
       return;
  }

  public void getIntersect (int i, int j, int new1, int new2) {
       int k;
       double xint,yint;
       double delad,dela,delb,turno,turnn,delpo,delpn,deriv ;
                                  /* check for shock crossings */
       xint = (sinter[i] - sinter[j])/
           (sslope[j] - sslope[i]);
       if((xint > sxbgn[i] + .01) &&
          (xint < sxnd[i] -.01)) {
             yint = xint * sslope[i] + sinter[i];
             sxnd[i] = xint;
             synd[i] = yint;
             sxnd[j] = xint;
             synd[j] = yint;

             turno = Math.atan(wslope[j])/convdr ;
             delad = Math.abs(turning[i-1]-turno);
             getAnglim(mach1[i],gama);
             delmax = delmax - .001;
             if (delad > delmax)  {           /* detached - normal shock */
               getNorm (mach1[i],gama,i-1,new1) ;
               getGeomNorm (xint,yint,xlong,sfamily[i],new1) ;
               nshocks++ ;
               nslip = 0 ;
               return;
             }

             dela = convdr*delad;
             getShkang(mach1[i],gama,dela,new1) ;
             getOblq(mach1[i],gama,i-1,new1) ;
             nshocks++ ;
             delpo = (pspo[new1] - pspo[j])/pspo[j] ;
             delad = 0.0 ;
   
             for (k=1; k<=10; ++k) {                /* iterate for def */
                delad = delad + delpo / 2.0  * 
                    Math.sqrt(Math.pow(mach2[j],2.0)-1.0)/
                    (gama * Math.pow(mach2[j],2.0)) ;
                if (delad > 0.0) {
                   getShkang(mach2[j],gama,delad,new2) ;
                   getOblq (mach2[j],gama,j,new2) ;
                }
                else {
                   getExpan (mach2[j],gama,delad, j, new2) ;
                }
                delb = dela - delad;
                getShkang(mach1[i],gama,delb,new1) ;
                getOblq (mach1[i],gama,i-1,new1) ;
                delpo = (pspo[new1] - pspo[new2]);
                if (Math.abs(delpo) < .001) break;
                delpo = delpo/pspo[j] ;
             }
   
             nshocks++ ;
             getGeomSlip(xint, yint, turno, nslip) ;

             turning[new1] = turno - delad/convdr;
             sang[new1] = shkang[new1] ;
             getGeomOblq (xint,yint,sfamily[i],new1) ;
   
             sfamily[new2] = -sfamily[j];
             turning[new2] = turno - delad/convdr;
             sang[new2] = shkang[new2] - Math.abs(turning[j]);
             getGeomOblq (xint,yint,-sfamily[j],new2) ;
             sxnd[new2] = (sinter[new2] - winter[2])/
                            (wslope[2] - sslope[new2]);
             synd[new2] = sxnd[new2] * sslope[new2]
                                 + sinter[new2];
   
             if(prat[new2] < 1.0) {          /* expansion */
               getGeomExpan (xint,yint,-sfamily[j],j,new2) ;
               exnd[new2] = (einter[new2] - winter[2])/
                               (wslope[2] - eslope[new2]);
               eynd[new2] = exnd[new2] * eslope[new2] +
                                einter[new2] ;
             }
       }
    return;
  }
 
  public void getCrossShock (int i, int j, int new1, int new2) {

       int counter ;
       double xint,yint,y1;
       double delad,dela,turno,turnn,delpo,delpn,deriv ;
                                  /* check for shock crossings */
       xint = (sinter[i] - sinter[j])/
           (sslope[j] - sslope[i]);
       if((xint > sxbgn[i] + .01) &&
          (xint < sxnd[i] -.01)) {
             yint = xint * sslope[i] + sinter[i];
             sxnd[i] = xint;
             synd[i] = yint;
             sxnd[j] = xint;
             synd[j] = yint;
             sxbgn[new1] = xint ;
             sxbgn[new2] = xint ;
             if(nslip>1) {
               slxnd[nslip-1] = xint;
               slynd[nslip-1] = xint*slslope[nslip-1]+slinter[nslip-1];
             }
             turno = Math.atan(wslope[1])/convdr + Math.atan(wslope[2])/convdr ;
                                          /* initial guess for branch 1 */
             delad = Math.abs(turning[i]-turno);
             getAnglim(mach2[i],gama);
             delmax = delmax - .001;
             if (delad > delmax)  {           /* detached - normal shock */
               getNorm (mach2[i],gama,i,new1) ;
               yint = xint*wslope[1] + winter[1];
               y1   = xint*wslope[2] + winter[2];
               getGeomNorm (xint,yint,y1,-sfamily[i],new1) ;
               return;
             }

             dela = convdr*delad;
             getShkang(mach2[i],gama,dela,new1) ;
             getOblq(mach2[i],gama,i,new1) ;
                                          /* initial guess for branch 2 */
             delad = Math.abs(turning[j]-turno);
             getAnglim(mach2[j],gama) ;
             if (delad > delmax )  {
               getNorm (mach2[j],gama,j,new2) ;
               yint = xint*wslope[1] + winter[1];
               y1   = xint*wslope[2] + winter[2];
               getGeomNorm (xint,yint,y1,-sfamily[j],new2) ;
               return;
             }
             dela = convdr*delad;
             getShkang(mach2[j],gama,dela,new2) ;
             getOblq (mach2[j],gama,j,new2) ;
             delpo = pspo[new1] - pspo[new2];
                                      /* iterate for deflection */
             turnn = turno + .005 ;
             counter = 1;
             while (Math.abs(delpo) > .0001) {
                counter++ ;
                delad = Math.abs(turning[i]-turnn);
                dela = convdr*delad;
                getShkang (mach2[i],gama,dela,new1) ;
                getOblq (mach2[i],gama,i,new1) ;
                delad = Math.abs(turning[j]-turnn);
                dela = convdr*delad;
                getShkang (mach2[j],gama,dela,new2) ;
                getOblq (mach2[j],gama,j,new2) ;
                delpn = pspo[new1] - pspo[new2];
                deriv = (delpn-delpo)/(turnn-turno) ;
                delpo = delpn ;
                turno = turnn ;
                turnn = turno - delpo /deriv ;
                if (counter > 25) break;
              }

              getGeomSlip(xint, yint, turno, nslip) ;
   
              turning[new1] = turno;
              sang[new1] = shkang[new1] - Math.abs(turning[i]);
              getGeomOblq (xint,yint,-sfamily[i],new1) ;

              turning[new2] = turno;
              sang[new2] = shkang[new2] - Math.abs(turning[j]);
              getGeomOblq (xint,yint,-sfamily[j],new2) ;
       }
    return;
  }

  public void getHitWall (int j, int i, int new1) {

       double xint,yint,y1;
       double delad,dela,turno ;
                             
       xint = (sinter[i] - winter[j])/
              (wslope[j] - sslope[i]);
       if((xint > sxbgn[i] + .01) &&
          (xint < sxnd[i] -.01)) {
             sxnd[i] = xint;
             yint = xint * sslope[i] + sinter[i];
             synd[i] = yint;
             if(mach2[i] < 1.0) {
               detach[new1] = true;
               return;
             }

             getAnglim(mach2[i],gama);
             delmax = delmax - .01;

             turno = Math.atan(wslope[j])/convdr ;
             delad = Math.abs(turning[i]-turno);
             detach[new1] = false ;
             if (delad > delmax)  {  /* flow detached */
               getNorm (mach2[i],gama,i,new1) ;
               yint = xint*wslope[1] + winter[1];
               y1   = xint*wslope[2] + winter[2];
               getGeomNorm (xint,yint,y1,-sfamily[i],new1) ;
               return;
             }

             dela = convdr*delad;
             getShkang(mach2[i],gama,dela,new1) ;
             getOblq(mach2[i],gama,i,new1) ;
             turning[new1] = turno;
             sang[new1] = shkang[new1] - sfamily[i]*turning[i];
             getGeomOblq (xint,yint,-sfamily[i],new1) ;
       }
       return;
  }

  class Solver {

     Solver () {
     }

     public void setDefaults() {
       int i ;

       nslip = 0 ;
       nramps = 1 ;
       nshocks = 1 ;
       prob = 0 ;
       xlong = 100.0 ;
 
       mach0 = 2.0 ;
       machlo = 1.0 ;
       machhi = 10.0 ;
       ang1 = 10.0 ;
       ang2 = 10.0 ;
       gama = 1.4 ;
       ang1mn = -30.0 ;
       ang1mx = 30.0 ;
       ang2mn = 0.0 ;
       ang2mx = 30.0 ;
       dist = 15. ; 
       distmn = 0. ;
       distmx = 50. ;

       mach1[0] = 2.0 ;
       ang[0]  = 0.0 ;
       ang[1]  = 10.0 ;
       ang[2]  = 10.0 ;
       prat[0] = 1.0 ;
       trat[0] = 1.0 ;
       ptrat[0] = 1.0 ;
       rhorat[0] = 1.0 ;
       pspo[0] = 1.0 ;
       tsto[0] = 1.0 ;
       rsro[0] = 1.0 ;
       ptpto[0]= 1.0 ;
       defl[0] = 0.0 ;
       turning[0] = 0.0 ;
       shkang[0] = 0.0 ;
       for (i=1; i<=20; i++) {
         sxnd[i] = 10.;
         synd[i] = 1.0;
       }
 
       xt = 100; yt = 220; sldloc = 120;
       fact = 35.0 ;
 
     }

     public void loadZero() {
       int i ;

       nslip = 0 ; 
       for(i=1; i<=20; ++i) {
          pspo[i] = 0.0 ;
          ptpto[i] = 0.0 ;
          rsro[i] = 0.0 ;
          tsto[i] = 0.0 ;
          defl[i] = 0.0 ;
          turning[i] = 0.0 ;
          shkang[i] = 0.0 ;
          mach2[i] = 0.0 ;
          mach1[i] = 0.0 ;
          ptrat[i] = 1.0 ;
          prat[i] = 1.0 ;
          trat[i] = 1.0 ;
          rhorat[i] = 1.0 ;
          detach[i] = false ;
          getGeomOblq(0.0,-10.0,1,i) ;
          getGeomExpan(0.0,-10.0,1,i-1,i) ;
       }
       for(i=0; i<=6; i++) {
          getGeomSlip(0.0,-10.0,0.0,i);
       }
       return ;
     }

     public void comPute() {

       mach2[0] = mach1[0] = mach0;
       gamma  = gama ;
       ang[1] = ang1 ;
       ang[2] = ang2 ;

       getGeomRamp () ;
 
       loadZero() ;
 
       if(prob == 0) anlSing() ;
       if(prob == 1) anlSame() ;
       if(prob == 2) anlOpos() ;
 
       num.out.loadOut() ;
 
       view.repaint();
     }

     public void anlSing() {     //  Analysis for Single  Wedge.

       mach1[1] = mach2[0] ;
       nshocks = 1 ;
       nramps = 1 ;
       angr  = ang[1] * convdr ;
       detach[1] = false ;

       getAnglim(mach1[1],gamma) ;

       if (ang[1] > delmax) {
         getNorm(mach1[1],gamma,0,1) ;
         getGeomNorm(wxbgn[1],wybgn[1],25.,wfamily[1],1) ;
         turning[1] = ang[1] ;
       }
       else {
         if (angr > 0.0001) {         // shock wave
           getShkang(mach1[1],gamma,angr,1) ;
           getOblq(mach1[1],gamma,0,1) ;
           turning[1] = turning[0] + defl[1] ;
           sang[1] = turning[0] + shkang[1] ;
           getGeomOblq(wxbgn[1],wybgn[1],wfamily[1],1) ;
         }
         else {                       // expansion
           getExpan(mach1[1],gamma,angr,0,1) ;
           turning[1] = turning[0] + defl[1] ;
           sang[1] = turning[0] + shkang[1] ;
           if (mach1[1] > 1.001) getGeomOblq(wxbgn[1],wybgn[1],wfamily[1],1) ;
           else getGeomNorm(wxbgn[1],wybgn[1],25.,wfamily[1],1) ;
           getGeomExpan(wxbgn[1],wybgn[1],wfamily[1],0,1) ;
         }
       }
       return ;
     }

     public void anlSame() {     //  Analysis for Double  Wedge.

       int i ;

       nshocks = 0 ;
       nramps = 2 ;

       for (i=1; i<=nramps; i++) {  // ramp shocks
          mach1[i] = mach2[i-1] ;
          getAnglim(mach1[i],gamma) ;
          delmax = delmax - .01 ;
          detach[i] = false ;

          if (ang[i] > delmax) {   // shock detached
            getNorm(mach1[i],gamma,i-1,i) ;
            getGeomNorm(wxbgn[i],wybgn[i],25.,wfamily[i],i) ;
            nshocks++ ;
            if (i == 1) return ;
          }
          else {
            angr = ang[i] * convdr ;
            if (angr > 0.0001) {         // oblique shock wave
              nshocks++ ;
              getShkang(mach1[i],gamma,angr,i) ;
              getOblq(mach1[i],gamma,i-1,nshocks) ;
              turning[nshocks] = turning[nshocks-1] + defl[nshocks] ;
              sang[nshocks] = turning[nshocks-1] + shkang[nshocks] ;
              getGeomOblq(wxbgn[i],wybgn[i],wfamily[i],nshocks) ;
            }
          }
       }

       if(nshocks == 2) {   // intersecting shocks
           nslip = 1;                
           getIntersect(1,2,3,4) ;
       }

       return ;
     }

     public void anlOpos() {     //  Analysis for Opposed Wedges.

       int i,k,nwl ;

       mach1[1] = mach2[0] ;
       getAnglim(mach1[1],gamma) ;
       delmax = delmax - .01 ;
       nshocks = 0 ;
       nramps = 2 ;

       for (i=1; i<=nramps; i++) {  // ramp shocks
          detach[i] = false ;

          if (ang[i] > delmax) {   // shock detached
            getNorm(mach1[1],gama,0,1) ;
            getGeomNorm(wxbgn[1],wybgn[1],wybgn[2],wfamily[i],i) ;
            nshocks++ ;
            return ;
          }
          else {
            angr = ang[i] * convdr ;
            if (angr > 0.0001) {         // oblique shock wave
              nshocks++ ;
              getShkang(mach1[0],gama,angr,nshocks) ;
              getOblq(mach1[0],gama,0,nshocks) ;
              turning[nshocks] = wfamily[i] * defl[nshocks] ;
              sang[nshocks] = shkang[nshocks] ;
              getGeomOblq(wxbgn[i],(1.0+(i-1)*dist),wfamily[i],nshocks) ;
            }
          }
       }

       if(nshocks == 2) {      // crossed shocks
          for (i=1; i<=4; ++i) {
             k = (i-1)*4;
             nslip = i;
             getCrossShock (k+1,k+2,k+3,k+4) ;
             nshocks = k+3;
             if (detach[k+3] == true) return;
             nshocks = k+4;
             if (detach[k+4] == true && detach[k+3] != true) {
                nshocks = k+3;
                getNorm (mach1[k+4],gama,k+2,k+3) ;
                getGeomNorm(sxbgn[k+4],sybgn[k+4],synd[k+4],
                                1,k+3);
                return;
             }
                                             /* reflect from ramps */
             getHitWall (1,k+3,k+5);
             getHitWall (2,k+4,k+6);
             nshocks = k+5;
             if (detach[k+5] == true) return;
             nshocks = k+6;
             if (detach[k+6] == true && detach[k+5] != true) {
                nshocks = k+5;
                getNorm (mach1[k+6],gama,k+4,k+5) ;
                getGeomNorm(sxbgn[k+6],sybgn[k+6],synd[k+6],
                                1,k+5);
                return;
             }
             if (detach[k+6] == true) return;
         }
       }
       else {           // single shock
         for (i=1; i<=9; ++i) {
            if(i/2*2 == i) nwl = (3 - sfamily[1])/2;
                 else nwl = (sfamily[1] + 3)/2 ;
            getHitWall (nwl,i,i+1);                /* reflect from ramp */
            nshocks = i+1;
            if (detach[i+1] == true) return;
         }
       }

       return ;
     }

  }  // end Solver

   class Num extends Panel {
     Mshock outerparent ;
     In in ;
     Out out ;

     Num (Mshock target) {
          outerparent = target ;
          setLayout(new GridLayout(1,2,5,5)) ;

          in = new In(outerparent) ;
          out = new Out(outerparent) ;

          add(in) ;
          add(out) ;
     }

     class In extends Panel {
        Mshock outerparent ;
        Rt rt ;
        Lt lt ;

        In (Mshock target) {
                             
           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           lt = new Lt(outerparent) ; 
           rt = new Rt(outerparent) ;
    
           add(lt) ;
           add(rt) ;
        }
 
        class Rt extends Panel {
           Mshock outerparent ;
           Scrollbar s1, s3, s4, s5 ;

           Rt (Mshock target) {
               int i1, i3, i4, i5 ;
    
               outerparent = target ;
               setLayout(new GridLayout(7,1,5,7)) ;
   
               i1 = (int) ((((double)mach0 - machlo)/(machhi-machlo))*1000.) ;
               i3 = (int) ((((double)ang1 - ang1mn)/(ang1mx - ang1mn))*1000.) ;
               i4 = (int) ((((double)ang2 - ang2mn)/(ang2mx - ang2mn))*1000.) ;
               i5 = (int) ((((double)dist - distmn)/(distmx - distmn))*1000.) ;
    
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
               s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);
               s5 = new Scrollbar(Scrollbar.HORIZONTAL,i5,10,0,1000);
 
               add(new Label(" ", Label.CENTER)) ;
               add(s1) ;
               add(s3) ;
               add(s4) ;
               add(s5) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
           }
   
           public boolean handleEvent(Event evt) {
               if(evt.id == Event.SCROLL_ABSOLUTE) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_LINE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_DOWN) {
                  this.handleBar(evt) ;
                  return true ;
               }
               if(evt.id == Event.SCROLL_PAGE_UP) {
                  this.handleBar(evt) ;
                  return true ;
               }
               else return false ;
           }
   
           public void handleBar(Event evt) {
                int i1, i3, i4, i5 ;
                Double V2 ;
                double v1, v2, v3, v4, v5 ;
                float fl1, fl3, fl4, fl5 ;
           
                i1 = rt.s1.getValue() ;
                i3 = rt.s3.getValue() ;
                i4 = rt.s4.getValue() ;
                i5 = rt.s5.getValue() ;
           
                v1 = i1 * (machhi-machlo) / 1000. + machlo ;
                v3 = i3 * (ang1mx-ang1mn) / 1000. + ang1mn ;
                v4 = i4 * (ang2mx-ang2mn) / 1000. + ang2mn ;
                v5 = i5 * (distmx-distmn) / 1000. + distmn ;
    
                fl1 = (float) v1 ;
                fl3 = (float) v3 ;
                fl4 = (float) v4 ;
                fl5 = (float) v5 ;
           
                lt.f1.setText(String.valueOf(fl1)) ;
                lt.f3.setText(String.valueOf(fl3)) ;
                lt.f4.setText(String.valueOf(fl4)) ;
                lt.f5.setText(String.valueOf(fl5)) ;
    
                mach0 = v1 ;
                ang1 = v3 ;
                if (prob > 0) ang2 = v4 ;
                if (prob > 0) dist = v5 ;
           
                solve.comPute() ;    
           }
        }
   
        class Lt extends Panel {
           Mshock outerparent ;
           TextField f1, f2, f3, f4, f5;
   
           Lt (Mshock target) {
               
               outerparent = target ;
               setLayout(new GridLayout(7,2,2,7)) ;
     
               f1 = new TextField(String.valueOf(mach0),5) ;
               f2 = new TextField(String.valueOf(gama),5) ;
               f3 = new TextField(String.valueOf(ang1),5) ;
               f4 = new TextField(String.valueOf(ang2),5) ;
               f5 = new TextField(String.valueOf(dist),5) ;

               add(new Label("Input", Label.CENTER)) ;  
               add(new Label(" ", Label.CENTER)) ;
   
               add(new Label("Mach", Label.CENTER)) ;  
               add(f1) ;
   
               add(new Label(" Angle 1", Label.CENTER)) ;
               add(f3) ;
   
               add(new Label(" Angle 2", Label.CENTER)) ;
               add(f4) ;
   
               add(new Label(" Spacing", Label.CENTER)) ;
               add(f5) ;
   
               add(new Label("gamma", Label.CENTER)) ;  
               add(f2) ;

               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
           }
     
           public boolean action(Event evt, Object arg) {
               if(evt.target instanceof TextField) {
                  this.handleText(evt) ;
                  return true ;
               }
               else return false ;
           }
   
           public void handleText(Event evt) {
              Double V1,V2,V3,V4,V5;
              double v1,v2,v3,v4,v5;
              float fl1 ;
              int i1,i3,i4,i5 ;
    
          // Mach number - range from 1.0 to 10.0
              V1 = Double.valueOf(lt.f1.getText()) ;
              v1 = V1.doubleValue() ;
              if(v1 < machlo) {
                 v1 = machlo + .02 ;
                 fl1 = (float) v1 ;
                 lt.f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > machhi) {
                 v1 = machhi ;
                 fl1 = (float) v1 ;
                 lt.f1.setText(String.valueOf(fl1)) ;
              }
          // gamma - range from 1.0 to 2.0
              V2 = Double.valueOf(lt.f2.getText()) ;
              v2 = V2.doubleValue() ;
              if(v2 < 1.0) {
                 v2 = 1.0 ;
                 fl1 = (float) v2 ;
                 lt.f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > 2.0) {
                 v2 = 2.0 ;
                 fl1 = (float) v2 ;
                 lt.f2.setText(String.valueOf(fl1)) ;
              }
          // ramp angle # 1  range from -30 to +30 for prob = 0
              V3 = Double.valueOf(lt.f3.getText()) ;
              v3 = V3.doubleValue() ;
              if(v3 < ang1mn) {
                v3 = ang1mn ;
                fl1 = (float) v3 ;
                lt.f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > ang1mx) {
                 v3 =  ang1mx ;
                 fl1 = (float) v3 ;
                 lt.f3.setText(String.valueOf(fl1)) ;
              }
          // ramp angle # 2  range from 0 to +30 for prob = 1
              V4 = Double.valueOf(lt.f4.getText()) ;
              v4 = V4.doubleValue() ;
              if(v4 < ang2mn) {
                v4 = ang2mn ;
                fl1 = (float) v4 ;
                lt.f4.setText(String.valueOf(fl1)) ;
              }
              if(v4 > ang2mx) {
                 v4 =  ang2mx ;
                 fl1 = (float) v4 ;
                 lt.f4.setText(String.valueOf(fl1)) ;
              }
          // spacing  range from 0 to +25 for prob = 1
              V5 = Double.valueOf(lt.f5.getText()) ;
              v5 = V5.doubleValue() ;
              if(v5 < distmn) {
                v5 = distmn ;
                fl1 = (float) v5 ;
                lt.f5.setText(String.valueOf(fl1)) ;
              }
              if(v5 > distmx) {
                 v5 =  distmx ;
                 fl1 = (float) v5 ;
                 lt.f5.setText(String.valueOf(fl1)) ;
              }
   
              i1 = (int) (((v1 - machlo)/(machhi-machlo))*1000.) ;
              i3 = (int) (((v3 - ang1mn)/(ang1mx-ang1mn))*1000.) ;
              i4 = (int) (((v4 - ang2mn)/(ang2mx-ang2mn))*1000.) ;
              i5 = (int) (((v5 - distmn)/(distmx-distmn))*1000.) ;
   
              rt.s1.setValue(i1) ;
              rt.s3.setValue(i3) ;
              rt.s4.setValue(i4) ;
              rt.s5.setValue(i5) ;
    
              mach0 = v1 ;
              gama = v2 ;
              ang1 = v3 ;
              if (prob > 0) ang2 = v4 ;
              if (prob > 0) dist = v5 ;
   
              solve.comPute() ;
           }
        }     // end left
   
        public void loadInput() {   // load the input panel
          int i1,i2,i3,i4,i5;
          double v1,v2,v3,v4,v5;
          float fl1,fl2,fl3,fl4,fl5 ;
 
          v1 = mach0 ;
          v2 = gama ;
          v3 = ang1 ;
          v4 = ang2 ;
          v5 = dist ;
 
          fl1 = filter3(v1) ;
          fl2 = filter3(v2) ;
          fl3 = filter3(v3) ;
          fl4 = filter3(v4) ;
          fl5 = filter3(v5) ;
 
          lt.f1.setText(String.valueOf(fl1)) ;
          lt.f2.setText(String.valueOf(fl2)) ;
          lt.f3.setText(String.valueOf(fl3)) ;
          lt.f4.setText(String.valueOf(fl4)) ;
          lt.f5.setText(String.valueOf(fl5)) ;
   
          i1 = (int) (((v1 - machlo)/(machhi-machlo))*1000.) ;
          i3 = (int) (((v3 - ang1mn)/(ang1mx-ang1mn))*1000.) ;
          i4 = (int) (((v4 - ang2mn)/(ang2mx-ang2mn))*1000.) ;
          i5 = (int) (((v5 - distmn)/(distmx-distmn))*1000.) ;
   
          rt.s1.setValue(i1) ;
          rt.s3.setValue(i3) ;
          rt.s4.setValue(i4) ;
          rt.s5.setValue(i5) ;
        }
     }    // end In
   
     class Out extends Panel {
        Mshock outerparent ;
        TextField o1, o2, o3, o4, o5, o6 ;
        TextField o7, o8, o9, o10, o11, o12, o13 ;
        Label lo1,lo2,lo3 ;
        Choice zonch ;
   
        Out (Mshock target) {
   
           outerparent = target ;
           setLayout(new GridLayout(7,4,2,7)) ;
    
           o1 = new TextField() ;
           o1.setBackground(Color.black) ;
           o1.setForeground(Color.yellow) ;
           o2 = new TextField() ;
           o2.setBackground(Color.black) ;
           o2.setForeground(Color.yellow) ;
           o3 = new TextField() ;
           o3.setBackground(Color.black) ;
           o3.setForeground(Color.yellow) ;
           o4 = new TextField() ;
           o4.setBackground(Color.black) ;
           o4.setForeground(Color.yellow) ;
           o5 = new TextField() ;
           o5.setBackground(Color.black) ;
           o5.setForeground(Color.yellow) ;
           o6 = new TextField() ;
           o6.setBackground(Color.black) ;
           o6.setForeground(Color.yellow) ;
           o7 = new TextField() ;
           o7.setBackground(Color.black) ;
           o7.setForeground(Color.yellow) ;
           o8 = new TextField() ;
           o8.setBackground(Color.black) ;
           o8.setForeground(Color.yellow) ;
           o9 = new TextField() ;
           o9.setBackground(Color.black) ;
           o9.setForeground(Color.yellow) ;
           o10 = new TextField() ;
           o10.setBackground(Color.black) ;
           o10.setForeground(Color.yellow) ;
           o11 = new TextField() ;
           o11.setBackground(Color.black) ;
           o11.setForeground(Color.yellow) ;
           o12 = new TextField() ;
           o12.setBackground(Color.black) ;
           o12.setForeground(Color.yellow) ;
           o13 = new TextField() ;
           o13.setBackground(Color.black) ;
           o13.setForeground(Color.yellow) ;
   
           lo3 = new Label("Output ", Label.CENTER) ;
           lo3.setForeground(Color.red) ;
    
           zonch = new Choice() ;
           zonch.setBackground(Color.white) ;
           zonch.setForeground(Color.red) ;
           zonch.addItem("Zone 0") ;
           zonch.addItem("Zone 1");
           zonch.addItem("Zone 2");
           zonch.addItem("Zone 3");
           zonch.addItem("Zone 4");
           zonch.addItem("Zone 5");
           zonch.addItem("Zone 6");
           zonch.addItem("Zone 7");
           zonch.addItem("Zone 8");
           zonch.addItem("Zone 9");
           zonch.addItem("Zone 10");
           zonch.addItem("Zone 11");
           zonch.addItem("Zone 12");
           zonch.addItem("Zone 13");
           zonch.addItem("Zone 14");
           zonch.addItem("Zone 15");
           zonch.addItem("Zone 16");
           zonch.addItem("Zone 17");
           zonch.addItem("Zone 18");
           zonch.addItem("Zone 19");
           zonch.addItem("Zone 20");
           zonch.select(1) ;
   
           add(lo3) ;
           add(zonch) ;
           add(new Label("Mach-up", Label.CENTER)) ;  
           add(o13) ;  

           add(new Label("Mach", Label.CENTER)) ;  
           add(o1) ;  
           add(new Label("Shock", Label.CENTER)) ;  
           add(o3) ;  
   
           add(new Label("Angle", Label.CENTER)) ; 
           add(o2) ; 
           add(new Label("Turning", Label.CENTER)) ; 
           add(o4) ; 
   
           add(new Label("p/p(up)", Label.CENTER)) ;  
           add(o5) ;
           add(new Label("p/p0", Label.CENTER)) ;  
           add(o6) ;
   
           add(new Label("pt/pt(up)", Label.CENTER)) ;  
           add(o7) ; 
           add(new Label("pt/pt0", Label.CENTER)) ;  
           add(o8) ; 
   
           add(new Label("T/T(up)", Label.CENTER)) ; 
           add(o9) ;
           add(new Label("T/T0", Label.CENTER)) ; 
           add(o10) ;
   
           add(new Label("r/r(up)", Label.CENTER)) ;  
           add(o11) ; 
           add(new Label("r/r0", Label.CENTER)) ;  
           add(o12) ; 
       }
 
       public boolean action(Event evt, Object arg) {
           if(evt.target instanceof Choice) {
               solve.comPute() ;
               return true ;
           }
           else return false ;
       }
   
       public void loadOut() {
          int outzn ;
   
          outzn = zonch.getSelectedIndex() ;
   
          o13.setText(String.valueOf(filter3(mach1[outzn]))) ;
          o1.setText(String.valueOf(filter3(mach2[outzn]))) ;
          o2.setText(String.valueOf(filter3(defl[outzn]))) ;
          o3.setText(String.valueOf(filter3(shkang[outzn])));
          o4.setText(String.valueOf(filter3(turning[outzn])));
          o5.setText(String.valueOf(filter5(prat[outzn]))) ;
          o6.setText(String.valueOf(filter5(pspo[outzn]))) ;
          o7.setText(String.valueOf(filter5(ptrat[outzn]))) ;
          o8.setText(String.valueOf(filter5(ptpto[outzn]))) ;
          o9.setText(String.valueOf(filter5(trat[outzn]))) ;
          o10.setText(String.valueOf(filter5(tsto[outzn]))) ;
          o11.setText(String.valueOf(filter5(rhorat[outzn])));
          o12.setText(String.valueOf(filter5(rsro[outzn])));
       }
     } // end Out
  }  // end Num

  class Viewer extends Canvas 
         implements Runnable{
     Mshock outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Mshock target) {
         setBackground(Color.black) ;
         runner = null ;
     }
 
     public boolean mouseDown(Event evt, int x, int y) {
        anchor = new Point(x,y) ;
        return true;
     }
 
     public boolean mouseUp(Event evt, int x, int y) {
        handleb(x,y) ;
        return true;
     }

     public boolean mouseDrag(Event evt, int x, int y) {
        handle(x,y) ;
        return true;
     }

     public void handle(int x, int y) {
         // determine location
         if (x >= 31 && x <= 700) {
            if (y >= 21 && y <= 300) {
               locate = new Point(x,y) ;
               yt =  yt + (int) (.2*(locate.y - anchor.y)) ;
               xt =  xt + (int) (.4*(locate.x - anchor.x))  ;
            }
         }
         if (x <= 30 ) {  
            if (y >= 20 && y <= 220 ) {   // zoom widget
              sldloc = y ;
              if (sldloc < 20) sldloc = 20;
              if (sldloc > 220) sldloc = 220;
              fact = 5.0 + (220 - sldloc)*0.25 ;
            }
         }
     }
 
     public void handleb(int x, int y) {
         if (y <= 21 ) {  
            if (x >= 200 && x <= 275 ) {   // single prob button
              prob = 0;
              ang1mn = -30.0 ;
              ang1 = 10.0 ;
              dist = 25. ;
              num.in.loadInput () ;
              solve.comPute() ;
            }
            if (x >= 300 && x <= 375 ) {   // double prob button
              prob = 1;
              ang1mn = 0.0 ;
              ang1 = 10.0 ;
              dist = 5. ;
              num.in.loadInput () ;
              solve.comPute() ;
            }
            if (x >= 400 && x <= 475 ) {   // opposed prob button
              prob = 2;
              ang1mn = 0.0 ;
              ang1 = 10.0 ;
              dist = 10. ;
              num.in.loadInput () ;
              solve.comPute() ;
            }
            if (x >= 550 && x <= 600 ) {   // find button
              xt = 100; yt = 220; sldloc = 120;
              fact = 35.0 ;
            }
            if (x >= 650 && x <= 700 ) {   // reset button
              solve.setDefaults () ;
              prob = 0;
              ang1mn = -30.0 ;
              ang1 = 10.0 ;
              dist = 25. ;
              num.in.loadInput () ;
              num.out.zonch.select(1) ;
              solve.comPute() ;
            }
         }
         view.repaint() ;
     }

     public void start() {
        if (runner == null) {
           runner = new Thread(this) ;
           runner.start() ;
        }
     }

     public void run() {
       int timer ;

       timer = 100 ;
       while (true) {
          try { Thread.sleep(timer); }
          catch (InterruptedException e) {}
          view.repaint() ;
       }
     }

     public void update(Graphics g) {
         view.paint(g) ;
     }

     public void paint(Graphics g) {
       int i,k,nsplot ;
       int exes[] = new int[5] ;
       int whys[] = new int[5] ;
       double ylong = 350. ;

       offsGg.setColor(Color.white) ;
       offsGg.fillRect(0,0,700,250) ;
          // draw shock waves or expansion
       nsplot = nshocks ;
       for(i=1; i <= nshocks; ++i) {
         if (mach2[i] > .0001) nsplot = i ;
         else break ;
       }

       for(i=1; i <= nsplot; ++i) {
         exes[0] = (int) (.5*fact*sxbgn[i]) + xt ;
         whys[0] = (int) (.5*fact*-sybgn[i]) + yt ;
         exes[1] = (int) (.5*fact* sxnd[i]) + xt ;
         whys[1] = (int) (.5*fact*-synd[i]) + yt ;
         if (detach[i]) {
           offsGg.setColor(Color.magenta) ; 
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           exes[3] = (exes[0] + exes[1])/2 ;
           whys[3] = (whys[0] + whys[1])/2 ;
           offsGg.drawString(String.valueOf(i), exes[3]+5, whys[3]) ;
         }
         else {
           if (prat[i] > 1.0) {
              offsGg.setColor(Color.blue) ; 
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;  
  // label
              exes[3] = (exes[0] + exes[1])/2 ;
              whys[3] = (whys[0] + whys[1])/2 ;
              offsGg.drawString(String.valueOf(i), exes[3]+5, whys[3]) ;
           }
           else {
              exes[2] = (int) (.5*fact* exnd[i]) + xt ;
              whys[2] = (int) (.5*fact*-eynd[i]) + yt ;
              offsGg.setColor(Color.black) ; 
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;  
              offsGg.drawLine(exes[0],whys[0],exes[2],whys[2]) ;  
  // label
              exes[3] = (exes[0] + exes[2])/2 ;
              whys[3] = (whys[0] + whys[2])/2 ;
              offsGg.drawString(String.valueOf(i), exes[3], whys[3]) ;
            }
          }
        }

        if (nslip > 0) {
          for (i=1; i<=nslip; ++i) {
             exes[0] = (int) (.5*fact*slxbgn[i]) + xt ;
             whys[0] = (int) (.5*fact*-slybgn[i]) + yt ;
             exes[1] = (int) (.5*fact* slxnd[i]) + xt ;
             whys[1] = (int) (.5*fact*-slynd[i]) + yt ;
             offsGg.setColor(Color.black) ; 
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;  
          }
        }

          // draw ramps
       if (prob < 2) {   // single and double wedge
         for (i = nramps; i >= 0; --i) {
           exes[0] = (int) (.5*fact*wxbgn[i]) + xt ;
           whys[0] = (int) (.5*fact*(-wybgn[i])) + yt ;
           exes[1] = (int) (.5*fact*wxnd[i]) + xt ;
           whys[1] = (int) (.5*fact*(-wynd[i])) + yt ;
           exes[2] = exes[1] ;
           whys[2] = (int) (.5*fact*ylong) + yt ;
           exes[3] = exes[0] ;
           whys[3] = whys[2] ;
           if (i == 0 && ang[1] >= 0.0) offsGg.setColor(Color.white) ;
           if (i == 0 && ang[1] < 0.0) offsGg.setColor(Color.red) ;
           if (i == 1) offsGg.setColor(Color.red) ;
           if (i == 2) offsGg.setColor(Color.green) ;
           offsGg.fillPolygon(exes,whys,4) ;
     // label
           exes[4] = exes[0] + 50;
           whys[4] = whys[0] - 10;
           offsGg.setColor(Color.black) ;
           offsGg.drawString(String.valueOf(i), exes[4], whys[4]) ;
         }
       }
       else {            // opposed wedges
         for (i = 1; i >= 0; --i) {
           exes[0] = (int) (.5*fact*wxbgn[i]) + xt ;
           whys[0] = (int) (.5*fact*(-wybgn[i])) + yt ;
           exes[1] = (int) (.5*fact*wxnd[i]) + xt ;
           whys[1] = (int) (.5*fact*(-wynd[i])) + yt ;
           exes[2] = exes[1] ;
           whys[2] = (int) (.5*fact*ylong) + yt ;
           exes[3] = exes[0] ;
           whys[3] = whys[0] ;
           if (i == 0) offsGg.setColor(Color.white) ;
           if (i == 1) offsGg.setColor(Color.red) ;
           offsGg.fillPolygon(exes,whys,4) ;
     // label
           exes[4] = exes[0] + 50 ;
           whys[4] = whys[0] - 10 ;
           offsGg.setColor(Color.black) ;
           offsGg.drawString(String.valueOf(i), exes[4], whys[4]) ;
         }
         exes[0] = (int) (.5*fact*wxbgn[2]) + xt ;
         whys[0] = (int) (.5*fact*(-wybgn[2])) + yt ;
         exes[1] = (int) (.5*fact*wxnd[2]) + xt ;
         whys[1] = (int) (.5*fact*(-wynd[2])) + yt ;
         exes[2] = exes[1] ;
         whys[2] = (int) (.5*fact*(-ylong - dist)) + yt ;
         exes[3] = exes[0] ;
         whys[3] = whys[0] ;
         offsGg.setColor(Color.green) ;
         offsGg.fillPolygon(exes,whys,4) ;
     // label
         exes[4] = exes[0] + 50 ;
         whys[4] = whys[0] + 10 ;
         offsGg.setColor(Color.black) ;
         offsGg.drawString(String.valueOf(2), exes[4], whys[4]) ;

         exes[0] = (int) (.5*fact*wxbgn[0]) + xt ;
         whys[0] = (int) (.5*fact*(-wybgn[2])) + yt ;
         exes[1] = (int) (.5*fact*wxnd[0]) + xt ;
         whys[1] = (int) (.5*fact*(-wybgn[2])) + yt ;
         exes[2] = exes[1] ;
         whys[2] = (int) (.5*fact*(-ylong - dist)) + yt ;
         exes[3] = exes[0] ;
         whys[3] = whys[2] ;
         offsGg.setColor(Color.white) ;
         offsGg.fillPolygon(exes,whys,4) ;
    // reflecting surface
         if (ang[2] <= .001) {
           exes[0] = (int) (.5*fact*wxbgn[2]) + xt ;
           whys[0] = (int) (.5*fact*(-wybgn[2])) + yt ;
           exes[1] = (int) (.5*fact*wxnd[2]) + xt ;
           whys[1] = (int) (.5*fact*(-wynd[2])) + yt ;
           exes[2] = exes[1] ;
           whys[2] = (int) (.5*fact*(-wynd[2] - 2.0)) + yt ;
           exes[3] = exes[0] ;
           whys[3] = whys[0] ;
           offsGg.setColor(Color.green) ;
           offsGg.fillPolygon(exes,whys,4) ;
         }

       }
 // free stream label
       offsGg.setColor(Color.blue) ;
       offsGg.drawString("Upstream", 50, 50) ;
       offsGg.drawString("Flow", 55, 65) ;
       offsGg.setColor(Color.black) ;
       offsGg.fillRect(50,72,30,9) ;
       exes[0] = 90 ;
       whys[0] = 76;
       exes[1] = 80;
       whys[1] = 86;
       exes[2] = 80;
       whys[2] = 66;
       Polygon poly1 = new Polygon(exes,whys,3) ;
       offsGg.fillPolygon(poly1) ;

 // zoom widget
        offsGg.setColor(Color.white) ;
        offsGg.fillRect(0,0,30,250) ;
        offsGg.setColor(Color.black) ;
        offsGg.drawString("Zoom",2,240) ;
        offsGg.drawLine(15,20,15,220) ;
        offsGg.fillRect(5,sldloc,20,5) ;

 // buttons
        offsGg.setColor(Color.blue) ;
        offsGg.fillRect(0,0,700,17) ;
        offsGg.setColor(Color.white) ;
        offsGg.drawString("ShockModeler 1.3 ",10,13) ;

        offsGg.setColor(Color.white) ;
        if (prob <= 0) offsGg.setColor(Color.yellow) ;  
        offsGg.fillRect(200,2,75,13) ;
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Single",205,14) ;

        offsGg.setColor(Color.white) ;
        if (prob > 0 && prob <= 1) offsGg.setColor(Color.yellow) ;  
        offsGg.fillRect(300,2,75,13) ;
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Double",305,14) ;

        offsGg.setColor(Color.white) ;
        if (prob > 1 && prob <= 2) offsGg.setColor(Color.yellow) ;  
        offsGg.fillRect(400,2,75,13) ;
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Opposed",405,14) ;

        offsGg.setColor(Color.white) ;
        offsGg.fillRect(545,2,50,13) ;
        offsGg.setColor(Color.red) ;
        offsGg.drawString("Find",550,14) ;

        offsGg.setColor(Color.white) ;
        offsGg.fillRect(645,2,50,13) ;
        offsGg.setColor(Color.red) ;
        offsGg.drawString("Reset",650,14) ;

       g.drawImage(offscreenImg,0,0,this) ;
    }
  }  // end viewer
}
