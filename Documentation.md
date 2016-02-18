## Package Structure ##

In this page you can find a brief description on how FINCoS code is structured. The framework is composed by seven main packages:
  * _adapters_: contains the classes that mediate the communication between FINCoS and the systems under test.
  * _basic_: contains general entity classes used throughout the entire framework.
  * _controller_: contains the classes responsible for coordinating performance runs.
  * _data_: contains the classes responsible for reading and writing data to disk.
  * _driver_: contains the classes involved in load generation.
  * _perfmon_: contains the classes that perform online and offline performance measuring.
  * _random_: contains the classes involved in random number generation.
  * _sink_: contains the classes responsible for processing the output produced by the systems under test.

More details on each package, including their main classes and corresponding relationships are presented next.

### _Basic_ and _Random_ packages ###
The _basic_ package contains two main classes that are used throughout the entire framework: _`Event`_ and _`CSV_Event`_. Both are used to carry information about events that are submitted to the systems under test. Instances of the _`Event`_ class are produced by synthetic workloads while _`CSV_Events`_ are created when reading data from user-provided files. Internally, a _`CSV_Event`_ stores its payload as an array of Strings. Instances of _`Event`_, on the other hand, maintain their payload as an Object-array.

In addition to the payload, a synthetic _`Event`_ contains type infomation, encapsulated as an instance of _`EventType`_. This class describes not only the schema of the events, but also which values their attributes can assume. In particular, an _`Attribute`_ can have its values limited to one of three kinds of _`Domains`_:
  * _random_: a number following a given random distribution;
  * _sequential_: a number with a (random) initial value and an (random) increment;
  * _predefined list_: a fixed list of possible values.
Events of a synthetic workload are then created by the data generation module, which inspects event type information and calls, for each attribute, the _`generateValue()`_ method of the _`Domain`_ class.

![![](http://fincos.googlecode.com/svn/imgs/model/package_basic_random.png)](http://fincos.googlecode.com/svn/imgs/model/package_basic_random.png)
.

### _Controller_ package ###
Contains the classes responsible for the creation, execution and monitoring of performance tests. All the control functions are centralized at the _`ControllerFacade`_ class, which uses the services of other classes defined in and outside the package.

The definition of experiments is done with the aid of the _`ConfigurationParser`_ class, which provide methods to save and parse the test setups created by users.  Test setups make use of reusable connection settings, which specify how the framework must connect with the systems under test. These settings are stored in a separate connections file, and accessed through the _`ConnectionsFileParser`_ class.

Test execution is mediated via RMI, by calling the methods defined in the classes _`RemoteDaemonServerFunctions`_, _`DriverRemoteFunctions`_ and _`SinkRemoteFunctions`_. These methods allow, among others, to start, pause, and stop components (i.e., Driver and Sinks) running on a local or remote machine.

![![](http://fincos.googlecode.com/svn/imgs/model/package_controller.png)](http://fincos.googlecode.com/svn/imgs/model/package_controller.png)
.

### _Driver_ package ###
Encompasses all the classes involved in load generation.

The _`Driver`_ class represents a load generation component running at a given machine, and offer a set of methods for controlling/monitoring test execution.

A _driver_ can have one or more _`Sender`_ threads, depending on the degree of parallelism specified by the user. It is inside the _`Sender`_ class where load generation effectively happens, with events being created or read from data files (using the services of the _`DataGen`_ and _`DataFileReader`_ classes respectively), and then sent to the system under test. A _`Scheduler`_, ensures that events are submitted at the appropriate moment, accordingly with workload directives (i.e. input rate, arrival process, etc.).

![http://fincos.googlecode.com/svn/imgs/model/package_driver.png](http://fincos.googlecode.com/svn/imgs/model/package_driver.png)
.

### _Sink_ package ###
The _`Sink`_ class represents a component that processes the output produced by the systems under test.

![http://fincos.googlecode.com/svn/imgs/model/package_sink.png](http://fincos.googlecode.com/svn/imgs/model/package_sink.png)
.

### _Adapters_ package ###
Contains the classes responsible for mediating the exchange of events between the framework and the systems under test.

The package is divided in two sub-packages, _`CEP`_ and _`JMS`_, indicating the two types of communication currently supported by the framework: i) via custom adapters, using proprietary APIs offered by CEP vendors and ii) through standard JMS messages.

Developers and researchers wishing to extend FINCoS to support direct communication with a CEP engine of his preference must implement a custom adapter that inherits from the _`CEP_EngineInterface`_ abstract class.

![http://fincos.googlecode.com/svn/imgs/model/package_adapter.png](http://fincos.googlecode.com/svn/imgs/model/package_adapter.png)
.

### _Perfmon_ package ###
Contains the classes responsible for performance measurement.

The  _`PerformanceMonitor`_ application can display performance results both after test completion (using the services of the _`OfflinePerformanceValidator`_ class) and in real-time (by calling the methods exposed by the _`DriverRemoteFunctions`_ and _`SinkRemoteFunctions`_ interfaces).


![http://fincos.googlecode.com/svn/imgs/model/package_perfmon.png](http://fincos.googlecode.com/svn/imgs/model/package_perfmon.png)
.