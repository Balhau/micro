## Benchmarking Memory

## Caches and NUMA
Today post is all about tinkering with memory. This experiments were inspired by a investigation of a [friend/coworker/geek_soldier](https://www.linkedin.com/in/luismiguelsilva/) regarding memory operations. His investigation is broader and the examples will be, certainly more complex. Here we will tackle the simpler of the memory analysis problem. Crossing boundaries between [L1,L2 and L3 caches](https://en.wikipedia.org/wiki/Cache_memory). Most modern CPU architectures will have some form of [NUMA](https://en.wikipedia.org/wiki/Non-uniform_memory_access) architecture. NUMA is a complex memory layout that tries to exploit the [phenomenon of data locality](https://en.wikipedia.org/wiki/Locality_of_reference). In short L1,L2,L3 are three levels of memory.

![Example of NUMA in Cortex-A53](http://shared.balhau.net/imgs/Cortex-A53.png)

L1 is the closest to CPU and L3 the farthest, and then, of course we got main memory. To be able to tinker with all these kinds of memories we need first to identify them on our laptop. For that, assuming you are using a linux distribution, you can use [lscpu](http://manpages.courier-mta.org/htmlman1/lscpu.1.html). For instance on my laptop I got following description

	lscpu
    Architecture:          x86_64
    CPU op-mode(s):        32-bit, 64-bit
    Byte Order:            Little Endian
    CPU(s):                4
    On-line CPU(s) list:   0-3
    Thread(s) per core:    2
    Core(s) per socket:    2
    Socket(s):             1
    NUMA node(s):          1
    Vendor ID:             GenuineIntel
    CPU family:            6
    Model:                 61
    Stepping:              4
    CPU MHz:               2105.593
    BogoMIPS:              5188.06
    Virtualization:        VT-x
    L1d cache:             32K
    L1i cache:             32K
    L2 cache:              256K
    L3 cache:              4096K
    NUMA node0 CPU(s):     0-3


## The experiment

The all purpose of today experiment is to answer a very simple question. *How does these memories impact performance?* We know that in terms of speed the following relationship holds L1<L2<L3<RAM regarding latency which is the same as memory access time. We just don't know how much. For that we can devise a very simple strategy to uncover the secrets behind these shadow concepts. First we create a program to access only L1 data, and benchmark those operations. Then we create a program that will access both L1 and L2, we benchmark it and compare the performance results. We repeat this process for all the 4 kinds of memory. If they have different latency properties then this will be revealed in this experiment.

## Benchmarking with JMH

To benchmark this low level constructs we need the help of a programming language and a benchmarking framework. We could use C and the compiler directives to align memory and loops to iterate over the memory. But for this experiment we don't really need a low level language like C to uncover the profile of these four types of memory. We can and will use java. We just need a framework to help us with the measurement. For this last purpose the choice is the industry standard of [JMH](http://openjdk.java.net/projects/code-tools/jmh/). This tool is a mature and very easy one to tinker with and so is a natural choice for this kind of work. The next step is just the formalization of these ideas.

## Swap my letter

To sum up all these ideas we create a simple *letter swap* algorithm that will pseudo-randomly swap letter in a byte[]. The idea is to create several byte[] structures of this form

  * byte[] living only in L1 cache
  * byte[] crossing boundaries between L1 and L2
  * byte[] crossing boundaries between L1, L2 and L3
  * byte[] crossing boundaries between L1, L2, L3 and main memory

Since the letter swapping is *kind of random* in the case that byte[] will cross a boundary a latency penalty should be noticed. So for that we devise this naive, but hopefully enough, algorithm.

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

We choose a prime number to avoid collisions of positions due rest of division (is just a nice property) that comes directly from number theory more specifically prime number theory. Aside from that this is a pretty simple algorithm to shuffle the letters inside the array. Next we need to define the size for the array. For that we take into account the output we got previously and build the following properties

      private static int MAX_ITERATIONS = 1000*1000*500;      //Max number of iterations
      private static int PRIME_NUMBER = 7919;                 //PRIME_NUMBER for shuffling
      private static int L1_CACHE_SIZE=1024*32;               //32 Kilobytes of data
      private static int L2_CACHE_SIZE=1024*256;              //256 Kilobytes of data
      private static int L3_CACHE_SIZE=1024*4096;             //4 Megabytes of data
      private static int RAM_SEGMENT = 1024*1024*10;          //10 Megabytes of data

      private static byte[] LETTERS_L1 = populateByteArray((int)Math.ceil(L1_CACHE_SIZE/2));
      private static byte[] LETTERS_L2 = populateByteArray((int)Math.ceil(L2_CACHE_SIZE/2));
      private static byte[] LETTERS_L3 = populateByteArray((int)Math.ceil(L3_CACHE_SIZE/2));
      private static byte[] LETTERS_RAM_SEGMENT = populateByteArray((int)Math.ceil(RAM_SEGMENT/2));


And finally we just need to create the four benchmarks previously defined

          @Benchmark
          public void l1CacheSizeToying() {
              shuffleLetters(LETTERS_L1,MAX_ITERATIONS);
          }

          @Benchmark
          public void l2CacheSizeToying() {
              shuffleLetters(LETTERS_L2,MAX_ITERATIONS);
          }

          @Benchmark
          public void l3CacheSizeToying() {
              shuffleLetters(LETTERS_L3,MAX_ITERATIONS);
          }

          @Benchmark
          public void ramCacheSizeToying() {
              shuffleLetters(LETTERS_RAM_SEGMENT,MAX_ITERATIONS);
          }


We also implemented a second swapping method. This second method only swaps the ends of the array. Theoretically this should end up with worst cases than the random algorithm since it will hit the ends of the array. Intuitively this should mean that we would end up swapping two characters belonging to different locations in the memory. Since to read the first char, part of the array should be copied to L1 and then the

Next we just need to compile and run the program. The compilation is a traditional mvn clean install. To run the program we decided to run with a warm-up cycle of 3 iterations and a run of 3 iterations also. This process was repeated 10 times for each benchmark. The result follows

        # Run complete. Total time: 00:09:07

        Benchmark                           Mode  Cnt     Score    Error  Units

        CacheBenchmark.l1CacheSizeRandom   thrpt   30  1343.130 ± 68.243  ops/s
        CacheBenchmark.l2CacheSizeRandom   thrpt   30  1374.820 ± 35.100  ops/s
        CacheBenchmark.l3CacheSizeRandom   thrpt   30  1376.034 ± 34.804  ops/s
        CacheBenchmark.ramCacheSizeRandom  thrpt   30  1377.505 ± 35.628  ops/s

        CacheBenchmark.l1CacheSizeEnds     thrpt   30  7846.218 ± 10.313  ops/s
        CacheBenchmark.l2CacheSizeEnds     thrpt   30  7854.716 ± 10.566  ops/s
        CacheBenchmark.l3CacheSizeEnds     thrpt   30  7848.913 ± 10.017  ops/s
        CacheBenchmark.ramCacheSizeEnds    thrpt   30  7851.176 ± 12.332  ops/s


The results are pretty interesting. The intuitive interpretation turns to be completely wrong and the apparently worst case scenario turns to be the best one. More, it appears that the throughput is independent on the size of the array, and as a consequence independent on the location of the data in the memory. A possible interpretation for this is that the java compiler is clever enough to avoid memory movement since the only data changed in the array is the first and last char. So it is not a fundamental reason to copy data between caches since only two bytes are being updated at each iteration.

After some tweeking we arrive at this **swap function**


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

and executed the tests with the following parametrization

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


With this last changes we were able to break locality between swaps and therefore the results ended up following our initial intuition


			# Run complete. Total time: 00:13:18

			Benchmark                                Mode  Cnt     Score     Error  Units
			CacheBenchmark.dummyRandomL1CacheSize   thrpt   30  2969.995 ±  76.271  ops/s
			CacheBenchmark.dummyRandomL2CacheSize   thrpt   30  2842.415 ±  94.449  ops/s
			CacheBenchmark.dummyRandomL3CacheSize   thrpt   30  1840.950 ± 148.141  ops/s
			CacheBenchmark.dummyRandomRamCacheSize  thrpt   30  2651.015 ±  78.600  ops/s

			CacheBenchmark.endsL1CacheSize          thrpt   30  7886.504 ±  10.746  ops/s
			CacheBenchmark.endsL2CacheSize          thrpt   30  7888.736 ±   9.530  ops/s
			CacheBenchmark.endsL3CacheSize          thrpt   30  7885.065 ±   9.686  ops/s
			CacheBenchmark.endsRamCacheSize         thrpt   30  7882.311 ±   9.807  ops/s

			CacheBenchmark.randomL1CacheSize        thrpt   30  2797.702 ± 179.115  ops/s
			CacheBenchmark.randomL2CacheSize        thrpt   30  1584.330 ±   3.536  ops/s
			CacheBenchmark.randomL3CacheSize        thrpt   30  1087.367 ±  56.219  ops/s
			CacheBenchmark.randomRamCacheSize       thrpt   30   462.359 ±   4.232  ops/s


## Conclusion

This exercise as a very interesting one. The several approaches used to break the locality pattern of data access showed us how complex the memory layout is and how surprising the results could be if we ignore the memory architecture. The lesson here is that we should avoid at all costs random access memory to minimize need to move data between cache layers. The work done here is just an heads up for the need to take into account lower level constrains, such computer architecture. Some people tend to spread the idea that high level languages decouple the programmer from the need to understand under the wood. This is just a simple example to point the other direction. **People that work with higher level languages, like java, should understand lower level concepts to take his programs to a higher level**


**NOTE**:

* Thanks again to [Luis Silva](https://www.linkedin.com/in/luismiguelsilva/) to share and inspire this kindergarten experiment


## Benchmarking Serializers

Recently I wanted microbenchmark different serialization mechanisms. For now we compared the [Gson](https://github.com/google/gson) and [Proto Buffer](https://developers.google.com/protocol-buffers/) implementations. The point here was to have a objective and measurable comparison between these two. For that we created an entity called warrior and we stressed the serialization and deserialization operations.
