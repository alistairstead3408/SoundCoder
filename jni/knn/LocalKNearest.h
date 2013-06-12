
/*
 *  localknearest.cpp
 *  
 *
 *  Created by Alistair Graham Stead on 09/03/2011.
 *  Copyright 2011. All rights reserved.
 *
 */


#include <math.h>
#include <stdio.h>
#include <iostream>
#include <time.h>

#include <opencv2/video/background_segm.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc_c.h>
using namespace cv;


class LocalKNearest {

// k Nearest Neighbors

	Mat *storedMat;
	cv::flann::Index *index;
	Mat *labelResponses;
	int K;
	vector<vector<int> > averages; //Hue, Sat, Val
	int hueWeighting;
	
public:
LocalKNearest(Mat* train_data, Mat* responses, int max_k , vector<vector<int> > averageVector);
//sample data will have 2 cols
vector<int> find_nearest(Mat* sampleData);
	vector<vector<int> > getVectorAverages();
	void printSampleAverage(Mat* img);
	int getSourceAverage(Mat* vectors, Mat* responses, int LabelNo, int vectorNo);

};