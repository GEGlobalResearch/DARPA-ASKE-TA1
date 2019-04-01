/*
                Interactive Shock Wave Program

     Program to perform two dimensional analysis of supersonic flow
        past a wedge including oblique and normal shock conditions
        and flow past a cone - Taylor-Maccoll flow

                     Version 1.3e   - 31 Aug 11

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
                 * change output box colors
                 * add planets
                 * enlarge display for the portal
                 * expand range for input variables for hypersonic site
                       M=4
                 * include real gas effects - new program shockh - not this program
                   include shock standoff (Moeckle method?)
                 * add streamline calculation 
                 *   put in streamline for cone flow
                 * add cone flow - Taylor-Maccoll
                 *   use integer prob = 0  wedge
                 *               prob = 1  cone
                 *   put in limiter for detached shock
                 *   put in 10 rays of output - new output panel for cone
                   
                                                     TJB 31 Aug 11
*/

import java.awt.*;
import java.lang.Math ;

public class Shock extends java.applet.Applet {

   final double convdr = 3.14515926/180.;
   double gama, mach0, ang1, ang2, sepval ;
   double angmn, angmx, machlo, machhi ;
   double xlong ;
   double vpold,vpnew,vpoldr,vpnewr,vpoldt,vpnewt,thetold,thetnew,machnew ;

   static double angr, delmax, dist, gamma, thmx ;
   int prob, nramps, nshocks, planet, numray, numd ;
   int outzn,drawray ;
              //  flow parameters
   static double[] turning = new double[30] ;
   static double[] mach1   = new double[30] ;
   static double[] mach2   = new double[30] ;
   static double[] prat    = new double[30] ;
   static double[] trat    = new double[30] ;
   static double[] ptrat   = new double[30] ;
   static double[] rhorat  = new double[30] ;
   static double[] defl    = new double[30] ;
   static double[] shkang  = new double[30] ;
   static double[] pspo    = new double[30] ;
   static double[] tsto    = new double[30] ;
   static double[] ptpto   = new double[30] ;
   static double[] rsro    = new double[30] ;
   static boolean[] detach = new boolean[30] ;
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
   static double[] sang = new double[15] ;
   static int[] sfamily = new int[15] ;
   static double[] sinter = new double[15] ;
   static double[] sxbgn = new double[15] ;
   static double[] sxnd = new double[15] ;
   static double[] sybgn = new double[15] ;
   static double[] synd = new double[15] ;
   static double[] sslope = new double[15] ;
               // conical ray flowfield
   static double[] vp = new double[200] ;
   static double[] vpr = new double[200] ;
   static double[] vpt = new double[200] ;
   static double[] rmach = new double[200] ;
   static double[] rthet = new double[200] ;
   static double[] rpt = new double[20] ;
   static double[] rdel = new double[20] ;
   static double[] rps = new double[20] ;
   static double[] rtemp = new double[20] ;
   static double[] rrho = new double[20] ;
                // isentropic relations
   double poverpt,tovertt,roverrt,arat,mu,nu ;
              // streamlines 
   static double[][] strx = new double[26][10] ;
   static double[][] stry = new double[26][10] ;

   CardLayout layout ;

   Viewer view ;
   Num num ;
   Image offscreenImg ;
   Graphics offsGg ;

   public void init() {
     boolean which = false ;
 
     offscreenImg = createImage(this.size().width,
                      this.size().height) ;
     offsGg = offscreenImg.getGraphics() ;
 
     setLayout(new GridLayout(1,2,0,0)) ;

     setDefaults () ;

     view = new Viewer(this) ;

     num = new Num(this) ;

     add(view) ;
     add(num) ;

     comPute( ) ;
  }
 
   public void setDefaults() {

     numd = 1;
     drawray = 0;
     prob = 0 ;  // wedge flow
     mach0 = 2.0 ;
     ang1 = 10.0 ;
     gamma  = 1.4 ;

     nramps = 1 ;
     nshocks = 1 ;
     xlong = 350. ;

     planet = 0 ;
     angmn = 4.0 ;
     angmx = 35.0 ;
     machlo = 1.0 ;
     machhi = 4.0 ;

     dist = 100. ; 
     ang[0]  = 0.0 ;
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
   }

   public void comPute() {

       mach2[0] = mach1[0] = mach0;
       ang[1] = ang1 ;

       if (prob == 0) {
           anlSing() ;
       }
       else {
           tayMac() ;
       }

       getStream() ;
       loadOut() ;
 
       view.repaint();
   }

   public void loadZero() {
       int i ;

       for(i=1; i<=27; ++i) {
            pspo[i] = 0.0 ;
            tsto[i] = 0.0 ;
            rsro[i] = 0.0 ;
            ptpto[i] = 0.0 ;
            prat[i] = 0.0 ;
            trat[i] = 0.0 ;
            rhorat[i] = 0.0 ;
            ptrat[i] = 0.0 ;
            turning[i] = 0.0 ;
            defl[i] = 0.0 ;
            shkang[i] = 0.0 ;
            mach2[i] = 0.0 ;
            detach[i] = false ;
       }
       for(i=1; i<=14; ++i) {
            getGeomOblq(300.0,300.0,1,i) ;
       }

   }

