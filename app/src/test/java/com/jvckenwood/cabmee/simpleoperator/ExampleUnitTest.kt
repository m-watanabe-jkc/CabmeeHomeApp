package com.jvckenwood.cabmee.homeapp

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest : ExpectSpec({
    expect("足し算 2+2 の結果は 4 になる") {
        val sum = 2 + 2
        sum.shouldBe(4)
    }

    expect("引き算 4-2 の結果は 2 になる") {
        val sub = 4 - 2
        sub.shouldBe(2)
    }
})
