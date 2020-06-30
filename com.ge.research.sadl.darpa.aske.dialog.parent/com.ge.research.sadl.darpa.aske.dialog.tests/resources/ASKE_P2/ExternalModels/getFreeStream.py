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
