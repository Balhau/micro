Today post is all about tinkering with memory. This experiments were inspired by a investigation of a [friend/coworker/geek_soldier](https://www.linkedin.com/in/luismiguelsilva/) regarding memory operations. His investigation is broader and the examples will be, certainly more complex. Here we will tackle the simpler of the memory alignment problem. Crossing boundaries between [L1,L2 and L3 caches](https://en.wikipedia.org/wiki/Cache_memory). Most modern CPU architectures will have some form of [NUMA](https://en.wikipedia.org/wiki/Non-uniform_memory_access) architecture. NUMA is a complex memory layout that tries to exploit the [phenomenon of data locality](https://en.wikipedia.org/wiki/Locality_of_reference). In short L1,L2,L3 are three levels of memory
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


The all purpose of today experiment is to answer a very simple question. *How does these memories impact performance?* We know that in terms of speed the following relationship holds L1<L2<L3<RAM regarding latency which is the same as memory access time. We just don't know how much. For that we can devise a very simple strategy to uncover the secrets behind these shadow concepts. First we create a program to access only L1 data, and benchmark those operations. Then we create a program that will access both L1 and L2, we benchmark it and compare the performance results. We repeat this process for all the 4 kinds of memory. If they have different latency properties then this will be revealed in this experiment. 
