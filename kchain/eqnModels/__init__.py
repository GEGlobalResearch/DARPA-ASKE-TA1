import autograd.numpy as np

""" Global variable declarations """ 
eta = [0]*20
cp = [0]*20
v = [0]*20
s = [0]*20
pt = [0]*20
gam = [0]*20
prat = [0]*20
trat = [0]*20
tt = [0]*20

def getFreeStream0():

    """ Specifying global scope for implicit variables """
    global a0, alt, altd, cpair, fsmach, gama, gamopt, lconv1, lconv2, ps0, psout, q0, rgas, rho0, ts0, tsout, u0, u0d

    """ generated source for method getFreeStream0 """

    rgas = 1718.

    #  ft2/sec2 R 

    alt = altd / lconv1

    ts0 = 0.0

    ps0 = 0.0

    if alt < 36152.:

        ts0 = 518.6 - 3.56 * alt / 1000.

        ps0 = 2116. * np.power(ts0 / 518.6, 5.256)

    if alt >= 36152. and alt <= 82345.:

        # // Stratosphere

        ts0 = 389.98

        ps0 = 2116. * 0.2236 * np.exp((36000. - alt) / (53.35 * 389.98))

    if alt >= 82345.:

        ts0 = 389.98 + 1.645 * (alt - 82345) / 1000.

        ps0 = 2116. * 0.02456 * np.power(ts0 / 389.98, -11.388)

    a0 = np.sqrt(gama * rgas * ts0)

    # /* speed of sound ft/sec */

    u0 = u0d / lconv2 * 5280. / 3600.

    # /* airspeed ft/sec */

    # //AG: input var is u0d

    fsmach = u0 / a0

    q0 = gama / 2.0 * fsmach * fsmach * ps0

    if u0 > 0.0001:

        rho0 = q0 / (u0 * u0)

    else:

        rho0 = 1.0

    tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)

    pt[0] = ps0 * np.power(tt[0] / ts0, gama / (gama - 1.0))

    ps0 = ps0 / 144.

    pt[0] = pt[0] / 144.

    cpair = getCp(tt[0], gamopt)

    # /*BTU/lbm R */

    tsout = ts0

    psout = ps0

    return


def getFreeStream1():

    """ Specifying global scope for implicit variables """
    global a0, alt, altd, cpair, fsmach, gama, gamopt, lconv1, lconv2, ps0, psout, q0, rgas, rho0, ts0, tsout, u0, u0d

    """ generated source for method getFreeStream1 """

    rgas = 1718.

    #  ft2/sec2 R 

    alt = altd / lconv1

    if alt < 36152.:

        ts0 = 518.6 - 3.56 * alt / 1000.

        ps0 = 2116. * np.power(ts0 / 518.6, 5.256)

    if alt >= 36152. and alt <= 82345.:

        # // Stratosphere

        ts0 = 389.98

        ps0 = 2116. * 0.2236 * np.exp((36000. - alt) / (53.35 * 389.98))

    if alt >= 82345.:

        ts0 = 389.98 + 1.645 * (alt - 82345) / 1000.

        ps0 = 2116. * 0.02456 * np.power(ts0 / 389.98, -11.388)

    a0 = np.sqrt(gama * rgas * ts0)

    # /* speed of sound ft/sec */

    u0 = fsmach * a0

    u0d = u0 * lconv2 / 5280. * 3600.

    # /* airspeed ft/sec */

    q0 = gama / 2.0 * fsmach * fsmach * ps0

    if u0 > 0.0001:

        rho0 = q0 / (u0 * u0)

    else:

        rho0 = 1.0

    tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)

    pt[0] = ps0 * np.power(tt[0] / ts0, gama / (gama - 1.0))

    ps0 = ps0 / 144.

    pt[0] = pt[0] / 144.

    cpair = getCp(tt[0], gamopt)

    # /*BTU/lbm R */

    tsout = ts0

    psout = ps0

    return


def getFreeStream2():

    """ Specifying global scope for implicit variables """
    global a0, cpair, fsmach, gama, gamopt, lconv2, ps0, ps0, psout, q0, rgas, rho0, ts0, tsout, u0, u0d

    """ generated source for method getFreeStream2 """

    rgas = 1718.

    #  ft2/sec2 R 

    ps0 = ps0 * 144.

    a0 = np.sqrt(gama * rgas * ts0)

    # /* speed of sound ft/sec */

    u0 = u0d / lconv2 * 5280. / 3600.

    # /* airspeed ft/sec */

    # //AG: input var is u0d

    fsmach = u0 / a0

    q0 = gama / 2.0 * fsmach * fsmach * ps0

    if u0 > 0.0001:

        rho0 = q0 / (u0 * u0)

    else:

        rho0 = 1.0

    tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)

    pt[0] = ps0 * np.power(tt[0] / ts0, gama / (gama - 1.0))

    ps0 = ps0 / 144.

    pt[0] = pt[0] / 144.

    cpair = getCp(tt[0], gamopt)

    # /*BTU/lbm R */

    tsout = ts0

    psout = ps0

    return


def getFreeStream3():

    """ Specifying global scope for implicit variables """
    global a0, cpair, fsmach, gama, gamopt, lconv2, ps0, ps0, psout, q0, rgas, rho0, ts0, tsout, u0, u0d

    """ generated source for method getFreeStream3 """

    rgas = 1718.

    #  ft2/sec2 R 

    ps0 = ps0 * 144.

    a0 = np.sqrt(gama * rgas * ts0)

    # /* speed of sound ft/sec */

    u0 = fsmach * a0

    u0d = u0 * lconv2 / 5280. * 3600.

    # /* airspeed ft/sec */

    q0 = gama / 2.0 * fsmach * fsmach * ps0

    if u0 > 0.0001:

        rho0 = q0 / (u0 * u0)

    else:

        rho0 = 1.0

    tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)

    pt[0] = ps0 * np.power(tt[0] / ts0, gama / (gama - 1.0))

    ps0 = ps0 / 144.

    pt[0] = pt[0] / 144.

    cpair = getCp(tt[0], gamopt)

    # /*BTU/lbm R */

    tsout = ts0

    psout = ps0

    return


