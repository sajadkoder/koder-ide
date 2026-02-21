package com.koder.ide.core.native

object NativeUtils {
    
    init {
        System.loadLibrary("koder_native")
    }

    external fun getNativeVersion(): String
    
    external fun isNativeSupported(): Boolean
}
