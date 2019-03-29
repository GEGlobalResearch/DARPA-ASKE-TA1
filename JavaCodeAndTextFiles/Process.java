/*
                 Process  - Post Process Wind Tunnel Data 
   
                          A Java Application
            to post process Kutta-Joukowski Airfoils from tunnel
                          Derived from FoilSim II

                     Version 1.0h   - 1 July 09

                              Written by

                             Tom Benson
                       NASA Glenn Research Center

                                 and

                            Anthony Vila
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
               * break FoilSim II into three programs
                   passing data by files
               * this is the post-processing module
               * based on the Tunnel Module
               * add plot buttons
               * plot data from the test as ****
               * cleanup - and add second plot window
               * check on metric - imperial units
               * fix-up density/pressure plot
                 add cl buttons & plots for press and speed

                                           TJB  1 July 09

*/

import java.awt.*;
import java.lang.Math ;
import java.io.* ;

public class Process extends java.applet.Applet {
 
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

   static int dftp[]     = new int[41] ;
   static double dcam[]  = new double[41] ;
   static double dthk[]  = new double[41] ;
   static double dspan[]  = new double[41] ;
   static double dchrd[]  = new double[41] ;
   static double dlft[][] = new double[21][41] ;
   static double dspd[][] = new double[21][41] ;
   static double dpin[][] = new double[21][41] ;
   static int dlunits[] = new int[41];

   int numtest,tstflag ;
   static int numpt[] = new int[20];
   static int modlnm[] = new int[20];
   static int testp[] = new int[20];
   static double spd [][] = new double[21][41] ;
   static double ang [][] = new double[21][41] ;
   static double pin [][] = new double[21][41] ;
   static double lft [][] = new double[21][41] ;
   static double qdt [][] = new double[21][41] ;
   int inptopt,outopt,iprint,nummod ;
   int nptc,npt2,nlnc,nln2,rdflag,browflag,probflag,anflag;
   int foiltype,flflag,lunits,lftout,planet ;
   int displ,viewflg,dispp,dout,antim,ancol,sldloc; 
   int calcrange,arcor ;
   int counter, datp ;
       /* units data */
   static double vmn,almn,angmn,vmx,almx,angmx ;
   static double camn,thkmn,camx,thkmx ;
   static double chrdmn,armn,chrdmx,armx ;
   static double radmn,spinmn,radmx,spinmx ;
   static double psmn,psmx,tsmn,tsmx ;
   static double vconv,vmax,vcon2 ;
   static double pconv,pmax,pmin,lconv,rconv,fconv,fmax,fmaxb;
   static double piconv,ticonv,ppconv ;
   int lflag,gflag,plscale,nond;
       /*  plot & probe data */
   static double fact,xpval,ypval,pbval,factp,chrdfac;
   static double prg,pthg,pxg,pyg,pxm,pym,pxpl,pypl ;
   int pboflag,xt,yt,ntikx,ntiky,npt,xtp,ytp ;
   int xt1,yt1,xt2,yt2,spanfac ;
   int lines,nord,nabs,ntr ;
   static double begx,endx,begy,endy ;
   static String labx,labxu,laby,labyu ;
   static double pltx[][]  = new double[3][41] ;
   static double plty[][]  = new double[3][41] ;
   static double plttx[][]  = new double[3][41] ;
   static double pltty[][]  = new double[3][41] ;
   static double plt2x[][]  = new double[21][41] ;
   static double plt2y[][]  = new double[21][41] ;
   static double plthg[]  = new double[2] ;
   static int plsav[] = new int[21];

   Solver solve ;
   Con con ;
   In in ;
   Plt plt ;
   Out out ;
   CardLayout layin,layout,layplt ;
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

     out  = new Out(this) ;
     con = new Con(this) ;
     in = new In(this) ;
     plt = new Plt(this) ;

     add(plt) ;
     add(out) ;
     add(in) ;
     add(con) ;
 
     f.show() ;

