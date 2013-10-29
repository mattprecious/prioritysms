Configuration
-------------

This project is built using gradle. Unfortunately, not all of the dependencies are ready for gradle out of the box. You will need to clone 3 modified repositories to your workspace and install them to your local Maven repository.

* [mattprecious/Android-ViewPagerIndicator][vpi-url]
* [mattprecious/android-switch-backport][asb-url]
* [mattprecious/GlowPadBackport][gpb-url]

For each of the projects, execute `gradle-install.sh` in the root directory to build and install to your local Maven repository.

  [vpi-url]: https://github.com/mattprecious/Android-ViewPagerIndicator
  [asb-url]: https://github.com/mattprecious/android-switch-backport
  [gpb-url]: https://github.com/mattprecious/GlowPadBackport