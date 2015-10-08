/*********************************************

	extraFunctions.h = Functions for various utility.

	Pietro Cavallo, Skin Analytics, 2012

**********************************************/

#ifndef EXTRAF_H
#define EXTRAF_H

#include <iostream>
#include <string>
#include <vector>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

using namespace std;
using namespace cv;

#define ADJUST_FACTOR 0.6*255

static cv::Mat sumRows(cv::Mat inMat);
static vector<double> matrow2vec(cv::Mat inMat);
static cv::Mat centroid(cv::Mat distr);
static void fillHoles(cv::Mat &inout, int blobColour=1);
static cv::Mat getContoursImg(const cv::Mat img, uchar value);
static int fastFindBiggestComponent(cv::Mat &img, float moleCentre[2], int fillValue = 1, bool noBorders = false, int minBorderDist = 5);
static double dist2points(cv::Point p1, cv::Point p2);
static double sqr(double n);
static int sqr(int n);
static unsigned int fastRoot(unsigned int x);
static void resizeLock(cv::Mat img, cv::Mat &dst, int rows, int cols);
static void resizeLock(cv::Mat img, cv::Mat &dst, int biggestSide);
static void resizeLock_(cv::Mat &img, int rows, int cols);
static void resizeLock_(cv::Mat &img, int biggestSide);
static void adjustColour(cv::Mat &img, cv::Size skinWindow = cv::Size(30,30));
static void adjustColour(cv::Mat &img, cv::Mat splitBuffer[], cv::Scalar skinMean);
static void identifySkin(cv::Mat in, cv::Mat &outMask, const int s1 = 135, const int s2 = 200);
static cv::Mat maskMul(cv::Mat img, cv::Mat mask);

/* PI */
static const double pi = 3.14159265358979323846;

static cv::Mat centroid(cv::Mat distr){
/*
	Computes the coordinates of the centroid of the distribution 'distr'
*/
	cv::Mat coord = sumRows(distr) / distr.rows;
	return coord;
}

static cv::Mat sumRows(cv::Mat inMat){
/*
	Sum of all the rows in a 1 channel matrix
*/
	cv::Mat outMat = cv::Mat::zeros(1,inMat.cols,inMat.type());
	for(unsigned int i=0; i<inMat.rows; i++){
		outMat = outMat + inMat.row(i);
	}
	return outMat;
}

// Transform a matrix of 1 row only into a vector
static vector<double> matrow2vec(cv::Mat inMat){
	vector<double> outVec;
	for(int i=0; i<inMat.cols; i++){
		outVec.push_back(inMat.at<double>(cv::Point(i,0)));
	}
	return outVec;
}

// Resize the image mantaining proportions
static void resizeLock(cv::Mat img, cv::Mat &dst, int rows, int cols){
	Mat imgRs;
	if(cols==0){
		imgRs.rows = rows; // fixed number of rows, cols will scale automatically:
		imgRs.cols = cvRound(img.cols * imgRs.rows/img.rows); 
	}else if(rows==0){
		imgRs.cols = cols; // fixed number of cols, rows will scale automatically:
		imgRs.rows = cvRound(img.rows * imgRs.cols/img.cols); 
	} else {
		imgRs.cols = cols;
		imgRs.rows = rows;
	}
	cv::resize(img, imgRs, imgRs.size(),1,1,CV_INTER_AREA); // Name conflict
	imgRs.copyTo(dst);
}
// Resize the image considering the biggest side
static void resizeLock(cv::Mat img, cv::Mat &dst, int biggestSide){
	Mat imgRs;
	if(img.rows>img.cols){ // Height is the biggest
		imgRs.rows = biggestSide; // fixed number of rows, cols will scale automatically:
		imgRs.cols = cvRound(img.cols * imgRs.rows/img.rows); 
	} else { // Width is the biggest
		imgRs.cols = biggestSide; // fixed number of cols, rows will scale automatically:
		imgRs.rows = cvRound(img.rows * imgRs.cols/img.cols); 
	}
	cv::resize(img, imgRs, imgRs.size(),1,1,CV_INTER_AREA); // Name conflict
	imgRs.copyTo(dst);
}

