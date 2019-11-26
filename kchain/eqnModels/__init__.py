import tensorflow as tf
def speedOfSound(inArg):
    gamma = inArg[0]
    R = inArg[1]
    theta = inArg[2]
    T = inArg[3]
    a = tf.math.pow(R * T *  (  1 + ( gamma-1 ) / ( 1 + ( gamma-1 ) *  ( tf.math.pow( theta/T,2) *  tf.math.exp( theta/T ) /  tf.math.pow( tf.math.exp( theta/T ) - 1,2 ))  ) ), 1/2)
    return a

