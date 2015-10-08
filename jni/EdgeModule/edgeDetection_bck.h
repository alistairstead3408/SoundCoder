/*********************************************

	edgeDetection.h = Edge detection module

	Pietro Cavallo, Skin Analytics, 2012

**********************************************/

#ifndef EDGEDETECTION_H
#define EDGEDETECTION_H

// Open CV headers
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

// Custom headers
#include "extraFunctions.h"
#include "BlobFinder.h"

// Namespaces
using namespace std;
using namespace cv;

static int edgeDetection(const cv::Mat inputImg, cv::Mat &outputImg, cv::Size skinBox = cv::Size(40,40), cv::Rect focusBox = cv::Rect());
static int computeEdges(cv::Mat inputImg, cv::Mat &outputImg, cv::Size skinBox = cv::Size(40,40), cv::Size smoothSize = cv::Size(5,5));
static int edgesSimple(cv::Mat inputImg, cv::Mat &outputImg, int thr, cv::Size smoothSize = cv::Size(5,5));
static int edgesPCA(cv::Mat inputImg, cv::Mat &outputImg, int thr = 0, cv::Size smoothSize = cv::Size(5,5));

static int edgeDetection(const cv::Mat inputImg, cv::Mat &outputImg, cv::Size skinBox, cv::Rect focusBox){
	cv::Mat distrSkin; // Skin distribution, computed by analysing four skinBox at the four corners
	cv::Mat croppedImg; // the image cropped on the focusBox
	cv::Mat copyImg;	// copy of the input image
	int res;	// Result to return
	
	inputImg.copyTo(copyImg);
	// If focusBox is not passed compute it as a half size box in the middle of the image
	if(focusBox.area()==0){  
		focusBox = cv::Rect(cvRound(inputImg.size().width/4),cvRound(inputImg.size().height/4),inputImg.size().width/2,inputImg.size().height/2);
	}
	// Work just in the focusBox
	croppedImg = inputImg(focusBox);
	// TODO: Colour conversion YUV->BGR  => can be done inside the computeEdges
//	cv::cvtColor(croppedImg,croppedImg,CV_YUV2BGR);
	// Compute the edges
	//res = computeEdges(croppedImg,outputImg,skinBox,Size(15,15));
	res = edgesSimple(croppedImg,outputImg,-1,Size(5,5)); // This is a simple Global Threshold with different options: grayscale/RGB, Fixed value/OTSU/adaptive
	//res = edgesPCA(croppedImg,outputImg);
	if(res!=0)
		return res;
	// Reconvert into YUV
//	cv::cvtColor(outputImg,outputImg,CV_BGR2YUV);
	// Paste outputImg in the middle of inputImg to obtain the real output
	//outputImg.copyTo(inputImg.colRange(focusBox.x,focusBox.x+focusBox.width).rowRange(focusBox.y,focusBox.y+focusBox.height));
	outputImg.copyTo(copyImg(focusBox));
	copyImg.copyTo(outputImg);
	// TODO: this is just for demo purpose, to be removed for the app
	cv::rectangle(outputImg,focusBox,cv::Scalar(128,128,128),2);
	return res;
}



