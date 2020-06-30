## This code is originally from an Engine Simulator applet downloaded from
## the Hypersonic Aerodynamics page of the NASA Glenn Research Center.
##  https://www.grc.nasa.gov/WWW/K-12/Enginesim/index.htm

import math
def rayleighflow(mach, tt4) :
     #/* analysis for rayleigh flow */

     mach1 = mach
     gamopt = 1
     ttrat = 4000/tt4
     tlow = tt4

     g1 = getGama(tlow,gamopt)
     gm1 = g1 - 1.0 
     wc1 = getAir(mach1,g1)
     g2 = getGama(tlow*ttrat,gamopt)
     gm2 = g2 - 1.0 
     number = .95 
                             #/* iterate for mach downstream */
     mgueso = .4                 # /* initial guess */
     mach2 = .5 
     while (abs(mach2 - mgueso) > .0001) :
         mgueso = mach2 
         fac1 = 1.0 + g1 * mach1 * mach1 
         fac2 = 1.0 + g2 * mach2 * mach2 
         fac3 = math.pow((1.0 + .5 * gm1 * mach1 * mach1),(g1/gm1)) 
         fac4 = math.pow((1.0 + .5 * gm2 * mach2 * mach2),(g2/gm2)) 
         number = fac1 * fac4 / fac2 / fac3 
         wc2 = wc1 * math.sqrt(ttrat) / number 
         mach2 = getMach(0,wc2,g2) 

     return number 

def getGama(temp,gamopt):
      a =  -7.6942651e-13;
      b =  1.3764661e-08;
      c =  -7.8185709e-05;
      d =  1.436914;

      number = a*temp*temp*temp + b*temp*temp + c*temp +d 

      return number

def getMach (sub, corair, gamma) :
#/* Utility to get the Mach number given the corrected airflow per area */

      chokair = getAir(1.0, gamma) 
      if (corair > chokair) : 
          number = 1.0 
      else :
        airo = .25618                  #/* initial guess */
        if (sub == 1) : 
           macho = 1.0    #/* sonic */
        else :
           if (sub == 2) :
              macho = 1.703  #/* supersonic */
           else :
              macho = .5                #/* subsonic */
           iter = 1 
           machn = macho - .2  
           while (abs(corair - airo) > .0001 and iter < 20) :
              airn =  getAir(machn,gamma) 
              deriv = (airn-airo)/(machn-macho) 
              airo = airn 
              macho = machn 
              machn = macho + (corair - airo)/deriv 
              ++ iter 
        number = macho 

      return number

def getAir(mach, gamma) :
    # Utility to get the corrected airflow per area given the Mach number
    fac2 = (gamma+1.0)/(2.0*(gamma-1.0)) 
    fac1 = math.pow((1.0+.5*(gamma-1.0)*mach*mach),fac2)
    number =  .50161*math.sqrt(gamma) * mach/ fac1 

    return number
