[![license](https://img.shields.io/badge/license-apache-brightgreen.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)[![Platform](https://img.shields.io/badge/Platform-%20Android-brightgreen.svg)](https://github.com/michaelxue19/JniString)[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/michaelxue19/JniString/pulls)
<a name="readme-top"></a>

<!-- TABLE OF CONTENTS -->
<details>
  <ol>
    <li><a href="#aarch64">aarch64 (moto g)</a></li>
    <li><a href="#armv7">armv7 (Chromecast)</a></li>
    <li><a href="#Conclusion">Conclusion</a></li>
  </ol>
</details>

# High Performance String Transmission between JNI and JVM

Usually we use the jstring to pass a rust string to java:
```rust
env.new_string(rust_string).unwrap().into_inner() 
``` 
when the length of `rust_string` is short, the JVM transmission time is <10ms. It can be ignored in the most cases. However, it becomes outstanding when the 'rust_string' becomes longer and longer.

the Java Direct Byte buffer provides another approach to pass the string yto java but transmission time gets reduced significantly.
Basically the code looks like:

```rust 
let mut buf = generate_string(); 
let mut buf = unsafe { buf.as_bytes_mut() };
env.new_direct_byte_buffer(&mut buf).unwrap().into_inner() 
```   

On the Java/Kotlin side, use the **callback/interface** to receive the JNI ByteBuffer:
```kotlin    
 override fun onApiResult(err: Int, result: ByteBuffer) {   
     val s = StandardCharsets.UTF_8.decode(result).toString()   
     // ....   
} 
```  


## aarch64 (moto g)

| String Length | Time via String | Time via ByteBuffer | 
| :---: | :---: | :---: |  
| 50k | 6ms | 1ms |
| 100k | 12ms | 2ms |
| 500k | 48ms | 7ms |
| 1M | 97ms | 11ms |
| 2M | 192ms | 19ms |  
| 4M | 389ms | 36ms |  
| 8M | 769ms | 69ms |

## armv7 (Chromecast)
| String Length | Time via String | Time via ByteBuffer | 
| :---: | :---: | :---: |  
| 50k | 9ms | 3ms |
| 100k | 16ms | 5ms |
| 500k | 73ms | 12ms |
| 1M | 145ms | 19ms |
| 2M | 294ms | 33ms |  
| 4M | 584ms | 64ms |  
| 8M | 1.16s | 129ms |


<p align="right">(<a href="#readme-top">back to top</a>)</p>


## Conclusion
- On the small/medium scale, the Direct byte buffer can speed up 3x-6x times than the regular string way.
- When the data size is greater than 1M, the transmission may accelerate 10x;
- In an addition, the direct buffer won't participate the GC, it will be good to performance further.

The direct byte buffer in the JVM doesn't copy the data actually if I recall correctly, it just set a pointer to the data buffer. but the pointer may points to the deprecated data section, so rust mark it `unsafe`. In  Java/Kotlin, the direct byte buffer must convert to Java String or other structures ASAP, don't save it for later use.


<p align="right">(<a href="#readme-top">back to top</a>)</p>

