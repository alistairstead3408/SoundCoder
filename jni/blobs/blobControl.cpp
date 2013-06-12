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
#include <time.h>
#include <jni.h>

#include <android/log.h>
#include <sstream>
#define  LOG_TAG    "BlobControlCPP"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


#include <opencv2/video/background_segm.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include "BlobControl.h"


using namespace cv;

BlobControl::BlobControl(LocalKNearest *inputknn)
{
	//Constructor
	knn = inputknn;
}

BlobControl::~BlobControl(){
	//Destructor
}

Blob* BlobControl::getBlob(int index){
	if(blobList.size() > index){
		return blobList[index];
	}
	else
		return NULL;
}


void BlobControl::putBlob(Blob* blob){
	blobList.push_back(blob);
}

vector<Blob*> BlobControl::getBlobList(){
	return blobList;
}

//Requires 8UC1 input
vector<int> BlobControl::getStateChanges(Mat* input){
	
	/*This method detects blobs in the usual way and adds them to
	* the blob list somehow. Probably as embedded vectors
	*
	* I'd imagine hierarchy is only important for making sure
	* two blobs are the same. Just need to do the appearence model check once
	*
	* This method returns an int with new blobs that weren't in the scene
	* before. I.E. not in the vector list. We should keep a history for something
	* like 2 frames in a Mat? Useful if blobs flicker out of view
	*/
	
	CV_Assert(input&&(input->type() == CV_8UC3));
	
	vector<Mat> planes;
	split(*input, planes);
	
	vector<vector<Point> > contours;
	vector<vector<Point> > filteredContours;
    vector<Vec4i> hierarchy;
	
	findContours( planes[2], contours, hierarchy,
				 CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE );
	
	//****  filter contours by area  ******************************************
	for(int i = 0; i < contours.size(); i++)
    {
        Mat mat = Mat(contours[i]);
		double area = contourArea(mat);
		if(area > 1000){ 
			filteredContours.push_back(contours[i]);
		}
    }
	
	//****  Go through each contour ***************************************
	
	int K = filteredContours.size();
	
	vector<Blob*> newBlobs;
	vector<vector<Point> > singleContour;
	for (int i = 0; i < K; i++)
    {
		Mat* maskedImg = new Mat(input->size(), CV_8UC1);
		//initialise to 0
		maskedImg->setTo(0, Mat());

		singleContour.push_back(filteredContours[i]);
		if(singleContour.size() != 1) LOGE("MASSIVE ERROR");
		drawContours(*maskedImg, singleContour, -1, i+1, CV_FILLED);
		
		
		Mat* fullmask = getBlobMask(input, maskedImg);
		
		
		//requires proper 3 channel image
		Mat *sampleList = getNonZeroBlobPixelList(fullmask, 3, 3); // mask, vectors, step
		
	
		//****  Get classification ****************
		vector<int> responses = knn->find_nearest(sampleList);
		
		int largestReponseIndex = 0;
		for(int d = 0; d < responses.size(); d++){
			//LOGE("response %d: %d", d, responses[d]);
			if(responses[d] > responses[largestReponseIndex])
				largestReponseIndex = d;
		}
		
		//LOGE("Classification %d", largestReponseIndex);
		//****  Add to the blob list for comparison ****
		
		Mat matRect = Mat(filteredContours[i]);
		Rect rect = boundingRect(matRect);
		float xc = rect.x + (0.5 * rect.width);
		float yc = rect.y + (0.5 * rect.height);
		
		
		//Each blob needs the contour, the response and the centre
		Blob* blob = new Blob(&filteredContours[i], largestReponseIndex, Point(xc, yc));
		newBlobs.push_back(blob);
		//add something about hierarchy here
		vector<vector<int> > averages = knn->getVectorAverages();
		//drawContours(*input, singleContour, -1, Scalar(averages[largestReponseIndex][0], averages[largestReponseIndex][1], averages[largestReponseIndex][2]), 15);
		rectangle(*input, Point(rect.x, rect.y), Point(rect.x + rect.width, rect.y + rect.height),Scalar(averages[largestReponseIndex][0], averages[largestReponseIndex][1], averages[largestReponseIndex][2]), 2, 8, 0);
		singleContour.pop_back();
		
		//Do loop cleanup
		delete(maskedImg);
		delete(fullmask);
		delete(sampleList);
		
		
    }
	
	
	//****  get parameters of the blobs ***************************************
	
	//This will be done automatically in the constructor of the blob
	
	
	//****check if the blob currently exists (take position into account)****
	vector<int> returnList;
	//This should always be used so that old blobs can be shown the door
	returnList = frameComparison(newBlobs); // <- There is a memory leak somewhere in here

	
	return returnList;

}




