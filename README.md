#### Simple Money Transfer Example Using Kotlin

###### Frameworks Used
* Kotlin
* Ktor (Framework for building asynchronous servers and clients in connected systems using kotlin)
* Koin (Dependency Injection)
* jetbrains exposed
* H2 db as inmemory data storage
* uchuhimo (for Config)
* Flyway (For database migration)

###### Data Model Concept
The implementation show only the transfer part out of the huge process , ignoring any other
before and after processing<br>

Portfolio is the wallet that is owned by user/s(can be shared between multiple users, but has only one registered owner)
and it's related to the customer by a contract(business partner)
portfolio can have one or many positions (1->m relationship)

<br/><u>Entities represented in the sample:</u><br/>

Position: <br/>
It represents the assets information including asset type , portfolio , account type(ex: money account)
current balance ...etc
Ideally , position/asset type should have it's own entities, but for simplicity and this point is out of the task scope
i decided to implement them as enums

Transfer:<br/>
 The transfer in our case is being implemented in direct way , in real world , it should be stored and triggered (cron job for example)
 in certain time frame , or waiting response from other service and then update the balances
 

###### How to start?
* Please Generate self contained Jar (**gradle clean build** is good option :D )
* Start the server by java -jar build/libs/Revolute.jar
* Use Curl or Insomnia (it's cool) to trigger your calls
* Code is covered by Tests (Please approach me for clarifications)-(hit **./gradlew test** will not bite :D)
* Hint , Use the following Endpoints to navigate (Don't worry you are almost covered)

      GET   "/api/positions/{id}"        -> Get position by id
      GET   "/api/positions"             -> Get All positions/you can decided if u want blocked/locked positions too
      POST  "/api/positions"             -> Create new Position
      PATCH "/api/positions/block/{id}"  -> block Position
      PATCH "/api/positions/unblock/{id}"-> unblock Position
      POST  "/api/transfer"              -> Make transfer
      GET   "/api/transfer/{id}"         -> Get transfer






