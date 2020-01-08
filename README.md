[![](https://jitpack.io/v/w3-engineers/viper.svg)](https://jitpack.io/#w3-engineers/viper)


# Important Note

This project is currently under highly breaking changes and team is now focusing on achieving some important features.
We are not accepting any contribution request right now.

# Viper

Viper is the android library that acts as a communication bridge between Telemesh Android Application and Telemesh Service.
It is responsible for coordinating the actions to the Telemesh Service. 
It can also perform some mappings to prepare the objects coming from the Android Application.

## Usage

### Dependency

Add it in your root `build.gradle` at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Include the library in app `build.gradle`

    dependencies{
        implementation 'com.github.w3-engineers.viper:paylib:0.0.1-alpha'
    }

To start using the library you just need to call :

TBA

### Supports provided on App side 
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
LiveData communication

### Supports taken from Telemesh Service 
It takes below supports:

* Discovery list​
* Events (Connect, Disconnect, Receive)​​
* Send & Receive Message (Local, Hop, Internet)
* Balance Check​
* Data Buy​
* Balance withdraw (Buyer/Seller])
* Token Air Drop​​



## Getting Started
TBA


### Prerequisites
TBA


### Installing

TBA



## Contributing

Please read [CONTRIBUTING.md](https://github.com/w3-engineers/viper/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## License

This project is licensed under the GNU GPLv3 License - see the [LICENSE.md](https://github.com/w3-engineers/viper/blob/master/LICENSE) file for details