def getGeo():

    """ Specifying global scope for implicit variables """
    global a2, a4, a4p, a8, a8d, a8max, a8rat, a8ref, ac, acore, aexsched, afan, afan, arexitd, arexmn, arexmx, arsched, arthd, arthmn, arthmx, athsched, entype, epr, etr, fsmach, gama, gamopt, lunits, mexit

    """ generated source for method getGeo """

    #  determine geometric variables 

    game = 0.0

    fl1 = 0.0

    i1 = 0

    if entype <= 2:

        # // turbine engines

        if afan < acore:

            afan = acore

        a8max = 0.75 * np.sqrt(etr) / epr

        # /* limits compressor face  */

        # /*  mach number  to < .5   */

        if a8max > 1.0:

            a8max = 1.0

        if a8rat > a8max:

            a8rat = a8max

            if lunits <= 1:

                fl1 = filter3(a8rat)

#                in_.nozl.left.f3.setText(String.valueOf(fl1))

                i1 = ((((a8rat - a8min) / (a8max - a8min)) * 1000.))

#                in_.nozl.right.s3.setValue(i1)

            if lunits == 2:

                fl1 = filter3(100. * (a8rat - a8ref) / a8ref)

#                in_.nozl.left.f3.setText(String.valueOf(fl1))

                i1 = (((((100. * (a8rat - a8ref) / a8ref) + 10.0) / 20.0) * 1000.))

#                in_.nozl.right.s3.setValue(i1)

        # /*    dumb down limit - a8 schedule */

        if arsched == 0:

            a8rat = a8max

            fl1 = filter3(a8rat)

#            in_.nozl.left.f3.setText(String.valueOf(fl1))

            i1 = ((((a8rat - a8min) / (a8max - a8min)) * 1000.))

#            in_.nozl.right.s3.setValue(i1)

        a8 = a8rat * acore

        a8d = a8 * prat[7] / np.sqrt(trat[7])

        # /* assumes choked a8 and a4 */

        a4 = a8 * prat[5] * prat[15] * prat[7] / np.sqrt(trat[7] * trat[5] * trat[15])

        a4p = a8 * prat[15] * prat[7] / np.sqrt(trat[7] * trat[15])

        ac = 0.9 * a2

    if entype == 3:

        # // ramjets

        game = getGama(tt[4], gamopt)

        if athsched == 0:

            # // scheduled throat area

            arthd = getAir(fsmach, gama) * np.sqrt(etr) / (getAir(1.0, game) * epr * prat[2])

            if arthd < arthmn:

                arthd = arthmn

            if arthd > arthmx:

                arthd = arthmx

            fl1 = filter3(arthd)

#            in_.nozr.left.f3.setText(String.valueOf(fl1))

            i1 = ((((arthd - arthmn) / (arthmx - arthmn)) * 1000.))

#            in_.nozr.right.s3.setValue(i1)

        if aexsched == 0:

            # // scheduled exit area

            mexit = np.sqrt((2.0 / (game - 1.0)) * ((1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach) * np.power((epr * prat[2]), (game - 1.0) / game) - 1.0))

            arexitd = getAir(1.0, game) / getAir(mexit, game)

            if arexitd < arexmn:

                arexitd = arexmn

            if arexitd > arexmx:

                arexitd = arexmx

            fl1 = filter3(arexitd)

#            in_.nozr.left.f4.setText(String.valueOf(fl1))

            i1 = ((((arexitd - arexmn) / (arexmx - arexmn)) * 1000.))

#            in_.nozr.right.s4.setValue(i1)


