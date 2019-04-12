/*
                      Tunnel  - Wind Tunnel Test
   
                           A Java Application
            to test Kutta-Joukowski Airfoils from the designer
                          Derived from FoilSim II

                     Version 1.0j   - 29 Jun 09

                         Written by Tom Benson
                       NASA Glenn Research Center

                           and Anthony Vila
                        Vanderbilt University

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
               - this is the tunnel test module
               * modify performance plot in stall
               * clear the con panel 
               *    put the probe panel in its ploace
               * add the load button - file from from design
                    clean up
               * add the record button/file to the analysis
               *  add variety of tests 

                                           TJB  29 Jun 09

New test A - 
			-Change code to read in span and chord values
			-Change code to read in lunits value

							AJV  5/27/09
New test H -
			-Set spanfac value to constant 2.0*50.0*0.3535
			-Fix units on panel to correspond to input lunits values
			-Rescaling Tunnel model to display chord changes in uploaded models
							
							AJV 5/29/09
New test Z -
			-Expand flow lines to match changes in chord length

							AJV 6/4/09
New test CC -
			-Add Output Buttons to plot points to output file

							AJV 6/18/09

*/

import java.awt.*;
import java.lang.Math ;
import java.io.* ;
import java.awt.event.*;

public class Tunnel extends java.applet.Applet {
 
   static double convdr = 3.1415926/180. ;
   static double pid2 = 3.1415926/2.0 ;
   static double rval,ycval,xcval,gamval,alfval,thkval,camval,chrd,clift ;
   static double thkinpt,caminpt ;                 /* MODS 10 Sep 99 */
   static double leg,teg,lem,tem;
   static double usq,vsq,alt,altmax,area,armax,armin ;
   static double chord,span,aspr,arold,chrdold,spnold ; 
   static double g0,q0,ps0,pt0,ts0,rho,rlhum,temf,presm ;
   static double psin,tsin,psmin,psmax,tsmin,tsmax ;
   static double lyg,lrg,lthg,lxgt,lygt,lrgt,lthgt,lxgtc,lygtc;
   static double lxm,lym,lxmt,lymt,vxdir;/* MOD 20 Jul */
   static double deltb,xflow ;             /* MODS  20 Jul 99 */
   static double delx,delt,vfsd,spin,spindr,yoff,radius ;
   static double vel,pres,lift,side,omega,radcrv,relsy,angr ;
   static double stfact ;

   static double rg[][]  = new double[50][80] ; 
   static double thg[][] = new double[50][80] ; 
   static double xg[][]  = new double[50][80] ; 
   static double yg[][]  = new double[50][80] ; 
   static double xm[][]  = new double[50][80] ; 
   static double ym[][]  = new double[50][80] ; 
   static double xpl[][]  = new double[50][80] ; 
   static double ypl[][]  = new double[50][80] ; 
   static double xgc[][]  = new double[50][80] ; 
   static double ygc[][]  = new double[50][80] ; 
   static double xplg[][]  = new double[50][80] ; 
   static double yplg[][]  = new double[50][80] ; 
   static double plp[]   = new double[80] ;
   static double plv[]   = new double[80] ;

   static int dftp[]     = new int[40] ;
   static double dcam[]  = new double[40] ;
   static double dthk[]  = new double[40] ;
   static double dspan[]  = new double[40] ;
   static double dchrd[]  = new double[40] ;
   static int dlunits[] = new int[40];

   int inptopt,outopt,iprint,nummod ;
   int nptc,npt2,nlnc,nln2,rdflag,browflag,probflag,anflag;
   int foiltype,flflag,lunits,lftout,planet ;
   int displ,viewflg,dispp,dout,antim,ancol,sldloc; 
   int calcrange,arcor ;
   int pointSet, datp, dato ;
       /* units data */
   static double vmn,almn,angmn,vmx,almx,angmx ;
   static double camn,thkmn,camx,thkmx ;
   static double chrdmn,armn,chrdmx,armx ;
   static double radmn,spinmn,radmx,spinmx ;
   static double psmn,psmx,tsmn,tsmx ;
   static double vconv,vmax ;
   static double pconv,pmax,pmin,lconv,rconv,fconv,fmax,fmaxb;
   static double piconv,ticonv ;
   int lflag,gflag,plscale,nond;
       /*  plot & probe data */
   static double fact,xpval,ypval,pbval,factp,chrdfac;
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
   Viewer view ;
   Con con ;
   In in ;
   Out out ;
   CardLayout layin,layout,layplt,layperf ;
   Image offImg1 ;
   Graphics off1Gg ;
   Image offImg2 ;
   Graphics off2Gg ;
   Image offImg3 ;
   Graphics off3Gg ;

   static Frame f ;
   static PrintStream prnt ;
   static OutputStream pfile ;
   static InputStream dfile ;
   static OutputStream dafile ;
   static DataInputStream datin ;
   static DataOutputStream datout ;

   public void init() {
     int i;
     solve = new Solver() ;

     offImg1 = createImage(this.size().width,
                      this.size().height) ;
     off1Gg = offImg1.getGraphics() ;
     offImg2 = createImage(this.size().width,
                      this.size().height) ;
     off2Gg = offImg2.getGraphics() ;
     offImg3 = createImage(this.size().width,
                      this.size().height) ;
     off3Gg = offImg3.getGraphics() ;

     setLayout(new GridLayout(2,2,5,5)) ;

     solve.setDefaults () ;

     view  = new Viewer(this) ;
     con = new Con(this) ;
     in = new In(this) ;
     out = new Out(this) ;

     add(view) ;
     add(out) ;
     add(in) ;
     add(con) ;
 
     f.show() ;

     solve.getFreeStream ();
     computeFlow () ;
     view.start() ;
     out.plt.start() ;
  }
 
  public Insets insets() {
     return new Insets(5,5,5,5) ;
  }

  public void computeFlow() { 

     solve.getFreeStream () ;

     solve.getCirc ();                   /* get circulation */
     solve.getGeom ();                   /* get geometry*/
     solve.genFlow () ;
 
     solve.getProbe() ;
 
     loadOut() ;

     out.plt.loadPlot() ;
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
       double alts,ares,pss,tss ;

       alts = alt / lconv ;
       chords = chord / lconv ;
       spans = span / lconv ;
       ares = area /lconv/lconv ;
       aros = arold /lconv/lconv ;
       chos = chrdold / lconv ;
       spos = spnold / lconv ;
       ovs = vfsd / vconv ;
       rads = radius / lconv ;
       pss = psin / piconv;
       tss = tsin / ticonv;

       switch (lunits) {
          case 0: {                             /* English */
            lconv = 1.;                      /*  feet    */
            vconv = .6818; vmax = 250.;   /*  mph  */
            if (planet == 2) vmax = 50. ;
            fconv = 1.0; fmax = 100000.; fmaxb = .5;  /* pounds   */
            piconv = 1.0  ;                   /* lb/sq in */
            ticonv = 1.0 ;
            pconv = 14.7  ;                   /* lb/sq in */
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                    /* meters */
            vconv = 1.097; vmax = 400. ;   /* km/hr  */
            if (planet == 2) vmax = 80. ;
            fconv = 4.448 ; fmax = 500000.; fmaxb = 2.5; /* newtons */
            piconv = .04788 ;               /* kilo-pascals */
            ticonv = .5555555 ;
            pconv = 101.3 ;               /* kilo-pascals */
            break ;
          }
       }

       psin = pss * piconv ;
       tsin = tss * ticonv ;
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
       int i1,i2,i3,i4,i5,i6 ;
       double v1,v2,v3,v4,v5,v6 ;
       float fl1,fl2,fl3,fl4,fl5,fl6 ;
       String outarea,outlngth ;

       outlngth = " ft" ;
       if (lunits == 1) outlngth = " m" ;
       outarea = " ft^2" ;
       if (lunits == 1) outarea = " m^2" ;

                  //  dimensional
       if (lunits == 0) {
           in.flt.upr.inl.l1.setText("Speed-mph") ;
           in.flt.upr.inl.l2.setText("Pressure-psf") ;
   //        in.flt.upr.inl.l4.setText("Temperature - R") ;
           in.cyl.inl.l2.setText("Radius ft") ;
           in.cyl.inl.l3.setText("Span ft") ;
       }
       if (lunits == 1) {
           in.flt.upr.inl.l1.setText("Speed-km/h") ;
           in.flt.upr.inl.l2.setText("Pressure-kPa") ;
  //         in.flt.upr.inl.l4.setText("Temperature - K") ;
           in.cyl.inl.l2.setText("Radius m") ;
           in.cyl.inl.l3.setText("Span m") ;
       }
       v4 = vfsd ;
       vmn = 0.0;   vmx= vmax ;
       v5 = psin ;
       psmn = psmin*piconv ;  psmx = psmax*piconv ;
       v6 = radius ;
       radmn = .05*lconv;  radmx = 5.0*lconv ;
       aspr = span/chord ;
       area = span * chord ;
       spanfac = (int)(4.0*50.0*.3535) ;

       fl4 = filter3(v4) ;
       fl5 = filter3(v5) ;
       fl6 = (float) v6 ;
   
       in.flt.upr.inl.f1.setText(String.valueOf(fl4)) ;
       in.flt.upr.inl.f2.setText(String.valueOf(fl5)) ;
       in.cyl.inl.f2.setText(String.valueOf(fl6)) ;
   
       i4 = (int) (((v4 - vmn)/(vmx-vmn))*1000.) ;
       i5 = (int) (((v5 - psmn)/(psmx-psmn))*1000.) ;
       i6 = (int) (((v6 - radmn)/(radmx-radmn))*1000.) ;

       in.flt.upr.inr.s1.setValue(i4) ;
       in.flt.upr.inr.s2.setValue(i5) ;
       in.cyl.inr.s2.setValue(i6) ;
/*
       v5 = tsin ;
       tsmn = tsmin*ticonv ;  tsmx = tsmax*ticonv ;
       fl5 = filter0(v5) ;
       in.flt.upr.inl.f4.setText(String.valueOf(fl5)) ;
       i5 = (int) (((v5 - tsmn)/(tsmx-tsmn))*1000.) ;
       in.flt.upr.inr.s4.setValue(i5) ;
*/

                //  non-dimensional
       v3 = alfval ;
       v4 = spin*60.0 ;

       fl3 = (float) v3 ;
       fl4 = (float) v4 ;

       in.flt.upr.inl.f3.setText(String.valueOf(fl3)) ;
       in.cyl.inl.f1.setText(String.valueOf(fl4)) ;

       i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
       i4 = (int) (((v4 - spinmn)/(spinmx-spinmn))*1000.) ;
     
       in.flt.upr.inr.s3.setValue(i3) ;
       in.cyl.inr.s1.setValue(i4) ;

       con.dwn.o2.setText(String.valueOf(filter3(camval * 25.0)) ) ;
       con.dwn.o4.setText(String.valueOf(filter3(thkval * 25.0)) ) ;
       con.dwn.o5.setText(String.valueOf(filter3(aspr)) ) ;
       con.dwn.o1.setText(String.valueOf(filter3(chord)) + outlngth) ;
       con.dwn.o3.setText(String.valueOf(filter3(span)) + outlngth) ;
       con.dwn.o6.setText(String.valueOf(filter3(area)) + outarea) ;

//        con.dwn.o4.setText(String.valueOf(filter3(clift))) ;
       computeFlow() ;
       return ;
  }

  public void loadOut() {   // output routine
     String outfor,outden,outpres,outarea ;

     outfor = " lbs" ;
     if (lunits == 1) outfor = " N" ;
     outden = " slug/ft^3" ;
     if (lunits == 1) outden = " kg/m^3" ;
     outpres = " lbs/ft^2" ;
     if (lunits == 1) outpres = " kPa" ;
     outarea = " ft^2" ;
     if (lunits == 1) outarea = " m^2" ;
     area = span*chord;

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

//        con.dwn.o4.setText(String.valueOf(filter3(clift))) ;

        if (lftout == 0) {
          lift = clift * q0 * area / lconv / lconv ; /* lift in lbs */
          lift = lift * fconv ;
          if (Math.abs(lift) <= 10.0) {
             con.dwn.o10.setText(String.valueOf(filter3(lift)) + outfor) ;
          }
          if (Math.abs(lift) > 10.0) {
             con.dwn.o10.setText(String.valueOf(filter0(lift)) + outfor) ;
          }
        }
     }
 
     switch (lunits)  {
       case 0: {                             /* English */
           con.dwn.o9.setText(String.valueOf(filter5(rho))+outden) ;
           con.dwn.o7.setText(String.valueOf(filter3(q0)) + outpres) ;
           con.dwn.o8.setText(String.valueOf(filter3(pt0)) + outpres) ;
           break;
        }
        case 1: {                             /* Metric */
           con.dwn.o9.setText(String.valueOf(filter3(rho*515.4))+outden) ;
           con.dwn.o7.setText(String.valueOf(filter3(q0*.04787)) + outpres) ;
           con.dwn.o8.setText(String.valueOf(filter3(pt0*.04787)) + outpres) ;
           break ;
        }
     }

