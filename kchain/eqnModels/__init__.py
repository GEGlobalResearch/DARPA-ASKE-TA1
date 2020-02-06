import tensorflow as tf
""" Global variable declarations """
eta = [0] * 20
pt = [0] * 20
prat = [0] * 20
tt = [0] * 20


def getFreeStream0():
    """ Specifying global scope for implicit variables """
    global a0, alt, altd, cpair, fsmach, gama, lconv1, lconv2, ps0, psout, q0, rgas, rho0, ts0, tsout, u0, u0d
    """ generated source for method getFreeStream0 """
    rgas = 1718.0
    alt = altd / lconv1
    if alt < 36152.0:
        ts0 = 518.6 - 3.56 * alt / 1000.0
        ps0 = 2116.0 * tf.math.pow(ts0 / 518.6, 5.256)
    if alt >= 36152.0 and alt <= 82345.0:
        ts0 = 389.98
        ps0 = 2116.0 * 0.2236 * tf.math.exp((36000.0 - alt) / (53.35 * 389.98))
    if alt >= 82345.0:
        ts0 = 389.98 + 1.645 * (alt - 82345) / 1000.0
        ps0 = 2116.0 * 0.02456 * tf.math.pow(ts0 / 389.98, -11.388)
    a0 = tf.math.sqrt(gama * rgas * ts0)
    u0 = u0d / lconv2 * 5280.0 / 3600.0
    fsmach = u0 / a0
    q0 = gama / 2.0 * fsmach * fsmach * ps0
    if u0 > 0.0001:
        rho0 = q0 / (u0 * u0)
    else:
        rho0 = 1.0
    tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)
    pt[0] = ps0 * tf.math.pow(tt[0] / ts0, gama / (gama - 1.0))
    ps0 = ps0 / 144.0
    pt[0] = pt[0] / 144.0
    cpair = getCp(tt[0], gamopt)
    tsout = ts0
    psout = ps0
    return


def loadCF6():
    """ Specifying global scope for implicit variables """
    global a2, a2d, a4, a4p, a8d, a8max, a8rat, abflag, ac, acore, afan, altd, altmax, altmt, arsched, byprat, dburner, dcomp, dfan, diameng, dinlt, dnozl, dturbin, entype, fhv, fhvd, fueltype, gama, gamopt, mburner, mcomp, mfan, minlt, mnozl, mturbin, ncflag, ntflag, p3fp2d, p3p2d, pt2flag, tburner, tcomp, tfan, tinlt, tnozl, tt4, tt4d, tt7, tt7d, tturbin, u0d, u0max, u0mt, weight, wtflag
    """ generated source for method loadCF6 """
    entype = 2
    abflag = 0
    fueltype = 0
    fhvd = fhv = 18600.0
    tt[4] = tt4 = tt4d = 2500.0
    tt[7] = tt7 = tt7d = 2500.0
    prat[3] = p3p2d = 21.86
    prat[13] = p3fp2d = 1.745
    byprat = 3.3
    acore = 6.965
    afan = acore * (1.0 + byprat)
    a2d = a2 = afan
    diameng = tf.math.sqrt(4.0 * a2d / 3.14159)
    a4 = 0.29
    a4p = 1.131
    ac = 0.9 * a2
    gama = 1.4
    gamopt = 1
    pt2flag = 0
    eta[2] = 1.0
    prat[2] = 1.0
    prat[4] = 1.0
    eta[3] = 0.959
    eta[4] = 0.984
    eta[5] = 0.982
    eta[7] = 1.0
    eta[13] = 1.0
    a8d = 2.436
    a8max = 0.35
    a8rat = 0.35
    u0max = u0mt
    u0d = 0.0
    altmax = altmt
    altd = 0.0
    arsched = 0
    wtflag = 0
    weight = 8229.0
    minlt = 1
    dinlt = 170.0
    tinlt = 900.0
    mfan = 2
    dfan = 293.0
    tfan = 1500.0
    mcomp = 0
    dcomp = 293.0
    tcomp = 1600.0
    mburner = 4
    dburner = 515.0
    tburner = 2500.0
    mturbin = 4
    dturbin = 515.0
    tturbin = 2500.0
    mnozl = 3
    dnozl = 515.0
    tnozl = 2500.0
    ncflag = 0
    ntflag = 0
    return