def getPerform():

    """ Specifying global scope for implicit variables """
    global a0, a2, a8, acore, afan, arexitd, arthd, byprat, cpair, dburner, dcomp, dfan, dinlt, dnozl, dnozr, dram, drlb, dturbin, eair, entype, epr, eteng, etr, fa, fglb, fgros, fhv, fireflag, flflo, fnet, fnlb, fntot, fsmach, fuelrat, g0, gama, gamopt, isp, lburn, lcomp, lnoz, lturb, m2, mexit, mfr, ncomp, npr, nturb, numeng, pexit, pfexit, ps0, rgas, sfc, snpr, t8, tburner, tcomp, tfan, tinlt, tnozl, tnozr, ts0, tturbin, u0, ues, uexit, weight, wtflag

    """ generated source for method getPerform """

    #  determine engine performance 

    fac1 = 0.0

    game = 0.0

    cpe = 0.0

    cp3 = 0.0

    rg = 0.0

    p8p5 = 0.0

    rg1 = 0.0

    index = 0

    rg1 = 53.3

    rg = cpair * (gama - 1.0) / gama

    cp3 = getCp(tt[3], gamopt)

    # /*BTU/lbm R */

    g0 = 32.2

    # //gravitational acceleration ft/sec2

    ues = 0.0

    game = getGama(tt[5], gamopt)

    fac1 = (game - 1.0) / game

    cpe = getCp(tt[5], gamopt)

    if eta[7] < 0.8:

        eta[7] = 0.8

    # /* protection during overwriting */

    if eta[4] < 0.8:

        eta[4] = 0.8

    # /*  specific net thrust  - thrust / (g0*airflow) -   lbf/lbm/sec  */

    # // turbine engine core

    if entype <= 2:

        # /* airflow determined at choked nozzle exit */

        pt[8] = epr * prat[2] * pt[0]

        eair = getAir(1.0, game) * 144. * a8 * pt[8] / 14.7 / np.sqrt(etr * tt[0] / 518.)

        m2 = getMach(0, eair * np.sqrt(tt[0] / 518.) / (prat[2] * pt[0] / 14.7 * acore * 144.), gama)

        npr = pt[8] / ps0

        uexit = np.sqrt(2.0 * rgas / fac1 * etr * tt[0] * eta[7] * (1.0 - np.power(1.0 / npr, fac1)))

        if npr <= 1.893:

            pexit = ps0

        else:

            pexit = 0.52828 * pt[8]

        fgros = (uexit + (pexit - ps0) * 144. * a8 / eair) / g0

    # // turbo fan -- added terms for fan flow

    if entype == 2:

        fac1 = (gama - 1.0) / gama

        snpr = pt[13] / ps0

        ues = np.sqrt(2.0 * rgas / fac1 * tt[13] * eta[7] * (1.0 - np.power(1.0 / snpr, fac1)))

        m2 = getMach(0, eair * (1.0 + byprat) * np.sqrt(tt[0] / 518.) / (prat[2] * pt[0] / 14.7 * afan * 144.), gama)

        if snpr <= 1.893:

            pfexit = ps0

        else:

            pfexit = 0.52828 * pt[13]

        fgros = fgros + (byprat * ues + (pfexit - ps0) * 144. * byprat * acore / eair) / g0

    # //TODO: Ramjet code start

    # // ramjets

    if entype == 3:

        # /* airflow determined at nozzle throat */

        eair = getAir(1.0, game) * 144.0 * a2 * arthd * epr * prat[2] * pt[0] / 14.7 / np.sqrt(etr * tt[0] / 518.)

        m2 = getMach(0, eair * np.sqrt(tt[0] / 518.) / (prat[2] * pt[0] / 14.7 * acore * 144.), gama)

        mexit = getMach(2, (getAir(1.0, game) / arexitd), game)

        uexit = mexit * np.sqrt(game * rgas * etr * tt[0] * eta[7] / (1.0 + 0.5 * (game - 1.0) * mexit * mexit))

        pexit = np.power((1.0 + 0.5 * (game - 1.0) * mexit * mexit), (-game / (game - 1.0))) * epr * prat[2] * pt[0]

        fgros = (uexit + (pexit - ps0) * arexitd * arthd * a2 / eair / 144.) / g0

    # // ram drag

    dram = u0 / g0

    if entype == 2:

        dram = dram + u0 * byprat / g0

    # // mass flow ratio

    if fsmach > 0.01:

        mfr = getAir(m2, gama) * prat[2] / getAir(fsmach, gama)

    else:

        mfr = 5.

    # // net thrust

    fnet = fgros - dram

    if entype == 3 and fsmach < 0.3:

        fnet = 0.0

        fgros = 0.0

    # // thrust in pounds

    fnlb = fnet * eair

    fglb = fgros * eair

    drlb = dram * eair

    # //fuel-air ratio and sfc

    fa = (trat[4] - 1.0) / (eta[4] * fhv / (cp3 * tt[3]) - trat[4]) + (trat[7] - 1.0) / (fhv / (cpe * tt[15]) - trat[7])

    if fnet > 0.0:

        sfc = 3600. * fa / fnet

        flflo = sfc * fnlb

        isp = (fnlb / flflo) * 3600.

    else:

        fnlb = 0.0

        flflo = 0.0

        sfc = 0.0

        isp = 0.0

    tt[8] = tt[7]

    t8 = etr * tt[0] - uexit * uexit / (2.0 * rgas * game / (game - 1.0))

    trat[8] = tt[8] / tt[7]

    p8p5 = ps0 / (epr * prat[2] * pt[0])

    cp[8] = getCp(tt[8], gamopt)

    pt[8] = pt[7]

    prat[8] = pt[8] / pt[7]

    # /* thermal effeciency */

    if entype == 2:

        eteng = (a0 * a0 * ((1.0 + fa) * (uexit * uexit / (a0 * a0)) + byprat * (ues * ues / (a0 * a0)) - (1.0 + byprat) * fsmach * fsmach)) / (2.0 * g0 * fa * fhv * 778.16)

    else:

        eteng = (a0 * a0 * ((1.0 + fa) * (uexit * uexit / (a0 * a0)) - fsmach * fsmach)) / (2.0 * g0 * fa * fhv * 778.16)

    s[0] = s[1] = 0.2

    v[0] = v[1] = rg1 * ts0 / (ps0 * 144.)

    # /* determine engine performance */

    # /*BTU/lbm R */

    # //gravitational acceleration ft/sec2

    # /* protection during overwriting */

    # /*  specific net thrust  - thrust / (g0*airflow) -   lbf/lbm/sec  */

    # // turbine engine core

    # /* airflow determined at choked nozzle exit */

    # //check if choked

    # // turbo fan -- added terms for fan flow

    # //TODO: Ramjet code start

    # // ramjets

    # /* airflow determined at nozzle throat */

    # // ram drag

    # // mass flow ratio

    # // net thrust

    # // thrust in pounds

    # //fuel-air ratio and sfc

    # /* thermal effeciency */

    while index <= 7:

        # /* determine engine performance */

        # /*BTU/lbm R */

        # //gravitational acceleration ft/sec2

        # /* protection during overwriting */

        # /*  specific net thrust  - thrust / (g0*airflow) -   lbf/lbm/sec  */

        # // turbine engine core

        # /* airflow determined at choked nozzle exit */

        # //check if choked

        # // turbo fan -- added terms for fan flow

        # //TODO: Ramjet code start

        # // ramjets

        # /* airflow determined at nozzle throat */

        # // ram drag

        # // mass flow ratio

        # // net thrust

        # // thrust in pounds

        # //fuel-air ratio and sfc

        # /* thermal effeciency */

        # /* compute entropy */

        s[index] = s[index - 1] + cpair * np.log(trat[index]) - rg * np.log(prat[index])

        v[index] = rg1 * tt[index] / (pt[index] * 144.)

        index += 1

    # /* compute entropy */

    s[13] = s[2] + cpair * np.log(trat[13]) - rg * np.log(prat[13])

    v[13] = rg1 * tt[13] / (pt[13] * 144.)

    s[15] = s[5] + cpair * np.log(trat[15]) - rg * np.log(prat[15])

    v[15] = rg1 * tt[15] / (pt[15] * 144.)

    s[8] = s[7] + cpair * np.log(t8 / (etr * tt[0])) - rg * np.log(p8p5)

    v[8] = rg1 * t8 / (ps0 * 144.)

    cp[0] = getCp(tt[0], gamopt)

    fntot = numeng * fnlb

    fuelrat = numeng * flflo

    # // weight  calculation

    if wtflag == 0:

        if entype == 0:

            # //            weight = .0838 * acore * (dcomp * ncomp + dburner +

            # //                     dturbin * nturb + dburner) * np.sqrt(acore / 1.753) ;

            weight = 0.132 * np.sqrt(acore * acore * acore) * (dcomp * lcomp + dburner * lburn + dturbin * lturb + dnozl * lnoz)

        if entype == 1:

            # //            weight = .0838 * acore * (dcomp * ncomp + dburner +

            # //                 dturbin * nturb + dburner * 2.0) * np.sqrt(acore / 6.00) ;

            weight = 0.100 * np.sqrt(acore * acore * acore) * (dcomp * lcomp + dburner * lburn + dturbin * lturb + dnozl * lnoz)

        if entype == 2:

            weight = 0.0932 * acore * ((1.0 + byprat) * dfan * 4.0 + dcomp * (ncomp - 3) + dburner + dturbin * nturb + dnozl * 2.0) * np.sqrt(acore / 6.965)

        if entype == 3:

            weight = 0.1242 * acore * (dburner + dnozr * 6. + dinlt * 3.) * np.sqrt(acore / 1.753)

    # // check for temp limits

