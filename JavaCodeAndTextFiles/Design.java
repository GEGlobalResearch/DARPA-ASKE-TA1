/*
                              FoilSys 

                               Design
   
                           A Java Application
               to design a  Kutta-Joukowski Airfoil and
               save data to passed to the Tunnel program
                          Derived from FoilSim II

                     Version 1.0h   - 5 Jun 09

                         Written by Tom Benson
                          and Anthony Vila
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
 
  New test -
               - break FoilSim II into three programs
                   passing data by files
               -  this is the design program
               *  get all input onto one panel
               *  modify viewer to only show design
               *  write design file for the tunnel
               *  clean out unused options
               *  copy viewer to show perspective design
               *  set up design window
                  re-arrange inputs

                                           TJB  22 Dec 08
New test A -
			-Fix span length to 1 meter
			-Remove read-in boxes and replace with blanks
			-Reset defaults
			
							AJV 5/27/09
New test I -
			-Erase commented out debugging
			-Change file output to include span and chord values
			-Rescale Layout to match test A modifications
			-Change file output to include lunits value

							AJV 5/27/09

Test T -
			-Add a graph paper conversion factor (graphconv) to convert the graph paper layout to decimeters

							AJV 6/4/09
*/

import java.awt.*;
import java.lang.Math ;
import java.io.* ;

public class Design extends java.applet.Applet {
 
   static double convdr = 3.1415926/180. ;
   static double pid2 = 3.1415926/2.0 ;
   static double rval,ycval,xcval,gamval,alfval,thkval,camval,chrd,clift ;
   static double thkinpt,caminpt ;                 /* MODS 10 Sep 99 */
   static double leg,teg,lem,tem;
   static double usq,vsq,alt,altmax,area,armax,armin ;
   static double chord,aspr,arold,chrdold,spnold ;
   static double span = 3.281;                  //  Hold wing span constant
   static double g0,q0,ps0,pt0,ts0,rho,rlhum,temf,presm ;
   static double lyg,lrg,lthg,lxgt,lygt,lrgt,lthgt,lxgtc,lygtc;
   static double lxm,lym,lxmt,lymt,vxdir;/* MOD 20 Jul */
   static double deltb,xflow ;             /* MODS  20 Jul 99 */
   static double delx,delt,vfsd,spin,spindr,yoff,radius ;
   static double vel,pres,lift,side,omega,radcrv,relsy,angr ;
   static double stfact ;

   static double rg[][]  = new double[20][40] ; 
   static double thg[][] = new double[20][40] ; 
   static double xg[][]  = new double[20][40] ; 
   static double yg[][]  = new double[20][40] ; 
   static double xm[][]  = new double[20][40] ; 
   static double ym[][]  = new double[20][40] ; 
   static double xpl[][]  = new double[20][40] ; 
   static double ypl[][]  = new double[20][40] ; 
   static double xgc[][]  = new double[20][40] ; 
   static double ygc[][]  = new double[20][40] ; 
   static double xplg[][]  = new double[20][40] ; 
   static double yplg[][]  = new double[20][40] ; 
   static double plp[]   = new double[40] ;
   static double plv[]   = new double[40] ;

   int inptopt,outopt,iprint,dato,datp ;
   int nptc,npt2,nlnc,nln2,rdflag,browflag,probflag,anflag;
   int foiltype,flflag,lunits,lftout,planet ;
   int displ,viewflg,dispp,dout,antim,ancol,sldloc; 
   int calcrange,arcor ;
   int counter;
       /* units data */
   static double vmn,almn,angmn,vmx,almx,angmx ;
   static double camn,thkmn,camx,thkmx ;
   static double chrdmn,armn,chrdmx,armx ;
   static double radmn,spinmn,radmx,spinmx ;
   static double vconv,vmax ;
   static double pconv,pmax,pmin,lconv,rconv,fconv,fmax,fmaxb,graphconv;
   int lflag,gflag,nond;
   static double plscale ;
       /*  plot & probe data */
   static double fact,xpval,ypval,pbval,factp;
   static double prg,pthg,pxg,pyg,pxm,pym,pxpl,pypl ;
   int pboflag,xt,yt,ntikx,ntiky,npt,xtp,ytp ;
   int xt1,yt1,xt2,yt2,spanfac ;
   int lines,nord,nabs,ntr ;
   static double begx,endx,begy,endy ;
   static String labx,labxu,laby,labyu ;
   static double pltx[][]  = new double[3][40] ;
   static double plty[][]  = new double[3][40] ;
   static double plthg[]  = new double[2] ;

   Solver solve ;
   Inp inp ;
   Out out ;
   CardLayout layin,layout,layplt ;
   Image offImg1 ;
   Graphics off1Gg ;
   Image offImg2 ;
   Graphics off2Gg ;

   static Frame fd ;
   static PrintStream prnt ;
   static OutputStream pfile ;
   static OutputStream sfile ;
   static DataOutputStream savout ;