static int computeEdges(cv::Mat inputImg, cv::Mat &outputImg, cv::Size skinBox, cv::Size smoothSize){
	vector<cv::Rect> skinRects;
	cv::Mat imgCopy;
	cv::Mat distrSkin;
	cv::Mat imgXYZ;
	cv::Mat imgXYR;
	cv::Mat BGR[3]; 
	cv::Mat XYZ[3];
	cv::Mat mask; // the result binary mask of the mole
	cv::Mat c1; // point to be evaluated
	cv::Mat c2; // centroid of the skin distribution
	cv::Mat covar2, mu2, icovar2;
	cv::Mat moleDistImg; // image of distance
	cv::Mat element; // element for morphological operations
	vector<cv::Point> borders; // borders points
	cv::Mat imgBorders; // image of the borders


	// Save a copy to restore it later
	inputImg.copyTo(imgCopy); 
	// Smooth
	cv::blur(inputImg,inputImg,smoothSize);
	// Avoid the image to be smaller than the skin box
	if(inputImg.size().width<skinBox.width || inputImg.size().height<skinBox.width) return 1;
	// Build the four rects
	cv::Rect upLeft = cv::Rect(cv::Point(0,0),skinBox);
	cv::Rect upRight = cv::Rect(cv::Point(inputImg.cols-skinBox.width,0),skinBox);
	cv::Rect downLeft = cv::Rect(cv::Point(0,inputImg.rows-skinBox.height),skinBox);
	cv::Rect downRight = cv::Rect(cv::Point(inputImg.cols-skinBox.width,inputImg.rows-skinBox.height),skinBox);
	skinRects.push_back(upLeft);
	skinRects.push_back(upRight);
	skinRects.push_back(downLeft);
	skinRects.push_back(downRight);

	cv::cvtColor(inputImg,imgXYZ,CV_BGR2XYZ); 
	cv::split(inputImg,BGR);
	cv::split(imgXYZ,XYZ);
	// XYR space
	BGR[2].copyTo(XYZ[2]); 
	cv::merge(XYZ,3,imgXYR); 

	// Build skin distribution
	for(int r=0; r<skinRects.size(); r++){
		cv::Mat XYR[3];
		cv::Mat skinImg = imgXYR(skinRects[r]);
		cv::split(skinImg,XYR);
		for(int i=0; i<skinImg.cols; i++){
			for(int j=0; j<skinImg.rows; j++){
				vector<double> fv;
				fv.push_back(XYR[0].at<uchar>(Point(i,j)));
				fv.push_back(XYR[1].at<uchar>(Point(i,j)));
				fv.push_back(XYR[2].at<uchar>(Point(i,j)));
				cv::Mat tmp(fv);
				tmp = tmp.t();
				distrSkin.push_back(tmp);			
			}
		}
	}

	// Compute mahal distance of every pixel and assign to 1 or 0 with a thresh
	mask = cv::Mat::zeros(inputImg.size(),CV_8UC1); // the result binary mask
	c2 = centroid(distrSkin);
	// Compute the covariance matrix
	cv::calcCovarMatrix(distrSkin, covar2, mu2, CV_COVAR_NORMAL + CV_COVAR_ROWS, -1 );
	covar2 = covar2/(distrSkin.rows-1); // Seems to work in some posts to obtain Cov Matrix	
	invert(covar2, icovar2, DECOMP_SVD);

	moleDistImg = cv::Mat::zeros(inputImg.size(),CV_32FC1);
	double thr = 2;
	for(int i=0; i<inputImg.cols; i++){
		for(int j=0; j<inputImg.rows; j++){
			imgXYR.row(j).col(i).reshape(1,1).copyTo(c1); // XYR point
			c1.convertTo(c1,c2.type());
			moleDistImg.at<float>(Point(i,j))=cv::Mahalanobis(c1,c2,icovar2);
			//mask.at<uchar>(Point(i,j)) = cv::Mahalanobis(c1,c2,icovar2)>thr? 1:0;
			// Euclid: moleDistImg.at<float>(Point(i,j))=euclidDistance(c1,c2);
		}
	}
	// Normalization: the average of moleDistImg has to become 128
	moleDistImg*=(128/cv::mean(moleDistImg).val[0]);
	moleDistImg.convertTo(moleDistImg,CV_8UC1);
	cv::blur(moleDistImg,moleDistImg,cv::Size(5,5));
	cv::threshold(moleDistImg,mask,0,1,THRESH_OTSU);
	// Hair removal (I put it here so if the hairs touch the border I wont get rid of the whole mole)
	element = getStructuringElement( MORPH_ELLIPSE , Size(5,5) );
	cv::dilate(mask,mask,element);
	cv::erode(mask,mask,element);
	cv::morphologyEx(mask,mask,MORPH_OPEN,getStructuringElement( MORPH_ELLIPSE , Size(10,10) ));
	cv::Mat tmpMask;
	if(findComponent(BIGGEST_COMPONENT,mask,tmpMask,1,true)!=0){
		imgCopy.copyTo(outputImg);
		return 1;
	}
	tmpMask.copyTo(mask);

	////////////// POSTPROCESSING //////////////
	cv::dilate(mask,mask,element);
	fillHoles(mask,1);
	cv::erode(mask,mask,element);

	// Restore the original image
	imgCopy.copyTo(inputImg); // Restore the original image

	/// Create the edges and the superimposed images
	// Extract the contours
	contours(mask,borders,mask.size());
	if(borders.size()<10){ // No borders detected, or borders inconsistent (<10 px)
		return 2;
	}
	imgBorders = cv::Mat::zeros(mask.size(), CV_8UC1);
	imgBorders = pixelsToMat(borders,inputImg.size()); // Keep it the same size of img so you can then multiply them
	// Make imgBorders thicker
	cv::dilate(imgBorders,imgBorders,getStructuringElement( MORPH_ELLIPSE , Size(3,3) ));

	// Superimposed edges
	cv::split(inputImg,BGR);
	BGR[0]=BGR[0]+imgBorders;
	BGR[1]=BGR[1]-imgBorders;
	BGR[2]=BGR[2]-imgBorders;
	cv::merge(BGR,3,outputImg);

	return 0;

}