def setDefaults():
    """ Specifying global scope for implicit variables """
    global a2, a2d, a2max, a2min, a4, a8, a8d, a8max, a8min, a8rat, abflag, ac, aconv, acore, aexsched, afan, altd, altmax, altmin, altmr, altmt, arexit, arexitd, arexmn, arexmx, arsched, arth, arthd, arthmn, arthmx, athsched, bconv, bypmax, bypmin, byprat, counter, cprmax, cprmin, dburner, dcomp, dconv, dfan, diamax, diameng, diamin, dinlt, dnozl, dnozr, dturbin, econv, econv2, entype, etmax, etmin, factor, factp, fconv, fhv, fhvd, fireflag, flconv, fprmax, fprmin, fueltype, g0, g0d, gama, gamopt, inflag, inptype, lconv1, lconv2, lunits, mburner, mcomp, mconv1, mconv2, mfan, minlt, mnozl, mnozr, move, mturbin, ncflag, ntflag, p3fp2d, p3p2d, pconv, pltkeep, plttyp, pmax, pt2flag, pt4max, showcom, siztype, sldloc, sldplt, t4max, t4min, t7max, t7min, tburner, tcomp, tconv, tfan, thrmax, thrmin, throtl, tinlt, tmax, tmin, tnozl, tnozr, tref, tt4, tt4d, tt7, tt7d, tturbin, u0d, u0max, u0min, u0mr, u0mt, varflag, vmn1, vmn2, vmn3, vmn4, vmx1, vmx2, vmx3, vmx4, weight, wtflag, xtranp, xtrans, ytranp, ytrans
    """ generated source for method setDefaults """
    i = 0
    move = 0
    inptype = 0
    siztype = 0
    lunits = 0
    lconv1 = 1.0
    lconv2 = 1.0
    fconv = 1.0
    mconv1 = 1.0
    pconv = 1.0
    econv = 1.0
    aconv = 1.0
    bconv = 1.0
    mconv2 = 1.0
    dconv = 1.0
    flconv = 1.0
    econv2 = 1.0
    tconv = 1.0
    tref = 459.6
    g0 = g0d = 32.2
    counter = 0
    showcom = 0
    plttyp = 3
    pltkeep = 0
    entype = 0
    inflag = 0
    varflag = 0
    pt2flag = 0
    wtflag = 0
    fireflag = 0
    gama = 1.4
    gamopt = 1
    u0d = 0.0
    altd = 0.0
    throtl = 100.0
    if i <= 19:
        tt[i] = 518.6
        prat[i] = 1.0
        pt[i] = 14.7
        eta[i] = 1.0
        i += 1
        if i <= 19:
            tt[i] = 518.6
            prat[i] = 1.0
            pt[i] = 14.7
            eta[i] = 1.0
            i += 1
            while i <= 19:
                tt[i] = 518.6
                prat[i] = 1.0
                pt[i] = 14.7
                eta[i] = 1.0
                i += 1
    tt[4] = tt4 = tt4d = 2500.0
    tt[7] = tt7 = tt7d = 2500.0
    prat[3] = p3p2d = 8.0
    prat[13] = p3fp2d = 2.0
    byprat = 1.0
    abflag = 0
    fueltype = 0
    fhvd = fhv = 18600.0
    a2d = a2 = acore = 2.0
    diameng = tf.math.sqrt(4.0 * a2d / 3.14159)
    ac = 0.9 * a2
    a8rat = 0.35
    a8 = 0.7
    a8d = 0.4
    arsched = 0
    afan = 2.0
    a4 = 0.418
    athsched = 1
    aexsched = 1
    arthmn = 0.1
    arthmx = 1.5
    arexmn = 1.0
    arexmx = 10.0
    arthd = arth = 0.4
    arexit = arexitd = 3.0
    u0mt = 1500.0
    u0mr = 4500.0
    altmt = 60000.0
    altmr = 100000.0
    u0min = 0.0
    u0max = u0mt
    altmin = 0.0
    altmax = altmt
    thrmin = 30
    thrmax = 100
    etmin = 0.5
    etmax = 1.0
    cprmin = 1.0
    cprmax = 50.0
    bypmin = 0.0
    bypmax = 10.0
    fprmin = 1.0
    fprmax = 2.0
    t4min = 1000.0
    t4max = 3200.0
    t7min = 1000.0
    t7max = 4000.0
    a8min = 0.1
    a8max = 0.4
    a2min = 0.001
    a2max = 50.0
    pt4max = 1.0
    diamin = tf.math.sqrt(4.0 * a2min / 3.14159)
    diamax = tf.math.sqrt(4.0 * a2max / 3.14159)
    pmax = 6000.0
    tmin = -300.0 + tref
    tmax = 600.0 + tref
    vmn1 = u0min
    vmx1 = u0max
    vmn2 = altmin
    vmx2 = altmax
    vmn3 = thrmin
    vmx3 = thrmax
    vmn4 = arexmn
    vmx4 = arexmx
    xtrans = 125.0
    ytrans = 115.0
    factor = 35.0
    sldloc = 75
    xtranp = 80.0
    ytranp = 180.0
    factp = 27.0
    sldplt = 130
    weight = 1000.0
    minlt = 1
    dinlt = 170.2
    tinlt = 900.0
    mfan = 2
    dfan = 293.02
    tfan = 1500.0
    mcomp = 2
    dcomp = 293.02
    tcomp = 1500.0
    mburner = 4
    dburner = 515.2
    tburner = 2500.0
    mturbin = 4
    dturbin = 515.2
    tturbin = 2500.0
    mnozl = 3
    dnozl = 515.2
    tnozl = 2500.0
    mnozr = 5
    dnozr = 515.2
    tnozr = 4500.0
    ncflag = 0
    ntflag = 0
    return


def getCp(temp, opt):
    """ generated source for method getCp """
    number = 0.0
    a = 0.0
    b = 0.0
    c = 0.0
    d = 0.0
    a = -4.470213e-13
    b = -5.1286514e-10
    c = 2.8323331e-05
    d = 0.2245283
    if opt == 0:
        number = 0.2399
    else:
        number = a * temp * temp * temp + b * temp * temp + c * temp + d
    return number


def getResponse2(u0d_val):
    global u0d
    setDefaults()
    loadCF6()
    u0d = u0d_val
    getFreeStream0()
    return fsmach
