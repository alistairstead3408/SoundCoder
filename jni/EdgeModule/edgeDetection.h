/*********************************************
 
 edgeDetection.h = Edge detection module
 
 Pietro Cavallo, Skin Analytics, 2012
 
 **********************************************/

// TODO: Make a structure with all the bufImages in order to understand better the names
//		 Or use pointers instead (as in fastfindbigcomponentsskindiff)

#ifndef EDGEDETECTION_H
#define EDGEDETECTION_H

#include <vector>

// Open CV headers
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

// Custom headers
#include "extraFunctions.h"

#define DOWN_SIZE 200		// How small is the resized copy
#define FOCUSBOX_RATIO 3	// The ratio of the focus box in mobile images


#define EDGES_MAHAL -3
#define EDGES_GRAY_OTSU -2
#define EDGES_GRAY_ADAPTIVE -1
#define EDGES_RGB_OTSU 0

// Namespaces
using namespace std;
using namespace cv;

static int edgeDetection(cv::Mat &inputImg, cv::Mat bufImages[], cv::Mat elements[3], float moleCentre[2], cv::Scalar &skinMeanG, int &frameCounter, int method = EDGES_GRAY_ADAPTIVE, cv::Rect focusBox = cv::Rect(), bool drawROI = false, bool drawEdges = true, cv::Size skinBox = cv::Size(40,40), cv::Size smoothSize = cv::Size(5,5));
//static int computeEdgesMahal(cv::Mat &inputImg, cv::Size skinBox = cv::Size(40,40), cv::Size smoothSize = cv::Size(5,5));
static int computeEdgesSimple(cv::Mat bufImages[], cv::Mat elements[3], float moleCentre[2], cv::Scalar &skinMeanG, int &frameCounter, int thr, cv::Size smoothSize = cv::Size(5,5));

/*
 Crop the focus area and compute the edge detection on it
 
 thr:
 EDGES_GRAY_OTSU = Grayscale Otsu
 EDGES_GRAY_ADAPTIVE = Grayscale adaptive,
 EDGES_RGB_OTSU = OTSU on RGB
 any other value > 0 = Fixed threshold on RGB
 
 */
