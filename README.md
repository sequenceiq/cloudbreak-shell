cloudbreak-shell
================

_Cloudbreak is a powerful left surf that breaks over a coral reef, a mile off southwest the island of Tavarua, Fiji._


_Cloudbreak is a cloud agnostic Hadoop as a Service API. Abstracts the provisioning and ease management and monitoring of on-demand clusters._


Cloudbreak is a RESTful application development platform with the goal of helping developers to build solutions for deploying Hadoop YARN clusters in different environments. Once it is deployed in your favorite servlet container it exposes a REST API allowing to span up Hadoop clusters of arbitary sizes and cloud providers. Provisioning Hadoop has never been easier. Cloudbreak is built on the foundation of cloud providers API (Amazon AWS, Microsoft Azure), Apache Ambari, Docker lightweight containers, Serf and dnsmasq.

The Cloudbreak Shell is based on  the REST [API](http://docs.cloudbreak.apiary.io/).

In order to start with the shell use `java -jar cloudbreak-shell-0.1-SNAPSHOT.jar --cloudbreak.host=cloudbreak-api.sequenceiq.com --cloudbreak.port=80 --cloudbreak.user=xxxxx@sequenceiq.com --cloudbreak.password=xxxxx` where
  
    * cloudbreak.host - the host name or IP address of a Cloudbreak deployment
    * cloudbreak.port - port where Cloudbreak is accessible
    * cloudbreak.user - user name 
    * cloudbreak.password - password 

