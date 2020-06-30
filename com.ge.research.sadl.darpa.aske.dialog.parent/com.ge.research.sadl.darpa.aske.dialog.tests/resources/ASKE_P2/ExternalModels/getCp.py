
def getCp(temp, opt):
        #  Utility to get cp as a function of temp 
        # /* BTU/R */
        a = -4.4702130e-13
        b = -5.1286514e-10
        c = 2.8323331e-05
        d = 0.2245283
        if opt == 0:
            number = 0.2399
        else:
            number = a * temp * temp * temp + b * temp * temp + c * temp + d
        return (number)