/* Computes the probability of a given blob being the same as 
 * the current blob on screan
 */
vector<int> BlobControl::frameComparison(vector<Blob*> newBlobList){
	
	vector<int> returnList;
	//This could be 
	/* Uses a mat to compute probabilities:
	 *
	 *          |   0    |   1   |    N   | <-newBlobList
	 *			 --------------------------
	 *       0  |  P(s)  | P(s)  |  P(s)  |
	 *       1  |  P(s)  | P(s)  |  P(s)  |
	 *		 N	|  P(s)  | P(s)  |  P(s)  |
	 *           --------------------------
	 *       ^blobList
	 */ 
	
	/* Uses following features probabilities:
	 *     Response  Area Distance
	 */
	
	//Everything above is present, below has left
	float threshold = 0.6;
	
	//Create a mat for the probabilities to go in
	Mat candidates = Mat::zeros(blobList.size(), newBlobList.size(), CV_32FC1);
	for(int i = 0; i < blobList.size(); i++){
		for(int j = 0; j < newBlobList.size(); j++){
			candidates.at<float>(i, j) = getSimilarityProbability(blobList[i], newBlobList[j]);
		}
	}
	
	
	printMatrix(&candidates, 1);
	
	//nothing should be removed from the new list!!!!!! That's all good stuff! Tried and tested
	// 1) Compare blobs with previous blob list - matching added as "seen"
	// 2) Find ones that have definitely left - "blob_exit"
	// 3) The blobs left on newBlobList are entering - "blob_entrance"
	
	
	
	vector<int> oldSeenIndexes;
	vector<int> newSeenIndexes;
	//Finds the closest match to the blob we're thinking about
	

	for(int i = 0; i < newBlobList.size(); i++){
		// first is index, second is probability
		int biggest[] = {-1, -1};
		//LOGE("old BlobList Size: %d", blobList.size());
		for(int j = 0; j < blobList.size(); j++){

			if(candidates.at<float>(j, i) > threshold){
				if(candidates.at<float>(j, i) > biggest[1]) {
					biggest[0] = i;
					biggest[1] = candidates.at<float>(j, i);
				}
			}
		}
		if(biggest[1] > -1){ //i.e. if this blob does match one on the original list
			//copy info on the blob over if it matches
			oldSeenIndexes.push_back(i);
			newSeenIndexes.push_back(biggest[0]);
			LOGE("Push");
			blobList[i] = newBlobList[biggest[0]];
			//<BlobID>
			returnList.push_back(blobList[i]->getClassification());
			//<Action>
			returnList.push_back(3); // blob_see
			returnList.push_back(blobList[i]->getArea());
			returnList.push_back(-1); // Seperator
			//remove the value so we don't see it as an entrance later on
		}
		else{
			LOGE("Not See*****");
		}
	}
	
	LOGE("Remove Seen Blobs from new list");
	//*************Remove seen blobs from new list *******
	for(int i = newBlobList.size()-1; i >= 0; i--){
		for(int j = 0; j < newSeenIndexes.size(); j++){
			if(newSeenIndexes[j] == i)
			{
				//Blob *tempBlob;
				//tempBlob = (Blob*) newBlobList[i];
				//delete(tempBlob);
				newBlobList.erase(newBlobList.begin() + i);
			}
		}
	}
	
	
	
	LOGE("remove leaving blobs");
	//remove leaving blobs ******************************
	LOGE("seen: %d unseen: %d", oldSeenIndexes.size(), blobList.size() - oldSeenIndexes.size());
	for(int i = blobList.size()-1; i >= 0; i--){
		int seen = 0; 
		for(int j = 0; j < oldSeenIndexes.size(); j++){
			if(oldSeenIndexes[j] == i)
				seen = 1;
		}
		if(seen == 1){
			
		}
		else{
			//<BlobID>
			returnList.push_back(blobList[i]->getClassification());
			//<Action>
			returnList.push_back(2); // blob_exit
			returnList.push_back(-1); // Seperator
			//Blob *tempBlob;
			//tempBlob = blobList[i];
			//delete(tempBlob);
			blobList.erase(blobList.begin() + i);
			
		}
	}
	
	//everything else stays the same
	
	//add the entering blobs ******************************
	//cout << "newBlobListSize: " << newBlobList.size() << endl;
	
	//Everything left in this vector has to be new, we've removed similar ones
	for(int i = 0; i < newBlobList.size(); i++){
		blobList.push_back(newBlobList[i]);
		//<BlobID>
		returnList.push_back(newBlobList[i]->getClassification());
		//<Action>
		returnList.push_back(1); // blob_enter
		returnList.push_back(newBlobList[i]->getArea());
		returnList.push_back(-1); // Seperator
	}
	
	
	// Do some mat releasing
	candidates.release();
	
	
	return returnList;

}


