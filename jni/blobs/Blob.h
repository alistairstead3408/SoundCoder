/*
 *  blobControl.h
 *  
 *
 *  Created by Alistair Graham Stead on 09/03/2011.
 *  Copyright 2011 none. All rights reserved.
 *
 */

#include <opencv2/core/core.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>
#include <opencv2/ml/ml.hpp>
#include <vector>
using namespace cv;

class Blob {


	int width;
	int height;
	double area;
	int classification;
	vector<Point> *contour;
	
	int lifetime; //frames in scene
	int active; //frames actually there
	int inactive; //frames not there
	

	public:
	Point centralPoint;

	Blob(vector<Point>* inputContour, int response, Point center);
	virtual ~Blob();
	Rect getBoundingRect();
	int getClassification();
	double getArea();
	Point getCentre();
	

};