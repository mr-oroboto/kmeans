# kmeans
Java implementation of the K-means clustering algorithm with AWS Lambda wrapper.

This repository contains two projects:

- backend

The implementation of the K-means clustering algorithm. The actual algorithm (KMeans.java) has a simple interface that accepts a set of training samples and attempts to isolate them into k clusters. It is fronted by RequestHandler.java, a class that allows the algorithm to be run on AWS Lambda and uses AWS DynamoDB as a persistence layer for training sets.

- frontend

A simple AngularJS frontend application to interrogate known training sets, find clusters within their training samples and add new training sets.

# Building

The backend can be quickly built using Gradle. The frontend can run more or less in place but does use partials so will need to be behind some form of webserver (rather than running standalone from your desktop). If that's a pain just build the "addsamples.html" directive template directly into the directive JS.
