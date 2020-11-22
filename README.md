# Rentals-Spring-Boot-Microservices
College OLX Flutter Application's backend microservices using Spring boot. **README UNDER PROCESS**

## Overview
<p align= "center">
<img width="600" height="400" src="Pictures/SchematicDiagram.png"><br>
</p>
The following services have been implemented which handle queries related to them:
<ul>
   <li> Users Service</li>
   <li> Products Service </li>
  <li> Colleges Service </li>
  <li> Orders Service </li>
</ul>   
For service discovery mechanism, additional "Discovery Server" has also been implemented.
   

### Service initiation
[Spring Initializer](https://start.spring.io) has been used to create spring applications of the above mentioned services(except Discovery Server) with the following dependencies:
<ul>
   <li> Spring Web </li>
   <li> Spring Data MongoDB</li>
   <li> Eureka Discovery Client</li>
</ul>
For Discovery server only one dependency of "Eureka Server" has been used.

