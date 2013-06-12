/*
 * Processor.cpp
 *
 *  Originally Created on: Jun 13, 2010 By Ethan
 *  Dramatically changed and extended by Alistair Stead    
 */

#ifndef KNEAREST_H_
#define KNEAREST_H_
#include "./knn/LocalKNearest.h"
#endif /*KNEAREST_H_*/

#ifndef PROCESSOR_H_
#define PROCESSOR_H_

#include <opencv2/core/core.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/ml/ml.hpp>
#include <opencv2/video/blobtrack.hpp>
#include <opencv2/video/tracking.hpp>
#include <vector>

#include "image_pool.h"



#define DETECT_HOUGH 3

class Processor {

	cv::StarFeatureDetector stard;
	cv::FastFeatureDetector fastd;
	cv::SurfFeatureDetector surfd;
        cv::FastFeatureDetector houghd;
	std::vector<cv::KeyPoint> keypoints;

	vector<vector<Point2f> > imagepoints;

	cv::Mat K;
	cv::Mat distortion;
	cv::Size imgsize;
	//image_pool pool;
	
	void showHoughLines(Mat* img);
	void backgroundSubtraction(Mat* img, int hsvReturn, int satThreshold, int valThreshold);
	void newLocalThreshold(Mat *img, int kernal, int satThreshold, int valThreshold);
	void localThresholdPR(Mat *img, int kernal);
	
	int depthCheck(Mat *img);
	Mat* filterMat(Mat *img, float value);
	Mat* getMask(Mat *img, Mat *mask, int replacement);
	LocalKNearest* trainKNearestNeighbour(Mat *Input_3C, Mat *labelImg, vector<vector<Point> > blobs, int vectors, vector<vector<int> > averagesVector);
	Mat* resizeMatrix(Mat *img, int size, int step);
	void drawText(int idx, image_pool* pool, const char* text);
	Mat* getNonZeroBlobPixelList(Mat* img, int vectors, int step);
	Mat* getBlobMask(Mat *img, Mat *mask);
public:

	Processor();
	virtual ~Processor();
	
	float at(Mat in, int row, int col);
	
	int getLongLength(long ptr);
	
	long getLongVal(long ptr, int index);
	
	float getLineHueValues(int input_idx, image_pool* pool);
        
	long getBlobLabels(int input_idx, image_pool* pool, int satThreshold, int valThreshold);
	
	void filterBackground(int input_idx, image_pool* pool, int satThreshold, int valThreshold);

	long detectNewBlobs(long inputBlobControl, int input_idx, image_pool* pool, int satThreshold, int valthreshold);
	
	long getBlobControl(long knn);
	
	void deleteObject(long ptr);
	
	int getContourVectorAverage(Mat* img, vector<Point> input, int vectorNumber);


};

#endif /* PROCESSOR_H_ */
