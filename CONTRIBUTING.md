Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.

If you have a suggestion for improvements, please fork the repo and create a pull request. You can also simply open an issue.
Don't forget to rate the project! Thanks again!

1.   Fork the Project
2.   Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3.   Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4.   Push to the Branch (`git push origin feature/AmazingFeature`)
5.   Open a Pull Request

### Code Formatting
The project uses *spotless:check* in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

> mvn spotless:apply

Additionally, you can import the eclipse formatting rules defined in */codestyle* into our IDE.

### Third Party License
If you use additional dependencies please be sure that the licenses of these dependencies are compliant with our [License](#license). If you are not sure which license your dependencies have, you can run
> mvn license:aggregate-third-party-report

and check the generated report in the directory `documentation/third_party_licenses_report.html`.
