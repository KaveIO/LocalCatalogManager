# LocalCatalogManager
The LocalCatalogManager is all about MetaData. However to be sure we are talking about the same thing we handle the following definition for MetaData: 

> a set of data that describes and gives information about other data.

Pretty straight forward. The LocalCatalogManager: Manages, Interprets, and Distributes MetaData. This helps Data Scientists in Curating their Data Sets keeping their work reproducible and reusable. The focus of this project is enabling this functionality between different Data Lakes / Data Science environments. This helps in setting up collaboration between data scientists by providing a distributed mechanism for data distribution. 

The LCM works by setting up explicit two-way SSL trust relationships and communicating data sets over these lines. Behind each LCM there can be a multiple storage backends that provide the actual data described. In this way LCM can share metadata and the associated data with each other. 

## Getting Started 

### Docker
In order to help with exploring this tool we offer docker images. Be aware though, the docker images are not build to be a production setup. Data is not persisted outside. 

In order to run this docker environment do the following: 

```bash 
docker pull kave/lcm

docker run -h mongo --name lcm-mongo -p 27017:27017 -d mongo:3
docker run -itd -h server --name lcm-server --link lcm-mongo:mongo -p 8081:8081 kave/lcm server
docker run -itd -h ui --name lcm-ui --link lcm-server:server -p 8080:8080 kave/lcm ui
```

This will start a mongo database, the LCM server and the LCM ui. You can use your browser to reach

    http://localhost:8080/ 

To access the UI. The default username and password are: admin, admin. 


### Stand alone deployment
Download the latest [release](https://github.com/KaveIO/LocalCatalogManager/releases). 

Untar the release. 
```bash 
  tar -xvzf lcm-complete-0.2.2-bin.tar.gz
```

After this you can get started. The quickest way to get this running is a unconfigured setup. The commands you have to run for this are the following:

```bash 
  bin/setup-ssl.sh - currently itis not working look at cetificates/Readme.md for details
  bin/start-dockerized-mongo.sh
  bin/populate-dockerized-mongo.sh
  bin/start-server.sh
  bin/start-ui.sh
```

A couple of notes with this procedure: 
 - This setup does depend on docker for running a MongoDB. 
 - The last two commands currently have to run in foreground. 
 - The ssl setup uses unsigned certificates, this will generate a warning in your browser. 

You can use your browser to reach

    https://localhost:4443/

To access the UI. The default username and password are: admin, admin

## Goals  

### GlobalCatalogManager
We are working towards a global hub for exchange of metadata, data & analytics. 

### Federated Analytics
Currently the mechanism works by sending data to the analytics. This is not a solution of this time. Our objective is to bring the analytics to the data on multi data lake environment. 
