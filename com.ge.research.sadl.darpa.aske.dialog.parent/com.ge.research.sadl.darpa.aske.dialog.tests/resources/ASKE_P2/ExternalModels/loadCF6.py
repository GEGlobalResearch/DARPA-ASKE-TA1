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
