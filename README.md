A simple yet powerful Quiz Web Application built with Spring Boot + Thymeleaf + Bootstrap — no database required.
Quizzes are dynamically loaded from .json or .csv files located in the resources/quizzes/ folder.

 Features

 Load quizzes dynamically from .json and .csv files.

 Countdown timer (10 minutes total).

 Navigate between questions (Previous / Next).

 Automatic quiz submission when time runs out.

 Results page with score, progress bar, and color-coded answers.

 Retake quiz or return to quiz list.

 No database required — all quizzes live in resource files.

 Styled using Bootstrap 5.

 How It Works
 1. Quiz Discovery

At startup or when you visit /quizzes,
the app scans the classpath folder:

src/main/resources/quizzes/


and detects all files ending with .json or .csv.

Each detected file becomes a “quiz topic” in the quiz list.

Example:
computer_science.csv → displayed as “Computer science”

 2. Quiz Loading

Depending on the file extension, the appropriate loader is used:

Loader	File Type	Class
JsonQuizLoader	.json	Parses using Jackson
CsvQuizLoader	.csv	Parses using Apache Commons CSV

Both loaders produce a List<Question>.

 3. Quiz Session

When you start a quiz (/quiz/{topic}/0):

The QuizSession object is created or reset.

The app tracks your:

current quiz topic

questions list

answers list

start time and duration (for timer)

All of this is kept in your HTTP session (no DB required).

 4. Navigation

Each question page shows:

The current question text and 4 options.

“Previous” and “Next” buttons to navigate.

“Submit” button on the last question.

Remaining time (countdown).

 5. Timer

The timer is handled in the frontend (JavaScript) and backed by session attributes:

quizStartTime

quizDurationMillis

If time runs out:

The JS script automatically submits the form.

The controller detects no action and redirects to /submit?timeout=true.

The quiz is graded automatically.

 6. Results

The results page shows:

Total score and percentage.

A green progress bar indicating performance.

Each question, with:

 Green background for correct answer.

 Red background for user’s wrong answer.

“Retake Quiz” and “Back to Quizzes” buttons.

A warning alert if the quiz timed out.

Quiz File Formats

To add quiz file .json or .csv save the file in src\main\resources\quizzes
JSON Format Example

[
  {
    "text": "What does CPU stand for?",
    "options": ["Central Process Unit", "Central Processing Unit", "Computer Personal Unit", "Central Peripheral Unit"],
    "correctIndex": 1
  },
  {
    "text": "Which data structure uses FIFO?",
    "options": ["Stack", "Queue", "Tree", "Graph"],
    "correctIndex": 1
  }
]

CSV Format Example

question,option1,option2,option3,option4,correctIndex
What does CPU stand for?,Central Process Unit,Central Processing Unit,Computer Personal Unit,Central Peripheral Unit,1
Which data structure uses FIFO?,Stack,Queue,Tree,Graph,1


The first line must be the header:

question,option1,option2,option3,option4,correctIndex


Each subsequent line defines one question.

correctIndex is zero-based (e.g., 0 = first option).

How to run:

Prerequisites
Java 21+
Maven 3.8+
Git (optional)

git clone https://github.com/yourusername/quiz-app.git
cd quiz-app
mvn spring-boot:run

In the browser:
http://localhost:8080

Key Endpoints

/	-                      Home page

/quizzes -      	       List available quizzes

/quiz/{topic}/{index} -  View question at index

/submit -	               Submit current quiz

/result -	               Display results

/quiz/load/{topic} -	   (AJAX) Returns quiz data as JSON

