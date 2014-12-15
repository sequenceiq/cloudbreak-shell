## Cloudbreak Shell

The goal with the CLI was to provide an interactive command line tool which supports:

* all functionality available through the REST API or Cloudbreak web UI
* makes possible complete automation of management task via **scripts**
* context aware command availability
* tab completion
* required/optional parameter support
* **hint** command to guide you on the usual path

## Install Cloudbreak Shell

You have 3 options to give it a try:

- use our prepared [docker image](https://registry.hub.docker.com/u/sequenceiq/cloudbreak/)
- download the latest self-containing executable jar form our maven repo
- build it from source

### Build from source

If want to use the code or extend it with new commands follow the steps below. You will need:
- jdk 1.7

```
git clone https://github.com/sequenceiq/cloudbreak-shell.git
cd cloudbreak-shell
./gradlew clean build
```

_Note: In case you use the hosted version of Cloudbreak you should use the `latest-release.sh` to get the right version of the CLI.
In case you build your own Cloudbreak from the `master` branch you should use the `latest-snap.sh` to get the right version of the CLI._

<!--more-->

## Sign in and connect to Cloudbreak

There are several different ways to use the shell. First of all you'll need a Cloudbreak instance you can connect to. The easiest way is to use our hosted solution - you can access it with your SequenceIQ credentials. If you don't have an account, you can subscribe [here](https://accounts.sequenceiq.com/register).

Alternatively you can host your own Cloudbreak instance - for that just follow up with the steps in the Cloudbreak [documentation](http://sequenceiq.com/cloudbreak/#quickstart-and-installation). If you're hosting your own Cloudbreak server you can still use your SequenceIQ credentials and our identity server, but then you'll have to configure your Cloudbreak installation with proper client credentials that will be accepted by our identity server. It is currently not supported to register your Cloudbreak application through an API (but it is planned), so contact us if you'd like to use this solution.

The third alternative is to deploy our whole stack locally in your organization along with [Cloudbreak](http://sequenceiq.com/cloudbreak/#quickstart-and-installation), our OAuth2 based [Identity Server](http://blog.sequenceiq.com/blog/2014/10/16/using-uaa-as-an-identity-server/), and our user management application, [Sultans](https://github.com/sequenceiq/sultans).

We suggest to try our hosted solution as in case you have any issues we can always help you. Please feel free to create bugs, ask for enhancements or just give us feedback by either using our [GitHub repository](https://github.com/sequenceiq/cloudbreak) or the other channels highlighted in the product documentation (Google Groups, email or social channels).

The shell is built as a single executable jar with the help of [Spring Boot](http://projects.spring.io/spring-boot/).

```
Usage:
  java -jar cloudbreak-shell-0.2-SNAPSHOT.jar                  : Starts Cloudbreak Shell in interactive mode.
  java -jar cloudbreak-shell-0.2-SNAPSHOT.jar --cmdfile=<FILE> : Cloudbreak executes commands read from the file.

Options:
  --cloudbreak.address=<http[s]://HOSTNAME:PORT>  Address of the Cloudbreak Server [default: https://cloudbreak-api.sequenceiq.com].
  --identity.address=<http[s]://HOSTNAME:PORT>    Address of the SequenceIQ identity server [default: https://identity.sequenceiq.com].
  --sequenceiq.user=<USER>                        Username of the SequenceIQ user [default: user@sequenceiq.com].
  --sequenceiq.password=<PASSWORD>                Password of the SequenceIQ user [default: password].

Note:
  You should specify at least your username and password.
```
Once you are connected you can start to create a cluster. If you are lost and need guidance through the process you can use `hint`. You can always use `TAB` for completion. Note that all commands are `context aware` - they are available only when it makes sense - this way you are never confused and guided by the system on the right path.

### Create a cloud credential

In order to start using Cloudbreak you will need to have a cloud user, for example an Amazon AWS account. Note that Cloudbreak **does not** store you cloud user details - we work around the concept of [IAM](http://aws.amazon.com/iam/) - on Amazon (or other cloud providers) you will have to create an IAM role, a policy and associate that with your Cloudbreak account - for further documentation please refer to the [documentation](http://sequenceiq.com/cloudbreak/#accounts).

```
credential createEC2 --description “description" --name “myCredentialName" --roleArn "arn:aws:iam::NUMBER:role/cloudbreak-ABC" --sshKeyUrl “URL towards your AWS public key"
```

Alternatively you can upload your public key from a file as well, by using the `—sshKeyPath` switch. You can check whether the credential was creates successfully by using the `credential list` command. You can switch between your cloud credential - when you’d like to use one and act with that you will have to use:

```
credential select --id #ID of the credential
```

### Create a template

A template gives developers and systems administrators an easy way to create and manage a collection of cloud infrastructure related resources, maintaining and updating them in an orderly and predictable fashion. A template can be used repeatedly to create identical copies of the same stack (or to use as a foundation to start a new stack).

```
template createEC2 --name awstemplate --description aws-template  --region EU_WEST_1 --instanceType M3Xlarge --volumeSize 100 --volumeCount 2
```
You can check whether the template was created successfully by using the `template list` command. Check the template and select it if you are happy with it:

```
template show --id #ID of the template

template select --id #ID of the template
```
### Create a stack

Stacks are template `instances` - a running cloud infrastructure created based on a template. Use the following command to create a stack to be used with your Hadoop cluster:

```
stack create --name “myStackName" --nodeCount 10
```
### Select a blueprint

We ship default Hadoop cluster blueprints with Cloudbreak. You can use these blueprints or add yours. To see the available blueprints and use one of them please use:

```
blueprint list

blueprint select --id #ID of the blueprint
```
### Create a Hadoop cluster
You are almost done - one more command and this will create your Hadoop cluster on your favorite cloud provider. Same as the API, or UI this will use your `template`, and by using CloudFormation will launch a cloud `stack` - once the `stack` is up and running (cloud provisioning is done) it will use your selected `blueprint` and install your custom Hadoop cluster with the selected components and services. For the supported list of Hadoop components and services please check the [documentation](http://sequenceiq.com/cloudbreak/#supported-components).

```
cluster create --description “my cluster desc"
```
You are done - you can check the progress through the Ambari UI. If you log back to [Cloudbreak UI](https://cloudbreak.sequenceiq.com/) you can check the progress over there as well, and learn the IP address of Ambari.

### Automate the process
Each time you start the shell the executed commands are logged in a file line by line and later either with the `script` command or specifying an `—cmdfile` option the same commands can be executed again.

## Commands

For the full list of available commands please check below. Please note that all commands are context aware, and you can always use `TAB` for command completion.


    * blueprint add - Add a new blueprint with either --url or --file
    * blueprint defaults - Adds the default blueprints to Cloudbreak
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



As usual for us - being committed to 100% open source - we are always open sourcing everything thus you can get the details on our [GitHub](https://github.com/sequenceiq/cloudbreak-shell) repository.
Should you have any questions feel free to engage with us on our [blog](http://blog.sequenceiq.com/) or follow us on [LinkedIn](https://www.linkedin.com/company/sequenceiq/), [Twitter](https://twitter.com/sequenceiq) or [Facebook](https://www.facebook).
