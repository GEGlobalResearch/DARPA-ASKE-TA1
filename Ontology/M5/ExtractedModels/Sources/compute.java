import java.lang.Math ;

public class Compute{

    public static void main(String[] args){
        double Cp, air; 
        Compute c = new Compute();
        
        Cp = c.getCp(200, 0);
        System.out.println("Cp = " + Cp);
        
        air = c.getAir(0.85, 1.4);
        System.out.println("Airflow = " + air);
        
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
        return(number);
    }
  
    public double getAir(double mach, double gamma) {
        /* Utility to get the corrected airflow per area given the Mach number */
        double number,fac1,fac2;
        fac2 = (gamma+1.0)/(2.0*(gamma-1.0)) ;
        fac1 = Math.pow((1.0+.5*(gamma-1.0)*mach*mach),fac2);
        number =  .50161*Math.sqrt(gamma) * mach/ fac1 ;
        return(number);
    }
}