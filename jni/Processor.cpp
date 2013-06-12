/*
 * Processor.cpp
 *
 *  Originally Created on: Jun 13, 2010 By Ethan
 *  Dramatically changed and extended by Alistair Stead    
 */

#include "Processor.h"
#include <sys/time.h>
#include <sys/stat.h>
#include <sstream>
#include <iomanip>
#include "./blobs/BlobControl.h"

#include <android/log.h>
#define  LOG_TAG    "ProcessorCPP"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


using namespace cv;

Processor::Processor() :
			stard(20/*max_size*/, 8/*response_threshold*/,
				15/*line_threshold_projected*/,
				8/*line_threshold_binarized*/, 5/*suppress_nonmax_size*/),
			fastd(20/*threshold*/, true/*nonmax_suppression*/),
			surfd(100./*hessian_threshold*/, 1/*octaves*/, 2/*octave_layers*/),
			houghd(20/*threshold*/, true/*nonmax_suppression*/)

{

}

Processor::~Processor() {
	// TODO Auto-generated destructor stub
}



float Processor::at(Mat in, int row, int col){
	
	float value = in.at<float>(row, col);
	return value;
	
}


void Processor::deleteObject(long ptr){
	long *tempLong = (long*) ptr;
	delete(tempLong);
}

// returns the number of blobs detected
long Processor::getBlobLabels(int input_idx, image_pool* pool, int satThreshold, int valThreshold){
	
	Ptr<Mat> imgBGR = pool->getImage(input_idx);
	Mat imgHSV = Mat::zeros(imgBGR->size(), CV_8UC3);
	LOGE("Default image size %d x %d", imgHSV.rows, imgHSV.cols);
	imgBGR->copyTo(imgHSV, Mat());
	backgroundSubtraction(&imgHSV, 1, satThreshold, valThreshold);
	
	CV_Assert(imgHSV.type() == CV_8UC3);
	
	vector<Mat> planes; 
	split(imgHSV, planes);
	
	CV_Assert(planes[0].type() == CV_8UC1);

	
	
	vector<vector<Point> > contours;
	vector<vector<Point> > filteredContours;
	vector<vector<Point> > singleContour;
	vector<Vec4i> hierarchy;
	findContours( planes[2], contours, hierarchy,
				 CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE );
	
	//findContours( planes[2], contours,CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE );
	

    for(int i = 0; i < contours.size(); i++)
    {
        Mat mat = Mat(contours[i]);
		double area = contourArea(mat);
		if(area > 5000){ 
			filteredContours.push_back(contours[i]);
		}
    }
	
	
	
	Mat labelImg = Mat::zeros(imgHSV.size(), CV_8UC1);
	for(int i = 0; i < filteredContours.size(); i++){
		singleContour.push_back(filteredContours[i]);
		//i+1 so we can remove background as 0
		drawContours (labelImg, singleContour, -1, i+1, CV_FILLED);
		singleContour.pop_back();
	}
	
	
	int K = filteredContours.size();
	
	vector<vector<int> > averagesVector;
	
	
	
	
	/* Return long array where format is:
	 * 0   length
	 * 1   KNN Ptr
	 * 2   Hue 
	 * 3   Sat  
	 * 4   Val 
	 * ...
	 * n-2 Hue 
	 * n-1 Sat 
	 * n   Val 
	 */
	
	
	//As above, we need to add length and knn pointer
	long *templong = new long[(3*filteredContours.size())+2];
	templong[0] = (long) ((3*filteredContours.size()) + 2);
	
	
	LOGE("FilteredContour Number: %d", filteredContours.size());
	LOGE("ArrayLength: %d", (3*filteredContours.size())+2);
	
	//This is done later instead, to get the averages
	//LocalKNearest *knn = trainKNearestNeighbour(&imgHSV, &labelImg, filteredContours, 3, averagesVector);
	//templong[1] = (long) knn;
	
	cvtColor(imgHSV, *imgBGR, CV_HSV2BGR);
	//used in the loop to store the averages
	vector<int> tempVector;
	vector<vector<Point> > tempContour;
	int counter = 0;
	int tempHSV[3] = {0};
	for(int i = 2; i < templong[0]-2; i+=3){
		
		templong[i] =   getContourVectorAverage(/*img */ imgBGR, filteredContours[(i-2) / 3], 0);
		templong[i+1] = getContourVectorAverage(/*img */ imgBGR, filteredContours[(i-2)/3], 1);
		templong[i+2] = getContourVectorAverage(/*img */ imgBGR, filteredContours[(i-2)/3], 2); 
		tempHSV[0] = getContourVectorAverage(&imgHSV, filteredContours[(i-2) / 3], 0);
		tempHSV[1] = getContourVectorAverage(&imgHSV, filteredContours[(i-2) / 3], 1);
		tempHSV[2] = getContourVectorAverage(&imgHSV, filteredContours[(i-2) / 3], 2);
		tempVector.push_back(tempHSV[0]);
		tempVector.push_back(tempHSV[1]);
		tempVector.push_back(tempHSV[2]);
		averagesVector.push_back(tempVector);
		tempVector.clear();
		tempContour.push_back(filteredContours[(i-2)/3]);
		drawContours (*imgBGR,tempContour , -1, Scalar (templong[i], templong[i+1], templong[i+2]), CV_FILLED);
		tempContour.pop_back();
	}
	

	//getContourVectorAverage is useless for H in HSV! Only good indication of S and V
	/*int tempHSV[3] = {0};
	tempHSV[0] =  getContourVectorAverage(&imgHSV, filteredContours[0], 0); //useless because red
	tempHSV[1] = getContourVectorAverage(&imgHSV, filteredContours[0], 1);
	tempHSV[2] = getContourVectorAverage(&imgHSV, filteredContours[0], 2); 
	LOGE("%d H: %d S: %d V: %d", 0, tempHSV[0], tempHSV[1], tempHSV[2]);
	tempHSV[0] =  getContourVectorAverage(&imgHSV, filteredContours[1], 0); //useless because red
	tempHSV[1] = getContourVectorAverage(&imgHSV, filteredContours[1], 1);
	tempHSV[2] = getContourVectorAverage(&imgHSV, filteredContours[1], 2); 
	LOGE("%d H: %d S: %d V: %d", 1, tempHSV[0], tempHSV[1], tempHSV[2]);*/
	
	
	
	LocalKNearest *knn = trainKNearestNeighbour(&imgHSV, &labelImg, filteredContours, 3, averagesVector);
	
	templong[1] = (long) knn;

	return (long) templong;
	
}