#    out.vars.to1.setForeground(Color.yellow)

#    out.vars.to2.setForeground(Color.yellow)

#    out.vars.to3.setForeground(Color.yellow)

#    out.vars.to4.setForeground(Color.yellow)

#    out.vars.to5.setForeground(Color.yellow)

#    out.vars.to6.setForeground(Color.yellow)

#    out.vars.to7.setForeground(Color.yellow)

    if entype < 3:

        if tt[2] > tinlt:

            fireflag = 1

#            out.vars.to1.setForeground(Color.red)

#            out.vars.to2.setForeground(Color.red)

        if tt[13] > tfan:

            fireflag = 1

#            out.vars.to2.setForeground(Color.red)

        if tt[3] > tcomp:

            fireflag = 1

#            out.vars.to3.setForeground(Color.red)

        if tt[4] > tburner:

            fireflag = 1

#            out.vars.to4.setForeground(Color.red)

        if tt[5] > tturbin:

            fireflag = 1

#            out.vars.to5.setForeground(Color.red)

        if tt[7] > tnozl:

            fireflag = 1

#            out.vars.to6.setForeground(Color.red)

#            out.vars.to7.setForeground(Color.red)

    if entype == 3:

        if tt[3] > tinlt:

            fireflag = 1

#            out.vars.to1.setForeground(Color.red)

#            out.vars.to2.setForeground(Color.red)

#            out.vars.to3.setForeground(Color.red)

        if tt[4] > tburner:

            fireflag = 1

#            out.vars.to4.setForeground(Color.red)

        if tt[7] > tnozr:

            fireflag = 1

#            out.vars.to5.setForeground(Color.red)

#            out.vars.to6.setForeground(Color.red)

#            out.vars.to7.setForeground(Color.red)

#    if fireflag == 1:

#        view.start()


def getThermo():

    """ Specifying global scope for implicit variables """
    global a4, a4, a4p, a8d, abflag, acore, afan, byprat, entype, epr, etr, fsmach, gamopt, inflag, p3fp2d, p3p2d, pt2flag, throtl, tt4, tt7

    """ generated source for method getThermo """

    dela = 0.0

    t5t4n = 0.0

    deriv = 0.0

    delan = 0.0

    m5 = 0.0

    delhc = 0.0

    delhht = 0.0

    delhf = 0.0

    delhlt = 0.0

    deltc = 0.0

    deltht = 0.0

    deltf = 0.0

    deltlt = 0.0

    itcount = 0

    # ,index ;

    fl1 = 0.0

    i1 = 0

    # /*   inlet recovery  */

    if pt2flag == 0:

        # /*     mil spec      */

        if fsmach > 1.0:

            # /* supersonic */

            prat[2] = 1.0 - 0.075 * np.power(fsmach - 1.0, 1.35)

        else:

            prat[2] = 1.0

        eta[2] = prat[2]

        fl1 = filter3(prat[2])

#        in_.inlet.left.f1.setText(String.valueOf(fl1))

        i1 = ((((prat[2] - etmin) / (etmax - etmin)) * 1000.))

