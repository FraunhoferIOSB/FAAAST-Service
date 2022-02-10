
<div id="top"></div>


<!-- PROJECT LOGO -->
<br />
<div align="center">

<h3 align="center">FA³ST Service</h3>

<p align="center">
	Easy-to-use Asset Administration Shell Service
	<br />
	<a href="https://github.com/FraunhoferIOSB/FAAAST-Service/tree/main/documentation"><strong>Explore the docs »</strong></a>
	<br />
	<a href="https://github.com/FraunhoferIOSB/FAAAST-Service/issues">Report Bug</a>
	·
	<a href="https://github.com/FraunhoferIOSB/FAAAST-Service/issues">Request Feature</a>
</p>

</div>



<!-- TABLE OF CONTENTS -->
<details>
<summary>Table of Contents</summary>
<ol>
	<li>
	<a href="#about-the-project">About The Project</a>
	<ul>
		<li><a href="#built-with">Built With</a></li>
	</ul>
	</li>
	<li>
	<a href="#getting-started">Getting Started</a>
	<ul>
		<li><a href="#prerequisites">Prerequisites</a></li>
		<li><a href="#installation">Installation</a></li>
	</ul>
	</li>
	<li><a href="#usage">Usage</a></li>
	<li><a href="#develop">Develop</a></li>
	<li><a href="#roadmap">Roadmap</a></li>
	<li><a href="#contributing">Contributing</a></li>
	<li><a href="#license">License</a></li>
	<li><a href="#contact">Contact</a></li>
	<li><a href="#acknowledgments">Acknowledgments</a></li>
</ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
FA³ST Service implements the Asset Administration specification from the Plattform Industrie4.0 and built up an easy-to-use AAS Service based on a custom AAS model instance.

<p align="right">(<a href="#top">back to top</a>)</p>


### Built With

* Java 11
* Maven

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Getting Started

This is an example of how you may setting up your project locally.
To get a local copy up and running follow these simple example steps.

### Prerequisites

This is an example of how to list things you need to use the software and how to install them.
* maven
* java

### Installation

1. Clone the repo
```sh
git clone https://github.com/FraunhoferIOSB/FAAAST-Service
```
2. Install
```sh
maven package
```
3. Use needed classes as dependency in your project

<p align="right">(<a href="#top">back to top</a>)</p>


## Usage with Command Line

To start a FA³ST Service from the command line:
1. Move to the starter project and build the project
	```sh
	cd /starter
	mvn clean package
	```
2. Move to the generated `.jar` file
	```sh
	cd starter/target
	```
3. Execute the `.jar` file to start a FA³ST Service directly with a default configuration. Replace the `{path/to/your/AASEnvironment}` with your file to the Asset Administration Shell Environment you want to load with the FA³ST Service. If you just want to play around, you can use a example AASEnvironment from us [here](starter/src/test/resources/AASFull.json).
	```sh
	java -jar starter-{version}.jar -e {path/to/your/AASEnvironment}
	```

Currently we supporting following formats of the Asset Administration Shell Environment model:
- json
- json-ld
- aml
- xml
- opcua nodeset
- rdf

<hr>
<p>



Following command line parameters could be used:
```
-c, --configFile=<configFilePath>
						The config file path. Default Value = config.json
-e, --environmentFile=<aasEnvironmentFilePath>
						Asset Administration Shell Environment FilePath.
							Default Value = aasenvironment.*
	--emptyEnvironment   Starts the FA³ST service with an empty Asset
							Administration Shell Environment. False by default
	--endpoints[=<endpoints>...]

-h, --help               Show this help message and exit.
	--[no-]autoCompleteConfig
						Autocompletes the configuration with default values
							for required configuration sections. True by
							default
	--[no-]modelValidation
						Validates the AAS Environment. True by default
-V, --version            Print version information and exit.
```
<hr>
<p>

#### Change the Configuration
<p>

In general there are 3 ways to configure your FA³ST Service:
1. Default values
2. Commandline parameters
3. Environment Variables

The 3 kinds can be combined, e.g. by using the default configuration and customizing with commandline parameters and environment variables. If they conflict, environment variables are preferred over all and commandline parameters are preferred over the default values.

Without any manual customization a FA³ST Service with default configuration will be started. For details to the structure and components of the configuration please have a look at the configuration section [here]()
```json
Default Configuration:
{
"core" : {
	"requestHandlerThreadPoolSize" : 2
},
"endpoints" : [ {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint",
	"port" : 8080
} ],
"persistence" : {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemory"
},
"messageBus" : {
	"@class" : "de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal"
}
}
```
<hr>
<p>


The FA³ST Service Starter consider following environment variables:
- `faaast.configFilePath` to use a own configuration file
- `faaast.aasEnvFilePath` to use a Asset Administration Environment file

Environment variables could also be used to adjust some config components in the configuration:
- `faaast.configParameter.[dot.separated.path]`

If you want to change for example the requestHandlerThreadPoolSize in the core configuration, just set the environment variable `faaast.configParameter.core.requestHandlerThreadPoolSize=42`. To access configuration components in a list use the index. For example to change the port of the HTTP endpoint in the default configuration you can set the environment variable `faaast.configParameter.endpoints.0.port=8081`.

<hr>
<p>

You could also use properties to adjust configuration components with the `-D` parameter. To change the `requestHandlerThreadPoolSize` of the core component and the port of the http endpoint use
```sh
java -jar starter-{version}.jar -e {path/to/your/AASEnvironment}
-Dcore.requestHandlerThreadPoolSize=42 -Dendpoints.0.port=8081
```
<hr>
<p>

#### Special Parameters

The parameter `--endpoints` accepts a list of endpoints which should be started with the service. Currently supported is only `http`. So a execution of
```sh
java -jar starter-{version}.jar -e {path/to/your/AASEnvironment} --endoints http
```
leads to a FA³ST Service with the HTTP endpoint implemented in class `de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint`.


<p align="right">(<a href="#top">back to top</a>)</p>

<!-- HOW TO DEVELOP -->
## Develop
### Spotless
The project uses *spotless:check* in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

> mvn spotless:apply

Additionally, you can import the eclipse formating rules defined in */codestyle* into our IDE.

<p align="right">(<a href="#top">back to top</a>)</p>


### Third Party License Usage Report
Generates a report of the licenses used in the subsystems (dependencies).
The report is stored in the directory ./documentation/third_party_licenses as a html page.
> mvn license:aggregate-third-party-report


<!-- ROADMAP -->
## Roadmap


See the [open issues](https://github.com/FraunhoferIOSB/FAAAST-Service/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>


<!-- LICENSE -->
## License

Distributed under the Apacche2.0 License. See `LICENSE` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

* []()
* []()
* []()

<p align="right">(<a href="#top">back to top</a>)</p>
