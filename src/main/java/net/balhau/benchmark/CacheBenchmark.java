/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.balhau.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class CacheBenchmark {
    private static int MAX_ITERATIONS = 1000*1000*500;
    private static int PRIME_NUMBER = 7919;
    private static int L1_CACHE_SIZE=1024*32;
    private static int L2_CACHE_SIZE=1024*256;
    private static int L3_CACHE_SIZE=1024*4096;


    private void populateCharArray(char[] letters){
        for(int i=0;i<letters.length;i++){
            letters[i]=(char)((i*PRIME_NUMBER)%256);
        }
    }


    private void shuffleLetters(char[] letters,int iterations){
        populateCharArray(letters);
        int p1,p2;
        for(int i=0;i<iterations;i++){
            p1=(PRIME_NUMBER*i)%letters.length;
            p2=(PRIME_NUMBER*(i+1))%letters.length;
        }
    }

    @Benchmark
    public void l1CacheSizeToying() {
        char[] letters = new char[(int)Math.ceil(L1_CACHE_SIZE/2)];
        shuffleLetters(letters,MAX_ITERATIONS);
    }

    @Benchmark
    public void l2CacheSizeToying() {
        char[] letters = new char[(int)Math.ceil(L2_CACHE_SIZE/2)];
        shuffleLetters(letters,MAX_ITERATIONS);
    }

    @Benchmark
    public void l3CacheSizeToying() {
        char[] letters = new char[(int)Math.ceil(L3_CACHE_SIZE/2)];
        shuffleLetters(letters,MAX_ITERATIONS);
    }

}
