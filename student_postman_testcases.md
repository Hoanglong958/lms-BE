# Student Access Postman Testcases

This file lists Postman testcases for endpoints that explicitly allow ROLE_USER (student) access.

## Setup

Base URL: `http://localhost:3900`

Environment variables (suggested):
- `base_url`
- `student_token`
- `student_id`
- `class_id`
- `course_id`
- `class_course_id`
- `session_id`
- `lesson_id`
- `doc_id`
- `video_id`
- `exercise_id`
- `quiz_id`
- `quiz_attempt_id`
- `quiz_result_id`
- `question_id`
- `exam_id`
- `exam_attempt_id`
- `roadmap_id`
- `period_id`

Common headers:
- `Authorization: Bearer {{student_token}}`
- `Content-Type: application/json`

Common negative cases (apply to all endpoints unless noted):
1. No `Authorization` header -> expect `401`.
2. Invalid/expired token -> expect `401`.
3. Token of a role that is NOT allowed by the controller -> expect `403`.

---

## Courses (CourseController)

### GET `/api/v1/courses`
Testcases:
1. Student token -> `200`, list of courses.
2. Missing token -> `401`.

### GET `/api/v1/courses/detail?id={{course_id}}`
Testcases:
1. Student token + valid `course_id` -> `200`, course detail.
2. Student token + non-existent `course_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/courses/paging?page=0&size=10&sort=createdAt,desc&q=`
Testcases:
1. Student token -> `200`, paged response.
2. Student token + invalid `sort` -> `400`.
3. Missing token -> `401`.

---

## Classes (ClassController)

### GET `/api/v1/classes`
Testcases:
1. Student token -> `200`, list of classes.
2. Missing token -> `401`.

