package com.qwert2603

open class A
open class B : A()
open class C : B()

interface Producer<out T> {
    fun p(): T
}

class PA : Producer<A> { override fun p() = A() }
class PB : Producer<B> { override fun p() = B() }
class PC : Producer<C> { override fun p() = C() }

interface Consumer<in T> {
    fun c(t: T)
}

class CA : Consumer<A> { override fun c(t: A) {} }
class CB : Consumer<B> { override fun c(t: B) {} }
class CC : Consumer<C> { override fun c(t: C) {} }

fun main(args: Array<String>) {
    val ca: Consumer<C> = CA()
    val cb: Consumer<C> = CB()
    val cc: Consumer<C> = CC()

    val pa: Producer<A> = PA()
    val pb: Producer<A> = PB()
    val pc: Producer<C> = PC()
}