// Fast Resize the image mantaining proportions
static void resizeLock_(cv::Mat &img, int rows, int cols){
	cv::Size newSize;
	if(cols==0){
		newSize.height = rows; // fixed number of rows, cols will scale automatically:
		newSize.width = cvRound(img.cols * newSize.height/img.rows); 
	}else if(rows==0){
		newSize.width = cols; // fixed number of cols, rows will scale automatically:
		newSize.height = cvRound(img.rows * newSize.width/img.cols); 
	} else {
		newSize.width = cols;
		newSize.height = rows;
	}
	cv::resize(img, img, newSize,0, 0, CV_INTER_AREA); // Name conflict
}
// Fast Resize the image considering the biggest side
static void resizeLock_(cv::Mat &img, int biggestSide){
	cv::Size newSize;
	if(img.rows>img.cols){ // Height is the biggest
		newSize.height = biggestSide; // fixed number of rows, cols will scale automatically:
		newSize.width = cvRound(img.cols * newSize.height/img.rows); 
	} else { // Width is the biggest
		newSize.width = biggestSide; // fixed number of cols, rows will scale automatically:
		newSize.height = cvRound(img.rows * newSize.width/img.cols); 
	}
	cv::resize(img, img, newSize, 0, 0, CV_INTER_AREA); // Name conflict
}

/* 
	Closes the holes in a image containing one or more connected components.
	NB: the components do not have to touch the borders!
*/
static void fillHoles(cv::Mat &inout, int blobColour){
	cv::Mat holes;
	inout.copyTo(holes);
	// Get only the holes
	floodFill(holes,cv::Point(0,0),blobColour);
	holes = blobColour - holes;
	// Fill the holes
	inout = inout + holes;
}


/* 
	Get the image containing the contours of a blob
*/
static cv::Mat getContoursImg(const cv::Mat img, uchar value){
	vector<vector<cv::Point> > contoursHierarchy;
	cv::Mat mat = cv::Mat::zeros(img.size(), CV_8UC1);

	cv::findContours(img,contoursHierarchy,CV_RETR_LIST,CV_CHAIN_APPROX_NONE);

	// Go through all the hierarchy and reshape it
	for(unsigned int i=0; i<contoursHierarchy.size(); i++){
		for(unsigned int j=0; j<contoursHierarchy[i].size(); j++){
			mat.ptr<uchar>(contoursHierarchy[i][j].y)[contoursHierarchy[i][j].x]=value;  // equivalent of mat.at<uchar>(contoursHierarchy[i][j])=value;
		}
	}
	return mat;
}


/* Square */
static double sqr(double n){
	return n*n;
}

static int sqr(int n){
	return n*n;
}
/* 15 times faster than the classical float sqrt. 
 Reasonably accurate up to root(32500)
 Source: http://supp.iar.com/FilesPublic/SUPPORT/000419/AN-G-002.pdf
*/
static unsigned int fastRoot(unsigned int x){
    unsigned int a,b;
    b     = x;
    a = x = 0x3f;
    x     = b/x;
    a = x = (x+a)>>1;
    x     = b/x;
    a = x = (x+a)>>1;
    x     = b/x;
    x     = (x+a)>>1;
    return(x);  
}
/*
	Distance between 2 points
*/
static double dist2points(cv::Point p1, cv::Point p2){
	return cv::sqrt((double)(sqr(p1.x - p2.x) + sqr(p1.y - p2.y)));
}


/* Transforms a vector of pixels into a 1 channel image of determined size*/
static cv::Mat pixelsToMat(vector<cv::Point > pixels, cv::Size sz, int value = 255, int padding = 10){
	// Find the max x and y to make the matrix
	cv::Mat mat = cv::Mat::zeros(sz, CV_8UC1);
	for(unsigned int i=0; i<pixels.size(); i++){
		if(pixels[i].x >= 0  &&  pixels[i].y >= 0  &&  pixels[i].x < sz.width  &&  pixels[i].y < sz.height) // avoids outsider
			mat.at<uchar>(pixels[i])=(uchar)value;
	}
	return mat;
}

