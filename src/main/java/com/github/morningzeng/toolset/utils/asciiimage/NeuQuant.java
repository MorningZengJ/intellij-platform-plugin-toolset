/* NeuQuant Neural-Net Quantization Algorithm
 * ------------------------------------------
 *
 * Copyright (c) 1994 Anthony Dekker
 *
 * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994.
 * See "Kohonen neural networks for optimal colour quantization"
 * in "Network: Computation in Neural Systems" Vol. 5 (1994) pp 351-367.
 * for a discussion of the algorithm.
 *
 * Any party obtaining a copy of these files from the author, directly or
 * indirectly, is granted, free of charge, a full and unrestricted irrevocable,
 * world-wide, paid up, royalty-free, nonexclusive right and license to deal
 * in this software and documentation files (the "Software"), including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons who receive
 * copies from any such party to do so, with the only requirement being
 * that this copyright notice remain intact.
 */

// Ported to Java 12/00 K Weiner

package com.github.morningzeng.toolset.utils.asciiimage;

public class NeuQuant {

    protected static final int netSize = 256; /* number of colours used */

    /* four primes near 500 - assume no image has a length so large */
    /* that it is divisible by all four primes */
    protected static final int prime1 = 499;
    protected static final int prime2 = 491;
    protected static final int prime3 = 487;
    protected static final int prime4 = 503;

    protected static final int minPictureBytes = (3 * prime4);
    /* minimum size for input image */

	/* Program Skeleton
	   ----------------
	   [select samplefac in range 1..30]
	   [read image from input file]
	   pic = (unsigned char*) malloc(3*width*height);
	   initnet(pic,3*width*height,samplefac);
	   learn();
	   unbiasnet();
	   [write output image header, using writecolourmap(f)]
	   inxbuild();
	   write output image using inxsearch(b,g,r)      */

	/* Network Definitions
	   ------------------- */

    protected static final int maxNetPos = (netSize - 1);
    protected static final int netBiasShift = 4; /* bias for colour values */
    protected static final int nCycles = 100; /* no. of learning cycles */

    /* defs for freq and bias */
    protected static final int intBiasShift = 16; /* bias for fractions */
    protected static final int intBias = (1 << intBiasShift);
    protected static final int betaShift = 10;
    protected static final int gammaShift = 10; /* gamma = 1024 */
    protected static final int beta = (intBias >> betaShift); /* beta = 1/1024 */
    protected static final int betaGamma =
            (intBias << (gammaShift - betaShift));
    protected static final int gamma = (1 << gammaShift);
    /* defs for decreasing radius factor */
    protected static final int initRad = (netSize >> 3); /* for 256 cols, radius starts */
    protected static final int radiusBiasShift = 6; /* at 32.0 biased by 6 bits */
    protected static final int radiusBias = (1 << radiusBiasShift);
    protected static final int initRadius = (initRad * radiusBias); /* and decreases by a */
    protected static final int radiusDec = 30; /* factor of 1/30 each cycle */

    /* defs for decreasing alpha factor */
    protected static final int alphaBiasShift = 10; /* alpha starts at 1.0 */
    protected static final int initAlpha = (1 << alphaBiasShift);
    /* radbias and alpharadbias used for radpower calculation */
    protected static final int radBiasShift = 8;
    protected static final int radBias = (1 << radBiasShift);
    protected static final int alphaRadShift = (alphaBiasShift + radBiasShift);
    protected static final int alphaRadBias = (1 << alphaRadShift);
    protected int alphaDec; /* biased by 10 bits */

    /* Types and Global Variables
    -------------------------- */
    protected byte[] thePicture; /* the input image itself */
    protected int lengthCount; /* lengthcount = H*W*3 */

    protected int sampleFac; /* sampling factor 1..30 */

    //   typedef int pixel[4];                /* BGRc */
    protected int[][] network; /* the network itself - [netsize][4] */

    protected int[] netInDex = new int[256];
    /* for network lookup - really 256 */

    protected int[] bias = new int[netSize];
    /* bias and freq arrays for learning */
    protected int[] freq = new int[netSize];
    protected int[] radPower = new int[initRad];
    /* radpower for precomputation */

    /* Initialise network in range (0,0,0) to (255,255,255) and set parameters
       ----------------------------------------------------------------------- */
    public NeuQuant(byte[] thePic, int len, int sample) {

        int i;
        int[] p;

        thePicture = thePic;
        lengthCount = len;
        sampleFac = sample;

        network = new int[netSize][];
        for (i = 0; i < netSize; i++) {
            network[i] = new int[4];
            p = network[i];
            p[0] = p[1] = p[2] = (i << (netBiasShift + 8)) / netSize;
            freq[i] = intBias / netSize; /* 1/netsize */
            bias[i] = 0;
        }
    }

