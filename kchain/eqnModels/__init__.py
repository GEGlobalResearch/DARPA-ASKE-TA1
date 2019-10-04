import tensorflow as tf
def Acc(inArg):
    Velocity = inArg[0]
    Time = inArg[1]
    Acc = tf.gradients(Velocity, Time, stop_gradients = [Time])
    return Acc