// TODO: noBorders is not used but it anyways avoid components which touch the borders
static int fastFindBiggestComponent(cv::Mat &img, float moleCentre[2], int fillValue, bool noBorders, int minBorderDist){

	std::vector<cv::Point2i> blob;
	if(fillValue>1) img = (img / fillValue);
	uchar label_count = 2; // starts at 2 because 0,1 are used already

	int nRows = img.rows;
    int nCols = img.cols;

    uchar* p; // Pointer to the img matrix
	for(int y=0; y < img.rows; y++) {
       p = img.ptr<uchar>(y);
		for(int x=0; x < img.cols; x++) {
			if(p[x] != 1) {
				continue;
			}

			cv::Rect rect;
			cv::floodFill(img, cv::Point(x,y), cv::Scalar(label_count), &rect, cv::Scalar(0), cv::Scalar(0), 4);
			vector<cv::Point2i> tmp;
		    uchar* pblob; // Pointer to the img matrix (cropped on the rect that contains the blob)
			for(int i=rect.y; i < (rect.y+rect.height); i++) {
				pblob = img.ptr<uchar>(i);
				for(int j=rect.x; j < (rect.x+rect.width); j++) {
					if(pblob[j] == label_count) {
						// if touches the borders goto label_count++
						// j=x, i=y. P(j,i)
						if (j<=minBorderDist || j >= (img.cols - minBorderDist) || 
							i<=minBorderDist || i >= (img.rows - minBorderDist) )
							goto end_loop;
						tmp.push_back(cv::Point2i(j,i));
					}
				}
			}
			if(tmp.size()>blob.size()){
				blob.clear();
				blob = tmp;
			}
end_loop:
			label_count++;
		}
	}
	if(blob.size()==0) return 1;

	cv::Point centreOfMass;
	centreOfMass.x = 0; centreOfMass.y = 0;

	// Draw the blob and return its centre of mass
	img = cv::Mat::zeros(img.size(),img.type());
	for(int i=0; i<blob.size(); i++){
		img.ptr<uchar>(blob[i].y)[blob[i].x]=fillValue;  //equivalent of: img.at<uchar>(blob[i])=fillValue;
		centreOfMass.x += blob[i].x;
		centreOfMass.y += blob[i].y;
	}
	centreOfMass.x=centreOfMass.x/blob.size();
	centreOfMass.y=centreOfMass.y/blob.size();
	moleCentre[0] = (float)centreOfMass.x /img.cols; // moleCentre[0];
	moleCentre[1] = (float)centreOfMass.y / img.rows; //moleCentre[1];

	return 0;
}
// Version based on skin colour difference
static int fastFindBiggestComponentSkinDiff(cv::Mat bufImages[], float moleCentre[2], int fillValue, bool noBorders, int minBorderDist){
	// bufImages[0] = input: originalImg
	// bufImages[1] = input/output: bw img containing the blobs / it will contain the final blob
	// bufImages[2] = buffer: blobImage (image containing just one blob at time)
	// bufImages[3] = buffer: skinMask (mask of the skin one blob at time)
	cv::Mat *originalImg = bufImages;
	cv::Mat *img = &bufImages[1];
	cv::Mat *blobImage = &bufImages[2];
	cv::Mat *skinMask = &bufImages[3];

	std::vector<cv::Point2i> blob;
	double chosenDiff = 0; // diff of the chosen blob

	if(fillValue>1) *img = (*img / fillValue);
	uchar label_count = 2; // starts at 2 because 0,1 are used already

	int nRows = img->rows;
    int nCols = img->cols;

	cv::Scalar moleMean;
	cv::Scalar skinMean;

    uchar* p; // Pointer to the img matrix
	bool borderTouched = false;
	for(int y=0; y < img->rows; y++) {
       p = img->ptr<uchar>(y);
		for(int x=0; x < img->cols; x++) {
			if(p[x] != 1) {
				continue;
			}
			cv::Point2i centre;
			centre.x = 0; centre.y = 0;
			cv::Rect rect;
			cv::floodFill(*img, cv::Point(x,y), cv::Scalar(label_count), &rect, cv::Scalar(0), cv::Scalar(0), 4);
			vector<cv::Point2i> tmp;
		    uchar* pblob; // Pointer to the img matrix (cropped on the rect that contains the blob)
			for(int i=rect.y; i < (rect.y+rect.height); i++) {
				pblob = img->ptr<uchar>(i);
				for(int j=rect.x; j < (rect.x+rect.width); j++) {
					if(pblob[j] == label_count) {
						// if touches the borders goto label_count++
						// j=x, i=y. P(j,i)
						if (j<=minBorderDist || j >= (img->cols - minBorderDist) || 
							i<=minBorderDist || i >= (img->rows - minBorderDist) ){
							borderTouched = true;
							i = (rect.y+rect.height); // stop loop i
							break; // stop loop j
						}
						tmp.push_back(cv::Point2i(j,i));
						centre.x = centre.x + j;
						centre.y = centre.y + i;
					}
				}
			}
			if(!borderTouched){
				centre.x = cvRound((double)(centre.x) / (double)(tmp.size()));
				centre.y = cvRound((double)(centre.y) / (double)(tmp.size()));
	
				// tmp contains the blob to be assessed
				*blobImage = pixelsToMat(tmp,img->size(),1,0);
				int R = cvRound(sqrt(cv::sum(*blobImage).val[0]));
				R = R*1.5;

				*skinMask = cv::Mat::zeros(originalImg->size(),CV_8UC1);
				cv::circle(*skinMask,cv::Point(centre.x,centre.y),R,cv::Scalar(1),-1);
				cv::dilate(*blobImage,*blobImage,cv::getStructuringElement( MORPH_ELLIPSE , cv::Size(2,2) ));
				*skinMask = *skinMask - *blobImage;
				moleMean = cv::mean(*originalImg,*blobImage);
				skinMean = cv::mean(*originalImg,*skinMask);
				double diff = abs((double)(skinMean.val[1]-moleMean.val[1]));

				if(diff>chosenDiff){
					blob.clear();
					blob = tmp;
					chosenDiff = diff;
				}
			}
			label_count++;
			borderTouched = false;
		}
	}
	if(blob.size()==0) return 1;

	cv::Point centreOfMass;
	centreOfMass.x = 0; centreOfMass.y = 0;

	// Draw the blob and return its centre of mass
	*img = cv::Mat::zeros(img->size(),img->type());
	for(int i=0; i<blob.size(); i++){
		img->ptr<uchar>(blob[i].y)[blob[i].x]=fillValue;  //equivalent of: img.at<uchar>(blob[i])=fillValue;
		centreOfMass.x += blob[i].x;
		centreOfMass.y += blob[i].y;
	}
	centreOfMass.x=centreOfMass.x/blob.size();
	centreOfMass.y=centreOfMass.y/blob.size();
	moleCentre[0] = (float)centreOfMass.x /img->cols; // moleCentre[0];
	moleCentre[1] = (float)centreOfMass.y / img->rows; //moleCentre[1];

	return 0;
}

