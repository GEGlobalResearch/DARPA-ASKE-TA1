/*
                Interactive  Nozzle  Design  Program

     Program to perform one dimensional design and analysis of
                a)   rocket nozzle
                b)   turbine convergent nozzle
                c)   turbine convergent - divergent nozzle

                     Version 1.3g   - 4 Oct 06

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
                 * clean-up the graphics
                 * re-organize panels
                 *    delete graphics input panel
                 *    add chemistry input panel
                 * add "reset"
                 * make gamma an optional function of Temp
                         (from E-sim)
                 * include the units in the output .. like EngineSim
                 * add different properties to rocket calc
                 *   change flow from air to other gases
                 *   change getAir -to getMfr 
                 * change layout slightly
                 * make the rocket the default
                 * fix bug in area output
      Old Test

                                                     TJB 4 Oct 06
*/

import java.awt.*;
import java.lang.Math ;

public class Nozzle extends java.applet.Applet {

   final double convdr = 3.14515926/180.;
   final double runiv = 1545. ;

   static double rthrt, rexit, rzero ;
   static double ath, pt, pamb, pexit, tt, machth, mflow ;
   static double athroat, athmx, athmn ;
   static double arat, aexmx, aexmn ;
   static double azrat, azmx, azmn, azero ;
   static double ptin, ptmx, ptmn ;
   static double psin, pemx, pemn ;
   static double ttin, ttmx, ttmn ;
   static double fact,lngth ;
   static double npr,nprm,uexm,uex,fgros ;
   static double mweight,rgas,gam0,gamma,tcomb ;
   static double mexit,aexit,athrust ;
   static int plntyp,plntpold,noztyp,opt ;
   static int gasopt, gamopt, tcomopt, counter ;

   static double xg[][]  = new double[11][45] ;
   static double yg[][]  = new double[11][45] ;

   static int antim,ancol,lunits ; 
   static double lconv,fconv,pconv,tref,tconv,mconv1 ;

   int xt,yt,sldloc ;

   Image offscreenImg ;
   Graphics offsGg ;
   Pn pn ;
   Viewer view ;
   CardLayout layout ;