     return ;
  }

  public void loadProbe() {   // probe output routine

     pbval = 0.0 ;
     if (pboflag == 1) pbval = vel * vfsd ;           // velocity
     if (pboflag == 2) pbval = ((ps0 + pres * q0)/2116.) * pconv ; // pressure
 
     con.prb.r.l2.repaint() ;
     return ;
  }

  class Solver {
 
     Solver () {
     }

     public void setDefaults() {

        dato = 0;
        datp = 0;
        arcor = 0 ;
        lunits = 0 ;
        lftout = 0 ;
        inptopt = 0 ;
        outopt = 0 ;
        nummod = 1 ;
        nlnc = 45 ;
        nln2 = nlnc/2 + 1 ;
        nptc = 75 ;
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
        displ   = 1 ;                            
        viewflg = 2 ;
        dispp = 0 ;
        calcrange = 0 ;
        dout = 0 ;
        stfact = 1.0 ;
//        factp = factp * chord/chrdold ;
 
        xpval = 2.1;
        ypval = -.5 ;
        pboflag = 0 ;
        xflow = -20.0;                             /* MODS  20 Jul 99 */

        pconv = 14.7;
        piconv = 1.0;
        ticonv = 1.0;
        pmin = .5 ;
        pmax = 1.0 ;
        fconv = 1.0 ;
        fmax = 100000. ;
        fmaxb = .50 ;
        vconv = .6818 ;
        vfsd = 0.0 ;
        vmax = 250. ;
        lconv = 1.0 ;

        planet = 3 ;
        psin = 2116 ;
        psmin = 100 ;
        psmax = 2500. ;
        tsin = 518.6 ;
        tsmin = 350. ;
        tsmax = 660. ;

        alt = 0.0 ;
        altmax = 50000. ;
        chrdold = chord = 1.0 ;
        spnold = span = 3.281 ;
	  aspr = span/chord;
        arold = area = .5*3.281 ;
        armax = 2500.01 ;
        armin = .01 ;                 /* MODS 9 SEP 99 */
 
        xt = 225;  yt = 140;
        sldloc = 52 ;
        xtp = 95; ytp = 240; factp = 40.0 ;
	  chrdfac = Math.sqrt(chord);  //Adds a chord factor to scale the foil according to the chord
	  fact = 32.0*chrdfac;  //chord is the only factor for the fact variable
        spanfac = (int)(4.0*50.0*.3535) ;
        xt1 = xt + spanfac ;
        yt1 = yt - spanfac ;
        xt2 = xt - spanfac;
        yt2 = yt + spanfac ;
        plthg[1] = 0.0 ;
 
        probflag = 2 ;
        anflag = 1 ;
        vmn = 0.0;     vmx = 250.0 ;
        almn = 0.0;    almx = 50000.0 ;
        angmn = -20.0; angmx = 20.0 ;
        camn = -25.0;  camx = 25.0 ;
        thkmn = 1.0; thkmx = 26.0 ;
        chrdmn = 5./(2.54*12) ;  chrdmx = 3.281;
        armn = .01 ;  armx = 2500.01 ;
        spinmn = -1500.0;   spinmx = 1500.0 ;
        radmn = .05;   radmx = 5.0 ;
        psmn = psmin;  psmx = psmax;
        tsmn = tsmin;  tsmx = tsmax;

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
       ps0 = psin / piconv ;
       ts0 = tsin / ticonv ;
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
           rnew = 20.0 - (20.0 - rval)*Math.sin(pid2*(index-1)/(npt2-1)) ;
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
           rnew = 20.0 + .01 - (20.0 - rval)*Math.cos(pid2*(index-1)/(npt2-1)) ;
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

  class Con extends Panel {
     Tunnel outerparent ;
     Prb prb ;
     Dwn dwn ;

     Con (Tunnel target) {
        outerparent = target ;
        setLayout(new GridLayout(2,1,5,5)) ;

        prb = new Prb(outerparent) ;
        dwn = new Dwn(outerparent) ;

        add(prb) ;
        add(dwn) ;
     }

     class Prb extends Panel {
        Tunnel outerparent ;
        L l ;
        R r ;

        Prb (Tunnel target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           l = new L(outerparent) ;
           r = new R(outerparent) ;

           add(l) ;
           add(r) ;
        }

        class L extends Panel {
           Tunnel outerparent ;
           Button bt0,bt1,bt2,bt3,bt4 ;
     
           L (Tunnel target) {
            outerparent = target ;
            setLayout(new GridLayout(5,1,2,2)) ;

            bt0 = new Button("Probe ON") ;
            bt0.setBackground(Color.white) ;
            bt0.setForeground(Color.blue) ;
            bt1 = new Button("Velocity") ;
            bt1.setBackground(Color.white) ;
            bt1.setForeground(Color.blue) ;
            bt2 = new Button("Pressure") ;
            bt2.setBackground(Color.white) ;
            bt2.setForeground(Color.blue) ;
            bt3 = new Button("Smoke") ;
            bt3.setBackground(Color.white) ;
            bt3.setForeground(Color.blue) ;
            bt4 = new Button("Probe OFF") ;
            bt4.setBackground(Color.red) ;
            bt4.setForeground(Color.white) ;

            add(bt0) ;
            add(bt1) ;
            add(bt2) ;
            add(bt3) ;
            add(bt4) ;
          }

          public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Button) {
               String label = (String)arg ;
               if(label.equals("Probe ON")) {
                   pboflag = 1 ;
                   bt0.setBackground(Color.green) ;
                   bt0.setForeground(Color.black) ;
                   bt4.setBackground(Color.white) ;
                   bt4.setForeground(Color.red) ;
               }
               if(label.equals("Velocity")) {
                   pboflag = 1 ;
                   bt0.setBackground(Color.green) ;
                   bt0.setForeground(Color.black) ;
                   bt4.setBackground(Color.white) ;
                   bt4.setForeground(Color.red) ;
               }
               if(label.equals("Pressure")) {
                   pboflag = 2 ;
                   bt0.setBackground(Color.green) ;
                   bt0.setForeground(Color.black) ;
                   bt4.setBackground(Color.white) ;
                   bt4.setForeground(Color.red) ;
               }
               if(label.equals("Smoke")) {
                   pboflag = 3 ;
                   bt0.setBackground(Color.green) ;
                   bt0.setForeground(Color.black) ;
                   bt4.setBackground(Color.white) ;
                   bt4.setForeground(Color.red) ;
               }
               if(label.equals("Probe OFF")) {
                   pboflag = 0 ;
                   bt0.setBackground(Color.white) ;
                   bt0.setForeground(Color.blue) ;
                   bt4.setBackground(Color.red) ;
                   bt4.setForeground(Color.white) ;
               }

               computeFlow() ;
               return true ;
            }
            else return false ;
          } // Handler
        }  // Inl

        class R extends Panel {
            Tunnel outerparent ;
            Scrollbar s1,s2;
            L2 l2;

            R (Tunnel target) {

             outerparent = target ;
             setLayout(new BorderLayout(5,5)) ;

             s1 = new Scrollbar(Scrollbar.VERTICAL,550,10,0,1000);
             s2 = new Scrollbar(Scrollbar.HORIZONTAL,550,10,0,1000);

             l2 = new L2(outerparent) ;

             add("West",s1) ;
             add("South",s2) ;
             add("Center",l2) ;
//             add("North",bt4) ;
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
             int i1,i2 ;

             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             ypval = 5.0 - i1 * 10.0/ 1000. ;
             xpval = i2 * 20.0/ 1000. -10.0 ;
    
             computeFlow() ;
           }

           class L2 extends Canvas  {
              Tunnel outerparent ;

              L2 (Tunnel target) {
                setBackground(Color.black) ;
              }

              public void update(Graphics g) {
                con.prb.r.l2.paint(g) ;
              }

              public void paint(Graphics g) {
                int ex,ey,index ;
                double pbout ;
    
                off3Gg.setColor(Color.black) ;
                off3Gg.fillRect(0,0,250,150) ;

                if (pboflag == 0 || pboflag == 3)off3Gg.setColor(Color.gray);
                if (pboflag == 1 || pboflag == 2)off3Gg.setColor(Color.yellow) ;
                off3Gg.fillArc(60,30,80,80,-23,227) ;
                off3Gg.setColor(Color.black) ;
         // tick marks
                for (index = 1; index <= 4; ++ index) {
                    ex = 100 + (int) (50.0 * Math.cos(convdr * (-22.5 + 45.0 * index))) ;
                    ey = 70 - (int) (50.0 * Math.sin(convdr * (-22.5 + 45.0 * index))) ;
                    off3Gg.drawLine(80,70,ex,ey) ;
                }
                off3Gg.fillArc(65,35,70,70,-25,235) ;
      
                off3Gg.setColor(Color.yellow) ;
                off3Gg.drawString("0",50,95) ;
                off3Gg.drawString("2",50,55) ;
                off3Gg.drawString("4",75,30) ;
                off3Gg.drawString("6",115,30) ;
                off3Gg.drawString("8",140,55) ;
                off3Gg.drawString("10",140,95) ;

                off3Gg.setColor(Color.green) ;
                if (pboflag == 1) {
                    off3Gg.drawString("Velocity",80,15) ;
                    if (lunits == 0) off3Gg.drawString("mph",90,125) ;
                    if (lunits == 1) off3Gg.drawString("km/h",90,125) ;
                }
                if (pboflag == 2) {
                    off3Gg.drawString("Pressure",70,15) ;
                    if (lunits == 0) off3Gg.drawString("psi",90,125) ;
                    if (lunits == 1) off3Gg.drawString("kPa",90,125) ;
                }

                off3Gg.setColor(Color.green) ;
                off3Gg.drawString("x 10",105,110) ;

                ex = 100 ;
                ey = 70 ;
               
                pbout = 0.0 ;
                if (pbval <= .001) {
                   pbout = pbval * 1000. ;
                   off3Gg.drawString("-4",130,105) ;
                }
                if (pbval <= .01 && pbval > .001) {
                   pbout = pbval * 100. ;
                   off3Gg.drawString("-3",130,105) ;
                }
                if (pbval <= .1 && pbval > .01) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-2",130,105) ;
                }
                if (pbval <= 1 && pbval > .1) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-1",130,105) ;
                }
                if (pbval <= 10 && pbval > 1) {
                   pbout = pbval  ;
                   off3Gg.drawString("0",130,105) ;
                }
                if (pbval <= 100 && pbval > 10) {
                   pbout = pbval * .1 ;
                   off3Gg.drawString("1",130,105) ;
                }
                if (pbval <= 1000 && pbval > 100) {
                   pbout = pbval * .01 ;
                   off3Gg.drawString("2",130,105) ;
                }
                if (pbval > 1000) {
                   pbout = pbval * .001 ;
                   off3Gg.drawString("3",130,105) ;
                }
                off3Gg.setColor(Color.green) ;
                off3Gg.drawString(String.valueOf(filter3(pbout)),70,110) ;

                off3Gg.setColor(Color.yellow) ;
                ex = 100 - (int) (30.0 * Math.cos(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                ey = 70 - (int) (30.0 * Math.sin(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                off3Gg.drawLine(100,70,ex,ey) ;

                g.drawImage(offImg3,0,0,this) ;
              }
           } //L2
        }  // Inr
     }  // Prb

     class Dwn extends Panel {
        Tunnel outerparent ;
        TextField o1,o2,o3,o4,o5,o6,o7,o8,o9,o10 ;
        Label l1,l2,l3,l4,l5,l6,l7,l8,l9,l10 ;
        Label lmod, lmod2 ;
        TextField mod;
        Button btload;
  
        Dwn (Tunnel target) {
          outerparent = target ;
          setLayout(new GridLayout(6,4,5,5)) ;
  
          lmod = new Label("Model #", Label.RIGHT) ;
          lmod.setForeground(Color.blue) ;
          lmod2 = new Label("Select ->", Label.RIGHT) ;
          lmod2.setForeground(Color.blue) ;

          mod = new TextField("1",5) ;
          mod.setBackground(Color.white) ;
          mod.setForeground(Color.blue) ;
 
          btload = new Button("Install Model") ;
          btload.setBackground(Color.blue) ;
          btload.setForeground(Color.white) ;
 
          l1 = new Label("Chord", Label.CENTER) ;
          o1 = new TextField(String.valueOf(filter3(chord)),5) ;
          o1.setBackground(Color.black) ;
          o1.setForeground(Color.yellow) ;
  
          l2 = new Label("Camber", Label.CENTER) ;
          o2 = new TextField("0.0",5) ;
          o2.setBackground(Color.black) ;
          o2.setForeground(Color.yellow) ;
  
          l3 = new Label("Span ", Label.CENTER) ;
          o3 = new TextField(String.valueOf(filter3(span)),5) ;
          o3.setBackground(Color.black) ;
          o3.setForeground(Color.yellow) ;
  
          l4 = new Label("Thickness ", Label.CENTER) ;
          o4 = new TextField("12.5",5) ;
          o4.setBackground(Color.black) ;
          o4.setForeground(Color.yellow) ;
  
          l5 = new Label("Aspect Ratio ", Label.CENTER) ;
          o5 = new TextField(String.valueOf(filter3(aspr)),5) ;
          o5.setBackground(Color.black) ;
          o5.setForeground(Color.yellow) ;
  
          l6 = new Label("Wing Area", Label.CENTER) ;
          o6 = new TextField(String.valueOf(filter3(area)),5) ;
          o6.setBackground(Color.black) ;
          o6.setForeground(Color.yellow) ;

          l7 = new Label("Dynamic Pressure ", Label.CENTER) ;
          o7 = new TextField("12.5",5) ;
          o7.setBackground(Color.black) ;
          o7.setForeground(Color.yellow) ;
  
          l8 = new Label("Total Pressure ", Label.CENTER) ;
          o8 = new TextField("1.5",5) ;
          o8.setBackground(Color.black) ;
          o8.setForeground(Color.yellow) ;

          l9 = new Label("Density", Label.CENTER) ;
          o9 = new TextField(".00237",5) ;
          o9.setBackground(Color.black) ;
          o9.setForeground(Color.yellow) ;
  
          l10 = new Label("Lift ", Label.CENTER) ;
          o10 = new TextField("12.5",5) ;
          o10.setBackground(Color.black) ;
          o10.setForeground(Color.green) ;

          add(l9) ;
          add(o9) ;
          add(l10) ;
          add(o10) ;

          add(l7) ;
          add(o7) ;
          add(l8) ;
          add(o8) ;

          add(lmod2);
          add(lmod) ;
          add(mod) ;
          add(btload) ;

          add(l1) ;
          add(o1) ;
          add(l2) ;
          add(o2) ;

          add(l3) ;
          add(o3) ;
          add(l4) ;
          add(o4) ;

          add(l5) ;
          add(o5) ;
          add(l6) ;
          add(o6) ;
        }

        public boolean action(Event evt, Object arg) {
          if(evt.target instanceof Button) {
             this.handleRefs(evt,arg) ;
             return true ;
          }
          else return false ;
        } //  handler

        public void handleRefs(Event evt, Object arg) {
            String label = (String)arg ;
            Double V1 ;
            double v1;
            int i1 ;

            if(label.equals("Install Model")) {
               V1 = Double.valueOf(mod.getText()) ;
               v1 = V1.doubleValue() ;
               i1 = (int) v1 ;

               if(i1 > nummod) {
                 i1 = nummod ;
                 mod.setText(String.valueOf(i1)) ;
               }
               foiltype = dftp[i1] ;
               lunits = dlunits[i1] ;
		   setUnits();
		   if (lunits == 0)  in.flt.lwr.untch.select(0);
		   if (lunits == 1)  in.flt.lwr.untch.select(1);
               camval = dcam[i1] ;
               thkval = dthk[i1] ;
               span = dspan[i1] ;
		   chord = dchrd[i1] ;  
		   chrdfac = Math.sqrt(chord/lconv);	   
	 	   fact = 32.0*chrdfac;

		   loadInput();
            }
         }
     } //  end Dwn
  } // Con

  class In extends Panel {
     Tunnel outerparent ;
     Flt flt ;
     Cyl cyl ;
     Filep filep ;

     In (Tunnel target) { 
        outerparent = target ;
        layin = new CardLayout() ;
        setLayout(layin) ;

        flt = new Flt(outerparent) ; 
        cyl = new Cyl(outerparent) ;
        filep = new Filep(outerparent) ;

        add ("first", flt) ;
        add ("fourth", cyl) ;
        add ("sixth", filep) ;
     }
 
     class Flt extends Panel {
        Tunnel outerparent ;
        Upr upr ;
        Lwr lwr ;

        Flt (Tunnel target) {

           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           upr = new Upr(outerparent) ;
           lwr = new Lwr(outerparent) ;

           add(upr) ;
           add(lwr) ;
        }

        class Upr extends Panel {
           Tunnel outerparent ;
           Inl inl ;
           Inr inr ;

           Upr (Tunnel target) {

              outerparent = target ;
              setLayout(new GridLayout(1,2,5,5)) ;

              inl = new Inl(outerparent) ;
              inr = new Inr(outerparent) ;

              add(inl) ;
              add(inr) ;
           }

           class Inl extends Panel {
              Tunnel outerparent ;
              TextField f1,f2,f3,f4 ;
              Label l1,l2,l3,l4,l5 ;
              Button brbt,bto,bdat,bdato,bprt,bprto;
              FileDialog fdd;
        
              Inl (Tunnel target) {
    
               outerparent = target ;
               setLayout(new GridLayout(6,2,2,5)) ;

               fdd = new FileDialog(f) ;
               l1 = new Label("Speed-mph", Label.CENTER) ;
               f1 = new TextField("0.0",5) ;
   
               l2 = new Label("Pressure- psf", Label.CENTER) ;
               f2 = new TextField("2116",5) ;

               l3 = new Label("Angle-deg", Label.CENTER) ;
               f3 = new TextField("0.0",5) ;

               l4 = new Label("Temperature - R", Label.CENTER) ;
               f4 = new TextField("518.6",5) ;

               brbt = new Button("Model File") ;
               brbt.setBackground(Color.blue) ;
               brbt.setForeground(Color.white) ;

               bto = new Button("Open File") ;
               bto.setBackground(Color.blue) ;
               bto.setForeground(Color.white) ;

               bdat = new Button("Data File") ;
               bdat.setBackground(Color.blue) ;
               bdat.setForeground(Color.white) ;

               bdato = new Button("Open Data") ;
               bdato.setBackground(Color.blue) ;
               bdato.setForeground(Color.white) ;

               bprt = new Button("Print File") ;
               bprt.setBackground(Color.blue) ;
               bprt.setForeground(Color.white) ;

               bprto = new Button("Open Print") ;
               bprto.setBackground(Color.blue) ;
               bprto.setForeground(Color.white) ;

               add(l1) ;
               add(f1) ;
   
               add(l3) ;
               add(f3) ;
   
               add(l2) ;
               add(f2) ;
   
  //             add(l4) ;
  //             add(f4) ;

               add(brbt) ;
               add(bto) ;

               add(bdat) ;
               add(bdato) ;

               add(bprt) ;
               add(bprto) ;
             }
   
             public boolean action(Event evt, Object arg) {
               if(evt.target instanceof TextField) {
                  this.handleText(evt) ;
                  return true ;
               }
               if(evt.target instanceof Button) {
                  this.handleBut(evt,arg) ;
                  return true ;
               }
               else return false ;
             } //  handler

             public void  handleText(Event evt) {
               Double V1,V2,V3,V4 ;
               double v1,v2,v3,v4 ;
               float fl1 ;
               int i1,i2,i3,i4 ;
   
               V1 = Double.valueOf(f1.getText()) ;
               v1 = V1.doubleValue() ;
               V2 = Double.valueOf(f2.getText()) ;
               v2 = V2.doubleValue() ;
               V3 = Double.valueOf(f3.getText()) ;
               v3 = V3.doubleValue() ;
               V4 = Double.valueOf(f4.getText()) ;
               v4 = V4.doubleValue() ;
 
               vfsd = v1 ;
               if(v1 < vmn) {
                 vfsd = v1 = vmn ;
                 fl1 = (float) v1 ;
                 f1.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx) {
                 vfsd = v1 = vmx ;
                 fl1 = (float) v1 ;
                 f1.setText(String.valueOf(fl1)) ;
               }
 
               psin = v2 ;
               if(psin < psmn) {
                   psin = v2 = psmn ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
               }
               if(psin > psmx) {
                   psin = v2 = psmx ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
               }
     
               alfval = v3 ;
               if(v3 < angmn) {
                 alfval = v3 = angmn  ;
                 fl1 = (float) v3 ;
                 f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > angmx) {
                 alfval = v3 = angmx ;
                 fl1 = (float) v3 ;
                 f3.setText(String.valueOf(fl1)) ;
               }
/* 
               tsin = v4 ;
               if(tsin < tsmn) {
                   tsin = v4 = tsmn ;
                   fl1 = (float) v4 ;
                   f4.setText(String.valueOf(fl1)) ;
               }
               if(tsin > tsmx) {
                   tsin = v4 = tsmx ;
                   fl1 = (float) v4 ;
                   f4.setText(String.valueOf(fl1)) ;
               }
*/
     
               i1 = (int) (((v1 - vmn)/(vmx-vmn))*1000.) ;
               i2 = (int) (((v2 - psmn)/(psmx-psmn))*1000.) ;
               i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
//               i4 = (int) (((v4 - tsmn)/(tsmx-tsmn))*1000.) ;
      
               inr.s1.setValue(i1) ;
               inr.s2.setValue(i2) ;
               inr.s3.setValue(i3) ;
//               inr.s4.setValue(i4) ;

               computeFlow() ;
            } // Text Handler

            public void handleBut(Event evt, Object arg) {
              String label = (String)arg ;
              String ddir,dfil,filnam,labmod,labrd ;
              int i ;

              labmod = "  models" ;
              labrd = "File Read -" ;
              if(label.equals("Model File")) {
                 bto.setBackground(Color.blue) ;
                 bto.setForeground(Color.white) ;
                 fdd.show() ;
                 ddir = fdd.getDirectory() ;
                 dfil = fdd.getFile();
                 inr.modf.setText(ddir + dfil) ;
              }
              if(label.equals("Open File")) {
                 bto.setBackground(Color.yellow) ;
                 bto.setForeground(Color.black) ;
                 filnam = inr.modf.getText() ;
                 try{
                  dfile = new FileInputStream(filnam) ;
                  datin = new DataInputStream(dfile) ;
                  
                  for (i=1; i<= 39; ++i) {
                     dcam[i] = datin.readDouble() ;
                     dthk[i] = datin.readDouble() ;
                     dspan[i] = datin.readDouble() ;
       		   dchrd[i] = datin.readDouble() ;
                     nummod = datin.readInt() ;
                     dftp[i] = datin.readInt() ;
  		         dlunits[i] = datin.readInt() ;
                  }
                } catch (IOException n) {
                  inr.modf.setText(labrd + String.valueOf(nummod) + labmod) ;
                }
              }
              if(label.equals("Data File")) {
                 bdato.setBackground(Color.blue) ;
                 bdato.setForeground(Color.white) ;
                 fdd.show() ;
                 ddir = fdd.getDirectory() ;
                 dfil = fdd.getFile();
                 inr.modd.setText(ddir + dfil) ;
              }
              if(label.equals("Open Data")) {
                 bdato.setBackground(Color.yellow) ;
                 bdato.setForeground(Color.black) ;
                 filnam = inr.modd.getText() ;
                 try{
                  dafile = new FileOutputStream(filnam) ;
                  datout = new DataOutputStream(dafile) ;

			dato = 1;
                  
                } catch (IOException n) {
                }
              }
              if(label.equals("Print File")) {
                 bprto.setBackground(Color.blue) ;
                 bprto.setForeground(Color.white) ;
                 fdd.show() ;
                 ddir = fdd.getDirectory() ;
                 dfil = fdd.getFile();
                 inr.modp.setText(ddir + dfil) ;

              }
              if(label.equals("Open Print")) {
                 bprto.setBackground(Color.yellow) ;
                 bprto.setForeground(Color.black) ;
                 filnam = inr.modp.getText() ;
                 try{
                  pfile = new FileOutputStream(filnam) ;
                  prnt = new PrintStream(pfile) ;

                  prnt.println("  ");
                  prnt.println(" Tunnel Application Version 1.0j - Jun 09 ");
                  prnt.println("  ");

			datp = 1;
			iprint = 1;
                  
                } catch (IOException n) {
                }
              }
            }  // end handler
          }  // Inl

           class Inr extends Panel {
              Tunnel outerparent ;
              Scrollbar s1,s2,s3,s4;
              TextField modf,modd,modp ;
		  AdjustmentListener scroller;
   
              Inr (Tunnel target) {
               int i1,i2,i3,i4 ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,2,5)) ;

		   scroller = new AdjustmentListener()
		        {
			  public void adjustmentValueChanged(AdjustmentEvent e)
			       {
				 int ii1,ii2,ii3,ii4 ;
                		 double v1,v2,v3,v4 ;
                		 float fl1,fl2,fl3,fl4 ;
     			   // Input for computations
                		 ii1 = s1.getValue() ;
                		 ii2 = s2.getValue() ;
                		 ii3 = s3.getValue() ;
                		 ii4 = s4.getValue() ;
   
                		 vfsd   = v1 = ii1 * (vmx - vmn)/ 1000. + vmn ;
                		 psin   = v2 = ii2 * (psmx - psmn)/ 1000. + psmn ;
                		 alfval  = v3 = ii3 * (angmx - angmn)/ 1000. + angmn ;
//                	   tsin    = v4 = ii4 * (tsmx - tsmn)/ 1000. + tsmn ;
   
                		 fl1 = (float) v1 ;
                		 fl2 = (float) v2 ;
                		 fl3 = (float) v3 ;
//                	   fl4 = (float) v4 ;
   
                		 inl.f1.setText(String.valueOf(fl1)) ;
                		 inl.f2.setText(String.valueOf(filter3(fl2))) ;
                 		 inl.f3.setText(String.valueOf(fl3)) ;
//                	   inl.f4.setText(String.valueOf(filter0(fl4))) ;
   
                		 computeFlow() ;
				 }
			  }; 
   
               i1 = (int) (((0.0 - vmn)/(vmx-vmn))*1000.) ;
               i2 = (int) (((2116 - psmn)/(psmax-psmn))*1000.) ;
               i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;
               i4 = (int) (((518.6 - tsmn)/(tsmx-tsmn))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
		   s1.addAdjustmentListener(scroller);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
		   s2.addAdjustmentListener(scroller);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
		   s3.addAdjustmentListener(scroller);
               s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);
		   s4.addAdjustmentListener(scroller);

               modf = new TextField("<-Select Models ") ;
               modf.setBackground(Color.white) ;
               modf.setForeground(Color.blue) ;

               modd = new TextField("Save to Data File ") ;
               modd.setBackground(Color.white) ;
               modd.setForeground(Color.blue) ;

               modp = new TextField("Print File ") ;
               modp.setBackground(Color.white) ;
               modp.setForeground(Color.blue) ;

               add(s1) ;
               add(s3) ;
               add(s2) ;
   //            add(s4) ;
               add(modf) ;
               add(modd);
               add(modp);
             }   
           }  // Inr 
        }  // Upr 

        class Lwr extends Panel {
           Tunnel outerparent ;
	     int testSet,testType;
	     Label testNumber,pointNumber,selectTest;
	     TextField testNumberTxt,pointNumberTxt;
           Button bt3,endit,speed,angle,pressure,begin,takeData,endTest;
           Choice untch;
	     ActionListener spdact,angact,presact,begact,takact,endact;
        
           Lwr (Tunnel target) {
    
            outerparent = target ;
            setLayout(new GridLayout(5,4,2,5)) ;

		spdact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				testType = 1;

				speed.setBackground(Color.yellow);
				speed.setForeground(Color.black);
				angle.setBackground(Color.blue);
				angle.setForeground(Color.white);
				pressure.setBackground(Color.blue);
				pressure.setForeground(Color.white);

				upr.inr.s1.setVisible(true);
				upr.inr.s2.setVisible(false);
				upr.inr.s3.setVisible(false);

				upr.inl.f1.setEditable(true);
				upr.inl.f2.setEditable(false);
				upr.inl.f3.setEditable(false);
				}
			};

		angact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				testType = 2;

				speed.setBackground(Color.blue);
				speed.setForeground(Color.white);
				angle.setBackground(Color.yellow);
				angle.setForeground(Color.black);
				pressure.setBackground(Color.blue);
				pressure.setForeground(Color.white);

				upr.inr.s1.setVisible(false);
				upr.inr.s2.setVisible(false);
				upr.inr.s3.setVisible(true);

				upr.inl.f1.setEditable(false);
				upr.inl.f2.setEditable(false);
				upr.inl.f3.setEditable(true);
				}
			};

		presact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				testType = 3;

				speed.setBackground(Color.blue);
				speed.setForeground(Color.white);
				angle.setBackground(Color.blue);
				angle.setForeground(Color.white);
				pressure.setBackground(Color.yellow);
				pressure.setForeground(Color.black);

				upr.inr.s1.setVisible(false);
				upr.inr.s2.setVisible(true);
				upr.inr.s3.setVisible(false);

				upr.inl.f1.setEditable(false);
				upr.inl.f2.setEditable(true);
				upr.inl.f3.setEditable(false);
				}
			};

		begact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				begin.setBackground(Color.green);
				begin.setForeground(Color.black);
				testSet = Integer.parseInt(testNumberTxt.getText());
				testSet = testSet + 1;
				if (testSet > 20)
					{
					upr.inr.modd.setText("Max Number of Tests = 20") ;
					testNumberTxt.setText("Max # of Tests");
					}
				String test = Integer.toString(testSet);
				testNumberTxt.setText(test);

				pointSet = 0;
				pointNumberTxt.setText("0");

				speed.removeActionListener(spdact);
				angle.removeActionListener(angact);
				pressure.removeActionListener(presact);
				takeData.addActionListener(takact);
				}
			};

		takact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				try
					{
					int modelNumber = Integer.parseInt(con.dwn.mod.getText()) ;
                            	if (dato == 1) 
						{		
						if (pointSet < 40)
							{
                             			datout.writeDouble(camval);
                             			datout.writeDouble(thkval);
                             			datout.writeDouble(chord);
					     		datout.writeDouble(span);
							datout.writeDouble(q0);
							datout.writeDouble(lift);
							datout.writeDouble(vfsd);
							datout.writeDouble(alfval);
							datout.writeDouble(psin);
							datout.writeInt(testSet);
							datout.writeInt(pointSet);
                  	           		datout.writeInt(modelNumber);
					     		datout.writeInt(lunits);
							datout.writeInt(testType);
	                             		datout.writeInt(foiltype);

							pointSet = pointSet + 1;
							String point = Integer.toString(pointSet);
							pointNumberTxt.setText(point);
							}
                                   		else 
							{
                                		upr.inr.modp.setText("Max Number of Points = 40") ;
							pointNumberTxt.setText("Max # of Points");
                             			}
                           		}
                           	
					if (datp == 1) 
						{
                             		if (iprint == 1)   // file open - print data
							{
							if (pointSet < 40)
								{
								pointSet = pointSet + 1;
								String point = Integer.toString(pointSet);
								pointNumberTxt.setText(point);
								
                                			prnt.println("----------------------------------------- ");
                                			prnt.println(" ") ;
                                			switch(foiltype) 
									{
                                   				case 1: 
										{
                                      				prnt.println( " Joukowski Airfoil" ) ;
                                      				break ;
                                   					}
                                   				case 2: 
										{
                                      				prnt.println( " Elliptical Airfoil" ) ;
                                      				break ;
                                   					}
                                   				case 3: 
										{
      	                                			prnt.println( " Plate" ) ;
            	                          			break ;
                  	                 				}
                        	        			}

								prnt.println( "Model Number:  " + con.dwn.mod.getText() );
								switch(testType)
									{
									case 1:
										{
										prnt.println( " Air Speed Test " );
										break;
										}
									case 2:
										{
										prnt.println( " Angle of Attack Test " );
										break;
										}
									case 3:
										{
										prnt.println( " Pressure Test " );
										break;
										}
									}
								 prnt.println( " Test Number:  " + testNumberTxt.getText() + "    Point Number:  " + pointNumberTxt.getText()); 
                               			 if (foiltype <= 3) 
									{
                                   				prnt.println( " Camber = " + String.valueOf(filter3(camval)) + " % chord ," ) ;
	                                   			prnt.println( " Thickness = " + String.valueOf(filter3(thkval)) + " % chord ," ) ;
      	                             			if (lunits == 0) 
										{
                  	                    			prnt.println( " Chord = " + String.valueOf(filter3(chord)) + " ft ," ) ;
                        	              			prnt.println( " Span = " + String.valueOf(filter3(span)) + " ft ," ) ;
                              	        			prnt.println( " Surface Area = " + String.valueOf(filter3(area)) +  " sq ft ," ) ;
										prnt.println( " Dynamic Pressure = " + String.valueOf(filter3(q0)) + " psf ," );
										prnt.println( " Lift = " + String.valueOf(filter3(lift)) + " lbs ," );
										prnt.println( " Air Speed = " + String.valueOf(filter3(vfsd)) + " mph ," );
										prnt.println( " Angle of Attack = " + String.valueOf(filter3(alfval)) + " degrees ," );
										prnt.println( " Tunnel Pressure = " + String.valueOf(filter3(psin)) + " psf ," );
      	                             				}
            	                       			if (lunits == 1) 
										{
                        	              			prnt.println( " Chord = " + String.valueOf(filter3(chord)) + " m ," ) ;
                              	        			prnt.println( " Span = " + String.valueOf(filter3(span)) + " m ," ) ;
                                    	  			prnt.println( " Surface Area = " + String.valueOf(filter3(area)) +  " sq m ," ) ;
										prnt.println( " Dynamic Pressure = " + String.valueOf(filter3(q0)) + " kPa ," );
										prnt.println( " Lift = " + String.valueOf(filter3(lift)) + " N ," );
										prnt.println( " Air Speed = " + String.valueOf(filter3(vfsd)) + " km/h ," );
										prnt.println( " Angle of Attack = " + String.valueOf(filter3(alfval)) + " degrees ," );
										prnt.println( " Tunnel Pressure = " + String.valueOf(filter3(psin)) + " kPa ," );
            	                       				}
                  	              			}
                        	   /*     		prnt.println( "\n \t Upper Surface \t \t Lower Surface") ;
                              	  		prnt.println( "\t X \t Y \t \t X \t Y \t \t  ");
                                			mapfac = 4.0 ;
                                			if (stfact >= 1.0) 
									{
	                                   			for (index = 0; index <= npt2-1; ++ index) 
										{
            	                        			prnt.println
											( "\t" +  String.valueOf(filter3(xpl[0][npt2-index]/mapfac)) +  "\t"
                        	             			 	+ String.valueOf(filter3(ypl[0][npt2-index]/mapfac)) + "\t" 
                              	       			  	+ "\t"
                                    	 			  	+ String.valueOf(filter3(xpl[0][npt2+index]/mapfac)) + "\t"
                                     				  	+ String.valueOf(filter3(ypl[0][npt2+index]/mapfac)) + "\t" 
                                     				  	+ "\t"
                                      				 	) ;
                                   					}
                                				}
                                			if (stfact < 1.0) 
									{
	                                   			for (index = 0; index <= npt2-1; ++ index) 
										{
            	                        			prnt.println(  String.valueOf(filter3(xpl[0][npt2-index]/mapfac)) +  "\t"
                  	                  		  	+ String.valueOf(filter3(ypl[0][npt2-index]/mapfac)) + "\t" 
                        	             		  	+  " - " + "\t"
                              	       		  	+  " - " + "\t" 
                                    	 		  	+ String.valueOf(filter3(xpl[0][npt2+index]/mapfac)) + "\t"
                                     			  	+ String.valueOf(filter3(ypl[0][npt2+index]/mapfac)) + "\t" 
                                     			  	+  " - " + "\t"
                                     		  		+  " - " ) ;
                                   					}
                                				}*/
                              			}
                                   		else 
								{
                                			upr.inr.modp.setText("Max Number of Points = 40") ;
								pointNumberTxt.setText("Max # of Points");
                             				}
                            			}
						}
                     		} 
				catch (IOException n) {}
				}
			};

		endact = new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				begin.setBackground(Color.black);
				begin.setForeground(Color.white);

				speed.setBackground(Color.blue);
				speed.setForeground(Color.white);

				angle.setBackground(Color.blue);
				angle.setForeground(Color.white);

				pressure.setBackground(Color.blue);
				pressure.setForeground(Color.white);

				speed.addActionListener(spdact);
				angle.addActionListener(angact);
				pressure.addActionListener(presact);
				takeData.removeActionListener(takact);

				upr.inr.s1.setVisible(true);
				upr.inr.s2.setVisible(true);
				upr.inr.s3.setVisible(true);

				upr.inl.f1.setEditable(true);
				upr.inl.f2.setEditable(true);
				upr.inl.f3.setEditable(true);
				}
			};

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
		untch.select(0);

		testNumber = new Label("Test #: ",Label.RIGHT);
		pointNumber = new Label("Point #: ",Label.RIGHT);
		selectTest = new Label("Select Test: ",Label.CENTER);

		testNumberTxt = new TextField("0");
		testNumberTxt.setEditable(false);
		testNumberTxt.setBackground(Color.black);
		testNumberTxt.setForeground(Color.yellow);

		pointNumberTxt = new TextField("0");
		pointNumberTxt.setEditable(false);
		pointNumberTxt.setBackground(Color.black);
		pointNumberTxt.setForeground(Color.yellow);

		speed = new Button("Speed");
		speed.setBackground(Color.blue);
		speed.setForeground(Color.white);
		speed.addActionListener(spdact);

		angle = new Button("Angle of Attack");
		angle.setBackground(Color.blue);
		angle.setForeground(Color.white);
		angle.addActionListener(angact);

		pressure = new Button("Pressure");
		pressure.setBackground(Color.blue);
		pressure.setForeground(Color.white);
		pressure.addActionListener(presact);

		begin = new Button("Begin Test");
		begin.setBackground(Color.black);
		begin.setForeground(Color.white);
		begin.addActionListener(begact);

		takeData = new Button("Take Data Point");
		takeData.setBackground(Color.blue);
		takeData.setForeground(Color.white);

		endTest = new Button("End Test");
		endTest.setBackground(Color.black);
		endTest.setForeground(Color.white);
		endTest.addActionListener(endact);

            add(selectTest);
            add(speed);
            add(angle);
            add(pressure);

            add(new Label("", Label.CENTER));
            add(begin);
            add(takeData);
            add(endTest);

            add(testNumber);
            add(testNumberTxt);
            add(pointNumber);
            add(pointNumberTxt);

            add(new Label("", Label.CENTER));
            add(new Label("", Label.CENTER));
            add(new Label("", Label.CENTER));
            add(new Label("", Label.CENTER));

            add(endit) ;
            add(bt3) ;
            add(untch) ;
            add(new Label("", Label.CENTER));
          }

          public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Choice) {
               this.handleCho(evt) ;
               return true ;
            }
            if(evt.target instanceof Button) {
               this.handleRefs(evt,arg) ;
               return true ;
            }
            else return false ;
          } //  handler

          public void handleCho(Event evt) {
              lunits  = untch.getSelectedIndex() ;
              setUnits () ;
              loadInput () ;

              computeFlow() ;
          } // handle choice
   
          public void handleRefs(Event evt, Object arg) {
            String label = (String)arg ;
            Double V1 ;
            double v1;
            int i1 ;

            if(label.equals("Exit")) {
               f.dispose() ;
               System.exit(1) ;
            }
            if(label.equals("Reset")) {
               in.flt.upr.inl.bto.setBackground(Color.blue) ;
               in.flt.upr.inl.bto.setForeground(Color.white) ;

		   begin.setBackground(Color.black);
		   begin.setForeground(Color.white);

		   speed.setBackground(Color.blue);
		   speed.setForeground(Color.white);

		   angle.setBackground(Color.blue);
		   angle.setForeground(Color.white);

		   pressure.setBackground(Color.blue);
		   pressure.setForeground(Color.white);

		   testNumberTxt.setText("0");
		   pointNumberTxt.setText("0");

		   upr.inr.s1.setVisible(true);
		   upr.inr.s2.setVisible(true);
		   upr.inr.s3.setVisible(true);

		   upr.inl.f1.setEditable(true);
		   upr.inl.f2.setEditable(true);
		   upr.inl.f3.setEditable(true);

               solve.setDefaults() ;
               layin.show(in, "first")  ;
               untch.select(lunits) ;
                   // **** the lunits check MUST come first
               setUnits () ;
               layplt.show(out.grf.l, "first") ;
               layout.show(out, "first")  ;
               outopt = 0 ;
       
               loadInput() ;
            }
          }
       }  // Lwr
     }  // Flt 

     class Cyl extends Panel {
        Tunnel outerparent ;
        Inl inl ;
        Inr inr ;

        Cyl (Tunnel target) {

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
           Tunnel outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02 ;
     
           Inl (Tunnel target) {
     
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

            l3 = new Label("Span ft", Label.CENTER) ;
            f3 = new TextField("5.0",5) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;
 
            add(new Label(" ", Label.CENTER)) ;
            add(new Label(" ", Label.CENTER)) ;
         }

         public boolean handleEvent(Event evt) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3 ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;

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
                radius = span ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
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
           Tunnel outerparent ;
           Scrollbar s1,s2;
           Choice shapch ;

           Inr (Tunnel target) {
             int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((spin*60.0 - spinmn)/(spinmx-spinmn))*1000.) ;
            i2 = (int) (((radius - radmn)/(radmx-radmn))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);

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
             int i1,i2,i3 ;
             double v1,v2,v3 ;
             float fl1,fl2,fl3 ;
              
    // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             spin = v1 = i1 * (spinmx - spinmn)/ 1000. + spinmn ;
             spin = spin / 60.0 ;
             radius = v2 = i2 * (radmx - radmn)/ 1000. + radmn ;
             v3 = span;
             if (foiltype == 5) radius = v3 ;
             spanfac = (int)(fact*span/radius*.3535) ;
             area = 2.0*radius*span ;
             if (foiltype ==5) area = 3.1415926 * radius * radius ;
             cyl.setLims() ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;
             fl3 = (float) v3 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;
             inl.f3.setText(String.valueOf(fl3)) ;
      
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
             }
             if(foiltype == 4) layin.show(in, "fourth")  ;
             if(foiltype == 5) {
                 radius = span ;
                 area = 3.1415926*radius*radius ;
                 layin.show(in, "fourth")  ;
                 if (viewflg != 0) viewflg = 0 ;
             }
     
             if (foiltype <= 3) {
                if (planet <= 1) {
                   layplt.show(out.grf.l, "first") ;
                }
                if (planet >= 2) {
                   layplt.show(out.grf.l, "second") ;
                }
             }
             if (foiltype >= 4) {
                layplt.show(out.grf.l, "second") ;
             }

             layout.show(out, "first")  ;
             outopt = 0 ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           } // handler
        }  // Inr
     }  // Cyl 

     class Filep extends Panel {  // print file
        Tunnel outerparent ;
        TextField namprnt,namlab ;
        Button pbopen,cancel ;

        Filep (Tunnel target) {

          outerparent = target ;
          setLayout(new GridLayout(5,2,10,10)) ;

          namprnt = new TextField() ;
          namprnt.setBackground(Color.white) ;
          namprnt.setForeground(Color.black) ;

          namlab = new TextField() ;
          namlab.setBackground(Color.white) ;
          namlab.setForeground(Color.black) ;

          pbopen = new Button("Open File") ;
          pbopen.setBackground(Color.red) ;
          pbopen.setForeground(Color.white) ;

          cancel = new Button("Cancel") ;
          cancel.setBackground(Color.yellow) ;
          cancel.setForeground(Color.black) ;

          add(new Label("Enter File Name -  ", Label.RIGHT)) ;
          add(new Label("Then Push Button", Label.LEFT)) ;

          add(new Label("Save Data to File:", Label.RIGHT)) ;
          add(namprnt) ;

          add(new Label("Label: ", Label.CENTER)) ;
          add(namlab) ;

          add(new Label("Push ->", Label.RIGHT)) ;
          add(pbopen) ;

          add(new Label(" ", Label.CENTER)) ;
          add(cancel) ;
       }

       public boolean action(Event evt, Object arg) {
          if(evt.target instanceof Button) {
            this.handleRefs(evt,arg) ;
            return true ;
          }
          else return false ;
       }

       public void handleRefs(Event evt, Object arg) {
          String filnam, fillab ;
          String label = (String)arg ;

          if(label.equals("Open File")) {
             filnam = namprnt.getText() ;
             fillab = namlab.getText() ;
             try{
              pfile = new FileOutputStream(filnam) ;
              pbopen.setBackground(Color.red) ;
              pbopen.setForeground(Color.white) ;
 
              prnt = new PrintStream(pfile) ;

              prnt.println("  ");
              prnt.println(" Tunnel -  Wind Tunnel Version 1.0j - June 09 ");
              prnt.println("  ");
              prnt.println(fillab);
              prnt.println("  ");
              iprint = 1;
              layin.show(in, "first")  ;
            } catch (IOException n) {
            }
          }
          if(label.equals("Cancel")) {  // Forget it
             layin.show(in, "first")  ;
          }
       } //end handler
    } // end Filep
  }  // In 

  class Out extends Panel {
     Tunnel outerparent ;
     Plt plt ;
     Grf grf ;
     Perf perf ;

     Out (Tunnel target) { 
        outerparent = target ;
        layout = new CardLayout() ;
        setLayout(layout) ;

        plt = new Plt(outerparent) ;
        grf = new Grf(outerparent) ;
        perf = new Perf(outerparent) ;

        add ("first", plt) ;
        add ("third", perf) ;
        add ("fourth", grf) ;
     }
 
     class Grf extends Panel {
        Tunnel outerparent ;
        U u;
        L l;

        Grf (Tunnel target) {
           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           u = new U(outerparent) ;
           l = new L(outerparent) ;

           add (u) ;
           add (l) ;
        }

        class U extends Panel {
           Tunnel outerparent ;
           Label l1,l2 ;
           Button pl1,pl2,pl3;

           U (Tunnel target) {
              outerparent = target ;
              setLayout(new GridLayout(3,4,5,5)) ;
  
              l1 = new Label("Surface", Label.RIGHT) ;
              l1.setForeground(Color.blue) ;
 
              l2 = new Label("Generation", Label.RIGHT) ;
              l2.setForeground(Color.black) ;
 
              pl1 = new Button("Pressure") ;
              pl1.setBackground(Color.white) ;
              pl1.setForeground(Color.blue) ;
              pl2 = new Button("Velocity") ;
              pl2.setBackground(Color.white) ;
              pl2.setForeground(Color.blue) ;
              pl3 = new Button("Plane") ;
              pl3.setBackground(Color.white) ;
              pl3.setForeground(Color.black) ;

              add(new Label("Select ", Label.RIGHT)) ;
              add(new Label(" Plot ", Label.LEFT)) ;
              add(new Label(" ", Label.RIGHT)) ;
              add(new Label(" ", Label.RIGHT)) ;
 
              add(l1) ;
              add(pl1) ;
              add(pl2) ;
              add(new Label(" ", Label.RIGHT)) ;
 
              add(l2) ;
              add(pl3) ;
              add(new Label(" ", Label.RIGHT)) ;
              add(new Label(" ", Label.RIGHT)) ;
           }

           public boolean action(Event evt, Object arg) {
             if(evt.target instanceof Button) {
                String label = (String)arg ;
                layout.show(out, "first")  ;
                outopt = 0 ;
                if(label.equals("Pressure")) dispp = 0 ;
                if(label.equals("Velocity")) dispp = 1 ;
                if(label.equals("Plane")) dispp = 25 ;
                calcrange = 0 ;
                computeFlow() ;
                return true ;
             }
             else return false ;
           } // Handler
        } // Upper

        class L extends Panel {
           Tunnel outerparent ;
           F f ;
           C c ;

           L (Tunnel target) {
              outerparent = target ;
              layplt = new CardLayout() ;
              setLayout(layplt) ;

              f = new F(outerparent) ;
              c = new C(outerparent) ;

              add ("first", f) ;
              add ("second", c) ;
           }

           class F extends Panel {
              Tunnel outerparent ;
              Label l2 ;
              Button pl3,pl4,pl5,pl6,pl7,pl8,pl9 ;
              Choice plout  ;
    
              F (Tunnel target) {
                 outerparent = target ;
                 setLayout(new GridLayout(3,4,5,5)) ;

                 l2 = new Label("Lift vs.", Label.RIGHT) ;
                 l2.setForeground(Color.red) ;
   
                 plout = new Choice() ;
                 plout.addItem("Lift vs.") ;
                 plout.addItem("Cl vs.");
                 plout.setBackground(Color.white) ;
                 plout.setForeground(Color.red) ;
                 plout.select(0) ;
     
                 pl3 = new Button("Angle") ;
                 pl3.setBackground(Color.white) ;
                 pl3.setForeground(Color.red) ;
                 pl4 = new Button("Thickness") ;
                 pl4.setBackground(Color.white) ;
                 pl4.setForeground(Color.red) ;
                 pl5 = new Button("Camber") ;
                 pl5.setBackground(Color.white) ;
                 pl5.setForeground(Color.red) ;
                 pl6 = new Button("Speed") ;
                 pl6.setBackground(Color.white) ;
                 pl6.setForeground(Color.red) ;
                 pl7 = new Button("Altitude") ;
                 pl7.setBackground(Color.white) ;
                 pl7.setForeground(Color.red) ;
                 pl8 = new Button("Wing Area") ;
                 pl8.setBackground(Color.white) ;
                 pl8.setForeground(Color.red) ;
                 pl9 = new Button("Density") ;
                 pl9.setBackground(Color.white) ;
                 pl9.setForeground(Color.red) ;
   
                 add(l2) ;
                 add(pl6) ;
                 add(pl7) ;
                 add(pl9) ;
   
                 add(new Label(" ", Label.RIGHT)) ;
                 add(pl8) ;
                 add(new Label(" ", Label.RIGHT)) ;
                 add(new Label(" ", Label.RIGHT)) ;

                 add(plout) ;
                 add(pl3) ;
                 add(pl5) ;
                 add(pl4) ;
              }

              public boolean action(Event evt, Object arg) {
                if(evt.target instanceof Button) {
                   String label = (String)arg ;
                   layout.show(out, "first")  ;
                   outopt = 0 ;
                   if(label.equals("Angle")) dispp = 2 ;
                   if(label.equals("Thickness")) dispp = 3 ;
                   if(label.equals("Camber")) dispp = 4 ;
                   if(label.equals("Speed")) dispp = 5 ;
                   if(label.equals("Altitude")) dispp = 6 ;
                   if(label.equals("Wing Area")) dispp = 7 ;
                   if(label.equals("Density")) dispp = 8 ;
    
                   computeFlow() ;
                   return true ;
                }
                if(evt.target instanceof Choice) {
                   String label = (String)arg ;
                   if(label.equals("Lift vs.")) dout = 0 ;
                   if(label.equals("Cl vs.")) dout = 1 ;
   
                   return true ;
                }
                else return false ;
              } // Handler
           }  // foil

           class C extends Panel {
              Tunnel outerparent ;
              Label l2 ;
   
              C (Tunnel target) {
                 outerparent = target ;
                 setLayout(new GridLayout(1,1,5,5)) ;

                 l2 = new Label(" ", Label.RIGHT) ;

                 add(l2) ;
              }
           }  // cyl
        } // Lower
     }  // Grf

     class Plt extends Canvas
         implements Runnable{
        Tunnel outerparent ;
        Thread run2 ;
        Point locp,ancp;

        Plt (Tunnel target) { 
           setBackground(Color.blue) ;
           run2 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
            if (y >= 300) { 
/*
               if (x >= 5 && x <= 55) {  rescale
                   endy = 0.0 ;
                   begy = 0.0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
*/
               if (x >= 82 && x <= 232) { // pressure plot
                   dispp = 0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
               if (x >= 240 && x <= 390) { // velocity plot
                   dispp = 1 ;
                   calcrange = 0 ;
                   computeFlow() ;
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

        public void loadPlot() {
          double rad,ang,xc,yc,lftref,clref ;
          double del,spd,awng,ppl,tpl,hpl,angl,thkpl,campl,clpl ;
          int index,ic ;

          lines = 1 ;
          clref =  getClplot(camval,thkval,alfval) ;
          if (Math.abs(clref) <= .001) clref = .001 ;    /* protection */
          lftref = clref * q0 * area/lconv/lconv ;
//  ******* attempt at constant re-scale   
                   endy = 0.0 ;
                   begy = 0.0 ;
                   calcrange = 0 ;
//  ********

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

          if (dispp == 0) {    // pressure variation
              npt = npt2 ;
              ntr = 3 ;
              nord = nabs = 1 ;
              for (index = 1; index <= npt; ++ index) {
                  if (foiltype <= 3) {
                     pltx[0][index] =100.*(xpl[0][npt2-index + 1]/4.0 + .5) ;
                     pltx[1][index] =100.*(xpl[0][npt2+index - 1]/4.0 + .5) ;
                     pltx[2][index] =100.*(xpl[0][npt2+index - 1]/4.0 + .5) ;
                  }
                  if (foiltype >= 4) {
                     pltx[0][index]=100.*(xpl[0][npt2-index+1]/(2.0*radius/lconv)+.5);
                     pltx[1][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                     pltx[2][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                  }
                  plty[0][index] = plp[npt2-index + 1] ;
                  plty[1][index] = plp[npt2+index - 1] ;
                  plty[2][index] = ps0/2116. * pconv ;
// **** attempt to impose pstatic on surface plot for stalled foil
                  if (index > 7) {
                     if (alfval >  10.0) plty[0][index] = plty[2][index] ;
                     if (alfval < -10.0) plty[1][index] = plty[2][index] ;
                  }
// *******
              }
              begx = 0.0 ;
              endx = 100. ;
              ntikx = 5 ;
              ntiky = 5 ;
       //       endy=1.02 * ps0/2116. * pconv ;
       //       begy=.95 * ps0/2116. * pconv ;
              laby = String.valueOf("Press");
              if (lunits == 0) labyu = String.valueOf("psi");
              if (lunits == 1) labyu = String.valueOf("k-Pa");
              labx = String.valueOf(" X ");
              if (foiltype <= 3) labxu = String.valueOf("% chord");
              if (foiltype >= 4) labxu = String.valueOf("% diameter");
          }
          if (dispp == 1) {    // velocity variation
              npt = npt2 ;
              ntr = 3 ;
              nord = 2 ;
              nabs = 1 ;
              for (index = 1; index <= npt; ++ index) {
                  if (foiltype <= 3) {
                     pltx[0][index] = 100.*(xpl[0][npt2-index+1]/4.0+.5) ;
                     pltx[1][index] = 100.*(xpl[0][npt2+index-1]/4.0+.5) ;
                     pltx[2][index] = 100.*(xpl[0][npt2-index+1]/4.0+.5) ;
                  }
                  if (foiltype >= 4) {
                     pltx[0][index]=100.*(xpl[0][npt2-index+1]/(2.0*radius/lconv)+.5);
                     pltx[1][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                     pltx[2][index]=100.*(xpl[0][npt2+index-1]/(2.0*radius/lconv)+.5);
                  }
                  plty[0][index] = plv[npt2-index+1];
                  plty[1][index] = plv[npt2+index-1] ;
                  plty[2][index] = vfsd ;
// **** attempt to impose free stream vel on surface plot for stalled foil
                  if (index > 7) {
                     if (alfval >  10.0) plty[0][index] = plty[2][index] ;
                     if (alfval < -10.0) plty[1][index] = plty[2][index] ;
                  }
// *******
              }
              begx = 0.0 ;
              endx = 100. ;
              ntikx = 5 ;
              ntiky = 6 ;
        //      begy = 0.0 ;
        //      endy = 500. ;
              laby = String.valueOf("Vel");
              if (lunits == 0) labyu = String.valueOf("mph");
              if (lunits == 1) labyu = String.valueOf("kmh");
              labx = String.valueOf(" X ");
              if (foiltype <= 3) labxu = String.valueOf("% chord");
              if (foiltype >= 4) labxu = String.valueOf("% diameter");
          }
          if (dispp == 2) {    // lift versus angle
              npt = 20 ;
              ntr = 1 ;
              nabs = 2;  nord = 3 ;
              begx=-20.0; endx=20.0; ntikx=5;
              labx = String.valueOf("Angle ");
              labxu = String.valueOf("degrees");
              del = 40.0 / npt ;
              for (ic=1; ic <=npt; ++ic) {
                   angl = -20.0 + (ic-1)*del ;
                   clpl = getClplot(camval,thkval,angl) ;
                   pltx[0][ic] = angl ;
                   if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                   if (dout == 1)plty[0][ic] = 100.*clpl ;
              }
              ntiky = 5 ;
              pltx[1][0] = alfval ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
          }
          if (dispp == 3) {    // lift versus thickness
              npt = 20 ;
              ntr = 1 ;
              nabs = 3;  nord = 3 ;
              begx=0.0; endx=25.0; ntikx=6;
              labx = String.valueOf("Thickness ");
              labxu = String.valueOf("% chord");
              del = 1.0 / npt ;
              for (ic=1; ic <=npt; ++ic) {
                   thkpl = .05 + (ic-1)*del ;
                   clpl = getClplot(camval,thkpl,alfval) ;
                   pltx[0][ic] = thkpl*25. ;
                   if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                   if (dout == 1)plty[0][ic] = 100.*clpl ;
              }
              ntiky = 5 ;
              pltx[1][0] = thkinpt ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
          }
          if (dispp == 4) {    // lift versus camber
              npt = 20 ;
              ntr = 1 ;
              nabs = 4;  nord = 3 ;
              begx=-25.; endx=25.; ntikx=5;
              labx = String.valueOf("Camber ");
              labxu = String.valueOf("% chord");
              del = 2.0 / npt ;
              for (ic=1; ic <=npt; ++ic) {
                  campl = -1.0 + (ic-1)*del ;
                  clpl = getClplot(campl,thkval,alfval) ;
                  pltx[0][ic] = campl*25.0 ;
                  if (dout == 0)plty[0][ic] = fconv*lftref * clpl/clref ;
                  if (dout == 1)plty[0][ic] = 100.*clpl ;
              }
              ntiky = 5 ;
              pltx[1][0] = caminpt ;
              if (dout == 0) {
                  laby = String.valueOf("Lift");
                  if (lunits == 0) labyu = String.valueOf("lbs");
                  if (lunits == 1) labyu = String.valueOf("N");
                  plty[1][0] = lftref*fconv ;
              }
              if (dout == 1) {
                  laby = String.valueOf("Cl");
                  labyu = String.valueOf("x 100 ");
                  plty[1][0] = 100.*clift ;
              }
          }
          if (dispp == 5) {    // lift versus speed
              npt = 20 ;
              ntr = 1 ;
              nabs = 5;  nord = 3 ;
              begx=0.0; endx=300.0; ntikx=7;
              labx = String.valueOf("Speed ");
              if (lunits == 0) labxu = String.valueOf("mph");
              if (lunits == 1) labxu = String.valueOf("kmh");
              del = vmax / npt ;
              for (ic=1; ic <=npt; ++ic) {
                  spd = (ic-1)*del ;
                  pltx[0][ic] = spd ;
                  plty[0][ic] = fconv*lftref * spd * spd / (vfsd * vfsd) ;
              }
              ntiky = 5 ;
              laby = String.valueOf("Lift");
              pltx[1][0] = vfsd ;
              plty[1][0] = lftref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }
          if (dispp == 6) {    // lift versus altitude
              npt = 20 ;
              ntr = 1 ;
              nabs = 6;  nord = 3 ;
              begx=0.0; endx=50.0; ntikx=6;
              if (lunits == 0) endx = 50.0 ;
              if (lunits == 1) endx = 15.0 ;
              labx = String.valueOf("Altitude");
              if (lunits == 0) labxu = String.valueOf("k-ft");
              if (lunits == 1) labxu = String.valueOf("km");
              del = altmax / npt ;
              for (ic=1; ic <=npt; ++ic) {
                  hpl = (ic-1)*del ;
                  pltx[0][ic] = lconv*hpl/1000. ;
                  tpl = 518.6 ;
                  ppl = 2116. ;
                  if (planet == 0) {
                      if (hpl < 36152.)   {
                            tpl = 518.6 - 3.56 * hpl /1000. ;
                            ppl = 2116. * Math.pow(tpl/518.6, 5.256) ;
                      }
                         else {
                            tpl = 389.98 ;
                            ppl = 2116. * .236 * Math.exp((36000.-hpl)/(53.35*tpl)) ;
                      }
                      plty[0][ic] = fconv*lftref * ppl/(tpl*53.3*32.17) / rho ;
                  }
                  if (planet == 1) {
                      if (hpl <= 22960.) {
                         tpl = 434.02 - .548 * hpl/1000. ;
                         ppl = 14.62 * Math.pow(2.71828,-.00003 * hpl) ;
                      }
                      if (hpl > 22960.) {
                         tpl = 449.36 - 1.217 * hpl/1000. ;
                         ppl = 14.62 * Math.pow(2.71828,-.00003 * hpl) ;
                      }
                      plty[0][ic] = fconv*lftref * ppl/(tpl*1149.) / rho ;
                  }
                  if (planet == 2) {
                      plty[0][ic] = fconv*lftref ;
                  }
              }
              ntiky = 5 ;
              laby = String.valueOf("Lift");
              pltx[1][0] = alt/1000. ;
              plty[1][0] = lftref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }
          if (dispp == 7) {    // lift versus area
              npt = 2 ;
              ntr = 1 ;
              nabs = 7;  nord = 3 ;
              begx=0.0; ntikx=6;
              labx = String.valueOf("Area ");
              if (lunits == 0) {
                  labxu = String.valueOf("sq ft");
                  endx = 2000.0 ;
                  labyu = String.valueOf("lbs");
                  pltx[0][1] = 0.0 ;
                  plty[0][1] = 0.0 ;
                  pltx[0][2] = 2000. ;
                  plty[0][2] = fconv*lftref * 2000. /area ;
              }
              if (lunits == 1) {
                  labxu = String.valueOf("sq m");
                  endx = 200. ;
                  labyu = String.valueOf("N");
                  pltx[0][1] = 0.0 ;
                  plty[0][1] = 0.0 ;
                  pltx[0][2] = 200. ;
                  plty[0][2] = fconv*lftref * 200. /area ; ;
              }

              ntiky = 5 ;
              laby = String.valueOf("Lift");
              pltx[1][0] = area ;
              plty[1][0] = lftref*fconv ;
          }
          if (dispp == 8) {    // lift versus density
              npt = 2 ;
              ntr = 1 ;
              nabs = 7; nord = 3 ;
              begx=0.0; ntikx=6;
              labx = String.valueOf("Density ");
              if (planet == 0) {
                  if (lunits == 0) {
                      labxu = String.valueOf("x 10,000 slug/cu ft");
                      endx = 25.0 ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 23.7 ;
                      plty[0][2] = fconv*lftref * 23.7 /(rho*10000.);
                      pltx[1][0] = rho*10000. ;
                  }
                  if (lunits == 1) {
                      labxu = String.valueOf("g/cu m");
                      endx = 1250. ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 1226 ;
                      plty[0][2] = fconv*lftref * 23.7 /(rho*10000.);
                      pltx[1][0] = rho*1000.*515.4 ;
                  }
              }
              if (planet == 1) {
                  if (lunits == 0) {
                      labxu = String.valueOf("x 100,000 slug/cu ft");
                      endx = 5.0 ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 2.93 ;
                      plty[0][2] = fconv*lftref * 2.93 /(rho*100000.);
                      pltx[1][0] = rho*100000. ;
                  }
                  if (lunits == 1) {
                      labxu = String.valueOf("g/cu m");
                      endx = 15. ;
                      pltx[0][1] = 0.0 ;
                      plty[0][1] = 0.0 ;
                      pltx[0][2] = 15.1 ;
                      plty[0][2] = fconv*lftref * 2.93 /(rho*100000.);
                      pltx[1][0] = rho*1000.*515.4 ;
                  }
              }
              ntiky = 5 ;
              laby = String.valueOf("Lift");
              plty[1][0] = lftref*fconv ;
              if (lunits == 0) labyu = String.valueOf("lbs");
              if (lunits == 1) labyu = String.valueOf("N");
          }

          if (dispp>= 2 && dispp < 6) {  // determine y - range zero in middle
              if (plty[0][npt] >= plty[0][1]) {
                  begy=0.0 ;
                  if (plty[0][1]   > endy) endy = plty[0][1]  ;
                  if (plty[0][npt] > endy) endy = plty[0][npt]  ;
                  if (endy <= 0.0) {
                     begy = plty[0][1] ;
                     endy = plty[0][npt] ;
                  }
              }
              if (plty[0][npt] < plty[0][1]) {
                  endy=0.0 ;
                  if (plty[0][1]   < begy) begy = plty[0][1]  ;
                  if (plty[0][npt] < begy) begy = plty[0][npt]  ;
                  if (begy <= 0.0) {
                     begy = plty[0][npt] ;
                     endy = plty[0][1] ;
                  }
              }
          }
          if (dispp >= 6 && dispp <= 8) {    // determine y - range
              if (plty[0][npt] >= plty[0][1]) {
                 begy = plty[0][1]  ;
                 endy = plty[0][npt]  ;
              }
              if (plty[0][npt] < plty[0][1]) {
                 begy = plty[0][npt]  ;
                  endy = plty[0][1]  ;
              }
          }
          if (dispp >= 0 && dispp <= 1) {    // determine y - range
              if (calcrange == 0) {
                 begy = plty[0][1] ;
                 endy = plty[0][1] ;
                 for (index = 1; index <= npt2; ++ index) {
                     if (plty[0][index] < begy) begy = plty[0][index] ;
                     if (plty[1][index] < begy) begy = plty[1][index] ;
                     if (plty[0][index] > endy) endy = plty[0][index] ;
                     if (plty[1][index] > endy) endy = plty[1][index] ;
                 }
                 calcrange = 1 ;
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
   
        public void update(Graphics g) {
           out.plt.paint(g) ;
        }
   
        public void paint(Graphics g) {
           int i,j,k,n,index ;
           int xlabel,ylabel,ind,inmax,inmin ;
           int exes[] = new int[8] ;
           int whys[] = new int[8] ;
           double offx,scalex,offy,scaley,waste,incy,incx;
           double xl,yl;
           double liftab ;
           int camx[] = new int[19] ;
           int camy[] = new int[19] ;
           Color col ;
  
           if (dispp <= 1) {
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,0,500,500) ;
/*
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,302,70,15) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,315) ;
*/
              off2Gg.setColor(Color.lightGray) ;
              off2Gg.fillRect(0,295,500,50) ;
              off2Gg.setColor(Color.white) ;
              if (dispp == 0) off2Gg.setColor(Color.yellow) ;
              off2Gg.fillRect(82,302,150,20) ;
              off2Gg.setColor(Color.black) ;
              off2Gg.drawString("Surface Pressure",88,317) ;

              off2Gg.setColor(Color.white) ;
              if (dispp == 1) off2Gg.setColor(Color.yellow) ;
              off2Gg.fillRect(240,302,150,20) ;
              off2Gg.setColor(Color.black) ;
              off2Gg.drawString("Velocity",288,317) ;
           }
           if (dispp > 1 && dispp <= 15) {
              off2Gg.setColor(Color.blue) ;
              off2Gg.fillRect(0,0,500,500) ;
/*
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,302,70,15) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,315) ;
*/
           }
           if (dispp >= 20) {
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,0,500,500) ;
           }
 
           if (ntikx < 2) ntikx = 2 ;     /* protection 13June96 */
           if (ntiky < 2) ntiky = 2 ;
           offx = 0.0 - begx ;
           scalex = 6.0/(endx-begx) ;
           incx = (endx-begx)/(ntikx-1);
           offy = 0.0 - begy ;
           scaley = 4.5/(endy-begy) ;
           incy = (endy-begy)/(ntiky-1) ;
 
           if (dispp <= 15) {             /*  draw a graph */
                                              /* draw axes */
              off2Gg.setColor(Color.white) ;
              exes[0] = (int) (factp* 0.0) + xtp ;
              whys[0] = (int) (factp* -4.5) + ytp ;
              exes[1] = (int) (factp* 0.0) + xtp ;
              whys[1] = (int) (factp* 0.0) + ytp ;
              exes[2] = (int) (factp* 6.0) + xtp ;
              whys[2] = (int) (factp* 0.0) + ytp ;
              off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
 
              xlabel = (int) (-90.0) + xtp ;   /*  label y axis */
              ylabel = (int) (factp*-1.5) + ytp ;
              off2Gg.drawString(laby,xlabel,ylabel) ;
              off2Gg.drawString(labyu,xlabel,ylabel+10) ;
                                                    /* add tick values */
              for (ind= 1; ind<= ntiky; ++ind){
                   xlabel = (int) (-50.0) + xtp ;
                   yl = begy + (ind-1) * incy ;
                   ylabel = (int) (factp* -scaley*(yl + offy)) + ytp ;
                   if (nord >= 2) {
                      off2Gg.drawString(String.valueOf((int) yl),xlabel,ylabel) ;
                   }
                   else {
                      off2Gg.drawString(String.valueOf(filter3(yl)),xlabel,ylabel);
                   }
              }
              xlabel = (int) (factp*3.0) + xtp ;    /* label x axis */
              ylabel = (int) (40.0) + ytp ;
              off2Gg.drawString(labx,xlabel,ylabel-10) ;
              off2Gg.drawString(labxu,xlabel,ylabel) ;
                                                   /* add tick values */
              for (ind= 1; ind<= ntikx; ++ind){
                   ylabel = (int) (15.) + ytp ;
                   xl = begx + (ind-1) * incx ;
                   xlabel = (int) (factp*(scalex*(xl + offx) -.05)) + xtp ;
                   if (nabs == 1) {
                      off2Gg.drawString(String.valueOf(xl),xlabel,ylabel) ;
                   }
                   if (nabs > 1) {
                      off2Gg.drawString(String.valueOf((int) xl),xlabel,ylabel) ;
                   }
              }
       
              if(lines == 0) {
                 for (i=1; i<=npt; ++i) {
                     xlabel = (int) (factp*scalex*(offx+pltx[0][i])) + xtp ;
                     ylabel = (int)(factp*-scaley*(offy+plty[0][i]) +7.)+ytp;
                     off2Gg.drawString("*",xlabel,ylabel) ;
                 }
              }
              else {
                if (dispp <= 1) {
//                 if (anflag != 1 || (anflag == 1 && Math.abs(alfval) < 10.0)) {
                   for (j=0; j<=ntr-1; ++j) {
                      k = 2 - j ;
                      if (k == 0) {
                        off2Gg.setColor(Color.magenta) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -2.5) + ytp ;
                        off2Gg.drawString("Upper",xlabel,ylabel) ;
                      }
                      if (k == 1) {
                        off2Gg.setColor(Color.yellow) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -1.5) + ytp ;
                        off2Gg.drawString("Lower",xlabel,ylabel) ;
                      }
                      if (k == 2) {
                        off2Gg.setColor(Color.green) ;
                        xlabel = (int) (factp* 2.0) + xtp ;
                        ylabel = (int) (factp* -5.0) + ytp ;
                        off2Gg.drawString("Free Stream",xlabel,ylabel) ;
                      }
                      exes[1] = (int) (factp*scalex*(offx+pltx[k][1])) + xtp;
                      whys[1] = (int) (factp*-scaley*(offy+plty[k][1]))+ ytp;
                      for (i=1; i<=npt; ++i) {
                        exes[0] = exes[1] ;
                        whys[0] = whys[1] ;
                        exes[1] = (int)(factp*scalex*(offx+pltx[k][i]))+xtp;
                        whys[1] = (int)(factp*-scaley*(offy+plty[k][i]))+ytp;
                        off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                      }
                   }
/*
                 }
                 if (anflag == 1 && Math.abs(alfval) > 10.0) {
                     off2Gg.setColor(Color.yellow) ;
                     xlabel = (int) (factp* 1.0) + xtp ;
                     ylabel = (int) (factp* -2.0) + ytp ;
                     off2Gg.drawString("Wing is Stalled",xlabel,ylabel) ;
       
                     xlabel = (int) (factp* 1.0) + xtp ;
                     ylabel = (int) (factp* -1.0) + ytp ;
                     off2Gg.drawString("Plot not Available",xlabel,ylabel) ;
                 }
*/
               }
               if (dispp > 1) {
                 off2Gg.setColor(Color.white) ;
                 exes[1] = (int) (factp*scalex*(offx+pltx[0][1])) + xtp;
                 whys[1] = (int) (factp*-scaley*(offy+plty[0][1])) + ytp;
                 for (i=1; i<=npt; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (factp*scalex*(offx+pltx[0][i])) + xtp;
                   whys[1] = (int) (factp*-scaley*(offy+plty[0][i])) + ytp;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 }
               }
               xlabel = (int) (factp*scalex*(offx+pltx[1][0])) + xtp ;
               ylabel = (int)(factp*-scaley*(offy+plty[1][0]))+ytp -4;
               off2Gg.setColor(Color.red) ;
               off2Gg.fillOval(xlabel,ylabel,5,5) ;
             }
          }

          if(dispp == 20)  {      /*  draw the lift gauge */
              off2Gg.setColor(Color.black) ;
              off2Gg.fillRect(0,100,300,30) ;
            // Thermometer gage
              off2Gg.setColor(Color.white) ;
              if (lftout == 0) {
                 off2Gg.drawString("Lift =",70,75) ;
                 if (lunits == 0) off2Gg.drawString("Pounds",190,75) ;
                 if (lunits == 1) off2Gg.drawString("Newtons",190,75) ;
              }
              if (lftout == 1) off2Gg.drawString(" Cl  =",70,75) ;
              off2Gg.setColor(Color.yellow);
              for (index=0 ; index <= 10; index ++) {
                off2Gg.drawLine(7+index*25,100,7+index*25,110) ;
                off2Gg.drawString(String.valueOf(index),5+index*25,125) ;
              }

              liftab = lift ;
              if (lftout == 0) {
                 if (Math.abs(lift) <= 1.0) {
                    liftab = lift*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("-1",180,70) ;
                 }
                 if (Math.abs(lift) > 1.0 && Math.abs(lift) <= 10.0) {
                    liftab = lift ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("0",180,70) ;
                 }
                 if (Math.abs(lift) > 10.0 && Math.abs(lift) <=100.0) {
                    liftab = lift/10.0 ;
                    off2Gg.setColor(Color.green);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("1",180,70) ;
                 }
                 if (Math.abs(lift) > 100.0 && Math.abs(lift) <=1000.0) {
                    liftab = lift/100.0 ;
                    off2Gg.setColor(Color.red);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("2",180,70) ;
                 }
                 if (Math.abs(lift) > 1000.0 && Math.abs(lift) <=10000.0) {
                    liftab = lift/1000.0 ;
                    off2Gg.setColor(Color.magenta);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("3",180,70) ;
                 }
                 if (Math.abs(lift) > 10000.0 && Math.abs(lift) <=100000.0) {
                    liftab = lift/10000.0 ;
                    off2Gg.setColor(Color.orange);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("4",180,70) ;
                 }
                 if (Math.abs(lift) > 100000.0 && Math.abs(lift) <=1000000.0) {
                    liftab = lift/100000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("5",180,70) ;
                 }
                 if (Math.abs(lift) > 1000000.0) {
                    liftab = lift/1000000.0 ;
                    off2Gg.setColor(Color.white);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("6",180,70) ;
                 }
             }
          
             if (lftout == 1) {
                 liftab = clift ;
                 if (Math.abs(clift) <= 1.0) {
                    liftab = clift*10.0 ;
                    off2Gg.setColor(Color.cyan);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("-1",180,70) ;
                 }
                 if (Math.abs(clift) > 1.0 && Math.abs(clift) <= 10.0) {
                    liftab = clift ;
                    off2Gg.setColor(Color.yellow);
                    off2Gg.fillRect(0,100,7 + (int) (25*Math.abs(liftab)),10) ;
                    off2Gg.drawString("0",180,70) ;
                 }
             }

             off2Gg.setColor(Color.white);
             off2Gg.drawString(String.valueOf(filter3(liftab)),110,75) ;
             off2Gg.drawString(" X 10 ",150,75) ;
          }

          if(dispp == 25)  {      /*  draw the generating cylinder */
             off2Gg.setColor(Color.yellow) ;
             for (j=1; j<=nln2-1; ++j) {           /* lower half */
                for (i=1 ; i<= nptc-1; ++i) {
                   exes[0] = (int) (fact*xplg[j][i]) + xt ;
                   whys[0] = (int) (fact*(-yplg[j][i])) + yt ;
                   exes[1] = (int) (fact*xplg[j][i+1]) + xt ;
                   whys[1] = (int) (fact*(-yplg[j][i+1])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
                                                 // stagnation lines
             exes[1] = (int) (fact*xplg[nln2][1]) + xt ;
             whys[1] = (int) (fact*(-yplg[nln2][1])) + yt ;
             for (i=2 ; i<= npt2-1; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (fact*xplg[nln2][i]) + xt ;
                   whys[1] = (int) (fact*(-yplg[nln2][i])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
             exes[1] = (int) (fact*xplg[nln2][npt2+1]) + xt ;
             whys[1] = (int) (fact*(-yplg[nln2][npt2+1])) + yt ;
             for (i=npt2+2 ; i<= nptc; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xplg[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-yplg[nln2][i])) + yt ;
                off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }

             for (j=nln2+1; j<=nlnc; ++j) {          /* upper half */
                for (i=1 ; i<= nptc-1; ++i) {
                   exes[0] = (int) (fact*xplg[j][i]) + xt ;
                   whys[0] = (int) (fact*(-yplg[j][i])) + yt ;
                   exes[1] = (int) (fact*xplg[j][i+1]) + xt ;
                   whys[1] = (int) (fact*(-yplg[j][i+1])) + yt ;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
                                         // draw the cylinder
             off2Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xplg[0][npt2])) + xt ;
             whys[1] = (int) (fact*(-yplg[0][npt2])) + yt ;
             exes[2] = (int) (fact*(xplg[0][npt2])) + xt ;
             whys[2] = (int) (fact*(-yplg[0][npt2])) + yt ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xplg[0][npt2-i])) + xt ;
                whys[1] = (int) (fact*(-yplg[0][npt2-i])) + yt ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xplg[0][npt2+i])) + xt ;
                whys[2] = (int) (fact*(-yplg[0][npt2+i])) + yt ;
                off2Gg.fillPolygon(exes,whys,4) ;
             }
                                         // draw the axes
             off2Gg.setColor(Color.cyan) ;
             exes[1] = (int) (fact*(0.0)) + xt ;
             whys[1] = (int) (fact*(-10.0)) + yt ;
             exes[2] = (int) (fact*(0.0)) + xt ;
             whys[2] = (int) (fact*(10.0)) + yt ;
             off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             exes[1] = (int) (fact*(-10.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             exes[2] = (int) (fact*(10.0)) + xt ;
             whys[2] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                                         // draw the poles
             exes[1] = (int) (fact*(1.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawString("*",exes[1],whys[1]+5) ;
             exes[1] = (int) (fact*(-1.0)) + xt ;
             whys[1] = (int) (fact*(0.0)) + yt ;
             off2Gg.drawString("*",exes[1],whys[1]+5) ;
          }
          g.drawImage(offImg2,0,0,this) ;   
       }
     }     // Plt 

     class Perf extends Panel {
        Tunnel outerparent ;
        TextArea prnt ;

        Perf (Tunnel target) {

           setLayout(new GridLayout(1,1,0,0)) ;

           prnt = new TextArea() ;
           prnt.setEditable(false) ;

           prnt.appendText("Tunnel - Wind Tunnel 1.0 beta - 10 Sept 08 ") ;
           add(prnt) ;
        }
     }  // Perf
  } // Out 

  class Viewer extends Canvas  
         implements Runnable{
     Tunnel outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Tunnel target) {
         setBackground(Color.black) ;
         runner = null ;
     } 

     public Insets insets() {
        return new Insets(0,5,0,5) ;
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
/*
             if (x >= 30 ) {   
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
             if (x < 30 ) {   
               sldloc = y ;
               if (sldloc < 30) sldloc = 30;
               if (sldloc > 165) sldloc = 165;
               fact = 10.0 + (sldloc-30)*1.0 ;
               spanfac = (int)(2.0*fact*1.0*.3535) ;
               xt1 = xt + spanfac ;
               yt1 = yt - spanfac ;
               xt2 = xt - spanfac;
               yt2 = yt + spanfac ;
             }
         con.dwn.o1.setText(String.valueOf(xt)) ;
         con.dwn.o2.setText(String.valueOf(yt)) ;
         con.dwn.o3.setText(String.valueOf(sldloc)) ;
         con.dwn.o4.setText(String.valueOf(fact)) ;
*/

         }
     }

     public void handleb(int x, int y) {
         if (y >= 300) { 
            if (x >= 82 && x <= 232) { // transparent wall
               viewflg = 0 ;
            }
            if (x >= 240 && x <= 390) { // solid wall
               viewflg = 2 ;
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
        double yprs,yprs1 ;
        int camx[] = new int[40] ;
        int camy[] = new int[40] ;
        Color col ;

        col = new Color(0,0,0) ;
        if(planet == 0) col = Color.cyan ;
        if(planet == 1) col = Color.orange ;
        if(planet == 2) col = Color.green ;
        if(planet >= 3) col = Color.cyan ;
        off1Gg.setColor(Color.lightGray) ;
        off1Gg.fillRect(0,0,500,500) ;
        off1Gg.setColor(Color.black) ;
        exes[0] = 0 ;
        whys[0] = 200 ;
        exes[1] = 500 ;
        whys[1] = 250 ;
        exes[2] = 500 ;
        whys[2] = 500 ;
        exes[3] = 0 ;
        whys[3] = 500 ;
        off1Gg.fillPolygon(exes,whys,4) ;

        radvec = .5 ;

        if (viewflg == 0 || viewflg == 2) {  // edge View
         if (vfsd > .01) {
                                            /* plot airfoil flowfield */
          for (j=1; j<=nln2-1; ++j) {           /* lower half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                yprs = .1 * xpl[j][i] ;
                yprs1 = .1 * xpl[j][i+1] ;
                whys[0] = (int) (fact*(-ypl[j][i] + yprs)) + yt ;
                slope = (ypl[j][i+1] - yprs1 -ypl[j][i] +yprs)/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] - yprs + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                   /* MODS  21 JUL 99 */
                  off1Gg.setColor(Color.yellow) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  yprs = .1 * xpl[j][i+1] ;
                  whys[1] = (int) (fact*(-ypl[j][i+1] +yprs)) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 2 && (i/3*3 == i) ) {
                  off1Gg.setColor(col) ;
                  for (n=1 ; n <= 4 ; ++n) {
                     if(i == 6 + (n-1)*9) off1Gg.setColor(Color.yellow);
                  }
                  if(i/9*9 == i) off1Gg.setColor(Color.white);
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 1 && ((i-antim)/3*3 == (i-antim)) ) {
                  if (ancol == -1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(col);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(Color.white);
                  }
                  if (ancol == 1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(Color.white);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(col);
                  }
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
          }
 
          off1Gg.setColor(Color.white) ; /* stagnation */
          exes[1] = (int) (fact*xpl[nln2][1]) + xt ;
          yprs = .1 * xpl[nln2][1] ;
          whys[1] = (int) (fact*(-ypl[nln2][1] +yprs)) + yt ;
          for (i=2 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                yprs = .1 * xpl[nln2][i] ;
                whys[1] = (int) (fact*(-ypl[nln2][i] +yprs)) + yt ;
                if (displ <= 2) {             /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
          exes[1] = (int) (fact*xpl[nln2][npt2+1]) + xt ;
          yprs = .1 * xpl[nln2][npt2+1] ;
          whys[1] = (int) (fact*(-ypl[nln2][npt2+1] +yprs)) + yt ;
          for (i=npt2+2 ; i<= nptc; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                yprs = .1 * xpl[nln2][i] ;
                whys[1] = (int) (fact*(-ypl[nln2][i] +yprs)) + yt ;
                if (displ <= 2) {                         /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
        }
                                               /*  probe location */
          if (pboflag > 0 && pypl <= 0.0) {
             off1Gg.setColor(Color.magenta) ;
             yprs = .1 * pxpl ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl + yprs)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl + yprs)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl + yprs)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl +50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3 && vfsd >= .01) {  // smoke trail 
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  yprs = .1 * xpl[19][i] ;
                  yprs1 = .1 * xpl[19][i+1] ;
                  whys[0] = (int) (fact*(-ypl[19][i] + yprs)) + yt ;
                  slope = (ypl[19][i+1] - yprs1 -ypl[19][i] + yprs)/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] - yprs + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
/*
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
*/
               }
             }
          }
 
 //  wing surface
          if (viewflg == 2) {           // 3d geom
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
                off1Gg.setColor(Color.red) ;
                off1Gg.fillPolygon(exes,whys,4) ;
                off1Gg.setColor(Color.black) ;
                off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             }
          }

         if (vfsd > .01) {
          for (j=nln2+1; j<=nlnc; ++j) {          /* upper half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                yprs = .1 * xpl[j][i] ;
                yprs1 = .1 * xpl[j][i+1] ;
                whys[0] = (int) (fact*(-ypl[j][i] + yprs)) + yt ;
                slope = (ypl[j][i+1] - yprs1 -ypl[j][i] + yprs)/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] - yprs + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                     /* MODS  21 JUL 99 */
                  off1Gg.setColor(col) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  yprs = .1 * xpl[j][1+1] ;
                  whys[1] = (int) (fact*(-ypl[j][i+1] + yprs)) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 2 && (i/3*3 == i) ) {
                  off1Gg.setColor(col);   /* MODS  27 JUL 99 */
                  for (n=1 ; n <= 4 ; ++n) {
                     if(i == 6 + (n-1)*9) off1Gg.setColor(Color.yellow);
                  }
                  if(i/9*9 == i) off1Gg.setColor(Color.white);
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                if (displ == 1 && ((i-antim)/3*3 == (i-antim)) ) {
                  if (ancol == -1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(col);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(Color.white);
                  }
                  if (ancol == 1) {          /* MODS  27 JUL 99 */
                    if((i-antim)/6*6 == (i-antim))off1Gg.setColor(Color.white);
                    if((i-antim)/6*6 != (i-antim))off1Gg.setColor(col);
                  }
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
             }
           }
         }
          if (pboflag > 0 && pypl > 0.0) {
             off1Gg.setColor(Color.magenta) ;
             yprs = .1 * pxpl ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl + yprs)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl + yprs)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl + yprs)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl -50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3 && vfsd >= .01)  {    // smoke trail 
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  yprs = .1 * xpl[19][i] ;
                  yprs1 = .1 * xpl[19][i+1] ;
                  whys[0] = (int) (fact*(-ypl[19][i] + yprs)) + yt ;
                  slope = (ypl[19][i+1] - yprs1 -ypl[19][i] + yprs)/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] - yprs + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
/*
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
*/
               }
             }
           }
 
         if (viewflg == 0) {
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
  // middle airfoil geometry
             off1Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt ;
                whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xpl[0][npt2+i])) + xt ;
                whys[2] = (int) (fact*(-ypl[0][npt2+i])) + yt ;
                camx[i] = (exes[1] + exes[2]) / 2 ;
                camy[i] = (whys[1] + whys[2]) / 2 ;
                if (foiltype == 3) {
                    off1Gg.setColor(Color.yellow) ;
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
                else {
                    off1Gg.setColor(Color.white) ;
                    off1Gg.fillPolygon(exes,whys,4) ;
                }
             }
 //   back foil
             off1Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt1 ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt1 ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt1 ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt1 ;
             for (i=1 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*(xpl[0][npt2-i])) + xt1 ;
                whys[1] = (int) (fact*(-ypl[0][npt2-i])) + yt1 ;
                exes[3] = exes[2] ;
                whys[3] = whys[2] ;
                exes[2] = (int) (fact*(xpl[0][npt2+i])) + xt1 ;
                whys[2] = (int) (fact*(-ypl[0][npt2+i])) + yt1 ;
                camx[i] = (exes[1] + exes[2]) / 2 ;
                camy[i] = (whys[1] + whys[2]) / 2 ;
                off1Gg.fillPolygon(exes,whys,4) ;
             }
// leading and trailing edge
             off1Gg.setColor(Color.white) ;
             exes[1] = (int) (fact*(xpl[0][npt2])) + xt1 ;
             whys[1] = (int) (fact*(-ypl[0][npt2])) + yt1 ;
             exes[2] = (int) (fact*(xpl[0][npt2])) + xt2 ;
             whys[2] = (int) (fact*(-ypl[0][npt2])) + yt2 ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             exes[1] = (int) (fact*(xpl[0][1])) + xt1 ;
             whys[1] = (int) (fact*(-ypl[0][1])) + yt1 ;
             exes[2] = (int) (fact*(xpl[0][1])) + xt2 ;
             whys[2] = (int) (fact*(-ypl[0][1])) + yt2 ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
   // put some info on the geometry
             if (displ == 3) {
                if (foiltype <= 3) {
                   inmax = 1 ;
                   for (n=1; n <= nptc; ++n) {
                     if(xpl[0][n] > xpl[0][inmax]) inmax = n ;
                   }
                   off1Gg.setColor(Color.green) ;
                   exes[0] = (int) (fact*(xpl[0][inmax])) + xt ;
                   whys[0] = (int) (fact*(-ypl[0][inmax])) + yt ;
                   off1Gg.drawLine(exes[0],whys[0],exes[0]-250,whys[0]) ;
                   off1Gg.drawString("Reference",30,whys[0]+10) ;
                   off1Gg.drawString("Angle",exes[0]+20,whys[0]) ;
      
                   off1Gg.setColor(Color.cyan) ;
                   exes[1] = (int) (fact*(xpl[0][inmax] -
                         4.0*Math.cos(convdr*alfval)))+xt;
                   whys[1] = (int) (fact*(-ypl[0][inmax] -
                         4.0*Math.sin(convdr*alfval)))+yt;
                   off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   off1Gg.drawString("Chord Line",exes[0]+20,whys[0]+20) ;
   
                   off1Gg.setColor(Color.red) ;
                   off1Gg.drawLine(exes[1],whys[1],camx[5],camy[5]) ;
                   for (i=7 ; i<= npt2-6; i = i+2) {
                      off1Gg.drawLine(camx[i],camy[i],camx[i+1],camy[i+1]) ;
                   }
                   off1Gg.drawString("Mean Camber Line",exes[0]-70,whys[1]-10) ;
                }
                if (foiltype >= 4) {
                   off1Gg.setColor(Color.red) ;
                   exes[0] = (int) (fact*(xpl[0][1])) + xt ;
                   whys[0] = (int) (fact*(-ypl[0][1])) + yt ;
                   exes[1] = (int) (fact*(xpl[0][npt2])) +xt ;
                   whys[1] = (int) (fact*(-ypl[0][npt2])) + yt ;
                   off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   off1Gg.drawString("Diameter",exes[0]+20,whys[0]+20) ;
                }
   
                off1Gg.setColor(Color.green) ;
                off1Gg.drawString("Flow",30,145) ;
                off1Gg.drawLine(30,152,60,152) ;
                exes[0] = 60 ;  exes[1] = 60; exes[2] = 70 ;
                whys[0] = 157 ;  whys[1] = 147 ; whys[2] = 152  ;
                off1Gg.fillPolygon(exes,whys,3) ;
             }
                                     //  spin the cylinder and ball
             if (foiltype >= 4) {
                exes[0] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*(plthg[1] + 180.)))) + xt ;
                whys[0] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*(plthg[1] + 180.)))) + yt ;
                exes[1] = (int) (fact* (.5*(xpl[0][1] + xpl[0][npt2]) +
                     rval * Math.cos(convdr*plthg[1]))) + xt ;
                whys[1] = (int) (fact* (-ypl[0][1] +
                     rval * Math.sin(convdr*plthg[1]))) + yt ;
                off1Gg.setColor(Color.red) ;
                off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
          }
          if (viewflg == 2) {
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
  // put some info on the geometry
             if (displ == 3) {
                off1Gg.setColor(Color.green) ;
                exes[1] = (int) (fact*(xpl[0][1])) + xt1 + 20 ;
                whys[1] = (int) (fact*(-ypl[0][1])) + yt1 ;
                exes[2] = (int) (fact*(xpl[0][1])) + xt2 + 20 ;
                whys[2] = (int) (fact*(-ypl[0][1])) + yt2 ;
                off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                off1Gg.drawString("Span",exes[2]+10,whys[2]+10) ;

                exes[1] = (int) (fact*(xpl[0][1])) + xt2 ;
                whys[1] = (int) (fact*(-ypl[0][1])) + yt2 + 15 ;
                exes[2] = (int) (fact*(xpl[0][npt2])) + xt2  ;
                whys[2] = whys[1] ;
                off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
                if (foiltype <= 3) off1Gg.drawString("Chord",exes[2]+10,whys[2]+15);
                if (foiltype >= 4) off1Gg.drawString("Diameter",exes[2]+10,whys[2]+15);

                off1Gg.drawString("Flow",40,75) ;
                off1Gg.drawLine(30,82,60,82) ;
                exes[0] = 60 ;  exes[1] = 60; exes[2] = 70 ;
                whys[0] = 87 ;  whys[1] = 77 ; whys[2] = 82  ;
                off1Gg.fillPolygon(exes,whys,3) ;
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
          }
        }

