#include <iostream>
#include "jni.h"
#include "PassHashMap.h"
#include <datadog/opentracing.h>
#include "text_map_carrier.h"
#include <unistd.h>
#include <chrono>


using namespace std;



JNIEXPORT int JNICALL Java_PassHashMap_displayHashMap
  (JNIEnv * env, jclass obj, jobjectArray keys, jobjectArray values) {

  /* We need to get array size. There is strong assumption that
     keys and values have the same length
   */
  //auto t1 = std::chrono::high_resolution_clock::now();
  std::unordered_map<std::string, std::string> text_map;	  
  
  int arraySize = env->GetArrayLength (keys);
  
  datadog::opentracing::TracerOptions tracer_options{"localhost", 8126, "cpp"};
  auto tracer = datadog::opentracing::makeTracer(tracer_options);


  /* For all elements in array, we will convert them to C based strings
   */
  for (int i = 0; i < arraySize; i++) {
    /* First, we take key */
    jstring objKey = (jstring)env->GetObjectArrayElement (keys, i);
    const char *c_string_key = env->GetStringUTFChars (objKey, 0);

    /* Then, we take the value value  */
    jstring objValue = (jstring)env->GetObjectArrayElement (values, i);
    const char *c_string_value = env->GetStringUTFChars (objValue, 0);

    /* And we print some info for user */
    printf ("[key, value] = [%s, %s]\n", c_string_key, c_string_value);
    
    text_map[c_string_key]=c_string_value;

    /* Make sure to release stuff */
    env->ReleaseStringUTFChars (objKey, c_string_key);
    env->DeleteLocalRef (objKey);

    env->ReleaseStringUTFChars (objValue, c_string_value);
    env->DeleteLocalRef (objValue);
  }

    TextMapCarrier carrier(text_map);


    // Using span_context based on the carrier generated on the java side

    auto span_context = tracer->Extract(carrier);
    auto span = tracer->StartSpan("nativeCode",
                                  {ChildOf(span_context->get())});
    span->SetTag("tag", "value");

//    auto t2 = std::chrono::high_resolution_clock::now();
  //  auto duration = std::chrono::duration_cast<std::chrono::microseconds>( t2 - t1 ).count();

    //std::cout << duration;

    sleep(2);
    tracer->Close();
    sleep(2);

  return 0;

}
