/*
 *  blobControl.h
 *  
 *
 *  Created by Alistair Graham Stead on 09/03/2011.
 *  Copyright 2011 none. All rights reserved.
 *
 */


#include <math.h>
#include <stdio.h>

#include <time.h>

#include <opencv2/video/background_segm.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include "Blob.h"

#ifndef KNEAREST_H_
#define KNEAREST_H_
#include "./knn/LocalKNearest.h"
#endif /*KNEAREST_H_*/


using namespace cv;

class BlobControl {


	vector<Blob*> blobList;
	LocalKNearest* knn;
	Mat* getBlobMask(Mat *img, Mat *mask);
	Mat* getNonZeroBlobPixelList(Mat* img, int vectors, int step);
	void typeCheck(Mat *img);
	float getSimilarityProbability(Blob* blob1, Blob* blob2);
	public:


	BlobControl(LocalKNearest* inputknn);
	virtual ~BlobControl();
	Blob* getBlob(int index);
	void putBlob(Blob* blob);
	vector<int> getStateChanges(Mat* input);
	vector<Blob*> getBlobList();
	vector<int> frameComparison(vector<Blob*> newBlobList);
	void printMatrix(Mat *img, int step);
	void drawBoundingBoxes(Mat *input);
	Mat* resizeMatrix(Mat *img, int size);

};