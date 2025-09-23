# Assignment 2: Service Layer Architecture - Habit Tracking System

## Project Information

**Author:** Kenneth Kousen  
**License:** MIT License  
**Spring Boot Version:** 3.5.5  
**Java Version:** 21+  

## Overview

This project implements a complete service layer architecture for a Habit Tracking System using Spring Boot. It demonstrates proper separation of concerns with controller, service, and repository layers, following SOLID principles and best practices.

## Architecture

The application follows a three-layer architecture:

```
┌─────────────────────────┐
│   Controller Layer      │  (REST endpoints)
├─────────────────────────┤
│   Service Layer         │  (Business logic & validation)
├─────────────────────────┤
│   Repository Layer      │  (In-memory data storage)
└─────────────────────────┘
```

### Key Components

#### 1. Repository Layer
- **Generic Repository Interface** (`Repository<T, ID>`): Defines standard CRUD operations
- **HabitRepository Interface**: Extends the generic repository with domain-specific queries
- **InMemoryHabitRepository**: Thread-safe implementation using `ConcurrentHashMap`

#### 2. Service Layer
- **BaseService Abstract Class**: Provides common CRUD operations and validation framework
- **HabitService**: Concrete implementation with business logic and collection operations

#### 3. Controller Layer
- **HabitController**: RESTful endpoints for habit management

#### 4. Model
- **Habit Entity**: Domain model with id, name, description, frequency, streaks, lastCompleted, createdAt, and archived fields

## Features

### Core Functionality
- ✅ Full CRUD operations for habits
- ✅ Frequency-based filtering (DAILY/WEEKLY/CUSTOM)
- ✅ Name-based search with case-insensitive matching
- ✅ Streak tracking and completion logic
- ✅ Overdue query helpers
- ✅ Collection operations (grouping, counts)
- ✅ Thread-safe in-memory storage
- ✅ Input validation

### Collection Operations Demonstrated
- **Map**: Group habits by frequency or archived flag
- **List**: Maintain ordered habit collections
- **Stream API**: Filter, map, and collect operations
- **Defensive Copying**: Protect internal state

## API Endpoints (Habits)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/habits` | Get all habits |
| GET | `/api/habits/{id}` | Get habit by ID |
| GET | `/api/habits/archived/{archived}` | Get habits by archived flag |
| GET | `/api/habits/frequency/{frequency}` | Get habits by frequency |
| GET | `/api/habits/search?query={query}` | Search habits by name |
| GET | `/api/habits/streak/best/{minBestStreak}` | Get habits by best streak |
| GET | `/api/habits/streak/current/{minStreak}` | Get habits by current streak |
| GET | `/api/habits/completed/{date}` | Get habits completed on a date (YYYY-MM-DD) |
| GET | `/api/habits/overdue` | Get habits overdue for completion |
| GET | `/api/habits/created/today` | Get habits created today |
| POST | `/api/habits` | Create new habit |
| PUT | `/api/habits/{id}` | Update existing habit |
| DELETE | `/api/habits/{id}` | Delete habit |

## Testing

The project includes comprehensive test coverage:

### Test Suites
- **HabitRepositoryTest**: Repository layer unit tests
- **HabitServiceTest**: Service layer unit tests
- **HabitIntegrationTest**: Full integration tests with MockMvc

### Running Tests

```bash
# Run all tests
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Test Coverage
- Target: 80% code coverage across all layers
- JaCoCo plugin configured for automatic coverage reporting
- Coverage verification integrated with build process

## Team Collaboration Setup

### For Teams Working on This Assignment

This project is designed to be completed by teams. Follow these steps to set up your team's development environment:

#### Step 1: Team Lead - Fork the Repository
One team member should be designated as the repository owner:

1. Navigate to the original repository: https://github.com/kousen/assignment-2-service-layer
2. Click the "Fork" button in the top-right corner
3. Select your personal GitHub account
4. This creates your team's working repository

#### Step 2: All Team Members - Clone the Fork
Each team member should clone the team's forked repository:

```bash
# Replace [team-lead-username] with the GitHub username of whoever forked the repo
git clone https://github.com/[team-lead-username]/assignment-2-service-layer.git
cd assignment-2-service-layer
```

#### Step 3: Configure Upstream Remote
All team members should set up the original repository as the upstream remote to receive updates:

```bash
# Add the original repository as upstream
git remote add upstream https://github.com/kousen/assignment-2-service-layer.git

