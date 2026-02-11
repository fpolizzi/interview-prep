# Interview Practice Repository

A collection of hands-on coding interview projects organized by language and interview format. Each project is a self-contained codebase with intentional issues, gaps, and areas for improvement — giving interviewers realistic scenarios to test candidates.

## How It Works

1. **Interviewer** picks a project and 1-2 tasks from its `INTERVIEW_TASKS.md`
2. **Candidate** clones the repo, explores the codebase (~10 min)
3. **Live coding** — candidate works through the assigned tasks while explaining their thought process

## Projects

| Language | Format | Project | Stack | Status |
|----------|--------|---------|-------|--------|
| Java | Pair Programming | [Bookstore API](java/pair-programming/bookstore-api/) | Spring Boot 4, Java 25, H2, JPA | Done |
| Java | Pair Programming | [Todo API](java/pair-programming/todo-api/) | Spring Boot 4, Java 25, H2, JPA | Done |
| Java | Pair Programming | [Delivery Food System](java/pair-programming/delivery-food-system/) | Spring Boot 3.4, Java 21, MongoDB, Kafka | Done |

## Structure

```
interviews/
├── java/
│   ├── pair-programming/
│   │   ├── bookstore-api/
│   │   ├── todo-api/
│   │   └── delivery-food-system/
│   ├── take-home/
│   └── system-design/
├── python/
│   ├── pair-programming/
│   └── take-home/
└── typescript/
    ├── pair-programming/
    └── take-home/
```

## Each Project Contains

- **README.md** — setup instructions, endpoints, architecture overview
- **INTERVIEW_TASKS.md** — tasks organized by level (Junior / Mid / Senior) with descriptions, acceptance criteria, hints, and time estimates
- **Working codebase** — with intentional issues baked in for candidates to find and fix

## Running a Project

Each project is self-contained. Navigate to its directory and follow the README:

```bash
cd java/pair-programming/bookstore-api
./mvnw spring-boot:run
```

## Tips for Interviewers

- Let candidates **explore the codebase for 10 minutes** before assigning tasks
- Pick **1-2 tasks** matching the candidate's level
- Encourage candidates to **talk through their approach** before coding
- For senior candidates, focus on **trade-off discussions** as much as implementation
- Each project has a reference implementation candidates can learn from
