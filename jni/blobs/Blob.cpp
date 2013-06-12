/*
 *  blobControl.cpp
 *  
 *
 *  Created by Alistair Graham Stead on 09/03/2011.
 *  Copyright 2011 none. All rights reserved.
 *
 */


#include <math.h>
#include <stdio.h>
#include <iostream>
#include <time.h>

#include <opencv2/video/background_segm.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include "BlobControl.h"
using namespace cv;

Blob::Blob(vector<Point>* inputContour, int response, Point center)
{
	//Constructor
	contour = inputContour;
	classification = response;
	centralPoint = center; /*relative to the image*/
	
	//calculate area
	Mat mat = Mat(*contour);
	area = contourArea(mat);
}

Blob::~Blob(){
	//Destructor
	delete(contour);
	
	
}

Rect Blob::getBoundingRect(){
	Mat matContour = Mat(*contour);
	Rect r;
	r = boundingRect(matContour);
	return r;
}

int Blob::getClassification(){
	return classification;
}

double Blob::getArea(){
	return area;
}

Point Blob::getCentre(){
	return centralPoint;
}