     solve.getFreeStream ();
     computeFlow () ;
     out.view.start() ;
     out.plt2.start() ;
     plt.start() ;
  }
 
  public Insets insets() {
     return new Insets(5,5,5,5) ;
  }

  public void computeFlow() { 

     solve.getFreeStream () ;

     solve.getCirc ();                   /* get circulation */
     solve.getGeom ();                   /* get geometry*/
     solve.genFlow () ;
 
     loadOut() ;

     plt.loadPlot() ;
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
            vconv = .6818; vmax = 250.;      /*  mph / ft/s  */
            if (planet == 2) vmax = 50. ;
            vcon2 = 1.0 ;                    // mph
            fconv = 1.0; fmax = 100000.; fmaxb = .5;  /* pounds   */
            piconv = 1.0  ;                   /* lb/sq in */
            ppconv = 1.0 ;
            ticonv = 1.0 ;
            pconv = 14.7  ;                   /* lb/sq in */
            break;
          }
          case 1: {                             /* Metric */
            lconv = .3048;                    /* meters */
            vconv = 1.097; vmax = 400. ;     /* km/hr  */
            if (planet == 2) vmax = 80. ;
            vcon2 = 1.609 ;                  // mph -> km/hr
            fconv = 4.448 ; fmax = 500000.; fmaxb = 2.5; /* newtons */
            piconv = .04788 ;               /* kilo-pascals */
            ppconv = 478.8 ;               /* n /m^2 */
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

       aspr = span/chord ;
       area = span * chord ;
       spanfac = (int)(4.0*50.0*.3535) ;

/*
       v5 = tsin ;
       tsmn = tsmin*ticonv ;  tsmx = tsmax*ticonv ;
       fl5 = filter0(v5) ;
       in.flt.upr.inl.f4.setText(String.valueOf(fl5)) ;
       i5 = (int) (((v5 - tsmn)/(tsmx-tsmn))*1000.) ;
       in.flt.upr.inr.s4.setValue(i5) ;
*/

                //  non-dimensional

       con.o2.setText(String.valueOf(filter3(camval * 25.0)) ) ;
       con.o4.setText(String.valueOf(filter3(thkval * 25.0)) ) ;
       con.o5.setText(String.valueOf(filter3(aspr)) ) ;
       con.o1.setText(String.valueOf(filter3(chord)) + outlngth) ;
       con.o3.setText(String.valueOf(filter3(span)) + outlngth) ;
       con.o6.setText(String.valueOf(filter3(area)) + outarea) ;
 
       if(foiltype == 1) con.type.setText("Airfoil") ;
       if(foiltype == 2) con.type.setText("Ellipse") ;
       if(foiltype == 3) con.type.setText("Plate") ;
//        con.o4.setText(String.valueOf(filter3(clift))) ;
       computeFlow() ;
       return ;
  }

  public void loadOut() {   // output routine
     String outfor,outden,outpres,outarea,outvel ;

     outvel = " mph" ;
     if (lunits == 1) outvel = " km/hr" ;
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

        con.o13.setText(String.valueOf(filter3(clift))) ;

        if (lftout == 0) {
          lift = clift * q0 * area / lconv / lconv ; /* lift in lbs */
          lift = lift * fconv ;
          if (Math.abs(lift) <= 10.0) {
             con.o10.setText(String.valueOf(filter3(lift)) + outfor) ;
          }
          if (Math.abs(lift) > 10.0) {
             con.o10.setText(String.valueOf(filter0(lift)) + outfor) ;
          }
        }
     }
 
     switch (lunits)  {
       case 0: {                             /* English */
           con.o9.setText(String.valueOf(filter5(rho))+outden) ;
           con.o7.setText(String.valueOf(filter3(q0)) + outpres) ;
           con.o8.setText(String.valueOf(filter3(pt0)) + outpres) ;
           con.o11.setText(String.valueOf(filter3(vfsd)) + outvel) ;
           con.o14.setText(String.valueOf(filter3(ps0)) + outpres) ;
           break;
        }
        case 1: {                             /* Metric */
           con.o9.setText(String.valueOf(filter3(rho*515.4))+outden) ;
           con.o7.setText(String.valueOf(filter3(q0*.04787)) + outpres) ;
           con.o8.setText(String.valueOf(filter3(pt0*.04787)) + outpres) ;
           con.o11.setText(String.valueOf(filter3(vfsd)) + outvel) ;
           con.o14.setText(String.valueOf(filter3(ps0*.04787)) + outpres) ;
           break ;
        }
     }

     return ;
  }

  public void reorder() {
     int item,i,nplp;
     double tempx,tempy;

     nplp = numpt[tstflag] ;
     for (item=1; item<=nplp-1; ++item) {
         for (i=item+1; i<=nplp; ++i) {
           if (plt2x[tstflag][i] < plt2x[tstflag][item]) {
              tempx = plt2x[tstflag][item];
              tempy = plt2y[tstflag][item];
              plt2x[tstflag][item] = plt2x[tstflag][i];
              plt2y[tstflag][item] = plt2y[tstflag][i];
              plt2x[tstflag][i] = tempx;
              plt2y[tstflag][i] = tempy;
           }
         }
     }
     return;
  }


  class Solver {
 
     Solver () {
     }

     public void setDefaults() {
        int i ;

        tstflag = 0 ;
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
        dispp = 2 ;
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
        ppconv = 1.0;
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
        vcon2 = 1.0 ;

        planet = 3 ;
        psin = 2116 ;
        psmin = 100 ;
        psmax = 5000. ;
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
//	  chrdfac = Math.sqrt(Math.sqrt(chord));  Adds a chord factor to scale the foil according to the chord
	  chrdfac = Math.sqrt(chord);  //Adds a chord factor to scale the foil according to the chord
	  fact = 32.0*chrdfac;  //chord is the only factor for the fact variable
        spanfac = (int)(4.0*50.0*.3535) ;
        xt1 = xt + spanfac ;
        yt1 = yt - spanfac ;
        xt2 = xt - spanfac;
        yt2 = yt + spanfac ;
        plthg[1] = 0.0 ;
        for (i=1; i<=20; ++i) {
          plsav[i] = 0 ;
        }
        begy = 0.0 ;
        endy = 10.0 ;
 
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
  } // end Solver

  class Con extends Panel {
        Process outerparent ;
        TextField o1,o2,o3,o4,o5,o6,o7,o8,o9,o10 ;
        TextField o11,o12,o13,o14,o15,o16,o17,o18,o19,o20 ;
        Label l1,l2,l3,l4,l5,l6,l7,l8,l9,l10 ;
        Label l11,l12,l13,l14,l15,l16,l17,l18,l19,l20 ;
        Label lmod1,lmod2,lab1,lab2 ;
        TextField mod,type,pnt;
        Button binc,bdec ;
        Choice disch;
  
        Con (Process target) {
          outerparent = target ;
          setLayout(new GridLayout(10,4,5,5)) ;
  
          lab1 = new Label("Geometry:", Label.RIGHT) ;
          lab1.setForeground(Color.red) ;

          lab2 = new Label("Diagnostics:", Label.RIGHT) ;
          lab2.setForeground(Color.red) ;

          lmod1 = new Label("Select Data Point ", Label.LEFT) ;
          lmod1.setForeground(Color.blue) ;

          lmod2 = new Label("Display", Label.RIGHT) ;
          lmod2.setForeground(Color.blue) ;

          disch = new Choice() ;
          disch.addItem("Graph") ;
          disch.addItem("Movie");
          disch.setBackground(Color.white) ;
          disch.setForeground(Color.blue) ;
          disch.select(0);

          pnt = new TextField("1",5) ;
          pnt.setBackground(Color.white) ;
          pnt.setForeground(Color.black) ;

          binc = new Button(" + Increase") ;
          binc.setBackground(Color.blue) ;
          binc.setForeground(Color.white) ;
 
          bdec = new Button(" - Decrease") ;
          bdec.setBackground(Color.blue) ;
          bdec.setForeground(Color.white) ;
 
          mod = new TextField("1",5) ;
          mod.setBackground(Color.black) ;
          mod.setForeground(Color.yellow) ;
 
          type = new TextField("1",5) ;
          type.setBackground(Color.black) ;
          type.setForeground(Color.yellow) ;
 
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
  
          l10 = new Label("Lift (L) ", Label.CENTER) ;
          o10 = new TextField("0.0",5) ;
          o10.setBackground(Color.black) ;
          o10.setForeground(Color.green) ;

          l11 = new Label("Speed ", Label.CENTER) ;
          o11 = new TextField("0.0",5) ;
          o11.setBackground(Color.black) ;
          o11.setForeground(Color.yellow) ;

          l12 = new Label("Angle of Attack ", Label.CENTER) ;
          o12 = new TextField("0.0",5) ;
          o12.setBackground(Color.black) ;
          o12.setForeground(Color.yellow) ;

          l13 = new Label("Coefficient (Cl)", Label.CENTER) ;
          o13 = new TextField("0.0",5) ;
          o13.setBackground(Color.black) ;
          o13.setForeground(Color.green) ;

          l14 = new Label("Static Pressure ", Label.CENTER) ;
          o14 = new TextField("0.0",5) ;
          o14.setBackground(Color.black) ;
          o14.setForeground(Color.yellow) ;

          add(lab2);
          add(lmod1) ;
          add(lmod2);
          add(disch);

          add(bdec);
          add(pnt);
          add(binc);
          add(new Label("", Label.CENTER));

          add(l11) ;
          add(o11) ;
          add(l12) ;
          add(o12) ;

          add(l14) ;
          add(o14) ;
          add(l9) ;
          add(o9) ;

          add(l8) ;
          add(o8) ;
          add(l7) ;
          add(o7) ;

          add(l10) ;
          add(o10) ;
          add(l13) ;
          add(o13) ;

          add(lab1);
          add(new Label("Model #", Label.RIGHT));
          add(mod) ;
          add(type);

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
          if(evt.target instanceof Choice) {
             this.handleCho(evt,arg) ;
             return true ;
          }
          if(evt.target instanceof TextField) {
             this.handleBut(evt,arg) ;
             return true ;
          }
          if(evt.target instanceof Button) {
             this.handleBut(evt,arg) ;
             return true ;
          }
          else return false ;
        } //  handler

        public void handleCho(Event evt, Object arg) {
           String label = (String)arg ;
           if(label.equals("Graph")) layout.show(out, "first") ;
           if(label.equals("Movie")) layout.show(out, "second") ;
           loadInput () ;
        } // handle choice
   
        public void handleBut(Event evt, Object arg) {
           String label = (String)arg ;
           String outpres,outvel ;
           Double V1 ;
           double v1;
           int i1 ;

           outpres = " lbs/ft^2" ;
           if (lunits == 1) outpres = " kPa" ;
           outvel = " mph" ;
           if (lunits == 1) outvel = " km/hr" ;

           V1 = Double.valueOf(pnt.getText()) ;
           v1 = V1.doubleValue() ;
           i1 = (int) v1 ;

           if(label.equals(" + Increase")) {
              i1 = i1 + 1 ;

              if(i1 >= numpt[tstflag]) i1 = numpt[tstflag] ; 
              pnt.setText(String.valueOf(i1)) ;
           }

           if(label.equals(" - Decrease")) {
              i1 = i1 - 1 ;

              if(i1 <= 1) i1 = 1 ; 
              pnt.setText(String.valueOf(i1)) ;
           }

           if (tstflag >= 1) {

                  vfsd = spd[tstflag][i1] * vcon2  ;
                  alfval = ang[tstflag][i1] ;
                  psin = pin[tstflag][i1] * piconv ;
                  o11.setText(String.valueOf(filter3(vcon2*spd[tstflag][i1])) + outvel) ;
                  o12.setText(String.valueOf(filter3(ang[tstflag][i1]))) ;
                  o14.setText(String.valueOf(filter3(piconv*pin[tstflag][i1])) + outpres) ;
   
               loadInput();
           }
       } // end handler
  } // Con

  class In extends Panel {
     Process outerparent ;
     Flt flt ;
     Cyl cyl ;
     Filep filep ;

     In (Process target) { 
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
        Process outerparent ;
        Upr upr ;
        Lwr lwr ;

        Flt (Process target) {

           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           upr = new Upr(outerparent) ;
           lwr = new Lwr(outerparent) ;

           add(upr) ;
           add(lwr) ;
        }

        class Upr extends Panel {
           Process outerparent ;
           Inl inl ;
           Inr inr ;

           Upr (Process target) {

              outerparent = target ;
              setLayout(new GridLayout(1,2,5,5)) ;

              inl = new Inl(outerparent) ;
              inr = new Inr(outerparent) ;

              add(inl) ;
              add(inr) ;
           }

           class Inl extends Panel {
              Process outerparent ;
              TextField f1,f2,f3,f4 ;
              Label l1,l2,l3,l4,l5 ;
              Button brbt,bto,bprt,bprto;
              Button btload;
              TextField mod;
              FileDialog fdd;
        
              Inl (Process target) {
    
               outerparent = target ;
               setLayout(new GridLayout(6,2,2,5)) ;

               fdd = new FileDialog(f) ;

               brbt = new Button("Data File") ;
               brbt.setBackground(Color.blue) ;
               brbt.setForeground(Color.white) ;

               bto = new Button("Open File") ;
               bto.setBackground(Color.blue) ;
               bto.setForeground(Color.white) ;

               l1 = new Label("Select Test#:", Label.RIGHT) ;
               l1.setForeground(Color.blue) ;

               l2 = new Label("Type of Test:", Label.RIGHT) ;
               l2.setForeground(Color.blue) ;

               mod = new TextField("1",5) ;
               mod.setBackground(Color.white) ;
               mod.setForeground(Color.blue) ;
 
               btload = new Button("Open Test") ;
               btload.setBackground(Color.blue) ;
               btload.setForeground(Color.white) ;

               bprt = new Button("Print File") ;
               bprt.setBackground(Color.blue) ;
               bprt.setForeground(Color.white) ;

               bprto = new Button("Open Print") ;
               bprto.setBackground(Color.blue) ;
               bprto.setForeground(Color.white) ;

               add(brbt) ;
               add(bto) ;

               add(l1);
               add(mod);

               add(new Label("", Label.CENTER));
               add(btload) ;

               add(new Label("", Label.CENTER));
               add(l2);

               add(new Label("", Label.CENTER));
               add(new Label("", Label.CENTER));

               add(bprt) ;
               add(bprto) ;
             }
   
             public boolean action(Event evt, Object arg) {
               if(evt.target instanceof Button) {
                  this.handleBut(evt,arg) ;
                  return true ;
               }
               else return false ;
             } //  handler

            public void handleBut(Event evt, Object arg) {
              String label = (String)arg ;
              String ddir,dfil,filnam ;
              String labmod,labrd,labnum,labv,laba,labp ;
              String outvel,outpres ;
              Double V1 ;
              double v1;
              double buf[][]  = new double[10][801] ; 
              double lftr[][]  = new double[21][41] ; 
              int ibuf[][]    = new int[7][801] ; 
              int i1,i2,ikeep,icount ;
              int i,j,k ;

              labmod = "  tests" ;
              labrd = "File Read -" ;
              labnum = " data points for this test" ;
              labv = " Vary Speed" ;
              laba = " Vary Angle of Attack" ;
              labp = " Vary Pressure" ;
              ikeep = 1;
              if(label.equals("Data File")) {
                 bto.setBackground(Color.blue) ;
                 bto.setForeground(Color.white) ;
                 fdd.show() ;
                 ddir = fdd.getDirectory() ;
                 dfil = fdd.getFile();
                 inr.modd.setText(ddir + dfil) ;
              }
              if(label.equals("Open File")) {
                 for (i=1; i<=20; ++i) {
                   plsav[i] = 0 ;
                 }
                 tstflag = 0 ;
                 bto.setBackground(Color.yellow) ;
                 bto.setForeground(Color.black) ;
                 btload.setBackground(Color.blue) ;
                 btload.setForeground(Color.white) ;
                 filnam = inr.modd.getText() ;
                 try{
                  dfile = new FileInputStream(filnam) ;
                  datin = new DataInputStream(dfile) ;
    //  read in data              
                  for (i=1; i<= 800; ++i) {
                     ikeep = i - 1 ;
                     for (j=1; j<= 9; ++j) {
                        buf[j][i] = datin.readDouble() ;
                     }
                     for (j=1; j<= 6; ++j) {
                        ibuf[j][i] = datin.readInt();
                     }
                  }
                } catch (IOException n) {
    // find number of tests and number of points in each test              
                  numtest = 1 ;
                  for (i=1; i<= ikeep; ++i) {
                     if (ibuf[1][i] > numtest) {
                        numpt[numtest] = ibuf[2][i-1] + 1 ;
                        numtest = numtest + 1 ;
                     }
                  }
                  numpt[numtest] = ibuf[2][ikeep] + 1 ;

                  icount = 0;
                  for (i=1; i<= numtest; ++i) {
                     for (j=1; j<= numpt[i]; ++j) {
                        icount = icount + 1 ;
                        dcam[i] = buf[1][icount] ;
                        dthk[i] = buf[2][icount] ;
                        dchrd[i] = buf[3][icount] ;
                        dspan[i] = buf[4][icount] ;
                        qdt[i][j] = buf[5][icount] ;
                        dlft[i][j] = buf[6][icount] ;
                        dspd[i][j] = buf[7][icount] ;
                        ang[i][j] = buf[8][icount] ;
                        dpin[i][j] = buf[9][icount] ;
                        modlnm[i] = ibuf[3][icount] ;
     		        dlunits[i] = ibuf[4][icount] ;
     		        testp[i] = ibuf[5][icount];
                        dftp[i] = ibuf[6][icount] ;
                     }
                  }
                  inr.modd.setText(labrd + String.valueOf(numtest) + labmod) ;
                }
              }

              if(label.equals("Open Test")) {
                 btload.setBackground(Color.yellow) ;
                 btload.setForeground(Color.blue) ;

                 V1 = Double.valueOf(mod.getText()) ;
                 v1 = V1.doubleValue() ;
                 i2 = (int) v1 ;

                 tstflag = i2;
                 if(i2 >= numtest) {
                   i2 = numtest ;
                   mod.setText(String.valueOf(i2)) ;
                 }

                i1 = modlnm[i2] ;
                con.pnt.setText("1") ;
                con.mod.setText(String.valueOf(i1)) ;
                foiltype = dftp[i2] ;
                lunits = dlunits[i2] ;
 	        setUnits();
	        if (lunits == 0)  in.flt.lwr.untch.select(0);
	        if (lunits == 1)  in.flt.lwr.untch.select(1);
                outpres = " lbs/ft^2" ;
                if (lunits == 1) outpres = " kPa" ;
                outvel = " mph" ;
                if (lunits == 1) outvel = " km/hr" ;

                camval = dcam[i2] ;
                thkval = dthk[i2] ;
                span = dspan[i2] ;
  	        chord = dchrd[i2] ;
                inr.modt.setText(String.valueOf(numpt[i2]) + labnum) ;
                if(testp[i2] == 1) {
                    inr.ttest.setText(labv) ;
                    dispp = 5 ;
                    dout = 0 ;
                    in.flt.lwr.pl2.setBackground(Color.blue);
                    in.flt.lwr.pl2.setForeground(Color.white);
                    in.flt.lwr.pl3.setBackground(Color.blue);
                    in.flt.lwr.pl3.setForeground(Color.white);
                    in.flt.lwr.pl4.setBackground(Color.yellow);
                    in.flt.lwr.pl4.setForeground(Color.black);
                    in.flt.lwr.pl5.setBackground(Color.blue);
                    in.flt.lwr.pl5.setForeground(Color.white);
                    in.flt.lwr.pl6.setBackground(Color.blue);
                    in.flt.lwr.pl6.setForeground(Color.white);
                    in.flt.lwr.pl7.setBackground(Color.blue);
                    in.flt.lwr.pl7.setForeground(Color.white);
                }
                if(testp[i2] == 2) {
                    inr.ttest.setText(laba) ;
                    dispp = 2 ;
                    dout = 0 ;
                    in.flt.lwr.pl2.setBackground(Color.blue);
                    in.flt.lwr.pl2.setForeground(Color.white);
                    in.flt.lwr.pl3.setBackground(Color.yellow);
                    in.flt.lwr.pl3.setForeground(Color.black);
                    in.flt.lwr.pl4.setBackground(Color.blue);
                    in.flt.lwr.pl4.setForeground(Color.white);
                    in.flt.lwr.pl5.setBackground(Color.blue);
                    in.flt.lwr.pl5.setForeground(Color.white);
                    in.flt.lwr.pl6.setBackground(Color.blue);
                    in.flt.lwr.pl6.setForeground(Color.white);
                    in.flt.lwr.pl7.setBackground(Color.blue);
                    in.flt.lwr.pl7.setForeground(Color.white);
                }
                if(testp[i2] == 3) {
                    inr.ttest.setText(labp) ;
                    dispp = 9 ;
                    dout = 0 ;
                    in.flt.lwr.pl2.setBackground(Color.blue);
                    in.flt.lwr.pl2.setForeground(Color.white);
                    in.flt.lwr.pl3.setBackground(Color.blue);
                    in.flt.lwr.pl3.setForeground(Color.white);
                    in.flt.lwr.pl4.setBackground(Color.blue);
                    in.flt.lwr.pl4.setForeground(Color.white);
                    in.flt.lwr.pl5.setBackground(Color.yellow);
                    in.flt.lwr.pl5.setForeground(Color.black);
                    in.flt.lwr.pl6.setBackground(Color.blue);
                    in.flt.lwr.pl6.setForeground(Color.white);
                    in.flt.lwr.pl7.setBackground(Color.blue);
                    in.flt.lwr.pl7.setForeground(Color.white);
                }
	        chrdfac = Math.sqrt(chord/lconv);	   
	        fact = 32.0*chrdfac;
//		   con.o2.setText(String.valueOf(chord));

                for (j=1; j<= numpt[tstflag]; ++j) {
                    lft[tstflag][j] = dlft[tstflag][j] / fconv ;
                    spd[tstflag][j] = dspd[tstflag][j] / vcon2 ;
                    pin[tstflag][j] = dpin[tstflag][j] / piconv ;
                }

                vfsd = vcon2*spd[tstflag][1] ;
                alfval = ang[tstflag][1] ;
                psin = piconv*pin[tstflag][1] ;

                con.o11.setText(String.valueOf(filter3(vcon2*spd[tstflag][1])) + outvel) ;
                con.o12.setText(String.valueOf(filter3(ang[tstflag][1]))) ;
                con.o14.setText(String.valueOf(filter3(piconv*pin[tstflag][1])) + outpres) ;

	        loadInput();
              }
              if(label.equals("Print File")) {
                 bprto.setBackground(Color.blue) ;
                 bprto.setForeground(Color.white) ;
                 fdd.show() ;
                 ddir = fdd.getDirectory() ;
                 dfil = fdd.getFile();
                 inr.modp.setText(ddir + dfil) ;
                 datp = 1;
              }
              if(label.equals("Open Print")) {
                 bprto.setBackground(Color.yellow) ;
                 bprto.setForeground(Color.black) ;
                 filnam = inr.modp.getText() ;
                 try{
                  pfile = new FileOutputStream(filnam) ;
                  prnt = new PrintStream(pfile) ;

                  prnt.println("  ");
                  prnt.println(" Post-Processing Application Version 1.0h - July 09 ");
                  prnt.println("  ");
                  
                } catch (IOException n) {
                }
              }
            }  // end handler
          }  // Inl

           class Inr extends Panel {
              Process outerparent ;
              TextField modt,modd,modp,ttest ;
   
              Inr (Process target) {
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,2,5)) ;

               modd = new TextField("<-Select Data File ") ;
               modd.setBackground(Color.white) ;
               modd.setForeground(Color.blue) ;

               modt = new TextField("Number of Test Points ") ;
               modt.setBackground(Color.white) ;
               modt.setForeground(Color.blue) ;

               ttest = new TextField("Test Type ") ;
               ttest.setBackground(Color.white) ;
               ttest.setForeground(Color.blue) ;

               modp = new TextField("Print File ") ;
               modp.setBackground(Color.white) ;
               modp.setForeground(Color.blue) ;

               add(modd) ;
               add(new Label("", Label.CENTER));
               add(modt) ;
               add(ttest);
               add(new Label("", Label.CENTER));
               add(modp);
             }
   
           }  // Inr 
        }  // Upr 

        class Lwr extends Panel {
           Process outerparent ;
           Button bt3,endit;
           Button pl1,pl2,pl3,pl4,pl5,pl6,pl7,pl8 ;
           Choice untch,plout;
           TextField d1,d2,d3,d4 ;
           Label l1 ;
        
           Lwr (Process target) {
    
            outerparent = target ;
            setLayout(new GridLayout(5,3,2,5)) ;

            l1 = new Label("Plot Selection", Label.CENTER) ;
            l1.setForeground(Color.red) ;

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

            pl2 = new Button("Cl vs Angle") ;
            pl2.setBackground(Color.blue) ;
            pl2.setForeground(Color.white) ;

            pl3 = new Button("L vs Angle") ;
            pl3.setBackground(Color.blue) ;
            pl3.setForeground(Color.white) ;

            pl4 = new Button("L vs Speed") ;
            pl4.setBackground(Color.blue) ;
            pl4.setForeground(Color.white) ;

            pl5 = new Button("L vs Pressure") ;
            pl5.setBackground(Color.blue) ;
            pl5.setForeground(Color.white) ;

            pl6 = new Button("Cl vs Speed") ;
            pl6.setBackground(Color.blue) ;
            pl6.setForeground(Color.white) ;

            pl7 = new Button("Cl vs Pressure") ;
            pl7.setBackground(Color.blue) ;
            pl7.setForeground(Color.white) ;

            d1 = new TextField("0.0",5) ;
            d2 = new TextField("0.0",5) ;
            d3 = new TextField("0.0",5) ;

            add(new Label("", Label.CENTER));
            add(l1);
            add(new Label("", Label.CENTER));

            add(pl3) ;
            add(pl5) ;
            add(pl4) ;

            add(pl2) ;
            add(pl7) ;
            add(pl6) ;
/*
            add(d1);
            add(d2);
            add(d3);
*/
            add(new Label("", Label.CENTER));
            add(new Label("", Label.CENTER));
            add(new Label("", Label.CENTER));

            add(endit) ;
            add(bt3) ;
            add(untch) ;
          }

          public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Choice) {
               this.handleCho(evt,arg) ;
               return true ;
            }
            if(evt.target instanceof Button) {
               this.handleRefs(evt,arg) ;
               return true ;
            }
            else return false ;
          } //  handler

          public void handleCho(Event evt, Object arg) {
              String label = (String)arg ;
              lunits  = untch.getSelectedIndex() ;
              setUnits () ;
              loadInput () ;

              computeFlow() ;
          } // handle choice
   
          public void handleRefs(Event evt, Object arg) {
            String label = (String)arg ;
            Double V1 ;
            double v1;
            int i1,i ;

            if(label.equals("Exit")) {
               f.dispose() ;
               System.exit(1) ;
            }
            if(label.equals("Reset")) {
               in.flt.upr.inl.bto.setBackground(Color.blue) ;
               in.flt.upr.inl.bto.setForeground(Color.white) ;
               solve.setDefaults() ;
               layin.show(in, "first")  ;
               untch.select(lunits) ;
                   // **** the lunits check MUST come first
               setUnits () ;
               outopt = 0 ;
       
               loadInput() ;
            }
            if(label.equals("Cl vs Angle")) {
               dispp = 2 ;
               dout = 1 ;
               pl2.setBackground(Color.yellow);
               pl2.setForeground(Color.black);
               pl3.setBackground(Color.blue);
               pl3.setForeground(Color.white);
               pl4.setBackground(Color.blue);
               pl4.setForeground(Color.white);
               pl5.setBackground(Color.blue);
               pl5.setForeground(Color.white);
               pl6.setBackground(Color.blue);
               pl6.setForeground(Color.white);
               pl7.setBackground(Color.blue);
               pl7.setForeground(Color.white);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            } 
            if(label.equals("L vs Angle")) {
               dispp = 2 ;
               dout = 0 ;
               pl2.setBackground(Color.blue);
               pl2.setForeground(Color.white);
               pl3.setBackground(Color.yellow);
               pl3.setForeground(Color.black);
               pl4.setBackground(Color.blue);
               pl4.setForeground(Color.white);
               pl5.setBackground(Color.blue);
               pl5.setForeground(Color.white);
               pl6.setBackground(Color.blue);
               pl6.setForeground(Color.white);
               pl7.setBackground(Color.blue);
               pl7.setForeground(Color.white);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            } 
            if(label.equals("L vs Speed")) {
               dispp = 5 ;
               dout = 0 ;
               pl2.setBackground(Color.blue);
               pl2.setForeground(Color.white);
               pl3.setBackground(Color.blue);
               pl3.setForeground(Color.white);
               pl4.setBackground(Color.yellow);
               pl4.setForeground(Color.black);
               pl5.setBackground(Color.blue);
               pl5.setForeground(Color.white);
               pl6.setBackground(Color.blue);
               pl6.setForeground(Color.white);
               pl7.setBackground(Color.blue);
               pl7.setForeground(Color.white);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            }
            if(label.equals("L vs Pressure")) {
               dispp = 9 ;
               dout = 0 ;
               pl2.setBackground(Color.blue);
               pl2.setForeground(Color.white);
               pl3.setBackground(Color.blue);
               pl3.setForeground(Color.white);
               pl4.setBackground(Color.blue);
               pl4.setForeground(Color.white);
               pl5.setBackground(Color.yellow);
               pl5.setForeground(Color.black);
               pl6.setBackground(Color.blue);
               pl6.setForeground(Color.white);
               pl7.setBackground(Color.blue);
               pl7.setForeground(Color.white);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            }
            if(label.equals("Cl vs Speed")) {
               dispp = 5 ;
               dout = 1 ;
               pl2.setBackground(Color.blue);
               pl2.setForeground(Color.white);
               pl3.setBackground(Color.blue);
               pl3.setForeground(Color.white);
               pl4.setBackground(Color.blue);
               pl4.setForeground(Color.white);
               pl5.setBackground(Color.blue);
               pl5.setForeground(Color.white);
               pl6.setBackground(Color.yellow);
               pl6.setForeground(Color.black);
               pl7.setBackground(Color.blue);
               pl7.setForeground(Color.white);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            }
            if(label.equals("Cl vs Pressure")) {
               dispp = 9 ;
               dout = 1 ;
               pl2.setBackground(Color.blue);
               pl2.setForeground(Color.white);
               pl3.setBackground(Color.blue);
               pl3.setForeground(Color.white);
               pl4.setBackground(Color.blue);
               pl4.setForeground(Color.white);
               pl5.setBackground(Color.blue);
               pl5.setForeground(Color.white);
               pl6.setBackground(Color.blue);
               pl6.setForeground(Color.white);
               pl7.setBackground(Color.yellow);
               pl7.setForeground(Color.black);
               for (i=1; i<=20; ++i) {
                 plsav[i] = 0 ;
               }
               loadInput() ;
            }
          }
       }  // Lwr
     }  // Flt 

     class Cyl extends Panel {
        Process outerparent ;
        Inl inl ;
        Inr inr ;

        Cyl (Process target) {

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
           Process outerparent ;
           TextField f1,f2,f3 ;
           Label l1,l2,l3 ;
           Label l01,l02 ;
     
           Inl (Process target) {
     
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
           Process outerparent ;
           Scrollbar s1,s2;
           Choice shapch ;

           Inr (Process target) {
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

             outopt = 0 ;
             dispp = 0 ;
             calcrange = 0 ;

             loadInput() ;
           } // handler
        }  // Inr
     }  // Cyl 

     class Filep extends Panel {  // print file
        Process outerparent ;
        TextField namprnt,namlab ;
        Button pbopen,cancel ;

        Filep (Process target) {

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
              prnt.println(" Process -  Post-Process Version 1.0h - July 09 ");
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
     Process outerparent ;
     Viewer view ;
     Plt2 plt2 ;

     Out (Process target) {
        outerparent = target ;
        layout = new CardLayout() ;
        setLayout(layout) ;

        view = new Viewer(outerparent) ;
        plt2 = new Plt2(outerparent) ;

        add ("first", plt2) ;
        add ("second", view) ;
     }

     class Plt2 extends Canvas
         implements Runnable{
        Process outerparent ;
        Thread run3 ;
        Point locp,ancp;

        Plt2 (Process target) { 
           setBackground(Color.blue) ;
           run3 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
            if (y >= 300) { 

/*
               if (x >= 5 && x <= 55) { // rescale
                   endy = 0.0 ;
                   begy = 0.0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
               if (x >= 82 && x <= 232) { 
                   dispp = 0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
               if (x >= 240 && x <= 390) {
                   dispp = 1 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
*/
            }
            plt2.repaint() ;
        }

        public void start() {
           if (run3 == null) {
              run3 = new Thread(this) ;
              run3.start() ;
           }
        }

        public void run() {
          int timer ;
 
          timer = 100 ;
          while (true) {
             try { Thread.sleep(timer); }
             catch (InterruptedException e) {}
             plt2.repaint() ;
          }
        }

        public void update(Graphics g) {
           plt2.paint(g) ;
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
           int pltdata ;
           Color col ;
  
           pltdata = 0 ;
           if (dispp <= 1) {
              off3Gg.setColor(Color.black) ;
              off3Gg.fillRect(0,0,500,500) ;
/*
              off3Gg.setColor(Color.white) ;
              off3Gg.fillRect(2,302,70,15) ;
              off3Gg.setColor(Color.red) ;
              off3Gg.drawString("Rescale",8,315) ;
*/
              off3Gg.setColor(Color.lightGray) ;
              off3Gg.fillRect(0,295,500,50) ;
              off3Gg.setColor(Color.white) ;
              if (dispp == 0) off2Gg.setColor(Color.yellow) ;
              off3Gg.fillRect(82,302,150,20) ;
              off3Gg.setColor(Color.black) ;
              off3Gg.drawString("Surface Pressure",88,317) ;

              off3Gg.setColor(Color.white) ;
              if (dispp == 1) off3Gg.setColor(Color.yellow) ;
              off3Gg.fillRect(240,302,150,20) ;
              off3Gg.setColor(Color.black) ;
              off3Gg.drawString("Velocity",288,317) ;
           }
           if (dispp > 1 && dispp <= 15) {
              off3Gg.setColor(Color.blue) ;
              off3Gg.fillRect(0,0,500,500) ;
/*
              off3Gg.setColor(Color.white) ;
              off3Gg.fillRect(2,302,70,15) ;
              off3Gg.setColor(Color.red) ;
              off3Gg.drawString("Rescale",8,315) ;
*/
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
              off3Gg.setColor(Color.white) ;
              exes[0] = (int) (factp* 0.0) + xtp ;
              whys[0] = (int) (factp* -4.5) + ytp ;
              exes[1] = (int) (factp* 0.0) + xtp ;
              whys[1] = (int) (factp* 0.0) + ytp ;
              exes[2] = (int) (factp* 6.0) + xtp ;
              whys[2] = (int) (factp* 0.0) + ytp ;
              off3Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              off3Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
 
              xlabel = (int) (-90.0) + xtp ;   /*  label y axis */
              ylabel = (int) (factp*-1.5) + ytp ;
              off3Gg.drawString(laby,xlabel,ylabel) ;
              off3Gg.drawString(labyu,xlabel,ylabel+10) ;
                                                    /* add tick values */
              for (ind= 1; ind<= ntiky; ++ind){
                   xlabel = (int) (-50.0) + xtp ;
                   yl = begy + (ind-1) * incy ;
                   ylabel = (int) (factp* -scaley*(yl + offy)) + ytp ;
                   if (nord >= 2) {
                      off3Gg.drawString(String.valueOf((int) yl),xlabel,ylabel) ;
                   }
                   else {
                      off3Gg.drawString(String.valueOf(filter3(yl)),xlabel,ylabel);
                   }
              }
              xlabel = (int) (factp*3.0) + xtp ;    /* label x axis */
              ylabel = (int) (40.0) + ytp ;
              off3Gg.drawString(labx,xlabel,ylabel-10) ;
              off3Gg.drawString(labxu,xlabel,ylabel) ;
                                                   /* add tick values */
              for (ind= 1; ind<= ntikx; ++ind){
                   ylabel = (int) (15.) + ytp ;
                   xl = begx + (ind-1) * incx ;
                   xlabel = (int) (factp*(scalex*(xl + offx) -.05)) + xtp ;
                   if (nabs == 1) {
                      off3Gg.drawString(String.valueOf(xl),xlabel,ylabel) ;
                   }
                   if (nabs > 1) {
                      off3Gg.drawString(String.valueOf((int) xl),xlabel,ylabel) ;
                   }
              }

    // put a header
              exes[0] = 10 ;
              whys[0] = 10 ;
              exes[1] = 50 ;
              whys[1] = 10 ;
              off3Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              off3Gg.drawString("Theory",60,15) ;
       
              off3Gg.drawString("*",150,20) ;
              off3Gg.drawString("Data",160,15) ;

              off3Gg.setColor(Color.red) ;
              off3Gg.fillOval(220,7,5,5) ;
              off3Gg.setColor(Color.white) ;
              off3Gg.drawString("Diagnostic Point",230,15) ;

     // draw plot
              if(lines == 0) {
                 for (i=1; i<=npt; ++i) {
                     xlabel = (int) (factp*scalex*(offx+pltx[0][i])) + xtp ;
                     ylabel = (int)(factp*-scaley*(offy+plty[0][i]) +7.)+ytp;
                     off3Gg.drawString("*",xlabel,ylabel) ;
                 }
              }
              else {
               if (dispp > 1) {
                 off3Gg.setColor(Color.white) ;
                 exes[1] = (int) (factp*scalex*(offx+pltx[0][1])) + xtp;
                 whys[1] = (int) (factp*-scaley*(offy+plty[0][1])) + ytp;
                 for (i=1; i<=npt; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (factp*scalex*(offx+pltx[0][i])) + xtp;
                   whys[1] = (int) (factp*-scaley*(offy+plty[0][i])) + ytp;
                   off3Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 }
               }
 // test data
               if (dispp == 9 && testp[tstflag] == 3) pltdata = 1 ;
               if (dispp == 2 && testp[tstflag] == 2) pltdata = 1 ;
               if (dispp == 5 && testp[tstflag] == 1) pltdata = 1 ;

               if (pltdata == 1) {
                 for (i=1; i<=numpt[tstflag]; ++i) {
                   xlabel = (int) (factp*scalex*(offx+plttx[1][i])) + xtp ;
                   ylabel = (int)(factp*-scaley*(offy+pltty[1][i]) +7.)+ytp;
                   off3Gg.drawString("*",xlabel,ylabel) ;
                 }
               }
               xlabel = (int) (factp*scalex*(offx+pltx[1][0])) + xtp ;
               ylabel = (int) (factp*-scaley*(offy+plty[1][0]))+ytp -4;
               off3Gg.setColor(Color.red) ;
               off3Gg.fillOval(xlabel,ylabel,5,5) ;
             }
          }

          g.drawImage(offImg3,0,0,this) ;   
       }
     }  // Plt2 

     class Viewer extends Canvas  
         implements Runnable{
        Process outerparent ;
        Thread runner ;
        Point locate,anchor;

        Viewer (Process target) {
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
         con.o1.setText(String.valueOf(xt)) ;
         con.o2.setText(String.valueOf(yt)) ;
         con.o3.setText(String.valueOf(sldloc)) ;
         con.o4.setText(String.valueOf(fact)) ;
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
  // middl   e airfoil geometry
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
 //      back foil
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
// leading and    trailing edge
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
   // pu   t some info on the geometry
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
  // p   ut some info on the geometry
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
  } // end Out 

  class Plt extends Canvas
         implements Runnable{
        Process outerparent ;
        Thread run2 ;
        Point locp,ancp;

        Plt (Process target) { 
           setBackground(Color.blue) ;
           run2 = null ;
        }
   
        public boolean mouseUp(Event evt, int x, int y) {
           handleb(x,y) ;
           return true;                                        
        }

        public void handleb(int x, int y) {
            if (y >= 2 && y <= 20) { // save plot
               if (x >= 350) { 
                  plsav[tstflag] = 1 ;
                  loadInput() ;
               }
               if (x >= 2 && x <= 80) { // rescale up
                  endy = 5.0 * endy ;
                  loadInput() ;
               }
               if (x >= 102 && x <= 180) { // rescale down 
                  endy = endy / 5.0 ;
                  loadInput() ;
               }
            }
/*
               if (x >= 82 && x <= 232) { 
                   dispp = 0 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
               if (x >= 240 && x <= 390) {
                   dispp = 1 ;
                   calcrange = 0 ;
                   computeFlow() ;
               }
*/
            plt.repaint() ;
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
             plt.repaint() ;
          }
        }

        public void loadPlot() { 
          double rad,xc,yc,lftref,clref ;
          double del,sped,awng,ppl,tpl,hpl,angl,thkpl,campl,clpl ;
          int index,ic ;

          lines = 1 ;
          clref =  getClplot(camval,thkval,alfval) ;
          if (Math.abs(clref) <= .001) clref = .001 ;    /* protection */
          lftref = clref * q0 * area/lconv/lconv ;
//  ******* attempt at constant re-scale   
/*
                   endy = 10.0 ;
                   begy = 0.0 ;
                   calcrange = 0 ;
*/
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
              ntiky = 6 ;
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
    //  test data
              for (ic=1; ic <= numpt[tstflag] ; ++ic) {
                  plttx[1][ic] = ang[tstflag][ic] ;
                  clpl = getClplot(camval,thkval,plttx[1][ic]) ;
                  if (dout == 0)pltty[1][ic] = fconv* lft[tstflag][ic] ;
  //                if (dout == 1)pltty[1][ic] = 100. * clpl ;
                  if (dout == 1) {
                     pltty[1][ic] = 100. * fconv * lft[tstflag][ic] / 
                           (area * qdt[tstflag][ic] * ppconv * lconv * lconv ) ;
                  }
                  plt2x[tstflag][ic] = plttx[1][ic] ;
                  plt2y[tstflag][ic] = pltty[1][ic] ;
              }
              reorder() ;
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
                  sped = (ic-1)*del ;
                  pltx[0][ic] = sped ;
                  if (dout == 0) plty[0][ic] = fconv*lftref * sped * sped / (vfsd * vfsd) ;
                  if (dout == 1) plty[0][ic] = 100.*clift ;
              }
              ntiky = 6 ;
              laby = String.valueOf("Lift");
              pltx[1][0] = vfsd ;
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
    //  test data
              for (ic=1; ic <= numpt[tstflag] ; ++ic) {
                  plttx[1][ic] = vcon2*spd[tstflag][ic] ;
                  if (dout == 0) pltty[1][ic] = fconv*lft[tstflag][ic] ;
                  if (dout == 1) {
                     pltty[1][ic] = 100. * fconv * lft[tstflag][ic] / 
                           (area * qdt[tstflag][ic] * ppconv * lconv * lconv ) ;
                  }
                  plt2x[tstflag][ic] = plttx[1][ic] ;
                  plt2y[tstflag][ic] = pltty[1][ic] ;
              }
              reorder() ;
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
          if (dispp == 9) {    // lift versus pressure
              npt = 2 ;
              ntr = 1 ;
              nabs = 7; nord = 3 ;
              ntikx=6;
              labx = String.valueOf("Pressure ");
              if (lunits == 0) {
                  labxu = String.valueOf("lb/sq ft");
                  begx = 100. ;
                  endx = 2500.0 ;
                  pltx[0][1] = begx ;
                  if (dout == 0) plty[0][1] = fconv*lftref * begx / ps0;
                  if (dout == 1) plty[0][1] = 100.*clift ;
                  pltx[0][2] = endx ;
                  if (dout == 0) plty[0][2] = fconv*lftref * endx / ps0;
                  if (dout == 1) plty[0][2] = 100.*clift ;
                  pltx[1][0] = psin ;
              }
              if (lunits == 1) {
                  labxu = String.valueOf("kPa");
                  begx = 100 * piconv ;
                  endx = 2500. * piconv ;
                  pltx[0][1] = begx ;
                  if (dout == 0) plty[0][1] = fconv*lftref * begx /(ps0 * piconv);
                  if (dout == 1) plty[0][1] = 100.*clift ;
                  pltx[0][2] = endx ;
                  if (dout == 0) plty[0][2] = fconv*lftref * endx /(ps0 * piconv);
                  if (dout == 1) plty[0][2] = 100.*clift ;
                  pltx[1][0] = ps0 * piconv ;
              }
              ntiky = 6 ;
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
    //  test data
              for (ic=1; ic <= numpt[tstflag] ; ++ic) {
                  plttx[1][ic] = piconv*pin[tstflag][ic] ;
                  if (dout == 0) pltty[1][ic] = fconv*lft[tstflag][ic] ;
                  if (dout == 1) {
                     pltty[1][ic] = 100. * fconv * lft[tstflag][ic] / 
                           (area * qdt[tstflag][ic] * ppconv * lconv * lconv ) ;
                  }
                  plt2x[tstflag][ic] = plttx[1][ic] ;
                  plt2y[tstflag][ic] = pltty[1][ic] ;
              }
              reorder() ;
          }
// determine y range - zero in the middle
/*
          if (dispp>= 2 && dispp < 6) { 
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
*/
// determine y range 
/*
          if (dispp >= 6 && dispp <= 8) { 
              if (plty[0][npt] >= plty[0][1]) {
                 begy = plty[0][1]  ;
                 endy = plty[0][npt]  ;
              }
              if (plty[0][npt] < plty[0][1]) {
                 begy = plty[0][npt]  ;
                  endy = plty[0][1]  ;
              }
          }
*/
// determine y range
/*
          if (dispp >= 0 && dispp <= 1) { 
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
          in.flt.lwr.d1.setText(String.valueOf(dispp)) ;
*/
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
           plt.paint(g) ;
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
           int pltdata ;
           Color col ;
  
           pltdata = 0 ;
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

              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(2,2,70,17) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("+ Rescale",8,15) ;

              off2Gg.setColor(Color.white) ;
              off2Gg.fillRect(102,2,70,17) ;
              off2Gg.setColor(Color.red) ;
              off2Gg.drawString("- Rescale",108,15) ;
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
     // draw plot
 // test data
               if (dispp == 9 && testp[tstflag] == 3) pltdata = 1 ;
               if (dispp == 2 && testp[tstflag] == 2) pltdata = 1 ;
               if (dispp == 5 && testp[tstflag] == 1) pltdata = 1 ;

               if (pltdata == 1) {
        //  new data
                 off2Gg.setColor(Color.white) ;
                 exes[1] = (int) (factp*scalex*(offx+plt2x[tstflag][1])) + xtp;
                 whys[1] = (int) (factp*-scaley*(offy+plt2y[tstflag][1])) + ytp;
                 for (i=2; i<=numpt[tstflag]; ++i) {
                   exes[0] = exes[1] ;
                   whys[0] = whys[1] ;
                   exes[1] = (int) (factp*scalex*(offx+plt2x[tstflag][i])) + xtp;
                   whys[1] = (int) (factp*-scaley*(offy+plt2y[tstflag][i])) + ytp;
                   off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 }
                 for (i=1; i<=numpt[tstflag]; ++i) {
                   xlabel = (int) (factp*scalex*(offx+plt2x[tstflag][i])) + xtp ;
                   ylabel = (int)(factp*-scaley*(offy+plt2y[tstflag][i]) +7.)+ytp;
                   off2Gg.drawString("*",xlabel,ylabel) ;
                 }
                 off2Gg.drawString(String.valueOf(tstflag),xlabel+10,ylabel) ;
      // old data
                 for (i=1; i<=20; ++i) {
                    if(plsav[i] == 1) {
                        off2Gg.setColor(Color.yellow) ;
                        exes[1] = (int) (factp*scalex*(offx+plt2x[i][1])) + xtp;
                        whys[1] = (int) (factp*-scaley*(offy+plt2y[i][1])) + ytp;
                        for (j=2; j<=numpt[i]; ++j) {
                          exes[0] = exes[1] ;
                          whys[0] = whys[1] ;
                          exes[1] = (int) (factp*scalex*(offx+plt2x[i][j])) + xtp;
                          whys[1] = (int) (factp*-scaley*(offy+plt2y[i][j])) + ytp;
                          off2Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                        }
                        for (j=1; j<=numpt[i]; ++j) {
                          xlabel = (int) (factp*scalex*(offx+plt2x[i][j])) + xtp ;
                          ylabel = (int)(factp*-scaley*(offy+plt2y[i][j]) +7.)+ytp;
                          off2Gg.drawString("*",xlabel,ylabel) ;
                        }
                        off2Gg.drawString(String.valueOf(i),xlabel+10,ylabel) ;
                    }
                 }

               }
               off2Gg.setColor(Color.white) ;
               off2Gg.fillRect(350,2,70,20) ;
               off2Gg.setColor(Color.red) ;
               off2Gg.drawString("Save",365,17) ;
          }

          g.drawImage(offImg2,0,0,this) ;   
       }
  }     // Plt 

 public static void main(String args[]) {
    Process foilst = new Process() ;

    f = new Frame("Process -  Post-Processor Version 1.0h") ;
    f.add("Center", foilst) ;
    f.resize(900, 700);
    f.show() ;

    foilst.init() ;
    foilst.start() ;
 }
}
