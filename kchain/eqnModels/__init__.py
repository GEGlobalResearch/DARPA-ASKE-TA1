def SpeedOfSound(inArg):
    gamma = inArg[0]
    R = inArg[1]
    theta = inArg[2]
    T = inArg[3]
    a = tf.math.pow( R * T *  (  1 + ( gamma-1 ) / ( 1 + ( gamma-1 ) *  (  ( theta/T ) ** 2 *  tf.math.exp( theta/T ) / (  tf.math.exp( theta/T ) - 1 ) ** 2 )  ) ) , 1/2)
    return a

