package com.ge.research.sadl.darpa.aske.tests;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.ge.research.sadl.darpa.aske.processing.imports.JavaToPythonServiceInterface;

public class JavaToPythonTests {

	@Test
	public void test() throws IOException {
		String javaContent = 
				"public double getAir(double mach, double gamma) {\r\n" + 
				"    /* Utility to get the corrected airflow per area given the Mach number */\r\n" + 
				"      double number,fac1,fac2;\r\n" + 
				"      fac2 = (gamma+1.0)/(2.0*(gamma-1.0)) ;\r\n" + 
				"      fac1 = Math.pow((1.0+.5*(gamma-1.0)*mach*mach),fac2);\r\n" + 
				"      number =  .50161*Math.sqrt(gamma) * mach/ fac1 ;\r\n" + 
				"      return(number) ;\r\n" + 
				"    }\r\n";
		String serviceBaseUrl = "http://vesuvius-dev.crd.ge.com:19092";
		JavaToPythonServiceInterface jtpsi = new JavaToPythonServiceInterface(serviceBaseUrl);
		String response = jtpsi.translateMethodJavaToPython("GetAir", javaContent);
		System.out.println(response);
		String desiredPython = "#!/usr/bin/env python\n" + 
				"\"\"\" generated source for module inputfile \"\"\"\n" + 
				"class GetAir(object):\n" + 
				"    \"\"\" generated source for class GetAir \"\"\"\n" + 
				"    def getAir(self, mach, gamma):\n" + 
				"        \"\"\" generated source for method getAir \"\"\"\n" + 
				"        #  Utility to get the corrected airflow per area given the Mach number \n" + 
				"        fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))\n" + 
				"        fac1 = Math.pow((1.0 + 0.5 * (gamma - 1.0) * mach * mach), fac2)\n" + 
				"        number = 0.50161 * Math.sqrt(gamma) * mach / fac1\n" + 
				"        return (number)";
		assertTrue(response.trim().equals(desiredPython));
	}

}
