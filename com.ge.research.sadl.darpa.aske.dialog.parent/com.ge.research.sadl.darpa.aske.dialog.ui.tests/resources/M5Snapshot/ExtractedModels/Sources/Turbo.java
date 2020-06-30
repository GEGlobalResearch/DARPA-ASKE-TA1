/*
                 EngineSim - Design and Wind Tunnel Mode
   
     Program to perform turbomachinery design and analysis
                a)   dry turbojet
                b)   afterburning turbojet
                c)   turbofan with separate nozzle
                d)   ramjet

                     Version 1.8a   - 19 Aug 14

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
 
      New Test  - 
                 * size for the portal
                 * re-size graphics
                 * re-size the .gifs
                 * change outputs
                 *    add pexit, pfexit and M2 to output
                 * update EngineSimU
                 * update application
                 * fix translation for viewer
                 * correct gross thrust calculation
                 * change view box for title
                   add CPR to output
                   fix bug in turbofan weight calculation
                 * add diagnostics panel 
                 *      debugg new problem with drop menus .. Dec 13
                   
                   work around Java security bugs .. take out .gif images



                                                     TJB 19 Aug 14
*/

import java.awt.*;
import java.lang.Math ;

public class Turbo extends java.applet.Applet {

   final double convdr = 3.14515926/180.;
 
   int abflag,entype,lunits,inflag,varflag,pt2flag,wtflag ;
   int abkeep,pltkeep,move ;
   int numeng,gamopt,arsched,plttyp,showcom ;
   int athsched,aexsched,fueltype,inptype,siztype ;
               // Flow variables
   static double g0d,g0,rgas,gama,cpair ;
   static double tt4,tt4d,tt7,tt7d,t8,p3p2d,p3fp2d,byprat,throtl;
   static double fsmach,altd,alt,ts0,ps0,q0,u0d,u0,a0,rho0,tsout,psout;
   static double epr,etr,npr,snpr,fnet,fgros,dram,sfc,fa,eair,uexit,ues;
   static double fnd,fnlb,fglb,drlb,flflo,fuelrat,fntot,eteng;
   static double arth,arthd,arexit,arexitd ;
   static double mexit,pexit,pfexit ;
   static double arthmn,arthmx,arexmn,arexmx ;
   static double a8,a8rat,a8d,afan,a7,m2,isp;
   static double acap,a2,a2d,acore,a4,a4p,fhv,fhvd,mfr,diameng ;
   static double altmin,altmax,u0min,u0max,thrmin,thrmax,pmax,tmin,tmax;
   static double etmin,etmax,cprmin,cprmax,t4min,t4max;
   static double a2min,a2max,a8min,a8max,t7min,t7max,diamin,diamax;
   static double bypmin,bypmax,fprmin,fprmax;
   static double vmn1,vmn2,vmn3,vmn4,vmx1,vmx2,vmx3,vmx4 ;
   static double lconv1,lconv2,fconv,pconv,tconv,tref,mconv1,mconv2,econv,econv2 ;
   static double aconv,bconv,dconv,flconv ;
               // weight and materials
   static double weight,wtref,wfref ;
   static int mcomp,mfan,mturbin,mburner,minlt,mnozl,mnozr ;
   static int ncflag,ncomp,ntflag,nturb,fireflag;
   static double dcomp,dfan,dturbin,dburner ;
   static double tcomp,tfan,tturbin,tburner ;
   static double tinlt,dinlt,tnozl,dnozl,tnozr,dnozr ;
   static double lcomp,lburn,lturb,lnoz;   // component length
               // Station Variables
   static double[] trat = new double[20] ;
   static double[] tt   = new double[20] ;
   static double[] prat = new double[20] ;
   static double[] pt   = new double[20] ;
   static double[] eta  = new double[20] ;
   static double[] gam  = new double[20] ;
   static double[] cp   = new double[20] ;
   static double[] s    = new double[20] ;
   static double[] v    = new double[20] ;
                 /* drawing geometry  */
   static double xtrans,ytrans,factor,gains,scale ;
   static double xtranp,ytranp,factp ;
   static double xg[][]  = new double[13][45] ;
   static double yg[][]  = new double[13][45] ;
   static int sldloc,sldplt,ncompd;
   static int antim,ancol ;
                 //  Percentage  variables
   static double u0ref,altref,thrref,a2ref,et2ref,fpref,et13ref,bpref ;
   static double cpref,et3ref,et4ref,et5ref,t4ref,p4ref,t7ref,et7ref,a8ref;
   static double fnref,fuelref,sfcref,airref,epref,etref,faref ;
                 // save design
   int ensav,absav,gamosav,ptfsav,arssav,arthsav,arxsav,flsav ;
   static double fhsav,t4sav,t7sav,p3sav,p3fsav,bysav,acsav ;
   static double a2sav,a4sav,a4psav,gamsav,et2sav,pr2sav,pr4sav ;
   static double et3sav,et4sav,et5sav,et7sav,et13sav,a8sav,a8mxsav ;
   static double a8rtsav,u0mxsav,u0sav,altsav ;
   static double trsav,artsav,arexsav ; 
                  // save materials info
   int wtfsav,minsav,mfnsav,mcmsav,mbrsav,mtrsav,mnlsav,mnrsav,ncsav,ntsav;
   static double wtsav, dinsav, tinsav, dfnsav, tfnsav, dcmsav, tcmsav;
   static double dbrsav, tbrsav, dtrsav, ttrsav, dnlsav, tnlsav, dnrsav, tnrsav;
                 // plot variables
   int lines,nord,nabs,param,npt,ntikx,ntiky ;
   int counter ;
   int ordkeep,abskeep ;
   static double begx,endx,begy,endy ;
   static double[] pltx = new double[26] ;
   static double[] plty = new double[26] ;
   static String labx,laby,labyu,labxu ;

   Solver solve ;
   Viewer view ;
   CardLayout layin,layout ;
   Con con ;
   In in ;
   Out out ;
   Image offscreenImg ;
   Graphics offsGg ;
//   Image[] anabimg = new Image[3] ;
//   Image[] anfnimg = new Image[3] ;
//   Image[] antjimg = new Image[3] ;
//   Image[] anrmimg = new Image[3] ;
//   Image partimg ;
   Image offImg1 ;
   Graphics off1Gg ;

