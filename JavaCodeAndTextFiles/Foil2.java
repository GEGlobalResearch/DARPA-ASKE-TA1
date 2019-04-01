/*
                      FoilSim II  - Airfoil  mode
                     University  Version - added input
                  Application Version ... stands alone
                                          reads and writes files
   
                           A Java Application
               to perform Kutta-Joukowski Airfoil analysis

                     Version 1.5a   - 8 Mar 04

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
 
  New test -
               include the drag - can I find it/program it ?
                    if we include this - put separation bubble on airfoil
             * rename modules and change layout
             * add option to remove Kutta conditions
             * add generating cylinder input and plot
             * add printed output
             * change colr scheme of plot

                                           TJB  8 Mar 04

*/

import java.awt.*;
import java.lang.Math ;
import java.io.* ;

public class Foil extends java.applet.Applet {
 
   static double convdr = 3.1415926/180. ;
   static double pid2 = 3.1415926/2.0 ;
   static double rval,ycval,xcval,gamval,alfval,thkval,camval,chrd,clift ;
   static double thkinpt,caminpt ;                 /* MODS 10 Sep 99 */
   static double leg,teg,lem,tem;
   static double usq,vsq,alt,altmax,area,armax,armin ;
   static double chord,span,aspr,arold,chrdold,spnold ; 
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

   int inptopt,outopt,iprint ;
   int nptc,npt2,nlnc,nln2,rdflag,browflag,probflag,anflag;
   int foiltype,flflag,lunits,lftout,planet ;
   int displ,viewflg,dispp,dout,antim,ancol,sldloc; 
   int calcrange,arcor ;
       /* units data */
   static double vmn,almn,angmn,vmx,almx,angmx ;
   static double camn,thkmn,camx,thkmx ;
   static double chrdmn,spanmn,armn,chrdmx,spanmx,armx ;
   static double radmn,spinmn,radmx,spinmx ;
   static double vconv,vmax ;
   static double pconv,pmax,pmin,lconv,rconv,fconv,fmax,fmaxb;
   int lflag,gflag,plscale,nond;
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
     add(con) ;
     add(in) ;
     add(out) ;
 
     f.show() ;

     solve.getFreeStream ();
     computeFlow () ;
     view.start() ;
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
            pconv = 14.696  ;                   /* lb/sq in */
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                    /* meters */
            vconv = 1.097; vmax = 400. ;   /* km/hr  */
            if (planet == 2) vmax = 80. ;
            fconv = 4.448 ; fmax = 500000.; fmaxb = 2.5; /* newtons */
            pconv = 101.325 ;               /* kilo-pascals */
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
       int i1,i2,i3,i4,i5,i6 ;
       double v1,v2,v3,v4,v5,v6 ;
       float fl1,fl2,fl3,fl4,fl5,fl6 ;
                  //  dimensional
       if (lunits == 0) {
           in.siz.inl.l1.setText("Chord-ft") ;
           in.siz.inl.l2.setText("Span-ft") ;
           in.siz.inl.l3.setText("Area-sq ft") ;
           in.flt.inl.l1.setText("Speed-mph") ;
           in.cyl.inl.l2.setText("Radius ft") ;
           in.cyl.inl.l3.setText("Span ft") ;
           if(planet == 2)in.flt.inl.l2.setText("Depth-ft") ;
           if(planet != 2)in.flt.inl.l2.setText("Altitude-ft");
       }
       if (lunits == 1) {
           in.siz.inl.l1.setText("Chord-m") ;
           in.siz.inl.l2.setText("Span-m") ;
           in.siz.inl.l3.setText("Area-sq m") ;
           in.flt.inl.l1.setText("Speed-km/h") ;
           in.cyl.inl.l2.setText("Radius m") ;
           in.cyl.inl.l3.setText("Span m") ;
           if(planet == 2)in.flt.inl.l2.setText("Depth-m") ;
           if(planet != 2)in.flt.inl.l2.setText("Altitude-m");
       }
       v1 = chord ;
       chrdmn = 0.1*lconv;   chrdmx = 20.1*lconv ;
       v2 = span ;
       spanmn = 0.1*lconv;   spanmx = 125.1*lconv ;
       if (planet == 2) {
           chrdmx = 5.1*lconv ;
           spanmx = 10.1*lconv ;
       }
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

       fl1 = (float) v1 ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = filter3(v4) ;
       fl5 = (float) v5 ;
       fl6 = (float) v6 ;
   
       in.siz.inl.f1.setText(String.valueOf(fl1)) ;
       in.siz.inl.f2.setText(String.valueOf(fl2)) ;
       in.siz.inl.f3.setText(String.valueOf(fl3)) ;
       in.flt.inl.f1.setText(String.valueOf(fl4)) ;
       in.flt.inl.f2.setText(String.valueOf(fl5)) ;
       in.cyl.inl.f2.setText(String.valueOf(fl6)) ;
       in.cyl.inl.f3.setText(String.valueOf(fl2)) ;
   
       i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
       i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
       i3 = (int) (((v3 - armn)/(armx-armn))*1000.) ;
       i4 = (int) (((v4 - vmn)/(vmx-vmn))*1000.) ;
       i5 = (int) (((v5 - almn)/(almx-almn))*1000.) ;
       i6 = (int) (((v6 - radmn)/(radmx-radmn))*1000.) ;

       in.siz.inr.sld1.s1.setValue(i1) ;
       in.siz.inr.sld2.s2.setValue(i2) ;
       in.siz.inr.sld3.s3.setValue(i3) ;
       in.flt.inr.s1.setValue(i4) ;
       in.flt.inr.s2.setValue(i5) ;
       in.cyl.inr.s2.setValue(i6) ;
       in.cyl.inr.s3.setValue(i2) ;
                //  non-dimensional
       v1 = caminpt ;
       v2 = thkinpt ;
       v3 = alfval ;
       v4 = spin*60.0 ;

       fl1 = (float) v1 ;
       fl2 = (float) v2 ;
       fl3 = (float) v3 ;
       fl4 = (float) v4 ;

       in.shp.inl.f1.setText(String.valueOf(fl1)) ;
       in.shp.inl.f2.setText(String.valueOf(fl2)) ;
       in.shp.inl.f3.setText(String.valueOf(fl3)) ;
       in.cyl.inl.f1.setText(String.valueOf(fl4)) ;

       i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
       i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
       i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
       i4 = (int) (((v4 - spinmn)/(spinmx-spinmn))*1000.) ;
     
       in.shp.inr.s1.setValue(i1) ;
       in.shp.inr.s2.setValue(i2) ;
       in.shp.inr.s3.setValue(i3) ;
       in.cyl.inr.s1.setValue(i4) ;
                // generating cylinder
       v1 = rval ;
       v2 = xcval ;
       v3 = ycval ;
       v4 = gamval ;

       fl1 = filter3(v1) ;
       fl2 = filter3(v2) ;
       fl3 = filter3(v3) ;
       fl4 = filter3(v4) ;

       in.genp.inl.f1.setText(String.valueOf(fl1)) ;
       in.genp.inl.f2.setText(String.valueOf(fl2)) ;
       in.genp.inl.f3.setText(String.valueOf(fl3)) ;
       in.genp.inl.f4.setText(String.valueOf(fl4)) ;

       i1 = (int) (((v1 - 1.0)/(4.0))*1000.) ;
       i2 = (int) (((v2 + 1.0)/(2.0))*1000.) ;
       i3 = (int) (((v3 + 1.0)/(2.0))*1000.) ;
       i4 = (int) (((v4 + 2.0)/(4.0))*1000.) ;

       in.genp.inr.s1.setValue(i1) ;
       in.genp.inr.s2.setValue(i2) ;
       in.genp.inr.s3.setValue(i3) ;
       in.genp.inr.s4.setValue(i4) ;

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


