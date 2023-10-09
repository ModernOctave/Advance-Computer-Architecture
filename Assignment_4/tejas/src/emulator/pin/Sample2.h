#include <jni.h>
int test()
{
   JavaVMOption options[1];
   JNIEnv *env;
   JavaVM *jvm;
   JavaVMInitArgs vm_args;
   
   jclass cls;
   jmethodID mid;
   jint square;
   long status;
   //boolean not;

   options[0].optionString = "-Djava.class.path=.";
   memset(&vm_args, 0, sizeof(vm_args));
   vm_args.version = JNI_VERSION_1_6;
   vm_args.nOptions = 1;
   vm_args.options = options;
   vm_args.ignoreUnrecognized=false;
   status = JNI_CreateJavaVM(&jvm, (void**)&env, &vm_args);
   static const char* const ECOClassName = "intMethod";
   static const char* const ECOClassName2 = "Sample2";
   if (status)
   {
     cls = env->FindClass(ECOClassName2);
     if(cls !=0)
     { 
	mid = env->GetStaticMethodID(cls, ECOClassName, "(I)I");
        if(mid !=0)
        { 
             square = env->CallStaticIntMethod(cls, mid, 5);
             printf("Result of intMethod: %d\n", square);
        }

       // mid = env->GetStaticMethodID(cls, "booleanMethod", "(Z)Z");
      //  if(mid !=0)
       // {
       //   not = env->CallStaticBooleanMethod(cls, mid, 1);
       //   printf("Result of booleanMethod: %d\n", not);
       // }
     }

     jvm->DestroyJavaVM();
     return 0;
   }
   else
    return -1;
}
