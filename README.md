# Viper

Viper is the Telemesh Android Client that acts as a communication bridge between Telemesh Android Application (TA) and Telemesh Service (TS).
It is responsible for coordinating the actions to the Telemesh Service. 
It can also perform some mappings to prepare the objects coming from the Telemesh Application.

## Supports
It contains few tools to ease developers daily development. Support provides:

* Custom components (BaseActivity, BaseFragment, BaseAdapter etc.)
* Custom Widgets (BaseButton, BaseRecyclerView, BaseEditText etc.)
* Close Coupled Behavior with Widget and Components
* Separate *release* and *build* application class with predefined library attached
* Few configurable options (debugDatabase, Toasty etc.Still we are improving here)
* Enhanced support for Room (migration, creation of database, columns etc.)
* Necessary library added such a way so that developers can use
without including in their gradle file (Timber, Multidex, Crashlytics, Debug Database etc.)
* Timber facilitates clickable logs with method name, line number prefixed
and automatic TAG of class name. It distinguishes expected release build
and debug build behavior
* BaseSplashViewModel provides time calculation facility and enforce ViewModel 
LiveData communication (provided _sample_ app has the usage)


## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

What things you need to install the software and how to install them

```
Give examples
```

### Installing

A step by step series of examples that tell you how to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone whose code was used
* Inspiration
* etc
