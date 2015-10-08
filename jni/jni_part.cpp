#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
#include <android/log.h>


// Custom headers
#include "EdgeModule/edgeDetection.h"
#include "extraFunctions.h"
#include "QualityAssessment/qualityAssessment.h"

using namespace std;
using namespace cv;




extern "C" {


#define APPNAME "SkinUploaderNative"

JNIEXPORT void JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityCamera_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityCamera_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    FastFeatureDetector detector(50);
    detector.detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}


JNIEXPORT void JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityCamera_EdgeDetector(JNIEnv*, jobject, jlong addrRgba);

JNIEXPORT void JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityCamera_EdgeDetector(JNIEnv*, jobject, jlong addrRgba)
{
  Mat& mRgb = *(Mat*)addrRgba;
  cv::Mat bufImgs[7];
  cv::Mat elements[3];
  float moleCentre[2];
  cv::Rect rect;
  cv::Scalar skinMeanG;
  int frameCounter;
  edgeDetection(mRgb,bufImgs,elements,moleCentre,skinMeanG,frameCounter,EDGES_GRAY_ADAPTIVE,rect,false,true); 
}

JNIEXPORT jboolean JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityQualityAssessment_QABlur(JNIEnv*, jobject, jlong addrRgba);

JNIEXPORT jboolean JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityQualityAssessment_QABlur(JNIEnv*, jobject, jlong addrRgba)
{
  Mat& mRgb = *(Mat*)addrRgba;
  if(blurAssessment(mRgb, 0.2)){
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Blurtrue");
     return true;
  }
  else{
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Blurfalse");
    return false;
  }
}

JNIEXPORT jboolean JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityQualityAssessment_QALighting(JNIEnv*, jobject, jlong addrRgba);

JNIEXPORT jboolean JNICALL Java_com_skinanalytics_skinuploader_ui_ActivityQualityAssessment_QALighting(JNIEnv*, jobject, jlong addrRgba)
{
  Mat& mRgb = *(Mat*)addrRgba;
  if(lightingAssessment(mRgb, 0.4, 150)){
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Lightingtrue");
    return true;
  }
  else{
    __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Lightingfalse");
    return false;
  }
}

}
