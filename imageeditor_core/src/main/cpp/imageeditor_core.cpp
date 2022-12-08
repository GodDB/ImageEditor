#include <jni.h>
#include <string>
#include <iostream>
#include <android/log.h>
#include "util/logger/Logger.cpp"

extern "C" JNIEXPORT void JNICALL
Java_com_example_imageeditor_1core_ImageEditorCore_helloWorld(JNIEnv* env,jobject /* this */) {
    log("hello world");
}