#        in_.inlet.right.s1.setValue(i1)

    else:

        # /* enter value */

        prat[2] = eta[2]

    # /* protection for overwriting input */

    if eta[3] < 0.5:

        eta[3] = 0.5

    if eta[5] < 0.5:

        eta[5] = 0.5

    trat[7] = 1.0

    prat[7] = 1.0

    tt[2] = tt[1] = tt[0]

    pt[1] = pt[0]

    gam[2] = getGama(tt[2], gamopt)

    cp[2] = getCp(tt[2], gamopt)

    pt[2] = pt[1] * prat[2]

    # /* design - p3p2 specified - tt4 specified */

    if inflag == 0:

        if entype <= 1:

            # /* turbojet */

            prat[3] = p3p2d

            # /* core compressor */

            if prat[3] < 0.5:

                prat[3] = 0.5

            delhc = (cp[2] * tt[2] / eta[3]) * (np.power(prat[3], (gam[2] - 1.0) / gam[2]) - 1.0)

            deltc = delhc / cp[2]

            pt[3] = pt[2] * prat[3]

            tt[3] = tt[2] + deltc

            trat[3] = tt[3] / tt[2]

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            tt[4] = tt4 * throtl / 100.0

            gam[4] = getGama(tt[4], gamopt)

            cp[4] = getCp(tt[4], gamopt)

            trat[4] = tt[4] / tt[3]

            pt[4] = pt[3] * prat[4]

            delhht = delhc

            deltht = delhht / cp[4]

            tt[5] = tt[4] - deltht

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            trat[5] = tt[5] / tt[4]

            prat[5] = np.power((1.0 - delhht / cp[4] / tt[4] / eta[5]), (gam[4] / (gam[4] - 1.0)))

            pt[5] = pt[4] * prat[5]

            # /* fan conditions */

            prat[13] = 1.0

            trat[13] = 1.0

            tt[13] = tt[2]

            pt[13] = pt[2]

            gam[13] = gam[2]

            cp[13] = cp[2]

            prat[15] = 1.0

            pt[15] = pt[5]

            trat[15] = 1.0

            tt[15] = tt[5]

            gam[15] = gam[5]

            cp[15] = cp[5]

        if entype == 2:

            # /* turbofan */

            prat[13] = p3fp2d

            if prat[13] < 0.5:

                prat[13] = 0.5

            delhf = (cp[2] * tt[2] / eta[13]) * (np.power(prat[13], (gam[2] - 1.0) / gam[2]) - 1.0)

            deltf = delhf / cp[2]

            tt[13] = tt[2] + deltf

            pt[13] = pt[2] * prat[13]

            trat[13] = tt[13] / tt[2]

            gam[13] = getGama(tt[13], gamopt)

            cp[13] = getCp(tt[13], gamopt)

            prat[3] = p3p2d

            # /* core compressor */

            if prat[3] < 0.5:

                prat[3] = 0.5

            delhc = (cp[13] * tt[13] / eta[3]) * (np.power(prat[3], (gam[13] - 1.0) / gam[13]) - 1.0)

            deltc = delhc / cp[13]

            tt[3] = tt[13] + deltc

            pt[3] = pt[13] * prat[3]

            trat[3] = tt[3] / tt[13]

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            tt[4] = tt4 * throtl / 100.0

            pt[4] = pt[3] * prat[4]

            gam[4] = getGama(tt[4], gamopt)

            cp[4] = getCp(tt[4], gamopt)

            trat[4] = tt[4] / tt[3]

            delhht = delhc

            deltht = delhht / cp[4]

            tt[5] = tt[4] - deltht

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            trat[5] = tt[5] / tt[4]

            prat[5] = np.power((1.0 - delhht / cp[4] / tt[4] / eta[5]), (gam[4] / (gam[4] - 1.0)))

            pt[5] = pt[4] * prat[5]

            delhlt = (1.0 + byprat) * delhf

            deltlt = delhlt / cp[5]

            tt[15] = tt[5] - deltlt

            gam[15] = getGama(tt[15], gamopt)

            cp[15] = getCp(tt[15], gamopt)

            trat[15] = tt[15] / tt[5]

            prat[15] = np.power((1.0 - delhlt / cp[5] / tt[5] / eta[5]), (gam[5] / (gam[5] - 1.0)))

            pt[15] = pt[5] * prat[15]

        if entype == 3:

            # /* ramjet */

            prat[3] = 1.0

            pt[3] = pt[2] * prat[3]

            tt[3] = tt[2]

            trat[3] = 1.0

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            tt[4] = tt4 * throtl / 100.0

            gam[4] = getGama(tt[4], gamopt)

            cp[4] = getCp(tt[4], gamopt)

            trat[4] = tt[4] / tt[3]

            pt[4] = pt[3] * prat[4]

            tt[5] = tt[4]

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            trat[5] = 1.0

            prat[5] = 1.0

            pt[5] = pt[4]

            # /* fan conditions */

            prat[13] = 1.0

            trat[13] = 1.0

            tt[13] = tt[2]

            pt[13] = pt[2]

            gam[13] = gam[2]

            cp[13] = cp[2]

            prat[15] = 1.0

            pt[15] = pt[5]

            trat[15] = 1.0

            tt[15] = tt[5]

            gam[15] = gam[5]

            cp[15] = cp[5]

        tt[7] = tt7

    else:

        # /* analysis -assume flow choked at both turbine entrances */

        # /* and nozzle throat ... then*/

        tt[4] = tt4 * throtl / 100.0

        gam[4] = getGama(tt[4], gamopt)

        cp[4] = getCp(tt[4], gamopt)

        if a4 < 0.02:

            a4 = 0.02

        if entype <= 1:

            # /* turbojet */

            dela = 0.2

            # /* iterate to get t5t4 */

            trat[5] = 1.0

            t5t4n = 0.5

            itcount = 0

            while np.abs(dela) > 0.001 and itcount < 20:

                itcount += 1

                delan = a8d / a4 - np.sqrt(t5t4n) * np.power((1.0 - (1.0 / eta[5]) * (1.0 - t5t4n)), -gam[4] / (gam[4] - 1.0))

                deriv = (delan - dela) / (t5t4n - trat[5])

                dela = delan

                trat[5] = t5t4n

                t5t4n = trat[5] - dela / deriv

            tt[5] = tt[4] * trat[5]

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            deltht = tt[5] - tt[4]

            delhht = cp[4] * deltht

            prat[5] = np.power((1.0 - (1.0 / eta[5]) * (1.0 - trat[5])), gam[4] / (gam[4] - 1.0))

            delhc = delhht

            # /* compressor work */

            deltc = -delhc / cp[2]

            tt[3] = tt[2] + deltc

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            trat[3] = tt[3] / tt[2]

            prat[3] = np.power((1.0 + eta[3] * (trat[3] - 1.0)), gam[2] / (gam[2] - 1.0))

            trat[4] = tt[4] / tt[3]

            pt[3] = pt[2] * prat[3]

            pt[4] = pt[3] * prat[4]

            pt[5] = pt[4] * prat[5]

            # /* fan conditions */

            prat[13] = 1.0

            trat[13] = 1.0

            tt[13] = tt[2]

            pt[13] = pt[2]

            gam[13] = gam[2]

            cp[13] = cp[2]

            prat[15] = 1.0

            pt[15] = pt[5]

            trat[15] = 1.0

            tt[15] = tt[5]

            gam[15] = gam[5]

            cp[15] = cp[5]

        if entype == 2:

            # /*  turbofan */

            dela = 0.2

            # /* iterate to get t5t4 */

            trat[5] = 1.0

            t5t4n = 0.5

            itcount = 0

            while np.abs(dela) > 0.001 and itcount < 20:

                itcount += 1

                delan = a4p / a4 - np.sqrt(t5t4n) * np.power((1.0 - (1.0 / eta[5]) * (1.0 - t5t4n)), -gam[4] / (gam[4] - 1.0))

                deriv = (delan - dela) / (t5t4n - trat[5])

                dela = delan

                trat[5] = t5t4n

                t5t4n = trat[5] - dela / deriv

            tt[5] = tt[4] * trat[5]

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            deltht = tt[5] - tt[4]

            delhht = cp[4] * deltht

            prat[5] = np.power((1.0 - (1.0 / eta[5]) * (1.0 - trat[5])), gam[4] / (gam[4] - 1.0))

            # /* iterate to get t15t14 */

            dela = 0.2

            trat[15] = 1.0

            t5t4n = 0.5

            itcount = 0

            while np.abs(dela) > 0.001 and itcount < 20:

                itcount += 1

                delan = a8d / a4p - np.sqrt(t5t4n) * np.power((1.0 - (1.0 / eta[5]) * (1.0 - t5t4n)), -gam[5] / (gam[5] - 1.0))

                deriv = (delan - dela) / (t5t4n - trat[15])

                dela = delan

                trat[15] = t5t4n

                t5t4n = trat[15] - dela / deriv

            tt[15] = tt[5] * trat[15]

            gam[15] = getGama(tt[15], gamopt)

            cp[15] = getCp(tt[15], gamopt)

            deltlt = tt[15] - tt[5]

            delhlt = cp[5] * deltlt

            prat[15] = np.power((1.0 - (1.0 / eta[5]) * (1.0 - trat[15])), gam[5] / (gam[5] - 1.0))

            byprat = afan / acore - 1.0

            delhf = delhlt / (1.0 + byprat)

            # /* fan work */

            deltf = -delhf / cp[2]

            tt[13] = tt[2] + deltf

            gam[13] = getGama(tt[13], gamopt)

            cp[13] = getCp(tt[13], gamopt)

            trat[13] = tt[13] / tt[2]

            prat[13] = np.power((1.0 + eta[13] * (trat[13] - 1.0)), gam[2] / (gam[2] - 1.0))

            delhc = delhht

            # /* compressor work */

            deltc = -delhc / cp[13]

            tt[3] = tt[13] + deltc

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            trat[3] = tt[3] / tt[13]

            prat[3] = np.power((1.0 + eta[3] * (trat[3] - 1.0)), gam[13] / (gam[13] - 1.0))

            trat[4] = tt[4] / tt[3]

            pt[13] = pt[2] * prat[13]

            pt[3] = pt[13] * prat[3]

            pt[4] = pt[3] * prat[4]

            pt[5] = pt[4] * prat[5]

            pt[15] = pt[5] * prat[15]

        if entype == 3:

            # /* ramjet */

            # //AG: same settings as above

            prat[3] = 1.0

            pt[3] = pt[2] * prat[3]

            tt[3] = tt[2]

            trat[3] = 1.0

            gam[3] = getGama(tt[3], gamopt)

            cp[3] = getCp(tt[3], gamopt)

            tt[4] = tt4 * throtl / 100.0

            trat[4] = tt[4] / tt[3]

            pt[4] = pt[3] * prat[4]

            tt[5] = tt[4]

            gam[5] = getGama(tt[5], gamopt)

            cp[5] = getCp(tt[5], gamopt)

            trat[5] = 1.0

            prat[5] = 1.0

            pt[5] = pt[4]

            # /* fan conditions */

            prat[13] = 1.0

            trat[13] = 1.0

            tt[13] = tt[2]

            pt[13] = pt[2]

            gam[13] = gam[2]

            cp[13] = cp[2]

            prat[15] = 1.0

            pt[15] = pt[5]

            trat[15] = 1.0

            tt[15] = tt[5]

            gam[15] = gam[5]

            cp[15] = cp[5]

        if abflag == 1:

            tt[7] = tt7

    prat[6] = 1.0

    pt[6] = pt[15]

    trat[6] = 1.0

    tt[6] = tt[15]

    gam[6] = getGama(tt[6], gamopt)

    cp[6] = getCp(tt[6], gamopt)

    if abflag > 0:

        # /* afterburner */

        trat[7] = tt[7] / tt[6]

        m5 = getMach(0, getAir(1.0, gam[5]) * a4 / acore, gam[5])

        prat[7] = getRayleighLoss(m5, trat[7], tt[6])

    tt[7] = tt[6] * trat[7]

    pt[7] = pt[6] * prat[7]

    gam[7] = getGama(tt[7], gamopt)

    cp[7] = getCp(tt[7], gamopt)

    # /* engine press ratio EPR*/

    epr = prat[7] * prat[15] * prat[5] * prat[4] * prat[3] * prat[13]

    # /* engine temp ratio ETR */

    etr = trat[7] * trat[15] * trat[5] * trat[4] * trat[3] * trat[13]

    return