static int edgeDetection(cv::Mat &inputImg, cv::Mat bufImages[], cv::Mat elements[], float moleCentre[2], cv::Scalar &skinMeanG, int &frameCounter, int method, cv::Rect focusBox, bool drawROI, bool drawEdges, cv::Size skinBox, cv::Size smoothSize){
	// bufImages[0] = downgraded copy of the focus box
	// bufImages[1] = image containing the borders
	// bufImages[6] = non normalized image
	
	if(inputImg.size().area()<=0)
		return 1;

	cv::Scalar boxColour = cv::Scalar(255,255,255); // Colour of the focus box frame (it changes according to detection)
	int res;	// Result to return
	
	// If focusBox is not passed compute it as a half size box in the middle of the image
	if(focusBox.area()==0){
		focusBox = computeFocusBox(inputImg,FOCUSBOX_RATIO);
	}
	// Check if skinMeanG is acceptable otherwise dont adjust the colour before the edge detection (adjusting the colour on a wrong skinMeanG could lead to white image and no moles detected for ever
	if(skinMeanG.val[0]<50 || skinMeanG.val[1]<50 || skinMeanG.val[2]<50){
		skinMeanG.val[0]=0;
		skinMeanG.val[1]=0;
		skinMeanG.val[2]=0;
	}

	// Work just in the focusBox
	bufImages[0] = inputImg(focusBox);

	// Non normalized copy
	bufImages[0].copyTo(bufImages[6]);

	// Colour normalization
	if(skinMeanG.val[0]!=0){ // Don't do it the first time otherwise you will always get a black image
		cv::Mat *skinBuffer = bufImages; skinBuffer+=2;
		adjustColour(bufImages[0],skinBuffer,skinMeanG);
	}
	//cv::Mat *skinBuffer = bufImages; skinBuffer+=2; // This is to pass pre-allocated arrays without creating new objects
	//cv::Mat *skinMask = bufImages; skinMask+=6;
	//fastAdjustColour(bufImages[0],skinBuffer,*skinMask);
	
	if(drawEdges) bufImages[0].copyTo(inputImg(focusBox)); // Show the adjusted light just if you want to display it on the phone

 	// On a downgraded copy
	if(DOWN_SIZE>0) resizeLock_(bufImages[0],DOWN_SIZE);
	if(DOWN_SIZE>0) resizeLock_(bufImages[6],DOWN_SIZE);

	// Compute the edges, returns bufImages[1] with the borders in it
	res = computeEdgesSimple(bufImages,elements,moleCentre, skinMeanG, frameCounter, method,smoothSize); // This is a simple Global Threshold with different options: grayscale/RGB, Fixed value/OTSU/adaptive

	// Update the frame counter
	frameCounter++;
	if(frameCounter>4) frameCounter = 0;


	// Adjust the centre according to the real image and not just the focus box
	//real_x% = A + B*x  // 25% + 50% * x%
	float percA[2], percB[2];
	percA[0] = (float)focusBox.x / inputImg.cols; percB[0] = focusBox.width / (float)inputImg.cols;
	percA[1] = (float)focusBox.y / inputImg.rows; percB[1] = focusBox.height / (float)inputImg.rows;
	moleCentre[0] = percA[0]+ percB[0]*moleCentre[0];
	moleCentre[1] = percA[1]+ percB[1]*moleCentre[1];
		
	// Resize the border to the original size 
	if(DOWN_SIZE>0) resizeLock_(bufImages[1],focusBox.height,focusBox.width);
    
	if(res==0 && drawEdges){ // Found a mole?
		// Paste the obtained output image in the middle of inputImg to obtain the real output
		int from_to[] = { 0,0, 0,1, 0,2, 0,3 };
		bufImages[0] = cv::Mat::zeros(bufImages[1].size(),inputImg.type()); // It could be a 4 channels or a 3 channels
		cv::mixChannels(&bufImages[1],1,&bufImages[0],1,from_to,inputImg.channels()); // bufImages[0] now stores the borders in multichannel
		inputImg(focusBox)=inputImg(focusBox)+bufImages[0];
		// Change the colour of the focus box
		boxColour = cv::Scalar(0,0,255);
	}
    	
	//// Zoom
	//int bs = inputImg.rows>inputImg.cols? inputImg.rows : inputImg.cols;
	//inputImg(focusBox).copyTo(inputImg);
	//resizeLock_(inputImg,bs);

	//Draw the centre of the mole (without zoom): 
	//cv::circle(inputImg,cv::Point(moleCentre[0]*inputImg.cols,moleCentre[1]*inputImg.rows),3,Scalar(0,0,255),3);

	// Show the ROI rectangle
	if(drawROI)
		cv::rectangle(inputImg,focusBox,boxColour,3);
	
	return res;
}

/*
 Computes the edge detection
 
 inputImg: already downgraded
 borderImg: will give you the borders. Any intermediate steps will be stored here
 element = cv::getStructuringElement( MORPH_ELLIPSE , Size(5,5) );
 
 */

