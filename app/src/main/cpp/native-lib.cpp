#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "KoderNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("Koder native library loaded");
    return JNI_VERSION_1_6;
}

JNIEXPORT jstring JNICALL
Java_com_koder_ide_core_native_NativeUtils_getNativeVersion(
        JNIEnv* env,
        jobject /* this */) {
    return env->NewStringUTF("1.0.0");
}

JNIEXPORT jboolean JNICALL
Java_com_koder_ide_core_native_NativeUtils_isNativeSupported(
        JNIEnv* env,
        jobject /* this */) {
    return JNI_TRUE;
}

}
