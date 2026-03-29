package dev.deftu.outcome

public fun interface Callback<in T> {
    public operator fun invoke(value: T)
}