   public void  tayMac() {     //  Analysis for Single Cone.
       double thets,delc,dtht,gm1,angerr,signa ;
       double delts,machs,vps,vpsr,vpst ;
       double pspts,tstts,rsrts ; 
       int i,iter,itrk,itermx ;

       loadZero ();
       getGeom() ;

       gm1 = gamma - 1.0 ;
       getThetmax (mach0,gamma) ;
       thets = .99 * thmx ;
       delc = convdr * ang1 ;
       iter = 0 ;
       itermx = 200 ;
       dtht = -.005 ;  

       getAnglim (mach0,gamma) ;
       if (ang1 > delmax ) {   // check for detached shock
           shkang[1] = 90.0 ;
           getNorm(mach1[1],gamma,0,1) ;
           getGeomNorm(wxbgn[1],wybgn[1],xlong,wfamily[1],1) ;
           turning[1] = ang[1] ;  
           rmach[numray] = mach2[1];
           rpt[numray] = ptpto[1] ;
           rps[numray] = pspo[1] ;
           rthet[numray] = shkang[1] * convdr ;
           rtemp[numray] = tsto[1] ;
           rrho[numray] = rsro[1] ;
           rdel[numray] = delc ;
           for (i=1; i<=numray-1; ++i) {
              rmach[i] = rmach[numray];
              rpt[i] = rpt[numray] ;
              rps[i] = rps[numray] ;
              rtemp[i] = rtemp[numray] ;
              rrho[i] = rrho[numray] ;
              rdel[i] = rdel[numray] ;
              rthet[i] = rthet[numray] ;
           }
           return ;         
       }

       while(true) {   //  find the shock angle - for the given cone half angle
           iter = iter + 1;
           if (iter > itermx) break;
           getOblq(mach0,thets/convdr,gamma,0,1) ;

           delts = defl[1] * convdr ;
           machs = mach2[1] ;
           vps = 1.0 / Math.sqrt(2.0/(gm1 * machs*machs) + 1.0) ;
           vpsr = vps * Math.cos(thets - delts) ;
           vpst = -vps * Math.sin(thets - delts) ;

           thetold = thets ;
           vpold = vps ;
           vpoldr = vpsr ;
           vpoldt = vpst ;

           itrk = 0 ;
           numd = iter ;
           while(vpoldt < 0.0) {  // integrate until positive look at every .01 radian (.5 degree)
               itrk = itrk + 1;

               rkCalc (gamma,dtht) ;
               thetnew = thetold + dtht ;
               vpnew = Math.sqrt(vpnewr*vpnewr + vpnewt*vpnewt) ;
               machnew = Math.sqrt(2.0*vpnew*vpnew/(gm1*(1.0-vpnew*vpnew))) ;
               vpoldt = vpnewt ;
               vpoldr = vpnewr ;
               thetold = thetnew ;
               
               if (itrk < 190) {  // diagnostics
                 rthet[itrk] = thetold / convdr ;
                 vp[itrk] = vpnew ;
                 vpr[itrk] = vpoldr ;
                 vpt[itrk] = vpoldt ;
                 rmach[itrk] = machnew ;
               }
               numray = itrk ;
           }

           angerr = Math.abs(thetnew - delc) ;
           signa = angerr / Math.abs(angerr) ;

           if(angerr <= .005) { // we found it
               break ;
           }
           else { // change thets and try again
               thets = thets - .05 * signa * angerr ;
               if (Math.abs(thets) > thmx) thets = thmx ; // protection
           }
       }
   num.out.diag.o1.setText(String.valueOf(numd)) ;
   num.out.diag.o2.setText(String.valueOf(numray)) ;

       if(iter >= itermx) {  // detached - no Taylor Maccoll possible
           shkang[1] = 90.0 ;
           getNorm(mach1[1],gamma,0,1) ;
           getGeomNorm(wxbgn[1],wybgn[1],xlong,wfamily[1],1) ;
           turning[1] = ang[1] ; 
           rmach[numray] = mach2[1];
           rpt[numray] = ptpto[1] ;
           rps[numray] = pspo[1] ;
           rtemp[numray] = tsto[1] ;
           rrho[numray] = rsro[1] ;
           rdel[numray] = delc ; 
           rthet[numray] = shkang[1] * convdr ;
           for (i=1; i<=numray-1; ++i) {
              rmach[i] = rmach[numray];
              rpt[i] = rpt[numray] ;
              rps[i] = rps[numray] ;
              rtemp[i] = rtemp[numray] ;
              rrho[i] = rrho[numray] ;
              rdel[i] = rdel[numray] ;
              rthet[i] = rthet[numray] ;
           }
           return ;         
       }

       numray = 10 ;
       dtht = -(thets - delc) / (numray-1) ;
       shkang[1] = thets / convdr ;
          // get conditions across the oblique shock
       getOblq(mach0,shkang[1],gamma,0,1) ;
       turning[1] = turning[0] + defl[1] ;
       sang[1] = turning[0] + shkang[1] ;
       getGeomOblq(wxbgn[1],wybgn[1],wfamily[1],1) ;
       delts = defl[1] * convdr ;
       machs = mach2[1] ;
       vps = 1.0 / Math.sqrt(2.0/(gm1 * machs*machs) + 1.0) ;
       vpsr = vps * Math.cos(thets - delts) ;
       vpst = -vps * Math.sin(thets - delts) ;
       rmach[numray] = machs;
       thetold = thets ;
       rthet[numray] = thetold ;
       rpt[numray] = ptpto[1] ;
       rps[numray] = pspo[1] ;
       rtemp[numray] = tsto[1] ;
       rrho[numray] = rsro[1] ;
       rdel[numray] = delts ;
          // get refernece conditions
       getIsen(machs,gamma) ;
       pspts = poverpt ;
       tstts = tovertt ;
       rsrts = roverrt ;
           // initialize for itegration
       vpold = vps ;
       vpoldr = vpsr ;
       vpoldt = vpst ;
       numd = numray - 1;
       while(numd > 0) {  // integrate Taylor Maccol equations to the surface
           rkCalc (gamma,dtht) ;
           thetnew = thetold + dtht ;
           vpnew = Math.sqrt(vpnewr*vpnewr + vpnewt*vpnewt) ;
           machnew = Math.sqrt(2.0*vpnew*vpnew/(gm1*(1.0-vpnew*vpnew))) ;
           vpoldt = vpnewt ;
           vpoldr = vpnewr ;
           thetold = thetnew ;
           rmach[numd] = machnew;
           getIsen(machnew,gamma) ;
           rthet[numd] = thetold ;
           rpt[numd] = rpt[numray] ;
           rps[numd] = rps[numray] * poverpt / pspts ;
           rtemp[numd] = rtemp[numray] * tovertt / tstts ;
           rrho[numd] = rrho[numray] * roverrt / rsrts ;
           rdel[numd] = rthet[numd] - Math.acos(vpoldr / vpnew) ;
           if (numd == 1) rdel[numd] = delc;
           numd = numd -1 ;
        }
        mach2[1] = rmach[1] ;
   
        return ;
   }

