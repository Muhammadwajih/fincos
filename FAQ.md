### FAQ ###



.

## 1. What is FINCoS? What is the history and motivation behind the framework? ##
The FINCoS framework is a set of benchmarking tools for load generation and performance measurement of event processing platforms. Its main purpose is to offer a starting point to end-users, researchers and engineers who desire to carry out independent performance evaluations on Complex Event Processing (CEP) systems.

FINCoS development started in the end of 2007, as part of the BiCEP benchmarking initiative, at University of Coimbra, that aims at studying and improving the performance and scalability of event processing platforms. At that time, there was very little information on how the CEP technology was being used in the real world and a great diversity of products, each with their own languages and implementation styles, which posed serious challenges for the development of novel benchmarks. Our goal then was to provide a flexible tool that allowed carrying out performance evaluation on CEP platforms independently on their structural differences or the workload employed.

## 2. What are the system requirements? ##
FINCoS is Java-based, so all that is required to use the framework is to have a JVM installed on the test machines (Java 6 or later).

## 3. Which CEP engines are supported? ##
FINCoS can be used to evaluate any CEP platform capable of exchanging events through the standard JMS API. In addition, the framework also supports direct communication with event processing platforms via an extensible set of vendor-specific adapters. Currently FINCoS is distributed with one such adapter for the open-source engine Esper.

## 4. How to support additional CEP products? ##
If the CEP engine of your choice does not support JMS, providing instead client libraries to connect directly with the platform, you can develop a custom adapter by extending the class `CEP_EngineInterface`.

## 5. How do I run performance tests? ##
To run performance tests, users must define a test setup, with at least one _Driver_ and one _Sink_, using the FINCoS _Controller_ application. Drivers are the load generation component of the framework while Sinks are the components that process the results produced by the CEP platform under test. Configuring a Driver involves informing workload parameters such as event input rate, test duration, event schemas, etc. Configuring a Sink requires only informing which streams the framework must subscribe to. You can find detailed instructions on how to create and execute performance tests at FINCoS User Guide.

## 6. Can I use my own event trace in the performance tests? ##
Yes, FINCoS allows users to run performance tests using their own datasets as input.

## 7. Can I run performance tests using multiple machines? ##
Yes, FINCoS allows distributing load generation and result processing across multiple nodes. Users are free to define as many Drivers and Sinks as they want.

## 8. How do I see the results of a performance test? ##
Users can visualize the results of performance runs both in real-time and after test completion using the _Performance Monitor_ component.

## 9. Does FINCoS include any “ready-to-run” benchmark? ##
Today FINCoS is distributed only with simple test setup scenarios, intended to familiarize users with the framework. We are currently working on more complex, representative, application benchmarks for the CEP domain and we plan to include the files required to execute them at FINCoS zip-file.

## 10. How does FINCoS relate to similar measurement tools and benchmark kits (e.g., Apache JMeter, SPECjms2007, etc.)? ##
FINCoS is targeted at CEP platforms. If you need to evaluate the performance of JMS-compliant message-oriented middlewares, you might consider using Apache JMeter or the SPECjms2007 benchmark kit. Although FINCoS can be used to submit load to a JMS server, the two aforementioned tools shall be more appropriate for that task.

## 11. The Esper CEP engine is bundled with a performance kit. Do I need FINCoS? ##
The Esper kit offers a good idea on the performance you can expect from the engine for simple queries like filters and moving averages. You might, however, want to exercise different operations (e.g., joins, pattern detection, real applications, etc.) or compare the performance of Esper with other engines under equivalent workload conditions. For these cases, FINCoS might be of great help.