static int edgesSimple(cv::Mat inputImg, cv::Mat &outputImg, int thr, cv::Size smoothSize){
	cv::Mat element;
	cv::Mat imgThr;
	cv::Mat imgGray;
	cv::Mat imgCopy;
	cv::Mat BGR[3];
	vector<cv::Point> borders;
	cv::Mat imgBorders;

	inputImg.copyTo(imgCopy);
	cv::blur(inputImg,inputImg,smoothSize);
	//cvtColor(inputImg,inputImg,CV_BGR2XYZ);

	if(thr==-1){ // Grayscale
		//cv::split(inputImg,BGR);
		cvtColor(inputImg,imgGray,CV_BGR2GRAY);
		//BGR[1].copyTo(imgGray);
		//cv::equalizeHist(imgGray,imgGray);
		cv::normalize(imgGray,imgGray,0,255,NORM_MINMAX);
		//imshow("gray image normalized",imgGray);
		//threshold(imgGray,imgThr,0,255,THRESH_OTSU); imgThr = 255 - imgThr; // needs to be inverted
		adaptiveThreshold(imgGray,imgThr,255,ADAPTIVE_THRESH_GAUSSIAN_C ,THRESH_BINARY_INV,151,0.03); 
		imshow("thr",imgThr);
		imgThr=imgThr/255;
	}
	else{ // RGB
		Mat spl[3]; cv::split(inputImg,spl);
		if(thr!=0){ // Fixed threshold
			threshold(spl[0],spl[0],thr,255,THRESH_BINARY);
			threshold(spl[1],spl[1],thr,255,THRESH_BINARY);
			threshold(spl[2],spl[2],thr,255,THRESH_BINARY);
		} else { // OTSU
			threshold(spl[0],spl[0],0,255,THRESH_OTSU);
			threshold(spl[1],spl[1],0,255,THRESH_OTSU);
			threshold(spl[2],spl[2],0,255,THRESH_OTSU);
		}
		spl[0]=spl[0]/255;
		spl[1]=spl[1]/255;
		spl[2]=spl[2]/255;
		imgThr = spl[0] | spl[1] | spl[2]; // Sum all channels
		imgThr=1-imgThr;
	}

	// Remove small unwanted components
	element = getStructuringElement( MORPH_ELLIPSE , Size(5,5) );
	cv::dilate(imgThr,imgThr,element);
	cv::erode(imgThr,imgThr,element);
	cv::morphologyEx(imgThr,imgThr,MORPH_OPEN,getStructuringElement( MORPH_ELLIPSE , Size(10,10) ));
	// Find the biggest component
	if(findComponent(BIGGEST_COMPONENT,imgThr,imgThr,1,true)!=0){
		imgCopy.copyTo(outputImg);
		return 1;
	}

	imgCopy.copyTo(inputImg); // reset original image

	/// Create the edge, masked and superimposed images
	// Extract the contours
	contours(imgThr,borders,imgThr.size());
	if(borders.size()<10){ // No borders detected, or borders inconsistent (<10 px)
		//cout<<"Error: no borders detected."<<endl;
		return 1;
	}
	imgBorders = cv::Mat::zeros(imgThr.size(), CV_8UC1);
	imgBorders = pixelsToMat(borders,inputImg.size()); // Keep it the same size of img so you can then multiply them
	// Make imgBorders thicker
	cv::dilate(imgBorders,imgBorders,getStructuringElement( MORPH_ELLIPSE , Size(3,3) ));

	// Show superimposed edges
	cv::split(inputImg,BGR);
	BGR[0]=BGR[0]+imgBorders;
	BGR[1]=BGR[1]-imgBorders;
	BGR[2]=BGR[2]-imgBorders;
	cv::merge(BGR,3,outputImg);

	return 0;
}