   public void rkCalc (double gam, double dthet) {
//           integrated runge -kutta function for Taylor-Maccoll
       double alp1,alp2,alp3 ;
       double vpr1,vpr2,vpr3 ;
       double vpt1,vpt2,vpt3 ;
       double tht1,tht2,tht3 ;

       alp1 = .25  ;
       alp2 = .33333333 ;
       alp3 = .5 ;

       vpt1 = vpoldt + alp1 * dthet * rkfunc(gam,thetold,vpoldr,vpoldt) ;
       vpr1 = vpoldr + alp1 * dthet * vpoldt ;
       tht1 = thetold + alp1 * dthet ;

       vpt2 = vpoldt + alp2 * dthet * rkfunc(gam,tht1,vpr1,vpt1) ;
       vpr2 = vpoldr + alp2 * dthet * vpt1 ;
       tht2 = thetold + alp2 * dthet ;

       vpt3 = vpoldt + alp3 * dthet * rkfunc(gam,tht2,vpr2,vpt2) ;
       vpr3 = vpoldr + alp3 * dthet * vpt2 ;
       tht3 = thetold + alp3 * dthet ;
 
       vpnewt = vpoldt + dthet * rkfunc(gam,tht3,vpr3,vpt3) ;
       vpnewr = vpoldr + dthet * vpt3 ;

       return ;
   }

   public double rkfunc (double gam, double theta, double vpra, double vpta) {
//         runge - kutta function for Taylor Maccoll
       double a,b,c,ans;

       a = 1.0 - vpra*vpra - vpta*vpta ;
       b = 2.0 * vpra + vpta / Math.tan(theta) ;
       c = .5 * (gam - 1.0) ;

       ans = (vpra * vpta*vpta -c*a*b) / (c*a-vpta*vpta) ;

       return ans ;
   }

   public void anlSing() {     //  Analysis for Single  Wedge.
       loadZero() ;
       getGeom () ;

       mach1[1] = mach2[0] ;
       nshocks = 1 ;
       nramps = 1 ;
       angr  = ang[1] * convdr ;
       detach[1] = false ;

       getAnglim(mach1[1],gamma) ;

       if (ang[1] > delmax) {
         getNorm(mach1[1],gamma,0,1) ;
         getGeomNorm(wxbgn[1],wybgn[1],xlong,wfamily[1],1) ;
         turning[1] = ang[1] ;
       }
       else {
         shkang[1]  = getShkang(mach1[1],angr,gamma) ;
         getOblq(mach1[1],shkang[1],gamma,0,1) ;
         turning[1] = turning[0] + defl[1] ;
         sang[1] = turning[0] + shkang[1] ;
         getGeomOblq(wxbgn[1],wybgn[1],wfamily[1],1) ;
       }
       return ;
   }

   public void getThetmax (double machin, double gam) {
       double a1, ab1, ac1, sints, msq, mfor ;

       msq = machin * machin ;
       mfor = msq * msq ;

       a1 = 2.0 * gam * mfor ;
       ab1 = 4.0 * msq - (gam + 1.0) * mfor ;
       ac1 = -(2.0 + (gam + 1.0) * msq) ;
       sints = (-ab1 + Math.sqrt(Math.pow(ab1,2.0)-4.0*a1*ac1))/(2.0*a1) ;
       thmx = Math.asin(Math.sqrt(sints)) ;

   num.out.diag.o12.setText(String.valueOf(filter3(thmx/convdr))) ;

       return ;
   }

   public void getAnglim (double machin, double gam) {
       double a1, ab1, ac1, sints, msq, mfor, cotd ;
       double f6 = -11.5385 ;
       double f5 = 78.2051 ;
       double f4 = -190.7517 ;
       double f3 = 170.169 ;
       double f2 = 41.5865 ;
       double f1 = -79.197 ;
       double g6 = -0.1002 ;
       double g5 =  1.1018 ;
       double g4 = -3.0212 ;
       double g3 = -7.4450 ;
       double g2 = 52.7644 ;
       double g1 = -25.2683 ;

       msq = machin * machin ;
       mfor = msq * msq ;

       if(prob == 0) {  // wedge problem
          a1 = 2.0 * gam * mfor ;
          ab1 = 4.0 * msq - (gam + 1.0) * mfor ;
          ac1 = -(2.0 + (gam + 1.0) * msq) ;
          sints = (-ab1 + Math.sqrt(Math.pow(ab1,2.0)-4.0*a1*ac1))/(2.0*a1) ;
          getThetmax(machin,gam) ;

          cotd = Math.tan(thmx)*(((gam+1.0)*msq)/    
                (2.0*(msq * sints - 1.0))-1.0);
          delmax = (Math.atan(1.0/cotd))/convdr ;

          return;
       }
       if (prob == 1) { // cone problem
          if(mach0 < 2.0) {
             delmax = f6 * machin * machin * machin * machin *machin
                 + f5 * machin * machin * machin * machin
                 + f4 * machin * machin * machin
                 + f3 * machin * machin
                 + f2 * machin + f1 ;
          }
          else {
             delmax = g6 * machin * machin * machin * machin *machin
                 + g5 * machin * machin * machin * machin
                 + g4 * machin * machin * machin
                 + g3 * machin * machin
                 + g2 * machin + g1 ;
          }

           return ;
       }
   }

