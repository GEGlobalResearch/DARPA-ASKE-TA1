import tensorflow as tf
def Newtons2LawModel(inArg):
    Mass = inArg[0]
    Acceleration = inArg[1]
    Force = Mass * Acceleration
    return Force

