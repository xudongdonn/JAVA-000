学习笔记
1.串行回收器
    串行回收器是最简单的一个，在单线程，堆空间小的时候性能高，
    这个回收器工作的时候会将所有应用线程全部冻结，
    就这一点而言就使得它完全不可能会被服务端应用所采用。
    如何使用它：你可以打开-XX:+UseSerialGC这个JVM参数来使用它。

2.并行/吞吐量回收器
    并行回收器（ Parallel collector）。
    这是JVM的默认回收器。正如它的名字所说的那样，
    它的最大的优点就是它使用多个线程来扫描及压缩堆。
    它的缺点就是不管执行的是minor GC还是full GC它都会暂停应用线程。
    并行回收器最适合那些可以容许暂停的应用，它试图减少由回收器所引起的CPU开销。

3.CMS回收器
    CMS回收器（concurrent-mark-sweep）。
    这个算法使用了多个线程（concurrent）来扫描堆并标记（mark）那些不再使用的可以回收(sweep)的对象。
    这个算法在两种情况下会进入一个”stop the world”的模式：当进行根对象的初始标记的时候 （老生代中线程入口点或静态变量可达的那些对象）
    以及当这个算法在并发运行的时候应用程序改变了堆的状态使得它不得不回去再次确认自己标记的对象都是正确的。
    使用这个回收器最大的问题就是会碰到promotion failure，
    这是指在回收新生代及年老代时出现了竞争条件的情况。
    如果回收器需要将年轻的对象提升到年老代中，
    而这个时候年老代没有多余的空间了，
    它就只能先进行一次STW(Stop The World)的full GC了——这种情况正是CMS所希望避免的。
    为了确保这种情况不会发生，你要么就是增加老生代的大小（或者增加整个堆的大小），
    要么就是给回收器分配一些后台线程以便与对象分配的速度进行赛跑。
    这个算法的另一个缺点就是和并行回收器相比，
    它使用的CPU资源会更多，它使用了多个线程来执行扫描和回收，这样才能让应用持续提供更高级别的吞吐量。
    对于大多数长期运行的程序而言，应用的暂停对它们是很不利的，这个时候可以考虑使用CMS回收器。
    尽管如此，这个算法也不是默认开启的。你得指定XX:+UseConcMarkSweepGC来启用它。
    假设你的堆小于4G，而你又希望分配更多的CPU资源以避免应用暂停，那么这就是你要选择的回收器。
    然而，如果堆大于4G的话，你可能更希望使用最后的这个——G1回收器。

4.G1回收器
    G1（ Garbage first）回收器在JDK 7update 4中首次引入，它的设计目标是能更好地支持大于4GB的堆。
    G1回收器将堆分为多个区域，大小从1MB到32MB不等，并使用多个后台线程来扫描它们。
    G1回收器会优先扫描那些包含垃圾最多的区域，这正是它的名字的由来（Garbage first）。这个回收器可以通过-XX:UseG1GC标记来启用。
    这一策略减少了后台线程还未扫描完无用对象前堆就已经用光的可能性，而那种情况回收器就必须得暂停应用，这就会导致STW回收。
    G1的另一个好处就是它总是会进行堆的压缩，而CMS回收器只有在full GC的时候才会干这事。