# Verify your remotes
git remote -v
# You should see:
# origin    https://github.com/[team-lead-username]/assignment-2-service-layer.git (fetch)
# origin    https://github.com/[team-lead-username]/assignment-2-service-layer.git (push)
# upstream  https://github.com/kousen/assignment-2-service-layer.git (fetch)
# upstream  https://github.com/kousen/assignment-2-service-layer.git (push)
```

#### Step 4: Sync with Upstream (When Updates are Available)
To get updates from the original repository:

```bash
# Fetch updates from upstream
git fetch upstream

# Merge upstream changes into your main branch
git checkout main
git merge upstream/main

# Push updates to your team's fork
git push origin main
```

#### Step 5: Team Workflow
1. **Create feature branches** for different parts of the assignment:
   ```bash
   git checkout -b feature/repository-layer
   git checkout -b feature/service-layer
   git checkout -b feature/controller-layer
   ```

2. **Push branches to your team's fork**:
   ```bash
   git push origin feature/your-branch-name
   ```

3. **Create Pull Requests** within your team's fork for code review

4. **Ensure all team members have commits** in the final submission

## Getting Started

### Prerequisites
- Java 21 or higher
- Gradle 8.x or higher
- Git
- GitHub account

### Installation (After Team Setup)

After completing the Team Collaboration Setup:

1. Build the project:
```bash
./gradlew build
```

2. Run the application:
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### Testing the API

Create a habit:
```bash
curl -X POST http://localhost:8080/api/habits \
  -H "Content-Type: application/json" \
  -d '{"name":"Exercise","description":"Daily workout","frequency":"DAILY","targetPerWeek":7}'
```

Get all habits:
```bash
curl http://localhost:8080/api/habits
```

Search by name:
```bash
curl "http://localhost:8080/api/habits/search?query=exer"
```

## Project Structure

```
assignment-2-service-layer/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── edu/trincoll/
│   │           ├── Assignment2Application.java
│   │           ├── model/
│   │           │   └── Habit.java
│   │           ├── repository/
│   │           │   ├── Repository.java
│   │           │   ├── HabitRepository.java
│   │           │   └── InMemoryHabitRepository.java
│   │           ├── service/
│   │           │   ├── BaseService.java
│   │           │   └── HabitService.java
│   │           └── controller/
│   │               └── HabitController.java
│   └── test/
│       └── java/
│           └── edu/trincoll/
│               ├── repository/
│               │   └── HabitRepositoryTest.java
│               ├── service/
│               │   └── HabitServiceTest.java
│               └── integration/
│                   └── HabitIntegrationTest.java
```

## Design Patterns Used

1. **Repository Pattern**: Abstracts data access logic
2. **Template Method Pattern**: BaseService provides template for CRUD operations
3. **Dependency Injection**: Constructor-based DI throughout
4. **Factory Pattern**: ID generation in repository

## SOLID Principles Applied

- **Single Responsibility**: Each layer has a distinct responsibility
- **Open/Closed**: BaseService is open for extension, closed for modification
- **Liskov Substitution**: ItemService can be substituted for BaseService
- **Interface Segregation**: Repository interfaces are focused and cohesive
- **Dependency Inversion**: Controllers depend on abstractions (services), not concrete implementations

## AI Collaboration

This project was developed with assistance from Claude AI for:
- Initial project structure setup
- Test case generation
- Documentation formatting

All code has been reviewed, tested, and validated to ensure correctness and adherence to best practices.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Kenneth Kousen

## Acknowledgments

- Trinity College Computer Science Department
- Spring Boot Documentation
- JUnit Testing Framework
