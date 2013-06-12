
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
	CV_Assert(train_data->type() == CV_8UC1 && train_data->cols == 2);
	
	//Mat  |   0   |   1   |   2    |   3     |    4   |
	//     | Vec1  |  Vec2 |Square1 | Square2 |  Label |   
	
	storedMat = new Mat(train_data->rows, 5, CV_32S);
	
	for(int i = 0; i < storedMat->rows; i++){
		int square1 =  (int) train_data->at<uchar>(i, 0) *  train_data->at<uchar>(i, 0);
		int square2 =  (int) train_data->at<uchar>(i, 1) * train_data->at<uchar>(i, 1);
		storedMat->at<int>(i, 0) = (int) train_data->at<uchar>(i, 0);
		storedMat->at<int>(i, 1) = (int) train_data->at<uchar>(i, 1);
		storedMat->at<int>(i, 2) = square1;
		storedMat->at<int>(i, 3) = square2;
		storedMat->at<int>(i, 4) = (int) responses->at<uchar>(i, 0);
	}
	K = max_k;
	
	
	//*************
	max_k = 5;
	Mat testMat = Mat(train_data->rows, 2, CV_32F);
	for(int i = 0; i < storedMat->rows; i++){
		testMat.at<float>(i, 0) = (int) train_data->at<uchar>(i, 0);
		testMat.at<float>(i, 1) = (int) train_data->at<uchar>(i, 1);
		//LOGE("Val: %f", testMat.at<float>(i, 1));
	}
	
	cv::Mat m_indices(max_k, 2, CV_32S);
    cv::Mat m_dists(max_k, 2, CV_32F);
	cv::Mat m_object(1, 2, CV_32F);
	
	m_object.at<float>(0, 0) = (float) 50;
	m_object.at<float>(0, 1) = (float) 150;
	
    cv::flann::Index *flann_index = new cv::flann::Index(testMat, cv::flann::KDTreeIndexParams(4)); 
	
	flann_index->knnSearch(m_object, m_indices, m_dists, max_k, cv::flann::SearchParams(64) ); // maximum 
	
	
	LOGE("Object: (%f, %f)",m_object.at<float>(0, 0), m_object.at<float>(0, 1));	
	LOGE("Closest Neighbour Label: %d", (int) responses->at<uchar>(m_indices.at<int>(0, 1), 0));
	LOGE("Closest Neighbour: (%f, %f)", testMat.at<float>(m_indices.at<int>(0, 1), 0), testMat.at<float>(m_indices.at<int>(0, 1), 1));
	LOGE("flann works");
	//************
    averages = averageVector;
	for(int i = 0; i < averageVector.size(); i++){
		LOGE("LocalKNearest Averages: %d %d, %d, %d", i, averageVector[i][0], averageVector[i][1], averageVector[i][2]);
	}
}


//sample data will have 2 cols
vector<int> LocalKNearest::find_nearest(Mat* sampleData){
	
	
	//Mat  |   0   |   1   |   2    |   3     |    4   |
	//     | Vec1  |  Vec2 |Square1 | Square2 |  Vote  |  
	
	
	CV_Assert(sampleData->type() == CV_8UC1 && sampleData->cols == 2);
	/*
	Mat localMat = Mat::zeros(sampleData->rows, 5, CV_32S);
	
	for(int i = 0; i < localMat.rows; i++){
		localMat.at<int>(i, 0) = (int) sampleData->at<uchar>(i, 0);
		localMat.at<int>(i, 1) = (int) sampleData->at<uchar>(i, 1);
		localMat.at<int>(i, 2) = localMat.at<int>(i, 0) * localMat.at<int>(i, 0);	
		localMat.at<int>(i, 3) = localMat.at<int>(i, 1) * localMat.at<int>(i, 1);	
	}

	
	
	
	int nearestClass[2];
	for(int sd = 0; sd < localMat.rows; sd++){
		nearestClass[0] = 1000000; //difference
		nearestClass[1] = -1; //label
		for(int i = 0; i < storedMat->rows; i++){
			int diff1 = localMat.at<int>(sd, 2) - storedMat->at<int>(i, 2);
			int diff2 = localMat.at<int>(sd, 3) - storedMat->at<int>(i, 3);
			diff1 = abs(diff1);
			diff2 = abs(diff2);
			int totalDiff = diff1 + diff2;
			
			if(totalDiff < nearestClass[0]){
				//cout << totalDiff << endl;
				nearestClass[0] = totalDiff;
				nearestClass[1] = storedMat->at<int>(i, 4);
				
			}
		}
		//cout <<  endl;
		localMat.at<int>(sd,4) = nearestClass[1];
	}
	
	
*/
	vector<int> votes;
	for(int i = 0; i < K; i++)
		votes.push_back(0);/*
	
	//return the k highest vote
	for(int p = 0; p < localMat.rows; p++){
		votes[localMat.at<int>(p, 4) -1]++;
	}
	
*/

	return votes;
	
	//LOGI("firstClass: %i",localMat.at<int>(0, 3));
	
	
	
}

//during localKNearest, for every sample point, go through the list and
// find the nearest class. The highest vote will be the final result



//during localKNearest, for every sample point, go through the list and
// find the nearest class. The highest vote will be the final result

vector<vector<int> > LocalKNearest::getVectorAverages(){
	return averages;
}

























