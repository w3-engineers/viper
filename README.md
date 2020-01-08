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

## Technical documentation





## Contributing

Please read [CONTRIBUTING.md](https://github.com/w3-engineers/viper/blob/master/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning.

## License

This project is licensed under the GNU GPLv3 License - see the [LICENSE.md](https://github.com/w3-engineers/viper/blob/master/LICENSE) file for details

