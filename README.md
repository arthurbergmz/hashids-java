# hashids-java
Unofficial version, in Java, of [Hashids](http://hashids.org/) v1.0.2, by Ivan Akimov.


[There's already a Java version of Hashids](https://github.com/jiecao-fm/hashids-java), but my version is faster and lighter. I made some benchmarking with JBenchX, you can see the results below, using a method like this one, running *for(int i = 0; i <= x; i++)*, encoding and decoding *i*:


```
    @Bench
	public void benchmark(@ForEachInt({100,1000,10000,100000}) int x){
		for(int i = 0; i <= x; i++){
			this.decode(this.encode(i));
		}
	}
```

- **_Benchmarks:_**

   - **1. where x = 100:** *(101 elements)*

      ![alt tag](http://i.imgur.com/ngL6oja.png)

   - **2. where x = 1000:** *(1001 elements)*

      ![alt tag](http://i.imgur.com/lWBbtR4.png)

   - **3. where x = 10000:** *(10001 elements)*

      ![alt tag](http://i.imgur.com/0eJ3pgD.png)

   - **4. where x = 100000:** *(100001 elements)*

      ![alt tag](http://i.imgur.com/qBeyQGT.png)