### GET `/api/v1/classes/detail?id={{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`.
2. Student token + non-existent `class_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/classes/paging?page=0&size=10&sort=createdAt,desc&q=`
Testcases:
1. Student token -> `200`, paged response.
2. Student token + invalid `sort` -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/classes/stats?id={{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`.
2. Student token + non-existent `class_id` -> `400/404`.
3. Missing token -> `401`.

---

## Class Courses (ClassCourseController)

### GET `/api/v1/classes/courses?classId={{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`, list of courses in class.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

---

## Class Students (ClassStudentController)

### GET `/api/v1/classes/students?classId={{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`, list of students.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/classes/students/by-student?studentId={{student_id}}`
Testcases:
1. Student token + own `student_id` -> `200`, list of classes for that student.
2. Student token + different `student_id` -> `403` (access denied).
3. Missing token -> `401`.

---

## Class Teachers (ClassTeacherController)

### GET `/api/v1/classes/teachers?classId={{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`, list of teachers.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

---

## Attendance (AttendanceController)

### GET `/api/v1/classes/{{class_id}}/attendance-sessions`
Testcases:
1. Student token + valid `class_id` -> `200`, list of attendance sessions.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/classes/{{class_id}}/schedule-dates?year=2026&month=3`
Testcases:
1. Student token + valid params -> `200`, list of dates.
2. Student token + invalid month (e.g. 13) -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/classes/{{class_id}}/attendance?date=2026-03-01`
Testcases:
1. Student token + valid `date` -> `200`, attendance records.
2. Student token + invalid `date` -> `400`.
3. Missing token -> `401`.

---

## Schedules (ScheduleItemController)

### GET `/api/v1/schedules/course/{{course_id}}`
Testcases:
1. Student token + valid `course_id` -> `200`.
2. Student token + invalid `course_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/schedules/class/{{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/schedules/class-course/{{class_course_id}}/schedule`
Testcases:
1. Student token + valid `class_course_id` -> `200`.
2. Student token + invalid `class_course_id` -> `400/404`.
3. Missing token -> `401`.

---

## Sessions (SessionController)

### GET `/api/v1/sessions?courseId={{course_id}}`
Testcases:
1. Student token + valid `course_id` -> `200`, list of sessions.
2. Student token + invalid `course_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/sessions/detail?id={{session_id}}`
Testcases:
1. Student token + valid `session_id` -> `200`.
2. Student token + invalid `session_id` -> `400/404`.
3. Missing token -> `401`.

---

## Lessons (LessonController)

### GET `/api/v1/lessons?sessionId={{session_id}}`
Testcases:
1. Student token + valid `session_id` -> `200`, list of lessons.
2. Student token + invalid `session_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/lessons/detail?id={{lesson_id}}`
Testcases:
1. Student token + valid `lesson_id` -> `200`.
2. Student token + invalid `lesson_id` -> `400/404`.
3. Missing token -> `401`.

---

## Lesson Documents (LessonDocumentController)

### GET `/api/v1/lesson-documents?lessonId={{lesson_id}}`
Testcases:
1. Student token + valid `lesson_id` -> `200`, list of documents.
2. Student token + invalid `lesson_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/lesson-documents/detail?id={{doc_id}}`
Testcases:
1. Student token + valid `doc_id` -> `200`.
2. Student token + invalid `doc_id` -> `400/404`.
3. Missing token -> `401`.

---

## Lesson Quizzes (LessonQuizController)

### GET `/api/v1/lesson-quizzes?lessonId={{lesson_id}}`
Testcases:
1. Student token + valid `lesson_id` -> `200`, list of quizzes.
2. Student token + invalid `lesson_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/lesson-quizzes/detail?id={{quiz_id}}`
Testcases:
1. Student token + valid `quiz_id` -> `200`.
2. Student token + invalid `quiz_id` -> `400/404`.
3. Missing token -> `401`.

---

## Lesson Videos (LessonVideoController)

### GET `/api/v1/lesson-videos?lessonId={{lesson_id}}`
Testcases:
1. Student token + valid `lesson_id` -> `200`, list of videos.
2. Student token + invalid `lesson_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/lesson-videos/detail?id={{video_id}}`
Testcases:
1. Student token + valid `video_id` -> `200`.
2. Student token + invalid `video_id` -> `400/404`.
3. Missing token -> `401`.

---

## Session Exercises (SessionExerciseController)

### GET `/api/v1/session-exercises?sessionId={{session_id}}`
Testcases:
1. Student token + valid `session_id` -> `200`, list of exercises.
2. Student token + invalid `session_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/session-exercises/detail?id={{exercise_id}}`
Testcases:
1. Student token + valid `exercise_id` -> `200`.
2. Student token + invalid `exercise_id` -> `400/404`.
3. Missing token -> `401`.

---

## Periods (PeriodController)

### GET `/api/v1/periods`
Testcases:
1. Student token -> `200`, list of periods.
2. Missing token -> `401`.

---

## Roadmaps (RoadmapController)

### GET `/api/v1/roadmaps?classId={{class_id}}&courseId={{course_id}}`
Testcases:
1. Student token + valid params -> `200`.
2. Student token + invalid params -> `400/404`.
3. Missing token -> `401`.

---

## User Progress (UserProgressController)

### POST `/api/v1/user-progress/lessons`
Body example:
```json
{
  "userId": {{student_id}},
  "lessonId": {{lesson_id}},
  "sessionId": {{session_id}},
  "courseId": {{course_id}},
  "type": "video",
  "status": "IN_PROGRESS",
  "progressPercent": 50,
  "lastPosition": 120,
  "videoDuration": 300
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + missing required fields (e.g. `userId`) -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/user-progress/users/{{student_id}}/courses/{{course_id}}/lessons`
Testcases:
1. Student token + valid ids -> `200`.
2. Student token + invalid ids -> `400/404`.
3. Missing token -> `401`.

### POST `/api/v1/user-progress/roadmaps`
Body example:
```json
{
  "userId": {{student_id}},
  "roadmapId": {{roadmap_id}},
  "status": "IN_PROGRESS",
  "currentItemId": 101
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + missing required fields -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/user-progress/users/{{student_id}}/roadmaps/{{roadmap_id}}`
Testcases:
1. Student token + valid ids -> `200`.
2. Student token + invalid ids -> `400/404`.
3. Missing token -> `401`.

---

## Registrations (RegistrationController)

### POST `/api/v1/registrations`
Body example:
```json
{
  "courseId": {{course_id}},
  "note": "Dang ky khoa hoc"
}
```
Testcases:
1. Student token + valid `courseId` -> `201`.
2. Student token + invalid `courseId` -> `400/404`.
3. Missing token -> `401`.
4. Token of TEACHER/ADMIN -> `403`.

### GET `/api/v1/registrations/my`
Testcases:
1. Student token -> `200`, list of registrations for current student.
2. Missing token -> `401`.
3. Token of TEACHER/ADMIN -> `403`.

### GET `/api/v1/registrations/bank-info`
Testcases:
1. Student token -> `200`, bank info.
2. Missing token -> `401`.

---

## Exams (ExamController)

### GET `/api/v1/exams`
Testcases:
1. Student token -> `200`, list of exams.
2. Missing token -> `401`.

### GET `/api/v1/exams/detail?id={{exam_id}}`
Testcases:
1. Student token + valid `exam_id` -> `200`.
2. Student token + invalid `exam_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/exams/class/{{class_id}}`
Testcases:
1. Student token + valid `class_id` -> `200`.
2. Student token + invalid `class_id` -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/exams/course/{{course_id}}`
Testcases:
1. Student token + valid `course_id` -> `200`.
2. Student token + invalid `course_id` -> `400/404`.
3. Missing token -> `401`.

---

## Exam Questions (ExamQuestionController)

### GET `/api/v1/exam-questions/by-exam/{{exam_id}}`
Testcases:
1. Student token + valid `exam_id` -> `200`.
2. Student token + invalid `exam_id` -> `400/404`.
3. Missing token -> `401`.

---

## Questions (QuestionController)

### GET `/api/v1/questions/page?page=0&size=10&keyword=&category=`
Testcases:
1. Student token -> `200`, paged questions.
2. Student token + invalid `page/size` -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/questions/categories?page=0&size=10`
Testcases:
1. Student token -> `200`, list of categories.
2. Missing token -> `401`.

### GET `/api/v1/questions/detail?id={{question_id}}`
Testcases:
1. Student token + valid `question_id` -> `200`.
2. Student token + invalid `question_id` -> `400/404`.
3. Missing token -> `401`.

---

## Exam Attempts (ExamAttemptController)

### POST `/api/v1/exam-attempts/start`
Body example:
```json
{
  "examId": {{exam_id}},
  "userId": {{student_id}}
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + missing `examId` -> `400`.
3. Missing token -> `401`.

### POST `/api/v1/exam-attempts/{{exam_attempt_id}}/submit`
Body example:
```json
{
  "answers": [
    { "questionId": 1, "answer": "A" },
    { "questionId": 2, "answer": "C" }
  ]
}
```
Testcases:
1. Student token + valid attempt -> `200`.
2. Student token + attempt not found -> `404`.
3. Missing token -> `401`.

### GET `/api/v1/exam-attempts/detail?id={{exam_attempt_id}}`
Testcases:
1. Student token + own attempt -> `200`.
2. Student token + other user attempt -> `403`.
3. Missing token -> `401`.

### GET `/api/v1/exam-attempts?userId={{student_id}}`
Testcases:
1. Student token + any `userId` -> `200`, server should return current student's attempts.
2. Missing token -> `401`.

### GET `/api/v1/exam-attempts?examId={{exam_id}}&userId={{student_id}}`
Testcases:
1. Student token + valid ids -> `200` (filtered by current student).
2. Missing token -> `401`.

---

## Exam Participants (ExamParticipantController)

### POST `/api/v1/exam-participants/join?examId={{exam_id}}&userId={{student_id}}`
Testcases:
1. Student token + valid ids -> `200`.
2. Student token + invalid examId -> `400/404`.
3. Missing token -> `401`.

### POST `/api/v1/exam-participants/submit?examId={{exam_id}}&userId={{student_id}}`
Testcases:
1. Student token + valid ids -> `200`.
2. Student token + invalid examId -> `400/404`.
3. Missing token -> `401`.

---

## Exam Answers (ExamAnswerController)

### GET `/api/v1/exam-answers/my/{{exam_attempt_id}}`
Testcases:
1. Student token + own attempt -> `200`.
2. Student token + other user's attempt -> `403` (expected if service enforces ownership).
3. Missing token -> `401`.

---

## Quiz Questions (QuizQuestionController)

### GET `/api/v1/quiz-questions/by-quiz/{{quiz_id}}`
Testcases:
1. Student token + valid `quiz_id` -> `200`.
2. Student token + invalid `quiz_id` -> `400/404`.
3. Missing token -> `401`.

---

## Quiz Attempts (QuizAttemptController)

### POST `/api/v1/quiz-attempts/start`
Body example:
```json
{
  "quizId": {{quiz_id}},
  "userId": {{student_id}}
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + missing `quizId` -> `400`.
3. Missing token -> `401`.

### POST `/api/v1/quiz-attempts/{{quiz_attempt_id}}/submit`
Body example:
```json
{
  "score": 85.5,
  "correctCount": 17,
  "totalCount": 20,
  "passed": true
}
```
Testcases:
1. Student token + valid attempt -> `200`.
2. Student token + attempt not found -> `404`.
3. Missing token -> `401`.

### GET `/api/v1/quiz-attempts/{{quiz_attempt_id}}`
Testcases:
1. Student token + valid attempt -> `200`.
2. Student token + invalid attempt -> `404`.
3. Missing token -> `401`.

### GET `/api/v1/quiz-attempts/by-user/{{student_id}}`
Testcases:
1. Student token + valid user -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/quiz-attempts/by-quiz/{{quiz_id}}`
Testcases:
1. Student token + valid quiz -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/quiz-attempts/by-user/{{student_id}}/quiz/{{quiz_id}}`
Testcases:
1. Student token + valid ids -> `200`.
2. Missing token -> `401`.

### POST `/api/v1/quiz-attempts/{{quiz_attempt_id}}/attachments` (multipart/form-data)
Form-data:
- `file`: choose a small file (e.g. pdf/png)
Testcases:
1. Student token + valid file -> `200`, returns file URL.
2. Student token + invalid file type/size -> `400`.
3. Missing token -> `401`.

---

## Quiz Results (QuizResultController)

### GET `/api/v1/quiz-results`
Testcases:
1. Student token -> `200`, only own results returned.
2. Missing token -> `401`.

### GET `/api/v1/quiz-results/detail?id={{quiz_result_id}}`
Testcases:
1. Student token + valid id -> `200`.
2. Student token + invalid id -> `400/404`.
3. Missing token -> `401`.

### POST `/api/v1/quiz-results/submit`
Body example:
```json
{
  "quizId": {{quiz_id}},
  "userId": {{student_id}},
  "answers": [
    { "questionId": 1, "answer": "A" },
    { "questionId": 2, "answer": "C" }
  ]
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + missing `answers` -> `400`.
3. Missing token -> `401`.
4. Token of TEACHER (not allowed) -> `403`.

---

## Dashboard (DashboardController)

### GET `/api/v1/dashboard`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/dashboard/new-users`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/dashboard/new-courses`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/dashboard/recent-quizzes`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

### GET `/api/v1/dashboard/course-progress/{{course_id}}`
Testcases:
1. Student token + valid course -> `200`.
2. Student token + invalid course -> `400/404`.
3. Missing token -> `401`.

### GET `/api/v1/dashboard/recent-exams`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

---

## AI (OllamaController)

### POST `/api/v1/ai/chat`
Body example:
```json
{
  "type": "QA",
  "question": "Bai hoc nay noi ve gi?",
  "lessonId": {{lesson_id}}
}
```
Testcases:
1. Student token + valid body -> `200`.
2. Student token + empty `question` -> `400`.
3. Missing token -> `401`.

### GET `/api/v1/ai/health`
Testcases:
1. Student token -> `200`.
2. Missing token -> `401`.

---

## Notes
- Expected error codes may vary between `400` and `404` depending on service implementation.
- For endpoints that enforce ownership (e.g., exam attempts, class list by student), include a negative case with a different `student_id` to verify `403`.
- Replace all `{{...}}` placeholders with real IDs from your database.