   public double getShkang (double machin, double delr, double gam) {
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
      return number;
   }

   public void getOblq (double machin, double shang, double gam,
                        int upstrm, int index) {
          // NACA 1135 - oblique shock relations.
       double mst, gm1, gp1, msq, m2sq, cotd ;

       mst = machin * Math.sin(shang*convdr) ;
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
       cotd = Math.tan(shang*convdr)*((gp1*msq)/    
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

  public void getIsen (double machin, double gam) { // isentropic relations 
     double mach1s,msm1,gm1,gp1,fac,fac1 ;   
          // poverpt and tovertt are ratiod to total conditions 
     mach1s = machin*machin ; 
     gm1 = gam - 1.0 ;
     gp1 = gam + 1.0 ;
     msm1 = mach1s - 1.0;
     fac = 1.0 + .5*gm1*mach1s;

     poverpt = Math.pow(1.0/fac,gam/gm1) ;                  /* EQ 44 */
     tovertt = 1.0 / fac ;                                   /* EQ 43 */
     roverrt = Math.pow(1.0/fac,1.0/gm1) ;                 /* EQ 45 */
     fac1 = gp1/(2.0*gm1) ;
     arat = machin * Math.pow(fac,-fac1) * Math.pow(gp1/2.0,fac1) ; /* EQ 80 */
     arat = 1.0/arat ;
     mu   = (Math.asin(1.0/machin))/convdr ;
     nu   = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1/gp1)) 
             - Math.atan(Math.sqrt(msm1)) ;
     nu   = nu / convdr;

     return;
  }

