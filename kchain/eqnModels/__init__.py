import tensorflow as tf


def speedOfSound(gamma, R, theta, T):
    a = tf.math.pow(R * T * (1 + (gamma - 1) / (1 + (gamma - 1) * (tf.math.
        pow(theta / T, 2) * tf.math.exp(theta / T) / tf.math.pow(tf.math.
        exp(theta / T) - 1, 2)))), 0.5)
    return a