    public byte[] colorMap() {
        byte[] map = new byte[3 * netSize];
        int[] index = new int[netSize];
        for (int i = 0; i < netSize; i++) {
            index[network[i][3]] = i;
        }
        int k = 0;
        for (int i = 0; i < netSize; i++) {
            int j = index[i];
            map[k++] = (byte) (network[j][0]);
            map[k++] = (byte) (network[j][1]);
            map[k++] = (byte) (network[j][2]);
        }
        return map;
    }

    /* Insertion sort of network and building of netindex[0..255] (to do after unbias)
       ------------------------------------------------------------------------------- */
    public void inxBuild() {

        int i, j, smallpos, smallval;
        int[] p;
        int[] q;
        int previouscol, startpos;

        previouscol = 0;
        startpos = 0;
        for (i = 0; i < netSize; i++) {
            p = network[i];
            smallpos = i;
            smallval = p[1]; /* index on g */
            /* find smallest in i..netsize-1 */
            for (j = i + 1; j < netSize; j++) {
                q = network[j];
                if (q[1] < smallval) { /* index on g */
                    smallpos = j;
                    smallval = q[1]; /* index on g */
                }
            }
            q = network[smallpos];
            /* swap p (i) and q (smallpos) entries */
            if (i != smallpos) {
                j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            /* smallval entry is now in position i */
            if (smallval != previouscol) {
                netInDex[previouscol] = (startpos + i) >> 1;
                for (j = previouscol + 1; j < smallval; j++) {
                    netInDex[j] = i;
                }
                previouscol = smallval;
                startpos = i;
            }
        }
        netInDex[previouscol] = (startpos + maxNetPos) >> 1;
        for (j = previouscol + 1; j < 256; j++) {
            netInDex[j] = maxNetPos; /* really 256 */
        }
    }

    /* Main Learning Loop
       ------------------ */
    public void learn() {

        int i, j, b, g, r;
        int radius, rad, alpha, step, delta, samplepixels;
        byte[] p;
        int pix, lim;

        if (lengthCount < minPictureBytes) {
            sampleFac = 1;
        }
        alphaDec = 30 + ((sampleFac - 1) / 3);
        p = thePicture;
        pix = 0;
        lim = lengthCount;
        samplepixels = lengthCount / (3 * sampleFac);
        delta = samplepixels / nCycles;
        alpha = initAlpha;
        radius = initRadius;

        rad = radius >> radiusBiasShift;
        for (i = 0; i < rad; i++) {
            radPower[i] =
                    alpha * (((rad * rad - i * i) * radBias) / (rad * rad));
        }

        //fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);

        if (lengthCount < minPictureBytes) {
            step = 3;
        } else if ((lengthCount % prime1) != 0) {
            step = 3 * prime1;
        } else {
            if ((lengthCount % prime2) != 0) {
                step = 3 * prime2;
            } else {
                if ((lengthCount % prime3) != 0) {
                    step = 3 * prime3;
                } else {
                    step = 3 * prime4;
                }
            }
        }

        i = 0;
        while (i < samplepixels) {
            b = (p[pix] & 0xff) << netBiasShift;
            g = (p[pix + 1] & 0xff) << netBiasShift;
            r = (p[pix + 2] & 0xff) << netBiasShift;
            j = contest(b, g, r);

            alterSingle(alpha, j, b, g, r);
            if (rad != 0) {
                alterNeigh(rad, j, b, g, r); /* alter neighbours */
            }

            pix += step;
            if (pix >= lim) {
                pix -= lengthCount;
            }

            i++;
            if (delta == 0) {
                delta = 1;
            }
            if (i % delta == 0) {
                alpha -= alpha / alphaDec;
                radius -= radius / radiusDec;
                rad = radius >> radiusBiasShift;
                if (rad <= 1) {
                    rad = 0;
                }
                for (j = 0; j < rad; j++) {
                    radPower[j] =
                            alpha * (((rad * rad - j * j) * radBias) / (rad * rad));
                }
            }
        }
        //fprintf(stderr,"finished 1D learning: final alpha=%f !\n",((float)alpha)/initalpha);
    }

    /* Search for BGR values 0..255 (after net is unbiased) and return colour index
       ---------------------------------------------------------------------------- */
    @SuppressWarnings("DuplicatedCode")
    public int map(int b, int g, int r) {

        int i, j, dist, a, bestd;
        int[] p;
        int best;

        bestd = 1000; /* biggest possible dist is 256*3 */
        best = -1;
        i = netInDex[g]; /* index on g */
        j = i - 1; /* start at netindex[g] and work outwards */

        while ((i < netSize) || (j >= 0)) {
            if (i < netSize) {
                p = network[i];
                dist = p[1] - g; /* inx key */
                if (dist >= bestd) {
                    i = netSize; /* stop iter */
                } else {
                    i++;
                    if (dist < 0) {
                        dist = -dist;
                    }
                    a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestd) {
                    j = -1; /* stop iter */
                } else {
                    j--;
                    if (dist < 0) {
                        dist = -dist;
                    }
                    a = p[0] - b;
                    if (a < 0) {
                        a = -a;
                    }
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0) {
                            a = -a;
                        }
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }
        return best;
    }

    public byte[] process() {
        learn();
        unBiasNet();
        inxBuild();
        return colorMap();
    }

    /* Unbias network to give byte values 0..255 and record position i to prepare for sort
       ----------------------------------------------------------------------------------- */
    public void unBiasNet() {
        for (int i = 0; i < netSize; i++) {
            network[i][0] >>= netBiasShift;
            network[i][1] >>= netBiasShift;
            network[i][2] >>= netBiasShift;
            network[i][3] = i; /* record colour no */
        }
    }

    /* Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in radpower[|i-j|]
       --------------------------------------------------------------------------------- */
    protected void alterNeigh(int rad, int i, int b, int g, int r) {

        int j, k, lo, hi, a, m;
        int[] p;

        lo = i - rad;
        if (lo < -1) {
            lo = -1;
        }
        hi = i + rad;
        if (hi > netSize) {
            hi = netSize;
        }

        j = i + 1;
        k = i - 1;
        m = 1;
        while ((j < hi) || (k > lo)) {
            a = radPower[m++];
            if (j < hi) {
                p = network[j++];
                adjustPixelColor(b, g, r, a, p);
            }
            if (k > lo) {
                p = network[k--];
                adjustPixelColor(b, g, r, a, p);
            }
        }
    }

    private void adjustPixelColor(final int b, final int g, final int r, final int a, final int[] p) {
        try {
            p[0] -= (a * (p[0] - b)) / alphaRadBias;
            p[1] -= (a * (p[1] - g)) / alphaRadBias;
            p[2] -= (a * (p[2] - r)) / alphaRadBias;
        } catch (Exception ignored) {
        } // prevents 1.3 miscompilation
    }

    /* Move neuron i towards biased (b,g,r) by factor alpha
       ---------------------------------------------------- */
    protected void alterSingle(int alpha, int i, int b, int g, int r) {

        /* alter hit neuron */
        int[] n = network[i];
        n[0] -= (alpha * (n[0] - b)) / initAlpha;
        n[1] -= (alpha * (n[1] - g)) / initAlpha;
        n[2] -= (alpha * (n[2] - r)) / initAlpha;
    }

    /* Search for biased BGR values
       ---------------------------- */
    protected int contest(int b, int g, int r) {

        /* finds closest neuron (min dist) and updates freq */
        /* finds best neuron (min dist-bias) and returns position */
        /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
        /* bias[i] = gamma*((1/netsize)-freq[i]) */

        int i, dist, a, biasdist, betafreq;
        int bestpos, bestbiaspos, bestd, bestbiasd;
        int[] n;

        bestd = ~(1 << 31);
        bestbiasd = bestd;
        bestpos = -1;
        bestbiaspos = bestpos;

        for (i = 0; i < netSize; i++) {
            n = network[i];
            dist = n[0] - b;
            if (dist < 0) {
                dist = -dist;
            }
            a = n[1] - g;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            a = n[2] - r;
            if (a < 0) {
                a = -a;
            }
            dist += a;
            if (dist < bestd) {
                bestd = dist;
                bestpos = i;
            }
            biasdist = dist - ((bias[i]) >> (intBiasShift - netBiasShift));
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist;
                bestbiaspos = i;
            }
            betafreq = (freq[i] >> betaShift);
            freq[i] -= betafreq;
            bias[i] += (betafreq << gammaShift);
        }
        freq[bestpos] += beta;
        bias[bestpos] -= betaGamma;
        return (bestbiaspos);
    }
}