static int computeEdgesSimple(cv::Mat bufImages[], cv::Mat elements[], float moleCentre[2], cv::Scalar &skinMeanG, int &frameCounter, int thr, cv::Size smoothSize){
	// bufImages[0] is the input image
	// bufImages[1] is the border image
	// bufImages[2] is the green channel which will help figure out if it's a mole or not
	// bufImages[3] is the skin mask
	// bufImages[6] is the non normalized image
	if(thr==-2){ // Grayscale Otsu
		cvtColor(bufImages[0],bufImages[1],CV_BGR2GRAY);
		cv::blur(bufImages[1],bufImages[1],smoothSize);
		cv::normalize(bufImages[1],bufImages[1],0,255,NORM_MINMAX);
		// Otsu threshold
		threshold(bufImages[1],bufImages[1],0,255,THRESH_OTSU);
		bufImages[1] = 255 - bufImages[1]; // needs to be inverted
		bufImages[1] = bufImages[1] / 255;
	}
	else if(thr==-1){ // Grayscale Adaptive
		cv::cvtColor(bufImages[0],bufImages[1],CV_BGR2GRAY);
		cv::blur(bufImages[1],bufImages[1],smoothSize);
		cv::normalize(bufImages[1],bufImages[1],0,255,NORM_MINMAX);
		// Find a good window dimension
		int minDim = bufImages[1].rows>bufImages[1].cols ? bufImages[1].cols : bufImages[1].rows;
		minDim*=0.65; 
		minDim = minDim%2==1 ? minDim : minDim-1; // make it odd
		// Adaptive threshold
		cv::adaptiveThreshold(bufImages[1],bufImages[1],255,ADAPTIVE_THRESH_GAUSSIAN_C ,THRESH_BINARY_INV,minDim,0.03);
		bufImages[1]=bufImages[1]/255;
	}
    
	// Remove small unwanted components
	cv::dilate(bufImages[1],bufImages[1],elements[0]);
	cv::erode(bufImages[1],bufImages[1],elements[0]);
	cv::morphologyEx(bufImages[1],bufImages[1],MORPH_OPEN,elements[1]);
    //imshow("after morph",bufImages[1]*255); waitKey();
	// Find the biggest component
	if(fastFindBiggestComponentSkinDiff(bufImages, moleCentre, 1, true, 5)!=0){
		return 1;
	}
    //imshow("after morph",bufImages[1]*255);	waitKey();	

// Fill the holes
	fillHoles(bufImages[1]);

	// Colour control (3 Channels version): if the mole is not much darker than skin it is not right
	const double MOLE_SKIN_DIFF_THR = 20.0;
	cv::Scalar moleMean = cv::mean(bufImages[0],bufImages[1]);
	bufImages[3] = cv::Mat::zeros(bufImages[1].size(),CV_8UC1);
	
	// Radius is proportional to square root of mole area
	int R = cvRound(sqrt(cv::sum(bufImages[1]).val[0]));
	R = R*1.5;

	cv::circle(bufImages[3],cv::Point(moleCentre[0]*bufImages[3].cols,moleCentre[1]*bufImages[3].rows),R,cv::Scalar(1),-1);
	cv::dilate(bufImages[1],bufImages[1],elements[2]);
	bufImages[3] = bufImages[3] - bufImages[1]; 
	// Find the skin mean on the normalized image
	cv::Scalar skinMean = cv::mean(bufImages[0],bufImages[3]);
	// Updates the global skin mean value every x frames with the value of the NON normalized image
	if(frameCounter==0) 
		skinMeanG = cv::mean(bufImages[6],bufImages[3]);
	cv::erode(bufImages[1],bufImages[1],elements[2]); 
	double diff = abs((double)(skinMean.val[1]-moleMean.val[1])); // Diff on the G channel
	//cout<<"Euclidean Diff on 3d:"<<euclidDist(moleMean,skinMean)<<endl;
	//cout<<"Mole mean: "<<moleMean<<"\tSkin mean: "<<skinMean<<endl;
	//cout<<"Diff: "<<diff<<endl;
	//imshow("skin",maskMul(bufImages[0],bufImages[3]));
	if(diff<MOLE_SKIN_DIFF_THR){
		return 1; // It should be without abs but white on black is not picked up anyway
	}
	//	if(euclidDist(moleMean,skinMean)<20) return 1;
	//*/

	// Draw just the silhouette
	bufImages[1]*=100;

	/* 
	// Extract the contours
	bufImages[1] = getContoursImg(bufImages[1],255);
	// Make imgBorders thicker
	cv::dilate(bufImages[1],bufImages[1],elements[2]);
    */

    
	return 0;
}

#endif

