//
// Created by beom.93 on 2022/12/08.

#include <jni.h>
#include <string>
#include <android/log.h>

static void log(std::string _msg) {
    const char* msg = _msg.c_str();

    __android_log_print(
            ANDROID_LOG_ERROR,
            "godgod",
            "%s", msg
    );
}
//

