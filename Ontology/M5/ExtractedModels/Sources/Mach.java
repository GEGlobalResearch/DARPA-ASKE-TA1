/*  
                      Mach and Speed of Sound Calculator
            Interactive Program to solve Standard Atmosphere Equations
                     for mach and speed of sound
                Calorically imperfect gas modeled for hypersonics

                          A Java Applet

                     Version 1.2b   - 5 Mar 12

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

      New Test :
                * move the choice for units
                * add compute button
                * remove Mars model
                * add calorically imperfect gas
                  correct error at low Mach number

                                                     TJB 5 Mar 12

*/

import java.awt.*;
import java.lang.Math ;

public class Mach extends java.applet.Applet {

   double gama,alt,temp,press,vel,ttot ;
   double rgas, rho0, rho, a0, lrat, mach ;
   double Q, TT, T, M, GAM, R, A, V, G  ;
   int lunits,vparam,tparam,ther,prs, planet ;
   int mode ;

   In in ;

   public void init() {

     setLayout(new GridLayout(1,1,0,0)) ;

     setDefaults () ;

     in = new In(this) ;

     add(in) ;

     computeMach() ;
  }
 
  public Insets insets() {
     return new Insets(10,10,10,10) ;
  }

  public void setDefaults() {
     vparam = 0 ;
     tparam = 0 ;
     lunits = 0;
     mode = 0 ;

     alt = 0.0 ;
     V = vel = 500. ;
     T = temp = 518.6 - 3.56 * alt/1000. ;
     a0 = Math.sqrt(gama*rgas*temp) ;  // feet /sec
     A = a0 = a0 * 60.0 / 88. ;   // mph
     M = mach = vel/a0 ;
     TT = ttot = temp * (1.0 + (gama - 1.0) * mach * mach) ;

     Q = 5500 ;
     R = 53.3 ;
 
  }

