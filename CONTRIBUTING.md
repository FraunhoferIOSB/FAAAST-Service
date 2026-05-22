Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions are **greatly appreciated**.

If you have a suggestion for improvements, please fork the repo and create a pull request. You can also simply open an issue.
Don't forget to rate the project! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Add your copyright statement into changed/added source files (.java).

   - For files where you made additions/modifications, add your copyright statement below the existing ones.

   - For new source files, add your copyright statement and the Apache 2.0 license text that you can find in any other .java file.

   - Additionally, add your name or the name of your organization as a one-liner to the `CONTRIBUTORS.txt` file in the root directory of this project.

   - For reference, you can check any other source file. The structure of your copyright statement must be: `Copyright (c) <year> <org-name>`. It may span multiple lines, like the Fraunhofer IOSB statement.
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

### Code Formatting
The project uses *spotless:check* in the build cycle, which means the project only compiles if all code, *.pom and *.xml files are formatted according to the project's codestyle definitions (see details on [spotless](https://github.com/diffplug/spotless)).
You can automatically format your code by running

> mvn spotless:apply

This will also add default license headers with Fraunhofer IOSB (c) if there is no license header in a source file.

Additionally, you can import the eclipse formatting rules defined in */codestyle* into our IDE.

### Third Party License
If you use additional dependencies please be sure that the licenses of these dependencies are compliant with our [License](#license). If you are not sure which license your dependencies have, you can run
> mvn license:aggregate-third-party-report

and check the generated report in the directory `documentation/third_party_licenses_report.html`.
