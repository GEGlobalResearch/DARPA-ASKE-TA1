/*
                Hypersonic Isentropic Flow  Program

     Program to perform one dimensional isentropic flow analysis 
                        from NACA 1135
           Including Thermally perfect gas effects

                     Version 1.3a   - 18 July 06

                            Written by 

                            Tom Benson
                           Chuck Trefny
                           Charles Lamp
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


      New Test :  * Derive from Nozzle program and vucalc
                  * Two panels -- input (Up) - output (Dn)
                  * add compute button
                  * add calorically imperfect gas
                  * move input variables


                                                     TJB 18 July 06
*/


import java.awt.*;
import java.lang.Math ;

public class Isentrop extends java.applet.Applet {

   final double convdr = 3.14515926/180.;
   
   static double gama, gm1, gp1 ;
   static double mach1, mmin, mmax ;
   static double arat, amin, amax ;
   static double prat, pmin, pmax ;
   static double trat, tmin, tmax ;
   static double rhorat, rhomin, rhomax ;
   static double qrat, qmin, qmax ;
   static double wcor, wmin, wmax ;
   static double nu, numin, numax ;
   static double mu, mumin, mumax ;
   static double inptval ;
   static int inopt, isub, itype ;
   double altmin, altmax, tempmin, tempmax, typeinpt;
   double alt, Q, TT, rgas, T;
   double trattp, M, prattp, rhorattp;
   double arattp, qrattp, wcortp;
   double mutp, nutp, G, R;
   double itpmach, itpprat, itptrat, itprhorat;
   double itparat, itpmu, itpnu, itpwcor, itpqrat;

   In in ;

