/*
 * include the headers required by the generated cpp code
 */
%{
#include "Processor.h"
#include "image_pool.h"
using namespace cv;
%}


/**
 * some constants, see Processor.h
 */

#define DETECT_HOUGH 3

//import the android-cv.i file so that swig is aware of all that has been previous defined
//notice that it is not an include....
%import "android-cv.i"

//make sure to import the image_pool as it is 
//referenced by the Processor java generated
//class
%typemap(javaimports) Processor "
import com.opencv.jni.Mat;
import com.opencv.jni.image_pool;// import the image_pool interface for playing nice with
								 // android-opencv

/** Processor - for processing images that are stored in an image pool
*/"

class Processor {
public:
	Processor();
	virtual ~Processor();
	
	float at(Mat in, int row, int col);
	
	int getLongLength(jlong ptr);
	
	jlong getLongVal(jlong ptr, int index);
	
	float getLineHueValues(int input_idx, image_pool* pool);
	
	long getBlobLabels(int input_idx, image_pool* pool, int satThreshold, int valThreshold);
	
	void filterBackground(int input_idx, image_pool* pool, int satThreshold, int valThreshold);
	
	jlong detectNewBlobs(jlong inputBlobControl, int input_idx, image_pool* pool, int satThreshold, int valThreshold);
	
	jlong getBlobControl(jlong knn);
	
	void deleteObject(jlong ptr);

};