Mat* Processor::getBlobMask(Mat *img, Mat *mask){
	CV_Assert(img&&(img->size() == mask->size()));
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


Mat* Processor::getNonZeroBlobPixelList(Mat* img, int vectors, int step){
	
	CV_Assert(img&&(img->type() == CV_8UC3));
	
	Mat *newMat = new Mat((img->rows * img->cols), vectors, CV_32F);
	int counter = 0;
	
	for(int i = 0; i < img->rows; i+=step){
		for(int j = 0; j < img->cols; j+=step){
			if((img->at<Vec3b>(i, j)[0] != 0) && (img->at<Vec3b>(i, j)[1] != 0))
			{
				for(int p = 0; p < vectors; p++){
					newMat->at<float>(counter, p) = (float) img->at<Vec3b>(i, j)[p];
				}
				counter++;
			}
		}
	}
	
	resizeMatrix(newMat, counter, 1);
	
	return newMat;
	
}

int Processor::getContourVectorAverage(Mat* img, vector<Point> input, int vectorNumber){
	//This is just a temp measure to synthesize having the val
	
	Mat mask = Mat::zeros(img->size(), CV_8UC1);
	vector<vector<Point> > tempContour;
	tempContour.push_back(input);
	drawContours (mask, tempContour, -1, 1, CV_FILLED);
	
	int replacement = 0; //has to be an unsigned int
	Mat* result = getMask(img, &mask, replacement);
	
	
	
	int sum = 0;
	int pixelCount = 0;
	int rejectionCount = 0;
	for(int i = 0; i < result->rows; i++){
		for(int j = 0; j < result->cols; j++){
			if(result->at<Vec3b>(i, j)[vectorNumber] != replacement){ // <- this is the problem
				sum += (unsigned int) result->at<Vec3b>(i, j)[vectorNumber];
				pixelCount++;
			}
			else{
				rejectionCount++;
			}
		}
	}
	 
	
	//cout << sum << ", " << pixelCount << endl;
	//LOGE("Sum: %d pixelCount %d, rejectionCount %d", sum, pixelCount, rejectionCount);
	if(pixelCount > 0)	
		sum /= pixelCount;
	
	return sum;
}

//These two help deal with problems sending arrays
long Processor::getLongVal(long ptr, int index){
	long *tempLong = (long*) ptr;
	if(index < (int) tempLong[0])
	{
		//LOGE("index: %d value: %ld", index, (long) *(tempLong + index));//(long) tempLong[index]);
	
		return (long) *(tempLong + index);	
	}
	else
		return NULL;
}

int Processor::getLongLength(long ptr){
	//Length is always the first element
	long *tempLong = (long*) ptr;
	//KNearest* knn;
	//knn = (KNearest*) tempLong[1];
	int length = tempLong[0];
	return length;
}

LocalKNearest* Processor::trainKNearestNeighbour(Mat *Input_3C, Mat *labelImg, vector<vector<Point> > blobs, int vectors, vector<vector<int> > averagesVector){
	CV_Assert(Input_3C&&(Input_3C->type() == CV_8UC3));
	CV_Assert(labelImg&&(labelImg->type() == CV_8UC1));
	
	CV_Assert(Input_3C&&(Input_3C->type() == CV_8UC3));
	CV_Assert(labelImg&&(labelImg->type() == CV_8UC1));
	if(labelImg->size() != Input_3C->size()){
		LOGE("SIZES DONT MATCH*******");
	}
	CV_Assert(labelImg->size() == Input_3C->size());
	
	
	vector<Mat> planes; 
	split(*Input_3C, planes);
	
	//here is where we'll train the k-nearest-neighbour algorithm
	
	
	int size = planes[0].rows * planes[0].cols;
	
    Mat trainingVectors = Mat::zeros(size, vectors, CV_8UC1); //each input vector on one row, just hue atm
    Mat trainingLabels = Mat::zeros(size, 1, CV_8UC1);
	int rowCounter = 0;
	for(unsigned int y = 0; y < labelImg->rows; y++){
		for(unsigned int x = 0; x < labelImg->cols; x++){
			
			int result = (uchar) labelImg->at<int>(y, x);
			
			if((result > 0)){ 
				trainingLabels.at<uchar>(rowCounter, 0) = result;
				//add image vectors horizontally
				for(int p = 0; p < vectors; p++){
					trainingVectors.at<uchar>(rowCounter, p) = Input_3C->at<Vec3b>(y, x)[p];
					//cout << (int) Input_3C->at<Vec3b>(y, x)[p] << "\t";
				}
				
				//cout << (int) result << endl;
				rowCounter++;
			}
			
		}
	}
	int K = blobs.size();
	//trainingLabels.resize(rowCounter);
	//trainingVectors.resize(rowCounter);
	int idealRowStep = rowCounter / 100;
	Mat *newlabels = resizeMatrix(&trainingLabels, rowCounter, idealRowStep); //rowCounter
	Mat *newvectors = resizeMatrix(&trainingVectors, rowCounter, idealRowStep); //rowCounter
	
	int sum[vectors];
	for(int i = 0; i < vectors; i++)
		sum[i] = 0;
	
	for(int p = 0; p < K; p++){
	for(int i = 0; i < newvectors->rows; i++){
		if(newlabels->at<uchar>(i, 0) == p){
		for(int j = 0; j < vectors; j++)
			sum[j] += newvectors->at<uchar>(i, j);
		}
	}
	
	for(int i = 0; i < vectors; i++){
		sum[i] /= newvectors->rows;
		LOGE("Average for label %d: %d %d",p, sum[0], sum[1]);
	}
	}
	
	

	
	for(int i = 0; i < trainingLabels.rows; i++){
		//cout << "Vector: (" << (int) trainingVectors.at<uchar>(i, 0) << ", " << (int) trainingVectors.at<uchar>(i, 1) << ") Label: " << (int) trainingLabels.at<uchar>(i, 0) << endl;
	}
	
	//KNearest *knn = new KNearest( trainingVectors, trainingLabels);
	
	//LocalKNearest *knn = new LocalKNearest(&trainingVectors, &trainingLabels, K, 0);
	LocalKNearest *knn = new LocalKNearest(newvectors, newlabels, K, averagesVector);
	
	return knn;

	
	
	
	
	
}



Mat* Processor::getMask(Mat *img, Mat *mask, int replacement){
	CV_Assert(img&&(img->size() == img->size()));
	Mat* newMat = new Mat();
	img->copyTo(*newMat);
	int replacementCount = 0;
	//assumes both 8UC3 and 8UC1
	for(int i = 0; i < newMat->rows; i++){
		for(int j = 0; j < newMat->cols; j++){
			if(mask->at<uchar>(i, j) == 0)
			{
				replacementCount++;
			    newMat->at<Vec3b>(i, j)[0] = replacement ;
				newMat->at<Vec3b>(i, j)[1] = replacement ;
				newMat->at<Vec3b>(i, j)[2] = replacement ;
				
			}
		}
	}
	//LOGE("getMask ReplacementCount %d", replacementCount);
	return newMat;
}


/* This is the main beast of the project
 * It's going to the most difficult, challenging part
 *
 * All we want is new blobs (to infer sounds), old blobs need
 * to be tracked so that the sounds are not repeated
 * 
 * the pointer to the blob list will be given and there will
 * be some functions here/blobcontrol that will allow java to
 * query things like area, colour etc. Colour is tracked by ID
 *
 */

long Processor::getBlobControl(long inputknn){
	
	LocalKNearest* knn = (LocalKNearest*) inputknn;
	BlobControl *control = new BlobControl(knn);
	return (long) control;
	
}
long Processor::detectNewBlobs(long inputBlobControl, int input_idx, image_pool* pool, int satThreshold, int valThreshold){
	
	//all that needs to be returned is blobcontrol
	//it'll handle everything from there
	
	BlobControl* blobControl = (BlobControl*) inputBlobControl;
	
	
	//----initialise--------------------------------------------
	
	
	Ptr<Mat> imgBGR = pool->getImage(input_idx);
	Mat imgHSVResized;
	 // This scaling is vitle for speed
	resize(*imgBGR, imgHSVResized, Size(), 0.5, 0.5, INTER_LINEAR);
	
	backgroundSubtraction(&imgHSVResized, 1, satThreshold, valThreshold); //<- returns HSV
	
	//
	CV_Assert(imgBGR->type() == CV_8UC3);
	vector<Mat> planes; 
	split(imgHSVResized, planes);
	CV_Assert(planes[0].type() == CV_8UC1);
	
	//This is the critical Section *********
	vector<int> returnList = blobControl->getStateChanges(&imgHSVResized);
	// *************************************
	
	cvtColor(imgHSVResized, imgHSVResized, CV_HSV2BGR);
	
	// expand it back to full size
	resize(imgHSVResized, *imgBGR, Size(), 2, 2, INTER_LINEAR);
	
	//Copy the vector to a long so we can use it in java
	//Longer by 1 to include the length
	long *templong = new long[returnList.size() + 1];
	templong[0] = (long) (returnList.size() + 1);
	
	for(int i = 1; i < (returnList.size() + 1); i++){
		templong[i] = (long) returnList[i-1];
	}
	
	
	return (long) templong;
	
}



//Function to resize matrix (vertically)
Mat* Processor::resizeMatrix(Mat *img, int maxsize, int step){
	CV_Assert(img&&(img->type() == CV_8UC1));
	Mat* newMat = new Mat(maxsize, img->cols, CV_8UC1);
	int counter = 0;
	for(int i = 0; (i < maxsize) && ((i*step) < maxsize); i++){
		counter++;
		for(int j = 0; j < img->cols; j++){
			newMat->at<uchar>(i, j) = img->at<uchar>(i * step, j);
		}
	}
	if(counter < maxsize){
		Mat *localMat = new Mat(counter, img->cols, CV_8UC1);
		Mat tempMat = newMat->rowRange(0, counter);
		tempMat.copyTo(*localMat);
		delete newMat;
		return localMat;
	}
	else
		return newMat;
}

Mat* Processor::filterMat(Mat *img, float value){
	
	Mat *newMat = new Mat(img->rows, img->cols, img->type());
	
	
	for(int i = 0; i < img->rows; i++){
		for(int j = 0; j < img->cols; j++){
			if(img->at<float>(i, j) == value)
				newMat->at<float>(i, j) = img->at<float>(i, j);
			
		}
	}
	return newMat;
	
}


int Processor::depthCheck(Mat *img){
	if(img->depth() == CV_8U)
		return 1;
		//cout << "8 bit unsigned" << endl;	
	else if(img->depth() == CV_8S)
		return 2;
		//cout << "8 bit signed" << endl;
	else if(img->depth() == CV_16U)
		return 3;
		//cout << "16 bit unsigned" << endl;	
	else if(img->depth() == CV_16S)
		return 4;
		//cout << "16 bit signed" << endl;	
	else if(img->depth() == CV_32S)
		return 5;
		//cout << "32 bit signed" << endl;	
	else if(img->depth() == CV_32F)
		return 6;
		//cout << "32 bit Floating Point" << endl;	
	else if(img->depth() == CV_64F)
		return 7;
		//cout << "64 bit Floating Point" << endl;	
	else
		return 8;
		//cout << "unknown: " << img->depth() << endl;
}


void Processor::filterBackground(int input_idx, image_pool* pool, int satThreshold, int valThreshold){

	
	Ptr<Mat> img = pool->getImage(input_idx);
	Mat resized;
	LOGI("rows: %i cols: %i", img->rows, img->cols);
	resize(*img, resized, Size(), 0.5, 0.5, INTER_LINEAR);
	LOGI("resized rows: %i cols: %i", resized.rows, resized.cols);
	
	backgroundSubtraction(&resized, 0, satThreshold, valThreshold);
	
	resize(resized, *img, Size(), 2, 2, INTER_LINEAR);
	
}

float Processor::getLineHueValues(int input_idx, image_pool* pool){
	
	
	
	Ptr<Mat> img = pool->getImage(input_idx);
	
	//do some preprocessing
	
	//Get a histogram
	Mat hsv;
    cvtColor(*img, hsv, CV_BGR2HSV);
	
    // let's quantize the hue to 30 levels
    // and the saturation to 32 levels
    int hbins = 30;
    int histSize[] = {hbins};
    // hue varies from 0 to 179, see cvtColor
    float hranges[] = { 0, 180 };

    const float* ranges[] = { hranges};
    MatND hist;
    // we compute the histogram from the 0-th and 1-st channels
    int channels[] = {0};
	
    calcHist( &hsv, 1, channels, Mat(), // do not use mask
			 hist, 1, histSize, ranges,
			 true, // the histogram is uniform
			 false );
	
    int scale = 10;
	int maxBinVal = 0;
	
	//we want to ignore the one with the most color (background
	//ignore the ones with the least (threshold them)
	const int minThreshold = 16000;
	const int maxThreshold = 30000;
	vector<float> goodBins;
	
    for( int h = 0; h < hbins; h++ )
	{

            float binVal = hist.at<float>(h);
			if(binVal != maxBinVal) maxBinVal = binVal;

	}
	//now we have our maxBinVal, ignore it
	for( int h = 0; h < hbins; h++ )
	{

            float binVal = hist.at<float>(h);
			if(binVal != maxBinVal && binVal > minThreshold && binVal < maxThreshold) 
			{
				goodBins.push_back(h);
			}

	}
	

	
	std::ostringstream ss;
	for(int i = 0; i < goodBins.size(); i++){
		ss << "(" << goodBins[i] << ") ";
	}
	
	const std::string tmp = ss.str();
	drawText(input_idx, pool, tmp.c_str());

	
	
	return (float) goodBins.size();
}
		   


void Processor::backgroundSubtraction(Mat* img, int hsvReturn, int satThreshold, int valThreshold){
	Mat hsv;
	cvtColor(*img, hsv, CV_BGR2HSV);
	GaussianBlur(hsv, hsv, Size(7,7), 2, 2);
	
	
	double localAverage = 0.0, localMin = 255, localMax = 0.0;
	double localAverageVal = 0.0;
	int kernal = 30;
	int pixelCount = 0;
	
	newLocalThreshold(&hsv, 1, satThreshold, valThreshold);
	//localThresholdPR(&hsv, 1);
	if(hsvReturn == 0)	
		cvtColor(hsv, *img, CV_HSV2BGR);
	else
		hsv.copyTo(*img);
	
}

//satThreshold = 60, valThreshold = 60

void Processor::newLocalThreshold(Mat *img, int kernal, int satThreshold, int valThreshold){
	double localAverage = 0.0, localMin = 255, localMax = 0.0;
	double localAverageVal = 0.0;
	int pixelCount = 0;
	
	for(int a = 0; a < img->rows; a++)
	{
		const uchar* Mi = img->ptr<uchar>(a);
		for(int b = 0; b < img->cols; b++)
		{
			//cout << (int) Mi[j] << endl;
			localAverage = 0.0;
			localAverageVal = 0.0;
			pixelCount = 0;
			for(int c = 0; c < kernal && ((c + a)  <= img->rows) ; c++){
				const uchar* Mi2 = img->ptr<uchar>(a + c);
				for(unsigned int d = 0; d < kernal && ((d + b)  <= img->cols); d++){
					pixelCount++;
					uchar sat = (uchar) Mi2[(3 *(b + d)) +1];
					uchar val = (uchar) Mi2[(3 *(b + d)) +2];
					//cout << one << endl;
					localAverage +=  sat;
					localAverageVal +=  val;
				}
			}
			localAverage = localAverage / pixelCount;
			localAverageVal = localAverageVal / pixelCount;
			
			if(localAverageVal > valThreshold && localAverage < satThreshold){ 
				for(unsigned int c = 0; c < kernal  && ((c + a)  <= img->rows); c++){
					uchar* Mi2 = img->ptr<uchar>(a + c);
					for(unsigned int d = 0; d < kernal  && ((d + b)  <= img->cols); d++){
						//shouldn't consider hue value
						//Mi2[(3 *(b + d)) +1] = 0; PRESERVE SAT VAL
						Mi2[(3 *(b + d)) +2] = 0;
						//classic values are 0, 0
						
					}
				}	
				
			}//if
		}
	}

	
}


void Processor::localThresholdPR(Mat *img, int kernal){
	 //This code isn't currently being used because it's not as effective as the thresholding method
	 
	 
	double runningAverage = 0;
	double pixelSum = 0;
	int direction = 0; //0 = forward, 1 = backward
	double threshold = 30;
	int pixelNo = 0;
	
	for(unsigned int a = 0; a < img->rows; a++ )
	{
		pixelSum = 0;
		
		if(direction == 0){
			
			for(unsigned int b = 0; b < img->cols; b++)
			{
				pixelNo++;
				pixelSum += abs(img->at<Vec3b>(a, b)[2] - img->at<Vec3b>(a, b)[1]); //lightness v -> 0 - 255 (Dark = 0, light = 255)
				if(((double) abs(img->at<Vec3b>(a, b)[2] - img->at<Vec3b>(a, b)[1]) > (runningAverage - threshold))){
					img->at<Vec3b>(a, b)[2] = 0;
				}
				runningAverage = pixelSum / (double) (b+1);
			}
		}
		else{
			for(unsigned int b = 0; b < img->cols; b++)
			{
				pixelNo++;
				pixelSum += abs((double) img->at<Vec3b>(a, (img->cols - b - 1))[2] - img->at<Vec3b>(a, (img->cols - b - 1))[1]); //lightness v -> 0 - 255 (Dark = 0, light = 255)
				if(abs((double) img->at<Vec3b>(a, (img->cols - b - 1))[2] - img->at<Vec3b>(a, (img->cols - b - 1))[1]) > (runningAverage - threshold)){ //e.g. average 150, pixel 130, difference 10 < 20
					img->at<Vec3b>(a, (img->cols - b - 1))[2] = 0;
				}
				runningAverage = pixelSum / (double) (b+1);
			}
			
		}
		//alternate direction
		if(direction == 1) direction = 0; else direction = 1;
	}
	
	dilate(*img, *img, Mat(), Point(0, 0), 1);
	 

}



void Processor::drawText(int i, image_pool* pool, const char* ctext){
	// Use "y" to show that the baseLine is about
	string text = ctext;
	int fontFace = FONT_HERSHEY_COMPLEX_SMALL;
	double fontScale = 1.5;
	int thickness = 1;

	Mat img = *pool->getImage(i);

	int baseline=0;
	Size textSize = getTextSize(text, fontFace,
	                            fontScale, thickness, &baseline);
	baseline += thickness;

	// center the text
	Point textOrg((img.cols - textSize.width)/2,
	              (img.rows - textSize.height *2));

	// draw the box
	//rectangle(img, textOrg + Point(0, baseline),
	//          textOrg + Point(textSize.width, -textSize.height),
	//          Scalar(0,0,255),CV_FILLED);
	// ... and the baseline first
	line(img, textOrg + Point(0, thickness),
	     textOrg + Point(textSize.width, thickness),
	     Scalar(0, 0, 255));

	// then put the text itself
	putText(img, text, textOrg, fontFace, fontScale,
	        Scalar::all(255), thickness, 8);
}

