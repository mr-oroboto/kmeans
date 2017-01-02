# kmeans
Java implementation of the K-means clustering algorithm with AWS Lambda wrapper.

This repository contains two projects:

1. backend

The implementation of the K-means clustering algorithm. The actual algorithm (KMeans.java) has a simple interface that accepts a set of training samples and attempts to isolate them into k clusters. It is fronted by RequestHandler.java, a class that allows the algorithm to be run on AWS Lambda and uses AWS DynamoDB as a persistence layer for training sets.

2. frontend

A simple AngularJS frontend application to interrogate known training sets, find clusters within their training samples and add new training sets.