def loadCF6():

    """ Specifying global scope for implicit variables """
    global a2, a2d, a4, a4p, a8d, a8max, a8rat, abflag, ac, acore, afan, altd, altmax, altmt, arsched, byprat, dburner, dcomp, dfan, diameng, dinlt, dnozl, dturbin, entype, fhv, fhvd, fueltype, gama, gamopt, mburner, mcomp, mfan, minlt, mnozl, mturbin, ncflag, ntflag, p3fp2d, p3p2d, pt2flag, tburner, tcomp, tfan, tinlt, tnozl, tt4, tt4d, tt7, tt7d, tturbin, u0d, u0max, u0mt, weight, wtflag

    """ generated source for method loadCF6 """

    entype = 2

    abflag = 0

    fueltype = 0

    fhvd = fhv = 18600.

    tt[4] = tt4 = tt4d = 2500.

    # turbine inlet temp (GE90 uses 2900)

    tt[7] = tt7 = tt7d = 2500.

    prat[3] = p3p2d = 21.86

    # //high-press compression ratio (new engines, 30)

    prat[13] = p3fp2d = 1.745

    # //low-press compression ratio (new engines 1.5)

    byprat = 3.3

    # // fan bypass ratio =  fan flow/core flow (GE90 uses 9, GENx 11)

    acore = 6.965

    # // area of core (typically at station 2)

    afan = acore * (1.0 + byprat)

    # // area of fan

    a2d = a2 = afan

    # // 

    diameng = np.sqrt(4.0 * a2d / 3.14159)

    # // diameter of engine 

    a4 = 0.290

    # // area of core (typically at station 4)

    a4p = 1.131

    ac = 0.9 * a2

    gama = 1.4

    gamopt = 1

    pt2flag = 0

    eta[2] = 1.0

    prat[2] = 1.0

    prat[4] = 1.0

    eta[3] = 0.959

    # //compressor efficiency

    eta[4] = 0.984

    eta[5] = 0.982

    eta[7] = 1.0

    eta[13] = 1.0

    a8d = 2.436

    # //exit area

    a8max = 0.35

    a8rat = 0.35

    u0max = u0mt

    u0d = 0.0

    altmax = altmt

    altd = 0.0

    arsched = 0

    wtflag = 0

    weight = 8229.

    # //engine weight

    minlt = 1

    dinlt = 170.

    tinlt = 900.

    mfan = 2

    dfan = 293.

    tfan = 1500.

    mcomp = 0

    dcomp = 293.

    tcomp = 1600.

    mburner = 4

    dburner = 515.

    tburner = 2500.

    mturbin = 4

    dturbin = 515.

    tturbin = 2500.

    mnozl = 3

    dnozl = 515.

    tnozl = 2500.

    ncflag = 0

    ntflag = 0

    # //        con.setPanl() ;

    return


