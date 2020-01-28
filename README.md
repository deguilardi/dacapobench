DaCapo Benchmark with Traces - Docker Version
===================
This project is a dockerized version of DaCapo Benchmark Suite.
All you have the option of using Docker with it.

This benchmark suite is intend as a tool for the research community.
It consists of a set of open source, real world applications with
non-trivial memory loads.


## Guidelines for use

When quoting results in publications, the authors of this suite
strongly request that:

* The exact version of the suite be given (number & name)

* The suite be cited in accordance with the usual standards of acknowledging credit in academic research.

* Please cite the [2006 OOPSLA paper](http://doi.acm.org/10.1145/1167473.1167488)

* All command line options used be reported.  For example, if you explicitly override the number of threads or set the number of iterations, you must report this when you publish results. 

For more information see the [DaCapo Benchmark web page](http://dacapobench.org).


## Building

* Install Docker
* Configure the Doccker image: `docker build -f Dockerfile -t dacapo .`
* Compile DaCapo: `docker container run dacapo /usr/local/benchmarks/ant`
* Iterative mode: `docker run -it dacapo /bin/bash`


## Running
* Simply call the banchmarks: `docker container run dacapo java -jar /usr/local/benchmarks/dacapo[...].jar avrora`


## Customization

It is possible to use callbacks to run code before a benchmark starts, when it stops, and when the run has completed.
To do so, extend the class `Callback` (see the file `harness/src/MyCallback.java` for an example).

To run a benchmark with your callback, run:

    java -jar dacapo.jar -c <callback> <benchmark>

## Source Code Structure

### `harness` (The benchmark harness)

This directory includes all of the source code for the DaCapo harness, which is used to invoke the benchmarks, validate output, etc.

  
### `bms` (The benchmarks)

* `bms/<bm>/src` Source written by the DaCapo group to drive the benchmark `<bm>`
* `bms/<bm>/downloads`  MD5 sums for each of the requisite downloads.  These are used to cache the downloads (avoiding re-downloading on each build)
* `bms/<bm>/data` Directory containing any data used to drive the benchmark
* `bms/<bm>/<bm>.cnf` Configuration file for `<bm>`
* `bms/<bm>/<bm>.patch` Patches against the orginal sources (if any)
* `bms/<bm>/build.xml`  Local build file for <bm>
* `bms/<bm>/build` _Directory where building occurs.  This is only created at build time._
* `bms/<bm>/dist` _Directory where the result of the build goes.  This is only created at build time._


### `libs` (Common code used by one or more benchmarks.)

Each of these directories more or less mirror the `bm` directories.


### Using the agent:
    docker container run dacapo /usr/local/benchmarks/agent/ant -d [-D64bit=true] -Darch=[darwin,win32,linux]
    docker container run dacapo /usr/local/benchmarks/agent/run [avrora]


## License

The DaCapo Benchmark with Traces - Docker Version is a fork of the DaCapo
Benchmark Suite. It has all original functionalities, plus traces output
files and a Docker file to make developers lives less painful. The DaCapo
Benchmark Suite has the same Apache License Version 2.0. Benchmarks are
distributed under their own licenses and the remaining component is also 
distributed under the Apache License, version 2.0.

   Copyright 2019 DaCapo Benchmark with Traces - Docker Version,
   Département d'informatique et de mathématique
   Université du Quebec à Chicoutimi,
   Chicoutimi QC, Canada

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
