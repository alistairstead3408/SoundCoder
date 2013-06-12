# date: January, 2011 
# author: Alistair Stead
# contact: alistair.stead@gmail.com
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#pass in OPENCV_ROOT or define it here
#OPENCV_ROOT := ../../opencv/

#define OPENCV_INCLUDES
include $(OPENCV_ROOT)/includes.mk
#define OPENCV_LIBS
include $(OPENCV_ROOT)/libs.mk

LOCAL_LDLIBS += $(OPENCV_LIBS) $(ANDROID_OPENCV_LIBS) -llog -lGLESv2
    
LOCAL_C_INCLUDES +=  $(OPENCV_INCLUDES) $(ANDROID_OPENCV_INCLUDES)

LOCAL_MODULE    := cvcamera

LOCAL_SRC_FILES := Processor.cpp ./knn/LocalKNearest.cpp gen/cvcamera_swig.cpp ./blobs/BlobControl.cpp ./blobs/Blob.cpp

include $(BUILD_SHARED_LIBRARY)