   public void init() {
     int i;
     solve = new Solver() ;

     offImg1 = createImage(this.size().width,
                      this.size().height) ;
     off1Gg = offImg1.getGraphics() ;
     offImg2 = createImage(this.size().width,
                      this.size().height) ;
     off2Gg = offImg2.getGraphics() ;

     setLayout(new GridLayout(1,2,5,5)) ;

     solve.setDefaults () ;

     inp = new Inp(this) ;
     out = new Out(this) ;

     add(inp) ;
     add(out) ;
 
     fd.show() ;

     solve.getFreeStream ();
     computeFlow () ;
     inp.view.start() ;
     out.plt.start() ;
  }
 
  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }

  public void computeFlow() { 

     solve.getFreeStream () ;

     if (flflag == 1) {
         solve.getCirc ();                   /* get circulation */
     }
     solve.getGeom ();                   /* get geometry*/
     solve.genFlow () ;
 
     solve.getProbe() ;
 
     loadOut() ;

     loadPlot() ;
  }

  public int filter0(double inumbr) {
        //  output only to .
       int number ;
       int intermed ;
 
       number = (int) (inumbr);
       return number ;
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
 
  public void setUnits() {   // Switching Units
       double ovs,chords,spans,aros,chos,spos,rads ;
       double alts,ares ;

       alts = alt / lconv ;
       chords = chord / lconv ;
       spans = span / lconv ;
       ares = area /lconv/lconv ;
       aros = arold /lconv/lconv ;
       chos = chrdold / lconv ;
       spos = spnold / lconv ;
       ovs = vfsd / vconv ;
       rads = radius / lconv ;

       switch (lunits) {
          case 0: {                             /* English */
            lconv = 1.;                      /*  feet    */
            vconv = .6818; vmax = 250.;   /*  mph  */
            if (planet == 2) vmax = 50. ;
            fconv = 1.0; fmax = 100000.; fmaxb = .5;  /* pounds   */
            pconv = 14.7  ;                   /* lb/sq in */
		graphconv = 1.;              /*Conversion factor for graph paper (feet)*/
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                    /* meters */
            vconv = 1.097; vmax = 400. ;   /* km/hr  */
            if (planet == 2) vmax = 80. ;
            fconv = 4.448 ; fmax = 500000.; fmaxb = 2.5; /* newtons */
            pconv = 101.3 ;               /* kilo-pascals */
		graphconv = 3.048;         /*Conversion factor for graph paper (decimeters)*/
            break ;
          }
       }
 
       alt = alts * lconv ;
       chord = chords * lconv ;
       span = spans * lconv ;
       area = ares * lconv * lconv ;
       arold = aros * lconv * lconv ;
       chrdold = chos * lconv ;
       spnold = spos * lconv ;
       vfsd  = ovs * vconv;
       radius  = rads * lconv;

       return ;
  }

  public void loadInput() {   // load the input panels
       int i1,i3,i4,i5,i6 ;
       double v1,v2,v3,v4,v5,v6 ;
       float fl1,fl2,fl3,fl4,fl5,fl6 ;
                  //  dimensional
       if (lunits == 0) {
           inp.in.shp.upr.inl.l3.setText("Chord-ft") ;
           inp.in.shp.lwr.dwn.l3.setText("Area-sq ft") ;
           inp.in.cyl.inl.l2.setText("Radius ft") ;
       }
       if (lunits == 1) {
           inp.in.shp.upr.inl.l3.setText("Chord-m") ;
           inp.in.shp.lwr.dwn.l3.setText("Area-sq m") ;
           inp.in.cyl.inl.l2.setText("Radius m") ;
       }
       v1 = chord ;
       chrdmn = (5./(2.54*12))*lconv;   chrdmx = 3.281*lconv ;
       v2 = span;
       v3 = area ;
       armn = armin*lconv*lconv; armx = armax*lconv*lconv ;
       v4 = vfsd ;
       vmn = 0.0;   vmx= vmax ;
       v5 = alt ;
       almn = 0.0;  almx = altmax*lconv ;
       v6 = radius ;
       radmn = .05*lconv;  radmx = 5.0*lconv ;
       aspr = span/chord ;
       spanfac = (int)(2.0*fact*aspr*.3535) ;

       fl1 = filter3(v1) ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = filter3(v4) ;
       fl5 = (float) v5 ;
       fl6 = (float) v6 ;
   
       inp.in.shp.upr.inl.f3.setText(String.valueOf(fl1)) ;
       inp.in.shp.lwr.dwn.o3.setText(String.valueOf(fl3)) ;
       inp.in.cyl.inl.f2.setText(String.valueOf(fl6)) ;
   
       i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
       i3 = (int) (((v3 - armn)/(armx-armn))*1000.) ;
       i4 = (int) (((v4 - vmn)/(vmx-vmn))*1000.) ;
       i5 = (int) (((v5 - almn)/(almx-almn))*1000.) ;
       i6 = (int) (((v6 - radmn)/(radmx-radmn))*1000.) ;

       inp.in.shp.upr.inr.s3.setValue(i1) ;
       inp.in.cyl.inr.s2.setValue(i6) ;
                //  non-dimensional
       v1 = caminpt ;
       v2 = thkinpt ;
       v3 = alfval ;
       v4 = spin*60.0 ;

       fl1 = (float) v1 ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = (float) v4 ;

       inp.in.shp.upr.inl.f1.setText(String.valueOf(fl1)) ;
       inp.in.shp.upr.inl.f2.setText(String.valueOf(fl2)) ;
       inp.in.cyl.inl.f1.setText(String.valueOf(fl4)) ;

       i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
       i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
       i4 = (int) (((v4 - spinmn)/(spinmx-spinmn))*1000.) ;
     
       inp.in.shp.upr.inr.s1.setValue(i1) ;
       inp.in.cyl.inr.s1.setValue(i4) ;

       computeFlow() ;
       return ;
  }

  public void loadOut() {   // output routine
     String outunit ;

     outunit = " lbs" ;
     if (lunits == 1) outunit = " N" ;

     if (foiltype <= 3) {     // mapped airfoil
                          // stall model
        stfact = 1.0 ;
        if (anflag == 1) {
            if (alfval > 10.0 ) {
               stfact = .5 + .1 * alfval - .005 * alfval * alfval ;
            }
            if (alfval < -10.0 ) {
               stfact = .5 - .1 * alfval - .005 * alfval * alfval ;
            }
            clift = clift * stfact ;
        }
               
        if (arcor == 1) {  // correction for low aspect ratio
            clift = clift /(1.0 + clift/(3.14159*aspr)) ;
        }


        if (lftout == 0) {
          lift = clift * q0 * area / lconv / lconv ; /* lift in lbs */
          lift = lift * fconv ;
        }
     }
     if (foiltype >= 4) {     // cylinder and ball

        lift = rho * vfsd/vconv * gamval * vfsd/vconv * span/lconv; // lift lbs
        if (foiltype == 5) lift = lift * 3.1415926 / 2.0 ;  // ball 
        lift = lift * fconv ;
        if (lftout == 1) {
          clift = (lift/fconv) / ( q0 *  area/lconv/lconv) ;
        }
     }
 
     switch (lunits)  {
       case 0: {                             /* English */
           inp.in.shp.lwr.dwn.o4.setText(String.valueOf(filter3(aspr))) ;
           break;
        }
        case 1: {                             /* Metric */
           inp.in.shp.lwr.dwn.o4.setText(String.valueOf(filter3(aspr))) ;
           break ;
        }
     }
//  diagnostics
//    inp.in.shp.lwr.dwn.o1.setText(String.valueOf(filter3(fact))) ;
//    inp.in.shp.lwr.dwn.o2.setText(String.valueOf(filter0(sldloc))) ;
     return ;
  }

  public void loadProbe() {   // probe output routine

     pbval = 0.0 ;
     if (pboflag == 1) pbval = vel * vfsd ;           // velocity
     if (pboflag == 2) pbval = ((ps0 + pres * q0)/2116.) * pconv ; // pressure
 
     return ;
  }

  public void loadPlot() {
     double rad,ang,xc,yc,lftref,clref ;
     double del,spd,awng,ppl,tpl,hpl,angl,thkpl,campl,clpl ;
     int index,ic ;

     lines = 1 ;
     clref =  getClplot(camval,thkval,alfval) ;
     if (Math.abs(clref) <= .001) clref = .001 ;    /* protection */
     lftref = clref * q0 * area/lconv/lconv ;
  
// load up the view image
     for (ic = 0; ic <= nlnc; ++ ic) {
        for (index = 0; index <= nptc; ++ index) {
           if (foiltype <= 3) {
              xpl[ic][index] = xm[ic][index] ;
              ypl[ic][index] = ym[ic][index] ;
           }
           if (foiltype >= 4) {
              xpl[ic][index] = xg[ic][index] ;
              ypl[ic][index] = yg[ic][index] ;
           }
        }
     }
// load up the generating plane
     if (dispp == 25) {
        for (ic = 0; ic <= nlnc; ++ ic) {
           for (index = 0; index <= nptc; ++ index) {
              xplg[ic][index] = xgc[ic][index] ;
              yplg[ic][index] = ygc[ic][index] ;
           }
        }
     }
// probe
     for (index = 0; index <= nptc; ++ index) {
        if (foiltype <= 3) {
           xpl[19][index] = xm[19][index] ;
           ypl[19][index] = ym[19][index] ;
           pxpl = pxm ;
           pypl = pym ;
        }
        if (foiltype >= 4) {
           xpl[19][index] = xg[19][index] ;
           ypl[19][index] = yg[19][index] ;
           pxpl = pxg ;
           pypl = pyg ;
        }
     }
   }

   public double getClplot (double camb, double thic, double angl) {
      double beta,xc,yc,rc,gamc,lec,tec,lecm,tecm,crdc ;
      double number ;

      xc = 0.0 ;
      yc = camb / 2.0 ;
      rc = thic/4.0 + Math.sqrt( thic*thic/16.0 + yc*yc + 1.0);
      xc = 1.0 - Math.sqrt(rc*rc - yc*yc) ;
      beta = Math.asin(yc/rc)/convdr ;       /* Kutta condition */
      gamc = 2.0*rc*Math.sin((angl+beta)*convdr) ;
      if (foiltype <= 3 && anflag == 2) gamc = 0.0 ;
      lec = xc - Math.sqrt(rc*rc - yc*yc) ;
      tec = xc + Math.sqrt(rc*rc - yc*yc) ;
      lecm = lec + 1.0/lec ;
      tecm = tec + 1.0/tec ;
      crdc = tecm - lecm ;
                                 // stall model 1
      stfact = 1.0 ;
      if (anflag == 1) {
          if (angl > 10.0 ) {
             stfact = .5 + .1 * angl - .005 * angl * angl ;
          }
          if (angl < -10.0 ) {
             stfact = .5 - .1 * angl - .005 * angl * angl ;
          }
      }

      number = stfact*gamc*4.0*3.1415926/crdc ;

      if (arcor == 1) {  // correction for low aspect ratio
          number = number /(1.0 + number/(3.14159*aspr)) ;
      }

      return (number) ;
  }

  class Solver {
 
     Solver () {
     }

     public void setDefaults() {

        dato = 0 ;
        datp = 0 ;
        plscale = 1.0 ;
        arcor = 0 ;
        planet = 0 ;
        lunits = 0 ;
        lftout = 0 ;
        inptopt = 0 ;
        outopt = 0 ;
        nlnc = 15 ;
        nln2 = nlnc/2 + 1 ;
        nptc = 37 ;
        npt2 = nptc/2 + 1 ;
        deltb = .5 ;
        foiltype = 1 ;
        flflag = 1;
        thkval = .5 ;
        thkinpt = 12.5 ;                   /* MODS 10 SEP 99 */
        camval = 0.0 ;
        caminpt = 0.0 ;
        alfval = 0.0 ;
        gamval = 0.0 ;
        radius = 1.0 ;
        spin = 0.0 ;
        spindr = 1.0 ;
        rval = 1.0 ;
        ycval = 0.0 ;
        xcval = 0.0 ;
        displ   = 3 ;                            
        viewflg = 0 ;
        dispp = 0 ;
        calcrange = 0 ;
        dout = 0 ;
        stfact = 1.0 ;
 
        xpval = 2.1;
        ypval = -.5 ;
        pboflag = 0 ;
        xflow = -10.0;                             /* MODS  20 Jul 99 */

        pconv = 14.7;
        pmin = .5 ;
        pmax = 1.0 ;
        fconv = 1.0 ;
        fmax = 100000. ;
        fmaxb = .50 ;
        vconv = .6818 ;
        vfsd = 100. ;
        vmax = 250. ;
        lconv = 1.0 ;
	  graphconv = 1.0;

        alt = 0.0 ;
        altmax = 50000. ;
        chrdold = chord = 1.0 ;
        aspr = 3.28084 ;
        arold = area = 100.0 ;
        armax = 2500.01 ;
        armin = .01 ;                 /* MODS 9 SEP 99 */
 
        xt = 240;  yt = 165; fact = 30.0 ;
        sldloc = 155 ;
        xtp = 240; ytp = 565; factp = 25.0 ;
        spanfac = (int)(2.0*fact*aspr*.3535) ;
        xt1 = xt + spanfac ;
        yt1 = yt - spanfac ;
        xt2 = xt - spanfac;
        yt2 = yt + spanfac ;
        plthg[1] = 0.0 ;
 
        probflag = 2 ;
        anflag = 0 ;
        vmn = 0.0;     vmx = 250.0 ;
        almn = 0.0;    almx = 50000.0 ;
        angmn = -20.0; angmx = 20.0 ;
        camn = -25.0;  camx = 25.0 ;
        thkmn = 1.0; thkmx = 26.0 ;
        chrdmn = 5./(2.54*12) ;  chrdmx = 3.281;
        armn = .01 ;  armx = 2500.01 ;
        spinmn = -1500.0;   spinmx = 1500.0 ;
        radmn = .05;   radmx = 5.0 ;

        laby = String.valueOf("Press");
        labyu = String.valueOf("psi");
        labx = String.valueOf(" X ");
        labxu = String.valueOf("% chord");
        iprint = 0 ;

        return ;
     }

     public void getFreeStream() {    //  free stream conditions
       double hite,pvap,rgas,gama ;       /* MODS  19 Jan 00  whole routine*/

       g0 = 32.2 ;
       rgas = 1718. ;                /* ft2/sec2 R */
       gama = 1.4 ;
       hite = alt/lconv ;
       if (planet == 0) {    // Earth  standard day
         if (hite <= 36152.) {           // Troposphere
            ts0 = 518.6 - 3.56 * hite/1000. ;
            ps0 = 2116. * Math.pow(ts0/518.6,5.256) ;
         }
         if (hite >= 36152. && hite <= 82345.) {   // Stratosphere
            ts0 = 389.98 ;
            ps0 = 2116. * .2236 *
                 Math.exp((36000.-hite)/(53.35*389.98)) ;
         }
         if (hite >= 82345.) {
            ts0 = 389.98 + 1.645 * (hite-82345)/1000. ;
            ps0 = 2116. *.02456 * Math.pow(ts0/389.98,-11.388) ;
         }
         temf = ts0 - 459.6 ;
         if (temf <= 0.0) temf = 0.0 ;                    
/* Eq 1:6A  Domasch  - effect of humidity 
         rlhum = 0.0 ;
         presm = ps0 * 29.92 / 2116. ;
         pvap = rlhum*(2.685+.00353*Math.pow(temf,2.245));
         rho = (ps0 - .379*pvap)/(rgas * ts0) ; 
*/
         rho = ps0/(rgas * ts0) ;
       }

       if (planet == 1) {   // Mars - curve fit of orbiter data
         rgas = 1149. ;                /* ft2/sec2 R */
         gama = 1.29 ;

         if (hite <= 22960.) {
            ts0 = 434.02 - .548 * hite/1000. ;
            ps0 = 14.62 * Math.pow(2.71828,-.00003 * hite) ;
         }
         if (hite > 22960.) {
            ts0 = 449.36 - 1.217 * hite/1000. ;
            ps0 = 14.62 * Math.pow(2.71828,-.00003 * hite) ;
         }
         rho = ps0/(rgas*ts0) ;
       }

       if (planet == 2) {   // water --  constant density
         hite = -alt/lconv ;
         ts0 = 520. ;
         rho = 1.94 ;
         ps0 = (2116. - rho * g0 * hite) ;
       }

       if (planet == 3) {   // specify air temp and pressure 
          rho = ps0/(rgas*ts0) ;
       }

       if (planet == 4) {   // specify fluid density
          ps0 = 2116. ;
       }

       q0  = .5 * rho * vfsd * vfsd / (vconv * vconv) ;
       pt0 = ps0 + q0 ;

       return ;
     }

     public void getCirc() {   // circulation from Kutta condition
       double beta;

       xcval = 0.0 ;
       switch (foiltype)  {
          case 1:  {                  /* Juokowski geometry*/
              ycval = camval / 2.0 ;
              rval = thkval/4.0 +Math.sqrt(thkval*thkval/16.0+ycval*ycval +1.0);
              xcval = 1.0 - Math.sqrt(rval*rval - ycval*ycval) ;
              beta = Math.asin(ycval/rval)/convdr ;     /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 2:  {                  /* Elliptical geometry*/
              ycval = camval / 2.0 ;
              rval = thkval/4.0 + Math.sqrt(thkval*thkval/16.0+ycval*ycval+1.0);
              beta = Math.asin(ycval/rval)/convdr ;    /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 3:  {                  /* Plate geometry*/
              ycval = camval / 2.0 ;
              rval = Math.sqrt(ycval*ycval+1.0);
              beta = Math.asin(ycval/rval)/convdr ;    /* Kutta condition */
              gamval = 2.0*rval*Math.sin((alfval+beta)*convdr) ;
              break ;
          }
          case 4: {         /* get circulation for rotating cylnder */
              rval = radius/lconv ;
              gamval = 4.0 * 3.1415926 * 3.1415926 *spin * rval * rval
                                 / (vfsd/vconv) ;
              gamval = gamval * spindr ;
              ycval = .0001 ;
              break ;
          }
          case 5: {         /* get circulation for rotating ball */
              rval = radius/lconv ;
              gamval = 4.0 * 3.1415926 * 3.1415926 *spin * rval * rval
                                 / (vfsd/vconv) ;
              gamval = gamval * spindr ;
              ycval = .0001 ;
              break ;
          }
       }
       
       if (foiltype <=3 && anflag == 2) gamval = 0.0 ;

       return ;
     }

     public void getGeom() {   // geometry
       double thet,rdm,thtm ;
       int index;

       for (index =1; index <= nptc; ++index) {
           thet = (index -1)*360./(nptc-1) ;
           xg[0][index] = rval * Math.cos(convdr * thet) + xcval ;
           yg[0][index] = rval * Math.sin(convdr * thet) + ycval ;
           rg[0][index] = Math.sqrt(xg[0][index]*xg[0][index] +
                                yg[0][index]*yg[0][index])  ;
           thg[0][index] = Math.atan2(yg[0][index],xg[0][index])/convdr;
           xm[0][index] = (rg[0][index] + 1.0/rg[0][index])*
                    Math.cos(convdr*thg[0][index]) ;
           ym[0][index] = (rg[0][index] - 1.0/rg[0][index])*
                    Math.sin(convdr*thg[0][index]) ;
           rdm = Math.sqrt(xm[0][index]*xm[0][index] +
                           ym[0][index]*ym[0][index])  ;
           thtm = Math.atan2(ym[0][index],xm[0][index])/convdr;
           xm[0][index] = rdm * Math.cos((thtm - alfval)*convdr);
           ym[0][index] = rdm * Math.sin((thtm - alfval)*convdr);
           getVel(rval,thet) ;
           plp[index] = ((ps0 + pres * q0)/2116.) * pconv ;
           plv[index] = vel * vfsd ;
           xgc[0][index] = rval * Math.cos(convdr * thet) + xcval ;
           ygc[0][index] = rval * Math.sin(convdr * thet) + ycval ;
       }

       xt1 = xt + spanfac ;
       yt1 = yt - spanfac ;
       xt2 = xt - spanfac;
       yt2 = yt + spanfac ;

       return ;
     }

     public void genFlow() {   // generate flowfield
       double rnew,thet,psv,fxg;
       int k,index;
                              /* all lines of flow  except stagnation line*/
       for (k=1; k<=nlnc; ++k) {
         psv = -.5*(nln2-1) + .5*(k-1) ;
         fxg = xflow ;
         for (index =1; index <=nptc; ++ index) {
           solve.getPoints (fxg,psv) ;
           xg[k][index]  = lxgt ;
           yg[k][index]  = lygt ;
           rg[k][index]  = lrgt ;
           thg[k][index] = lthgt ;
           xm[k][index]  = lxmt ;
           ym[k][index]  = lymt ;
           if (anflag == 1) {           // stall model
              if (alfval > 10.0 && psv > 0.0) {
                   if (xm[k][index] > 0.0) {
                      ym[k][index] = ym[k][index -1] ;
                   }
              }
              if (alfval < -10.0 && psv < 0.0) {
                   if (xm[k][index] > 0.0) {
                      ym[k][index] = ym[k][index -1] ;
                   }
              }
           }
           solve.getVel(lrg,lthg) ;
           fxg = fxg + vxdir*deltb ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
         }
       }
                                              /*  stagnation line */
       k = nln2 ;
       psv = 0.0 ;
                                              /*  incoming flow */
       for (index =1; index <= npt2; ++ index) {
           rnew = 10.0 - (10.0 - rval)*Math.sin(pid2*(index-1)/(npt2-1)) ;
           thet = Math.asin(.999*(psv - gamval*Math.log(rnew/rval))/
                                   (rnew - rval*rval/rnew)) ;
           fxg =  - rnew * Math.cos(thet) ;
           solve.getPoints (fxg,psv) ;
           xg[k][index]  = lxgt ;
           yg[k][index]  = lygt ;
           rg[k][index]  = lrgt ;
           thg[k][index] = lthgt ;
           xm[k][index]  = lxmt ;
           ym[k][index]  = lymt ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
       }
                                              /*  downstream flow */
       for (index = 1; index <= npt2; ++ index) {
           rnew = 10.0 + .01 - (10.0 - rval)*Math.cos(pid2*(index-1)/(npt2-1)) ;
           thet = Math.asin(.999*(psv - gamval*Math.log(rnew/rval))/
                                      (rnew - rval*rval/rnew)) ;
           fxg =   rnew * Math.cos(thet) ;
           solve.getPoints (fxg,psv) ;
           xg[k][npt2+index]  = lxgt ;
           yg[k][npt2+index]  = lygt ;
           rg[k][npt2+index]  = lrgt ;
           thg[k][npt2+index] = lthgt ;
           xm[k][npt2+index]  = lxmt ;
           ym[k][npt2+index]  = lymt ;
           xgc[k][index]  = lxgtc ;
           ygc[k][index]  = lygtc ;
       }
                                              /*  stagnation point */
       xg[k][npt2]  = xcval ;
       yg[k][npt2]  = ycval ;
       rg[k][npt2]  = Math.sqrt(xcval*xcval+ycval*ycval) ;
       thg[k][npt2] = Math.atan2(ycval,xcval)/convdr ;
       xm[k][npt2]  = (xm[k][npt2+1] + xm[k][npt2-1])/2.0 ;
       ym[k][npt2]  = (ym[0][nptc/4+1] + ym[0][nptc/4*3+1])/2.0 ;
                                /*  compute lift coefficient */
       leg = xcval - Math.sqrt(rval*rval - ycval*ycval) ;
       teg = xcval + Math.sqrt(rval*rval - ycval*ycval) ;
       lem = leg + 1.0/leg ;
       tem = teg + 1.0/teg ;
       chrd = tem - lem ;
       clift = gamval*4.0*3.1415926/chrd ;

       return ;
     }

     public void getPoints(double fxg, double psv) {   // flow in x-psi
       double radm,thetm ;                /* MODS  20 Jul 99  whole routine*/
       double fnew,ynew,yold,rfac,deriv ;
       double xold,xnew,thet ;
       double rmin,rmax ;
       int iter,isign;
                       /* get variables in the generating plane */
                           /* iterate to find value of yg */
       ynew = 10.0 ;
       yold = 10.0 ;
       if (psv < 0.0) ynew = -10.0 ;
       if (Math.abs(psv) < .001 && alfval < 0.0) ynew = rval ;
       if (Math.abs(psv) < .001 && alfval >= 0.0) ynew = -rval ;
       fnew = 0.1 ;
       iter = 1 ;
       while (Math.abs(fnew) >= .00001 && iter < 25) {
           ++iter ;
           rfac = fxg*fxg + ynew*ynew ;
           if (rfac < rval*rval) rfac = rval*rval + .01 ;
           fnew = psv - ynew*(1.0 - rval*rval/rfac)
                  - gamval*Math.log(Math.sqrt(rfac)/rval) ;
           deriv = - (1.0 - rval*rval/rfac)
               - 2.0 * ynew*ynew*rval*rval/(rfac*rfac)
               - gamval * ynew / rfac ;
           yold = ynew ;
           ynew = yold  - .5*fnew/deriv ;
       }
       lyg = yold ;
                                     /* rotate for angle of attack */
       lrg = Math.sqrt(fxg*fxg + lyg*lyg) ;
       lthg = Math.atan2(lyg,fxg)/convdr ;
       lxgt = lrg * Math.cos(convdr*(lthg + alfval)) ;
       lygt = lrg * Math.sin(convdr*(lthg + alfval)) ;
                              /* translate cylinder to generate airfoil */
       lxgtc = lxgt = lxgt + xcval ;
       lygtc = lygt = lygt + ycval ;
       lrgt = Math.sqrt(lxgt*lxgt + lygt*lygt) ;
       lthgt = Math.atan2(lygt,lxgt)/convdr ;
                               /*  Kutta-Joukowski mapping */
       lxm = (lrgt + 1.0/lrgt)*Math.cos(convdr*lthgt) ;
       lym = (lrgt - 1.0/lrgt)*Math.sin(convdr*lthgt) ;
                              /* tranforms for view fixed with free stream */
                /* take out rotation for angle of attack mapped and cylinder */
       radm = Math.sqrt(lxm*lxm+lym*lym) ;
       thetm = Math.atan2(lym,lxm)/convdr ;
       lxmt = radm*Math.cos(convdr*(thetm-alfval)) ;
       lymt = radm*Math.sin(convdr*(thetm-alfval)) ;

       lxgt = lxgt - xcval ;
       lygt = lygt - ycval ;
       lrgt = Math.sqrt(lxgt*lxgt + lygt*lygt)  ;
       lthgt = Math.atan2(lygt,lxgt)/convdr;
       lxgt = lrgt * Math.cos((lthgt - alfval)*convdr);
       lygt = lrgt * Math.sin((lthgt - alfval)*convdr);

       return ;
     }
 
     public void getVel(double rad, double theta) {  //velocity and pressure 
      double ur,uth,jake1,jake2,jakesq ;
      double xloc,yloc,thrad,alfrad ;

      thrad = convdr * theta ;
      alfrad = convdr * alfval ;
                                /* get x, y location in cylinder plane */
      xloc = rad * Math.cos(thrad) ;
      yloc = rad * Math.sin(thrad) ;
                                /* velocity in cylinder plane */
      ur  = Math.cos(thrad-alfrad)*(1.0-(rval*rval)/(rad*rad)) ;
      uth = -Math.sin(thrad-alfrad)*(1.0+(rval*rval)/(rad*rad))
                            - gamval/rad;
      usq = ur*ur + uth*uth ;
      vxdir = ur * Math.cos(thrad) - uth * Math.sin(thrad) ; // MODS  20 Jul 99 
                                /* translate to generate airfoil  */
      xloc = xloc + xcval ;
      yloc = yloc + ycval ;
                                   /* compute new radius-theta  */
      rad = Math.sqrt(xloc*xloc + yloc*yloc) ;
      thrad  = Math.atan2(yloc,xloc) ;
                                   /* compute Joukowski Jacobian  */
      jake1 = 1.0 - Math.cos(2.0*thrad)/(rad*rad) ;
      jake2 = Math.sin(2.0*thrad)/(rad*rad) ;
      jakesq = jake1*jake1 + jake2*jake2 ;
      if (Math.abs(jakesq) <= .01) jakesq = .01 ;  /* protection */
      vsq = usq / jakesq ;
          /* vel is velocity ratio - pres is coefficient  (p-p0)/q0   */
      if (foiltype <= 3) {
           vel = Math.sqrt(vsq) ;
           pres = 1.0 - vsq ;
      }
      if (foiltype >= 4) {
           vel = Math.sqrt(usq) ;
           pres = 1.0 - usq ;
      }
      return ;
    }

    public void getProbe () { /* all of the information needed for the probe */
      double prxg;
      int index;
                       /* get variables in the generating plane */
      if (Math.abs(ypval) < .01) ypval = .05 ;
      solve.getPoints (xpval,ypval) ;

      solve.getVel(lrg,lthg) ;
      loadProbe() ;

      pxg = lxgt ;
      pyg = lygt ;
      prg = lrgt ;
      pthg = lthgt ;
      pxm = lxmt ;
      pym = lymt ;
                                    /* smoke */
      if (pboflag == 3 ) {
        prxg = xpval ;
        for (index =1; index <=nptc; ++ index) {
          solve.getPoints (prxg,ypval) ;
          xg[19][index] = lxgt ;
          yg[19][index] = lygt ;
          rg[19][index] = lrgt ;
          thg[19][index] = lthgt ;
          xm[19][index] = lxmt ;
          ym[19][index] = lymt ;
          if (anflag == 1) {           // stall model
             if (xpval > 0.0) {
                if (alfval > 10.0 && ypval > 0.0) { 
                   ym[19][index] = ym[19][1] ;
                } 
                if (alfval < -10.0 && ypval < 0.0) {
                     ym[19][index] = ym[19][1] ;
                }
             }
             if (xpval < 0.0) {
                if (alfval > 10.0 && ypval > 0.0) { 
                   if (xm[19][index] > 0.0) {
                       ym[19][index] = ym[19][index-1] ;
                   }
                } 
                if (alfval < -10.0 && ypval < 0.0) {
                   if (xm[19][index] > 0.0) {
                     ym[19][index] = ym[19][index-1] ;
                   }
                }
             }
          }
          solve.getVel(lrg,lthg) ;
          prxg = prxg + vxdir*deltb ;
        }
      }
      return ;
    }
  } // end Solver

  class Inp extends Panel {
     Design outerparent ;
     In in ;
     Viewer view ;

     Inp (Design target) {
        outerparent = target ;
        setLayout(new GridLayout(2,1,5,5)) ;

        view = new Viewer(outerparent) ;
        in = new In(outerparent) ;

        add(view) ;
        add(in) ;
     }

     class In extends Panel {
        Design outerparent ;
        Shp shp ;
        Cyl cyl ;

        In (Design target) { 
           outerparent = target ;
           layin = new CardLayout() ;
           setLayout(layin) ;

           shp = new Shp(outerparent) ;
           cyl = new Cyl(outerparent) ;

           add ("second", shp) ;
           add ("fourth", cyl) ;
        }
 
        class Shp extends Panel {
           Design outerparent ;
           Upr upr ;
           Lwr lwr ;

           Shp (Design target) {

              outerparent = target ;
              setLayout(new GridLayout(2,1,5,5)) ;

              upr = new Upr(outerparent) ;
              lwr = new Lwr(outerparent) ;

              add(upr) ;
              add(lwr) ;
           }

           class Upr extends Panel {
              Design outerparent ;
              Inr inr ;
              Inl inl;

              Upr (Design target) {
   
                 outerparent = target ;
                 setLayout(new GridLayout(1,2,5,5)) ;
      
                 inl = new Inl(outerparent) ;
                 inr = new Inr(outerparent) ;
      
                 add(inl) ;
                 add(inr) ;
              }
      
              class Inl extends Panel {
                 Design outerparent ;
                 TextField f1,f2,f3 /* ,f4 */;  //TextField commented out from Span Scrollbar space
                 Label l1,l2,l3 /* ,l4 */; //Label commented out from Span Scrollbar.
                 Label l01,l02 ;
                 Choice shapch ;
         
                 Inl (Design target) {
         
                  outerparent = target ;
                  setLayout(new GridLayout(5,2,2,10)) ;
      
                  l02 = new Label("Label:", Label.RIGHT) ;
                  l02.setForeground(Color.blue) ;
      
                  l1 = new Label("Camber-%c", Label.CENTER) ;
                  f1 = new TextField("0.0",5) ;
      
                  l2 = new Label("Thick-%crd", Label.CENTER) ;
                  f2 = new TextField("12.5",5) ;
      
                  l3 = new Label("Chord-ft", Label.CENTER) ;
                  f3 = new TextField("1.0",5) ;
      
                  /* l4 = new Label("Span-ft", Label.CENTER) ;  //See previous comments
                  f4 = new TextField("20.0",5) ;*/
      
                  shapch = new Choice() ;
                  shapch.addItem("Airfoil") ;
                  shapch.addItem("Ellipse");
                  shapch.addItem("Plate");
//                  shapch.addItem("Cylinder");
//                  shapch.addItem("Ball");
                  shapch.setBackground(Color.white) ;
                  shapch.setForeground(Color.blue) ;
                  shapch.select(0) ;
      
                  add(shapch) ;
                  add(new Label(" ", Label.CENTER)) ;
      
                  add(l1) ;
                  add(f1) ;
      
                  add(l2) ;
                  add(f2) ;
       
                  add(l3) ;
                  add(f3) ;
      
                  add(new Label(" ", Label.CENTER)) ;
                  add(l02) ;
   /* add(l4) ;  //See previous comments add(f4) ;*/
                }
      
                public boolean handleEvent(Event evt) {
                  Double V1,V2,V3;
                  double v1,v2,v3,v4 ;
                  float fl1 ;
                  int i1,i2,i3,choice ;

                  if(evt.id == Event.ACTION_EVENT) {
                    foiltype  = shapch.getSelectedIndex() + 1 ;
                    if(foiltype == 3) {
                      thkinpt = v2 = thkmn ;
                      thkval  = thkinpt / 25.0 ;
                      fl1 = (float) v2 ;
                      f2.setText(String.valueOf(fl1)) ;
                      i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                      inr.s2.setValue(i2) ;
                    }

                    in.cyl.inr.shapch.select(foiltype-1);
                    layout.show(out, "first")  ;
                    outopt = 0 ;
                    dispp = 0 ;
                    calcrange = 0 ;
   
                    V1 = Double.valueOf(f1.getText()) ;
                    v1 = V1.doubleValue() ;
                    V2 = Double.valueOf(f2.getText()) ;
                    v2 = V2.doubleValue() ;
                    V3 = Double.valueOf(f3.getText()) ;
                    v3 = V3.doubleValue() ;
   
                    caminpt = v1 ;
                    if(v1 < camn) {
                      caminpt = v1 = camn ;
                      fl1 = (float) v1 ;
                      f1.setText(String.valueOf(fl1)) ;
                    }
                    if(v1 > camx) {
                      caminpt = v1 = camx ;
                      fl1 = (float) v1 ;
                      f1.setText(String.valueOf(fl1)) ;
                    }
                    camval = caminpt / 25.0 ;
      
                    thkinpt = v2 ;
                    if(v2 < thkmn) {
                      thkinpt = v2 = thkmn ;
                      fl1 = (float) v2 ;
                      f2.setText(String.valueOf(fl1)) ;
                    }
                    if(v2 > thkmx) {
                      thkinpt = v2 = thkmx ;
                      fl1 = (float) v2 ;
                      f2.setText(String.valueOf(fl1)) ;
                    }
                    thkval  = thkinpt / 25.0 ;
          
                    chord = v3 ;
                    if(v3 < chrdmn) {
                      chord = v3 = chrdmn ;
                      fl1 = (float) v3 ;
                      f3.setText(String.valueOf(fl1)) ;
                    }
                    if(v3 > chrdmx) {
                      chord = v3 = chrdmx ;
                      fl1 = (float) v3 ;
                      f3.setText(String.valueOf(fl1)) ;
                    }
      
                    v4 = span ;
                             
           // keeping consistent
                    choice = 3 ;
                    if (chord >= (chrdold+.01) || chord <= (chrdold-.01))choice = 1;
                    if (span >= (spnold+.1) || span <= (spnold-.1)) choice = 2;
                    switch(choice) {
                       case 1: {          // chord changed
                         if (chord < span) {
                           area = span * chord ;
                           aspr = span*span/area ;
                         }
                         if (chord >= span) {
                           v4 = chord ;
                           aspr = 1.0 ;
                           area = v4 * chord ;
                           fl1 = (float) v4 ;
                           spnold = v4 ;
                         }
                         fact = fact * chord/chrdold ;
                         factp = factp * chord/chrdold ;
                         chrdold = chord ;
                         break ;
                       }
                       case 2: {          // span changed
                         if (span > chord) {
                           area = span * chord ;
                           aspr = span*span/area ;
                         }
                         if (span <= chord) {
                            v3 = span ;
                            aspr = 1.0 ;
                            area = v3 * span ;
                            fl1 = (float) v3 ;
                            f3.setText(String.valueOf(fl1)) ;
                            chord = v3 ;
                            fact = fact * chord/chrdold ;
                            factp = factp * chord/chrdold ;
                            chrdold = chord ;
                          }
                          spnold = span ;
                          break ;
                       }
                       case 3: {          // nothing changed
                          chrdold = chord ;
                          spnold = span ;
                          break ;
                       }
                    }
                    spanfac = (int)(2.0*fact*aspr*.3535) ;
   
                    lwr.dwn.o3.setText(String.valueOf(filter3(area))) ;
                    lwr.dwn.o4.setText(String.valueOf(filter3(aspr))) ;
   
                    i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
                    i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                    i3 = (int) (((v3 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
          
                    inr.s1.setValue(i1) ;
                    inr.s2.setValue(i2) ;
                    inr.s3.setValue(i3) ;
      
                    computeFlow() ;
                    return true ;
                  }
                  else return false ;
                } // Handler
              }  // Inl 
   
              class Inr extends Panel {
                 Design outerparent ;
                 Scrollbar s1,s2,s3/* ,s4 */;  //Former scrollbar for Span values commented out
                 TextField labl ;
      
                 Inr (Design target) {
                  int i1,i2,i3;
      
                  outerparent = target ;
                  setLayout(new GridLayout(5,1,2,10)) ;
      
                  i1 = (int) (((0.0 - camn)/(camx-camn))*1000.) ;
                  i2 = (int) (((12.5 - thkmn)/(thkmx-thkmn))*1000.) ;
                  i3 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
   
                  s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
                  s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
                  s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
                  /* s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000); */  //See previous comment
      
                  labl = new TextField(" Enter Name ") ;
   
                  add(new Label(" ", Label.CENTER)) ;
                  add(s1) ;
                  add(s2) ;
                  add(s3) ;
                  add(labl) ;
                  /* add(s4) ;*/
                }
   
                public boolean handleEvent(Event evt) {
                     if(evt.id == Event.ACTION_EVENT) {
                        this.handleTxt(evt) ;
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
                   int i1,i2,i3,choice;
                   double v1,v2,v3,v4;
                   float fl1,fl2,fl3;
                    
    // Input       for computations
                   i1 = s1.getValue() ;
                   i2 = s2.getValue() ;
                   i3 = s3.getValue() ;
   
                   caminpt = v1 = i1 * (camx - camn)/ 1000. + camn ;
                   camval  = caminpt / 25.0 ;
                   thkinpt = v2 = i2 * (thkmx - thkmn)/ 1000. + thkmn ;
                   thkval  = thkinpt / 25.0 ;
                   chord   = v3 = i3 * (chrdmx - chrdmn)/ 1000. + chrdmn ;
			 v4 = span;
   
           // keeping consistent
                   choice = 0 ;
                   if (chord >= (chrdold+.01) || chord <= (chrdold-.01))choice = 1;
                   if (span >= (spnold+.1) || span <= (spnold-.1)) choice = 2;
                   switch(choice) {
                      case 1: {          // chord changed
                        if (chord < span) {
                          area = span * chord ;
                          aspr = span*span/area ;
                        }
                        if (chord >= span) {
                          chord = v4 ;
                          aspr = 1.0 ;
                          area = v4 * chord ;
                          spnold = v4 ;
                        }
                        fact = fact * chord/chrdold ;
                        factp = factp * chord/chrdold ;
                        chrdold = chord ;
                        break ;
                      }
                      case 2: {          // span changed
                        if (span > chord) {
                          area = span * chord ;
                          aspr = span*span/area ;
                        }
                        if (span <= chord) {
                           v3 = span ;
                           aspr = 1.0 ;
                           area = v3 * span ;
                           chord = v3 ;
                           fact = fact * chord/chrdold ;
                           factp = factp * chord/chrdold ;
                           chrdold = chord ;
                           i3 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
                           s3.setValue(i3) ;
                         }
                         spnold = span ;
                         break ;
                      }
                   }
                   spanfac = (int)(2.0*fact*aspr*.3535) ;
   
                   lwr.dwn.o3.setText(String.valueOf(filter3(area))) ;
                   lwr.dwn.o4.setText(String.valueOf(filter3(aspr))) ;
      
                   fl1 = (float) v1 ;
                   fl2 = (float) v2 ;
                   fl3 = filter3(v3) ;
      
                   inl.f1.setText(String.valueOf(fl1)) ;
                   inl.f2.setText(String.valueOf(fl2)) ;
                   inl.f3.setText(String.valueOf(fl3)) ;
           
                   computeFlow() ;
                 }

                 public void handleTxt(Event evt) {
                   int i2 ;
                 }
   
/*
                 public void handleCho(Event evt) {
                   int i2 ;
                   double v2 ;
                   float fl1 ;
      
                   foiltype  = shapch.getSelectedIndex() + 1 ;
                   if (foiltype >= 4) alfval = 0.0 ;
                   if(foiltype <= 2) layin.show(in, "second")  ;
                   if(foiltype == 3) {
                      layin.show(in, "second")  ;
                      thkinpt = v2 = thkmn ;
                      thkval  = thkinpt / 25.0 ;
                      fl1 = (float) v2 ;
                      inl.f2.setText(String.valueOf(fl1)) ;
                      i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                      inr.s2.setValue(i2) ;
                   }
                   if(foiltype == 4) layin.show(in, "fourth")  ;
                   if(foiltype == 5) {
                       span = radius ;
                       area = 3.1415926*radius*radius ;
                       layin.show(in, "fourth")  ;
                       if (viewflg != 0) viewflg = 0 ;
                   }
          
                   in.cyl.inr.shapch.select(foiltype-1);
                   layout.show(out, "first")  ;
                   con.outpch.select(0) ;
                   outopt = 0 ;
                   dispp = 0 ;
                   calcrange = 0 ;
      
                   loadInput() ;
                 }
*/
              }  // Inr
            }   //  end Upr

            class Lwr extends Panel {
               Design outerparent ;
               Upa upa ;
               Dwn dwn;
    
               Lwr (Design target) {
    
                  outerparent = target ;
                  setLayout(new GridLayout(2,1,5,5)) ;
    
                  upa = new Upa(outerparent) ;
                  dwn = new Dwn(outerparent) ;
    
                  add(upa) ;
                  add(dwn) ;
               }
    
               class Upa extends Panel {
                  Design outerparent ;
                  Lft lft ;
                  Rit rit;
    
                  Upa (Design target) {
                     outerparent = target ;
                     setLayout(new GridLayout(1,2,5,5)) ;
    
                     lft = new Lft(outerparent) ;
                     rit = new Rit(outerparent) ;
    
                     add(lft) ;
                     add(rit) ;
                  }
    
                  class Lft extends Panel {
                     TextField sflnam,pflnam ;
    
                     Lft (Design target) {
                        outerparent = target ;
                        setLayout(new GridLayout(3,1,2,2)) ;
       
                        sflnam = new TextField("Enter File Name",5) ;
                        sflnam.setBackground(Color.white) ;
                        sflnam.setForeground(Color.black) ;
       
                        pflnam = new TextField("Enter File Name",5) ;
                        pflnam.setBackground(Color.white) ;
                        pflnam.setForeground(Color.black) ;
       
                        add(sflnam) ;
                        add(pflnam) ;
                        add(new Label(" ", Label.CENTER)) ;
                    }

                    public boolean handleEvent(Event evt) {
               
                       if(evt.id == Event.ACTION_EVENT) {
                          dato = 1;
                          datp = 1;
                          return true;
                       }
                       else return false ;
                    }
                 }  // end Lft

                 class Rit extends Panel {
                     Design outerparent ;
                     Button btsb,btswrt,btso;
                     Button btpb,btpo;
                     FileDialog fds,fdp;
    
                     Rit (Design target) {
                        outerparent = target ;
                        setLayout(new GridLayout(3,2,2,2)) ;
       
                        fds = new FileDialog(fd) ;
                        fdp = new FileDialog(fd) ;
                        btsb = new Button("Data File") ;
                        btsb.setBackground(Color.blue) ;
                        btsb.setForeground(Color.white) ;
   
                        btpb = new Button("Print File") ;
                        btpb.setBackground(Color.blue) ;
                        btpb.setForeground(Color.white) ;
       
                        btso = new Button("Open Data") ;
                        btso.setBackground(Color.blue) ;
                        btso.setForeground(Color.white) ;
       
                        btpo = new Button("Open Print") ;
                        btpo.setBackground(Color.blue) ;
                        btpo.setForeground(Color.white) ;
       
                        btswrt = new Button("Save Data") ;
                        btswrt.setBackground(Color.blue) ;
                        btswrt.setForeground(Color.white) ;
       
                        add(btsb) ;
                        add(btso) ;
          
                        add(btpb) ;
                        add(btpo) ;
          
                        add(btswrt) ;
                        add(new Label(" ", Label.CENTER)) ;
                     }
       
                     public boolean action(Event evt, Object arg) {
                        String label = (String)arg ;
       
                         if(evt.target instanceof Button) {
                            this.handleRefs(evt,arg) ;
                            return true ;
                         }
                         else return false ;
                     } //      end handler
       
                     public void handleRefs(Event evt, Object arg) {
                        String label = (String)arg ;
                        String sdir,sfil,filnam ;
                        String pdir,pfil,pfilnam,fillab ;
                        double mapfac ;
                        int index ;
       
                        if(label.equals("Data File")) {
                           fds.show() ;
                           sdir = fds.getDirectory() ;
                           sfil = fds.getFile();
                           lft.sflnam.setText(sdir + sfil) ;
                           dato = 1;
                        }
                       if(label.equals("Open Data")) {
                          if (dato == 1) {
                            filnam = lft.sflnam.getText() ;
                            try{
                             sfile = new FileOutputStream(filnam) ;
                             savout = new DataOutputStream(sfile) ;
      
                             btso.setBackground(Color.yellow) ;
                             btso.setForeground(Color.black) ;
                             btswrt.setBackground(Color.yellow) ;
                             btswrt.setForeground(Color.black) ;
      
                             counter = 1;
      
                           } catch (IOException n) {
                           }
                         }
                       }
                       if(label.equals("Save Data")) {
      
                          try{
                            if (dato == 1) {
                             savout.writeDouble(camval);
                             savout.writeDouble(thkval);
                             savout.writeDouble(span);
			     savout.writeDouble(chord);
                             savout.writeInt(counter);
                             savout.writeInt(foiltype);
			     savout.writeInt(lunits);
                             counter = counter + 1 ;
         
                             if (counter > 40) {
                                lft.sflnam.setText("Max Number of Models = 40") ;
                             }
                           }
                           if (datp == 1) {
                             if (iprint == 1) {  // file open - print data
                                fillab = upr.inr.labl.getText() ;
                                prnt.println("  ");
                                prnt.println("----------------------------------------- ");
                                prnt.println(fillab);
                                prnt.println("  ");
                                switch(foiltype) {
                                   case 1: {
                                      prnt.println( " Joukowski Airfoil" ) ;
                                      break ;
                                   }
                                   case 2: {
                                      prnt.println( " Elliptical Airfoil" ) ;
                                      break ;
                                   }
                                   case 3: {
                                      prnt.println( " Plate" ) ;
                                      break ;
                                   }
                                }
                                if (foiltype <= 3) {
                                   prnt.println( " Camber = " + String.valueOf(filter3(caminpt)) + " % chord ," ) ;
                                   prnt.println( " Thickness = " + String.valueOf(filter3(thkinpt)) + " % chord ," ) ;
                                   if (lunits == 0) {
                                      prnt.println( " Chord = " + String.valueOf(filter3(chord)) + " ft ," ) ;
                                      prnt.println( " Span = " + String.valueOf(filter3(span)) + " ft ," ) ;
                                      prnt.println( " Surface Area = " + String.valueOf(filter3(area)) +  " sq ft ," ) ;
                                   }
                                   if (lunits == 1) {
                                      prnt.println( " Chord = " + String.valueOf(filter3(chord)) + " m ," ) ;
                                      prnt.println( " Span = " + String.valueOf(filter3(span)) + " m ," ) ;
                                      prnt.println( " Surface Area = " + String.valueOf(filter3(area)) +  " sq m ," ) ;
                                   }
                                }
                                prnt.println( "\n \t Upper Surface \t \t Lower Surface") ;
                                prnt.println( "\t X \t Y \t \t X \t Y \t \t  ");
                                mapfac = 4.0 ;
                                if (stfact >= 1.0) {
                                   for (index = 0; index <= npt2-1; ++ index) {
                                    prnt.println( "\t" +  String.valueOf(filter3(xpl[0][npt2-index]/mapfac)) +  "\t"
                                     + String.valueOf(filter3(ypl[0][npt2-index]/mapfac)) + "\t" 
                                     + "\t"
                                     + String.valueOf(filter3(xpl[0][npt2+index]/mapfac)) + "\t"
                                     + String.valueOf(filter3(ypl[0][npt2+index]/mapfac)) + "\t" 
                                     + "\t"
                                      ) ;
                                   }
                                }
                                if (stfact < 1.0) {
                                   for (index = 0; index <= npt2-1; ++ index) {
                                    prnt.println(  String.valueOf(filter3(xpl[0][npt2-index]/mapfac)) +  "\t"
                                     + String.valueOf(filter3(ypl[0][npt2-index]/mapfac)) + "\t" 
                                     +  " - " + "\t"
                                     +  " - " + "\t" 
                                     + String.valueOf(filter3(xpl[0][npt2+index]/mapfac)) + "\t"
                                     + String.valueOf(filter3(ypl[0][npt2+index]/mapfac)) + "\t" 
                                     +  " - " + "\t"
                                     +  " - " ) ;
                                   }
                                }
                              }
                            }
                           } catch (IOException n) {
                           }
                       }
                       if(label.equals("Print File")) {
                          fdp.show() ;
                          pdir = fdp.getDirectory() ;
                          pfil = fdp.getFile();
                          lft.pflnam.setText(pdir + pfil) ;
                          datp =1 ;
                       }
                       if(label.equals("Open Print")) {
                          if (datp == 1) {
                            pfilnam = lft.pflnam.getText() ;
                            try{
                             pfile = new FileOutputStream(pfilnam) ;
                             prnt = new PrintStream(pfile) ;
        
                             btpo.setBackground(Color.yellow) ;
                             btpo.setForeground(Color.black) ;
      
                             prnt.println("  ");
                             prnt.println(" Design Application Version 1.0h - Dec 08 ");
                             prnt.println("  ");
                             iprint = 1;
                           } catch (IOException n) {
                           }
                         }
                       }
                    } //  end handleRef
                 }  // end Rit
              }  // end Upa
   
              class Dwn extends Panel {
                 Design outerparent ;
                 Button bt3,endit;
                 TextField o1,o2,o3,o4,o5,o6 ;
                 Label l3,l4 ;
                 Choice untch ;
   
                 Dwn (Design target) {
                    outerparent = target ;
                    setLayout(new GridLayout(3,4,5,5)) ;
         
                    l3 = new Label("Area-sq ft", Label.CENTER) ;
                    o3 = new TextField("100.0",5) ;
                    o3.setBackground(Color.black) ;
                    o3.setForeground(Color.yellow) ;
   
                    l4 = new Label("Aspect Ratio", Label.CENTER) ;
                    o4 = new TextField("0.0",5) ;
                    o4.setBackground(Color.black) ;
                    o4.setForeground(Color.yellow) ;
      
                    o1 = new TextField("0.0",5) ;
                    o1.setBackground(Color.black) ;
                    o1.setForeground(Color.yellow) ;

                    o2 = new TextField("0.0",5) ;
                    o2.setBackground(Color.black) ;
                    o2.setForeground(Color.yellow) ;

                    o5 = new TextField("0.0",5) ;
                    o5.setBackground(Color.black) ;
                    o5.setForeground(Color.yellow) ;

                    o6 = new TextField("0.0",5) ;
                    o6.setBackground(Color.black) ;
                    o6.setForeground(Color.yellow) ;

                    bt3 = new Button("Reset") ;
                    bt3.setBackground(Color.magenta) ;
                    bt3.setForeground(Color.white) ;
       
                    endit = new Button("Exit") ;
                    endit.setBackground(Color.red) ;
                    endit.setForeground(Color.white) ;
       
                    untch = new Choice() ;
                    untch.addItem("Imperial Units") ;
                    untch.addItem("Metric Units");
                    untch.setBackground(Color.white) ;
                    untch.setForeground(Color.red) ;
                    untch.select(0) ;
      
                    add(new Label(" ", Label.CENTER)) ;
                    add(new Label(" ", Label.CENTER)) ;
                    add(new Label(" ", Label.CENTER)) ;
                    add(new Label(" ", Label.CENTER)) ;
/*
                    add(o1) ;
                    add(o2) ;
                    add(o5) ;
                    add(o6) ;
*/
                    add(l3) ;
                    add(o3) ;
                    add(l4) ;
                    add(o4) ;
      
                    add(endit) ;
                    add(bt3) ;
                    add(untch) ;
                    add(new Label(" ", Label.CENTER)) ;
                 }
   
                 public boolean action(Event evt, Object arg) {
                   int oldout ;
                   String label = (String)arg ;
      
                   if(evt.target instanceof Button) {
                      this.handleRefs(evt,arg) ;
                      return true ;
                   }
                   if(evt.target instanceof Choice) {
                      lunits  = untch.getSelectedIndex() ;
                   // **** the lunits check MUST come first
                      setUnits () ;
      
                      loadInput() ;
      
                      return true ;
                   }
                   else return false ;
                 } // end action
   
                 public void handleRefs(Event evt, Object arg) {
                   String label = (String)arg ;
                   double mapfac ;
                   int index,datos,datps ;
      
                   if(label.equals("Reset")) {
                      datos = dato ;
                      datps = datp ;
                      solve.setDefaults() ;
                      inp.in.shp.upr.inl.shapch.select(0);
                      inp.in.cyl.inr.shapch.select(0);
                      inp.in.shp.lwr.dwn.untch.select(lunits) ;
                              // **** the lunits check MUST come first
                      setUnits () ;
                      layout.show(out, "first")  ;
                      outopt = 0 ;
                      dato = datos ;
                      datp = datps ;
                     
                      loadInput() ;
                   }
      
                   if(label.equals("Exit")) {
                      fd.dispose() ;
                      System.exit(1) ;
                   }
                 }   //  end handler
              }   //  end Lwr
            }   //  end Dwn
        }  // Shp 

        class Cyl extends Panel {
           Design outerparent ;
           Inl inl ;
           Inr inr ;
   
           Cyl (Design target) {
   
              outerparent = target ;
              setLayout(new GridLayout(1,2,5,5)) ;
   
              inl = new Inl(outerparent) ;
              inr = new Inr(outerparent) ;
   
              add(inl) ;
              add(inr) ;
           }
   
           public void setLims() {
              Double V1 ;
              double v1 ;
              float fl1 ;
              int i1 ;
   
              spinmx = 2.75 * vfsd/vconv /(radius/lconv) ;
              spinmn = -2.75 * vfsd/vconv/(radius/lconv) ;
              if(spin*60.0 < spinmn) {
                  spin = spinmn/60.0 ;
                  fl1 = (float) (spin*60.0)  ;
                  inl.f1.setText(String.valueOf(fl1)) ;
              }
              if(spin*60.0 > spinmx) {
                  spin = spinmx/60.0 ;
                  fl1 = (float) (spin*60.0)  ;
                  inl.f1.setText(String.valueOf(fl1)) ;
              }
              i1 = (int) (((60*spin - spinmn)/(spinmx-spinmn))*1000.) ;
              inr.s1.setValue(i1) ;
           }
   
           class Inl extends Panel {
              Design outerparent ;
              TextField f1,f2 /* ,f3 */;  //TextField used for former Span Scrollbar
              Label l1,l2 /* ,l3 */;   //Label used for former Span Scrollbar
              Label l01,l02 ;
     
              Inl (Design target) {
        
               outerparent = target ;
               setLayout(new GridLayout(5,2,2,10)) ;
   
               l01 = new Label("Cylinder-", Label.RIGHT) ;
               l01.setForeground(Color.blue) ;
               l02 = new Label("Ball Input", Label.LEFT) ;
               l02.setForeground(Color.blue) ;
   
               l1 = new Label("Spin rpm", Label.CENTER) ;
               f1 = new TextField("0.0",5) ;
   
               l2 = new Label("Radius ft", Label.CENTER) ;
               f2 = new TextField(".5",5) ;
   
               /* l3 = new Label("Span ft", Label.CENTER) ;  //See previous comments
               f3 = new TextField("5.0",5) ; */
   
               add(l01) ;
               add(l02) ;
   
               add(l1) ;
               add(f1) ;
   
               add(l2) ;
               add(f2) ;
   
               /* add(l3) ;   //See previous comments
               add(f3) ; */
    
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
            }
   
            public boolean handleEvent(Event evt) {
               Double V1,V2;
               double v1,v2,v3 ;
               float fl1 ;
               int i1,i2;
   
               if(evt.id == Event.ACTION_EVENT) {
                 V1 = Double.valueOf(f1.getText()) ;
                 v1 = V1.doubleValue() ;
                 V2 = Double.valueOf(f2.getText()) ;
                 v2 = V2.doubleValue() ;
   
                 spin = v1 ;
                 if(v1 < spinmn) {
                   spin = v1 = spinmn ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                 }
                 if(v1 > spinmx) {
                   spin = v1 = spinmx ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                 }
                 spin = spin/60.0 ;

                 radius = v2 ;
                 if(v2 < radmn) {
                   radius = v2 = radmn ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                 }
                 if(v2 > radmx) {
                   radius = v2 = radmx ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                 }
                 cyl.setLims() ;
   
                 v3 = span ;
                 if (foiltype == 5) {
                   v3 = radius ;
                   fl1 = (float) v3 ;
                 }
                 spanfac = (int)(fact*span/radius*.3535) ;
                 area = 2.0*radius*span ;
                 if (foiltype ==5) area = 3.1415926 * radius * radius ;
   
                 i1 = (int) (((v1 - spinmn)/(spinmx-spinmn))*1000.) ;
                 i2 = (int) (((v2 - radmn)/(radmx-radmn))*1000.) ;
      
                 inr.s1.setValue(i1) ;
                 inr.s2.setValue(i2) ;
   
                 computeFlow() ;
                 return true ;
               }
               else return false ;
             } // Handler
           }  // Inl 

           class Inr extends Panel {
              Design outerparent ;
              Scrollbar s1,s2 /* ,s3 */;  //Span Scrollbar commented out
              Choice shapch ;
   
              Inr (Design target) {
                int i1,i2;
   
               outerparent = target ;
               setLayout(new GridLayout(5,1,2,10)) ;
   
               i1 = (int) (((spin*60.0 - spinmn)/(spinmx-spinmn))*1000.) ;
               i2 = (int) (((radius - radmn)/(radmx-radmn))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               /* s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);*/  //See previous comment
   
               shapch = new Choice() ;
               shapch.addItem("Airfoil") ;
               shapch.addItem("Ellipse");
               shapch.addItem("Plate");
               shapch.addItem("Cylinder");
               shapch.addItem("Ball");
               shapch.setBackground(Color.white) ;
               shapch.setForeground(Color.blue) ;
               shapch.select(0) ;
   
               add(shapch) ;
               add(s1) ;
               add(s2) ;
               /* add(s3) ;*/    //See previous comment
               add(new Label(" ", Label.CENTER)) ;
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
                int i1,i2;
                double v1,v2,v3;
                float fl1,fl2;
                 
    // Input    for computations
                i1 = s1.getValue() ;
                i2 = s2.getValue() ;
   
                spin = v1 = i1 * (spinmx - spinmn)/ 1000. + spinmn ;
                spin = spin / 60.0 ;
                radius = v2 = i2 * (radmx - radmn)/ 1000. + radmn ;
                if (foiltype == 5) span = v3 = radius ;
                spanfac = (int)(fact*span/radius*.3535) ;
                area = 2.0*radius*span ;
                if (foiltype ==5) area = 3.1415926 * radius * radius ;
                cyl.setLims() ;
   
                fl1 = (float) v1 ;
                fl2 = (float) v2 ;
   
                inl.f1.setText(String.valueOf(fl1)) ;
                inl.f2.setText(String.valueOf(fl2)) ;
         
                computeFlow() ;
              }
   
              public void handleCho(Event evt) {
                int i2 ;
                double v2 ;
                float fl1 ;
   
                foiltype  = shapch.getSelectedIndex() + 1 ;
                if (foiltype >= 4) alfval = 0.0 ;
                if(foiltype <= 2) layin.show(in, "second")  ;
                if(foiltype == 3) {
                   layin.show(in, "second")  ;
                   thkinpt = v2 = thkmn ;
                   thkval  = thkinpt / 25.0 ;
                   fl1 = (float) v2 ;
                   in.shp.upr.inl.f2.setText(String.valueOf(fl1)) ;
                   i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                   in.shp.upr.inr.s2.setValue(i2) ;
                }
                if(foiltype == 4) layin.show(in, "fourth")  ;
                if(foiltype == 5) {
                    span = radius ;
                    area = 3.1415926*radius*radius ;
                    layin.show(in, "fourth")  ;
                    if (viewflg != 0) viewflg = 0 ;
                }
        
                in.shp.upr.inl.shapch.select(foiltype-1);
                layout.show(out, "first")  ;
                outopt = 0 ;
                dispp = 0 ;
                calcrange = 0 ;
   
                loadInput() ;
              } // handler
           }  // Inr
        }  // Cyl 
     }  // In 

     class Viewer extends Canvas  
         implements Runnable{
        Design outerparent ;
        Thread runner ;
        Point locate,anchor;
   
        Viewer (Design target) {
            setBackground(Color.black) ;
            runner = null ;
        } 

        public Insets insets() {
           return new Insets(0,10,0,10) ;
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
                   if (displ != 2) {
                     locate = new Point(x,y) ;
                     yt =  yt + (int) (.2*(locate.y - anchor.y)) ;
                     xt =  xt + (int) (.4*(locate.x - anchor.x))  ;
                     if (xt > 320) xt = 320 ;
                     if (xt < -280) xt = -280 ;
                     if (yt > 300) yt = 300 ;
                     if (yt <-300) yt = -300 ;
                     xt1 = xt + spanfac ;
                     yt1 = yt - spanfac ;
                     xt2 = xt - spanfac;
                     yt2 = yt + spanfac ;
                   }
                   if(displ == 2)  {          // move the rake
                     locate = new Point(x,y) ;
                     xflow = xflow + .01*(locate.x - anchor.x) ;
                     if (xflow < -10.0) xflow = -10.0 ;
                     if (xflow > 0.0) xflow = 0.0 ;
                     computeFlow() ;
                   }
                }
                if (x < 30 ) {   // zoom widget
                  sldloc = y ;
                  if (sldloc < 130) sldloc = 130;
                  if (sldloc > 265) sldloc = 265;
                  fact = 5.0 + (sldloc-130)*1.0 ;
                  spanfac = (int)(2.0*fact*aspr*.3535) ;
                  xt1 = xt + spanfac ;
                  yt1 = yt - spanfac ;
                  xt2 = xt - spanfac;
                  yt2 = yt + spanfac ;
                }
            }
        }

        public void handleb(int x, int y) {
            if (y >= 300 && y <= 320) { 
                if (x >= 0 && x <= 40) {   //find
                     xt = 240;  yt = 165; fact = 15.0 ;
                     sldloc = 140 ;
                     spanfac = (int)(2.0*fact*aspr*.3535) ;
                     xt1 = xt + spanfac ;
                     yt1 = yt - spanfac ;
                     xt2 = xt - spanfac;
                     yt2 = yt + spanfac ;
                }
            }
            view.repaint() ;
        }
   
        public void start() {
           if (runner == null) {
              runner = new Thread(this) ;
              runner.start() ;
           }
           antim = 0 ;                              /* MODS  21 JUL 99 */
           ancol = 1 ;                              /* MODS  27 JUL 99 */
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
                ancol = - ancol ;               /* MODS 27 JUL 99 */
             }
             timer = 135 - (int) (.227 *vfsd/vconv) ;
                                            // make the ball spin
             if (foiltype >= 4) {
                plthg[1] = plthg[1] + spin*spindr*5. ;
                if (plthg[1] < -360.0) {
                   plthg[1] = plthg[1] + 360.0 ;
                }
                if (plthg[1] > 360.0) {
                   plthg[1] = plthg[1] - 360.0 ;
                }
             }
          }
        }

        public void update(Graphics g) {
           view.paint(g) ;
        }
    
        public void paint(Graphics g) {
           int i,j,k,n ;
           int xlabel,ylabel,ind,inmax,inmin ;
           int exes[] = new int[8] ;
           int whys[] = new int[8] ;
           double offx,scalex,offy,scaley,waste,incy,incx;
           double xl,yl,slope,radvec,xvec,yvec ;
           int camx[] = new int[19] ;
           int camy[] = new int[19] ;
           Color col ;
   
           col = new Color(0,0,0) ;
           if(planet == 0) col = Color.cyan ;
           if(planet == 1) col = Color.orange ;
           if(planet == 2) col = Color.green ;
           if(planet >= 3) col = Color.cyan ;
           off1Gg.setColor(Color.black) ;
           off1Gg.fillRect(0,0,500,500) ;
   
 //  wing surface
           off1Gg.setColor(Color.red) ;
           exes[1] = (int) (fact*(xpl[0][npt2])) + xt1 ;
           whys[1] = (int) (fact*(-ypl[0][npt2])) + yt1 ;
           exes[2] = (int) (fact*(xpl[0][npt2])) + xt2 ;
           whys[2] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
           for (i=1 ; i<= npt2-1; ++i) {
              exes[0] = exes[1] ;
              whys[0] = whys[1] ;
              exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt1 ;
              whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt1 ;
              exes[3] = exes[2] ;
              whys[3] = whys[2] ;
              exes[2] = (int) (fact*(xpl[0][npt2-i])) + xt2 ;
              whys[2] = (int) (fact*(-ypl[0][npt2-i])) + yt2 ;
              off1Gg.fillPolygon(exes,whys,4) ;
           }
    
 //   front foil
           off1Gg.setColor(Color.white) ;
           exes[1] = (int) (fact*(xpl[0][npt2])) + xt2 ;
           whys[1] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
           exes[2] = (int) (fact*(xpl[0][npt2])) + xt2 ;
           whys[2] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
           for (i=1 ; i<= npt2-1; ++i) {
              exes[0] = exes[1] ;
              whys[0] = whys[1] ;
              exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt2 ;
              whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt2 ;
              exes[3] = exes[2] ;
              whys[3] = whys[2] ;
              exes[2] = (int) (fact*(xpl[0][npt2+i])) + xt2 ;
              whys[2] = (int) (fact*(-ypl[0][npt2+i])) + yt2 ;
              camx[i] = (exes[1] + exes[2]) / 2 ;
              camy[i] = (whys[1] + whys[2]) / 2 ;
              off1Gg.fillPolygon(exes,whys,4) ;
           }

                                   //  spin the cylinder and ball
           if (foiltype >= 4) {
              exes[0] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                   rval * Math.cos(convdr*(plthg[1] + 180.)))) + xt2 ;
              whys[0] = (int) (fact* (-ypl[0][1] +
                   rval * Math.sin(convdr*(plthg[1] + 180.)))) + yt2 ;
              exes[1] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                   rval * Math.cos(convdr*plthg[1]))) + xt2 ;
              whys[1] = (int) (fact* (-ypl[0][1] +
                   rval * Math.sin(convdr*plthg[1]))) + yt2 ;
              off1Gg.setColor(Color.red) ;
              off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }

 // zoom in
           off1Gg.setColor(Color.green) ;
           off1Gg.drawString("3D View",10,20) ;
           off1Gg.setColor(Color.black) ;
           off1Gg.fillRect(0,130,30,150) ;
           off1Gg.setColor(Color.green) ;
           off1Gg.drawString("Zoom",2,280) ;
           off1Gg.drawLine(15,135,15,265) ;
           off1Gg.fillRect(5,sldloc,20,5) ;
           off1Gg.setColor(Color.yellow) ;
           off1Gg.drawString("Find",5,310) ;
   
           g.drawImage(offImg1,0,0,this) ;   
       }
     } // end Viewer
  }  // Inp 

  class Out extends Panel {
     Design outerparent ;
     Plt plt ;

     Out (Design target) { 
        outerparent = target ;
        layout = new CardLayout() ;
        setLayout(layout) ;

        plt = new Plt(outerparent) ;

        add ("first", plt) ;
     }
 
     class Plt extends Canvas
         implements Runnable{
        Design outerparent ;
        Thread run2 ;
        Point locp,ancp;

        Plt (Design target) { 
           setBackground(Color.blue) ;
           run2 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
           if (x >= 5 && x <= 75) { 
              if (y >= 580 && y <= 600) {   // increase by 2
                plscale = plscale * 2.0 ;
              }
              if (y >= 630 && y <= 650) {   // decrease by 2
                plscale = plscale / 2.0 ;
              }
           }
           out.plt.repaint() ;
        }

        public void start() {
           if (run2 == null) {
              run2 = new Thread(this) ;
              run2.start() ;
           }
        }

        public void run() {
          int timer ;
 
          timer = 100 ;
          while (true) {
             try { Thread.sleep(timer); }
             catch (InterruptedException e) {}
             out.plt.repaint() ;
          }
        }

        public void update(Graphics g) {
           out.plt.paint(g) ;
        }
   
        public void paint(Graphics g) {
            int i,j,k,n ;
            int xlabel,ylabel,ind,inmax,inmin ;
            int exes[] = new int[8] ;
            int whys[] = new int[8] ;
            double offx,scalex,offy,scaley,waste,incy,incx;
            double xl,yl,slope,radvec,xvec,yvec ;
            double scale,ydisp ;
            int camx[] = new int[19] ;
            int camy[] = new int[19] ;
            Color col ;
  
           col = new Color(0,0,0) ;
           off2Gg.setColor(Color.white) ;
           off2Gg.fillRect(0,0,500,1000) ;

                                 // graph paper
           xl = 0.0 +xtp ;
           yl = 0.0 +ytp ;

           off2Gg.setColor(Color.blue) ;
           for (j=0; j<=40; ++j) {
               exes[0] = 0 ; exes[1] = 500 ;
               whys[0] = whys[1] = (int) (yl + (100./graphconv * j));
               off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
               whys[0] = whys[1] = (int) (yl - (100./graphconv * j));
               off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           for (j=0; j<=20; ++j) {
               whys[0] = 0 ; whys[1] = 1000 ;
               exes[0] = exes[1] = (int) (xl + (100./graphconv * j)) ;
               off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
               exes[0] = exes[1] = (int) (xl - (100./graphconv * j)) ;
               off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }

    // top view
           ydisp = 100.0 ;
           inmax = 1 ;
           inmin = 1 ;
           for (n=1; n <= nptc; ++n) {
              if(xpl[0][n] > xpl[0][inmax]) inmax = n ;
              if(xpl[0][n] < xpl[0][inmin]) inmin = n ;
           }
           off2Gg.setColor(Color.black) ;
           exes[0] = (int) (plscale*factp*(xpl[0][inmin])) + xtp ;
           whys[0] = (int) (plscale*100.*(0.0) - ydisp) + ytp ;
           exes[1] = (int) (plscale*factp*(xpl[0][inmax])) + xtp ;
           whys[1] = (int) (plscale*100.*(0.0) - ydisp) + ytp ;
           exes[2] = (int) (plscale*factp*(xpl[0][inmax])) + xtp ;
           whys[2] = (int) (plscale*100.*(-span/lconv) - ydisp) + ytp ;
           exes[3] = (int) (plscale*factp*(xpl[0][inmin])) + xtp ;
           whys[3] = (int) (plscale*100.*(-span/lconv) - ydisp) + ytp ;
           off2Gg.fillPolygon(exes,whys,4) ;
           off2Gg.setColor(Color.green) ;
           off2Gg.drawString("Span",exes[0]+10,(whys[0] + whys[2])/2 ) ;
           if (foiltype <= 3) off2Gg.drawString("Chord",(exes[0] + exes[1])/2 - 20,whys[0]-10) ;
           if (foiltype == 4) off2Gg.drawString("Diameter",exes[2]+10,55) ;
   
     // draw the airfoil geometry
           off2Gg.setColor(Color.black) ;
           exes[1] = (int) (plscale*factp*(xpl[0][npt2])) + xtp ;
           whys[1] = (int) (plscale*factp*(-ypl[0][npt2])) + ytp ;
           exes[2] = (int) (plscale*factp*(xpl[0][npt2])) + xtp ;
           whys[2] = (int) (plscale*factp*(-ypl[0][npt2])) + ytp ;
           for (i=1 ; i<= npt2-1; ++i) {
              exes[0] = exes[1] ;
              whys[0] = whys[1] ;
              exes[1] = (int) (plscale*factp*(xpl[0][npt2-i])) + xtp ;
              whys[1] = (int) (plscale*factp*(-ypl[0][npt2-i])) + ytp ;
              exes[3] = exes[2] ;
              whys[3] = whys[2] ;
              exes[2] = (int) (plscale*factp*(xpl[0][npt2+i])) + xtp ;
              whys[2] = (int) (plscale*factp*(-ypl[0][npt2+i])) + ytp ;
              camx[i] = (exes[1] + exes[2]) / 2 ;
              camy[i] = (whys[1] + whys[2]) / 2 ;
              if (foiltype == 3) {
                  off2Gg.setColor(Color.yellow) ;
                  off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
              else {
                  off2Gg.setColor(Color.black) ;
                  off2Gg.fillPolygon(exes,whys,4) ;
              }
           }
   // put some info on the geometry
           if (displ == 3) {
              if (foiltype <= 3) {
                 off2Gg.setColor(Color.red) ;
                 exes[1] = (int) (plscale*factp*(xpl[0][inmax] -
                       4.0*Math.cos(convdr*alfval)))+xtp;
                 whys[1] = (int) (plscale*factp*(-ypl[0][inmax] -
                       4.0*Math.sin(convdr*alfval)))+ytp;
                 off2Gg.drawLine(exes[1],whys[1],camx[6],camy[6]) ;
                 for (i=6 ; i<= npt2-2; ++i) {
                    off2Gg.drawLine(camx[i],camy[i],camx[i+1],camy[i+1]) ;
                 }
                 off2Gg.setColor(Color.white) ;
                 off2Gg.fillRect(160,615,180,20) ;
                 off2Gg.setColor(Color.red) ;
                 off2Gg.drawString("Mean Camber Line",175,630) ;
              }
              if (foiltype >= 4) {
                 off2Gg.setColor(Color.red) ;
                 exes[0] = (int) (plscale*factp*(xpl[0][1])) + xtp ;
                 whys[0] = (int) (plscale*factp*(-ypl[0][1])) + ytp ;
                 exes[1] = (int) (plscale*factp*(xpl[0][npt2])) +xtp ;
                 whys[1] = (int) (plscale*factp*(-ypl[0][npt2])) + ytp ;
                 off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 off2Gg.drawString("Diameter",exes[0]+20,whys[0]+20) ;
              }
           }
                              //  spin the cylinder and ball
           if (foiltype >= 4) {
              exes[0] = (int) (plscale*factp* (.5*(xpl[0][1] + xpl[0][npt2]) +
                   rval * Math.cos(convdr*(plthg[1] + 180.)))) + xtp ;
              whys[0] = (int) (plscale*factp* (-ypl[0][1] +
                   rval * Math.sin(convdr*(plthg[1] + 180.)))) + ytp ;
              exes[1] = (int) (plscale*factp* (.5*(xpl[0][1] + xpl[0][npt2]) +
                   rval * Math.cos(convdr*plthg[1]))) + xtp ;
              whys[1] = (int) (plscale*factp* (-ypl[0][1] +
                   rval * Math.sin(convdr*plthg[1]))) + ytp ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
   
           off2Gg.setColor(Color.white) ;
           off2Gg.fillRect(5,5,60,20) ;
           off2Gg.setColor(Color.black) ;
           off2Gg.drawString("Layout",10,20) ;
           off2Gg.setColor(Color.white) ;
           off2Gg.fillRect(5,590,120,60) ;
           off2Gg.setColor(Color.red) ;
           off2Gg.drawString("  Double",10,605) ;
           off2Gg.setColor(Color.blue) ;
           off2Gg.drawString("Scale =",10,625) ;
           off2Gg.drawString(String.valueOf(filter3(1.0/plscale)),70,625) ;
           if (lunits == 0) off2Gg.drawString("ft",110,625) ;
           if (lunits == 1) off2Gg.drawString("dm",110,625) ;
           off2Gg.setColor(Color.red) ;
           off2Gg.drawString("  Half",10,645) ;

           g.drawImage(offImg2,0,0,this) ;   
       }
     }     // Plt 
  } // Out 

 
 public static void main(String args[]) {
    Design dfoil = new Design() ;

    fd = new Frame("Design Application Version 1.0h") ;
    fd.add("Center", dfoil) ;
    fd.resize(900,700);
    fd.show() ;

    dfoil.init() ;
    dfoil.start() ;
 }
}