def setDefaults():

    """ Specifying global scope for implicit variables """
    global a2, a2d, a2max, a2min, a4, a8, a8d, a8max, a8min, a8rat, abflag, ac, aconv, acore, aexsched, afan, altd, altmax, altmin, altmr, altmt, arexit, arexitd, arexmn, arexmx, arsched, arth, arthd, arthmn, arthmx, athsched, bconv, bypmax, bypmin, byprat, counter, cprmax, cprmin, dburner, dcomp, dconv, dfan, diamax, diameng, diamin, dinlt, dnozl, dnozr, dturbin, econv, econv2, entype, etmax, etmin, factor, factp, fconv, fhv, fhvd, fireflag, flconv, fprmax, fprmin, fueltype, g0, g0d, gama, gamopt, inflag, inptype, lburn, lcomp, lconv1, lconv2, lnoz, lturb, lunits, mburner, mcomp, mconv1, mconv2, mfan, minlt, mnozl, mnozr, move, mturbin, ncflag, ncomp, ntflag, nturb, numeng, p3fp2d, p3p2d, pconv, pltkeep, plttyp, pmax, pt2flag, pt4max, showcom, siztype, sldloc, sldplt, t4max, t4min, t7max, t7min, tburner, tcomp, tconv, tfan, thrmax, thrmin, throtl, tinlt, tmax, tmin, tnozl, tnozr, tref, tt4, tt4d, tt7, tt7d, tturbin, u0d, u0max, u0min, u0mr, u0mt, varflag, vmn1, vmn2, vmn3, vmn4, vmx1, vmx2, vmx3, vmx4, weight, wtflag, xtranp, xtrans, ytranp, ytrans

    """ generated source for method setDefaults """

    numeng = 1 

    fireflag = 0 

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

    throtl = 100.

    while i <= 19:

        trat[i] = 1.0

        tt[i] = 518.6

        prat[i] = 1.0

        pt[i] = 14.7

        eta[i] = 1.0

        i += 1

    tt[4] = tt4 = tt4d = 2500.

    tt[7] = tt7 = tt7d = 2500.

    prat[3] = p3p2d = 8.0

    prat[13] = p3fp2d = 2.0

    byprat = 1.0

    abflag = 0

    fueltype = 0

    fhvd = fhv = 18600.

    a2d = a2 = acore = 2.0

    diameng = np.sqrt(4.0 * a2d / 3.14159)

    ac = 0.9 * a2

    a8rat = 0.35

    a8 = 0.7

    a8d = 0.40

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

    u0mt = 1500.

    u0mr = 4500.

    altmt = 60000.

    altmr = 100000.

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

    a2max = 50.

    pt4max = 1.0

    diamin = np.sqrt(4.0 * a2min / 3.14159)

    diamax = np.sqrt(4.0 * a2max / 3.14159)

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

    factor = 35.

    sldloc = 75

    xtranp = 80.0

    ytranp = 180.0

    factp = 27.

    sldplt = 130

    weight = 1000.

    minlt = 1

    dinlt = 170.2

    tinlt = 900.

    mfan = 2

    dfan = 293.02

    tfan = 1500.

    mcomp = 2

    dcomp = 293.02

    tcomp = 1500.

    mburner = 4

    dburner = 515.2

    tburner = 2500.

    mturbin = 4

    dturbin = 515.2

    tturbin = 2500.

    mnozl = 3

    dnozl = 515.2

    tnozl = 2500.

    mnozr = 5

    dnozr = 515.2

    tnozr = 4500.

    ncflag = 0

    ntflag = 0





    ncomp = (1.0 + p3p2d / 1.5) 

    if ncomp > 15: 

       ncomp = 15 



    hblade = np.sqrt(2.0/3.1415926)

    lburn = hblade



    sblade = .02

    hblade = np.sqrt(2.0/3.1415926)

    tblade = .2*hblade



    xcomp = ncomp*(tblade+sblade)

    lcomp = xcomp



    lnoz = lburn 

    if entype == 1:

       lnoz = 3.0 * lburn 

    if entype == 3:

      lnoz = 3.0 * lburn 



    if ntflag == 0 :

       nturb = 1 + ncomp/4 

       if entype == 2:

          nturb = nturb + 1



    lturb = nturb*(tblade+sblade)





    return