        if (lftout == 1) {
          con.outlft.setText(String.valueOf(filter3(clift))) ;
        }
        if (lftout == 0) {
          lift = clift * q0 * area / lconv / lconv ; /* lift in lbs */
          lift = lift * fconv ;
          if (Math.abs(lift) <= 10.0) {
             con.outlft.setText(String.valueOf(filter3(lift)) + outunit) ;
          }
          if (Math.abs(lift) > 10.0) {
             con.outlft.setText(String.valueOf(filter0(lift)) + outunit) ;
          }
        }
     }
     if (foiltype >= 4) {     // cylinder and ball

        lift = rho * vfsd/vconv * gamval * vfsd/vconv * span/lconv; // lift lbs
        if (foiltype == 5) lift = lift * 3.1415926 / 2.0 ;  // ball 
        lift = lift * fconv ;
        if (Math.abs(lift) <= 10.0) {
           con.outlft.setText(String.valueOf(filter3(lift)) + outunit) ;
        }
        if (Math.abs(lift) > 10.0) {
           con.outlft.setText(String.valueOf(filter0(lift)) + outunit) ;
        }
        if (lftout == 1) {
          clift = (lift/fconv) / ( q0 *  area/lconv/lconv) ;
          con.outlft.setText(String.valueOf(filter3(clift))) ;
        }
     }
 
     switch (lunits)  {
       case 0: {                             /* English */
           in.flt.inl.o1.setText(String.valueOf(filter3(ps0/144.))) ;
           in.flt.inl.lo1.setText("Press-lb/in2") ;
           in.flt.inr.inr2.o2.setText(String.valueOf(filter0(ts0 - 460.))) ;
           in.flt.inr.inr2.lo2.setText("Temp-F") ;
           in.flt.inr.inr3.o3.setText(String.valueOf(filter5(rho))) ;
           in.flt.inr.inr3.lo3.setText("slug/cu ft") ;
           in.siz.inl.o4.setText(String.valueOf(filter3(aspr))) ;
           break;
        }
        case 1: {                             /* Metric */
           in.flt.inl.o1.setText(String.valueOf(filter3(101.325/14.696*ps0/144.))) ;
           in.flt.inl.lo1.setText("Press-kPa") ;
           in.flt.inr.inr2.o2.setText(String.valueOf(filter0(ts0*5.0/9.0 - 273.1))) ;
           in.flt.inr.inr2.lo2.setText("Temp-C") ;
           in.flt.inr.inr3.o3.setText(String.valueOf(filter3(rho*515.4)))
;
           in.flt.inr.inr3.lo3.setText("kg/cu m") ;
           in.siz.inl.o4.setText(String.valueOf(filter3(aspr))) ;
           break ;
        }
     }

     return ;
  }

  public void loadProbe() {   // probe output routine

     pbval = 0.0 ;
     if (pboflag == 1) pbval = vel * vfsd ;           // velocity
     if (pboflag == 2) pbval = ((ps0 + pres * q0)/2116.217) * pconv ; // pressure
 
     out.prb.r.l2.repaint() ;
     return ;
  }

  class Solver {
 
     Solver () {
     }

     public void setDefaults() {

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
        alfval = 5.0 ;
        gamval = 0.0 ;
        radius = 1.0 ;
        spin = 0.0 ;
        spindr = 1.0 ;
        rval = 1.0 ;
        ycval = 0.0 ;
        xcval = 0.0 ;
        displ   = 1 ;                            
        viewflg = 0 ;
        dispp = 0 ;
        calcrange = 0 ;
        dout = 0 ;
        stfact = 1.0 ;
 
        xpval = 2.1;
        ypval = -.5 ;
        pboflag = 0 ;
        xflow = -10.0;                             /* MODS  20 Jul 99 */

        pconv = 14.696;
        pmin = .5 ;
        pmax = 1.0 ;
        fconv = 1.0 ;
        fmax = 100000. ;
        fmaxb = .50 ;
        vconv = .6818 ;
        vfsd = 100. ;
        vmax = 250. ;
        lconv = 1.0 ;

        alt = 0.0 ;
        altmax = 50000. ;
        chrdold = chord = 5.0 ;
        spnold = span = 20.0 ;
        aspr = 4.0 ;
        arold = area = 100.0 ;
        armax = 2500.01 ;
        armin = .01 ;                 /* MODS 9 SEP 99 */
 
        xt = 150;  yt = 100; fact = 20.0 ;
        sldloc = 40 ;
        xtp = 95; ytp = 140; factp = 25.0 ;
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
        chrdmn = .1 ;  chrdmx = 20.1 ;
        spanmn = .1 ;  spanmx = 125.1 ;
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
            ps0 = 2116.217 * Math.pow(ts0/518.6,5.256) ;
         }
         if (hite >= 36152. && hite <= 82345.) {   // Stratosphere
            ts0 = 389.98 ;
            ps0 = 2116.217 * .2236 *
                 Math.exp((36000.-hite)/(53.35*389.98)) ;
         }
         if (hite >= 82345.) {
            ts0 = 389.98 + 1.645 * (hite-82345)/1000. ;
            ps0 = 2116.217 *.02456 * Math.pow(ts0/389.98,-11.388) ;
         }
         temf = ts0 - 459.6 ;
         if (temf <= 0.0) temf = 0.0 ;                    
/* Eq 1:6A  Domasch  - effect of humidity 
         rlhum = 0.0 ;
         presm = ps0 * 29.92 / 2116.217 ;
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
         ps0 = (2116.217 - rho * g0 * hite) ;
       }

       if (planet == 3) {   // specify air temp and pressure 
          rho = ps0/(rgas*ts0) ;
       }

       if (planet == 4) {   // specify fluid density
          ps0 = 2116.217 ;
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
           plp[index] = ((ps0 + pres * q0)/2116.217) * pconv ;
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

  class Con extends Panel {
     Foil outerparent ;
     Label l1,l2 ;
     Choice untch,outch,analch,outpch,inptch;
     Button bt3,endit,record;
     TextField outlft ;
     Pout pout;
   
     Con (Foil target) { 
       outerparent = target ;
       setLayout(new GridLayout(6,2,5,5)) ;

       l1 = new Label("Output", Label.RIGHT) ;
       l1.setForeground(Color.red) ;
       l2 = new Label("Input", Label.RIGHT) ;
       l2.setForeground(Color.blue) ;

       bt3 = new Button("Reset") ;
       bt3.setBackground(Color.orange) ;
       bt3.setForeground(Color.black) ;
 
       endit = new Button("Exit") ;
       endit.setBackground(Color.red) ;
       endit.setForeground(Color.white) ;
 
       record = new Button("Print Data") ;
       record.setBackground(Color.blue) ;
       record.setForeground(Color.white) ;

       untch = new Choice() ;
       untch.addItem("English Units") ;
       untch.addItem("Metric Units");
       untch.setBackground(Color.white) ;
       untch.setForeground(Color.red) ;
       untch.select(0) ;

       outch = new Choice() ;
       outch.setBackground(Color.white) ;
       outch.setForeground(Color.black) ;
       outch.addItem("Lift ") ;
       outch.addItem("  Cl ");
       outch.select(0) ;

       outpch = new Choice() ;
       outpch.setBackground(Color.white) ;
       outpch.setForeground(Color.red) ;
       outpch.addItem("Plot") ;
       outpch.addItem("Plot Selection") ;
       outpch.addItem("Probe");
       outpch.addItem("Lift Meter");
       outpch.addItem("Performance");
       outpch.addItem("Printed");
       outpch.select(0) ;

       inptch = new Choice() ;
       inptch.setBackground(Color.white) ;
       inptch.setForeground(Color.blue) ;
       inptch.addItem("Flight Test") ;
       inptch.addItem("Shape/Angle");
       inptch.addItem("Size");
       inptch.addItem("Generating Plane");
       inptch.select(0) ;

       outlft = new TextField("12.5",5) ;
       outlft.setBackground(Color.black) ;
       outlft.setForeground(Color.yellow) ;

       analch = new Choice() ;
       analch.addItem("Ideal Flow") ;
       analch.addItem("Stall Model") ;
       analch.addItem("No Kutta Condition") ;
       analch.setBackground(Color.white) ;
       analch.setForeground(Color.black) ;
       analch.select(0) ;

       pout = new Pout(outerparent) ;

       add(analch) ;
       add(endit) ;

       add(untch) ;
       add(bt3) ;

       add(l2) ;
       add(inptch) ;

       add(l1) ;
       add(outpch) ;

       add(record) ;
       add(pout) ;

       add(outch) ;
       add(outlft) ;
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
          anflag = analch.getSelectedIndex() ;
                // **** the lunits check MUST come first
          setUnits () ;
                // **** inputs MUST come before outputs
          inptopt = inptch.getSelectedIndex() ;
          switch(inptopt) {
             case 0: {       
                layin.show(in, "first")  ;
                probflag = 2 ;
                flflag = 1 ;
                break ;
             }
             case 1: {
                if (foiltype <= 3) layin.show(in, "second")  ;
                if (foiltype >= 4) layin.show(in, "fourth")  ;
                probflag = 0 ;
                flflag = 1 ;
                break ;
             }
             case 2: {
                if (foiltype <= 3) layin.show(in, "third")  ;
                if (foiltype >= 4) layin.show(in, "fourth")  ;
                probflag = 1 ;
                flflag = 1 ;
                break ;
             }
             case 3: {
                layin.show(in, "fifth")  ;
                probflag = 3 ;
                flflag = 0 ;
                break ;
             }
          }

          lftout = outch.getSelectedIndex() ;
          oldout = outopt ;
          outopt = outpch.getSelectedIndex() ;
          if (outopt == 5) {  // special logic for printed output
             layin.show(in, "sixth")  ;
             outopt = oldout ;
             outpch.select(outopt) ;
          }
          if (outopt != oldout) {
             switch(outopt) {
                case 0: {       
                   if (oldout == 3) dispp = 0;
                   layout.show(out, "first")  ;
                   layperf.show(pout, "first")  ;
                   pboflag = 0 ;
                   break ;
                }
                case 1: {       
                   layout.show(out, "fourth")  ;
                   layperf.show(pout, "first")  ;
                   pboflag = 0 ;
                   break ;
                }
                case 2: {
                   layout.show(out, "second")  ;
                   layperf.show(pout, "first")  ;
                   pboflag = 0 ;
                   break ;
                }
                case 3: {
                   layout.show(out, "first")  ;
                   layperf.show(pout, "first")  ;
                   dispp = 20 ; pboflag = 0 ;
                   break ;
                }
                case 4: {
                   layout.show(out, "third")  ;
                   layperf.show(pout, "second")  ;
                   pboflag = 0 ;
                   break ;
                }
             }
          }

          loadInput() ;

          return true ;
       }
       else return false ;
     } // Choice handler
 
      public void handleRefs(Event evt, Object arg) {
        String label = (String)arg ;
        double mapfac ;
        int index ;

        if(label.equals("Reset")) {
           solve.setDefaults() ;
           in.flt.inr.plntch.select(0) ;
           in.shp.inr.shapch.select(0);
           in.cyl.inr.shapch.select(0);
           in.siz.inr.arch.select(0);
           in.flt.inl.o1.setBackground(Color.black) ;
           in.flt.inl.o1.setForeground(Color.yellow) ;
           in.flt.inr.inr2.o2.setBackground(Color.black) ;
           in.flt.inr.inr2.o2.setForeground(Color.yellow) ;
           in.flt.inr.inr3.o3.setBackground(Color.black) ;
           in.flt.inr.inr3.o3.setForeground(Color.yellow) ;
           layin.show(in, "first")  ;
           con.inptch.select(0) ;
           analch.select(anflag) ;
           untch.select(lunits) ;
                   // **** the lunits check MUST come first
           setUnits () ;
           lftout = outch.getSelectedIndex() ;
           layplt.show(out.grf.l, "first") ;
           layout.show(out, "first")  ;
           con.outpch.select(0) ;
           outopt = 0 ;
           layperf.show(con.pout, "first")  ;
   
           loadInput() ;
        }

        if(label.equals("Exit")) {
           f.dispose() ;
           System.exit(1) ;
        }

        if(label.equals("Print Data")) {
           if (iprint == 1) {  // file open - print data
              prnt.println("----------------------------------------- ");
              prnt.println(" ") ;
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
                 case 4: {
                    prnt.println( " Rotating Cylinder" ) ;
                    break ;
                 }
                 case 5: {
                    prnt.println( " Spinning Ball" ) ;
                    break ;
                 }
              }
              if (anflag == 0) prnt.println( " Ideal Flow Analysis - Kutta Condition" ) ;
              if (anflag == 1) prnt.println( " Ideal Flow Analysis - Stall Model" ) ;
              if (anflag == 2) prnt.println( " Ideal Flow Analysis - No Kutta Condition" ) ;
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
                 prnt.println( " Angle of attack = " + String.valueOf(filter3(alfval)) + " degrees ," ) ;
              }
              if (foiltype >= 4) {
                 prnt.println( " Spin  = " + String.valueOf(filter3(spin*60.0)) + " rpm ," ) ;
                 if (lunits == 0) {
                    prnt.println( " Radius = " + String.valueOf(filter3(radius)) + " ft ," ) ;
                    prnt.println( " Span = " + String.valueOf(filter3(span)) + " ft ," ) ;
                 }
                 if (lunits == 1) {
                    prnt.println( " Radius = " + String.valueOf(filter3(radius)) + " m ," ) ;
                    prnt.println( " Span = " + String.valueOf(filter3(span)) + " m ," ) ;
                 }
              }
              switch(planet) {
                 case 0: {
                    prnt.println( " Standard Earth Atmosphere" ) ;                     
                    break ;
                 }
                 case 1: {
                    prnt.println( " Martian Atmosphere" ) ;
                    break ;
                 }
                 case 2: {
                    prnt.println( " Under Water" ) ;
                    break ;
                 }
                 case 3: {
                    prnt.println( " Specified Conditions" ) ;
                    break ;
                 }
                 case 4: {
                    prnt.println( " Specified Conditions" ) ;
                    break ;
                 }
              }
              switch (lunits)  {
                 case 0: {                             /* English */
                    prnt.println( " Altitude = " + String.valueOf(filter0(alt)) + " ft ," ) ;
                    prnt.println( " Density = " + String.valueOf(filter5(rho)) + "slug/cu ft ," ) ;
                    prnt.println( " Pressure = " + String.valueOf(filter3(ps0/144.)) + "lb/sq in," ) ;
                    prnt.println( " Temperature = " + String.valueOf(filter0(ts0 - 460.)) + "F,");
                    prnt.println( " Airspeed = " + String.valueOf(filter0(vfsd)) + " mph ," ) ;
                    break;
                 }
                 case 1: {                             /* Metric */
                    prnt.println( " Altitude = " + String.valueOf(filter0(alt)) + " m ," ) ;
                    prnt.println( " Density = " + String.valueOf(filter3(rho*515.4)) + "kg/cu m" );
                    prnt.println( " Pressure = " + String.valueOf(filter3(101.325/14.696*ps0/144.)) + "kPa," );
                    prnt.println( " Temperature = " + String.valueOf(filter0(ts0*5.0/9.0 - 273.1)) + "C," ) ;
                    prnt.println( " Airspeed = " + String.valueOf(filter0(vfsd)) + " km/hr ," ) ;
                    break;
                 }
              }
              if (lftout == 1)
                 prnt.println( " Lift Coefficient = " + String.valueOf(filter3(clift)) ) ;
              if (lftout == 0) {
                if (Math.abs(lift) <= 10.0) {
                    if (lunits == 0) prnt.println( "  Lift = " + String.valueOf(filter3(lift)) + " lbs " ) ;
                    if (lunits == 1) prnt.println( "  Lift = " + String.valueOf(filter3(lift)) + " Newtons " ) ;
                }
                if (Math.abs(lift) > 10.0) {
                    if (lunits == 0) prnt.println( "  Lift = " + String.valueOf(filter0(lift)) + " lbs " ) ;
                    if (lunits == 1) prnt.println( "  Lift = " + String.valueOf(filter0(lift)) + " Newtons " ) ;
                }
              }
              if (stfact < 1.0 ) prnt.println( " Airfoil is stalled " ) ;
              prnt.println( "\n \t Upper Surface \t \t \t Lower Surface") ;
              prnt.println( " X \t Y \t P \t V \t X \t Y \t P \t V  ");
              mapfac = 4.0 ;
              if (foiltype >= 4) mapfac = 2.0 ;
              if (stfact >= 1.0) {
                 for (index = 0; index <= npt2-1; ++ index) {
                  prnt.println(  String.valueOf(filter3(xpl[0][npt2-index]/mapfac)) +  "\t"
                   + String.valueOf(filter3(ypl[0][npt2-index]/mapfac)) + "\t" 
                   + String.valueOf(filter3(plp[npt2-index])) + "\t"
                   + String.valueOf(filter0(plv[npt2-index])) + "\t" 
                   + String.valueOf(filter3(xpl[0][npt2+index]/mapfac)) + "\t"
                   + String.valueOf(filter3(ypl[0][npt2+index]/mapfac)) + "\t" 
                   + String.valueOf(filter3(plp[npt2+index])) + "\t"
                   + String.valueOf(filter0(plv[npt2+index])) ) ;
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

           if (iprint == 0) {  // file closed
              return;
           }
        }
     } //  button handler 
 
     class Pout extends Panel {
        Foil outerparent ;
        S s ;
        Ns ns ;

        Pout (Foil target) {
           outerparent = target ;
           layperf = new CardLayout() ;
           setLayout(layperf) ;

           s  = new S(outerparent) ;
           ns = new Ns(outerparent) ;

           add ("first", ns) ;
           add ("second", s) ;
        }

        class S extends Panel {  // show performance buttons
           Foil outerparent ;
           Button bt1,bt2;

           S (Foil target) {
              outerparent = target ;
              setLayout(new GridLayout(1,2,5,5)) ;

              bt1 = new Button("Geom") ;
              bt1.setBackground(Color.white) ;
              bt1.setForeground(Color.red) ;
              bt2 = new Button("Data") ;
              bt2.setBackground(Color.white) ;
              bt2.setForeground(Color.red) ;

              add(bt1) ;
              add(bt2) ;
           }

           public boolean action(Event evt, Object arg) {
             String label = (String)arg ;

             if(evt.target instanceof Button) {
                if(label.equals("Data")) this.printData(arg) ;
                if(label.equals("Geom")) this.printGeom(arg) ;
                return true ;
             }
             else return false ;
           } // Handler

           public void printData(Object obj) {
               switch(foiltype) {
                 case 1: {       
                    out.perf.prnt.appendText( "\n\n Joukowski Airfoil" ) ;
                    break ;
                 }
                 case 2: {        
                    out.perf.prnt.appendText( "\n\n Elliptical Airfoil" ) ;
                    break ;
                 }
                 case 3: {     
                    out.perf.prnt.appendText( "\n\n Plate" ) ;
                    break ;
                 }
                 case 4: {     
                    out.perf.prnt.appendText( "\n\n Rotating Cylinder" ) ;
                    break ;
                 }
                 case 5: {     
                    out.perf.prnt.appendText( "\n\n Spinning Ball" ) ;
                    break ;
                 }
               }
               if (foiltype <= 3) {
                  out.perf.prnt.appendText( "\n Camber = " + filter3(caminpt) ) ;
                  out.perf.prnt.appendText( " % chord ," ) ;
                  out.perf.prnt.appendText( " Thickness = " + filter3(thkinpt) ) ;
                  out.perf.prnt.appendText( " % chord ," ) ;
                  out.perf.prnt.appendText( "\n Chord = " + filter3(chord) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
                  out.perf.prnt.appendText( " Span = " + filter3(span) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
                  out.perf.prnt.appendText( "\n Surface Area = " + filter3(area) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " sq ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " sq m ," ) ;
                  out.perf.prnt.appendText( "\n  Angle of attack = " + filter3(alfval) ) ;
                  out.perf.prnt.appendText( " degrees ," ) ;
               }
               if (foiltype >= 4) {
                  out.perf.prnt.appendText( "\n Spin  = " + filter3(spin*60.0) ) ;
                  out.perf.prnt.appendText( " rpm ," ) ;
                  out.perf.prnt.appendText( " Radius = " + filter3(radius) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
                  out.perf.prnt.appendText( "\n Span = " + filter3(span) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               }
               switch(planet) {
                 case 0: {       
                    out.perf.prnt.appendText( "\n Standard Earth Atmosphere" ) ;
                    break ;
                 }
                 case 1: {       
                    out.perf.prnt.appendText( "\n Martian Atmosphere" ) ;
                    break ;
                 }
                 case 2: {       
                    out.perf.prnt.appendText( "\n Under Water" ) ;
                    break ;
                 }
                 case 3: {       
                    out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                    break ;
                 }
                 case 4: {       
                    out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                    break ;
                 }
               }
               out.perf.prnt.appendText( "\n Altitude = " + filter0(alt) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               switch (lunits)  {
                  case 0: {                             /* English */
                     out.perf.prnt.appendText( " Density = " + filter5(rho) ) ;
                     out.perf.prnt.appendText( "slug/cu ft" ) ;
                     out.perf.prnt.appendText( "\n Pressure = " + filter3(ps0/144.) ) ;
                     out.perf.prnt.appendText( "lb/sq in," ) ;
                     out.perf.prnt.appendText( " Temperature = " + filter0(ts0 - 460.) );
                     out.perf.prnt.appendText( "F," ) ;
                     break;
                  }
                  case 1: {                             /* Metric */
                     out.perf.prnt.appendText( " Density = " + filter3(rho*515.4) );
                     out.perf.prnt.appendText( "kg/cu m" ) ;
                     out.perf.prnt.appendText( "\n Pressure = " + filter3(101.325/14.696*ps0/144.) );
                     out.perf.prnt.appendText( "kPa," ) ;
                     out.perf.prnt.appendText( " Temperature = " + filter0(ts0*5.0/9.0 - 273.1) ) ;
                     out.perf.prnt.appendText( "C," ) ;
                     break;
                  }
               }
               out.perf.prnt.appendText( "\n Airspeed = " + filter0(vfsd) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " mph ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " km/hr ," ) ;
               if (lftout == 1)
                  out.perf.prnt.appendText( "\n  Lift Coefficient = " + filter3(clift) ) ;
               if (lftout == 0) {
                 if (Math.abs(lift) <= 10.0) out.perf.prnt.appendText( "\n  Lift = " + filter3(lift) ) ;
                 if (Math.abs(lift) > 10.0) out.perf.prnt.appendText( "\n  Lift  = " + filter0(lift) ) ;
                 if (lunits == 0) out.perf.prnt.appendText( " lbs " ) ;
                 if (lunits == 1) out.perf.prnt.appendText( " Newtons " ) ;
               }
            } // print Data
      
            public void printGeom(Object obj) {
               int index ;
               double mapfac; 
      
               switch(foiltype) {
                 case 1: {       
                    out.perf.prnt.appendText( "\n\n Joukowski Airfoil" ) ;
                    break ;
                 }
                 case 2: {        
                    out.perf.prnt.appendText( "\n\n Elliptical Airfoil" ) ;
                    break ;
                 }
                 case 3: {     
                    out.perf.prnt.appendText( "\n\n Plate" ) ;
                    break ;
                 }
                 case 4: {     
                    out.perf.prnt.appendText( "\n\n Rotating Cylinder" ) ;
                    break ;
                 }
                 case 5: {     
                    out.perf.prnt.appendText( "\n\n Spinning Ball" ) ;
                    break ;
                 }
               }
               if (foiltype <= 3) {
                 out.perf.prnt.appendText( "\n Camber = " + filter3(caminpt) ) ;
                 out.perf.prnt.appendText( " % chord ," ) ;
                 out.perf.prnt.appendText( " Thickness = " + filter3(thkinpt) ) ;
                 out.perf.prnt.appendText( " % chord ," ) ;
                 out.perf.prnt.appendText( "\n Chord = " + filter3(chord) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
                 out.perf.prnt.appendText( " Span = " + filter3(span) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
                 out.perf.prnt.appendText( "\n Angle of attack = " + filter3(alfval) );
                 out.perf.prnt.appendText( " degrees ," ) ;
               }
               if (foiltype >= 4) {
                  out.perf.prnt.appendText( "\n Spin  = " + filter3(spin*60.0) ) ;
                  out.perf.prnt.appendText( " rpm ," ) ;
                  out.perf.prnt.appendText( " Radius = " + filter3(radius) ) ;
                  if (lunits == 0) out.perf.prnt.appendText( " ft ," ) ;
                  if (lunits == 1) out.perf.prnt.appendText( " m ," ) ;
               }
               switch(planet) {
                 case 0: {       
                    out.perf.prnt.appendText( "\n Standard Earth Atmosphere" ) ;
                    break ;
                 }
                 case 1: {       
                    out.perf.prnt.appendText( "\n Martian Atmosphere" ) ;
                    break ;
                 }
                 case 2: {       
                    out.perf.prnt.appendText( "\n Under Water" ) ;
                    break ;
                 }
                 case 3: {       
                    out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                    break ;
                 }
                 case 4: {       
                    out.perf.prnt.appendText( "\n Specified Conditions" ) ;
                    break ;
                 }
               }
               switch (lunits)  {
                  case 0: {                             /* English */
                     out.perf.prnt.appendText( "\n Ambient Pressure = " + filter3(ps0/144.) ) ;
                     out.perf.prnt.appendText( "lb/sq in," ) ;
                     break;
                  }
                  case 1: {                             /* Metric */
                     out.perf.prnt.appendText( "\n Ambient Pressure = " + filter3(101.325/14.696*ps0/144.) );
                     out.perf.prnt.appendText( "kPa," ) ;
                     break;
                  }
               }
               out.perf.prnt.appendText( "\n Ambient Velocity = " + filter0(vfsd) ) ;
               if (lunits == 0) out.perf.prnt.appendText( " mph ," ) ;
               if (lunits == 1) out.perf.prnt.appendText( " km/hr ," ) ;

               out.perf.prnt.appendText( "\n \t Upper Surface \t \t \t ") ;
               out.perf.prnt.appendText( "\n X \t Y \t P \t V \t ");
               mapfac = 4.0 ;
               if (foiltype >= 4) mapfac = 2.0 ;
               for (index = 0; index <= npt2-1; ++ index) {
                out.perf.prnt.appendText( "\n" + filter3(xpl[0][npt2-index]/mapfac) +  "\t"
                 + filter3(ypl[0][npt2-index]/mapfac) + "\t" + filter3(plp[npt2-index]) + "\t"
                 + filter0(plv[npt2-index]) + "\t"); 
               }
               out.perf.prnt.appendText( "\n \t Lower Surface \t \t \t ") ;
               for (index = 0; index <= npt2-1; ++ index) {
                out.perf.prnt.appendText( "\n"  + filter3(xpl[0][npt2+index]/mapfac) + "\t"
                 + filter3(ypl[0][npt2+index]/mapfac) + "\t" + filter3(plp[npt2+index]) + "\t"
                 + filter0(plv[npt2+index]) ) ;
               }
            } // print Geom
        } // end Show

        class Ns extends Panel {
           Foil outerparent ;
           Label l2 ;
   
           Ns (Foil target) {
              outerparent = target ;
              setLayout(new GridLayout(1,1,5,5)) ;

              l2 = new Label(" ", Label.RIGHT) ;

              add(l2) ;
           }
        }  // No Show
     } //  end Pout

  } // Con

  class In extends Panel {
     Foil outerparent ;
     Flt flt ;
     Shp shp ;
     Siz siz ;
     Cyl cyl ;
     Genp genp ;
     Filep filep ;

     In (Foil target) { 
        outerparent = target ;
        layin = new CardLayout() ;
        setLayout(layin) ;

        flt = new Flt(outerparent) ;
        shp = new Shp(outerparent) ;
        siz = new Siz(outerparent) ;
        cyl = new Cyl(outerparent) ;
        genp = new Genp(outerparent) ;
        filep = new Filep(outerparent) ;

        add ("first", flt) ;
        add ("second", shp) ;
        add ("third", siz) ;
        add ("fourth", cyl) ;
        add ("fifth", genp) ;
        add ("sixth", filep) ;
     }
 
     class Flt extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Flt (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,o1 ;
           Label l1,l2 ;
           Label l01,l02 ;
           Label lo1 ;
     
           Inl (Foil target) {
    
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Flight", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Test", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Speed-mph", Label.CENTER) ;
            f1 = new TextField("100.0",5) ;

            l2 = new Label("Altitude-ft", Label.CENTER) ;
            f2 = new TextField("0.0",5) ;

            lo1 = new Label("Press-lb/in^2", Label.CENTER) ;
            o1 = new TextField("0.0",5) ;
            o1.setBackground(Color.black) ;
            o1.setForeground(Color.yellow) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(lo1) ;
            add(o1) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(new Label("Density", Label.RIGHT)) ;
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

              alt = v2 ;
              if(v2 < almn) {
                alt = v2 = almn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > almx) {
                alt = v2 = almx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
    
              i1 = (int) (((v1 - vmn)/(vmx-vmn))*1000.) ;
              i2 = (int) (((v2 - almn)/(almx-almn))*1000.) ;
     
              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;

              if (planet == 3) {    // read in the pressure
                V1 = Double.valueOf(o1.getText()) ;
                v1 = V1.doubleValue() ;
                ps0 = v1 /pconv * 2116.217 ;
                if(ps0 < .5) {
                  ps0 = .5 ;
                  v1 = ps0 / 2116.217 * pconv ;
                  fl1 = (float) v1 ;
                  o1.setText(String.valueOf(fl1)) ;
                }
                if(ps0 > 5000.) {
                  ps0 = 5000. ;
                  v1 = ps0 / 2116.217 * pconv ;
                  fl1 = (float) v1 ;
                  o1.setText(String.valueOf(fl1)) ;
                }
              }

       //  set limits on spin
              if(foiltype >= 4) cyl.setLims() ;

              computeFlow() ;
              return true ;
            }
            else return false ;
         } // Handler
       }  // Inl

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2;
           Choice plntch;
           Inr2 inr2 ;
           Inr3 inr3 ;

           Inr (Foil target) {
            int i1,i2 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((100.0 - vmn)/(vmx-vmn))*1000.) ;
            i2 = (int) (((0.0 - almn)/(almx-almn))*1000.) ;

            plntch = new Choice() ;
            plntch.addItem("Earth - Average Day") ;
            plntch.addItem("Mars - Average Day");
            plntch.addItem("Water-Const Density");
            plntch.addItem("Specify Air T & P");
            plntch.addItem("Specify Fluid Density");
            plntch.setBackground(Color.white) ;
            plntch.setForeground(Color.blue) ;
            plntch.select(0) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            inr2 = new Inr2(outerparent) ;
            inr3 = new Inr3(outerparent) ;

            add(plntch) ;
            add(s1) ;
            add(s2) ;
            add(inr2) ;
            add(inr3) ;
          }

          class Inr3 extends Panel {
             Foil outerparent ;
             TextField o3;
             Label lo3 ;

             Inr3 (Foil target) {
               outerparent = target ;
               setLayout(new GridLayout(1,2,2,10)) ;

               lo3 = new Label("slug/cu ft", Label.LEFT) ;
               o3 = new TextField("0.0",5) ;
               o3.setBackground(Color.black) ;
               o3.setForeground(Color.yellow) ;

               add(o3) ;
               add(lo3) ;
             }

             public boolean handleEvent(Event evt) {
               Double V1 ;
               double v1 ;
               float fl1 ;
  
               if(evt.id == Event.ACTION_EVENT) {

                if (planet == 4) {    // read in the density
                   V1 = Double.valueOf(o3.getText()) ;
                   v1 = V1.doubleValue() ;
                   rho = v1 ;
                   if (lunits == 1) rho = v1 /515.4 ;
                   if(rho < .000001) {
                     rho = .000001 ;
                     v1 = rho;
                     if (lunits == 1) v1 = rho * 515.4 ;
                     fl1 = (float) v1 ;
                     o3.setText(String.valueOf(fl1)) ;
                   }
                   if(rho > 3.0) {
                     rho = 3. ;
                     v1 = rho;
                     if (lunits == 1) v1 = rho * 515.4 ;
                     fl1 = (float) v1 ;
                     o3.setText(String.valueOf(fl1)) ;
                   }
                 }

                 computeFlow() ;
                 return true ;
               }
               else return false ;
             }
          }

          class Inr2 extends Panel {
             Foil outerparent ;
             TextField o2;
             Label lo2 ;

             Inr2 (Foil target) {
               outerparent = target ;
               setLayout(new GridLayout(1,2,2,10)) ;

               lo2 = new Label("Temp-F", Label.CENTER) ;
               o2 = new TextField("12.5",5) ;
               o2.setBackground(Color.black) ;
               o2.setForeground(Color.yellow) ;

               add(lo2) ;
               add(o2) ;
             }

             public boolean handleEvent(Event evt) {
               Double V1 ;
               double v1 ;
               float fl1 ;

               if(evt.id == Event.ACTION_EVENT) {

                 if (planet == 3) {    // read in the temperature
                   V1 = Double.valueOf(o2.getText()) ;
                   v1 = V1.doubleValue() ;
                   ts0 = v1 + 460. ;
                   if (lunits == 1) ts0 = (v1 + 273.1)*9.0/5.0 ;
                   if(ts0 < 350.) {
                     ts0 = 350. ;
                     v1 = ts0 - 460. ;
                     if (lunits == 1) v1 = ts0*5.0/9.0 - 273.1 ;
                     fl1 = (float) v1 ;
                     o2.setText(String.valueOf(fl1)) ;
                   }
                   if(ts0 > 660.) {
                     ts0 = 660. ;
                     v1 = ts0 - 460. ;
                     if (lunits == 1) v1 = ts0*5.0/9.0 - 273.1 ;
                     fl1 = (float) v1 ;
                     o2.setText(String.valueOf(fl1)) ;
                   }
                 }

                 computeFlow() ;
                 return true ;
               }
               else return false ;
             }
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
             int i1,i2 ;
             double v1,v2 ;
             float fl1,fl2 ;
     // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             vfsd   = v1 = i1 * (vmx - vmn)/ 1000. + vmn ;
             alt    = v2 = i2 * (almx - almn)/ 1000. + almn ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;

       //  set limits on spin
             if(foiltype >= 4) cyl.setLims() ;

             computeFlow() ;
          } // handle bar

          public void handleCho(Event evt) {
             int i1,i2 ;
             double v1,v2 ;
             float fl1,fl2 ;

             planet  = plntch.getSelectedIndex() ;

             if (planet == 2) {
                vfsd = 5. ;
                vmax = 50. ;
                if (lunits == 1) vmax = 80. ;
                alt = 0.0 ;
                altmax = 5000. ;
                area = 10.0 ;
                armax = 50. ;
             }
             else {
                vmax = 250. ;
                if (lunits == 1) vmax = 400. ;
                altmax = 50000. ;
                armax = 2500. ;
             }

             if (planet == 3) {
                inl.o1.setBackground(Color.white) ;
                inl.o1.setForeground(Color.black) ;
                inr.inr2.o2.setBackground(Color.white) ;
                inr.inr2.o2.setForeground(Color.black) ;
             }
             else {
                inl.o1.setBackground(Color.black) ;
                inl.o1.setForeground(Color.yellow) ;
                inr.inr2.o2.setBackground(Color.black) ;
                inr.inr2.o2.setForeground(Color.yellow) ;
             }

             if (planet == 4) {
                inr.inr3.o3.setBackground(Color.white) ;
                inr.inr3.o3.setForeground(Color.black) ;
             }
             else {
                inr.inr3.o3.setBackground(Color.black) ;
                inr.inr3.o3.setForeground(Color.yellow) ;
             }

             if (planet <= 1) {
                if (foiltype <= 3) {
                   layplt.show(out.grf.l, "first") ;
                }
                if (foiltype >= 4) {
                   layplt.show(out.grf.l, "second") ;
                }
             }
             if (planet >= 2) {
                layplt.show(out.grf.l, "second") ;
             }

             layout.show(out, "first")  ;
             con.outpch.select(0) ;
             outopt = 0 ;
             layperf.show(con.pout, "first")  ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
          } // handle  choice
        }  // Inr 
     }  // Flt 

     class Shp extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Shp (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02 ;
      
           Inl (Foil target) {
      
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Airfoil", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Shape", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Camber-%c", Label.CENTER) ;
            f1 = new TextField("0.0",5) ;

            l2 = new Label("Thick-%crd", Label.CENTER) ;
            f2 = new TextField("12.5",5) ;

            l3 = new Label("Angle-deg", Label.CENTER) ;
            f3 = new TextField("5.0",5) ;

            add(l01) ;
            add(l02) ;

            add(l3) ;
            add(f3) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;
 
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

              i1 = (int) (((v1 - camn)/(camx-camn))*1000.) ;
              i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
              i3 = (int) (((v3 - angmn)/(angmx-angmn))*1000.) ;
    
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
           Foil outerparent ;
           Scrollbar s1,s2,s3;
           Choice shapch ;

           Inr (Foil target) {
            int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((0.0 - camn)/(camx-camn))*1000.) ;
            i2 = (int) (((12.5 - thkmn)/(thkmx-thkmn))*1000.) ;
            i3 = (int) (((alfval - angmn)/(angmx-angmn))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

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
            add(s3) ;
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
             i3 = s3.getValue() ;

             caminpt = v1 = i1 * (camx - camn)/ 1000. + camn ;
             camval  = caminpt / 25.0 ;
             thkinpt = v2 = i2 * (thkmx - thkmn)/ 1000. + thkmn ;
             thkval  = thkinpt / 25.0 ;
             alfval  = v3 = i3 * (angmx - angmn)/ 1000. + angmn ;

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

             in.cyl.inr.shapch.select(foiltype-1);
             layout.show(out, "first")  ;
             con.outpch.select(0) ;
             outopt = 0 ;
             layperf.show(con.pout, "first")  ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           }
        }  // Inr
     }  // Shp 

     class Siz extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Siz (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3,o4 ;
           Label l1,l2,l3,l4 ;
           Label l01,l02 ;
    
           Inl (Foil target) {
   
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Wing", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Size", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Chord-ft", Label.CENTER) ;
            f1 = new TextField("5.0",5) ;

            l2 = new Label("Span-ft", Label.CENTER) ;
            f2 = new TextField("20.0",5) ;

            l3 = new Label("Area-sq ft", Label.CENTER) ;
            f3 = new TextField("100.0",5) ;

            l4 = new Label("Aspect Rat", Label.CENTER) ;
            o4 = new TextField("0.0",5) ;
            o4.setBackground(Color.black) ;
            o4.setForeground(Color.yellow) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;

            add(l4) ;
            add(o4) ;
         }

          public boolean handleEvent(Event evt) {
            Double V1,V2,V3 ;
            double v1,v2,v3 ;
            float fl1 ;
            int i1,i2,i3,choice ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;

              chord = v1 ;
              if(v1 < chrdmn) {
                chord = v1 = chrdmn ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > chrdmx) {
                chord = v1 = chrdmx ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }

              span = v2 ;
              if(v2 < spanmn) {
                span = v2 = spanmn ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > spanmx) {
                span = v2 = spanmx ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
   
              area = v3 ;
              if(v3 < armn) {
                area = v3 = armn  ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > armx) {
                area = v3 = armx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }

        // keeping consistent
             choice = 0 ;
             if (chord >= (chrdold+.01) || chord <= (chrdold-.01))choice = 1;
             if (span >= (spnold+.1) || span <= (spnold-.1)) choice = 2;
             if (area >= (arold+1.0) || area <= (arold-1.0)) choice = 3;
             switch(choice) {
                case 1: {          // chord changed
                  if (chord < span) {
                    v3 = span * chord ;
                    aspr = span*span/v3 ;
                  }
                  if (chord >= span) {
                    v2 = chord ;
                    aspr = 1.0 ;
                    v3 = v2 * chord ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                    spnold = span = v2 ;
                  }
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
                  if (viewflg == 2) {
                    fact = fact * chord/chrdold ;
                  }
                  chrdold = chord ;
                  arold = area = v3 ;
                  break ;
                }
                case 2: {          // span changed
                  if (span > chord) {
                    v3 = span * chord ;
                    aspr = span*span/v3 ;
                  }
                  if (span <= chord) {
                     v1 = span ;
                     aspr = 1.0 ;
                     v3 = v1 * span ;
                     fl1 = (float) v1 ;
                     f1.setText(String.valueOf(fl1)) ;
                     chord = v1 ;
                     if (viewflg == 2) {
                       fact = fact * chord/chrdold ;
                     }
                     chrdold = chord ;
                   }
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                   spnold = span ;
                   arold = area = v3 ;
                   break ;
                }
                case 3: {          // area changed
                   v2 = Math.sqrt(area*aspr) ;
                   v1 = area / v2 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                   chord = v1 ;
                   if (viewflg == 2) {
                     fact = fact * chord/chrdold ;
                   }
                   chrdold = chord ;
                   spnold = span = v2 ;
                   arold = area ;
                }
              }
              spanfac = (int)(2.0*fact*aspr*.3535) ;

              i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
              i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
              i3 = (int) (((v3 - armn)/(armx-armn))*1000.) ;
   
              inr.sld1.s1.setValue(i1) ;
              inr.sld2.s2.setValue(i2) ;
              inr.sld3.s3.setValue(i3) ;

              computeFlow() ;
              return true ;
            }
            else return false ;
          } // Handler
        }  // Inl 

        class Inr extends Panel {
           Foil outerparent ;
           Sld1 sld1 ;
           Sld2 sld2 ;
           Sld3 sld3 ;
           Choice arch;

           Inr (Foil target) {
            int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
            i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;
            i3 = (int) (((area - armn)/(armx-armn))*1000.) ;

            sld1 = new Sld1(outerparent) ;
            sld2 = new Sld2(outerparent) ;
            sld3 = new Sld3(outerparent) ;

            arch = new Choice() ;
            arch.addItem("AR correction - OFF") ;
            arch.addItem("AR correction - ON");
            arch.setBackground(Color.white) ;
            arch.setForeground(Color.red) ;
            arch.select(0) ;

            add(new Label(" ", Label.CENTER)) ;
            add(sld1) ;
            add(sld2) ;
            add(sld3) ;
            add(arch) ;
          }

          public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleCho(evt) ;
                  return true ;
               }
               else return false ;
          }

          public void handleCho(Event evt) {

             arcor = arch.getSelectedIndex() ;

             computeFlow() ;
          }

          class Sld1 extends Panel {  // chord slider
             Foil outerparent ;
             Scrollbar s1 ; 

             Sld1 (Foil target) {
              int i1 ;

               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;

               i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;

               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);

               add(s1) ;
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
                int i1,i3 ;
                double v1 ;
                float fl1,fl3 ;

       // Input for computations
                i1 = s1.getValue() ;

                chord  = v1 = i1 * (chrdmx - chrdmn)/ 1000. + chrdmn ;

                if (chord >= span) {  // limit apsect ratio to 1.0
                      chord = v1 = span ;
                      i1 = (int) (((chord - chrdmn)/(chrdmx-chrdmn))*1000.) ;
                      s1.setValue(i1) ;
                }

                area = span * chord ;
                aspr = span*span/area ;
                i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
                sld3.s3.setValue(i3) ;

                if (viewflg == 2) {
                   fact = fact * chord/chrdold ;
                }

                spanfac = (int)(2.0*fact*aspr*.3535) ;

                arold = area ;
                chrdold = chord ;

                fl1 = (float) v1 ;
                fl3 = (float) area ;

                inl.f1.setText(String.valueOf(fl1)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
       
                computeFlow() ;
              }  // handler for scroll
          }  // sld1

          class Sld2 extends Panel {  // span slider
             Foil outerparent ;
             Scrollbar s2 ; 

             Sld2 (Foil target) {
              int i2 ;

               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;

               i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;

               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);

               add(s2) ;
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
                int i2,i3 ;
                double v2 ;
                float fl2,fl3 ;
  
      // Input for computations
                i2 = s2.getValue() ;

                span   = v2 = i2 * (spanmx - spanmn)/ 1000. + spanmn ;

                if (span <= chord) {  // limit apsect ratio to 1.0
                      span = v2 = chord ;
                      i2 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;
                      s2.setValue(i2) ;
                }

                area = span * chord ;
                aspr = span*span/area ;
                i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
                sld3.s3.setValue(i3) ;

                arold = area ;
                spnold = span ;

                spanfac = (int)(2.0*fact*aspr*.3535) ;

                fl2 = (float) v2 ;
                fl3 = (float) area ;
  
                inl.f2.setText(String.valueOf(fl2)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
          
                computeFlow() ;
              }  // handler for scroll
           }  // sld2

          class Sld3 extends Panel {  // area slider
             Foil outerparent ;
             Scrollbar s3 ; 
 
             Sld3 (Foil target) {
              int i3 ;
 
               outerparent = target ;
               setLayout(new GridLayout(1,1,0,0)) ;
  
               i3 = (int) (((area - armn)/(armx-armn))*1000.) ;
 
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

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
                int i1,i2,i3 ;
                double v1,v2,v3 ;
                float fl1,fl2,fl3 ;
 
       // Input for computations
                i3 = s3.getValue() ;

                area   = v3 = i3 * (armx - armn)/ 1000. + armn ;
 
                v2 = span = Math.sqrt(area*aspr) ;
                v1 = chord = area / v2 ;
                i1 = (int) (((v1 - chrdmn)/(chrdmx-chrdmn))*1000.) ;
                i2 = (int) (((v2 - spanmn)/(spanmx-spanmn))*1000.) ;
                sld1.s1.setValue(i1) ;
                sld2.s2.setValue(i2) ;

                if (viewflg == 2) {
                   fact = fact * chord/chrdold ;
                }
                spanfac = (int)(2.0*fact*aspr*.3535) ;

                arold = area ;
                spnold = span ;
                chrdold = chord ;

                fl1 = (float) v1 ;
                fl2 = (float) v2 ;
                fl3 = (float) v3 ;

                inl.f1.setText(String.valueOf(fl1)) ;
                inl.f2.setText(String.valueOf(fl2)) ;
                inl.f3.setText(String.valueOf(fl3)) ;
        
                computeFlow() ;
              }  // handler for scroll
           }  // sld3
        }     // Inr
     }  // Siz 

     class Cyl extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Cyl (Foil target) {

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
           Foil outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02 ;
     
           Inl (Foil target) {
     
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
   
              span = v3 ;
              if (foiltype == 5) {
                span = v3 = radius ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 < spanmn) {
                span = v3 = spanmn ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > spanmx) {
                span = v3 = spanmx ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              spanfac = (int)(fact*span/radius*.3535) ;
              area = 2.0*radius*span ;
              if (foiltype ==5) area = 3.1415926 * radius * radius ;

              i1 = (int) (((v1 - spinmn)/(spinmx-spinmn))*1000.) ;
              i2 = (int) (((v2 - radmn)/(radmx-radmn))*1000.) ;
              i3 = (int) (((v3 - spanmn)/(spanmx-spanmn))*1000.) ;
   
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
           Foil outerparent ;
           Scrollbar s1,s2,s3;
           Choice shapch ;

           Inr (Foil target) {
             int i1,i2,i3 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((spin*60.0 - spinmn)/(spinmx-spinmn))*1000.) ;
            i2 = (int) (((radius - radmn)/(radmx-radmn))*1000.) ;
            i3 = (int) (((span - spanmn)/(spanmx-spanmn))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);

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
            add(s3) ;
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
             i3 = s3.getValue() ;

             spin = v1 = i1 * (spinmx - spinmn)/ 1000. + spinmn ;
             spin = spin / 60.0 ;
             radius = v2 = i2 * (radmx - radmn)/ 1000. + radmn ;
             span = v3 = i3 * (spanmx - spanmn)/ 1000. + spanmn ;
             if (foiltype == 5) span = v3 = radius ;
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
                fl1 = (float) v2 ;
                in.shp.inl.f2.setText(String.valueOf(fl1)) ;
                i2 = (int) (((v2 - thkmn)/(thkmx-thkmn))*1000.) ;
                in.shp.inr.s2.setValue(i2) ;
             }
             if(foiltype == 4) layin.show(in, "fourth")  ;
             if(foiltype == 5) {
                 span = radius ;
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

             in.shp.inr.shapch.select(foiltype-1);
             layout.show(out, "first")  ;
             con.outpch.select(0) ;
             outopt = 0 ;
             layperf.show(con.pout, "first")  ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           } // handler
        }  // Inr
     }  // Cyl 

     class Genp extends Panel {
        Foil outerparent ;
        Inl inl ;
        Inr inr ;

        Genp (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           inl = new Inl(outerparent) ;
           inr = new Inr(outerparent) ;

           add(inl) ;
           add(inr) ;
        }

        class Inl extends Panel {
           Foil outerparent ;
           TextField f1,f2,f3,f4 ;
           Label l1,l2,l3,l4 ;
           Label l01,l02 ;
      
           Inl (Foil target) {
      
            outerparent = target ;
            setLayout(new GridLayout(5,2,2,10)) ;

            l01 = new Label("Generating", Label.RIGHT) ;
            l01.setForeground(Color.blue) ;
            l02 = new Label("Cylinder", Label.LEFT) ;
            l02.setForeground(Color.blue) ;

            l1 = new Label("Radius", Label.CENTER) ;
            f1 = new TextField("1.0",5) ;

            l2 = new Label("X-val", Label.CENTER) ;
            f2 = new TextField("0.0",5) ;

            l3 = new Label("Y-val", Label.CENTER) ;
            f3 = new TextField("0.0",5) ;

            l4 = new Label("Circulation", Label.CENTER) ;
            f4 = new TextField("0.0",5) ;

            add(l01) ;
            add(l02) ;

            add(l1) ;
            add(f1) ;

            add(l2) ;
            add(f2) ;

            add(l3) ;
            add(f3) ;
 
            add(l4) ;
            add(f4) ;
          }

          public boolean handleEvent(Event evt) {
            Double V1,V2,V3,V4 ;
            double v1,v2,v3,v4 ;
            float fl1 ;
            int i1,i2,i3,i4 ;

            if(evt.id == Event.ACTION_EVENT) {
              V1 = Double.valueOf(f1.getText()) ;
              v1 = V1.doubleValue() ;
              V2 = Double.valueOf(f2.getText()) ;
              v2 = V2.doubleValue() ;
              V3 = Double.valueOf(f3.getText()) ;
              v3 = V3.doubleValue() ;
              V4 = Double.valueOf(f4.getText()) ;
              v4 = V4.doubleValue() ;

              rval = v1 ;
              if(v1 < 1.0) {
                rval = v1 = 1.0 ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }
              if(v1 > 5.0) {
                rval = v1 = 5.0 ;
                fl1 = (float) v1 ;
                f1.setText(String.valueOf(fl1)) ;
              }

              xcval = v2 ;
              if(v2 < -1.0) {
                xcval = v2 = -1.0 ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
              if(v2 > 1.0) {
                xcval = v2 = 1.0 ;
                fl1 = (float) v2 ;
                f2.setText(String.valueOf(fl1)) ;
              }
    
              ycval = v3 ;
              if(v3 < -1.0) {
                ycval = v3 = -1.0  ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }
              if(v3 > 1.0) {
                ycval = v3 = 1.0 ;
                fl1 = (float) v3 ;
                f3.setText(String.valueOf(fl1)) ;
              }

              gamval = v4 ;
              if(v4 < -2.0) {
                gamval = v4 = -2.0  ;
                fl1 = (float) v4 ;
                f4.setText(String.valueOf(fl1)) ;
              }
              if(v4 > 2.0) {
                gamval = v4 = 2.0 ;
                fl1 = (float) v4 ;
                f4.setText(String.valueOf(fl1)) ;
              }

              i1 = (int) (((v1 - 1.0)/(4.0))*1000.) ;
              i2 = (int) (((v2 + 1.0)/(2.0))*1000.) ;
              i3 = (int) (((v3 + 1.0)/(2.0))*1000.) ;
              i4 = (int) (((v4 + 2.0)/(4.0))*1000.) ;
    
              inr.s1.setValue(i1) ;
              inr.s2.setValue(i2) ;
              inr.s3.setValue(i3) ;
              inr.s4.setValue(i4) ;

              computeFlow() ;
              return true ;
            }
            else return false ;
          } // Handler
        }  // Inl 

        class Inr extends Panel {
           Foil outerparent ;
           Scrollbar s1,s2,s3,s4;

           Inr (Foil target) {
            int i1,i2,i3,i4 ;

            outerparent = target ;
            setLayout(new GridLayout(5,1,2,10)) ;

            i1 = (int) (((rval - 1.0)/(4.0))*1000.) ;
            i2 = (int) (((xcval + 1.0)/(2.0))*1000.) ;
            i3 = (int) (((ycval + 1.0)/(2.0))*1000.) ;
            i4 = (int) (((gamval + 2.0)/(4.0))*1000.) ;

            s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
            s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
            s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
            s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);

            add(new Label(" ", Label.CENTER)) ;
            add(s1) ;
            add(s2) ;
            add(s3) ;
            add(s4) ;
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
             int i1,i2,i3,i4 ;
             double v1,v2,v3,v4 ;
             float fl1,fl2,fl3,fl4 ;
              
    // Input for computations
             i1 = s1.getValue() ;
             i2 = s2.getValue() ;
             i3 = s3.getValue() ;
             i4 = s4.getValue() ;

             rval = v1 = i1 * (4.0)/ 1000. + 1.0 ;
             xcval = v2 = i2 * (2.0)/ 1000. - 1.0 ;
             ycval = v3 = i3 * (2.0)/ 1000. - 1.0 ;
             gamval = v4 = i4 * (4.0)/ 1000. - 2.0 ;

             fl1 = (float) v1 ;
             fl2 = (float) v2 ;
             fl3 = (float) v3 ;
             fl4 = (float) v4 ;

             inl.f1.setText(String.valueOf(fl1)) ;
             inl.f2.setText(String.valueOf(fl2)) ;
             inl.f3.setText(String.valueOf(fl3)) ;
             inl.f4.setText(String.valueOf(fl4)) ;
     
             computeFlow() ;
           }
        }  // Inr
     }  // Genp 
 
     class Filep extends Panel {  // print file
        Foil outerparent ;
        TextField namprnt,namlab ;
        Button pbopen,cancel ;

        Filep (Foil target) {

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
              prnt.println(" FoilSim II Application Version 1.5a - Feb 04 ");
              prnt.println("  ");
              prnt.println(fillab);
              prnt.println("  ");
              iprint = 1;
              layin.show(in, "first")  ;
              con.inptch.select(0) ;
            } catch (IOException n) {
            }
          }
          if(label.equals("Cancel")) {  // Forget it
             layin.show(in, "first")  ;
             con.inptch.select(0) ;
          }
       } //end handler
    } // end Filep
  }  // In 

  class Out extends Panel {
     Foil outerparent ;
     Plt plt ;
     Prb prb ;
     Grf grf ;
     Perf perf ;

     Out (Foil target) { 
        outerparent = target ;
        layout = new CardLayout() ;
        setLayout(layout) ;

        plt = new Plt(outerparent) ;
        prb = new Prb(outerparent) ;
        grf = new Grf(outerparent) ;
        perf = new Perf(outerparent) ;

        add ("first", plt) ;
        add ("second", prb) ;
        add ("third", perf) ;
        add ("fourth", grf) ;
     }
 
     class Grf extends Panel {
        Foil outerparent ;
        U u;
        L l;

        Grf (Foil target) {
           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           u = new U(outerparent) ;
           l = new L(outerparent) ;

           add (u) ;
           add (l) ;
        }

        class U extends Panel {
           Foil outerparent ;
           Label l1,l2 ;
           Button pl1,pl2,pl3;

           U (Foil target) {
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
                con.outpch.select(0) ;
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
           Foil outerparent ;
           F f ;
           C c ;

           L (Foil target) {
              outerparent = target ;
              layplt = new CardLayout() ;
              setLayout(layplt) ;

              f = new F(outerparent) ;
              c = new C(outerparent) ;

              add ("first", f) ;
              add ("second", c) ;
           }

           class F extends Panel {
              Foil outerparent ;
              Label l2 ;
              Button pl3,pl4,pl5,pl6,pl7,pl8,pl9 ;
              Choice plout  ;
    
              F (Foil target) {
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
                   con.outpch.select(0) ;
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
              Foil outerparent ;
              Label l2 ;
   
              C (Foil target) {
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
        Foil outerparent ;
        Thread run2 ;
        Point locp,ancp;

        Plt (Foil target) { 
           setBackground(Color.blue) ;
           run2 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
            if (y <= 20) { 
               if (x >= 5 && x <= 55) {   //rescale
                   endy = 0.0 ;
                   begy = 0.0 ;
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
                  plty[2][index] = ps0/2116.217 * pconv ;
              }
              begx = 0.0 ;
              endx = 100. ;
              ntikx = 5 ;
              ntiky = 5 ;
       //       endy=1.02 * ps0/2116.217 * pconv ;
       //       begy=.95 * ps0/2116.217 * pconv ;
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
                  ppl = 2116.217 ;
                  if (planet == 0) {
                      if (hpl < 36152.)   {
                            tpl = 518.6 - 3.56 * hpl /1000. ;
                            ppl = 2116.217 * Math.pow(tpl/518.6, 5.256) ;
                      }
                         else {
                            tpl = 389.98 ;
                            ppl = 2116.217 * .236 * Math.exp((36000.-hpl)/(53.35*tpl)) ;
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
              off2Gg.fillRect(0,0,300,300) ;
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,2,70,15) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,15) ;
           }
           if (dispp > 1 && dispp <= 15) {
              off2Gg.setColor(Color.blue) ;
              off2Gg.fillRect(0,0,300,300) ;
              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,2,70,15) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("Rescale",8,15) ;
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
                 if (anflag != 1 || (anflag == 1 && Math.abs(alfval) < 10.0)) {
                   for (j=0; j<=ntr-1; ++j) {
                      if (j == 0) {
                        off2Gg.setColor(Color.magenta) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -2.5) + ytp ;
                        off2Gg.drawString("Upper",xlabel,ylabel) ;
                      }
                      if (j == 1) {
                        off2Gg.setColor(Color.yellow) ;
                        xlabel = (int) (factp* 6.1) + xtp ;
                        ylabel = (int) (factp* -1.5) + ytp ;
                        off2Gg.drawString("Lower",xlabel,ylabel) ;
                      }
                      if (j == 2) {
                        off2Gg.setColor(Color.green) ;
                        xlabel = (int) (factp* 2.0) + xtp ;
                        ylabel = (int) (factp* -5.0) + ytp ;
                        off2Gg.drawString("Free Stream",xlabel,ylabel) ;
                      }
                      exes[1] = (int) (factp*scalex*(offx+pltx[j][1])) + xtp;
                      whys[1] = (int) (factp*-scaley*(offy+plty[j][1]))+ ytp;
                      for (i=1; i<=npt; ++i) {
                        exes[0] = exes[1] ;
                        whys[0] = whys[1] ;
                        exes[1] = (int)(factp*scalex*(offx+pltx[j][i]))+xtp;
                        whys[1] = (int)(factp*-scaley*(offy+plty[j][i]))+ytp;
                        off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                      }
                   }
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

     class Prb extends Panel {
        Foil outerparent ;
        L l ;
        R r ;

        Prb (Foil target) {

           outerparent = target ;
           setLayout(new GridLayout(1,2,5,5)) ;

           l = new L(outerparent) ;
           r = new R(outerparent) ;

           add(l) ;
           add(r) ;
        }

        class L extends Panel {
           Foil outerparent ;
           Label l01 ;
           Button bt1,bt2,bt3 ;
     
           L (Foil target) {
            outerparent = target ;
            setLayout(new GridLayout(4,1,10,10)) ;

            l01 = new Label("Probe", Label.CENTER) ;
            l01.setForeground(Color.red) ;

            bt1 = new Button("Velocity") ;
            bt1.setBackground(Color.white) ;
            bt1.setForeground(Color.blue) ;
            bt2 = new Button("Pressure") ;
            bt2.setBackground(Color.white) ;
            bt2.setForeground(Color.blue) ;
            bt3 = new Button("Smoke") ;
            bt3.setBackground(Color.white) ;
            bt3.setForeground(Color.blue) ;
            add(l01) ;
            add(bt1) ;
            add(bt2) ;
            add(bt3) ;
          }

          public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Button) {
               String label = (String)arg ;
               if(label.equals("Velocity")) pboflag = 1 ;
               if(label.equals("Pressure")) pboflag = 2 ;
               if(label.equals("Smoke")) pboflag = 3 ;

               computeFlow() ;
               return true ;
            }
            else return false ;
          } // Handler
        }  // Inl

        class R extends Panel {
            Foil outerparent ;
            Scrollbar s1,s2;
            L2 l2;
            Button bt4 ;

            R (Foil target) {

             outerparent = target ;
             setLayout(new BorderLayout(5,5)) ;

             s1 = new Scrollbar(Scrollbar.VERTICAL,550,10,0,1000);
             s2 = new Scrollbar(Scrollbar.HORIZONTAL,550,10,0,1000);

             l2 = new L2(outerparent) ;

             bt4 = new Button("OFF") ;
             bt4.setBackground(Color.red) ;
             bt4.setForeground(Color.white) ;

             add("West",s1) ;
             add("South",s2) ;
             add("Center",l2) ;
             add("North",bt4) ;
           }

           public boolean handleEvent(Event evt) {
                if(evt.id == Event.ACTION_EVENT) {
                   pboflag = 0 ;
                   computeFlow() ;
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
             int i1,i2 ;

             i1 = s1.getValue() ;
             i2 = s2.getValue() ;

             ypval = 5.0 - i1 * 10.0/ 1000. ;
             xpval = i2 * 20.0/ 1000. -10.0 ;
    
             computeFlow() ;
           }

           class L2 extends Canvas  {
              Foil outerparent ;

              L2 (Foil target) {
                setBackground(Color.black) ;
              }

              public void update(Graphics g) {
                out.prb.r.l2.paint(g) ;
              }

              public void paint(Graphics g) {
                int ex,ey,index ;
                double pbout ;
    
                off3Gg.setColor(Color.black) ;
                off3Gg.fillRect(0,0,150,150) ;

                if (pboflag == 0 || pboflag == 3)off3Gg.setColor(Color.gray);
                if (pboflag == 1 || pboflag == 2)off3Gg.setColor(Color.yellow) ;
                off3Gg.fillArc(20,30,80,80,-23,227) ;
                off3Gg.setColor(Color.black) ;
         // tick marks
                for (index = 1; index <= 4; ++ index) {
                    ex = 60 + (int) (50.0 * Math.cos(convdr * (-22.5 + 45.0 * index))) ;
                    ey = 70 - (int) (50.0 * Math.sin(convdr * (-22.5 + 45.0 * index))) ;
                    off3Gg.drawLine(60,70,ex,ey) ;
                }
                off3Gg.fillArc(25,35,70,70,-25,235) ;
      
                off3Gg.setColor(Color.yellow) ;
                off3Gg.drawString("0",10,95) ;
                off3Gg.drawString("2",10,55) ;
                off3Gg.drawString("4",35,30) ;
                off3Gg.drawString("6",75,30) ;
                off3Gg.drawString("8",100,55) ;
                off3Gg.drawString("10",100,95) ;

                off3Gg.setColor(Color.green) ;
                if (pboflag == 1) {
                    off3Gg.drawString("Velocity",40,15) ;
                    if (lunits == 0) off3Gg.drawString("mph",50,125) ;
                    if (lunits == 1) off3Gg.drawString("km/h",50,125) ;
                }
                if (pboflag == 2) {
                    off3Gg.drawString("Pressure",30,15) ;
                    if (lunits == 0) off3Gg.drawString("psi",50,125) ;
                    if (lunits == 1) off3Gg.drawString("kPa",50,125) ;
                }

                off3Gg.setColor(Color.green) ;
                off3Gg.drawString("x 10",65,110) ;

                ex = 60 ;
                ey = 70 ;
               
                pbout = 0.0 ;
                if (pbval <= .001) {
                   pbout = pbval * 1000. ;
                   off3Gg.drawString("-4",90,105) ;
                }
                if (pbval <= .01 && pbval > .001) {
                   pbout = pbval * 100. ;
                   off3Gg.drawString("-3",90,105) ;
                }
                if (pbval <= .1 && pbval > .01) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-2",90,105) ;
                }
                if (pbval <= 1 && pbval > .1) {
                   pbout = pbval * 10. ;
                   off3Gg.drawString("-1",90,105) ;
                }
                if (pbval <= 10 && pbval > 1) {
                   pbout = pbval  ;
                   off3Gg.drawString("0",90,105) ;
                }
                if (pbval <= 100 && pbval > 10) {
                   pbout = pbval * .1 ;
                   off3Gg.drawString("1",90,105) ;
                }
                if (pbval <= 1000 && pbval > 100) {
                   pbout = pbval * .01 ;
                   off3Gg.drawString("2",90,105) ;
                }
                if (pbval > 1000) {
                   pbout = pbval * .001 ;
                   off3Gg.drawString("3",90,105) ;
                }
                off3Gg.setColor(Color.green) ;
                off3Gg.drawString(String.valueOf(filter3(pbout)),30,110) ;

                off3Gg.setColor(Color.yellow) ;
                ex = 60 - (int) (30.0 * Math.cos(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                ey = 70 - (int) (30.0 * Math.sin(convdr *
                           (-22.5 + pbout * 225. /10.0))) ;
                off3Gg.drawLine(60,70,ex,ey) ;

                g.drawImage(offImg3,0,0,this) ;
              }
           } //L2
        }  // Inr
     }  // Prb

     class Perf extends Panel {
        Foil outerparent ;
        TextArea prnt ;

        Perf (Foil target) {

           setLayout(new GridLayout(1,1,0,0)) ;

           prnt = new TextArea() ;
           prnt.setEditable(false) ;

           prnt.appendText("FoilSim II 1.5 beta - 2 Feb 02 ") ;
           add(prnt) ;
        }
     }  // Perf
  } // Out 

  class Viewer extends Canvas  
         implements Runnable{
     Foil outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Foil target) {
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
               if (sldloc < 30) sldloc = 30;
               if (sldloc > 165) sldloc = 165;
               fact = 10.0 + (sldloc-30)*1.0 ;
               spanfac = (int)(2.0*fact*aspr*.3535) ;
               xt1 = xt + spanfac ;
               yt1 = yt - spanfac ;
               xt2 = xt - spanfac;
               yt2 = yt + spanfac ;
             }
         }
     }

     public void handleb(int x, int y) {
         if (y < 15) { 
             if (x >= 60 && x <= 109) {   //edge view
                  viewflg = 0 ;
             }
             if (x >= 110 && x <= 140) {   //top view
                  if (foiltype <= 4) viewflg = 1 ;
                  if (foiltype == 5) viewflg = 0 ;
                  displ = 3 ;
                  pboflag = 0 ;
             }
             if (x >= 141 && x <= 209) {   //side view
                  if (foiltype <= 4) viewflg = 2 ;
                  if (foiltype == 5) viewflg = 0 ;
             }
             if (x >= 210 && x <= 240) {   //find
                  xt = 120;  yt = 85; fact = 20.0 ;
                  sldloc = 45 ;
                  spanfac = (int)(2.0*fact*aspr*.3535) ;
                  xt1 = xt + spanfac ;
                  yt1 = yt - spanfac ;
                  xt2 = xt - spanfac;
                  yt2 = yt + spanfac ;
             }
         }
         if (y > 15 && y <= 30) { 
             if (x >= 50 && x <= 124) {   //display streamlines
                 if(viewflg != 1)  displ = 0 ;
             }
             if (x >= 125 && x <= 174) {   //display animation
                 if(viewflg != 1) displ = 1 ;
             }
             if (x >= 175 && x <= 219) {   //display direction
                 if(viewflg != 1) displ = 2 ;
             }
             if (x >= 220 && x <= 300) {   //display geometry
                  displ = 3 ;
                  pboflag = 0 ;
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

        if (viewflg == 1) {              // Top View
          off1Gg.setColor(Color.white) ;
          exes[0] = (int) (.25*fact*(-span)) + xt ;
          whys[0] = (int) (.25*fact*(-chord)) + yt ;
          exes[1] = (int) (.25*fact*(-span)) + xt ;
          whys[1] = (int) (.25*fact*(chord)) + yt ;
          exes[2] = (int) (.25*fact*(span)) + xt ;
          whys[2] = (int) (.25*fact*(chord)) + yt ;
          exes[3] = (int) (.25*fact*(span)) + xt ;
          whys[3] = (int) (.25*fact*(-chord)) + yt ;
          off1Gg.fillPolygon(exes,whys,4) ;
          off1Gg.setColor(Color.green) ;
          off1Gg.drawLine(exes[0],whys[1]+5,exes[2],whys[1]+5) ;
          off1Gg.drawString("Span",exes[2]-20,whys[1]+20) ;
          off1Gg.drawLine(exes[2]+5,whys[0],exes[2]+5,whys[1]) ;
          if (foiltype <= 3) off1Gg.drawString("Chord",exes[2]+10,55) ;
          if (foiltype == 4) off1Gg.drawString("Diameter",exes[2]+10,55) ;

          off1Gg.setColor(Color.green) ;
          off1Gg.drawString("Flow",45,145) ;
          off1Gg.drawLine(40,155,40,125) ;
          exes[0] = 35 ;  exes[1] = 45; exes[2] = 40 ;
          whys[0] = 125 ;  whys[1] = 125; whys[2] = 115 ;
          off1Gg.fillPolygon(exes,whys,3) ;
        }

        if (viewflg == 0 || viewflg == 2) {  // edge View
         if (vfsd > .01) {
                                            /* plot airfoil flowfield */
          radvec = .5 ;
          for (j=1; j<=nln2-1; ++j) {           /* lower half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                whys[0] = (int) (fact*(-ypl[j][i])) + yt ;
                slope = (ypl[j][i+1]-ypl[j][i])/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                   /* MODS  21 JUL 99 */
                  off1Gg.setColor(Color.yellow) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  whys[1] = (int) (fact*(-ypl[j][i+1])) + yt ;
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
          whys[1] = (int) (fact*(-ypl[nln2][1])) + yt ;
          for (i=2 ; i<= npt2-1; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-ypl[nln2][i])) + yt ;
                if (displ <= 2) {             /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
          exes[1] = (int) (fact*xpl[nln2][npt2+1]) + xt ;
          whys[1] = (int) (fact*(-ypl[nln2][npt2+1])) + yt ;
          for (i=npt2+2 ; i<= nptc; ++i) {
                exes[0] = exes[1] ;
                whys[0] = whys[1] ;
                exes[1] = (int) (fact*xpl[nln2][i]) + xt ;
                whys[1] = (int) (fact*(-ypl[nln2][i])) + yt ;
                if (displ <= 2) {                         /* MODS  21 JUL 99 */
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
          }
                                               /*  probe location */
          if (pboflag > 0 && pypl <= 0.0) {
             off1Gg.setColor(Color.magenta) ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl +50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3) {    /* smoke trail  MODS  21 JUL 99 */
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  whys[0] = (int) (fact*(-ypl[19][i])) + yt ;
                  slope = (ypl[19][i+1]-ypl[19][i])/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
               }
             }
          }
 
 //  wing surface
          if (viewflg == 2) {           // 3d geom
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
          }

          for (j=nln2+1; j<=nlnc; ++j) {          /* upper half */
             for (i=1 ; i<= nptc-1; ++i) {
                exes[0] = (int) (fact*xpl[j][i]) + xt ;
                whys[0] = (int) (fact*(-ypl[j][i])) + yt ;
                slope = (ypl[j][i+1]-ypl[j][i])/(xpl[j][i+1]-xpl[j][i]) ;
                xvec = xpl[j][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                yvec = ypl[j][i] + slope * (xvec - xpl[j][i]) ;
                exes[1] = (int) (fact*xvec) + xt ;
                whys[1] = (int) (fact*(-yvec)) + yt ;
                if (displ == 0) {                     /* MODS  21 JUL 99 */
                  off1Gg.setColor(col) ;
                  exes[1] = (int) (fact*xpl[j][i+1]) + xt ;
                  whys[1] = (int) (fact*(-ypl[j][i+1])) + yt ;
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
                                               /*  probe location */
          if (pboflag > 0 && pypl > 0.0) {
             off1Gg.setColor(Color.magenta) ;
             off1Gg.fillOval((int) (fact*pxpl) + xt,
                  (int) (fact*(-pypl)) + yt - 2,5,5);
             off1Gg.setColor(Color.white) ;
             exes[0] = (int) (fact*(pxpl + .1)) +xt ;
             whys[0] = (int) (fact*(-pypl)) + yt ;
             exes[1] = (int) (fact*(pxpl + .5)) +xt ;
             whys[1] = (int) (fact*(-pypl)) + yt ;
             exes[2] = (int) (fact*(pxpl + .5)) +xt ;
             whys[2] = (int) (fact*(-pypl -50.)) +yt ;
             off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
             if (pboflag == 3) {    /* smoke trail  MODS  21 JUL 99 */
               off1Gg.setColor(Color.green) ;
               for (i=1 ; i<= nptc-1; ++i) {
                  exes[0] = (int) (fact*xpl[19][i]) + xt ;
                  whys[0] = (int) (fact*(-ypl[19][i])) + yt ;
                  slope = (ypl[19][i+1]-ypl[19][i])/(xpl[19][i+1]-xpl[19][i]) ;
                  xvec = xpl[19][i] + radvec / Math.sqrt(1.0 + slope*slope) ;
                  yvec = ypl[19][i] + slope * (xvec - xpl[19][i]) ;
                  exes[1] = (int) (fact*xvec) + xt ;
                  whys[1] = (int) (fact*(-yvec)) + yt ;
                  if ((i-antim)/3*3 == (i-antim) ) {
                    off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  }
               }
             }
           }
         }
 
         if (viewflg == 0) {
  // draw the airfoil geometry
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

        g.drawImage(offImg1,0,0,this) ;   
    }
  } // end Viewer
 
 public static void main(String args[]) {
    Foil foil = new Foil() ;

    f = new Frame("FoilSim II Application Version 1.5a") ;
    f.add("Center", foil) ;
    f.resize(620, 430);
    f.show() ;

    foil.init() ;
    foil.start() ;
 }
}