   public void init() {

     setLayout(new GridLayout(1,2,0,0)) ;

     setDefaults () ;

     in = new In(this) ;

     add(in) ;

     comPute() ;
  }

  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }

  public void setDefaults() {

       inopt = 0 ;
       isub = 0 ;
       inptval = 2.0 ;
	   itype = 0;
	   typeinpt = 0;
       gama = 1.4 ;
 
       mmin = .01;  mmax = 15 ;
       pmin = .000001;  pmax = .990 ;
       tmin = .005;  tmax = .990;
       rhomin = .0005;  rhomax = .990 ;
       qmin = .0005;  qmax =  400. ;
       amin = 1.09 ;  amax = 60.0  ;
       wmin = .0005  ;  wmax = .320 ;
       mumin = 2.0  ;  mumax = 90.0 ;
       numin = 2.0  ;  numax = 120. ;
	   altmin = 0   ; altmax = 262448;
	   tempmin = 500  ; tempmax = 5000;
	   Q = 5500     ; R = 53.3;
  }

  double TQTT(double M, double G)
  {
     return Math.pow((1 + (G - 1) / 2 * Math.pow(M, 2)), -1);
  }

  double CAL_GAM (double T, double G, double Q)
  {
     return (1 + (G - 1) / (1 + (G - 1) * (Math.pow((Q / T), 2) * 
        Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2))));
  }

  double CAL_TT (double T, double M, double G, double Q)
  {
     double TT = T / TQTT(M,G);
     double EPS = 0.00001;
     double Z = 1;
     double Z2 = 0;
     double TT2 = 0;
     double TT1;
     double Z1;
     double i = 1;
     while (Math.abs(Z) > EPS)
     {
        Z = Math.pow(M, 2) - 2 * TT / CAL_GAM(T, G, Q) / T * 
         (G / (G - 1) * (1 - T / TT) + Q / TT * 
         (1 / (Math.exp(Q / TT) - 1) - 1 / (Math.exp(Q / T) - 1)));
        if (i == 1)
        {
          Z2=Z;
          TT2 = TT;
          TT = TT2 + 100;
          i = 2;
        }
        else
        {
           TT1 = TT2;
           Z1 = Z2;
           TT2 = TT;
           Z2 = Z;
           TT = TT2 - Z2 * (TT2 - TT1) / (Z2 - Z1);
        }
     }
     return TT;
  }

  double CAL_T (double TT, double M, double G, double Q)
  {
     double T = TT * TQTT(M,G);
     double EPS = 0.00001;
     double Z=1;
     double Z2 = 0;
     double T2 = 0;
     double T1;
     double Z1;
     double i=1;
     while (Math.abs(Z) > EPS)
     {
        Z = Math.pow(M, 2) - 2 * TT / CAL_GAM(T, G, Q) / T * 
           (G / (G - 1) * (1 - T / TT) + Q / TT * 
         (1 / (Math.exp(Q / TT) - 1) - 1 / (Math.exp(Q / T) - 1)));
        if (i==1)
        {
           Z2 = Z;
           T2 = T;
           T = T2 + 100;
           i=2;
        }
        else 
        {
           T1 = T2;
           Z1 = Z2;
           T2 = T;
           Z2 = Z;
           T = T2 - Z2 * (T2 - T1) / (Z2 - Z1);
        }
     }
     return T;
  }

  double CAL_PQPT (double TT, double M, double G, double Q)
  {
     double T = CAL_T(TT, M, G, Q);
     return (((Math.exp(Q / TT) - 1) / (Math.exp(Q / T) - 1)) * 
         Math.pow((T / TT), (G / (G - 1))) * Math.exp(Q / T * Math.exp(Q / T) / 
        (Math.exp(Q / T) - 1) - Q / TT * Math.exp(Q / TT) / (Math.exp(Q / TT) - 1)));
  }

  double CAL_DQDT (double TT, double M, double G, double Q)
  {
     double T = CAL_T (TT, M, G, Q);
     return (((Math.exp(Q / TT) - 1) / (Math.exp(Q / T) - 1)) * 
         Math.pow((T / TT), (1 / (G - 1))) * Math.exp(Q / T * Math.exp(Q / T) / 
        (Math.exp(Q / T) - 1) - Q / TT * Math.exp(Q / TT) / (Math.exp(Q / TT) - 1)));
  }

  double CAL_ASQA (double TT, double M, double G, double  Q)
  {
      double T = CAL_T(TT, M, G, Q);
      double TSTAR = CAL_T(TT, 1, G, Q);
      double SOSRAT = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) * 
        Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));
      SOSRAT = SOSRAT / (1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / TSTAR), 2) * 
        Math.exp(Q / TSTAR) / Math.pow((Math.exp(Q / TSTAR) - 1), 2)));
      SOSRAT = Math.sqrt(T / TSTAR * SOSRAT);
      return (CAL_DQDT(TT, M, G, Q) * SOSRAT * M / CAL_DQDT(TT, 1, G, Q));
  }

  double CAL_GNU (double TT, double M, double G, double Q)
  {
      if (M <= 1)
      {
         return 0;
      }
      double Pi = 4 * Math.atan(1);
      double TSTAR = CAL_T(TT, 1, G, Q);
      double TMIN = CAL_T(TT, M, G, Q);
      double DELT = (TMIN - TSTAR) / 1000;
      double PR1 = ((Math.exp(Q / TT) - 1) / (Math.exp(Q / TSTAR) - 1)) * 
         Math.pow((TSTAR / TT), (G / (G - 1))) * 
         Math.exp(Q / TSTAR * Math.exp(Q / TSTAR) / (Math.exp(Q / TSTAR) - 1) - 
          Q / TT * Math.exp(Q / TT) / (Math.exp(Q / TT) - 1));
      double ARG1 = 0;
      double T = TSTAR + DELT;
      double Sum = 0;
      double GAM;
      double PR;
      double MACH;
      double MU;
      double ARG;
      for (int i=0; i<999; ++i)
      {
          GAM = CAL_GAM(T, G, Q);
          PR = ((Math.exp(Q / TT) - 1) / (Math.exp(Q / T) - 1)) * 
              Math.pow((T / TT), (G / (G - 1))) * 
              Math.exp(Q / T * Math.exp(Q / T) / (Math.exp(Q / T) - 1) - Q / TT * 
              Math.exp(Q / TT) / (Math.exp(Q / TT) - 1));
          MACH = Math.sqrt(2 * TT / CAL_GAM(T, G, Q) / T * (G / (G - 1) * 
             (1 - T / TT) + Q / TT * (1 / (Math.exp(Q / TT) - 1) - 1 / 
             (Math.exp(Q / T) - 1))));
          MU = Math.atan(Math.sqrt(1 / (Math.pow(MACH, 2) - 1)));
          ARG = Math.sin(2 * MU) / 2 / GAM / PR;
          Sum = Sum - (ARG + ARG1) / 2 * (PR - PR1);  //Trapezoidal integration
          T = T + DELT;
          PR1 = PR;
          ARG1 = ARG;
      }
      double NU = Sum * 180 / Pi;
      return NU;
  }

  double CAL_SOS (double T, double G, double R, double Q)
  {
      double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) * 
         Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));
      return (Math.sqrt(32.174 * T * R * WOW));
  }

  double CAL_QQP (double TT, double M, double G, double Q)
  {
      double T = CAL_T (TT, M, G, Q);
      double SOS = CAL_SOS (T, G, R, Q);
      double PRAT = CAL_PQPT (TT, G, R, Q);
      return (Math.pow(M * SOS, 2)) / (2 * 32.174 * T * R);
  }

  double CAL_WCOR (double TT, double M, double G, double Q)
  {
      double T = CAL_T (TT, M, G, Q);
      double TRAT = T / TT;
      double A = CAL_SOS (T, G, R, Q);
      double PRAT = CAL_PQPT (TT, M, G, Q);
      double wcortp = (PRAT * M * A * 2116) / (TRAT * Math.sqrt(TT) * Math.sqrt(518) * R);
      return wcortp / 144;
  }

  double CAL_PQPT_LOOP (double TT, double prattp, double G, double Q)
  {
      double M = 10;
      double ML = 0;
      double MR = 20;
      double EPS = 0;
      if (prattp > .99997)
      {
         EPS = 0.0001;
      }
      if (prattp <= .99997)
      {
         EPS = .00000001;
      }
      double DIFF= prattp - CAL_PQPT (TT, M, G, Q);
      while (Math.abs(DIFF) > EPS)
      {
          if (DIFF > 0.0)
          {
             MR = M;
          }
          if (DIFF < 0.0)
          {
             ML = M;
          }
          M = (MR + ML) / 2;
          DIFF= prattp - CAL_PQPT (TT, M, G, Q);
       }
       return M;
  }

  double CAL_TQTT_LOOP (double T, double trattp, double G, double Q)
  {
      double M = 30;
      double MR = 60;
      double ML = 0;
      double TT = CAL_TT (T, M, G, Q);
      double rat = T / TT;
      double EPS = .000001;
      double DIFF = trattp - rat;
      while (Math.abs(DIFF) > EPS)
      {
         if (DIFF > 0.0)
         {
             MR = M;
         }
         if (DIFF < 0.0)
         {
             ML = M;
         }
         M = (ML + MR) / 2;
         TT = CAL_TT (T, M, G, Q);
         rat = T / TT;
         DIFF = trattp - rat;
      }
      return M;
  }

  double CAL_DQDT_LOOP (double TT, double rhorattp, double G, double Q)
  {
      double M = 6;
      double MR = 12;
      double ML = 0;
      double EPS = .00001;
      double DIFF = rhorattp - CAL_DQDT (TT, M, G, Q);
      while (Math.abs(DIFF) > EPS)
      {
         if (DIFF > 0)
         {
            MR = M;
         }
         if (DIFF < 0)
         {
            ML = M;
         }
         M = (MR + ML) / 2;
         DIFF = rhorattp - CAL_DQDT (TT, M, G, Q);
      }
      return M;
   }

   double CAL_AQAS_LOOP (double TT, double arattp, double G, double Q)
   {
      double M = 5;
      double MR = 10;
      double ML = 0;
      double EPS = .000001;
      double DIFF = arattp - (1 / CAL_ASQA(TT, M, G, Q));
      while (Math.abs(DIFF) > EPS)
      {
         if (DIFF > 0)
         {
            ML = M;
         }
         if (DIFF < 0)
         {
            MR = M;
         }
         M = (MR + ML) / 2;
         DIFF = arattp - (1 / CAL_ASQA(TT, M, G, Q));
      }
      return M;
   }

   double CAL_QQP_LOOP (double TT, double qrattp, double G, double Q)
   {
       double M = 15;
       double MR = 30;
       double ML = 0;
       double EPS = .000001;
       double DIFF = qrattp - CAL_QQP(TT, M, G, Q);
       while (Math.abs(DIFF) > EPS)
       {
          if (DIFF > 0)
          {
             ML = M;
          }
          if (DIFF < 0)
          {
            MR = M;
          }
          M = (MR + ML) / 2;
          DIFF = qrattp - CAL_QQP (TT, M, G, Q);
       }
       return M;
   }

   double CAL_WCOR_LOOP (double TT, double wcortp, double G, double Q)
   {
      double M = 9;
      double MR = 18;
      double ML = 1;
      double EPS = .000001;
      double DIFF = wcortp - CAL_WCOR (TT, M, G, Q);
      while (Math.abs(DIFF) > EPS)
      {
         if (DIFF > 0)
         {
            MR = M;
         }
         if (DIFF < 0)
         {
            ML = M;
         }
         M = (MR + ML) / 2;
         DIFF = wcortp - CAL_WCOR (TT, M, G, Q);
      }
      return M;
   }

   double CAL_MFGNU (double TT, double NU, double G, double Q)
   {
      if (NU <= 0)
      {
         return 1;
      }
      double Pi = 4 * Math.atan(1);
      double M = 0.04 * NU + 1;
      NU = NU * Pi / 180;
      double TMIN = CAL_T(TT, M, G, Q);
      double TSTAR = CAL_T(TT, 1, G, Q);
      double DELT = (TMIN - TSTAR) / 1000;
      double PR1 = ((Math.exp(Q / TT) - 1) / (Math.exp(Q / TSTAR) - 1)) * 
          Math.pow((TSTAR / TT), (G / (G - 1))) * Math.exp(Q / TSTAR * 
          Math.exp(Q / TSTAR) / (Math.exp(Q / TSTAR) - 1) - Q / TT * 
          Math.exp(Q / TT) / (Math.exp(Q / TT) - 1));
      double ARG1 = 0;
      double MACH1 = 0;
      double T = TSTAR + DELT;
      double Sum = 0;
      double GAM;
      double PR;
      double MACH = 0;
      double MU;
      double ARG;
      double SUM1 = 0;
      while (Sum < NU)
      {
         GAM = CAL_GAM(T, G, Q);
         PR = ((Math.exp(Q / TT) - 1) / (Math.exp(Q / T) - 1)) * 
              Math.pow((T / TT), (G / (G - 1))) * Math.exp(Q / T * 
              Math.exp(Q / T) / (Math.exp(Q / T) - 1) - Q / TT * 
              Math.exp(Q / TT) / (Math.exp(Q / TT) - 1));
         MACH = Math.sqrt(2 * TT / CAL_GAM(T, G, Q) / T * (G / (G - 1) * 
              (1 - T / TT) + Q / TT * (1 / (Math.exp(Q / TT) - 1) - 1 / 
              (Math.exp(Q / T) - 1))));
         MU = Math.atan(Math.sqrt(1 / (Math.pow(MACH, 2) - 1)));
         ARG = Math.sin(2 * MU) / 2 / GAM / PR;
         Sum = Sum - (ARG + ARG1) / 2 * (PR - PR1);
         if (Sum > NU)
         {
             M = (NU - SUM1) / (Sum - SUM1) * (MACH - MACH1) + MACH1;
             return M;
         }
         T = T + DELT;
         PR1 = PR;
         ARG1 = ARG;
         MACH1 = MACH;
         SUM1 = Sum;
     }
     M = (NU - SUM1) / (Sum - SUM1) * (MACH - MACH1) + MACH1;
     return M;
   }

  public void comPute() 
  {
     G = gama;
     if (itype == 0)
     {
        alt = typeinpt;
        gm1 = gama -1.0 ;
        gp1 = gama +1.0 ;
        rgas = 1718. ;                /* ft2/sec2 R */
        if (alt <= 36152.) 
        {           // Troposphere
            T = 518.6 - 3.56 * alt/1000. ;
        }
        if (alt >= 36152. && alt <= 82345.) 
        {   // Stratosphere
            T = 389.98 ;
        }
        if (alt >= 82345. && alt <= 155348.) 
        {          
            T = 389.98 + 1.645 * (alt-82345.)/1000. ;
        }
        if (alt >= 155348. && alt <= 175346.) 
        {          
            T = 508.788 ;
        }
        if (alt >= 175346. && alt <= 262448.) 
        {          
            T = 508.788 - 2.46888 * (alt-175346.)/1000. ;
        }
    }
    switch (inopt) 
    {
       case 0: {                               /* mach number given */
          mach1 = inptval ;
          M = inptval;
          if (itype == 0)
          {
            TT = CAL_TT (T, M, G, Q);             
          }
          if (itype == 1)
          {
            TT = typeinpt;
          }
          break ;
       }
       case 1: {                               /* pressure ratio given */
          prattp = prat = inptval ;
          mach1 = Math.sqrt(2.0*(Math.pow(prat,-gm1/gama) - 1.0)/gm1) ;
          if (itype == 0)
          {
             for (int i = 0; i < 100; ++i)
             {
                M = CAL_PQPT_LOOP (TT, inptval, G, Q);
                TT = CAL_TT (T, M, G, Q);
             }
          }
          if (itype == 1)
          {
             TT = typeinpt;
             M = CAL_PQPT_LOOP (TT, inptval, G, Q);
          }
          break ;
       }
       case 2: {                               /* temperature ratio given */
          trattp = trat = inptval ;
          mach1 = Math.sqrt(2.0*(Math.pow(trat,-1.0) - 1.0)/gm1) ;
          if (itype == 0)
          {
             for (int i = 0; i < 100; ++i)
             {
                M = CAL_TQTT_LOOP (T, inptval, G, Q);
                TT = CAL_TT (T, M, G, Q);
             }
          }
          if (itype == 1)
          {
             TT = typeinpt;
             T = TT * trattp;
             M = CAL_TQTT_LOOP (T, inptval, G, Q);
          }
          break ;
       }
       case 3: {                               /* density ratio given */
          rhorattp = rhorat = inptval ;
          mach1 = Math.sqrt(2.0*(Math.pow(rhorat,-gm1) - 1.0)/gm1) ;
          if (itype == 0)
          {
              for (int i = 0; i < 100; ++i)
              {
                 M = CAL_DQDT_LOOP (TT, inptval, G, Q);
                 TT = CAL_TT (T, M, G, Q);
              }
          }
          if (itype == 1)
          {
              TT = typeinpt;
              M = CAL_DQDT_LOOP (TT, inptval, G, Q);
          }
          break ;
      }
      case 4: {                               /* area ratio given */
          arattp = arat = inptval ;
          getMachArat() ;
          if (itype == 0)
          {
             for (int i = 0; i < 100; ++i)
             {
                M = CAL_AQAS_LOOP (TT, inptval, G, Q);
                TT = CAL_TT (T, M, G, Q);
             }
          }
          if (itype == 1)
          {
             TT = typeinpt;
             M = CAL_AQAS_LOOP (TT, inptval, G, Q);
          }
          break ;
       }
       case 5: {                               /* q ratio given */
          qrattp = qrat = inptval ;
          mach1 = Math.sqrt(2.0*qrat/gama) ;
          if (itype == 0)
          {
             for (int i = 0; i < 100; ++i)
             {
                M = CAL_QQP_LOOP (TT, inptval, G, Q);
                TT = CAL_TT (T, M, G, Q);
             }
          }
          if (itype == 1)
          {
             TT = typeinpt;
             M = CAL_QQP_LOOP (TT, inptval, G, Q);
          }
          break ;
      }
      case 6: {                               /* Corrected Airflow given */
          wcortp = wcor = inptval ;
          mach1 = getMach(isub, wcor) ;
          if (itype == 0)
          {
              for (int i = 0; i < 100; ++i)
              {
                 M = CAL_WCOR_LOOP (TT, inptval, G, Q);
                 TT = CAL_TT (T, M, G, Q);
              }
          }
          if (itype == 1)
          {
              TT = typeinpt;
              M = CAL_WCOR_LOOP (TT, inptval, G, Q);
          }
          break ;
      }
      case 7: {                               /* Mach angle given */
          mutp = mu = inptval ;
          M = mach1 = 1.0 / Math.sin(mu * convdr) ;
          if (itype ==0)
          {
              TT = CAL_TT (T, M, G, Q);
          }
          if (itype == 1)
          {
              TT = typeinpt;
          }
          break ;
      }
      case 8: {                               /* Prandtl-Meyer angle given */
          nutp = nu = inptval ;
          getMachpm() ;
          if (itype == 0)
          {
             for (int i = 0; i < 10; ++i)
             {
                M = CAL_MFGNU (TT, inptval, G, Q);
                TT = CAL_TT (T, M, G, Q);
             }
          }
          if (itype == 1)
          {
             TT = typeinpt;
             M = CAL_MFGNU (TT, inptval, G, Q);
          }
          break ;
      }
    }

    getIsen () ;
    
    return ; 
  }   
 
  public void getIsen () {                /* isentropic relations */
     double mach1s,msm1,fac,fac1 ;            /* mach is given        */
                                              /* prat and trat are ratiod */
     mach1s = mach1*mach1 ;                   /* to total conditions - */
     msm1 = mach1s - 1.0;
     fac = 1.0 + .5*gm1*mach1s;

     prat = Math.pow(1.0/fac,gama/gm1) ;                  /* EQ 44 */
     trat = 1.0 / fac ;                                   /* EQ 43 */
     rhorat = Math.pow(1.0/fac,1.0/gm1) ;                 /* EQ 45 */
     fac1 = gp1/(2.0*gm1) ;
     arat = mach1 * Math.pow(fac,-fac1) * Math.pow(gp1/2.0,fac1) ; /* EQ 80 */
     arat = 1.0/arat ;
     qrat = gama*mach1s*.5 ;                              /* EQ 47 */
     wcor = getAir(mach1) ;
     mu   = (Math.asin(1.0/mach1))/convdr ;
     nu   = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1/gp1)) 
             - Math.atan(Math.sqrt(msm1)) ;
     nu   = nu / convdr;

	  // Thermally Perfect Calculations
     prattp = CAL_PQPT (TT, M, G, Q);
     T = CAL_T (TT, M, G, Q);
	 trattp = T / TT;
     rhorattp = CAL_DQDT (TT, M, G, Q);
     arattp = 1 / CAL_ASQA (TT, M, G, Q);
     qrattp = CAL_QQP (TT, M, G, Q);
	 wcortp = CAL_WCOR (TT, M, G, Q);
	 mutp = (Math.asin(1.0/M))/convdr ;
	 nutp = CAL_GNU (TT, M, G, Q);

	  // tp to ideal ratios
     itpmach = M / mach1;
     itpprat = prattp / prat;
     itptrat = trattp / trat;
     itprhorat = rhorattp / rhorat;
     itparat = arattp / arat;
     itpmu = mutp / mu;
     itpnu = nutp / nu;
     itpwcor = wcortp / wcor;
     itpqrat = qrattp / qrat;

     loadOut() ;
     return;
  }

  public double getAir(double mach) {
/* Utility to get the corrected airflow per area given the Mach number */
       double number,fac1,fac2;
       fac2 = (gama+1.0)/(2.0*(gama-1.0)) ;
       fac1 = Math.pow((1.0+.5*(gama-1.0)*mach*mach),fac2);
       number =  .50161*Math.sqrt(gama) * mach/ fac1 ;

       return(number) ;
  }

  public double getMach (int sub, double corair) {
/* Utility to get the Mach number given the corrected airflow per area */
         double number,chokair;              /* iterate for mach number */
         double deriv,machn,macho,airo,airn;
         int iter ;

         chokair = getAir(1.0) ;
         if (corair > chokair) {
           number = 1.0 ;
           return (number) ;
         }
         else {
           airo = .25618 ;                 /* initial guess */
           if (sub == 2) macho = 1.0 ;   /* sonic */
           else {
              if (sub == 0) macho = 1.703 ; /* supersonic */
              else macho = .5;                /* subsonic */
              iter = 1 ;
              machn = macho - .2  ;
              while (Math.abs(corair - airo) > .0001 && iter < 20) {
                 airn =  getAir(machn) ;
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

   public void getMachArat()  {                 /*  get the Mach number */
                                            /* given the area ratio A/Astar */
      double deriv,machn,macho,aro,arn,fac,fac1;

      fac1 = gp1/(2.0*gm1) ;

      aro = 2.0 ;                 /* initial guess */
      macho = 2.2 ;                      /* supersonic */
      if (isub == 1) macho = .30;        /* subsonic */
      machn = macho + .05  ;
      while (Math.abs(arat - aro) > .0001) {
         fac = 1.0 + .5*gm1*machn*machn;
         arn = 1.0/(machn * Math.pow(fac,-fac1) * Math.pow(gp1/2.0,fac1))  ;
         deriv = (arn-aro)/(machn-macho) ;
         aro = arn ;
         macho = machn ;
         machn = macho + (arat - aro)/deriv ;
      }
      mach1 = macho ;
      return ;
   }

   public void getMachpm ()  {                 /* get the Mach number */
                                             /* given the Prandtl-meyer angle */
      double msm1o,msm1n;
      double nur,nuo,nun,deriv ;

      nur = nu*convdr ;

      msm1o = 2.0;                                  /* iterate for mach */
      nuo = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1o/gp1)) 
             - Math.atan(Math.sqrt(msm1o));
      msm1n = msm1o+.01 ;
      while (Math.abs(nur - nuo) > .0001) {
         nun = Math.sqrt(gp1/gm1)*Math.atan(Math.sqrt(gm1*msm1n/gp1)) 
                - Math.atan(Math.sqrt(msm1n));
         deriv = (nun-nuo)/(msm1n-msm1o) ;
         nuo = nun ;
         msm1o = msm1n ;
         msm1n = msm1o + (nur-nuo)/deriv ;
       }
      mach1 = Math.sqrt(msm1o + 1.0);
      return ;
   }
   

   public void loadOut() {

      in.dn.or1.setText(String.valueOf(filter3(mach1))) ;
      in.dn.or2.setText(String.valueOf(filter7(prat)));
      in.dn.or3.setText(String.valueOf(filter3(trat)));
      in.dn.or4.setText(String.valueOf(filter7(rhorat)));
      in.dn.or5.setText(String.valueOf(filter3(arat)));
      in.dn.or6.setText(String.valueOf(filter3(mu)));
      in.dn.or7.setText(String.valueOf(filter3(nu)));
	  in.dn.or8.setText(String.valueOf(filter7(wcor)));
	  in.dn.or9.setText(String.valueOf(filter3(qrat)));

	  in.dn.otp1.setText(String.valueOf(filter3(M))) ;
	  in.dn.otp2.setText(String.valueOf(filter7(prattp)));
	  in.dn.otp3.setText(String.valueOf(filter3(trattp)));
	  in.dn.otp4.setText(String.valueOf(filter7(rhorattp)));
	  in.dn.otp5.setText(String.valueOf(filter3(arattp)));
	  in.dn.otp6.setText(String.valueOf(filter3(mutp)));
	  in.dn.otp7.setText(String.valueOf(filter3(nutp)));
      in.dn.otp8.setText(String.valueOf(filter7(wcortp)));
      in.dn.otp9.setText(String.valueOf(filter3(qrattp)));

	  in.dn.opc1.setText(String.valueOf(filter3(itpmach))) ;
	  in.dn.opc2.setText(String.valueOf(filter3(itpprat)));
	  in.dn.opc3.setText(String.valueOf(filter3(itptrat)));
	  in.dn.opc4.setText(String.valueOf(filter3(itprhorat)));
	  in.dn.opc5.setText(String.valueOf(filter3(itparat)));
	  in.dn.opc6.setText(String.valueOf(filter3(itpmu)));
	  in.dn.opc7.setText(String.valueOf(filter3(itpnu)));
	  in.dn.opc8.setText(String.valueOf(filter7(itpwcor)));
	  in.dn.opc9.setText(String.valueOf(filter3(itpqrat)));
   }

   public float filter3(double inumbr) {
     //  output only to .001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 1000.) ;
       number = (float) (intermed / 1000. );
       return number ;
   }

   public float filter7(double inumbr) {
     //  output only to .0000001
       float number ;
       int intermed ;

       intermed = (int) (inumbr * 10000000.) ;
       number = (float) (intermed / 10000000. );
       return number ;
   }

   class In extends Panel {
        Isentrop outerparent ;
        Up up ;
        Dn dn ;

        In (Isentrop target) {

           outerparent = target ;
           setLayout(new GridLayout(2,1,5,5)) ;

           up = new Up(outerparent) ;
           dn = new Dn(outerparent) ;

           add(up) ;
           add(dn) ;
        }

        class Up extends Panel {
             Isentrop outerparent ;
             Label l1,l2,l3 ;
             TextField f1,f2,f3;
             Choice inch,sub,type ;
             Button bt1 ;

             Up (Isentrop target) {

                outerparent = target ;
                setLayout(new GridLayout(9,3,5,5)) ;

                l1 = new Label("Isentropic Flow", Label.RIGHT) ;
                l1.setForeground(Color.red) ;
                l2 = new Label(" Calculator", Label.LEFT) ;
                l2.setForeground(Color.red) ;

                l3 = new Label("Input:", Label.LEFT) ;
                l3.setForeground(Color.red) ;

                inch = new Choice() ;
                inch.addItem("Mach Number - M =") ;
                inch.addItem("Pressure Ratio  p/pt =");
                inch.addItem("Temperature Ratio  T/Tt =");
                inch.addItem("Density Ratio  rho/rhot =");
                inch.addItem("Area Ratio  A/A* =");
                inch.addItem("Dynamic Press Ratio  q/p =");
                inch.addItem("Flow per Area Wcor/A =");
                inch.addItem("Mach Angle mu =");
                inch.addItem("Prandtl-Meyer Angle nu =");
                inch.select(0) ;
 
                sub = new Choice() ;
                sub.addItem("Supersonic");
                sub.addItem("Subsonic") ;
                sub.select(0) ;

                type = new Choice();
                type.addItem("Altitude");
                type.addItem("Total Temperature (R)");
                type.select(0);

                f1 = new TextField(String.valueOf(inptval),5) ;
                f2 = new TextField(String.valueOf(gama),5) ;
                f3 = new TextField(String.valueOf(typeinpt),5) ;

                bt1 = new Button("COMPUTE") ;
                bt1.setBackground(Color.red) ;
                bt1.setForeground(Color.white) ;

                add(l1) ;
                add(l2) ;
                add(sub) ;

                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));

                add(l3) ;
                add(new Label("Gamma", Label.RIGHT)) ;
                add(f2) ;

                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));

                add(new Label("Input Variable", Label.RIGHT)) ;
                add(inch) ;
                add(f1) ;
				
                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));

                add(new Label("Temperature Input", Label.RIGHT)) ;
                add(type) ;
                add(f3) ;

                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));
                add(new Label(" ", Label.RIGHT));

                add(new Label(" ", Label.RIGHT)) ;
                add(bt1) ;
                add(new Label(" ", Label.RIGHT)) ;
	 }
 
         public boolean handleEvent(Event evt) {
                Double V1, V2, V3 ;
                double v1, v2, v3, varmin, varmax ;
                float fl1 ;

                if(evt.id == Event.ACTION_EVENT) {
                  inopt = inch.getSelectedIndex() ;
                  isub = sub.getSelectedIndex() ;
				  itype = type.getSelectedIndex();

                  V2 = Double.valueOf(f2.getText()) ;
                  v2 = V2.doubleValue() ;
                  if(v2 < 1.0) {
                       v2 = 1.0 ;
                       fl1 = (float) v2 ;
                       f2.setText(String.valueOf(fl1)) ;
                  }
                  if(v2 > 1.6) {
                       v2 = 1.6 ;
                       fl1 = (float) v2 ;
                       f2.setText(String.valueOf(fl1)) ;
                  }
                  gama = v2 ;

                  varmin = 0.0; varmax = 1.0 ;
                  switch (inopt) {
                     case 0: {
                         varmin = mmin ; varmax = mmax;
                         break ;
                     }
                     case 1: {
                         varmin = pmin ; varmax = pmax;
                         break ;
                     }
                     case 2: {
                         varmin = tmin ; varmax = tmax;
                         break ;
                     }
                     case 3: {
                         varmin = rhomin ; varmax = rhomax;
                         break ;
                     }
                     case 4: {
                         varmin = amin ; varmax = amax;
                         break ;
                     }
                     case 5: {
                         varmin = qmin ; varmax = qmax;
                         break ;
                     }
                     case 6: {
                         varmin = wmin ; varmax = wmax;
                         break ;
                     }
                     case 7: {
                         varmin = mumin ; varmax = mumax;
                         break ;
                     }
                     case 8: {
                         varmin = numin ; varmax = numax;
                         break ;
                     }
                  }
                  V1 = Double.valueOf(f1.getText()) ;
                  v1 = V1.doubleValue() ;
                  if(v1 < varmin) {
                      v1 = varmin ;
                      fl1 = (float) v1 ;
                      f1.setText(String.valueOf(fl1)) ;
                  }
                  if(v1 > varmax) {
                      v1 = varmax ;
                      fl1 = (float) v1 ;
                      f1.setText(String.valueOf(fl1)) ;
                  }
                  inptval = v1 ;
				  V3 = Double.valueOf(f3.getText());
				  v3 = V3.doubleValue();
					if (itype == 0)
					{
						if (v3 < altmin)
						{
							v3 = altmin;
							fl1 = (float) v3;
							f3.setText(String.valueOf(fl1));
						}
						if (v3 > altmax)
						{
							v3 = altmax;
							fl1 = (float) v3;
							f3.setText(String.valueOf(fl1));
						}
					}
					if (itype == 1)
					{
						if (v3 < tempmin)
						{
							v3 = tempmin;
							fl1 = (float) v3;
							f3.setText(String.valueOf(fl1));
						}
						if (v3 > tempmax)
						{
							v3 = tempmax;
							fl1 = (float) v3;
							f3.setText(String.valueOf(fl1));
						}
					}
				  typeinpt = v3;
           // set sub flag  -- if applicable
                  if (inopt == 0 && inptval <= 1.0) {
                      isub = 1 ;
                      sub.select(1) ;
                  }
                  if (inopt == 0 && inptval >= 1.0) {
                      isub = 0 ;
                      sub.select(0) ;
                  }
                  if (inopt == 1 && inptval >= .5283) {
                      isub = 1 ;
                      sub.select(1) ;
                  }
                  if (inopt == 1 && inptval <= .5283) {
                      isub = 0 ;
                      sub.select(0) ;
                  }
                  if (inopt == 2 && inptval >= .8333) {
                      isub = 1 ;
                      sub.select(1) ;
                  }
                  if (inopt == 2 && inptval <= .8333) {
                      isub = 0 ;
                      sub.select(0) ;
                  }
                  if (inopt == 3 && inptval >= .6339) {
                      isub = 1 ;
                      sub.select(1) ;
                  }
                  if (inopt == 3 && inptval <= .6339) {
                      isub = 0 ;
                      sub.select(0) ;
                  }
 
                  comPute () ;

                  return true ;
                }
                else return false ;
             }
        } // end Up

        class Dn extends Panel {
             Isentrop outerparent ;
             Label l1, l2, l3, l4 ;
             TextField or1,or2,or3,or4,or5,or6,or7,or8,or9 ;
			 TextField otp1,otp2,otp3,otp4,otp5,otp6,otp7,otp8,otp9 ;
			 TextField opc1,opc2,opc3,opc4,opc5,opc6,opc7,opc8,opc9 ;

             Label lo1,lo2,lo3,lo4,lo5,lo6,lo7,lo8,lo9 ;

             Dn (Isentrop target) {

                outerparent = target ;
                setLayout(new GridLayout(10,4,5,5)) ;

                l1 = new Label(" ", Label.LEFT) ;
                l1.setForeground(Color.blue) ;
 
                l2 = new Label("Perfect", Label.CENTER) ;
                l2.setForeground(Color.blue) ;

				l3 = new Label("Calorically Imperfect", Label.CENTER) ;
				l3.setForeground(Color.blue) ;

				l4 = new Label("Calorically Imperfect / Perfect", Label.CENTER) ;
				l4.setForeground(Color.blue) ;

			    lo1 = new Label("Mach", Label.RIGHT);
			    or1 = new TextField();
			    or1.setBackground(Color.black);
				or1.setForeground(Color.yellow);
				otp1 = new TextField();
				otp1.setBackground(Color.black);
				otp1.setForeground(Color.yellow);
				opc1 = new TextField();
				opc1.setBackground(Color.black);
				opc1.setForeground(Color.cyan);

			
				lo2 = new Label("P/Pt", Label.RIGHT);
			    or2 = new TextField();
				or2.setBackground(Color.black);
				or2.setForeground(Color.yellow);
				otp2 = new TextField();
				otp2.setBackground(Color.black);
				otp2.setForeground(Color.yellow);
				opc2 = new TextField();
				opc2.setBackground(Color.black);
				opc2.setForeground(Color.cyan);


                lo3 = new Label("T/Tt", Label.RIGHT) ;
				or3 = new TextField();
				or3.setBackground(Color.black);
				or3.setForeground(Color.yellow);
				otp3 = new TextField();
				otp3.setBackground(Color.black);
				otp3.setForeground(Color.yellow);
				opc3 = new TextField();
				opc3.setBackground(Color.black);
				opc3.setForeground(Color.cyan);

                lo4 = new Label("rho/rhot", Label.RIGHT) ;
				or4 = new TextField();
				or4.setBackground(Color.black);
				or4.setForeground(Color.yellow);
				otp4 = new TextField();
				otp4.setBackground(Color.black);
				otp4.setForeground(Color.yellow);
				opc4 = new TextField();
				opc4.setBackground(Color.black);
				opc4.setForeground(Color.cyan);

                lo5 = new Label("A/A*", Label.RIGHT) ;
				or5 = new TextField();
				or5.setBackground(Color.black);
				or5.setForeground(Color.yellow);
				otp5 = new TextField();
				otp5.setBackground(Color.black);
				otp5.setForeground(Color.yellow);
				opc5 = new TextField();
				opc5.setBackground(Color.black);
				opc5.setForeground(Color.cyan);

				lo6 = new Label("Mach Ang", Label.RIGHT) ;
				or6 = new TextField();
				or6.setBackground(Color.black);
				or6.setForeground(Color.yellow);
				otp6 = new TextField();
				otp6.setBackground(Color.black);
				otp6.setForeground(Color.yellow);
				opc6 = new TextField();
				opc6.setBackground(Color.black);
				opc6.setForeground(Color.cyan);

				lo7 = new Label("P-M Ang", Label.RIGHT) ;
				or7 = new TextField();
				or7.setBackground(Color.black);
				or7.setForeground(Color.yellow);
				otp7 = new TextField();
				otp7.setBackground(Color.black);
				otp7.setForeground(Color.yellow);
				opc7 = new TextField();
				opc7.setBackground(Color.black);
				opc7.setForeground(Color.cyan);

                lo9 = new Label("q/p", Label.RIGHT) ;
				or9 = new TextField();
				or9.setBackground(Color.black);
				or9.setForeground(Color.yellow);
				otp9 = new TextField();
				otp9.setBackground(Color.black);
				otp9.setForeground(Color.yellow);
				opc9 = new TextField();
				opc9.setBackground(Color.black);
    			opc9.setForeground(Color.cyan);

                lo8 = new Label("Wcor/A", Label.RIGHT) ;
				or8 = new TextField();
				or8.setBackground(Color.black);
				or8.setForeground(Color.yellow);
				otp8 = new TextField();
				otp8.setBackground(Color.black);
				otp8.setForeground(Color.yellow);
				opc8 = new TextField();
				opc8.setBackground(Color.black);
				opc8.setForeground(Color.cyan);

                add(l1) ;
                add(l2) ;
                add(l3) ;
                add(l4) ;


                add(lo1) ;
                add(or1) ;
			    add(otp1);
			    add(opc1);

				add(lo2) ;
				add(or2) ;
				add(otp2);
				add(opc2);

				add(lo3) ;
				add(or3) ;
				add(otp3);
				add(opc3);

				add(lo4) ;
				add(or4) ;
				add(otp4);
				add(opc4);

				add(lo5) ;
				add(or5) ;
				add(otp5);
				add(opc5);

				add(lo6) ;
				add(or6) ;
				add(otp6);
				add(opc6);;

				add(lo7) ;
				add(or7) ;
				add(otp7);
				add(opc7);

				add(lo8) ;
				add(or8) ;
				add(otp8);
				add(opc8) ;

				add(lo9) ;
				add(or9) ;
				add(otp9);
				add(opc9) ;


			 }
        } // end Dn
   } // end In
}