// Colour Equalization
static void adjustColour(cv::Mat &img, cv::Size skinWindow){
	cv::Mat BGR[4];

	int originalType = img.type(); // We then need double to do the processing, we will reconvert into this type in the end

	// Build the skin image from the external frame
	cv::Mat mask = cv::Mat::ones(img.size(),CV_8UC1);
	cv::Rect insideMask = cv::Rect(cv::Point(skinWindow.width,skinWindow.height),cv::Point(img.cols-skinWindow.width,img.rows-skinWindow.height));
	mask(insideMask) = cv::Mat::zeros(insideMask.size(),CV_8UC1);
	cv::Scalar skinMean3 = cv::mean(img,mask);

	img.convertTo(img,CV_64F);
	cv::split(img,BGR);
	for(int n=0; n<3; n++)
		BGR[n] = BGR[n] / skinMean3.val[n];
	cv::merge(BGR,img.channels(),img);
	img=img*0.7;
	img=img*255;
	img.convertTo(img,originalType);
}

static void adjustColour(cv::Mat &img, cv::Mat splitBuffer[], cv::Scalar skinMean){

	int originalType = img.type(); // We then need double to do the processing, we will reconvert into this type in the end
	img.convertTo(img,CV_64F);

	cv::split(img,splitBuffer);
	for(int n=0; n<3; n++)
		splitBuffer[n] = splitBuffer[n] / skinMean.val[n];
	cv::merge(splitBuffer,img.channels(),img);

	img=img*ADJUST_FACTOR ;//70% of 255

	img.convertTo(img,originalType);

}

// Colour Equalization
static void fastAdjustColour(cv::Mat &img, cv::Mat skinBuffer[], cv::Mat &skinMask, cv::Size skinWindow = cv::Size(30,30)){

	int originalType = img.type(); // We then need double to do the processing, we will reconvert into this type in the end

	// Build the skin image from the external frame
	skinMask = cv::Mat::ones(img.size(),CV_8UC1);
	cv::Rect insideMask = cv::Rect(cv::Point(skinWindow.width,skinWindow.height),cv::Point(img.cols-skinWindow.width,img.rows-skinWindow.height));
	skinMask(insideMask) = cv::Mat::zeros(insideMask.size(),CV_8UC1);
	cv::Scalar skinMean3 = cv::mean(img,skinMask);

	img.convertTo(img,CV_64F);


	cv::split(img,skinBuffer);
	for(int n=0; n<3; n++)
		skinBuffer[n] = skinBuffer[n] / skinMean3.val[n];
	cv::merge(skinBuffer,img.channels(),img);
	
	
	img=img*ADJUST_FACTOR;
	img.convertTo(img,originalType);
}

