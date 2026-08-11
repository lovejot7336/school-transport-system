# School Transport System — Prac1 Prototype

A console-based Java prototype for the Local Education Authority (LEA)
School Transport System case study, built for CMP7001 Prac1.

## Requirements coverage

| ITT requirement | Where it's implemented |
|---|---|
| 1. Import CSV pupil/parent data | `service.CsvImporter`, menu option "Import pupil & parent CSV data" |
| 2a. Admin — whole-system correction | `auth.Administrator`, `Action.CORRECT_ANY_DATA` |
| 2b. School — edit pupil/parent data | `auth.SchoolStaff`, school-scoped edits enforced in `TransportSystemFacade` |
| 2c. LEA — contracts + route reassignment | `auth.LEAOfficer`, `service.ContractService`, `service.RouteAssignmentService` |
| 3c. Parent — route/collection-point enquiry | `auth.ParentAccount`, pupil-scoped view enforced in the Facade |
| 3e. Multi-user locking mechanism | `pattern.strategy.LockingStrategy` / `InMemoryLockingStrategy` (justified in Javadoc) |
| 3f. Reporting per access level | `report.Report` hierarchy + `service.ReportService` |
| 4a. Separation of concerns | `model` / `auth` / `report` / `service` / `pattern.*` / `ui` packages |
| 4b. Design patterns (creational/structural/behavioural) | Factory Method (`pattern.factory`), Facade (`pattern.facade`), Strategy (`pattern.strategy`) |
| 4c. Polymorphism | `AccessControl` interface (4 role classes) and `Report` abstract class (3 report subclasses) |
| 4d. Console menu system | `ui.Main` |
| 4e. Exception handling (API + custom), with tests | `exception.*`, custom `test` framework in `src/test`, 32 tests including deliberately-failing paths |

## Requirements deliberately *not* built

Per the assessment brief, this is a design/prototype exercise — data does
**not** need to be persisted to a file or database and can be hard-coded.
Accordingly:

- All data is seeded in memory at startup (`ui.SeedData`) and lost on exit.
- The locking mechanism is in-memory (`InMemoryLockingStrategy`) — a
  deliberate, documented choice for a single-JVM prototype. See the
  Javadoc on that class for the full justification, and "Design
  Challenges" in the accompanying presentation for the known limitation
  (a distributed lock would be needed for a genuine multi-instance
  deployment).

## Project layout

```
school_transport_system/
├── src/main/java/com/lea/transport/
│   ├── model/      School, Pupil, ParentContact, BusRoute, BusContractor, Contract
│   ├── auth/        Role, Action, AccessControl + 4 role classes, UserAccount
│   ├── report/      Report (abstract) + AdminReport / SchoolReport / LEAReport
│   ├── exception/   TransportSystemException + 6 custom subtypes
│   ├── pattern/
│   │   ├── factory/    AccessControlFactory        (Factory Method — creational)
│   │   ├── facade/      TransportSystemFacade        (Facade — structural)
│   │   └── strategy/    LockingStrategy + InMemoryLockingStrategy (Strategy — behavioural)
│   ├── service/     DataRepository, UserAccountService, CsvImporter,
│   │                PupilRecordService, RouteAssignmentService,
│   │                ContractService, ReportService
│   └── ui/          Main (console menu), SeedData (demo data)
├── src/test/java/com/lea/transport/test/
│   ├── Test.java, Assertions.java, TestRunner.java   (lightweight hand-rolled test framework)
│   └── suite/       32 tests across 6 test classes
├── data/
│   └── pupils_september.csv   (sample CSV for the import use case, includes one deliberately bad row)
└── README.md
```

## Building

Requires JDK 17+ (built and tested against JDK 21).

```bash
cd school_transport_system
find src/main/java src/test/java -name "*.java" > sources.txt
javac -d out @sources.txt
```

## Running the console application

```bash
java -cp out com.lea.transport.ui.Main
```

Demo accounts (also printed on startup):

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Administrator |
| `oakfield.staff` | `school123` | School Staff (Oakfield High) |
| `lea.officer` | `lea123` | LEA Officer |
| `sarah.jenkins` | `parent123` | Parent (linked to pupil `P1`) |

Try, for example:

1. Log in as `oakfield.staff`, choose "Import pupil & parent CSV data",
   and enter `data/pupils_september.csv` — one row deliberately has an
   invalid address (`NA`) to show the import continuing past a bad row.
2. Log in as `admin`, choose "Correct any pupil record", edit pupil `P1`
   with a new address of `9 Mill Lane` — this moves them out of their
   current route's catchment and triggers the `<<extend>>` reassignment
   to Route `R2` automatically.
3. Log in as `sarah.jenkins` and view her linked pupil's route — then try
   logging in as `admin` and viewing pupil `P2`'s route as a Parent
   action to see the pupil-scoping check (not directly exposed in the
   menu, but exercised by `FacadeAuthorizationTests`).

## Running the tests

```bash
java -cp out com.lea.transport.test.TestRunner
```

This project has no internet access to Maven Central inside its build
environment, so rather than depending on JUnit, `src/test` contains a
small hand-rolled equivalent (`@Test` annotation, `Assertions` helper,
reflection-based `TestRunner`) that mirrors JUnit's discovery-and-report
mechanics closely enough to demonstrate the same testing discipline.

32 tests currently pass, across:

- `AuthorizationTests` — role polymorphism, the Factory Method, login success/failure
- `LockingTests` — the Strategy-pattern locking mechanism, **including a
  real two-thread concurrency test**, not just sequential calls
- `PupilRecordServiceTests` — edit flow, validate-before-lock ordering, locked-record rejection
- `RouteAssignmentServiceTests` — the `<<extend>>` reassignment behaviour
- `CsvImporterTests` — import success, partial failure, and the wrapped `IOException` path
- `FacadeAuthorizationTests` — end-to-end role/school/pupil scoping through the Facade

Several tests are **deliberately failing-path tests** — they pass when
the system correctly *throws* the exception it should (e.g. a second
user being rejected by a held lock, a Parent being denied an edit
action). This directly demonstrates the exception-handling behaviour
required by the brief, rather than only testing happy paths.
