
/*
 *  localknearest.cpp
 *  
 *
 *  Created by Alistair Graham Stead on 09/03/2011.
 *  Copyright 2011. All rights reserved.
 *
 */


#include "LocalKNearest.h"
#include <android/log.h>
#define  LOG_TAG    "LocalKNearestCPP"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)



// k Nearest Neighbors

LocalKNearest::LocalKNearest(Mat* train_data, Mat* responses, int max_k , vector<vector<int> > averageVector)
{
	CV_Assert(train_data->type() == CV_8UC1 && train_data->cols == 3);
	
	

	//LOGE("Training Data rows: %d, Responses rows: %d", train_data->rows, responses->rows);
	
	storedMat = new Mat(train_data->rows, 3, CV_32F);
	for(int i = 0; i < storedMat->rows; i++){
		storedMat->at<float>(i, 0) = (int) ((float)train_data->at<uchar>(i, 0) / 180 * 255);// * 500;
		storedMat->at<float>(i, 1) = (int) train_data->at<uchar>(i, 1);// * 10;//was 10
		storedMat->at<float>(i, 2) = (int) train_data->at<uchar>(i, 2);
	}
	

	
	/** Print out the number of labels each class has - important to detect bias*/
	vector<int> countVector; 
	for(int i = 0; i < responses->rows; i++){
		while(countVector.size() <= responses->at<uchar>(i, 0)){
			countVector.push_back(0);
		}
		countVector[responses->at<uchar>(i, 0)]++;
	}
	for(int i = 0; i < countVector.size(); i++){
		LOGE("LabelCounts %d: %d", i, countVector[i]);
	}
	
	
	
	/** Copy labels across to local variable so we can use them when classifying */
	labelResponses = new Mat(responses->rows, 1, CV_32S);
	for(int i = 0; i < responses->rows; i++){
		labelResponses->at<int>(i, 0) = responses->at<uchar>(i, 0);
	}
	delete(responses);
	
    index = new cv::flann::Index(*storedMat, cv::flann::KDTreeIndexParams(4)); 
	/*index = new cv::flann::Index(*storedMat /*,cv::flann::LinearIndexParams()); *///,/*cv::flann::AutotunedIndexParams(0.9, 0.01, 1.5, 0.1)); */
	//index = new cv::flann::Index(*storedMat, cv::flann::LinearIndexParams());
	K = max_k;
	
	
	/* Store the averages here so we can give some sort of feedback later to the user */
	averages = averageVector;
}


//sample data will have 2 cols
vector<int> LocalKNearest::find_nearest(Mat* sampleData){
	//printSampleAverage(sampleData);
	
	/* Doesn't seem to make much of a difference here. Given small range, can't be too high */
	int kNearest = 10; // was 3
	
	//LOGE("sampleData->cols == %d", sampleData->cols);
	CV_Assert(sampleData->type() == CV_8U && sampleData->cols == 3);
	vector<int> votes;
	
	cv::Mat m_indices = Mat::zeros(kNearest, kNearest, CV_32S);
    cv::Mat m_dists = Mat::zeros(kNearest, kNearest, CV_32F);
	cv::Mat m_object = Mat::zeros(1, 3, CV_32F);
	
	for(int i = 0; i < K; i++){
		votes.push_back(0);
	}
	
	for(int i = 0; i < sampleData->rows; i++){
		//Load up the feature vector
		m_object.at<float>(0, 0) = ((float)sampleData->at<uchar>(i, 0) / 180) * 255;// * 500; // has been 100, 500, 1000
		m_object.at<float>(0, 1) = sampleData->at<uchar>(i, 1);// * 10;//was 10
		m_object.at<float>(0, 2) = sampleData->at<uchar>(i, 2);
		
		index->knnSearch(m_object, m_indices, m_dists, kNearest, cv::flann::SearchParams(64) );
		
		
		/** Now count the mode label for nearest neighbours*/
		/*
		int labelCount[K];
		for(int p = 0; p < K; p++)
			labelCount[p] = 0;
		
		for(int p = 0; p < kNearest; p++){
			if(m_indices.at<int>(p, 1) != 0){
				
				for(int q = 0; q < kNearest; q++){
					labelCount[labelResponses->at<int>(m_indices.at<int>(p, q))-1]++;
				}
				
			}
		}
		int highestIndex = 0;
		for(int f = 0; f < K; f++){
			if(labelCount[f] > labelCount[highestIndex])
				highestIndex = f;
		}
		int label = highestIndex +1; //simulate what used to happen
		*/
		
		float labelCount[K];
		for(int p = 0; p < K; p++)
			labelCount[p] = 0;
		
		//for loop goes through each line of the indices
		for(int p = 0; p < kNearest;p++){
			if(m_indices.at<int>(p, 1) != 0){
				for(int q = 0; q < kNearest; q++){
					labelCount[labelResponses->at<int>(m_indices.at<int>(p, q))-1] += ((float) 1 / (float) m_dists.at<float>(p, q));
				}
			}
		}
		int highestIndex = 0;
		for(int f = 0; f < K; f++){
			//cout << (float) labelCount[f] << endl;
			if(labelCount[f] > labelCount[highestIndex])
				highestIndex = f;
		}
		int label = highestIndex +1;
		
		
		
	
		votes[label-1]++;
	}
	
	
	
	
	
	
	return votes;
	
	
	
}

//during localKNearest, for every sample point, go through the list and
// find the nearest class. The highest vote will be the final result

vector<vector<int> > LocalKNearest::getVectorAverages(){
	return averages;
}


void LocalKNearest::printSampleAverage(Mat* sample){
	
	int sumHue = 0;
	int sumSat = 0;
	int sumVal = 0;
	for(int i = 0; i < sample->rows; i++){
		sumHue += sample->at<uchar>(i, 0);
		sumSat += sample->at<uchar>(i, 1);
		sumVal += sample->at<uchar>(i, 2);
	}
	
	sumHue /= sample->rows;
	sumSat /= sample->rows;
	sumVal /= sample->rows;
	
	LOGE("Sample H: %d S: %d V: %d", sumHue, sumSat, sumVal); 

}




int LocalKNearest::getSourceAverage(Mat* vectors, Mat* responses, int LabelNo, int vectorNo){
	float sum = 0;
	int count = 0;
	
	for(int i = 0; i < vectors->rows; i++){
		if(responses->at<uchar>(i, 0) == LabelNo){
			sum +=  vectors->at<float>(i, vectorNo);
			count++;
		}
	}
	
	
	if(sum != 0 && count != 0){
		sum /= count;
	}
	
	
	return (int) sum;

}
















