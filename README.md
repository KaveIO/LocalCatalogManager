# LocalCatalogManager
The LocalCatalogManager is all about MetaData. However to be sure we are talking about the same thing we handle the following definition for MetaData: 

> a set of data that describes and gives information about other data.

Pretty straight forward. The LocalCatalogManager: Manages, Interprets, and Distributes MetaData. This helps Data Scientists in Curating their Data Sets keeping their work reproducible and reusable. The focus of this project is enabling this functionality between different Data Lakes / Data Science environments. This helps in setting up collaboration between data scientists by providing a distributed mechanism for data distribution. 

The LCM works by setting up explicit two-way SSL trust relationships and communicating data sets over these lines. Behind each LCM there can be a multiple storage backends that provide the actual data described. In this way LCM can share metadata and the associated data with each other. 

## Goals  

### GlobalCatalogManager
We are working towards a global hub for exchange of metadata, data & analytics. 

### Federated Analytics
Currently the mechanism works by sending data to the analytics. This is not a solution of this time. Our objective is to bring the analytics to the data on multi data lake environment. 