   public void init() {
     int i;
     solve = new Solver() ;

/*
     for (i = 1; i <= 3; i++ ) {
       anabimg[3-i] = getImage(getCodeBase(),
               "ab" + i + ".gif") ;
       anfnimg[3-i] = getImage(getCodeBase(),
               "fn" + i + ".gif") ;
       antjimg[3-i] = getImage(getCodeBase(),
               "tj" + i + ".gif") ;
       anrmimg[3-i] = getImage(getCodeBase(),
               "rm" + i + ".gif") ;
     }
*/
     offscreenImg = createImage(this.size().width,
                      this.size().height) ;
     offsGg = offscreenImg.getGraphics() ;
     offImg1 = createImage(this.size().width,
                      this.size().height) ;
     off1Gg = offImg1.getGraphics() ;

     setLayout(new GridLayout(2,2,5,5)) ;

     solve.setDefaults () ;
 
     view   = new Viewer(this) ;
     con = new Con(this) ;
     in = new In(this) ;
     out = new Out(this) ;

     add(view) ;
     add(con) ;
     add(in) ;
     add(out) ;

     solve.comPute() ;
//     partimg = getImage(getCodeBase(),"photo.gif");
     layout.show(out, "first")  ;
     out.plot.loadPlot () ;
     out.plot.repaint() ;
     view.start() ;
  }
 
  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }

  public int filter0(double inumbr) {
     //  output only to .
     float number ;
     int intermed ;
    
     intermed = (int) (inumbr) ;
     number = (float) (intermed);
     return intermed ;
  }
 
  public float filter1(double inumbr) {
      //  output only to .1
      float number ;
      int intermed ;

      intermed = (int) (inumbr * 10.) ;
      number = (float) (intermed / 10. );
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

  public double getCp(double temp, int opt)  {
            // Utility to get cp as a function of temp 
      double number,a,b,c,d ;
                              /* BTU/R */
      a =  -4.4702130e-13;
      b =  -5.1286514e-10;
      c =   2.8323331e-05;
      d =  0.2245283;
      if(opt == 0) {
         number = .2399 ;
      }
      else {
         number = a*temp*temp*temp + b*temp*temp + c*temp +d ;
      }
      return(number) ;
  }

  public double getMach (int sub, double corair, double gamma) {
/* Utility to get the Mach number given the corrected airflow per area */
      double number,chokair;              /* iterate for mach number */
      double deriv,machn,macho,airo,airn;
      int iter ;

      chokair = getAir(1.0, gamma) ;
      if (corair > chokair) {
        number = 1.0 ;
        return (number) ;
      }
      else {
        airo = .25618 ;                 /* initial guess */
        if (sub == 1) macho = 1.0 ;   /* sonic */
        else {
           if (sub == 2) macho = 1.703 ; /* supersonic */
           else macho = .5;                /* subsonic */
           iter = 1 ;
           machn = macho - .2  ;
           while (Math.abs(corair - airo) > .0001 && iter < 20) {
              airn =  getAir(machn,gamma) ;
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

  public double getRayleighLoss(double mach1, double ttrat, double tlow) {
                                         /* analysis for rayleigh flow */
      double number ;
      double wc1,wc2,mgueso,mach2,g1,gm1,g2,gm2 ;
      double fac1,fac2,fac3,fac4;
   
      g1 = getGama(tlow,gamopt);
      gm1 = g1 - 1.0 ;
      wc1 = getAir(mach1,g1);
      g2 = getGama(tlow*ttrat,gamopt);
      gm2 = g2 - 1.0 ;
      number = .95 ;
                              /* iterate for mach downstream */
      mgueso = .4 ;                 /* initial guess */
      mach2 = .5 ;
      while (Math.abs(mach2 - mgueso) > .0001) {
          mgueso = mach2 ;
          fac1 = 1.0 + g1 * mach1 * mach1 ;
          fac2 = 1.0 + g2 * mach2 * mach2 ;
          fac3 = Math.pow((1.0 + .5 * gm1 * mach1 * mach1),(g1/gm1)) ;
          fac4 = Math.pow((1.0 + .5 * gm2 * mach2 * mach2),(g2/gm2)) ;
          number = fac1 * fac4 / fac2 / fac3 ;
          wc2 = wc1 * Math.sqrt(ttrat) / number ;
          mach2 = getMach(0,wc2,g2) ;
      }
      return(number) ;
  }
 
  public double getAir(double mach, double gamma) {
/* Utility to get the corrected airflow per area given the Mach number */
     double number,fac1,fac2;
     fac2 = (gamma+1.0)/(2.0*(gamma-1.0)) ;
     fac1 = Math.pow((1.0+.5*(gamma-1.0)*mach*mach),fac2);
     number =  .50161*Math.sqrt(gamma) * mach/ fac1 ;
 
     return(number) ;
  }

  class Solver {
 
     Solver () {
     }

     public void comPute() { 
 
        numeng = 1 ;
        fireflag = 0 ;

        getFreeStream ();
 
        getThermo() ;
 
        if (inflag == 0) getGeo (); /*determine engine size and geometry*/
        if (inflag == 1) {
           if (entype < 3)  a8 = a8d * Math.sqrt(trat[7]) / prat[7] ;
        }

        view.getDrawGeo() ;
   
        getPerform() ;

        out.box.loadOut() ;
        out.vars.loadOut() ;
        in.fillBox() ;
 
        if (plttyp >= 3 && plttyp <= 7)  {
            out.plot.loadPlot () ;
            out.plot.repaint() ;
        }
 
        view.repaint() ;
    
        if (inflag == 0) myDesign() ;
     }

     public void setDefaults() {
        int i ;
    
        move = 0;
        inptype = 0 ;
        siztype = 0 ;
        lunits = 0 ;
        lconv1 = 1.0 ; lconv2 = 1.0 ;fconv  = 1.0 ;mconv1 = 1.0 ;
        pconv  = 1.0 ; econv  = 1.0 ;aconv  = 1.0 ;bconv  = 1.0 ;
        mconv2 = 1.0 ; dconv  = 1.0 ;flconv = 1.0 ;econv2 = 1.0 ;
        tconv  = 1.0 ;  tref = 459.6;
        g0 = g0d = 32.2 ;

        counter = 0 ;
        showcom = 0 ;
        plttyp  = 3 ;
        pltkeep = 0 ;
        entype  = 0 ;
        inflag  = 0 ;
        varflag = 0 ;
        pt2flag = 0 ;
        wtflag  = 0 ;
        fireflag = 0 ;
        gama = 1.4 ;
        gamopt = 1 ;
        u0d = 0.0 ;
        altd = 0.0 ;
        throtl = 100. ;

        for (i=0; i<=19; ++i) {
             trat[i] = 1.0 ;
             tt[i]   = 518.6 ;
             prat[i] = 1.0 ;
             pt[i]   = 14.7 ;
             eta[i]  = 1.0 ;
        }
        tt[4] = tt4 = tt4d = 2500. ;
        tt[7] = tt7 = tt7d = 2500. ;
        prat[3] = p3p2d = 8.0 ;
        prat[13] = p3fp2d = 2.0 ;
        byprat = 1.0 ;
        abflag = 0 ;
    
        fueltype = 0 ;
        fhvd = fhv = 18600. ;
        a2d = a2 = acore = 2.0 ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        acap = .9*a2 ;
        a8rat = .35 ;
        a8 = .7 ;
        a8d = .40 ;
        arsched = 0 ;
        afan = 2.0 ;
        a4 = .418 ;
 
        athsched = 1 ;
        aexsched = 1 ;
        arthmn = 0.1;     arthmx = 1.5 ;
        arexmn = 1.0;     arexmx = 10.0 ;
        arthd = arth = .4 ;
        arexit = arexitd = 3.0 ;
 
        u0min  = 0.0 ;   u0max = 1500.;
        altmin = 0.0 ;   altmax = 60000. ;
        thrmin = 30;     thrmax = 100 ;
        etmin  = .5;     etmax  = 1.0 ;
        cprmin = 1.0;   cprmax = 50.0 ;
        bypmin = 0.0;   bypmax = 10.0 ;
        fprmin = 1.0;   fprmax = 2.0 ;
        t4min = 1000.0;   t4max = 3200.0 ;
        t7min = 1000.0;   t7max = 4000.0 ;
        a8min = 0.1;      a8max = 0.4 ;
        a2min = .001;       a2max = 50.;
        diamin = Math.sqrt(4.0*a2min/3.14159);
        diamax = Math.sqrt(4.0*a2max/3.14159);
        pmax = 20.0;  tmin = -100.0 + tref;  tmax = 100.0 + tref ;
        vmn1 = u0min ;     vmx1 = u0max ;
        vmn2 = altmin ;    vmx2 = altmax ;
        vmn3 = thrmin ;    vmx3 = thrmax ;
        vmn4 = arexmn ;    vmx4 = arexmx ;

        xtrans = 125.0 ;
        ytrans = 115.0 ;
        factor = 35. ;
        sldloc = 75 ;

        xtranp = 80.0 ;
        ytranp = 180.0 ;
        factp = 27. ;
        sldplt = 138 ;

        weight = 1000. ;
        minlt = 1; dinlt = 170.2 ;  tinlt = 900. ;
        mfan = 2;  dfan = 293.02 ; tfan = 1500. ;
        mcomp = 2; dcomp = 293.02 ; tcomp = 1500. ;
        mburner = 4 ; dburner  = 515.2 ; tburner = 2500. ;
        mturbin = 4 ; dturbin = 515.2 ; tturbin = 2500. ;
        mnozl = 3; dnozl = 515.2 ; tnozl = 2500. ;
        mnozr = 5; dnozr = 515.2 ; tnozr = 4500. ;
        ncflag = 0 ; ntflag = 0 ;
        return ;
     }

     public void myDesign() {

        ensav = entype ;
        absav = abflag ;
        flsav = fueltype ;
        fhsav = fhvd/flconv ;
        t4sav = tt4d/tconv ;
        t7sav = tt7d/tconv ;
        p3sav = p3p2d ;
        p3fsav = p3fp2d ;
        bysav = byprat;
        acsav = acore ;
        a2sav = a2d/aconv;
        a4sav = a4 ;
        a4psav = a4p;
        gamsav = gama ;
        gamosav = gamopt;
        ptfsav = pt2flag ;
        et2sav = eta[2] ;
        pr2sav = prat[2];
        pr4sav = prat[4];
        et3sav = eta[3];
        et4sav = eta[4];
        et5sav = eta[5];
        et7sav = eta[7];
        et13sav = eta[13];
        a8sav = a8d/aconv ; 
        a8mxsav = a8max/aconv ;
        a8rtsav = a8rat ;
   
        u0mxsav = u0max/lconv2 ;
        u0sav = u0d/lconv2 ;
        altsav = altd/lconv1 ;
        arssav = arsched ;
 
        wtfsav = wtflag; wtsav = weight;
        minsav = minlt; dinsav = dinlt;  tinsav = tinlt;
        mfnsav = mfan;  dfnsav = dfan;   tfnsav = tfan;
        mcmsav = mcomp; dcmsav = dcomp;  tcmsav = tcomp;
        mbrsav = mburner; dbrsav = dburner; tbrsav = tburner;
        mtrsav = mturbin; dtrsav = dturbin; ttrsav = tturbin;
        mnlsav = mnozl; dnlsav = dnozl; tnlsav = tnozl;
        mnrsav = mnozr; dnrsav = dnozr; tnrsav = tnozr;
        ncsav = ncflag; ntsav = ntflag;

        if (entype == 3) {
           arthsav = athsched ;
           arxsav = aexsched ;
           artsav = arthd ;
           arexsav = arexitd ;
        }
   
        return ;
     }

     public void loadMine() {

        entype = ensav ;
        abflag = absav ;
        fueltype = flsav ;
        fhvd = fhv = fhsav ;
        tt[4] = tt4 = tt4d = t4sav ;
        tt[7] = tt7 = tt7d = t7sav ;
        prat[3] = p3p2d = p3sav ;
        prat[13] = p3fp2d = p3fsav ;
        byprat = bysav;
        acore = acsav ;
        afan = acore * (1.0 + byprat) ;
        a2d = a2 = a2sav ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        a4 = a4sav ;
        a4p = a4psav ;
        acap = .9*a2 ;
        gama = gamsav ;
        gamopt = gamosav ;
        pt2flag = ptfsav ;
        eta[2] = et2sav ;
        prat[2] = pr2sav ;
        prat[4] = pr4sav ;
        eta[3] = et3sav ;
        eta[4] = et4sav ;
        eta[5] = et5sav ;
        eta[7] = et7sav ;
        eta[13] = et13sav ;
        a8d = a8sav ; 
        a8max = a8mxsav ;
        a8rat = a8rtsav ;
   
        u0max = u0mxsav ;
        u0d = u0sav ;
        altd = altsav ;
        arsched = arssav ;
   
        wtflag = wtfsav; weight = wtsav;
        minlt = minsav; dinlt = dinsav;  tinlt = tinsav;
        mfan = mfnsav;  dfan = dfnsav;   tfan = tfnsav;
        mcomp = mcmsav; dcomp = dcmsav; tcomp = tcmsav;
        mburner = mbrsav; dburner = dbrsav; tburner = tbrsav;
        mturbin = mtrsav; dturbin = dtrsav; tturbin = ttrsav;
        mnozl = mnlsav; dnozl = dnlsav; tnozl = tnlsav;
        mnozr = mnrsav; dnozr = dnrsav; tnozr = tnrsav;
        ncflag = ncsav; ntflag = ntsav;

        if (entype == 3) {
           athsched = arthsav  ;
           aexsched = arxsav ;
           arthd = artsav ;
           arexitd = arexsav  ;
        }
   
        con.setPanl() ;
        return ;
     }

     public void loadCF6() {

        entype = 2 ;
        abflag = 0 ;
        fueltype = 0;
        fhvd = fhv = 18600. ;
        tt[4] = tt4 = tt4d = 2500. ;
        tt[7] = tt7 = tt7d = 2500. ;
        prat[3] = p3p2d = 21.86 ;
        prat[13] = p3fp2d = 1.745 ;
        byprat = 3.3 ;
        acore = 6.965 ;
        afan = acore * (1.0 + byprat) ;
        a2d = a2 = afan ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        a4 = .290 ;
        a4p = 1.131 ;
        acap = .9*a2 ;
        gama = 1.4 ;
        gamopt = 1 ;
        pt2flag = 0 ;
        eta[2] = 1.0 ;
        prat[2] = 1.0 ;
        prat[4] = 1.0 ;
        eta[3] = .959 ;
        eta[4] = .984 ;
        eta[5] = .982 ;
        eta[7] = 1.0 ;
        eta[13] = 1.0 ;
        a8d = 2.436 ; 
        a8max = .35 ;
        a8rat = .35 ;
   
        u0max = 1500. ;
        u0d = 0.0 ;
        altd = 0.0 ;
        arsched = 0 ;
   
        wtflag = 0; weight = 8229.;
        minlt = 1; dinlt = 170.;  tinlt = 900.;
        mfan = 2;  dfan = 293.;   tfan = 1500.;
        mcomp = 0; dcomp = 293.; tcomp = 1600.;
        mburner = 4; dburner = 515.; tburner = 2500.;
        mturbin = 4; dturbin = 515.; tturbin = 2500.;
        mnozl = 3; dnozl = 515.; tnozl = 2500.;
        ncflag = 0; ntflag = 0;

        con.setPanl() ;
        return ;
     }

     public void loadJ85() {
   
        entype = 0 ;
        abflag = 0 ;
        fueltype = 0;
        fhvd = fhv = 18600. ;
        tt[4] = tt4 = tt4d = 2260. ;
        tt[7] = tt7 = tt7d = 4000. ;
        prat[3] = p3p2d = 8.3 ;
        prat[13] = p3fp2d = 1.0 ;
        byprat = 0.0 ;
        a2d = a2 = acore = 1.753 ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        afan = acore * (1.0 + byprat) ;
        a4 = .323 ;
        a4p = .818 ;
        acap = .9*a2 ;
        gama = 1.4 ;
        gamopt = 1 ;
        pt2flag = 0 ;
        eta[2] = 1.0 ;
        prat[2] = 1.0 ;
        prat[4] = .85 ;
        eta[3] = .822 ;
        eta[4] = .982 ;
        eta[5] = .882;
        eta[7] = .97 ;
        eta[13] = 1.0 ;
        a8d = .818 ; 
        a8max = .467 ;
        a8rat = .467 ;

        u0max = 1500. ;
        u0d = 0.0 ;
        altd = 0.0 ;
        arsched = 1 ;

        wtflag = 0; weight = 561.;
        minlt = 1; dinlt = 170.;  tinlt = 900.;
        mfan = 2;  dfan = 293.;   tfan = 1500.;
        mcomp = 2; dcomp = 293.; tcomp = 1500.;
        mburner = 4; dburner = 515.; tburner = 2500.;
        mturbin = 4; dturbin = 515.; tturbin = 2500.;
        mnozl = 5; dnozl = 600.; tnozl = 4100.;
        ncflag = 0; ntflag = 0;

        con.setPanl() ;
        return ;
     }

     public void loadF100() {
   
        entype = 1 ;
        abflag = 1 ;
        fueltype = 0;
        fhvd = fhv = 18600. ;
        tt[4] = tt4 = tt4d = 2499. ;
        tt[7] = tt7 = tt7d = 3905. ;
        prat[3] = p3p2d = 20.04 ;
        prat[13] = p3fp2d = 1.745 ;
        byprat = 0.0 ;
        a2d = a2 = acore = 6.00 ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        afan = acore * (1.0 + byprat) ;
        a4 = .472 ;
        a4p = 1.524 ;
        acap = .9*a2 ;
        gama = 1.4 ;
        gamopt = 1 ;
        pt2flag = 0 ;
        eta[2] = 1.0 ;
        prat[2] = 1.0 ;
        prat[4] = 1.0 ;
        eta[3] = .959 ;
        eta[4] = .984 ;
        eta[5] = .982 ;
        eta[7] = .92 ;
        eta[13] = 1.0 ;
        a8d = 1.524 ; 
        a8max = .335 ;
        a8rat = .335 ;
   
        u0max = 1500. ;
        u0d = 0.0 ;
        altd = 0.0 ;
        arsched = 0 ;

        wtflag = 0; weight = 3875.;
        minlt = 1; dinlt = 170.;  tinlt = 900.;
        mfan = 2;  dfan = 293.;   tfan = 1500.;
        mcomp = 2; dcomp = 293.; tcomp = 1500.;
        mburner = 4; dburner = 515.; tburner = 2500.;
        mturbin = 4; dturbin = 515.; tturbin = 2500.;
        mnozl = 5; dnozl = 400.2 ; tnozl = 4100. ;
        ncflag = 0; ntflag = 0;

        con.setPanl() ;
        return ;
     }

     public void loadRamj() {
   
        entype = 3 ;
        athsched = 1  ;
        aexsched = 1 ;
        arthd = .4 ;
        arexitd = 3.0 ;
        abflag = 0 ;
        fueltype = 0;
        fhvd = fhv = 18600. ;
        tt[4] = tt4 = tt4d = 4000. ;
        t4max = 4500. ;
        tt[7] = tt7 = tt7d = 4000. ;
        prat[3] = p3p2d = 1.0 ;
        prat[13] = p3fp2d = 1.0 ;
        byprat = 0.0 ;
        a2d = a2 = acore = 1.753 ;
        diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
        afan = acore * (1.0 + byprat) ;
        a4 = .323 ;
        a4p = .818 ;
        acap = .9*a2 ;
        gama = 1.4 ;
        gamopt = 1 ;
        pt2flag = 0 ;
        eta[2] = 1.0 ;
        prat[2] = 1.0 ;
        prat[4] = 1.0 ;
        eta[3] = 1.0 ;
        eta[4] = .982 ;
        eta[5] = 1.0 ;
        eta[7] = 1.0 ;
        eta[13] = 1.0 ;
        a8 = a8d = 2.00 ; 
        a8max = 15. ;
        a8rat = 4.0 ;
        a7 = .50 ;

        u0max = 4500. ;
        u0d = 2200.0 ;
        altd = 10000.0 ;
        arsched = 0 ;

        wtflag = 0; weight = 976.;
        minlt = 2; dinlt = 293.;  tinlt = 1500.;
        mfan = 2;  dfan = 293.;   tfan = 1500.;
        mcomp = 2; dcomp = 293.; tcomp = 1500.;
        mburner = 7; dburner = 515.; tburner = 4500.;
        mturbin = 4; dturbin = 515.; tturbin = 2500.;
        mnozr = 5; dnozr = 515.2 ; tnozr = 4500. ;
        ncflag = 0; ntflag = 0;

        con.setPanl() ;
        return ;
     }

     public void getFreeStream() {
       rgas = 1718. ;                /* ft2/sec2 R */
       if (inptype >= 2) {
         ps0 = ps0 * 144. ;
       }
       if (inptype <= 1) {            /* input altitude */
         alt = altd / lconv1  ;
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
       }
       a0 = Math.sqrt(gama*rgas*ts0) ;             /* speed of sound ft/sec */
       if (inptype == 0 || inptype == 2) {           /* input speed  */
          u0 = u0d /lconv2 *5280./3600. ;           /* airspeed ft/sec */
          fsmach = u0/a0 ;
          q0 = gama / 2.0*fsmach*fsmach*ps0 ;
       }
       if (inptype == 1 || inptype == 3) {            /* input mach */
          u0 = fsmach * a0 ;
          u0d = u0 * lconv2 / 5280.* 3600. ;      /* airspeed ft/sec */
          q0 = gama / 2.0*fsmach*fsmach*ps0 ;
       }
       if (u0 > .0001) rho0 = q0 /(u0*u0) ;
       else rho0 = 1.0 ;

       tt[0] = ts0*(1.0 + .5 * (gama -1.0) * fsmach * fsmach) ;
       pt[0] = ps0 * Math.pow(tt[0]/ts0,gama/(gama-1.0)) ;
       ps0 = ps0 / 144. ;
       pt[0] = pt[0] / 144. ;
       cpair = getCp(tt[0],gamopt);              /*BTU/lbm R */
       tsout = ts0 ;
       psout = ps0 ;
   
       return ;
     }
 
     public void getThermo() {
       double dela,t5t4n,deriv,delan,m5;
       double delhc,delhht,delhf,delhlt;
       double deltc,deltht,deltf,deltlt;
       int itcount,index ;
       float fl1 ;
       int i1 ;
                                         /*   inlet recovery  */
       if (pt2flag == 0) {                    /*     mil spec      */
          if (fsmach > 1.0 ) {          /* supersonic */
             prat[2] = 1.0 - .075*Math.pow(fsmach - 1.0, 1.35) ;
          }
          else {
             prat[2] = 1.0 ;
          }
          eta[2] = prat[2] ;
          fl1 = filter3(prat[2]) ;
          in.inlet.left.f1.setText(String.valueOf(fl1)) ;
          i1 = (int) (((prat[2] - etmin)/(etmax-etmin))*1000.) ;
          in.inlet.right.s1.setValue(i1) ;
       }
       else {                       /* enter value */
          prat[2] = eta[2] ;          
       }
                               /* protection for overwriting input */
       if (eta[3] < .5) eta[3] = .5 ;
       if (eta[5] < .5) eta[5] = .5 ;
       trat[7] = 1.0 ;
       prat[7] = 1.0 ;
       tt[2] = tt[1] = tt[0] ;
       pt[1] = pt[0] ;
       gam[2] = getGama(tt[2],gamopt) ;
       cp[2]  = getCp(tt[2],gamopt);
       pt[2] = pt[1] * prat[2] ;
        /* design - p3p2 specified - tt4 specified */
       if(inflag == 0) {
   
        if (entype <= 1) {              /* turbojet */
          prat[3] = p3p2d ;                      /* core compressor */
          if (prat[3] < .5) prat[3] = .5 ;
          delhc = (cp[2]*tt[2]/eta[3])*
                (Math.pow(prat[3],(gam[2]-1.0)/gam[2])-1.0) ;
          deltc = delhc / cp[2] ;
          pt[3] = pt[2] * prat[3] ;
          tt[3] = tt[2] + deltc ;
          trat[3] = tt[3]/tt[2] ;
          gam[3] = getGama(tt[3],gamopt) ;
          cp[3]  = getCp(tt[3],gamopt);
          tt[4] = tt4 * throtl/100.0 ;
          gam[4] = getGama(tt[4],gamopt) ;
          cp[4]  = getCp(tt[4],gamopt);
          trat[4] = tt[4] / tt[3] ;
          pt[4] = pt[3] * prat[4] ;
          delhht = delhc ;
          deltht = delhht / cp[4] ;
          tt[5] = tt[4] - deltht ;
          gam[5] = getGama(tt[5],gamopt) ;
          cp[5]  = getCp(tt[5],gamopt);
          trat[5] = tt[5]/tt[4] ;
          prat[5] = Math.pow((1.0-delhht/cp[4]/tt[4]/eta[5]),
                   (gam[4]/(gam[4]-1.0))) ;
          pt[5] = pt[4] * prat[5] ;
                                        /* fan conditions */
          prat[13] = 1.0 ;
          trat[13] = 1.0 ;
          tt[13]   = tt[2] ;
          pt[13]   = pt[2] ;
          gam[13]  = gam[2] ;
          cp[13]   = cp[2] ;
          prat[15] = 1.0 ;
          pt[15]   = pt[5] ;
          trat[15] = 1.0 ;
          tt[15]   = tt[5] ;
          gam[15]  = gam[5] ;
          cp[15]   = cp[5] ;
       }
   
       if(entype == 2) {                         /* turbofan */
          prat[13] = p3fp2d ;
          if (prat[13] < .5) prat[13] = .5 ;
          delhf = (cp[2]*tt[2]/eta[13])*
                (Math.pow(prat[13],(gam[2]-1.0)/gam[2])-1.0) ;
          deltf = delhf / cp[2] ;
          tt[13] = tt[2] + deltf ;
          pt[13] = pt[2] * prat[13] ;
          trat[13] = tt[13]/tt[2] ;
          gam[13] = getGama(tt[13],gamopt) ;
          cp[13]  = getCp(tt[13],gamopt);
          prat[3] = p3p2d ;                      /* core compressor */
          if (prat[3] < .5) prat[3] = .5 ;
          delhc = (cp[13]*tt[13]/eta[3])*
                (Math.pow(prat[3],(gam[13]-1.0)/gam[13])-1.0) ;
          deltc = delhc / cp[13] ;
          tt[3] = tt[13] + deltc ;
          pt[3] = pt[13] * prat[3] ;
          trat[3] = tt[3]/tt[13] ;
          gam[3] = getGama(tt[3],gamopt) ;
          cp[3]  = getCp(tt[3],gamopt);
          tt[4] = tt4 * throtl/100.0 ;
          pt[4] = pt[3] * prat[4] ;
          gam[4] = getGama(tt[4],gamopt) ;
          cp[4]  = getCp(tt[4],gamopt);
          trat[4] = tt[4]/tt[3] ;
          delhht = delhc ;
          deltht = delhht / cp[4] ;
          tt[5] = tt[4] - deltht ;
          gam[5] = getGama(tt[5],gamopt) ;
          cp[5]  = getCp(tt[5],gamopt);
          trat[5] = tt[5]/tt[4] ;
          prat[5] = Math.pow((1.0-delhht/cp[4]/tt[4]/eta[5]),
                      (gam[4]/(gam[4]-1.0))) ;
          pt[5] = pt[4] * prat[5] ;
          delhlt = (1.0 + byprat) * delhf ;
          deltlt = delhlt / cp[5] ;
          tt[15] = tt[5] - deltlt ;
          gam[15] = getGama(tt[15],gamopt) ;
          cp[15]  = getCp(tt[15],gamopt);
          trat[15] = tt[15]/tt[5] ;
          prat[15] = Math.pow((1.0-delhlt/cp[5]/tt[5]/eta[5]),
                      (gam[5]/(gam[5]-1.0))) ;
          pt[15] = pt[5] * prat[15] ;
        }

        if (entype == 3) {              /* ramjet */
          prat[3] = 1.0 ;                      
          pt[3] = pt[2] * prat[3] ;
          tt[3] = tt[2] ;
          trat[3] = 1.0 ;
          gam[3] = getGama(tt[3],gamopt) ;
          cp[3]  = getCp(tt[3],gamopt);
          tt[4] = tt4 * throtl/100.0 ;
          gam[4] = getGama(tt[4],gamopt) ;
          cp[4]  = getCp(tt[4],gamopt);
          trat[4] = tt[4] / tt[3] ;
          pt[4] = pt[3] * prat[4] ;
          tt[5] = tt[4] ;
          gam[5] = getGama(tt[5],gamopt) ;
          cp[5]  = getCp(tt[5],gamopt);
          trat[5] = 1.0 ;
          prat[5] = 1.0 ;
          pt[5] = pt[4] ;
                                        /* fan conditions */
          prat[13] = 1.0 ;
          trat[13] = 1.0 ;
          tt[13]   = tt[2] ;
          pt[13]   = pt[2] ;
          gam[13]  = gam[2] ;
          cp[13]   = cp[2] ;
          prat[15] = 1.0 ;
          pt[15]   = pt[5] ;
          trat[15] = 1.0 ;
          tt[15]   = tt[5] ;
          gam[15]  = gam[5] ;
          cp[15]   = cp[5] ;
        }

        tt[7] = tt7 ;
      }
             /* analysis -assume flow choked at both turbine entrances */
                                  /* and nozzle throat ... then*/
      else {
        tt[4] = tt4 * throtl/100.0 ;
        gam[4] = getGama(tt[4],gamopt) ;
        cp[4]  = getCp(tt[4],gamopt);
        if (a4 < .02) a4 = .02 ;
   
        if (entype <= 1) {              /* turbojet */
           dela = .2 ;                           /* iterate to get t5t4 */
           trat[5] = 1.0 ;
           t5t4n = .5 ;
           itcount = 0 ;
           while(Math.abs(dela) > .001 && itcount < 20) {
              ++ itcount ;
              delan = a8d/a4 - Math.sqrt(t5t4n)*
                   Math.pow((1.0-(1.0/eta[5])*(1.0-t5t4n)),
                          -gam[4]/(gam[4]-1.0)) ;
              deriv = (delan-dela)/(t5t4n-trat[5]) ;
              dela = delan ;
              trat[5] = t5t4n ;
              t5t4n = trat[5] - dela/deriv ;
           }
           tt[5] = tt[4] * trat[5] ;
           gam[5] = getGama(tt[5],gamopt) ;
           cp[5]  = getCp(tt[5],gamopt);
           deltht = tt[5]- tt[4] ;
           delhht  = cp[4] * deltht ;
           prat[5] = Math.pow((1.0-(1.0/eta[5])*(1.0-trat[5])),
                        gam[4]/(gam[4]-1.0)) ;
           delhc = delhht  ;           /* compressor work */
           deltc = -delhc / cp[2] ;
           tt[3] = tt[2] + deltc ;
           gam[3] = getGama(tt[3],gamopt) ;
           cp[3]  = getCp(tt[3],gamopt);
           trat[3] = tt[3]/tt[2] ;
           prat[3] = Math.pow((1.0+eta[3]*(trat[3]-1.0)),
                        gam[2]/(gam[2]-1.0)) ;
           trat[4] = tt[4]/tt[3] ;
           pt[3]   = pt[2] * prat[3] ;
           pt[4]   = pt[3] * prat[4] ;
           pt[5]   = pt[4] * prat[5] ;
                                        /* fan conditions */
           prat[13] = 1.0 ;
           trat[13] = 1.0 ;
           tt[13]   = tt[2] ;
           pt[13]   = pt[2] ;
           gam[13]  = gam[2] ;
           cp[13]   = cp[2] ;
           prat[15] = 1.0 ;
           pt[15]   = pt[5] ;
           trat[15] = 1.0 ;
           tt[15]   = tt[5] ;
           gam[15]  = gam[5] ;
           cp[15]   = cp[5] ;
        }

        if(entype == 2) {                        /*  turbofan */
           dela = .2 ;                           /* iterate to get t5t4 */
           trat[5] = 1.0 ;
           t5t4n = .5 ;
           itcount = 0 ;
           while(Math.abs(dela) > .001 && itcount < 20) {
              ++ itcount ;
              delan = a4p/a4 - Math.sqrt(t5t4n)*
                      Math.pow((1.0-(1.0/eta[5])*(1.0-t5t4n)),
                          -gam[4]/(gam[4]-1.0)) ;
              deriv = (delan-dela)/(t5t4n-trat[5]) ;
              dela = delan ;
              trat[5] = t5t4n ;
              t5t4n = trat[5] - dela/deriv ;
           }
           tt[5] = tt[4] * trat[5] ;
           gam[5] = getGama(tt[5],gamopt) ;
           cp[5]  = getCp(tt[5],gamopt);
           deltht = tt[5] - tt[4] ;
           delhht  = cp[4] * deltht ;
           prat[5] = Math.pow((1.0-(1.0/eta[5])*(1.0-trat[5])),
                     gam[4]/(gam[4]-1.0)) ;
                                       /* iterate to get t15t14 */
           dela = .2 ;
           trat[15] = 1.0 ;
           t5t4n = .5 ;
           itcount = 0 ;
           while(Math.abs(dela) > .001 && itcount < 20) {
               ++ itcount ;
               delan = a8d/a4p - Math.sqrt(t5t4n)*
                        Math.pow((1.0-(1.0/eta[5])*(1.0-t5t4n)),
                          -gam[5]/(gam[5]-1.0)) ;
               deriv = (delan-dela)/(t5t4n-trat[15]) ;
               dela = delan ;
               trat[15] = t5t4n ;
               t5t4n = trat[15] - dela/deriv ;
           }
           tt[15] = tt[5] * trat[15] ;
           gam[15] = getGama(tt[15],gamopt) ;
           cp[15]  = getCp(tt[15],gamopt);
           deltlt = tt[15] - tt[5] ;
           delhlt = cp[5] * deltlt ;
           prat[15] = Math.pow((1.0-(1.0/eta[5])*(1.0-trat[15])),
                       gam[5]/(gam[5]-1.0)) ;
           byprat =  afan / acore - 1.0  ;
           delhf = delhlt / (1.0 + byprat) ;              /* fan work */
           deltf =  - delhf / cp[2] ;
           tt[13] = tt[2] + deltf ;
           gam[13] = getGama(tt[13],gamopt) ;
           cp[13]  = getCp(tt[13],gamopt);
           trat[13] = tt[13]/tt[2] ;
           prat[13] = Math.pow((1.0+eta[13]*(trat[13]-1.0)),
                     gam[2]/(gam[2]-1.0)) ;
           delhc = delhht  ;                         /* compressor work */
           deltc = -delhc / cp[13] ;
           tt[3] = tt[13] + deltc ;
           gam[3] = getGama(tt[3],gamopt) ;
           cp[3]  = getCp(tt[3],gamopt);
           trat[3] = tt[3]/tt[13] ;
           prat[3] = Math.pow((1.0+eta[3]*(trat[3]-1.0)),
                        gam[13]/(gam[13]-1.0)) ;
           trat[4] = tt[4]/tt[3] ;
           pt[13]  = pt[2]  * prat[13] ;
           pt[3]   = pt[13] * prat[3] ;
           pt[4]   = pt[3]  * prat[4] ;
           pt[5]   = pt[4]  * prat[5] ;
           pt[15]  = pt[5]  * prat[15] ;
         }

         if (entype == 3) {              /* ramjet */
           prat[3] = 1.0 ;                      
           pt[3] = pt[2] * prat[3] ;
           tt[3] = tt[2] ;
           trat[3] = 1.0 ;
           gam[3] = getGama(tt[3],gamopt) ;
           cp[3]  = getCp(tt[3],gamopt);
           tt[4] = tt4 * throtl/100.0  ;
           trat[4] = tt[4] / tt[3] ;
           pt[4] = pt[3] * prat[4] ;
           tt[5] = tt[4] ;
           gam[5] = getGama(tt[5],gamopt) ;
           cp[5]  = getCp(tt[5],gamopt);
           trat[5] = 1.0 ;
           prat[5] = 1.0 ;
           pt[5] = pt[4] ;
                                         /* fan conditions */
           prat[13] = 1.0 ;
           trat[13] = 1.0 ;
           tt[13]   = tt[2] ;
           pt[13]   = pt[2] ;
           gam[13]  = gam[2] ;
           cp[13]   = cp[2] ;
           prat[15] = 1.0 ;
           pt[15]   = pt[5] ;
           trat[15] = 1.0 ;
           tt[15]   = tt[5] ;
           gam[15]  = gam[5] ;
           cp[15]   = cp[5] ;
         }

         if (abflag == 1) tt[7] = tt7 ;
       }
   
       prat[6] = 1.0;
       pt[6] = pt[15];
       trat[6] = 1.0 ;
       tt[6] = tt[15] ;
       gam[6] = getGama(tt[6],gamopt) ;
       cp[6]  = getCp(tt[6],gamopt);
       if (abflag > 0) {                   /* afterburner */
             trat[7] = tt[7] / tt[6] ;
             m5 = getMach(0,getAir(1.0,gam[5])*a4/acore,gam[5]) ;
             prat[7] = getRayleighLoss(m5,trat[7],tt[6]) ;
       }
       tt[7] = tt[6] * trat[7] ;
       pt[7] = pt[6] * prat[7] ;
       gam[7] = getGama(tt[7],gamopt) ;
       cp[7]  = getCp(tt[7],gamopt);
                 /* engine press ratio EPR*/
       epr = prat[7]*prat[15]*prat[5]*prat[4]*prat[3]*prat[13];
              /* engine temp ratio ETR */
       etr = trat[7]*trat[15]*trat[5]*trat[4]*trat[3]*trat[13];
       return;
     }
   
     public void getPerform ()  {       /* determine engine performance */
       double fac1,game,cpe,cp3,rg,p8p5,rg1 ;
       int index ;
   
       rg1 = 53.3 ;
       rg = cpair * (gama-1.0)/gama ;
       cp3 = getCp(tt[3],gamopt);                  /*BTU/lbm R */
       g0 = 32.2 ;
       ues = 0.0 ;
       game = getGama(tt[5],gamopt) ;
       fac1 = (game - 1.0)/game ;
       cpe = getCp(tt[5],gamopt) ;
       if (eta[7] < .8) eta[7] = .8 ;    /* protection during overwriting */
       if (eta[4] < .8) eta[4] = .8 ;

       /*  specific net thrust  - thrust / (g0*airflow) -   lbf/lbm/sec  */
// turbine engine core
       if (entype <=2) {    
                            /* airflow determined at choked nozzle exit */
            pt[8] = epr*prat[2]*pt[0] ;
            eair = getAir(1.0,game) * 144.*a8 * pt[8]/14.7 /
                    Math.sqrt(etr*tt[0]/518.)   ;
            m2 = getMach(0,eair*Math.sqrt(tt[0]/518.)/
                    (prat[2]*pt[0]/14.7*acore*144.),gama) ;
            npr = pt[8]/ps0;
            uexit = Math.sqrt(2.0*rgas/fac1*etr*tt[0]*eta[7]*
                    (1.0-Math.pow(1.0/npr,fac1)));
            if (npr <= 1.893) pexit = ps0 ;
            else pexit = .52828 * pt[8] ;
            fgros = (uexit + (pexit - ps0) * 144. *  a8 /eair) / g0 ;
       }

// turbo fan -- added terms for fan flow
       if (entype == 2) {
            fac1 = (gama - 1.0)/gama ;
            snpr = pt[13]/ps0;
            ues = Math.sqrt(2.0*rgas/fac1*tt[13]*eta[7]*
                     (1.0-Math.pow(1.0/snpr,fac1)));
            m2 = getMach(0,eair*(1.0+byprat)*Math.sqrt(tt[0]/518.)/
                     (prat[2]*pt[0]/14.7*afan*144.),gama) ;
            if (snpr <= 1.893) pfexit = ps0 ;
            else pfexit = .52828 * pt[13] ;
            fgros = fgros + (byprat*ues + (pfexit - ps0)*144. * byprat*acore / eair)/g0 ;
       }

// ramjets
       if (entype == 3) {
                           /* airflow determined at nozzle throat */
            eair = getAir(1.0,game)*144.0*a2*arthd * epr*prat[2]*pt[0]/14.7 /
                    Math.sqrt(etr*tt[0]/518.)   ;
            m2 = getMach(0,eair*Math.sqrt(tt[0]/518.)/
                    (prat[2]*pt[0]/14.7*acore*144.),gama) ;
            mexit = getMach(2,(getAir(1.0,game) / arexitd),game) ;
            uexit = mexit * Math.sqrt(game * rgas * etr * tt[0] *eta[7] /
                     (1.0 + .5 *(game-1.0) * mexit *mexit)) ;
            pexit = Math.pow((1.0 + .5 *(game-1.0) * mexit *mexit),(-game/(game-1.0)))
                     * epr * prat[2] * pt[0] ;
            fgros = (uexit + (pexit - ps0)*arexitd*arthd*a2/eair/144.) / g0 ;
       }

// ram drag
       dram = u0 / g0 ;
       if (entype == 2) dram = dram + u0 * byprat / g0 ;

// mass flow ratio 
       if (fsmach > .01) mfr = getAir(m2,gama)*prat[2]/getAir(fsmach,gama) ;
       else mfr = 5.;

// net thrust
       fnet = fgros - dram;
       if (entype == 3 && fsmach < .3) {
         fnet = 0.0 ;
         fgros = 0.0 ;
       }

// thrust in pounds
       fnlb = fnet * eair ;
       fglb = fgros * eair ;
       drlb = dram * eair ;
   
//fuel-air ratio and sfc
       fa = (trat[4]-1.0)/(eta[4]*fhv/(cp3*tt[3])-trat[4]) +
         (trat[7]-1.0)/(fhv/(cpe*tt[15])-trat[7]) ;
       if ( fnet > 0.0)  {
           sfc = 3600. * fa /fnet ;
           flflo = sfc*fnlb ;
           isp = (fnlb/flflo) * 3600. ;
       }
       else {
           fnlb = 0.0 ;
           flflo = 0.0 ;
           sfc = 0.0 ;
           isp = 0.0 ;
       }

 // plot output
       tt[8] = tt[7] ;
       t8 = etr * tt[0] - uexit * uexit /(2.0*rgas*game/(game-1.0)) ;
       trat[8] = tt[8]/tt[7] ;
       p8p5 = ps0 / (epr * prat[2] *pt[0]) ;
       cp[8] = getCp(tt[8],gamopt) ;
       pt[8] = pt[7] ;
       prat[8] = pt[8]/pt[7] ;
        /* thermal efficiency */
       if (entype == 2) {
           eteng = (a0*a0*((1.0+fa)*(uexit*uexit/(a0*a0))
           + byprat*(ues*ues/(a0*a0))
           - (1.0+byprat)*fsmach*fsmach))/(2.0*g0*fa*fhv*778.16)    ;
       }
       else {
           eteng = (a0*a0*((1.0+fa)*(uexit*uexit/(a0*a0))
           - fsmach*fsmach))/(2.0*g0*fa*fhv*778.16)    ;
       }

       s[0] = s[1] = .2 ;
       v[0] = v[1] = rg1*ts0/(ps0*144.) ;
       for (index=2; index <=7 ; ++index ) {     /* compute entropy */
         s[index] = s[index-1] + cpair*Math.log(trat[index])
                               - rg*Math.log(prat[index])  ;
         v[index] = rg1*tt[index]/(pt[index]*144.) ;
       }
       s[13] = s[2] + cpair*Math.log(trat[13])-rg*Math.log(prat[13]);
       v[13] = rg1*tt[13]/(pt[13]*144.) ;
       s[15] = s[5] + cpair*Math.log(trat[15])-rg*Math.log(prat[15]);
       v[15] = rg1*tt[15]/(pt[15]*144.) ;
       s[8]  = s[7] + cpair*Math.log(t8/(etr*tt[0]))- rg*Math.log(p8p5)  ;
       v[8]  = rg1*t8/(ps0*144.) ;
       cp[0] = getCp(tt[0],gamopt) ;

       fntot   = numeng * fnlb ;
       fuelrat = numeng * flflo ;
    // weight  calculation
       if (wtflag == 0) {
          if (entype == 0) {
            weight = .132 * Math.sqrt(acore*acore*acore) * 
             (dcomp * lcomp + dburner * lburn + dturbin * lturb + dnozl * lnoz);
          }
          if (entype == 1) {
            weight = .100 * Math.sqrt(acore*acore*acore) * 
             (dcomp * lcomp + dburner * lburn + dturbin * lturb + dnozl * lnoz);
          }
          if (entype == 2) {
            weight = .0932 * acore * ((1.0 + byprat) * dfan * 4.0 + dcomp * (ncomp -3) +
                      dburner + dturbin * nturb + dnozl * 2.0) * Math.sqrt(acore / 6.965) ;
          }
          if (entype == 3) {
            weight = .1242 * acore * (dburner + dnozr *6. + dinlt * 3.) * Math.sqrt(acore / 1.753) ;
          }
       }
     // check for temp limits
       out.vars.to1.setForeground(Color.yellow) ;
       out.vars.to2.setForeground(Color.yellow) ;
       out.vars.to3.setForeground(Color.yellow) ;
       out.vars.to4.setForeground(Color.yellow) ;
       out.vars.to5.setForeground(Color.yellow) ;
       out.vars.to6.setForeground(Color.yellow) ;
       out.vars.to7.setForeground(Color.yellow) ;
       if (entype < 3) {
          if (tt[2] > tinlt) {
             fireflag =1 ;      
             out.vars.to1.setForeground(Color.red) ;
             out.vars.to2.setForeground(Color.red) ;
          }
          if (tt[13] > tfan) {
             fireflag =1 ;      
             out.vars.to2.setForeground(Color.red) ;
          }
          if (tt[3] > tcomp) {
             fireflag =1 ;      
             out.vars.to3.setForeground(Color.red) ;
          }
          if (tt[4] > tburner) {
             fireflag =1 ;      
             out.vars.to4.setForeground(Color.red) ;
          }
          if (tt[5] > tturbin) {
             fireflag =1 ;      
             out.vars.to5.setForeground(Color.red) ;
          }
          if (tt[7] > tnozl) {
             fireflag =1 ;      
             out.vars.to6.setForeground(Color.red) ;
             out.vars.to7.setForeground(Color.red) ;
          }
       }
       if (entype == 3) {
          if (tt[3] > tinlt) {
             fireflag =1 ;      
             out.vars.to1.setForeground(Color.red) ;
             out.vars.to2.setForeground(Color.red) ;
             out.vars.to3.setForeground(Color.red) ;
          }
          if (tt[4] > tburner) {
             fireflag =1 ;    
             out.vars.to4.setForeground(Color.red) ;
          }
          if (tt[7] > tnozr) {
             fireflag =1 ;      
             out.vars.to5.setForeground(Color.red) ;
             out.vars.to6.setForeground(Color.red) ;
             out.vars.to7.setForeground(Color.red) ;
          }
       }
       if (fireflag == 1) view.start() ;
     }
 
     public void getGeo () {
                            /* determine geometric variables */
        double game ;
        float fl1 ;
        int i1 ;

        if (entype <= 2) {          // turbine engines
          if (afan < acore) afan = acore ;
          a8max = .75 * Math.sqrt(etr) / epr ; /* limits compressor face  */
                                               /*  mach number  to < .5   */
          if (a8max > 1.0) a8max = 1.0 ;
          if (a8rat > a8max) {
           a8rat = a8max ;
           if (lunits <= 1) {
               fl1 = filter3(a8rat) ;
               in.nozl.left.f3.setText(String.valueOf(fl1)) ;
               i1 = (int) (((a8rat - a8min)/(a8max-a8min))*1000.) ;
               in.nozl.right.s3.setValue(i1) ;
           }
           if (lunits == 2) {
               fl1 = filter3(100.*(a8rat - a8ref)/a8ref) ;
               in.nozl.left.f3.setText(String.valueOf(fl1)) ;
               i1 = (int) ((((100.*(a8rat - a8ref)/a8ref) +10.0)/20.0)*1000.) ;
               in.nozl.right.s3.setValue(i1) ;
           }
          }
              /*    dumb down limit - a8 schedule */
          if (arsched == 0) {
           a8rat = a8max ;
           fl1 = filter3(a8rat) ;
           in.nozl.left.f3.setText(String.valueOf(fl1)) ;
           i1 = (int) (((a8rat - a8min)/(a8max-a8min))*1000.) ;
           in.nozl.right.s3.setValue(i1) ;
          }
          a8 = a8rat * acore ;
          a8d = a8 * prat[7] / Math.sqrt(trat[7]) ;
             /* assumes choked a8 and a4 */
          a4 = a8*prat[5]*prat[15]*prat[7]/
           Math.sqrt(trat[7]*trat[5]*trat[15]);
          a4p = a8*prat[15]*prat[7]/Math.sqrt(trat[7]*trat[15]);
          acap = .9 * a2 ;
        }

        if (entype == 3) {      // ramjets
          game = getGama(tt[4],gamopt) ;
          if (athsched == 0) {   // scheduled throat area
             arthd = getAir(fsmach,gama) * Math.sqrt(etr) /
                     (getAir(1.0,game) * epr * prat[2]) ;
             if (arthd < arthmn) arthd = arthmn ;
             if (arthd > arthmx) arthd = arthmx ;
             fl1 = filter3(arthd) ;
             in.nozr.left.f3.setText(String.valueOf(fl1)) ;
             i1 = (int) (((arthd - arthmn)/(arthmx-arthmn))*1000.) ;
             in.nozr.right.s3.setValue(i1) ;
          }
          if (aexsched == 0) {   // scheduled exit area
             mexit = Math.sqrt((2.0/(game-1.0))*((1.0+.5*(gama-1.0)*fsmach*fsmach)
                  *Math.pow((epr*prat[2]),(game-1.0)/game) - 1.0) ) ;
             arexitd = getAir(1.0,game) / getAir(mexit,game) ;
             if (arexitd < arexmn) arexitd = arexmn ;
             if (arexitd > arexmx) arexitd = arexmx ;
             fl1 = filter3(arexitd) ;
             in.nozr.left.f4.setText(String.valueOf(fl1)) ;
             i1 = (int) (((arexitd - arexmn)/(arexmx-arexmn))*1000.) ;
             in.nozr.right.s4.setValue(i1) ;
          }
        }
     }
  }    // end Solver

  class Con extends Panel {
     Turbo outerparent ;
     Up up ;
     Down down ;

     Con (Turbo target) { 
                               
       outerparent = target ;
       setLayout(new GridLayout(2,1,5,5)) ;

       up = new Up(outerparent) ; 
       down = new Down() ;
 
       add(up) ;
       add(down) ;
    }

    class Up extends Panel {
       Button setref ;
       Turbo outerparent ;
       Choice engch,modch,pltch,untch ;

       Up (Turbo target) { 
          outerparent = target ;
          setLayout(new GridLayout(3,2,5,5)) ;

          modch = new Choice() ;
          modch.addItem("Design Mode") ;
          modch.addItem("Tunnel Test Mode");
          modch.addItem("Override");
          modch.select(0) ;

          engch = new Choice() ;
          engch.addItem("Load My Design") ;
          engch.addItem("Load J85 Model") ;
          engch.addItem("Load F100 Model");
          engch.addItem("Load CF6 Model");
          engch.addItem("Load Ramjet Model");
          engch.addItem("Override");
          engch.select(0) ;

          pltch = new Choice() ;
  //        pltch.addItem("Pictures") ;
          pltch.addItem("Graphs") ;
          pltch.addItem("Engine Performance") ;
          pltch.addItem("Component Performance") ;
          pltch.addItem("Override");
   //       pltch.addItem("Diagnostics");
          pltch.select(1) ;
          pltch.setBackground(Color.white) ;
          pltch.setForeground(Color.red) ;

          untch = new Choice() ;
          untch.addItem("Imperial Units") ;
          untch.addItem("Metric Units");
          untch.addItem("Override");
          untch.select(0) ;

          setref = new Button("Reset") ;
          setref.setBackground(Color.red) ;
          setref.setForeground(Color.white) ;

          add(modch) ;
          add(setref) ;

          add(engch) ;
          add(untch) ;  

          add(new Label("Output:", Label.RIGHT)) ;  
          add(pltch) ;
       }

       public Insets insets() {
          return new Insets(0,5,5,0) ;
       }

       public boolean action(Event evt, Object arg) {
         String label = (String)arg ;

         if(evt.target instanceof Choice) {
            this.handleProb(arg) ;
            return true ;
         }
         if(evt.target instanceof Button) {
            this.handleBut(arg) ;
            return true ;
         }
         else return false ;
       }

       public void handleBut(Object arg) {
             String label = (String)arg ;

    //       if(label.equals("Reset")) {
                 solve.setDefaults() ;
                 setUnits() ;
                 setPanl() ;
                 layin.show(in, "first")  ;
                 layout.show(out, "first")  ;

                 engch.select(0) ;
                 modch.select(0) ;
                 pltch.select(0) ;
                 untch.select(lunits) ;

                 in.flight.left.o1.setBackground(Color.black) ;
                 in.flight.left.o1.setForeground(Color.yellow) ;
                 in.flight.left.o2.setBackground(Color.black) ;
                 in.flight.left.o2.setForeground(Color.yellow) ;
                 in.flight.left.o3.setBackground(Color.black) ;
                 in.flight.left.o3.setForeground(Color.yellow) ;
                 in.flight.left.f1.setBackground(Color.white) ;
                 in.flight.left.f1.setForeground(Color.black) ;
                 in.flight.left.f2.setBackground(Color.white) ;
                 in.flight.left.f2.setForeground(Color.black) ;
                 in.size.right.sizch.select(0) ;
                 in.size.left.f1.setBackground(Color.white) ;
                 in.size.left.f1.setForeground(Color.black) ;
                 in.size.left.f3.setBackground(Color.black) ;
                 in.size.left.f3.setForeground(Color.yellow) ;
                 in.comp.right.stgch.select(0) ;
                 in.comp.left.f3.setBackground(Color.black) ;
                 in.comp.left.f3.setForeground(Color.yellow) ;
                 in.turb.right.stgch.select(0) ;
                 in.turb.left.f3.setBackground(Color.black) ;
                 in.turb.left.f3.setForeground(Color.yellow) ;
                 in.inlet.right.imat.select(minlt) ;
                 in.fan.right.fmat.select(mfan) ;
                 in.comp.right.cmat.select(mcomp) ;
                 in.burn.right.bmat.select(mburner) ;
                 in.turb.right.tmat.select(mturbin) ;
                 in.nozl.right.nmat.select(mnozl) ;
                 in.nozr.right.nrmat.select(mnozr) ;
                 solve.comPute() ;
                 out.plot.repaint() ;
                 return;
       //    }
       }

       public void handleProb(Object arg) {
        String label = (String)arg ;
 
        if (plttyp != 7) {
    // units change
          if(label.equals("Metric Units")) {
             lunits = 1 ;
             setUnits () ;
             setPanl() ;
             if (plttyp >=3) setPlot () ;
          }
          if(label.equals("Imperial Units")) {
             lunits = 0 ;
             setUnits () ;
             setPanl() ;
             if (plttyp >=3) setPlot () ;
          }
          if(label.equals("% Change")) {
             lunits = 2 ;
             setUnits () ;
             setPanl() ;
             if (plttyp >=3) setPlot () ;
          }
 // mode
          if(label.equals("Design Mode")) {
             inflag = 0 ;
          }
          if(label.equals("Tunnel Test Mode")) {
             inflag = 1 ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.comPute() ;
             solve.myDesign() ;
             ytrans = 115.0 ;
             view.start() ;
             varflag = 0 ;
             layin.show(in, "first")  ;
          }

          if(label.equals("Load My Design")) {
             varflag = 0 ;
             layin.show(in, "first")  ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.loadMine() ;
          }
          if(label.equals("Load J85 Model")) {
             varflag = 0 ;
             layin.show(in, "first")  ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.loadJ85() ;
          }
          if(label.equals("Load F100 Model")) {
             varflag = 0 ;
             layin.show(in, "first")  ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.loadF100() ;
          }
          if(label.equals("Load CF6 Model")) {
             varflag = 0 ;
             layin.show(in, "first")  ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.loadCF6() ;
          }
          if(label.equals("Load Ramjet Model")) {
             varflag = 0 ;
             layin.show(in, "first")  ;
             lunits = 0 ;
             setUnits () ;
             untch.select(0) ;
             solve.loadRamj() ;
          }
  
          if(label.equals("Engine Performance")) {
              plttyp = 0 ;
              layout.show(out, "first")  ;
              showcom = 0 ;
          }
          if(label.equals("Component Performance")) {
              plttyp = 0 ;
              layout.show(out, "third")  ;
              showcom = 1 ;
          }
/*
          if(label.equals("Pictures")) {
              plttyp = 2 ;
              showcom = 0 ;
              layout.show(out, "second")  ;
              partimg = getImage(getCodeBase(),"photo.gif");
              out.plot.repaint() ;
          }
*/
          if(label.equals("Graphs")) {
              plttyp = 3 ;
              showcom = 0 ;
              layout.show(out, "second")  ;
              setPlot () ;
              out.plot.repaint() ;
          }
/*
          if(label.equals("Diagnostics")) {
              plttyp = 0 ;
              showcom = 0 ;
              layout.show(out, "fourth")  ;
          }
*/
          solve.comPute() ; 
        }
       } // end handler
     } // end up

     class Down extends Panel {
        TextField o4, o5, o6, o10, o11, o12, o14, o15 ;

        Down () { 
            setLayout(new GridLayout(4,4,1,5)) ;

            o4 = new TextField() ;
            o4.setBackground(Color.black) ;
            o4.setForeground(Color.yellow) ;
            o5 = new TextField() ;
            o5.setBackground(Color.black) ;
            o5.setForeground(Color.yellow) ;
            o6 = new TextField() ;
            o6.setBackground(Color.black) ;
            o6.setForeground(Color.yellow) ;
            o10 = new TextField() ;
            o10.setBackground(Color.black) ;
            o10.setForeground(Color.yellow) ;
            o11 = new TextField() ;
            o11.setBackground(Color.black) ;
            o11.setForeground(Color.yellow) ;
            o12 = new TextField() ;
            o12.setBackground(Color.black) ;
            o12.setForeground(Color.yellow) ;
            o14 = new TextField() ;
            o14.setBackground(Color.black) ;
            o14.setForeground(Color.yellow) ;
            o15 = new TextField() ;
            o15.setBackground(Color.black) ;
            o15.setForeground(Color.yellow) ;
   
            add(new Label("Net Thrust", Label.CENTER)) ;  
            add(o4) ;
            add(new Label("Fuel Flow", Label.CENTER)) ;  
            add(o5) ;

            add(new Label("Gross Thrust", Label.CENTER)) ;
            add(o14) ;
            add(new Label("TSFC", Label.CENTER)) ;  
            add(o6) ;  

            add(new Label("Ram Drag", Label.CENTER)) ;
            add(o15) ;
            add(new Label("Core Airflow", Label.CENTER)) ;  
            add(o10) ;
 
            add(new Label("Fnet / W ", Label.CENTER)) ;  
            add(o12) ;  
            add(new Label("Weight", Label.CENTER)) ;  
            add(o11) ;
        }

     }  // end Down

     public void setPanl() {
        double v1,v2,v3,v4 ;
        float fl1, fl2, fl3, fl4 ;
        int i1,i2,i3,i4 ;

// set limits and labels
   // flight conditions
        v1 = 0.0 ;
        vmn1 = -10.0 ;  vmx1 = 10.0 ;
        v2 = 0.0 ;
        vmn2 = -10.0 ;  vmx2 = 10.0 ;
        v3 = 0.0 ;
        vmn3 = -10.0 ;  vmx3 = 10.0 ;
        v4 = gama ;

        if (lunits <= 1) {
           v1 = u0d ;
           vmn1 = u0min;  vmx1 = u0max ;
           v2 = altd ;
           vmn2 = altmin;  vmx2 = altmax ;
           v3 = throtl ;
           vmn3 = thrmin;  vmx3 = thrmax ;
        }

        in.flight.left.f1.setText(String.valueOf(filter0(v1))) ;
        in.flight.left.f2.setText(String.valueOf(filter0(v2))) ;
        in.flight.left.f3.setText(String.valueOf(filter3(v3))) ;
        in.flight.left.f4.setText(String.valueOf(filter3(v4))) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
        i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;

        in.flight.right.s1.setValue(i1) ;
        in.flight.right.s2.setValue(i2) ;
        in.flight.right.s3.setValue(i3) ;

        in.flight.right.inptch.select(inptype) ;
        in.flight.right.nozch.select(abflag) ;
        in.flight.left.inpch.select(gamopt) ;

   // size
        v1 = 0.0 ;
        vmn1 = -10.0 ;  vmx1 = 10.0 ;
        vmn3 = -10.0 ;  vmx3 = 10.0 ;
        if (lunits <= 1) {
           v1 = a2d ;
           vmn1 = a2min;  vmx1 = a2max ;
           v3 = diameng ;
        }
        fl1 = filter3(v1) ;
        fl3 = filter3(v3) ;
        in.size.left.f1.setText(String.valueOf(fl1)) ;
        in.size.left.f3.setText(String.valueOf(fl3)) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;

        in.size.right.sizch.select(siztype) ;
        in.size.right.s1.setValue(i1) ;

        in.size.left.f2.setText(String.valueOf(filter0(weight))) ;
        in.size.right.chmat.select(wtflag) ;

   // inlet
        if (pt2flag == 0) {             /*     mil spec      */
          if (fsmach > 1.0 ) {          /* supersonic */
             eta[2] = 1.0 - .075*Math.pow(fsmach - 1.0, 1.35) ;
          }
          else {
             eta[2] = 1.0 ;
          }
        }

        v1 = eta[2] ;
        vmn1 = etmin;  vmx1 = etmax ;

        if (lunits == 2) {
          v1 = 0.0 ;
          vmx1 = 100.0 - 100.0 * et2ref ;
          vmn1 = vmx1 - 20.0 ;
        }
        fl1 = filter3(v1) ;
        in.inlet.left.f1.setText(String.valueOf(fl1)) ;
        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        in.inlet.right.s1.setValue(i1) ;
          // materials
        in.inlet.right.imat.select(minlt) ;
        in.inlet.left.di.setText(String.valueOf(filter0(dinlt))) ;
        in.inlet.left.ti.setText(String.valueOf(filter0(tinlt))) ;
  //  fan
        v1 = p3fp2d ;
        vmn1 = fprmin;  vmx1 = fprmax ;
        v2 = eta[13] ;
        vmn2 = etmin;  vmx2 = etmax ;
        v3 = byprat ;
        vmn3 = bypmin;  vmx3 = bypmax ;

        if (lunits == 2) {
          v1 = 0.0 ;
          vmn1 = -10.0;  vmx1 = 10.0 ;
          v2 = 0.0 ;
          vmx2 = 100.0 - 100.0 * et13ref ;
          vmn2 = vmx2 - 20.0 ;
          v3 = 0.0 ;
          vmn3 = -10.0;  vmx3 = 10.0 ;
        }
        fl1 = (float) v1 ;
        fl2 = (float) v2 ;
        fl3 = (float) v3 ;

        in.fan.left.f1.setText(String.valueOf(fl1)) ;
        in.fan.left.f2.setText(String.valueOf(fl2)) ;
        in.fan.left.f3.setText(String.valueOf(fl3)) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
        i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;

        in.fan.right.s1.setValue(i1) ;
        in.fan.right.s2.setValue(i2) ;
        in.fan.right.s3.setValue(i3) ;

          // materials
        in.fan.right.fmat.select(mfan) ;
        in.fan.left.df.setText(String.valueOf(filter0(dfan))) ;
        in.fan.left.tf.setText(String.valueOf(filter0(tfan))) ;
  // compressor 
        v1 = p3p2d ;
        vmn1 = cprmin;  vmx1 = cprmax ;
        v2 = eta[3] ;
        vmn2 = etmin;  vmx2 = etmax ;

        if (lunits == 2) {
          v1 = 0.0 ;
          vmn1 = -10.0;  vmx1 = 10.0 ;
          v2 = 0.0 ;
          vmx2 = 100.0 - 100.0 * et3ref ;
          vmn2 = vmx2 - 20.0 ;
        }
        fl1 = (float) v1 ;
        fl2 = (float) v2 ;

        in.comp.left.f1.setText(String.valueOf(fl1)) ;
        in.comp.left.f2.setText(String.valueOf(fl2)) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;

        in.comp.right.s1.setValue(i1) ;
        in.comp.right.s2.setValue(i2) ;
          // materials
        in.comp.right.cmat.select(mcomp) ;
        in.comp.left.dc.setText(String.valueOf(filter0(dcomp))) ;
        in.comp.left.tc.setText(String.valueOf(filter0(tcomp))) ;
  //  burner
        v1 = tt4d ;
        vmn1 = t4min;  vmx1 = t4max ;
        v2 = eta[4] ;
        vmn2 = etmin;  vmx2 = etmax ;
        v3 = prat[4] ;
        vmn3 = etmin;  vmx3 = etmax ;
        v4 = fhvd ;

        if (lunits == 2) {
          v1 = 0.0 ;
          vmn1 = -10.0;  vmx1 = 10.0 ;
          v2 = 0.0 ;
          vmx2 = 100.0 - 100.0 * et4ref ;
          vmn2 = vmx2 - 20.0 ;
          v3 = 0.0 ;
          vmx3 = 100.0 - 100.0 * p4ref ;
          vmn3 = vmx3 - 20.0 ;
        }
        fl1 = (float) v1 ;
        fl2 = (float) v2 ;
        fl3 = (float) v3 ;

        in.burn.left.f1.setText(String.valueOf(filter0(v1))) ;
        in.burn.left.f2.setText(String.valueOf(fl2)) ;
        in.burn.left.f3.setText(String.valueOf(fl3)) ;
        in.burn.left.f4.setText(String.valueOf(filter0(v4))) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
        i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;

        in.burn.right.s1.setValue(i1) ;
        in.burn.right.s2.setValue(i2) ;
        in.burn.right.s3.setValue(i3) ;
        in.burn.right.fuelch.select(fueltype) ;
          // materials
        in.burn.right.bmat.select(mburner) ;
        in.burn.left.db.setText(String.valueOf(filter0(dburner))) ;
        in.burn.left.tb.setText(String.valueOf(filter0(tburner))) ;
  //  turbine
        v1 = eta[5] ;
        vmn1 = etmin;  vmx1 = etmax ;
        if (lunits == 2) {
          v1 = 0.0 ;
          vmx1 = 100.0 - 100.0 * et5ref ;
          vmn1 = vmx1 - 20.0 ;
        }
        fl1 = (float) v1 ;
        in.turb.left.f1.setText(String.valueOf(fl1)) ;
        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        in.turb.right.s1.setValue(i1) ;
          // materials
        in.turb.right.tmat.select(mturbin) ;
        in.turb.left.dt.setText(String.valueOf(filter0(dturbin))) ;
        in.turb.left.tt.setText(String.valueOf(filter0(tturbin))) ;
  //  turbine nozzle 
        v1 = tt7d ;
        vmn1 = t7min;  vmx1 = t7max ;
        v2 = eta[7] ;
        vmn2 = etmin;  vmx2 = etmax ;
        v3 = a8rat ;
        vmn3 = a8min;  vmx3 = a8max ;

        if (lunits == 2) {
          v1 = 0.0 ;
          vmn1 = -10.0;  vmx1 = 10.0 ;
          v2 = 0.0 ;
          vmx2 = 100.0 - 100.0 * et7ref ;
          vmn2 = vmx2 - 20.0 ;
          v3 = 0.0 ;
          vmn3 = -10.0;  vmx3 = 10.0 ;
        }
        fl1 = filter0(v1) ;
        fl2 = filter3(v2) ;
        fl3 = filter3(v3) ;

        in.nozl.left.f1.setText(String.valueOf(fl1)) ;
        in.nozl.left.f2.setText(String.valueOf(fl2)) ;
        in.nozl.left.f3.setText(String.valueOf(fl3)) ;

        i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
        i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;

        in.nozl.right.s1.setValue(i1) ;
        in.nozl.right.s2.setValue(i2) ;
        in.nozl.right.s3.setValue(i3) ;
        in.nozl.right.arch.select(arsched) ;
          // materials
        in.nozl.right.nmat.select(mnozl) ;
        in.nozl.left.dn.setText(String.valueOf(filter0(dnozl))) ;
        in.nozl.left.tn.setText(String.valueOf(filter0(tnozl))) ;
  //  ramjet nozzle 
        v2 = eta[7] ;
        vmn2 = etmin;  vmx2 = etmax ;
        v3 = arthd ;
        vmn3 = arthmn;  vmx3 = arthmx ;
        v4 = arexitd ;
        vmn4 = arexmn;  vmx4 = arexmx ;

        if (lunits == 2) {
          v2 = 0.0 ;
          vmx2 = 100.0 - 100.0 * et7ref ;
          vmn2 = vmx2 - 20.0 ;
          v3 = 0.0 ;
          vmn3 = -10.0;  vmx3 = 10.0 ;
          v4 = 0.0 ;
          vmn4 = -10.0;  vmx4 = 10.0 ;
        }
        fl2 = filter3(v2) ;
        fl3 = filter3(v3) ;
        fl4 = filter3(v4) ;

        in.nozr.left.f2.setText(String.valueOf(fl2)) ;
        in.nozr.left.f3.setText(String.valueOf(fl3)) ;
        in.nozr.left.f4.setText(String.valueOf(fl4)) ;

        i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
        i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
        i4 = (int) (((v4 - vmn4)/(vmx4-vmn4))*1000.) ;

        in.nozr.right.s2.setValue(i2) ;
        in.nozr.right.s3.setValue(i3) ;
        in.nozr.right.s4.setValue(i4) ;
          // materials
        in.nozr.right.nrmat.select(mnozr) ;
        in.nozr.left.dn.setText(String.valueOf(filter0(dnozr))) ;
        in.nozr.left.tn.setText(String.valueOf(filter0(tnozr))) ;
        return ;
     }

     public void setPlot() {   // Plot Scales

        showcom = 1 ;

        switch (plttyp) {
           case 3: {                              // press variation 
             nabs = nord = 1 ;
             ordkeep = abskeep = 1 ;
             lines = 1;
             npt = 9;
             laby = String.valueOf("Press");
             begy = 0.0; 
             if (lunits == 0) {
                 labyu = String.valueOf("psi");
                 endy = 1000.; 
             }
             if (lunits == 1) {
                 labyu = String.valueOf("kPa");
                 endy = 5000.; 
             }
             ntiky=11;
             labx = String.valueOf("Station");
             labxu = String.valueOf(" ");
             begx = 0.0; endx = 8.0; 
             ntikx=9;
             break ;                 
           }
           case 4: {                              // temp variation 
             nabs = nord = 1 ;
             ordkeep = abskeep = 1 ;
             lines = 1;
             npt = 9;
             laby = String.valueOf("Temp");
             if (lunits == 0) labyu = String.valueOf("R");
             if (lunits == 1) labyu = String.valueOf("K");
             if (lunits == 2) labyu = String.valueOf("%");
             begy = 0.0; endy = 5000.; 
             ntiky=11;
             labx = String.valueOf("Station");
             labxu = String.valueOf(" ");
             begx = 0.0; endx = 8.0; 
             ntikx=9;
             break ;                 
           }
           case 5: {                              //  T - s plot
             nabs = nord = 2 ;
             ordkeep = abskeep = 1 ;
             lines = 1;
             npt = 7;
             laby = String.valueOf("Temp");
             if (lunits == 0) labyu = String.valueOf("R");
             if (lunits == 1) labyu = String.valueOf("K");
             begy = 0.0; endy = 5000.; 
             ntiky=11;
             labx = String.valueOf("s");
             if (lunits == 0) labxu = String.valueOf("Btu/lbm R");
             if (lunits == 1) labxu = String.valueOf("Joule/kg K");
             begx = 0.0; endx = 1.0*bconv; 
             ntikx=2;
             break;
           }
           case 6: {                              //  p - v plot
             nord = nabs = 3 ;
             ordkeep = abskeep = 2 ;
             lines = 1;
             npt = 25;
             laby = String.valueOf("Press");
             begy = 0.0;
             if (lunits == 0) {
                 labyu = String.valueOf("psi");
                 endy = 1000.;
             }
             if (lunits == 1) {
                 labyu = String.valueOf("kPa");
                 endy = 5000.;
             }
             ntiky=11;
             labx = String.valueOf("v");
             if (lunits == 0)labxu = String.valueOf("ft^3/lb");
             if (lunits == 1)labxu = String.valueOf("m^3/Kg");
             begx=0.0; endx=100.0*dconv; 
             ntikx=2;
             break;
           }
           case 7: {                              //  generate plot 
             nord = nabs = 3 ;
             ordkeep = abskeep = 3 ;
             lines = 0;
             npt = 0;
             laby = String.valueOf("Fn");
             if (lunits == 0) labyu = String.valueOf("lb");
             if (lunits == 1) labyu = String.valueOf("N");
             begy=0.0; endy=100000.; 
             ntiky=11 ;
             labx = String.valueOf("Mach");
             labxu = String.valueOf(" ");
             if (entype <=2) {
                begx=0.0; endx=2.0; 
             }
             if (entype ==3) {
                begx=0.0; endx=6.0; 
             }
             ntikx=5;
             break;
           }
        }
     }
   
     public void setUnits() {   // Switching Units
       double alts,alm1s,alm2s,ars,arm1s,arm2s,t4s,t7s,t4m1s,t4m2s,t7m1s,t7m2s ;
       double u0s,pmxs,tmns,tmxs,diars,dim1s,dim2s,fhvs ;
       int i1 ;
   
       alts  = altd / lconv1 ;
       alm1s = altmin / lconv1 ;
       alm2s = altmax / lconv1 ;
       ars   = a2d / aconv ;
       arm1s = a2min / aconv ;
       arm2s = a2max / aconv ;
       diars = diameng/ lconv1 ;
       dim1s = diamin / lconv1 ;
       dim2s = diamax / lconv1 ;
       u0s   = u0d / lconv2 ;
       pmxs  = pmax / pconv ;
       tmns  = tmin / tconv ;
       tmxs  = tmax / tconv ;
       t4s   = tt4d / tconv ;
       t4m1s = t4min / tconv ;
       t4m2s = t4max / tconv ;
       t7s   = tt7d / tconv ;
       t7m1s = t7min / tconv ;
       t7m2s = t7max / tconv ;
       fhvs = fhvd / flconv ;
       switch (lunits) {
          case 0:{                   /* Imperial Units */
                 lconv1 = 1.0 ; lconv2 = 1.0 ; fconv = 1.0 ; econv = 1.0 ;
                 mconv1 = 1.0 ; pconv  = 1.0 ; tconv = 1.0 ; mconv2= 1.0 ;
                 econv2 = 1.0 ;
                 tref = 459.7 ;
                 out.vars.lpa.setText("Pres-psi") ;
                 out.vars.lpb.setText("Pres-psi") ;
                 out.vars.lta.setText("Temp-R") ;
                 out.vars.ltb.setText("Temp-R") ;
                 in.flight.right.l2.setText("lb/sq in") ;
                 in.flight.right.l3.setText("F") ;
                 in.flight.left.l1.setText("Speed-mph") ;
                 in.flight.left.l2.setText("Altitude-ft") ;
                 in.size.left.l2.setText("Weight-lbs") ;
                 in.size.left.l1.setText("Area-sq ft") ;
                 in.size.left.l3.setText("Diameter-ft") ;
                 in.burn.left.l1.setText("Tmax -R") ;
                 in.burn.left.l4.setText("FHV BTU/lb") ;
                 in.nozl.left.l1.setText("Tmax -R") ;
                 in.inlet.right.lmat.setText("lbm/ft^3");
                 in.fan.right.lmat.setText("lbm/ft^3");
                 in.comp.right.lmat.setText("lbm/ft^3");
                 in.burn.right.lmat.setText("lbm/ft^3");
                 in.turb.right.lmat.setText("lbm/ft^3");
                 in.nozl.right.lmat.setText("lbm/ft^3");
                 in.nozr.right.lmat.setText("lbm/ft^3");
                 in.inlet.left.lmat.setText("T lim -R");
                 in.fan.left.lmat.setText("T lim -R");
                 in.comp.left.lmat.setText("T lim -R");
                 in.burn.left.lmat.setText("T lim -R");
                 in.turb.left.lmat.setText("T lim -R");
                 in.nozl.left.lmat.setText("T lim -R");
                 in.nozr.left.lmat.setText("T lim -R");
                 u0max = 1500. ;
                 if (entype == 3) u0max = 4500. ;
                 g0d = 32.2 ;
//                 setref.setVisible(false) ;
                 break ;
          }
          case 1:{                   /* Metric Units */
                 lconv1 = .3048 ; lconv2 = 1.609 ; fconv = 4.448 ; 
                 econv = 1055.; econv2 = 1.055 ;
                 mconv1 = .4536 ; pconv  = 6.891 ; tconv = 0.555555 ; 
                 mconv2 = 14.59;    tref = 273.1 ;
                 out.vars.lpa.setText("Pres-kPa") ;
                 out.vars.lpb.setText("Pres-kPa") ;
                 out.vars.lta.setText("Temp-K") ;
                 out.vars.ltb.setText("Temp-K") ;
                 in.flight.right.l2.setText("k Pa") ;
                 in.flight.right.l3.setText("C") ;
                 in.flight.left.l1.setText("Speed-kmh") ;
                 in.flight.left.l2.setText("Altitude-m") ;
                 in.size.left.l2.setText("Weight-N") ;
                 in.size.left.l1.setText("Area-sq m") ;
                 in.size.left.l3.setText("Diameter-m") ;
                 in.burn.left.l1.setText("Tmax -K") ;
                 in.burn.left.l4.setText("FHV kJ/kg") ;
                 in.nozl.left.l1.setText("Tmax -K") ;
                 in.inlet.right.lmat.setText("kg/m^3");
                 in.fan.right.lmat.setText("kg/m^3");
                 in.comp.right.lmat.setText("kg/m^3");
                 in.burn.right.lmat.setText("kg/m^3");
                 in.turb.right.lmat.setText("kg/m^3");
                 in.nozl.right.lmat.setText("kg/m^3");
                 in.nozr.right.lmat.setText("kg/m^3");
                 in.inlet.left.lmat.setText("T lim -K");
                 in.fan.left.lmat.setText("T lim -K");
                 in.comp.left.lmat.setText("T lim -K");
                 in.burn.left.lmat.setText("T lim -K");
                 in.turb.left.lmat.setText("T lim -K");
                 in.nozl.left.lmat.setText("T lim -K");
                 in.nozr.left.lmat.setText("T lim -K");
                 u0max = 2500. ;
                 if (entype == 3) u0max = 7500. ;
                 g0d = 9.81 ;
//                 setref.setVisible(false) ;
                 break ;
          }
          case 2:{            /* Percent Change .. convert to English */
                 lconv1 = 1.0 ; lconv2 = 1.0 ; fconv = 1.0 ; econv = 1.0 ;
                 mconv1 = 1.0 ; pconv  = 1.0 ; tconv = 1.0 ; mconv2= 1.0 ;
                 tref = 459.7 ;
                 in.flight.right.l2.setText("lb/sq in") ;
                 in.flight.right.l3.setText("F") ;
                 in.flight.left.l1.setText("Speed-%") ;
                 in.flight.left.l2.setText("Altitude-%") ;
                 in.size.left.l2.setText("Weight-lbs") ;
                 in.size.left.l1.setText("Area-%") ;
                 in.burn.left.l1.setText("Tmax -%") ;
                 in.nozl.left.l1.setText("Tmax -%") ;
                 in.inlet.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.fan.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.comp.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.burn.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.turb.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.nozl.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 in.nozr.right.lmat.setText("<-lbm/ft^3 -Rankine");
                 u0max = 1500. ;
                 if (entype == 3) u0max = 4500. ;
                 g0d = 32.2 ;
//                 setref.setVisible(true) ;
                 pt2flag = 1 ;
                 in.inlet.right.inltch.select(pt2flag) ;
                 arsched = 1 ;
                 in.nozl.right.arch.select(arsched) ;
                 athsched = 1 ;
                 in.nozr.right.atch.select(athsched) ;
                 aexsched = 1 ;
                 in.nozr.right.aech.select(aexsched) ;
                 break ;
          }
        }
        aconv = lconv1 * lconv1 ;
        bconv = econv / tconv / mconv1 ;
        dconv = mconv1/ aconv / lconv1 ;
        flconv = econv2 / mconv1 ;
     
        altd    = alts  * lconv1 ;
        altmin  = alm1s * lconv1 ;
        altmax  = alm2s * lconv1 ;
        a2d     = ars * aconv ;
        a2min   = arm1s * aconv ;
        a2max   = arm2s * aconv ;
        diameng = diars * lconv1 ;
        diamin  = dim1s * lconv1 ;
        diamax  = dim2s * lconv1 ;
        u0d     = u0s * lconv2 ;
        pmax   = pmxs * pconv ;
        tmax   = tmxs * tconv ;
        tmin   = tmns * tconv ;
        tt4d   = t4s * tconv ;
        t4min  = t4m1s * tconv ;
        t4max  = t4m2s * tconv ;
        tt7d   = t7s * tconv ;
        t7min  = t7m1s * tconv ;
        t7max  = t7m2s * tconv ;
        fhvd   = fhvs * flconv ;

        if (lunits == 2) {     // initialization of reference variables
           if (u0d <= 10.0) u0d = 10.0 ;
           u0ref = u0d;  
           if (altd <= 10.0) altd = 10.0 ;
           altref = altd;   
           thrref = throtl ;
           a2ref = a2d;  et2ref = eta[2] ; fpref = p3fp2d ;
           et13ref = eta[13]; bpref = byprat ; cpref = p3p2d ;
           et3ref  = eta[3];  et4ref = eta[4];  et5ref = eta[5] ;
           t4ref = tt4d ;  p4ref = prat[4] ; t7ref = tt7d;
           et7ref = eta[7]; a8ref = a8rat ; 
           fnref = fnlb; fuelref = fuelrat; sfcref = sfc;
           airref = eair ; epref = epr; etref=etr; faref = fa ;
           wtref = weight ; wfref = fnlb/weight;
        }
               //  Ouput panel
        out.box.loadOut () ;
        out.vars.loadOut () ;
        in.fillBox () ;
   
        return ;
     }
  }  // end Control Panel

  class In extends Panel {
     Turbo outerparent ;
     Flight flight ;
     Size size ;
     Inlet inlet ;
     Fan fan ;
     Comp comp ;
     Burn burn ;
     Turb turb ;
     Nozl nozl ;
     Plot plot ;
     Nozr nozr ;

     In (Turbo target) {
                            
          outerparent = target ;
          layin = new CardLayout() ;
          setLayout(layin) ;

          flight = new Flight(outerparent) ; 
          size = new Size(outerparent) ;
          inlet = new Inlet(outerparent) ;
          fan = new Fan(outerparent) ;
          comp = new Comp(outerparent) ;
          burn = new Burn(outerparent) ;
          turb = new Turb(outerparent) ;
          nozl = new Nozl(outerparent) ;
          plot = new Plot(outerparent) ;
          nozr = new Nozr(outerparent) ;
 
          add ("first", flight) ;
          add ("second", size) ;
          add ("third", inlet) ;
          add ("fourth", fan) ;
          add ("fifth", comp) ;
          add ("sixth", burn) ;
          add ("seventh", turb) ;
          add ("eighth", nozl) ;
          add ("ninth", plot) ;
          add ("tenth", nozr) ;
     }

     class Flight extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Flight (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,5,5)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2,s3;
           Label l2, l3 ;
           Choice nozch,inptch ;

           Right (Turbo target) {
    
               int i1, i2, i3  ;
   
               outerparent = target ;
               setLayout(new GridLayout(7,1,10,5)) ;
    
               i1 = (int) (((u0d - vmn1)/(vmx1-vmn1))*1000.) ;
               i2 = (int) (((altd - vmn2)/(vmx2-vmn2))*1000.) ;
               i3 = (int) (((throtl - vmn3)/(vmx3-vmn3))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,1,0,1000);
  
               l2 = new Label("lb/sq in", Label.LEFT) ;
               l3 = new Label("F", Label.LEFT) ;

               nozch = new Choice() ;
               nozch.addItem("Afterburner OFF") ;
               nozch.addItem("Afterburner ON");
               nozch.select(0) ;

               inptch = new Choice() ;
               inptch.addItem("Input Speed + Altitude") ;
               inptch.addItem("Input Mach + Altitude");
               inptch.addItem("Input Speed+Pres+Temp") ;
               inptch.addItem("Input Mach+Pres+Temp") ;
               inptch.select(0) ;

               add(inptch) ;
               add(l2) ;  
               add(l3) ;  
               add(s1) ;
               add(s2) ;
               add(s3) ;
               add(nozch) ;
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
   
          public void handleBar(Event evt) {     // flight conditions
            int i1, i2,i3 ;
            Double V6,V7 ;
            double v1,v2,v3,v6,v7 ;
            float fl1, fl2, fl3 ;

            i1 = s1.getValue() ;
            i2 = s2.getValue() ;
            i3 = s3.getValue() ;
 
            inptype = inptch.getSelectedIndex() ;
            if (inptype == 0 || inptype == 2) {
               left.f1.setBackground(Color.white) ;
               left.f1.setForeground(Color.black) ;
               left.o1.setBackground(Color.black) ;
               left.o1.setForeground(Color.yellow) ;
            }
            if (inptype == 1 || inptype == 3 ) {
               left.f1.setBackground(Color.black) ;
               left.f1.setForeground(Color.yellow) ;
               left.o1.setBackground(Color.white) ;
               left.o1.setForeground(Color.black) ;
            }
            if (inptype <= 1) {
               left.o2.setBackground(Color.black) ;
               left.o2.setForeground(Color.yellow) ;
               left.o3.setBackground(Color.black) ;
               left.o3.setForeground(Color.yellow) ;
               left.f2.setBackground(Color.white) ;
               left.f2.setForeground(Color.black) ;
            }
            if (inptype >= 2) {
               left.o2.setBackground(Color.white) ;
               left.o2.setForeground(Color.black) ;
               left.o3.setBackground(Color.white) ;
               left.o3.setForeground(Color.black) ;
               left.f2.setBackground(Color.black) ;
               left.f2.setForeground(Color.yellow) ;
            }

            if (lunits <= 1) {
               vmn1 = u0min;   vmx1 = u0max ;
               vmn2 = altmin;  vmx2 = altmax ;
               vmn3 = thrmin;  vmx3 = thrmax ;
            }
            if (lunits == 2) {
               vmn1 = -10.0 ;  vmx1 = 10.0 ;
               vmn2 = -10.0 ;  vmx2 = 10.0 ;
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
            v3 = i3 * (vmx3 - vmn3)/ 1000. + vmn3 ;
 
            if (inptype >= 2) {
               v2 = 0.0 ;
               i2 = 0 ;
               s2.setValue(i2) ;

               V6 = Double.valueOf(left.o2.getText()) ;
               v6 = V6.doubleValue() ;
               V7 = Double.valueOf(left.o3.getText()) ;
               v7 = V7.doubleValue() ;
               ps0 = v6 ;
               if (v6 <= 0.0) {
                 ps0 = v6 = 0.0 ;
                 fl1 = (float) v6 ;
                 left.o2.setText(String.valueOf(fl1)) ;
               }
               if (v6 >= pmax) {
                 ps0 = v6 = pmax ;
                 fl1 = (float) v6 ;
                 left.o2.setText(String.valueOf(fl1)) ;
               }
               ps0 = ps0 / pconv ;
               ts0 = v7 + tref ;
               if (ts0 <= tmin) {
                 ts0 = tmin ;
                 v7 = ts0 - tref ;
                 fl1 = (float) v7 ;
                 left.o3.setText(String.valueOf(fl1)) ;
               }
               if (ts0 >= tmax) {
                 ts0 = tmax ;
                 v7 = ts0 - tref ;
                 fl1 = (float) v7 ;
                 left.o3.setText(String.valueOf(fl1)) ;
               }
               ts0 = ts0 / tconv ;
            }
         
   // flight conditions
            if (lunits <= 1) {
               u0d    = v1 ;
               altd   = v2 ;
               throtl = v3 ;
            }
            if (lunits == 2) {
               u0d   = v1 * u0ref/100. + u0ref ;
               altd  = v2 * altref/100. + altref ;
               throtl  = v3 * thrref/100. + thrref ;
            }
   
            if(entype == 1) abflag = nozch.getSelectedIndex() ;

            left.f1.setText(String.valueOf(filter0(v1))) ;
            left.f2.setText(String.valueOf(filter0(v2))) ;
            left.f3.setText(String.valueOf(filter3(v3))) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3, f4 ;
           TextField o1, o2, o3 ;
           Label l1, l2, l3, lmach ;
           Choice inpch ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(7,2,5,5)) ;
     
              l1 = new Label("Speed-mph", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)u0d),5) ;
              f1.setBackground(Color.white) ;
              f1.setForeground(Color.black) ;

              l2 = new Label("Altitude-ft", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)altd),5) ;
              f2.setBackground(Color.white) ;
              f2.setForeground(Color.black) ;

              l3 = new Label("Throttle", Label.CENTER) ;
              f3 = new TextField(String.valueOf((float)throtl),5) ;
   
              inpch = new Choice() ;
              inpch.addItem("Gamma") ;
              inpch.addItem("Gam(T)");
              inpch.select(1) ;
              f4 = new TextField(String.valueOf((float)gama),5) ;

              lmach = new Label("Mach", Label.CENTER) ;
              o1 = new TextField(String.valueOf((float)fsmach),5) ;
              o1.setBackground(Color.black) ;
              o1.setForeground(Color.yellow) ;

              o2 = new TextField() ;
              o2.setBackground(Color.black) ;
              o2.setForeground(Color.yellow) ;

              o3 = new TextField() ;
              o3.setBackground(Color.black) ;
              o3.setForeground(Color.yellow) ;

              add(lmach) ;
              add(o1) ;

              add(new Label(" Press ", Label.CENTER)) ;  
              add(o2) ;  

              add(new Label(" Temp  ", Label.CENTER)) ;  
              add(o3) ;  

              add(l1) ;
              add(f1) ;

              add(l2) ;
              add(f2) ;

              add(l3) ;
              add(f3) ;

              add(inpch) ;
              add(f4) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V2,V3,V4,V5,V6,V7 ;
             double v1,v2,v3,v4,v5,v6,v7 ;
             int i1,i2,i3 ;
             float fl1 ;

             gamopt  = inpch.getSelectedIndex() ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             V4 = Double.valueOf(f4.getText()) ;
             v4 = V4.doubleValue() ;
             V5 = Double.valueOf(o1.getText()) ;
             v5 = V5.doubleValue() ;
             V6 = Double.valueOf(o2.getText()) ;
             v6 = V6.doubleValue() ;
             V7 = Double.valueOf(o3.getText()) ;
             v7 = V7.doubleValue() ;

             if (lunits <= 1) {
     // Airspeed 
                 if (inptype == 0 || inptype == 2) {
                    u0d = v1 ;
                    vmn1 = u0min;   vmx1 = u0max ;
                    if(v1 < vmn1) {
                       u0d = v1 = vmn1 ;
                       fl1 = (float) v1 ;
                       f1.setText(String.valueOf(fl1)) ;
                    }
                    if(v1 > vmx1) {
                       u0d = v1 = vmx1 ;
                       fl1 = (float) v1 ;
                       f1.setText(String.valueOf(fl1)) ;
                    }
                 }
     // Mach 
                 if (inptype == 1 || inptype == 3) {
                    fsmach = v5 ;
                    if (fsmach < 0.0) {
                       fsmach = v5 = 0.0 ;
                       fl1 = (float) v5 ;
                       o1.setText(String.valueOf(fl1)) ;
                    }
                    if (fsmach > 2.25 && entype <=2) {
                       fsmach = v5 = 2.25 ;
                       fl1 = (float) v5 ;
                       o1.setText(String.valueOf(fl1)) ;
                    }
                    if (fsmach > 6.75 && entype ==3) {
                       fsmach = v5 = 6.75 ;
                       fl1 = (float) v5 ;
                       o1.setText(String.valueOf(fl1)) ;
                    }
                 }
     // Altitude
                 if (inptype <= 1) {
                    altd = v2 ;
                    vmn2 = altmin;  vmx2 = altmax ;
                    if(v2 < vmn2) {
                       altd = v2 =  vmn2 ;
                       fl1 = (float) v2 ;
                       f2.setText(String.valueOf(fl1)) ;
                    }
                    if(v2 > vmx2) {
                       altd = v2 =  vmx2 ;
                       fl1 = (float) v2 ;
                       f2.setText(String.valueOf(fl1)) ;
                    }
                 }
     // Pres and Temp
                 if (inptype >= 2) {
                    altd = v2 = 0.0 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                    ps0 = v6 ;
                    if (v6 <= 0.0) {
                      ps0 = v6 = 0.0 ;
                      fl1 = (float) v6 ;
                      o2.setText(String.valueOf(fl1)) ;
                    }
                    if (v6 >= pmax) {
                      ps0 = v6 = pmax ;
                      fl1 = (float) v6 ;
                      o2.setText(String.valueOf(fl1)) ;
                    }
                    ps0 = ps0 / pconv ;
                    ts0 = v7 + tref ;
                    if (ts0 <= tmin) {
                      ts0 = tmin ;
                      v7 = ts0 - tref ;
                      fl1 = (float) v7 ;
                      o3.setText(String.valueOf(fl1)) ;
                    }
                    if (ts0 >= tmax) {
                      ts0 = tmax ;
                      v7 = ts0 - tref ;
                      fl1 = (float) v7 ;
                      o3.setText(String.valueOf(fl1)) ;
                    }
                    ts0 = ts0 / tconv ;
                 }
     // Throttle
                 throtl = v3 ;
                 vmn3 = thrmin;  vmx3 = thrmax ;
                 if(v3 < vmn3) {
                    throtl = v3 =  vmn3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
                 if(v3 > vmx3) {
                    throtl = v3 = vmx3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
             }
             if (lunits == 2) {
     // Airspeed 
                 vmn1 = -10.0;   vmx1 = 10.0 ;
                 if(v1 < vmn1) {
                    v1 = vmn1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 if(v1 > vmx1) {
                    v1 = vmx1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 u0d = v1 * u0ref/100. + u0ref ;
     // Altitude 
                 vmn2 = -10.0;  vmx2 = 10.0 ;
                 if(v2 < vmn2) {
                    v2 =  vmn2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
                 if(v2 > vmx2) {
                    v2 =  vmx2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
                 altd = v2 * altref/100. + altref ;
     // Throttle 
                 vmn3 = -10.0;  vmx3 = 10.0 ;
                 if(v3 < vmn3) {
                    v3 =  vmn3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
                 if(v3 > vmx3) {
                    v3 = vmx3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
                 throtl = v3 * thrref/100. + thrref ;
            }
     // Gamma 
            gama = v4 ;
            if(v4 < 1.0) {
               gama = v4 =  1.0 ;
               fl1 = (float) v4 ;
               f4.setText(String.valueOf(fl1)) ;
            }
            if(v4 > 2.0) {
               gama = v4 = 2.0 ;
               fl1 = (float) v4 ;
               f4.setText(String.valueOf(fl1)) ;
            }
        
            i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
            i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
            i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
   
            right.s1.setValue(i1) ;
            right.s2.setValue(i2) ;
            right.s3.setValue(i3) ;

            solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Flight input

     class Size extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Size (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1;
           Choice chmat,sizch ;

           Right (Turbo target) {
    
               int i1 ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,10,5)) ;
    
               i1 = (int) (((a2d - a2min)/(a2max-a2min))*1000.) ;
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
   
               chmat = new Choice() ;
               chmat.addItem("Computed Weight") ;
               chmat.addItem("Input Weight ");
               chmat.select(0) ;
   
               sizch = new Choice() ;
               sizch.addItem("Input Frontal Area") ;
               sizch.addItem("Input Diameter ");
               sizch.select(0) ;
   
               add(sizch) ;
               add(s1) ;
               add(new Label(" ", Label.CENTER)) ;
               add(chmat) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
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
   
          public void handleBar(Event evt) {     // engine size
            int i1,i3 ;
            Double V2,V3 ;
            double v1,v2,v3 ;
            float fl1,fl2,fl3 ;

            siztype = sizch.getSelectedIndex() ;

            if (siztype == 0) {
// area input
               i1 = s1.getValue() ;
               vmn1 = a2min;    vmx1 = a2max ;

               a2d = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
               diameng = Math.sqrt(4.0 * a2d / 3.14159) ;

               left.f1.setBackground(Color.white) ;
               left.f1.setForeground(Color.black) ;
               left.f3.setBackground(Color.black) ;
               left.f3.setForeground(Color.yellow) ;
            }

            if (siztype == 1) {
// diameter input
               V3 = Double.valueOf(left.f3.getText()) ;
               diameng = v3 = V3.doubleValue() ;

               a2d  = 3.14159 * diameng * diameng / 4.0 ;

               left.f1.setBackground(Color.black) ;
               left.f1.setForeground(Color.yellow) ;
               left.f3.setBackground(Color.white) ;
               left.f3.setForeground(Color.black) ;
            }

            a2 = a2d / aconv ;
            if (entype == 2) {
                afan = a2 ;
                acore = afan / (1.0+byprat) ;
            }
            else acore = a2 ;

// compute or input weight 
            wtflag = chmat.getSelectedIndex() ;
            if (wtflag == 1) {
              left.f2.setForeground(Color.black) ;
              left.f2.setBackground(Color.white) ;
              V2 = Double.valueOf(left.f2.getText()) ;
              v2 = V2.doubleValue() ;
              weight = v2 / fconv ;
              if (weight < 10.0) {
                 weight = v2 = 10.0  ;
                 fl2 = (float) v2 ;
                 left.f2.setText(String.valueOf(fl2)) ;
              }
            }
            if (wtflag == 0) {
              left.f2.setForeground(Color.yellow) ;
              left.f2.setBackground(Color.black) ;
            }

            fl1 = filter3(a2d) ;
            fl3 = filter3(diameng) ;

            left.f1.setText(String.valueOf(fl1)) ;
            left.f3.setText(String.valueOf(fl3)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3 ;
           Label l1, l2, l3, lab ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(6,2,5,5)) ;
     
              l1 = new Label("Area-sq ft", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)a2d),5) ;
              f1.setBackground(Color.white) ;
              f1.setForeground(Color.black) ;
  
              l2 = new Label("Weight-lbs", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)weight),5) ;
              f2.setBackground(Color.black) ;
              f2.setForeground(Color.yellow) ;
  
              l3 = new Label("Diameter-ft", Label.CENTER) ;
              f3 = new TextField(String.valueOf((float)diameng),5) ;
              f3.setBackground(Color.black) ;
              f3.setForeground(Color.yellow) ;
  
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(l1) ;
              add(f1) ;
              add(l3) ;
              add(f3) ;
              add(l2) ;
              add(f2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V2,V3 ;
             double v1,v2,v3 ;
             int i1,i3 ;
             float fl1,fl2,fl3 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
     // area input
             if (siztype == 0) {
                a2d  = v1 ;
                vmn1 = a2min;   vmx1 = a2max ;
                if(v1 < vmn1) {
                   a2d = v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   a2d =  v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
                fl3 = filter3(diameng) ;
                f3.setText(String.valueOf(fl3)) ;
             }
     // diameter input
             if (siztype == 1) {
                diameng  = v3 ;
                vmn1 = diamin;   vmx1 = diamax ;
                if(v3 < vmn1) {
                   diameng = v3 = vmn1 ;
                   fl3 = (float) v3 ;
                   f3.setText(String.valueOf(fl3)) ;
                }
                if(v3 > vmx1) {
                   diameng =  v3 = vmx1 ;
                   fl3 = (float) v3 ;
                   f3.setText(String.valueOf(fl3)) ;
                }
                a2d = 3.14159 * diameng * diameng / 4.0 ;
                fl1 = filter3(a2d) ;
                f1.setText(String.valueOf(fl1)) ;
              }

              a2 = a2d/aconv ;
              if (entype == 2) {
                 afan = a2 ;
                 acore = afan / (1.0 + byprat) ;
              }
              else acore = a2 ;
 
              weight = v2 / fconv ;
              if (weight < 10.0 ) {
                 weight = v2 = 10.0 ;
                 fl2 = (float) v2 ;
                 f2.setText(String.valueOf(fl2)) ;
              }
        
              i1 = (int) (((a2d - a2min)/(a2max-a2min))*1000.) ;

              right.s1.setValue(i1) ;

              solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Size input

     class Inlet extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Inlet (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1;
           Choice inltch,imat ;
           Label lmat;

           Right (Turbo target) {
    
               int i1 ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,10,5)) ;
    
               inltch = new Choice() ;
               inltch.addItem("Mil Spec Recovery") ;
               inltch.addItem("Input Recovery");
               inltch.select(0) ;
 
               i1 = (int) (((eta[2] - etmin)/(etmax-etmin))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
   
               imat = new Choice() ;
               imat.setBackground(Color.white) ;
               imat.setForeground(Color.blue) ;
               imat.addItem("<-- My Material") ;
               imat.addItem("Aluminum") ;
               imat.addItem("Titanium ");
               imat.addItem("Stainless Steel");
               imat.addItem("Nickel Alloy");
               imat.addItem("Actively Cooled");
               imat.select(1) ;
   
               lmat = new Label("lbm/ft^3 ", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(inltch) ;
               add(s1) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(imat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
           public void handleMat(Event evt) {  // materials
              Double V1,V2 ;
              double v1,v2 ;

         // inlet
               pt2flag = inltch.getSelectedIndex() ;
               if (pt2flag == 0) {
                  left.f1.setBackground(Color.black) ;
                  left.f1.setForeground(Color.yellow) ;
               }
               if (pt2flag == 1) {
                  left.f1.setBackground(Color.white) ;
                  left.f1.setForeground(Color.black) ;
               }
               minlt = imat.getSelectedIndex() ;
               if (minlt > 0) {
                  left.di.setBackground(Color.black) ;
                  left.di.setForeground(Color.yellow) ;
                  left.ti.setBackground(Color.black) ;
                  left.ti.setForeground(Color.yellow) ;
               }
               if (minlt == 0) {
                  left.di.setBackground(Color.white) ;
                  left.di.setForeground(Color.blue) ;
                  left.ti.setBackground(Color.white) ;
                  left.ti.setForeground(Color.blue) ;
               }
               switch (minlt) {
                   case 0: {
                        V1 = Double.valueOf(left.di.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.ti.getText()) ;
                        v2 = V2.doubleValue() ;
                        dinlt = v1/dconv ; 
                        tinlt = v2/tconv ;
                        break ;
                   }
                   case 1: dinlt = 170.7 ; tinlt = 900.; break ;
                   case 2: dinlt = 293.02 ; tinlt = 1500.; break ;
                   case 3: dinlt = 476.56 ; tinlt = 2000.; break ;
                   case 4: dinlt = 515.2 ; tinlt = 2500.; break ;
                   case 5: dinlt = 515.2 ; tinlt = 4000.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) {     // inlet recovery
            int i1 ;
            double v1 ;
            float fl1 ;

            i1 = s1.getValue() ;

            if (lunits <= 1) {
                vmn1 = etmin;    vmx1 = etmax ;
            }
            if (lunits == 2) {
               vmx1 = 100.0 - 100.0 * et2ref ;
               vmn1 = vmx1 - 20.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
         
            fl1 = filter3(v1) ;

            if (lunits <= 1) eta[2] = v1 ;
            if (lunits == 2) eta[2] = et2ref + v1 / 100. ;

            left.f1.setText(String.valueOf(fl1)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, ti, di ;
           Label l1, l5, lmat, lm2 ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(6,2,5,5)) ;
     
              l1 = new Label("Pres Recov.", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)eta[2]),5) ;
              f1.setBackground(Color.black) ;
              f1.setForeground(Color.yellow) ;
              lmat = new Label("T lim -R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              ti = new TextField(String.valueOf((float)tinlt),5) ;
              ti.setBackground(Color.black) ;
              ti.setForeground(Color.yellow) ;
              di = new TextField(String.valueOf((float)dinlt),5) ;
              di.setBackground(Color.black) ;
              di.setForeground(Color.yellow) ;

              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(l1) ;
              add(f1) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(ti) ;
              add(l5) ;
              add(di) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V3,V5 ;
             double v1,v3,v5 ;
             int i1 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V3 = Double.valueOf(di.getText()) ;
             v3 = V3.doubleValue() ;
             V5 = Double.valueOf(ti.getText()) ;
             v5 = V5.doubleValue() ;

     // materials
              if (minlt == 0) {
                if (v3 <= 1.0*dconv) {
                   v3 = 1.0*dconv ;
                   di.setText(String.valueOf(filter0(v3*dconv))) ;
                }
                dinlt = v3/dconv ;
                if (v5 <= 500.*tconv) {
                   v5 = 500.*tconv ;
                   ti.setText(String.valueOf(filter0(v5*tconv))) ;
                }
                tinlt = v5/tconv ;
              }
     // Inlet pressure ratio
             if (lunits <= 1) {
                eta[2]  = v1 ;
                vmn1 = etmin;   vmx1 = etmax ;
                if(v1 < vmn1) {
                   eta[2] = v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   eta[2] =  v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
              }
              if (lunits == 2) {
                vmx1 = 100.0 - 100.0 * et2ref ;
                vmn1 = vmx1 - 20.0 ;
                if(v1 < vmn1) {
                   v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                eta[2] = et2ref + v1 / 100. ;
              }
        
              i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
   
              right.s1.setValue(i1) ;

              solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Inlet panel

     class Fan extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Fan (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2,s3;
           Label lmat;
           Choice fmat;

           Right (Turbo target) {
    
               int i1, i2, i3  ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,10,5)) ;
    
               i1 = (int) (((p3fp2d - fprmin)/(fprmax-fprmin))*1000.) ;
               i2 = (int) (((eta[13] - etmin)/(etmax-etmin))*1000.) ;
               i3 = (int) (((byprat - bypmin)/(bypmax-bypmin))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
   
               fmat = new Choice() ;
               fmat.setBackground(Color.white) ;
               fmat.setForeground(Color.blue) ;
               fmat.addItem("<-- My Material") ;
               fmat.addItem("Aluminum") ;
               fmat.addItem("Titanium ");
               fmat.addItem("Stainless Steel");
               fmat.addItem("Nickel Alloy");
               fmat.addItem("Nickel Crystal");
               fmat.addItem("Ceramic");
               fmat.select(2) ;

               lmat = new Label("lbm/ft^3 ", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(s3) ;
               add(s1) ;
               add(s2) ;
               add(new Label(" ", Label.LEFT)) ;
               add(fmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
           public void handleMat(Event evt) {
              Double V1,V2 ;
              double v1,v2 ;

                // fan
               mfan = fmat.getSelectedIndex() ;
               if(mfan > 0) {
                  left.df.setBackground(Color.black) ;
                  left.df.setForeground(Color.yellow) ;
                  left.tf.setBackground(Color.black) ;
                  left.tf.setForeground(Color.yellow) ;
               }
               if (mfan == 0) {
                  left.df.setBackground(Color.white) ;
                  left.df.setForeground(Color.blue) ;
                  left.tf.setBackground(Color.white) ;
                  left.tf.setForeground(Color.blue) ;
               }
               switch (mfan) {
                   case 0: {
                        V1 = Double.valueOf(left.df.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tf.getText()) ;
                        v2 = V2.doubleValue() ;
                        dfan = v1/dconv ; 
                        tfan = v2/tconv ;
                        break ;
                   }
                   case 1: dfan = 170.7; tfan = 900.; break ;
                   case 2: dfan = 293.02 ; tfan = 1500.; break ;
                   case 3: dfan = 476.56 ; tfan = 2000.; break ;
                   case 4: dfan = 515.2 ; tfan = 2500.; break ;
                   case 5: dfan = 515.2 ; tfan = 3000.; break ;
                   case 6: dfan = 164.2 ; tfan = 3000.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) {     // fan design
            int i1, i2,i3 ;
            double v1,v2,v3 ;
            float fl1, fl2, fl3 ;

            i1 = s1.getValue() ;
            i2 = s2.getValue() ;
            i3 = s3.getValue() ;

            if (lunits <= 1) {
               vmn1 = fprmin;   vmx1 = fprmax ;
               vmn2 = etmin;    vmx2 = etmax ;
               vmn3 = bypmin;   vmx3 = bypmax ;
            }
            if (lunits == 2) {
               vmn1 = -10.0 ;  vmx1 = 10.0 ;
               vmx2 = 100.0 - 100.0 * et13ref ;
               vmn2 = vmx2 - 20.0 ;
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
            v3 = i3 * (vmx3 - vmn3)/ 1000. + vmn3 ;
         
            fl1 = (float) v1 ;
            fl2 = (float) v2 ;
            fl3 = (float) v3 ;

// fan design
            if (lunits <= 1) {
               prat[13] = p3fp2d = v1 ;
               eta[13]  = v2 ;
               byprat   = v3 ;
            }
            if (lunits == 2) {
               prat[13] = p3fp2d = v1 * fpref/100. + fpref;
               eta[13]  = et13ref +  v2 / 100. ;
               byprat   = v3* bpref/100. + bpref ;
            }
            if (entype == 2) {
                 a2 = afan = acore * (1.0+byprat) ;
                 a2d = a2 * aconv ;
            }
            diameng = Math.sqrt(4.0 * a2d / 3.14159) ;

            left.f1.setText(String.valueOf(fl1)) ;
            left.f2.setText(String.valueOf(fl2)) ;
            left.f3.setText(String.valueOf(fl3)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3 ;
           TextField df,tf;

           Label l1, l2, l3, l5, lmat, lm2 ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(6,2,5,5)) ;
     
              l1 = new Label("Press. Ratio", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)p3fp2d),5) ;
              l2 = new Label("Efficiency", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)eta[13]),5) ;
              l3 = new Label("Bypass Rat.", Label.CENTER) ;
              f3 = new TextField(String.valueOf((float)byprat),5) ;
              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              df = new TextField(String.valueOf((float)dfan),5) ;
              df.setBackground(Color.black) ;
              df.setForeground(Color.yellow) ;
              tf = new TextField(String.valueOf((float)tfan),5) ;
              tf.setBackground(Color.black) ;
              tf.setForeground(Color.yellow) ;
   
              add(l3) ;
              add(f3) ;
              add(l1) ;
              add(f1) ;
              add(l2) ;
              add(f2) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(tf) ;
              add(l5) ;
              add(df) ;
   
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
             int i1,i2,i3 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             V4 = Double.valueOf(df.getText()) ;
             v4 = V4.doubleValue() ;
             V5 = Double.valueOf(tf.getText()) ;
             v5 = V5.doubleValue() ;

             if (lunits <= 1) {
   // Fan pressure ratio
               prat[13] = p3fp2d  = v1 ;
               vmn1 = fprmin;   vmx1 = fprmax ;
               if(v1 < vmn1) {
                  prat[13] = p3fp2d = v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  prat[13] = p3fp2d =  v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
   // Fan efficiency
               eta[13] = v2 ;
               vmn2 = etmin;  vmx2 = etmax ;
               if(v2 < vmn2) {
                  eta[13] = v2 =  vmn2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               if(v2 > vmx2) {
                  eta[13] = v2 =  vmx2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
   // bypass ratio
               byprat = v3 ;
               vmn3 = bypmin;  vmx3 = bypmax ;
               if(v3 < vmn3) {
                  byprat = v3 =  vmn3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > vmx3) {
                  byprat = v3 = vmx3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
            }
            if (lunits == 2) {
   // Fan pressure ratio
               vmn1 = -10.0;   vmx1 = 10.0 ;
               if(v1 < vmn1) {
                  v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               prat[13] = p3fp2d  = v1 * fpref/100. + fpref  ;
     // Fan efficiency
               vmx2 = 100.0 - 100.0 * et13ref ;
               vmn2 = vmx2 - 20.0 ;
               if(v2 < vmn2) {
                  v2 =  vmn2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               if(v2 > vmx2) {
                  v2 =  vmx2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               eta[13] = et13ref + v2 / 100. ;
     // bypass ratio
               vmn3 = -10.0;  vmx3 = 10.0 ;
               if(v3 < vmn3) {
                  v3 =  vmn3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > vmx3) {
                  v3 = vmx3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
                }
                byprat  = v3 * bpref/100. + bpref  ;
             }
             if (entype == 2) {
                a2 = afan = acore * (1.0+byprat) ;
                a2d = a2 * aconv ;
             }
             diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
   // materials
            if (mfan == 0) {
                if (v4 <= 1.0*dconv) {
                   v4 = 1.0*dconv ;
                   df.setText(String.valueOf(filter0(v4*dconv))) ;
                }
                dfan = v4/dconv ;
                if (v5 <= 500.*tconv) {
                   v5 = 500.*tconv ;
                   tf.setText(String.valueOf(filter0(v5*tconv))) ;
                }
                tfan = v5/tconv ;
             }

             i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
             i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
             i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
   
             right.s1.setValue(i1) ;
             right.s2.setValue(i2) ;
             right.s3.setValue(i3) ;

             solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Fan

     class Comp extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Comp (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2;
           Choice stgch,cmat ;
           Label lmat ;

           Right (Turbo target) {
    
               int i1, i2 ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,10,5)) ;
    
               i1 = (int) (((p3p2d - cprmin)/(cprmax-cprmin))*1000.) ;
               i2 = (int) (((eta[3] - etmin)/(etmax-etmin))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
   
               cmat = new Choice() ;
               cmat.setBackground(Color.white) ;
               cmat.setForeground(Color.blue) ;
               cmat.addItem("<-- My Material") ;
               cmat.addItem("Aluminum") ;
               cmat.addItem("Titanium ");
               cmat.addItem("Stainless Steel");
               cmat.addItem("Nickel Alloy");
               cmat.addItem("Nickel Crystal");
               cmat.addItem("Ceramic");
               cmat.select(2) ;

               lmat = new Label("lbm/ft^3", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               stgch = new Choice() ;
               stgch.addItem("Compute # Stages") ;
               stgch.addItem("Input # Stages");
               stgch.select(0) ;
   
               add(stgch) ;
               add(s1) ;
               add(s2) ;
               add(new Label(" ", Label.LEFT)) ;
               add(cmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
           public void handleMat(Event evt) {
              Double V1,V2 ;
              double v1,v2 ;

             // compressor
               ncflag = stgch.getSelectedIndex() ;
               if (ncflag == 0) {
                  left.f3.setBackground(Color.black) ;
                  left.f3.setForeground(Color.yellow) ;
               }
               if (ncflag == 1) {
                  left.f3.setBackground(Color.white) ;
                  left.f3.setForeground(Color.black) ;
               }

               mcomp = cmat.getSelectedIndex() ;
               if(mcomp > 0) {
                  left.dc.setBackground(Color.black) ;
                  left.dc.setForeground(Color.yellow) ;
                  left.tc.setBackground(Color.black) ;
                  left.tc.setForeground(Color.yellow) ;
               }
               if (mcomp == 0) {
                  left.dc.setBackground(Color.white) ;
                  left.dc.setForeground(Color.blue) ;
                  left.tc.setBackground(Color.white) ;
                  left.tc.setForeground(Color.blue) ;
               }
               switch (mcomp) {
                   case 0: {
                        V1 = Double.valueOf(left.dc.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tc.getText()) ;
                        v2 = V2.doubleValue() ;
                        dcomp = v1/dconv ; 
                        tcomp = v2/tconv ; 
                        break ;
                   }
                   case 1: dcomp = 170.7 ; tcomp = 900.; break ;
                   case 2: dcomp = 293.02 ; tcomp = 1500.; break ;
                   case 3: dcomp = 476.56 ; tcomp = 2000.; break ;
                   case 4: dcomp = 515.2 ; tcomp = 2500.; break ;
                   case 5: dcomp = 515.2 ; tcomp = 3000.; break ;
                   case 6: dcomp = 164.2 ; tcomp = 3000.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) {  // compressor design
            int i1, i2 ;
            double v1,v2 ;
            float fl1, fl2 ;

            i1 = s1.getValue() ;
            i2 = s2.getValue() ;

            if (lunits <= 1) {
               vmn1 = cprmin;   vmx1 = cprmax ;
               vmn2 = etmin;    vmx2 = etmax ;
            }
            if (lunits == 2) {
               vmn1 = -10.0 ;  vmx1 = 10.0 ;
               vmx2 = 100.0 - 100.0 * et3ref ;
               vmn2 = vmx2 - 20.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
         
            fl1 = (float) v1 ;
            fl2 = (float) v2 ;

//  compressor design
            if (lunits <= 1) {
               prat[3] = p3p2d = v1 ;
               eta[3]  = v2 ;
            }
            if (lunits == 2) {
               prat[3] = p3p2d = v1 * cpref/100. + cpref ;
               eta[3]  = et3ref + v2 / 100.  ;
            }

            left.f1.setText(String.valueOf(fl1)) ;
            left.f2.setText(String.valueOf(fl2)) ;
 
            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3, dc, tc ;
           Label l1, l2, l5, lmat, lm2 ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(6,2,5,5)) ;
     
              l1 = new Label("Press. Ratio", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)p3p2d),5) ;
              l2 = new Label("Efficiency", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)eta[13]),5) ;
              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              f3 = new TextField(String.valueOf((int)ncomp),5) ;
              f3.setBackground(Color.black) ;
              f3.setForeground(Color.yellow) ;

              dc = new TextField(String.valueOf((float)dcomp),5) ;
              dc.setBackground(Color.black) ;
              dc.setForeground(Color.yellow) ;
              tc = new TextField(String.valueOf((float)tcomp),5) ;
              tc.setBackground(Color.black) ;
              tc.setForeground(Color.yellow) ;
   
              add(new Label("Stages ", Label.CENTER)) ;
              add(f3) ;
              add(l1) ;
              add(f1) ;
              add(l2) ;
              add(f2) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(tc) ;
              add(l5) ;
              add(dc) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V2,V4,V6 ;
             double v1,v2,v4,v6 ;
             Integer I3 ;
             int i1,i2,i3 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V4 = Double.valueOf(dc.getText()) ;
             v4 = V4.doubleValue() ;
             V6 = Double.valueOf(tc.getText()) ;
             v6 = V6.doubleValue() ;

             I3 = Integer.valueOf(f3.getText()) ;
             i3 = I3.intValue() ;

      // materials
              if (mcomp == 0) {
                if (v4 <= 1.0*dconv) {
                   v4 = 1.0*dconv ;
                   dc.setText(String.valueOf(filter0(v4*dconv))) ;
                }
                dcomp = v4/dconv ;
                if (v6 <= 500.*tconv) {
                   v6 = 500.*tconv ;
                   tc.setText(String.valueOf(filter0(v6*tconv))) ;
                }
                tcomp = v6/tconv ;
              }
      // number of stages
             if (ncflag == 1) {
                ncomp = i3 ;
                if (ncomp <= 0) {
                   ncomp = 1;
                   f3.setText(String.valueOf(ncomp)) ;
                }
             }

             if (lunits <= 1) {
      // Compressor pressure ratio
                prat[3] = p3p2d  = v1 ;
                vmn1 = cprmin;   vmx1 = cprmax ;
                if(v1 < vmn1) {
                   prat[3] = p3p2d = v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   prat[3] = p3p2d = v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
     // Compressor efficiency
                eta[3] = v2 ;
                vmn2 = etmin;  vmx2 = etmax ;
                if(v2 < vmn2) {
                   eta[3] = v2 =  vmn2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
                if(v2 > vmx2) {
                   eta[3] = v2 =  vmx2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
              }
              if (lunits == 2) {
       // Compressor pressure ratio
                 vmn1 = -10.0;   vmx1 = 10.0 ;
                 if(v1 < vmn1) {
                    v1 = vmn1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 if(v1 > vmx1) {
                    v1 = vmx1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 prat[3] = p3p2d  = v1 * cpref/100. + cpref ;
      // Compressor efficiency
                 vmx2 = 100.0 - 100.0 * et3ref ;
                 vmn2 = vmx2 - 20.0 ;
                 if(v2 < vmn2) {
                    v2 =  vmn2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
                 if(v2 > vmx2) {
                    v2 =  vmx2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
                 eta[3] = et3ref + v2 / 100. ;
             }

             i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
             i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
   
             right.s1.setValue(i1) ;
             right.s2.setValue(i2) ;

             solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Comp panel

     class Burn extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Burn (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2,s3;
           Label lmat ;
           Choice bmat,fuelch ;

           Right (Turbo target) {
    
               int i1, i2, i3  ;
   
               outerparent = target ;
               setLayout(new GridLayout(7,1,10,5)) ;
    
               i1 = (int) (((tt4d - t4min)/(t4max-t4min))*1000.) ;
               i2 = (int) (((eta[4] - etmin)/(etmax-etmin))*1000.) ;
               i3 = (int) (((prat[4] - etmin)/(etmax-etmin))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
   
               bmat = new Choice() ;
               bmat.setBackground(Color.white) ;
               bmat.setForeground(Color.blue) ;
               bmat.addItem("<-- My Material") ;
               bmat.addItem("Aluminum") ;
               bmat.addItem("Titanium ");
               bmat.addItem("Stainless Steel");
               bmat.addItem("Nickel Alloy");
               bmat.addItem("Nickel Crystal");
               bmat.addItem("Ceramic");
               bmat.addItem("Actively Cooled");
               bmat.select(4) ;
 
               fuelch = new Choice() ;
               fuelch.addItem("Jet - A") ;
               fuelch.addItem("Hydrogen");
               fuelch.addItem("<-- Your Fuel");
               fuelch.select(0) ;

               lmat = new Label("lbm/ft^3 ", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(fuelch) ;
               add(s1) ;
               add(s3) ;
               add(s2) ;
               add(new Label(" ", Label.LEFT)) ;
               add(bmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
          public void handleMat(Event evt) {
              Double V1,V2 ;
              double v1,v2 ;
              float fl1;

               fueltype = fuelch.getSelectedIndex() ;
                if (fueltype == 0) fhv = 18600. ;
                if (fueltype == 1) fhv = 49900. ;
                left.f4.setBackground(Color.black) ;
                left.f4.setForeground(Color.yellow) ;
                fhvd = fhv * flconv ;
                fl1 = (float) (fhvd) ;
                left.f4.setText(String.valueOf(filter0(fhvd))) ;

                if (fueltype == 2) {
                   left.f4.setBackground(Color.white) ;
                   left.f4.setForeground(Color.black) ;
                }

                // burner
               mburner = bmat.getSelectedIndex() ;
               if(mburner > 0) {
                  left.db.setBackground(Color.black) ;
                  left.db.setForeground(Color.yellow) ;
                  left.tb.setBackground(Color.black) ;
                  left.tb.setForeground(Color.yellow) ;
               }
               if (mburner == 0) {
                  left.db.setBackground(Color.white) ;
                  left.db.setForeground(Color.blue) ;
                  left.tb.setBackground(Color.white) ;
                  left.tb.setForeground(Color.blue) ;
               }
               switch (mburner) {
                   case 0: {
                        V1 = Double.valueOf(left.db.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tb.getText()) ;
                        v2 = V2.doubleValue() ;
                        dburner = v1/dconv ; 
                        tburner = v2/tconv ; 
                        break ;
                   }
                   case 1: dburner = 170.7 ; tburner = 900.; break ;
                   case 2: dburner = 293.02 ; tburner = 1500.; break ;
                   case 3: dburner = 476.56 ; tburner = 2000.; break ;
                   case 4: dburner = 515.2 ; tburner = 2500.; break ;
                   case 5: dburner = 515.2 ; tburner = 3000.; break ;
                   case 6: dburner = 164.2 ; tburner = 3000.; break ;
                   case 7: dburner = 515.2 ; tburner = 4500.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) {     // burner design
            int i1, i2,i3 ;
            double v1,v2,v3 ;
            float fl1, fl2, fl3 ;

            i1 = s1.getValue() ;
            i2 = s2.getValue() ;
            i3 = s3.getValue() ;

            if (lunits <= 1) {
               vmn1 = t4min;    vmx1 = t4max ;
               vmn2 = etmin;    vmx2 = etmax ;
               vmn3 = etmin;    vmx3 = etmax ;
            }
            if (lunits == 2) {
               vmn1 = -10.0 ;  vmx1 = 10.0 ;
               vmx2 = 100.0 - 100.0 * et4ref ;
               vmn2 = vmx2 - 20.0 ;
               vmx3 = 100.0 - 100.0 * p4ref ;
               vmn3 = vmx3 - 20.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
            v3 = i3 * (vmx3 - vmn3)/ 1000. + vmn3 ;
         
            fl1 = (float) v1 ;
            fl2 = (float) v2 ;
            fl3 = (float) v3 ;
// burner design
            if (lunits <= 1) {
               tt4d = v1 ;
               eta[4]  = v2 ;
               prat[4] = v3 ;
            }
            if (lunits == 2) {
               tt4d = v1 * t4ref/100. + t4ref ;
               eta[4]  = et4ref + v2 / 100. ;
               prat[4] = p4ref + v3 / 100.  ;
            }
            tt4 = tt4d/tconv  ;

            left.f1.setText(String.valueOf(fl1)) ;
            left.f2.setText(String.valueOf(fl2)) ;
            left.f3.setText(String.valueOf(fl3)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3, f4 ;
           Label l1, l2, l3, l4, l5, lmat, lm2 ;
           TextField db, tb;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(7,2,5,5)) ;
     
              l1 = new Label("Tmax -R", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)tt4d),5) ;
              l2 = new Label("Efficiency", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)eta[4]),5) ;
              l3 = new Label("Press. Ratio", Label.CENTER) ;
              f3 = new TextField(String.valueOf((float)prat[4]),5) ;
              l4 = new Label("FHV Btu/lb", Label.CENTER) ;
              f4 = new TextField(String.valueOf((float)fhv),5) ;
              f4.setBackground(Color.black) ;
              f4.setForeground(Color.yellow) ;

              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              db = new TextField(String.valueOf((float)dburner),5) ;
              db.setBackground(Color.black) ;
              db.setForeground(Color.yellow) ;
              tb = new TextField(String.valueOf((float)tburner),5) ;
              tb.setBackground(Color.black) ;
              tb.setForeground(Color.yellow) ;
      
              add(l4) ;
              add(f4) ;
              add(l1) ;
              add(f1) ;
              add(l3) ;
              add(f3) ;
              add(l2) ;
              add(f2) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(tb) ;
              add(l5) ;
              add(db) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V2,V3,V4,V5,V6 ;
             double v1,v2,v3,v4,v5,v6 ;
             int i1,i2,i3 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             V6 = Double.valueOf(f4.getText()) ;
             v6 = V6.doubleValue() ;
             V4 = Double.valueOf(db.getText()) ;
             v4 = V4.doubleValue() ;
             V5 = Double.valueOf(tb.getText()) ;
             v5 = V5.doubleValue() ;

     // Materials
             if (mburner == 0) {
                if (v4 <= 1.0*dconv) {
                   v4 = 1.0*dconv ;
                   db.setText(String.valueOf(filter0(v4*dconv))) ;
                }
                dburner = v4/dconv ;
                if (v5 <= 500.*tconv) {
                   v5 = 500.*tconv ;
                   tb.setText(String.valueOf(filter0(v5*tconv))) ;
                }
                tburner = v5/tconv ;
             }

             if (lunits <= 1) {
     // Max burner temp
                 tt4d  = v1 ;
                 vmn1 = t4min;   vmx1 = t4max ;
                 if(v1 < vmn1) {
                    tt4d  = v1 = vmn1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 if(v1 > vmx1) {
                    tt4d  = v1 = vmx1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 tt4 = tt4d/tconv ;
     // burner  efficiency
                 eta[4] = v2 ;
                 vmn2 = etmin;  vmx2 = etmax ;
                 if(v2 < vmn2) {
                    eta[4] = v2 =  vmn2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
                 if(v2 > vmx2) {
                    eta[4] = v2 =  vmx2 ;
                    fl1 = (float) v2 ;
                    f2.setText(String.valueOf(fl1)) ;
                 }
     //  burner pressure ratio
                 prat[4] = v3 ;
                 vmn3 = etmin;  vmx3 = etmax ;
                 if(v3 < vmn3) {
                    prat[4] = v3 =  vmn3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
                 if(v3 > vmx3) {
                    prat[4] = v3 =  vmx3 ;
                    fl1 = (float) v3 ;
                    f3.setText(String.valueOf(fl1)) ;
                 }
     // fuel heating value
                 if (fueltype == 2) {
                    fhvd = v6 ;
                    fhv = fhvd / flconv ;
                 }
             }

             if (lunits == 2) {
     // Max burner temp
               vmn1 = -10.0;   vmx1 = 10.0 ;
               if(v1 < vmn1) {
                  v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               tt4d  = v1 * t4ref/100. + t4ref ;
               tt4 = tt4d/tconv ;
     // burner  efficiency
               vmx2 = 100.0 - 100.0 * et4ref ;
               vmn2 = vmx2 - 20.0 ;
               if(v2 < vmn2) {
                  v2 =  vmn2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               if(v2 > vmx2) {
                  v2 =  vmx2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               eta[4] = et4ref + v2 / 100. ;
     //  burner pressure ratio
               vmx3 = 100.0 - 100.0 * p4ref ;
               vmn3 = vmx3 - 20.0 ;
               if(v3 < vmn3) {
                  v3 =  vmn3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > vmx3) {
                  v3 =  vmx3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               prat[4] = p4ref + v3 / 100.  ;
             }

             i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
             i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
             i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
   
             right.s1.setValue(i1) ;
             right.s2.setValue(i2) ;
             right.s3.setValue(i3) ;

             solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Burn

     class Turb extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Turb (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1;
           Label lmat ;
           Choice tmat,stgch ;

           Right (Turbo target) {
    
               int i1 ;
   
               outerparent = target ;
               setLayout(new GridLayout(6,1,10,5)) ;
    
               i1 = (int) (((eta[5] - etmin)/(etmax-etmin))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
   
               stgch = new Choice() ;
               stgch.addItem("Compute # Stages") ;
               stgch.addItem("Input # Stages");
               stgch.select(0) ;
   
               tmat = new Choice() ;
               tmat.setBackground(Color.white) ;
               tmat.setForeground(Color.blue) ;
               tmat.addItem("<-- My Material") ;
               tmat.addItem("Aluminum") ;
               tmat.addItem("Titanium ");
               tmat.addItem("Stainless Steel");
               tmat.addItem("Nickel Alloy");
               tmat.addItem("Nickel Crystal");
               tmat.addItem("Ceramic");
               tmat.select(4) ;
   
               lmat = new Label("lbm/ft^3", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(stgch) ;
               add(s1) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(tmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
          public void handleMat(Event evt) {
              Double V1,V2 ;
              double v1,v2 ;

              ntflag = stgch.getSelectedIndex() ;
              if (ntflag == 0) {
                 left.f3.setBackground(Color.black) ;
                 left.f3.setForeground(Color.yellow) ;
              }
              if (ntflag == 1) {
                 left.f3.setBackground(Color.white) ;
                 left.f3.setForeground(Color.black) ;
              }
                // turnine
              mturbin = tmat.getSelectedIndex() ;
              if(mturbin > 0) {
                  left.dt.setBackground(Color.black) ;
                  left.dt.setForeground(Color.yellow) ;
                  left.tt.setBackground(Color.black) ;
                  left.tt.setForeground(Color.yellow) ;
              }
              if (mturbin == 0) {
                  left.dt.setBackground(Color.white) ;
                  left.dt.setForeground(Color.blue) ;
                  left.tt.setBackground(Color.white) ;
                  left.tt.setForeground(Color.blue) ;
              }
              switch (mturbin) {
                   case 0: {
                        V1 = Double.valueOf(left.dt.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tt.getText()) ;
                        v2 = V2.doubleValue() ;
                        dturbin = v1/dconv ; 
                        tturbin = v2/tconv ; 
                        break ;
                   }
                   case 1: dturbin = 170.7 ; tturbin = 900.; break ;
                   case 2: dturbin = 293.02 ; tturbin = 1500.; break ;
                   case 3: dturbin = 476.56 ; tturbin = 2000.; break ;
                   case 4: dturbin = 515.2 ; tturbin = 2500.; break ;
                   case 5: dturbin = 515.2 ; tturbin = 3000.; break ;
                   case 6: dturbin = 164.2 ; tturbin = 3000.; break ;
              }
              solve.comPute() ;
          }

          public void handleBar(Event evt) {     // turbine
            int i1 ;
            double v1 ;
            float fl1 ;

            i1 = s1.getValue() ;

            if (lunits <= 1) {
               vmn1 = etmin;    vmx1 = etmax ;
            }
            if (lunits == 2) {
               vmx1 = 100.0 - 100.0 * et5ref ;
               vmn1 = vmx1 - 20.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
         
            fl1 = (float) v1 ;

            if (lunits <= 1) {
               eta[5]  = v1 ;
            }
            if (lunits == 2) {
               eta[5]  = et5ref + v1 / 100.  ;
            }
 
            left.f1.setText(String.valueOf(fl1)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1,f3,dt,tt ;
           Label l1, l5, lmat, lm2;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(6,2,5,5)) ;
     
              f3 = new TextField(String.valueOf((int)nturb),5) ;
              f3.setBackground(Color.black) ;
              f3.setForeground(Color.yellow) ;

              l1 = new Label("Efficiency", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)eta[5]),5) ;
              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
  
              dt = new TextField(String.valueOf((float)dturbin),5) ;
              dt.setBackground(Color.black) ;
              dt.setForeground(Color.yellow) ;
              tt = new TextField(String.valueOf((float)tturbin),5) ;
              tt.setBackground(Color.black) ;
              tt.setForeground(Color.yellow) ;
      
              add(new Label("Stages ", Label.CENTER)) ;
              add(f3) ;
              add(l1) ;
              add(f1) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(tt) ;
              add(l5) ;
              add(dt) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V4,V8 ;
             double v1,v4,v8 ;
             Integer I3 ;
             int i1,i3 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V4 = Double.valueOf(dt.getText()) ;
             v4 = V4.doubleValue() ;
             V8 = Double.valueOf(tt.getText()) ;
             v8 = V8.doubleValue() ;

             I3 = Integer.valueOf(f3.getText()) ;
             i3 = I3.intValue() ;
     // number of stages
             if (ntflag == 1 && i3 >= 1) {
                nturb = i3 ;
             }
     // materials
             if (mturbin == 0) {
               if (v4 <= 1.0*dconv) {
                  v4 = 1.0*dconv ;
                  dt.setText(String.valueOf(filter0(v4*dconv))) ;
               }
               dturbin = v4/dconv ;
               if (v8 <= 500.*tconv) {
                  v8 = 500.*tconv ;
                  tt.setText(String.valueOf(filter0(v8*tconv))) ;
               }
               tturbin = v8/tconv ;
             }
     // turbine efficiency
             if (lunits <= 1) {
                eta[5]  = v1 ;
                vmn1 = etmin;   vmx1 = etmax ;
                if(v1 < vmn1) {
                   eta[5] = v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   eta[5] =  v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
              }
              if (lunits == 2) {
      // Turbine efficiency
                 vmx1 = 100.0 - 100.0 * et5ref ;
                 vmn1 = vmx1 - 20.0 ;
                 if(v1 < vmn1) {
                    v1 =  vmn1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 if(v1 > vmx1) {
                    v1 =  vmx1 ;
                    fl1 = (float) v1 ;
                    f1.setText(String.valueOf(fl1)) ;
                 }
                 eta[5] = et5ref + v1 / 100. ;
              }
        
              i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
   
              right.s1.setValue(i1) ;

              solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end Turb

     class Nozl extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Nozl (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2,s3;
           Label lmat ;
           Choice arch, nmat ;

           Right (Turbo target) {
    
               int i1, i2, i3  ;
   
               outerparent = target ;
               setLayout(new GridLayout(7,1,10,5)) ;
    
               i1 = (int) (((tt7d - t7min)/(t7max-t7min))*1000.) ;
               i2 = (int) (((eta[7] - etmin)/(etmax-etmin))*1000.) ;
               i3 = (int) (((a8rat - a8min)/(a8max-a8min))*1000.) ;
   
               s1 = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
   
               nmat = new Choice() ;
               nmat.setBackground(Color.white) ;
               nmat.setForeground(Color.blue) ;
               nmat.addItem("<-- My Material") ;
               nmat.addItem("Titanium ");
               nmat.addItem("Stainless Steel");
               nmat.addItem("Nickel Alloy");
               nmat.addItem("Ceramic");
               nmat.addItem("Passively Cooled");
               nmat.select(3) ;
   
               arch = new Choice() ;
               arch.addItem("Compute A8/A2") ;
               arch.addItem("Input A8/A2");
               arch.select(0) ;
 
               lmat = new Label("lbm/ft^3 ", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(arch) ;
               add(s3) ;
               add(s1) ;
               add(s2) ;
               add(new Label(" ", Label.LEFT)) ;
               add(nmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
          public void handleMat(Event evt) {
               Double V1,V2 ;
               double v1,v2 ;

               arsched = arch.getSelectedIndex() ;
               if (arsched == 0) {
                  left.f3.setBackground(Color.black) ;
                  left.f3.setForeground(Color.yellow) ;
               }
               if (arsched == 1) {
                  left.f3.setBackground(Color.white) ;
                  left.f3.setForeground(Color.black) ;
               }
                // nozzle
               mnozl = nmat.getSelectedIndex() ;
               if(mnozl > 0) {
                  left.tn.setBackground(Color.black) ;
                  left.tn.setForeground(Color.yellow) ;
                  left.dn.setBackground(Color.black) ;
                  left.dn.setForeground(Color.yellow) ;
               }
               if (mnozl == 0) {
                  left.tn.setBackground(Color.white) ;
                  left.tn.setForeground(Color.black) ;
                  left.dn.setBackground(Color.white) ;
                  left.dn.setForeground(Color.black) ;
               }
               switch (mnozl) {
                   case 0: {
                        V1 = Double.valueOf(left.dn.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tn.getText()) ;
                        v2 = V2.doubleValue() ;
                        dnozl = v1/dconv ; 
                        tnozl = v2/tconv ; 
                        break ;
                   }
                   case 1: dnozl = 293.02 ; tnozl = 1500.; break ;
                   case 2: dnozl = 476.56 ; tnozl = 2000.; break ;
                   case 3: dnozl = 515.2 ; tnozl = 2500.; break ;
                   case 4: dnozl = 164.2 ; tnozl = 3000.; break ;
                   case 5: dnozl = 400.2 ; tnozl = 4100.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) {     // nozzle design
            int i1, i2,i3 ;
            double v1,v2,v3 ;
            float fl1, fl2, fl3 ;

            i1 = s1.getValue() ;
            i2 = s2.getValue() ;
            i3 = s3.getValue() ;

            if (lunits <= 1) {
               vmn1 = t7min;    vmx1 = t7max ;
               vmn2 = etmin;    vmx2 = etmax ;
               vmn3 = a8min;    vmx3 = a8max ;
            }
            if (lunits == 2) {
               vmn1 = -10.0 ;  vmx1 = 10.0 ;
               vmx2 = 100.0 - 100.0 * et7ref ;
               vmn2 = vmx2 - 20.0 ;
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
            }

            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
            v3 = i3 * (vmx3 - vmn3)/ 1000. + vmn3 ;
         
            fl1 = (float) v1 ;
            fl2 = (float) v2 ;
            fl3 = filter3(v3) ;

// nozzle design
            if (lunits <= 1) {
               tt7d = v1 ;
               eta[7]  = v2 ;
               a8rat = v3 ;
            }
            if (lunits == 2) {
               tt7d = v1 * t7ref/100. + t7ref ;
               eta[7]  = et7ref + v2 / 100. ;
               a8rat = v3 * a8ref/100. + a8ref  ;
            }
            tt7 = tt7d/tconv  ;

            left.f1.setText(String.valueOf(fl1)) ;
            left.f2.setText(String.valueOf(fl2)) ;
            left.f3.setText(String.valueOf(fl3)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3, dn, tn ;
           Label l1, l2, l3, l5, lmat, lm2 ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(7,2,5,5)) ;
     
              l1 = new Label("Tmax -R", Label.CENTER) ;
              f1 = new TextField(String.valueOf((float)tt7d),5) ;

              l2 = new Label("Efficiency", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)eta[7]),5) ;

              f3 = new TextField(String.valueOf((float)a8rat),5) ;
              f3.setBackground(Color.black) ;
              f3.setForeground(Color.yellow) ;

              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              dn = new TextField(String.valueOf((float)dnozl),5) ;
              dn.setBackground(Color.black) ;
              dn.setForeground(Color.yellow) ;
              tn = new TextField(String.valueOf((float)tnozl),5) ;
              tn.setBackground(Color.black) ;
              tn.setForeground(Color.yellow) ;
      
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;

              add(new Label("A8/A2 ", Label.CENTER)) ;
              add(f3) ;

              add(l1) ;
              add(f1) ;

              add(l2) ;
              add(f2) ;

              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;

              add(lmat) ;
              add(tn) ;

              add(l5) ;
              add(dn) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V1,V2,V3,V7,V8 ;
             double v1,v2,v3,v7,v8 ;
             int i1,i2,i3 ;
             float fl1 ;

             V1 = Double.valueOf(f1.getText()) ;
             v1 = V1.doubleValue() ;
             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             V7 = Double.valueOf(dn.getText()) ;
             v7 = V7.doubleValue() ;
             V8 = Double.valueOf(tn.getText()) ;
             v8 = V8.doubleValue() ;

    // Materials
             if (mnozl == 0) {
                if (v7 <= 1.0*dconv) {
                   v7 = 1.0*dconv ;
                   dn.setText(String.valueOf(filter0(v7*dconv))) ;
                }
                dnozl = v7/dconv ;
                if (v8 <= 500.*tconv) {
                   v8 = 500.*tconv ;
                   tn.setText(String.valueOf(filter0(v8*tconv))) ;
                }
                tnozl = v8/tconv ;
             }

             if (lunits <= 1) {
    // Max afterburner temp
                tt7d  = v1 ;
                vmn1 = t7min;   vmx1 = t7max ;
                if(v1 < vmn1) {
                   tt7d  = v1 = vmn1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                if(v1 > vmx1) {
                   tt7d  = v1 = vmx1 ;
                   fl1 = (float) v1 ;
                   f1.setText(String.valueOf(fl1)) ;
                }
                tt7 = tt7d/tconv ;
    // nozzle  efficiency
                eta[7] = v2 ;
                vmn2 = etmin;  vmx2 = etmax ;
                if(v2 < vmn2) {
                   eta[7] = v2 =  vmn2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
                if(v2 > vmx2) {
                   eta[7] = v2 =  vmx2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
    //  nozzle area ratio
                a8rat = v3 ;
                vmn3 = a8min;  vmx3 = a8max ;
                if(v3 < vmn3) {
                   a8rat = v3 =  vmn3 ;
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                }
                if(v3 > vmx3) {
                   a8rat = v3 =  vmx3 ;
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                }
              }
              if (lunits == 2) {
    // Max afterburner temp
               vmn1 = -10.0;   vmx1 = 10.0 ;
               if(v1 < vmn1) {
                  v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  f1.setText(String.valueOf(fl1)) ;
               }
               tt7d  = v1 * t7ref/100. + t7ref ;
               tt7 = tt7d/tconv ;
    // nozzl e  efficiency
               vmx2 = 100.0 - 100.0 * et7ref ;
               vmn2 = vmx2 - 20.0 ;
               if(v2 < vmn2) {
                  v2 =  vmn2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               if(v2 > vmx2) {
                  v2 =  vmx2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               eta[7] = et7ref + v2 / 100. ;
     //  nozzle area ratio
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
               if(v3 < vmn3) {
                  v3 =  vmn3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > vmx3) {
                  v3 =  vmx3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               a8rat  = v3 * a8ref/100. + a8ref ;
             }

             i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
             i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
             i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
   
             right.s1.setValue(i1) ;
             right.s2.setValue(i2) ;
             right.s3.setValue(i3) ;

             solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end nozl

     class Plot extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Plot (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Button takbt ;
           Scrollbar splt;

           Right (Turbo target) {
    
               int i1 ;
   
               outerparent = target ;
               setLayout(new GridLayout(5,1,10,5)) ;
    
               takbt = new Button("Take Data") ;
               takbt.setBackground(Color.blue) ;
               takbt.setForeground(Color.white) ;

               i1 = (int) (((u0d - vmn1)/(vmx1-vmn1))*1000.) ;
   
               splt = new Scrollbar(Scrollbar.HORIZONTAL,i1,10,0,1000);
               splt.setBackground(Color.white) ;
               splt.setForeground(Color.red) ;
 
               add(takbt) ;
               add(new Label(" ", Label.CENTER)) ;
               add(new Label(" ", Label.CENTER)) ;
               add(splt) ;
               add(new Label(" ", Label.CENTER)) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleBut(evt) ;
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
   
          public void handleBar(Event evt) {     //  generate plot
            int i1 ;
            double v1 ;
            float fl1 ;

            i1 = splt.getValue() ;
            if (nabs == 3) {  //  speed
                vmn1 = u0min;   vmx1 = u0max ;
            }
            if (nabs == 4) {  //  altitude
                vmn2 = altmin;  vmx2 = altmax ;
            }
            if (nabs == 5) {  //  throttle
                vmn3 = thrmin;  vmx3 = thrmax ;
            }
            if (nabs == 6) {  //  cpr
                vmn1 = cprmin;   vmx1 = cprmax ;
            }
            if (nabs == 7) {  // burner temp
                vmn1 = t4min;    vmx1 = t4max ;
            }
            v1 = i1 * (vmx1 - vmn1)/ 1000. + vmn1 ;
            fl1 = (float) v1 ;
            if (nabs == 3) u0d = v1 ;
            if (nabs == 4) altd = v1 ;
            if (nabs == 5) throtl = v1 ;
            if (nabs == 6) prat[3] = p3p2d = v1 ;
            if (nabs == 7) {
                tt4d = v1 ;
                tt4 = tt4d/tconv ;
            }
            left.fplt.setText(String.valueOf(fl1)) ;

            solve.comPute() ; 

            switch (nord) {
                case 3: fl1 = (float) fnlb; break ;
                case 4: fl1 = (float) flflo; break ;
                case 5: fl1 = (float) sfc; break ;
                case 6: fl1 = (float) epr  ; break ;
                case 7: fl1 = (float) etr ; break ;
            }
            left.oplt.setText(String.valueOf(fl1)) ;

          }  // end handle

          public void handleBut(Event evt) {     //  generate plot
             if (npt == 25 ) return ;
             ++npt ;
             switch (nord) {
                 case 3: plty[npt] = fnlb; break ;
                 case 4: plty[npt] = flflo; break ;
                 case 5: plty[npt] = sfc; break ;
                 case 6: plty[npt] = epr  ; break ;
                 case 7: plty[npt] = etr ; break ;
             }
             switch (nabs) {
                 case 3: pltx[npt] = fsmach; break ;
                 case 4: pltx[npt] = alt ; break ;
                 case 5: pltx[npt] = throtl ; break ;
                 case 6: pltx[npt] = prat[3] ; break ;
                 case 7: pltx[npt] = tt[4] ; break ;
             }
      
             out.plot.repaint() ;
          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField fplt, oplt ;
           Button strbt, endbt, exitpan ;
           Choice absch, ordch ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(5,2,5,5)) ;
     
              strbt = new Button("Begin") ;
              strbt.setBackground(Color.blue) ;
              strbt.setForeground(Color.white) ;
              endbt = new Button("End") ;
              endbt.setBackground(Color.blue) ;
              endbt.setForeground(Color.white) ;

              ordch = new Choice() ;
              ordch.addItem("Fn") ;
              ordch.addItem("Fuel");
              ordch.addItem("SFC");
              ordch.addItem("EPR");
              ordch.addItem("ETR");
              ordch.select(0) ;
              ordch.setBackground(Color.red) ;
              ordch.setForeground(Color.white) ;
 
              oplt = new TextField(String.valueOf(fnlb),5) ;
              oplt.setBackground(Color.black) ;
              oplt.setForeground(Color.yellow) ;
  
              absch = new Choice() ;
              absch.addItem("Speed") ;
              absch.addItem("Altitude ");
              absch.addItem("Throttle");
              absch.addItem(" CPR   ");
              absch.addItem("Temp 4");
              absch.select(0) ;
              absch.setBackground(Color.red) ;
              absch.setForeground(Color.white) ;

              fplt = new TextField(String.valueOf(u0d),5) ;
              fplt.setBackground(Color.white) ;
              fplt.setForeground(Color.red) ;

              exitpan = new Button("Exit") ;
              exitpan.setBackground(Color.red) ;
              exitpan.setForeground(Color.white) ;

              add(strbt) ;  
              add(endbt) ;  

              add(ordch) ;  
              add(oplt) ;
 
              add(new Label("vs ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;

              add(absch) ;
              add(fplt) ;

              add(exitpan) ;
              add(new Label(" ", Label.CENTER)) ;
           }
     
           public boolean action(Event evt, Object arg) {
               if(evt.target instanceof Button) {
                  this.handlePlot(arg) ;
                  return true ;
               }
               if(evt.target instanceof Choice) {
                  this.handlePlot(arg) ;
                  return true ;
               }
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleText(evt) ;
                  return true ;
               }
               else return false ;
           }

           public void handlePlot(Object arg) {
             String label = (String)arg ;
             int item,i;
             double tempx,tempy;
             double v1 ;
             int i1 ;
             float fl1 ;
      
              nord = 3 + ordch.getSelectedIndex();
              if (nord != ordkeep) {  // set the plot parameters
                if (nord == 3) {  // Thrust 
                   laby = String.valueOf("Fn");
                   labyu = String.valueOf("lb");
                   begy= 0.0 ; endy = 100000.0; ntiky = 11 ;
                }
                if (nord == 4) {  //  Fuel
                   laby = String.valueOf("Fuel Rate");
                   labyu = String.valueOf("lbs/hr");
                   begy= 0.0 ; endy = 100000.0; ntiky = 11 ;
                }
                if (nord == 5) {  //  TSFC
                   laby = String.valueOf("TSFC");
                   labyu = String.valueOf("lbm/hr/lb");
                   begy= 0.0 ; endy = 2.0; ntiky = 11 ;
                }
                if (nord == 6) {  //  EPR
                   laby = String.valueOf("EPR");
                   labyu = String.valueOf(" ");
                   begy= 0.0 ; endy = 50.0; ntiky = 11 ;
                }
                if (nord == 7) {  //  ETR
                   laby = String.valueOf("ETR");
                   labyu = String.valueOf(" ");
                   begy= 0.0 ; endy = 50.0; ntiky = 11 ;
                }
                ordkeep = nord ;
                npt = 0 ;
                lines = 0 ;
              }

              nabs = 3 + absch.getSelectedIndex();
               v1 = u0d ;
               if (nabs != abskeep) {  // set the plot parameters
                if (nabs == 3) {  //  speed
                   labx = String.valueOf("Mach");
                   labxu = String.valueOf(" ");
                   if (entype <=2) {
                     begx= 0.0 ; endx = 2.0; ntikx = 5 ;
                   }
                   if (entype ==3) {
                     begx= 0.0 ; endx = 6.0; ntikx = 5 ;
                   }
                   v1 = u0d ;
                   vmn1 = u0min;   vmx1 = u0max ;
                }
                if (nabs == 4) {  //  altitude
                   labx = String.valueOf("Alt");
                   labxu = String.valueOf("ft");
                   begx= 0.0 ; endx = 60000.0; ntikx = 4 ;
                   v1 = altd ;
                   vmn1 = altmin;  vmx1 = altmax ;
                }
                if (nabs == 5) {  //  throttle
                   labx = String.valueOf("Throttle");
                   labxu = String.valueOf(" %");
                   begx= 0.0 ; endx = 100.0; ntikx = 5 ;
                   v1 = throtl ;
                   vmn1 = thrmin;  vmx1 = thrmax ;
                }
                if (nabs == 6) {  //  Compressor pressure ratio
                   labx = String.valueOf("CPR");
                   labxu = String.valueOf(" ");
                   begx= 0.0 ; endx = 50.0; ntikx = 6 ;
                   v1 = p3p2d ;
                   vmn1 = cprmin;   vmx1 = cprmax ;
                }
                if (nabs == 7) {  // Burner temp
                   labx = String.valueOf("Temp");
                   labxu = String.valueOf("R");
                   begx= 1000.0 ; endx = 4000.0; ntikx = 4 ;
                   v1 = tt4d ;
                   vmn1 = t4min;   vmx1 = t4max ;
                }
                fl1 = (float) v1 ;
                fplt.setText(String.valueOf(fl1)) ;
                i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
                right.splt.setValue(i1) ;
                abskeep = nabs ;
                npt = 0 ;
                lines = 0 ;
              }

              if (label.equals("Begin")) {
                npt = 0 ;
                lines = 0 ;
              }
      
              if (label.equals("End")) {
                lines = 1 ;
                for (item=1; item<=npt-1; ++item) {
                  for (i=item+1; i<=npt; ++i) {
                     if (pltx[i] < pltx[item]) {
                          tempx = pltx[item];
                          tempy = plty[item];
                          pltx[item] = pltx[i];
                          plty[item] = plty[i];
                          pltx[i] = tempx;
                          plty[i] = tempy;
                      }
                  }
                }
              }
   
              if (label.equals("Exit")) {
                 varflag = 0 ;
                 layin.show(in, "first")  ;
                 con.up.pltch.select(1) ;
                 solve.loadMine() ;
                 plttyp = 3 ;
                 con.setPlot() ;
                 con.up.untch.select(0) ;
                 con.up.engch.select(0) ;
                 con.up.modch.select(inflag) ;
              }
      
              solve.comPute() ;
           }
       
           public void handleText(Event evt) {
             Double V1 ;
             double v1 ;
             int i1 ;
             float fl1 ;
  
             V1 = Double.valueOf(fplt.getText()) ;
             v1 = V1.doubleValue() ;
             fl1 = (float) v1 ;
             if (nabs == 3) {  //  speed
               u0d = v1 ;
               vmn1 = u0min;   vmx1 = u0max ;
               if(v1 < vmn1) {
                  u0d = v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  u0d = v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
             }
             if (nabs == 4) {  //  altitude
               altd = v1 ;
               vmn1 = altmin;  vmx1 = altmax ;
               if(v1 < vmn1) {
                  altd = v1 =  vmn1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  altd = v1 =  vmx1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
             }
             if (nabs == 5) {  //  throttle
               throtl = v1 ;
               vmn1 = thrmin;  vmx1 = thrmax ;
               if(v1 < vmn1) {
                  throtl = v1 =  vmn1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  throtl = v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
             }
             if (nabs == 6) {  //  Compressor pressure ratio
               prat[3] = p3p2d  = v1 ;
               vmn1 = cprmin;   vmx1 = cprmax ;
               if(v1 < vmn1) {
                  prat[3] = p3p2d = v1 = vmn1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
               if(v1 > vmx1) {
                  prat[3] = p3p2d = v1 = vmx1 ;
                  fl1 = (float) v1 ;
                  fplt.setText(String.valueOf(fl1)) ;
               }
             }
             if (nabs == 7) {  // Burner temp
                  tt4d  = v1 ;
                  vmn1 = t4min;   vmx1 = t4max ;
                  if(v1 < vmn1) {
                     tt4d  = v1 = vmn1 ;
                     fl1 = (float) v1 ;
                     fplt.setText(String.valueOf(fl1)) ;
                  }
                  if(v1 > vmx1) {
                     tt4d  = v1 = vmx1 ;
                     fl1 = (float) v1 ;
                     fplt.setText(String.valueOf(fl1)) ;
                  }
                  tt4 = tt4d/tconv ;
             }
             i1 = (int) (((v1 - vmn1)/(vmx1-vmn1))*1000.) ;
             right.splt.setValue(i1) ;

             solve.comPute() ;

             switch (nord) {
                case 3: fl1 = (float) fnlb; break ;
                case 4: fl1 = (float) flflo; break ;
                case 5: fl1 = (float) sfc; break ;
                case 6: fl1 = (float) epr  ; break ;
                case 7: fl1 = (float) etr ; break ;
             }
             oplt.setText(String.valueOf(fl1)) ;

           }  // end handle
         }  //  end  left
     }  // end Plot

     class Nozr extends Panel {
        Turbo outerparent ;
        Right right ;
        Left left ;

        Nozr (Turbo target) {
                               
          outerparent = target ;
          setLayout(new GridLayout(1,2,10,10)) ;

          left = new Left(outerparent) ; 
          right = new Right(outerparent) ;
 
          add(left) ;
          add(right) ;
        }

        public Insets insets() {
           return new Insets(5,0,5,0) ;
        }
 
        class Right extends Panel {
           Turbo outerparent ;
           Scrollbar s1,s2,s3,s4;
           Label lmat ;
           Choice nrmat ;
           Choice atch,aech ;

           Right (Turbo target) {
    
               int  i2, i3, i4  ;
   
               outerparent = target ;
               setLayout(new GridLayout(7,1,10,5)) ;
    
               i2 = (int) (((eta[7] - etmin)/(etmax-etmin))*1000.) ;
               i3 = (int) (((arthd - arthmn)/(arthmx-arthmn))*1000.) ;
               i4 = (int) (((arexitd - arexmn)/(arexmx-arexmn))*1000.) ;
   
               s2 = new Scrollbar(Scrollbar.HORIZONTAL,i2,10,0,1000);
               s3 = new Scrollbar(Scrollbar.HORIZONTAL,i3,10,0,1000);
               s4 = new Scrollbar(Scrollbar.HORIZONTAL,i4,10,0,1000);
   
               nrmat = new Choice() ;
               nrmat.setBackground(Color.white) ;
               nrmat.setForeground(Color.blue) ;
               nrmat.addItem("<-- My Material") ;
               nrmat.addItem("Titanium ");
               nrmat.addItem("Stainless Steel");
               nrmat.addItem("Nickel Alloy");
               nrmat.addItem("Ceramic");
               nrmat.addItem("Actively Cooled");
               nrmat.select(5) ;
   
               atch = new Choice() ;
               atch.addItem("Calculate A7/A2") ;
               atch.addItem("Input A7/A2");
               atch.select(1) ;

               aech = new Choice() ;
               aech.addItem("Calculate A8/A7") ;
               aech.addItem("Input A8/A7");
               aech.select(1) ;

               lmat = new Label("lbm/ft^3", Label.LEFT) ;
               lmat.setForeground(Color.blue) ;

               add(atch) ;
               add(s3) ;
               add(s2) ;
               add(s4) ;
               add(aech) ;
               add(nrmat) ;
               add(lmat) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  this.handleMat(evt) ;
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
   
          public void handleMat(Event evt) {
              Double V1,V2 ;
              double v1,v2 ;

              athsched = atch.getSelectedIndex() ;
               if (athsched == 0) {
                  left.f3.setBackground(Color.black) ;
                  left.f3.setForeground(Color.yellow) ;
               }
               if (athsched == 1) {
                  left.f3.setBackground(Color.white) ;
                  left.f3.setForeground(Color.black) ;
               }
              aexsched = aech.getSelectedIndex() ;
               if (aexsched == 0) {
                  left.f4.setBackground(Color.black) ;
                  left.f4.setForeground(Color.yellow) ;
               }
               if (aexsched == 1) {
                  left.f4.setBackground(Color.white) ;
                  left.f4.setForeground(Color.black) ;
               }

                // ramjet burner - nozzle
               mnozr = nrmat.getSelectedIndex() ;
               if(mnozr > 0) {
                  left.tn.setBackground(Color.black) ;
                  left.tn.setForeground(Color.yellow) ;
               }
               if (mnozr == 0) {
                  left.tn.setBackground(Color.white) ;
                  left.tn.setForeground(Color.black) ;
               }
               switch (mnozr) {
                   case 0: {
                        V1 = Double.valueOf(left.dn.getText()) ;
                        v1 = V1.doubleValue() ;
                        V2 = Double.valueOf(left.tn.getText()) ;
                        v2 = V2.doubleValue() ;
                        dnozr = v1/dconv ; 
                        tnozr = v2/tconv ; 
                        break ;
                   }
                   case 1: dnozr = 293.02 ; tnozr = 1500.; break ;
                   case 2: dnozr = 476.56 ; tnozr = 2000.; break ;
                   case 3: dnozr = 515.2 ; tnozr = 2500.; break ;
                   case 4: dnozr = 164.2 ; tnozr = 3000.; break ;
                   case 5: dnozr = 515.2 ; tnozr = 4500.; break ;
               }
               solve.comPute() ;
          }

          public void handleBar(Event evt) { // ramjet burn -nozzle design
            int i2,i3,i4 ;
            double v2,v3,v4 ;
            float fl2, fl3, fl4 ;

            i2 = s2.getValue() ;
            i3 = s3.getValue() ;
            i4 = s4.getValue() ;

            if (lunits <= 1) {
               vmn2 = etmin;    vmx2 = etmax ;
               vmn3 = arthmn;   vmx3 = arthmx ;
               vmn4 = arexmn;   vmx4 = arexmx ;
            }
            if (lunits == 2) {
               vmx2 = 100.0 - 100.0 * et7ref ;
               vmn2 = vmx2 - 20.0 ;
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
               vmn4 = -10.0 ;  vmx4 = 10.0 ;
            }

            v2 = i2 * (vmx2 - vmn2)/ 1000. + vmn2 ;
            v3 = i3 * (vmx3 - vmn3)/ 1000. + vmn3 ;
            v4 = i4 * (vmx4 - vmn4)/ 1000. + vmn4 ;
         
            fl2 = (float) v2 ;
            fl3 = (float) v3 ;
            fl4 = (float) v4 ;

// nozzle design
            if (lunits <= 1) {
               eta[7]  = v2 ;
               arthd   = v3 ;
               arexitd  = v4 ;
            }
            if (lunits == 2) {
               eta[7]  = et7ref + v2 / 100. ;
               arthd = v3 * a8ref/100. + a8ref  ;
               arexitd = v4 * a8ref/100. + a8ref  ;
            }

            left.f2.setText(String.valueOf(fl2)) ;
            left.f3.setText(String.valueOf(fl3)) ;
            left.f4.setText(String.valueOf(fl4)) ;

            solve.comPute() ; 

          }  // end handle
        }  // end right
   
        class Left extends Panel {
           Turbo outerparent ;
           TextField f1, f2, f3, f4, dn, tn ;
           Label l1, l2, l3, l5, lmat, lm2 ;
   
           Left (Turbo target) { 
               
              outerparent = target ;
              setLayout(new GridLayout(7,2,5,5)) ;
     
              l2 = new Label("Efficiency", Label.CENTER) ;
              f2 = new TextField(String.valueOf((float)eta[7]),5) ;
              f3 = new TextField(String.valueOf((float)arthd),5) ;
              f3.setForeground(Color.black) ;
              f3.setBackground(Color.white) ;
              f4 = new TextField(String.valueOf((float)arexitd),5) ;
              f4.setForeground(Color.black) ;
              f4.setBackground(Color.white) ;
              lmat = new Label("T lim-R", Label.CENTER) ;
              lmat.setForeground(Color.blue) ;
              lm2 = new Label("Materials:", Label.CENTER) ;
              lm2.setForeground(Color.blue) ;
              l5 = new Label("Density", Label.CENTER) ;
              l5.setForeground(Color.blue) ;
  
              dn = new TextField(String.valueOf((float)dnozr),5) ;
              dn.setBackground(Color.black) ;
              dn.setForeground(Color.yellow) ;
              tn = new TextField(String.valueOf((float)tnozr),5) ;
              tn.setBackground(Color.black) ;
              tn.setForeground(Color.yellow) ;
      
              add(new Label(" ", Label.CENTER)) ;
              add(new Label(" ", Label.CENTER)) ;
              add(new Label("A7/A2", Label.CENTER)) ;
              add(f3) ;
              add(l2) ;
              add(f2) ;
              add(new Label("A8/A7", Label.CENTER)) ;
              add(f4) ;
              add(lm2) ;
              add(new Label(" ", Label.CENTER)) ;
              add(lmat) ;
              add(tn) ;
              add(l5) ;
              add(dn) ;
           }
     
           public boolean handleEvent(Event evt) {
             if(evt.id == Event.ACTION_EVENT) {
                this.handleText(evt) ;
                return true ;
             }
             else return false ;
           }
   
           public void handleText(Event evt) {
             Double V2,V3,V4,V7,V8 ;
             double v2,v3,v4,v7,v8 ;
             int i2,i3,i4 ;
             float fl1 ;

             V2 = Double.valueOf(f2.getText()) ;
             v2 = V2.doubleValue() ;
             V3 = Double.valueOf(f3.getText()) ;
             v3 = V3.doubleValue() ;
             V4 = Double.valueOf(f4.getText()) ;
             v4 = V4.doubleValue() ;
             V7 = Double.valueOf(dn.getText()) ;
             v7 = V7.doubleValue() ;
             V8 = Double.valueOf(tn.getText()) ;
             v8 = V8.doubleValue() ;

    // Materials
             if (mnozr == 0) {
                if (v7 <= 1.0*dconv) {
                   v7 = 1.0*dconv ;
                   dn.setText(String.valueOf(filter0(v7*dconv))) ;
                }
                dnozr = v7/dconv ;
                if (v8 <= 500.*tconv) {
                   v8 = 500.*tconv ;
                   tn.setText(String.valueOf(filter0(v8*tconv))) ;
                }
                tnozr = v8/tconv ;
             }

             if (lunits <= 1) {
    // nozzle  efficiency
                eta[7] = v2 ;
                vmn2 = etmin;  vmx2 = etmax ;
                if(v2 < vmn2) {
                   eta[7] = v2 =  vmn2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
                if(v2 > vmx2) {
                   eta[7] = v2 =  vmx2 ;
                   fl1 = (float) v2 ;
                   f2.setText(String.valueOf(fl1)) ;
                }
    //  throat area ratio
                arthd = v3 ;
                vmn3 = arthmn;  vmx3 = arthmx ;
                if(v3 < vmn3) {
                   arthd = v3 =  vmn3 ;
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                }
                if(v3 > vmx3) {
                   arthd = v3 =  vmx3 ;
                   fl1 = (float) v3 ;
                   f3.setText(String.valueOf(fl1)) ;
                }
    //  exit area ratio
                arexitd = v4 ;
                vmn4 = arexmn;  vmx4 = arexmx ;
                if(v4 < vmn4) {
                   arexitd = v4 =  vmn4 ;
                   fl1 = (float) v4 ;
                   f4.setText(String.valueOf(fl1)) ;
                }
                if(v4 > vmx4) {
                   arexitd = v4 =  vmx4 ;
                   fl1 = (float) v4 ;
                   f4.setText(String.valueOf(fl1)) ;
                }
              }

              if (lunits == 2) {
    // nozzle efficiency
               vmx2 = 100.0 - 100.0 * et7ref ;
               vmn2 = vmx2 - 20.0 ;
               if(v2 < vmn2) {
                  v2 =  vmn2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               if(v2 > vmx2) {
                  v2 =  vmx2 ;
                  fl1 = (float) v2 ;
                  f2.setText(String.valueOf(fl1)) ;
               }
               eta[7] = et7ref + v2 / 100. ;
     //  throat area ratio
               vmn3 = -10.0 ;  vmx3 = 10.0 ;
               if(v3 < vmn3) {
                  v3 =  vmn3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               if(v3 > vmx3) {
                  v3 =  vmx3 ;
                  fl1 = (float) v3 ;
                  f3.setText(String.valueOf(fl1)) ;
               }
               arthd  = v3 * a8ref/100. + a8ref ;
     //  exit area ratio
               vmn4 = -10.0 ;  vmx4 = 10.0 ;
               if(v4 < vmn4) {
                  v4 =  vmn4 ;
                  fl1 = (float) v4 ;
                  f4.setText(String.valueOf(fl1)) ;
               }
               if(v4 > vmx4) {
                  v4 =  vmx4 ;
                  fl1 = (float) v4 ;
                  f4.setText(String.valueOf(fl1)) ;
               }
               arexitd  = v4 * a8ref/100. + a8ref ;
             }

             i2 = (int) (((v2 - vmn2)/(vmx2-vmn2))*1000.) ;
             i3 = (int) (((v3 - vmn3)/(vmx3-vmn3))*1000.) ;
             i4 = (int) (((v4 - vmn4)/(vmx4-vmn4))*1000.) ;
   
             right.s2.setValue(i2) ;
             right.s3.setValue(i3) ;
             right.s4.setValue(i4) ;

             solve.comPute() ;

          }  // end handle
        }  //  end  left
     }  // end nozr

     public void fillBox() {
       inlet.left.di.setText(String.valueOf(filter0(dinlt*dconv))) ;
       fan.left.df.setText(String.valueOf(filter0(dfan*dconv))) ;
       comp.left.dc.setText(String.valueOf(filter0(dcomp*dconv))) ;
       burn.left.db.setText(String.valueOf(filter0(dburner*dconv))) ;
       turb.left.dt.setText(String.valueOf(filter0(dturbin*dconv))) ;
       nozl.left.dn.setText(String.valueOf(filter0(dnozl*dconv))) ;
       nozr.left.dn.setText(String.valueOf(filter0(dnozr*dconv))) ;
       inlet.left.ti.setText(String.valueOf(filter0(tinlt*tconv))) ;
       fan.left.tf.setText(String.valueOf(filter0(tfan*tconv))) ;
       comp.left.tc.setText(String.valueOf(filter0(tcomp*tconv))) ;
       burn.left.tb.setText(String.valueOf(filter0(tburner*tconv))) ;
       turb.left.tt.setText(String.valueOf(filter0(tturbin*tconv))) ;
       nozl.left.tn.setText(String.valueOf(filter0(tnozl*tconv))) ;
       nozr.left.tn.setText(String.valueOf(filter0(tnozr*tconv))) ;
     }
  }  // end Inppnl

  class Out extends Panel {
     Turbo outerparent ;
     Box box ;
     Plot plot ;
     Vars vars ;
     Diag diag ;

     Out (Turbo target) {

          outerparent = target ;
          layout = new CardLayout() ;
          setLayout(layout) ;

          box = new Box(outerparent) ; 
          plot = new Plot(outerparent) ; 
          vars = new Vars(outerparent) ; 
          diag = new Diag(outerparent) ; 
 
          add ("first", box) ;
          add ("second", plot) ;
          add ("third", vars) ;
          add ("fourth", diag) ;
     }

     class Diag extends Panel {
        Turbo outerparent ;
        TextField o1, o2, o3, o4, o5, o6, o7, o8, o9 , o10 ;

        Diag (Turbo target) {

            outerparent = target ;
            setLayout(new GridLayout(5,2,1,5)) ;

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

        }
     }

     class Box extends Panel {
        Turbo outerparent ;
        TextField o7, o8, o9, o13, o16, o17 ;
        TextField o18, o19, o20, o21, o22, o23, o24, o25 ;

        Box (Turbo target) {

            outerparent = target ;
            setLayout(new GridLayout(7,4,1,5)) ;

            o7 = new TextField() ;
            o7.setBackground(Color.black) ;
            o7.setForeground(Color.yellow) ;
            o8 = new TextField() ;
            o8.setBackground(Color.black) ;
            o8.setForeground(Color.yellow) ;
            o9 = new TextField() ;
            o9.setBackground(Color.black) ;
            o9.setForeground(Color.yellow) ;
            o13 = new TextField() ;
            o13.setBackground(Color.black) ;
            o13.setForeground(Color.yellow) ;
            o16 = new TextField() ;
            o16.setBackground(Color.black) ;
            o16.setForeground(Color.yellow) ;
            o17 = new TextField() ;
            o17.setBackground(Color.black) ;
            o17.setForeground(Color.yellow) ;
            o18 = new TextField() ;
            o18.setBackground(Color.black) ;
            o18.setForeground(Color.yellow) ;
            o19 = new TextField() ;
            o19.setBackground(Color.black) ;
            o19.setForeground(Color.yellow) ;
            o20 = new TextField() ;
            o20.setBackground(Color.black) ;
            o20.setForeground(Color.yellow) ;
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

            add(new Label("Fn/air", Label.CENTER)) ;
            add(o13) ;
            add(new Label("fuel/air", Label.CENTER)) ;
            add(o9) ;
   
            add(new Label("EPR ", Label.CENTER)) ;
            add(o7) ;
            add(new Label("ETR ", Label.CENTER)) ;
            add(o8) ;

            add(new Label("M2 ", Label.CENTER)) ;
            add(o23) ; 
            add(new Label("q0 ", Label.CENTER)) ;
            add(o19) ;

            add(new Label("NPR ", Label.CENTER)) ;
            add(o17) ;
            add(new Label("V-exit", Label.CENTER)) ;
            add(o16) ;

            add(new Label("Pexit", Label.CENTER)) ;
            add(o22) ; 
            add(new Label("T8 ", Label.CENTER)) ;
            add(o21) ;

            add(new Label("P Fan exit", Label.CENTER)) ;
            add(o24) ; 
            add(new Label("CPR", Label.CENTER)) ;
            add(o25) ; 

            add(new Label("ISP", Label.CENTER)) ;
            add(o20) ;
            add(new Label("Efficiency", Label.CENTER)) ;
            add(o18) ;
        }
   
        public Insets insets() {
           return new Insets(5,0,0,0) ;
        }
   
        public void loadOut() {
           String outfor,outful,outair,outvel,outprs,outtmp,outtim,outpri ;
           int i1 ;

           outfor = " lbs" ;
           if (lunits == 1) outfor = " N" ;
           outful = " lb/hr" ;
           if (lunits == 1) outful = " kg/hr" ;
           outair = " lb/s" ;
           if (lunits == 1) outair = " kg/s" ;
           outvel = " fps" ;
           if (lunits == 1) outvel = " mps" ;
           outprs = " psf" ;
           if (lunits == 1) outprs = " Pa" ;
           outpri = " psi" ;
           if (lunits == 1) outpri = " kPa" ;
           outtmp = " R" ;
           if (lunits == 1) outtmp = " K" ;
           outtim = " sec" ;

           if (inptype == 0 || inptype == 2) {
             in.flight.left.o1.setText(String.valueOf(filter3(fsmach))) ;
           }
           if (inptype == 1 || inptype == 3) {
             vmn1 = u0min;   vmx1 = u0max ;
             in.flight.left.f1.setText(String.valueOf(filter0(u0d))) ;
             i1 = (int) (((u0d - vmn1)/(vmx1-vmn1))*1000.) ;
             in.flight.right.s1.setValue(i1) ;
           }
           in.flight.left.o2.setText(String.valueOf(filter3(psout*pconv)));
           in.flight.left.o3.setText(String.valueOf(filter3(tsout*tconv - tref))) ;
           if (lunits <= 1) {
             if (etr >= 1.0) {
               con.down.o4.setForeground(Color.yellow) ;
               con.down.o4.setText(String.valueOf(filter0(fnlb*fconv)) + outfor) ;
               con.down.o5.setForeground(Color.yellow) ;
               con.down.o5.setText(String.valueOf(filter0(mconv1*fuelrat)) + outful);
               con.down.o6.setForeground(Color.yellow) ;
               con.down.o6.setText(String.valueOf(filter3(sfc*mconv1/fconv))) ;
               con.down.o14.setForeground(Color.yellow) ;
               con.down.o14.setText(String.valueOf(filter0(fglb*fconv)) + outfor) ;
               con.down.o15.setForeground(Color.yellow) ;
               con.down.o15.setText(String.valueOf(filter0(drlb*fconv)) + outfor) ;
             }
             if (etr < 1.0) {
               con.down.o4.setForeground(Color.yellow) ;
               con.down.o4.setText("0.0") ;
               con.down.o5.setForeground(Color.yellow) ;
               con.down.o5.setText("0.0");
               con.down.o6.setForeground(Color.yellow) ;
               con.down.o6.setText("-") ;
               con.down.o14.setForeground(Color.yellow) ;
               con.down.o14.setText("0.0") ;
               con.down.o15.setForeground(Color.yellow) ;
               con.down.o15.setText(String.valueOf(filter0(drlb*fconv)) + outfor) ;
             }
             o7.setForeground(Color.yellow) ;
             o7.setText(String.valueOf(filter3(epr))) ;
             o8.setForeground(Color.yellow) ;
             o8.setText(String.valueOf(filter3(etr))) ;
             o9.setForeground(Color.yellow) ;
             o9.setText(String.valueOf(filter3(fa))) ;
             con.down.o10.setForeground(Color.yellow) ;
             con.down.o10.setText(String.valueOf(filter3(mconv1*eair)) + outair) ;
             con.down.o11.setForeground(Color.yellow) ;
             con.down.o11.setText(String.valueOf(filter3(fconv*weight)) + outfor) ;
             in.size.left.f2.setText(String.valueOf(filter0(fconv*weight))) ;
             con.down.o12.setForeground(Color.yellow) ;
             con.down.o12.setText(String.valueOf(filter3(fnlb/weight))) ;
             o13.setForeground(Color.yellow) ;
             o13.setText(String.valueOf(filter1(fnet*fconv/mconv1))) ;
             o16.setForeground(Color.yellow) ;
             o16.setText(String.valueOf(filter0(uexit*lconv1)) + outvel) ;
             o17.setForeground(Color.yellow) ;
             o17.setText(String.valueOf(filter3(npr))) ;
             o18.setForeground(Color.yellow) ;
             o18.setText(String.valueOf(filter3(eteng))) ;
             o19.setForeground(Color.yellow) ;
             o19.setText(String.valueOf(filter0(q0*fconv/aconv)) + outprs) ;
             o20.setForeground(Color.yellow) ;
             o20.setText(String.valueOf(filter0(isp)) + outtim) ;
             o21.setText(String.valueOf(filter0(t8*tconv)) + outtmp) ;
             o22.setText(String.valueOf(filter3(pexit*pconv)) + outpri) ;
             o23.setText(String.valueOf(filter3(m2))) ;
             if (entype == 2) o24.setText(String.valueOf(filter3(pfexit*pconv)) + outpri) ;
             else o24.setText("-") ;
             o25.setText(String.valueOf(filter3(prat[3]))) ;
           }
           if (lunits == 2) {
             if (etr >= 1.0) {
               con.down.o4.setForeground(Color.green) ;
               con.down.o4.setText(String.valueOf(filter3(100.*(fnlb-fnref)/fnref))) ;
               con.down.o5.setForeground(Color.green) ;
               con.down.o5.setText(String.valueOf(filter3(100.*(fuelrat-fuelref)/fuelref)));
               con.down.o6.setForeground(Color.green) ;
               con.down.o6.setText(String.valueOf(filter3(100.*(sfc-sfcref)/sfcref))) ;
             }
             if (etr < 1.0) {
               con.down.o4.setForeground(Color.yellow) ;
               con.down.o4.setText("0.0") ;
               con.down.o5.setForeground(Color.yellow) ;
               con.down.o5.setText("0.0");
               con.down.o6.setForeground(Color.yellow) ;
               con.down.o6.setText("-") ;
             }
             o7.setForeground(Color.green) ;
             o7.setText(String.valueOf(filter3(100.*(epr-epref)/epref))) ;
             o8.setForeground(Color.green) ;
             o8.setText(String.valueOf(filter3(100.*(etr-etref)/etref))) ;
             o9.setForeground(Color.green) ;
             o9.setText(String.valueOf(filter3(100.*(fa-faref)/faref))) ;
             con.down.o10.setForeground(Color.green) ;
             con.down.o10.setText(String.valueOf(filter3(100.*(eair-airref)/airref))) ;
             con.down.o11.setForeground(Color.green) ;
             con.down.o11.setText(String.valueOf(filter3(100.*(weight-wtref)/wtref))) ;
             con.down.o12.setForeground(Color.green) ;
             con.down.o12.setText(String.valueOf(filter3(100.*(fnlb/weight-wfref)/wfref))) ;
           }
        }
     } //  end Box Output

     class Vars extends Panel {
        Turbo outerparent ;
        TextField po1, po2, po3, po4, po5, po6, po7, po8 ;
        TextField to1, to2, to3, to4, to5, to6, to7, to8 ;
        Label lpa, lpb, lta, ltb;

        Vars (Turbo target) {

            outerparent = target ;
            setLayout(new GridLayout(6,6,1,5)) ;

            po1 = new TextField() ;
            po1.setBackground(Color.black) ;
            po1.setForeground(Color.yellow) ;
            po2 = new TextField() ;
            po2.setBackground(Color.black) ;
            po2.setForeground(Color.yellow) ;
            po3 = new TextField() ;
            po3.setBackground(Color.black) ;
            po3.setForeground(Color.yellow) ;
            po4 = new TextField() ;
            po4.setBackground(Color.black) ;
            po4.setForeground(Color.yellow) ;
            po5 = new TextField() ;
            po5.setBackground(Color.black) ;
            po5.setForeground(Color.yellow) ;
            po6 = new TextField() ;
            po6.setBackground(Color.black) ;
            po6.setForeground(Color.yellow) ;
            po7 = new TextField() ;
            po7.setBackground(Color.black) ;
            po7.setForeground(Color.yellow) ;
            po8 = new TextField() ;
            po8.setBackground(Color.black) ;
            po8.setForeground(Color.yellow) ;
   
            to1 = new TextField() ;
            to1.setBackground(Color.black) ;
            to1.setForeground(Color.yellow) ;
            to2 = new TextField() ;
            to2.setBackground(Color.black) ;
            to2.setForeground(Color.yellow) ;
            to3 = new TextField() ;
            to3.setBackground(Color.black) ;
            to3.setForeground(Color.yellow) ;
            to4 = new TextField() ;
            to4.setBackground(Color.black) ;
            to4.setForeground(Color.yellow) ;
            to5 = new TextField() ;
            to5.setBackground(Color.black) ;
            to5.setForeground(Color.yellow) ;
            to6 = new TextField() ;
            to6.setBackground(Color.black) ;
            to6.setForeground(Color.yellow) ;
            to7 = new TextField() ;
            to7.setBackground(Color.black) ;
            to7.setForeground(Color.yellow) ;
            to8 = new TextField() ;
            to8.setBackground(Color.black) ;
            to8.setForeground(Color.yellow) ;
   
            lpa = new Label("Pres-psi", Label.CENTER) ;
            lpb = new Label("Pres-psi", Label.CENTER) ;
            lta = new Label("Temp-R", Label.CENTER) ;
            ltb = new Label("Temp-R", Label.CENTER) ;

            add(new Label(" ", Label.CENTER)) ;
            add(new Label("Total ", Label.RIGHT)) ;
            add(new Label("Press.", Label.LEFT)) ;
            add(new Label("and", Label.CENTER)) ;
            add(new Label("Temp.", Label.LEFT)) ;
            add(new Label(" ", Label.CENTER)) ;

            add(new Label("Station", Label.CENTER)) ;
            add(lpa) ;
            add(lta) ;
            add(new Label("Station", Label.CENTER)) ;
            add(lpb) ;
            add(ltb) ;

            add(new Label("1", Label.CENTER)) ;
            add(po1) ; 
            add(to1) ; 
            add(new Label("5", Label.CENTER)) ;
            add(po5) ; 
            add(to5) ; 

            add(new Label("2", Label.CENTER)) ;
            add(po2) ; 
            add(to2) ; 
            add(new Label("6", Label.CENTER)) ;
            add(po6) ; 
            add(to6) ; 

            add(new Label("3", Label.CENTER)) ;
            add(po3) ; 
            add(to3) ; 
            add(new Label("7", Label.CENTER)) ;
            add(po7) ; 
            add(to7) ; 

            add(new Label("4", Label.CENTER)) ;
            add(po4) ; 
            add(to4) ; 
            add(new Label("8", Label.CENTER)) ;
            add(po8) ; 
            add(to8) ; 
        }
   
        public Insets insets() {
           return new Insets(5,0,0,0) ;
        }
   
        public void loadOut() {
           po1.setText(String.valueOf(filter1(pt[2]*pconv))) ;
           po2.setText(String.valueOf(filter1(pt[13]*pconv))) ;
           po3.setText(String.valueOf(filter1(pt[3]*pconv))) ;
           po4.setText(String.valueOf(filter1(pt[4]*pconv))) ;
           po5.setText(String.valueOf(filter1(pt[5]*pconv))) ;
           po6.setText(String.valueOf(filter1(pt[15]*pconv))) ;
           po7.setText(String.valueOf(filter1(pt[7]*pconv))) ;
           po8.setText(String.valueOf(filter1(pt[8]*pconv))) ;
           to1.setText(String.valueOf(filter0(tt[2]*tconv))) ;
           to2.setText(String.valueOf(filter0(tt[13]*tconv))) ;
           to3.setText(String.valueOf(filter0(tt[3]*tconv))) ;
           to4.setText(String.valueOf(filter0(tt[4]*tconv))) ;
           to5.setText(String.valueOf(filter0(tt[5]*tconv))) ;
           to6.setText(String.valueOf(filter0(tt[15]*tconv))) ;
           to7.setText(String.valueOf(filter0(tt[7]*tconv))) ;
           to8.setText(String.valueOf(filter0(tt[8]*tconv))) ;
        }
     } //  end Vars Output

     class Plot extends Canvas {
        Turbo outerparent ;

        Plot (Turbo target) {
            setBackground(Color.blue) ;
        }

        public Insets insets() {
           return new Insets(0,10,0,10) ;
        }

        public boolean mouseDrag(Event evt, int x, int y) {
           handle(x,y) ;
           return true;
        }

        public boolean mouseUp(Event evt, int x, int y) {
           handle(x,y) ;
           return true;
        }

        public void handle(int x, int y) {
           if (y <= 27) {    // labels
              if ( x <= 71) {    // pressure variation
                 plttyp = 3 ;
                 if(pltkeep == 7) {
                    varflag = 0 ;
                    layin.show(in, "first")  ;
                 }
              }
              if ( x > 71 && x <= 151) {    // temperature variation
                 plttyp = 4 ;
                 if(pltkeep == 7) {
                    varflag = 0 ;
                    layin.show(in, "first")  ;
                 }
              }
              if ( x > 151 && x <= 181) {    //  T - s
                 plttyp = 5 ;
                 if(pltkeep == 7) {
                    varflag = 0 ;
                    layin.show(in, "first")  ;
                 }
              }
              if ( x > 181 && x <= 211) {    //  p - v
                 plttyp = 6 ;
                 if(pltkeep == 7) {
                    varflag = 0 ;
                    layin.show(in, "first")  ;
                 }
              }
              if ( x > 211 && x < 290) {    //  generate plot
                 plttyp = 7 ;
                 layin.show(in, "ninth")  ;
                 lunits = 0 ;
                 varflag = 0 ;
                 con.setUnits () ;
                 con.up.untch.select(lunits) ;
                 in.plot.left.ordch.select(0) ;
                 in.plot.left.absch.select(0) ;
              }
              pltkeep = plttyp ;
              con.setPlot() ;
           }
           if (y > 27) {
              if (x >= 306) {   // zoom widget
                sldplt = y ;
                if (sldplt < 45) sldplt = 45;
                if (sldplt > 155) sldplt = 155;
                factp = 120.0 - (sldplt-45)*1.0 ;
              }
           } 
           solve.comPute() ;
           plot.repaint() ;
           return ;
        }

        public void update(Graphics g) {
           plot.paint(g) ;
        }

        public void loadPlot() {
          double cnst,delp ;
          int ic;
   
          switch (plttyp) {
           case 3:   {                       /*  press variation */
               npt = 9 ;
               pltx[1] = 0.0 ;  plty[1] = ps0*pconv ;
               pltx[2] = 1.0 ;  plty[2] = pt[2]*pconv ;
               pltx[3] = 2.0 ;  plty[3] = pt[13]*pconv ;
               pltx[4] = 3.0 ;  plty[4] = pt[3]*pconv ;
               pltx[5] = 4.0 ;  plty[5] = pt[4]*pconv ;
               pltx[6] = 5.0 ;  plty[6] = pt[5]*pconv ;
               pltx[7] = 6.0 ;  plty[7] = pt[15]*pconv ;
               pltx[8] = 7.0 ;  plty[8] = pt[7]*pconv ;
               pltx[9] = 8.0 ;  plty[9] = pt[8]*pconv ;
               return;
           }
           case 4:   {                       /*  temp variation */
               npt = 9 ;
               pltx[1] = 0.0 ; plty[1] = ts0*tconv ;
               pltx[2] = 1.0 ; plty[2] = tt[2]*tconv ;
               pltx[3] = 2.0 ; plty[3] = tt[13]*tconv ;
               pltx[4] = 3.0 ; plty[4] = tt[3]*tconv ;
               pltx[5] = 4.0 ; plty[5] = tt[4]*tconv ;
               pltx[6] = 5.0 ; plty[6] = tt[5]*tconv ;
               pltx[7] = 6.0 ; plty[7] = tt[15]*tconv ;
               pltx[8] = 7.0 ; plty[8] = tt[7]*tconv ;
               pltx[9] = 8.0 ; plty[9] = tt[8]*tconv ;
               return;
           }
           case 5:   {                       /*  t-s plot */
               npt = 7 ;
               pltx[1] = s[0]*bconv;
               plty[1] = ts0*tconv ;
               for(ic =2; ic<=5; ++ic) {
                    pltx[ic] = s[ic]*bconv ;
                    plty[ic] = tt[ic]*tconv ;
               }
               pltx[6] = s[7]*bconv ;
               plty[6] = tt[7]*tconv ;
               pltx[7] = s[8]*bconv ;
               plty[7] = t8*tconv;
               return;
           }
           case 6:  {                        /*  p-v plot */
               npt = 25 ;
               plty[1] = ps0*pconv;
               pltx[1] = v[0]*dconv ;
               cnst = plty[1]*Math.pow(pltx[1],gama) ;
               plty[11] = pt[3]*pconv ;
               pltx[11] = v[3]*dconv ;
               delp = (plty[11]-plty[1])/11.0 ;
               for (ic=2; ic<=10; ++ic) {
                    plty[ic] = plty[1]+ic*delp ;
                    pltx[ic] = Math.pow(cnst/plty[ic],1.0/gama) ;
               }
               plty[12] = pt[4]*pconv ;
               pltx[12] = v[4]*dconv ;
               cnst = plty[12]*Math.pow(pltx[12],gama) ;
               if (abflag == 1) {
                    plty[25] = ps0*pconv ;
                    pltx[25] = v[8]*dconv ;
                    delp = (plty[25]-plty[12])/13.0 ;
                    for (ic=13; ic<=24; ++ic) {
                         plty[ic] = plty[12]+(ic-12)*delp ;
                         pltx[ic] = Math.pow(cnst/plty[ic],1.0/gama) ;
                    }
               }
               else {
                    plty[18] = pt[5]*pconv ;
                    pltx[18] = v[5]*dconv ;
                    delp = (plty[18]-plty[12])/6.0 ;
                    for (ic=13; ic<=17; ++ic) {
                         plty[ic] = plty[12]+(ic-12)*delp ;
                         pltx[ic] = Math.pow(cnst/plty[ic],1.0/gama) ;
                    }
                    plty[19] = pt[7]*pconv  ;
                    pltx[19] = v[7]*dconv ;
                    cnst = plty[19]*Math.pow(pltx[19],gama) ;
                    plty[25] = ps0*pconv ;
                    pltx[25] = v[8]*dconv ;
                    delp = (plty[25]-plty[19])/6.0 ;
                    for (ic=20; ic<=24; ++ic) {
                         plty[ic] = plty[19]+(ic-19)*delp ;
                         pltx[ic] = Math.pow(cnst/plty[ic],1.0/gama) ;
                    }
               }
               return;
           }
           case 7: break ;                   /* create plot */
          }
        }
    
        public void paint(Graphics g) {
//          int iwidth = partimg.getWidth(this) ;
 //         int iheight = partimg.getHeight(this) ;
          int i,j,k ;
          int exes[] = new int[8] ;
          int whys[] = new int[8] ;
          int xlabel, ylabel,ind;
          double xl,yl;
          double offx,scalex,offy,scaley,waste,incy,incx;
    
          if (plttyp >= 3 && plttyp <= 7) {         //  perform a plot
            off1Gg.setColor(Color.blue) ;
            off1Gg.fillRect(0,0,350,350) ;

            if (ntikx < 2) ntikx = 2 ;     /* protection 13June96 */
            if (ntiky < 2) ntiky = 2 ;
            offx = 0.0 - begx ;
            scalex = 6.5/(endx-begx) ;
            incx = (endx-begx)/(ntikx-1);
            offy = 0.0 - begy ;
            scaley = 10.0/(endy-begy) ;
            incy = (endy-begy)/(ntiky-1) ;
                                            /* draw axes */
            off1Gg.setColor(Color.white) ;
            exes[0] = (int) (factp* 0.0 + xtranp) ;
            whys[0] = (int) (-150. + ytranp) ;
            exes[1] = (int) (factp* 0.0 + xtranp) ;
            whys[1] = (int) (factp* 0.0 + ytranp) ;
            exes[2] = (int) (215. + xtranp) ;
            whys[2] = (int) (factp* 0.0 + ytranp) ;
            off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
            off1Gg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;

            xlabel = (int) (-75. + xtranp) ;      /*     label y axis */
            ylabel = (int) (-65. + ytranp) ;
            off1Gg.drawString(laby,xlabel,ylabel) ; 
            off1Gg.drawString(labyu,xlabel,ylabel+20) ; 
                                             /* add tick values */
            for (ind= 1; ind<= ntiky; ++ind){
                  xlabel = (int) (-33.+xtranp) ;
                  yl = begy + (ind-1) * incy ;
                  ylabel = (int) (factp* -scaley*yl + ytranp) ;
                  if (nord != 5) {
                     off1Gg.drawString(String.valueOf((int) yl),xlabel,ylabel) ; 
                  }
                  else {
                     off1Gg.drawString(String.valueOf(filter3(yl)),xlabel,ylabel) ; 
                  }
            }
            xlabel = (int) (75. + xtranp) ;       /*   label x axis */
            ylabel = (int) (20. + ytranp) ;
            off1Gg.drawString(labx,xlabel,ylabel) ; 
            off1Gg.drawString(labxu,xlabel + 50,ylabel) ; 
                                             /* add tick values */
            for (ind= 1; ind<= ntikx; ++ind){
                  ylabel = (int) (10. + ytranp) ;
                  xl = begx + (ind-1) * incx ;
                  xlabel = (int) (33.*(scalex*(xl + offx) -.05) +xtranp) ;
                  if (nabs >= 2 && nabs <= 3) {
                     off1Gg.drawString(String.valueOf(xl),xlabel,ylabel) ; 
                  }
                  if (nabs < 2 || nabs > 3) {
                     off1Gg.drawString(String.valueOf((int) xl),xlabel,ylabel) ; 
                  }
            }
      
            if(lines == 0) {
                for (i=1; i<=npt; ++i) {
                    xlabel = (int) (33.*scalex*(offx+pltx[i])+xtranp) ;
                    ylabel = (int) (factp*-scaley*(offy+plty[i])+ytranp +7.) ;
                    off1Gg.drawString("*",xlabel,ylabel) ; 
                }
            }
            else {
              exes[1] = (int) (33.*scalex*(offx+pltx[1])+xtranp);
              whys[1] = (int) (factp*-scaley*(offy+plty[1])+ytranp);
              for (i=2; i<=npt; ++i) {
                  exes[0] = exes[1] ;
                  whys[0] = whys[1] ;
                  exes[1] = (int) (33.*scalex*(offx+pltx[i])+xtranp);
                  whys[1] = (int) (factp*-scaley*(offy+plty[i])+ytranp);
                  off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
            if (plttyp == 4) {       // draw temp limits
              off1Gg.setColor(Color.yellow) ;
              if (entype < 3) {
                 exes[0] = (int) (33.*scalex*(offx+pltx[0])+xtranp);
                 whys[0] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 exes[1] = (int) (33.*scalex*(offx+pltx[1])+xtranp);
                 whys[1] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 off1Gg.drawString("Limit",exes[0]+5,whys[0]) ; 
                 exes[0] = (int) (33.*scalex*(offx+pltx[2])+xtranp);
                 whys[0] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[1] = (int) (33.*scalex*(offx+pltx[3])+xtranp);
                 whys[1] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 if (entype == 2) {
                    whys[1] = (int) (factp*-scaley*(offy+tconv*tfan)+ytranp);
                 }
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[0] = (int) (33.*scalex*(offx+pltx[4])+xtranp);
                 whys[0] = (int) (factp*-scaley*(offy+tconv*tcomp)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[1] = (int) (33.*scalex*(offx+pltx[5])+xtranp);
                 whys[1] = (int) (factp*-scaley*(offy+tconv*tburner)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[0] = (int) (33.*scalex*(offx+pltx[6])+xtranp);
                 whys[0] = (int) (factp*-scaley*(offy+tconv*tturbin)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[1] = (int) (33.*scalex*(offx+pltx[6])+xtranp);
                 whys[1] = (int)(factp*-scaley*(offy+tconv*tnozl)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[0] = (int) (33.*scalex*(offx+pltx[9])+xtranp);
                 whys[0] = (int)(factp*-scaley*(offy+tconv*tnozl)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
              if (entype == 3) {
                 exes[1] = (int) (33.*scalex*(offx+pltx[0])+xtranp);
                 whys[1] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 exes[0] = (int) (33.*scalex*(offx+pltx[4])+xtranp);
                 whys[0] = (int) (factp*-scaley*(offy+tconv*tinlt)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 off1Gg.drawString("Limit",exes[1]+5,whys[1]) ;
                 exes[1] = (int) (33.*scalex*(offx+pltx[5])+xtranp);
                 whys[1] = (int) (factp*-scaley*(offy+tconv*tburner)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
                 exes[0] = (int) (33.*scalex*(offx+pltx[9])+xtranp);
                 whys[0] = (int)(factp*-scaley*(offy+tconv*tnozr)+ytranp);
                 off1Gg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
            }
              // plot  labels
            off1Gg.setColor(Color.blue) ;
            off1Gg.fillRect(0,0,300,27) ;
            off1Gg.setColor(Color.white) ;
            if (plttyp == 3) {
              off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(0,0,70,12) ;
              off1Gg.setColor(Color.black) ;
            }
            off1Gg.drawString("Pressure",10,10) ; 
            off1Gg.setColor(Color.white) ;
            if (plttyp == 4) {
              off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(71,0,80,12) ;
              off1Gg.setColor(Color.black) ;
            }
            off1Gg.drawString("Temperature",75,10) ; 
            off1Gg.setColor(Color.white) ;
            if (plttyp == 5) {
              off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(151,0,30,12) ;
              off1Gg.setColor(Color.black) ;
            }
            off1Gg.drawString("T-s",155,10) ; 
            off1Gg.setColor(Color.white) ;
            if (plttyp == 6) {
              off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(181,0,30,12) ;
              off1Gg.setColor(Color.black) ;
            }
            off1Gg.drawString("P-v",185,10) ; 
            off1Gg.setColor(Color.white) ;
            if (plttyp == 7) {
              off1Gg.setColor(Color.yellow) ;
              off1Gg.fillRect(211,0,80,12) ;
              off1Gg.setColor(Color.black) ;
            }
            off1Gg.drawString("Generate",220,10) ; 
                                 // zoom widget
            off1Gg.setColor(Color.blue) ;
            off1Gg.fillRect(305,15,35,145) ;
            off1Gg.setColor(Color.white) ;
            off1Gg.drawString("Scale",305,25) ;
            off1Gg.drawLine(320,35,320,155) ;
            off1Gg.fillRect(310,sldplt,20,5) ;
          }

          if (plttyp == 2) {           // draw photo
             off1Gg.setColor(Color.white) ;
             off1Gg.fillRect(0,0,350,350) ;
             off1Gg.drawString("Pictures not allowed by Java patch",185,100) ; 
//             off1Gg.drawImage(partimg,0,0,iwidth,iheight,this) ;
//             off1Gg.drawImage(partimg,30,40,this) ;
          }

          g.drawImage(offImg1,0,0,this) ;   
        }  // end paint
      }  // end Plot Output

  } //  end Output panel

  class Viewer extends Canvas 
         implements Runnable{
     Point locate,anchor ;
     Turbo outerparent ;
     Thread runner ;
//     Image displimg ;
     double r0,x0,xcowl,rcowl,liprad;  /* cowl  and free stream */
     double capa,capb,capc ;           /* capture tube coefficients */
     double cepa,cepb,cepc,lxhst ;     /* exhaust tube coefficients */
     double xfan,fblade ;              /* fan blade */
     double xcomp,hblade,tblade,sblade; /* compressor blades */
     double xburn,rburn,tsig,radius ;   /* combustor */
     double xturb,xturbh,rnoz,xnoz,xflame,xit,rthroat;

     Viewer (Turbo target) {
         setBackground(Color.black) ;
         runner = null ;
 //        displimg = getImage(getCodeBase(),"ab1.gif") ;
     }

     public Insets insets() {
        return new Insets(0,10,0,10) ;
     }

     public void start() {
        if (runner == null) {
           runner = new Thread(this) ;
           runner.start() ;
        }
        antim = 0 ;
        ancol = 1 ;
        counter = 0 ;
     }
 
     public void run() {
       while (true) {
           counter ++ ;
           ++ antim ;
/*
           if (inflag == 0 && fireflag == 0) {
              runner = null ;
              return;
           }

           if(entype == 0) displimg = antjimg[counter-1] ;
           if(entype == 1) displimg = anabimg[counter-1] ;
           if(entype == 2) displimg = anfnimg[counter-1] ;
           if(entype == 3) displimg = anrmimg[counter-1] ;
*/
           try { Thread.sleep(100); }
           catch (InterruptedException e) {}
           view.repaint() ;
           if (counter == 3) counter = 0 ;
           if (antim == 3) {
             antim = 0;
             ancol = - ancol ;
          }
       }
     }
 
     public boolean mouseDown(Event evt, int x, int y) {
        anchor = new Point(x,y) ;
        return true;
     }

     public boolean mouseDrag(Event evt, int x, int y) {
        handle(x,y) ;
        return true;
     }

     public boolean mouseUp(Event evt, int x, int y) {
         handleb(x,y) ;
         return true;
     }

     public void handle(int x, int y) {
         // determine location and move
         if (inflag == 1) return ;
         if (plttyp == 7) return ;

         if (x <= 35) {
           if (y > 42 && y < 161 ) {      // Zoom widget
             sldloc = y ;
             if (sldloc < 50) sldloc = 50;
             if (sldloc > 160) sldloc = 160;
             factor = 10.0 + (sldloc-50)*1.0 ;
             view.repaint();
             return ;
           }
         }

         if (y >= 42 && x >= 35) {      //  move the engine
           locate = new Point(x,y) ;
           xtrans = xtrans + (int) (.2*(locate.x - anchor.x)) ;
           ytrans = ytrans + (int) (.2*(locate.y - anchor.y)) ;
           if (xtrans > 320) xtrans = 320 ;
           if (xtrans < -280) xtrans = -280 ;
           if (ytrans > 300) ytrans = 300 ;
           if (ytrans <-300) ytrans = -300 ;
           view.repaint();
           return ;
         }
     }

     public void handleb(int x, int y) {
         // determine choices
         if (inflag == 1) return ;
         if (plttyp == 7) return ;

         if (x <= 35) {
           if (y >= 180) {   // find plot
             xtrans = 125.0 ;
             ytrans = 115.0 ;
             factor = 35. ;
             sldloc = 75 ;
             view.repaint();
             return ;
           }
         }

         if (y >= 27 && y <= 42 ) {   // set flight conitions 
           if (x >= 5   && x <= 40 ) {  
             layin.show(in, "first")  ;
             varflag = 0 ;
             solve.comPute () ; 
             view.repaint();
             out.plot.repaint() ;
             con.setPanl() ;
             return ;
           }
         }
 
         if (y >= 12 && y <= 26 ) {   // set size
           if (x >= 5  && x <= 40) {   // size
             layin.show(in, "second")  ;
             varflag = 1 ;
             solve.comPute () ; 
             view.repaint();
             out.plot.repaint() ;
             con.setPanl() ;
             return ;
           }
         }
 
         if (y >= 27 && y <= 42) {                // key off the words
           if (entype <= 2) {
             if (x >= 50 && x <= 89) {    //inlet
               layin.show(in, "third")  ;
               varflag = 2 ;
   //            if (plttyp == 2) partimg = getImage(getCodeBase(),
   //                        "inlet.gif"); 
             }
             if (x >= 90 && x <= 119) {   //fan
               if (entype != 2) return ;
               layin.show(in, "fourth")  ;
               varflag = 3 ;
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "fan.gif"); 
             }
             if (x >= 120 && x <= 199) {  //compress
               layin.show(in, "fifth")  ;
               varflag = 4 ;
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "compres.gif"); 
             }
             if (x >= 200 && x <= 249) {  // burner
               layin.show(in, "sixth")  ;
               varflag = 5 ;
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "burner.gif"); 
             }
             if (x >= 250 && x <= 299) {  //turbine
               layin.show(in, "seventh")  ;
               varflag = 6 ;
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "turbine.gif"); 
             }
             if (x >= 300 && x <= 349) {  // nozzle
               layin.show(in, "eighth")  ;
               varflag = 7 ;
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "nozzle.gif"); 
             }
           }
           if (entype == 3) {
             if (x >= 50 && x <= 89) {
               layin.show(in, "third")  ;
               varflag = 2 ;
   //            if (plttyp == 2) partimg = getImage(getCodeBase(),
   //                        "ramin.gif"); 
             }
             if (x >= 90 && x <= 200) {  // burner
               layin.show(in, "sixth")  ;
               varflag = 5 ;
    //           if (plttyp == 2) partimg = getImage(getCodeBase(),
    //                       "burner.gif");
             }
             if (x >= 201 && x <= 349) {
               layin.show(in, "tenth")  ;
               varflag = 7 ;
    //           if (plttyp == 2) partimg = getImage(getCodeBase(),
     //                      "ramburn.gif"); 
             }
           }
           view.repaint();
           out.plot.repaint() ;
           return ;
         }

         if (y > 12 && y < 27) {          // set engine type from words
           if (x >= 50   && x <= 110 ) {   // turbojet
               entype = 0 ; 
 //              if (plttyp == 2) partimg = getImage(getCodeBase(),
 //                          "tjet.gif"); 
           }
           if (x >= 111  && x <= 191) {    // afterburner
               entype = 1 ;  
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "aburner.gif"); 
           }
           if (x >= 192 && x <= 262)  {   // turbo fan
               entype = 2 ;  
  //             if (plttyp == 2) partimg = getImage(getCodeBase(),
  //                         "tfan.gif"); 
           }
           if (x >= 263 && x <= 350)   {   // ramjet
               entype = 3 ; 
               u0d = 1500. ;
               altd = 35000. ;
   //            if (plttyp == 2) partimg = getImage(getCodeBase(),
   //                        "ramjet.gif"); 
           }
           varflag = 0 ;
           layin.show(in, "first")  ;
                                  // reset limits
           if (entype <=2) {
             if (lunits != 1) {
                 u0max = 1500. ;
                 altmax = 60000. ;
                 t4max = 3200. ;
                 t7max = 4100. ;
             }
             if (lunits == 1) {
                 u0max = 2500. ;
                 altmax = 20000. ;
                 t4max = 1800. ;
                 t7max = 2100. ;
             }
             if (u0d > u0max) u0d = u0max ;
             if (altd > altmax) altd = altmax ;
             if (tt4d > t4max) tt4 = tt4d = t4max ;
             if (tt7d > t7max) tt7 = tt7d = t7max ;
           }
           else {
             if (lunits != 1) {
                 u0max = 4500. ;
                 altmax = 100000. ;
                 t4max = 4500. ;
                 t7max = 4500. ;
             }
             if (lunits == 1) {
                 u0max = 7500. ;
                 altmax = 35000. ;
                 t4max = 2500. ;
                 t7max = 2200. ;
             }
           }
                  // get the areas correct
           if (entype != 2) {
              a2 = acore ;
              a2d = a2 * aconv ;
           }
           if (entype == 2) {
              afan = acore * (1.0 + byprat) ;
              a2 = afan ;
              a2d = a2 * aconv ;
           }
           diameng = Math.sqrt(4.0 * a2d / 3.14159) ;
                 // set the abflag correctly
           if (entype == 1) {
                abflag = 1 ;
                mnozl = 5; 
                dnozl = 400.2 ; 
                tnozl = 4100. ;
                in.flight.right.nozch.select(abflag) ;
           }
           if (entype != 1) {
                abflag = 0 ;
                mnozl = 3; 
                dnozl = 515.2 ; 
                tnozl = 2500. ;
                in.flight.right.nozch.select(abflag) ;
           }

           con.setUnits() ;
           con.setPanl() ;
           solve.comPute () ; 
           view.repaint();
           out.plot.repaint() ;
           return ;
         }
     }

     public void update(Graphics g) {
        view.paint(g) ;
     }

     public void getDrawGeo()  { /* get the drawing geometry */
        double delx,delt ;
        int index,i,j ;

        lxhst = 5. ;
 
        scale = Math.sqrt(acore/3.1415926) ;
        if (scale > 10.0) scale = scale / 10.0 ;
    
        if (ncflag == 0) {
           ncomp = (int) (1.0 + p3p2d / 1.5) ;
           if (ncomp > 15) ncomp = 15 ;
           in.comp.left.f3.setText(String.valueOf(ncomp)) ;
        }
        sblade = .02;
        hblade = Math.sqrt(2.0/3.1415926);
        tblade = .2*hblade;
        r0 = Math.sqrt(2.0*mfr/3.1415926);
        x0 = -4.0 * hblade ;
    
        radius = .3*hblade;
        rcowl = Math.sqrt(1.8/3.1415926);
        liprad = .1*hblade ;
        xcowl = - hblade - liprad;
        xfan = 0.0 ;
        xcomp = ncomp*(tblade+sblade) ;
        ncompd = ncomp ;
        if (entype == 2) {                    /* fan geometry */
            ncompd = ncomp + 3 ;
            fblade = Math.sqrt(2.0*(1.0+byprat)/3.1415926);
            rcowl = fblade ;
            r0 = Math.sqrt(2.0*(1.0+byprat)*mfr/3.1415926);
            xfan = 3.0 * (tblade+sblade) ;
            xcomp = ncompd*(tblade+sblade) ;
        }
        if (r0 < rcowl) {
          capc = (rcowl - r0)/((xcowl-x0)*(xcowl-x0)) ;
          capb = -2.0 * capc * x0 ;
          capa = r0 + capc * x0*x0 ;
        }
        else {
          capc = (r0 - rcowl)/((xcowl-x0)*(xcowl-x0)) ;
          capb = -2.0 * capc * xcowl ;
          capa = rcowl + capc * xcowl*xcowl ;
        }
        lcomp = xcomp ;
        lburn = hblade ;
        xburn = xcomp + lburn ;
        rburn = .2*hblade ;

        if (ntflag == 0) {
          nturb = 1 + ncomp/4 ;
          in.turb.left.f3.setText(String.valueOf(nturb)) ;
          if (entype == 2) nturb = nturb + 1 ;
        }
        lturb = nturb*(tblade+sblade) ;
        xturb = xburn + lturb ;
        xturbh = xturb - 2.0*(tblade+sblade) ;
        lnoz = lburn ;
        if (entype == 1) lnoz = 3.0 * lburn ;
        if (entype == 3) lnoz = 3.0 * lburn ;
        xnoz = xturb + lburn ;
        xflame = xturb + lnoz ;
        xit = xflame + hblade ;
        if (entype <=2) {
          rnoz = Math.sqrt(a8rat*2.0/3.1415926);
          cepc = -rnoz/(lxhst*lxhst) ;
          cepb = -2.0*cepc*(xit + lxhst) ;
          cepa = rnoz - cepb*xit - cepc*xit*xit ;
        }
        if (entype == 3) {
          rnoz = Math.sqrt(arthd*arexitd*2.0 / 3.1415926) ;
          rthroat = Math.sqrt(arthd*2.0 / 3.1415926) ;
       }
                                // animated flow field
       for(i=0; i<=5; ++ i) {   // upstream
           xg[4][i] = xg[0][i] = i * (xcowl - x0)/5.0 +x0  ;
           yg[0][i] = .9*hblade;
           yg[4][i] = 0.0 ;
       }
       for(i=6; i<=14; ++ i) {  // compress
           xg[4][i] = xg[0][i] = (i-5) * (xcomp - xcowl)/9.0 + xcowl ;
           yg[0][i] = .9*hblade ;
           yg[4][i] = (i-5) * (1.5*radius)/9.0 ;
       }
       for(i=15; i<=18; ++ i) {  // burn
           xg[0][i] = (i-14) * (xburn - xcomp)/4.0 + xcomp ;
           yg[0][i] = .9*hblade ;
           yg[4][i] = .5*radius ;
       }
       for(i=19; i<=23; ++ i) {  // turb
           xg[0][i] = (i-18) * (xturb - xburn)/5.0 + xburn ;
           yg[0][i] = .9*hblade ;
           yg[4][i] = (i-18) * (-.5*radius)/5.0 + radius ;
       }
       for(i=24; i<=29; ++ i) { // nozzl
           xg[0][i] = (i-23) * (xit - xturb)/6.0 + xturb ;
           if (entype != 3) {
             yg[0][i] = (i-23) * (rnoz - hblade)/6.0 + hblade ;
           }
           if (entype == 3) {
             yg[0][i] = (i-23) * (rthroat - hblade)/6.0 + hblade ;
           }
           yg[4][i] = 0.0 ;
       }
       for(i=29; i<=34; ++ i) { // external
           xg[0][i] = (i-28) * (3.0)/3.0 + xit ;
           if (entype != 3) {
              yg[0][i] = (i-28) * (rnoz)/3.0 + rnoz ;
           }
           if (entype == 3) {
              yg[0][i] = (i-28) * (rthroat)/3.0 + rthroat ;
           }
           yg[4][i] = 0.0 ;
       }

       for (j=1; j<=3; ++ j) { 
           for(i=0; i<=34; ++ i) {
             xg[j][i] = xg[0][i] ;
             yg[j][i] = (1.0 - .25 * j) * (yg[0][i]-yg[4][i]) + yg[4][i] ;
           }
       }
       for (j=5; j<=8; ++ j) { 
           for(i=0; i<=34; ++ i) {
              xg[j][i] = xg[0][i] ;
              yg[j][i] = -yg[8-j][i] ;
           }
       }
       if (entype == 2) {  // fan flow
           for(i=0; i<=5; ++ i) {   // upstream
               xg[9][i] = xg[0][i] ;
               xg[10][i] = xg[0][i] ;
               xg[11][i] = xg[0][i] ;
               xg[12][i] = xg[0][i] ;
           }
           for(i=6; i<=34; ++ i) {  // compress
               xg[9][i] = xg[10][i] = xg[11][i] = xg[12][i] =
                     (i-6) * (7.0 - xcowl)/28.0 + xcowl ;
           }
           for(i=0; i<=34; ++ i) {  // compress
               yg[9][i] = .5*(hblade + .9*rcowl) ;
               yg[10][i] = .9*rcowl ;
               yg[11][i] = -.5*(hblade + .9*rcowl) ;
               yg[12][i] = -.9*rcowl ;
           }
       }
     }

  public void paint(Graphics g) {
    int i,j,k ;
    int bcol,dcol ;
    int exes[] = new int[8] ;
    int whys[] = new int[8] ;
    int xlabel, ylabel,ind;
    double xl,yl;
    double offx,scalex,offy,scaley,waste,incy,incx;
 
//    if (inflag == 0) {
      bcol = 0 ;
      dcol = 7 ;
      xl = factor*0.0 + xtrans ;
      yl = factor*0.0 + ytrans ;

      offsGg.setColor(Color.black) ;
      offsGg.fillRect(0,0,500,500) ;
      if (inflag == 0) {       // Grid paper background
        offsGg.setColor(Color.blue) ;
        for (j=0; j<=20; ++j) {
            exes[0] = 0 ; exes[1] = 500 ;
            whys[0] = whys[1] = (int) (yl + factor*(20./scale * j)/25.0);
            offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
            whys[0] = whys[1] = (int) (yl - factor*(20./scale * j)/25.0);
            offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
        }
        for (j=0; j<=40; ++j) {
           whys[0] = 0 ; whys[1] = 500 ;
           exes[0] = exes[1] = (int) (xl + factor*(20./scale * j)/25.0) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           exes[0] = exes[1] = (int) (xl - factor*(20./scale * j)/25.0) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
        }
      }

      if (entype <=2) {
                            /* blades */
        offsGg.setColor(Color.white) ;
        for (j=1; j<=ncompd; ++j) {
           exes[0] = (int) (xl + factor*(.02 +(j-1)*(tblade+sblade))) ;
           whys[0] = (int) (factor*hblade + ytrans)  ;
           exes[1] = exes[0] + (int) (factor*tblade) ;
           whys[1] = whys[0] ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*-hblade + ytrans) ;
           exes[3] = exes[0] ;
           whys[3] = whys[2] ;
           offsGg.fillPolygon(exes,whys,4) ;
        }

        if (entype == 2) {                        /*  fan blades */
          offsGg.setColor(Color.white) ;
          for (j=1; j<=3; ++j) {
            if (j==3 && bcol == 0) offsGg.setColor(Color.black) ;
            if (j==3 && bcol == 7) offsGg.setColor(Color.white) ;
            exes[0] = (int) (xl + factor*(.02 +(j-1)*(tblade+sblade))) ;
            whys[0] = (int) (factor*fblade + ytrans) ;
            exes[1] = exes[0] + (int) (factor*tblade) ;
            whys[1] = whys[0] ;
            exes[2] = exes[1] ;
            whys[2] = (int) (factor*-fblade + ytrans) ;
            exes[3] = exes[0] ;
            whys[3] = whys[2] ;
            offsGg.fillPolygon(exes,whys,4) ;
          }
        }
                             /* core */
        offsGg.setColor(Color.cyan) ;
        if (varflag == 4) offsGg.setColor(Color.yellow) ;
        offsGg.fillArc((int)(xl-factor*radius),(int)(yl-factor*radius),
           (int)(2.0*factor*radius),(int)(2.0*factor*radius),90,180) ;
        exes[0] = (int) (xl) ;
        whys[0] = (int) (factor*radius + ytrans);
        exes[1] = (int) (factor*xcomp + xtrans) ;
        whys[1] = (int) (factor * 1.5 * radius + ytrans) ;
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*-1.5*radius + ytrans) ;
        exes[3] = exes[0];
        whys[3] = (int) (factor*-radius + ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
        if (entype == 2) {  // fan
          offsGg.setColor(Color.green) ;
          if (varflag == 3) offsGg.setColor(Color.yellow) ;
          offsGg.fillArc((int)(xl-factor*radius),(int)(yl-factor*radius),
             (int)(2.0*factor*radius),(int)(2.0*factor*radius),90,180) ;
          exes[0] = (int) (xl) ;
          whys[0] = (int) (factor*radius + ytrans);
          exes[1] = (int) (factor*xfan + xtrans) ;
          whys[1] = (int) (factor * 1.2 * radius + ytrans) ;
          exes[2] = exes[1] ;
          whys[2] = (int) (factor*-1.2*radius + ytrans) ;
          exes[3] = exes[0];
          whys[3] = (int) (factor*-radius + ytrans);
          offsGg.fillPolygon(exes,whys,4) ;
        }
    /* combustor */
        offsGg.setColor(Color.black) ;
        exes[0] = (int) (factor*xcomp + xtrans) ;
        whys[0] = (int) (factor*hblade + ytrans);
        exes[1] = (int) (factor*xburn + xtrans) ;
        whys[1] = (int) (factor*hblade + ytrans);
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*-hblade + ytrans);
        exes[3] = exes[0] ;
        whys[3] = whys[2] ;
        offsGg.fillPolygon(exes,whys,4) ;
  
        offsGg.setColor(Color.white) ;
        xl = xcomp + .05 + rburn ;
        yl = .6*hblade ;
        offsGg.drawArc((int)(factor*(xl-rburn)+xtrans),(int)(factor*(yl-rburn)+ytrans),
            (int)(2.0*factor*rburn),(int)(2.0*factor*rburn),90,180) ;
        offsGg.drawArc((int)(factor*(xl-rburn)+xtrans),(int)(factor*(-yl-rburn)+ytrans),
            (int)(2.0*factor*rburn),(int)(2.0*factor*rburn),90,180) ;  
                                     /* core */
        offsGg.setColor(Color.red) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xcomp +xtrans) ;
        whys[0] = (int) (factor*1.5 * radius + ytrans);
        exes[1] = (int) (factor*xcomp +.25*lburn + xtrans) ;
        whys[1] = (int) (factor*.8*radius + ytrans) ;
        exes[2] = (int) (factor*(xcomp + .75*lburn) + xtrans) ;
        whys[2] = (int) (factor*.8*radius + ytrans);
        exes[3] = (int) (factor*xburn + xtrans) ;
        whys[3] = (int) (factor* 1.5 * radius + ytrans) ;
        exes[4] = exes[3];
        whys[4] = (int) (factor*-1.5 * radius + ytrans) ;
        exes[5] = exes[2] ;
        whys[5] = (int) (factor*-.8*radius + ytrans) ;
        exes[6] = exes[1] ;
        whys[6] = (int) (factor*-.8*radius + ytrans) ;
        exes[7] = exes[0] ;
        whys[7] = (int) (factor*-1.5 * radius + ytrans);
        offsGg.fillPolygon(exes,whys,8) ;
   /* turbine */
                            /* blades */
        for (j=1; j<=nturb; ++j) {
           offsGg.setColor(Color.white) ;
           if (entype == 2) {
              if (j==(nturb-1) && bcol == 0) offsGg.setColor(Color.black) ;
              if (j==(nturb-1) && bcol == 7) offsGg.setColor(Color.white) ;
           }
           exes[0] = (int) (factor*(xburn +.02 +(j-1)*(tblade+sblade))+xtrans) ;
           whys[0] = (int) (factor*hblade+ytrans) ;
           exes[1] = exes[0] + (int) (factor*tblade) ;
           whys[1] = whys[0] ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*-hblade+ytrans) ;
           exes[3] = exes[0] ;
           whys[3] = whys[2] ;
           offsGg.fillPolygon(exes,whys,4) ;
        }
                           /* core */
        offsGg.setColor(Color.magenta) ;
        if (varflag == 6) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xburn+xtrans) ;
        whys[0] = (int) (factor*1.5*radius+ytrans);
        exes[1] = (int) (factor*xnoz +xtrans) ;
        whys[1] = (int) (factor*0.0 + ytrans) ;
        exes[2] = exes[0];
        whys[2] = (int) (factor*-1.5*radius+ytrans);
        offsGg.fillPolygon(exes,whys,3) ;
    /* afterburner */
        if(entype == 1) {
           if (dcol == 0) offsGg.setColor(Color.black) ;
           if (dcol == 7) offsGg.setColor(Color.white) ;
           if (varflag == 7) offsGg.setColor(Color.yellow) ;
           exes[0] = (int) (factor*(xflame - .1*lnoz)  + xtrans) ;
           whys[0] = (int) (factor*.6*hblade+ytrans) ;
           exes[1] = (int) (factor*(xflame -.2*lnoz) + xtrans) ;
           whys[1] = (int) (factor*.5*hblade+ytrans) ;
           exes[2] = (int) (factor*(xflame  -.1*lnoz) + xtrans) ;
           whys[2] = (int) (factor*.4*hblade+ytrans) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
           whys[0] = (int) (factor*-.6*hblade+ytrans) ;
           whys[1] = (int) (factor*-.5*hblade+ytrans) ;
           whys[2] = (int) (factor*-.4*hblade+ytrans) ;
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           offsGg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
        }

  /* cowl */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (entype == 0 ) {   /*   turbojet  */
           if (varflag == 2) offsGg.setColor(Color.yellow) ;
           xl = xcowl + liprad ;                /*   core cowl */
           yl = rcowl ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(yl-liprad)+ytrans),
             (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(-yl-liprad)+ytrans),
               (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ;
           exes[0] = (int) (factor*xl + xtrans) ;
           whys[0] = (int) (factor*(yl +liprad) + ytrans);
           exes[1] = (int) (factor*-radius + xtrans) ;
           whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
           exes[2] = exes[1];
           whys[2] = (int) (factor*hblade+ytrans);
           exes[3] = exes[0];
           whys[3] = (int) (factor*(yl -liprad)+ytrans) ;
           offsGg.fillPolygon(exes,whys,4) ;
           whys[0] = (int) (factor*(-yl -liprad) + ytrans) ;
           whys[1] = (int) (factor*(-hblade-liprad) +ytrans)  ;
           whys[2] = (int) (factor*-hblade+ytrans);
           whys[3] = (int) (factor*(-yl +liprad) + ytrans);
           offsGg.fillPolygon(exes,whys,4) ;
                                      // compressor
           offsGg.setColor(Color.cyan) ;
           if (varflag == 4) offsGg.setColor(Color.yellow) ;
           exes[0] = (int) (factor*-radius + xtrans) ;
           whys[0] = (int) (factor*(hblade+liprad)+ytrans)  ;
           exes[1] = (int) (factor*xcomp+xtrans) ;
           whys[1] = whys[0] ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*.8* hblade+ytrans) ;
           exes[3] = (int) (factor*.02 + xtrans);
           whys[3] = (int) (factor*hblade+ytrans);
           exes[4] = exes[0];
           whys[4] = whys[3];
           offsGg.fillPolygon(exes,whys,5) ;

           whys[0] = (int) (factor*(-hblade-liprad) +ytrans)  ;
           whys[1] = whys[0]  ;
           whys[2] = (int) (factor*-.8* hblade+ytrans) ;
           whys[3] = (int) (factor*-hblade+ytrans);
           whys[4] = whys[3];
           offsGg.fillPolygon(exes,whys,5) ;
        }
        if (entype == 1) {            /*   fighter plane  */
           offsGg.setColor(Color.white) ;
           if (varflag == 2) offsGg.setColor(Color.yellow) ;
           xl = xcowl + liprad ;                     /*   inlet */
           yl = rcowl ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(yl-liprad)+ytrans),
              (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ; 
           exes[0] = (int) (factor*xl + xtrans) ;
           whys[0] = (int) (factor*(yl +liprad) + ytrans) ;
           exes[1] = (int) (factor*-radius + xtrans) ;
           whys[1] = (int) (factor*(hblade+liprad) +ytrans)  ;
           exes[2] = exes[1];
           whys[2] = (int) (factor*hblade+ytrans);
           exes[3] = exes[0];
           whys[3] = (int) (factor*(yl -liprad)+ytrans) ;
           offsGg.fillPolygon(exes,whys,4) ;
           exes[0] = (int) (factor*(xl + 1.5*xcowl)+xtrans) ;
           whys[0] = (int) (factor*(-hblade -liprad)+ytrans);
           exes[1] = (int) (factor*-radius + xtrans) ;
           whys[1] = whys[0] ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*-hblade+ytrans);
           exes[3] = (int) (factor*xl + xtrans) ;
           whys[3] = (int) (factor*-.7*hblade + ytrans) ;
           exes[4] = exes[0];
           whys[4] = whys[0];
           offsGg.fillPolygon(exes,whys,5) ;
                                      // compressor
           offsGg.setColor(Color.cyan) ;
           if (varflag == 4) offsGg.setColor(Color.yellow) ;
           exes[0] = (int) (factor*-radius + xtrans) ;
           whys[0] = (int) (factor*(hblade+liprad)+ytrans)  ;
           exes[1] = (int) (factor*xcomp+xtrans) ;
           whys[1] = whys[0] ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*.8* hblade+ytrans) ;
           exes[3] = (int) (factor*.02 + xtrans);
           whys[3] = (int) (factor*hblade+ytrans);
           exes[4] = exes[0];
           whys[4] = whys[3];
           offsGg.fillPolygon(exes,whys,5) ;
  
           whys[0] = (int) (factor*(-hblade-liprad) +ytrans)  ;
           whys[1] = whys[0]  ;
           whys[2] = (int) (factor*-.8* hblade+ytrans) ;
           whys[3] = (int) (factor*-hblade+ytrans);
           whys[4] = whys[3];
           offsGg.fillPolygon(exes,whys,5) ;
        }
        if(entype == 2) {                                  /* fan jet */
           if (dcol == 0) offsGg.setColor(Color.black) ;
           if (dcol == 7) offsGg.setColor(Color.white) ;
           if (varflag == 2) offsGg.setColor(Color.yellow) ;
           xl = xcowl + liprad ;                     /*   fan cowl inlet */
           yl = rcowl ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(yl-liprad)+ytrans),
              (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(-yl-liprad)+ytrans),
              (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ; 
           exes[0] = (int) (factor*xl + xtrans) ;
           whys[0] = (int) (factor*(yl +liprad) + ytrans);
           exes[1] = (int) (factor*-radius + xtrans) ;
           whys[1] = (int) (factor*(fblade+liprad)+ytrans)  ;
           exes[2] = exes[1];
           whys[2] = (int) (factor*fblade+ytrans);
           exes[3] = exes[0];
           whys[3] = (int) (factor*(yl -liprad)+ytrans) ;
           offsGg.fillPolygon(exes,whys,4) ;
  
           whys[0] = (int) (factor*(-yl -liprad)+ytrans);
           whys[1] = (int) (factor*(-fblade-liprad)+ytrans)  ;
           whys[2] = (int) (factor*-fblade+ytrans);
           whys[3] = (int) (factor*(-yl +liprad) + ytrans);
           offsGg.fillPolygon(exes,whys,4) ;
  
           offsGg.setColor(Color.green) ;
           if (varflag == 3) offsGg.setColor(Color.yellow) ;
           xl = xcowl + liprad ;                     /*   fan cowl */
           yl = rcowl ;
           exes[0] = (int) (factor*-radius + xtrans) ;
           whys[0] = (int) (factor*(fblade+liprad)+ytrans)  ;
           exes[1] = (int) (factor*xcomp/2.0+xtrans) ;
           whys[1] = whys[0]  ;
           exes[2] = (int) (factor*xcomp +xtrans) ;
           whys[2] = (int) (factor*fblade+ytrans);
           exes[3] = (int) (factor*.02 + xtrans);
           whys[3] = (int) (factor*fblade+ytrans);
           exes[4] = exes[0];
           whys[4] = whys[3];
           offsGg.fillPolygon(exes,whys,5) ;
  
           whys[0] = (int) (factor*(-fblade-liprad)+ytrans)  ;
           whys[1] = whys[0] ;
           whys[2] = (int) (factor*-fblade+ytrans);
           whys[3] = whys[2];
           whys[4] = whys[2];
           offsGg.fillPolygon(exes,whys,5) ;
  
           xl = xfan + .02 ;             /* core cowl */
           yl = hblade ;
           offsGg.setColor(Color.cyan) ;
           if (varflag == 4) offsGg.setColor(Color.yellow) ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(yl-liprad)+ytrans), 
                (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ;
           offsGg.fillArc((int)(factor*(xl-liprad)+xtrans),(int)(factor*(-yl-liprad)+ytrans),
                (int)(2.0*factor*liprad),(int)(2.0*factor*liprad),90,180) ;
           exes[0] = (int) (factor*(xl-.01)+xtrans);
           whys[0] = (int) (factor*(hblade +liprad)+ytrans)  ;
           exes[1] = (int) (factor*xcomp+xtrans) ;
           whys[1] = whys[0]  ;
           exes[2] = exes[1] ;
           whys[2] = (int) (factor*(.8* hblade)+ytrans) ;
           exes[3] = exes[0];
           whys[3] = (int) (factor*(hblade -liprad) + ytrans);
           offsGg.fillPolygon(exes,whys,4) ;
  
           whys[0] = (int) (factor*(-hblade -liprad)+ytrans);
           whys[1] = whys[0]  ;
           whys[2] = (int) (factor*-.8* hblade+ytrans) ;
           whys[3] = (int) (factor*(-hblade +liprad) + ytrans);
           offsGg.fillPolygon(exes,whys,4) ;
        }
                                                       /* combustor */
        offsGg.setColor(Color.red) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xcomp+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xburn+xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*.8 * hblade+ytrans) ;
        exes[3] = (int) (factor*(xcomp + .75*lburn) + xtrans);
        whys[3] = (int) (factor*.9 * hblade +ytrans);
        exes[4] = (int) (factor*(xcomp + .25*lburn) +xtrans);
        whys[4] = (int) (factor*.9 * hblade + ytrans);
        exes[5] = (int) (factor*xcomp + xtrans) ;
        whys[5] = (int) (factor*.8 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,6) ;
  
        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*(-hblade-liprad)+ytrans)  ;
        whys[2] = (int) (factor*-.8 * hblade+ytrans) ;
        whys[3] = (int) (factor*-.9 * hblade +ytrans);
        whys[4] = (int) (factor*-.9 * hblade + ytrans);
        whys[5] = (int) (factor*-.8 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,6) ;
                                                        /* turbine */
        offsGg.setColor(Color.magenta) ;
        if (varflag == 6) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xburn+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xturb +xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[2] = exes[1];
        whys[2] = (int) (factor*.9 * hblade +ytrans) ;
        exes[3] = (int) (factor*xburn+xtrans);
        whys[3] = (int) (factor*.8 * hblade +ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
  
        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*(-hblade-liprad)+ytrans)  ;
        whys[2] = (int) (factor*-.9 * hblade +ytrans) ;
        whys[3] = (int) (factor*-.8 * hblade +ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
                                                       /* nozzle */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 7) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xturb+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xflame + xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans);
        exes[2] = (int) (factor*xit+ xtrans)  ;
        whys[2] = (int) (factor*rnoz+ytrans)  ;
        exes[3] = (int) (factor*xflame + xtrans) ;
        whys[3] = (int) (factor*.9 * hblade+ytrans);
        exes[4] = (int) (factor*xturb+xtrans);
        whys[4] = (int) (factor*.9 * hblade+ytrans) ;
        offsGg.fillPolygon(exes,whys,5) ;

        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[2] = (int) (factor*-rnoz+ytrans)  ;
        whys[3] = (int) (factor*-.9 * hblade+ytrans);
        whys[4] = (int) (factor*-.9 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,5) ;
                                             //   show stations 
        if (showcom == 1 && inflag == 0) {
           offsGg.setColor(Color.white) ;
           ylabel = (int) (factor*1.5*hblade + 20. + ytrans) ;
           whys[1] = 370 ;
    
           xl = xcomp -.1 ;                   /* burner entrance */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("3",xlabel,ylabel) ; 
    
           xl = xburn - .1 ;                   /* turbine entrance */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("4",xlabel,ylabel) ; 
  
           xl = xnoz ;            /* Afterburner entry */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("6",xlabel,ylabel) ; 
      
           if (entype == 1) {
              xl = xflame ;               /* Afterburner exit */
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*.2 + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] + (int) (factor*.05) ;
              offsGg.drawString("7",xlabel,ylabel) ; 
           }
    
           xl = xit ;                    /* nozzle exit */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*.2 + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] - (int) (factor* .2) ;
           offsGg.drawString("8",xlabel,ylabel) ; 
    
           if (entype < 2) {
              xl = -radius ;                   /* compressor entrance */
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*(hblade - .2) + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] + (int) (factor* .05) ;
              offsGg.drawString("2",xlabel,ylabel) ; 
    
              xl = xturb+.1 ;                   /* turbine exit */
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*(hblade - .2) + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] + (int) (factor*.05) ;
              offsGg.drawString("5",xlabel,ylabel) ; 
           }
           if (entype == 2) {
              xl = xturbh ;               /*high pressturbine exit*/
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*(hblade - .2) + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] + (int) (factor*.05) ;
              offsGg.drawString("5",xlabel,ylabel) ; 
    
              xl = 0.0 - .1 ;                            /* fan entrance */
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*(hblade - .2) + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] - (int) (factor* .2) ;
              offsGg.drawString("1",xlabel,ylabel) ; 
   
              xl = 3.0*tblade ;                            /* fan exit */
              exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
              whys[0] = (int) (factor*(hblade - .2) + ytrans);
              offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              xlabel = exes[0] + (int) (factor* .12) ;
              offsGg.drawString("2",xlabel,ylabel) ;
  
           }
      
           xl =  - 2.0 ;                   /* free stream */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("0",xlabel,ylabel) ; 
        }
                             // labels
        offsGg.setColor(Color.black) ;
        offsGg.fillRect(0,27,350,15) ;
        if(inflag == 0) {
          offsGg.setColor(Color.white) ;
          if (varflag == 2) offsGg.setColor(Color.yellow) ;
          offsGg.drawString("Inlet",60,40) ; 
          if (entype == 2) { 
            offsGg.setColor(Color.green) ;
            if (varflag == 3) offsGg.setColor(Color.yellow) ;
            offsGg.drawString("Fan",90,40) ; 
          }
          offsGg.setColor(Color.cyan) ;
          if (varflag == 4) offsGg.setColor(Color.yellow) ;
          offsGg.drawString("Compressor",120,40) ; 
          offsGg.setColor(Color.red) ;
          if (varflag == 5) offsGg.setColor(Color.yellow) ;
          offsGg.drawString("Burner",200,40) ; 
          offsGg.setColor(Color.magenta) ;
          if (varflag == 6) offsGg.setColor(Color.yellow) ;
          offsGg.drawString("Turbine",250,40) ; 
          offsGg.setColor(Color.white) ;
          if (varflag == 7) offsGg.setColor(Color.yellow) ;
          offsGg.drawString("Nozzle",300,40) ; 
        }
      }

      if (entype == 3) {                  //ramjet geom
                             /* inlet spike */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        exes[0] = (int) (factor*-2.0 + xtrans) ;
        whys[0] = (int) (0.0 + ytrans);
        exes[1] = (int) (factor*xcomp + xtrans) ;
        whys[1] = (int) (factor * 1.5 * radius + ytrans) ;
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*-1.5*radius + ytrans) ;
        exes[3] = exes[0];
        whys[3] = (int) (0.0 + ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
                                   /* spraybars */
        offsGg.setColor(Color.white) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        xl = xcomp + .05 + rburn ;
        yl = .6*hblade ;
        offsGg.drawArc((int)(factor*(xl-rburn)+xtrans),(int)(factor*(yl-rburn)+ytrans),
            (int)(2.0*factor*rburn),(int)(2.0*factor*rburn),90,180) ;
        offsGg.drawArc((int)(factor*(xl-rburn)+xtrans),(int)(factor*(-yl-rburn)+ytrans),
            (int)(2.0*factor*rburn),(int)(2.0*factor*rburn),90,180) ;  
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        exes[0] = (int) (factor*xcomp +xtrans) ;
        whys[0] = (int) (factor*1.5 * radius + ytrans);
        exes[1] = (int) (factor*xcomp +.25*lburn + xtrans) ;
        whys[1] = (int) (factor*.8*radius + ytrans) ;
        exes[2] = (int) (factor*(xcomp + .75*lburn) + xtrans) ;
        whys[2] = (int) (factor*.8*radius + ytrans);
        exes[3] = (int) (factor*xburn + xtrans) ;
        whys[3] = (int) (factor* 1.5 * radius + ytrans) ;
        exes[4] = exes[3];
        whys[4] = (int) (factor*-1.5 * radius + ytrans) ;
        exes[5] = exes[2] ;
        whys[5] = (int) (factor*-.8*radius + ytrans) ;
        exes[6] = exes[1] ;
        whys[6] = (int) (factor*-.8*radius + ytrans) ;
        exes[7] = exes[0] ;
        whys[7] = (int) (factor*-1.5 * radius + ytrans);
        offsGg.fillPolygon(exes,whys,8) ;
                           /* aft cone */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        exes[0] = (int) (factor*xburn+xtrans) ;
        whys[0] = (int) (factor*1.5*radius+ytrans);
        exes[1] = (int) (factor*xnoz +xtrans) ;
        whys[1] = (int) (factor*0.0 + ytrans) ;
        exes[2] = exes[0];
        whys[2] = (int) (factor*-1.5*radius+ytrans);
        offsGg.fillPolygon(exes,whys,3) ;
                             /* fame holders */
        offsGg.setColor(Color.white) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*(xnoz +.2*lnoz)  + xtrans) ;
        whys[0] = (int) (factor*.6*hblade+ytrans) ;
        exes[1] = (int) (factor*(xnoz +.1*lnoz) + xtrans) ;
        whys[1] = (int) (factor*.5*hblade+ytrans) ;
        exes[2] = (int) (factor*(xnoz +.2*lnoz) + xtrans) ;
        whys[2] = (int) (factor*.4*hblade+ytrans) ;
        offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
        offsGg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;
        whys[0] = (int) (factor*-.6*hblade+ytrans) ;
        whys[1] = (int) (factor*-.5*hblade+ytrans) ;
        whys[2] = (int) (factor*-.4*hblade+ytrans) ;
        offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
        offsGg.drawLine(exes[1],whys[1],exes[2],whys[2]) ;

        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 2) offsGg.setColor(Color.yellow) ;

        xl = xcowl + liprad ;                /*   core cowl */
        yl = rcowl ;
        exes[0] = (int) (factor*xl + xtrans) ;
        whys[0] = (int) (factor*(yl) + ytrans);
        exes[1] = (int) (factor*-radius + xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[2] = exes[1];
        whys[2] = (int) (factor*hblade+ytrans);
        exes[3] = exes[0];
        whys[3] = (int) (factor*(yl)+ytrans) ;
        offsGg.fillPolygon(exes,whys,4) ;
        whys[0] = (int) (factor*(-yl) + ytrans) ;
        whys[1] = (int) (factor*(-hblade-liprad) +ytrans)  ;
        whys[2] = (int) (factor*-hblade+ytrans);
        whys[3] = (int) (factor*(-yl) + ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
                                      // compressor
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 2) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*-radius + xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[1] = (int) (factor*xcomp+xtrans) ;
        whys[1] = whys[0] ;
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*.8* hblade+ytrans) ;
        exes[3] = (int) (factor*.02 + xtrans);
        whys[3] = (int) (factor*hblade+ytrans);
        exes[4] = exes[0];
        whys[4] = whys[3];
        offsGg.fillPolygon(exes,whys,5) ;

        whys[0] = (int) (factor*(-hblade-liprad) +ytrans)  ;
        whys[1] = whys[0]  ;
        whys[2] = (int) (factor*-.8* hblade+ytrans) ;
        whys[3] = (int) (factor*-hblade+ytrans);
        whys[4] = whys[3];
        offsGg.fillPolygon(exes,whys,5) ;
                                                       /* combustor */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xcomp+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xburn+xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[2] = exes[1] ;
        whys[2] = (int) (factor*.8 * hblade+ytrans) ;
        exes[3] = (int) (factor*(xcomp + .75*lburn) + xtrans);
        whys[3] = (int) (factor*.9 * hblade +ytrans);
        exes[4] = (int) (factor*(xcomp + .25*lburn) +xtrans);
        whys[4] = (int) (factor*.9 * hblade + ytrans);
        exes[5] = (int) (factor*xcomp + xtrans) ;
        whys[5] = (int) (factor*.8 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,6) ;
  
        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*(-hblade-liprad)+ytrans)  ;
        whys[2] = (int) (factor*-.8 * hblade+ytrans) ;
        whys[3] = (int) (factor*-.9 * hblade +ytrans);
        whys[4] = (int) (factor*-.9 * hblade + ytrans);
        whys[5] = (int) (factor*-.8 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,6) ;
                                                        /* turbine */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 5) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xburn+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xturb +xtrans) ;
        whys[1] = (int) (factor*(hblade+liprad)+ytrans)  ;
        exes[2] = exes[1];
        whys[2] = (int) (factor*.9 * hblade +ytrans) ;
        exes[3] = (int) (factor*xburn+xtrans);
        whys[3] = (int) (factor*.8 * hblade +ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
  
        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*(-hblade-liprad)+ytrans)  ;
        whys[2] = (int) (factor*-.9 * hblade +ytrans) ;
        whys[3] = (int) (factor*-.8 * hblade +ytrans);
        offsGg.fillPolygon(exes,whys,4) ;
                                                       /* nozzle */
        if (dcol == 0) offsGg.setColor(Color.black) ;
        if (dcol == 7) offsGg.setColor(Color.white) ;
        if (varflag == 7) offsGg.setColor(Color.yellow) ;
        exes[0] = (int) (factor*xturb+xtrans) ;
        whys[0] = (int) (factor*(hblade+liprad)+ytrans);
        exes[1] = (int) (factor*xit+ xtrans)  ;
        whys[1] = (int) (factor*rnoz+ytrans)  ;
        exes[2] = (int) (factor*xflame + xtrans) ;
        whys[2] = (int) (factor*rthroat+ytrans);
        exes[3] = (int) (factor*xturb+xtrans);
        whys[3] = (int) (factor*.9 * hblade+ytrans) ;
        offsGg.fillPolygon(exes,whys,4) ;

        whys[0] = (int) (factor*(-hblade-liprad)+ytrans);
        whys[1] = (int) (factor*-rnoz+ytrans)  ;
        whys[2] = (int) (factor*-rthroat+ytrans);
        whys[3] = (int) (factor*-.9 * hblade+ytrans);
        offsGg.fillPolygon(exes,whys,4) ;

                                             //   show stations 
        if (showcom == 1 && inflag == 0) {
           offsGg.setColor(Color.white) ;
           ylabel = (int) (factor*1.5*hblade + 20. + ytrans) ;
           whys[1] = 370 ;
    
           xl = xcomp -.1 ;                   /* burner entrance */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("3",xlabel,ylabel) ; 
    
           xl = xnoz +.1*lnoz ;        /* flame holders */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*.2 + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("4",xlabel,ylabel) ; 

           xl = xflame ;               /* Afterburner exit */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*.2 + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("7",xlabel,ylabel) ; 
    
           xl = xit ;                    /* nozzle exit */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*.2 + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] - (int) (factor* .2) ;
           offsGg.drawString("8",xlabel,ylabel) ; 
    
           xl =  - 2.0 ;                   /* free stream */
           exes[0] = exes[1] = (int) (factor*xl +xtrans) ;
           whys[0] = (int) (factor*(hblade - .2) + ytrans);
           offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
           xlabel = exes[0] + (int) (factor*.05) ;
           offsGg.drawString("0",xlabel,ylabel) ; 
        }
                             // labels
        offsGg.setColor(Color.black) ;
        offsGg.fillRect(0,27,350,15) ;
        if (inflag == 0) {
           offsGg.setColor(Color.white) ;
           if (varflag == 2) offsGg.setColor(Color.yellow) ;
           offsGg.drawString("Inlet",60,40) ; 
           offsGg.setColor(Color.white) ;
           if (varflag == 5) offsGg.setColor(Color.yellow) ;
           offsGg.drawString("Burner",150,40) ; 
           offsGg.setColor(Color.white) ;
           if (varflag == 7) offsGg.setColor(Color.yellow) ;
           offsGg.drawString("Nozzle",300,40) ;
        } 
      }
    /* animated flow */
      for (j=1 ; j<=8 ; ++ j) {
           exes[1] = (int) (factor*xg[j][0] + xtrans) ;
           whys[1] = (int) (factor*yg[j][0] + ytrans);
           for (i=1 ; i<= 34; ++i) {
              exes[0] = exes[1] ;
              whys[0] = whys[1] ;
              exes[1] = (int) (factor*xg[j][i] + xtrans) ;
              whys[1] = (int) (factor*yg[j][i] + ytrans);
              if ((i-antim)/3*3 == (i-antim)) {
                if (i< 15) {
                   if (ancol == -1) {
                     if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.white);
                     if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.cyan);
                   }
                   if (ancol == 1) {
                     if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.cyan);
                     if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.white);
                   }
                }
                if (i >= 16) {
                   if (ancol == -1) {
                     if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.yellow);
                     if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.red);
                   }
                   if (ancol == 1) {
                     if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.red);
                     if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.yellow);
                   }
                }
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
              }
           }
      }
      if (entype == 2) {   // fan flow
         for (j=9 ; j<=12 ; ++ j) {
           exes[1] = (int) (factor*xg[j][0] + xtrans) ;
           whys[1] = (int) (factor*yg[j][0] + ytrans);
           for (i=1 ; i<= 34; ++i) {
              exes[0] = exes[1] ;
              whys[0] = whys[1] ;
              exes[1] = (int) (factor*xg[j][i] + xtrans) ;
              whys[1] = (int) (factor*yg[j][i] + ytrans);
              if ((i-antim)/3*3 == (i-antim)) {
                if (ancol == -1) {
                  if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.white);
                  if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.cyan);
                }
                if (ancol == 1) {
                  if((i-antim)/6*6 ==(i-antim))offsGg.setColor(Color.cyan);
                  if((i-antim)/6*6 !=(i-antim))offsGg.setColor(Color.white);
                }
                offsGg.drawLine(exes[0],whys[0],exes[1],whys[1]) ;
             }
           }
         }
      }
         // engine type labels
      offsGg.setColor(Color.blue) ;
      offsGg.fillRect(0,0,350,13) ;
      offsGg.setColor(Color.white) ;
      offsGg.drawString("EngineSim 1.8a  -- Student Version",100,12) ; 

      offsGg.setColor(Color.black) ;
      offsGg.fillRect(0,14,350,14) ;
      if (inflag == 0) {
        offsGg.setColor(Color.green) ;
        if (entype == 0) {
          offsGg.setColor(Color.yellow) ;
          offsGg.fillRect(50,15,60,12) ;
          offsGg.setColor(Color.black) ;
        }
        offsGg.drawString("Turbojet",60,25) ; 
        offsGg.setColor(Color.green) ;
        if (entype == 1) {
          offsGg.setColor(Color.yellow) ;
          offsGg.fillRect(111,15,80,12) ;
          offsGg.setColor(Color.black) ;
        }
        offsGg.drawString("Afterburner",125,25) ; 
        offsGg.setColor(Color.green) ;
        if (entype == 2) {
          offsGg.setColor(Color.yellow) ;
          offsGg.fillRect(192,15,70,12) ;
          offsGg.setColor(Color.black) ;
        }
        offsGg.drawString("Turbo Fan",200,25) ; 
        offsGg.setColor(Color.green) ;
        if (entype == 3) {
          offsGg.setColor(Color.yellow) ;
          offsGg.fillRect(263,15,90,12) ;
          offsGg.setColor(Color.black) ;
        }
        offsGg.drawString("Ramjet",275,25) ; 

        offsGg.setColor(Color.white) ;
        if (varflag == 0) offsGg.setColor(Color.yellow) ;
        offsGg.drawString("Flight",5,40) ; 
        offsGg.setColor(Color.white) ;
        if (varflag == 1) offsGg.setColor(Color.yellow) ;
        offsGg.drawString("Size",5,25) ;
      }
                                 // zoom widget
      if (inflag == 0) {
        offsGg.setColor(Color.black) ;
        offsGg.fillRect(0,42,35,200) ;
        offsGg.setColor(Color.cyan) ;
        offsGg.drawString("Zoom",5,180) ;
        offsGg.drawLine(15,50,15,165) ;
        offsGg.fillRect(5,sldloc,20,5) ;
        offsGg.setColor(Color.cyan) ;
        offsGg.drawString("Find",5,210);
      }
 //   }

    if (inflag == 1) {           // load animation
       offsGg.setColor(Color.black) ;
 //      offsGg.fillRect(0,14,350,350) ;
 //      offsGg.drawString("No Image",60,100) ; 
 //      offsGg.drawImage(displimg,0,20,this) ;
    }
                               // temp limit warning  
    if (fireflag == 1) {
       offsGg.setColor(Color.yellow) ;
       offsGg.fillRect(50,80,200,30) ;
       if(counter==1)offsGg.setColor(Color.black) ;
       if(counter>=2)offsGg.setColor(Color.white) ;
       offsGg.fillRect(55,85,190,20) ;
       offsGg.setColor(Color.red) ;
       offsGg.drawString("Temperature  Limits Exceeded",60,100) ; 
    }

    g.drawImage(offscreenImg,0,0,this) ;   
  }

 }
}