static int edgesPCA(cv::Mat inputImg, cv::Mat &outputImg, int thr, cv::Size smoothSize){
	cv::Mat element;
	cv::Mat imgThr;
	cv::Mat imgGray;
	cv::Mat imgCopy;
	cv::Mat pcaImg;
	cv::Mat BGR[3];
	cv::Mat pcaPlanes[3];
	vector<cv::Point> borders;
	cv::Mat imgBorders;

	inputImg.copyTo(imgCopy);
	cv::blur(inputImg,inputImg,smoothSize);
	pcaColorSpace(inputImg,pcaImg);
	cv::split(pcaImg,pcaPlanes);
	cv::Mat principalPlane;
	pcaPlanes[0].convertTo(principalPlane,CV_8UC1);
	threshold(principalPlane,imgThr,0,255,THRESH_OTSU);
	imgThr=255-imgThr;
	imshow("pca thresh",imgThr);
	imgThr=imgThr/255;

	// Remove small unwanted components
	element = getStructuringElement( MORPH_ELLIPSE , Size(5,5) );
	cv::dilate(imgThr,imgThr,element);
	cv::erode(imgThr,imgThr,element);
	cv::morphologyEx(imgThr,imgThr,MORPH_OPEN,getStructuringElement( MORPH_ELLIPSE , Size(10,10) ));
	// Find the biggest component
	if(findComponent(BIGGEST_COMPONENT,imgThr,imgThr,1,true)!=0){
		imgCopy.copyTo(outputImg);
		return 1;
	}

	imgCopy.copyTo(inputImg); // reset original image

	/// Create the edge, masked and superimposed images
	// Extract the contours
	contours(imgThr,borders,imgThr.size());
	if(borders.size()<10){ // No borders detected, or borders inconsistent (<10 px)
		//cout<<"Error: no borders detected."<<endl;
		return 1;
	}
	imgBorders = cv::Mat::zeros(imgThr.size(), CV_8UC1);
	imgBorders = pixelsToMat(borders,inputImg.size()); // Keep it the same size of img so you can then multiply them
	// Make imgBorders thicker
	cv::dilate(imgBorders,imgBorders,getStructuringElement( MORPH_ELLIPSE , Size(3,3) ));

	// Show superimposed edges
	cv::split(inputImg,BGR);
	BGR[0]=BGR[0]+imgBorders;
	BGR[1]=BGR[1]-imgBorders;
	BGR[2]=BGR[2]-imgBorders;
	cv::merge(BGR,3,outputImg);

	return 0;
}

#endif
