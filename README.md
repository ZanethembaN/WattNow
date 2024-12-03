
### WattNow

WattNow is a distributed loadshedding notifier system inspired by the Eskom loadshedding website. It offers a modular, scalable architecture for tracking stages, schedules, and locations impacted by loadshedding.


### Features

**Stage Module**: Provides the current loadshedding stage.
**Schedule Module**: Fetches and delivers schedules for specific areas.
**Places Module**: Includes a PlaceNameService that retrieves area details from a CSV file.
**Web Module**: Serves as the central hub, hosting the server and starting all other services.


### System Architecture
WattNow is a distributed system, with each module functioning independently and communicating seamlessly to deliver accurate loadshedding information.


### Modules Overview

# Stage Module
**StageService**: Retrieves and provides the current loadshedding stage.

# Schedule Module
**ScheduleService**: Supplies the loadshedding schedule for a given area.

# Places Module
**PlaceNameService**: Reads area data from a CSV file to map places effectively.

# Web Module
**WebService**: Acts as the entry point of the application, starting all the other services.


### Getting Started

Prerequisites
Ensure you have the following installed:

**Java**: Download Java

## Installation
Clone the repository:

```bash
Copy code
git clone https://github.com/your-username/wattnow.git
Navigate to the web module directory:

bash
Copy code
cd wattnow/web
Start the web server:

bash
Copy code
java -jar webservice.jar
The webservice will automatically initialize all other services.

Access the application via your browser or API client at http://localhost:8080.

How It Works
Starting the Application

The webservice in the web module acts as the central orchestrator, bootstrapping all required modules (stage, schedule, places).
Stage Module

Provides the current loadshedding stage (e.g., Stage 2, Stage 4).
Schedule Module

Retrieves the schedule for specific areas based on the current stage.
Places Module

Uses PlaceNameService to fetch area names and related data from a CSV file.
Example Usage
Get Current Stage:

http
Copy code
GET /api/stage
Response: {"stage": "Stage 2"}
Get Schedule for a Place:

http
Copy code
GET /api/schedule?place=YourArea
Response: {"place": "YourArea", "schedule": ["08:00-10:00", "16:00-18:00"]}
Get Available Places:

http
Copy code
GET /api/places
Response: ["Area1", "Area2", "Area3"]
Contributing
Contributions are welcome! Please fork the repository and create a pull request for review.
