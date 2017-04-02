/**
 * This Benchmark aims to analyse the memory access latency on a NUMA arquitecture
 */

package net.balhau.benchmark;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * Benchmark over L1,L2,L3 and main memory
 */
public class CacheBenchmark {
    private static int MAX_ITERATIONS = 1000*100;           //MAX ITERATIONS
    private static int PRIME_NUMBER = 7919;                 //PRIME_NUMBER for shuffling
    private static int L1_CACHE_SIZE=1024*32;               //32 Kilobytes of data
    private static int L2_CACHE_SIZE=1024*256;              //256 Kilobytes of data
    private static int L3_CACHE_SIZE=1024*4096;             //4 Megabytes of data
    private static int RAM_SEGMENT = 1024*1024*10;          //10 Megabytes of data
    private static int BUFFER_PADDING = 1024;

    private static byte[] LETTERS_L1 = populateByteArray((int)Math.ceil(L1_CACHE_SIZE)-BUFFER_PADDING);
    private static byte[] LETTERS_L2 = populateByteArray((int)Math.ceil(L2_CACHE_SIZE)-BUFFER_PADDING);
    private static byte[] LETTERS_L3 = populateByteArray((int)Math.ceil(L3_CACHE_SIZE)-BUFFER_PADDING);
    private static byte[] LETTERS_RAM_SEGMENT = populateByteArray((int)Math.ceil(RAM_SEGMENT)-BUFFER_PADDING);


    private static byte[] populateByteArray(int size){
        byte[] letters = new byte[size];
        for(int i=0;i<letters.length;i++){
            letters[i]=(byte)((i*PRIME_NUMBER)%256);
        }
        return letters;
    }

    private void shuffleLetters(byte[] letters,int iterations){
        int p1,p2;
        byte aux;
        for(int i=0;i<iterations;i++){
            p1=(PRIME_NUMBER*i)%letters.length;
            p2=(PRIME_NUMBER*(i+1))%letters.length;

            aux=letters[p1];
            letters[p1]=letters[p2];
            letters[p2]=aux;
        }
    }


    private void shuffleLettersWithSpace(byte[] letters,int iterations,int space){
        int p1,p2;
        byte aux;
        int offset1 = space == 0 ? L1_CACHE_SIZE : space;
        int offset2 = space == 0 ? 0 : space;
        for(int i=0;i<iterations;i++){
            p1=(PRIME_NUMBER*i)%offset1;
            p2=space+(PRIME_NUMBER*(i+1))%(letters.length-offset2);
            aux=letters[p1];
            letters[p1]=letters[p2];
            letters[p2]=aux;
        }
    }

    private void swapEnds(byte[] letters,int iterations){
        int p1,p2;
        byte aux;
        for(double i=0;i<iterations;i++){
            p1=0;
            p2=letters.length-1;
            aux=letters[p1];
            letters[p1]=letters[p2];
            letters[p2]=aux;
        }
    }

    @Benchmark
    public void dummyRandomL1CacheSize() {
        shuffleLetters(LETTERS_L1,MAX_ITERATIONS);
    }

    @Benchmark
    public void dummyRandomL2CacheSize() {
        shuffleLetters(LETTERS_L2,MAX_ITERATIONS);
    }

    @Benchmark
    public void dummyRandomL3CacheSize() {
        shuffleLetters(LETTERS_L3,MAX_ITERATIONS);
    }

    @Benchmark
    public void dummyRandomRamCacheSize() {
        shuffleLetters(LETTERS_RAM_SEGMENT,MAX_ITERATIONS);
    }

    @Benchmark
    public void randomL1CacheSize() {
        shuffleLettersWithSpace(LETTERS_L1,MAX_ITERATIONS,1);
    }

    @Benchmark
    public void randomL2CacheSize() {
        shuffleLettersWithSpace(LETTERS_L2,MAX_ITERATIONS,L1_CACHE_SIZE);
    }

    @Benchmark
    public void randomL3CacheSize() {
        shuffleLettersWithSpace(LETTERS_L3,MAX_ITERATIONS,L2_CACHE_SIZE);
    }

    @Benchmark
    public void randomRamCacheSize() {
        shuffleLettersWithSpace(LETTERS_RAM_SEGMENT,MAX_ITERATIONS,L3_CACHE_SIZE);
    }


    @Benchmark
    public void endsL1CacheSize() {
        swapEnds(LETTERS_L1,MAX_ITERATIONS);
    }

    @Benchmark
    public void endsL2CacheSize() {
        swapEnds(LETTERS_L2,MAX_ITERATIONS);
    }

    @Benchmark
    public void endsL3CacheSize() {
        swapEnds(LETTERS_L3,MAX_ITERATIONS);
    }

    @Benchmark
    public void endsRamCacheSize() {
        swapEnds(LETTERS_RAM_SEGMENT,MAX_ITERATIONS);
    }

}