// Colour Equalization
static void adjustColour_identifySkin(cv::Mat &img, cv::Size skinWindow = cv::Size(30,30)){
	cv::Mat BGR[3];

	int originalType = img.type(); // We then need double to do the processing, we will reconvert into this type in the end

	// Build the skin image from the external frame
	cv::Mat mask;
	identifySkin(img,mask,120,220);
	cv::Scalar skinMean3 = cv::mean(img,mask);

	img.convertTo(img,CV_64F);
	cv::split(img,BGR);
	for(int n=0; n<3; n++)
		BGR[n] = BGR[n] / skinMean3.val[n];
	cv::merge(BGR,3,img);
	img=img*0.7;
	img=img*255;
	img.convertTo(img,originalType);
}

// Identify what is skin and what is not, returns a mask of the skin
static void identifySkin(cv::Mat in, cv::Mat &outMask, const int s1, const int s2){
	outMask = cv::Mat::ones(in.size(), CV_8UC1);
	for(int i=0; i<in.rows; i++){
		for(int j=0; j<in.cols; j++){
			Vec3b pix = in.at<Vec3b>(i,j);
			if(pix[0]<s1 || pix[0]>s2 || pix[1]<s1 || pix[1]>s2 || pix[2]<s1 || pix[2]>s2){ // Non skin
				pix[0]=0; pix[1]=0; pix[2]=0;
				outMask.at<uchar>(i,j)=0;
			}
		}
	}
}

static double euclidDist(cv::Scalar p1, cv::Scalar p2){
	return sqrt(sqr(p1.val[0]-p2.val[0])+sqr(p1.val[1]-p2.val[1])+sqr(p1.val[2]-p2.val[2]));
}


static cv::Mat maskMul(cv::Mat img, cv::Mat mask){
	vector<cv::Mat> BGR;
	cv::Mat out;

	cv::split(img,BGR);
	BGR[0] = BGR[0].mul(mask);
	BGR[1] = BGR[1].mul(mask);
	BGR[2] = BGR[2].mul(mask);
	cv::merge(BGR,out);
		
	return out;
}

static double mag(cv::Mat &img, cv::Mat buff[], int thr = 0, cv::Mat mask = cv::Mat()){
	cv::Mat *mv; mv = &buff[0];
	cv::Mat *mh; mh = &buff[1];
	cv::Mat *skinMag; skinMag = &buff[2];

	img.copyTo(*mv); img.copyTo(*mh);
	cv::Scharr(img,*mv,-1,1,0);
	cv::Scharr(img,*mh,-1,0,1);
	cvtColor((*mv+*mh),*skinMag,CV_RGB2GRAY);
	//imshow("Skinmag",skinMag*255);

	if(thr>0) // Sum only pixels with a magnitude > thr
	{
		for(int i=0; i<skinMag->rows; i++)
			for(int j=0; j<skinMag->cols; j++){
				if(skinMag->at<uchar>(i,j)<thr)
					skinMag->at<uchar>(i,j) = 0;
			}
	}

	// Filter by mask
	if(mask.size().area()>0)
		*skinMag = skinMag->mul(mask);
	//imshow("skin mag pur",skinMag*255); waitKey();

	return cv::sum(*skinMag).val[0]/(double)img.size().area();

}

static cv::Rect computeFocusBox(const cv::Mat &img, const int RATIO, const bool SQUARED = false){
	int boxHeight, boxWidth;
	if(SQUARED){
		int smallestDim = img.rows < img.cols ? img.rows : img.cols;
		boxHeight = cvRound((double) smallestDim / RATIO);
		boxWidth = cvRound((double) smallestDim / RATIO);
	} else {
		boxHeight = cvRound((double)img.rows / RATIO);
		boxWidth = cvRound((double)img.cols / RATIO);
	}
	return cv::Rect( cvRound((double)img.cols/2 ) - cvRound(boxWidth/2), cvRound((double)img.rows/2) - cvRound(boxHeight/2),boxWidth,boxHeight);
}

#endif