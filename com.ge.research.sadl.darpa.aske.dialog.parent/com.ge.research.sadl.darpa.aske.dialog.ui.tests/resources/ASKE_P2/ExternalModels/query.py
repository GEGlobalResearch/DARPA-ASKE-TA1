import math as Math
trat = [0]*20
tt   = [0]*20
prat = [0]*20
pt   = [0]*20
eta  = [0]*20
gam  = [0]*20
cp   = [0]*20
s    = [0]*20
v    = [0]*20
def setDefaults():
        global a2
        global a2d
        global a2max
        global a2min
        global a4
        global a8
        global a8d
        global a8max
        global a8min
        global a8rat
        global abflag
        global acap
        global aconv
        global acore
        global aexsched
        global afan
        global altd
        global altmax
        global altmin
        global arexit
        global arexitd
        global arexmn
        global arexmx
        global arsched
        global arth
        global arthd
        global arthmn
        global arthmx
        global athsched
        global bconv
        global bypmax
        global bypmin
        global byprat
        global counter
        global cprmax
        global cprmin
        global dburner
        global dcomp
        global dconv
        global dfan
        global diamax
        global diameng
        global diamin
        global dinlt
        global dnozl
        global dnozr
        global dturbin
        global econv
        global econv2
        global entype
        global etmax
        global etmin
        global factor
        global factp
        global fconv
        global fhv
        global fhvd
        global fireflag
        global flconv
        global fprmax
        global fprmin
        global fueltype
        global g0
        global g0d
        global gama
        global gamopt
        global inflag
        global inptype
        global lconv1
        global lconv2
        global lunits
        global mburner
        global mcomp
        global mconv1
        global mconv2
        global mfan
        global minlt
        global mnozl
        global mnozr
        global move
        global mturbin
        global ncflag
        global ntflag
        global p3fp2d
        global p3p2d
        global pconv
        global pltkeep
        global plttyp
        global pmax
        global pt2flag
        global showcom
        global siztype
        global sldloc
        global sldplt
        global t4max
        global t4min
        global t7max
        global t7min
        global tburner
        global tcomp
        global tconv
        global tfan
        global thrmax
        global thrmin
        global throtl
        global tinlt
        global tmax
        global tmin
        global tnozl
        global tnozr
        global tref
        global tt4
        global tt4d
        global tt7
        global tt7d
        global tturbin
        global u0d
        global u0max
        global u0min
        global varflag
        global vmn1
        global vmn2
        global vmn3
        global vmn4
        global vmx1
        global vmx2
        global vmx3
        global vmx4
        global weight
        global wtflag
        global xtranp
        global xtrans
        global ytranp
        global ytrans

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
        diameng = Math.sqrt(4.0 * a2d / 3.14159)
        acap = 0.9 * a2
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
        u0min = 0.0
        u0max = 1500.
        altmin = 0.0
        altmax = 60000.
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
        diamin = Math.sqrt(4.0 * a2min / 3.14159)
        diamax = Math.sqrt(4.0 * a2max / 3.14159)
        pmax = 20.0
        tmin = -100.0 + tref
        tmax = 100.0 + tref
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
        sldplt = 138
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
        return
def getFreeStream():
        global a0
        global alt
        global altd
        global cpair
        global fsmach
        global gama
        global inptype
        global lconv1
        global lconv2
        global ps0
        global psout
        global q0
        global rgas
        global rho0
        global ts0
        global tsout
        global u0
        global u0d
        
        rgas = 1718.
        #  ft2/sec2 R 
        if inptype >= 2:
            ps0 = ps0 * 144.
        if inptype <= 1:
            # /* input altitude */
            alt = altd / lconv1
            if alt < 36152.:
                ts0 = 518.6 - 3.56 * alt / 1000.
                ps0 = 2116. * Math.pow(ts0 / 518.6, 5.256)
            if alt >= 36152. and alt <= 82345.:
                # // Stratosphere
                ts0 = 389.98
                ps0 = 2116. * 0.2236 * Math.exp((36000. - alt) / (53.35 * 389.98))
            if alt >= 82345.:
                ts0 = 389.98 + 1.645 * (alt - 82345) / 1000.
                ps0 = 2116. * 0.02456 * Math.pow(ts0 / 389.98, -11.388)
        a0 = Math.sqrt(gama * rgas * ts0)
        # /* speed of sound ft/sec */
        if inptype == 0 or inptype == 2:
            # /* input speed  */
            u0 = u0d / lconv2 * 5280. / 3600.
            # /* airspeed ft/sec */
            fsmach = u0 / a0
            q0 = gama / 2.0 * fsmach * fsmach * ps0
        if inptype == 1 or inptype == 3:
            # /* input mach */
            u0 = fsmach * a0
            u0d = u0 * lconv2 / 5280. * 3600.
            # /* airspeed ft/sec */
            q0 = gama / 2.0 * fsmach * fsmach * ps0
        if u0 > 0.0001:
            rho0 = q0 / (u0 * u0)
        else:
            rho0 = 1.0
        tt[0] = ts0 * (1.0 + 0.5 * (gama - 1.0) * fsmach * fsmach)
        pt[0] = ps0 * Math.pow(tt[0] / ts0, gama / (gama - 1.0))
        ps0 = ps0 / 144.
        pt[0] = pt[0] / 144.
        cpair = getCp(tt[0], gamopt)
        # /*BTU/lbm R */
        tsout = ts0
        psout = ps0
        return

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
def loadCF6():

        global a2
        global a2d
        global a4
        global a4p
        global a8d
        global a8max
        global a8rat
        global abflag
        global acap
        global acore
        global afan
        global altd
        global arsched
        global byprat
        global dburner
        global dcomp
        global dfan
        global diameng
        global dinlt
        global dnozl
        global dturbin
        global entype
        global fhv
        global fhvd
        global fueltype
        global gama
        global gamopt
        global mburner
        global mcomp
        global mfan
        global minlt
        global mnozl
        global mturbin
        global ncflag
        global ntflag
        global p3fp2d
        global p3p2d
        global pt2flag
        global tburner
        global tcomp
        global tfan
        global tinlt
        global tnozl
        global tt4
        global tt4d
        global tt7
        global tt7d
        global tturbin
        global u0d
        global u0max
        global weight
        global wtflag


        entype = 2
        abflag = 0
        fueltype = 0
        fhvd = fhv = 18600.
        tt[4] = tt4 = tt4d = 2500.
        tt[7] = tt7 = tt7d = 2500.
        prat[3] = p3p2d = 21.86
        prat[13] = p3fp2d = 1.745
        byprat = 3.3
        acore = 6.965
        afan = acore * (1.0 + byprat)
        a2d = a2 = afan
        diameng = Math.sqrt(4.0 * a2d / 3.14159)
        a4 = 0.290
        a4p = 1.131
        acap = 0.9 * a2
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
        u0max = 1500.
        u0d = 0.0
        altd = 0.0
        arsched = 0
        wtflag = 0
        weight = 8229.
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
        return