float BlobControl::getSimilarityProbability(Blob* firstBlob, Blob* secondBlob){
	
	//weightings on each metric
	int R = 10;// Response
	int A = 5; // Area
	int D = 40;// Distance
	
	//classification, area, distance
	vector<float> similarityFeatures;
	
	
	//first check if the vector contains other similarly classified blobs
	//****************************************
	
	if(firstBlob->getClassification() == secondBlob->getClassification()){
		similarityFeatures.push_back(1);
		//cout << "C: " << (1 * R) << "\t";
	}
	else{
		similarityFeatures.push_back(0);
		//cout << "C: 0" << "\t";
	}
	
	
	//the area of the blob is a good indicator
	//****************************************
	float areaDiff = firstBlob->getArea() / secondBlob->getArea();
	
	if(areaDiff > 1) areaDiff = 1 / areaDiff;
	//any difference is negative, not positive
	similarityFeatures.push_back((float) areaDiff);
	//cout << "A: " << (1 * A) << "\t";
	
	
	//the distance between the blobs is a good indicator
	//****************************************
	float width = 480;
	float height = 800;
	float xDiff = (firstBlob->centralPoint.x - secondBlob->centralPoint.x)/width;
	float yDiff = (firstBlob->centralPoint.y - secondBlob->centralPoint.y)/height;
	float distance = sqrt((xDiff * xDiff) + (yDiff * yDiff));
	//any difference is negative, not positive
	distance = 1 - distance; //make distance a negative feature
	//cout <<"D: " <<  (distance * D) << "\t\t";
	similarityFeatures.push_back(distance);
	
	//calculate using the weightings

	float RProb =  similarityFeatures[0] * (float) R;
	float AProb =  similarityFeatures[1] * (float) A;
	float DProb =  similarityFeatures[2] * (float) D;
	
	
	float returnProb =  ((RProb + AProb + DProb) / (R + A + D));
	//cout << "Prob: " << returnProb << endl;
	
	return returnProb;
	
}


