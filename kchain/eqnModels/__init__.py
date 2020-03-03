import tensorflow as tf


def getAir(mach, gamma):
    fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))
    fac1 = tf.math.pow(1.0 + 0.5 * (gamma - 1.0) * mach * mach, fac2)
    number = 0.50161 * tf.math.sqrt(gamma) * mach / fac1
    aflow = number
    return aflow
