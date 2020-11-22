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
Inter-service communication happens through Eureka Server so an additional *Discovery Server* has also been implemented. The schematic diagram above represents how *Orders Service* communicates with *Products service* through Eureka Server. Following are the steps:

<ul>
   <li> At their startup, each of the four services register themselves with the Discovery Server </li>
   <li> When one service requires to communicate with the other, it looks up the Discovery Server for the target service's address </li>
   <li> The Discovery Server returns the address of the required service in a load-balanced way if more than one instances of the target service is active </li>
   <li> Request is now made to the target service from the client service </li>
</ul>

This type of service discovery is known as client-side service discovery as client is the one doing all the work!
   

## Service initiation
[Spring Initializer](https://start.spring.io) has been used to create spring applications of the above mentioned services(except Discovery Server) with the following dependencies:
<ul>
   <li> Spring Web </li>
   <li> Spring Data MongoDB</li>
   <li> Eureka Discovery Client</li>
</ul>

For Discovery server only one dependency of "Eureka Server" has been used.

### Discovery Server
Eureka servers have a tendency of registering themselves to enable communication between multiple Eureka servers. But since we only have one discovery server, we don't need it to register itself. So add the following lines to *application.properties* file:
```
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```
Next, add the following annotation the DiscoveryServerApplication class in *DiscoveryServerApplication.java* file to mark the application as an Eureka Server:
```
@EnableEurekaServer
```
Now Run the application. By default it will run in port 8761, which can be altered by specifying the *server.port* property.

### User, College, Order and Product Services
Specify the following properties for each of the four services in their *application.properties* file:
```
server.port=port_number_for_each_service
spring.data.mongodb.database=db_name_for_each_service
spring.data.mongodb.port=27017
spring.application.name=service-name
```
Add the Eureka Client Annotation to the application's main java class:
```
@EnableEurekaClient
```
After doing so for all the four services, run them and check Eureka Server's url (by default in port 8761) to see if your services have been registered.
**Note:** Make sure MongoDB is active in your machine before running the above services.

## MongoDB integration
Create a model class to specify the structure of objects to be stored in the database. Orders service has the following model class:
```
public class Order {
    @Id
    private String id;

    private String sellerId;
    private String renterId;
    private String productId;
    private String status;
    private String userStatus;
    private long timestamp;

    public Order() { }

    public Order(String sellerId, String renterId, String productId, String status, String userStatus, long timestamp) {
        this.sellerId = sellerId;
        this.renterId = renterId;
        this.productId = productId;
        this.status = status;
        this.userStatus = userStatus;
        this.timestamp = timestamp;
    }
    //INCLUDES GETTERS AND SETTERS...
 }
```
**Note**: The empty constructor is for RestTemplate.<br>
Create a repository file to act as an interface between your application and the mongoDB database. The following *OrderRespository* class extends the *MongoRepository* interface and so its methods like *findById* can be called using an object of *OrderRepository*.
```
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByRenterIdAndStatus(String renterId, String status, Sort sort);
    List<Order> findBySellerIdAndStatus(String sellerId, String status, Sort sort);
    List<Order> findByProductIdAndStatus(String productId, String status);
}
```
**Spring manages the implementations of the above methods (Cool, right!?)**

## BASIC APIs
Create a REST controller class which tells the application where to route http requests. Following is the controller class for *Orders Service*:
```
@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping(value = "/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Order> createOrder(Order order) {
        //IMPLEMENTATION...
    }
 }
```
All the requests made to *Orders Service* with */order* suffix in the URL are mapped to methods of this class. So, a POST request to *http://localhost:service_port/order/create* are routed to the *createOrder* function. *Autowired* annotation makes sure that only one object of the OrderRepository class is formed. 