def filter3( inumbr):


    """ generated source for method filter3 """

    #   output only to .001

    number = 0.0

    intermed = 0

    intermed = ((inumbr * 1000.))

    number = ((intermed / 1000.))

    return number


def getAir( mach, gamma):


    """ generated source for method getAir """

    #  Utility to get the corrected airflow per area given the Mach number 

    number = 0.0

    fac1 = 0.0

    fac2 = 0.0

    fac2 = (gamma + 1.0) / (2.0 * (gamma - 1.0))

    fac1 = np.power((1.0 + 0.5 * (gamma - 1.0) * mach * mach), fac2)

    number = 0.50161 * np.sqrt(gamma) * mach / fac1

    return (number)


def getCp( temp, opt):


    """ generated source for method getCp """

    #  Utility to get cp as a function of temp 

    number = 0.0

    a = 0.0

    b = 0.0

    c = 0.0

    d = 0.0

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


def getGama( temp, opt):


    """ generated source for method getGama """

    #  Utility to get gamma as a function of temp 

    number = 0.0

    a = 0.0

    b = 0.0

    c = 0.0

    d = 0.0

    a = -7.6942651e-13

    b = 1.3764661e-08

    c = -7.8185709e-05

    d = 1.436914

    if opt == 0:

        number = 1.4

    else:

        number = a * temp * temp * temp + b * temp * temp + c * temp + d

    return (number)


def getMach( sub, corair, gamma):


    """ generated source for method getMach """

    #  Utility to get the Mach number given the corrected airflow per area 

    number = 0.0

    chokair = 0.0

    # /* iterate for mach number */

    deriv = 0.0

    machn = 0.0

    macho = 0.0

    airo = 0.0

    airn = 0.0

    iter = 0

    chokair = getAir(1.0, gamma)

    if corair > chokair:

        number = 1.0

        return (number)

    else:

        airo = 0.25618

        # /* initial guess */

        if sub == 1:

            macho = 1.0

        else:

            # /* sonic */

            if sub == 2:

                macho = 1.703

            else:

                macho = 0.5

            # /* subsonic */

            iter = 1

            machn = macho - 0.2

            while np.abs(corair - airo) > 0.0001 and iter < 20:

                airn = getAir(machn, gamma)

                deriv = (airn - airo) / (machn - macho)

                airo = airn

                macho = machn

                machn = macho + (corair - airo) / deriv

                iter += 1

        number = macho

    return (number)


def getRayleighLoss( mach1, ttrat, tlow):

    """ Specifying global scope for implicit variables """
    global gamopt

    """ generated source for method getRayleighLoss """

    #  analysis for rayleigh flow 

    number = 0.0

    wc1 = 0.0

    wc2 = 0.0

    mgueso = 0.0

    mach2 = 0.0

    g1 = 0.0

    gm1 = 0.0

    g2 = 0.0

    gm2 = 0.0

    fac1 = 0.0

    fac2 = 0.0

    fac3 = 0.0

    fac4 = 0.0

    g1 = getGama(tlow, gamopt)

    gm1 = g1 - 1.0

    wc1 = getAir(mach1, g1)

    g2 = getGama(tlow * ttrat, gamopt)

    gm2 = g2 - 1.0

    number = 0.95

    # /* iterate for mach downstream */

    mgueso = 0.4

    # /* initial guess */

    mach2 = 0.5

    while np.abs(mach2 - mgueso) > 0.0001:

        mgueso = mach2

        fac1 = 1.0 + g1 * mach1 * mach1

        fac2 = 1.0 + g2 * mach2 * mach2

        fac3 = np.power((1.0 + 0.5 * gm1 * mach1 * mach1), (g1 / gm1))

        fac4 = np.power((1.0 + 0.5 * gm2 * mach2 * mach2), (g2 / gm2))

        number = fac1 * fac4 / fac2 / fac3

        wc2 = wc1 * np.sqrt(ttrat) / number

        mach2 = getMach(0, wc2, g2)

    return (number)



def getResponseCF6(altd_val,u0d_val):
    global altd,u0d

    setDefaults()

    loadCF6()

    altd = altd_val
    u0d = u0d_val
    getFreeStream0()
    getThermo()
    getPerform()

    return fireflag,sfc,fnet,fnlb


