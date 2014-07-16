cloudbreak-shell
================

_Cloudbreak is a powerful left surf that breaks over a coral reef, a mile off southwest the island of Tavarua, Fiji._

_Cloudbreak is a cloud agnostic Hadoop as a Service API. Abstracts the provisioning and ease management and monitoring of on-demand clusters._

Cloudbreak [API documentation](http://docs.cloudbreak.apiary.io/).


Cloudbreak is a RESTful application development platform with the goal of helping developers to build solutions for deploying Hadoop YARN clusters in different environments. Once it is deployed in your favourite servlet container it exposes a REST API allowing to span up Hadoop clusters of arbitrary sizes and cloud providers. Provisioning Hadoop has never been easier. Cloudbreak is built on the foundation of cloud providers API (Amazon AWS, Microsoft Azure), Apache Ambari, Docker lightweight containers, Serf and dnsmasq.

The Cloudbreak Shell is based on  the REST [API](http://docs.cloudbreak.apiary.io/).

In order to start with the shell use `java -jar cloudbreak-shell-0.1-SNAPSHOT.jar --cloudbreak.host=cloudbreak-api.sequenceiq.com --cloudbreak.port=80 --cloudbreak.user=xxxxx@sequenceiq.com --cloudbreak.password=xxxxx` where
  
    * cloudbreak.host - the host name or IP address of a Cloudbreak deployment
    * cloudbreak.port - port where Cloudbreak is accessible
    * cloudbreak.user - user name 
    * cloudbreak.password - password 

The list of available commands:

    * blueprint add - Add a new blueprint with either --url or --file
    * blueprint defaults - Adds the default blueprints to Ambari
    * blueprint list - Shows the currently available blueprints
    * blueprint select - Select the blueprint by its id
    * blueprint show - Shows the blueprint by its id
    * cluster create - Create a new cluster based on a blueprint and template
    * cluster show - Shows the cluster by stack id
    * credential createAzure - Create a new Azure credential
    * credential createEC2 - Create a new EC2 credential
    * credential defaults - Adds the default credentials to Cloudbreak
    * credential list - Shows all of your credentials
    * credential select - Select the credential by its id
    * credential show - Shows the credential by its id
    * exit - Exits the shell
    * help - List all commands usage
    * hint - Shows some hints
    * quit - Exits the shell
    * script - Parses the specified resource file and executes its commands
    * stack create - Create a new stack based on a template
    * stack list - Shows all of your stack
    * stack select - Select the stack by its id
    * stack show - Shows the stack by its id
    * stack terminate - Terminate the stack by its id
    * template create - Create a new cloud template
    * template createEC2 - Create a new EC2 template
    * template defaults - Adds the default templates to Cloudbreak
    * template list - Shows the currently available cloud templates
    * template select - Select the template by its id
    * template show - Shows the template by its id
    * version - Displays shell version
