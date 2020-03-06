import autograd.numpy as np


def getAirPy(mach, gamma):
    fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))
    fac1 = np.power(1.0 + 0.5 * (gamma - 1.0) * mach * mach, fac2)
    number = 0.50161 * np.sqrt(gamma) * mach / fac1
    aflow = number
    return aflow
