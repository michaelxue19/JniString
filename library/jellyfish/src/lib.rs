use jni::{
    objects::{JClass, JObject, JString, JValue},
    sys::{jboolean, jbyteArray, jint, jobject, jstring, JNI_OK},
    JNIEnv, JavaVM,
};
use std::os::raw::c_void;
use std::time::{Duration, Instant};

extern crate android_logger;
use android_logger::Config;
use jni::sys::JNI_VERSION_1_6;

#[macro_use]
extern crate log;

#[no_mangle]
pub extern "system" fn JNI_OnLoad(vm: JavaVM, _: *mut c_void) -> jint {
    //let env = vm.get_env().expect("Cannot get reference to the JNIEnv");
    android_logger::init_once(
        Config::default()
            .with_tag("MemBench")
            .with_min_level(log::Level::Debug),
    );
    JNI_VERSION_1_6
}

#[no_mangle]
pub extern "system" fn Java_com_tubitv_native_MemBench_nativeGetResultViaDirect(
    env: JNIEnv,
    _: JClass,
    string_size: jint,
    callback: JObject,
) {
    let start = Instant::now();
    let mut buf = generate_string(string_size);
    let duration = start.elapsed();
    let str_gen_time: i32 = duration.as_millis().try_into().unwrap();
    debug!("generate_string is using: {:?}, str_gen_time={}", duration, str_gen_time);

    let start = Instant::now();
    let mut buf = unsafe { buf.as_bytes_mut() };
    let duration = start.elapsed();
    // debug!(
    //     "string.buf.as_bytes().to_vec() is using: {:?}, len={}",
    //     duration,
    //     buf.len()
    // );

    let _ = env.call_method(
        callback,
        "onApiResult",
        "(ILjava/nio/ByteBuffer;)V",
        &[
            JValue::from(str_gen_time),
            JValue::from(env.new_direct_byte_buffer(&mut buf).unwrap().into_inner()),
        ],
    );
}

#[no_mangle]
pub extern "system" fn Java_com_tubitv_native_MemBench_nativeGetResultViaReturnString(
    env: JNIEnv,
    _: JClass,
    string_size: jint,
    callback: JObject,
) {
    let start = Instant::now();
    let buf = generate_string(string_size);
    let duration = start.elapsed();
    let str_gen_time: i32 = duration.as_millis().try_into().unwrap();
    debug!("generate_string is using: {:?}, str_gen_time={}", duration, str_gen_time);

    let _ = env.call_method(
        callback,
        "onApiStringResult",
        "(ILjava/lang/String;)V",
        &[
            JValue::from(str_gen_time),
            JValue::from(env.new_string(buf).unwrap().into_inner()),
        ],
    );
}

fn generate_string(size: i32) -> String {
    let mut s = String::with_capacity(size as usize);
    let cap = s.capacity();
    //const SAMPLE: [u8; 6] = [b'薛', b'ð', b'ð', b'i', b't', b'v'];
    for _ in 0..cap {
        s.push('a');
    }
    debug!("after push, s len {}", s.len());
    s
}
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
