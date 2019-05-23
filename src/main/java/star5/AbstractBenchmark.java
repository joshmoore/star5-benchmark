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

package star5;

import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.type.numeric.integer.ByteType;
import org.openjdk.jmh.annotations.*;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@State(Scope.Benchmark)
public abstract class AbstractBenchmark {

    public File dir;

    public String prefix = UUID.randomUUID().toString().replaceAll("-", "");

    public ArrayImg<ByteType, ByteArray> rai;

    // Note: trial = fork
    @Setup(Level.Trial)
    public void createTemp() {
        dir = Files.createTempDir();
        rai = createData();
    }

    @TearDown(Level.Trial)
    public void deleteTempDir() throws IOException {
        FileUtils.deleteDirectory(dir);
    }

    public abstract StorageDescriptor createDescriptor(String base);

    public abstract ArrayImg<ByteType, ByteArray> createData();

    @Benchmark
    public void randomWrite() throws Exception {
        String base = dir.getAbsolutePath() + "/" + prefix;
        StorageDescriptor sd = createDescriptor(base);
        sd.saveRAI(rai);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MultiFileHDF5Benchmark.class.getSimpleName())
                .include(N5Benchmark.class.getSimpleName())
                .warmupIterations(1)
                .measurementIterations(1)
                .forks(1)
                .shouldDoGC(true)
                .resultFormat(ResultFormatType.JSON)
                .mode(Mode.SampleTime)
                .build();


        new Runner(opt).run();
    }
}
