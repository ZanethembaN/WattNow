
# WattNow

WattNow is a distributed loadshedding notifier system inspired by the Eskom loadshedding website. It offers a modular, scalable architecture for tracking stages, schedules, and locations impacted by loadshedding.


# Features

**Stage Module**: Provides the current loadshedding stage.
**Schedule Module**: Fetches and delivers schedules for specific areas.
**Places Module**: Includes a PlaceNameService that retrieves area details from a CSV file.
**Web Module**: Serves as the central hub, hosting the server and starting all other services.


# System Architecture
WattNow is a distributed system, with each module functioning independently and communicating seamlessly to deliver accurate loadshedding information.


## Modules Overview

### Stage Module
**StageService**: Retrieves and provides the current loadshedding stage.

#### Schedule Module
**ScheduleService**: Supplies the loadshedding schedule for a given area.

### Places Module
**PlaceNameService**: Reads area data from a CSV file to map places effectively.

### Web Module
**WebService**: Acts as the entry point of the application, starting all the other services.


### Getting Started

Prerequisites
Ensure you have the following installed:

**Java**: Download Java

## Installation
Clone the repository:

```bash
git clone https://github.com/your-username/wattnow.git

cd wattnow/web/src/main/java/wethinkcode/web

java -jar webservice.jar

Access the application via your browser or API client at http://localhost:5050.