// Labels
/*
        off1Gg.setColor(Color.black) ;
        off1Gg.fillRect(0,0,300,30) ;
        off1Gg.setColor(Color.white) ;
        off1Gg.drawString("View:",5,10) ;
        if (viewflg == 0) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Edge",65,10) ;
        if (viewflg == 1) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Top",115,10) ;
        if (viewflg == 2) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Side-3D",150,10) ;
        off1Gg.setColor(Color.red) ;
        off1Gg.drawString("Find",210,10) ;
*/
   
              off1Gg.setColor(Color.lightGray) ;
              off1Gg.fillRect(0,295,500,50) ;
              off1Gg.setColor(Color.white) ;
              if (viewflg == 0) off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(72,302,160,20) ;
              off1Gg.setColor(Color.black) ;
              off1Gg.drawString("Transparent Surface",80,317) ;

              off1Gg.setColor(Color.white) ;
              if (viewflg == 2) off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(240,302,150,20) ;
              off1Gg.setColor(Color.black) ;
              off1Gg.drawString("Solid Surface",268,317) ;
/*
        if (displ == 0) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Streamlines",55,25) ;
        if (displ == 1) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Moving",130,25) ;
        if (displ == 2) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Frozen",180,25) ;
        if (displ == 3) off1Gg.setColor(Color.yellow) ;
        else off1Gg.setColor(Color.cyan) ;
        off1Gg.drawString("Geometry",230,25) ;
        off1Gg.setColor(Color.white) ;
        off1Gg.drawString("Display:",5,25) ;
 // zoom in

        off1Gg.setColor(Color.black) ;
        off1Gg.fillRect(0,30,30,150) ;
        off1Gg.setColor(Color.green) ;
        off1Gg.drawString("Zoom",2,180) ;
        off1Gg.drawLine(15,35,15,165) ;
        off1Gg.fillRect(5,sldloc,20,5) ;
*/

        g.drawImage(offImg1,0,0,this) ;   
    }
  } // end Viewer
 
 public static void main(String args[]) {
    Tunnel foilst = new Tunnel() ;

    f = new Frame("Tunnel -  Wind Tunnel Version 1.0j") ;
    f.add("Center", foilst) ;
    f.resize(900, 700);
    f.show() ;

    foilst.init() ;
    foilst.start() ;
 }
}
