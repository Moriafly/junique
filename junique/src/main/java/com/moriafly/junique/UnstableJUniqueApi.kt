@file:Suppress("UNUSED")

package com.moriafly.junique

@RequiresOptIn(
    message =
        "This JUnique API is experimental and is likely to change or be removed in the future",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
annotation class UnstableJUniqueApi
