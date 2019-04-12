                                                                                                                                                                                                                                                                                                                                                                                /*
                      Interactive Nozzle Program
                    Application Version - stands alone
                                          reads and writes files

                        A Java Application
       to perform two dimensional analysis of supersonic flow
         through nozzles using the method of characteristics
       (derived from Supersonic Flows Version 1.6f - jmymoc)

                     Version 1.7c   -14 Aug 14
                (derived from Version 1.15c applet)

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
 
                 * add jet problem 
                 * add nozzle problem
                 *     get flow
                 *     add geometry
                 *     add pressure - temp - other flow variables
                 *     cleanup low PM angle integration
                 *     check/cleanup geometry to match a/a*
                 * add axisymmetric nozzle
                 *     add additional terms to equations
                 *     change x-y(r) to be non-dimensionalized by throat height
                 *     add geometry output to probs 7-8-9
                 * put in logic for delhi -- limit for nozzle
                 * Put only MOC nozzle design in this code ...
                 * change input to throat area ... derive nzht
                 * put all inputs on one panel
                 * make shock diamond analysis on centerline
                 *     add shock diamond analysis to end of nozzle analysis
                 * reflect drawing about centerline - with a button
                 * output thrust .. NPR ...
                 * add external cowl angle - changes p local through an expansion
                 *     include this logic/case for plug nozzle+
                 *     change the graphics to show external cowl angle
                 *     add external cowl geometry ..  ecxbgn ecybgn .. etc. 
                 *     add expansion fan details & graphics
                 *     make the flow and geometry definitions consistent .. 
                 *        "e" denotes external= exbgn   -  "i"  denotes internal = ixbgn
                 * change plumopt
                 *       0 = no plume
                 *       1 = plume into static
                 *       2 = plume into supersonic
                 * add external shock waves/expansions from the plume for plumopt = 2
                 * add color graphics for external flow
                 * clean up color graphics for plumopt = 2
                 *     add extra dimension to all the flow variables .. mach1, mach2, ptrat ..
                 *        1 = external 
                 *        2 = plume
                 *     include details in the expansion fan of the plume
                 *     modify plume calculation to include shocks for over-expansion 
                 *     fix bug in plume option
                   add Plug nozzle
                       internal/external expansion - Axisymmetric
                       external expansion - Axisymmetric
                 *     add flat cowl plug nozzle
                 *       complex MOC region near cowl
                 *       cleanup - plots 
                 *       cleanup - output
                 *       use consistent definitions of cowl exit and plug exit
                 *       add plume -
                 *         under expanded
                 *         over expanded
                 *     FIX a big bug in the variables which were reversed in V 1.7
                 *      .. mcx mcy are moc geometry .. wxbgn -wxnd are wall geometry 
                 *         output wxbgn, wybgn, wxnd, wynd
                 *     FIX a bug with indices for Q and R invarients .. makes prob 7 easier
                       internal/external expansion - 2D - CC-Lee design
                         check thrust calculation
                         clean-up details in the mesh output - get the correct grid
                            make throat the min area by adjusting cowl
                 *       add other variables to output plots .. mach and temp
                 *       fix plume graphic
                 *       calculate M exit
                 *       determine cowl surface
                 *       set up print box to handle both plug and cowl surfaces
                 *       make delx input also handle radius input
                 *     external expansion - 2D
                 *     add external plume to plug calculation 
                 *     correct thrust calculation for plug nozzle
                 *       add nozzle width to area calculation    
                 *     modify over-expanded calculation for plug
                 *       given the pressure ratio .. find the mach number and inclination/of the last ray
                 *       calculate additional isentropic compression based on surface slope 
                             - get exit mach   
                 *       find slope of compression wave ..  connect to the rest of plume calculation
                 *       modify xl1 yl1 a and b calculations 
                 *       fix up the pressure graphic for over=expanded 
                 *       add the initial compression wave   
                   add Rao nozzle
                 * add color graphics for pressure
                 *     put color bar on its own panel - to allow setting max and min
                 *     move output panel selection to Viewer
                 *     add point and axi analysis
                 *     add the plume
                 *     add other output variables - temp and mach 
                 *     create new variable for output and plotting 
                 *       - static pressure - static temperature (not ratios)
                 *     work on min-max for the new variables 
                 * clean up graphics for reflected flows 
                 * add afterbody drag 
                 * make smaller display
                 * fix up error in axi design code .. radians to degrees .. runge-kutta on angle
                 * add wedge (cone) nozzle - straight surface - prob = 7  
                 *    add external flow
                 *        plume
                 *          add coalescence model for compression waves
                 *          add color graphics
                          interaction
                 *    modify thrust calculation for divergence of flow for non-uniform (prob 7-8)
                 *    output average flow turning
                   
                                                     TJB 14 Aug 14
*/

import java.awt.*;
import java.lang.Math ;
import java.io.* ;

public class Moc extends java.applet.Applet {

   final double convdr = 3.14515926/180.;
   final double pi = 3.14515926;
   double gama, mach0, machdef, ang1, ang2, sepval, machpm ;
   double angmn, angmx, machlo, machhi, ang2mn, ang2mx, mlaslo, mlashi ;
   double xlong, xr1, xr1mn, xr1mx, yr1 ;
   double macl,nzht,nzhtlo,nzhthi,nzlg,nzlghi,nzlglo,nzar,nzarhi,nzarlo;
   double nprat,prathi,pratlo, pto,ptolo,ptohi,tto,ttolo,ttohi ;
   double nzw,nzwlo,nzwhi,mexlo,mexhi ;

   double delx, delxlo, delxhi, delxsav ;
   double radius,radlo,radhi,radsav ;
   double perint,perlo,perhi ;

   double angr, delmax, gamma, thmx ;
   double thet, thetmn, thetmx, delthet, dely ;
   double alt,altlo,althi,ps0,ts0,psth,tsth ;
   double xexit,yexit,xpexit,ypexit,exht,exthk,aboat ;
   double dragab, fgros, fnet ;
   double texit,aexit,uexit,rgas,gc,mexit,wflow,mflow,machend,aastr ;
   double mlocal,plocal,mex,pexit,pplume,plumrat ;

   int prob, nramps, nshocks, nslip, planet, numray, numd, desmod, ncycle ;
   int outzn,drawray,gamopt, plumopt, inex, reflect, jetinit, group ;
   int jexplug,jexm1 ;
              //  plot data 
   static double fact,pfac1,pfac2 ;
   int xt,yt,sldloc ;
   int vgrid, vplot, vpress, vtemp, vmach, vturn;
   int cbar, perfout, geomout, diout, geomr;
   int intin, extin, anlin;

   static double varmin, varmax, var;

              //  flow parameters
   static double[][] turn    = new double[3][55] ;
   static double[][] mach1   = new double[3][55] ;
   static double[][] mach2   = new double[3][55] ;
   static double[][] prat    = new double[3][55] ;
   static double[][] trat    = new double[3][55] ;
   static double[][] ptrat   = new double[3][55] ;
   static double[][] rhorat  = new double[3][55] ;
   static double[][] defl    = new double[3][55] ;
   static double[][] shkang  = new double[3][55] ;
   static double[][] expang  = new double[3][55] ;
   static double[][] pspo    = new double[3][55] ;
   static double[][] tsto    = new double[3][55] ;
   static double[][] ptpto   = new double[3][55] ;
   static double[][] rsro    = new double[3][55] ;
   static double[][] pm      = new double[3][55] ;
   static double[][] mang    = new double[3][55] ;
   static double[][] ppt     = new double[3][55] ;
   static double[][] ttt     = new double[3][55] ;
   static double[][] rrt     = new double[3][55] ;
   static double[][] xflow   = new double[3][55] ;
   static double[][] yflow   = new double[3][55] ;
   static double[][] pres    = new double[3][55] ;
   static double[][] temp    = new double[3][55] ;
   static boolean[][] detach = new boolean[3][55] ;
              //  wedge geometry
   static double[] ang = new double[110] ;
   static int[] wfamily = new int[110] ;
   static double[] winter = new double[110] ;
   static double[] wxbgn = new double[110] ;
   static double[] wxnd = new double[110] ;
   static double[] wybgn = new double[110] ;
   static double[] wynd = new double[110] ;
   static double[] wslope = new double[110] ;
              // internal cowl geometry
   static double[] icinter = new double[60] ;
   static double[] icxbgn = new double[60] ;
   static double[] icxnd = new double[60] ;
   static double[] icybgn = new double[60] ;
   static double[] icynd = new double[60] ;
   static double[] icslope = new double[60] ;
              // external cowl geometry
   static double[] ecinter = new double[60] ;
   static double[] ecxbgn = new double[60] ;
   static double[] ecxnd = new double[60] ;
   static double[] ecybgn = new double[60] ;
   static double[] ecynd = new double[60] ;
   static double[] ecslope = new double[60] ;
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
         // internal plume waves
   static int[] ifamily = new int[55] ;
   static double[] iinter = new double[55];
   static double[] ixbgn = new double[55] ;
   static double[] ixnd = new double[55] ;
   static double[] iybgn = new double[55] ;
   static double[] iynd = new double[55] ;
   static double[] islope = new double[55] ;
             // external plume waves
   static int[] efamily = new int[55] ;
   static double[] einter = new double[55] ;
   static double[] exbgn = new double[55] ;
   static double[] exnd = new double[55] ;
   static double[] eybgn = new double[55] ;
   static double[] eynd = new double[55] ;
   static double[] eslope = new double[55] ; 
                // isentropic relations
   double poverpt,tovertt,roverrt,arat,mu,nu ;
   double nuexit,muexit,pref,tref,rref ;
              // streamlines 
   static double[][] strx = new double[26][10] ;
   static double[][] stry = new double[26][10] ;
              // moc flow
   static double[][] mcmach  = new double[255][255] ;
   static double[][] mcturn  = new double[255][255] ;
   static double[][] mcdefl  = new double[255][255] ;
   static double[][] mcpm    = new double[255][255] ;
   static double[][] mcmang  = new double[255][255] ;
   static double[][] mcQ  = new double[255][255] ;
   static double[][] mcR  = new double[255][255] ;
   static double[][] mcx  = new double[255][255] ;
   static double[][] mcy  = new double[255][255] ;
   static double[][] mcal  = new double[255][255] ;
   static double[][] mcbe  = new double[255][255] ;
   static double[][] mcaxi1  = new double[255][255] ;
   static double[][] mcaxi2  = new double[255][255] ;
   static double[][] mcpres  = new double[255][255] ;
   static double[][] mctemp  = new double[255][255] ;

   static double[][] mcxul   = new double[255][255] ;
   static double[][] mcyul   = new double[255][255] ;
   static double[][] mcxur   = new double[255][255] ;
   static double[][] mcyur   = new double[255][255] ;
   static double[][] mcxll   = new double[255][255] ;
   static double[][] mcyll   = new double[255][255] ;
   static double[][] mcxlr   = new double[255][255] ;
   static double[][] mcylr   = new double[255][255] ;

   static double[][] mcprat  = new double[255][255] ;
   static double[][] mctrat  = new double[255][255] ;
   static double[][] mcrrat  = new double[255][255] ;
   static double[][] mcpp0   = new double[255][255] ;
   static double[][] mctt0   = new double[255][255] ;
   static double[][] mcrr0   = new double[255][255] ;
   static double[][] mcarat  = new double[255][255] ;
   static double[][] mcptpt0 = new double[255][255] ;
   static double[][] mcptrat = new double[255][255] ;

   int row, rowlo, rowhi, col, collo, colhi ;

   CardLayout layout,layin,layin2 ;

   Viewer view ;
   Num num ;
   Image offscreenImg ;
   Graphics offsGg ;
   Image offscreenImg2 ;
   Graphics offsGg2 ;

   static Frame f ;
   static PrintStream prnt;
   static FileWriter pfile ;
   static PrintWriter prntGrid ;

   DataInputStream inStream;
   DataOutputStream outStream ; 
   FileDialog openFile ;
   FileDialog saveFile, saveFil2 ;

   public void init() {
     boolean which = false ;
 
     offscreenImg = createImage(this.size().width,
                      this.size().height) ;
     offsGg = offscreenImg.getGraphics() ;
 
     offscreenImg2 = createImage(this.size().width,
                      this.size().height) ;
     offsGg2 = offscreenImg2.getGraphics() ;

     openFile = new FileDialog(f,"Open Restart",FileDialog.LOAD) ;
     saveFile = new FileDialog(f,"Save Restart",FileDialog.SAVE) ;
     saveFil2 = new FileDialog(f,"Save Grid",FileDialog.SAVE) ;
 
     setLayout(new GridLayout(2,1,0,0)) ;

     setDefaults () ;

     view = new Viewer(this) ;

     num = new Num(this) ;

     add(view) ;
     add(num) ;

     f.show() ;

     comPute( ) ;
     view.start() ;
  }
 
   public void setDefaults() {

     reflect = -1 ;
     ncycle = 2 ;
     drawray = 0;
     prob = 1 ;  // Nozzle design - moc points
     numray = 30 ;
     desmod = 1 ;
     alt = 35000.; altlo = 0.0; althi=90000.;
     pto = 50.; ptolo= 1.0; ptohi = 300.;
     tto = 1000.; ttolo=10.0; ttohi = 5000.;
     gamopt = 1 ;
     gamma = getGama(tto,gamopt) ;
     gama = gamma ;
     rgas = 1716. ;
     gc = 32.2 ;
     plumopt = 0 ;
     machdef = 2.0 ;

     nramps = 1 ;
     nshocks = 1 ;
     xlong = 1000. ;

     planet = 0 ;
     angmn = 0.0 ;
     angmx = 10.0 ;
     ang1 = 0.0 ;
     ang2mn = 0.0 ;
     ang2mx = 35.0 ;
     ang2 = 8.0 ;
     mach0 = machdef ; machlo = 0.0 ; machhi = 4.0 ;
     mex = 1.0 ; mexlo = 1.0; mexhi = 2.0 ;
     macl = 1.3 ;
     mlaslo = 1.05 ;
     mlashi = 1.6 ;

     xr1 = 500. ;
     xr1mn = 10. ;
     xr1mx = 1000. ; 

     ang[0]  = 0.0 ;
     prat[2][0] = 1.0 ;
     trat[2][0] = 1.0 ;
     ptrat[2][0] = 1.0 ;
     rhorat[2][0] = 1.0 ;
     pspo[2][0] = 1.0 ;
     tsto[2][0] = 1.0 ;
     rsro[2][0] = 1.0 ;
     ptpto[2][0]= 1.0 ;
     defl[2][0] = 0.0 ;
     turn[2][0] = 0.0 ;
     shkang[2][0] = 0.0 ;

     exht = 10.0 ;
     nzht = 10.0 ; nzhtlo = 1.0 ; nzhthi = 50. ;
     nzw = 10.0 ; nzwlo = 1.0; nzwhi = 50. ;
     nzar = nzht * nzw;
 //    nzar = pi*nzht*nzht/4.0;
 //    nzarlo = pi*nzhtlo*nzhtlo/4.0;
 //    nzarhi = pi*nzhthi*nzhthi/4.0;
     nzarlo = nzhtlo*nzw ;
     nzarhi = nzhthi*nzw ;
     nzlg = 1.0 ; nzlglo = .05 ; nzlghi = 2. ; 
     nprat = 1.0 ; pratlo = .2 ;  prathi = 2.0 ;
     perint = .5 ;  perlo = .01; perhi = 1.0 ;
//        exit conditions
     machend = 2.0 ;
     getIsen(machend,gamma) ;
     pexit = pto * poverpt ;
     texit = tto * tovertt ;
     aexit = nzar * arat;

     delx = .01 ; delxlo = .0001; delxhi = .25 ; 
     radius = .5; radlo = .1 ; radhi = 5.0 ;
     delxsav = delx; radsav = radius ;
 
     row =1; rowlo=1; rowhi= 100;
     col =1; collo=1; colhi= 100;

     xt = 80; yt = 20; sldloc = 130;
     vgrid = 1 ;
     vplot = -1 ;
     vpress = 1 ;
     vtemp = -1 ;
     vmach = -1 ;
     vturn = -1 ;

     intin = -1 ;
     extin = -1 ;
     anlin = 1 ;

     cbar = -1 ;
     perfout = -1 ;
     geomout = 1 ;
     diout = -1 ;
     geomr = -1 ;
     jetinit = 0 ;

     pfac1 = 5.0; pfac2 = .12;
     fact = 15.2 ;
 //    pfac1 = 1.25; pfac2 = .03;
 //    fact = 3.95 ;

   }

   public void getFreeStream() {
    /* input altitude */
     if (alt < 36152. ) {
        ts0 = 518.6 - 3.56 * alt / 1000. ;
        ps0 = 2116. * Math.pow(ts0/518.6, 5.256) ;
     }
     if (alt >= 36152. && alt <= 82345.) {   // Stratosphere
        ts0 = 389.98 ;
        ps0 = 2116. * .2236 *
            Math.exp((36000.-alt)/(53.35*389.98)) ;
     }
     if (alt >= 82345.) {
        ts0 = 389.98 + 1.645 * (alt-82345)/1000. ;
        ps0 = 2116. *.02456 * Math.pow(ts0/389.98,-11.388) ;
     }
 
     ps0 = ps0 / 144. ;  
     return ;
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

  public void getAir(double mach, double gam, double area, double pres, double temp) {
  // total pres in lb/sq ft .. total temp in Rankine .. area in feet^2 .. rgas in ft^2/s^2 / R
    double fac1, fac2,gm1,gp1 ;

    gm1 = gam -1.0 ;
    gp1 = gam +1.0 ;

    fac1 = 1.0 + .5 * gm1 * mach * mach ;
    fac2 = gp1 / (2.0 * gm1) ;
    mflow = area * pres * Math.sqrt(gam/(rgas * temp)) * 
                mach * Math.pow(fac1,-fac2) ;
    wflow = mflow * gc ;
              
    return ; 
  }   

   public double getShkang (double machin, double delr, double gam) {
        // Iterate to get Shock Angle (degrees) given Wedge Angle (radians).
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
                        int surf, int upstrm, int index) {
          // NACA 1135 - oblique shock relations.
       double mst, gm1, gp1, msq, m2sq, cotd ;

       mst = machin * Math.sin(shang*convdr) ;
       msq = machin * machin ;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       prat[surf][index] = (2.0*gam*mst*mst - gm1)/gp1 ;
       rhorat[surf][index] = (gp1*mst*mst)/(gm1*mst*mst + 2.0) ;
       trat[surf][index] = (2.0*gam*mst*mst - gm1) * (gm1*mst*mst + 2.0)
                  /(mst*mst*Math.pow(gp1,2.0)) ; 
       ptrat[surf][index] = (Math.pow(((gp1*mst*mst)/(gm1*mst*mst+2.0)),(gam/gm1)))
               * Math.pow((gp1/(2.0*gam*mst*mst - gm1)),(1.0/gm1)) ;
       m2sq = ((msq * mst * mst * Math.pow(gp1,2.0)) +
              (-4.0 * (mst*mst  - 1.0) * (gam*mst*mst + 1.0))) /
              ((2.0*gam*mst*mst - gm1) * (gm1*mst*mst + 2.0)) ;
       mach2[surf][index] = Math.sqrt(m2sq) ;
       cotd = Math.tan(shang*convdr)*((gp1*msq)/    
                (2.0*(mst * mst - 1.0))-1.0);
       defl[surf][index] = (Math.atan(1.0/cotd))/convdr ;
       mach1[surf][index] = machin ;

       pspo[surf][index] = pspo[surf][upstrm]*prat[surf][index] ;
       tsto[surf][index] = tsto[surf][upstrm]*trat[surf][index] ;
       rsro[surf][index] = rsro[surf][upstrm]*rhorat[surf][index] ;
       ptpto[surf][index] = ptpto[surf][upstrm]*ptrat[surf][index] ;
  
       return;
   }

   public void getPerform () {
 //   calculate airflow and thrust and drag
      int i,j,k,jk ;
      double gam,aft,ptft,machthrt,aexft,soundex,soundth ;
      double tth,pav,yav,uav,tav,mav,trav ;
      double[] yo = new double[100] ;
      double[] tro = new double[100] ;
      double[] mo = new double[100] ;
      double[] to = new double[100] ;
      double[] po = new double[100] ;

      gam = gamma ;
      aft = nzar / 144. ;   // throat area in sq ft
      ptft = pto  * 144. ;  // throat total pressure in lb /sq ft
      machthrt = 1.0 ;      // throat mach  - choked
      getAir(machthrt,gam,aft,ptft,tto) ;     
      soundex = Math.sqrt(gam * rgas * texit) ; 
      uexit = soundex * machend ;
      mexit = machend;

      if (prob < 4) {
         aexft = aexit / 144. ;  // exit area in sq ft
         fgros = uexit * mflow + (pexit - ps0)*aexit ; 
         aboat = xexit * nzw * Math.tan(convdr*ang1) ;
         dragab = aboat * (ps0 - plocal);
         num.out.nozp.o51.setText(String.valueOf(filter3(0.0))) ;
      }

  // plug nozzle
      if (prob >= 4 && prob <=5) {
         getIsen(machthrt,gam) ;
         tth = tovertt * tto ;
         soundth = Math.sqrt(gam * rgas * tth) ;
         fgros = soundth * mflow * Math.cos(convdr * mcturn[1][1]) ; 
         for (j=jexplug; j<=numd; ++j) {
           pav = .5 * pto * (mcpp0[1][j] + mcpp0[1][j+1]) ;
           fgros = fgros + (pav - ps0)*(mcy[1][j] - mcy[1][j+1])*nzw*yexit ;
         }
         if(nprat > 1.0) {
           pav = .5 * pto*poverpt * (pspo[2][2] + pspo[2][3]) ;
           fgros = fgros + (pav - ps0)*mcy[1][numd]*nzw*yexit ;
         }
         dragab = 0.0 ; 
         num.out.nozp.o51.setText(String.valueOf(filter3(0.0))) ;                
      }
      if (prob == 6) {
         aexft = aexit / 144. ;  // exit area in sq ft
         fgros = uexit * mflow + (pexit - ps0)*aexit ;
         aboat = xexit * nzw * Math.tan(convdr*ang1) ; 
         dragab = aboat * (ps0 - plocal) ;
         num.out.nozp.o51.setText(String.valueOf(filter3(0.0))) ;  
      }
      if (prob >= 7) {  // non-uniform flow
         k = 0 ;
         jk = 0 ;
         for (i=numd+1; i>=1 ; --i) {
            k = k + 1 ;
            for(j=numd - numray/2 + 1; j<= numd; ++ j) {
               jk = j-1 ;
               if (mcx[i][j] > xexit/nzht) break ;
            }
            yo[k] = mcy[i][jk] ;
            tro[k] = mcturn[i][jk] ;
            mo[k] = mcmach[i][jk] ;
            to[k] = mctemp[i][jk] ;
            po[k] = mcpres[i][jk] ;
            if(yo[k] == 0.0) break ;            
         }
         pav = 0.0 ;
         tav = 0.0 ;
         mav = 0.0 ;
         trav  = 0.0 ;
         yav = 0.0 ;
         for (i=1; i<= k-2; ++i){
            if (prob == 7) {
               pav = pav + .5*(po[i]+po[i+1])*(yo[i]-yo[i+1]) ;
               tav = tav + .5*(to[i]+to[i+1])*(yo[i]-yo[i+1]) ;
               mav = mav + .5*(mo[i]+mo[i+1])*(yo[i]-yo[i+1]) ;
               trav = trav + .5*(tro[i]+tro[i+1])*(yo[i]-yo[i+1]) ;
               yav = yav + (yo[i]-yo[i+1]) ;
            }
            if (prob == 8) {
               pav = pav + .5*(po[i]+po[i+1])*(yo[i]*yo[i]-yo[i+1]*yo[i+1]) ;
               tav = tav + .5*(to[i]+to[i+1])*(yo[i]*yo[i]-yo[i+1]*yo[i+1]) ;
               mav = mav + .5*(mo[i]+mo[i+1])*(yo[i]*yo[i]-yo[i+1]*yo[i+1]) ;
               trav = trav + .5*(tro[i]+tro[i+1])*(yo[i]*yo[i]-yo[i+1]*yo[i+1]) ;
               yav = yav + (yo[i]*yo[i]-yo[i+1]*yo[i+1]) ;
            }
         }
         if(prob == 7) {
            pav = pav + po[k-1]*yo[k-1] ;
            tav = tav + to[k-1]*yo[k-1] ;
            mav = mav + mo[k-1]*yo[k-1] ;
            trav = trav + tro[k-1]*yo[k-1] ;
            yav = yav + yo[k-1] ;
            pav = pav / (yexit/nzht) ; 
            tav = tav / (yexit/nzht) ; 
            mav = mav / (yexit/nzht) ; 
            trav = trav / (yexit/nzht) ;
            yav = yav / (yexit/nzht) ;
         }
         if(prob == 8) {
            pav = pav + po[k-1]*yo[k-1]*yo[k-1] ;
            tav = tav + to[k-1]*yo[k-1]*yo[k-1] ;
            mav = mav + mo[k-1]*yo[k-1]*yo[k-1] ;
            trav = trav + tro[k-1]*yo[k-1]*yo[k-1] ;
            yav = yav + yo[k-1]*yo[k-1] ;
            pav = pav / (yexit*yexit/(nzht*nzht)) ; 
            tav = tav / (yexit*yexit/(nzht*nzht)) ; 
            mav = mav / (yexit*yexit/(nzht*nzht)) ; 
            trav = trav / (yexit*yexit/(nzht*nzht)) ;
            yav = yav / (yexit*yexit/(nzht*nzht)) ;
         }

         uav = mav * Math.sqrt(gam * rgas * tav) * Math.cos(convdr*trav) ; 
         num.out.nozp.o51.setText(String.valueOf(filter3(trav))) ;

         pexit = pav ;
         texit = tav ;
         uexit = uav ;
         mexit = mav ;
         aexft = aexit / 144. ;  // exit area in sq ft
         fgros = uexit * mflow + (pexit - ps0)*aexit ; 

//  diagnostics
         num.out.diag.o1.setText(String.valueOf(k)) ;
         num.out.diag.o2.setText(String.valueOf(filter3(yo[1]))) ;
         num.out.diag.o3.setText(String.valueOf(filter3(tro[1]))) ;
         num.out.diag.o4.setText(String.valueOf(filter3(mo[1]))) ;
         num.out.diag.o5.setText(String.valueOf(filter3(to[1]))) ;
         num.out.diag.o7.setText(String.valueOf(filter3(yo[2]))) ;
         num.out.diag.o8.setText(String.valueOf(filter3(tro[2]))) ;
         num.out.diag.o9.setText(String.valueOf(filter3(mo[2]))) ;
         num.out.diag.o10.setText(String.valueOf(filter3(to[2]))) ;
         num.out.diag.o12.setText(String.valueOf(filter3(yo[3]))) ;
         num.out.diag.o13.setText(String.valueOf(filter3(tro[3]))) ;
         num.out.diag.o14.setText(String.valueOf(filter3(mo[3]))) ;
         num.out.diag.o15.setText(String.valueOf(filter3(to[3]))) ;
         num.out.diag.o17.setText(String.valueOf(filter3(yo[4]))) ;
         num.out.diag.o18.setText(String.valueOf(filter3(tro[4]))) ;
         num.out.diag.o19.setText(String.valueOf(filter3(mo[4]))) ;
         num.out.diag.o20.setText(String.valueOf(filter3(to[4]))) ;
         num.out.diag.o22.setText(String.valueOf(filter3(yo[5]))) ;
         num.out.diag.o23.setText(String.valueOf(filter3(tro[5]))) ;
         num.out.diag.o24.setText(String.valueOf(filter3(mo[5]))) ;
         num.out.diag.o25.setText(String.valueOf(filter3(to[5]))) ;

         num.out.diag.o26.setText(String.valueOf(filter3(yav))) ;
         num.out.diag.o27.setText(String.valueOf(filter3(trav))) ;
         num.out.diag.o28.setText(String.valueOf(filter3(tav))) ;
         num.out.diag.o29.setText(String.valueOf(filter3(mav))) ;
         num.out.diag.o30.setText(String.valueOf(filter3(pav))) ;
      }

      fnet = fgros - dragab ;   
      return;
   }

   public void comPute() {
       double gamex ;

       loadZex() ;
   // external flow
       getFreeStream() ;
       gamex = 1.4 ;
       pspo[1][0] = 1.0 ;
       tsto[1][0] = 1.0 ;
       rsro[1][0] = 1.0 ;
       ptpto[1][0] = 1.0 ;
       prat[1][1] = 1.0 ;
       mach2[1][0] = mach1[1][0] = 0.0 ;
       if (plumopt == 2) {
         mach2[1][0] = mach1[1][0] = mach0 ;
         getIsenExp(mach0,gamex,-(ang1*convdr),1,0,1) ;
         eslope[1] = shkang[1][1] ;
         eslope[2] = expang[1][1] ;
         getIsen(mach2[1][1],gamex) ;
         mang[1][1] = mu ;
         pm[1][1] = nu ;
       }
       plocal = ps0 * prat[1][1] ;
       mlocal = mach2[1][1] ;

   // internal flow
       gamma = getGama(tto, gamopt) ;
       getIsen(machend,gamma) ;
       pexit = pto * poverpt ;
       texit = tto * tovertt ;
       aexit = nzar * arat ;

       nprat = pexit / plocal ;
  //     nprat = pexit / ps0 ;
       mach2[2][0] = mach1[2][0] = machend ;
  // throat conditions
       getIsen(1.0,gamma) ;
       psth = pto * poverpt ;
       tsth = tto * tovertt ;

       if (prob == 0) {   // 2d -ideal nozzle - zone
           anlNoz() ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 1) {   // 2d - ideal nozzle - points
           anlNozII() ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 2) {   // Axi - ideal nozzle - points
           anlNozIII() ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 3) {   // 2d - jet exhaust - over or under expanded
           anlJet() ;
       }
       if (prob == 4) {   // 2D external plug nozzle
           anlPlugED() ;    // external plug design
           nprat = pexit/ps0 ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 5) {   // 2D internal/external plug nozzle - cc lee 
           anlPlugED() ;    // external plug design
           anlPlugID() ;    // internal plug design
           nprat = pexit/ps0 ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 6) {   // 2d internal external plug - flat cowl
           anlPlugII() ;
           if (plumopt >=1) {
              anlJet() ;
           }
       }
       if (prob == 7) {   // 2D Wedge
           anlNozIV() ;
       }
       if (prob == 8) {   // Axi Wedge
           anlNozV() ;
       }

       getPerform () ;
       loadOut() ;
 
       view.repaint();
   }

   public void loadZex() {
       int i,j,k ;

       for (k=0; k<=2; ++ k) {
         for(i=1; i<=27; ++i) {
            pspo[k][i] = 0.0 ;
            tsto[k][i] = 0.0 ;
            rsro[k][i] = 0.0 ;
            ptpto[k][i] = 1.0 ;
            prat[k][i] = 0.0 ;
            trat[k][i] = 0.0 ;
            rhorat[k][i] = 0.0 ;
            ptrat[k][i] = 1.0 ;
            turn[k][i] = 0.0 ;
            defl[k][i] = 0.0 ;
            shkang[k][i] = 0.0 ;
            mach2[k][i] = 0.0 ;
            detach[k][i] = false ;
         }
       }
   }

   public void loadZero() {
       int i,j,k ;

       if(jetinit == 0) {
         for(i=1; i<=254; ++i) {
           for(j=1; j<=254; ++j) {
             mcpp0[i][j] = 1.0 ;
             mctt0[i][j] = 1.0 ;
             mcmach[i][j] = 1.0 ;
             mcpres[i][j] = 0.0 ;
             mctemp[i][j] = 0.0 ;
             mcturn[i][j] = 0.0 ;
           }
         }
       }
   }

   public void getMinMax() {
       int i,j ;
       int grp,jcr,jcl;

       if(prob <= 3) {
         if(vplot == 1)  {  // plot min max
           if (vpress == 1) {  // pressure plot
              varmin = mcpres[1][1] ;
              varmax = mcpres[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcpres[i][j] > varmax) varmax = mcpres[i][j] ;
                    if(mcpres[i][j] < varmin) varmin = mcpres[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(pres[2][j] > varmax) varmax = pres[2][j] ;
                       if(pres[2][j] < varmin) varmin = pres[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(pres[2][j] > varmax) varmax = pres[2][j] ;
                       if(pres[2][j] < varmin) varmin = pres[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(pres[2][j] > varmax) varmax = pres[2][j] ;
                    if(pres[2][j] < varmin) varmin = pres[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
              }
           }
           if (vtemp == 1) {  // temperature plot
              varmin = mctemp[1][1] ;
              varmax = mctemp[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mctemp[i][j] > varmax) varmax = mctemp[i][j] ;
                    if(mctemp[i][j] < varmin) varmin = mctemp[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(temp[2][j] > varmax) varmax = temp[2][j] ;
                       if(temp[2][j] < varmin) varmin = temp[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(temp[2][j] > varmax) varmax = temp[2][j] ;
                       if(temp[2][j] < varmin) varmin = temp[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(temp[2][j] > varmax) varmax = temp[2][j] ;
                    if(temp[2][j] < varmin) varmin = temp[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
              }
           }
           if (vmach == 1) {  // mach number plot
              varmin = mcmach[1][1] ;
              varmax = mcmach[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcmach[i][j] > varmax) varmax = mcmach[i][j] ;
                    if(mcmach[i][j] < varmin) varmin = mcmach[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                       if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                       if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                    if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
              }
           }
           if (vturn == 1) {  // flow turning plot
              varmin = mcturn[1][1] ;
              varmax = mcturn[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcturn[i][j] > varmax) varmax = mcturn[i][j] ;
                    if(mcturn[i][j] < varmin) varmin = mcturn[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(turn[2][j] > varmax) varmax = turn[2][j] ;
                       if(turn[2][j] < varmin) varmin = turn[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(turn[2][j] > varmax) varmax = turn[2][j] ;
                       if(turn[2][j] < varmin) varmin = turn[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(turn[2][j] > varmax) varmax = turn[2][j] ;
                    if(turn[2][j] < varmin) varmin = turn[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
              }
           }
           num.out.colout.colvar.co1.setText(String.valueOf(filter3(varmax))) ;
           num.out.colout.colvar.co2.setText(String.valueOf(filter3(varmin))) ;
         }
       }

       if(prob >= 4 && prob <= 5) {
         if(vplot == 1)  {  // plot min max
            if (vpress == 1) { // pressure
               varmin = mcpres[1][1] ;
               varmax = mcpres[1][1] ;
               for (j=jexplug; j<=numray; ++j) {
                  if(mcpres[1][j] > varmax) varmax = mcpres[1][j] ;
                  if(mcpres[1][j] < varmin) varmin = mcpres[1][j] ;
               }
               if(plumopt == 1) {
                  if(nprat > 1.0) {
                     for(j=1; j<=5; ++j) {
                        if(pres[2][j] > varmax) varmax = pres[2][j] ;
                        if(pres[2][j] < varmin) varmin = pres[2][j] ;
                     }
                  }
                  if(nprat <= 1.0) {
                     for(j=1; j<=7; ++j) {
                        if(pres[2][j] > varmax) varmax = pres[2][j] ;
                        if(pres[2][j] < varmin) varmin = pres[2][j] ;
                     }
                  }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(pres[2][j] > varmax) varmax = pres[2][j] ;
                    if(pres[2][j] < varmin) varmin = pres[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
              }
           }
           if (vtemp == 1) { // temperature
               varmin = mctemp[1][1] ;
               varmax = mctemp[1][1] ;
               for (j=jexplug; j<=numray; ++j) {
                  if(mctemp[1][j] > varmax) varmax = mctemp[1][j] ;
                  if(mctemp[1][j] < varmin) varmin = mctemp[1][j] ;
               }
               if(plumopt == 1) {
                  if(nprat > 1.0) {
                     for(j=1; j<=5; ++j) {
                        if(temp[2][j] > varmax) varmax = temp[2][j] ;
                        if(temp[2][j] < varmin) varmin = temp[2][j] ;
                     }
                  }
                  if(nprat <= 1.0) {
                     for(j=1; j<=7; ++j) {
                        if(temp[2][j] > varmax) varmax = temp[2][j] ;
                        if(temp[2][j] < varmin) varmin = temp[2][j] ;
                     }
                  }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(temp[2][j] > varmax) varmax = temp[2][j] ;
                    if(temp[2][j] < varmin) varmin = temp[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
              }
           }
           if (vmach == 1) { // mach number
               varmin = mcmach[1][1] ;
               varmax = mcmach[1][1] ;
               for (j=jexplug; j<=numray; ++j) {
                  if(mcmach[1][j] > varmax) varmax = mcmach[1][j] ;
                  if(mcmach[1][j] < varmin) varmin = mcmach[1][j] ;
               }
               if(plumopt == 1) {
                  if(nprat > 1.0) {
                     for(j=1; j<=5; ++j) {
                        if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                        if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                     }
                  }
                  if(nprat <= 1.0) {
                     for(j=1; j<=7; ++j) {
                        if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                        if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                     }
                  }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                    if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
              }
           }
           if (vturn == 1) { // flow turning
               varmin = mcturn[1][1] ;
               varmax = mcturn[1][1] ;
               for (j=jexplug; j<=numray; ++j) {
                  if(mcturn[1][j] > varmax) varmax = mcturn[1][j] ;
                  if(mcturn[1][j] < varmin) varmin = mcturn[1][j] ;
               }
               if(plumopt == 1) {
                  if(nprat > 1.0) {
                     for(j=1; j<=5; ++j) {
                        if(turn[2][j] > varmax) varmax = turn[2][j] ;
                        if(turn[2][j] < varmin) varmin = turn[2][j] ;
                     }
                  }
                  if(nprat <= 1.0) {
                     for(j=1; j<=7; ++j) {
                        if(turn[2][j] > varmax) varmax = turn[2][j] ;
                        if(turn[2][j] < varmin) varmin = turn[2][j] ;
                     }
                  }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(turn[2][j] > varmax) varmax = turn[2][j] ;
                    if(turn[2][j] < varmin) varmin = turn[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
              }
           }
           num.out.colout.colvar.co1.setText(String.valueOf(filter3(varmax))) ;
           num.out.colout.colvar.co2.setText(String.valueOf(filter3(varmin))) ;
         }
       }

       if(prob == 6) {
         if(vplot == 1)  {  // plot min max
           if (vpress == 1) {  // pressure plot
              varmin = mcpres[1][1] ;
              varmax = mcpres[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcpres[i][j] > varmax) varmax = mcpres[i][j] ;
                    if(mcpres[i][j] < varmin) varmin = mcpres[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(pres[2][j] > varmax) varmax = pres[2][j] ;
                       if(pres[2][j] < varmin) varmin = pres[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(pres[2][j] > varmax) varmax = pres[2][j] ;
                       if(pres[2][j] < varmin) varmin = pres[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(pres[2][j] > varmax) varmax = pres[2][j] ;
                    if(pres[2][j] < varmin) varmin = pres[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(pres[1][j] > varmax) varmax = pres[1][j] ;
                       if(pres[1][j] < varmin) varmin = pres[1][j] ;
                    }
                 }
              }
           }
           if (vtemp == 1) {  // temperature plot
              varmin = mctemp[1][1] ;
              varmax = mctemp[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mctemp[i][j] > varmax) varmax = mctemp[i][j] ;
                    if(mctemp[i][j] < varmin) varmin = mctemp[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(temp[2][j] > varmax) varmax = temp[2][j] ;
                       if(temp[2][j] < varmin) varmin = temp[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(temp[2][j] > varmax) varmax = temp[2][j] ;
                       if(temp[2][j] < varmin) varmin = temp[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(temp[2][j] > varmax) varmax = temp[2][j] ;
                    if(temp[2][j] < varmin) varmin = temp[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(temp[1][j] > varmax) varmax = temp[1][j] ;
                       if(temp[1][j] < varmin) varmin = temp[1][j] ;
                    }
                 }
              }
           }
           if (vmach == 1) {  // mach number plot
              varmin = mcmach[1][1] ;
              varmax = mcmach[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcmach[i][j] > varmax) varmax = mcmach[i][j] ;
                    if(mcmach[i][j] < varmin) varmin = mcmach[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                       if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                       if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(mach2[2][j] > varmax) varmax = mach2[2][j] ;
                    if(mach2[2][j] < varmin) varmin = mach2[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(mach2[1][j] > varmax) varmax = mach2[1][j] ;
                       if(mach2[1][j] < varmin) varmin = mach2[1][j] ;
                    }
                 }
              }
           }
           if (vturn == 1) {  // flow turning plot
              varmin = mcturn[1][1] ;
              varmax = mcturn[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcturn[i][j] > varmax) varmax = mcturn[i][j] ;
                    if(mcturn[i][j] < varmin) varmin = mcturn[i][j] ;
                 }
              }
              if(plumopt == 1) {
                 if(nprat > 1.0) {
                    for(j=1; j<=5; ++j) {
                       if(turn[2][j] > varmax) varmax = turn[2][j] ;
                       if(turn[2][j] < varmin) varmin = turn[2][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=7; ++j) {
                       if(turn[2][j] > varmax) varmax = turn[2][j] ;
                       if(turn[2][j] < varmin) varmin = turn[2][j] ;
                    }
                 }
              }
              if(plumopt == 2) {
    //  internal
                 for(j=1; j<=3; ++j) {
                    if(turn[2][j] > varmax) varmax = turn[2][j] ;
                    if(turn[2][j] < varmin) varmin = turn[2][j] ;
                 }
     // external
                 if(nprat > 1.0) {
                    for(j=1; j<=4; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
                 if(nprat <= 1.0) {
                    for(j=1; j<=3; ++j) {
                       if(turn[1][j] > varmax) varmax = turn[1][j] ;
                       if(turn[1][j] < varmin) varmin = turn[1][j] ;
                    }
                 }
              }
           }
           num.out.colout.colvar.co1.setText(String.valueOf(filter3(varmax))) ;
           num.out.colout.colvar.co2.setText(String.valueOf(filter3(varmin))) ;
         }
       }

       if(prob >= 7) {  // wedge nozzle
         if(vplot == 1)  {  // plot min max
           if (vpress == 1) {  // pressure plot
              varmin = mcpres[1][1] ;
              varmax = mcpres[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcpres[i][j] > varmax) varmax = mcpres[i][j] ;
                    if(mcpres[i][j] < varmin) varmin = mcpres[i][j] ;
                 }
              }
              for(grp=1; grp<=group; ++grp) {
   // complex region on cowl
                for(jcr=1; jcr<=numray/2-1; ++jcr) {
                   i = grp*numray/2 + jcr ;
                   for (jcl=jcr; jcl<=numray/2-1; ++jcl) {
                      j = (grp-1)*numray/2 + 1 + jcl ;
                      if(mcpres[i][j] > varmax) varmax = mcpres[i][j] ;
                      if(mcpres[i][j] < varmin) varmin = mcpres[i][j] ;
                   }
                }
   // complex region on centerline
                for(jcl=1; jcl<=numray/2; ++jcl) {
                   j = grp*numray/2 + 1 + jcl ;
                   for (jcr=jcl; jcr<=numray/2; ++jcr) {
                      i = grp*numray/2 + jcr ;
                      if(mcpres[i][j] > varmax) varmax = mcpres[i][j] ;
                      if(mcpres[i][j] < varmin) varmin = mcpres[i][j] ;
                   }
                }
              }
           }
           if (vtemp == 1) {  // temperature plot
              varmin = mctemp[1][1] ;
              varmax = mctemp[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mctemp[i][j] > varmax) varmax = mctemp[i][j] ;
                    if(mctemp[i][j] < varmin) varmin = mctemp[i][j] ;
                 }
              }
              for(grp=1; grp<=group; ++grp) {
   // complex region on cowl
                for(jcr=1; jcr<=numray/2-1; ++jcr) {
                   i = grp*numray/2 + jcr ;
                   for (jcl=jcr; jcl<=numray/2-1; ++jcl) {
                      j = (grp-1)*numray/2 + 1 + jcl ;
                      if(mctemp[i][j] > varmax) varmax = mctemp[i][j] ;
                      if(mctemp[i][j] < varmin) varmin = mctemp[i][j] ;
                   }
                }
   // complex region on centerline
                for(jcl=1; jcl<=numray/2; ++jcl) {
                   j = grp*numray/2 + 1 + jcl ;
                   for (jcr=jcl; jcr<=numray/2; ++jcr) {
                      i = grp*numray/2 + jcr ;
                      if(mctemp[i][j] > varmax) varmax = mctemp[i][j] ;
                      if(mctemp[i][j] < varmin) varmin = mctemp[i][j] ;
                   }
                }
              }
           }
           if (vmach == 1) {  // mach number plot
              varmin = mcmach[1][1] ;
              varmax = mcmach[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcmach[i][j] > varmax) varmax = mcmach[i][j] ;
                    if(mcmach[i][j] < varmin) varmin = mcmach[i][j] ;
                 }
              }
              for(grp=1; grp<=group; ++grp) {
   // complex region on cowl
                for(jcr=1; jcr<=numray/2-1; ++jcr) {
                   i = grp*numray/2 + jcr ;
                   for (jcl=jcr; jcl<=numray/2-1; ++jcl) {
                      j = (grp-1)*numray/2 + 1 + jcl ;
                      if(mcmach[i][j] > varmax) varmax = mcmach[i][j] ;
                      if(mcmach[i][j] < varmin) varmin = mcmach[i][j] ;
                   }
                }
   // complex region on centerline
                for(jcl=1; jcl<=numray/2; ++jcl) {
                   j = grp*numray/2 + 1 + jcl ;
                   for (jcr=jcl; jcr<=numray/2; ++jcr) {
                      i = grp*numray/2 + jcr ;
                      if(mcmach[i][j] > varmax) varmax = mcmach[i][j] ;
                      if(mcmach[i][j] < varmin) varmin = mcmach[i][j] ;
                   }
                }
              }
           }
           if (vturn == 1) {  // flow turning plot
              varmin = mcturn[1][1] ;
              varmax = mcturn[1][1] ;
              for (i=1; i<=numray/2+1; ++ i) {
                 for(j=1; j<=numray/2+1; ++j) {
                    if(mcturn[i][j] > varmax) varmax = mcturn[i][j] ;
                    if(mcturn[i][j] < varmin) varmin = mcturn[i][j] ;
                 }
              }
              for(grp=1; grp<=group; ++grp) {
   // complex region on cowl
                for(jcr=1; jcr<=numray/2-1; ++jcr) {
                   i = grp*numray/2 + jcr ;
                   for (jcl=jcr; jcl<=numray/2-1; ++jcl) {
                      j = (grp-1)*numray/2 + 1 + jcl ;
                      if(mcturn[i][j] > varmax) varmax = mcturn[i][j] ;
                      if(mcturn[i][j] < varmin) varmin = mcturn[i][j] ;
                   }
                }
   // complex region on centerline
                for(jcl=1; jcl<=numray/2; ++jcl) {
                   j = grp*numray/2 + 1 + jcl ;
                   for (jcr=jcl; jcr<=numray/2; ++jcr) {
                      i = grp*numray/2 + jcr ;
                      if(mcturn[i][j] > varmax) varmax = mcturn[i][j] ;
                      if(mcturn[i][j] < varmin) varmin = mcturn[i][j] ;
                   }
                }
              }
           }
           num.out.colout.colvar.co1.setText(String.valueOf(filter3(varmax))) ;
           num.out.colout.colvar.co2.setText(String.valueOf(filter3(varmin))) ;
         }
       }
   }
  
   public void getDelmx() {     //  routine to calculate the maximum delx
       double xthrtx,xthrty,alfa,betar,betar90,xrefl,tanalf,tanbet,tanb90, x1max ;
       float fl1 ;
       int i1;

       xthrtx  = 0.0 ;
       xthrty  = .5 ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       alfa =  nuexit / numray ;

       tanalf = Math.tan(convdr*alfa) ;

       betar = mu ;
       tanbet = Math.tan(convdr*betar);
       xrefl = xthrty * tanbet ;
       
       betar90 = 90.0 - betar;
       tanb90 = Math.tan(convdr*betar90) ;

       x1max = - 2.0 * xrefl * tanb90 / (tanalf - tanb90) ;

       delxhi = x1max / (numray/2 -1) ;

       if (delx > delxhi) delx = delxhi ;
  
       fl1 = (float) delx ;
       num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl1))) ;
       i1 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
       num.inp.anlpan.inright.s8.setValue(i1) ;

       return ;
  }

   public void anlPlugII() {     //  Design for 2D plug nozzle - flat cowl
  // find the shape of the plug - cowl is flat.
       int i, j, k;
       double dell1, dell2, shifty ;

       jetinit = 0 ;
       loadZero() ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       thetmx = nuexit / 2.0 ;
       delthet = nuexit / numray ;
       aastr = arat;

       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
       mcmach[0][0] = 1.0 ;
       getIsen(mcmach[0][0], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[0][0] = 1.0 ;
       mctt0[0][0] = 1.0 ;
       mcrr0[0][0] = 1.0 ;
       mcprat[0][0] = 1.0 ;
       mctrat[0][0] = 1.0 ;
       mcrrat[0][0] = 1.0 ;
       mcarat[0][0] = arat ;
       mcx[1][0] = 0.0 ;
       mcy[1][0] = 1.0 ;

       wxbgn[1] = -nzlg ;
       wybgn[1] = 0.0;
       wxnd[1]  = 0.0 ;
       wynd[1]  = 1.0 - 1.0 / aastr ;

// analysis by points 

// 1-1  - plug surface
       mcdefl[1][1] = -delthet ;
       mcturn[1][1] = -delthet ;
       mcpm[1][1]   = Math.abs(mcturn[1][1]) ;
       mcQ[1][1] = mcpm[1][1] + mcturn[1][1] ;
       mcR[1][1] = mcpm[1][1] - mcturn[1][1] ;
       getMOCVar(1,1,0,0) ;
       mcx[1][1] = 0.0 ;
       mcy[1][1] = wynd[1] ;
// 1-2  - cowl
       mcturn[2][1] = 0.0 ;
       mcdefl[2][1] = mcturn[2][1] - mcturn[1][1] ;
       mcR[2][1] = mcR[1][1] ;
       mcQ[2][1] = mcR[2][1] ;
       mcpm[2][1]= mcR[2][1] ;
       getMOCVar(2,1,1,1) ;
       mcy[2][1] = 1.0;
       mcx[2][1] = mcx[1][1] + 
          (mcy[2][1]-mcy[1][1]) / Math.tan(convdr*(mcmang[1][1])) ;

//  n-1 - plug surface
       for(k=2; k<=numray/2; ++k) {
          mcdefl[1][k] = -delthet ;
          mcturn[1][k] = mcturn[1][k-1] + mcdefl[1][k] ;
          mcpm[1][k]   = Math.abs(mcturn[1][k]) ;
          mcQ[1][k] = mcpm[1][k] + mcturn[1][k] ;
          mcR[1][k] = mcpm[1][k] - mcturn[1][k] ;
          getMOCVar(1,k,1,k-1) ;
          mcx[1][k] = mcx[1][k-1] + delx ;
          mcy[1][k] = mcy[1][k-1] + 
                (mcx[1][k]-mcx[1][k-1])*Math.tan(convdr*mcturn[1][k-1]);
// internal points  
          for(i=2; i<=k; ++i) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i][k-1] ;
             getMOCVar(i,k,i-1,k) ;
/*
             mcal[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcbe[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i-1][k] * Math.tan(convdr*mcal[i-1][k]) + 
                mcx[i][k-1] * Math.tan(convdr*mcbe[i][k-1]) )/
                (Math.tan(convdr*mcal[i-1][k]) + Math.tan(convdr*mcbe[i][k-1]) );
             mcy[i][k] = mcy[i-1][k] + 
               (mcx[i][k] - mcx[i-1][k])*Math.tan(convdr*mcal[i-1][k]);
*/
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) )/
                (Math.tan(convdr*mcbe[i-1][k]) + Math.tan(convdr*mcal[i][k-1]) );
             mcy[i][k] = mcy[i-1][k] + 
               (mcx[i][k] - mcx[i-1][k])*Math.tan(convdr*mcbe[i-1][k]);

          }
//  cowl
          mcturn[k+1][k] = 0.0 ;
          mcdefl[k+1][k] = mcturn[k+1][k] - mcturn[k][k] ;
          mcR[k+1][k] = mcR[k][k] ;
          mcQ[k+1][k] = mcR[k+1][k] ;
          mcpm[k+1][k]= mcR[k+1][k] ;
          getMOCVar(k+1,k,k,k) ;
          mcy[k+1][k] = 1.0;
          mcx[k+1][k] = mcx[k][k] + 
            (mcy[k+1][k]-mcy[k][k]) / Math.tan(convdr*(mcmang[k][k]+mcturn[k][k])) ;
       }
 //  cancellation surface
       k = numray/2 + 1 ;
       mcdefl[1][k] = mcdefl[1][k-1] ;
       mcturn[1][k] = mcturn[1][k-1] ;
       mcQ[1][k] = mcQ[1][k-1] ;
       mcpm[1][k] = mcpm[1][k-1] ;
       mcR[1][k] = mcR[1][k-1] ;
       mcmach[1][k] = mcmach[1][k-1] ;
       getIsen(mcmach[1][k], gamma) ;
       mcmang[1][k] = mcmang[1][k-1];
       mcal[1][k] =  mcal[1][k-1] ;
       mcbe[1][k] = mcbe[1][k-1] ;
       mcx[1][k] = mcx[1][k-1] ;
       mcy[1][k] = mcy[1][k-1] ; 
       mcpp0[1][k] = mcpp0[1][k-1] ;
       mctt0[1][k] = mctt0[1][k-1] ;
       mcrr0[1][k] = mcrr0[1][k-1] ;
       mcarat[1][k] = mcarat[1][k-1] ;
       mcprat[1][k] = mcprat[1][k-1] ;
       mctrat[1][k] = mctrat[1][k-1] ;
       mcrrat[1][k] = mcrrat[1][k-1] ;

       for(i=2; i<=k; ++i) {
          mcdefl[i][k] = delthet ;
          mcturn[i][k] = mcturn[i-1][k] + mcdefl[i][k] ;
          mcQ[i][k] = mcQ[i][k-1] ;
          mcpm[i][k] = mcQ[i][k] - mcturn[i][k] ;
          mcR[i][k] = mcpm[i][k] + mcturn[i][k] ;
          getMOCVar(i,k,i-1,k) ;
          mcal[i-1][k] = -mcturn[i-1][k] ;
  //        mcbe[i][k-1] = (mcmang[i][k-1] + mcturn[i][k-1]) ;
          mcbe[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
          mcx[i][k] = (mcy[i-1][k] - mcy[i][k-1] + 
             mcx[i-1][k] * Math.tan(convdr*mcal[i-1][k]) - 
             mcx[i][k-1] * Math.tan(convdr*mcbe[i][k-1]) )/
             (Math.tan(convdr*mcal[i-1][k]) - Math.tan(convdr*mcbe[i][k-1]) );
          mcy[i][k] = mcy[i-1][k] - 
            (mcx[i][k] - mcx[i-1][k])*Math.tan(convdr*mcal[i-1][k]);
       }
// ****  shift geometry for small inaccuracies in y-location
       shifty = -mcy[k][k] ;
       for (j=0; j<=k; ++j) {
          for(i=1; i<=k; ++i) {
             mcy[i][j] = mcy[i][j] + shifty ;            
          }
       }
       wynd[1] = wynd[1] + shifty ;
//  plug geometry
       for(i=2; i<=numray/2; ++i) {
          wxbgn[i] = wxnd[i-1] ;
          wybgn[i] = wynd[i-1] ;
          wxnd[i]  = mcx[1][i] ;
          wynd[i]  = mcy[1][i] ;
       }
       for(i=numray/2+1; i<=numray; ++i) {
          wxbgn[i] = wxnd[i-1] ;
          wybgn[i] = wynd[i-1] ;
          j = i - numray/2 + 1 ;
          wxnd[i]  = mcx[j][k] ;
          wynd[i]  = mcy[j][k] ;
       }
       wxbgn[numray + 1] = wxnd[numray] ;
       wybgn[numray + 1] = wynd[numray] ;
//       wxnd[i]  = wxbgn[numray+1] + (wxnd[numray] - wxbgn[numray]) ;
       wxnd[i]  = wxbgn[numray+1] ;
       wynd[i]  = wybgn[numray+1] ;

//  internal cowl geometry
       icxbgn[1] = -nzlg ;
       icybgn[1] = 1.0 + shifty;
       icxnd[1]  = 0.0 ;
       icynd[1]  = 1.0 + shifty ;

       for(i=2; i<=numray/2; ++i) {
          icxbgn[i] = icxnd[i-1] ;
          icybgn[i] = 1.0 + shifty ;
          icxnd[i]  = mcx[i+1][i] ;
          icynd[i]  = 1.0 + shifty ;
       }

// external cowl geometry

       exthk = icxnd[numray/2]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[numray/2] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[numray/2] ;
       ecynd[2] = icynd[numray/2] ;
       
       xexit = nzht*icxnd[numray/2] ;
       yexit = nzht*icynd[numray/2] ;
       xpexit = nzht*wxnd[numray] ;
       ypexit = nzht*wynd[numray] ;

// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

       return ;
   }

   public void anlPlugID() {     //  Design for Internal Plug Nozzle
// find the shape of the plug  
       double l1,m1,dphi,phi,phi0,cenx,ceny,delr,t1,t2 ;
       int i, j, k ;

       jexm1 = jexplug -1 ;

//  last left running ray
       mcmach[1][jexm1] = mcmach[1][jexplug] ;
       mcmang[1][jexm1] = mcmang[1][jexplug] ;
       mcpm[1][jexm1] = mcpm[1][jexplug] ;
       mcturn[1][jexm1] = mcturn[1][jexplug] ;
       mcdefl[1][jexm1] = 0.0 ;
       mcpp0[1][jexm1] = mcpp0[1][jexplug] ;
       mctt0[1][jexm1] = mctt0[1][jexplug] ;
       mcrr0[1][jexm1] = mcrr0[1][jexplug] ;
       mcarat[1][jexm1] = mcarat[1][jexplug] ;
       mcprat[1][jexm1] = 1.0 ;
       mctrat[1][jexm1] = 1.0 ;
       mcrrat[1][jexm1] = 1.0 ;

// straight section geometry
       l1 = Math.sqrt(wxnd[jexplug]*wxnd[jexplug] + (1.0 - wynd[jexplug])*(1.0 - wynd[jexplug])) ;
       m1 = l1 * Math.cos(convdr * mcmang[1][jexplug]) ;
       wxnd[jexm1] = wxnd[jexplug] - 2.0 * m1 * Math.cos(convdr*mcturn[1][jexm1]) ;
       wynd[jexm1] = wynd[jexplug] + 2.0 * m1 * Math.sin(convdr*mcturn[1][jexm1]) ;
       wxbgn[jexplug] = wxnd[jexm1] ;
       wybgn[jexplug] = wynd[jexm1] ;  
// last left running ray 
       mcx[1][jexm1] = wxnd[jexm1] ;
       mcy[1][jexm1] = wynd[jexm1] ;
       mcx[2][jexm1] = 0.0 ;
       mcy[2][jexm1] = 1.0 ;

// circular section geometry
       cenx = wxnd[jexm1] - radius * Math.sin(convdr*mcturn[1][jexm1]) ;
       ceny = wynd[jexm1] - radius * Math.cos(convdr*mcturn[1][jexm1]) ;
       dphi = (nuexit - mcturn[1][jexm1]) / (jexm1) ;
       phi0 = 2.0 * mcturn[1][jexm1] - nuexit ;
       for (j=1; j<= jexm1 - 1; ++j) {
          phi = phi0 + j* dphi ;
          wxnd[j] = cenx + radius * Math.sin(convdr*phi) ;
          wynd[j] = ceny + radius * Math.cos(convdr*phi) ;
          mcturn[1][j] = - phi ;
          mcx[1][j] = wxnd[j] ;
          mcy[1][j] = wynd[j] ;
       }
       wxbgn[1] = wxnd[1] - nzlg ;
       wybgn[1] = 0.0 ;
//       wybgn[1] = wynd[jexm1] ;
       for(i=2; i<=jexm1; ++i) {
          wxbgn[i] = wxnd[i-1] ;
          wybgn[i] = wynd[i-1] ;
       }
       wxnd[0] = wxnd[1] ;
       wynd[0] = wynd[1] ;
       wxbgn[0] = wxbgn[1] ;
       wybgn[0] = wybgn[1] ;

// flow in throat
       mcmach[1][1] = 1.0 ;
       mcmang[1][1] = 90.;
       mcpm[1][1] = 0.0 ; 
       getIsen(mcmach[1][1], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[1][1] = poverpt ;
       mctt0[1][1] = tovertt;
       mcrr0[1][1] = roverrt ;
       mcprat[1][1] = 1.0 ;
       mctrat[1][1] = 1.0 ;
       mcrrat[1][1] = 1.0 ;
       mcarat[1][1] = arat ;
  //     mcdefl[1][1] = mcturn[1][2] ; 
       mcdefl[1][1] = - dphi ;
       mcx[2][1] = mcx[1][1] - (1.0 / mcarat[1][numray]) * Math.sin(convdr * mcturn[1][1]) ;
       mcy[2][1] = mcy[1][1] + (1.0 / mcarat[1][numray]) * Math.cos(convdr * mcturn[1][1]) ;
       pspo[2][1] = mcpp0[1][1] ;
       tsto[2][1] = mctt0[1][1] ;
       rsro[2][1] = mcrr0[1][1] ;
//flow
       for(i=2; i<=jexm1-1; ++i) {
           delr = mcdefl[1][i-1] * convdr ;
           getIsenExp(mcmach[1][i-1], gamma,delr,2,i-1,i) ;
           mcprat[1][i] = prat[2][i] ;
           mctrat[1][i] = trat[2][i] ;
           mcrrat[1][i] = rhorat[2][i];
           mcpp0[1][i] = pspo[2][i] ;
           mctt0[1][i] = tsto[2][i] ;
           mcrr0[1][i] = rsro[2][i] ;
           mcmach[1][i] = mach2[2][i] ;
           getIsen(mcmach[1][i], gamma) ;
           mcmang[1][i] = mu ;
           mcpm[1][i] = nu ;
           mcdefl[1][i] = mcturn[1][i] - mcturn[1][i-1] ; 
       }
// expansion waves
       for(j=jexm1-1; j>=1; --j) {
          t1 = Math.tan(convdr*(mcmang[1][j] + mcturn[1][j])) ;
          t2 = -Math.tan(convdr*mcturn[1][j]) ;
          mcx[2][j] = (mcx[1][j]* t1 + mcy[2][j+1] + mcx[2][j+1]* t2 - mcy[1][j]) / (t2 + t1) ;
          mcy[2][j] = mcy[2][j+1] + (mcx[2][j+1] - mcx[2][j]) * t2;
       }
/*
       for(j=2; j<=jexm1-1; ++j) {
          t1 = Math.tan(convdr*(mcmang[1][j] + mcturn[1][j])) ;
          t2 = Math.tan(convdr*mcturn[1][j-1]) ;
          mcx[2][j] = (mcx[2][j-1]* t2 + mcy[2][j-1] - mcx[1][j]* t1 + mcy[1][j]) / (t2 - t1) ;
          mcy[2][j] = mcy[1][j] + (mcx[2][j] - mcx[1][j]) * t1;
       }
*/
// internal cowl geometry
       for(j=1; j<=jexm1; ++j) { 
          icxnd[j] = mcx[2][j] ;
          icynd[j] = mcy[2][j] ;
          icxbgn[j+1] = icxnd[j] ;
          icybgn[j+1] = icynd[j] ;          
       }
       icxbgn[1] = wxbgn[1] ;
       icybgn[1] = icybgn[2] + .1 ;

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = pto * mcpp0[i][j] ;
            mctemp[i][j] = tto * mctt0[i][j] ;
         }
       }          
       return ;
  }

   public void anlPlugED() {     //  Design for External Plug Nozzle
// find the shape of the plug
       double delmach,xstat,ystat,mstat,mu1,thet1,dee,phiex ;
       int i, j, k ;

       jetinit = 0 ;
       loadZero() ;

// last ray
       getIsen(machend,gamma) ;
       nuexit = nu ;
       muexit = mu;
       mcturn[1][numray] = 0.0 ;
       mcpm[1][numray] = nuexit ;
       mcmang[1][numray] = muexit ;  
       mcmach[1][numray] = machend ;
       mcx[1][numray] = 1.0 / Math.tan(muexit * convdr) ;
       mcy[1][numray] = 0.0 ;
       mcx[2][numray] = 0.0 ;
       mcy[2][numray] = 1.0 ;

// beginning of exit variables
       if (prob == 4) {   //  exit = throat - choked - jexplug = 1
           jexplug = 1 ;
           mcmach[1][jexplug] = 1.0 ;
       }
       if (prob == 5) {  // exit = mex   jexplug = numray / 2 
           jexplug = (numray/2) ;
  //         perint = .2 ;
           phiex = nuexit * (perint) ;
           getMachpm(phiex,gamma) ;
           mex = machpm ;
           mcmach[1][jexplug] = mex;
       }
       jexm1 = jexplug -1 ;
       getIsen(mcmach[1][jexplug], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcturn[1][jexm1] = nuexit ;
       mcpp0[1][jexm1] = poverpt ;
       mctt0[1][jexm1] = tovertt ;
       mcrr0[1][jexm1] = roverrt ;
       mcprat[1][jexplug] = 1.0 ;
       mctrat[1][jexplug] = 1.0 ;
       mcrrat[1][jexplug] = 1.0 ;
       mcarat[1][jexplug] = arat ;
       mcpm[1][jexplug] = nu ;
       mcmang[1][jexplug] = mu ;
       mcdefl[1][jexplug] = 0.0 ;
       mcturn[1][jexplug] = nuexit ;  

       delmach = (machend-mcmach[1][jexplug]) /(numray-jexplug)  ;
//flow
       for (j=jexplug; j<=numray; ++j) {
          k = j - jexplug ;
          mstat = k * delmach + mcmach[1][jexplug] ;
          getIsen(mstat,gamma) ;
          mcmach[1][j] = mstat;
          mcmang[1][j] = mu ;
          mcpm[1][j] = nu ;
          mcturn[1][j] = nuexit - mcpm[1][j] ;
          mcdefl[1][j] = mcturn[1][j] - mcturn[1][j-1] ;
          mcpp0[1][j] = poverpt ;
          mctt0[1][j] = tovertt ;
          mcrr0[1][j] = roverrt ;
          mcarat[1][j] = arat ;
          mcprat[1][j] = mcpp0[1][j] / mcpp0[1][j-1] ;
          mctrat[1][j] = mctt0[1][j] / mctt0[1][j-1] ;
          mcrrat[1][j] = mcrr0[1][j] / mcrr0[1][j-1] ;          
       }

//plug geometry
       ystat = 1.0 ;
       xstat = mcx[1][numray] ;
       wxnd[numray] = xstat ;
       wynd[numray] = mcy[1][numray] ;
       for (j=numray-1; j>=jexplug; --j) {
          thet1 = mcturn[1][j] ;
          mu1 = mcmang[1][j] + thet1 ;
          thet1 = convdr * thet1;
          mu1 = convdr * mu1 ;
          dee = (ystat - xstat*Math.tan(mu1))/(Math.tan(thet1) - Math.tan(mu1)) ;
          xstat = xstat - dee ;
          ystat = xstat * Math.tan(mu1) ;
          mcx[1][j] = xstat ;
          mcy[1][j] = 1.0 - ystat ;
          wxnd[j] = mcx[1][j] ;
          wynd[j] = mcy[1][j] ;
       }
       mcx[1][jexm1] = -nzlg ;
       mcy[1][jexm1] = mcy[1][jexplug] ;
       wxnd[jexm1] = mcx[1][jexm1] ;
       wynd[jexm1] = mcy[1][jexm1] ;

       for(i=jexplug; i<=numray; ++i) {
          wxbgn[i] = wxnd[i-1] ;
          wybgn[i] = wynd[i-1] ;
       }

       wxbgn[numray + 1] = wxnd[numray] ;
       wybgn[numray + 1] = wynd[numray] ;
       wxnd[numray + 1]  = wxbgn[numray+1] ;
       wynd[numray + 1]  = wybgn[numray+1] ;
       wxbgn[jexm1] = -nzlg ;
       wybgn[jexm1] = wybgn[jexplug] ;
       wybgn[1] = 0.0 ;

// internal cowl geometry
       icxnd[2] = 0.0 ;
       icynd[2] = 1.0 ;
       icxbgn[2] = mcx[1][jexplug] ;
       icybgn[2] = 1.0 - (icxbgn[2] * Math.tan(convdr*mcturn[1][jexplug])) ;
       if (icxbgn[2] > icxnd[2]) icxbgn[2] = icxnd[2] ;
       if (icybgn[2] < icynd[2]) icybgn[2] = icynd[2] ;
       icxnd[1] = icxbgn[2] ;
       icynd[1] = icybgn[2] ;
       icxbgn[1] = -nzlg ;
       icybgn[1] = icynd[1] ;

//  end of expansion rays

       for(i=jexm1; i<=numray; ++i) {
         mcx[2][i] = 0.0 ;
         mcy[2][i] = 1.0 ;
       }

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = pto * mcpp0[i][j] ;
            mctemp[i][j] = tto * mctt0[i][j] ;
         }
       }

       xexit = 0.0 ;
       yexit = nzht;
       xpexit = nzht*wxnd[numray] ;
       ypexit = nzht*wynd[numray] ;
       numd = numray ;
   }

   public void anlNozIV() {     //  Analysis for 2D Wedge design by points
// shape is given .. find the flowfield
       int i, j, k, grp, grptest;
       int ip,jp,kp,np,nps ;
       double dgroup ;
       boolean GO;

       jetinit = 0 ;
       loadZero() ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       aastr = arat ;
       dgroup = nuexit / ang2 ;
       grptest = (int) (dgroup) ;

//  wall geometry
       icxnd[1]  = 0.0 ;
       icynd[1]  = 1.0 ;
       icxbgn[1] = -nzlg ;
       icybgn[1] = icynd[1] ;

       yexit = nzht*aastr ;
       if (Math.abs(ang2) > .0001) {
           xexit = nzht*((aastr-1.0) / Math.tan(convdr*ang2)) ;
       }
       else {
           xexit = nzht * 500000. ;
       }

       icxbgn[2] = icxnd[1] ;
       icybgn[2] = icynd[1] ;
       icxnd[2] = xexit / nzht  ;
       icynd[2] = yexit / nzht ;

// external cowl geometry
       exthk = icxnd[2]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[2] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[2] ;
       ecynd[2] = icynd[2] ;
       
// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

       delthet = 2.0 * ang2 / numray ;

// throat conditions
       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
       mcmach[0][0] = 1.0 ;
       getIsen(mcmach[0][0], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[0][0] = 1.0 ;
       mctt0[0][0] = 1.0 ;
       mcrr0[0][0] = 1.0 ;
       mcprat[0][0] = 1.0 ;
       mctrat[0][0] = 1.0 ;
       mcrrat[0][0] = 1.0 ;
       mcarat[0][0] = arat ;
// analysis by points 

//  expansion from the cowl 
       mcdefl[1][1] = delthet ;
       mcturn[1][1] = delthet ;
       mcpm[1][1]   = mcturn[1][1] ;
       mcQ[1][1] = mcpm[1][1] + mcturn[1][1] ;
       mcR[1][1] = mcpm[1][1] - mcturn[1][1] ;
       getMOCVar(1,1,0,0) ;
       mcx[1][1] = 0.0 ;
       mcy[1][1] = icybgn[1] ;
       for(i=2; i<=numray/2; ++i) {
          mcdefl[i][1] = delthet ;
          mcturn[i][1] = mcturn[i-1][1] + mcdefl[i][1] ;
          mcpm[i][1]   = mcturn[i][1] ;
          mcQ[i][1] = mcpm[i][1] + mcturn[i][1] ;
          mcR[i][1] = mcpm[i][1] - mcturn[i][1] ;
          getMOCVar(i,1,i-1,1) ;
          mcx[i][1] = mcx[i-1][1] + delx * icybgn[1] ;
          mcy[i][1] = mcy[i-1][1] + 
                (mcx[i][1]-mcx[i-1][1])*Math.tan(convdr*mcturn[i-1][1]);
       }
// complex region near centerline
       j = 1 ;
       mcturn[j][j+1] = 0.0 ;
       mcdefl[j][j+1] = mcturn[j][j+1] - mcturn[j][j] ;
       mcQ[j][j+1] = mcQ[j][j] ;
       mcR[j][j+1] = mcQ[j][j+1] ;
       mcpm[j][j+1]= mcQ[j][j+1] ;
       getMOCVar(j,j+1,j,j) ;
       mcy[j][j+1] = 0.0;
       mcx[j][j+1] = mcx[j][j] + 
          (mcy[j][j]-mcy[j][j+1]) / Math.tan(convdr*(mcmang[j][j]-mcturn[j][j])) ;

       for(i=j+1; i<=numray/2; ++i) {
          for(k=j+1; k<=i; ++k) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             getMOCVar(i,k,i,k-1) ;
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
          }
//  plane of symmetry
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          mcQ[i][i+1] = mcQ[i][i] ;
          mcR[i][i+1] = mcQ[i][i+1] ;
          mcpm[i][i+1]= mcQ[i][i+1] ;
          getMOCVar(i,i+1,i,i) ;
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
       }

// groups .. bounce off cowl then back onto centerline .. two complex regions per group
   grp = 0;
   GO = true ;
   while(GO) {
      grp = grp + 1;
 // cowl surface
      for(j= (grp-1)*(numray/2) + 1; j<= grp*(numray/2); ++j) {
          i = numray/2 + j ;
          mcturn[i][j+1] = mcturn[i-1][j] ;
          mcdefl[i][j+1] = mcturn[i][j+1] - mcturn[i-1][j+1] ;
          mcR[i][j+1] = mcR[i-1][j+1] ;
          mcpm[i][j+1] = mcR[i][j+1] + mcturn[i][j+1] ;
          mcQ[i][j+1] = mcpm[i][j+1] + mcturn[i][j+1] ;
          getMOCVar(i,j+1,i-1,j+1) ;
          mcx[i][j+1] = (mcy[i-1][j] - mcy[i-1][j+1] +
                    mcx[i-1][j+1]*Math.tan(convdr*(mcmang[i-1][j+1]+ mcturn[i-1][j+1])) -
                    mcx[i-1][j]*Math.tan(convdr*mcturn[i-1][j]) )/
             (Math.tan(convdr*(mcmang[i-1][j+1]+mcturn[i-1][j+1])) - Math.tan(convdr*mcturn[i-1][j])) ;
          mcy[i][j+1] = mcy[i-1][j] + (mcx[i][j+1]-mcx[i-1][j])*Math.tan(convdr*mcturn[i-1][j]) ;

// complex region near cowl
          for(k=j+2; k<=i; ++k) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             getMOCVar(i,k,i,k-1) ;
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
          }

          mcturn[i][j] = mcturn[i][j+1] ;
          mcdefl[i][j] = mcdefl[i][j+1] ;
          mcR[i][j] = mcR[i][j+1] ;
          mcpm[i][j] = mcpm[i][j+1] ;
          mcQ[i][j] = mcQ[i][j+1] ;
          mcx[i][j] = mcx[i][j+1] ;
          mcy[i][j] = mcy[i][j+1] ;
          mcmach[i][j] = mcmach[i][j+1] ;
          getIsen(mcmach[i][j], gamma) ;
          mcmang[i][j] = mcmang[i][j+1];
          mcpp0[i][j] = mcpp0[i][j+1] ;
          mctt0[i][j] = mctt0[i][j+1] ;
          mcrr0[i][j] = mcrr0[i][j+1] ;
          mcarat[i][j] = 1.0 ;
          mcprat[i][j] = 1.0 ;
          mctrat[i][j] = 1.0 ;
          mcrrat[i][j] = 1.0 ;
      }
// complex region near centerline
       j = grp*(numray/2) + 1 ;
       mcturn[j][j+1] = 0.0 ;
       mcdefl[j][j+1] = mcturn[j][j+1] - mcturn[j][j] ;
       mcQ[j][j+1] = mcQ[j][j] ;
       mcR[j][j+1] = mcQ[j][j+1] ;
       mcpm[j][j+1]= mcQ[j][j+1] ;
       getMOCVar(j,j+1,j,j) ;
       mcy[j][j+1] = 0.0;
       mcx[j][j+1] = mcx[j][j] + 
          (mcy[j][j]-mcy[j][j+1]) / Math.tan(convdr*(mcmang[j][j]-mcturn[j][j])) ;

       for(i=j+1; i<=(grp+1)*numray/2; ++i) {
          for(k=j+1; k<=i; ++k) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             getMOCVar(i,k,i,k-1) ;
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
          }

//  plane of symmetry
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          mcQ[i][i+1] = mcQ[i][i] ;
          mcR[i][i+1] = mcQ[i][i+1] ;
          mcpm[i][i+1]= mcQ[i][i+1] ;
          getMOCVar(i,i+1,i,i) ;
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
       }
       group = grp ;
       GO = false ;
       if(mcx[(grp+1)*(numray/2)][(grp)*(numray/2)+1] <= xexit/nzht) GO = true;
   }

// find last internal right running wave
       numd = numray/2 ;
       for (i=numray/2; i <=(group+1)*(numray/2); ++i) {
           numd = i-1 ;
           if(mcx[i][i+1-numray/2] >= xexit/nzht) break ;
       }

// find exit right running ray
       i = numd ;
       j = numd - numray/2 + 1 ;
       getQXit(i,j) ;
       mcturn[i+1][j] = mcturn[i][j] ;
       mcpm[i+1][j] = mcQ[i+1][j] - mcturn[i+1][j] ;
       mcR[i+1][j] = mcpm[i+1][j] - mcturn[i+1][j] ;
       mcdefl[i+1][j] = 0.0 ;
       mcx[i+1][j] = xexit/nzht ;
       mcy[i+1][j] = yexit/nzht ;
       getMOCVar(i+1,j,i,j) ;
       for(k=j+1; k<=i+1; ++k) {
         mcQ[i+1][k] = mcQ[i+1][k-1] ;
         mcR[i+1][k] = mcR[i][k] ;
         mcpm[i+1][k]   = .5*(mcQ[i+1][k] + mcR[i+1][k]) ;
         mcturn[i+1][k] = .5*(mcQ[i+1][k] - mcR[i+1][k]) ;
         mcdefl[i+1][k] = mcturn[i+1][k] - mcturn[i][k] ;
         getMOCVar(i+1,k,i+1,k-1) ;
         mcal[i+1][k-1] = (mcmang[i+1][k-1] - mcturn[i+1][k-1]) ;
         mcbe[i][k] = (mcmang[i][k] + mcturn[i][k]) ;
         mcx[i+1][k] = (mcy[i+1][k-1] - mcy[i][k] + 
            mcx[i+1][k-1] * Math.tan(convdr*mcal[i+1][k-1]) + 
            mcx[i][k] * Math.tan(convdr*mcbe[i][k]) )/
           (Math.tan(convdr*mcal[i+1][k-1]) + Math.tan(convdr*mcbe[i][k]) );
         mcy[i+1][k] = mcy[i+1][k-1] - 
           (mcx[i+1][k] - mcx[i+1][k-1])*Math.tan(convdr*mcal[i+1][k-1]);
      }
//  plane of symmetry
      mcturn[i+1][i+2] = 0.0 ;
      mcdefl[i+1][i+2] = mcturn[i+1][i+2] - mcturn[i+1][i+1] ;
      mcQ[i+1][i+2] = mcQ[i+1][i+1] ;
      mcR[i+1][i+2] = mcQ[i+1][i+2] ;
      mcpm[i+1][i+2]= mcQ[i+1][i+2] ;
      getMOCVar(i+1,i+2,i+1,i+1) ;
      mcy[i+1][i+2] = 0.0;
      mcx[i+1][i+2] = mcx[i+1][i+1] + 
        (mcy[i+1][i+1]-mcy[i+1][i+2]) / Math.tan(convdr*(mcmang[i+1][i+1]-mcturn[i+1][i+1])) ;

// output and plotting variables
       for(i=0; i<=254; ++i) {
         for(j=0; j<=254; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

//         determine nozzle pressure ratio at cowl exit 
       nprat = mcpres[numd+1][numd - numray/2 + 1] / ps0 ;

//  plume into static flow
       if(plumopt == 1) {
         i = numd + 2 ;
         j = numd - numray/2 + 1 ;
         if(nprat >= 1.0) {
// expansion
// last right running ray of expansion
           getPresBC(i,j,ps0) ;
           mcx[i][j] = xexit/nzht ;
           mcy[i][j] = yexit/nzht ;  
           for(k=j+1; k<=i; ++k) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             getMOCVar(i,k,i,k-1) ;
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
               (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
          }
//  plane of symmetry
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          mcQ[i][i+1] = mcQ[i][i] ;
          mcR[i][i+1] = mcQ[i][i+1] ;
          mcpm[i][i+1]= mcQ[i][i+1] ;
          getMOCVar(i,i+1,i,i) ;
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
         }

         if(nprat < 1.0) {
 //  compression wave
 //  re-compute first right running ray
           getPresBC(i-1,j,ps0) ;
           mcx[i-1][j] = xexit/nzht ;
           mcy[i-1][j] = yexit/nzht ;  
           for(k=j+1; k<=i-1; ++k) {
             mcQ[i-1][k] = mcQ[i-1][k-1] ;
             mcR[i-1][k] = mcR[i-2][k] ;
             mcpm[i-1][k]   = .5*(mcQ[i-1][k] + mcR[i-1][k]) ;
             mcturn[i-1][k] = .5*(mcQ[i-1][k] - mcR[i-1][k]) ;
             mcdefl[i-1][k] = mcturn[i-1][k] - mcturn[i-2][k] ;
             getMOCVar(i-1,k,i-1,k-1) ;
             mcal[i-1][k-1] = (mcmang[i-1][k-1] - mcturn[i-1][k-1]) ;
             mcbe[i-2][k] = (mcmang[i-2][k] + mcturn[i-2][k]) ;
             mcx[i-1][k] = (mcy[i-1][k-1] - mcy[i-2][k] + 
                mcx[i-1][k-1] * Math.tan(convdr*mcal[i-1][k-1]) + 
                mcx[i-2][k] * Math.tan(convdr*mcbe[i-2][k]) )/
               (Math.tan(convdr*mcal[i-1][k-1]) + Math.tan(convdr*mcbe[i-2][k]) );
             mcy[i-1][k] = mcy[i-1][k-1] - 
               (mcx[i-1][k] - mcx[i-1][k-1])*Math.tan(convdr*mcal[i-1][k-1]);
          }
//  plane of symmetry
          mcturn[i-1][i] = 0.0 ;
          mcdefl[i-1][i] = mcturn[i-1][i] - mcturn[i-1][i-1] ;
          mcQ[i-1][i] = mcQ[i-1][i-1] ;
          mcR[i-1][i] = mcQ[i-1][i] ;
          mcpm[i-1][i]= mcQ[i-1][i] ;
          getMOCVar(i-1,i,i-1,i-1) ;
          mcy[i-1][i] = 0.0;
          mcx[i-1][i] = mcx[i-1][i-1] + 
             (mcy[i-1][i-1]-mcy[i-1][i]) / Math.tan(convdr*(mcmang[i-1][i-1]-mcturn[i-1][i-1])) ;

 // check for crossing left running characteristic 
          nps = 1;
          for(k=j+1; k<=i-1; ++k) {
             nps = 1 ; 
             for(np=i-2; np>=j+1; --np) {
               if(mcx[i-1][k] < mcx[np][k]) {
                  nps = np ;
               }
             }
             if(nps > 1) {
                for(np=nps; np<=i-2; ++np) {
                   mcQ[np][k] = mcQ[i-1][k] ;
                   mcR[np][k] = mcR[i-1][k] ;
                   mcpm[np][k] = mcpm[i-1][k] ;
                   mcturn[np][k] = mcturn[i-1][k] ;
                   mcdefl[np][k]= mcdefl[i-1][k] ;
                   getMOCVar(np,k,np,k-1) ;
                   mcx[np][k] = mcx[i-1][k] ;
                   mcy[np][k] = mcy[i-1][k] ;
                }
             }
             if(mcy[i-1][k] < 0.0) {
//  plane of symmetry
               mcturn[i-1][k] = 0.0 ;
               mcy[i-1][k] = 0.0;
               mcx[i-1][k] = mcx[i-1][k-1] + 
                 (mcy[i-1][k-1]-mcy[i-1][k]) / Math.tan(convdr*(mcmang[i-1][k-1]-mcturn[i-1][k-1])) ;
               mcdefl[i-1][k] = mcdefl[i-1][k-1] ;
               mcQ[i-1][k] = mcQ[i-1][k-1] ;
               mcR[i-1][k] = mcR[i-1][k-1] ;
               mcpm[i-1][k]= mcpm[i-1][k-1] ;
               getMOCVar(i-1,k,i-1,k-1) ;
             }
          }
 //  copy last right running ray from first rrr
           for(k=j; k<=i; ++k) {
             mcQ[i][k] = mcQ[i-1][k] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k] = mcpm[i-1][k] ;
             mcturn[i][k] = mcturn[i-1][k] ;
             mcdefl[i][k] = mcdefl[i-1][k] ;
             getMOCVar(i,k,i-1,k) ;
             mcx[i][k] = mcx[i-1][k] ; 
             mcy[i][k] = mcy[i-1][k] ; 
           }
           mcQ[i][i+1] = mcQ[i][i] ;
           mcR[i][i+1] = mcR[i][i] ;
           mcpm[i][i+1] = mcpm[i][i] ;
           mcturn[i][i+1] = mcturn[i][i] ;
           mcdefl[i][i+1] = mcdefl[i][i] ;
           getMOCVar(i,i+1,i,i) ;
           mcx[i][i+1] = mcx[i][i] ; 
           mcy[i][i+1] = mcy[i][i] ; 

         }  // end of compression wave test
//  last check for negative y values

          for(ip=numray/2 +1; ip<=numd; ++ ip) {
             for(jp=numray/2+1; jp<=numd+1; ++ jp) {
               if(mcy[ip][jp] < 0.0) {
                 mcturn[ip][jp] = 0.0 ;
                 mcy[ip][jp] = 0.0;
                 mcx[ip][jp] = mcx[ip][jp-1] + 
                   (mcy[ip][jp-1]-mcy[ip][jp]) / Math.tan(convdr*(mcmang[ip][jp-1]-mcturn[ip][jp-1])) ;
                 mcdefl[ip][jp] = mcdefl[ip+1][jp] ;
                 mcQ[ip][jp] = mcQ[ip+1][jp] ;
                 mcR[ip][jp] = mcR[ip+1][jp] ;
                 mcpm[ip][jp]= mcpm[ip+1][jp] ;
                 getMOCVar(ip,jp,ip+1,jp) ;
               }
             }
           }

         for(ip=0 ; ip<=numray; ++ip) {
//  free stream boundary
            getPresBC(i+1+ip,j+1+ip,ps0) ;
            mcal[i+ip][j+1+ip] = (mcmang[i+ip][j+1+ip] + mcturn[i+ip][j+1+ip]) ;
            mcbe[i+ip][j+ip] = mcturn[i+ip][j+ip] ;
            mcx[i+1+ip][j+1+ip] = (mcy[i+ip][j+ip] - mcy[i+ip][j+1+ip] + 
               mcx[i+ip][j+1+ip] * Math.tan(convdr*mcal[i+ip][j+1+ip]) - 
               mcx[i+ip][j+ip] * Math.tan(convdr*mcbe[i+ip][j+ip]) )/
              (Math.tan(convdr*mcal[i+ip][j+1+ip]) - Math.tan(convdr*mcbe[i+ip][j+ip]) );
            mcy[i+1+ip][j+1+ip] = mcy[i+ip][j+1+ip] + 
              (mcx[i+1+ip][j+1+ip] - mcx[i+ip][j+1+ip])*Math.tan(convdr*mcal[i+ip][j+1+ip]);
//  plume internal
            for(k=j+2+ip; k<=i+1+ip; ++k) {
               mcQ[i+1+ip][k] = mcQ[i+1+ip][k-1] ;
               mcR[i+1+ip][k] = mcR[i+ip][k] ;
               mcpm[i+1+ip][k]   = .5*(mcQ[i+1+ip][k] + mcR[i+1+ip][k]) ;
               mcturn[i+1+ip][k] = .5*(mcQ[i+1+ip][k] - mcR[i+1+ip][k]) ;
               mcdefl[i+1+ip][k] = mcturn[i+1+ip][k] - mcturn[i+ip][k] ;
               getMOCVar(i+1+ip,k,i+1+ip,k-1) ;
               mcal[i+1+ip][k-1] = (mcmang[i+1+ip][k-1] - mcturn[i+1+ip][k-1]) ;
               mcbe[i+ip][k] = (mcmang[i+ip][k] + mcturn[i+ip][k]) ;
               mcx[i+1+ip][k] = (mcy[i+1+ip][k-1] - mcy[i+ip][k] + 
                  mcx[i+1+ip][k-1] * Math.tan(convdr*mcal[i+1+ip][k-1]) + 
                  mcx[i+ip][k] * Math.tan(convdr*mcbe[i+ip][k]) )/
                 (Math.tan(convdr*mcal[i+1+ip][k-1]) + Math.tan(convdr*mcbe[i+ip][k]) );
               mcy[i+1+ip][k] = mcy[i+1+ip][k-1] - 
                 (mcx[i+1+ip][k] - mcx[i+1+ip][k-1])*Math.tan(convdr*mcal[i+1+ip][k-1]);
               checkCles(i+1+ip,k) ;
            }
//  plane of symmetry
            mcturn[i+1+ip][i+2+ip] = 0.0 ;
            mcdefl[i+1+ip][i+2+ip] = mcturn[i+1+ip][i+2+ip] - mcturn[i+1+ip][i+1+ip] ;
            mcQ[i+1+ip][i+2+ip] = mcQ[i+1+ip][i+1+ip] ;
            mcR[i+1+ip][i+2+ip] = mcQ[i+1+ip][i+2+ip] ;
            mcpm[i+1+ip][i+2+ip]= mcQ[i+1+ip][i+2+ip] ;
            getMOCVar(i+1+ip,i+2+ip,i+1+ip,i+1+ip) ;
            mcy[i+1+ip][i+2+ip] = 0.0;
            mcx[i+1+ip][i+2+ip] = mcx[i+1+ip][i+1+ip] + 
              (mcy[i+1+ip][i+1+ip]-mcy[i+1+ip][i+2+ip]) / Math.tan(convdr*(mcmang[i+1+ip][i+1+ip]-mcturn[i+1+ip][i+1+ip])) ;
          }

// output and plotting variables
          for(i=0; i<=254; ++i) {
            for(j=0; j<=254; ++j) {
              mcpres[i][j] = psth * mcpp0[i][j] ;
              mctemp[i][j] = tsth * mctt0[i][j] ;
            }
          }  
       }

// working here now !
// plume into supersonic flow
       if(plumopt == 2) {
       }

       return ;
   }

   public void anlNozV() {     //  Analysis for Axi Wedge design by points
// shape is given .. find the flowfield
       int i, j, k, grp, grptest;
       int ip,jp,kp,np,nps ;
       double dgroup ;
       double dell1,dell2 ;
       boolean GO;

       jetinit = 0 ;
       loadZero() ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       aastr = arat ;
       dgroup = nuexit / ang2 ;
       grptest = (int) (dgroup) ;

//  wall geometry
       icxnd[1]  = 0.0 ;
       icynd[1]  = 1.0 ;
       icxbgn[1] = -nzlg ;
       icybgn[1] = icynd[1] ;

       yexit = nzht*Math.sqrt(aastr) ;
       if (Math.abs(ang2) > .0001) {
           xexit = nzht*((Math.sqrt(aastr)-1.0) / Math.tan(convdr*ang2)) ;
       }
       else {
           xexit = nzht * 500000. ;
       }

       icxbgn[2] = icxnd[1] ;
       icybgn[2] = icynd[1] ;
       icxnd[2] = xexit / nzht  ;
       icynd[2] = yexit / nzht ;

// external cowl geometry
       exthk = icxnd[2]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[2] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[2] ;
       ecynd[2] = icynd[2] ;
       
// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

       delthet = 2.0 * ang2 / numray ;

// throat conditions
       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
       mcmach[0][0] = 1.0 ;
       getIsen(mcmach[0][0], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[0][0] = 1.0 ;
       mctt0[0][0] = 1.0 ;
       mcrr0[0][0] = 1.0 ;
       mcprat[0][0] = 1.0 ;
       mctrat[0][0] = 1.0 ;
       mcrrat[0][0] = 1.0 ;
       mcarat[0][0] = arat ;
// analysis by points 

//  expansion from the cowl 
       mcdefl[1][1] = delthet ;
       mcturn[1][1] = delthet ;
       mcpm[1][1]   = mcturn[1][1] ;
       mcQ[1][1] = 0.0 ;
       mcR[1][1] = 0.0 ;
       getMOCVar(1,1,0,0) ;
       mcx[1][1] = 0.0 ;
       mcy[1][1] = icybgn[1] ;

       for(i=2; i<=numray/2; ++i) {
          mcdefl[i][1] = delthet ;
          mcturn[i][1] = mcturn[i-1][1] + mcdefl[i][1] ;
          mcpm[i][1]   = mcturn[i][1] ;
          mcQ[i][1] = 0.0 ;
          mcR[i][1] = 0.0 ;
          getMOCVar(i,1,i-1,1) ;
          mcx[i][1] = mcx[i-1][1] + delx * icybgn[1] ;
          mcy[i][1] = mcy[i-1][1] + 
                (mcx[i][1]-mcx[i-1][1])*Math.tan(convdr*mcturn[i-1][1]);
       }
// complex region near centerline
       j = 1 ;
       mcy[j][j+1] = 0.0;
       mcx[j][j+1] = mcx[j][j] + 
          (mcy[j][j]-mcy[j][j+1]) / Math.tan(convdr*(mcmang[j][j]-mcturn[j][j])) ;
       dell1 = Math.sqrt(((mcx[j][j+1] - mcx[j][j]) * (mcx[j][j+1]-mcx[j][j])) 
               +  ((mcy[j][j+1] - mcy[j][j]) * (mcy[j][j+1]-mcy[j][j]))) ;
       mcturn[j][j+1] = 0.0 ;
       mcdefl[j][j+1] = mcturn[j][j+1] - mcturn[j][j] ;
       mcaxi1[j][j] = (dell1 * Math.sin(convdr*mcmang[j][j]) * 
              Math.sin(convdr*mcturn[j][j]) / mcy[j][j])/convdr ;
       mcpm[j][j+1]= mcpm[j][j] + mcturn[j][j] + mcaxi1[j][j] ;
       mcQ[j][j+1] = 0.0 ;
       mcR[j][j+1] = 0.0 ;
       getMOCVar(j,j+1,j,j) ;

       for(i=j+1; i<=numray/2; ++i) {
          for(k=j+1; k<=i; ++k) {
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
             dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
                +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
                +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
             mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
                Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;
             mcaxi2[i][k] = 0.0 ;
             if(mcy[i-1][k] > 0.0) {
                mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                    Math.sin(convdr*mcturn[i-1][k]) / mcy[i-1][k])/convdr ;
             }  
             mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                          + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
             mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                          + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             getMOCVar(i,k,i,k-1) ;
          }
//  plane of symmetry
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          dell1 = Math.sqrt(((mcx[i][i+1]-mcx[i][i]) * (mcx[i][i+1]-mcx[i][i])) 
                +  ((mcy[i][i+1]-mcy[i][i]) * (mcy[i][i+1]-mcy[i][i]))) ;
          mcaxi1[i][i] = (dell1 * Math.sin(convdr*mcmang[i][i]) * 
                Math.sin(convdr*mcturn[i][i]) / mcy[i][i])/convdr ;
          mcpm[i][i+1]= mcpm[i][i] + mcturn[i][i] + mcaxi1[i][i] ;
          mcQ[i][i+1] = 0.0 ;
          mcR[i][i+1] = 0.0 ;
          getMOCVar(i,i+1,i,i) ;
       }

// groups .. bounce off cowl then back onto centerline .. two complex regions per group
   grp = 0;
   GO = true ;
   while(GO) {
      grp = grp + 1;
      for(j= (grp-1)*(numray/2) + 1; j<= grp*(numray/2); ++j) {
 // cowl surface
          i = numray/2 + j ;
          mcx[i][j+1] = (mcy[i-1][j] - mcy[i-1][j+1] +
                    mcx[i-1][j+1]*Math.tan(convdr*(mcmang[i-1][j+1]+ mcturn[i-1][j+1])) -
                    mcx[i-1][j]*Math.tan(convdr*mcturn[i-1][j]) )/
             (Math.tan(convdr*(mcmang[i-1][j+1]+mcturn[i-1][j+1])) - Math.tan(convdr*mcturn[i-1][j])) ;
          mcy[i][j+1] = mcy[i-1][j] + (mcx[i][j+1]-mcx[i-1][j])*Math.tan(convdr*mcturn[i-1][j]) ;
          mcturn[i][j+1] = mcturn[i-1][j] ;
          mcdefl[i][j+1] = mcturn[i][j+1] - mcturn[i-1][j+1] ;
          dell2 = Math.sqrt(((mcx[i][j+1]-mcx[i-1][j+1]) * (mcx[i][j+1]-mcx[i-1][j+1])) 
                +  ((mcy[i][j+1]-mcy[i-1][j+1]) * (mcy[i][j+1]-mcy[i-1][j+1]))) ;
          mcaxi2[i][j+1] = 0.0 ;
/*
          if(mcy[i-1][j+1] > 0.0) {
            mcaxi2[i][j+1] = (dell2 * Math.sin(convdr*mcmang[i-1][j+1]) * 
                  Math.sin(convdr*mcturn[i-1][j+1]) / mcy[i-1][j+1])/convdr ;
          }
*/
          mcpm[i][j+1]= mcturn[i][j+1] + (mcpm[i-1][j+1] - mcturn[i-1][j+1]) + mcaxi2[i][j+1] ;
          mcR[i][j+1] = 0.0 ;
          mcQ[i][j+1] = 0.0 ;
          getMOCVar(i,j+1,i-1,j+1) ;
// first right running ray
          if (j == ((grp-1)*(numray/2) + 1)) {
           for(k=j+2; k<=i; ++k) {
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
             dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
                +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
                +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
             mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
                Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;

             if(mcy[i-1][k] > 0.0) {
                mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                    Math.sin(convdr*mcturn[i-1][k]) / mcy[i-1][k])/convdr ;
             }
             else {
                mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                    Math.sin(convdr*mcturn[i-1][k-1]) / mcy[i-1][k-1])/convdr ;
             }

             mcaxi2[i][k] = 0.0 ;
             mcaxi1[i][k] = 0.0 ;

             mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                          + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
             mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                          + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             getMOCVar(i,k,i,k-1) ;
           }
          }
 
          else{
// complex region near cowl
           for(k=j+2; k<=i; ++k) {
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
             dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
                +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
                +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
             mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
                Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;
             mcaxi2[i][k] = 0.0 ;
             if(mcy[i-1][k] > 0.0) {
                mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                    Math.sin(convdr*mcturn[i-1][k]) / mcy[i-1][k])/convdr ;
             }
             mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                          + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
             mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                          + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             getMOCVar(i,k,i,k-1) ;
           }
         }
          mcturn[i][j] = mcturn[i][j+1] ;
          mcdefl[i][j] = mcdefl[i][j+1] ;
          mcR[i][j] = mcR[i][j+1] ;
          mcpm[i][j] = mcpm[i][j+1] ;
          mcQ[i][j] = mcQ[i][j+1] ;
          mcx[i][j] = mcx[i][j+1] ;
          mcy[i][j] = mcy[i][j+1] ;
          mcmach[i][j] = mcmach[i][j+1] ;
          getIsen(mcmach[i][j], gamma) ;
          mcmang[i][j] = mcmang[i][j+1];
          mcpp0[i][j] = mcpp0[i][j+1] ;
          mctt0[i][j] = mctt0[i][j+1] ;
          mcrr0[i][j] = mcrr0[i][j+1] ;
          mcarat[i][j] = 1.0 ;
          mcprat[i][j] = 1.0 ;
          mctrat[i][j] = 1.0 ;
          mcrrat[i][j] = 1.0 ;
      }
// complex region near centerline
       j = grp*(numray/2) + 1 ;
       mcy[j][j+1] = 0.0;
       mcx[j][j+1] = mcx[j][j] + 
          (mcy[j][j]-mcy[j][j+1]) / Math.tan(convdr*(mcmang[j][j]-mcturn[j][j])) ;
       mcturn[j][j+1] = 0.0 ;
       mcdefl[j][j+1] = mcturn[j][j+1] - mcturn[j][j] ;
       dell1 = Math.sqrt(((mcx[j][j+1] - mcx[j][j]) * (mcx[j][j+1]-mcx[j][j])) 
               +  ((mcy[j][j+1] - mcy[j][j]) * (mcy[j][j+1]-mcy[j][j]))) ;
       mcaxi1[j][j] = (dell1 * Math.sin(convdr*mcmang[j][j]) * 
              Math.sin(convdr*mcturn[j][j]) / mcy[j][j])/convdr ;
       mcpm[j][j+1]= mcpm[j][j] + mcturn[j][j] + mcaxi1[j][j] ;
         mcpm[j][j+1]= mcpm[j][j] - mcturn[j][j] + mcaxi1[j][j] ;

       mcQ[j][j+1] = 0.0 ;
       mcR[j][j+1] = 0.0 ;
       getMOCVar(j,j+1,j,j) ;
       for(i=j+1; i<=(grp+1)*numray/2; ++i) {
          for(k=j+1; k<=i; ++k) {
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
             dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
                +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
                +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
             mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
                Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;
             mcaxi2[i][k] = 0.0 ;
             if(mcy[i-1][k] > 0.0) {
                mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                    Math.sin(convdr*mcturn[i-1][k]) / mcy[i-1][k])/convdr ;
             }
             mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                          + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
             mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                          + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             getMOCVar(i,k,i,k-1) ;
          }

//  plane of symmetry
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          dell1 = Math.sqrt(((mcx[i][i+1]-mcx[i][i]) * (mcx[i][i+1]-mcx[i][i])) 
                +  ((mcy[i][i+1]-mcy[i][i]) * (mcy[i][i+1]-mcy[i][i]))) ;
          mcaxi1[i][i] = (dell1 * Math.sin(convdr*mcmang[i][i]) * 
                Math.sin(convdr*mcturn[i][i]) / mcy[i][i])/convdr ;
          mcpm[i][i+1]= mcpm[i][i] + mcturn[i][i] + mcaxi1[i][i] ;
          mcQ[i][i+1] = 0.0 ;
          mcR[i][i+1] = 0.0 ;
          getMOCVar(i,i+1,i,i) ;
       }
       group = grp ;
       GO = false ;
       if(mcx[(grp+1)*(numray/2)][(grp)*(numray/2)+1] <= xexit/nzht) GO = true;
   }

// find last internal right running wave
       numd = numray/2 ;
       for (i=numray/2; i <=(group+1)*(numray/2); ++i) {
           numd = i-1 ;
           if(mcx[i][i+1-numray/2] >= xexit/nzht) break ;
       }

// find exit right running ray
       i = numd ;
       j = numd - numray/2 + 1 ;
       getQXit(i,j) ;
       mcturn[i+1][j] = mcturn[i][j] ;
       mcQ[i+1][j] = 0.0 ;
       mcR[i+1][j] = 0.0 ;
       mcdefl[i+1][j] = 0.0 ;
       mcx[i+1][j] = xexit/nzht ;
       mcy[i+1][j] = yexit/nzht ;
       getMOCVar(i+1,j,i,j) ;
       for(k=j+1; k<=i+1; ++k) {
         mcal[i+1][k-1] = (mcmang[i+1][k-1] - mcturn[i+1][k-1]) ;
         mcbe[i][k] = (mcmang[i][k] + mcturn[i][k]) ;
         mcx[i+1][k] = (mcy[i+1][k-1] - mcy[i][k] + 
            mcx[i+1][k-1] * Math.tan(convdr*mcal[i+1][k-1]) + 
            mcx[i][k] * Math.tan(convdr*mcbe[i][k]) )/
           (Math.tan(convdr*mcal[i+1][k-1]) + Math.tan(convdr*mcbe[i][k]) );
         mcy[i+1][k] = mcy[i+1][k-1] - 
           (mcx[i+1][k] - mcx[i+1][k-1])*Math.tan(convdr*mcal[i+1][k-1]);
         dell1 = Math.sqrt(((mcx[i+1][k]-mcx[i+1][k-1]) * (mcx[i+1][k]-mcx[i+1][k-1])) 
            +  ((mcy[i+1][k]-mcy[i+1][k-1]) * (mcy[i+1][k]-mcy[i+1][k-1]))) ;
         dell2 = Math.sqrt(((mcx[i+1][k]-mcx[i][k]) * (mcx[i+1][k]-mcx[i][k])) 
            +  ((mcy[i+1][k]-mcy[i][k]) * (mcy[i+1][k]-mcy[i][k]))) ;
         mcaxi1[i+1][k] = (dell1 * Math.sin(convdr*mcmang[i+1][k-1]) * 
            Math.sin(convdr*mcturn[i+1][k-1]) / mcy[i+1][k-1])/convdr ;
         mcaxi2[i+1][k] = 0.0 ;
         if(mcy[i][k] > 0.0) {
            mcaxi2[i+1][k] = (dell2 * Math.sin(convdr*mcmang[i][k]) * 
                Math.sin(convdr*mcturn[i][k]) / mcy[i][k])/convdr ;
         }  
         mcpm[i+1][k] = .5*(mcpm[i+1][k-1] + mcpm[i][k])  
                      + .5*(mcturn[i+1][k-1] - mcturn[i][k])
                      + .5*(mcaxi1[i+1][k] + mcaxi2[i+1][k]) ;
         mcturn[i+1][k] = .5*(mcpm[i+1][k-1] - mcpm[i][k]) 
                      + .5*(mcturn[i+1][k-1] + mcturn[i][k])
                      + .5*(mcaxi1[i+1][k] - mcaxi2[i+1][k]) ;
         mcdefl[i+1][k] = mcturn[i+1][k] - mcturn[i][k] ;
         mcQ[i+1][k] = 0.0 ;
         mcR[i+1][k] = 0.0 ;
         getMOCVar(i+1,k,i+1,k-1) ;
      }
//  plane of symmetry
      mcy[i+1][i+2] = 0.0;
      mcx[i+1][i+2] = mcx[i+1][i+1] + 
        (mcy[i+1][i+1]-mcy[i+1][i+2]) / Math.tan(convdr*(mcmang[i+1][i+1]-mcturn[i+1][i+1])) ;
      mcturn[i+1][i+2] = 0.0 ;
      mcdefl[i+1][i+2] = mcturn[i+1][i+2] - mcturn[i+1][i+1] ;
      dell1 = Math.sqrt(((mcx[i+1][i+2]-mcx[i+1][i+1]) * (mcx[i+1][i+2]-mcx[i+1][i+1])) 
            +  ((mcy[i+1][i+2]-mcy[i+1][i+1]) * (mcy[i+1][i+2]-mcy[i+1][i+1]))) ;
      mcaxi1[i+1][i+1] = (dell1 * Math.sin(convdr*mcmang[i+1][i+1]) * 
            Math.sin(convdr*mcturn[i+1][i+1]) / mcy[i+1][i+1])/convdr ;
      mcpm[i+1][i+2]= mcpm[i+1][i+1] + mcturn[i+1][i+1] + mcaxi1[i+1][i+1] ;
      mcQ[i+1][i+2] = 0.0 ;
      mcR[i+1][i+2] = 0.0 ;
      getMOCVar(i+1,i+2,i+1,i+1) ;

// output and plotting variables
       for(i=0; i<=254; ++i) {
         for(j=0; j<=254; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

// working here!!
//         determine nozzle pressure ratio at cowl exit 
       nprat = mcpres[numd+1][numd - numray/2 + 1] / ps0 ;

//  plume into static flow
       if(plumopt == 1) {
           i = numd + 2 ;
           j = numd - numray/2 + 1 ;
         if(nprat >= 1.0) {
// expansion
// last right running ray of expansion
           getPresBC(i,j,ps0) ;
           mcx[i][j] = xexit/nzht ;
           mcy[i][j] = yexit/nzht ;  
           for(k=j+1; k<=i; ++k) {
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
               (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
             dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
               +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
               +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
             mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
               Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;
             mcaxi2[i][k] = 0.0 ;
             mcaxi1[i][k] = 0.0 ;
             mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                         + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                         + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
             mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                         + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                         + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             getMOCVar(i,k,i,k-1) ;
          }
//  plane of symmetry
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          dell1 = Math.sqrt(((mcx[i][i+1]-mcx[i][i]) * (mcx[i][i+1]-mcx[i][i])) 
                +  ((mcy[i][i+1]-mcy[i][i]) * (mcy[i][i+1]-mcy[i][i]))) ;
          mcaxi1[i][i] = (dell1 * Math.sin(convdr*mcmang[i][i]) * 
                Math.sin(convdr*mcturn[i][i]) / mcy[i][i])/convdr ;
          mcaxi1[i][i] = 0.0 ;
          mcpm[i][i+1]= mcpm[i][i] + mcturn[i][i] + mcaxi1[i][i] ;
          mcQ[i][i+1] = 0.0 ;
          mcR[i][i+1] = 0.0 ;
          getMOCVar(i,i+1,i,i) ;
        }

        if(nprat < 1.0) {
 //  compression wave
 //  re-compute first right running ray
           getPresBC(i-1,j,ps0) ;
           mcx[i-1][j] = xexit/nzht ;
           mcy[i-1][j] = yexit/nzht ;  
           for(k=j+1; k<=i-1; ++k) {
             mcal[i-1][k-1] = (mcmang[i-1][k-1] - mcturn[i-1][k-1]) ;
             mcbe[i-2][k] = (mcmang[i-2][k] + mcturn[i-2][k]) ;
             mcx[i-1][k] = (mcy[i-1][k-1] - mcy[i-2][k] + 
                mcx[i-1][k-1] * Math.tan(convdr*mcal[i-1][k-1]) + 
                mcx[i-2][k] * Math.tan(convdr*mcbe[i-2][k]) )/
               (Math.tan(convdr*mcal[i-1][k-1]) + Math.tan(convdr*mcbe[i-2][k]) );
             mcy[i-1][k] = mcy[i-1][k-1] - 
               (mcx[i-1][k] - mcx[i-1][k-1])*Math.tan(convdr*mcal[i-1][k-1]);
             dell1 = Math.sqrt(((mcx[i-1][k]-mcx[i-1][k-1]) * (mcx[i-1][k]-mcx[i-1][k-1])) 
               +  ((mcy[i-1][k]-mcy[i-1][k-1]) * (mcy[i-1][k]-mcy[i-1][k-1]))) ;
             dell2 = Math.sqrt(((mcx[i-1][k]-mcx[i-2][k]) * (mcx[i-1][k]-mcx[i-2][k])) 
               +  ((mcy[i-1][k]-mcy[i-2][k]) * (mcy[i-1][k]-mcy[i-2][k]))) ;
             mcaxi1[i-1][k] = (dell1 * Math.sin(convdr*mcmang[i-1][k-1]) * 
               Math.sin(convdr*mcturn[i-1][k-1]) / mcy[i-1][k-1])/convdr ;
             mcaxi2[i-1][k] = 0.0 ;
             mcaxi1[i-1][k] = 0.0 ;
             mcpm[i-1][k]   = .5*(mcpm[i-1][k-1] + mcpm[i-2][k])  
                         + .5*(mcturn[i-1][k-1] - mcturn[i-2][k])
                         + .5*(mcaxi1[i-1][k] + mcaxi2[i-1][k]) ;
             mcturn[i-1][k] = .5*(mcpm[i-1][k-1] - mcpm[i-2][k]) 
                         + .5*(mcturn[i-1][k-1] + mcturn[i-2][k])
                         + .5*(mcaxi1[i-1][k] - mcaxi2[i-1][k]) ;
             mcdefl[i-1][k] = mcturn[i-1][k] - mcturn[i-2][k] ;
             mcQ[i-1][k] = 0.0 ;
             mcR[i-1][k] = 0.0 ;
             getMOCVar(i-1,k,i-1,k-1) ;
          }
//  plane of symmetry
          mcy[i-1][i] = 0.0;
          mcx[i-1][i] = mcx[i-1][i-1] + 
            (mcy[i-1][i-1]-mcy[i-1][i]) / Math.tan(convdr*(mcmang[i-1][i-1]-mcturn[i-1][i-1])) ;
          mcturn[i-1][i] = 0.0 ;
          mcdefl[i-1][i] = mcturn[i-1][i] - mcturn[i-1][i-1] ;
          dell1 = Math.sqrt(((mcx[i-1][i]-mcx[i-1][i-1]) * (mcx[i-1][i]-mcx[i-1][i-1])) 
                +  ((mcy[i-1][i]-mcy[i-1][i-1]) * (mcy[i-1][i]-mcy[i-1][i-1]))) ;
          mcaxi1[i-1][i-1] = (dell1 * Math.sin(convdr*mcmang[i-1][i-1]) * 
                Math.sin(convdr*mcturn[i-1][i-1]) / mcy[i-1][i-1])/convdr ;
          mcaxi1[i-1][i-1] = 0.0 ;
          mcpm[i-1][i]= mcpm[i-1][i-1] + mcturn[i-1][i-1] + mcaxi1[i-1][i-1] ;
          mcQ[i-1][i] = 0.0 ;
          mcR[i-1][i] = 0.0 ;
          getMOCVar(i-1,i,i-1,i-1) ;

 // check for crossing left running characteristic 
          nps = 1;
          for(k=j+1; k<=i-1; ++k) {
             nps = 1 ; 
             for(np=i-2; np>=j+1; --np) {
               if(mcx[i-1][k] < mcx[np][k]) {
                  nps = np ;
               }
             }
             if(nps > 1) {
                for(np=nps; np<=i-2; ++np) {
                   mcQ[np][k] = 0.0 ;
                   mcR[np][k] = 0.0 ;
                   mcpm[np][k] = mcpm[i-1][k] ;
                   mcturn[np][k] = mcturn[i-1][k] ;
                   mcdefl[np][k]= mcdefl[i-1][k] ;
                   getMOCVar(np,k,np,k-1) ;
                   mcx[np][k] = mcx[i-1][k] ;
                   mcy[np][k] = mcy[i-1][k] ;
                }
             }
             if(mcy[i-1][k] < 0.0) {
//  plane of symmetry
               mcturn[i-1][k] = 0.0 ;
               mcy[i-1][k] = 0.0;
               mcx[i-1][k] = mcx[i-1][k-1] + 
                 (mcy[i-1][k-1]-mcy[i-1][k]) / Math.tan(convdr*(mcmang[i-1][k-1]-mcturn[i-1][k-1])) ;
               mcdefl[i-1][k] = mcdefl[i-1][k-1] ;
               mcQ[i-1][k] = 0.0 ;
               mcR[i-1][k] = 0.0 ;
               mcpm[i-1][k]= mcpm[i-1][k-1] ;
               getMOCVar(i-1,k,i-1,k-1) ;
             }
          }
 //  copy last right running ray from first rrr
           for(k=j; k<=i; ++k) {
             mcQ[i][k] = 0.0 ;
             mcR[i][k] = 0.0 ;
             mcpm[i][k] = mcpm[i-1][k] ;
             mcturn[i][k] = mcturn[i-1][k] ;
             mcdefl[i][k] = mcdefl[i-1][k] ;
             getMOCVar(i,k,i-1,k) ;
             mcx[i][k] = mcx[i-1][k] ; 
             mcy[i][k] = mcy[i-1][k] ; 
           }
           mcQ[i][i+1] = 0.0 ;
           mcR[i][i+1] = 0.0 ;
           mcpm[i][i+1] = mcpm[i][i] ;
           mcturn[i][i+1] = mcturn[i][i] ;
           mcdefl[i][i+1] = mcdefl[i][i] ;
           getMOCVar(i,i+1,i,i) ;
           mcx[i][i+1] = mcx[i][i] ; 
           mcy[i][i+1] = mcy[i][i] ; 

         }  // end of compression wave test
//  last check for negative y values

          for(ip=numray/2 +1; ip<=numd; ++ ip) {
             for(jp=numray/2+1; jp<=numd+1; ++ jp) {
               if(mcy[ip][jp] < 0.0) {
                 mcturn[ip][jp] = 0.0 ;
                 mcy[ip][jp] = 0.0;
                 mcx[ip][jp] = mcx[ip][jp-1] + 
                   (mcy[ip][jp-1]-mcy[ip][jp]) / Math.tan(convdr*(mcmang[ip][jp-1]-mcturn[ip][jp-1])) ;
                 mcdefl[ip][jp] = mcdefl[ip+1][jp] ;
                 mcQ[ip][jp] = 0.0 ;
                 mcR[ip][jp] = 0.0 ;
                 mcpm[ip][jp]= mcpm[ip+1][jp] ;
                 getMOCVar(ip,jp,ip+1,jp) ;
               }
             }
           }

          for(ip=0 ; ip<=numray; ++ip) {
//  free stream boundary
            getPresBC(i+1+ip,j+1+ip,ps0) ;
            mcal[i+ip][j+1+ip] = (mcmang[i+ip][j+1+ip] + mcturn[i+ip][j+1+ip]) ;
            mcbe[i+ip][j+ip] = mcturn[i+ip][j+ip] ;
            mcx[i+1+ip][j+1+ip] = (mcy[i+ip][j+ip] - mcy[i+ip][j+1+ip] + 
               mcx[i+ip][j+1+ip] * Math.tan(convdr*mcal[i+ip][j+1+ip]) - 
               mcx[i+ip][j+ip] * Math.tan(convdr*mcbe[i+ip][j+ip]) )/
              (Math.tan(convdr*mcal[i+ip][j+1+ip]) - Math.tan(convdr*mcbe[i+ip][j+ip]) );
            mcy[i+1+ip][j+1+ip] = mcy[i+ip][j+1+ip] + 
              (mcx[i+1+ip][j+1+ip] - mcx[i+ip][j+1+ip])*Math.tan(convdr*mcal[i+ip][j+1+ip]);
//  plume internal
            for(k=j+2+ip; k<=i+1+ip; ++k) {
               mcal[i+1+ip][k-1] = (mcmang[i+1+ip][k-1] - mcturn[i+1+ip][k-1]) ;
               mcbe[i+ip][k] = (mcmang[i+ip][k] + mcturn[i+ip][k]) ;
               mcx[i+1+ip][k] = (mcy[i+1+ip][k-1] - mcy[i+ip][k] + 
                  mcx[i+1+ip][k-1] * Math.tan(convdr*mcal[i+1+ip][k-1]) + 
                  mcx[i+ip][k] * Math.tan(convdr*mcbe[i+ip][k]) )/
                 (Math.tan(convdr*mcal[i+1+ip][k-1]) + Math.tan(convdr*mcbe[i+ip][k]) );
               mcy[i+1+ip][k] = mcy[i+1+ip][k-1] - 
                 (mcx[i+1+ip][k] - mcx[i+1+ip][k-1])*Math.tan(convdr*mcal[i+1+ip][k-1]);
               dell1 = Math.sqrt(((mcx[i+1+ip][k]-mcx[i+1+ip][k-1]) * (mcx[i+1+ip][k]-mcx[i+1+ip][k-1])) 
                  +  ((mcy[i+1+ip][k]-mcy[i+1+ip][k-1]) * (mcy[i+1+ip][k]-mcy[i+1+ip][k-1]))) ;
               dell2 = Math.sqrt(((mcx[i+1+ip][k]-mcx[i+ip][k]) * (mcx[i+1+ip][k]-mcx[i+ip][k])) 
                  +  ((mcy[i+1+ip][k]-mcy[i+ip][k]) * (mcy[i+1+ip][k]-mcy[i+ip][k]))) ;
               mcaxi1[i+1+ip][k] = (dell1 * Math.sin(convdr*mcmang[i+1+ip][k-1]) * 
                  Math.sin(convdr*mcturn[i+1+ip][k-1]) / mcy[i+1+ip][k-1])/convdr ;
               mcaxi2[i+1+ip][k] = 0.0 ;
               mcaxi1[i+1+ip][k] = 0.0 ;
/*
               if(mcy[i+ip][k] > 0.0) {
                  mcaxi2[i+1+ip][k] = (dell2 * Math.sin(convdr*mcmang[i+ip][k]) * 
                    Math.sin(convdr*mcturn[i+ip][k]) / mcy[i+ip][k])/convdr ;
               }
*/
               mcpm[i+1+ip][k]   = .5*(mcpm[i+1+ip][k-1] + mcpm[i+ip][k])  
                            + .5*(mcturn[i+1+ip][k-1] - mcturn[i+ip][k])
                            + .5*(mcaxi1[i+1+ip][k] + mcaxi2[i+1+ip][k]) ;
               mcturn[i+1+ip][k] = .5*(mcpm[i+1+ip][k-1] - mcpm[i+ip][k]) 
                            + .5*(mcturn[i+1+ip][k-1] + mcturn[i+ip][k])
                            + .5*(mcaxi1[i+1+ip][k] - mcaxi2[i+1+ip][k]) ;
               mcdefl[i+1+ip][k] = mcturn[i+1+ip][k] - mcturn[i+ip][k] ;
               mcQ[i+1+ip][k] = 0.0 ;
               mcR[i+1+ip][k] = 0.0 ;
               getMOCVar(i+1+ip,k,i+1+ip,k-1) ;
               checkCles(i+1+ip,k) ;
            }
//  plane of symmetry            
            mcy[i+1+ip][i+2+ip] = 0.0;
            mcx[i+1+ip][i+2+ip] = mcx[i+1+ip][i+1+ip] + 
              (mcy[i+1+ip][i+1+ip]-mcy[i+1+ip][i+2+ip]) / Math.tan(convdr*(mcmang[i+1+ip][i+1+ip]-mcturn[i+1+ip][i+1+ip])) ;
            mcturn[i+1+ip][i+2+ip] = 0.0 ;
            mcdefl[i+1+ip][i+2+ip] = mcturn[i+1+ip][i+2+ip] - mcturn[i+1+ip][i+1+ip] ;

            mcdefl[i+1+ip][i+2+ip] = mcturn[i+1+ip][i+2+ip] - mcturn[i+1+ip][i+1+ip] ;
            dell1 = Math.sqrt(((mcx[i+1+ip][i+2+ip]-mcx[i+1+ip][i+1+ip]) * (mcx[i+1+ip][i+2+ip]-mcx[i+1+ip][i+1+ip])) 
                +  ((mcy[i+1+ip][i+2+ip]-mcy[i+1+ip][i+1+ip]) * (mcy[i+1+ip][i+2+ip]-mcy[i+1+ip][i+1+ip]))) ;
            mcaxi1[i+1+ip][i+1+ip] = (dell1 * Math.sin(convdr*mcmang[i+1+ip][i+1+ip]) * 
                Math.sin(convdr*mcturn[i+1+ip][i+1+ip]) / mcy[i+1+ip][i+1+ip])/convdr ;
            mcaxi1[i+1+ip][i+1+ip] = 0.0 ;
            mcpm[i+1+ip][i+2+ip]= mcpm[i+1+ip][i+1+ip] + mcturn[i+1+ip][i+1+ip] + mcaxi1[i+1+ip][i+1+ip] ;
            mcQ[i+1+ip][i+2+ip] = 0.0 ;
            mcR[i+1+ip][i+2+ip] = 0.0 ;
            getMOCVar(i+1+ip,i+2+ip,i+1+ip,i+1+ip) ;
          }

// output and plotting variables
          for(i=0; i<=254; ++i) {
            for(j=0; j<=254; ++j) {
              mcpres[i][j] = psth * mcpp0[i][j] ;
              mctemp[i][j] = tsth * mctt0[i][j] ;
            }
          }  
       }

// plume into supersonic flow
       if(plumopt == 2) {
       }

       return ;
   }

   public void checkCles(int i1, int j1) { // Function to check for coalescence of compression waves

       if (mcx[i1][j1] >= mcx[i1-1][j1]){
           if(mcx[i1][j1] >= mcx[i1][j1-1]){
               return ;
           }
           else {
              mcQ[i1][j1] = mcQ[i1][j1-1] ;
              mcR[i1][j1] = mcR[i1][j1-1] ;
              mcpm[i1][j1] = mcpm[i1][j1-1] ;
              mcturn[i1][j1] = mcturn[i1][j1-1] ;
              mcdefl[i1][j1] = mcdefl[i1][j1-1] ;
              mcx[i1][j1] = mcx[i1][j1-1] ;
              mcy[i1][j1] = mcy[i1][j1-1] ;
              getMOCVar(i1,j1,i1-1,j1-1) ;      
              return ;
           }
       }
       else {
           mcQ[i1][j1] = mcQ[i1-1][j1] ;
           mcR[i1][j1] = mcR[i1-1][j1] ;
           mcpm[i1][j1] = mcpm[i1-1][j1] ;
           mcturn[i1][j1] = mcturn[i1-1][j1] ;
           mcdefl[i1][j1] = mcdefl[i1-1][j1] ;
           mcx[i1][j1] = mcx[i1-1][j1] ;
           mcy[i1][j1] = mcy[i1-1][j1] ;
           getMOCVar(i1,j1,i1-1,j1-1) ;      
           return ;
       }
   }

   public void getQXit(int i1, int j1) { // Function to find Q at the exit for wedge nozzle
       double slope,intrcpt ;

       if(prob == 7 ) {
          slope = (mcQ[i1+1][j1+1] - mcQ[i1][j1])/(mcx[i1+1][j1+1] - mcx[i1][j1]) ;
          intrcpt = mcQ[i1][j1] - slope*mcx[i1][j1] ;
          mcQ[i1+1][j1] = slope*(xexit/nzht) + intrcpt;
          return ;
       }
       if(prob == 8) {
          slope = (mcpm[i1+1][j1+1] - mcpm[i1][j1])/(mcx[i1+1][j1+1] - mcx[i1][j1]) ;
          intrcpt = mcpm[i1][j1] - slope*mcx[i1][j1] ;
          mcpm[i1+1][j1] = slope*(xexit/nzht) + intrcpt;
          return ;
       }
   }

   public void getPresBC(int i1, int j1, double pres) { // Routine to find conditions on pressure boundary

      mcmach[i1][j1] = Math.sqrt(2.0*(Math.pow(pres/pto,-(gamma-1.0)/gamma)-1.0)/(gamma-1.0)) ;
      getIsen(mcmach[i1][j1],gamma) ;
      mcpm[i1][j1] = nu ;
      mcmang[i1][j1] = mu ;
      mcdefl[i1][j1] = mcpm[i1][j1] - mcpm[i1-1][j1] ;
      mcturn[i1][j1] = mcturn[i1-1][j1] + mcdefl[i1][j1] ;
      if(prob == 7) {
         mcQ[i1][j1] = mcpm[i1][j1] + mcturn[i1][j1] ;
         mcR[i1][j1] = mcR[i1-1][j1] ;
      }
      if(prob == 8 ) {
         mcQ[i1][j1] = 0.0 ;
         mcR[i1][j1] = 0.0 ;
      }
      getMOCVar(i1,j1,i1-1,j1) ;

      return ;
   }

   public void anlNozIII() {     //  Design for Axi Moc Nozzle design by points
//  find the shape of the nozzle
       double dell1, dell2 ;
       double machold,machnew,delold, deriv ;
       int counter ;
       int i, j, k;

       jetinit = 0 ;
       loadZero() ;

       icxbgn[1] = -nzlg ;
       icxnd[1]  = 0.0 ;
 //      icybgn[1] = .5;
 //      icynd[1]  = .5 ;
       icybgn[1] = 1.0 ;
       icynd[1]  = 1.0 ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       thetmx = nuexit / 2.0 ;
 //      delthet = nuexit / numray ;
       delthet = Math.atan(Math.sqrt(arat)-1.0)/convdr / numray ;
       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
       mcmach[0][0] = 1.0 ;
       getIsen(mcmach[0][0], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[0][0] = 1.0 ;
       mctt0[0][0] = 1.0 ;
       mcrr0[0][0] = 1.0 ;
       mcprat[0][0] = 1.0 ;
       mctrat[0][0] = 1.0 ;
       mcrrat[0][0] = 1.0 ;
       mcarat[0][0] = arat ;
// analysis by points 
// initialize R-K
       delold = delthet + 0.1 ;
       machold = machend + .05 ;
       machnew = machend ;
       counter = 0 ;
// R-K analysis
       while(Math.abs(machend-machold) > .001 && counter < 20) {
// 1-1  - solid boundary
          mcx[1][1] = 0.0 ;
          mcy[1][1] = icybgn[1] ;
          mcdefl[1][1] = delthet ;
          mcturn[1][1] = delthet ;
          mcpm[1][1]   = mcturn[1][1] ;
          getMOCVar(1,1,0,0) ;
// 1-2  - plane of symmetry
          mcy[1][2] = 0.0;
          mcx[1][2] = mcx[1][1] + 
             (mcy[1][1]-mcy[1][2]) / Math.tan(convdr*(mcmang[1][1]-mcturn[1][1])) ;
          dell1 = Math.sqrt(((mcx[1][2] - mcx[1][1]) * (mcx[1][2]-mcx[1][1])) 
                      +  ((mcy[1][2] - mcy[1][1]) * (mcy[1][2]-mcy[1][1]))) ;
          mcturn[1][2] = 0.0 ;
          mcdefl[1][2] = mcturn[1][2] - mcturn[1][1] ;
          mcaxi1[1][1] = (dell1 * Math.sin(convdr*mcmang[1][1]) * 
                 Math.sin(convdr*mcturn[1][1]) / mcy[1][1])/convdr ;
          mcpm[1][2]= mcpm[1][1] + mcturn[1][1] + mcaxi1[1][1] ;
          getMOCVar(1,2,1,1) ;

//  n-1 - expansion on the boundary
          for(i=2; i<=numray/2; ++i) {
             mcx[i][1] = mcx[i-1][1] + delx * icybgn[1] ;
             mcy[i][1] = mcy[i-1][1] + 
                (mcx[i][1]-mcx[i-1][1])*Math.tan(convdr*mcturn[i-1][1]);
             mcdefl[i][1] = delthet ;
             mcturn[i][1] = mcturn[i-1][1] + mcdefl[i][1] ;
             mcpm[i][1]   = mcturn[i][1] ;
             getMOCVar(i,1,i-1,1) ;
// internal points  
             for(k=2; k<=i; ++k) {
                mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
                mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
                mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                   mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                   mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                  (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
                mcy[i][k] = mcy[i][k-1] - 
                  (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
                dell1 = Math.sqrt(((mcx[i][k]-mcx[i][k-1]) * (mcx[i][k]-mcx[i][k-1])) 
                            +  ((mcy[i][k]-mcy[i][k-1]) * (mcy[i][k]-mcy[i][k-1]))) ;
                dell2 = Math.sqrt(((mcx[i][k]-mcx[i-1][k]) * (mcx[i][k]-mcx[i-1][k])) 
                            +  ((mcy[i][k]-mcy[i-1][k]) * (mcy[i][k]-mcy[i-1][k]))) ;
                mcaxi1[i][k] = (dell1 * Math.sin(convdr*mcmang[i][k-1]) * 
                       Math.sin(convdr*mcturn[i][k-1]) / mcy[i][k-1])/convdr ;
                mcaxi2[i][k] = 0.0 ;
                if(mcy[i-1][k] > 0.0) {
                   mcaxi2[i][k] = (dell2 * Math.sin(convdr*mcmang[i-1][k]) * 
                       Math.sin(convdr*mcturn[i-1][k]) / mcy[i-1][k])/convdr ;
                }  
                mcpm[i][k]   = .5*(mcpm[i][k-1] + mcpm[i-1][k])  
                          + .5*(mcturn[i][k-1] - mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] + mcaxi2[i][k]) ;
                mcturn[i][k] = .5*(mcpm[i][k-1] - mcpm[i-1][k]) 
                          + .5*(mcturn[i][k-1] + mcturn[i-1][k])
                          + .5*(mcaxi1[i][k] - mcaxi2[i][k]) ;
                mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
                getMOCVar(i,k,i,k-1) ;
             }

//  plane of symmetry
             mcy[i][i+1] = 0.0;
             mcx[i][i+1] = mcx[i][i] + 
               (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
             mcturn[i][i+1] = 0.0 ;
             mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
             dell1 = Math.sqrt(((mcx[i][i+1]-mcx[i][i]) * (mcx[i][i+1]-mcx[i][i])) 
                         +  ((mcy[i][i+1]-mcy[i][i]) * (mcy[i][i+1]-mcy[i][i]))) ;
             mcaxi1[i][i] = (dell1 * Math.sin(convdr*mcmang[i][i]) * 
                    Math.sin(convdr*mcturn[i][i]) / mcy[i][i])/convdr ;
             mcpm[i][i+1]= mcpm[i][i] + mcturn[i][i] + mcaxi1[i][i] ;
             getMOCVar(i,i+1,i,i) ;
             machnew = mcmach[i][i+1] ;
          }
          deriv = (machnew - machold)/(delthet - delold) ;
          machold = machnew ;
          delold = delthet ;
          delthet = delold + (machend - machold)/deriv ;
          counter = counter + 1 ;
       }  // end of R-K
 //  cancellation surface
       k = numray/2 + 1 ;
       mcdefl[k][1] = mcdefl[k-1][1] ;
       mcturn[k][1] = mcturn[k-1][1] ;
       mcpm[k][1] = mcpm[k-1][1] ;
       mcmach[k][1] = mcmach[k-1][1] ;
       getIsen(mcmach[k][1], gamma) ;
       mcmang[k][1] = mcmang[k-1][1];
       mcal[k][1] = mcal[k-1][1] ;
       mcbe[k][1] = mcbe[k-1][1] ;
       mcx[k][1] = mcx[k-1][1] ;
       mcy[k][1] = mcy[k-1][1] ; 
       mcpp0[k][1] = mcpp0[k-1][1] ;
       mctt0[k][1] = mctt0[k-1][1] ;
       mcrr0[k][1] = mcrr0[k-1][1] ;
       mcarat[k][1] = mcarat[k-1][1] ;
       mcprat[k][1] = mcprat[k-1][1] ;
       mctrat[k][1] = mctrat[k-1][1] ;
       mcrrat[k][1] = mcrrat[k-1][1] ;

       for(i=2; i<=k; ++i) {
          mcal[k][i-1] =  mcturn[k][i-1] ;
          mcbe[k-1][i] = (mcmang[k-1][i] + mcturn[k-1][i]) ;
          mcx[k][i] = (mcy[k-1][i] - mcy[k][i-1] + 
             mcx[k][i-1] * Math.tan(convdr*mcal[k][i-1]) - 
             mcx[k-1][i] * Math.tan(convdr*mcbe[k-1][i]) )/
             (Math.tan(convdr*mcal[k][i-1]) - Math.tan(convdr*mcbe[k-1][i]) );
          mcy[k][i] = mcy[k][i-1] + 
            (mcx[k][i] - mcx[k][i-1])*Math.tan(convdr*mcal[k][i-1]);
          mcdefl[k][i] = -delthet ;
          mcturn[k][i] = mcturn[k][i-1] + mcdefl[k][i] ;
          mcpm[k][i] = mcpm[k-1][i]  ;
          getMOCVar(k,i,k,i-1) ;
       }
//  internal cowl geometry
       for(i=2; i<=numray/2; ++i) {
          icxbgn[i] = icxnd[i-1] ;
          icybgn[i] = icynd[i-1] ;
          icxnd[i]  = mcx[i][1] ;
          icynd[i]  = mcy[i][1] ;
       }
       for(i=numray/2+1; i<=numray; ++i) {
          icxbgn[i] = icxnd[i-1] ;
          icybgn[i] = icynd[i-1] ;
          j = i - numray/2 + 1 ;
          icxnd[i]  = mcx[k][j] ;
          icynd[i]  = mcy[k][j] ;
       }
       icxbgn[numray + 1] = icxnd[numray] ;
       icybgn[numray + 1] = icynd[numray] ;
//       icxnd[i]  = icxbgn[numray+1] + (icxnd[numray] - icxbgn[numray]) ;
       icxnd[i]  = icxbgn[numray+1] ;
       icynd[i]  = icybgn[numray+1] ;

// external cowl geometry

       exthk = icxnd[numray+1]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[numray + 1] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[numray + 1] ;
       ecynd[2] = icynd[numray + 1] ;
       
       xexit = nzht*icxnd[numray + 1] ;
       yexit = nzht*icynd[numray + 1] ;

// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

       return ;
   }

   public void anlNozII() {     //  Design for 2D Moc Nozzle design by points
// find the shape of the nozzle
       int i, j, k;

       jetinit = 0 ;
       loadZero() ;

       icxbgn[1] = -nzlg ;
       icxnd[1]  = 0.0 ;
 //      icybgn[1] = .5;
 //      icynd[1]  = .5 ;
       icybgn[1] = 1.0 ;
       icynd[1]  = 1.0 ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       thetmx = nuexit / 2.0 ;
       delthet = nuexit / numray ;
       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
       mcmach[0][0] = 1.0 ;
       getIsen(mcmach[0][0], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[0][0] = 1.0 ;
       mctt0[0][0] = 1.0 ;
       mcrr0[0][0] = 1.0 ;
       mcprat[0][0] = 1.0 ;
       mctrat[0][0] = 1.0 ;
       mcrrat[0][0] = 1.0 ;
       mcarat[0][0] = arat ;

// analysis by points 
// 1-1  - solid boundary
       mcdefl[1][1] = delthet ;
       mcturn[1][1] = delthet ;
       mcpm[1][1]   = mcturn[1][1] ;
       mcQ[1][1] = mcpm[1][1] + mcturn[1][1] ;
       mcR[1][1] = mcpm[1][1] - mcturn[1][1] ;
       getMOCVar(1,1,0,0) ;
       mcx[1][1] = 0.0 ;
       mcy[1][1] = icybgn[1] ;
// 1-2  - plane of symmetry
       mcturn[1][2] = 0.0 ;
       mcdefl[1][2] = mcturn[1][2] - mcturn[1][1] ;
       mcQ[1][2] = mcQ[1][1] ;
       mcR[1][2] = mcQ[1][2] ;
       mcpm[1][2]= mcQ[1][2] ;
       getMOCVar(1,2,1,1) ;
       mcy[1][2] = 0.0;
       mcx[1][2] = mcx[1][1] + 
          (mcy[1][1]-mcy[1][2]) / Math.tan(convdr*(mcmang[1][1]-mcturn[1][1])) ;

//  n-1 - expansion on the boundary
       for(i=2; i<=numray/2; ++i) {
          mcdefl[i][1] = delthet ;
          mcturn[i][1] = mcturn[i-1][1] + mcdefl[i][1] ;
          mcpm[i][1]   = mcturn[i][1] ;
          mcQ[i][1] = mcpm[i][1] + mcturn[i][1] ;
          mcR[i][1] = mcpm[i][1] - mcturn[i][1] ;
          getMOCVar(i,1,i-1,1) ;
          mcx[i][1] = mcx[i-1][1] + delx * icybgn[1] ;
          mcy[i][1] = mcy[i-1][1] + 
                (mcx[i][1]-mcx[i-1][1])*Math.tan(convdr*mcturn[i-1][1]);
// internal points  
          for(k=2; k<=i; ++k) {
             mcQ[i][k] = mcQ[i][k-1] ;
             mcR[i][k] = mcR[i-1][k] ;
             mcpm[i][k]   = .5*(mcQ[i][k] + mcR[i][k]) ;
             mcturn[i][k] = .5*(mcQ[i][k] - mcR[i][k]) ;
             mcdefl[i][k] = mcturn[i][k] - mcturn[i-1][k] ;
             getMOCVar(i,k,i,k-1) ;
             mcal[i][k-1] = (mcmang[i][k-1] - mcturn[i][k-1]) ;
             mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
             mcx[i][k] = (mcy[i][k-1] - mcy[i-1][k] + 
                mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) + 
                mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
                (Math.tan(convdr*mcal[i][k-1]) + Math.tan(convdr*mcbe[i-1][k]) );
             mcy[i][k] = mcy[i][k-1] - 
               (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
          }

//  plane of symmetry
          mcturn[i][i+1] = 0.0 ;
          mcdefl[i][i+1] = mcturn[i][i+1] - mcturn[i][i] ;
          mcQ[i][i+1] = mcQ[i][i] ;
          mcR[i][i+1] = mcQ[i][i+1] ;
          mcpm[i][i+1]= mcQ[i][i+1] ;
          getMOCVar(i,i+1,i,i) ;
          mcy[i][i+1] = 0.0;
          mcx[i][i+1] = mcx[i][i] + 
            (mcy[i][i]-mcy[i][i+1]) / Math.tan(convdr*(mcmang[i][i]-mcturn[i][i])) ;
       }
 //  cancellation surface
       i = numray/2 + 1 ;
       mcdefl[i][1] = mcdefl[i-1][1] ;
       mcturn[i][1] = mcturn[i-1][1] ;
       mcR[i][1] = mcR[i-1][1] ;
       mcpm[i][1] = mcpm[i-1][1] ;
       mcQ[i][1] = mcQ[i-1][1] ;
       mcmach[i][1] = mcmach[i-1][1] ;
       getIsen(mcmach[i][1], gamma) ;
       mcmang[i][1] = mcmang[i-1][1];
       mcal[i][1] = mcal[i-1][1] ;
       mcbe[i][1] = mcbe[i-1][1] ;
       mcx[i][1] = mcx[i-1][1] ;
       mcy[i][1] = mcy[i-1][1] ; 
       mcpp0[i][1] = mcpp0[i-1][1] ;
       mctt0[i][1] = mctt0[i-1][1] ;
       mcrr0[i][1] = mcrr0[i-1][1] ;
       mcarat[i][1] = mcarat[i-1][1] ;
       mcprat[i][1] = mcprat[i-1][1] ;
       mctrat[i][1] = mctrat[i-1][1] ;
       mcrrat[i][1] = mcrrat[i-1][1] ;

       for(k=2; k<=i; ++k) {
          mcdefl[i][k] = -delthet ;
          mcturn[i][k] = mcturn[i][k-1] + mcdefl[i][k] ;
          mcR[i][k] = mcR[i-1][k] ;
          mcpm[i][k] = mcR[i][k] + mcturn[i][k] ;
          mcQ[i][k] = mcpm[i][k] + mcturn[i][k] ;
          getMOCVar(i,k,i,k-1) ;
          mcal[i][k-1] =  mcturn[i][k-1] ;
          mcbe[i-1][k] = (mcmang[i-1][k] + mcturn[i-1][k]) ;
          mcx[i][k] = (mcy[i-1][k] - mcy[i][k-1] + 
             mcx[i][k-1] * Math.tan(convdr*mcal[i][k-1]) - 
             mcx[i-1][k] * Math.tan(convdr*mcbe[i-1][k]) )/
             (Math.tan(convdr*mcal[i][k-1]) - Math.tan(convdr*mcbe[i-1][k]) );
          mcy[i][k] = mcy[i][k-1] + 
            (mcx[i][k] - mcx[i][k-1])*Math.tan(convdr*mcal[i][k-1]);
       }
//  wall geometry
       for(j=2; j<=numray/2; ++j) {
          icxbgn[j] = icxnd[j-1] ;
          icybgn[j] = icynd[j-1] ;
          icxnd[j]  = mcx[j][1] ;
          icynd[j]  = mcy[j][1] ;
       }
       for(j=numray/2+1; j<=numray; ++j) {
          icxbgn[j] = icxnd[j-1] ;
          icybgn[j] = icynd[j-1] ;
          k = j - numray/2 + 1 ;
          icxnd[j]  = mcx[i][k] ;
          icynd[j]  = mcy[i][k] ;
       }
       icxbgn[numray + 1] = icxnd[numray] ;
       icybgn[numray + 1] = icynd[numray] ;
//       icxnd[i]  = icxbgn[numray+1] + (icxnd[numray] - icxbgn[numray]) ;
       icxnd[j]  = icxbgn[numray+1] ;
       icynd[j]  = icybgn[numray+1] ;

// external cowl geometry

       exthk = icxnd[numray+1]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[numray + 1] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[numray + 1] ;
       ecynd[2] = icynd[numray + 1] ;
       
       xexit = nzht*icxnd[numray + 1] ;
       yexit = nzht*icynd[numray + 1] ;

// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

       return ;
   }

   public void getMOCVar(int irow, int icol, int upr, int upc) {     
  // Routine to compute MOC variables
       getMachpm(mcpm[irow][icol],gamma) ;
       mcmach[irow][icol] = machpm ;
       getIsen(mcmach[irow][icol], gamma) ;
       mcmang[irow][icol] = mu ;
       mcpp0[irow][icol] = poverpt * pref ;
       mctt0[irow][icol] = tovertt * tref ;
       mcrr0[irow][icol] = roverrt * rref ;
       mcarat[irow][icol] = arat ;
       mcprat[irow][icol] = mcpp0[irow][icol] / mcpp0[upr][upc] ;
       mctrat[irow][icol] = mctt0[irow][icol] / mctt0[upr][upc] ;
       mcrrat[irow][icol] = mcrr0[irow][icol] / mcrr0[upr][upc] ;

       return ;
   }

   public void anlNoz() {     //  Design for 2d Moc Nozzle Design - Field Method
// find the shape of the nozzle
       double nang1,nang2 ;
       int i, j, k ;

       jetinit = 0 ;
       loadZero() ;

       icxbgn[1] = -nzlg ;
       icxnd[1]  = 0.0 ;
//       icybgn[1] = .5;
//       icynd[1]  = .5;
       icybgn[1] = 1.0 ;
       icynd[1]  = 1.0 ;

       getDelmx() ;

       getIsen(machend,gamma) ;
       nuexit = nu ;
       thetmx = nuexit / 2.0 ;
       delthet = nuexit / numray ;
       dely = .5 * delx * Math.tan(convdr * delthet) ;
       mcpm[0][0] = 0.0 ;
       mcmang[0][0] = 90.0 ;  
//   solve for all the flows     
// 1-1  - initial
       mcmach[1][1] = 1.0 ;
       mcdefl[1][1] = 0.0 ;
       mcturn[1][1] = 0.0 ;
       mcpm[1][1]   = 0.0 ;
       mcmang[1][1] = 90.0 ;
       getIsen(mcmach[1][1], gamma) ;
       pref = 1.0 / poverpt ;
       tref = 1.0 / tovertt ;
       rref = 1.0 / roverrt ;
       mcpp0[1][1] = 1.0 ;
       mctt0[1][1] = 1.0 ;
       mcrr0[1][1] = 1.0 ;
       mcprat[1][1] = 1.0 ;
       mctrat[1][1] = 1.0 ;
       mcrrat[1][1] = 1.0 ;
       mcarat[1][1] = arat ;
//  1-n - expansion
       for(i=2; i<=numray/2+1; ++i) {
          mcdefl[1][i] = delthet ;
          mcturn[1][i] = mcturn[1][i-1] + mcdefl[1][i] ;
          mcpm[1][i]   = mcpm[1][i-1] + mcdefl[1][i] ;
          getMOCVar(1,i,1,i-1) ;
       }
// k- columns
       for(k=2; k<=numray/2+1; ++k) {
          mcdefl[k][k] = -delthet ;
          mcturn[k][k] = 0.0 ;
          mcpm[k][k]   = mcpm[k-1][k] - mcdefl[k][k] ;
          getMOCVar(k,k,k-1,k) ;
//  k-n - expansion
          if(k <= numray/2) {
             for(i=k+1; i<=numray/2 + 1; ++i) {
                mcdefl[k][i] =  delthet ;
                mcturn[k][i] = mcturn[k][i-1] + mcdefl[k][i] ;
                mcpm[k][i]   = mcpm[k][i-1] + mcdefl[k][i] ;
                getMOCVar(k,i,k,i-1) ;
             }
          }
       }
//  solve for geometry
//  moc grid
       mcxul[1][1] = 0.0 ;
       mcyul[1][1] = icybgn[1] ;
       mcxll[1][1] = 0.0 ;
       mcyll[1][1] = 0.0 ;
       mcxur[1][1] = mcxul[1][1] ;
       mcyur[1][1] = mcyul[1][1] ;
       nang1 = convdr*(mcmang[1][2]- mcturn[1][2]) ;
       mcxlr[1][1] = (mcyul[1][1]- mcyll[1][1])/ Math.tan(nang1) ;
       mcylr[1][1] = mcyll[1][1] ;

       for (i=2; i<=numray/2; ++ i) {
         mcxul[1][i] = mcxur[1][i-1];
         mcyul[1][i] = mcyur[1][i-1];
         mcxll[1][i] = mcxlr[1][i-1];
         mcyll[1][i] = mcylr[1][i-1];
         mcxur[1][i] = mcxul[1][i] + delx ;
         mcyur[1][i] = mcyul[1][i] + delx * Math.tan(convdr*mcturn[1][i]) ;
         nang1 = convdr*(mcmang[1][i+1] - mcturn[1][i+1]) ;
         nang2 = convdr*(mcmang[2][i] + mcturn[2][i]) ;
         mcxlr[1][i] = (mcyur[1][i]-mcyll[1][i]+
                 mcxur[1][i]*Math.tan(nang1)+ mcxll[1][i]*Math.tan(nang2))/
                 (Math.tan(nang1)+ Math.tan(nang2)) ;
         mcylr[1][i] = mcyll[1][i] + (mcxlr[1][i]-mcxll[1][i])*Math.tan(nang2);
       }

       mcxul[2][2] = mcxll[1][2];
       mcyul[2][2] = mcyll[1][2];
       mcxll[2][2] = mcxll[1][2];
       mcyll[2][2] = mcyll[1][2];
       mcxur[2][2] = mcxlr[1][2] ;
       mcyur[2][2] = mcylr[1][2] ;
       mcylr[2][2] = 0.0 ;
       nang1 = convdr*(mcmang[2][3] - mcturn[2][3]) ;
       mcxlr[2][2] = mcxur[2][2] + (mcyur[2][2] - mcylr[2][2])/Math.tan(nang1) ;

       for (k=3; k<=numray/2; ++ k) {
          for (i=k; i<=numray/2; ++ i) {
            mcxul[k-1][i] = mcxll[k-2][i];
            mcyul[k-1][i] = mcyll[k-2][i];
            mcxll[k-1][i] = mcxlr[k-1][i-1];
            mcyll[k-1][i] = mcylr[k-1][i-1];
            mcxur[k-1][i] = mcxlr[k-2][i];
            mcyur[k-1][i] = mcylr[k-2][i] ;
            nang1 = convdr*(mcmang[k-1][i+1] - mcturn[k-1][i+1]) ;
            nang2 = convdr*(mcmang[k][i] + mcturn[k][i]) ;
            mcxlr[k-1][i] = (mcyur[k-1][i]-mcyll[k-1][i]+
                 mcxur[k-1][i]*Math.tan(nang1)+ mcxll[k-1][i]*Math.tan(nang2))/
                      (Math.tan(nang2)+ Math.tan(nang1)) ;
            mcylr[k-1][i] = mcyll[k-1][i] + (mcxlr[k-1][i]-mcxll[k-1][i])*Math.tan(nang2);
          }

          mcxul[k][k] = mcxll[k-1][k];
          mcyul[k][k] = mcyll[k-1][k];
          mcxll[k][k] = mcxll[k-1][k];
          mcyll[k][k] = mcyll[k-1][k];
          mcxur[k][k] = mcxlr[k-1][k] ;
          mcyur[k][k] = mcylr[k-1][k] ;
          mcylr[k][k] = 0.0 ;
          nang1 = convdr*(mcmang[k][k+1] - mcturn[k][k+1]) ;
          mcxlr[k][k] = mcxur[k][k] + (mcyur[k][k] - mcylr[k][k])/Math.tan(nang1) ;
       }
  // triangle at 1 - numray/2 + 1
       mcxul[1][numray/2 + 1] = mcxur[1][numray/2];
       mcyul[1][numray/2 + 1] = mcyur[1][numray/2];
       mcxll[1][numray/2 + 1] = mcxlr[1][numray/2];
       mcyll[1][numray/2 + 1] = mcylr[1][numray/2];
       nang1 = convdr*(mcmang[2][numray/2+1] + mcturn[2][numray/2 + 1]) ;
  //     ang1 = convdr*(mcmang[1][numray/2+1] + mcturn[1][numray/2 + 1]) ;
       nang2 = convdr*mcturn[1][numray/2+1] ;
       mcxur[1][numray/2 + 1] = (mcyll[1][numray/2 + 1] - mcyul[1][numray/2+1] + 
          mcxul[1][numray/2 +1]*Math.tan(nang2) - mcxll[1][numray/2 +1]*Math.tan(nang1)) /
          (Math.tan(nang2)-Math.tan(nang1)) ;
       mcyur[1][numray/2 + 1] = mcyul[1][numray/2 + 1] + 
          (mcxur[1][numray/2 + 1]-mcxul[1][numray/2 + 1])*Math.tan(nang2) ; 
       mcxlr[1][numray/2 + 1] = mcxur[1][numray/2 + 1] ;
       mcylr[1][numray/2 + 1] = mcyur[1][numray/2 + 1];

       for (k=2; k<=numray/2; ++k) {
         mcxul[k][numray/2 + 1] = mcxur[k][numray/2];
         mcyul[k][numray/2 + 1] = mcyur[k][numray/2];
         mcxll[k][numray/2 + 1] = mcxlr[k][numray/2];
         mcyll[k][numray/2 + 1] = mcylr[k][numray/2];
         mcxur[k][numray/2 + 1] = mcxlr[k-1][numray/2 + 1] ;
         mcyur[k][numray/2 + 1] = mcylr[k-1][numray/2 + 1];
         nang1 = convdr*(mcmang[k+1][numray/2 + 1] + mcturn[k+1][numray/2+1]) ;
  //       ang1 = convdr*(mcmang[k][numray/2 + 1] + mcturn[k][numray/2+1]) ;
         nang2 = convdr*mcturn[k][numray/2 + 1] ;
         mcxlr[k][numray/2 + 1] = (mcyll[k][numray/2 + 1] - mcyur[k][numray/2+1] + 
           mcxur[k][numray/2 +1]*Math.tan(nang2) - mcxll[k][numray/2 +1]*Math.tan(nang1)) /
           (Math.tan(nang2)-Math.tan(nang1)) ;
         mcylr[k][numray/2 + 1] =  mcyur[k][numray/2 + 1] + 
          (mcxlr[k][numray/2 + 1]-mcxur[k][numray/2 + 1])*Math.tan(nang2) ; 
       }

  // final zone at numray/2 + 1 - numray/2 + 1
       mcxul[numray/2 + 1][numray/2 + 1] = mcxlr[numray/2][numray/2 + 1];
       mcyul[numray/2 + 1][numray/2 + 1] = mcylr[numray/2][numray/2 + 1];
       mcxll[numray/2 + 1][numray/2 + 1] = mcxll[numray/2][numray/2 + 1];
       mcyll[numray/2 + 1][numray/2 + 1] = mcyll[numray/2 + 1][numray/2 + 1];
 //      mcxur[numray/2 + 1][numray/2 + 1] = 2.0 * mcxlr[numray/2][numray/2 + 1] - 
 //                           mcxlr[numray/2 - 1][numray/2 + 1]  ;
       mcxur[numray/2 + 1][numray/2 + 1] = mcxul[numray/2 + 1][numray/2 + 1];
       mcyur[numray/2 + 1][numray/2 + 1] = mcylr[numray/2][numray/2 + 1] ; 
       mcxlr[numray/2 + 1][numray/2 + 1] = mcxur[numray/2 + 1][numray/2 + 1] ;
       mcylr[numray/2 + 1][numray/2 + 1] = mcyll[numray/2][numray/2 + 1];
//  internal cowl geometry
       for(i=2; i<=numray/2; ++i) {
          icxbgn[i] = icxnd[i-1] ;
          icybgn[i] = icynd[i-1] ;
          icxnd[i]  = mcxur[1][i] ;
          icynd[i]  = mcyur[1][i] ;
       }
       for(i=numray/2+1; i<=numray; ++i) {
          icxbgn[i] = icxnd[i-1] ;
          icybgn[i] = icynd[i-1] ;
          j = i - numray/2 + 1 ;
          icxnd[i]  = mcxur[j][k] ;
          icynd[i]  = mcyur[j][k] ;
       }
       j = numray/2 + 1 ;
       k = numray + 1 ;
       icxbgn[k] = icxnd[k-1] ;
       icybgn[k] = icynd[k-1] ;
       icxnd[i]  = mcxur[j][j] ;
       icynd[i]  = mcyur[j][j] ;

// external cowl geometry

       exthk = icxnd[numray+1]*Math.tan(convdr*ang1) ;
       ecxbgn[1] = icxbgn[1] ;
       ecybgn[1] = icynd[numray + 1] + exthk ;
       ecxbgn[2] = ecxnd[1] = icxnd[1] ;
       ecybgn[2] = ecynd[1] = ecybgn[1] ;
       ecxnd[2] = icxnd[numray + 1] ;
       ecynd[2] = icynd[numray + 1] ;
       
       xexit = nzht*icxnd[numray + 1] ;
       yexit = nzht*icynd[numray + 1] ;

// external expansion fan geometry
       for(i=1; i<=2; ++i) {
         exbgn[i] = ecxbgn[2] ;
         eybgn[i] = ecybgn[2] ;
         eynd[i] = eybgn[i] + exht ;
         exnd[i] = exbgn[i] + (exht / Math.tan(convdr*eslope[i])) ;
       }

// output and plotting variables
       for(i=0; i<=54; ++i) {
         for(j=0; j<=54; ++j) {
            mcpres[i][j] = psth * mcpp0[i][j] ;
            mctemp[i][j] = tsth * mctt0[i][j] ;
         }
       }

       return ;
   }

   public void getPlumrat() { 
  // Routine to compute matching conditions in zone 2 of the plume 

       double delp,pupper,plower,dflo,dfln,deriv,gamu,gaml,theta,delta ; 
       int counter ;   

       if (plumopt < 2) {
         plumrat = 1.0 ;
         return;
       }
      
       gamu = 1.4 ;
       gaml = gamma ;
       delp = 5.0;
       dflo = 0.0 ;
       dfln = .001 ;
       if (nprat < 1.0) dfln = -.001 ;
       plumrat = 1.0 ;
       counter = 0 ;
       pupper = plocal ;
       plower = pexit ;
       theta = getShkang(mlocal,dfln,gamu) ;
       delta = Math.abs(dfln) ;

       while(Math.abs(delp) >= .01 && counter < 500) {
          counter = counter + 1 ;

          if(nprat >= 1.0) { // underexpanded - shock up - expand down
             getIsenExp(machend,gaml,-dfln,2,1,2) ;
             plower = prat[2][2] * pexit ;
             theta = getShkang(mlocal,dfln,gamu) ;
             getOblq(mlocal,theta,gamu,1,1,2) ;
             pupper = prat[1][2] * plocal ;
             delp = plower - pupper; 
             deriv = Math.abs(delp /(dfln - dflo)) ;
             dflo = dfln ;
             dfln = dflo + delp / deriv ;             
          }
          if(nprat < 1.0) { // overexpanded - expand up - shock down
             delta = Math.abs(dfln) ;
             theta = getShkang(machend,delta,gaml) ;
             getOblq(machend,theta,gaml,2,1,2) ;
             plower = prat[2][2] * pexit ;
             getIsenExp(mlocal,gamu,-delta,1,1,2) ;
             pupper = prat[1][2] * plocal ;
             delp = plower - pupper; 
             deriv = Math.abs(delp /(dfln - dflo)) ;
             dflo = dfln ;
             dfln = dflo + delp / deriv ;          
          }

       }

       plumrat = pupper / plocal ;
       return ;
   }

   public void anlJet() {     //  Analysis for Jet exhaust - over or under-expanded
       double fac1, fac2 ;
       double xl1, xl2, xl3, yl1, yl2, xa1, xb1, xc1 ;
       double sinthet,gamu ;
       int k ;

       jetinit = 1 ;
       loadZero() ;
       
       if(prob == 3) {
          xexit = 1000.*delx;
          yexit = nzht / 2.0 ;

 // geometry
          wxbgn[1] = xexit ;
          wybgn[1] = yexit ;
          wxnd[1]  = xexit ;
          wynd[1]  = nzht*(1.0 + nzlg) ;

          wxbgn[2] = xexit;
          wybgn[2] = yexit ;
          wxnd[2]  = xexit -nzht*(1.0 + nzlg) ;
          wynd[2]  = yexit ;
       }

//  internal flow variables in zones 0 and 1
       getIsenRamp(mach2[2][0],gamma,2,0,0) ;
       mach1[2][1] = mach2[2][0] ;
       getIsenRamp(mach1[2][1],gamma,2,0,1) ;
       mach2[2][1] = mach1[2][1] ;
       getIsen(mach2[2][1],gamma) ;
       mang[2][1] = mu ;
       pm[2][1] = nu ;
       ppt[2][1] = poverpt ;
       ttt[2][1] = tovertt ;
       rrt[2][1] = roverrt ;
       ptrat[2][1] = 1.0 ;
       prat[2][1] = ppt[2][1] ;
       trat[2][1] = ttt[2][1] ;
       rhorat[2][1] = rrt[2][1] ;
       if(prob == 3) {   
          pspo[2][1] = 1.0 ;
          tsto[2][1] = 1.0;
          rsro[2][1] = 1.0 ;
          ptpto[2][1] = 1.0 ;
       }
       else {
          if (prob < 3) {
             k = numray/2 + 1 ;
             pspo[2][1] = mcpp0[k][k] ;
             tsto[2][1] = mctt0[k][k] ;
             rsro[2][1] = mcrr0[k][k] ;
             ptpto[2][1] = 1.0 ;
          }
          if (prob >= 4 && prob <= 5) {  // plug - cc lee
             k = numray;
             pspo[2][1] = mcpp0[1][k] ;
             tsto[2][1] = mctt0[1][k] ;
             rsro[2][1] = mcrr0[1][k] ;
             ptpto[2][1] = 1.0 ;
          }
          if (prob == 6) {   // plug - ideal
             k = numray/2 + 1;
             pspo[2][1] = mcpp0[k][k] ;
             tsto[2][1] = mctt0[k][k] ;
             rsro[2][1] = mcrr0[k][k] ;
             ptpto[2][1] = 1.0 ;
          }
       }

// matching conditions in plume flow zone 2
       getPlumrat() ;

// internal plume flow zone 2
       if (nprat > 1.0) {  //  underexpanded
          mach1[2][2] = mach2[2][1] ;
          fac1 = (gamma - 1.0) / gamma ;
          fac2 = (gamma - 1.0) * .5 ;
          mach2[2][2] = Math.sqrt((1.0/fac2)*(((1.0+fac2*mach1[2][2]*mach1[2][2])*
                  Math.exp(fac1*Math.log(nprat/plumrat)))-1.0)) ;
          getIsen(mach2[2][2],gamma) ;
          mang[2][2] = mu ;
          pm[2][2] = nu ;
          defl[2][2] = pm[2][2] - pm[2][1] ;
          ppt[2][2] = poverpt ;
          ttt[2][2] = tovertt ;
          rrt[2][2] = roverrt ;
          ptrat[2][2] = 1.0 ;
          prat[2][2] = ppt[2][2]/ppt[2][1] ;
          trat[2][2] = ttt[2][2]/ttt[2][1] ;
          rhorat[2][2] = rrt[2][2]/rrt[2][1] ;   
          pspo[2][2] = pspo[2][1] * prat[2][2] ;
          tsto[2][2] = tsto[2][1] * trat[2][2] ;
          rsro[2][2] = rsro[2][1] * rhorat[2][2] ;
          ptpto[2][2] = ptpto[2][1] ;
          turn[2][2] = turn[2][1] + defl[2][2] ;
          pplume = ppt[2][2] * pto ;

// external plume flow zone 2
//  underexpanded nozzle .. external shock
          if (plumopt == 2) {
            gamu = 1.4 ;
            defl[1][2] = defl[2][2] ;
            shkang[1][2] = getShkang(mlocal,defl[1][2] * convdr, gamu) ;
            getOblq(mlocal,shkang[1][2],gamu,1,1,2) ;
            turn[1][2] = turn[1][1] + defl[1][2] ;
            getIsen(mach2[1][2],gamu) ;
            mang[1][2] = mu ;
            pm[1][2] = nu ;
            ppt[1][2] = poverpt ;
            ttt[1][2] = tovertt ;
            rrt[1][2] = roverrt ;
          }
       }

       if (nprat <= 1.0 ) { // over expanded
          if (prob >= 4 && prob <= 5) {  // special logic for over-expanded plug  
             jetPlug() ;
 // ---- compression waves - no loss of pt
             mach1[2][2] = mach2[2][1] ;
             mach2[2][2] = mach1[2][2] ;
             getIsen(mach2[2][2],gamma) ;
             mang[2][2] = mu ;
             pm[2][2] = nu ;
             defl[2][2] = -defl[2][1] ;
             ppt[2][2] = poverpt ;
             ttt[2][2] = tovertt ;
             rrt[2][2] = roverrt ;
             ptrat[2][2] = 1.0 ;
             prat[2][2] = ppt[2][2]/ppt[2][1] ;
             trat[2][2] = ttt[2][2]/ttt[2][1] ;
             rhorat[2][2] = rrt[2][2]/rrt[2][1] ;   
             pspo[2][2] = pspo[2][1] * prat[2][2] ;
             tsto[2][2] = tsto[2][1] * trat[2][2] ;
             rsro[2][2] = rsro[2][1] * rhorat[2][2] ;
             ptpto[2][2] = ptpto[2][1] ;
             turn[2][2] = -turn[2][1] ;
          }
          if (prob < 4.0) {
// ---- shock waves  - loss of pt
             mach1[2][2] = mach2[2][1] ;
             fac1 = (gamma + 1.0) ;
             fac2 = (gamma - 1.0) ;
             sinthet = Math.sqrt((fac1*(plumrat/nprat) + fac2)/(2.0*mach1[2][2]*mach1[2][2]*gamma)) ;
             shkang[2][2] = Math.asin(sinthet) / convdr ;
             getOblq(mach1[2][2],shkang[2][2],gamma,2,1,2) ;
             defl[2][2] = -defl[2][2] ;
             getIsen(mach2[2][2],gamma) ;
             mang[2][2] = mu ;
             pm[2][2] = nu ;
             ppt[2][2] = poverpt ;
             ttt[2][2] = tovertt ;
             rrt[2][2] = roverrt ;
             turn[2][2] = turn[2][1] + defl[2][2] ;
// external plume flow zone 2
// over expanded nozzle .. external expansion
             if (plumopt == 2) {
               gamu = 1.4 ;
               defl[1][2] = defl[2][2] ;
               getIsenExp(mlocal,gamu,defl[1][2]*convdr,1,1,2) ;
               getIsen(mach2[1][2],gamu) ;
               mang[1][2] = mu ;
               pm[1][2] = nu ;
               ppt[1][2] = poverpt ;
               ttt[1][2] = tovertt ;
               rrt[1][2] = roverrt ;
               turn[1][2] = turn[1][1] + defl[1][2] ;
             }
          }
          if (prob == 6) {  // special logic for over-expanded plug  
// ---- shock waves  - loss of pt
             fac1 = (gamma + 1.0) ;
             fac2 = (gamma - 1.0) ;
             mach1[2][2] = mach2[2][1] ;
             sinthet = Math.sqrt((fac1*(plumrat/nprat) + fac2)/(2.0*mach1[2][2]*mach1[2][2]*gamma)) ;
             shkang[2][2] = Math.asin(sinthet) / convdr ;
             getOblq(mach1[2][2],shkang[2][2],gamma,2,1,2) ;
             defl[2][2] = -defl[2][2] ;
             getIsen(mach2[2][2],gamma) ;
             mang[2][2] = mu ;
             pm[2][2] = nu ;
             ppt[2][2] = poverpt ;
             ttt[2][2] = tovertt ;
             rrt[2][2] = roverrt ;
             turn[2][2] = turn[2][1] + defl[2][2] ;
          }
          pplume = ppt[2][2] * pto ;
       }

// flow in other zones
       if (nprat > 1.0) {  //  underexpanded
          jetUnder() ;
       }
       if (nprat <= 1.0 ) { // over expanded
          jetOver() ;
       }

       return ;
   }

   public void jetPlug() {  //  Analysis for over-expanded plug  (prob = 4 or Prob == 5)
       int k,nmd1 ;
       double gm1,gp1;
       double mu1,thet1,dee ;
       
       gm1 = gamma - 1.0 ;
       gp1 = gamma + 1.0 ;

//     determine the Mach number of the last expansion wave .. function of nprat

       mach1[2][1] = Math.sqrt(2.0 * (Math.pow(ps0*plumrat/pto, -gm1/gamma) - 1.0)/gm1) ;     

//  flow variables in zone 1

       mach2[2][1] = mach1[2][1] ;
       getIsen(mach2[2][1],gamma) ;
       mang[2][1] = mu ;
       pm[2][1] = nu ;
       ppt[2][1] = poverpt ;
       ttt[2][1] = tovertt ;
       rrt[2][1] = roverrt ;
       ptrat[2][1] = 1.0 ;
       prat[2][1] = ppt[2][1] ;
       trat[2][1] = ttt[2][1] ;
       rhorat[2][1] = rrt[2][1] ;
       turn[2][1]= nuexit - pm[2][1];
       defl[2][1] = turn[2][1] ;

//      determine which rays are to be cut off

       for (k=jexplug; k<=numray; ++k) {
          numd = k - 1 ;
          if (mach2[2][1] <= mcmach[1][k]) break;
       }

// load variables into numd + 1
       nmd1 = numd + 1 ;
       mcmach[1][nmd1] = mach2[2][1];
       mcmang[1][nmd1] = mu ;
       mcpm[1][nmd1] = nu ;
       mcturn[1][nmd1] = nuexit - mcpm[1][nmd1] ;
       mcdefl[1][nmd1] = mcturn[1][nmd1] - mcturn[1][numd] ;
       mcpp0[1][nmd1] = poverpt ;
       mctt0[1][nmd1] = tovertt ;
       mcrr0[1][nmd1] = roverrt ;
       mcarat[1][nmd1] = arat ;
       mcprat[1][nmd1] = mcpp0[1][nmd1] / mcpp0[1][numd] ;
       mctrat[1][nmd1] = mctt0[1][nmd1] / mctt0[1][numd] ;
       mcrrat[1][nmd1] = mcrr0[1][nmd1] / mcrr0[1][numd] ; 

       thet1 = mcturn[1][nmd1] ;
       mu1 = mcmang[1][nmd1] + thet1 ;
       thet1 = convdr * thet1;
       mu1 = convdr * mu1 ;
       dee = (1.0 - wynd[nmd1] - wxnd[nmd1]*Math.tan(mu1))/(Math.tan(thet1) - Math.tan(mu1)) ;
       mcx[1][nmd1] = wxnd[nmd1] -dee ;
       mcy[1][nmd1] = 1.0 - mcx[1][nmd1]*Math.tan(mu1) ;

       pspo[2][1] = mcpp0[1][nmd1] ;
       tsto[2][1] = mctt0[1][nmd1] ;
       rsro[2][1] = mcrr0[1][nmd1] ;
       ptpto[2][1] = 1.0 ;

       return ;
   }
   
   public void jetUnder() {     //  Analysis for under-expanded plume
       double xl1, xl2, xl3, yl1, yl2, xa1, xb1, xc1, gamu ;
       int k ;

// geometry parameters for streamlines
         xa1 = nzht/Math.tan(convdr*mang[2][1]) ;
         xb1 = nzht/Math.tan(convdr*(mang[2][2]-defl[2][2])) ;
         xl1 = 2.0*nzht*xa1 /(nzht - xa1*Math.tan(convdr*defl[2][2])) ;
         yl1 = xl1 * Math.tan(convdr*defl[2][2]) ;
         xl2 = xb1 + (nzht + yl1)/(Math.tan(convdr*(mang[2][2]-defl[2][2]))) ;
         gamu = 1.4 ;
 // internal  flow
 // zone 3
         mach1[2][3] = mach2[2][2] ;
         defl[2][3]  = 0.0 ;
         pm[2][3] = pm[2][2] + defl[2][2] ;
         getMachpm(pm[2][3],gamma) ;
         mach2[2][3] = machpm ;
         getIsenJet(mach2[2][3],gamma,2,2,3) ;
         turn[2][3] = turn[2][2] + defl[2][3] ;
// plume into static 
         if (plumopt == 1) {
 // zone 4
            mach1[2][4] = mach2[2][3];
            defl[2][4] = - defl[2][2] ;
            pm[2][4] = pm[2][3] + defl[2][4] ;
            getMachpm(pm[2][4],gamma) ;
            mach2[2][4] = machpm ;
            getIsenJet(mach2[2][4],gamma,2,3,4) ;
            turn[2][4] = turn[2][3] + defl[2][4] ;
 // zone 5
            mach1[2][5] = mach2[2][4];
            defl[2][5] = 0.0 ;
            pm[2][5] = pm[2][1] ;
            getMachpm(pm[2][5],gamma) ;
            mach2[2][5] = machpm ;
            getIsenJet(mach2[2][5],gamma,2,4,5) ;
            turn[2][5] = turn[2][4] + defl[2][5] ;
         }
// plume into supersonic
         if (plumopt == 2) {
 // external  flow
 // zone 3
            mach1[1][3] = mach2[1][2] ;
            defl[1][3]  = -defl[1][2] ;
            pm[1][3] = pm[1][2] - defl[1][3] ;
            getMachpm(pm[1][3],gamu) ;
            mach2[1][3] = machpm ;
            getIsenJet(mach2[1][3],gamu,1,2,3) ;
            turn[1][3] = turn[1][2] + defl[1][3] ;
 // zone 4
            mach1[1][4] = mach2[1][3];
            defl[1][4] = defl[1][3] ;
            pm[1][4] = pm[1][3] + defl[1][4] ;
            getMachpm(pm[1][4],gamu) ;
            mach2[1][4] = machpm ;
            getIsenJet(mach2[1][4],gamu,1,3,4) ;
            turn[1][4] = turn[1][3] + defl[1][4] ;
         }
  //  middle stream line
         strx[1][1] = xexit + xa1 ;
         stry[1][1] = 0.0  ;
         strx[2][1] = xexit + xb1 ;
         stry[2][1] = stry[1][1] ;
         strx[3][1] = xexit + xl1 + xl2 - xb1 ;
         stry[3][1] = stry[2][1] ;
         strx[4][1] = xexit + xl1 + xl2 - xa1 ;
         stry[4][1] = stry[1][1] ;
 //  upper stream line
         strx[1][2] = xexit ;
         stry[1][2] = yexit ;
         strx[2][2] = xexit + xl1 ;
         stry[2][2] = yexit + yl1 ;
         strx[3][2] = xexit + xl2 ;
         stry[3][2] = stry[2][2] ;
         if (plumopt == 1) {
            strx[4][2] = xexit + xl2 + xl1 ;
            stry[4][2] = stry[1][2] ;
         }
         if (plumopt == 2) {
            strx[4][2] = xexit + 500. ;
            stry[4][2] = stry[3][2] ;
         }
// waves
// internal
         ixbgn[1] = strx[1][2] ;
         iybgn[1] = stry[1][2] ;
         ixnd[1]  = strx[1][1] ;
         iynd[1]  = stry[1][1] ;
         ifamily[1] = 1 ;
         ixbgn[2] = strx[1][2] ;
         iybgn[2] = stry[1][2] ;
         ixnd[2]  = strx[2][1] ;
         iynd[2]  = stry[2][1] ;
         ifamily[2] = 1 ;
         islope[2] = iybgn[2] / (ixnd[2] - ixbgn[2]) ;

         ixbgn[3] = strx[1][1] ;
         iybgn[3] = stry[1][1] ;
         ixnd[3]  = strx[2][2] ;
         iynd[3]  = stry[2][2] ;
         ifamily[3] = 1 ;
         islope[3] = iynd[3] / (ixnd[3] - ixbgn[3]) ;
         ixbgn[4] = strx[2][1] ;
         iybgn[4] = stry[2][1] ;
         ixnd[4]  = strx[3][2] ;
         iynd[4]  = stry[3][2] ;
         ifamily[4] = 1;
         islope[4] = iynd[4] / (ixnd[4] - ixbgn[4]) ;

// plume into static
       if(plumopt == 1) {
         ixbgn[5] = strx[2][2] ;
         iybgn[5] = stry[2][2] ;
         ixnd[5]  = strx[3][1] ;
         iynd[5]  = stry[3][1] ;
         ifamily[5] = 2 ;
         islope[5] = iybgn[5] / (ixnd[5] - ixbgn[5]) ;
         ixbgn[6] = strx[3][2] ;
         iybgn[6] = stry[3][2] ;
         ixnd[6]  = strx[4][1] ;
         iynd[6]  = stry[4][1] ;
         ifamily[6] = 2 ;
         islope[6] = iybgn[6] / (ixnd[6] - ixbgn[6]) ;

         ixbgn[7] = strx[3][1] ;
         iybgn[7] = stry[3][1] ;
         ixnd[7]  = strx[4][2] ;
         iynd[7]  = stry[4][2] ;
         ifamily[7] = 2 ;
         islope[7] = iynd[7] / (ixnd[7] - ixbgn[7]) ;
         ixbgn[8] = strx[4][1] ;
         iybgn[8] = stry[4][1] ;
         ixnd[8]  = strx[4][2] ;
         iynd[8]  = stry[4][2] ;
         ifamily[8] = 2 ;
     }
// plume into supersonic
     if (plumopt == 2) {
         ixbgn[5] = strx[4][2] ;
         iybgn[5] = stry[3][1] ;
         ixnd[5]  = strx[4][2] ;
         iynd[5]  = stry[4][2] ;
//external
         eslope[3] = shkang[1][2] + defl[1][1] ;
         efamily[3] = 2 ;
         exbgn[3] = strx[1][2] ;
         eybgn[3] = stry[1][2] ;
         eynd[3] = eybgn[3] + nzht*exht ;
         exnd[3] = exbgn[3] + (nzht*exht / Math.tan(convdr*eslope[3])) ;

 //        eslope[4] = mang[1][2] + defl[1][2] ;
         eslope[4] = mang[1][2] + turn[1][2] ;
         efamily[4] = 1 ;
         exbgn[4] = strx[2][2] ;
         eybgn[4] = stry[2][2] ;
         eynd[4] = eybgn[4] + nzht*exht ;
         exnd[4] = exbgn[4] + (nzht*exht / Math.tan(convdr*eslope[4])) ;

 //        eslope[5] = mang[1][3] - defl[1][3] ;
         eslope[5] = mang[1][3] + turn[1][3] ;
         efamily[5] = 1 ;
         exbgn[5] = strx[3][2] ;
         eybgn[5] = stry[3][2] ;
         eynd[5] = eybgn[5] + nzht*exht ;
         exnd[5] = exbgn[5] + (nzht*exht / Math.tan(convdr*eslope[5])) ;

         eslope[6] = 90.0 ;
         efamily[6] = 1 ;
         exbgn[6] = strx[4][2] ;
         eybgn[6] = stry[4][2] ;
         eynd[6] = eybgn[6] + nzht*exht ;
         exnd[6] = exbgn[6] ;
       }

// output and plotting variables
       for(k=0; k<=54; ++k) {
          pres[1][k] = ps0 * pspo[1][k] ;
          temp[1][k] = ts0 * tsto[1][k] ;
          pres[2][k] = psth * pspo[2][k] ;
          temp[2][k] = tsth * tsto[2][k] ;
       }

       return ;
   }

   public void jetOver() {     //  Analysis for over-expanded plume
       double fac1, fac2 ;
       double xl1, xl2, xl3, yl1, yl2, xa1, ya1, xb1, xc1, xb2 ;
       double xe,ye,xf,yf,thet1,mu1,gamu ;
       double sinthet;
       int k,nmd1 ;

       xe = 1.0 ;
       ye = 1.0 ;
       xf = 1.0 ;
       yf = 1.0 ;
       gamu = 1.4 ;

 // zone 3
       if (prob >= 4 && prob <= 5) {   //plug nozzle
  // geometry for reflected compression wave
          nmd1 = numd + 1 ;
          thet1 = mcturn[1][nmd1] ;
          mu1 = mcmang[1][nmd1] - thet1 ;
          thet1 = convdr * thet1;
          mu1 = convdr * mu1 ;
          xe = mcx[1][nmd1] ;
          ye = mcy[1][nmd1] ;
 //         yf = 1.0 ;
 //         xf = xe + (yf-ye) / Math.tan(mu1) ;
          xf = (1.0 - ye + xe*Math.tan(mu1)) / (Math.tan(mu1) + Math.tan(thet1)) ;
          yf = 1.0 - xf * Math.tan(thet1) ;
          mach1[2][3] = mach2[2][2] ;
          defl[2][3]  = 0.0 ;
          turn[2][3] = turn[2][2] ;
          pm[2][3] = pm[2][2] + turn[2][3] ;
          getMachpm(pm[2][3],gamma) ;
          mach2[2][3] = machpm ;
          getIsenJet(mach2[2][3],gamma,2,2,3) ;
      }

      if (prob < 4) {  // not plug nozzle
         mach1[2][3] = mach2[2][2] ;
         shkang[2][3] = getShkang(mach1[2][3],-defl[2][2]*convdr,gamma) ;
          getOblq(mach1[2][3],shkang[2][3],gamma,2,2,3) ;
          getIsen(mach2[2][3],gamma) ;
          mang[2][3] = mu ;
          pm[2][3] = nu ;
          ppt[2][3] = poverpt ;
          ttt[2][3] = tovertt ;
          rrt[2][3] = roverrt ;
          turn[2][3] = turn[2][2] + defl[2][3] ;
        }
      if (prob == 6) {  // flat cowl - plug nozzle
         mach1[2][3] = mach2[2][2] ;
         shkang[2][3] = getShkang(mach1[2][3],-defl[2][2]*convdr,gamma) ;
          getOblq(mach1[2][3],shkang[2][3],gamma,2,2,3) ;
          getIsen(mach2[2][3],gamma) ;
          mang[2][3] = mu ;
          pm[2][3] = nu ;
          ppt[2][3] = poverpt ;
          ttt[2][3] = tovertt ;
          rrt[2][3] = roverrt ;
          turn[2][3] = turn[2][2] + defl[2][3] ;
        }
// plume into supersonic
// external
       if(plumopt == 2) {
         mach1[1][3] = mach2[1][2] ;
         shkang[1][3] = getShkang(mach1[1][3],-defl[1][2]*convdr,gamu) ;
          getOblq(mach1[1][3],shkang[1][3],gamu,1,2,3) ;
          getIsen(mach2[1][3],gamu) ;
          mang[1][3] = mu ;
          pm[1][3] = nu ;
          ppt[1][3] = poverpt ;
          ttt[1][3] = tovertt ;
          rrt[1][3] = roverrt ;
          turn[1][3] = turn[1][2] + defl[1][3] ;
       }
// plume into static
       if(plumopt == 1) {
 // zone 4
         mach1[2][4] = mach2[2][3] ;
         fac1 = (gamma - 1.0) / gamma ;
         fac2 = (gamma - 1.0) * .5 ;
 //        mach2[2][4] = Math.sqrt((1.0/fac2)*(((1.0+fac2*mach1[2][4]*mach1[2][4])*
 //                   Math.exp(fac1*Math.log(nprat*pspo[2][3])))-1.0)) ;
         mach2[2][4] = Math.sqrt((1.0/fac2)*(((1.0+fac2*mach1[2][4]*mach1[2][4])*
                   Math.exp(fac1*Math.log(nprat*prat[2][3]*prat[2][2])))-1.0)) ;
         getIsenJet(mach2[2][4],gamma,2,3,4) ;
         pm[2][4] = nu ;
         defl[2][4] = pm[2][4] - pm[2][3] ;
         turn[2][4] = turn[2][3] + defl[2][4] ;
 // zone 5
         mach1[2][5] = mach2[2][4] ;
         defl[2][5]  = 0.0 ;
         pm[2][5] = pm[2][4] + defl[2][4] ;
         getMachpm(pm[2][5],gamma) ;
         mach2[2][5] = machpm ;
         getIsenJet(mach2[2][5],gamma,2,4,5) ;
         turn[2][5] = turn[2][4] + defl[2][5] ;
 // zone 6
         mach1[2][6] = mach2[2][5];
         defl[2][6] = - defl[2][4] ;
         pm[2][6] = pm[2][5] + defl[2][6] ;
         getMachpm(pm[2][6],gamma) ;
         mach2[2][6] = machpm ;
         getIsenJet(mach2[2][6],gamma,2,5,6) ;
         turn[2][6] = turn[2][5] + defl[2][6] ;
 // zone 7
         mach1[2][7] = mach2[2][6];
         defl[2][7] = 0.0 ;
         pm[2][7] = pm[2][3] ;
         getMachpm(pm[2][7],gamma) ;
         mach2[2][7] = machpm ;
         getIsenJet(mach2[2][7],gamma,2,6,7) ;
         turn[2][7] = turn[2][6] + defl[2][7] ;
       }
// geometry parameters for streamlines
    //  plug nozzle - prob = 4 or prob =5
         xa1 = nzht*wxnd[numray] ;
         ya1 = 0.0 ;
         xb1 = (nzht + xa1* Math.tan(convdr*defl[2][2]))/(Math.tan(convdr*mang[2][3]) -Math.tan(convdr*defl[2][2])) ;
         xb2 = xb1 ;

         if (prob < 4) {
            xa1 = nzht/Math.tan(convdr*shkang[2][2]) ;
            xb2 = (nzht + xa1* Math.tan(convdr*defl[2][2]))/
                   (Math.tan(convdr*(shkang[2][3]+defl[2][2])) -Math.tan(convdr*defl[2][2])) ;
            xb1 = (nzht + xa1* Math.tan(convdr*defl[2][2]))/
                   (Math.tan(convdr*mang[2][3]) -Math.tan(convdr*defl[2][2])) ;
         }
         if (prob == 6) {
            xe = (xpexit - xexit)/nzht ;
            xa1 = nzht*(1.0 + xe * Math.tan(convdr*defl[2][2]))/
                   (Math.tan(convdr*shkang[2][2])+Math.tan(convdr*defl[2][2])) ;
            ya1 = (xe - xa1/nzht)*Math.tan(convdr*mcdefl[numray/2][numray/2]) ;
            xb2 = (nzht + xa1* Math.tan(convdr*defl[2][2]))/
                   (Math.tan(convdr*(shkang[2][3]+defl[2][2])) -Math.tan(convdr*defl[2][2])) ;
            xb1 = (nzht + xa1* Math.tan(convdr*defl[2][2]))/
                   (Math.tan(convdr*mang[2][3]) -Math.tan(convdr*defl[2][2])) ;
         }
         xc1 = xb1 * Math.tan(convdr*mang[2][3])/Math.tan(convdr*(mang[2][4]-defl[2][4])) ;
         xl1 = xa1 + xb2 ;
         yl1 = xl1 * Math.tan(convdr*defl[2][2]) ;
         xl2 = 2.0*xb1*Math.tan(convdr*mang[2][3]) /(Math.tan(convdr*mang[2][3]) - Math.tan(convdr*defl[2][4])) ;
         yl2 = xl2 * Math.tan(convdr*defl[2][4]) ;
         xl3 = xc1 + (xb1*Math.tan(convdr*mang[2][3]) + yl2)/(Math.tan(convdr*(mang[2][5]-defl[2][5]))) ;
 //  middle stream line
         strx[1][1] = xexit + xa1 ;
         stry[1][1] = ya1*nzht  ;
         strx[2][1] = xexit + xl1 + xb1 ;
         stry[2][1] = 0.0 ;
         strx[3][1] = xexit + xl1 + xc1 ;
         stry[3][1] = stry[2][1] ;
         strx[4][1] = xexit + xl1 + xl2 + xl3 - xc1 ;
         stry[4][1] = stry[2][1] ;
         strx[5][1] = xexit + xl1 + xl2 + xl3 - xb1 ;
         stry[5][1] = stry[2][1] ; 
 //  upper stream line
         strx[1][2] = xexit ;
         stry[1][2] = yexit ;
         strx[2][2] = xexit + xl1 ;
         stry[2][2] = yexit + yl1 ;
         if (plumopt == 1) {
           strx[3][2] = xexit + xl1 + xl2 ;
           stry[3][2] = yexit + yl1 + yl2  ;
           strx[4][2] = xexit + xl1 + xl3 ;
           stry[4][2] = stry[3][2] ;
           strx[5][2] = xexit + xl1 + xl2 + xl3 ;
           stry[5][2] = stry[2][2] ; 
         }
         if (plumopt == 2) {
           strx[3][2] = xexit + 500. ;
           stry[3][2] = stry[2][2] ;
         }
 // waves
         if(prob < 4) {   
           ixbgn[1] = strx[1][2] ;
           iybgn[1] = stry[1][2] ;
           ixnd[1]  = strx[1][1] ;
           iynd[1]  = stry[1][1] ;
         }
         if (prob >= 4 && prob <=5) {
           ixbgn[1] = nzht * xe ;
           iybgn[1] = nzht * ye ;
           ixnd[1]  = nzht * xf ;
           iynd[1]  = nzht * yf ;
         }
         if(prob == 6) {   
           ixbgn[1] = strx[1][2] ;
           iybgn[1] = stry[1][2] ;
           ixnd[1]  = strx[1][1] ;
           iynd[1]  = stry[1][1] ;
         }
         ifamily[1] = 2 ; 
         ixbgn[2] = strx[1][1] ;
         iybgn[2] = stry[1][1] ;
         ixnd[2]  = strx[2][2] ;
         iynd[2]  = stry[2][2] ;
         ifamily[2] = 2 ;

// plume into supersonic
    if (plumopt == 2) {
//external
         eslope[3] = shkang[1][2] + defl[1][1]  ;
         efamily[3] = 1 ;
         exbgn[3] = strx[1][2] ;
         eybgn[3] = stry[1][2] ;
         eynd[3] = eybgn[3] + nzht*exht ;
         exnd[3] = exbgn[3] + (nzht*exht / Math.tan(convdr*eslope[3])) ;

         eslope[4] = expang[1][2] + defl[1][1] ;
         efamily[4] = 1 ;
         exbgn[4] = strx[1][2] ;
         eybgn[4] = stry[1][2] ;
         eynd[4] = eybgn[4] + nzht*exht ;
         exnd[4] = exbgn[4] + (nzht*exht / Math.tan(convdr*eslope[4])) ;

         eslope[5] = shkang[1][3] + defl[1][2] ;
         efamily[5] = 2 ;
         exbgn[5] = strx[2][2] ;
         eybgn[5] = stry[2][2] ;
         eynd[5] = eybgn[5] + nzht*exht ;
         exnd[5] = exbgn[5] + (nzht*exht / Math.tan(convdr*eslope[5])) ;

         eslope[6] = 90. ;
         efamily[6] = 2 ;
         exbgn[6] = strx[3][2] ;
         eybgn[6] = stry[3][2] ;
         eynd[6] = eybgn[6] + nzht*exht ;
         exnd[6] = exbgn[6] ;
      }
// plume into static
      if(plumopt == 1) {
         ixbgn[3] = strx[2][2] ;
         iybgn[3] = stry[2][2] ;
         ixnd[3]  = strx[2][1] ;
         iynd[3]  = stry[2][1] ;
         ifamily[3] = 1 ;
         ixbgn[4] = strx[2][2] ;
         iybgn[4] = stry[2][2] ;
         ixnd[4]  = strx[3][1] ;
         iynd[4]  = stry[3][1] ;
         ifamily[4] = 1 ;
         islope[4] = iybgn[4] / (ixnd[4] - ixbgn[4]) ;

         ixbgn[5] = strx[2][1] ;
         iybgn[5] = stry[2][1] ;
         ixnd[5]  = strx[3][2] ;
         iynd[5]  = stry[3][2] ;
         ifamily[5] = 1 ;
         islope[5] = iynd[5] / (ixnd[5] - ixbgn[5]) ;
         ixbgn[6] = strx[3][1] ;
         iybgn[6] = stry[3][1] ;
         ixnd[6]  = strx[4][2] ;
         iynd[6]  = stry[4][2] ;
         ifamily[6] = 1 ;
         islope[6] = iynd[6] / (ixnd[6] - ixbgn[6]) ;

         ixbgn[7] = strx[3][2] ;
         iybgn[7] = stry[3][2] ;
         ixnd[7]  = strx[4][1] ;
         iynd[7]  = stry[4][1] ;
         ifamily[7] = 2 ;
         islope[7] = iybgn[7] / (ixnd[7] - ixbgn[7]) ;
         ixbgn[8] = strx[4][2] ;
         iybgn[8] = stry[4][2] ;
         ixnd[8]  = strx[5][1] ;
         iynd[8]  = stry[5][1] ;
         ifamily[8] = 2 ;
         islope[8] = iybgn[8] / (ixnd[8] - ixbgn[8]) ;

         ixbgn[9] = strx[4][1] ;
         iybgn[9] = stry[4][1] ;
         ixnd[9]  = strx[5][2] ;
         iynd[9]  = stry[5][2] ;
         ifamily[9] = 2 ;
         islope[9] = iynd[9] / (ixnd[9] - ixbgn[9]) ;
         ixbgn[10] = strx[5][1] ;
         iybgn[10] = stry[5][1] ;
         ixnd[10]  = strx[5][2] ;
         iynd[10]  = stry[5][2] ;
         ifamily[10] = 2 ;
     }

// output and plotting variables
       for(k=0; k<=54; ++k) {
          pres[1][k] = ps0 * pspo[1][k] ;
          temp[1][k] = ts0 * tsto[1][k] ;
          pres[2][k] = psth * pspo[2][k] ;
          temp[2][k] = tsth * tsto[2][k] ;
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
   }

  public void getIsenExp (double machin, double gam, double delr,
                        int surf, int upstrm, int index) {
  // Centered Prandtl-Meyer Expansion
  //           delr is a negative number 
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
       shkang[surf][index] =  Math.asin(1.0/machin)/convdr  ;
       if (machin < 1.00001) shkang[surf][index] = 90.0 ;

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

          mach2[surf][index] = Math.sqrt(m2sm1o + 1.0) ;
          fac1 = 1.0 + .5*gm1*msq;
          fac2 = 1.0 + .5*gm1*(m2sm1o+1.0);
       }
       else {
          mach2[surf][index] = 50.0 ;
          fac1 = 1.0 + .5*gm1*msq;
          fac2 = 1.0 + .5*gm1*(mach2[surf][index]*mach2[surf][index]);
       }

       expang[surf][index] = (Math.asin(1.0/mach2[surf][index]) +delr)/convdr ;
       prat[surf][index] = Math.pow((fac1/fac2),(gam/gm1));
       rhorat[surf][index] = Math.pow((fac1/fac2),(1.0/gm1));
       trat[surf][index] = fac1/fac2 ;
       ptrat[surf][index] = 1.0;                   /* Isentropic */
       defl[surf][index] = delr/convdr ;
       mach1[surf][index] = machin ;

       pspo[surf][index] = pspo[surf][upstrm]*prat[surf][index] ;
       tsto[surf][index] = tsto[surf][upstrm]*trat[surf][index] ;
       rsro[surf][index] = rsro[surf][upstrm]*rhorat[surf][index] ;
       ptpto[surf][index] = ptpto[surf][upstrm]*ptrat[surf][index] ;
  
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

  public void getIsenJet (double machin, double gam, int surf, int upstream, int index) {
 // isentropic relations for Jet analysis
         getIsen(machin,gam) ;
         mang[surf][index] = mu ;
         ppt[surf][index] = poverpt ;
         ttt[surf][index] = tovertt ;
         rrt[surf][index] = roverrt ;
         ptrat[surf][index] = 1.0 ;
         prat[surf][index] = ppt[surf][index]/ppt[surf][upstream] ;
         trat[surf][index] = ttt[surf][index]/ttt[surf][upstream] ;
         rhorat[surf][index] = rrt[surf][index]/rrt[surf][upstream] ;   
         pspo[surf][index] = pspo[surf][upstream] * prat[surf][index] ;
         tsto[surf][index] = tsto[surf][upstream] * trat[surf][index] ;
         rsro[surf][index] = rsro[surf][upstream] * rhorat[surf][index] ;
         ptpto[surf][index] = ptpto[surf][upstream] ;
         return ;
  }

  public void getIsenRamp (double machin, double gam, int surf, int upstream, int index) { // isentropic relations 
     double mach1s,msm1,gm1,gp1,fac,fac1 ;   
          // poverpt and tovertt are ratiod to total conditions 
     mach1s = machin*machin ; 
     gm1 = gam - 1.0 ;
     gp1 = gam + 1.0 ;
     msm1 = mach1s - 1.0;
     fac = 1.0 + .5*gm1*mach1s;

     ppt[surf][index] = Math.pow(1.0/fac,gam/gm1) ;                  /* EQ 44 */
     ttt[surf][index] = 1.0 / fac ;                                   /* EQ 43 */
     rrt[surf][index] = Math.pow(1.0/fac,1.0/gm1) ;                 /* EQ 45 */
     fac1 = gp1/(2.0*gm1) ;
     arat = machin * Math.pow(fac,-fac1) * Math.pow(gp1/2.0,fac1) ; /* EQ 80 */
     arat = 1.0/arat ;
     mu = (Math.asin(1.0/machin))/convdr ;
     nu = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1/gp1)) 
             - Math.atan(Math.sqrt(msm1)) ;
     nu = nu / convdr;

     pm[surf][index] = nu ;
     mang[surf][index] = mu ;
     if(index >= 2) {
       ptrat[surf][index] = 1.0 ;
       prat[surf][index] = ppt[surf][index]/ppt[surf][upstream] ;
       trat[surf][index] = ttt[surf][index]/ttt[surf][upstream] ;
       rhorat[surf][index] = rrt[surf][index]/rrt[surf][upstream] ;
   
       pspo[surf][index] = pspo[surf][upstream] * prat[surf][index] ;
       tsto[surf][index] = tsto[surf][upstream] * trat[surf][index] ;
       rsro[surf][index] = rsro[surf][upstream] * rhorat[surf][index] ;
       ptpto[surf][index] = ptpto[surf][upstream] ;
     }

     return;
  }

  public void getIsenComp (double machin, double gam, double delr,
                        int surf, int upstrm, int index) {
  // Prandtl-Meyer Compression
       double msq, msm1, gm1, gp1, fac1, fac2 ;
       double numax, nuo, nur, nun, m2sm1o, m2sm1n, deriv ;

       msq  = machin * machin ;
       msm1 = msq - 1.0;
       gm1 = gam - 1.0 ;
       gp1 = gam + 1.0 ;

       nuo = Math.sqrt(gp1/gm1) * Math.atan( Math.sqrt(gm1*msm1/gp1)) 
             - Math.atan(Math.sqrt(msm1)) ;

       nur = nuo - delr;

       m2sm1o = msm1;      // iterate for downstream mach 
       m2sm1n = msm1+.1 ;
       while (Math.abs(nur - nuo) > .000001) {
         nun = Math.sqrt(gp1/gm1) * Math.atan(Math.sqrt(gm1*m2sm1n/gp1)) 
                 - Math.atan(Math.sqrt(m2sm1n));
         deriv = (nun-nuo)/(m2sm1n-m2sm1o) ;
         nuo = nun ;
         m2sm1o = m2sm1n ;
         m2sm1n = m2sm1o + (nur-nuo)/deriv ;
       }

       mach2[surf][index] = Math.sqrt(m2sm1o + 1.0) ;
       mang[surf][index] =  Math.asin(1.0/mach2[surf][index])/convdr  ;
       if (mach2[surf][index] < 1.00001) mang[surf][index] = 90.0 ;
       fac1 = 1.0 + .5*gm1*msq;
       fac2 = 1.0 + .5*gm1*(m2sm1o+1.0);

       prat[surf][index] = Math.pow((fac1/fac2),(gam/gm1));
       rhorat[surf][index] = Math.pow((fac1/fac2),(1.0/gm1));
       trat[surf][index] = fac1/fac2 ;
       ptrat[surf][index] = 1.0;                   /* Isentropic */
       mach1[surf][index] = machin ;

       pspo[surf][index] = pspo[surf][upstrm]*prat[surf][index] ;
       tsto[surf][index] = tsto[surf][upstrm]*trat[surf][index] ;
       rsro[surf][index] = rsro[surf][upstrm]*rhorat[surf][index] ;
       ptpto[surf][index] = ptpto[surf][upstrm]*ptrat[surf][index] ;
  
       return;
  }

   public void getMachpm(double nuin, double gam)  {                 /* get the Mach number */
                                             /* given the Prandtl-meyer angle */
      double msm1o,msm1n,gp1,gm1;
      double nur,nuo,nun,deriv ;

      nur = nuin*convdr ;
      gm1 = gam - 1.0 ;
      gp1 = gam + 1.0 ;

      msm1o = 1.0;                                  /* iterate for mach */
      nuo = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1o/gp1)) 
             - Math.atan(Math.sqrt(msm1o));
      msm1n = msm1o+.01 ;
      while (Math.abs(nur - nuo) > .000001) {
         nun = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1n/gp1)) 
                - Math.atan(Math.sqrt(msm1n));
         deriv = (nun-nuo)/(msm1n-msm1o) ;
         nuo = nun ;
         msm1o = msm1n ;
         msm1n = msm1o + (nur-nuo)/deriv ;
       }
      machpm = Math.sqrt(msm1o + 1.0);
      return ;
   }

   public void loadOut() {

      if (gamopt == 1) {
          num.inp.intern.inleft.f2.setText(String.valueOf(filter3(gamma))) ;
      }

      if (prob == 0) {  // ideal nozzle - planes
          if ( inex == 0) {
             num.out.noz.o1.setText(String.valueOf(filter3(mcmach[row][col]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(mcdefl[row][col]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(mcprat[row][col]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(mcpp0[row][col]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(mctrat[row][col]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(mctt0[row][col]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(mcrrat[row][col])));
             num.out.noz.o12.setText(String.valueOf(filter3(mcrr0[row][col])));
             num.out.noz.o16.setText(String.valueOf(filter3(mcarat[row][col]))) ;
             num.out.noz.o15.setText(String.valueOf(filter3(mcpm[row][col]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(mcturn[row][col]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mcmang[row][col]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(0.0))) ;
             if (col > row) {
               num.out.noz.o26.setText(String.valueOf(filter3(mcmach[row][col-1]))) ;
             }
             else {
               num.out.noz.o26.setText(String.valueOf(filter3(mcmach[row-1][col]))) ;
             }
             num.out.noz.o45.setText(String.valueOf(filter3(mcpres[row][col]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(mctemp[row][col]))) ;
          }

          if (inex >= 1) {
             outzn = num.out.noz.zonch.getSelectedIndex() ;
             num.out.noz.o1.setText(String.valueOf(filter3(mach2[inex][outzn]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(defl[inex][outzn]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(prat[inex][outzn]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(pspo[inex][outzn]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(trat[inex][outzn]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(tsto[inex][outzn]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(rhorat[inex][outzn])));
             num.out.noz.o12.setText(String.valueOf(filter3(rsro[inex][outzn])));
             num.out.noz.o15.setText(String.valueOf(filter3(pm[inex][outzn]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(turn[inex][outzn]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mang[inex][outzn]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(ptrat[inex][outzn]))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(ptpto[inex][outzn]))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(shkang[inex][outzn]))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mach1[inex][outzn]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(pres[inex][outzn]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(temp[inex][outzn]))) ;
          }

          num.out.noz.o3.setText(String.valueOf(filter3(mcxll[row][col]))) ;
          num.out.noz.o4.setText(String.valueOf(filter3(mcyll[row][col]))) ;
          num.out.noz.o7.setText(String.valueOf(filter3(mcxul[row][col]))) ;
          num.out.noz.o8.setText(String.valueOf(filter3(mcyul[row][col]))) ;
          num.out.noz.o17.setText(String.valueOf(filter3(mcxur[row][col]))) ;
          num.out.noz.o18.setText(String.valueOf(filter3(mcyur[row][col]))) ;
          num.out.noz.o19.setText(String.valueOf(filter3(mcxlr[row][col]))) ;
          num.out.noz.o20.setText(String.valueOf(filter3(mcylr[row][col]))) ;
          num.out.nozp.o31.setText(String.valueOf(filter3(xexit))) ;
          num.out.nozp.o41.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o42.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o47.setText(String.valueOf(filter3(aboat))) ;
          num.out.nozp.o48.setText(String.valueOf(filter3(dragab))) ;
       }

      if (prob > 0 && prob < 3) {  // ideal nozzle - points
          if (inex == 0) {
             num.out.noz.o1.setText(String.valueOf(filter3(mcmach[row][col]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(mcdefl[row][col]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(mcprat[row][col]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(mcpp0[row][col]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(mctrat[row][col]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(mctt0[row][col]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(mcrrat[row][col])));
             num.out.noz.o12.setText(String.valueOf(filter3(mcrr0[row][col])));
             num.out.noz.o16.setText(String.valueOf(filter3(mcarat[row][col]))) ;
             num.out.noz.o15.setText(String.valueOf(filter3(mcpm[row][col]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(mcturn[row][col]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mcmang[row][col]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(0.0))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mcmach[row][col-1]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(mcpres[row][col]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(mctemp[row][col]))) ;
          }

          if (inex >= 1) {
             outzn = num.out.noz.zonch.getSelectedIndex() ;
             num.out.noz.o1.setText(String.valueOf(filter3(mach2[inex][outzn]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(defl[inex][outzn]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(prat[inex][outzn]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(pspo[inex][outzn]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(trat[inex][outzn]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(tsto[inex][outzn]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(rhorat[inex][outzn])));
             num.out.noz.o12.setText(String.valueOf(filter3(rsro[inex][outzn])));
             num.out.noz.o15.setText(String.valueOf(filter3(pm[inex][outzn]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(turn[inex][outzn]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mang[inex][outzn]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(ptrat[inex][outzn]))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(ptpto[inex][outzn]))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(shkang[inex][outzn]))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mach1[inex][outzn]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(pres[inex][outzn]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(temp[inex][outzn]))) ;
          }

          num.out.noz.o7.setText(String.valueOf(filter3(mcQ[row][col]))) ;
          num.out.noz.o8.setText(String.valueOf(filter3(mcR[row][col]))) ;
          num.out.noz.o3.setText(String.valueOf(filter3(mcx[row][col]))) ;
          num.out.noz.o4.setText(String.valueOf(filter3(mcy[row][col]))) ;
          num.out.noz.o17.setText(String.valueOf(filter3(mcal[row][col]))) ;
          num.out.noz.o18.setText(String.valueOf(filter3(mcbe[row][col]))) ;
          num.out.nozp.o31.setText(String.valueOf(filter3(xexit))) ;
          num.out.nozp.o41.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o42.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o47.setText(String.valueOf(filter3(aboat))) ;
          num.out.nozp.o48.setText(String.valueOf(filter3(dragab))) ;
      }

      if (prob == 3) {  // over or underexpanded jet
          outzn = num.out.noz.zonch.getSelectedIndex() ;
          num.out.noz.o1.setText(String.valueOf(filter3(mach2[2][outzn]))) ;
          num.out.noz.o2.setText(String.valueOf(filter3(defl[2][outzn]))) ;
          num.out.noz.o5.setText(String.valueOf(filter3(prat[2][outzn]))) ;
          num.out.noz.o6.setText(String.valueOf(filter3(pspo[2][outzn]))) ;
          num.out.noz.o9.setText(String.valueOf(filter3(trat[2][outzn]))) ;
          num.out.noz.o10.setText(String.valueOf(filter3(tsto[2][outzn]))) ;
          num.out.noz.o11.setText(String.valueOf(filter3(rhorat[2][outzn])));
          num.out.noz.o12.setText(String.valueOf(filter3(rsro[2][outzn])));
    //      num.out.noz.o16.setText(String.valueOf(filter3(mcarat[row][col]))) ;
          num.out.noz.o15.setText(String.valueOf(filter3(pm[2][outzn]))) ;
          num.out.noz.o13.setText(String.valueOf(filter3(turn[2][outzn]))) ;
          num.out.noz.o14.setText(String.valueOf(filter3(mang[2][outzn]))) ;
          num.out.noz.o37.setText(String.valueOf(filter3(ptrat[2][outzn]))) ;
          num.out.noz.o38.setText(String.valueOf(filter3(ptpto[2][outzn]))) ;
          num.out.noz.o39.setText(String.valueOf(filter3(shkang[2][outzn]))) ;
          num.out.noz.o26.setText(String.valueOf(filter3(mach1[2][outzn]))) ;
          num.out.nozp.o41.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o42.setText(String.valueOf(filter3(machend))) ;
          num.out.noz.o45.setText(String.valueOf(filter3(pres[2][outzn]))) ;
          num.out.noz.o46.setText(String.valueOf(filter0(temp[2][outzn]))) ;
          num.out.nozp.o47.setText(String.valueOf(filter3(0.0))) ;
          num.out.nozp.o48.setText(String.valueOf(filter3(0.0))) ;
      }

      if (prob >= 4 && prob <= 5) {  // external plug nozzle
          if ( inex == 0) {
             num.out.noz.o1.setText(String.valueOf(filter3(mcmach[1][col]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(mcdefl[1][col]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(mcprat[1][col]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(mcpp0[1][col]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(mctrat[1][col]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(mctt0[1][col]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(mcrrat[1][col])));
             num.out.noz.o12.setText(String.valueOf(filter3(mcrr0[1][col])));
             num.out.noz.o16.setText(String.valueOf(filter3(mcarat[1][col]))) ;
             num.out.noz.o15.setText(String.valueOf(filter3(mcpm[1][col]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(mcturn[1][col]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mcmang[1][col]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(0.0))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mcmach[1][col]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(mcpres[1][col]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(mctemp[1][col]))) ;
          }
          if (inex >= 1) {
             outzn = num.out.noz.zonch.getSelectedIndex() ;
             num.out.noz.o1.setText(String.valueOf(filter3(mach2[inex][outzn]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(defl[inex][outzn]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(prat[inex][outzn]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(pspo[inex][outzn]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(trat[inex][outzn]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(tsto[inex][outzn]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(rhorat[inex][outzn])));
             num.out.noz.o12.setText(String.valueOf(filter3(rsro[inex][outzn])));
             num.out.noz.o15.setText(String.valueOf(filter3(pm[inex][outzn]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(turn[inex][outzn]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mang[inex][outzn]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(ptrat[inex][outzn]))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(ptpto[inex][outzn]))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(shkang[inex][outzn]))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mach1[inex][outzn]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(pres[inex][outzn]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(temp[inex][outzn]))) ;
          }

             num.out.noz.o3.setText(String.valueOf(filter3(wxbgn[col]))) ;
             num.out.noz.o4.setText(String.valueOf(filter3(wybgn[col]))) ;
             num.out.noz.o7.setText(String.valueOf(filter3(icxnd[2]))) ;
             num.out.noz.o8.setText(String.valueOf(filter3(icynd[2]))) ;
             num.out.noz.o17.setText(String.valueOf(filter3(icxnd[2]))) ;
             num.out.noz.o18.setText(String.valueOf(filter3(icynd[2]))) ;
             num.out.noz.o19.setText(String.valueOf(filter3(wxnd[col]))) ;
             num.out.noz.o20.setText(String.valueOf(filter3(wynd[col]))) ;
             num.out.nozp.o31.setText(String.valueOf(filter3(wxnd[numray]*nzht))) ;
             num.out.nozp.o41.setText(String.valueOf(filter3(machend))) ;
             num.out.nozp.o42.setText(String.valueOf(filter3(mex))) ;
             if (prob == 5) {
                num.out.noz.o7.setText(String.valueOf(filter3(icxbgn[col]))) ;
                num.out.noz.o8.setText(String.valueOf(filter3(icybgn[col]))) ;
                num.out.noz.o17.setText(String.valueOf(filter3(icxnd[col]))) ;
                num.out.noz.o18.setText(String.valueOf(filter3(icynd[col]))) ;
             }
            num.out.nozp.o47.setText(String.valueOf(filter3(0.0))) ;
            num.out.nozp.o48.setText(String.valueOf(filter3(0.0))) ;
      } 

      if (prob == 6) {  // plug nozzle - straight cowl points
          if (inex == 0) {
             num.out.noz.o1.setText(String.valueOf(filter3(mcmach[row][col]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(mcdefl[row][col]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(mcprat[row][col]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(mcpp0[row][col]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(mctrat[row][col]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(mctt0[row][col]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(mcrrat[row][col])));
             num.out.noz.o12.setText(String.valueOf(filter3(mcrr0[row][col])));
             num.out.noz.o16.setText(String.valueOf(filter3(mcarat[row][col]))) ;
             num.out.noz.o15.setText(String.valueOf(filter3(mcpm[row][col]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(mcturn[row][col]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mcmang[row][col]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(0.0))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mcmach[row][col-1]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(mcpres[row][col]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(mctemp[row][col]))) ;
          }

          if (inex >= 1) {
             outzn = num.out.noz.zonch.getSelectedIndex() ;
             num.out.noz.o1.setText(String.valueOf(filter3(mach2[inex][outzn]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(defl[inex][outzn]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(prat[inex][outzn]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(pspo[inex][outzn]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(trat[inex][outzn]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(tsto[inex][outzn]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(rhorat[inex][outzn])));
             num.out.noz.o12.setText(String.valueOf(filter3(rsro[inex][outzn])));
             num.out.noz.o15.setText(String.valueOf(filter3(pm[inex][outzn]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(turn[inex][outzn]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mang[inex][outzn]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(ptrat[inex][outzn]))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(ptpto[inex][outzn]))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(shkang[inex][outzn]))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mach1[inex][outzn]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(pres[inex][outzn]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(temp[inex][outzn]))) ;
          }
          num.out.noz.o7.setText(String.valueOf(filter3(mcQ[row][col]))) ;
          num.out.noz.o8.setText(String.valueOf(filter3(mcR[row][col]))) ;
          num.out.noz.o3.setText(String.valueOf(filter3(mcx[row][col]))) ;
          num.out.noz.o4.setText(String.valueOf(filter3(mcy[row][col]))) ;
          num.out.noz.o17.setText(String.valueOf(filter3(mcal[row][col]))) ;
          num.out.noz.o18.setText(String.valueOf(filter3(mcbe[row][col]))) ;
          num.out.nozp.o31.setText(String.valueOf(filter3(xexit))) ;
          num.out.nozp.o41.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o42.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o47.setText(String.valueOf(filter3(aboat))) ;
          num.out.nozp.o48.setText(String.valueOf(filter3(dragab))) ;
      }

      if (prob >= 7) {  // wedge nozzle -  points
          if (inex == 0) {
             num.out.noz.o1.setText(String.valueOf(filter3(mcmach[row][col]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(mcdefl[row][col]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(mcprat[row][col]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(mcpp0[row][col]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(mctrat[row][col]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(mctt0[row][col]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(mcrrat[row][col])));
             num.out.noz.o12.setText(String.valueOf(filter3(mcrr0[row][col])));
             num.out.noz.o16.setText(String.valueOf(filter3(mcarat[row][col]))) ;
             num.out.noz.o15.setText(String.valueOf(filter3(mcpm[row][col]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(mcturn[row][col]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mcmang[row][col]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(1.0))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(0.0))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mcmach[row][col-1]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(mcpres[row][col]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(mctemp[row][col]))) ;
          }
/*
          if (inex >= 1) {
             outzn = num.out.noz.zonch.getSelectedIndex() ;
             num.out.noz.o1.setText(String.valueOf(filter3(mach2[inex][outzn]))) ;
             num.out.noz.o2.setText(String.valueOf(filter3(defl[inex][outzn]))) ;
             num.out.noz.o5.setText(String.valueOf(filter3(prat[inex][outzn]))) ;
             num.out.noz.o6.setText(String.valueOf(filter3(pspo[inex][outzn]))) ;
             num.out.noz.o9.setText(String.valueOf(filter3(trat[inex][outzn]))) ;
             num.out.noz.o10.setText(String.valueOf(filter3(tsto[inex][outzn]))) ;
             num.out.noz.o11.setText(String.valueOf(filter3(rhorat[inex][outzn])));
             num.out.noz.o12.setText(String.valueOf(filter3(rsro[inex][outzn])));
             num.out.noz.o15.setText(String.valueOf(filter3(pm[inex][outzn]))) ;
             num.out.noz.o13.setText(String.valueOf(filter3(turn[inex][outzn]))) ;
             num.out.noz.o14.setText(String.valueOf(filter3(mang[inex][outzn]))) ;
             num.out.noz.o37.setText(String.valueOf(filter3(ptrat[inex][outzn]))) ;
             num.out.noz.o38.setText(String.valueOf(filter3(ptpto[inex][outzn]))) ;
             num.out.noz.o39.setText(String.valueOf(filter3(shkang[inex][outzn]))) ;
             num.out.noz.o26.setText(String.valueOf(filter3(mach1[inex][outzn]))) ;
             num.out.noz.o45.setText(String.valueOf(filter3(pres[inex][outzn]))) ;
             num.out.noz.o46.setText(String.valueOf(filter0(temp[inex][outzn]))) ;
          }
*/
          num.out.noz.o7.setText(String.valueOf(filter3(mcQ[row][col]))) ;
          num.out.noz.o8.setText(String.valueOf(filter3(mcR[row][col]))) ;
          num.out.noz.o3.setText(String.valueOf(filter3(mcx[row][col]))) ;
          num.out.noz.o4.setText(String.valueOf(filter3(mcy[row][col]))) ;
          num.out.noz.o17.setText(String.valueOf(filter3(mcal[row][col]))) ;
          num.out.noz.o18.setText(String.valueOf(filter3(mcbe[row][col]))) ;
          num.out.nozp.o31.setText(String.valueOf(filter3(xexit))) ;
          num.out.nozp.o41.setText(String.valueOf(filter3(mexit))) ;
          num.out.nozp.o42.setText(String.valueOf(filter3(machend))) ;
          num.out.nozp.o47.setText(String.valueOf(filter3(aboat))) ;
          num.out.nozp.o48.setText(String.valueOf(filter3(dragab))) ;

          num.out.nozp.o31.setText(String.valueOf(filter3(xexit))) ;
          num.out.noz.o49.setText(String.valueOf(group))  ;
          num.out.noz.o51.setText(String.valueOf(numd)) ;
          num.out.noz.o52.setText(String.valueOf(filter3(nprat))) ;
      }

      num.out.nozp.o22.setText(String.valueOf(filter3(ps0))) ;
      num.out.nozp.o23.setText(String.valueOf(filter3(pexit))) ;
      num.out.nozp.o29.setText(String.valueOf(filter0(texit))) ;
      num.out.nozp.o30.setText(String.valueOf(filter0(uexit))) ;
      num.out.nozp.o34.setText(String.valueOf(filter3(plocal))) ;
      num.out.nozp.o35.setText(String.valueOf(filter3(pexit - ps0))) ;
      num.out.nozp.o24.setText(String.valueOf(filter3(pexit/plocal))) ;
      num.out.nozp.o25.setText(String.valueOf(filter3(mlocal))) ; 
      num.out.nozp.o27.setText(String.valueOf(filter3(wflow))) ;
      if (fgros > 10.0) {
          num.out.nozp.o21.setText(String.valueOf(filter0(fgros))) ;
          num.out.nozp.o49.setText(String.valueOf(filter0(fnet))) ;
      }
      else {
         num.out.nozp.o21.setText(String.valueOf(filter3(fgros))) ;
         num.out.nozp.o49.setText(String.valueOf(filter3(fnet))) ;
      }

      num.out.nozp.o43.setText(String.valueOf(filter3(nzar))) ;
      num.out.nozp.o40.setText(String.valueOf(filter3(nzw))) ;
      num.out.nozp.o28.setText(String.valueOf(filter3(arat)))  ;
      num.out.nozp.o33.setText(String.valueOf(filter3(nzht/2.0))) ;
      if(prob >= 4) {
         num.out.nozp.o28.setText(String.valueOf(filter3(mcarat[1][numray])))  ;
         num.out.nozp.o33.setText(String.valueOf(filter3(nzht))) ;
      }
      if(prob >= 6) num.out.nozp.o28.setText(String.valueOf(filter3(aastr)))  ; 
      num.out.nozp.o36.setText(String.valueOf(filter3(aexit))) ;
      num.out.nozp.o32.setText(String.valueOf(filter3(aexit/nzw))) ;
      if(plumopt == 0) num.out.nozp.o44.setText(String.valueOf(filter3(0.0))) ;
      if(plumopt >= 1) num.out.nozp.o44.setText(String.valueOf(filter3(pplume))) ;
      num.out.nozp.o50.setText(String.valueOf(filter3(nuexit))) ;

//  diagnostics
       num.out.diag.o1.setText(String.valueOf(filter3(mcaxi1[row][col]))) ;
       num.out.diag.o2.setText(String.valueOf(filter3(mcaxi2[row][col]))) ;
       num.out.diag.o3.setText(String.valueOf(row)) ;
       num.out.diag.o4.setText(String.valueOf(col)) ;
       num.out.diag.o5.setText(String.valueOf(filter3(mcpm[row][col]))) ;
       num.out.diag.o6.setText(String.valueOf(filter3(mcturn[row][col]))) ;
       num.out.diag.o7.setText(String.valueOf(filter3(mcaxi1[row][col]/convdr))) ;
       num.out.diag.o8.setText(String.valueOf(filter3(mcaxi2[row][col]/convdr))) ;
       num.out.diag.o9.setText(String.valueOf(filter3(mcx[row][col]))) ;
       num.out.diag.o10.setText(String.valueOf(filter3(mcy[row][col]))) ;
       num.out.diag.o11.setText(String.valueOf(filter3(exnd[3]))) ;
       num.out.diag.o12.setText(String.valueOf(filter3(eynd[3]))) ;
       num.out.diag.o15.setText(String.valueOf(filter3(exnd[4]))) ;
       num.out.diag.o16.setText(String.valueOf(filter3(eynd[4]))) ;
 
      return ;
   }

   public float filter0(double inumbr) {
     //  output only to 1
       float number ;
       int intermed ;

       intermed = (int) (inumbr) ;
       number = (float) (intermed);
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
 
  class Num extends Panel {
     Moc outerparent ;
     Inp inp ;
     Out out ;

     Num (Moc target) {                           
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;
 
          inp = new Inp(outerparent) ;  
          out = new Out(outerparent) ;
 
          add(inp) ;
          add(out) ;
     }

     public Insets insets() {
        return new Insets(5,5,5,5) ;
     }

     class Inp extends Panel {
       Moc outerparent ;
       Intern intern ;
       Extern extern ;
       Anlpan anlpan ;

       Inp (Moc target) {                             
          outerparent = target ;
          layin = new CardLayout() ;
          setLayout(layin) ;

          intern = new Intern(outerparent) ; 
          extern = new Extern(outerparent) ;
          anlpan = new Anlpan(outerparent) ;

          add ("third", anlpan) ;
          add ("first", intern) ;
          add ("second", extern) ;
       }
   
       class Intern extends Panel {
          Moc outerparent ; 
          Inright inright ;
          Inleft inleft ;
 
          Intern (Moc target) {                                                       
             outerparent = target ;
             setLayout(new GridLayout(1,2,2,2)) ;

             inleft = new Inleft(outerparent) ; 
             inright = new Inright(outerparent) ;
 
             add(inleft) ;
             add(inright) ;
         }

         class Inright extends Panel {
            Moc outerparent ;
            Label l1,l3,l4 ;
            Choice plntch ;
            Scrollbar s3,s4,s5,s7,s11,s13,s14;
  
            Inright (Moc target) {
             int i3,i4,i5,i7,i11,i13,i14 ; 

             outerparent = target ;
             setLayout(new GridLayout(10,1,2,2)) ;

             i3 = (int) (((nzar - nzarlo)/(nzarhi - nzarlo))*1000.) ;
             i4 = (int) (((pto - ptolo)/(ptohi - ptolo))*1000.) ;
             i5 = (int) (((tto - ttolo)/(ttohi - ttolo))*1000.) ;
             i7 = (int) (((nzlg - nzlglo)/(nzlghi - nzlglo))*1000.) ;
             i11 = (int) (((machend - machlo)/(machhi - machlo))*1000.) ;
             i13 = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;
             i14 = (int) (((ang2 - ang2mn)/(ang2mx - ang2mn))*1000.) ;

             s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
             s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);
             s5 = new Scrollbar(Scrollbar.HORIZONTAL,i5,10,0,1000);
             s7 = new Scrollbar(Scrollbar.HORIZONTAL,i7,10,0,1000);
             s11 = new Scrollbar(Scrollbar.HORIZONTAL,i11,10,0,1000);
             s13 = new Scrollbar(Scrollbar.HORIZONTAL,i13,10,0,1000);
             s14 = new Scrollbar(Scrollbar.HORIZONTAL,i14,10,0,1000);
             s14.hide() ;
    
             add(new Label(" ", Label.RIGHT)) ;
             add(s11) ;
             add(s3) ;
             add(s13) ;
             add(s7) ;
             add(s4) ;  
             add(s5) ;
             add(new Label(" ", Label.RIGHT)) ; 
             add(s14) ; 
             add(new Label(" ", Label.RIGHT)) ; 
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
                int i3, i4, i5, i7, i11, i13,i14 ;
                Double V2 ;
                double v2, v3, v4, v5, v7, v11, v13, v14 ;
                float fl1  ;
 // pto
                i4 = s4.getValue() ;
                v4 = i4 * (ptohi - ptolo)/ 1000. + ptolo ;
                fl1 = (float) v4 ;
                inleft.f4.setText(String.valueOf(fl1)) ;
                pto = v4 ;
 // tto
                i5 = s5.getValue() ;
                v5 = i5 * (ttohi - ttolo)/ 1000. + ttolo ;
                fl1 = (float) v5 ;
                inleft.f5.setText(String.valueOf(fl1)) ;
                tto = v5 ;
 // nzlg
                i7 = s7.getValue() ;
                v7 = i7 * (nzlghi - nzlglo)/ 1000. + nzlglo ;
                fl1 = (float) v7 ;
                inleft.f7.setText(String.valueOf(filter3(fl1))) ;
                nzlg = v7 ;
 // mach
                i11 = s11.getValue() ;
                v11 = i11 * (machhi - machlo)/ 1000. + machlo ;
                fl1 = (float) v11 ;
                inleft.f11.setText(String.valueOf(fl1)) ;
                machend = v11 ;
 // nz area
                i3 = s3.getValue() ;
                v3 = i3 * (nzarhi - nzarlo)/ 1000. + nzarlo ;
                fl1 = (float) v3 ;
                inleft.f3.setText(String.valueOf(filter3(fl1))) ;
                nzar = v3 ;
                nzht = nzar / nzw ;
                if (prob == 2 || prob == 8) {
                   nzht= 2.0 * Math.sqrt(nzar/ pi) ;
                   nzw = nzht ;
                   i13 = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;
                   s13.setValue(i13) ;     
                   fl1 = (float) nzw ;
                   inleft.f13.setText(String.valueOf(filter3(fl1))) ;
                }
 // nz width
                if (prob < 2 || (prob >2 && prob <8)) {
                   i13 = s13.getValue() ;
                   v13 = i13 * (nzwhi - nzwlo)/ 1000. + nzwlo ;
                   fl1 = (float) v13 ;
                   inleft.f13.setText(String.valueOf(fl1)) ;
                   nzw = v13 ;
                   nzar = nzht * nzw ;

                   i3 = (int) (((nzar - nzarlo)/(nzarhi - nzarlo))*1000.) ;
                   s3.setValue(i3) ;     
                   fl1 = (float) nzar ;
                   inleft.f3.setText(String.valueOf(filter3(fl1))) ;
                }
 // angle
                i14 = s14.getValue() ;
                v14 = i14 * (ang2mx - ang2mn)/ 1000. + ang2mn ;
                fl1 = (float) v14 ;
                inleft.f14.setText(String.valueOf(fl1)) ;
                ang2 = v14 ;


                comPute() ;    
             }

          }  // end Right

          class Inleft extends Panel {
             Moc outerparent ;
             TextField f2, f3,f4,f5,f7,f11,f13,f14;
             Label l1,l2,l3,l4,l13,l14 ;
             Choice gamch ;

             Inleft (Moc target) {
                outerparent = target ;
                setLayout(new GridLayout(10,2,2,2)) ;

                f2 = new TextField(String.valueOf(filter3(gamma)),5) ;
                f2.setBackground(Color.black) ;
                f2.setForeground(Color.yellow) ;

                f3 = new TextField(String.valueOf(filter3(nzar)),5) ;
                f4 = new TextField(String.valueOf(filter3(pto)),5) ;
                f5 = new TextField(String.valueOf(filter3(tto)),5) ;
                f7 = new TextField(String.valueOf(nzlg),5) ;
                f11 = new TextField(String.valueOf(machend),5) ;
                f13 = new TextField(String.valueOf(nzw),5) ;
                f14 = new TextField(String.valueOf(ang2),5) ;
                f14.hide() ;

                l2 = new Label("Nozzle Design", Label.LEFT) ;
                l2.setForeground(Color.blue) ;
                l14 = new Label("Wedge Angle", Label.CENTER) ;
                l14.hide() ;
                l3 = new Label("Area thrt -sq in", Label.CENTER) ;
                l13 = new Label("Width -in", Label.CENTER) ;

                gamch = new Choice() ;
                gamch.setBackground(Color.white) ;
                gamch.setForeground(Color.black) ;
                gamch.addItem("Gamma") ;
                gamch.addItem("Gamma (T)");
                gamch.select(1) ;

                add(l2) ;  
                add(new Label(" ", Label.CENTER)) ; 

                add(new Label("Design Mach", Label.CENTER)) ;  
                add(f11) ;

                add(l3) ; 
                add(f3) ;

                add(l13) ;
                add(f13) ;

                add(new Label(" Up Lngth ", Label.CENTER)) ;
                add(f7) ;

                add(new Label("P Total- psi ", Label.CENTER)) ;  
                add(f4) ;  

                add(new Label("T Total- R ", Label.CENTER)) ;  
                add(f5) ; 

                add(gamch) ;  
                add(f2) ;

                add(l14) ;
                add(f14) ; 

                add(new Label(" ", Label.CENTER)) ;  
                add(new Label(" ", Label.CENTER)) ;  
             } 

             public boolean action(Event evt, Object arg) {
                 if(evt.target instanceof TextField) {
                     this.handleText(evt) ;
                     return true ;
                 }
                 if(evt.target instanceof Choice) {
                     this.handleCho(evt)  ;
                     return true ;
                 }
                 else return false ;
             }

              public void handleCho(Event evt) {
                 gamopt = gamch.getSelectedIndex() ;

                 if (gamopt == 1) {
                     num.inp.intern.inleft.f2.setBackground(Color.black) ;
                     num.inp.intern.inleft.f2.setForeground(Color.yellow) ;
                 }
                 if (gamopt == 0) {
                     num.inp.intern.inleft.f2.setBackground(Color.white) ;
                     num.inp.intern.inleft.f2.setForeground(Color.black) ;
                 }
 
                 comPute() ;
              }
 
             public void handleText(Event evt) {
                  Double V2,V3,V4,V5,V7,V11,V13,V14;
                  double v2,v3,v4,v5,v7,v11,v13,v14 ;
                  float fl1,fl2 ;
                  int i1,i3,i4,i5,i6,i7,i11,i12,i13,i14 ;

         // Gamma - range from 1.0 to 2.0
                  if (gamopt == 0) {
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
                     gamma = v2 ;
                   }

         // Nozzle area & height
                   V3 = Double.valueOf(f3.getText()) ;
                   v3 = V3.doubleValue() ;
                   if(v3 < nzarlo) {
                      v3 = nzarlo ;
                      fl2 = (float) v3 ;
                      f3.setText(String.valueOf(filter3(fl2))) ;
                   }
                   if(v3 > nzarhi) {
                      v3 =nzarhi ;
                      fl2 = (float) v3 ;
                      f3.setText(String.valueOf(filter3(fl2))) ; 
                   }  
                   i3 = (int) (((v3 - nzarlo)/(nzarhi - nzarlo))*1000.) ;
                   inright.s3.setValue(i3) ;     
                   nzar = v3 ;
                   nzht = nzar /nzw ;
                   if (prob == 2 || prob == 8) {
                      nzht= 2.0 * Math.sqrt(nzar/ pi) ;
                      nzw = nzht ;
                      i13 = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;
                      inright.s13.setValue(i13) ;     
                      fl1 = (float) nzw ;
                      inleft.f13.setText(String.valueOf(filter3(fl1))) ;
                   }

         //          nzht= 2.0 * Math.sqrt(nzar/ pi) ;

         // Nozzle width
                   V13 = Double.valueOf(f13.getText()) ;
                   v13 = V13.doubleValue() ;
                   if(v13 < nzwlo) {
                      v13 = nzwlo ;
                      fl2 = (float) v13 ;
                      f13.setText(String.valueOf(filter3(fl2))) ;
                   }
                   if(v13 > nzwhi) {
                      v13 =nzwhi ;
                      fl2 = (float) v13 ;
                      f13.setText(String.valueOf(filter3(fl2))) ; 
                   }  
                   i13 = (int) (((v13 - nzwlo)/(nzwhi - nzwlo))*1000.) ;
                   inright.s13.setValue(i13) ;     
                   nzw = v13 ;
                   nzar = nzw * nzht ;
                       i3 = (int) (((nzar - nzarlo)/(nzarhi - nzarlo))*1000.) ;
                       inright.s3.setValue(i3) ;     
                       fl1 = (float) nzar ;
                       f3.setText(String.valueOf(filter3(fl1))) ;

         // total pressure - range from ptolo to ptohi
                   V4 = Double.valueOf(f4.getText()) ;
                   v4 = V4.doubleValue() ;
                   if(v4 < ptolo) {
                     v4 = ptolo ;
                     fl1 = (float) v4 ;
                     f4.setText(String.valueOf(fl1)) ;
                   }
                   if(v4 > ptohi) {
                      v4 = ptohi ;
                      fl1 = (float) v4 ;
                      f4.setText(String.valueOf(fl1)) ;
                   }
                   i4 = (int) (((v4 - ptolo)/(ptohi - ptolo))*1000.) ;
                   inright.s4.setValue(i4) ;
                   pto = v4 ;

         // total temperature - range from ttolo to ttohi
                   V5 = Double.valueOf(f5.getText()) ;
                   v5 = V5.doubleValue() ;
                   if(v5 < ttolo) {
                     v5 = ttolo ;
                     fl1 = (float) v5 ;
                     f5.setText(String.valueOf(fl1)) ;
                   }
                   if(v5 > ttohi) {
                      v5 = ttohi ;
                      fl1 = (float) v5 ;
                      f5.setText(String.valueOf(fl1)) ;
                   }
                   i5 = (int) (((v5 - ttolo)/(ttohi - ttolo))*1000.) ;
                   inright.s5.setValue(i5) ;
                   tto = v5 ;

        // nozzle length  for graphics
                V7 = Double.valueOf(f7.getText()) ;
                v7 = V7.doubleValue() ;
                if(v7 < nzlglo) {
                  v7 = nzlglo ;
                  fl1 = (float) v7 ;
                  f7.setText(String.valueOf(fl1)) ;
                }
                if(v7 > nzlghi) {
                   v7 =  nzlghi ;
                   fl1 = (float) v7 ;
                   f7.setText(String.valueOf(fl1)) ;
                } 
       
                i7 = (int) (((v7 - nzlglo)/(nzlghi-nzlglo))*1000.) ;
                inright.s7.setValue(i7) ;
                nzlg = v7 ;

         // Design Mach number - range from machlo to machhi

                   V11 = Double.valueOf(f11.getText()) ;
                   v11 = V11.doubleValue() ;
                   if(v11 < machlo) {
                     v11 = machlo+ .02 ;
                     fl1 = (float) v11 ;
                     f11.setText(String.valueOf(fl1)) ;
                   }
                   if(v11 > machhi) {
                      v11 = machhi ;
                      fl1 = (float) v11 ;
                      f11.setText(String.valueOf(fl1)) ;
                   }
                   i11 = (int) (((v11 - machlo)/(machhi - machlo))*1000.) ;
                   inright.s11.setValue(i11) ;
                   machend = v11 ;

        // wedge angle
                V14 = Double.valueOf(f14.getText()) ;
                v14 = V14.doubleValue() ;
                if(v14 < ang2mn) {
                  v14 = ang2mn ;
                  fl1 = (float) v14 ;
                  f14.setText(String.valueOf(fl1)) ;
                }
                if(v14 > ang2mx) {
                   v14 =  ang2mx ;
                   fl1 = (float) v14 ;
                   f14.setText(String.valueOf(fl1)) ;
                } 
       
                i14 = (int) (((v14 - ang2mn)/(ang2mx-ang2mn))*1000.) ;
                inright.s14.setValue(i14) ;
                ang2 = v14 ;

                 comPute() ;
              } // end handle Text
           }  // end Left
        } // end intern

       class Extern extends Panel {
          Moc outerparent ; 
          Inright inright ;
          Inleft inleft ;
 
          Extern (Moc target) {                                                       
             outerparent = target ;
             setLayout(new GridLayout(1,2,2,2)) ;

             inleft = new Inleft(outerparent) ; 
             inright = new Inright(outerparent) ;
 
             add(inleft) ;
             add(inright) ;
         }

         class Inright extends Panel {
            Moc outerparent ;
            Label l1,l3,l4 ;
            Choice plumech ;
            Scrollbar s1,s6,s12 ;
  
            Inright (Moc target) {
             int i1,i6,i12; 

             outerparent = target ;
             setLayout(new GridLayout(8,1,2,2)) ;

             i1 = (int) (((mach0 - machlo)/(machhi - machlo))*1000.) ;
             i6 = (int) (((alt - altlo)/(althi - altlo))*1000.) ;
             i12 = (int) (((ang1 - angmn)/(angmx - angmn))*1000.) ;

             s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
             s6 = new Scrollbar(Scrollbar.HORIZONTAL,i6,10,0,1000);
             s12 = new Scrollbar(Scrollbar.HORIZONTAL,i12,10,0,1000);

             plumech = new Choice() ;
             plumech.setBackground(Color.blue) ;
             plumech.setForeground(Color.white) ;
             plumech.addItem("No Plume") ;
             plumech.addItem("Plume into Static");
             plumech.addItem("Plume into Supersonic");
             plumech.select(0) ;   

             add(new Label(" ", Label.RIGHT)) ;  
             add(plumech) ; 
             add(new Label(" ", Label.RIGHT)) ; 
             add(s1) ;
             add(s6) ;
             add(s12) ;
             add(new Label(" ", Label.RIGHT)) ; 
             add(new Label(" ", Label.RIGHT)) ; 

          }

             public boolean handleEvent(Event evt) {
                if(evt.target instanceof Choice) {
                     this.handleCho(evt)  ;
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
                int i1,  i6, i12 ;
                Double V2 ;
                double v1, v6, v12 ;
                float fl1  ;

                i1 = s1.getValue() ;
                v1 = i1 * (machhi - machlo)/ 1000. + machlo ;
                fl1 = (float) v1 ;
                inleft.f1.setText(String.valueOf(fl1)) ;
                mach0 = v1 ;
 
                i6 = s6.getValue() ;
                v6 = i6 * (althi - altlo)/ 1000. + altlo ;
                fl1 = (float) v6 ;
                inleft.f6.setText(String.valueOf(fl1)) ;
                alt = v6 ;

                i12 = s12.getValue() ;
                v12 = i12 * (angmx - angmn)/ 1000. + angmn ;
                fl1 = (float) v12 ;
                inleft.f12.setText(String.valueOf(fl1)) ;
                ang1 = v12 ;
                comPute() ;    
             }

              public void handleCho(Event evt) {
                int i1 ;
                double v1 ;
                float fl1  ;

                 plumopt = plumech.getSelectedIndex() ;

                 if(plumopt == 1) mach0 = 0.0 ;
                 if(plumopt == 2) mach0 = machdef ;

                 if(plumopt > 0) {
                    v1 = mach0 ;
                    fl1 = (float) v1 ;
                    inleft.f1.setText(String.valueOf(fl1)) ;
                    i1 = (int) (((v1 - machlo)/(machhi - machlo))*1000.) ;
                    inright.s1.setValue(i1) ;
                 }

                 comPute() ;
              }

          }  // end Right

          class Inleft extends Panel {
             Moc outerparent ;
             TextField f1, f6,f10,f12;
             Label l1,l2,l3,l4,l8 ;

             Inleft (Moc target) {
                outerparent = target ;
                setLayout(new GridLayout(8,2,2,2)) ;

                f1 = new TextField(String.valueOf(mach0),5) ;
                f6 = new TextField(String.valueOf(filter3(alt)),5) ;
                f10 = new TextField(String.valueOf(ncycle),5) ;
                f12 = new TextField(String.valueOf(ang1),5) ;

                l1 = new Label("Free Stream", Label.LEFT) ;
                l1.setForeground(Color.blue) ;

                add(l1) ; 
                add(new Label(" ", Label.CENTER)) ; 

                add(new Label("Plume Cycles ", Label.CENTER)) ;
                add(f10) ;

                add(new Label(" ", Label.CENTER)) ; 
                add(new Label(" ", Label.CENTER)) ; 

                add(new Label("External Mach", Label.CENTER)) ;  
                add(f1) ;

                add(new Label("Altitude - ft", Label.CENTER)) ;  
                add(f6) ;

                add(new Label("Ext Cowl Angle", Label.CENTER)) ;  
                add(f12) ;

                add(new Label(" ", Label.CENTER)) ; 
                add(new Label(" ", Label.CENTER)) ; 

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
                  Double V1,V6, V10, V12 ;
                  double v1,v6,v10,v12 ;
                  float fl1,fl2 ;
                  int i1,i6,i12 ;

         // Free Stream Mach number - range from machlo to machhi
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
                   i1 = (int) (((v1 - machlo)/(machhi - machlo))*1000.) ;
                   inright.s1.setValue(i1) ;
                   mach0 = v1 ;
         // altitude - range from altlo to althi
                   V6 = Double.valueOf(f6.getText()) ;
                   v6 = V6.doubleValue() ;
                   if(v6 < altlo) {
                     v6 = altlo ;
                     fl1 = (float) v6 ;
                     f6.setText(String.valueOf(fl1)) ;
                   }
                   if(v6 > althi) {
                      v6 = althi ;
                      fl1 = (float) v6 ;
                      f6.setText(String.valueOf(fl1)) ;
                   }
                   i6 = (int) (((v6 - altlo)/(althi - altlo))*1000.) ;
                   inright.s6.setValue(i6) ;
                   alt = v6 ;

         // External Angle - range from angmn to angmx
                   V12 = Double.valueOf(f12.getText()) ;
                   v12 = V12.doubleValue() ;
                   if(v12 < angmn) {
                     v12 = angmn ;
                     fl1 = (float) v12 ;
                     f12.setText(String.valueOf(fl1)) ;
                   }
                   if(v12 > angmx) {
                      v12 = angmx ;
                      fl1 = (float) v12 ;
                      f12.setText(String.valueOf(fl1)) ;
                   }
                   i12 = (int) (((v12 - angmn)/(angmx - angmn))*1000.) ;
                   inright.s12.setValue(i12) ;
                   ang1 = v12 ;

         // number of cycles of exhaust plume
                V10 = Double.valueOf(f10.getText()) ;
                v10 = V10.doubleValue() ;
                ncycle = (int) v10 ;

                  comPute() ;
              } // end handle Text
           }  // end Left
        } // end extern

        class Anlpan extends Panel {
           Moc outerparent ; 
           Inleft inleft ;
           Inright inright ; 

           Anlpan (Moc target) {                                                       
              outerparent = target ;
              setLayout(new GridLayout(1,2,2,2)) ;

              inleft = new Inleft(outerparent) ; 
              inright = new Inright(outerparent) ;
 
              add(inleft) ;
              add(inright) ;
          }

             class Inright extends Panel {
                Moc outerparent ;
                Label l1,l3,l4 ;
                Choice probch ;
                Scrollbar s8,s14;
  
                Inright (Moc target) {
                  int i8,i14 ; 

                  outerparent = target ;
                  setLayout(new GridLayout(9,1,2,2)) ;

                  i8 = (int) (((delx - delxlo)/(delxhi - delxlo))*1000.) ;
                  i14 = (int) (((perint - perlo)/(perhi - perlo))*1000.) ;

                  s8 = new Scrollbar(Scrollbar.HORIZONTAL,i8,10,0,1000);
                  s14 = new Scrollbar(Scrollbar.HORIZONTAL,i14,10,0,1000);

                  l3 = new Label("  Version 1.7c", Label.LEFT) ;
                  l3.setForeground(Color.red) ;

                  probch = new Choice() ;
                  probch.addItem(" 2D Ideal - Field Method");
                  probch.addItem(" 2D Ideal - Points");
                  probch.addItem(" Axi Ideal - Points");
                  probch.addItem("Jet into Static Field");
                  probch.addItem(" 2D External Plug ") ;
                  probch.addItem(" 2D In/Ex Plug - C.C. Lee") ;
                  probch.addItem(" 2D In/Ex Plug - Flat Cowl") ;
                  probch.addItem(" 2D Wedge - Points") ;
                  probch.addItem(" Axi Wedge - Points") ;
                  probch.setBackground(Color.white) ;
                  probch.setForeground(Color.blue) ;
                  probch.select(0) ;

                  add(l3) ; 
                  add(probch) ;   
                  add(new Label(" ", Label.RIGHT)) ;
                  add(s8) ;
                  add(s14) ;
                  add(new Label(" ", Label.RIGHT)) ;
                  add(new Label(" ", Label.RIGHT)) ;  
                  add(new Label(" ", Label.RIGHT)) ;  
                  add(new Label(" ", Label.RIGHT)) ; 
             }

             public boolean handleEvent(Event evt) {
                if(evt.target instanceof Choice) {
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

 
             public void handleCho(Event evt) {     // problem
                float fl1,fl2 ;
                int probo,i2,i13 ;

                probo   = probch.getSelectedIndex() ;
                mex = 1.0 ;
/*
                perfout = -1 ;
                geomout = 1 ;
                cbar = -1 ;
                layout.show(out, "sixth")  ;
*/
                if (probo == 0) {   // 2d Nozzle Design - Moc
                   prob = 0;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.show() ;
                   num.out.noz.l20.show() ;
                   num.out.noz.o19.show() ;
                   num.out.noz.o20.show() ;

                   num.out.noz.l3.setText("X low left") ;
                   num.out.noz.l4.setText("Y low left") ;
                   num.out.noz.l7.setText("X up left") ;
                   num.out.noz.l8.setText("Y up left") ;
                   num.out.noz.l17.setText("X up right") ;
                   num.out.noz.l18.setText("Y up right") ;
                   num.out.noz.l19.setText("X low right") ;
                   num.out.noz.l20.setText("Y low right") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 1) {   // 2D Nozzle Design - Moc Point
                   prob = 1;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.hide() ;
                   num.out.noz.l20.hide() ;
                   num.out.noz.o19.hide() ;
                   num.out.noz.o20.hide() ;

                   num.out.noz.l3.setText("X") ;
                   num.out.noz.l4.setText("Y") ;
                   num.out.noz.l7.setText("Q") ;
                   num.out.noz.l8.setText("R") ;
                   num.out.noz.l17.setText("alpha") ;
                   num.out.noz.l18.setText("beta") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 2) {   // Axi Nozzle Design - Moc Point
                   prob = 2;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.hide() ;
                   num.out.noz.l20.hide() ;
                   num.out.noz.o19.hide() ;
                   num.out.noz.o20.hide() ;

                   num.out.noz.l3.setText("X") ;
                   num.out.noz.l4.setText("Y") ;
                   num.out.noz.l7.setText("Q") ;
                   num.out.noz.l8.setText("R") ;
                   num.out.noz.l17.setText("alpha") ;
                   num.out.noz.l18.setText("beta") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Diameter-ex") ;
                   num.out.nozp.lo33.setText("Diameter-th") ;
                   num.out.nozp.lo40.setText("Radius-ex") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Diameter -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.black) ;
                   num.inp.intern.inleft.f13.setForeground(Color.yellow) ;
                   nzht= 2.0 * Math.sqrt(nzar/ pi) ;
                   nzw = nzht ;
                   i13 = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;             
                   num.inp.intern.inright.s13.setValue(i13) ;     
                   fl1 = (float) nzw ;              
                   num.inp.intern.inleft.f13.setText(String.valueOf(filter3(fl1))) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 3) {   // 2d Jet exhaust - over or under expanded
                   prob = 3;
                   desmod = 1 ;
                   plumopt = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;
                   num.out.noz.inexch.select(1) ;
                   num.out.noz.zonch.select(1) ;
                   num.out.noz.zonch.show() ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 4) {   // 2d external plug
                   prob = 4;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.show() ;
                   num.out.noz.l20.show() ;
                   num.out.noz.o19.show() ;
                   num.out.noz.o20.show() ;

                   num.out.noz.l3.setText("X low left") ;
                   num.out.noz.l4.setText("Y low left") ;
                   num.out.noz.l7.setText("X up left") ;
                   num.out.noz.l8.setText("Y up left") ;
                   num.out.noz.l17.setText("X up right") ;
                   num.out.noz.l18.setText("Y up right") ;
                   num.out.noz.l19.setText("X low right") ;
                   num.out.noz.l20.setText("Y low right") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 5) {   // 2d internal/external plug - C.C. Lee
                   prob = 5;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.show() ;
                   num.out.noz.l20.show() ;
                   num.out.noz.o19.show() ;
                   num.out.noz.o20.show() ;

                   num.out.noz.l3.setText("X low left") ;
                   num.out.noz.l4.setText("Y low left") ;
                   num.out.noz.l7.setText("X up left") ;
                   num.out.noz.l8.setText("Y up left") ;
                   num.out.noz.l17.setText("X up right") ;
                   num.out.noz.l18.setText("Y up right") ;
                   num.out.noz.l19.setText("X low right") ;
                   num.out.noz.l20.setText("Y low right") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Radius " ) ;
                   radius = radsav ;
                   fl2 = (float) radius ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((radius - radlo)/(radhi-radlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }
                if (probo == 6) {   // 2d internal/external plug - Flat cowl
                   prob = 6;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.hide() ;
                   num.out.noz.l20.hide() ;
                   num.out.noz.o19.hide() ;
                   num.out.noz.o20.hide() ;

                   num.out.noz.l3.setText("X") ;
                   num.out.noz.l4.setText("Y") ;
                   num.out.noz.l7.setText("Q") ;
                   num.out.noz.l8.setText("R") ;
                   num.out.noz.l17.setText("alpha") ;
                   num.out.noz.l18.setText("beta") ;

                   num.out.nozp.lo23.setText(" P exit") ;
                   num.out.nozp.lo29.setText(" T exit") ;
                   num.out.nozp.lo30.setText(" U exit") ;
                   num.out.nozp.lo41.setText(" M exit") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.hide() ;
                   num.inp.intern.inleft.f14.hide() ;
                   num.inp.intern.inleft.l14.hide() ;
                   num.out.noz.l49.hide() ;
                   num.out.noz.o49.hide() ;
                   num.out.noz.l51.hide() ;
                   num.out.noz.o51.hide() ;
                   num.out.noz.l52.hide() ;
                   num.out.noz.o52.hide() ;
                   num.out.noz.lo2.show() ;
                   num.out.noz.inexch.show() ;
                }

                if (probo == 7) {   // 2D Wedge - Points
                   prob = 7;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.hide() ;
                   num.out.noz.l20.hide() ;
                   num.out.noz.o19.hide() ;
                   num.out.noz.o20.hide() ;

                   num.out.noz.l3.setText("X") ;
                   num.out.noz.l4.setText("Y") ;
                   num.out.noz.l7.setText("Q") ;
                   num.out.noz.l8.setText("R") ;
                   num.out.noz.l17.setText("alpha") ;
                   num.out.noz.l18.setText("beta") ;

                   num.out.nozp.lo23.setText("P ex av") ;
                   num.out.nozp.lo29.setText("T ex av") ;
                   num.out.nozp.lo30.setText("U ex av") ;
                   num.out.nozp.lo41.setText("M ex av") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Y exit") ;
                   num.out.nozp.lo33.setText("Height-th") ;
                   num.out.nozp.lo40.setText("Z exit") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.show() ;
                   num.inp.intern.inleft.f14.show() ;
                   num.inp.intern.inleft.l14.show() ;
                   num.inp.intern.inleft.l13.setText("Width -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.white) ;
                   num.inp.intern.inleft.f13.setForeground(Color.black) ;

                   num.out.noz.l49.show() ;
                   num.out.noz.o49.show() ;
                   num.out.noz.l51.show() ;
                   num.out.noz.o51.show() ;
                   num.out.noz.l52.show() ;
                   num.out.noz.o52.show() ;
                   num.out.noz.lo2.hide() ;
                   num.out.noz.inexch.hide() ;
                }

                if (probo == 8) {   // Axi Wedge - Points
                   prob = 8;
                   desmod = 1 ;
                   num.inp.extern.inright.plumech.select(plumopt) ;

                   num.out.noz.l19.hide() ;
                   num.out.noz.l20.hide() ;
                   num.out.noz.o19.hide() ;
                   num.out.noz.o20.hide() ;

                   num.out.noz.l3.setText("X") ;
                   num.out.noz.l4.setText("Y") ;
                   num.out.noz.l7.setText("Q") ;
                   num.out.noz.l8.setText("R") ;
                   num.out.noz.l17.setText("alpha") ;
                   num.out.noz.l18.setText("beta") ;

                   num.out.nozp.lo23.setText("P ex av") ;
                   num.out.nozp.lo29.setText("T ex av") ;
                   num.out.nozp.lo30.setText("U ex av") ;
                   num.out.nozp.lo41.setText("M ex av") ;

                   num.out.nozp.lo31.setText("X exit") ;
                   num.out.nozp.lo32.setText("Diameter-ex") ;
                   num.out.nozp.lo33.setText("Diameter-th") ;
                   num.out.nozp.lo40.setText("Radius-ex") ;

                   num.inp.anlpan.inleft.l8.setText(" Delx" ) ;
                   delx = delxsav ;
                   fl2 = (float) delx ;
                   num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(fl2))) ;
                   i2 = (int) (((delx - delxlo)/(delxhi-delxlo))*1000.) ;
                   num.inp.anlpan.inright.s8.setValue(i2) ;

                   num.inp.intern.inright.s14.show() ;
                   num.inp.intern.inleft.f14.show() ;
                   num.inp.intern.inleft.l14.show() ;
                   num.inp.intern.inleft.l13.setText("Diameter -in") ;
                   num.inp.intern.inleft.f13.setBackground(Color.black) ;
                   num.inp.intern.inleft.f13.setForeground(Color.yellow) ;
                   nzht= 2.0 * Math.sqrt(nzar/ pi) ;
                   nzw = nzht ;
                   i13 = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;             
                   num.inp.intern.inright.s13.setValue(i13) ;     
                   fl1 = (float) nzw ;              
                   num.inp.intern.inleft.f13.setText(String.valueOf(filter3(fl1))) ;

                   num.out.noz.l49.show() ;
                   num.out.noz.o49.show() ;
                   num.out.noz.l51.show() ;
                   num.out.noz.o51.show() ;
                   num.out.noz.l52.show() ;
                   num.out.noz.o52.show() ;
                   num.out.noz.lo2.hide() ;
                   num.out.noz.inexch.hide() ;
                }

                if (planet == 0) gamma = 1.4;
                if (planet == 1) gamma = 1.29;

                fl2 = (float) gamma ;
                num.inp.intern.inleft.f2.setText(String.valueOf(fl2)) ;

                comPute() ;    
             }

             public void handleBar(Event evt) {
                int  i8, i14 ;
                Double V2 ;
                double v8, v14 ;
                float fl1  ;
 
                if (prob <= 4) {
                   i8 = s8.getValue() ;
                   v8 = i8 * (delxhi - delxlo)/ 1000. + delxlo ;
                   fl1 = (float) v8 ;
                   inleft.f8.setText(String.valueOf(filter3(fl1))) ;
                   delx = v8 ;
                   delxsav = delx ;
                }

                if( prob == 5) {
                   i14 = s14.getValue() ;
                   v14 = i14 * (perhi - perlo)/ 1000. + perlo ;
                   fl1 = (float) v14 ;
                   inleft.f14.setText(String.valueOf(fl1)) ;
                   perint = v14 ;

                   i8 = s8.getValue() ;
                   v8 = i8 * (radhi - radlo)/ 1000. + radlo ;
                   fl1 = (float) v8 ;
                   inleft.f8.setText(String.valueOf(filter3(fl1))) ;
                   radius = v8 ;
                   radsav = radius ;
                }

                if (prob >= 6) {
                   i8 = s8.getValue() ;
                   v8 = i8 * (delxhi - delxlo)/ 1000. + delxlo ;
                   fl1 = (float) v8 ;
                   inleft.f8.setText(String.valueOf(filter3(fl1))) ;
                   delx = v8 ;
                   delxsav = delx ;
                }
                comPute() ;    
             } // end handler
          }  // end Right

          class Inleft extends Panel {
             Moc outerparent ;
             TextField f8,f9,f10,f14;
             Label l1,l2,l3,l4,l8 ;
             Button savit,opnit,savit2,diagbut,geobut ;

             Inleft (Moc target) {
                outerparent = target ;
                setLayout(new GridLayout(9,2,2,2)) ;

                f8 = new TextField(String.valueOf(delx),5) ;
                f9 = new TextField(String.valueOf(numray),5) ;
                f14 = new TextField(String.valueOf(perint),5) ;

                l1 = new Label("Analysis ", Label.CENTER) ;
                l1.setForeground(Color.blue) ;
                l2 = new Label("Problem:", Label.RIGHT) ;
                l2.setForeground(Color.blue) ;
                l3 = new Label("Nozzle Flows", Label.RIGHT) ;
                l3.setForeground(Color.red) ;
                l8 = new Label(" Delx ", Label.CENTER) ;
                l8.setForeground(Color.black) ;

                opnit = new Button("Open Restart") ;
                opnit.setBackground(Color.white) ;
                opnit.setForeground(Color.blue) ;

                diagbut = new Button("Diagnostics") ;
                diagbut.setBackground(Color.blue) ;
                diagbut.setForeground(Color.white) ;

                geobut = new Button("Show Geom") ;
                geobut.setBackground(Color.blue) ;
                geobut.setForeground(Color.white) ;

                savit = new Button("Save Restart") ;
                savit.setBackground(Color.blue) ;
                savit.setForeground(Color.white) ;

//                endit = new Button("Exit") ;
//                endit.setBackground(Color.red) ;
//                endit.setForeground(Color.white) ;

                savit2 = new Button("Save PLOT3D") ;
                savit2.setBackground(Color.blue) ;
                savit2.setForeground(Color.white) ;
 
                add(l1) ;
                add(l3) ;

                add(new Label(" ", Label.CENTER)) ;
                add(l2) ;

                add(new Label(" # of Rays ", Label.CENTER)) ;
                add(f9) ;

                add(l8) ;
                add(f8) ;

                add(new Label("% Int Exp - Plug", Label.CENTER)) ;  
                add(f14) ;

                add(new Label(" ", Label.CENTER)) ;
                add(new Label(" ", Label.CENTER)) ;

                add(opnit) ;
                add(savit) ;

                add(new Label(" ", Label.CENTER)) ;
                add(savit2) ;

                add(diagbut) ;
                add(geobut) ;
             } 

             public boolean action(Event evt, Object arg) {
                 if(evt.target instanceof TextField) {
                     this.handleText(evt) ;
                     return true ;
                 }
                 if(evt.target instanceof Button) {
                     this.handleBut(evt,arg)  ;
                     return true ;
                 }
                 else return false ;
             }

              public void handleBut(Event evt, Object arg) {
                 String label = (String)arg ;
                 int i, j, k ;

                 comPute() ;

                 if(label.equals("Show Geom")) {
                   cbar = -1 ;
                   perfout = -1 ;
                   geomout = -1 ;
                   diout = -1 ;
                   geomr = 1 ;
                   layout.show(num.out, "eigth")  ;
                      num.out.nozg.prnt.appendText("\n ") ;
                      if (prob == 0) num.out.nozg.prnt.appendText("\n 2D Ideal ") ;
                      if (prob == 1) num.out.nozg.prnt.appendText("\n 2D Ideal ") ;
                      if (prob == 2) num.out.nozg.prnt.appendText("\n Axi Ideal ") ;
                      if (prob == 3) num.out.nozg.prnt.appendText("\n Jet Plume ") ;
                      if (prob == 4) num.out.nozg.prnt.appendText("\n External Plug - Rocket ") ;
                      if (prob == 5) num.out.nozg.prnt.appendText("\n Internal/External Plug- Rocket") ;
                      if (prob == 5) num.out.nozg.prnt.appendText("\n      Plug Surface ") ;
                      if (prob == 6) num.out.nozg.prnt.appendText("\n 2D Ideal Plug") ;
                      num.out.nozg.prnt.appendText("\n  X  \t Y \t X  \t Y \t X \t Y ") ;
                      j = numray / 3 ;
                      if (prob <= 3) {
                         for(k=1; k<=j; ++k) {
                            num.out.nozg.prnt.appendText("\n"  
                           + filter3(icxbgn[(k-1)*3 + 1]) + "\t" + filter3(icybgn[(k-1)*3 + 1]) + "\t"
                           + filter3(icxbgn[(k-1)*3 + 2]) + "\t" + filter3(icybgn[(k-1)*3 + 2]) + "\t"
                           + filter3(icxbgn[(k-1)*3 + 3]) + "\t" + filter3(icybgn[(k-1)*3 + 3]) ) ;
                         }
                         i = (numray + 1) - (3 * j) ;
                         num.out.nozg.prnt.appendText("\n ") ;
                         for(k=1; k<=i; ++k) {
                            num.out.nozg.prnt.appendText( 
                           + filter3(icxbgn[3*j + k]) + "\t" + filter3(icybgn[3*j + k]) + "\t" ) ;
                         }
                      }
                      if (prob >= 4 && prob <= 6) {
                         for(k=1; k<=j; ++k) {
                            num.out.nozg.prnt.appendText("\n"  
                           + filter3(wxbgn[(k-1)*3 + 1]) + "\t" + filter3(wybgn[(k-1)*3 + 1]) + "\t"
                           + filter3(wxbgn[(k-1)*3 + 2]) + "\t" + filter3(wybgn[(k-1)*3 + 2]) + "\t"
                           + filter3(wxbgn[(k-1)*3 + 3]) + "\t" + filter3(wybgn[(k-1)*3 + 3]) ) ;
                         }
                         i = (numray + 1) - (3 * j) ;
                         num.out.nozg.prnt.appendText("\n ") ;
                         for(k=1; k<=i; ++k) {
                             num.out.nozg.prnt.appendText( 
                           + filter3(wxbgn[3*j + k]) + "\t" + filter3(wybgn[3*j + k]) + "\t" ) ;
                         }
                      }
                      if (prob == 5) {
                         num.out.nozg.prnt.appendText("\n Internal Cowl Surface ") ;
                         num.out.nozg.prnt.appendText("\n  X  \t Y \t X  \t Y \t X \t Y ") ;
                         j = jexm1 / 3 ;
                         for(k=1; k<=j; ++k) {
                            num.out.nozg.prnt.appendText("\n"  
                              + filter3(icxbgn[(k-1)*3 + 1]) + "\t" + filter3(icybgn[(k-1)*3 + 1]) + "\t"
                              + filter3(icxbgn[(k-1)*3 + 2]) + "\t" + filter3(icybgn[(k-1)*3 + 2]) + "\t"
                              + filter3(icxbgn[(k-1)*3 + 3]) + "\t" + filter3(icybgn[(k-1)*3 + 3]) ) ;
                         }
                         i = (jexm1 + 1) - (3 * j) ;
                         num.out.nozg.prnt.appendText("\n ") ;
                         for(k=1; k<=i; ++k) {
                            num.out.nozg.prnt.appendText( 
                             + filter3(icxbgn[3*j + k]) + "\t" + filter3(icybgn[3*j + k]) + "\t" ) ;
                         }
                      }
                      diagbut.setBackground(Color.blue) ;
                      diagbut.setForeground(Color.white) ;
                      geobut.setBackground(Color.yellow) ;
                      geobut.setForeground(Color.black) ;          
                 }

                 if(label.equals("Diagnostics")) {
                    cbar = -1 ;
                    perfout = -1 ;
                    geomout = -1 ;
                    diout = 1 ;
                    geomr = -1 ;
                    layout.show(num.out, "last")  ;
                    diagbut.setBackground(Color.yellow) ;
                    diagbut.setForeground(Color.black) ;
                    geobut.setBackground(Color.blue) ;
                    geobut.setForeground(Color.white) ;          
                 }

                 if(label.equals("Save PLOT3D")) {
                    saveFil2.show() ;
                    String outFil2 = saveFil2.getFile() ;
                    try {
                        pfile = new FileWriter(outFil2) ;
                        prntGrid = new PrintWriter(pfile) ;
                        prntGrid.print(String.valueOf(numray + 1));
                        prntGrid.print(" 3");
                        prntGrid.println(" 2");
                        for (k=1; k<=2 ; ++k) {
                           for (j=1; j <=3; ++j) {
                             for (i=1; i <=(numray+1); ++i) {
                                prntGrid.println(String.valueOf(filter3(icxbgn[i])));
                             }
                           }
                        }
                        for (k=1; k<=2 ; ++k) {
                           for (j=1; j<=3; ++j) {
                             for (i=1; i<=(numray+1) ; ++i) {
                                if (j==1) {
                                   prntGrid.println(" 0.0000 ");
                                }
                                if (j==2) {
                                    prntGrid.println(String.valueOf(filter3(icybgn[i])));
                                }
                                if (j==3) {
                                    prntGrid.println(String.valueOf(filter3(icybgn[numray+1]))); 
                                }                                    
                             }
                           }
                        }
                        for (k=1; k<=2 ; ++k) {
                           for (j=1; j<=3; ++j) {
                             for (i=1; i<=(numray+1); ++i) {
                                if (k==1) {
                                   prntGrid.println(" 0.0000 ");
                                }
                                if (k==2) {
                                    prntGrid.println(" 1.0000 ");
                                }
                             }
                           }
                        }
                        pfile.close() ;
                     } catch (IOException ex){
                        notifyDialog("Error Writing file") ;
                     }
                 }

                 if(label.equals("Save Restart")) {
                     saveFile.show() ;
                     String outFile = saveFile.getFile() ;
                     try {
                        outStream = new DataOutputStream(new FileOutputStream(outFile)) ;
                        outStream.writeInt(ncycle) ;
                        outStream.writeDouble(delx) ;
                        outStream.writeInt(numray) ;
                        outStream.writeDouble(mach0) ;
                        outStream.writeDouble(gamma) ;
                        outStream.writeDouble(nzar) ;
                        outStream.writeDouble(pto) ;
                        outStream.writeDouble(tto) ;
                        outStream.writeDouble(alt) ;
                        outStream.writeDouble(nzlg) ;
                        outStream.writeDouble(machend) ; 
                        outStream.writeDouble(ang1) ; 
                        outStream.writeDouble(nzw) ; 
                        outStream.writeDouble(ang2) ; 
                        outStream.writeDouble(perint) ; 

                        outStream.writeInt(gamopt) ;
                        outStream.writeInt(prob) ;
                        outStream.writeInt(plumopt) ;
                        outStream.close() ;
                     } catch (IOException ex){
                         notifyDialog("Error Writing file") ;
                     }
                 }

                 if(label.equals("Open Restart")) {
                    openFile.show() ;
                    String inFile = openFile.getFile() ;
                     try {
                        inStream = new DataInputStream(new FileInputStream(inFile)) ;
                        ncycle = inStream.readInt() ;
                        num.inp.extern.inleft.f10.setText(String.valueOf(ncycle)) ;
                        delx = inStream.readDouble() ;
                        num.inp.anlpan.inleft.f8.setText(String.valueOf(filter3(delx))) ;
                        i = (int) (((delx - delxlo)/(delxhi - delxlo))*1000.) ;
                        num.inp.anlpan.inright.s8.setValue(i) ;
                        numray = inStream.readInt() ;
                        num.inp.anlpan.inleft.f9.setText(String.valueOf(numray)) ;
                        mach0 = inStream.readDouble() ;
                        num.inp.extern.inleft.f1.setText(String.valueOf(filter3(mach0))) ;
                        i = (int) (((mach0 - machlo)/(machhi - machlo))*1000.) ;
                        num.inp.extern.inright.s1.setValue(i) ;
                        gamma = inStream.readDouble() ;
                        num.inp.intern.inleft.f2.setText(String.valueOf(filter3(gamma))) ;
                        nzar = inStream.readDouble() ;
                        num.inp.intern.inleft.f3.setText(String.valueOf(filter3(nzar))) ;
                        i = (int) (((nzar - nzarlo)/(nzarhi - nzarlo))*1000.) ;
                        num.inp.intern.inright.s3.setValue(i) ;
                        pto = inStream.readDouble() ;
                        num.inp.intern.inleft.f4.setText(String.valueOf(filter3(pto))) ;
                        i = (int) (((pto - ptolo)/(ptohi - ptolo))*1000.) ;
                        num.inp.intern.inright.s4.setValue(i) ;
                        tto = inStream.readDouble() ;
                        num.inp.intern.inleft.f5.setText(String.valueOf(filter3(tto))) ;
                        i = (int) (((tto - ttolo)/(ttohi - ttolo))*1000.) ;
                        num.inp.intern.inright.s5.setValue(i) ;
                        alt = inStream.readDouble() ;
                        num.inp.extern.inleft.f6.setText(String.valueOf(filter0(alt))) ;
                        i = (int) (((alt - altlo)/(althi - altlo))*1000.) ;
                        num.inp.extern.inright.s6.setValue(i) ;
                        nzlg = inStream.readDouble() ;
                        num.inp.intern.inleft.f7.setText(String.valueOf(filter3(nzlg))) ;
                        i = (int) (((nzlg - nzlglo)/(nzlghi - nzlglo))*1000.) ;
                        num.inp.intern.inright.s7.setValue(i) ;
                        machend = inStream.readDouble() ;
                        num.inp.intern.inleft.f11.setText(String.valueOf(filter3(machend))) ;
                        i = (int) (((machend - machlo)/(machhi - machlo))*1000.) ;
                        num.inp.intern.inright.s11.setValue(i) ;
                        ang1 = inStream.readDouble() ;
                        num.inp.extern.inleft.f12.setText(String.valueOf(filter3(ang1))) ;
                        i = (int) (((ang1 - angmn)/(angmx - angmn))*1000.) ;
                        num.inp.extern.inright.s12.setValue(i) ;
                        nzw = inStream.readDouble() ;
                        num.inp.intern.inleft.f13.setText(String.valueOf(filter3(nzw))) ;
                        i = (int) (((nzw - nzwlo)/(nzwhi - nzwlo))*1000.) ;
                        num.inp.intern.inright.s13.setValue(i) ;
                        ang2 = inStream.readDouble() ;
                        num.inp.intern.inleft.f14.setText(String.valueOf(filter3(ang2))) ;
                        i = (int) (((ang2 - ang2mn)/(ang2mx - ang2mn))*1000.) ;
                        num.inp.intern.inright.s14.setValue(i) ;
                        perint = inStream.readDouble() ;
                        num.inp.anlpan.inleft.f14.setText(String.valueOf(filter3(perint))) ;
                        i = (int) (((perint - perlo)/(perhi - perlo))*1000.) ;
                        num.inp.anlpan.inright.s14.setValue(i) ;

                        gamopt = inStream.readInt() ;
                        num.inp.intern.inleft.gamch.select(gamopt) ;
                        prob = inStream.readInt() ;
                        num.inp.anlpan.inright.probch.select(prob) ;
                        plumopt = inStream.readInt() ;
                        num.inp.extern.inright.plumech.select(plumopt) ;

                        inStream.close() ;
                        comPute() ;
                     } catch (IOException ex){
                         notifyDialog("Error reading file") ;
                     }
                 }

                 return ;
             } 

             public void notifyDialog(String msg) {
                 Notification notification = new Notification(f, msg) ;
                 notification.show() ;
             } 
 
             public void handleText(Event evt) {
                  Double V8,V9,V10,V14 ;
                  double v8,v9,v10,v14 ;
                  float fl1,fl2 ;
                  int i8,i14 ;

         // delx for prob <= 4
                if (prob <= 4) {
                   V8 = Double.valueOf(f8.getText()) ;
                   v8 = V8.doubleValue() ;
                   if(v8 < delxlo) {
                     v8 = delxlo ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   }
                   if(v8 > delxhi) {
                     v8 =  delxhi ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   } 
       
                   i8 = (int) (((v8 - delxlo)/(delxhi-delxlo))*1000.) ;
                   inright.s8.setValue(i8) ;
                   delx = v8 ;
                   delxsav = delx ;
                }

         // radius for prob == 5
                if (prob == 5) {
                   V8 = Double.valueOf(f8.getText()) ;
                   v8 = V8.doubleValue() ;
                   if(v8 < radlo) {
                     v8 = radlo ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   }
                   if(v8 > radhi) {
                     v8 =  radhi ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   } 
       
                   i8 = (int) (((v8 - radlo)/(radhi-radlo))*1000.) ;
                   inright.s8.setValue(i8) ;
                   radius = v8 ;
                   radsav = radius ;
                }

         // delx for prob = 6
                if (prob >= 6) {
                   V8 = Double.valueOf(f8.getText()) ;
                   v8 = V8.doubleValue() ;
                   if(v8 < delxlo) {
                     v8 = delxlo ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   }
                   if(v8 > delxhi) {
                     v8 =  delxhi ;
                     fl1 = (float) v8 ;
                     f8.setText(String.valueOf(fl1)) ;
                   } 
       
                   i8 = (int) (((v8 - delxlo)/(delxhi-delxlo))*1000.) ;
                   inright.s8.setValue(i8) ;
                   delx = v8 ;
                   delxsav = delx ;
                }
         // % Internal expansion - (for inter/external plug nozzle) range from perlo to perhi
                 if(prob == 5) {
                      V14 = Double.valueOf(f14.getText()) ;
                      v14 = V14.doubleValue() ;
                      if(v14 < perlo) {
                        v14 = perlo ;
                        fl1 = (float) v14 ;
                        f14.setText(String.valueOf(fl1)) ;
                      }
                      if(v14 > perhi) {
                         v14 = perhi ;
                         fl1 = (float) v14 ;
                         f14.setText(String.valueOf(fl1)) ;
                      }
                      i14 = (int) (((v14 - perlo)/(perhi - perlo))*1000.) ;
                      inright.s14.setValue(i14) ;
                      perint = v14 ;
                   }
         // number of rays  -- must be an even number
                V9 = Double.valueOf(f9.getText()) ;
                v9 = V9.doubleValue() ;
 
                numray = (int) v9 ;
                if(numray / 2 * 2 < numray) {
                    numray  = numray + 1 ;
                    f9.setText(String.valueOf(numray)) ;
                }

                comPute() ;
              } // end handle Text
            }  // end Left
        } // end anlpan
     } // end Inp

     class Out extends Panel {
       Moc outerparent ;

       Diag diag;
       Noz noz ;
       Nozp nozp ;
       Nozg nozg ;
       Colout colout ;

       Out (Moc target) { 
         outerparent = target ;
         layout = new CardLayout() ;
         setLayout(layout) ;

         diag = new Diag(outerparent) ;
         noz = new Noz(outerparent) ;
         nozp = new Nozp(outerparent) ;
         nozg = new Nozg(outerparent) ;
         colout = new Colout(outerparent) ;

         add ("sixth", noz) ;
         add ("seventh", nozp) ;
         add ("eigth", nozg) ;
         add ("ninth", colout) ;
         add ("last", diag) ;
       }

       class Noz extends Panel {
          TextField o1, o2, o3, o4, o5, o6, o7, o8, o9, o10 ;
          TextField o11, o12, o13, o14, o15, o16, o17, o18, o19, o20 ;
          TextField o21, o22, o23, o24, o25, o26, o27, o28, o29, o30 ;
          TextField o31, o32, o33, o34, o35, o36, o37, o38, o39, o40 ;
          TextField o41, o42, o43, o44, o45, o46, o47, o48, o49, o51, o52;
          Label lo1,lo2,lo3,lo4,lo5 ;
          Label lab1,lab2,lab3;
          Label l3,l4,l7,l8,l17,l18,l19,l20,l49,l51,l52;
          TextField rowch,colch ;
          Button rowp,rowm,colp,colm ;
          Choice zonch,inexch ;

          Noz (Moc target) {
             outerparent = target ;
             setLayout(new GridLayout(13,6,2,2)) ;

             zonch = new Choice() ;
             zonch.setBackground(Color.white) ;
             zonch.setForeground(Color.red) ;
             zonch.addItem("0") ;
             zonch.addItem("1");
             zonch.addItem("2");
             zonch.addItem("3");
             zonch.addItem("4");
             zonch.addItem("5");
             zonch.addItem("6");
             zonch.addItem("7");
             zonch.select(1) ;
             zonch.hide() ;

             lab1 = new Label("Zone:", Label.RIGHT) ;
             lab1.setForeground(Color.red) ;
             lab1.hide() ;

             inexch = new Choice() ;
             inexch.setBackground(Color.white) ;
             inexch.setForeground(Color.red) ;
             inexch.addItem("Internal") ;
             inexch.addItem("External");
             inexch.addItem("Plume");
             inexch.select(0) ;

             lo1 = new Label("MOC ", Label.RIGHT) ;
             lo1.setForeground(Color.blue) ;
             lo5 = new Label("Grid", Label.LEFT) ;
             lo5.setForeground(Color.blue) ;
             lo2 = new Label("Flow", Label.CENTER) ;
             lo2.setForeground(Color.blue) ;

             l3 = new Label("X low left", Label.CENTER) ;
             l4 = new Label("Y low left", Label.CENTER) ;
             l7 = new Label("X up left", Label.CENTER) ;
             l8 = new Label("Y up left", Label.CENTER) ;
             l17 = new Label("X up right", Label.CENTER) ;
             l18 = new Label("Y up right", Label.CENTER) ;
             l19 = new Label("X low right", Label.CENTER) ;
             l20 = new Label("Y low right", Label.CENTER) ;

             o1 = new TextField() ;
             o1.setBackground(Color.black) ;
             o1.setForeground(Color.green) ;
             o2 = new TextField() ;
             o2.setBackground(Color.black) ;
             o2.setForeground(Color.green) ;
             o13 = new TextField() ;
             o13.setBackground(Color.black) ;
             o13.setForeground(Color.green) ;

             o5 = new TextField() ;
             o5.setBackground(Color.black) ;
             o5.setForeground(Color.green) ;
             o6 = new TextField() ;
             o6.setBackground(Color.black) ;
             o6.setForeground(Color.green) ;
             o15 = new TextField() ;
             o15.setBackground(Color.black) ;
             o15.setForeground(Color.green) ;

             o9 = new TextField() ;
             o9.setBackground(Color.black) ;
             o9.setForeground(Color.green) ;
             o10 = new TextField() ;
             o10.setBackground(Color.black) ;
             o10.setForeground(Color.green) ;
             o14 = new TextField() ;
             o14.setBackground(Color.black) ;
             o14.setForeground(Color.green) ;

             o11 = new TextField() ;
             o11.setBackground(Color.black) ;
             o11.setForeground(Color.green) ;
             o12 = new TextField() ;
             o12.setBackground(Color.black) ;
             o12.setForeground(Color.green) ;
             o16 = new TextField() ;
             o16.setBackground(Color.black) ;
             o16.setForeground(Color.green) ;
             o26 = new TextField() ;
             o26.setBackground(Color.black) ;
             o26.setForeground(Color.green) ;
             o45 = new TextField() ;
             o45.setBackground(Color.black) ;
             o45.setForeground(Color.green) ;
             o46 = new TextField() ;
             o46.setBackground(Color.black) ;
             o46.setForeground(Color.green) ;

             o3 = new TextField() ;
             o3.setBackground(Color.black) ;
             o3.setForeground(Color.cyan) ;
             o4 = new TextField() ;
             o4.setBackground(Color.black) ;
             o4.setForeground(Color.cyan) ;

             o7 = new TextField() ;
             o7.setBackground(Color.black) ;
             o7.setForeground(Color.cyan) ;
             o8 = new TextField() ;
             o8.setBackground(Color.black) ;
             o8.setForeground(Color.cyan) ;

             o17 = new TextField() ;
             o17.setBackground(Color.black) ;
             o17.setForeground(Color.cyan) ;
             o18 = new TextField() ;
             o18.setBackground(Color.black) ;
             o18.setForeground(Color.cyan) ;

             o19 = new TextField() ;
             o19.setBackground(Color.black) ;
             o19.setForeground(Color.cyan) ;
             o20 = new TextField() ;
             o20.setBackground(Color.black) ;
             o20.setForeground(Color.cyan) ;

             o49 = new TextField() ;
             o49.setBackground(Color.black) ;
             o49.setForeground(Color.cyan) ;
             o49.hide() ; 
             l49 = new Label("Groups", Label.CENTER) ;
             l49.hide() ; 
             o51 = new TextField() ;
             o51.setBackground(Color.black) ;
             o51.setForeground(Color.cyan) ;
             o51.hide() ; 
             l51 = new Label("Numd", Label.CENTER) ;
             l51.hide() ; 
             o52 = new TextField() ;
             o52.setBackground(Color.black) ;
             o52.setForeground(Color.cyan) ;
             o52.hide() ; 
             l52 = new Label("N Prat", Label.CENTER) ;
             l52.hide() ; 

             o37 = new TextField() ;
             o37.setBackground(Color.black) ;
             o37.setForeground(Color.green) ;
             o38 = new TextField() ;
             o38.setBackground(Color.black) ;
             o38.setForeground(Color.green) ;
             o39 = new TextField() ;
             o39.setBackground(Color.black) ;
             o39.setForeground(Color.green) ;

             rowch = new TextField(String.valueOf(row),5) ;
             rowch.setBackground(Color.white) ;
             rowch.setForeground(Color.black) ;
             colch = new TextField(String.valueOf(col),5) ;
             colch.setBackground(Color.white) ;
             colch.setForeground(Color.black) ;

             lo3 = new Label("Right:", Label.RIGHT) ;
             lo3.setForeground(Color.blue) ;
             lo4 = new Label("Left:", Label.RIGHT) ;
             lo4.setForeground(Color.red) ;

             rowp = new Button("R +") ;
             rowp.setBackground(Color.white) ;
             rowp.setForeground(Color.blue) ;
             rowm = new Button("R -") ;
             rowm.setBackground(Color.white) ;
             rowm.setForeground(Color.blue) ;

             colp = new Button("L +") ;
             colp.setBackground(Color.white) ;
             colp.setForeground(Color.red) ;
             colm = new Button("L -") ;
             colm.setBackground(Color.white) ;
             colm.setForeground(Color.red) ;

             add(lo2) ;  
             add(inexch) ;
             add(new Label(" ", Label.CENTER)) ;    
             add(new Label(" ", Label.CENTER)) ;  
             add(lab1) ;
             add(zonch) ;   

             add(new Label("Mach", Label.CENTER)) ;  
             add(o1) ; 
             add(new Label("Temp", Label.CENTER)) ; 
             add(o46) ; 
             add(new Label("Pressure ", Label.CENTER)) ;
             add(o45) ; 

             add(new Label("Deflect", Label.CENTER)) ; 
             add(o2) ;  
             add(new Label("Turning", Label.CENTER)) ;  
             add(o13) ;
             add(new Label("A/A*", Label.CENTER)) ;
             add(o16) ; 
  
             add(new Label("Mach Up", Label.CENTER)) ;  
             add(o26) ;
             add(new Label("P-M Angle ", Label.CENTER)) ;  
             add(o15) ;   
             add(new Label("Mach-Ang", Label.CENTER)) ;  
             add(o14) ; 
 
             add(new Label("Shock Ang ", Label.CENTER)) ; 
             add(o39) ; 
             add(new Label("pt/pt(up) ", Label.CENTER)) ; 
             add(o37) ;
             add(new Label("pt/pto ", Label.CENTER)) ;
             add(o38) ; 
  
             add(new Label("p/p(up)", Label.CENTER)) ;  
             add(o5) ;
             add(new Label("T/T(up)", Label.CENTER)) ; 
             add(o9) ;
             add(new Label("r/r(up)", Label.CENTER)) ;  
             add(o11) ; 

             add(new Label("p/p0", Label.CENTER)) ;  
             add(o6) ;
             add(new Label("T/T0", Label.CENTER)) ; 
             add(o10) ;
             add(new Label("r/r0", Label.CENTER)) ;  
             add(o12) ;

             add(lo1) ;
             add(lo5) ;
             add(lo3) ;  
             add(rowp) ; 
             add(rowch) ;
             add(rowm) ;

             add(new Label(" ", Label.CENTER)) ;
             add(new Label(" ", Label.CENTER)) ;
             add(lo4) ; 
             add(colp) ;
             add(colch) ;  
             add(colm) ; 

             add(l49) ;
             add(o49) ;
             add(l7) ;  
             add(o7) ;  
             add(l8) ;  
             add(o8) ; 

             add(l51) ;
             add(o51) ;
             add(l17) ;  
             add(o17) ; 
             add(l18) ;  
             add(o18) ; 

             add(l52) ;
             add(o52) ;
             add(l3) ; 
             add(o3) ;
             add(l4) ; 
             add(o4) ;

             add(new Label(" ", Label.CENTER)) ;
             add(new Label(" ", Label.CENTER)) ;
             add(l19) ;  
             add(o19) ; 
             add(l20) ;  
             add(o20) ; 
          }

          public boolean action(Event evt, Object arg) {
              if(evt.target instanceof TextField) {
                    this.handleText(evt) ;
                    return true ;
              }
              if(evt.target instanceof Button) {
                   this.handleBut(evt,arg)  ;
                   return true ;
              }
              if(evt.target instanceof Choice) {
                   this.handleCho(evt,arg)  ;

                 return true ;
              }
              else return false ;
           }

          public void handleCho(Event evt, Object arg) {
              inex = inexch.getSelectedIndex() ;

              if (inex == 0) {
                  lab1.hide() ;
                  zonch.hide() ;
              }
              if (inex == 1) {
                  lab1.show() ;
                  zonch.show() ;
                  outzn  = 1 + zonch.getSelectedIndex() ;
              }
              if (inex == 2) {
                  lab1.show() ;
                  zonch.show() ;
                  outzn  = 1 + zonch.getSelectedIndex() ;
              }
 
              comPute() ;
          }

          public void handleText(Event evt) {
             Double V1,V2,V3,V4,V5 ;
             double v1,v2,v3,v4,v5 ;
             float fl1,fl2 ;
             int i1,i3,i4,i5 ;

         // row index
             V1 = Double.valueOf(rowch.getText()) ;
             v1 = V1.doubleValue() ;
             if(v1 < rowlo) {
                v1 = rowlo ;
                fl1 = (float) v1 ;
                rowch.setText(String.valueOf(fl1)) ;
             }
             if(v1 > rowhi) {
                v1 = rowhi ;
                fl1 = (float) v1 ;
                rowch.setText(String.valueOf(fl1)) ;
             }
             row = (int) v1 ;

         // column index
             V2 = Double.valueOf(colch.getText()) ;
             v2 = V2.doubleValue() ;
             if(v2 < collo) {
                v2 = collo ;
                fl1 = (float) v2 ;
                colch.setText(String.valueOf(fl1)) ;
             }
             if(v2 > colhi) {
                v2 = colhi ;
                fl1 = (float) v2 ;
                colch.setText(String.valueOf(fl1)) ;
             }
             col = (int) v2 ;

             comPute() ;
          }

          public void handleBut(Event evt, Object arg) {
              String label = (String)arg ;

              if(label.equals("R +")) {
                 row = row + 1 ;
                 if(row > rowhi) row = rowhi ;
                 rowch.setText(String.valueOf(row)) ;
              }
              if(label.equals("R -")) {
                 row = row - 1 ;
                 if(row < rowlo) row = rowlo ;
                 rowch.setText(String.valueOf(row)) ;
              }
              if(label.equals("L +")) {
                 col = col + 1 ;
                 if(col > colhi) col = colhi ;
                 colch.setText(String.valueOf(col)) ;
              }
              if(label.equals("L -")) {
                 col = col - 1 ;
                 if(col < collo) col = collo ;
                 colch.setText(String.valueOf(col)) ;
              }
              comPute() ;
              return ;
          }  

        } // end Noz

       class Nozp extends Panel {
          TextField o21, o22, o23, o24, o25, o26, o27, o28, o29, o30 ;
          TextField o31, o32, o33, o34, o35, o36, o37, o38, o39, o40 ;
          TextField o41, o42, o43, o44, o45, o46, o47, o48, o49, o50 ;
          TextField o51, o52, o53 ;
          Label l1,l2,l3 ;
          Label lo23,lo29,lo30,lo31,lo32,lo33,lo40,lo41,lo51;

          Nozp (Moc target) {
             outerparent = target ;
             setLayout(new GridLayout(10,6,2,2)) ;

             l1 = new Label("Nozzle ", Label.RIGHT) ;
             l1.setForeground(Color.blue) ;
             l2 = new Label(" Perform", Label.RIGHT) ;
             l2.setForeground(Color.blue) ;
             l3 = new Label("ance", Label.LEFT) ;
             l3.setForeground(Color.blue) ;

             lo23 = new Label(" P exit", Label.CENTER) ;
             lo29 = new Label(" T exit", Label.CENTER) ;
             lo30 = new Label(" U exit", Label.CENTER) ;
             lo31 = new Label("X exit", Label.CENTER) ;
             lo32 = new Label("Y exit", Label.CENTER) ;
             lo33 = new Label("Height-th", Label.CENTER) ;
             lo40 = new Label("Z exit", Label.CENTER) ;
             lo41 = new Label(" M exit", Label.CENTER) ;
             lo51 = new Label("Ang ex av", Label.CENTER) ;

             o21 = new TextField() ;
             o21.setBackground(Color.black) ;
             o21.setForeground(Color.yellow) ;
             o22 = new TextField() ;
             o22.setBackground(Color.black) ;
             o22.setForeground(Color.yellow) ;
             o23 = new TextField() ;
             o23.setBackground(Color.black) ;
             o23.setForeground(Color.yellow) ;
             o24 = new TextField() ;
             o24.setBackground(Color.black) ;
             o24.setForeground(Color.yellow) ;
             o25 = new TextField() ;
             o25.setBackground(Color.black) ;
             o25.setForeground(Color.yellow) ;

             o27 = new TextField() ;
             o27.setBackground(Color.black) ;
             o27.setForeground(Color.yellow) ;
             o28 = new TextField() ;
             o28.setBackground(Color.black) ;
             o28.setForeground(Color.yellow) ;
             o29 = new TextField() ;
             o29.setBackground(Color.black) ;
             o29.setForeground(Color.yellow) ;
             o30 = new TextField() ;
             o30.setBackground(Color.black) ;
             o30.setForeground(Color.yellow) ;
             o31 = new TextField() ;
             o31.setBackground(Color.black) ;
             o31.setForeground(Color.yellow) ;
             o32 = new TextField() ;
             o32.setBackground(Color.black) ;
             o32.setForeground(Color.yellow) ;
             o33 = new TextField() ;
             o33.setBackground(Color.black) ;
             o33.setForeground(Color.yellow) ;
             o34 = new TextField() ;
             o34.setBackground(Color.black) ;
             o34.setForeground(Color.yellow) ;
             o35 = new TextField() ;
             o35.setBackground(Color.black) ;
             o35.setForeground(Color.yellow) ;
             o36 = new TextField() ;
             o36.setBackground(Color.black) ;
             o36.setForeground(Color.yellow) ;

             o40 = new TextField() ;
             o40.setBackground(Color.black) ;
             o40.setForeground(Color.yellow) ;
             o41 = new TextField() ;
             o41.setBackground(Color.black) ;
             o41.setForeground(Color.yellow) ;
             o42 = new TextField() ;
             o42.setBackground(Color.black) ;
             o42.setForeground(Color.yellow) ;
             o43 = new TextField() ;
             o43.setBackground(Color.black) ;
             o43.setForeground(Color.yellow) ;
             o44 = new TextField() ;
             o44.setBackground(Color.black) ;
             o44.setForeground(Color.yellow) ;
             o47 = new TextField() ;
             o47.setBackground(Color.black) ;
             o47.setForeground(Color.yellow) ;
             o48 = new TextField() ;
             o48.setBackground(Color.black) ;
             o48.setForeground(Color.yellow) ;
             o49 = new TextField() ;
             o49.setBackground(Color.black) ;
             o49.setForeground(Color.yellow) ;
             o50 = new TextField() ;
             o50.setBackground(Color.black) ;
             o50.setForeground(Color.yellow) ;
             o51 = new TextField() ;
             o51.setBackground(Color.black) ;
             o51.setForeground(Color.yellow) ;

             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(l1) ;
             add(l2) ;   
             add(l3) ; 
             add(new Label(" ", Label.CENTER)) ;

             add(new Label("F gross", Label.CENTER)) ; 
             add(o21) ;
             add(new Label("Drag ", Label.CENTER)) ; 
             add(o48) ;
             add(new Label("F net ", Label.CENTER)) ; 
             add(o49) ; 

             add(new Label("W dot", Label.CENTER)) ; 
             add(o27) ; 
             add(lo30) ;  
             add(o30) ; 
             add(lo41) ; 
             add(o41) ;  

             add(lo29) ;  
             add(o29) ;    
             add(lo23) ; 
             add(o23) ;
             add(new Label(" A exit ", Label.CENTER)) ; 
             add(o36) ;  

             add(new Label("M external", Label.CENTER)) ;  
             add(o25) ;
             add(new Label("P amb ", Label.CENTER)) ; 
             add(o22) ;
             add(new Label("A boat-tail ", Label.CENTER)) ;
             add(o47) ;
   
             add(new Label("P boat-tail", Label.CENTER)) ; 
             add(o34) ;
             add(new Label("Pex - Pam", Label.CENTER)) ; 
             add(o35) ; 
             add(new Label("P ex/P boat", Label.CENTER)) ;  
             add(o24) ; 

             add(lo33) ; 
             add(o33) ; 
             add(new Label("A th", Label.CENTER)) ; 
             add(o43) ;
             add(new Label(" A exit / A* ", Label.CENTER)) ;  
             add(o28) ;

             add(lo31) ; 
             add(o31) ; 
             add(lo32) ; 
             add(o32) ; 
             add(lo40) ; 
             add(o40) ;

             add(new Label("M cwl exit", Label.CENTER)) ; 
             add(o42) ;
             add(new Label("P2 Plume ", Label.CENTER)) ;  
             add(o44) ;
             add(new Label("PM exit ", Label.CENTER)) ;  
             add(o50) ; 

             add(lo51) ; 
             add(o51) ;  
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 
             add(new Label(" ", Label.CENTER)) ; 

          }
        } //end nozp

        class Nozg extends Panel {
          TextArea prnt ;

          Nozg (Moc target) {
            outerparent = target ;
            setLayout(new GridLayout(1,1,10,10)) ;

            prnt = new TextArea() ;
            prnt.setEditable(false) ;

            prnt.appendText("MOC Version 1.7c ") ;
            add(prnt) ;
          }
        } //Nozg


       class Colout extends Panel {
          Moc outerparent ;
          Colcon colcon ;
          Colplt colplt ;
          Colvar colvar ;

          Colout (Moc target) {
                             
            outerparent = target ;
            setLayout(new GridLayout(1,3,2,2)) ;

            colcon = new Colcon(outerparent) ; 
            colplt = new Colplt(outerparent) ;
            colvar = new Colvar(outerparent) ;
 
            add(colcon) ;
            add(colplt) ;
            add(colvar) ;
         }
   
         class Colcon extends Panel {
           Button bt1,bt2,bt3,bt4 ;

            Colcon (Moc target) {
               outerparent = target ;
               setLayout(new GridLayout(10,1,1,1)) ;

               bt1 = new Button("Pressure") ;
               bt1.setBackground(Color.yellow) ;
               bt1.setForeground(Color.blue) ;
               bt2 = new Button("Temperature") ;
               bt2.setBackground(Color.white) ;
               bt2.setForeground(Color.blue) ;
               bt3 = new Button("Mach Number") ;
               bt3.setBackground(Color.white) ;
               bt3.setForeground(Color.blue) ;
               bt4 = new Button("Turning") ;
               bt4.setBackground(Color.white) ;
               bt4.setForeground(Color.blue) ;

               add(bt1) ;
               add(bt2) ;
               add(bt3) ;   
               add(bt4) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
            }

            public boolean action(Event evt, Object arg) {
               if(evt.target instanceof Button) {
                   this.handleBut(evt,arg)  ;
                   return true ;
               }
               else return false ;
            }

            public void handleBut(Event evt, Object arg) {
               String label = (String)arg ;
               Double V1,V2,V3,V4,V5 ;
               double v1,v2,v3,v4,v5 ;
               float fl1,fl2 ;

               if(label.equals("Pressure")) {
                    bt1.setBackground(Color.yellow) ;
                    bt2.setBackground(Color.white) ;
                    bt3.setBackground(Color.white) ;
                    bt4.setBackground(Color.white) ;
                    vpress = 1 ;
                    vtemp = -1 ;
                    vmach = -1 ;
                    vturn = -1 ;
               }
               if(label.equals("Temperature")) {
                    bt1.setBackground(Color.white) ;
                    bt2.setBackground(Color.yellow) ;
                    bt3.setBackground(Color.white) ;
                    bt4.setBackground(Color.white) ;
                    vpress = -1 ;
                    vtemp = 1 ;
                    vmach = -1 ;
                    vturn = -1 ;
               }
               if(label.equals("Mach Number")) {
                    bt1.setBackground(Color.white) ;
                    bt2.setBackground(Color.white) ;
                    bt3.setBackground(Color.yellow) ;
                    bt4.setBackground(Color.white) ;
                    vpress = -1 ;
                    vtemp = -1 ;
                    vmach = 1 ;
                    vturn = -1 ;
               }
               if(label.equals("Turning")) {
                    bt1.setBackground(Color.white) ;
                    bt2.setBackground(Color.white) ;
                    bt3.setBackground(Color.white) ;
                    bt4.setBackground(Color.yellow) ;
                    vpress = -1 ;
                    vtemp = -1 ;
                    vmach = -1 ;
                    vturn = 1 ;
               }

               colvar.bt1.setBackground(Color.yellow) ;
               colvar.bt2.setBackground(Color.white) ;
               getMinMax() ;
               comPute() ;
               return ;
            }  

        } //end Colcon

        class Colplt extends Canvas 
          implements Runnable{
             Moc outerparent ;
             Thread runc ;

             Colplt (Moc target) {
                setBackground(Color.white) ;
                runc = null ;
             } 

             public void start() {
                if (runc == null) {
                   runc = new Thread(this) ;
                   runc.start() ;
                }
             }

             public void run() {
                int timer ;

                timer = 100 ;
                while (true) {
                    try { Thread.sleep(timer); }
                    catch (InterruptedException e) {}
                    colout.repaint() ;
                }
            }

            public void update(Graphics g) {
                colout.paint(g) ;
            }

            public void paint(Graphics g) {
               int i,j,k ;
               int exes[] = new int[10] ;
               int whys[] = new int[10] ;
               int yorgn = 200 ;
               int xorgn = 40 ;
               int xlab,ylab ;
               double xmax,xmin,xval;
               Color colr ;

               offsGg2.setColor(Color.white) ;
               offsGg2.fillRect(0,0,250,500) ;
   // color bar
   // 40 increments
              offsGg2.setColor(Color.black) ;
              offsGg2.drawRect(xorgn+4,4,52,250) ;
              xmin = 0.0 ;
              xmax = 1.0 ;
              for(i=0; i<=40; ++ i) {
                xval = xmax - (i * (xmax/40)) ;
                colr = view.getColbar(xmin,xmax,xval) ;
                offsGg2.setColor(colr) ;
                offsGg2.fillRect(xorgn+5,5+6*i,50,6) ;
              }
   // tick marks
              for(i=0; i<=5; ++ i) {
                offsGg2.setColor(Color.black) ;
                offsGg2.fillRect(xorgn+60,5+48*i,30,4) ;
              }
   // labels
              offsGg2.drawString("100 %", xorgn+95, 10) ;
              offsGg2.drawString(" 80 %", xorgn+95, 58) ;
              offsGg2.drawString(" 60 %", xorgn+95, 106) ;
              offsGg2.drawString(" 40 %", xorgn+95, 154) ;
              offsGg2.drawString(" 20 %", xorgn+95, 202) ;
              offsGg2.drawString("  0 %", xorgn+95, 250) ;

              g.drawImage(offscreenImg2,0,0,this) ;
            }
          }// end colplt

         class Colvar extends Panel {
           Label l1,l2,l3 ;
           TextField co1, co2 ;
           Button bt1,bt2 ;

            Colvar (Moc target) {
               outerparent = target ;
               setLayout(new GridLayout(10,2,10,10)) ;

               l1 = new Label("Max", Label.LEFT) ;
               l2 = new Label("Min", Label.LEFT) ;
               l3 = new Label("Limits", Label.CENTER) ;

               co1 = new TextField() ;
               co2 = new TextField() ;

               bt1 = new Button("Find") ;
               bt1.setBackground(Color.yellow) ;
               bt1.setForeground(Color.blue) ;
               bt2 = new Button("Set") ;
               bt2.setBackground(Color.white) ;
               bt2.setForeground(Color.blue) ;

               add(co1) ;
               add(l1) ;  
   
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;

               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;

               add(new Label(" ", Label.CENTER)) ; 
               add(l3) ;

               add(new Label(" ", Label.CENTER)) ;
               add(bt1) ;

               add(new Label(" ", Label.CENTER)) ;
               add(bt2) ;

               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;

               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;

               add(co2) ;
               add(l2) ; 

               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
            }

            public boolean action(Event evt, Object arg) {
               if(evt.target instanceof Button) {
                   this.handleBut(evt,arg)  ;
                   return true ;
               }
               else return false ;
            }

            public void handleBut(Event evt, Object arg) {
               String label = (String)arg ;
               Double V1,V2,V3,V4,V5 ;
               double v1,v2,v3,v4,v5 ;
               float fl1,fl2 ;

               if(label.equals("Find")) {
                   getMinMax() ;
                   bt1.setBackground(Color.yellow) ;
                   bt2.setBackground(Color.white) ;
               }
               if(label.equals("Set")) {
                   bt1.setBackground(Color.white) ;
                   bt2.setBackground(Color.yellow) ;
                   V1 = Double.valueOf(co1.getText()) ;
                   v1 = V1.doubleValue() ;
                   varmax = v1 ;
                   V2 = Double.valueOf(co2.getText()) ;
                   v2 = V2.doubleValue() ;
                   varmin = v2 ;
              }

              comPute() ;
              return ;
            }  
          } //end Colvar
        } //end Colout

        class Diag extends Panel {
          TextField o1, o2, o3, o4, o5, o6, o7, o8, o9, o10 ;
          TextField o11, o12, o13, o14, o15, o16, o17, o18, o19, o20 ;
          TextField o21, o22, o23, o24, o25, o26, o27, o28, o29, o30 ;

          Diag (Moc target) {
            outerparent = target ;
            setLayout(new GridLayout(6,5,10,10)) ;

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
             o13 = new TextField() ;
             o14 = new TextField() ;
             o15 = new TextField() ;
             o16 = new TextField() ;        
             o17 = new TextField() ;
             o18 = new TextField() ;
             o19 = new TextField() ;
             o20 = new TextField() ;    

             o21 = new TextField() ;
             o22 = new TextField() ;        
             o23 = new TextField() ;
             o24 = new TextField() ;
             o25 = new TextField() ;
             o26 = new TextField() ;        
             o27 = new TextField() ;
             o28 = new TextField() ;
             o29 = new TextField() ;
             o30 = new TextField() ;    

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
             add(o13) ;
             add(o14) ;
             add(o15) ;

             add(o16) ;
             add(o17) ;
             add(o18) ;
             add(o19) ;
             add(o20) ;

             add(o21) ;
             add(o22) ;
             add(o23) ;
             add(o24) ;
             add(o25) ;

             add(o26) ;
             add(o27) ;
             add(o28) ;
             add(o29) ;
             add(o30) ;
          }
        } //Diag
     } // end Out
  } // end Num

  class Viewer extends Canvas 
         implements Runnable{
     Moc outerparent ;
     Thread runner ;
     Point locate,anchor;

     Viewer (Moc target) {
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
         if (x >= 76 && x <= 700) {
            if (y >= 21 && y <= 300) {
               locate = new Point(x,y) ;
               yt =  yt + (int) (.2*(locate.y - anchor.y)) ;
               xt =  xt + (int) (.4*(locate.x - anchor.x))  ;
            }
         }
         if (x <= 30 ) {  
            if (y >= 10 && y <= 220 ) {   // zoom widget
              sldloc = y ;
              if (sldloc < 10) sldloc = 10;
              if (sldloc > 220) sldloc = 220;
              fact = pfac1 + (220 - sldloc)*pfac2 ;
            }
         }
     }
 
     public void handleb(int x, int y) {  // handle buttons
         int i,j,k ;

         if (y >= 250 && y <= 265 ) { 
            if (x >= 5 && x <= 20) {    // decrease factor
              pfac1 = pfac1 / 2.0 ;
              pfac2 = pfac2 / 2.0 ;
              fact = pfac1 + (220 - sldloc)*pfac2 ;
            }
            if (x >= 25 && x <= 40) {    // increase factor
              pfac1 = pfac1 * 2.0 ;
              pfac2 = pfac2 * 2.0 ;
              fact = pfac1 + (220 - sldloc)*pfac2 ;
            }
         }

         if (y >= 270 && y <= 290 ) {   // find button
           if (x >=5 && x <=45) {  
             xt = 80; yt = 20; sldloc = 130;
             pfac1 = 5.0; pfac2 = .12;
             fact = 15.2 ;
 //            pfac1 = 1.25; pfac2 = .03;
 //            fact = 3.95 ;

           }
         }

         if (y >= 5 && y <= 30 ) {   // top buttons
           if (x >=770 && x <=820) {   // exit button
              f.dispose() ;
              System.exit(1) ;
           }

           if (x >= 130 && x <= 180) {   // reflect button
               reflect = - reflect ;  
           }
           if (x >= 85 && x <= 130) {   // grid output
               vgrid = 1;
               vplot = -1; 
               cbar = -1 ;
               perfout = -1 ;
               geomout = 1 ;
               geomr = -1 ;
               layout.show(num.out, "sixth")  ; 
            }
           if (x >= 35 && x <= 80) {   // plot output
               vgrid = -1 ;
               vplot = 1 ;
               getMinMax() ;
               cbar = 1 ;
               perfout = -1 ;
               geomout = -1 ;
               geomr = -1 ;
               layout.show(num.out, "ninth")  ;  
           }

           if (x >=235 && x <= 290 ) {   // internal input
              intin = 1 ;
              extin = -1 ;
              anlin = -1 ;
              layin.show(num.inp, "first")  ;
           }
           if (x >=300 && x <= 355 ) {   // external input
              intin = -1 ;
              extin = 1 ;
              anlin = -1 ;
              layin.show(num.inp, "second")  ;
           }
           if (x >=365 && x <= 420 ) {   // analysis input
              intin = -1 ;
              extin = -1 ;
              anlin = 1 ;
              layin.show(num.inp, "third")  ;
           }

           if (x >=475 && x <= 535 ) {   //color bar
              cbar = 1 ;
              perfout = -1 ;
              geomout = -1 ;
              diout = -1 ;
              geomr = -1 ;
              layout.show(num.out, "ninth")  ;
              num.inp.anlpan.inleft.diagbut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.diagbut.setForeground(Color.white) ;
              num.inp.anlpan.inleft.geobut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.geobut.setForeground(Color.white) ;          
           }
           if (x >=635 && x <= 730 ) {   //MOC geometry
              cbar = -1 ;
              perfout = -1 ;
              geomout = 1 ;
              diout = -1 ;
              geomr = -1 ;
              layout.show(num.out, "sixth")  ;
              num.inp.anlpan.inleft.diagbut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.diagbut.setForeground(Color.white) ;
              num.inp.anlpan.inleft.geobut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.geobut.setForeground(Color.white) ;          
           }
           if (x >=545 && x <= 625 ) {   //performance
              cbar = -1 ;
              perfout = 1 ;
              geomout = -1 ;
              diout = -1 ;
              geomr = -1 ;
              layout.show(num.out, "seventh")  ;
              num.inp.anlpan.inleft.diagbut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.diagbut.setForeground(Color.white) ;
              num.inp.anlpan.inleft.geobut.setBackground(Color.blue) ;
              num.inp.anlpan.inleft.geobut.setForeground(Color.white) ;          
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
 
     public Color getColbar(double min, double max, double vrbl) {
                       // Utility to get a color from the color bar
        Color value ;
        double innon, maxnon, minnon ;
        float r,g,b ;
 
        maxnon = 1.0 ;
        minnon = 0.0 ;
        innon = (vrbl - min)  / (max - min) ;

    //       num.out.colout.colcon.co3.setText(String.valueOf(filter3(innon))) ;
     //      num.out.colout.colcon.co4.setText(String.valueOf(filter3(minnon))) ;

        r = (float) 0.0 ;
        g = (float) 0.0 ;
        b = (float) 0.0 ;

        if (innon > minnon && innon <= .1) {
            b = (float) (innon/.1) ;  
        }
        if (innon > .1 && innon <=.3) {
            b = (float) 1.0 ; 
            g = (float) ((innon-.1)/.2) ;
        }     
        if (innon > .3 && innon <=.45) {
            g = (float) 1.0 ;
            b = (float) (1.0 - ((innon-.31)/.15)) ;
    //        b = (float) 0.1666667 ;
        }     
        if (innon > .45 && innon <=.55) {
            g = (float) 1.0 ;
            r = (float) ((innon-.45)/.1) ;
        }
        if (innon > .55 && innon <=.7) {
            r = (float) 1.0 ;
            g = (float) (1.0 - ((innon-.55)/.15)) ;
        }
        if (innon > .7 && innon <=.9) {
            r = (float) 1.0 ; 
            b = (float) ((innon-.7)/.2) ;
        }
        if (innon > .9 && innon <=maxnon) {
            r = (float) 1.0 ;
            b = (float) 1.0 ;
            g = (float) ((innon-.9)/.1) ;
        }

        if (r > (float) 1.0) r = (float) 1.0 ;
        if (g > (float) 1.0) g = (float) 1.0 ; 
        if (b > (float) 1.0) b = (float) 1.0 ;
                   
        value = new Color(r,g,b) ;
        return(value) ;
     }

     public void update(Graphics g) {
         view.paint(g) ;
     }

     public void paint(Graphics g) {
       int i,j,k ;
       int jl, jr, jcl, jcr, jex, grp;
       int exes[] = new int[10] ;
       int whys[] = new int[10] ;
       int yorgn = 200 ;
       int xorgn = 50 ;
       int xlab,ylab ;
       int numplg,jbgn ;
       int n,nmax, si ;
       double xmax,xmin,xval;
       double xpt,ypt,xpt2,ypt2 ;
       Color colr ;

    if (prob >= 7) {
      paint7(g);
    }
    else {

       xmin = varmin ;
       xmax = varmax ;

       colr = getColbar(xmin,xmax,1.0) ;
       offsGg.setColor(Color.white) ;
       offsGg.fillRect(0,0,1100,500) ;
//       offsGg.setColor(Color.red) ;
//       offsGg.drawString("0",xorgn + 5,70) ;
          // draw geometry
       offsGg.setColor(Color.red) ;
       offsGg.drawString("1", exes[1]-70, whys[1]-10) ;

// centerline
          exes[0] = xorgn + (int) (.5*fact*-500.0) + xt ;   
          whys[0] = yorgn - (int) (.5*fact*0.0) + yt ;
          exes[1] = xorgn + (int) (.5*fact*500.0) + xt ;
          whys[1] = yorgn - (int) (.5*fact*0.0) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;

       if (prob <=2) {  // nozzle surface
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
//  external flow
           if (plumopt == 2) {
             if(vgrid == 1) {
              for(i=1; i<=2; ++i) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[i]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[i]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[i]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[i]) + yt ;
                offsGg.setColor(Color.blue) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
             }
             if (vplot == 1) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                if (vpress == 1) var = pres[1][0] ;
                if (vtemp == 1) var = temp[1][0]  ;
                if (vmach == 1) var = mach2[1][0] ;
                if (vturn == 1) var = turn[1][0] ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                if (vpress == 1) var = .5* (pres[1][0] + pres[1][1]) ;
                if (vtemp == 1) var = .5* (temp[1][0] + temp[1][1]) ;
                if (vmach == 1) var = .5* (mach2[1][0] + mach2[1][1]) ;
                if (vturn == 1) var = .5* (turn[1][0] + turn[1][1]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[3]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[3]) + yt ;
                 if (vpress == 1) var = pres[1][1]  ;
                 if (vtemp == 1) var =  temp[1][1]  ;
                 if (vmach == 1) var =  mach2[1][1] ;
                 if (vturn == 1) var =  turn[1][1]  ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
             }
           }
 // draw geom
 // internal surface
           for(i=1; i<=numray; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*icxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*icynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
// external surface
           for(i=1; i<=2; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*ecxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*ecybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*ecxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*ecynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
           exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[1]) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[1]) + yt ;
           exes[1] = xorgn + (int) (.5*fact*nzht*ecxbgn[1]) + xt ;
           whys[1] = yorgn + (int) (.5*si*fact*nzht*ecybgn[1]) + yt ;
           exes[2] = exes[1] + 5 ;
           whys[2] = whys[1] ;
           exes[3] = exes[0] + 5;
           whys[3] = whys[0] ;
           offsGg.setColor(Color.red) ;
           offsGg.fillPolygon(exes,whys,4) ;
         }
       } // end prob < 2

       if (prob == 0) {  // moc nozzle by planes - internal flow
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
//  flow grid
           exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[1][1]) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[1][1]) + yt ;
           exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[1][1]) + xt ;
           whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[1][1]) + yt ;
           exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[1][1]) + xt ;
           whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[1][1]) + yt ;
           exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[1][1]) + xt ;
           whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[1][1]) + yt ;
           if (vgrid == 1) {
              offsGg.setColor(Color.blue) ;
              offsGg.drawPolygon(exes,whys,4) ;
           }
           if (vplot == 1) {
              if (vpress == 1) colr = getColbar(xmin,xmax,mcpres[1][1]) ;
              if (vtemp == 1) colr = getColbar(xmin,xmax,mctemp[1][1]) ;
              if (vmach == 1) colr = getColbar(xmin,xmax,mcmach[1][1]) ;
              if (vturn == 1) colr = getColbar(xmin,xmax,mcturn[1][1]) ;
              offsGg.setColor(colr) ;
              offsGg.fillPolygon(exes,whys,4) ;
           }

           for(i=2; i<=numray/2+1 ; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[1][i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[1][i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[1][i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[1][i]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[1][i]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[1][i]) + yt ;
             exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[1][i]) + xt ;
             whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[1][i]) + yt ;

             if (vgrid == 1) {
                offsGg.setColor(Color.blue) ;
                offsGg.drawPolygon(exes,whys,4) ;
             }
             if (vplot == 1) {
                if (vpress == 1) colr = getColbar(xmin,xmax,mcpres[1][i]) ;
                if (vtemp == 1) colr = getColbar(xmin,xmax,mctemp[1][i]) ;
                if (vmach == 1) colr = getColbar(xmin,xmax,mcmach[1][i]) ;
                if (vturn == 1) colr = getColbar(xmin,xmax,mcturn[1][i]) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,4) ;
             }
           }

           exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[2][2]) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[2][2]) + yt ;
           exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[2][2]) + xt ;
           whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[2][2]) + yt ;
           exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[2][2]) + xt ;
           whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[2][2]) + yt ;
           exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[2][2]) + xt ;
           whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[2][2]) + yt ;
           if (vgrid == 1) {
              offsGg.setColor(Color.blue) ;
              offsGg.drawPolygon(exes,whys,4) ;
           }
           if (vplot == 1) {
              if ( vpress == 1) colr = getColbar(xmin,xmax,mcpres[2][2]) ;
              if ( vtemp == 1) colr = getColbar(xmin,xmax,mctemp[2][2]) ;
              if ( vmach == 1) colr = getColbar(xmin,xmax,mcmach[2][2]) ;
              if ( vturn == 1) colr = getColbar(xmin,xmax,mcturn[2][2]) ;
              offsGg.setColor(colr) ;
              offsGg.fillPolygon(exes,whys,4) ;
           }

           for (k=3; k<= numray/2+1; ++k) {
              for(i=k; i<=numray/2+1 ; ++i) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[k-1][i]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[k-1][i]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[k-1][i]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[k-1][i]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[k-1][i]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[k-1][i]) + yt ;
                exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[k-1][i]) + xt ;
                whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[k-1][i]) + yt ;
                if (vgrid == 1) {
                  offsGg.setColor(Color.blue) ;
                  offsGg.drawPolygon(exes,whys,4) ;
                }
                if (vplot == 1) {
                  if (vpress == 1) colr = getColbar(xmin,xmax,mcpres[k-1][i]) ;
                  if (vtemp == 1) colr = getColbar(xmin,xmax,mctemp[k-1][i]) ;
                  if (vmach == 1) colr = getColbar(xmin,xmax,mcmach[k-1][i]) ;
                  if (vturn == 1) colr = getColbar(xmin,xmax,mcturn[k-1][i]) ;
                  offsGg.setColor(colr) ;
                  offsGg.fillPolygon(exes,whys,4) ;
                }
              }
              exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[k][k]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[k][k]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[k][k]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[k][k]) + yt ;
              exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[k][k]) + xt ;
              whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[k][k]) + yt ;
              exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[k][k]) + xt ;
              whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[k][k]) + yt ;
              if (vgrid == 1) {
                 offsGg.setColor(Color.blue) ;
                 offsGg.drawPolygon(exes,whys,4) ;
              }
              if (vplot == 1) {
                 if (vpress == 1) colr = getColbar(xmin,xmax,mcpres[k][k]) ;
                 if (vtemp == 1) colr = getColbar(xmin,xmax,mctemp[k][k]) ;
                 if (vmach == 1) colr = getColbar(xmin,xmax,mcmach[k][k]) ;
                 if (vturn == 1) colr = getColbar(xmin,xmax,mcturn[k][k]) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
          }
// geometry labels
          if (vgrid == 1) {
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[1]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("th",exes[0]-10,whys[0]-15*si) ; 
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[numray]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[numray]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("ex",exes[0]+5,whys[0]) ;
          }
// chosen zone
          exes[0] = xorgn + (int) (.5*fact*nzht*mcxll[row][col]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*mcyll[row][col]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*nzht*mcxul[row][col]) + xt ;
          whys[1] = yorgn + (int) (.5*si*fact*nzht*mcyul[row][col]) + yt ;
          exes[2] = xorgn + (int) (.5*fact*nzht*mcxur[row][col]) + xt ;
          whys[2] = yorgn + (int) (.5*si*fact*nzht*mcyur[row][col]) + yt ;
          exes[3] = xorgn + (int) (.5*fact*nzht*mcxlr[row][col]) + xt ;
          whys[3] = yorgn + (int) (.5*si*fact*nzht*mcylr[row][col]) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawPolygon(exes,whys,4) ;
        }
       } // end prob 0 flow

       if (prob == 1) {  // moc nozzle by points - internal flow
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
 // flow
         if(vgrid ==1) {
           offsGg.setColor(Color.blue) ;
           for(i=0; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][1]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][1]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           k = numray/2 + 1 ;
           for(i=1; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k][i+1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i+1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
           for(i=2; i<=numray/2+1; ++i) {
              for (k=i-1; k<=numray/2; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k+1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k+1][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
// geometry labels
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[1]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("th",exes[0]-10,whys[0]-15*si) ; 
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[numray]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[numray]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("ex",exes[0]+5,whys[0]) ;
         }

          if (vplot == 1) {
            for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]) ;
                 if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                 if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                 if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
            }
            j = 1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+2]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+2]) + yt ;
            if (vpress == 1) var = mcpres[j][j+2] ;
            if (vtemp == 1) var = mctemp[j][j+2] ;
            if (vturn == 1) var = mcturn[j][j+2] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
            for (j=2; j<=numray/2; ++j) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[j-1][j] + 2.0 * mcpres[j][j] + mcpres[j][j+1]) ;
                 if (vtemp == 1) var = .25 * (mctemp[j-1][j] + 2.0 * mctemp[j][j] + mctemp[j][j+1]) ;
                 if (vmach == 1) var = .25 * (mcmach[j-1][j] + 2.0 * mcmach[j][j] + mcmach[j][j+1]) ;
                 if (vturn == 1) var = .25 * (mcturn[j-1][j] + 2.0 * mcturn[j][j] + mcturn[j][j+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
            }
            j = numray/2+1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            if (vpress == 1) var = mcpres[j-1][j] ;
            if (vtemp == 1) var = mctemp[j-1][j] ;
            if (vmach == 1) var = mcmach[j-1][j] ;
            if (vturn == 1) var = mcturn[j-1][j] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
          }
// chosen point
          exes[0] = xorgn + (int) (.5*fact*nzht*mcx[row][col]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[row][col]) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawString("o",exes[0]-3,whys[0]+5) ;
        }
       } // end prob 1 flow

       if (prob == 2) {  // axi moc nozzle by points - internal flow
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
// flow
         if(vgrid ==1) {
           offsGg.setColor(Color.blue) ;
           for(i=0; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][1]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][1]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           k = numray/2 + 1 ;
           for(i=1; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k][i+1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i+1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
            for(i=2; i<=numray/2+1; ++i) {
              for (k=i-1; k<=numray/2; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k+1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k+1][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
// geometry labels
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[1]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("th",exes[0]-10,whys[0]-15*si) ; 
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[numray]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[numray]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("ex",exes[0]+5,whys[0]) ;
          }
          if (vplot == 1) {
            for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]) ;
                 if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                 if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                 if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
            }
            j = 1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+2]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+2]) + yt ;
            if (vpress == 1) var = mcpres[j][j+2] ;
            if (vtemp == 1) var = mctemp[j][j+2] ;
            if (vmach == 1) var = mcmach[j][j+2] ;
            if (vturn == 1) var = mcturn[j][j+2] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
            for (j=2; j<=numray/2; ++j) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[j-1][j] + 2.0 * mcpres[j][j] + mcpres[j][j+1]) ;
                 if (vtemp == 1) var = .25 * (mctemp[j-1][j] + 2.0 * mctemp[j][j] + mctemp[j][j+1]) ;
                 if (vmach == 1) var = .25 * (mcmach[j-1][j] + 2.0 * mcmach[j][j] + mcmach[j][j+1]) ;
                 if (vturn == 1) var = .25 * (mcturn[j-1][j] + 2.0 * mcturn[j][j] + mcturn[j][j+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
            }
            j = numray/2+1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            if (vpress == 1) var = mcpres[j-1][j] ;
            if (vtemp == 1) var = mctemp[j-1][j] ;
            if (vmach == 1) var = mcmach[j-1][j] ;
            if (vturn == 1) var = mcturn[j-1][j] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
          }
// chosen point
          exes[0] = xorgn + (int) (.5*fact*nzht*mcx[row][col]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[row][col]) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawString("o",exes[0]-3,whys[0]+5) ;
         }
       } // end prob 2 flow

       if (prob == 3) {  // over or under expanded jet
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
 // draw geom
          exes[0] = xorgn + (int) (.5*fact*wxbgn[1]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*wybgn[1]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*wxnd[1]) + xt ;
          whys[1] = yorgn + (int) (.5*si*fact*wynd[1]) + yt ;
          exes[2] = exes[1]-5 ;
          whys[2] = whys[1]  ;
          exes[3] = exes[0]-5 ;
          whys[3] = whys[0]  ;
          offsGg.setColor(Color.red) ;
          offsGg.fillPolygon(exes,whys,4) ;

          exes[0] = xorgn + (int) (.5*fact*wxbgn[2]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*wybgn[2]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*wxnd[2]) + xt ;
          whys[1] = yorgn + (int) (.5*si*fact*wynd[2]) + yt ;
          exes[2] = exes[1] ;
          whys[2] = whys[1] - 5*si ;
          exes[3] = exes[0] ;
          whys[3] = whys[0] - 5*si ;
          offsGg.setColor(Color.red) ;
          offsGg.fillPolygon(exes,whys,4) ;
         }
       } // end prob 3 geom

       if (prob == 6) {  // moc plug nozzle by points

         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
//  external flow
           if (plumopt == 2) {
             if(vgrid == 1) {
              for(i=1; i<=2; ++i) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[i]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[i]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[i]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[i]) + yt ;
                offsGg.setColor(Color.blue) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
             }
             if (vplot == 1) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                if (vpress == 1) var = pres[1][0] ;
                if (vtemp == 1) var = temp[1][0]  ;
                if (vmach == 1) var = mach2[1][0] ;
                if (vturn == 1) var = turn[1][0]  ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                if (vpress == 1) var = .5* (pres[1][0] + pres[1][1]) ;
                if (vtemp == 1) var = .5* (temp[1][0] + temp[1][1]) ;
                if (vmach == 1) var = .5* (mach2[1][0] + mach2[1][1]) ;
                if (vturn == 1) var = .5* (turn[1][0] + turn[1][1]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[3]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[3]) + yt ;
                 if (vpress == 1) var = pres[1][1]  ;
                 if (vtemp == 1) var =  temp[1][1]  ;
                 if (vmach == 1) var =  mach2[1][1] ;
                 if (vturn == 1) var =  turn[1][1]  ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
             }
           }
// draw geom
 // plug surface
           for(i=1; i<=numray; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*wxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*wybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*wxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*wynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
 // internal cowl surface
           for(i=1; i<=numray/2; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*icxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*icynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
// external cowl surface
           for(i=1; i<=2; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*ecxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*ecybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*ecxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*ecynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
           exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[1]) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[1]) + yt ;
           exes[1] = xorgn + (int) (.5*fact*nzht*ecxbgn[1]) + xt ;
           whys[1] = yorgn + (int) (.5*si*fact*nzht*ecybgn[1]) + yt ;
           exes[2] = exes[1] - 5*si ;
           whys[2] = whys[1] ;
           exes[3] = exes[0] - 5*si;
           whys[3] = whys[0] ;
           offsGg.setColor(Color.red) ;
           offsGg.fillPolygon(exes,whys,4) ;

// internal flow
         if(vgrid ==1) {
           offsGg.setColor(Color.blue) ;
           for(i=0; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[1][i]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[1][i+1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i+1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           k = numray/2 + 1 ;
           for(i=1; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
           for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k+1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k+1][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
            for(i=2; i<=numray/2+1; ++i) {
              for (k=i-1; k<=numray/2; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
// geometry labels
            exes[0] = xorgn + (int) (.5*fact*nzht*wxnd[1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*wynd[1]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("th",exes[0]-10,whys[0]-15*si) ; 

            exes[0] = xorgn + (int) (.5*fact*xexit) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*yexit) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("ex",exes[0]+5,whys[0]) ;

            exes[0] = xorgn + (int) (.5*fact*xpexit) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*ypexit) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("p ex",exes[0]+5,whys[0]) ;
          }
          if (vplot == 1) {
            for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k+1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k+1][i]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[k+1][i+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[k+1][i+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[k][i+1]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[k][i+1]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[k][i] + mcpres[k+1][i] + mcpres[k+1][i+1] + mcpres[k][i+1]) ;
                 if (vtemp == 1) var = .25 * (mctemp[k][i] + mctemp[k+1][i] + mctemp[k+1][i+1] + mctemp[k][i+1]) ;
                 if (vmach == 1) var = .25 * (mcmach[k][i] + mcmach[k+1][i] + mcmach[k+1][i+1] + mcmach[k][i+1]) ;
                 if (vturn == 1) var = .25 * (mcturn[k][i] + mcturn[k+1][i] + mcturn[k+1][i+1] + mcturn[k][i+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
            }
            j = 1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j][j-1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j-1]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j+1][j]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j+1][j]) + yt ;
            if (vpress == 1) var = mcpres[j][j] ;
            if (vtemp == 1) var = mctemp[j][j] ;
            if (vmach == 1) var = mcmach[j][j] ;
            if (vturn == 1) var = mcturn[j][j] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
            for (j=2; j<=numray/2; ++j) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j][j-1]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j-1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j+1][j]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j+1][j]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[j][j-1] + 2.0 * mcpres[j][j] + mcpres[j+1][j]) ;
                 if (vtemp == 1) var = .25 * (mctemp[j][j-1] + 2.0 * mctemp[j][j] + mctemp[j+1][j]) ;
                 if (vmach == 1) var = .25 * (mcmach[j][j-1] + 2.0 * mcmach[j][j] + mcmach[j+1][j]) ;
                 if (vturn == 1) var = .25 * (mcturn[j][j-1] + 2.0 * mcturn[j][j] + mcturn[j+1][j]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
            }
/*
            j = numray/2+1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
            if (vpress == 1) var = mcpres[j-1][j] ;
            if (vtemp == 1) var = mctemp[j-1][j] ;
            if (vmach == 1) var = mcmach[j-1][j] ;
            if (vturn == 1) var = mcturn[j-1][j] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
*/
          }
// chosen point
          exes[0] = xorgn + (int) (.5*fact*nzht*mcx[row][col]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[row][col]) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawString("o",exes[0]-3,whys[0]+5) ;
         }
       } // end prob 6 flow

       if(plumopt >= 1) {    // plume and external flow field
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
           if(nprat > 1.0) {  // under expanded
  // waves
             for (j=1 ; j<=ncycle; ++j) { 
               if(vgrid ==1) {
  // plume into static
                 if(plumopt ==1) {
  // internal flow 
                    for (i = 1; i <= 8; ++i) {
                      exes[0] = xorgn + (int) (.5*fact*(ixbgn[i]
                             +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                      whys[0] = yorgn + (int) (.5*si*fact*iybgn[i]) + yt ;
                      exes[1] = xorgn + (int) (.5*fact*(ixnd[i]
                            +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;
                      whys[1] = yorgn + (int) (.5*si*fact*iynd[i]) + yt ;
                      if(ifamily[i] == 1) offsGg.setColor(Color.blue) ; 
                      if(ifamily[i] == 2) offsGg.setColor(Color.red) ; 
                      offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                    }
                 }
  // plume into supersonic
                 if (plumopt == 2) { 
  // internal flow 
                    for (i = 1; i <= 4; ++i) {
                      exes[0] = xorgn + (int) (.5*fact*ixbgn[i]) + xt ;   
                      whys[0] = yorgn + (int) (.5*si*fact*iybgn[i]) + yt ;
                      exes[1] = xorgn + (int) (.5*fact*ixnd[i]) + xt ;
                      whys[1] = yorgn + (int) (.5*si*fact*iynd[i]) + yt ;
                      if(ifamily[i] == 1) offsGg.setColor(Color.blue) ; 
                      if(ifamily[i] == 2) offsGg.setColor(Color.red) ; 
                      offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                    }
  // external flow 
                   for (i = 3; i <= 5; ++i) {
                     exes[0] = xorgn + (int) (.5*fact*exbgn[i] ) + xt ;
                     whys[0] = yorgn + (int) (.5*si*fact*eybgn[i]) + yt ;
                     exes[1] = xorgn + (int) (.5*fact*exnd[i]) + xt ;
                     whys[1] = yorgn + (int) (.5*si*fact*eynd[i]) + yt ;
                     if(efamily[i] == 1) offsGg.setColor(Color.blue) ; 
                     if(efamily[i] == 2) offsGg.setColor(Color.red) ; 
                     offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   }
                 }
               }
  // color plot
              if(vplot ==1) {
  // plume into static
               if(plumopt ==1) {
                if (prob < 6) {
                   k = 1 ;
                   exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   exes[2] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   if (vpress == 1) var = pres[2][k] ;
                   if (vtemp == 1) var = temp[2][k] ;
                   if (vmach == 1) var = mach2[2][k] ;
                   if (vturn == 1) var = turn[2][k] ;
                   colr = getColbar(xmin,xmax,var) ;
                   offsGg.setColor(colr) ;
                   offsGg.fillPolygon(exes,whys,3) ;
                 }
                 if (prob == 6 && j >= 2) {
                   k = 1 ;
                   exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   exes[2] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                   whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   if (vpress == 1) var = pres[2][k] ;
                   if (vtemp == 1) var = temp[2][k] ;
                   if (vmach == 1) var = mach2[2][k] ;
                   if (vturn == 1) var = turn[2][k] ;
                   colr = getColbar(xmin,xmax,var) ;
                   offsGg.setColor(colr) ;
                   offsGg.fillPolygon(exes,whys,3) ;
                 }
                 k = 2 ;
                 ypt = (ixnd[k]-ixnd[k-1])*islope[k]*islope[k+1]/(islope[k]+islope[k+1]) ;
                 xpt = ixnd[k-1] + (ypt / islope[k+1]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k-1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 if (vpress == 1) var = .5 * (pres[2][k]+pres[2][k-1]) ;
                 if (vtemp == 1) var = .5 * (temp[2][k]+temp[2][k-1]) ;
                 if (vmach == 1) var = .5 * (mach2[2][k]+mach2[2][k-1]) ;
                 if (vturn == 1) var = .5 * (turn[2][k]+turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k-1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 if (vpress == 1) var = .3333 * (pres[2][k]+pres[2][k-1]+pres[2][k+1]) ;
                 if (vtemp == 1) var = .3333 * (temp[2][k]+temp[2][k-1]+temp[2][k+1]) ;
                 if (vmach == 1) var = .3333 * (mach2[2][k]+mach2[2][k-1]+mach2[2][k+1]) ;
                 if (vturn == 1) var = .3333 * (turn[2][k]+turn[2][k-1]+turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 3 ;
                 ypt2 = iynd[k] - (ixnd[k+1]-ixnd[k])*islope[k+1]*islope[k+2]/(islope[k+1]+islope[k+2]) ;
                 xpt2 = ixnd[k] + ((iynd[k]-ypt2) / islope[k+2]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+2] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+2]) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = .5* (pres[2][k] + pres[2][k-1]) ;
                 if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k-1]) ;
                 if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k-1]) ;
                 if (vturn == 1) var = .5* (turn[2][k] + turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+1] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 if (vpress == 1) var = .33333 *(pres[2][k] + pres[2][k-1] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .33333 *(temp[2][k] + temp[2][k-1] + temp[2][k+1]) ;
                 if (vmach == 1) var = .33333 *(mach2[2][k] + mach2[2][k-1] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .33333 *(turn[2][k] + turn[2][k-1] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 4 ;
                 ypt = (ixnd[k+2]-ixnd[k+1])*islope[k+2]*islope[k+3]/(islope[k+2]+islope[k+3]) ;
                 xpt = ixnd[k+1] + (ypt / islope[k+3]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+2] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+3]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 if (vpress == 1) var = .5* (pres[2][k] + pres[2][k-1]) ;
                 if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k-1]) ;
                 if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k-1]) ;
                 if (vturn == 1) var = .5* (turn[2][k] + turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+4] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+4]) + yt ;
                 if (vpress == 1) var = .33333 *(pres[2][k] + pres[2][k-1] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .33333 *(temp[2][k] + temp[2][k-1] + temp[2][k+1]) ;
                 if (vmach == 1) var = .33333 *(mach2[2][k] + mach2[2][k-1] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .33333 *(turn[2][k] + turn[2][k-1] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+4] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+4]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+4] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+4]) + yt ;
                 if (vpress == 1) var = .5 *(pres[2][k] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .5 *(temp[2][k] + temp[2][k+1]) ;
                 if (vmach == 1) var = .5 *(mach2[2][k] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .5 *(turn[2][k] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 5 ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+3]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
               }
  // plume into supersonic
              if(plumopt ==2) {
//   plume flow
//   internal flow 
                if (prob < 6) {
                   k = 1 ;
                   exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                   whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                   whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   exes[2] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                   whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                   if (vpress == 1) var = pres[2][k] ;
                   if (vtemp == 1) var = temp[2][k] ;
                   if (vmach == 1) var = mach2[2][k] ;
                   if (vturn == 1) var = turn[2][k] ;
                   colr = getColbar(xmin,xmax,var) ;
                   offsGg.setColor(colr) ;
                   offsGg.fillPolygon(exes,whys,3) ;
                 }

                 k = 2 ;
                 ypt = (ixnd[k]-ixnd[k-1])*islope[k]*islope[k+1]/(islope[k]+islope[k+1]) ;
                 xpt = ixnd[k-1] + (ypt / islope[k+1]) ;
                 exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*ixnd[k+1] ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*xpt) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*ixbgn[k] ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*xpt ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*ixnd[k-1]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 if (vpress == 1) var = .5 * (pres[2][k]+pres[2][k-1]) ;
                 if (vmach == 1) var = .5 * (mach2[2][k]+mach2[2][k-1]) ;
                 if (vtemp == 1) var = .5 * (temp[2][k]+temp[2][k-1]) ;
                 if (vturn == 1) var = .5 * (turn[2][k]+turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*ixnd[k-1] ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*xpt) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 if (vpress == 1) var = .3333 * (pres[2][k]+pres[2][k-1]+pres[2][k+1]) ;
                 if (vtemp == 1) var = .3333 * (temp[2][k]+temp[2][k-1]+temp[2][k+1]) ;
                 if (vmach == 1) var = .3333 * (mach2[2][k]+mach2[2][k-1]+mach2[2][k+1]) ;
                 if (vturn == 1) var = .3333 * (turn[2][k]+turn[2][k-1]+turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 3 ;
                 exes[0] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*ixnd[k+1]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*ixbgn[k+1]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*xpt) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = .5* (pres[2][k] + pres[2][k-1]) ;
                 if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k-1]) ;
                 if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k-1]) ;
                 if (vturn == 1) var = .5* (turn[2][k] + turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 exes[0] = xorgn + (int) (.5*fact*ixbgn[k+1]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*ixbgn[k+2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iybgn[k+2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*ixnd[k+2]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+2]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*ixnd[k+1]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 if (vpress == 1) var = pres[2][k]  ;
                 if (vtemp == 1) var =  temp[2][k] ;
                 if (vmach == 1) var =  mach2[2][k] ;
                 if (vturn == 1) var =  turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
//  external flow
                 k = 2;
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+1]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+1]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[k+2]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[k+2]) + yt ;
                 if (vpress == 1) var = pres[1][k]  ;
                 if (vtemp == 1) var =  temp[1][k]  ;
                 if (vmach == 1) var =  mach2[1][k] ;
                 if (vturn == 1) var =  turn[1][k]  ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;

                 k = 3;
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+1]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+1]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[k+2]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[k+2]) + yt ;
                 if (vpress == 1) var = .5* (pres[1][k] + pres[1][k-1])  ;
                 if (vtemp == 1) var =  .5* (temp[1][k] + temp[1][k-1]) ;
                 if (vmach == 1) var =  .5* (mach2[1][k] + mach2[1][k-1]) ;
                 if (vturn == 1) var =  .5* (turn[1][k] + turn[1][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;

                 k = 4;
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+1]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+1]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[k+2]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[k+2]) + yt ;
                 if (vpress == 1) var = pres[1][k-1]  ;
                 if (vtemp == 1) var =  temp[1][k-1]  ;
                 if (vmach == 1) var =  mach2[1][k-1] ;
                 if (vturn == 1) var =  turn[1][k-1]  ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
               }
              } // end vplot=1
            }
 // streamlines
             offsGg.setColor(Color.black) ; 
             exes[3] = xorgn + (int) (.5*fact*strx[1][2]) + xt ;   
             whys[3] = yorgn + (int) (.5*si*fact*stry[1][1]) + yt ;
             offsGg.drawString("1", exes[3]+10, whys[3]) ;
// plume into static
          if(plumopt == 1) {
             for (j=1 ; j<=ncycle; ++j) {
               for (i = 1; i <= 2; ++i) {
                 exes[0] = xorgn + (int) (.5*fact*(strx[1][i] 
                             +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[1][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(strx[2][i]
                             +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[2][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[4] = (int) (.5*(exes[0] + exes[1])) ;
                 whys[4] = whys[0]-10*si ;
                 if(i==2) {
                    offsGg.drawString("2", exes[4], whys[4]) ;
                 }   

                 exes[0] = xorgn + (int) (.5*fact*(strx[2][i]
                            +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[2][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(strx[3][i]
                            +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[3][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[3] = (int) (.5*(exes[0] + exes[1])) ;
                 if(i==1) {
                    offsGg.drawString("3", exes[3], whys[3]) ;
                 }   

                 exes[0] = xorgn + (int) (.5*fact*(strx[3][i]
                           +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[3][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(strx[4][i]
                           +(j-1)*(strx[4][2]-strx[1][2])) ) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[4][i]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[4] = (int) (.5*(exes[0] + exes[1])) ;
                 exes[3] = exes[1] + 20 ;
                 if(i==2) {
                    offsGg.drawString("4", exes[4], whys[4]) ;
                 }
                 if(i==1) {
                    offsGg.drawString("5", exes[3], whys[3]) ;
                 }   
   
               }
              }
             }
//plume into supersonic
             if (plumopt ==2) {
                 exes[0] = xorgn + (int) (.5*fact*strx[1][2] ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[1][2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*strx[2][2]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[2][2]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[4] = (int) (.5*(exes[0] + exes[1])) ;
                 whys[4] = whys[0]-10*si ;
                 offsGg.drawString("2", exes[4], whys[4]) ;  

                 exes[0] = xorgn + (int) (.5*fact*strx[2][2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[2][2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*strx[3][2]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[3][2]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[3] = (int) (.5*(exes[0] + exes[1])) ;
                 offsGg.drawString("3", exes[3], whys[3]) ; 

                 exes[0] = xorgn + (int) (.5*fact*strx[3][2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*stry[3][2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*strx[4][2]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*stry[4][2]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ; 
    // labels
                 exes[0] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                 whys[1] = whys[0] + si*15 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("1", exes[0], whys[1]) ; 
                 exes[0] = xorgn + (int) (.5*fact*(.5 *(exbgn[3]+exbgn[4]))) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[4]) + yt ;
                 whys[1] = whys[0] + si*10 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("2", exes[0], whys[1]) ; 
                 exes[0] = xorgn + (int) (.5*fact*exbgn[5]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[5]) + yt ;
                 whys[1] = whys[0] + si*10 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("3", exes[0]+50, whys[1]) ; 
             }

           }

           if (nprat <= 1.0) { // over expanded
  // waves
            if(vgrid ==1) {
 // internal flow
                exes[0] = xorgn + (int) (.5*fact*ixbgn[1]) + xt ;   
                whys[0] = yorgn + (int) (.5*si*fact*iybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*ixnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*iynd[1]) + yt ;
                if(ifamily[1] == 1) offsGg.setColor(Color.blue) ; 
                if(ifamily[1] == 2) offsGg.setColor(Color.red) ; 
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                exes[0] = xorgn + (int) (.5*fact*ixbgn[2]) + xt ;   
                whys[0] = yorgn + (int) (.5*si*fact*iybgn[2]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*ixnd[2]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*iynd[2]) + yt ;
                if(ifamily[2] == 1) offsGg.setColor(Color.blue) ; 
                if(ifamily[2] == 2) offsGg.setColor(Color.red) ; 
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
 // plume into supersonic
                if (plumopt == 2) {
  // external flow
                   exes[0] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                   whys[0] = yorgn + (int) (.5*si*fact*eybgn[3]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*exnd[3]) + xt ;
                   whys[1] = yorgn + (int) (.5*si*fact*eynd[3]) + yt ;
                   if(efamily[3] == 1) offsGg.setColor(Color.blue) ; 
                   if(efamily[3] == 2) offsGg.setColor(Color.red) ; 
                   offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   exes[2] = xorgn + (int) (.5*fact*exbgn[4]) + xt ;   
                   whys[2] = yorgn + (int) (.5*si*fact*eybgn[4]) + yt ;
                   exes[3] = xorgn + (int) (.5*fact*exnd[4]) + xt ;
                   whys[3] = yorgn + (int) (.5*si*fact*eynd[4]) + yt ;
                   if(efamily[4] == 1) offsGg.setColor(Color.blue) ; 
                   if(efamily[4] == 2) offsGg.setColor(Color.red) ; 
                   offsGg.drawLine(exes[2],whys[2],exes[3],whys[3]) ;
                   exes[0] = xorgn + (int) (.5*fact*exbgn[5]) + xt ;   
                   whys[0] = yorgn + (int) (.5*si*fact*eybgn[5]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*exnd[5]) + xt ;
                   whys[1] = yorgn + (int) (.5*si*fact*eynd[5]) + yt ;
                   if(efamily[5] == 1) offsGg.setColor(Color.blue) ; 
                   if(efamily[5] == 2) offsGg.setColor(Color.red) ; 
                   offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
            }
// plume into static
          if(plumopt == 1) {
            for (j=1 ; j<=ncycle; ++j) {
             if(vgrid ==1) {
//internal mesh
                for (i = 3; i<= 10; ++i) {
                  exes[0] = xorgn + (int) (.5*fact*(ixbgn[i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;   
                  whys[0] = yorgn + (int) (.5*si*fact*iybgn[i]) + yt ;
                  exes[1] = xorgn + (int) (.5*fact*(ixnd[i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
                  whys[1] = yorgn + (int) (.5*si*fact*iynd[i]) + yt ;
                  if(ifamily[i] == 1) offsGg.setColor(Color.blue) ; 
                  if(ifamily[i] == 2) offsGg.setColor(Color.red) ; 
                  offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                }
              }
  // plot
            if(vplot ==1) {
                if (prob <=3) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    if (vpress == 1) var = pres[2][k] ;
                    if (vtemp == 1) var = temp[2][k] ;
                    if (vmach == 1) var = mach2[2][k] ;
                    if (vturn == 1) var = turn[2][k] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k-1]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k-1]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k-1]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    if (vpress ==1) var = pres[2][k] ;
                    if (vtemp ==1) var = temp[2][k] ;
                    if (vmach ==1) var = mach2[2][k] ;
                    if (vturn ==1) var = turn[2][k] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress ==1) var = pres[2][k+1] ;
                    if (vtemp ==1) var = temp[2][k+1] ;
                    if (vmach ==1) var = mach2[2][k+1] ;
                    if (vturn ==1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                 }
   // plug nozzle
                 if (prob >=4 && prob<=5) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*strx[1][2]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*stry[1][2]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k-1]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k-1]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k-1]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[3] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[3] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = .5* (pres[2][k] + pres[2][k+1]) ;
                    if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k+1]) ;
                    if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k+1]) ;
                    if (vturn == 1) var = .5* (turn[2][k] + turn[2][k+1]) ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,4) ;

                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                 }
                 if (prob ==6 ) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k+1]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                 }
                 k=3 ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 4 ;
                 ypt = (ixnd[k]-ixnd[k-1])*islope[k]*islope[k+1]/(islope[k]+islope[k+1]) ;
                 xpt = ixnd[k-1] + (ypt / islope[k+1]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k-1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 if (vpress == 1) var = .5 * (pres[2][k]+pres[2][k-1]) ;
                 if (vtemp == 1) var = .5 * (temp[2][k]+temp[2][k-1]) ;
                 if (vmach == 1) var = .5 * (mach2[2][k]+mach2[2][k-1]) ;
                 if (vturn == 1) var = .5 * (turn[2][k]+turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k-1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 if (vpress == 1) var = .3333 * (pres[2][k]+pres[2][k-1]+pres[2][k+1]) ;
                 if (vtemp == 1) var = .3333 * (temp[2][k]+temp[2][k-1]+temp[2][k+1]) ;
                 if (vmach == 1) var = .3333 * (mach2[2][k]+mach2[2][k-1]+mach2[2][k+1]) ;
                 if (vturn == 1) var = .3333 * (turn[2][k]+turn[2][k-1]+turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 5 ;
                 ypt2 = iynd[k] - (ixnd[k+1]-ixnd[k])*islope[k+1]*islope[k+2]/(islope[k+1]+islope[k+2]) ;
                 xpt2 = ixnd[k] + ((iynd[k]-ypt2) / islope[k+2]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+2] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+2]) + yt ;
                 if (vpress ==1) var = pres[2][k] ;
                 if (vtemp ==1) var = temp[2][k] ;
                 if (vmach ==1) var = mach2[2][k] ;
                 if (vturn ==1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = .5* (pres[2][k] + pres[2][k-1]) ;
                 if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k-1]) ;
                 if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k-1]) ;
                 if (vturn == 1) var = .5* (turn[2][k] + turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+1] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                 if (vpress == 1) var = .33333 *(pres[2][k] + pres[2][k-1] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .33333 *(temp[2][k] + temp[2][k-1] + temp[2][k+1]) ;
                 if (vmach == 1) var = .33333 *(mach2[2][k] + mach2[2][k-1] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .33333 *(turn[2][k] + turn[2][k-1] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 6 ;
                 ypt = (ixnd[k+2]-ixnd[k+1])*islope[k+2]*islope[k+3]/(islope[k+2]+islope[k+3]) ;
                 xpt = ixnd[k+1] + (ypt / islope[k+3]) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+2] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+3]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixnd[k] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*(xpt2 +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*ypt2) + yt ;
                 if (vpress == 1) var = .5* (pres[2][k] + pres[2][k-1]) ;
                 if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k-1]) ;
                 if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k-1]) ;
                 if (vturn == 1) var = .5* (turn[2][k] + turn[2][k-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixbgn[k+4] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+4]) + yt ;
                 if (vpress == 1) var = .33333 *(pres[2][k] + pres[2][k-1] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .33333 *(temp[2][k] + temp[2][k-1] + temp[2][k+1]) ;
                 if (vmach == 1) var = .33333 *(mach2[2][k] + mach2[2][k-1] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .33333 *(turn[2][k] + turn[2][k-1] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+4] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+4]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(xpt +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*ypt) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+4] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k+4]) + yt ;
                 if (vpress == 1) var = .5 *(pres[2][k] + pres[2][k+1]) ;
                 if (vtemp == 1) var = .5 *(temp[2][k] + temp[2][k+1]) ;
                 if (vmach == 1) var = .5 *(mach2[2][k] + mach2[2][k+1]) ;
                 if (vturn == 1) var = .5 *(turn[2][k] + turn[2][k+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;

                 k = 7 ;
                 exes[0] = xorgn + (int) (.5*fact*(ixbgn[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k+3]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*(ixnd[k+3] +(j-1)*(strx[5][2]-strx[2][2])) ) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iybgn[k+3]) + yt ;
                 if (vpress == 1) var = pres[2][k] ;
                 if (vtemp == 1) var = temp[2][k] ;
                 if (vmach == 1) var = mach2[2][k] ;
                 if (vturn == 1) var = turn[2][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
               }
             }
            }
// plume into supersonic
          if(plumopt == 2) {
  // color plot
            if(vplot ==1) {
// internal flow
                if (prob <=3) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    if (vpress == 1) var = pres[2][k] ;
                    if (vtemp == 1) var = temp[2][k] ;
                    if (vmach == 1) var = mach2[2][k] ;
                    if (vturn == 1) var = turn[2][k] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k-1]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k-1]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k-1]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    if (vpress ==1) var = pres[2][k] ;
                    if (vtemp ==1) var = temp[2][k] ;
                    if (vmach ==1) var = mach2[2][k] ;
                    if (vturn ==1) var = turn[2][k] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress ==1) var = pres[2][k+1] ;
                    if (vtemp ==1) var = temp[2][k+1] ;
                    if (vmach ==1) var = mach2[2][k+1] ;
                    if (vturn ==1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                }
   // plug nozzle
                 if (prob >=4 && prob<=5) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*strx[1][2]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*stry[1][2]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k-1]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k-1]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k-1]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k-1]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[3] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[3] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = .5* (pres[2][k] + pres[2][k+1]) ;
                    if (vtemp == 1) var = .5* (temp[2][k] + temp[2][k+1]) ;
                    if (vmach == 1) var = .5* (mach2[2][k] + mach2[2][k+1]) ;
                    if (vturn == 1) var = .5* (turn[2][k] + turn[2][k+1]) ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,4) ;

                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                 }
                 if (prob ==6 ) {
                    k=1 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k+1]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iynd[k+1]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;

                    k=2 ;
                    exes[0] = xorgn + (int) (.5*fact*ixbgn[k]) + xt ;   
                    whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                    exes[2] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                    whys[2] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                    if (vpress == 1) var = pres[2][k+1] ;
                    if (vtemp == 1) var = temp[2][k+1] ;
                    if (vmach == 1) var = mach2[2][k+1] ;
                    if (vturn == 1) var = turn[2][k+1] ;
                    colr = getColbar(xmin,xmax,var) ;
                    offsGg.setColor(colr) ;
                    offsGg.fillPolygon(exes,whys,3) ;
                 }
                 k = 2 ;
                 exes[0] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*ixnd[k]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[6]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*iynd[k]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exnd[6]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*iybgn[k]) + yt ;
                 if (vpress ==1) var = pres[2][k+1] ;
                 if (vtemp ==1) var = temp[2][k+1] ;
                 if (vmach ==1) var = mach2[2][k+1] ;
                 if (vturn ==1) var = turn[2][k+1] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
  // external flow
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+1]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+1]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+1]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 if (vpress ==1) var = .5* (pres[1][k-1]+pres[1][k]) ;
                 if (vtemp ==1) var = .5 * (temp[1][k-1]+temp[1][k]) ;
                 if (vmach ==1) var = .5 * (mach2[1][k-1]+mach2[1][k]) ;
                 if (vturn ==1) var = .5 * (turn[1][k-1]+turn[1][k]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+3]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[k+3]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[k+3]) + yt ;
                 if (vpress ==1) var = pres[1][k] ;
                 if (vtemp ==1) var = temp[1][k] ;
                 if (vmach ==1) var = mach2[1][k] ;
                 if (vturn ==1) var = turn[1][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
                 k=3 ;
                 exes[0] = xorgn + (int) (.5*fact*exbgn[k+2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[k+2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*exnd[k+2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*eynd[k+2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[k+3]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[k+3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[k+3]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[k+3]) + yt ;
                 if (vpress ==1) var = pres[1][k] ;
                 if (vtemp ==1) var = temp[1][k] ;
                 if (vmach ==1) var = mach2[1][k] ;
                 if (vturn ==1) var = turn[1][k] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
            }
          }
 // streamlines
             offsGg.setColor(Color.black) ; 

             exes[0] = xorgn + (int) (.5*fact*strx[1][1]) + xt ;   
             whys[0] = yorgn + (int) (.5*si*fact*stry[1][1]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*strx[2][1]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*stry[2][1]) + yt ;
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
 
             exes[0] = xorgn + (int) (.5*fact*strx[1][2]) + xt ; 
             offsGg.drawString("1", exes[0]+10, whys[0]) ;
  
             whys[0] = yorgn + (int) (.5*si*fact*stry[1][2]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*strx[2][2]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*stry[2][2]) + yt ;
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;

             exes[3] = xorgn + (int) (.5*fact*strx[1][2]) + xt ;   
             whys[3] = yorgn + (int) (.5*si*fact*stry[1][1]) + yt ;
             exes[4] = (int) (.5*(exes[0] + exes[1])) ;
             whys[4] = whys[1]-10*si ;
             offsGg.drawString("2", exes[4], whys[4]) ;
             exes[3] = exes[1] - 10 ;
             offsGg.drawString("3", exes[3], whys[3]) ;
// plume into static
         if(plumopt ==1) {
            for (j=1 ; j<=ncycle; ++j) {
             for (i = 1; i <= 2; ++i) {
               exes[0] = xorgn + (int) (.5*fact*(strx[2][i] +(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*stry[2][i]) + yt ;    
               exes[1] = xorgn + (int) (.5*fact*(strx[3][i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*stry[3][i]) + yt ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
               exes[4] = (int) (.5*(exes[0] + exes[1])) ;
               if(i==2) {
                  offsGg.drawString("4", exes[4], whys[4]) ;
               }   

               exes[0] = xorgn + (int) (.5*fact*(strx[3][i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;   
               whys[0] = yorgn + (int) (.5*si*fact*stry[3][i]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*(strx[4][i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*stry[4][i]) + yt ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
               exes[3] = (int) (.5*(exes[0] + exes[1])) ;
               if(i==1) {
                  offsGg.drawString("5", exes[3], whys[3]) ;
               }   

               exes[0] = xorgn + (int) (.5*fact*(strx[4][i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;   
               whys[0] = yorgn + (int) (.5*si*fact*stry[4][i]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*(strx[5][i]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*stry[5][i]) + yt ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
               exes[4] = (int) (.5*(exes[0] + exes[1])) ;
               exes[3] = xorgn + (int) (.5*fact*(strx[5][2]+(j-1)*(strx[5][2]-strx[2][2]))) + xt ;
               if(i==2) {
                  offsGg.drawString("6", exes[4], whys[4]) ;
               } 
               if(i==1) {
                  offsGg.drawString("7", exes[3], whys[3]) ;
               }    
             }
            }
          }
// plume into supersonic
           if(plumopt ==2) {
               exes[0] = xorgn + (int) (.5*fact*strx[2][2]) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*stry[2][2]) + yt ;    
               exes[1] = xorgn + (int) (.5*fact*strx[3][2]) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*stry[3][2]) + yt ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;

    // labels
                 exes[0] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                 whys[1] = whys[0] + si*15 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("1", exes[0], whys[1]) ; 
                 exes[0] = xorgn + (int) (.5*fact*(.5 *(exbgn[4]+exbgn[5]))) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[5]) + yt ;
                 whys[1] = whys[0] + si*10 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("2", exes[0], whys[1]) ; 
                 exes[0] = xorgn + (int) (.5*fact*exbgn[5]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*eybgn[5]) + yt ;
                 whys[1] = whys[0] + si*10 ;
                 offsGg.setColor(Color.red) ; 
                 offsGg.drawString("3", exes[0]+50, whys[1]) ; 
           }
          }
         }
       } // end of plume

      if (prob >= 4 && prob <= 5) {  // plug nozzle - CC Lee
         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
//geometry
          jbgn = 1;
 // flow
         numplg = numray ;
         if (nprat <= 1.0) numplg = numd+1 ;
// MOC mesh
         if(vgrid ==1) {
           offsGg.setColor(Color.blue) ;
           for(i=jbgn; i<=numplg; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[1][i]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[2][i]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[2][i]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
// geometry labels
 
           exes[0] = xorgn + (int) (.5*fact*xexit) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*yexit) + yt ;
           offsGg.setColor(Color.red) ;
           offsGg.drawString("ex",exes[0]+5,whys[0]) ;

           exes[0] = xorgn + (int) (.5*fact*xpexit) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*ypexit) + yt ; 
           offsGg.setColor(Color.red) ;
           offsGg.drawString("p ex",exes[0]+5,whys[0]) ;

           if (prob == 5) {
              exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[1]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[1]) + yt ;
              offsGg.setColor(Color.red) ; 
              offsGg.drawString("th",exes[0],whys[0]-15*si) ; 
           }
           if (prob == 4) {
              exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[2]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[2]) + yt ;
              offsGg.setColor(Color.red) ; 
              offsGg.drawString("th",exes[0]-15,whys[0]-15*si) ; 
           }
    
         }
// plot
         if (vplot == 1) {
            if (prob >= 4) {
               for(i=jbgn; i<=numplg; ++i) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[2][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[2][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[1][i-1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i-1]) + yt ;
                 if (vpress == 1) var = mcpres[1][i] ;
                 if (vtemp == 1) var = mctemp[1][i] ;
                 if (vmach == 1) var = mcmach[1][i] ;
                 if (vturn == 1) var = mcturn[1][i] ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
              }
            }
            if (prob == 5) {
               for(i=2; i<=jexm1; ++i) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[2][i]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[2][i]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[1][i]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[1][i-1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[1][i-1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[2][i-1]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[2][i-1]) + yt ;  
                 if (vpress == 1) var = .5 *(mcpres[1][i] + mcpres[1][i-1]) ;
                 if (vtemp == 1) var = .5 *(mctemp[1][i] + mctemp[1][i-1]) ;
                 if (vmach == 1) var = .5 *(mcmach[1][i] + mcmach[1][i-1]) ;
                 if (vturn == 1) var = .5 *(mcturn[1][i] + mcturn[1][i-1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
            }
          }
   //       if (prob == 5) jbgn = jexplug -1 ;
          for (j=jbgn ; j<=numray; ++j) {
             exes[0] = xorgn + (int) (.5*fact*nzht*wxbgn[j]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*wybgn[j]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*wxnd[j]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*wynd[j]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             if (vgrid == 1) {
               offsGg.setColor(Color.red) ;
               offsGg.fillPolygon(exes,whys,4) ;
             }
             exes[2] = exes[1] ;
             whys[2] = yorgn - (int) (0.0) + yt;
             exes[3] = exes[0] ;
             whys[3] = yorgn - (int) (0.0) + yt ;
             offsGg.setColor(Color.white) ;
             offsGg.fillPolygon(exes,whys,4) ;
          }
          exes[0] = xorgn + (int) (.5*fact*nzht*wxbgn[0]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*wybgn[0]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*nzht*wxnd[0]) + xt ;
          whys[1] = yorgn + (int) (.5*si*fact*nzht*wynd[0]) + yt ;
          exes[2] = exes[1] ;
          whys[2] = whys[1] + 5*si ;
          exes[3] = exes[0] ;
          whys[3] = whys[0] + 5*si ;
          offsGg.setColor(Color.red) ;
          offsGg.fillPolygon(exes,whys,4) ;
          if (prob == 4) { 
             for (j=1 ; j<=2; ++j) {
                exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[j]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[j]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*icxnd[j]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*icynd[j]) + yt ;
                exes[2] = exes[1] ;
                whys[2] = whys[1] + 5*si ;
                exes[3] = exes[0] ;
                whys[3] = whys[0] + 5*si ;
                offsGg.setColor(Color.red) ;
                offsGg.fillPolygon(exes,whys,4) ;
              }
          }
          if (prob == 5) { 
             for (j=1 ; j<=jexm1; ++j) {
                exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[j]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[j]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*icxnd[j]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*icynd[j]) + yt ;
                exes[2] = exes[1] ;
                whys[2] = whys[1] + 5*si ;
                exes[3] = exes[0] ;
                whys[3] = whys[0] + 5*si ;
                offsGg.setColor(Color.red) ;
                offsGg.fillPolygon(exes,whys,4) ;
              }
          }
// chosen point
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[1][col]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[1][col]) + yt ;
             offsGg.setColor(Color.black) ;
             offsGg.drawString("o",exes[0]-3,whys[0]+5) ;
        }
      }

// Free stream flow arrow & label
      if(plumopt == 2) {
       if (prob <= 3) {
          exes[0] = xorgn + (int) (.5*fact*nzht*ecxbgn[1]) + xt ;   
          whys[0] = yorgn - (int) (.5*fact*nzht*ecybgn[1]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*nzht*ecxnd[1]) + xt ;
          whys[1] = yorgn - (int) (.5*fact*nzht*ecynd[1]) + yt ;
          xlab = exes[0] ;
          ylab = whys[0] - 60 ;
          offsGg.setColor(Color.blue) ;
          offsGg.drawString("External", xlab,ylab) ;
          offsGg.drawString("Flow", xlab+20, ylab+15) ;
          offsGg.setColor(Color.black) ;
          offsGg.fillRect(xlab + 10,ylab+30,30,9) ;
          exes[0] = xlab + 50 ;
          whys[0] = ylab + 35;
          exes[1] = xlab + 40;
          whys[1] = ylab + 45;
          exes[2] = xlab + 40;
          whys[2] = ylab + 25;
          Polygon poly1 = new Polygon(exes,whys,3) ;
          offsGg.fillPolygon(poly1) ;
       }
       if (prob >= 4) {
          exes[0] = xorgn + (int) (.5*fact*nzht*ecxbgn[1]) + xt ;   
          whys[0] = yorgn - (int) (.5*fact*nzht*ecybgn[1]) + yt ;
          exes[1] = xorgn + (int) (.5*fact*nzht*ecxnd[1]) + xt ;
          whys[1] = yorgn - (int) (.5*fact*nzht*ecynd[1]) + yt ;
          xlab = exes[0] ;
          ylab = whys[0] - 60 ;
          offsGg.setColor(Color.blue) ;
          offsGg.drawString("External", xlab,ylab) ;
          offsGg.drawString("Flow", xlab+20, ylab+15) ;
          offsGg.setColor(Color.black) ;
          offsGg.fillRect(xlab + 10,ylab+30,30,9) ;
          exes[0] = xlab + 50 ;
          whys[0] = ylab + 35;
          exes[1] = xlab + 40;
          whys[1] = ylab + 45;
          exes[2] = xlab + 40;
          whys[2] = ylab + 25;
          Polygon poly1 = new Polygon(exes,whys,3) ;
          offsGg.fillPolygon(poly1) ;
       }
     }
 // zoom widget
        offsGg.setColor(Color.white) ;
        offsGg.fillRect(0,0,30,375) ;
        offsGg.setColor(Color.black) ;
        offsGg.drawLine(15,10,15,230) ;
        offsGg.fillRect(5,sldloc,20,5) ;

//  buttons
//diagnostics
/*
        offsGg.setColor(Color.blue) ;
        if (diout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(600,5,85,15) ;
        offsGg.setColor(Color.white) ;
        if (diout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Diagnostics",610,16) ;
*/
// reflect button
        offsGg.setColor(Color.blue) ;
        if (reflect == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(135,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (reflect == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Reflect",137,16) ;
// Mesh button
        offsGg.setColor(Color.blue) ;
        if (vgrid == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(85,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (vgrid == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString(" Mesh",87,16) ;
// Plot button
        offsGg.setColor(Color.blue) ;
        if (vplot == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(35,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (vplot == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Plot",40,16) ;

// bottom labels

        offsGg.setColor(Color.black) ;
        offsGg.drawString("Zoom",2,245) ;
        offsGg.drawRect(5,250,15,15) ;
        offsGg.drawString("-",10,262) ;
        offsGg.drawRect(25,250,15,15) ;
        offsGg.drawString("+",30,262) ;
 // find button
        offsGg.setColor(Color.white) ;
        offsGg.fillRect(5,270,40,15) ;
        offsGg.setColor(Color.red) ;
        offsGg.drawString("Find",10,282) ;

 //       offsGg.setColor(Color.red) ;
 //       offsGg.drawString("Nozzle Flows - Version 1.7c",300,10) ;

 //input buttons
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Input-",200,16) ;

        offsGg.setColor(Color.blue) ;
        if (intin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(235,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (intin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Internal",240,16) ;

        offsGg.setColor(Color.blue) ;
        if (extin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(300,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (extin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("External",305,16) ;

        offsGg.setColor(Color.blue) ;
        if (anlin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(365,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (anlin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Analysis",370,16) ;

 //output buttons
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Output-",430,16) ;

        offsGg.setColor(Color.blue) ;
        if (cbar == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(475,5,60,15) ;
        offsGg.setColor(Color.white) ;
        if (cbar == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Color Bar",480,16) ;

        offsGg.setColor(Color.blue) ;
        if (perfout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(545,5,80,15) ;
        offsGg.setColor(Color.white) ;
        if (perfout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Performance",550,16) ;

        offsGg.setColor(Color.blue) ;
        if (geomout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(635,5,95,15) ;
        offsGg.setColor(Color.white) ;
        if (geomout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Flow-Geometry",640,16) ;
/*
        offsGg.setColor(Color.blue) ;
        if (geomr == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(695,250,55,15) ;
        offsGg.setColor(Color.white) ;
        if (geomr == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Record",700,261) ;
*/
 // exit button
        offsGg.setColor(Color.red) ;
        offsGg.fillRect(770,5,45,15) ;
        offsGg.setColor(Color.white) ;
        offsGg.drawString("Exit",780,16) ;

 //  draw x-y axes
        if (vgrid == 1) {
           exes[0] = xorgn + (int) (0.0) + xt ;
           whys[0] = yorgn + (int) (0.0) + yt ;
           exes[1] = xorgn + (int) (12.0) + xt ;
           whys[1] = yorgn + (int) (0.0) + yt ;
           offsGg.setColor(Color.black) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawString("X",exes[1]+5,whys[1]) ;
           exes[0] = xorgn + (int) (0.0) + xt ;
           whys[0] = yorgn + (int) (0.0) + yt ;
           exes[1] = xorgn + (int) (0.0) + xt ;
           whys[1] = yorgn - (int) (12.0) + yt ;
           offsGg.setColor(Color.black) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawString("Y",exes[1],whys[1]-5) ;
       }

       g.drawImage(offscreenImg,0,0,this) ;
      }
    }

     public void paint7(Graphics g) {
       int i,j,k ;
       int jl, jr, jcl, jcr, jex, grp;
       int exes[] = new int[10] ;
       int whys[] = new int[10] ;
       int yorgn = 200 ;
       int xorgn = 50 ;
       int xlab,ylab ;
       int numplg,jbgn ;
       int n,nmax, si ;
       double xmax,xmin,xval;
       double xpt,ypt,xpt2,ypt2 ;
       Color colr ;

       xmin = varmin ;
       xmax = varmax ;

       colr = getColbar(xmin,xmax,1.0) ;
       offsGg.setColor(Color.white) ;
       offsGg.fillRect(0,0,1100,500) ;
//       offsGg.setColor(Color.red) ;
//       offsGg.drawString("0",xorgn + 5,70) ;
          // draw geometry
       offsGg.setColor(Color.red) ;
       offsGg.drawString("1", exes[1]-70, whys[1]-10) ;

// centerline
          exes[0] = xorgn + (int) (.5*fact*-500.0) + xt ;   
          whys[0] = yorgn - (int) (.5*fact*0.0) + yt ;
          exes[1] = xorgn + (int) (.5*fact*500.0) + xt ;
          whys[1] = yorgn - (int) (.5*fact*0.0) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;

         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
//  external flow
           if (plumopt == 2) {
             if(vgrid == 1) {
              for(i=1; i<=2; ++i) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[i]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[i]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[i]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[i]) + yt ;
                offsGg.setColor(Color.blue) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
             }
             if (vplot == 1) {
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                if (vpress == 1) var = pres[1][0] ;
                if (vtemp == 1) var = temp[1][0]  ;
                if (vmach == 1) var = mach2[1][0] ;
                if (vturn == 1) var = turn[1][0]  ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[1]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[1]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*exnd[1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                if (vpress == 1) var = .5* (pres[1][0] + pres[1][1]) ;
                if (vtemp == 1) var = .5* (temp[1][0] + temp[1][1]) ;
                if (vmach == 1) var = .5* (mach2[1][0] + mach2[1][1]) ;
                if (vturn == 1) var = .5* (turn[1][0] + turn[1][1]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
                 exes[0] = xorgn + (int) (.5*fact*nzht*exbgn[2]) + xt ;   
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*eybgn[2]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*exnd[2]) + xt ;   
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*eynd[2]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*exnd[3]) + xt ;   
                 whys[2] = yorgn + (int) (.5*si*fact*eynd[3]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*exbgn[3]) + xt ;   
                 whys[3] = yorgn + (int) (.5*si*fact*eybgn[3]) + yt ;
                 if (vpress == 1) var = pres[1][1]  ;
                 if (vtemp == 1) var =  temp[1][1]  ;
                 if (vmach == 1) var =  mach2[1][1] ;
                 if (vturn == 1) var =  turn[1][1]  ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
             }
           }
 // draw geom
 // internal surface
           for(i=1; i<=2; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*icxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*icynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
// external surface
           for(i=1; i<=2; ++i) {
             exes[0] = xorgn + (int) (.5*fact*nzht*ecxbgn[i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*ecybgn[i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*ecxnd[i]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*ecynd[i]) + yt ;
             exes[2] = exes[1] ;
             whys[2] = whys[1] + 5*si ;
             exes[3] = exes[0] ;
             whys[3] = whys[0] + 5*si ;
             offsGg.setColor(Color.red) ;
             offsGg.fillPolygon(exes,whys,4) ;
           }
           exes[0] = xorgn + (int) (.5*fact*nzht*icxbgn[1]) + xt ;
           whys[0] = yorgn + (int) (.5*si*fact*nzht*icybgn[1]) + yt ;
           exes[1] = xorgn + (int) (.5*fact*nzht*ecxbgn[1]) + xt ;
           whys[1] = yorgn + (int) (.5*si*fact*nzht*ecybgn[1]) + yt ;
           exes[2] = exes[1] + 5 ;
           whys[2] = whys[1] ;
           exes[3] = exes[0] + 5;
           whys[3] = whys[0] ;
           offsGg.setColor(Color.red) ;
           offsGg.fillPolygon(exes,whys,4) ;
         }

         nmax = 1;
         si = -1 ;
         if (reflect == 1) nmax = 2;
         for(n=1; n<=nmax; ++n) {
           if(n == 2) si = 1 ;
 // flow
         if(vgrid ==1) {
           offsGg.setColor(Color.blue) ;
           for(i=0; i<=numray/2; ++i) {
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][1]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][1]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][1]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][1]) + yt ;
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           }
   // expansion from cowl
           for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
   // complex region along centerline
           offsGg.setColor(Color.black) ;
           for(k=2; k<=numray/2+1; ++k) {
              for (i=k-1; i<=numray/2-1; ++i) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                 offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }

         for(grp=1; grp<=group; ++grp) {
           offsGg.setColor(Color.red) ;
           jl = (grp-1)*(numray/2) + 2 ;
           jr = grp*(numray/2)+1 ;
   // left running
           for(k=jl; k<=jr; ++k) {
              i=jr-1;
              if (i < numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
   //  right running
           offsGg.setColor(Color.blue) ;
           for(i=jr; i<=jr + numray/2; ++i) {
              if(i <= numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jr]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jr]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][jr+1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jr+1]) + yt ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
   // complex region on cowl
           offsGg.setColor(Color.black) ;
           for(jcr=1; jcr<=numray/2-1; ++jcr) {
              i = grp*numray/2 + jcr ;
              if (i <= numd) {
                for (jcl=jcr; jcl<=numray/2-1; ++jcl) {
                  k = (grp-1)*numray/2 + 1 + jcl ;
                  exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                  whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                  exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                  whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                  exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                  whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                  offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                  offsGg.drawLine(exes[0],whys[0],exes[2],whys[2]) ;
                }
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jr]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jr]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jr]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jr]) + yt ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
   // complex region on centerline
           for(jcl=1; jcl<=numray/2-1; ++jcl) {
              k = grp*numray/2 + 1 + jcl ;
              for (jcr=jcl; jcr<=numray/2-1; ++jcr) {
                 i = grp*numray/2 + jcr ;
                 if(i<=numd) {
                   exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                   whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                   whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                   exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                   whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                   offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                   offsGg.drawLine(exes[0],whys[0],exes[2],whys[2]) ;
                 }
              }
              if(i<=numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
         }
//  last right running - numd+1
         i = numd +1 ;
         k = numd - numray/2 + 1 ;
         for (jcr=k; jcr<=numd + 1; ++jcr) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jcr]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcr]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][jcr+1]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcr+1]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][jcr]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][jcr]) + yt ;
             offsGg.setColor(Color.black) ;
             offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             offsGg.setColor(Color.red) ;
             offsGg.drawLine(exes[0],whys[0],exes[2],whys[2]) ;
         }

// geometry labels
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[1]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[1]) + yt ;
            offsGg.setColor(Color.red) ;
            offsGg.drawString("th",exes[0]-10,whys[0]-15*si) ; 
            exes[0] = xorgn + (int) (.5*fact*nzht*icxnd[numray]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*icynd[numray]) + yt ;
            offsGg.setColor(Color.red) ;
         }
// end of mesh plot
// begin of contour plot
          if (vplot == 1) {
            for(i=1; i<=numray/2; ++i) {
              for (k=1; k<=i; ++k) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]) ;
                 if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                 if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                 if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
              }
            }
            j = 1 ;
            exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
            whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
            exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
            whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
            exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+2]) + xt ;
            whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+2]) + yt ;
            if (vpress == 1) var = mcpres[j][j+2] ;
            if (vtemp == 1) var = mctemp[j][j+2] ;
            if (vmach == 1) var = mcmach[j][j+2] ;
            if (vturn == 1) var = mcturn[j][j+2] ;
            colr = getColbar(xmin,xmax,var) ;
            offsGg.setColor(colr) ;
            offsGg.fillPolygon(exes,whys,3) ;
            for (j=2; j<=numray/2; ++j) {
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[j-1][j]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[j-1][j]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[j][j]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[j][j+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[j][j+1]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[j-1][j] + 2.0 * mcpres[j][j] + mcpres[j][j+1]) ;
                 if (vtemp == 1) var = .25 * (mctemp[j-1][j] + 2.0 * mctemp[j][j] + mctemp[j][j+1]) ;
                 if (vmach == 1) var = .25 * (mcmach[j-1][j] + 2.0 * mcmach[j][j] + mcmach[j][j+1]) ;
                 if (vturn == 1) var = .25 * (mcturn[j-1][j] + 2.0 * mcturn[j][j] + mcturn[j][j+1]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,3) ;
            }
 // groups
         for(grp=1; grp<=group; ++grp) {
           jl = (grp-1)*(numray/2) + 2 ;
           jr = grp*(numray/2)+1 ;
   // left running
           for(k=jl; k<=jr-1; ++k) {
              i=jr-1;
              if(i<=numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]);
                if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,4) ;
              }
           }
           if(jr<=numd+1) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[jr-1][jr]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-1][jr]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[jr][jr]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[jr][jr]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[jr][jr+1]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[jr][jr+1]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[jr-1][jr] + 2.0*mcpres[jr][jr] + mcpres[jr][jr+1]);
             if (vtemp == 1) var = .25 * (mctemp[jr-1][jr] + 2.0*mctemp[jr][jr] + mctemp[jr][jr+1]) ;
             if (vmach == 1) var = .25 * (mcmach[jr-1][jr] + 2.0*mcmach[jr][jr] + mcmach[jr][jr+1]) ;
             if (vturn == 1) var = .25 * (mcturn[jr-1][jr] + 2.0*mcturn[jr][jr] + mcturn[jr][jr+1]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,3) ;
           }
   //  right running
           for(i=jr; i<=jr-2+numray/2; ++i) {
              if(i<=numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jr]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jr]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jr]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jr]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jr+1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jr+1]) + yt ;
                exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i][jr+1]) + xt ;
                whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jr+1]) + yt ;
                if (vpress == 1) var = .25 * (mcpres[i][jr] + mcpres[i][jr+1] + mcpres[i+1][jr+1] + mcpres[i+1][jr]);
                if (vtemp == 1) var = .25 * (mctemp[i][jr] + mctemp[i][jr+1] + mctemp[i+1][jr+1] + mctemp[i+1][jr]) ;
                if (vmach == 1) var = .25 * (mcmach[i][jr] + mcmach[i][jr+1] + mcmach[i+1][jr+1] + mcmach[i+1][jr]) ;
                if (vturn == 1) var = .25 * (mcturn[i][jr] + mcturn[i][jr+1] + mcturn[i+1][jr+1] + mcturn[i+1][jr]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,4) ;
             }
           }
           if(jr<=numd+1) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[jr-1][jr-(numray/2)]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-1][jr-(numray/2)]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[jr-1][jr+1-(numray/2)]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-1][jr+1-(numray/2)]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[jr][jr+1-(numray/2)]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[jr][jr+1-(numray/2)]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[jr-1][jr-(numray/2)] + 2.0*mcpres[jr-1][jr+1-(numray/2)] + mcpres[jr][jr+1-(numray/2)]);
             if (vtemp == 1) var = .25 * (mctemp[jr-1][jr-(numray/2)] + 2.0*mctemp[jr-1][jr+1-(numray/2)] + mctemp[jr][jr+1-(numray/2)]) ;
             if (vmach == 1) var = .25 * (mcmach[jr-1][jr-(numray/2)] + 2.0*mcmach[jr-1][jr+1-(numray/2)] + mcmach[jr][jr+1-(numray/2)]) ;
             if (vturn == 1) var = .25 * (mcturn[jr-1][jr-(numray/2)] + 2.0*mcturn[jr-1][jr+1-(numray/2)] + mcturn[jr][jr+1-(numray/2)]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,3) ;
           }
   // complex region on cowl
           for(jcr=1; jcr<=numray/2-2; ++jcr) {
              i = grp*numray/2 + jcr ;
              if(i<=numd) {
               for (jcl=jcr; jcl<=numray/2-2; ++jcl) {
                 k = (grp-1)*numray/2 + 2 + jcl ;
                 exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                 whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                 exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                 whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                 exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                 whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                 exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                 whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                 if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]);
                 if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                 if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                 if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                 colr = getColbar(xmin,xmax,var) ;
                 offsGg.setColor(colr) ;
                 offsGg.fillPolygon(exes,whys,4) ;
               }
              exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][i-numray/2 + 1]) + xt ;
              whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i-numray/2+1]) + yt ;
              exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][i-numray/2+2]) + xt ;
              whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][i-numray/2+2]) + yt ;
              exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i][i-numray/2+2]) + xt ;
              whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i-numray/2+2]) + yt ;
              if (vpress == 1) var = .25 * (mcpres[i][i-numray/2+1] + 2.0*mcpres[i][i-numray/2+2] + mcpres[i+1][i-numray/2+1]);
              if (vtemp == 1) var = .25 * (mctemp[i][i-numray/2+1] + 2.0*mctemp[i][i-numray/2+2] + mctemp[i+1][i-numray/2+1]) ;
              if (vmach == 1) var = .25 * (mcmach[i][i-numray/2+1] + 2.0*mcmach[i][i-numray/2+2] + mcmach[i+1][i-numray/2+1]) ;
              if (vturn == 1) var = .25 * (mcturn[i][i-numray/2+1] + 2.0*mcturn[i][i-numray/2+2] + mcturn[i+1][i-numray/2+1]) ;
              colr = getColbar(xmin,xmax,var) ;
              offsGg.setColor(colr) ;
              offsGg.fillPolygon(exes,whys,3) ;
              }
           }
           if((jr-2+numray/2)<=numd) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[jr-2+numray/2][jr-1]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-2+numray/2][jr-1]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[jr-1+numray/2][jr]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-1+numray/2][jr]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[jr-2+numray/2][jr]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[jr-2+numray/2][jr]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[jr-2+numray/2][jr-1] + 2.0*mcpres[jr-2+numray/2][jr] + mcpres[jr-1+numray/2][jr]);
             if (vtemp == 1) var = .25 * (mctemp[jr-2+numray/2][jr-1] + 2.0*mctemp[jr-2+numray/2][jr] + mctemp[jr-1+numray/2][jr]) ;
             if (vmach == 1) var = .25 * (mcmach[jr-2+numray/2][jr-1] + 2.0*mcmach[jr-2+numray/2][jr] + mcmach[jr-1+numray/2][jr]) ;
             if (vturn == 1) var = .25 * (mcturn[jr-2+numray/2][jr-1] + 2.0*mcturn[jr-2+numray/2][jr] + mcturn[jr-1+numray/2][jr]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,3) ;
           }
   // complex region on centerline
           for(jcl=1; jcl<=numray/2-2; ++jcl) {
              k = grp*numray/2 + 1 + jcl ;
              for (jcr=jcl; jcr<=numray/2-2; ++jcr) {
                 i = grp*numray/2 + jcr + 1 ;
                 if(i<=numd) {
                   exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                   whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                   exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k]) + xt ;
                   whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k]) + yt ;
                   exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i+1][k+1]) + xt ;
                   whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][k+1]) + yt ;
                   exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                   whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                   if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i+1][k+1] + mcpres[i+1][k]);
                   if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i+1][k+1] + mctemp[i+1][k]) ;
                   if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i+1][k+1] + mcmach[i+1][k]) ;
                   if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i+1][k+1] + mcturn[i+1][k]) ;
                   colr = getColbar(xmin,xmax,var) ;
                   offsGg.setColor(colr) ;
                   offsGg.fillPolygon(exes,whys,4) ;
                 }
              }
              if((k-1)<=numd) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[k-1][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[k-1][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[k][k]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[k][k]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcx[k][k+1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[k][k+1]) + yt ;
                if (vpress == 1) var = .25 * (mcpres[k-1][k] + 2.0*mcpres[k][k] + mcpres[k][k+1]);
                if (vtemp == 1) var = .25 * (mctemp[k-1][k] + 2.0*mctemp[k][k] + mctemp[k][k+1]) ;
                if (vmach == 1) var = .25 * (mcmach[k-1][k] + 2.0*mcmach[k][k] + mcmach[k][k+1]) ;
                if (vturn == 1) var = .25 * (mcturn[k-1][k] + 2.0*mcturn[k][k] + mcturn[k][k+1]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,3) ;
              }
           }
           jex = (grp+1)*numray/2 ;
           if(jex <= numd) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[jex-1][jex]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[jex-1][jex]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[jex][jex]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[jex][jex]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[jex][jex+1]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[jex][jex+1]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[jex-1][jex] + 2.0*mcpres[jex][jex] + mcpres[jex][jex+1]);
             if (vtemp == 1) var = .25 * (mctemp[jex-1][jex] + 2.0*mctemp[jex][jex] + mctemp[jex][jex+1]) ;
             if (vmach == 1) var = .25 * (mcmach[jex-1][jex] + 2.0*mcmach[jex][jex] + mcmach[jex][jex+1]) ;
             if (vturn == 1) var = .25 * (mcturn[jex-1][jex] + 2.0*mcturn[jex][jex] + mcturn[jex][jex+1]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,3) ;
           }
          }
//  last right running - numd+1
         i = numd +1 ;
         k = numd - numray/2 + 1 ;
         for (jcr=k; jcr<=numd ; ++jcr) {
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jcr]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcr]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][jcr+1]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcr+1]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][jcr+1]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][jcr+1]) + yt ;
             exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i-1][jcr]) + xt ;
             whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][jcr]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[i][jcr] + mcpres[i][jcr+1] + mcpres[i-1][jcr+1] + mcpres[i-1][jcr]);
             if (vtemp == 1) var = .25 * (mctemp[i][jcr] + mctemp[i][jcr+1] + mctemp[i-1][jcr+1] + mctemp[i-1][jcr]) ;
             if (vmach == 1) var = .25 * (mcmach[i][jcr] + mcmach[i][jcr+1] + mcmach[i-1][jcr+1] + mcmach[i-1][jcr]) ;
             if (vturn == 1) var = .25 * (mcturn[i][jcr] + mcturn[i][jcr+1] + mcturn[i-1][jcr+1] + mcturn[i-1][jcr]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,4) ;
         }
             exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][i]) + xt ;
             whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i]) + yt ;
             exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][i+1]) + xt ;
             whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i+1]) + yt ;
             exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][i]) + xt ;
             whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][i]) + yt ;
             if (vpress == 1) var = .25 * (mcpres[i-1][i] + 2.0*mcpres[i][i] + mcpres[i][i+1]);
             if (vtemp == 1) var = .25 * (mctemp[i-1][i] + 2.0*mctemp[i][i] + mctemp[i][i+1]) ;
             if (vmach == 1) var = .25 * (mcmach[i-1][i] + 2.0*mcmach[i][i] + mcmach[i][i+1]) ;
             if (vturn == 1) var = .25 * (mcturn[i-1][i] + 2.0*mcturn[i][i] + mcturn[i][i+1]) ;
             colr = getColbar(xmin,xmax,var) ;
             offsGg.setColor(colr) ;
             offsGg.fillPolygon(exes,whys,3) ;
        }
//  WORKING HERE!
         if(plumopt == 1) { // plume into vacuum
           jex = numd + 2 ;
           jcl = numd - numray/2 + 1 ;
           if(vgrid ==1) {
     // plume flow - grid lines
             for(j=0; j<=numray; ++j) {
              i = jex + j ;
              for(k=jcl+j; k<=i; ++k) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][k]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][k]) + yt ;
                offsGg.setColor(Color.red) ;
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                offsGg.drawLine(exes[0],whys[0],exes[2],whys[2]) ;
              }
     //  plume streamline
               exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jcl+j]) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcl+j]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jcl+j+1]) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jcl+j+1]) + yt ;
               offsGg.setColor(Color.black) ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
           }
           if(vplot ==1) {
     // plume flow - color graphics
     //  core
             for(j=0; j<=numray; ++j) {
              i = jex + j ;
              for(k=jcl+j; k<=i-1; ++k) {
                exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][k]) + xt ;
                whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k]) + yt ;
                exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][k+1]) + xt ;
                whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][k+1]) + yt ;
                exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][k+1]) + xt ;
                whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][k+1]) + yt ;
                exes[3] = xorgn + (int) (.5*fact*nzht*mcx[i-1][k]) + xt ;
                whys[3] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][k]) + yt ;
                if (vpress == 1) var = .25 * (mcpres[i][k] + mcpres[i][k+1] + mcpres[i-1][k+1] + mcpres[i-1][k]);
                if (vtemp == 1) var = .25 * (mctemp[i][k] + mctemp[i][k+1] + mctemp[i-1][k+1] + mctemp[i-1][k]) ;
                if (vmach == 1) var = .25 * (mcmach[i][k] + mcmach[i][k+1] + mcmach[i-1][k+1] + mcmach[i-1][k]) ;
                if (vturn == 1) var = .25 * (mcturn[i][k] + mcturn[i][k+1] + mcturn[i-1][k+1] + mcturn[i-1][k]) ;
                colr = getColbar(xmin,xmax,var) ;
                offsGg.setColor(colr) ;
                offsGg.fillPolygon(exes,whys,4) ;
              }
     // centerline
               exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][i]) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i][i+1]) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i][i+1]) + yt ;
               exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i-1][i]) + xt ;
               whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i-1][i]) + yt ;
               if (vpress == 1) var = .25 * (2.0* mcpres[i][i] + mcpres[i][i+1] + mcpres[i-1][i]);
               if (vtemp == 1) var = .25 * (2.0*mctemp[i][i] + mctemp[i][i+1] + mctemp[i-1][i]) ;
               if (vmach == 1) var = .25 * (2.0*mcmach[i][i] + mcmach[i][i+1] + mcmach[i-1][i]) ;
               if (vturn == 1) var = .25 * (2.0*mcturn[i][i] + mcturn[i][i+1] + mcturn[i-1][i]) ;
               colr = getColbar(xmin,xmax,var) ;
               offsGg.setColor(colr) ;
               offsGg.fillPolygon(exes,whys,3) ;
     // pressure boundary
               exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jcl+j]) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcl+j]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jcl+j+1]) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jcl+j+1]) + yt ;
               exes[2] = xorgn + (int) (.5*fact*nzht*mcx[i][jcl+j+1]) + xt ;
               whys[2] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcl+j+1]) + yt ;
               if (vpress == 1) var = .25 * (2.0* mcpres[i][jcl+j] + mcpres[i+1][jcl+j+1] + mcpres[i][jcl+j+1]);
               if (vtemp == 1) var = .25 * (2.0*mctemp[i][jcl+j] + mctemp[i+1][jcl+j+1] + mctemp[i][jcl+j+1]) ;
               if (vmach == 1) var = .25 * (2.0*mcmach[i][jcl+j] + mcmach[i+1][jcl+j+1] + mcmach[i][jcl+j+1]) ;
               if (vturn == 1) var = .25 * (2.0*mcturn[i][jcl+1] + mcturn[i+1][jcl+j+1] + mcturn[i][jcl+j+1]) ;
               colr = getColbar(xmin,xmax,var) ;
               offsGg.setColor(colr) ;
               offsGg.fillPolygon(exes,whys,3) ;

     //  plume streamline
               exes[0] = xorgn + (int) (.5*fact*nzht*mcx[i][jcl+j]) + xt ;
               whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[i][jcl+j]) + yt ;
               exes[1] = xorgn + (int) (.5*fact*nzht*mcx[i+1][jcl+j+1]) + xt ;
               whys[1] = yorgn + (int) (.5*si*fact*nzht*mcy[i+1][jcl+j+1]) + yt ;
               offsGg.setColor(Color.black) ;
               offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
           }

         }

// chosen point
          exes[0] = xorgn + (int) (.5*fact*nzht*mcx[row][col]) + xt ;
          whys[0] = yorgn + (int) (.5*si*fact*nzht*mcy[row][col]) + yt ;
          offsGg.setColor(Color.red) ;
          offsGg.drawString("o",exes[0]-3,whys[0]+5) ;
        }

 // zoom widget
        offsGg.setColor(Color.white) ;
        offsGg.fillRect(0,0,30,375) ;
        offsGg.setColor(Color.black) ;
        offsGg.drawLine(15,10,15,230) ;
        offsGg.fillRect(5,sldloc,20,5) ;

//  buttons
//diagnostics
/*
        offsGg.setColor(Color.blue) ;
        if (diout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(600,5,85,15) ;
        offsGg.setColor(Color.white) ;
        if (diout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Diagnostics",610,16) ;
*/
// reflect button
        offsGg.setColor(Color.blue) ;
        if (reflect == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(135,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (reflect == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Reflect",137,16) ;
// Mesh button
        offsGg.setColor(Color.blue) ;
        if (vgrid == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(85,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (vgrid == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString(" Mesh",87,16) ;
// Plot button
        offsGg.setColor(Color.blue) ;
        if (vplot == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(35,5,45,15) ;
        offsGg.setColor(Color.white) ;
        if (vplot == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Plot",40,16) ;

// bottom labels
        offsGg.setColor(Color.black) ;
        offsGg.drawString("Zoom",2,245) ;
        offsGg.drawRect(5,250,15,15) ;
        offsGg.drawString("-",10,262) ;
        offsGg.drawRect(25,250,15,15) ;
        offsGg.drawString("+",30,262) ;
 // find button
        offsGg.setColor(Color.white) ;
        offsGg.fillRect(5,270,40,15) ;
        offsGg.setColor(Color.red) ;
        offsGg.drawString("Find",10,282) ;

 //       offsGg.setColor(Color.red) ;
 //       offsGg.drawString("Nozzle Flows - Version 1.7c",300,10) ;

 //input buttons
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Input-",200,16) ;

        offsGg.setColor(Color.blue) ;
        if (intin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(235,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (intin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Internal",240,16) ;

        offsGg.setColor(Color.blue) ;
        if (extin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(300,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (extin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("External",305,16) ;

        offsGg.setColor(Color.blue) ;
        if (anlin == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(365,5,55,15) ;
        offsGg.setColor(Color.white) ;
        if (anlin == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Analysis",370,16) ;

 //output buttons
        offsGg.setColor(Color.blue) ;
        offsGg.drawString("Output-",430,16) ;

        offsGg.setColor(Color.blue) ;
        if (cbar == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(475,5,60,15) ;
        offsGg.setColor(Color.white) ;
        if (cbar == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Color Bar",480,16) ;

        offsGg.setColor(Color.blue) ;
        if (perfout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(545,5,80,15) ;
        offsGg.setColor(Color.white) ;
        if (perfout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Performance",550,16) ;

        offsGg.setColor(Color.blue) ;
        if (geomout == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(635,5,95,15) ;
        offsGg.setColor(Color.white) ;
        if (geomout == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Flow-Geometry",640,16) ;
/*
        offsGg.setColor(Color.blue) ;
        if (geomr == 1) offsGg.setColor(Color.yellow) ;
        offsGg.fillRect(695,250,55,15) ;
        offsGg.setColor(Color.white) ;
        if (geomr == 1) offsGg.setColor(Color.black) ;
        offsGg.drawString("Record",700,261) ;
*/
 // exit button
        offsGg.setColor(Color.red) ;
        offsGg.fillRect(770,5,45,15) ;
        offsGg.setColor(Color.white) ;
        offsGg.drawString("Exit",780,16) ;

 //  draw x-y axes
        if (vgrid == 1) {
           exes[0] = xorgn + (int) (0.0) + xt ;
           whys[0] = yorgn + (int) (0.0) + yt ;
           exes[1] = xorgn + (int) (12.0) + xt ;
           whys[1] = yorgn + (int) (0.0) + yt ;
           offsGg.setColor(Color.black) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawString("X",exes[1]+5,whys[1]) ;
           exes[0] = xorgn + (int) (0.0) + xt ;
           whys[0] = yorgn + (int) (0.0) + yt ;
           exes[1] = xorgn + (int) (0.0) + xt ;
           whys[1] = yorgn - (int) (12.0) + yt ;
           offsGg.setColor(Color.black) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawString("Y",exes[1],whys[1]-5) ;
       }

       g.drawImage(offscreenImg,0,0,this) ;
     }
  }
   class Notification extends Dialog{
       String msg;

       public Notification(Frame d,String s) {
          super(d, "Notification",true) ;
          msg = s;
       }
       
       public void show() {
          add("North",new Label(msg,Label.CENTER)) ;
          Panel p = new Panel() ;
          p.add(new Button("OK")) ;
          add("South",p) ;
          setBackground(Color.gray) ;
          resize(160,100) ;
          super.show() ;
      }

      public boolean handleEvent(Event event) {
          if(event.id==Event.WINDOW_DESTROY) {
            dispose() ;
            return true;
          }
          else if(event.id==Event.ACTION_EVENT) {
            if(event.target instanceof Button) {
              if("OK".equals(event.arg)) {
                dispose() ;
                return true;
              }
            }
          }
          return false ;
    }
 }

 public static void main(String args[]) {
    Moc moc = new Moc() ;

    f = new Frame(" Nozzle MOC Application Version 1.7c") ;
    f.add("Center", moc) ;
    f.resize(850, 650);
    f.show() ;

    moc.init() ;
    moc.start() ;
 }

}