   public void init() {
     Nozzle a = new Nozzle() ;

     offscreenImg = createImage(this.size().width,
                      this.size().height) ;
     offsGg = offscreenImg.getGraphics() ;

     setLayout(new GridLayout(1,2,5,5)) ;

     setDefaults () ;

     view = new Viewer(this) ;
     pn = new Pn(this) ;

     add(view) ;
     add(pn) ;

     comPute() ;
     view.start() ;
  }


  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }


   public void setDefaults() {

       noztyp = 1 ;                 // CD nozzle 
       plntyp = 0 ;                 // rocket
       plntpold = 0 ;

       lunits = 0 ;
       lconv = 1.;                         /*  feet    */
       fconv = 1.0;                        /* pounds   */
       pconv = 1.0  ;                   /* lb/sq in */
       tref =  459.7 ;                 /* zero rankine */
       tconv = 1.0 ;                    /* degrees F */
       mconv1 = 1.0 ;                   /* airflow rate lbs/sec*/

       athmx   = 3.0 ;
       athmn = .01 ;
       aexmx = 2.0  ;
       azmx = 2.0  ;
       azmx = 10. ;
       azmn = 1.1 ;
       ptmx = 200. ;
       ptmn = 1. ;
       pemx = 15. ;
       pemn = .01 ;
       ttmx = 4000. ;
       ttmn = 32. ;

       mweight = 29.0 ;
       rgas = 1716. ;                /* air - ft2/sec2 R */
       gasopt = 1 ;
       gamopt = 1 ;
       gam0 = 1.4 ;
       gamma = 1.3 ;
       tcomopt = 1 ;

       athroat = 2.0  ;
       arat = 1.6 ;
       azero  = 4.0 ;
       azrat = 1.5 ;
       ptin = 70. ;
       tcomb = ttin = 2500. ;
       psin = 14.7 ;

       xt = 0;  yt = 0; 
       fact = 50.0 ;
       sldloc = 70 ;
       lngth = .5 ;
   }

   public void comPute() {
       double gm1,fac1,prat,trat,aircor,g0 ;
       double psub,psup ;
       double mnsup,mnsub,ptrat,anso,ansn,pso,psn,deriv ;
       double gamtfac ;

       g0    = 32.2 ;
       rgas = runiv * g0 / mweight ;
       ath = athroat / lconv / lconv ;
       rthrt = Math.sqrt(144.*ath / 3.1415926) ;
       aexit = arat * ath ;
       rexit = Math.sqrt(144.*aexit / 3.1415926) ;
//       azero = 20. ;
//       if (athroat > 20.) azero = 3.0 * athroat ;
       azero = azrat * ath ;
       rzero = Math.sqrt(144.*azero / 3.1415926) ;
       pt    = ptin / pconv ;
       pamb  = psin / pconv ;
       tt    = (ttin + tref) / tconv ;
       if (gamopt == 1) {
           gamtfac = getGama(tt,gamopt) ;
           gamma = gam0 * gamtfac / 1.4 ;
       }
       gm1   = gamma - 1.0 ;
       fac1  = gm1 / gamma ;
       prat  = pamb / pt ;
       nprm  = 1.0 /prat ;
       athrust = ath ;

       if (noztyp == 0) {    // convergent nozzle
          machth = Math.sqrt(2.0*(Math.pow(prat,-fac1) - 1.0)/gm1) ;
          athrust = ath ;
          if (machth < 1.0 ) {     //  subsonic flow
             trat = getTisen(machth,gamma) ;
             uex = machth * Math.sqrt (gamma * rgas * trat * tt) ;
             aircor = getMfr(machth,rgas,gamma) ;
             mflow = aircor*(144.*ath)*(pt/14.7)/Math.sqrt(tt/518.) ;
             pexit = pamb ;
          }
          if (machth > 1.0 ) {     //  choked flow
             machth = 1.0 ;
             trat = getTisen(1.0,gamma) ;
             psup = getPisen(1.0,gamma) * pt ;
             uex =  Math.sqrt (gamma * rgas * trat * tt) ;
             aircor = getMfr(1.0,rgas,gamma) ;
             mflow = aircor*(144.*ath)*(pt/14.7)/Math.sqrt(tt/518.) ;
             pexit = psup ;
          }
          mexit = machth ;
          rexit = rthrt ;
       }
       if (noztyp == 1) {  // convergent - divergent nozzle
          counter = 0 ;
          athrust = aexit ;
          machth = 1.0 ;         // assume flow is choked
          aircor = getMfr(1.0,rgas,gamma) ;
          mexit = getMach(2,(aircor/arat),rgas,gamma) ;
//      pn.out.t6.diag3.setText(String.valueOf(filter3(gamma)));
//      pn.out.t6.diag4.setText(String.valueOf(filter3(mexit)));
          psup = getPisen(mexit,gamma) * pt ;
          mflow = aircor*(144.*ath)*(pt/14.7)/Math.sqrt(tt/518.) ;
          if (pamb <= psup) {   // under expanded
             trat = getTisen(mexit,gamma) ;
             uex = mexit * Math.sqrt (gamma * rgas * trat * tt) ;
             pexit = psup ;
          }
          if (pamb > psup) {    // over expanded
              // find exit pressure at which normal shock leaves the nozzle
             mnsup = mexit ;
             psub = psup * getNsps(mnsup,gamma) ;
             if (pamb <= psub) {     // no normal shock in nozzle
                pexit = psup ;
                trat = getTisen(mexit,gamma) ;
                uex = mexit * Math.sqrt (gamma * rgas * trat * tt) ;
             }
             if (pamb > psub) {     // normal shock in nozzle
                pexit = pamb ;
                anso  = aexit ;
                mnsup = mexit ;
                mnsub = getNsmac(mnsup,gamma) ;
                ptrat = getNspt(mnsup,gamma) ;
                pso = getPisen(mnsub,gamma) * ptrat * pt ;
                ansn = anso - 1. ;
                while ((Math.abs(pexit - pso) > .001) && (counter < 20)) {
                   ++ counter ;
                   mnsup = getMach(2,(aircor*ath/ansn),rgas,gamma) ;
                   mnsub = getNsmac(mnsup,gamma) ;
                   ptrat = getNspt(mnsup,gamma) ;
                   mexit = getMach(0,(aircor/arat/ptrat),rgas,gamma) ;
                   psn = getPisen(mexit,gamma) * ptrat * pt ;
                   deriv = (psn-pso)/(ansn-anso) ;
                   pso = psn ;
                   anso = ansn ;
                   ansn = anso + (pexit -pso)/deriv ;
                }
                trat = getTisen(mexit,gamma) ;
                uex = mexit * Math.sqrt (gamma * rgas * trat * tt) ;
             }
          }
       }
       npr = 1.0 /prat ;
       uexm = Math.sqrt(2.0*rgas/fac1*tt*(1.0-Math.pow(1.0/nprm,fac1)));
       fgros = mflow * uex / g0 + (pexit - pamb) * athrust * 144. ;

       loadOut() ;
 
       loadGeom() ;

       view.repaint();
    }
 
    public double getGama(double temp, int opt) {
             // Utility to get gamma as a function of temp
       double number,a,b,c,d ;
       a =  -7.6942651e-13;
       b =  1.3764661e-08;
       c =  -7.8185709e-05;
       d =  1.436914;
       if(opt == 0) {
          number = 1.4 ;
       }
       else {
          number = a*temp*temp*temp + b*temp*temp + c*temp +d ;
       }
       return(number) ;
    } 

    public double getMfr(double mach, double gascon, double gam) {
/* Utility to get the corrected weightflow per area given the Mach number */
       double number,fac1,fac2;

       fac2 = (gam+1.0)/(2.0*(gam-1.0)) ;
       fac1 = Math.pow((1.0+.5*(gam-1.0)*mach*mach),fac2);
       number =  20.78 * Math.sqrt(gam/gascon) * mach / fac1 ;

       return(number) ;
    }

     public double getMach (int sub, double corair, double gascon, double gam) {
/* Utility to get the Mach number given the corrected airflow per area */
         double number,chokair,a,b,k;     /* iterate for mach number */
         double deriv,machn,macho,airo,airn;
         int iter ;

         a = (gam -1)/2.0 ;
         b = -(gam+1.0)/(2.0*(gam-1.0)) ;
         k = 20.78 * Math.sqrt(gam/gascon) ;

         chokair = getMfr(1.0,gascon,gam) ;
         if (corair > chokair) {
           number = 1.0 ;
           return (number) ;
         }
         else {
           if (sub == 1) macho = 1.0 ;   /* sonic */
           else {
              if (sub == 2) macho = 1.703 ; /* supersonic */
              else macho = .5;                /* subsonic */
              airo = getMfr(macho,gascon,gam) ;    /* initial guess */
              iter = 1 ;
              machn = macho - .2  ;
              while (Math.abs(corair - airo) > .0001 && iter < 50) {
                 airn =  getMfr(machn,gascon,gam) ;
                 deriv = (airn-airo)/(machn-macho) ;
                 airo = airn ;
                 macho = machn ;
                 machn = macho + (corair - airo)/deriv ;
                 ++ iter ;
              }
           }
           number = macho ;
         }
         return(number) ;
     }

     public double getPisen(double machin, double gam)  {
       /* Utility to get the isentropic pressure ratio given the mach number */
       double number,fac1,gm1,mach1s;
       mach1s = machin*machin ;
       gm1 = gam - 1.0 ;
       fac1 = 1.0 + .5*gm1*mach1s;
       number = Math.pow(1.0/fac1,gam/gm1) ;

       return(number) ;
    }

    public double getTisen(double machin, double gam)  {
      /* Utility to get the isentropic temperature ratio given the mach number*/       double number,gm1,mach1s;
       mach1s = machin*machin ;
       gm1 = gam - 1.0 ;
       number = 1.0 /(1.0 + .5*gm1*mach1s) ;

       return(number) ;
    }

    public double getNsps (double machin, double gam) {
          // NACA 1135 - normal shock relation ps ratio
       double number, gm1, gp1, msq, fac1, fac2 ;

       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       fac2 = (2.0*gam*msq - gm1)/gp1 ;
       number = fac2 ;

       return(number) ;
    }

    public double getNspt (double machin, double gam) {
          // NACA 1135 - normal shock relation pt ratio
       double number, gm1, gp1, msq, fac1, fac2 ;

       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       fac2 = (2.0*gam*msq - gm1)/gp1 ;
       fac1 = (gp1*msq)/(gm1*msq + 2.0) ;
       number = (Math.pow(fac1,(gam/gm1)))
               * (Math.pow((1.0/fac2),(1.0/gm1))) ;

       return(number) ;
    }

    public double getNsmac (double machin, double gam) {
          // NACA 1135 - normal shock relation  mach
       double number, gm1, gp1, msq, fac1, fac2 ;

       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       fac2 = (2.0*gam*msq - gm1)/gp1 ;
       fac1 = (gp1*msq)/(gm1*msq + 2.0) ;
       number = Math.sqrt(msq / (fac2 * fac1)) ;

       return(number) ;
   }

   public void loadOut() {
      String outfor,outair,outvel,outprs,outarea,outisp,outtem  ;

      outfor = " lbs" ;
      if (lunits == 1) outfor = " N" ;
      outair = " pps" ;
      if (lunits == 1) outair = " kg/s" ;
      outvel = " fps" ;
      if (lunits == 1) outvel = " mps" ;
      outprs = " psi" ;
      if (lunits == 1) outprs = " kPa" ;
      outarea = " sq ft" ;
      if (lunits == 1) outarea = " sq m" ;
      outisp = " s" ;
      outtem = " F" ;
      if (lunits == 1) outtem = " C" ;

      if (npr <= 99.99) {
         pn.out.t2.o1.setText(String.valueOf(filter3(npr))) ;
      }
      if (npr >= 99.99) {
         pn.out.t2.o1.setText(String.valueOf(filter0(npr))) ;
      }
      pn.con.l.o2.setText(String.valueOf(filter0(uex * lconv)) + outvel);
      pn.con.l.o10.setText(String.valueOf(filter0(fgros / mflow)) + outisp);
      pn.out.t2.o3.setText(String.valueOf(filter3(machth)));
      pn.out.t4.o5.setText(String.valueOf(filter3(pexit * pconv)) + outprs);
      pn.out.t4.o12.setText(String.valueOf(filter3(pamb * pconv)) + outprs);
      pn.out.t6.o9.setText(String.valueOf(filter3(gamma)));
      pn.out.t6.o11.setText(String.valueOf(filter0(ttin)) + outtem);
      pn.out.t2.o6.setText(String.valueOf(filter3(mexit)));
      pn.out.t5.o7.setText(String.valueOf(filter3(athrust * lconv * lconv)) + outarea);
      pn.out.t5.o13.setText(String.valueOf(filter3(athroat)) + outarea);
      pn.con.l.o8.setText(String.valueOf(filter0(fgros * fconv))+ outfor);
      if ((mflow*mconv1) <= 99.99) {
         pn.con.l.o4.setText(String.valueOf(filter3(mflow * mconv1)) + outair);
      }
      if ((mflow*mconv1) >= 99.99) {
         pn.con.l.o4.setText(String.valueOf(filter0(mflow * mconv1)) + outair);
      }
      if (gamopt == 1) {
         pn.in.chem.r.f4.setText(String.valueOf(filter3(gamma)));
      }
   }

   public void loadInput() {
      int i1, i2, i3, i4, i5, i6 ;
      Double V1, V2, V3, V4, V5, V6 ;
      double v1, v2, v3, v4,  v5, v6 ;
      float fl1, fl2, fl3, fl4, fl5, fl6, fl7, fl8 ;

      i1 = (int) (((athroat - athmn)/(athmx-athmn))*1000.) ;
      i2 = (int) (((arat - aexmn)/(aexmx-aexmn))*1000.) ;
      i3 = (int) (((azrat - azmn)/(azmx-azmn))*1000.) ;
      i4 = (int) (((lngth - .05)/(1.05))*1000.) ;
      i5 = (int) (((ptin - ptmn)/(ptmx - ptmn))*1000.) ;
      i6 = (int) (((ttin - ttmn)/(ttmx - ttmn))*1000.) ;
  
      v1 = i1 * (athmx-athmn) / 1000. + athmn ;
      v2 = i2 * (aexmx-aexmn) / 1000. + aexmn ;
      v3 = i3 * (azmx-azmn) / 1000. + azmn ;
      v5 = i5 * (ptmx-ptmn) / 1000. + ptmn ;
      v6 = i6 * (ttmx-ttmn) / 1000. + ttmn ;
      if (v5 < psin) v5 = psin ;

      fl1 = (float) v1 ;
      fl2 = (float) v2 ;
      fl3 = (float) v3 ;
      fl5 = (float) v5 ;
      fl6 = (float) v6 ;
      fl7 = (float) gamma ;
      fl8 = (float) mweight ;
 
      pn.in.geo.l.f1.setText(String.valueOf(fl1)) ;
      pn.in.geo.l.f2.setText(String.valueOf(fl2)) ;
      pn.in.geo.l.f4.setText(String.valueOf(fl3)) ;
      pn.in.flo.l.f1.setText(String.valueOf(fl5)) ;
      pn.in.flo.l.f2.setText(String.valueOf(fl6)) ;
      pn.in.geo.r.s1.setValue(i1) ;
      pn.in.geo.r.s2.setValue(i2) ;
      pn.in.geo.r.s4.setValue(i3) ;
      pn.in.geo.r.s3.setValue(i4) ;
      pn.in.flo.r.s1.setValue(i5) ;
      pn.in.flo.r.s2.setValue(i6) ;
      pn.in.chem.r.f4.setText(String.valueOf(fl7)) ;
      pn.in.chem.r.f5.setText(String.valueOf(fl8)) ;
      pn.in.chem.r.f6.setText(String.valueOf(fl6)) ;
 
   }

   public float filter3(double inumbr) {
     //  output only to .001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 1000.) ;
       number = (float) (intermed / 1000. );
       return number ;
   }

   public int filter0(double inumbr) {
     //  output only to .
       int number ;

       number = (int) (inumbr) ;
       return number ;
   }
 
   public void setUnits() {   // Switching Units
       double aths,athms,pts,tts,ttmxs,pss,ptmxs,ttcos ;
       int i1,i2,i3,i4 ;
 
       aths = athroat /lconv /lconv ;
       athms = athmx /lconv /lconv ;
       pts  = ptin / pconv ;
       tts  = ttin / tconv ;
       ttmxs  = ttmx / tconv ;
       ttcos  = tcomb / tconv ;
       pss  = psin / pconv ;
       ptmxs  = ptmx / pconv ;

       switch (lunits) {
          case 0: {                             /* English */
            lconv = 1.;                         /*  feet    */
            fconv = 1.0;                        /* pounds   */
            pconv = 1.0  ;                   /* lb/sq in */
            tref =  459.7 ;                 /* zero rankine */
            tconv = 1.0 ;                    /* degrees F */
            mconv1 = 1.0 ;                   /* airflow rate lbs/sec*/
            pn.in.geo.l.li1.setText("Ath sq ft") ;
            pn.in.flo.l.li1.setText("Pto  psi") ;
            pn.in.flo.l.li2.setText("Tto  F") ;
            pn.in.flo.l.li3.setText("Pfs  psi") ;
            pn.in.chem.r.l6.setText("F") ;
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                      /* meters */
            fconv = 4.448 ;                     /* newtons */
            pconv = 6.891 ;               /* kilo-pascals */
            tref = 273.1 ;                /* zero kelvin */
            tconv = 0.555555 ;             /* degrees C */
            mconv1 = .4536 ;                /* kg/sec */
            pn.in.geo.l.li1.setText("Ath sq m") ;
            pn.in.flo.l.li1.setText("Pto  kPa") ;
            pn.in.flo.l.li2.setText("Tto  C") ;
            pn.in.flo.l.li3.setText("Pfs  kPa") ;
            pn.in.chem.r.l6.setText("C") ;
            break ;
          }
       }
 
       athroat = aths * lconv * lconv ;
       athmx = athms * lconv * lconv ;
       ptin = pts * pconv ;
       ttin = tts * tconv ;
       ttmx = ttmxs * tconv ;
       tcomb = ttcos * tconv ;
       psin = pss * pconv ;
       ptmx = ptmxs * pconv ;
       athmn = .01 * lconv * lconv ;
       ptmn = 1. * pconv ;
       pemx = 15. * pconv ;
       pemn = .01 * pconv ;
       ttmn = 32. * tconv ;

       pn.in.geo.l.f1.setText(String.valueOf((float) athroat)) ;
       pn.in.flo.l.f1.setText(String.valueOf((float) ptin)) ;
       pn.in.flo.l.f2.setText(String.valueOf((float) ttin)) ;
       pn.in.flo.l.f3.setText(String.valueOf((float) psin)) ;
       pn.in.chem.r.f6.setText(String.valueOf((float) tcomb)) ;

       i1 = (int) (((athroat - athmn)/(athmx-athmn))*1000.) ;
       i2 = (int) (((ptin - ptmn)/(ptmx-ptmn))*1000.) ;
       i3 = (int) (((ttin - ttmn)/(ttmx-ttmn))*1000.) ;
       i4 = (int) (((psin - pemn)/(pemx-pemn))*1000.) ;

       pn.in.geo.r.s1.setValue(i1) ;
       pn.in.flo.r.s1.setValue(i2) ;
       pn.in.flo.r.s2.setValue(i3) ;
       pn.in.flo.r.s3.setValue(i4) ;

       return ;
    }

    public void loadGeom() {
       double x0,y0,x01,y01,xth,yth,xth1,yth1,xex,yex,xex1,yex1 ;
       int i,j ;

       x0  = 1.0*lngth ;    y0 = rzero ;      // turbojet
       x01 = 20.0*lngth ;   y01 = rzero ;
       xth = 60.0*lngth ;   yth = rthrt ;
       xth1 = 80.0*lngth ;  yth1 = rthrt ;
       xex = 180.0*lngth ;  yex = rexit ;
       xex1 = 200.0*lngth ; yex1 = rexit ;

       if (plntyp == 1) {   // rocket
           y0  = 1.0 ;    x0 = rzero ;
           y01 = 20.0 * .5 ;   x01 = rzero ;
           yth = 60.0 * .5 ;   xth = rthrt ;
           yth1 = 80.0 * .5 ;  xth1 = rthrt ;
           yex = 180.0 * (.5 +lngth) ;  xex = rexit ;
           yex1 = 200.0 * (.5 +lngth) ; xex1 = rexit ;
       }

       for(i=0; i<=5; ++ i) {   //  basic geometry
           xg[0][i] = i * (x01 - x0)/5.0 + x0 ;
           yg[0][i] = i * (y01 - y0)/5.0 + y0 ;
       } 
       for(i=6; i<=14; ++ i) {
           xg[0][i] = (i-5) * (xth - x01)/9.0 + x01 ;
           yg[0][i] = (i-5) * (yth - y01)/9.0 + y01 ;
       } 
       for(i=15; i<=18; ++ i) {
           xg[0][i] = (i-14) * (xth1 - xth)/4.0 + xth ;
           yg[0][i] = (i-14) * (yth1 - yth)/4.0 + yth ;
       } 
       for(i=19; i<=28; ++ i) {
           xg[0][i] = (i-18) * (xex - xth1)/10.0 + xth1 ;
           yg[0][i] = (i-18) * (yex - yth1)/10.0 + yth1 ;
       } 
       for(i=29; i<=34; ++ i) {
           xg[0][i] = (i-28) * (xex1 - xex)/3.0 + xex ;
           yg[0][i] = (i-28) * (yex1 - yex)/3.0 + yex ;
       } 

       for (j=1; j<=5; ++ j) {  // lower flow
           for(i=0; i<=34; ++ i) {
              if (plntyp == 0) {   // turbojet
                  xg[j][i] = xg[0][i] ;
                  yg[j][i] = (1.0 - .2 * j) * yg[0][i] ;
              }
              if (plntyp == 1) {   // rocket
                  xg[j][i] = (1.0 - .2 * j) * xg[0][i] ;
                  yg[j][i] = yg[0][i] ;
              }
           }
       }

       for (j=6; j<=10; ++ j) {  // mirror
           for(i=0; i<=34; ++ i) {
              if (plntyp == 0) {   // turbojet
                  xg[j][i] = xg[0][i] ;
                  yg[j][i] = -yg[10-j][i] ;
              }
              if (plntyp == 1) {   // rocket
                  xg[j][i] = -xg[10-j][i] ;
                  yg[j][i] = yg[0][i] ;
              }
           }
       }
   }

   class Pn extends Panel {
        Nozzle outerparent ;
        Con con ;
        Out out ;
        In in ;

        Pn (Nozzle target) {

           outerparent = target ;
           setLayout(new GridLayout(3,1,5,5)) ;

           con = new Con(outerparent) ;
           in = new In(outerparent) ;
           out = new Out(outerparent) ;

           add(con) ;
           add(out) ;
           add(in) ;
        }

        class Con extends Panel {
           Nozzle outerparent ;
           U u ;
           L l ;
        
           Con (Nozzle target) {

               outerparent = target ;
               setLayout(new GridLayout(2,1,5,5)) ;

               u = new U(outerparent) ;
               l = new L(outerparent) ;

               add(u) ;
               add(l) ;
            }
 
            class U extends Panel {  //upper
                 Nozzle outerparent ;
                 Label l2 ;
                 Button b3 ;
                 Choice vhkl,untch ;
     
                 U (Nozzle target) {
    
                    outerparent = target ;
                    setLayout(new GridLayout(2,2,5,5)) ;
 
                    l2 = new Label("Nozzle Simulator", Label.CENTER) ;
                    l2.setForeground(Color.red) ;
 
                    b3 = new Button("Reset") ;
                    b3.setBackground(Color.red) ;
                    b3.setForeground(Color.white) ;

                    vhkl = new Choice() ;
                    vhkl.addItem("Turbine Converge") ;
                    vhkl.addItem("Turbine Con-Diverge") ;
                    vhkl.addItem("Rocket");
                    vhkl.select(1) ;
 
                    untch = new Choice() ;
                    untch.addItem("English Units") ;
                    untch.addItem("Metric Units");
                    untch.select(0) ;
     
                    add(l2) ;
                    add(b3) ;
    
                    add(vhkl) ;
                    add(untch) ;
                 }
    
                 public boolean action(Event evt, Object arg) {
                     String label = (String)arg ;
                     int pix;
     
                     if (evt.target instanceof Choice) {
                         pix = vhkl.getSelectedIndex() ;
                         lunits = untch.getSelectedIndex() ;

                         setUnits() ;
                         if (pix == 0) {
                            plntyp = 0 ;
                            noztyp = 0 ;
                         }
                         if (pix == 1) {
                            plntyp = 0 ;
                            noztyp = 1 ;
                         }
                         if (pix == 2) {
                            plntyp = 1 ;
                            noztyp = 1 ;
                         }
    
                         if (plntyp != plntpold) {
                           if(plntyp == 0) {  // limits for aircraft
                              athmx   = 3.0 * lconv * lconv ;
                              athroat = 2.0 * lconv * lconv ;
                              aexmx = 2.0  ;
                              arat = 1.6 ;
                              azmx = 2.0  ;
                              azrat = 1.5 ;
                              ptmx = 200. * pconv ;
                              ptin = 70.  * pconv ;
                              ttmx = 4000. * tconv ;
                              ttin = 2500. * tconv ;
                           }
                           if(plntyp == 1) {  // limits for rocket
                              athmx   = 2.0 * lconv * lconv  ;
                              athroat = 1.0 * lconv * lconv ;
                              aexmx = 100.0 ;
                              arat = 50.0 ;
                              azmx = 10.0  ;
                              azrat = 4.0 ;
                              ptmx = 3000. * pconv ;
                              ptin = 2000. * pconv ;
                              ttmx = 6500. * tconv ;
                              ttin = 4000. * tconv ;
                           }
                           plntpold = plntyp ;
    
                           loadInput() ;
                         }
     
                         comPute() ;
                         return true ;
                     }
    
                     if(evt.target instanceof Button) {
                        if(label.equals("Reset")) {
                           setDefaults() ;
                           layout.show(in, "first") ;
                           vhkl.select(2) ;
                           untch.select(0) ;
                           out.t3.inpch.select(0) ;
                           in.chem.l.gamch.select(1) ;
                           in.chem.l.propch.select(2) ;
                           in.chem.l.tcomch.select(1) ;
                           in.chem.r.f4.setBackground(Color.black) ;
                           in.chem.r.f4.setForeground(Color.yellow) ;
                           in.chem.r.f5.setBackground(Color.black) ;
                           in.chem.r.f5.setForeground(Color.yellow) ;
                           in.chem.r.f6.setBackground(Color.black) ;
                           in.chem.r.f6.setForeground(Color.yellow) ;
    
                           setUnits() ;
                           loadInput() ;
    
                           comPute() ;
                        } 
    
                        return true ;
                     }

                     else return false ;
                 }
            }  // end U

            class L extends Panel {  // lower
                 Nozzle outerparent ;
                 TextField o2, o10, o4, o8 ;
     
                 L (Nozzle target) {
    
                    outerparent = target ;
                    setLayout(new GridLayout(2,4,5,5)) ;
 
                    o2 = new TextField() ;
                    o2.setBackground(Color.black) ;
                    o2.setForeground(Color.green) ;

                    o10 = new TextField() ;
                    o10.setBackground(Color.black) ;
                    o10.setForeground(Color.green) ;
      
                    o4 = new TextField() ;
                    o4.setBackground(Color.black) ;
                    o4.setForeground(Color.green) ;
          
                    o8 = new TextField() ;
                    o8.setBackground(Color.black) ;
                    o8.setForeground(Color.green) ;
       
                    add(new Label("Flow", Label.RIGHT)) ;
                    add(o4) ;
                    add(new Label("Thrust", Label.RIGHT)) ;
                    add(o8) ;

                    add(new Label("Uex", Label.RIGHT)) ;
                    add(o2) ;
                    add(new Label("Isp ", Label.RIGHT)) ;
                    add(o10) ;
                 }
            }  // end L
        }  // end Con

        class In extends Panel {
             Nozzle outerparent ;
             Geo geo ;
             Flo flo ;
             Chem chem ;
     
             In (Nozzle target) {

                outerparent = target ;
                layout = new CardLayout() ;
                setLayout(layout) ;

                geo = new Geo(outerparent) ;
                flo = new Flo(outerparent) ;
                chem = new Chem(outerparent) ;

                add ("first", geo) ;
                add ("second", flo) ;
                add ("third", chem) ;
     
             }
 
             class Geo extends Panel {
                Nozzle outerparent ;
                L l ;
                R r ;
        
                Geo (Nozzle target) {

                   outerparent = target ;
                   setLayout(new GridLayout(1,2,5,5)) ;
 
                   l = new L(outerparent) ;
                   r = new R(outerparent) ;

                   add(l) ;
                   add(r) ;
                }
 
                class L extends Panel {
                      Nozzle outerparent ;
                      TextField f1,f2,f4;
                      Label li1,li2,li4 ;
       
                      L (Nozzle target) {
       
                        outerparent = target ;
                        setLayout(new GridLayout(4,2,2,5)) ;

                        f1 = new TextField(String.valueOf(athroat),5) ;
                        li1 = new Label("Ath sq ft", Label.CENTER) ;

                        f2 = new TextField(String.valueOf(arat),5) ;
                        li2 = new Label("Aex/Ath", Label.CENTER) ;

                        f4 = new TextField(String.valueOf(azrat),5) ;
                        li4 = new Label("Ao/Ath", Label.CENTER) ;

                        add(li1) ;
                        add(f1) ;

                        add(li2) ;
                        add(f2) ;

                        add(li4) ;
                        add(f4) ;

                        add(new Label(" ", Label.LEFT)) ;
                        add(new Label("Length", Label.RIGHT)) ;
                    }
 
                    public boolean handleEvent(Event evt) {
                        Double V1, V2, V4 ;
                        double v1, v2, v4 ;
                        float fl1 ;
                        int i1, i2, i4 ;

                        if(evt.id == Event.ACTION_EVENT) {
                        // Throat area
                           V1 = Double.valueOf(f1.getText()) ;
                           v1 = V1.doubleValue() ;
                           if(v1 < athmn) {
                              v1 = athmn ;
                              fl1 = (float) v1 ;
                              f1.setText(String.valueOf(fl1)) ;
                           }
                           if(v1 > athmx) {
                              v1 = athmx ;
                              fl1 = (float) v1 ;
                              f1.setText(String.valueOf(fl1)) ;
                           }
                        // Exit area ratio
                           V2 = Double.valueOf(f2.getText()) ;
                           v2 = V2.doubleValue() ;
                           if(v2 < aexmn) {
                              v2 = aexmn ;
                              fl1 = (float) v2 ;
                              f2.setText(String.valueOf(fl1)) ;
                           }
                           if(v2 > aexmx) {
                              v2 = aexmx ;
                              fl1 = (float) v2 ;
                              f2.setText(String.valueOf(fl1)) ;
                           }
                        // Plenum area ratio
                           V4 = Double.valueOf(f4.getText()) ;
                           v4 = V4.doubleValue() ;
                           if(v4 < azmn) {
                              v4 = azmn ;
                              fl1 = (float) v4 ;
                              f4.setText(String.valueOf(fl1)) ;
                           }
                           if(v4 > azmx) {
                              v4 = azmx ;
                              fl1 = (float) v4 ;
                              f4.setText(String.valueOf(fl1)) ;
                           }
 
                           athroat = v1 ;
                           arat = v2 ;
                           azrat = v4 ;
                          
                           i1 = (int) (((v1 - athmn)/(athmx-athmn))*1000.) ;
                           i2 = (int) (((v2 - aexmn)/(aexmx-aexmn))*1000.) ;
                           i4 = (int) (((v4 - azmn)/(azmx-azmn))*1000.) ;

                           r.s1.setValue(i1) ;
                           r.s2.setValue(i2) ;
                           r.s4.setValue(i4) ;

                           comPute() ;

                           return true ;
                        }
                        else return false ;
                    }
                }
 
                class R extends Panel {
                     Nozzle outerparent ;
                     Scrollbar s1, s2, s3, s4 ;

                     R (Nozzle target) {
                        int i1,i2,i3,i4 ;

                        outerparent = target ;
                        setLayout(new GridLayout(4,1,5,5)) ;

                        i1 = (int) ((((double)athroat - athmn)/(athmx-athmn))
                                       *1000.) ;
                        s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);

                        i2 = (int) ((((double)arat - aexmn)/(aexmx-aexmn))*1000.);
                        s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
 
                        i3 = (int) (((.5 - .05)/(1.05))*1000.) ;
                        s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

                        i4 = (int) ((((double)azrat - azmn)/(azmx-azmn))*1000.);
                        s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);

                        add(s1) ;
                        add(s2) ;
                        add(s4) ;
                        add(s3) ;
                    }

                    public boolean handleEvent(Event evt) {
                         if(evt.id == Event.ACTION_EVENT) {
                            this.handleBar(evt) ;
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
                        int i1, i2, i3, i4 ;
                        Double V1, V2, V4 ;
                        double v1, v2, v4 ;
                        float fl1, fl2, fl4 ;

                        i1 = s1.getValue() ;
                        i2 = s2.getValue() ;
                        i4 = s4.getValue() ;
                        i3 = s3.getValue() ;

                        v1 = i1 * (athmx-athmn) / 1000. + athmn ;
                        v2 = i2 * (aexmx-aexmn) / 1000. + aexmn ;
                        v4 = i4 * (azmx-azmn) / 1000. + azmn ;

                        fl1 = (float) v1 ;
                        fl2 = (float) v2 ;
                        fl4 = (float) v4 ;

                        athroat = v1 ;
                        arat = v2 ;
                        azrat = v4 ;
                        lngth = i3 * (1.05)/ 1000. + .05 ;

                        l.f1.setText(String.valueOf(fl1)) ;
                        l.f2.setText(String.valueOf(fl2)) ;
                        l.f4.setText(String.valueOf(fl4)) ;
                        
                        comPute() ;
                     }
                 }
             }

             class Flo extends Panel {
                Nozzle outerparent ;
                L l ;
                R r ;
        
                Flo (Nozzle target) {

                   outerparent = target ;
                   setLayout(new GridLayout(1,2,5,5)) ;
 
                   l = new L(outerparent) ;
                   r = new R(outerparent) ;

                   add(l) ;
                   add(r) ;
                }

                class L extends Panel {
                      Nozzle outerparent ;
                      TextField f1,f2,f3;
                      Label li1,li2,li3 ;
       
                      L (Nozzle target) {
       
                        outerparent = target ;
                        setLayout(new GridLayout(4,2,2,5)) ;

                        f1 = new TextField(String.valueOf(ptin),5) ;
                        li1 = new Label("Pto psi", Label.CENTER) ;

                        f2 = new TextField(String.valueOf(ttin),5) ;
                        li2 = new Label("Tto  F", Label.CENTER) ;

                        f3 = new TextField(String.valueOf(psin),5) ;
                        li3 = new Label("Pfs psi", Label.CENTER) ;

                        add(li1) ;
                        add(f1) ;

                        add(li2) ;
                        add(f2) ;

                        add(li3) ;
                        add(f3) ;

                        add(new Label(" ", Label.RIGHT)) ;
                        add(new Label(" ", Label.RIGHT)) ;
                    }
 
                    public boolean handleEvent(Event evt) {
                        Double V1, V2, V3 ;
                        double v1, v2, v3 ;
                        float fl1 ;
                        int i1, i2, i3 ;

                        if(evt.id == Event.ACTION_EVENT) {
                       // total pressure
                           V1 = Double.valueOf(f1.getText()) ;
                           v1 = V1.doubleValue() ;
                           if(v1 < ptmn) {
                              v1 = ptmn ;
                              fl1 = (float) v1 ;
                              f1.setText(String.valueOf(fl1)) ;
                           }
                           if(v1 > ptmx) {
                              v1 = ptmx ;
                              fl1 = (float) v1 ;
                              f1.setText(String.valueOf(fl1)) ;
                           }
                           if(v1 < psin) {
                              v1 = psin ;
                              fl1 = (float) v1 ;
                              f1.setText(String.valueOf(fl1)) ;
                           }
                        // total temperature
                           V2 = Double.valueOf(f2.getText()) ;
                           v2 = V2.doubleValue() ;
                           if(v2 < ttmn) {
                              v2 = ttmn ;
                              fl1 = (float) v2 ;
                              f2.setText(String.valueOf(fl1)) ;
                           }
                           if(v2 > ttmx) {
                              v2 = ttmx ;
                              fl1 = (float) v2 ;
                              f2.setText(String.valueOf(fl1)) ;
                           }
                       // exit pressure
                           V3 = Double.valueOf(f3.getText()) ;
                           v3 = V3.doubleValue() ;
                           if(v3 < pemn) {
                              v3 = pemn ;
                              fl1 = (float) v3 ;
                              f3.setText(String.valueOf(fl1)) ;
                           }
                           if(v3 > pemx) {
                              v3 = pemx ;
                              fl1 = (float) v3 ;
                              f3.setText(String.valueOf(fl1)) ;
                           }
                           if(v3 > v1) {
                              v3 = v1 ;
                              fl1 = (float) v3 ;
                              f3.setText(String.valueOf(fl1)) ;
                           }
 
                           ptin = v1 ;
                           ttin = v2 ;
                           psin = v3 ;
                          
                           i1 = (int) (((v1 - ptmn)/(ptmx-ptmn))*1000.) ;
                           i2 = (int) (((v2 - ttmn)/(ttmx-ttmn))*1000.) ;
                           i3 = (int) (((v3 - pemn)/(pemx-pemn))*1000.) ;

                           r.s1.setValue(i1) ;
                           r.s2.setValue(i2) ;
                           r.s3.setValue(i3) ;

                           comPute() ;

                           return true ;
                        }
                        else return false ;
                    }
                }

                class R extends Panel {
                     Nozzle outerparent ;
                     Scrollbar s1, s2, s3 ;
                     TextField f4;

                     R (Nozzle target) {
                        int i1,i2, i3 ;

                        outerparent = target ;
                        setLayout(new GridLayout(4,1,5,5)) ;

                        i1 = (int) ((((double)ptin - ptmn)/(ptmx-ptmn))*1000.) ;
                        i2 = (int) ((((double)ttin - ttmn)/(ttmx-ttmn))*1000.) ;
                        i3 = (int) ((((double)psin - pemn)/(pemx-pemn))*1000.) ;

                        s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
                        s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
                        s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

                        add(s1) ;
                        add(s2) ;
                        add(s3) ;
                        add(new Label(" ", Label.RIGHT)) ;
                    }

                    public boolean handleEvent(Event evt) {
                        Double V2 ;
                        double v2 ;
                        float fl1 ;

                         if(evt.id == Event.ACTION_EVENT) {
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
                        int i1, i2, i3 ;
                        Double V1, V2, V3 ;
                        double v1, v2, v3 ;
                        float fl1, fl2, fl3 ;

                        i1 = s1.getValue() ;
                        i2 = s2.getValue() ;
                        i3 = s3.getValue() ;

                        v1 = i1 * (ptmx-ptmn) / 1000. + ptmn ;
                        if (v1 < psin) v1 = psin ;
                        v3 = i3 * (pemx-pemn) / 1000. + pemn ;
                        if (v3 > v1) v3 = v1 ;
                        v2 = i2 * (ttmx-ttmn) / 1000. + ttmn ;

                        fl1 = (float) v1 ;
                        fl2 = (float) v2 ;
                        fl3 = (float) v3 ;

                        ptin = v1 ;
                        ttin = v2 ;
                        psin = v3 ;
 
                        l.f1.setText(String.valueOf(fl1)) ;
                        l.f2.setText(String.valueOf(fl2)) ;
                        l.f3.setText(String.valueOf(fl3)) ;
                        
                        comPute() ;
                     }
                 }
             }

             class Chem extends Panel {
                Nozzle outerparent ;
                L l ;
                R r ;
        
                Chem (Nozzle target) {

                   outerparent = target ;
                   setLayout(new GridLayout(1,2,5,5)) ;
 
                   l = new L(outerparent) ;
                   r = new R(outerparent) ;

                   add(l) ;
                   add(r) ;
                }

                class L extends Panel {
                   Nozzle outerparent ;
                   Choice gamch,propch,tcomch ;
       
                   L (Nozzle target) {
       
                        outerparent = target ;
                        setLayout(new GridLayout(4,2,2,5)) ;

                        gamch = new Choice() ;
                        gamch.addItem("Input") ;
                        gamch.addItem("Gam(T)");
                        gamch.select(1) ;

                        propch = new Choice() ;
                        propch.addItem("Input") ;
                        propch.addItem("Air") ;
                        propch.addItem("H2-O2");
                        propch.addItem("JP 10-O2");
                        propch.select(1) ;

                        tcomch = new Choice() ;
                        tcomch.addItem("Input") ;
                        tcomch.addItem("Table");
                        tcomch.select(1) ;

                        add(new Label(" ", Label.RIGHT)) ;
                        add(new Label("Typical ", Label.RIGHT)) ;

                        add(new Label("Mol. Wt.", Label.CENTER)) ;
                        add(propch) ;

                        add(new Label("Gamma:", Label.CENTER)) ;
                        add(gamch) ;

                        add(new Label("T combust", Label.RIGHT)) ;
                        add(tcomch) ;
                   }
 
                   public boolean handleEvent(Event evt) {
                        int i6 ;
                        double v6 ;
                        float fl6 ;
                        
                        if(evt.id == Event.ACTION_EVENT) {
                          gasopt  = propch.getSelectedIndex() ;

                          if (gasopt == 0) {  // input
                             r.f5.setBackground(Color.white) ;
                             r.f5.setForeground(Color.black) ;
                          }
                          if (gasopt >= 1) {  // specified
                             r.f5.setBackground(Color.black) ;
                             r.f5.setForeground(Color.yellow) ;
                          }

                          if (gasopt == 1) {  // air
                             mweight = 29.0 ;
                             gam0 = 1.4 ;
                             tcomb = 4000. * tconv ;
                          }
                          if (gasopt == 2) {  // H2-O2 - steam
                             mweight = 13.0 ;
                             gam0 = 1.3 ;
                             tcomb = 5870. * tconv ;
                          }
                          if (gasopt == 3) {  // JP
                             mweight = 25.0 ;
                             gam0 = 1.25 ;
                             tcomb = 5770. * tconv ;
                          }
     
                          gamopt  = gamch.getSelectedIndex() ;

                          if (gamopt == 0) {
                             r.f4.setBackground(Color.white) ;
                             r.f4.setForeground(Color.black) ;
                             gamma = gam0 ;
                          }

                          if (gamopt == 1) {
                             r.f4.setBackground(Color.black) ;
                             r.f4.setForeground(Color.yellow) ;
                          }
 
                          tcomopt = tcomch.getSelectedIndex() ;
   
                          if (tcomopt == 0) {
                             r.f6.setBackground(Color.white) ;
                             r.f6.setForeground(Color.black) ;
                          }

                          if (tcomopt == 1) {
                             r.f6.setBackground(Color.black) ;
                             r.f6.setForeground(Color.yellow) ;
                          }
 
                          ttin = tcomb ;
                          if (plntyp == 0) ttin = 2500 * tconv;
                          i6 = (int) (((ttin - ttmn)/(ttmx - ttmn))*1000.) ;
                          v6 = i6 * (ttmx-ttmn) / 1000. + ttmn ;
                          fl6 = (float) v6 ;
                          pn.in.flo.l.f2.setText(String.valueOf(fl6)) ;
                          pn.in.flo.r.s2.setValue(i6) ;

                          r.f6.setText(String.valueOf(tcomb)) ;
                          r.f5.setText(String.valueOf(mweight)) ;
                          r.f4.setText(String.valueOf(gam0)) ;

                          comPute();
                          return true ;
                        }
  
                        else  return false;
                    }
                }

                class R extends Panel {
                     Nozzle outerparent ;
                     TextField f4,f5,f6;
                     Label l6;

                     R (Nozzle target) {
                        int i1,i2, i3 ;

                        outerparent = target ;
                        setLayout(new GridLayout(4,2,5,5)) ;

                        f4 = new TextField(String.valueOf((float)gamma),5) ;
                        f4.setBackground(Color.black) ;
                        f4.setForeground(Color.yellow) ;

                        f5 = new TextField(String.valueOf((float)mweight),5) ;
                        f5.setBackground(Color.black) ;
                        f5.setForeground(Color.yellow) ;

                        f6 = new TextField(String.valueOf((float)tcomb),5) ;
                        f6.setBackground(Color.black) ;
                        f6.setForeground(Color.yellow) ;

                        l6 = new Label("F", Label.LEFT) ;

                        add(new Label(" Values", Label.LEFT)) ;
                        add(new Label(" ", Label.RIGHT)) ;

                        add(f5) ;
                        add(new Label(" ", Label.RIGHT)) ;

                        add(f4) ;
                        add(new Label(" ", Label.RIGHT)) ;

                        add(f6) ;
                        add(l6) ;
                    }

                    public boolean handleEvent(Event evt) {
                        Double V4,V5,V6 ;
                        double v4,v5,v6 ;
                        float fl1 ;
                        int i1, i2, i3 ;
                        
                        if(evt.id == Event.ACTION_EVENT) {
                          if (gamopt == 0) {

                             V4 = Double.valueOf(f4.getText()) ;
                             v4 = V4.doubleValue() ;
   
                             gamma = v4 ;
                             if(v4 < 1.0) {
                                gamma = v4 =  1.0 ;
                                fl1 = (float) v4 ;
                                f4.setText(String.valueOf(fl1)) ;
                             }
                             if(v4 > 2.0) {
                                gamma = v4 = 2.0 ;
                                fl1 = (float) v4 ;
                                f4.setText(String.valueOf(fl1)) ;
                             }
                          }

                          if (gasopt == 0) {

                             V5 = Double.valueOf(f5.getText()) ;
                             v5 = V5.doubleValue() ;
   
                             mweight = v5 ;
                             if(v5 < 1.0) {
                                mweight = v5 =  1.0 ;
                                fl1 = (float) v5 ;
                                f5.setText(String.valueOf(fl1)) ;
                             }
                             if(v5 > 50.0) {
                                mweight = v5 = 50.0 ;
                                fl1 = (float) v5 ;
                                f5.setText(String.valueOf(fl1)) ;
                             }
                          }

                          if (tcomopt == 0) {

                             V6 = Double.valueOf(f6.getText()) ;
                             v6 = V6.doubleValue() ;
   
                             tcomb = v6 ;
                             if(v6 < ttmn) {
                                tcomb = v6 =  ttmn ;
                                fl1 = (float) v6 ;
                                f6.setText(String.valueOf(fl1)) ;
                             }
                             if(v6 > ttmx) {
                                tcomb = v6 = ttmx ;
                                fl1 = (float) v6 ;
                                f6.setText(String.valueOf(fl1)) ;
                             }

                             ttin = tcomb ;
                             if (plntyp == 0) ttin = 2500 * tconv;
                             i3 = (int) (((ttin - ttmn)/(ttmx - ttmn))*1000.) ;
                             fl1 = (float) v6 ;
                             pn.in.flo.l.f2.setText(String.valueOf(fl1)) ;
                             pn.in.flo.r.s2.setValue(i3) ;

                          }

                          comPute();
                          return true ;
                        }
  
                        else  return false;
                    }
                 }
             }
        }

        class Out extends Panel {
           Nozzle outerparent ;
           T2 t2 ;
           T3 t3 ;
           T4 t4 ;
           T5 t5 ;
           T6 t6 ;
    
           Out (Nozzle target) {

              outerparent = target ;
              setLayout(new GridLayout(5,1,2,2)) ;
 
              t2 = new T2(outerparent) ;
              t3 = new T3(outerparent) ;
              t4 = new T4(outerparent) ;
              t6 = new T6(outerparent) ;
              t5 = new T5(outerparent) ;
     
              add(t4) ;
              add(t5) ;
              add(t6) ;
              add(t2) ;
              add(t3) ;
           }
 
           class T6 extends Panel {
             Nozzle outerparent ;
             TextField o9,o11,diag3,diag4 ;

             T6 (Nozzle target) {
      
                outerparent = target ;
                setLayout(new GridLayout(1,4,2,2)) ;
    
                o9 = new TextField() ;
                o9.setBackground(Color.black) ;
                o9.setForeground(Color.green) ;
                o11 = new TextField() ;
                o11.setBackground(Color.black) ;
                o11.setForeground(Color.green) ;
                diag3 = new TextField() ;
                diag4 = new TextField() ;
      
                add(new Label("Tt0", Label.RIGHT)) ;
                add(o11) ;
                add(new Label("Gamma", Label.RIGHT)) ;
                add(o9) ;
             //   add(diag3) ;
             //   add(diag4) ;
             }
           }

           class T2 extends Panel {
             Nozzle outerparent ;
             TextField o1, o3, o6 ;

             T2 (Nozzle target) {
      
                outerparent = target ;
                setLayout(new GridLayout(1,6,2,2)) ;
    
                o1 = new TextField() ;
                o1.setBackground(Color.black) ;
                o1.setForeground(Color.green) ;
                o3 = new TextField() ;
                o3.setBackground(Color.black) ;
                o3.setForeground(Color.green) ;
                o6 = new TextField() ;
                o6.setBackground(Color.black) ;
                o6.setForeground(Color.green) ;
      
                add(new Label("NPR", Label.RIGHT)) ;
                add(o1) ;
                add(new Label("Mth", Label.RIGHT)) ;
                add(o3) ;
                add(new Label("Mex", Label.RIGHT)) ;
                add(o6) ;
             }
           }

           class T3 extends Panel {
              Nozzle outerparent ;
              Label l1 ;
              Choice inpch ;

              T3 (Nozzle target) {
      
                  outerparent = target ;
                  setLayout(new GridLayout(1,2,2,2)) ;
    
                  l1 = new Label("Input Panel:", Label.RIGHT) ;
                  l1.setForeground(Color.blue) ;
 
                  inpch = new Choice() ;
                  inpch.addItem("Geometry") ;
                  inpch.addItem("Flow");
                  inpch.addItem("Propellants");
                  inpch.select(0) ;
 
                  add(l1) ;
                  add(inpch) ;
              }

              public boolean action(Event evt, Object arg) {
                  String label = (String)arg ;
                  int  whichin ;
     
                  if (evt.target instanceof Choice) {
                      whichin = inpch.getSelectedIndex() ;
     
                      if (whichin == 0) {
                         layout.show(in, "first") ;
                      }
                      if (whichin == 1) {
                         layout.show(in, "second") ;
                      }
                      if (whichin == 2) {
                         layout.show(in, "third") ;
                      }
 
                      comPute() ;
                      return true ;
                  }
                  else return false ;
              }
           }

           class T4 extends Panel {
             Nozzle outerparent ;
             TextField o5, o12 ;

             T4 (Nozzle target) {
      
                outerparent = target ;
                setLayout(new GridLayout(1,4,2,2)) ;
    
                o5 = new TextField() ;
                o5.setBackground(Color.black) ;
                o5.setForeground(Color.green) ;
                o12 = new TextField() ;
                o12.setBackground(Color.black) ;
                o12.setForeground(Color.green) ;
      
                add(new Label("Pex", Label.RIGHT)) ;
                add(o5) ;
                add(new Label("Pfs", Label.RIGHT)) ;
                add(o12) ;
             }
           }

           class T5 extends Panel {
             Nozzle outerparent ;
             TextField o7,o13 ;

             T5 (Nozzle target) {
      
                outerparent = target ;
                setLayout(new GridLayout(1,4,2,2)) ;
    
                o7 = new TextField() ;
                o7.setBackground(Color.black) ;
                o7.setForeground(Color.green) ;
                o13 = new TextField() ;
                o13.setBackground(Color.black) ;
                o13.setForeground(Color.green) ;

                add(new Label("Aex", Label.RIGHT)) ;
                add(o7) ;
                add(new Label("Ath", Label.RIGHT)) ;
                add(o13) ;
             }
           }
        }
  }

  class Viewer extends Canvas 
         implements Runnable{
     Nozzle outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Nozzle target) {
         setBackground(Color.white) ;
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
         if (y >= 30) {
             if (x >= 30 ) {   // translate
                locate = new Point(x,y) ;
                yt =  yt + (int) (.2*(locate.y - anchor.y)) ;
                xt =  xt + (int) (.4*(locate.x - anchor.x))  ;
                if (xt > 320) xt = 320 ;
                if (xt < -280) xt = -280 ;
                if (yt > 300) yt = 300 ;
                if (yt <-300) yt = -300 ;
             }
             if (x < 30 ) {   // zoom widget
               sldloc = y ;
               if (sldloc < 30) sldloc = 30;
               if (sldloc > 165) sldloc = 165;
               fact = 10.0 + (sldloc-30)*1.0 ;
             }
         }
     }
     public void handleb(int x, int y) {
         if (y < 15) {
             if (x <= 30) {   //find
                  xt = 0;  yt = 0; fact = 50.0 ;
                  sldloc = 70 ;
             }
         }
         view.repaint() ;
     }

     public void start() {
        if (runner == null) {
           runner = new Thread(this) ;
           runner.start() ;
        }
        antim = 0 ;                
        ancol = 1 ;                        
     }

     public void run() {
       int timer ;

       timer = 100 ;
       while (true) {
          ++ antim ;
          try { Thread.sleep(timer); }
          catch (InterruptedException e) {}
          view.repaint() ;
          if (antim == 3) {
             antim = 0;
             ancol = - ancol ;       
          }
       }
     }

     public void update(Graphics g) {
         view.paint(g) ;
     }

     public void paint(Graphics g) {

       int exes[] = new int[10] ;
       int whys[] = new int[10] ;
       int yorgn = 170 ;
       int xorgn = 150 ;
       int i,j ;
       int npts ;

       offsGg.setColor(Color.black) ;
       offsGg.fillRect(0,0,500,900) ;
       
       if (plntyp == 0) {      // turbojet nozzle
          xorgn = 40 ;
          yorgn = 170 ;
       }
       if (plntyp == 1) {   // rocket
          xorgn = 150 ;
          yorgn = 0 ;
       }

       npts = 30 ;
       if (noztyp == 0) npts = 18 ;

   /*  geometry */
       exes[1] = xorgn + (int) (.05*fact*xg[0][0]) + xt ;
       whys[1] = yorgn + (int) (.05*fact*yg[0][0]) + yt ;
       if (plntyp == 0 ) {
         exes[2] = exes[1] ;
         whys[2] = whys[1] + 5 ;
       }
       if (plntyp == 1 ) {
         exes[2] = exes[1] + 5 ;
         whys[2] = whys[1] ;
       }
       for (i=1 ; i<= npts; ++i) { 
          exes[0] = exes[1] ;
          whys[0] = whys[1] ;
          exes[1] = xorgn + (int) (.05*fact*xg[0][i]) + xt ;
          whys[1] = yorgn + (int) (.05*fact*yg[0][i]) + yt ;
          exes[3] = exes[2] ;
          whys[3] = whys[2] ;
          if (plntyp == 0 ) {
            exes[2] = exes[1] ;
            whys[2] = whys[1] + 5 ;
               /* labels */
            if (i == 15) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Free Stream - fs",exes[2],whys[2]+50) ;
            }
          }
          if (plntyp == 1 ) {
            exes[2] = exes[1] + 5 ;
            whys[2] = whys[1] ;
               /* labels */
            if (i == 15) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Free Stream - fs",exes[2]+10,whys[2]+10) ;
            }
            if (i == npts) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Exit-ex",exes[2],whys[2]+20) ;
            }
          }
          offsGg.setColor(Color.white) ;
          offsGg.fillPolygon(exes,whys,4) ;
       }

       exes[1] = xorgn + (int) (.05*fact*xg[10][0]) + xt ;
       whys[1] = yorgn + (int) (.05*fact*yg[10][0]) + yt ;

       if (plntyp == 0 ) {
         exes[2] = exes[1] ;
         whys[2] = whys[1] - 5 ;
         /* labels */
         offsGg.setColor(Color.white) ;
         offsGg.drawString("Plenum-0",exes[2],whys[2]-20) ;
       }
       if (plntyp == 1 ) {
         exes[2] = exes[1] - 5 ;
         whys[2] = whys[1] ;
         /* labels */
         offsGg.setColor(Color.white) ;
         offsGg.drawString("Plenum-0",exes[2]-65,whys[2]+20) ;
       }
       for (i=1 ; i<= npts; ++i) { 
          exes[0] = exes[1] ;
          whys[0] = whys[1] ;
          exes[1] = xorgn + (int) (.05*fact*xg[10][i]) + xt ;
          whys[1] = yorgn + (int) (.05*fact*yg[10][i]) + yt ;
          exes[3] = exes[2] ;
          whys[3] = whys[2] ;
          if (plntyp == 0 ) {
            exes[2] = exes[1] ;
            whys[2] = whys[1] - 5 ;
               /* labels */
            if (i == 15) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Throat-th",exes[2],whys[2]-20) ;
            }
            if (i == npts) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Exit-ex",exes[2]-30,whys[2]-10) ;
            }
          }
          if (plntyp == 1 ) {
            exes[2] = exes[1] - 5 ;
            whys[2] = whys[1] ;
               /* labels */
            if (i == 15) {
               offsGg.setColor(Color.white) ;
               offsGg.drawString("Throat-th",exes[2]-60,whys[2]+10) ;
            }
          }
          offsGg.setColor(Color.white) ;
          offsGg.fillPolygon(exes,whys,4) ;
       }

    /* animated flow */
       npts = 34 ;
       for (j=1 ; j<=9 ; ++ j) {  
          exes[1] = xorgn + (int) (.05*fact*xg[j][0]) + xt ;
          whys[1] = yorgn + (int) (.05*fact*yg[j][0]) + yt ;
          for (i=1 ; i<= npts; ++i) { 
             exes[0] = exes[1] ;
             whys[0] = whys[1] ;
             exes[1] = xorgn + (int) (.05*fact*xg[j][i]) + xt ;
             whys[1] = yorgn + (int) (.05*fact*yg[j][i]) + yt ;
             if ((i-antim)/3*3 == (i-antim)) {
               if (ancol == -1) {         
                 if((i-antim)/6*6 == (i-antim))offsGg.setColor(Color.yellow);
                 if((i-antim)/6*6 != (i-antim))offsGg.setColor(Color.red);
               }
               if (ancol == 1) {     
                 if((i-antim)/6*6 == (i-antim))offsGg.setColor(Color.red);
                 if((i-antim)/6*6 != (i-antim))offsGg.setColor(Color.yellow);
               }
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
          }
       }
 
        offsGg.setColor(Color.yellow) ;
        offsGg.drawString("Find",10,15) ;

 // zoom in
        offsGg.setColor(Color.black) ;
        offsGg.fillRect(0,30,30,150) ;
        offsGg.setColor(Color.green) ;
        offsGg.drawString("Zoom",2,180) ;
        offsGg.drawLine(15,35,15,165) ;
        offsGg.fillRect(5,sldloc,20,5) ;

       g.drawImage(offscreenImg,0,0,this) ;
    }
  }
}