Mat* BlobControl::getBlobMask(Mat *img, Mat *mask){
	CV_Assert(img&&(img->size() == mask->size() && img->type()==CV_8UC3 && mask->type()==CV_8UC1));
	//typeCheck(img);
	Mat* newMat = new Mat();
	img->copyTo(*newMat);
	//assumes 8UC3 and 8UC1
	for(int i = 0; i < newMat->rows; i++){
		for(int j = 0; j < newMat->cols; j++){
			if(mask->at<uchar>(i, j) == 0)
			{
			    newMat->at<Vec3b>(i, j)[0] = 0 ;
				newMat->at<Vec3b>(i, j)[1] = 0 ;
				newMat->at<Vec3b>(i, j)[2] = 0 ;
				
			}
		}
	}
	return newMat;
}
Mat* BlobControl::getNonZeroBlobPixelList(Mat* img, int vectors, int step){
	
	CV_Assert(img&&(img->type() == CV_8UC3));
	
	int sizeCols = (int) (img->cols / step);
	int sizeRows = (int) (img->rows / step);
	int listSize = sizeCols * sizeRows;
	
	Mat newMat = Mat(listSize, vectors, CV_8UC1);
	int counter = 0;
	
	for(int i = 0; i < img->rows; i+=step){
		for(int j = 0; j < img->cols; j+=step){
			if((img->at<Vec3b>(i, j)[0] != 0) && (img->at<Vec3b>(i, j)[1] != 0))
			{
				for(int p = 0; p < vectors; p++){
					newMat.at<uchar>(counter, p) = (uchar) (int)  img->at<Vec3b>(i, j)[p];
				}
				counter++;
			}
		}
	}
	
	
	Mat *returnMat = new Mat(counter, vectors, CV_8UC1);
	for(int i = 0; i < counter; i++){
		for(int p = 0; p < vectors; p++){
			returnMat->at<uchar>(i, p) = newMat.at<uchar>(i, p);
		}
	}
	
	return returnMat;
	
}




void BlobControl::typeCheck(Mat *img){
	if(img->type() == CV_8UC1);
		//cout << "8 bit unsigned C1" << endl;	
		else if(img->type() == CV_8SC1);
		//cout << "8 bit signed C1" << endl;
		else if(img->type() == CV_16UC1);
		//cout << "16 bit unsigned C1" << endl;	
		else if(img->type() == CV_16SC1);
		///cout << "16 bit signed C1" << endl;	
		else if(img->type() == CV_32SC1);
		//cout << "32 bit signed C1" << endl;	
		else if(img->type() == CV_32FC1);
		//cout << "32 bit Floating Point C1" << endl;	
		else if(img->type() == CV_64FC1);
		//cout << "64 bit Floating Point C1" << endl;
		else if(img->type() == CV_8UC3);
		//cout << "8 bit unsigned C3" << endl;
		else if(img->type() == CV_8SC3);
		//cout << "8 bit signed C3" << endl;
		else if(img->type() == CV_16UC3);
		//cout << "16 bit unsigned C3" << endl;	
		else if(img->type() == CV_16SC3);
		//cout << "16 bit signed C3" << endl;	
		else if(img->type() == CV_32SC3);
		//cout << "32 bit signed C3" << endl;	
		else if(img->type() == CV_32FC3);
		//cout << "32 bit Floating Point C3" << endl;	
		else if(img->type() == CV_64FC3);
		//cout << "64 bit Floating Point C3" << endl;
		else;
		//cout << "unknown: " << img->depth() << endl;
}
void BlobControl::printMatrix(Mat *img, int step){
	CV_Assert(img&&(img->type() == CV_32FC1 || img->type() == CV_8UC1 || img->type() == CV_64FC1));
	std::stringstream ss;		
	std::string tmp;
	const char* cstr = tmp.c_str();
	for(int i = 0; i < img->rows; i+=step){
		for(int j = 0; j < img->cols; j+=step){
			if(img->type() == CV_8UC1){
				;//ss << (int) img->at<uchar>(i, j) << ", ";
			}
			else if(img->type() == CV_32FC1) {
				;//ss << (float) img->at<float>(i, j) << ", ";
			}
			else if(img->type() == CV_64FC1)
				;//ss << (double) img->at<double>(i, j) << ", ";
			}
		//LOGE("");
		}
								  
	}