   public void getGeom () {
          // wedge geometry
       int i ;
         wfamily[1] = 1 ;
         wslope[1]  = Math.tan(convdr * ang[1]) ;
         wxbgn[1]   = 0.0 ;
         wybgn[1]   = 0.0 ;
         winter[1]  = wybgn[1] - wslope[1] * wxbgn[1] ;

         wfamily[2] = 1 ;
         wslope[2]  = Math.tan(convdr * (ang[1] + ang[2])) ;
         wxbgn[2]   = wxbgn[1] + dist ;
         wybgn[2]   = wxbgn[2] * wslope[1] + winter[1];
         winter[2]  = wybgn[2] - wslope[2] * wxbgn[2] ;
         wxnd[2]    = xlong ;
         wynd[2]    = wxnd[2] * wslope[2] + winter[2] ;

         wxnd[1]    = wxbgn[2] ;
         wynd[1]    = wybgn[2] ;
 
       return;
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

   public void loadOut() {

      if (prob == 0) {  // wedge problem
         outzn = 1;

         num.out.wdg.o1.setText(String.valueOf(filter3(mach2[outzn]))) ;
         num.out.wdg.o3.setText(String.valueOf(filter3(shkang[outzn])));
         num.out.wdg.o2.setText(String.valueOf(filter3(prat[outzn]))) ;
         num.out.wdg.o4.setText(String.valueOf(filter3(ptrat[outzn]))) ;
         num.out.wdg.o6.setText(String.valueOf(filter3(trat[outzn]))) ;
         num.out.wdg.o7.setText(String.valueOf(filter3(rhorat[outzn])));
         return ;
      }
      if (prob ==1) {  // cone problem
         num.out.con.o3.setText(String.valueOf(filter3(shkang[1])));
         num.out.con.o1.setText(String.valueOf(filter3(mach2[1]))) ;
         num.out.con.o8.setText(String.valueOf(filter3(rmach[outzn]))) ;
         num.out.con.o2.setText(String.valueOf(filter3(rps[outzn]))) ;
         num.out.con.o4.setText(String.valueOf(filter3(rpt[outzn]))) ;
         num.out.con.o6.setText(String.valueOf(filter3(rtemp[outzn]))) ;
         num.out.con.o7.setText(String.valueOf(filter3(rrho[outzn])));
         num.out.con.o9.setText(String.valueOf(filter3(rdel[outzn]/convdr)));
         num.out.con.o10.setText(String.valueOf(filter3(rthet[outzn]/convdr)));
      }
   }

   public void getStream () {
       int i,k,p ;

      for(i=1; i<=4; ++ i) {
         strx[1][i] = wxbgn[1] - 100. ;
         stry[1][i] = 100.0 - (i-1)*100./3. ;
         strx[2][i] = wxbgn[1] ;
         stry[2][i] = stry[1][i] ;
      }
      for(i=4; i<=7; ++ i) {
         strx[1][i] = wxbgn[1] - 100. ;
         stry[1][i] = 0.0 - (i-4)*100./3. ;
         strx[2][i] = wxbgn[1] ;
         stry[2][i] = stry[1][i] ;
      }

      if(detach[1]) {   // detached
         for(i=1; i<=4; ++ i) {
            stry[3][i] = stry[2][i] ;
            strx[3][i] = strx[2][i] ;
            strx[4][i] = wxnd[2] ;
            stry[4][i] = stry[3][i] + (strx[4][i]-strx[3][i]) * wslope[1] ;
         }
         for(i=4; i<=7; ++ i) {
            stry[3][i] = stry[2][i] ;
            strx[3][i] = strx[2][i] ;
            strx[4][i] = wxnd[2] ;
            stry[4][i] = stry[3][i] - (strx[4][i]-strx[3][i]) * wslope[1]  ;
         }
      }
      else {  // attached
         if(prob == 0) { // wedge problem
             for(i=1; i<=4; ++ i) {
                stry[3][i] = stry[2][i] ;
                strx[3][i] = (stry[3][i] - sinter[1])/sslope[1] ;
                strx[4][i] = wxnd[2] ;
                stry[4][i] = stry[3][i] + (strx[4][i]-strx[3][i]) * wslope[1] ;
             }
             for(i=4; i<=7; ++ i) {
                stry[3][i] = stry[2][i] ;
                strx[3][i] = (stry[3][i] + sinter[1])/-sslope[1] ;
                strx[4][i] = wxnd[2] ;
                stry[4][i] = stry[3][i] - (strx[4][i]-strx[3][i]) * wslope[1]  ;
             }
          }
          else {  // cone problem
             for(i=1; i<=4; ++ i) {
                stry[3][i] = stry[2][i] ;
                strx[3][i] = (stry[3][i]) / Math.tan(rthet[numray]) ;
             }
             for(i=4; i<=7; ++ i) {
                stry[3][i] = stry[2][i] ;
                strx[3][i] = (stry[3][i]) / - Math.tan(rthet[numray]) ;
             }
             for(k=4; k<=12; ++k) {
                p=k-4;
                for(i=1; i<=4; ++i) {
                    strx[k][i] = Math.abs(strx[k-1][i] * 
                        (Math.tan(rthet[numray-p])-Math.tan(rdel[numray-p]))/
                        (Math.tan(rthet[numray-p-1])-Math.tan(rdel[numray-p]))) ;
                    stry[k][i] = strx[k][i]* Math.tan(rthet[numray-p-1]) ;
                }
                for(i=4; i<=7; ++i) {
                    strx[k][i] = Math.abs(strx[k-1][i] * 
                        (Math.tan(rthet[numray-p])-Math.tan(rdel[numray-p]))/
                        (Math.tan(rthet[numray-p-1])-Math.tan(rdel[numray-p]))) ;
                    stry[k][i] = -strx[k][i]* Math.tan(rthet[numray-p-1]) ;
                }
             }
          }
       }

       return ;
   }

   public float filter3(double inumbr) {
     //  output only to .001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 1000.) ;
       number = (float) (intermed / 1000. );
       return number ;
  }

   class Num extends Panel {
     Shock outerparent ;
     Inp inp ;
     Out out ;

     Num (Shock target) {                           
          outerparent = target ;
          setLayout(new GridLayout(2,1,10,10)) ;
 
          inp = new Inp(outerparent) ;  
          out = new Out(outerparent) ;
 
          add(inp) ;
          add(out) ;
     }

     class Inp extends Panel {
       Shock outerparent ;
       Inright inright ;
       Inleft inleft ;

       Inp (Shock target) {
                             
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          inleft = new Inleft(outerparent) ; 
          inright = new Inright(outerparent) ;
 
          add(inleft) ;
          add(inright) ;
       }
 
       class Inright extends Panel {
        Shock outerparent ;
        Scrollbar s1, s3 ;
        Label l1 ;
        Choice plntch ;

        Inright (Shock target) {
            int i1, i3 ;
 
            outerparent = target ;
            setLayout(new GridLayout(6,1,10,10)) ;

            i1 = (int) (((mach0 - machlo)/(machhi - machlo))*1000.) ;
            i3 = (int) (((ang1 - angmn)/(angmx-angmn))*1000.) ;
 
            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

            l1 = new Label("Version 1.3e ", Label.LEFT) ;
            l1.setForeground(Color.red) ;

            plntch = new Choice() ;
            plntch.addItem("Earth") ;
            plntch.addItem("Mars");
            plntch.setBackground(Color.white) ;
            plntch.setForeground(Color.blue) ;
            plntch.select(0) ;

            add(l1) ; 
            add(new Label(" ", Label.CENTER)) ;
            add(new Label(" ", Label.CENTER)) ;
            add(s1) ;
            add(s3) ;
            add(plntch) ;
          }

          public boolean handleEvent(Event evt) {
            if(evt.id == Event.ACTION_EVENT) {
               this.handleCho(evt) ;
               return true ;
            }
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

             i1 = s1.getValue() ;
             i3 = s3.getValue() ;

             v1 = i1 * (machhi - machlo)/ 1000. + machlo ;
             v3 = i3 * (angmx - angmn)/ 1000. + angmn ;
 
             fl1 = (float) v1 ;
             fl3 = (float) v3 ;

             inleft.f1.setText(String.valueOf(fl1)) ;
             inleft.f3.setText(String.valueOf(fl3)) ;

             mach0 = v1 ;
             ang1 = v3 ;

             comPute() ;    
          }
 
          public void handleCho(Event evt) {     // range
             float fl2 ;
             if (planet == 0) gamma = 1.4;
             if (planet == 1) gamma = 1.29;

             fl2 = (float) gamma ;
             inleft.f2.setText(String.valueOf(fl2)) ;

             comPute() ;    
          }
        }

        class Inleft extends Panel {
          Shock outerparent ;
          TextField f1, f2, f3;
          Label l1,l2,l3,l4 ;
          Choice probch ;

          Inleft (Shock target) {
            
            outerparent = target ;
            setLayout(new GridLayout(6,2,2,10)) ;
  
            f1 = new TextField(String.valueOf(mach0),5) ;
            f2 = new TextField(String.valueOf(gamma),5) ;
            f3 = new TextField(String.valueOf(ang1),5) ;

            l1 = new Label("Input - ", Label.RIGHT) ;
            l1.setForeground(Color.blue) ;
            l2 = new Label("Upstream", Label.LEFT) ;
            l2.setForeground(Color.blue) ;
            l3 = new Label("Shock", Label.RIGHT) ;
            l3.setForeground(Color.red) ;
            l4 = new Label("Waves", Label.LEFT) ;
            l4.setForeground(Color.red) ;

            probch = new Choice() ;
            probch.addItem("Wedge") ;
            probch.addItem("Cone");
//            probch.addItem("Diagnostic");
            probch.setBackground(Color.white) ;
            probch.setForeground(Color.blue) ;
            probch.select(0) ;

            add(l3) ;
            add(l4) ;

            add(new Label("Shape:", Label.RIGHT)) ;  
            add(probch) ;

            add(l1) ;
            add(l2) ;

            add(new Label("Mach", Label.CENTER)) ;  
            add(f1) ;

            add(new Label(" Angle", Label.CENTER)) ;
            add(f3) ;

            add(new Label(" Gamma", Label.CENTER)) ;
            add(f2) ;
          }
  
          public boolean handleEvent(Event evt) {
            if(evt.id == Event.ACTION_EVENT) {
               this.handleText(evt) ;
               return true ;
            }
            else return false ;
          }

          public void handleText(Event evt) {
             Double V1,V2,V3,V4,V5 ;
             double v1,v2,v3,v4,v5 ;
             float fl1,fl2 ;
             int i1,i3,i4,i5 ;
             int probo ;

             probo   = probch.getSelectedIndex() ;

             if (probo == 0) {
                 layout.show(out, "first")  ;
                 prob = 0 ;
             }
             if (probo == 1) {
                 layout.show(out, "third")  ;
                 prob = 1;
             }
/*
             if (probo == 2) {
                 layout.show(out, "second")  ;
                 prob = 1;
             }
*/

         // Mach number - range from machlo to machhi
             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             if(v1 < machlo) {
                v1 = machlo+ .02 ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
             }
             if(v1 > machhi) {
                v1 = machhi ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
             }
         // Gamma - range from 1.0 to 2.0
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             if(v2 < 1.0) {
                v2 = 1.02 ;
                fl2 = (float) v2 ;
                f2.setText(String.valueOf(fl2)) ;
             }
             if(v2 > 2.0) {
                v2 = 2.0 ;
                fl2 = (float) v2 ;
                f2.setText(String.valueOf(fl2)) ;
             }
         // ramp angle # 1  range from 0 to +30 for prob = 0
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             if(v3 < angmn) {
               v3 = angmn ;
               fl1 = (float) v3 ;
               f3.setText(String.valueOf(fl1)) ;
             }
             if(v3 > angmx) {
                v3 =  angmx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
             }
        
             i1 = (int) (((v1 - machlo)/(machhi - machlo))*1000.) ;
             i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;

             inright.s1.setValue(i1) ;
             inright.s3.setValue(i3) ;

             mach0 = v1 ;
             gamma = v2 ;
             ang1 = v3 ;

             comPute() ;
          }
        }
     } // end In

     class Out extends Panel {
       Shock outerparent ;
       Wdg wdg;
       Con con ;
       Diag diag;

       Out (Shock target) { 
         outerparent = target ;
         layout = new CardLayout() ;
         setLayout(layout) ;

         wdg = new Wdg(outerparent) ;
         con = new Con(outerparent) ;
         diag = new Diag(outerparent) ;

         add ("first", wdg) ;
         add ("second", diag) ;
         add ("third", con) ;
       }

       class Diag extends Panel {
          TextField o1,o2,o3,o4,o5,o6,o7,o8,o9,o10,o11,o12 ;
          TextField i1 ;
          Label lo1,lo2,lo3,lo4 ;

          Diag (Shock target) {
             outerparent = target ;
             setLayout(new GridLayout(5,3,2,10)) ;
 
             i1 = new TextField("1",5) ;

             o1 = new TextField() ;
             o2 = new TextField() ;
             o3 = new TextField() ;
             o4 = new TextField() ;
             o5 = new TextField() ;
             o6 = new TextField() ;
             o7 = new TextField() ;
             o8 = new TextField() ;
             o9 = new TextField() ;
             o10 = new TextField() ;
             o11 = new TextField() ;
             o12 = new TextField() ;

             add(new Label("Diagnostics ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(i1) ; 

             add(o1) ;
             add(o2) ;
             add(o3) ;

             add(o4) ;
             add(o5) ;
             add(o6) ;

             add(o7) ;
             add(o8) ;
             add(o9) ;

             add(o10) ;
             add(o11) ;
             add(o12) ;
          }

          public boolean handleEvent(Event evt) {
            Double V1 ;
            double v1;
            int indx ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(i1.getText()) ;
              v1 = V1.doubleValue() ;

              indx = (int) v1 ;

              comPute ();

               o4.setText(String.valueOf(filter3(vp[indx]))) ;
               o5.setText(String.valueOf(filter3(vpr[indx]))) ;
               o6.setText(String.valueOf(filter3(vpt[indx]))) ;

               o7.setText(String.valueOf(filter3(rpt[indx]))) ;
               o8.setText(String.valueOf(filter3(rmach[indx]))) ;

              return true ;
            }
            else return false ;
         } // Handler
       } //Diag

       class Wdg extends Panel {
          TextField o1, o2, o3, o4, o6, o7 ;
          Label lo1,lo2,lo3,lo4 ;

          Wdg (Shock target) {
             outerparent = target ;
             setLayout(new GridLayout(6,4,2,5)) ;
 
             o1 = new TextField() ;
             o1.setBackground(Color.black) ;
             o1.setForeground(Color.green) ;
             o2 = new TextField() ;
             o2.setBackground(Color.black) ;
             o2.setForeground(Color.green) ;
             o3 = new TextField() ;
             o3.setBackground(Color.black) ;
             o3.setForeground(Color.green) ;
             o4 = new TextField() ;
             o4.setBackground(Color.black) ;
             o4.setForeground(Color.green) ;
             o6 = new TextField() ;
             o6.setBackground(Color.black) ;
             o6.setForeground(Color.green) ;
             o7 = new TextField() ;
             o7.setBackground(Color.black) ;
             o7.setForeground(Color.green) ;

             lo1 = new Label("Output -", Label.RIGHT) ;
             lo1.setForeground(Color.blue) ;
             lo2 = new Label("Downstream", Label.LEFT) ;
             lo2.setForeground(Color.blue) ;
             lo3 = new Label(" ", Label.CENTER) ;
             lo3.setForeground(Color.blue) ;
             lo4 = new Label("Zone 1", Label.CENTER) ;
             lo4.setForeground(Color.blue) ;

             add(lo1) ;
             add(lo2) ;
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 

             add(lo3) ;
             add(lo4) ;
             add(new Label("p / p0", Label.CENTER)) ;  
             add(o2) ;

             add(new Label("Mach", Label.CENTER)) ;  
             add(o1) ;  
             add(new Label("T / T0", Label.CENTER)) ; 
             add(o6) ;

             add(new Label("Shock Angle", Label.CENTER)) ; 
             add(o3) ; 
             add(new Label("rho / rho0", Label.CENTER)) ;  
             add(o7) ; 

             add(new Label("pt / pt0", Label.CENTER)) ;  
             add(o4) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 

             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
          }
        } // end Wdg

       class Con extends Panel {
          TextField o1, o2, o3, o4, o6, o7, o8, o9, o10 ;
          Label lo1,lo2,lo3,lo4 ;
          Choice raych,draych ;

          Con (Shock target) {
             outerparent = target ;
             setLayout(new GridLayout(6,4,2,5)) ;
 
             o1 = new TextField() ;
             o1.setBackground(Color.black) ;
             o1.setForeground(Color.green) ;
             o2 = new TextField() ;
             o2.setBackground(Color.black) ;
             o2.setForeground(Color.green) ;
             o3 = new TextField() ;
             o3.setBackground(Color.black) ;
             o3.setForeground(Color.green) ;
             o4 = new TextField() ;
             o4.setBackground(Color.black) ;
             o4.setForeground(Color.green) ;
             o6 = new TextField() ;
             o6.setBackground(Color.black) ;
             o6.setForeground(Color.green) ;
             o7 = new TextField() ;
             o7.setBackground(Color.black) ;
             o7.setForeground(Color.green) ;
             o8 = new TextField() ;
             o8.setBackground(Color.black) ;
             o8.setForeground(Color.green) ;
             o9 = new TextField() ;
             o9.setBackground(Color.black) ;
             o9.setForeground(Color.green) ;
             o10 = new TextField() ;
             o10.setBackground(Color.black) ;
             o10.setForeground(Color.green) ;

             raych = new Choice() ;
             raych.addItem("Surface") ;
             raych.addItem("Ray 2");
             raych.addItem("Ray 3");
             raych.addItem("Ray 4");
             raych.addItem("Ray 5");
             raych.addItem("Ray 6");
             raych.addItem("Ray 7");
             raych.addItem("Ray 8");
             raych.addItem("Ray 9");
             raych.addItem("Shock") ;
             raych.setBackground(Color.white) ;
             raych.setForeground(Color.blue) ;
             raych.select(0) ;

             draych = new Choice() ;
             draych.addItem("Hide");
             draych.addItem("Show") ;
             draych.setBackground(Color.white) ;
             draych.setForeground(Color.blue) ;
             draych.select(0) ;

             lo1 = new Label("Output -", Label.RIGHT) ;
             lo1.setForeground(Color.blue) ;
             lo2 = new Label("Downstream", Label.LEFT) ;
             lo2.setForeground(Color.blue) ;
             lo3 = new Label("Ray:", Label.RIGHT) ;
             lo3.setForeground(Color.black) ;

             add(lo1) ;
             add(lo2) ;
             add(new Label("Mach", Label.CENTER)) ;  
             add(o8) ;

             add(lo3) ;
             add(raych) ;
             add(new Label("p / p0", Label.CENTER)) ;  
             add(o2) ; 
 
             add(new Label("Surface Mach", Label.CENTER)) ;  
             add(o1) ; 
             add(new Label("T / T0", Label.CENTER)) ; 
             add(o6) ;

             add(new Label("Shock Angle", Label.CENTER)) ; 
             add(o3) ; 
             add(new Label("rho / rho0", Label.CENTER)) ;  
             add(o7) ; 

             add(new Label("pt / pt0", Label.CENTER)) ; 
             add(o4) ;
             add(new Label("Turning ", Label.CENTER)) ; 
             add(o9) ; 

             add(new Label("Ray Plot:", Label.CENTER)) ; 
             add(draych) ; 
             add(new Label("Angle", Label.CENTER)) ; 
             add(o10) ; 
          }

          public boolean handleEvent(Event evt) {
            if(evt.id == Event.ACTION_EVENT) {
               outzn  = 1 + raych.getSelectedIndex() ;
               drawray  = draych.getSelectedIndex() ;

               comPute() ;
               return true ;
            }
            else return false ;
          }    
        } // end Con

     } // end Out
  } // end Num

  class Viewer extends Canvas {
     Shock outerparent ;

     Viewer (Shock target) {
         setBackground(Color.white) ;
     }
 
     public void update(Graphics g) {
         view.paint(g) ;
     }

     public void paint(Graphics g) {
       int i,k ;
       int exes[] = new int[10] ;
       int whys[] = new int[10] ;
       int yorgn = 200 ;
       int xorgn = 50 ;

       offsGg.setColor(Color.white) ;
       offsGg.fillRect(0,0,400,500) ;
       offsGg.setColor(Color.blue) ;
       offsGg.drawString("Upstream", 0, 20) ;
       offsGg.drawString("Flow", xorgn-40, 35) ;
       offsGg.setColor(Color.black) ;
       offsGg.fillRect(xorgn-40,42,30,9) ;
       exes[0] = xorgn ;
       whys[0] = 46;
       exes[1] = xorgn - 10;
       whys[1] = 56;
       exes[2] = xorgn - 10;
       whys[2] = 36;
       Polygon poly1 = new Polygon(exes,whys,3) ;
       offsGg.fillPolygon(poly1) ;
          // draw geometry
       offsGg.setColor(Color.red) ;
          // draw ramps
       for (i = 1; i <= nramps; ++i) {
         exes[0] = xorgn + (int) wxbgn[i] ;
         whys[0] = yorgn - (int) (wxbgn[i]*wslope[i] + winter[i]) ;
         exes[1] = xorgn + (int) xlong ;
         whys[1] = yorgn - (int) (xlong*wslope[i] + winter[i]) ;
         exes[2] = xorgn + (int) xlong ;
         whys[2] = yorgn + (int) (xlong*wslope[i] + winter[i]) ;
         offsGg.setColor(Color.red) ;
         Polygon poly = new Polygon(exes,whys,3) ;
         offsGg.fillPolygon(poly) ;
         if (prob == 0) {
           offsGg.drawString("1", exes[1]-70, whys[1]-10) ;
           offsGg.drawString("1", exes[1]-70, whys[2]+10) ;
         }
         exes[0] = xorgn + (int) wxbgn[i] ;
         whys[0] = yorgn - (int) (wxbgn[i]*wslope[i] + winter[i]) ;
         exes[1] = xorgn + (int) xlong ;
         whys[1] = whys[0] ;
         offsGg.setColor(Color.white) ;
         offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
         offsGg.drawString("Angle", exes[1]-50, whys[0]-5) ;
       }
          // draw shock waves
      for(i=1; i <= nshocks; ++i) {
         exes[0] = xorgn + (int) sxbgn[i] ;
         whys[0] = yorgn - (int) sybgn[i] ;
         exes[1] = xorgn + (int) sxnd[i] ;
         whys[1] = yorgn - (int) synd[i] ;
         if (detach[i]) {
           offsGg.setColor(Color.magenta) ; 
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
         }
         else {
           offsGg.setColor(Color.blue) ; 
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;  
         }
         whys[1] = yorgn + (int) synd[i] ;
         if (detach[i]) {
           offsGg.setColor(Color.magenta) ; 
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
         }
         else {
           offsGg.setColor(Color.blue) ; 
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;  
         }
       }
             // draw rays for prob = 1
       if(drawray == 1) {
          if(prob == 1 && detach[1] != true) {
             offsGg.setColor(Color.blue) ; 
             exes[0] = xorgn + (int) sxbgn[1] ;
             whys[0] = yorgn - (int) sybgn[1] ;
             for(i=1; i<=numray; ++ i) {
                exes[1] = xorgn + (int) (sxbgn[1] + xlong*Math.cos(rthet[i])) ;
                whys[1] = yorgn - (int) (sybgn[1] + xlong*Math.sin(rthet[i])) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
             exes[1] = xorgn + (int) (sxbgn[1] + xlong*Math.cos(rthet[outzn])) ;
             whys[1] = yorgn - (int) (sybgn[1] + xlong*Math.sin(rthet[outzn])) ;
             offsGg.setColor(Color.red) ; 
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             offsGg.setColor(Color.blue) ; 
             for(i=1; i<=numray; ++ i) {
                exes[1] = xorgn + (int) (sxbgn[1] + xlong*Math.cos(rthet[i])) ;
                whys[1] = yorgn - (int) (sybgn[1] - xlong*Math.sin(rthet[i])) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
             exes[1] = xorgn + (int) (sxbgn[1] + xlong*Math.cos(rthet[outzn])) ;
             whys[1] = yorgn - (int) (sybgn[1] - xlong*Math.sin(rthet[outzn])) ;
             offsGg.setColor(Color.red) ; 
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
          }
       }
          // draw streamlines
    if(prob == 0 || detach[1] == true) {
       offsGg.setColor(Color.black) ; 
        for (i = 1; i <= 7; ++i) {
          exes[0] = (int) strx[1][i] + xorgn ;
          whys[0] = (int) stry[1][i] + yorgn ;
          for(k=2; k<=4; ++k) {
            exes[1] = (int) strx[k][i] + xorgn ;
            whys[1] = (int) stry[k][i] + yorgn ;
            offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
            exes[0] = exes[1] ;
            whys[0] = whys[1] ;
          }
        }
     }

     if(prob == 1 && detach[1] != true) {
        offsGg.setColor(Color.black) ; 
        for (i = 1; i <= 7; ++i) {
          exes[0] = (int) strx[1][i] + xorgn ;
          whys[0] = (int) stry[1][i] + yorgn ;
          for(k=2; k<=12; ++k) {
            exes[1] = (int) strx[k][i] + xorgn ;
            whys[1] = (int) stry[k][i] + yorgn ;
            offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
            exes[0] = exes[1] ;
            whys[0] = whys[1] ;
          }
        }
      }

       g.drawImage(offscreenImg,0,0,this) ;
    }
  }
}