  public double CAL_SOS (double T, double G, double R, double Q) {
      double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) *
         Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));
      return (Math.sqrt(32.174 * T * R * WOW));
  }

  public double TQTT(double M, double G) {
     return Math.pow((1 + (G - 1) / 2 * Math.pow(M, 2)), -1);
  }

  public double CAL_GAM (double T, double G, double Q) {
     return (1 + (G - 1) / (1 + (G - 1) * (Math.pow((Q / T), 2) *
        Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2))));
  }

  public double CAL_TT (double T, double M, double G, double Q) {
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
     if (M <= .1) TT = T / TQTT(M,G);

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
     if (M <= .1) T = TT * TQTT(M,G);

     return T;
  }

  double CAL_TV (double TT, double V, double G, double Q)
  {
     double T = TT - (G -1.0) * V * V / (2.0 * G * R * 32.2);
     double EPS = 0.00001;
     double Z=1;
     double Z2 = 0;
     double T2 = 0;
     double T1;
     double Z1;
     double i=1;
     while (Math.abs(Z) > EPS)
     {
        Z = Math.pow(V, 2) - 2 * R * 32.2 * TT *
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
     if (V <= 100.)  T = TT - (G -1.0) * V * V / (2.0 * G * R * 32.2);

     return T;
  }

  public void computeMach() {
 
     // Earth  standard day
        rgas = 1718. ;                /* ft2/sec2 R */
        G = gama = 1.4 ;
 
      if (tparam == 0) {
        if (alt <= 36152.) {           // Troposphere
          T = temp = 518.6 - 3.56 * alt/1000. ;
        }
        if (alt >= 36152. && alt <= 82345.) {   // Stratosphere
          T = temp = 389.98 ;
        }
        if (alt >= 82345. && alt <= 155348.) {          
          T = temp = 389.98 + 1.645 * (alt-82345.)/1000. ;
        }
        if (alt >= 155348. && alt <= 175346.) {          
          T = temp = 508.788 ;
        }
        if (alt >= 175346. && alt <= 262448.) {          
          T = temp = 508.788 - 2.46888 * (alt-175346.)/1000. ;
        }

        a0 = Math.sqrt(gama*rgas*temp) ;  // feet /sec
        a0 = a0 * 60.0 / 88. ;   // mph
        A = CAL_SOS(T, G, R, Q) ;
        A = A * 60.0 / 88. ;   // mph
               // compute either mach or velocity 
        if (vparam == 0) {
             mach = vel/a0 ;
             M = V / A ;
        }
        if (vparam == 1) {
             vel = mach * a0 ;
             V = M * A ;
        }
        ttot = temp * (1.0 + .5 * (gama - 1.0) * mach * mach) ;
        TT = CAL_TT(T, M, G, Q) ;
        GAM = CAL_GAM(T ,G, Q) ;
     }

     if (tparam == 1) {
        if (vparam == 1) {
          temp = ttot / (1.0 + .5 * (gama - 1.0) * mach * mach) ;
          a0 = Math.sqrt(gama*rgas*temp) ;  // feet /sec
          a0 = a0 * 60.0 / 88. ;   // mph
          vel = mach * a0 ;
          T = CAL_T(TT, M, G, Q) ;
          A = CAL_SOS(T, G, R, Q) ;
          A = A * 60.0 / 88. ;   // mph
          V = M * A ;
          GAM = CAL_GAM(T ,G, Q) ;
        }
        if (vparam == 0) {
          V = vel = vel * 88. / 60. ;  // v in ft/sec
          temp = ttot - (gama -1.0) * vel * vel / (2.0 * gama * rgas) ;
          a0 = Math.sqrt(gama*rgas*temp) ;  // feet /sec
          mach = vel / a0 ;
          a0 = a0 * 60.0 / 88. ;   // mph
          vel = vel * 60. / 88. ;  // v in mph
          T = CAL_TV(TT, V, G, Q) ;
          A = CAL_SOS(T, G, R, Q) ;
          GAM = CAL_GAM(T ,G, Q) ;
          M = V / A ;
          A = A * 60.0 / 88. ;   // mph
          V = V * 60. / 88. ;  // v in mph
        }
     }

     if (lunits == 0) {
        in.dn.o3.setText(String.valueOf(filter0(a0))) ;
        in.dn.o4.setText(String.valueOf(filter0(vel))) ;
        in.dn.o6.setText(String.valueOf(filter0(temp))) ;
        in.dn.o8.setText(String.valueOf(filter0(ttot))) ;
        in.dn.o3r.setText(String.valueOf(filter0(A))) ;
        in.dn.o4r.setText(String.valueOf(filter0(V))) ;
        in.dn.o6r.setText(String.valueOf(filter0(T))) ;
        in.dn.o8r.setText(String.valueOf(filter0(TT))) ;
     }
     if (lunits == 1) {
        in.dn.o3.setText(String.valueOf(filter0(a0  * .447))) ;
        in.dn.o4.setText(String.valueOf(filter0(vel * .447))) ;
        in.dn.o6.setText(String.valueOf(filter0(temp * 5.0 /9.0))) ;
        in.dn.o8.setText(String.valueOf(filter0(ttot * 5.0 /9.0))) ;
        in.dn.o3r.setText(String.valueOf(filter0(A  * .447))) ;
        in.dn.o4r.setText(String.valueOf(filter0(V * .447))) ;
        in.dn.o6r.setText(String.valueOf(filter0(T * 5.0 /9.0))) ;
        in.dn.o8r.setText(String.valueOf(filter0(TT * 5.0 /9.0))) ;
     }
 
     in.dn.o5.setText(String.valueOf(filter3(mach))) ;
     in.dn.o7.setText(String.valueOf(filter3(gama))) ;
     in.dn.o5r.setText(String.valueOf(filter3(M))) ;
     in.dn.o7r.setText(String.valueOf(filter3(GAM))) ;
 
     if (mode == 0) loadInpt() ;
  }
 
  public void loadInpt() {
     if (lunits == 0) {
         if (vparam == 0) {
            in.up.o2.setText(String.valueOf(filter0(vel))) ;
         }
         if (vparam == 1) {
            in.up.o2.setText(String.valueOf(filter3(mach))) ;
         }
         if (tparam == 0) {
            in.up.o1.setText(String.valueOf(filter0(alt))) ;
         }
         if (tparam == 1) {
            in.up.o1.setText(String.valueOf(filter0(ttot))) ;
         }
     }
     if (lunits == 1) {
         if (vparam == 0) {
            in.up.o2.setText(String.valueOf(filter0(vel*.447))) ;
         }
         if (vparam == 1) {
            in.up.o2.setText(String.valueOf(filter3(mach))) ;
         }
         if (tparam == 0) {
            in.up.o1.setText(String.valueOf(filter0(alt*.3048))) ;
         }
         if (tparam == 1) {
            in.up.o1.setText(String.valueOf(filter0(ttot*5.0/9.0))) ;
         }
     }
  }

  public int filter0(double inumbr) {
     //  integer output
       float number ;
       int intermed ;

       intermed = (int) (inumbr) ;
       number = (float) (intermed);
       return intermed ;
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

  class In extends Panel {
     Mach outerparent ;
     Titl titl ;
     Up up ;
     Dn dn ;

     In (Mach target) {                           
        outerparent = target ;
        setLayout(new GridLayout(3,1,5,5)) ;

        titl = new Titl(outerparent) ;
        up = new Up(outerparent) ;
        dn = new Dn(outerparent) ;

        add(titl) ;
        add(up) ;
        add(dn) ;
     }

     public Insets insets() {
        return new Insets(5,5,0,0) ;
     }

     class Titl extends Panel {
        Label la ;
        In2 in2;

        Titl (Mach target) {                           
            outerparent = target ;
            setLayout(new GridLayout(2,1,0,0)) ;

            la = new Label("Mach and Speed of Sound Calculator", Label.CENTER) ;
            la.setForeground(Color.red) ;

            in2 = new In2(outerparent) ;
 
            add(la) ;
            add(in2) ;
        }
 
        class In2 extends Panel {
           Label lc,lb ;
           Choice plntch,untch ;

           In2 (Mach target) {                           
               outerparent = target ;
               setLayout(new GridLayout(2,4,0,0)) ;

               lb = new Label("Units:", Label.RIGHT) ;
               lb.setForeground(Color.red) ;
 
               untch = new Choice() ;
               untch.addItem("Imperial") ;
               untch.addItem("Metric");
               untch.setBackground(Color.white) ;
               untch.setForeground(Color.red) ;
               untch.select(0) ;

               add(new Label(" ", Label.RIGHT)) ;
               add(lb) ;
               add(untch) ;
               add(new Label(" ", Label.RIGHT)) ;

               add(new Label(" ", Label.RIGHT)) ;
               add(new Label(" ", Label.RIGHT)) ;
               add(new Label(" ", Label.RIGHT)) ;
               add(new Label(" ", Label.RIGHT)) ;
           }

           public boolean handleEvent(Event evt) {
               if(evt.id == Event.ACTION_EVENT) {
                  lunits  = untch.getSelectedIndex() ;

                  if (lunits == 0) {  // English units labels
                      dn.l4.setText("Object Speed - mph") ;
                      dn.l3.setText("Speed of Sound - mph") ;
                      dn.l6.setText("Static Temperature - R") ;
                      dn.l8.setText("Total Temperature - R") ;
                      if (vparam == 0) {
                           up.l2u.setText("mph") ;
                      }
                      if (vparam == 1) {
                           up.l2u.setText(" ") ;
                      }
                      if (tparam == 0) {
                           up.l1u.setText("feet") ;
                      }
                      if (tparam == 1) {
                           up.l1u.setText("R") ;
                      }
                  }
                  if (lunits == 1) {  // Metric units labels
                      dn.l4.setText("Object Speed - m/sec") ;
                      dn.l3.setText("Speed of Sound - m/sec") ;
                      dn.l6.setText("Static Temperature - K") ;
                      dn.l8.setText("Total Temperature - K") ;
                      if (vparam == 0) {
                           up.l2u.setText("m/sec") ;
                      } 
                      if (vparam == 1) {
                           up.l2u.setText(" ") ;
                      }
                      if (tparam == 0) {
                           up.l1u.setText("meters") ;
                      }
                      if (tparam == 1) {
                           up.l1u.setText("K") ;
                      }
                  }
 
                  mode = 0 ;
                  computeMach() ;
                  return true ;
               }
               else return false ;
           }
        }
     }

     class Up extends Panel {
        TextField o1,o2 ;
        Label l1,l1u,l2u, la ;
        Button bt1;
        Choice vch,tch ;

        Up (Mach target) {                           
            outerparent = target ;
            setLayout(new GridLayout(4,4,5,5)) ;
    
            la = new Label("Input", Label.LEFT) ;
            la.setForeground(Color.red) ;

            l1u = new Label(" feet ", Label.CENTER) ;
   
            o1 = new TextField() ;
            o1.setBackground(Color.white) ;
            o1.setForeground(Color.black) ;

            l2u = new Label("mph", Label.CENTER) ;

            o2 = new TextField() ;
            o2.setBackground(Color.white) ;
            o2.setForeground(Color.black) ;
 
            vch = new Choice() ;
            vch.addItem("Object Speed") ;
            vch.addItem("Mach");
            vch.select(0) ;

            tch = new Choice() ;
            tch.addItem("Altitude") ;
            tch.addItem("Total Temperature");
            tch.select(0) ;

            bt1 = new Button("COMPUTE") ;
            bt1.setBackground(Color.red) ;
            bt1.setForeground(Color.white) ;

            add(la) ;
            add(new Label(" ", Label.RIGHT)) ;
            add(new Label(" ", Label.RIGHT)) ;
            add(new Label(" ", Label.RIGHT)) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(vch) ;
            add(o2) ;
            add(l2u) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(tch) ;
            add(o1) ;
            add(l1u) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(new Label("Press -> ", Label.RIGHT)) ;
            add(bt1) ;
            add(new Label(" ", Label.RIGHT)) ;
        }

        public Insets insets() {
           return new Insets(5,5,5,5) ;
        }

        public boolean action(Event evt, Object arg) {
            if(evt.target instanceof Choice) {
               this.handleProb(arg) ;
               return true ;
            }

            if(evt.target instanceof Button) {
               this.handleBut(evt) ;
               return true ;
            }

            else return false ;
        }
 
        public void handleProb(Object obj) {

            vparam = vch.getSelectedIndex() ;
            tparam = tch.getSelectedIndex() ;

            if (lunits == 0) {  // English units labels
                if (vparam == 0) {
                     l2u.setText("mph") ;
                }
                if (vparam == 1) {
                     l2u.setText(" ") ;
                }
                if (tparam == 0) {
                     l1u.setText("feet") ;
                }
                if (tparam == 1) {
                     l1u.setText("R") ;
                }
            }
            if (lunits == 1) {  // Metric units labels
                if (vparam == 0) {
                     l2u.setText("m/sec") ;
                } 
                if (vparam == 1) {
                     l2u.setText(" ") ;
                }
                if (tparam == 0) {
                     l1u.setText("meters") ;
                }
                if (tparam == 1) {
                     l1u.setText("K") ;
                }
            }
 
            mode = 0 ;
            computeMach() ;
        }

        public void handleBut(Event evt) {
            Double V1,V2 ;
            double v1,v2 ;

            V1 = Double.valueOf(o1.getText()) ;
            v1 = V1.doubleValue() ;
            V2 = Double.valueOf(o2.getText()) ;
            v2 = V2.doubleValue() ;

            if (lunits == 0) {
                if (vparam == 0) {
                   if (v2 < 0.0) {
                      v2 = 0.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   if (v2 >17600.0) {
                      v2 = 17600.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   V = vel = v2 ;
                }
                if (vparam == 1) {
                   if (v2 < 0.0) {
                      v2 = 0.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   if (v2 >25.0) {
                      v2 = 25.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   M = mach = v2 ;
                }
                if (tparam == 0) {
                   if (v1 < 0.0) {
                      v1 = 0.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   if (v1 >250000.0) {
                      v1 = 250000.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   alt = v1 ;
                }
                if (tparam == 1) {
                   if (v1 < 500.0) {
                      v1 = 500.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   if (v1 >5000.0) {
                      v1 = 5000.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   TT = ttot = v1 ;
                }
            }
            if (lunits == 1) {
                if (vparam == 0) {
                   if (v2 < 0.0) {
                      v2 = 0.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   if (v2 >7867.0) {
                      v2 = 7867.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   V = vel = v2 / .447 ;
                }
                if (vparam == 1) {
                   if (v2 < 0.0) {
                      v2 = 0.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   if (v2 >25.0) {
                      v2 = 25.0 ;
                      o2.setText(String.valueOf(filter0(v2))) ;
                   }
                   M = mach = v2 ;
                }
                if (tparam == 0) {
                   if (v1 < 0.0) {
                      v1 = 0.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   if (v1 >76200.0) {
                      v1 = 76200.0 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   alt = v1 / .3048 ;
                }
                if (tparam == 1) {
                   if (v1 < 277.7) {
                      v1 = 277.7 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   if (v1 >2777.7) {
                      v1 = 2777.7 ;
                      o1.setText(String.valueOf(filter0(v1))) ;
                   }
                   TT = ttot = v1 * 9.0 / 5.0 ;
                }
            }

            mode = 1;
            computeMach() ;
        }
     }

     class Dn extends Panel {
        Mach outerparent ;
        TextField o3,o4,o5,o6,o7,o8 ;
        TextField o3r,o4r,o5r,o6r,o7r,o8r ;
        Label l3,l4,l5,l6,l7,l8 ;
        Label lb,lc,ld ;

        Dn (Mach target) {
            outerparent = target ;
            setLayout(new GridLayout(7,4,0,0)) ;
    
            lb = new Label("Output", Label.LEFT) ;
            lb.setForeground(Color.blue) ;
            lc = new Label("Perfect", Label.CENTER) ;
            lc.setForeground(Color.blue) ;
            ld = new Label("Calorically Imperfect", Label.CENTER) ;
            ld.setForeground(Color.blue) ;

            l3 = new Label("Speed of Sound - mph", Label.CENTER) ;
            o3 = new TextField() ;
            o3.setBackground(Color.black) ;
            o3.setForeground(Color.yellow) ;
            o3r = new TextField() ;
            o3r.setBackground(Color.black) ;
            o3r.setForeground(Color.cyan) ;

            l4 = new Label("Object Speed - mph", Label.CENTER) ;
            o4 = new TextField() ;
            o4.setBackground(Color.black) ;
            o4.setForeground(Color.yellow) ;
            o4r = new TextField() ;
            o4r.setBackground(Color.black) ;
            o4r.setForeground(Color.cyan) ;
 
            l5 = new Label("Mach", Label.CENTER) ;
            o5 = new TextField() ;
            o5.setBackground(Color.black) ;
            o5.setForeground(Color.yellow) ;
            o5r = new TextField() ;
            o5r.setBackground(Color.black) ;
            o5r.setForeground(Color.cyan) ;
 
            l6 = new Label("Static Temperature - R", Label.CENTER) ;
            o6 = new TextField() ;
            o6.setBackground(Color.black) ;
            o6.setForeground(Color.yellow) ;
            o6r = new TextField() ;
            o6r.setBackground(Color.black) ;
            o6r.setForeground(Color.cyan) ;
 
            l7 = new Label("Gamma", Label.CENTER) ;
            o7 = new TextField() ;
            o7.setBackground(Color.black) ;
            o7.setForeground(Color.yellow) ;
            o7r = new TextField() ;
            o7r.setBackground(Color.black) ;
            o7r.setForeground(Color.cyan) ;
 
            l8 = new Label("Total Temperature - R", Label.CENTER) ;
            o8 = new TextField() ;
            o8.setBackground(Color.black) ;
            o8.setForeground(Color.yellow) ;
            o8r = new TextField() ;
            o8r.setBackground(Color.black) ;
            o8r.setForeground(Color.cyan) ;
 
            add(lb) ;
            add(new Label(" ", Label.RIGHT)) ;
            add(lc) ;
            add(ld) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l4) ;
            add(o4) ;
            add(o4r) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l3) ;
            add(o3) ;
            add(o3r) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l5) ;
            add(o5) ;
            add(o5r) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l6) ;
            add(o6) ;
            add(o6r) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l8) ;
            add(o8) ;
            add(o8r) ;

            add(new Label(" ", Label.RIGHT)) ;
            add(l7) ;
            add(o7) ;
            add(o7r) ;
        }
     }
  }
}
