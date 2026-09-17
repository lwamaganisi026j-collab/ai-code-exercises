# AI for Everyday Work — Completed Exercises

## Submission note

I worked through these exercises using the Task Manager projects and the other Java/JavaScript exercises in this repository. I have written the reflections in my own voice rather than copying the exercise wording. Where I describe an AI prompt, I have paraphrased it into the kind of question I would naturally type while working on the problem.

I mainly used Java and JavaScript because those are the languages I have been working with in this repository. The Task Manager Java project is organised around `TaskManager`, `Task`, `TaskPriority`, `TaskStatus`, and `TaskStorage`. The JavaScript version also contains separate modules for task priority scoring, text parsing, and merging task lists. Those structures gave me enough real code to work through the comprehension and algorithm exercises instead of inventing an unrelated project.

---

# 1. Knowing Where to Start — Codebase Comprehension

## Step 1 — Understanding the project structure

### My first impression

At first I expected the Task Manager to be one large file because it is a small application. After looking at the names, I realised it was separated into different responsibilities. In the Java version there is an application layer, a model layer, and a storage layer. The model contains the task itself plus the status and priority enums, while storage is responsible for keeping tasks and saving/loading them. The application class connects those pieces and provides operations such as creating, listing, updating and deleting tasks.

The Java project also uses Gradle for building and testing. The source tree made more sense once I stopped thinking of every Java file as a separate application and started seeing the folders as layers.

### What I asked AI, in my own words

> I have looked through this Task Manager project and this is what I currently think each folder and class is responsible for. Please check my understanding against the actual structure. Point out anything I have misunderstood, but explain it in practical terms rather than just describing every file.

### What I learned

The biggest correction was that `Task` is mainly the domain object, while `TaskManager` coordinates operations and `TaskStorage` handles persistence. `TaskPriority` and `TaskStatus` are not managers themselves; they define controlled values that other parts of the application use.

The repository structure confirms those separate responsibilities: the Java project has `TaskManager.java`, `Task.java`, `TaskPriority.java`, `TaskStatus.java`, and `TaskStorage.java`. fileciteturn161file0

### My final understanding

The application follows a fairly straightforward flow:

`CLI/application logic → TaskManager → Task/TaskStatus/TaskPriority → TaskStorage → JSON file`

The most useful lesson was to look at the structure before reading every line. The folder names already tell me a lot about where a future change is likely to belong.

---

## Step 2 — Finding where a feature lives

### Feature: Task Export to CSV

Before asking AI, I would search for the task retrieval and storage code. I would expect the feature to need access to the task list, but I would not immediately put CSV code into the `Task` model.

The Java code shows that `TaskStorage` already provides methods such as `getAllTasks()`, `getTasksByStatus()`, `getTasksByPriority()`, and `getOverdueTasks()`. fileciteturn167file0

### My AI question

> I need to add a CSV export feature to this Task Manager. I have already found the classes that create and retrieve tasks. Based on the current responsibilities, help me work out where the export logic should live. I want a small map of the classes I would probably touch and why. Please don't write the feature for me yet.

### My plan

I would keep CSV formatting out of `Task`. The task model should represent task data, not know about CSV files. I would probably add an export/service component or a dedicated method close to the application/storage boundary. `TaskManager` could coordinate the request, while a separate exporter would turn a list of `Task` objects into CSV.

That approach means that if JSON, CSV and another format are needed later, the task model does not become filled with unrelated formatting code.

---

## Step 3 — Understanding the domain model

### My initial model

The main entity is `Task`. A task has an ID, title, description, priority, status, dates and tags. `TaskPriority` controls four priority levels: LOW, MEDIUM, HIGH and URGENT. `TaskStatus` controls TODO, IN_PROGRESS, REVIEW and DONE. fileciteturn162file0 fileciteturn163file0 fileciteturn164file0

### My AI question

> I think I understand the main Task Manager entities. Instead of just explaining them to me, ask me questions that would expose gaps in my understanding. Start with how Task relates to status, priority, dates and storage. I'll answer from the code.

### Questions I worked through

- What happens to a new task's status? → It starts as `TODO`.
- What happens when a task is completed? → `markAsDone()` changes the status and records the completion time.
- What makes a task overdue? → It needs a due date in the past and must not already be DONE.
- Where is priority represented? → `TaskPriority` is an enum with numeric values.
- Where is the task actually stored? → `TaskStorage` keeps tasks in memory and persists them using JSON/Gson.

The `Task` class confirms that construction starts a task as TODO, records creation/update times and keeps tags as a separate list. fileciteturn162file0

### My glossary

| Term | My meaning |
|---|---|
| Task | The main piece of work being tracked. |
| Priority | How important/urgent the task is relative to other tasks. |
| Status | Where the task currently is in its lifecycle. |
| Due date | The point in time by which the task is expected to be finished. |
| Overdue | A task whose due date has passed and which is not DONE. |
| Tag | A label attached to a task for extra categorisation. |
| Storage | The part responsible for loading, saving and retrieving tasks. |

---

## Step 4 — Applying the business rule

### Rule

“Tasks overdue by more than 7 days should automatically be marked as abandoned unless they are high priority.”

### My implementation plan

No code yet. I would first add an `ABANDONED` value to `TaskStatus`, because the current status enum does not contain it. fileciteturn164file0

Then I would decide where the automatic rule belongs. I would avoid putting the whole rule inside `Task.isOverdue()` because that method currently answers a simple question: whether the task is overdue. The new rule is a business action, not just a check.

I would probably add a method in the application/service layer that:

1. Gets all tasks from storage.
2. Ignores tasks that have no due date.
3. Calculates how many days have passed since the due date.
4. Ignores tasks that are not more than seven days overdue.
5. Ignores HIGH and URGENT tasks if the business rule means “high priority or above”.
6. Changes the remaining eligible tasks to ABANDONED.
7. Saves the changes.
8. Adds tests for exactly seven days, more than seven days, high priority and already-completed tasks.

### What I would check first

I would clarify whether “high priority” means only HIGH or HIGH plus URGENT. I would also clarify whether the rule should run when the application starts, when tasks are listed, or through a scheduled process. Those details affect where the code belongs.

---

# 2. Code Exploration Challenge

## Step 1 — Looking without reading code

From the JavaScript Task Manager folder names alone, I expected to see a CLI, models, storage and separate modules for more complicated behaviour. That turned out to be accurate. The project contains `app.js`, `cli.js`, `models.js`, `storage.js`, `task_priority.js`, `task_parser.js` and `task_list_merge.js`, along with separate test files. fileciteturn169file0

My first guess was that the CLI would mainly collect input and hand it to the application logic, while the model and storage files would contain the core data and persistence behaviour.

## Step 2 — How are tasks created?

The Java implementation confirms that task creation starts in `TaskManager.createTask()`. It converts the numeric priority into a `TaskPriority`, parses an optional date, creates a `Task`, and then sends it to storage. fileciteturn166file0

### My natural AI prompt

> I found the method that creates a task. Walk me through one example from the moment a user asks to create a task until the task is stored. Follow the actual method calls and data changes. If something isn't obvious from the snippet, tell me instead of guessing.

### What I learned

The important flow is:

`createTask input → priority/date conversion → new Task → TaskStorage.addTask() → save()`

This helped me see that “creating a task” is not one action inside one class. It is a small chain of responsibilities.

## Step 3 — How does priority work?

The Java priority enum has four values with numeric weights: LOW=1, MEDIUM=2, HIGH=3 and URGENT=4. fileciteturn163file0

The JavaScript version goes further by using those priority values when calculating a task score. It starts with a priority-based score, then adjusts it for due dates, status, special tags and how recently the task was updated. fileciteturn170file0

### My AI prompt

> I think priority is basically just a 1-to-4 value, but I suspect it affects more than display. Ask me a few questions based on this code so I can prove whether I understand how priority affects the rest of the system.

### What I corrected

Initially I thought priority was simply a label. The scoring code showed me that priority is also an input to ranking. A task with higher priority gets a larger starting score before other factors are added or subtracted.

## Step 4 — What happens when a task is completed?

In the Java model, `markAsDone()` changes the status to DONE, records `completedAt`, and updates the timestamp. `TaskManager.updateTaskStatus()` also calls this method when the requested status is DONE and then saves the storage. fileciteturn162file0 fileciteturn166file0

### My simple diagram

`User marks task done`

`↓`

`TaskManager.updateTaskStatus()`

`↓`

`Task.setStatus(DONE)` / `Task.markAsDone()`

`↓`

`completedAt + updatedAt recorded`

`↓`

`TaskStorage.save()`

`↓`

`Task is persisted`

---

# 3. Understanding a Complex Algorithm

## Chosen algorithm: Task Priority Sorting

I chose the priority scoring/sorting algorithm because it is directly available in the JavaScript Task Manager.

### Step 1 — My first guess

I expected the function to simply sort tasks by priority. After reading it, I realised it was more involved. It calculates a score using priority, due date, status, tags and recent updates, then sorts by that score.

### Step 2 — My AI prompt

> I think this function is trying to rank tasks by importance, but I don't want a line-by-line explanation first. Take a small example with three tasks and walk me through the score each task receives. Show where priority, due date, status and tags change the result.

### Step 3 — What became clearer

The base priority contributes 10 points per priority level. Due dates can add 10, 15, 20 or 30 points depending on urgency. DONE tasks lose 50 points and REVIEW tasks lose 15. Special tags add 8, and a task updated less than a day ago receives another 5 points. fileciteturn170file0

That means the final ranking is not simply “URGENT first”. A lower-priority task can potentially move higher if its due date and other factors make its total score larger.

### Step 4 — Testing myself

Example reasoning:

- Task A: HIGH priority, due soon, normal status.
- Task B: URGENT priority, but already DONE.
- Task C: MEDIUM priority, overdue, tagged `blocker`.

Before asking AI for the answer, I would calculate the scores myself and then compare them. The important lesson is that the algorithm combines several business rules rather than applying one simple sort key.

### Reflection

My understanding changed from “this sorts by priority” to “this calculates a business score and uses the score for ranking.”

In plain English I would explain it as: **the system gives every task points for how important and urgent it is, subtracts points when the task is already in a less actionable state, adds a few signals such as blocker tags and recent activity, and then sorts using the final score.**

An edge case I noticed is that the function assumes `task.tags` exists because it calls `.some()` on it. Another important area to test is date handling because the score depends on the current time.

---

# 4. Writing Documentation for Complex Code

## Chosen function: `calculateTaskScore`

### What confused me before documentation

The hardest part was not the syntax. It was understanding the reason behind all the separate score adjustments. Someone reading the function needs to know that the final number is a business ranking score, not a database priority value.

### My paraphrased documentation prompt

> Please document this function as JSDoc, but stick strictly to what the code actually does. Explain the input, return value and the scoring rules. Don't invent validation or behaviour that isn't present. Also mention anything that could cause an error if the expected task shape is missing.

### Documentation result I would use

```javascript
/**
 * Calculates a ranking score used to sort tasks by practical importance.
 *
 * The score starts from the task priority and is then adjusted for the
 * due date, current status, special tags and recent updates. Higher scores
 * represent tasks that should appear earlier in an importance-based list.
 *
 * @param {Object} task - Task data used by the scoring rules.
 * @returns {number} The calculated importance score.
 *
 * @throws {TypeError} If required task properties such as tags are missing
 *                     in a way that prevents the calculation from running.
 */
function calculateTaskScore(task) {
    // existing implementation
}
```

I would keep the detailed scoring rules in the surrounding documentation rather than adding huge comments to every line.

### Intent and logic insight

My second AI question was:

> I understand what each block does now. Explain why a developer might have designed the score this way. Separate the actual behaviour from your interpretation so I can tell the difference.

The useful insight was that the function is combining multiple signals into one number because the application needs one consistent way to rank tasks. That is different from saying the scoring weights are objectively correct; those weights are business decisions.

### Final documentation decision

I would combine the factual JSDoc with a short explanation of the scoring policy. I would not document assumptions that cannot be proved from the code.

---

# 5. Writing Documentation for an API

## Chosen API-style operation: Task creation

The repository's Task Manager is primarily a CLI rather than an HTTP service, so I used the task creation operation as an API-style contract rather than pretending there is an Express endpoint that is not in the repository.

The Java `createTask` operation accepts a title, description, numeric priority, optional due date and tags, then creates a `Task` and stores it. fileciteturn166file0

### My documentation prompt

> Treat this method like a small service API. Describe what a caller needs to provide, what comes back, what can go wrong, and give me a simple example. Only document behaviour visible in the code.

### Contract

| Input | Meaning |
|---|---|
| title | The task title. |
| description | Additional task details. |
| priorityValue | 1–4 priority value. |
| dueDateStr | Optional ISO date such as `2026-09-30`. |
| tags | Optional task labels. |

**Return:** the new task ID when creation succeeds; `null` when the date string cannot be parsed.

### Converted format — Markdown API table

| Operation | Purpose | Success result | Failure case |
|---|---|---|---|
| Create task | Creates and stores a new task | New task ID | Invalid date returns `null`; invalid priority can raise an exception |

### Developer guide

1. Give the operation a title.
2. Choose a priority from 1 to 4.
3. Supply a date in `YYYY-MM-DD` format if a deadline is needed.
4. Pass tags when the task needs categorisation.
5. Check the returned ID before assuming the task was stored successfully.

### Reflection

The main lesson was that good API documentation should describe what a caller actually needs to know, not repeat the implementation line by line.

---

# 6. README, User Guide and FAQ

## Project chosen: JavaScript Task Manager CLI

The repository describes it as a command-line application for creating, updating, listing and analysing tasks. It supports priorities, statuses, due dates, tags, statistics and JSON-backed storage. fileciteturn178file0

### README — my version

> **Task Manager CLI**
>
> This is a small command-line task manager for keeping track of work without needing a separate web application. You can create tasks, set priorities and deadlines, update their status, add tags, inspect statistics and store the data locally.
>
> **Getting started**
>
> Make sure Node.js and npm are installed. From the Task Manager folder, run `npm install`, then use `node cli.js` followed by the command you want.
>
> **Example**
>
> `node cli.js create "Finish report" -p 3 -u 2026-09-30 -t "work,important"`
>
> This creates a high-priority task with a due date and two tags.
>
> **Useful commands**
>
> - `create` — add a task
> - `list` — view tasks, optionally filtered by status or priority
> - `status` — change a task's status
> - `priority` — change its priority
> - `due` — update its due date
> - `tag` / `untag` — manage tags
> - `show` — display one task
> - `delete` — remove a task
> - `stats` — show task statistics
>
> **Tests**
>
> Run `npm test` to run the Jest test suite. The repository includes unit and integration tests for the task model, manager and storage layers. fileciteturn178file0

### User guide: creating a task

1. Open a terminal in the Task Manager folder.
2. Run the `create` command.
3. Put the task title in quotes if it contains spaces.
4. Add `-p` when the default medium priority is not suitable.
5. Add `-u` for a deadline.
6. Add `-t` for comma-separated tags.
7. Keep the returned task ID because later commands use it.

### FAQ

**Why can't I see a task I just created?**

Check that you are using the same project/data location and run the list command again.

**What does priority 4 mean?**

It is URGENT. The documented values are 1 LOW, 2 MEDIUM, 3 HIGH and 4 URGENT. fileciteturn178file0

**Can I see only overdue tasks?**

Yes. Use the overdue option on the list command.

**Where are tasks stored?**

The project uses a JSON file named `tasks.json` in the project directory. fileciteturn178file0

**How do I check whether the project is working?**

Run the Jest test suite with `npm test`.

---

# 7. Understanding and Diagnosing Code Errors

## Chosen scenario: Java path validation / invalid file access

I used the FileManager exercise because it gave me a real debugging problem to reason about. The important issue was around path validation: `resolve()` deliberately rejects paths that escape the configured base directory. The corrected `readFile()` now resolves the path before entering the `IOException` catch block, so an invalid path is not silently turned into a normal “file could not be read” result. fileciteturn176file0

### My initial explanation

I would describe the problem as: “The program is checking whether a filename stays inside the allowed directory. If the validation exception is caught too broadly, the caller may never know that someone supplied an unsafe path.”

### My natural AI debugging prompt

> I have an error around file path validation. Please explain the error in plain English first. Then trace what happens from the input filename through the validation method and the catch blocks. I want to understand why the program behaves the way it does, not just see a replacement line of code.

### Root cause

The problem was the location of the `resolve()` call relative to exception handling. The path validation can throw `IllegalArgumentException`, while normal file I/O failures are `IOException`. Treating both the same way hides an important validation failure.

### Fix

Move `resolve(fileName)` outside the `try` block and only catch `IOException` around the actual file-reading operation. That preserves the security validation error instead of swallowing it. The current repository version uses exactly that approach. fileciteturn176file0

### What I would do differently

I would keep validation failures separate from recoverable I/O failures and write a test specifically for path traversal instead of only testing normal file reads.

---

# 8. Performance Optimization Challenge

## Chosen scenario: Java ImageProcessor

The ImageProcessor exercise is about handling large batches of images without keeping unnecessary image data in memory. The current implementation processes one image at a time and explicitly flushes both the original and processed images after writing. fileciteturn175file0

### My initial thought

My first concern was that image objects are large and that processing an entire folder could consume a lot of memory if all images were loaded and retained at once.

### My paraphrased AI prompt

> This image processor can handle a large folder of images. Walk through where memory could build up and explain the difference between processing one image at a time and keeping a whole batch in memory. Point out the exact parts of the code that affect memory usage.

### Bottleneck / risk

The expensive part is image data itself. A `BufferedImage` contains pixel data, so large images can consume significant memory. The nested pixel loop in `applyEffects()` also does work for every pixel. fileciteturn175file0

### Improvements

1. Process images one at a time instead of loading a complete batch.
2. Release image resources after each image has been written.
3. If performance becomes the main issue, consider parallel processing carefully, because more simultaneous images could improve throughput while increasing memory pressure.
4. Benchmark before changing the pixel-processing algorithm, because a change that is theoretically faster may not help the real workload.

### Reflection

The key lesson for me is that performance work should identify the actual bottleneck first. In this case, simply making everything parallel could make memory usage worse rather than better.

---

# 9. Checking Whether an AI Solution Actually Works

## JavaScript Merge Sort

This was one of the exercises I actually implemented and verified.

### Original bug

The bug was in the first loop that copies remaining items from the left array. The code added `left[i]` to the result but incremented `j` instead of `i`. That meant the loop condition never progressed correctly and could keep running indefinitely. The original exercise intentionally contained that mistake.

### My natural AI prompt

> I think there is something wrong in the merge step, but I don't want to assume my first guess is right. Check the function and explain exactly which variable should change in each loop. Then give me a few small examples that would expose the bug.

### Fix

The remaining-left loop must increment `i`. I also made the merge comparison use `<=`, added input validation, and returned a copy for the one-element case so the function does not unnecessarily expose the original array reference.

### Verification approach

I expanded the tests to cover:

- empty arrays
- one item
- already sorted data
- reverse order
- duplicate values
- negative/zero/positive values
- a deterministic large input
- input immutability
- invalid non-array input

### What I learned

The important change was in my attitude to AI-generated fixes. Getting a plausible-looking correction is only the beginning. I need tests that would fail if the proposed fix is wrong.

### Alternative approaches

I could have used a different merge implementation, but the simplest approach was to correct the pointer increment and test the boundaries. The divide-and-merge structure itself is appropriate for merge sort, so replacing the entire algorithm would have added unnecessary risk.

### Critical-eye questions

Before trusting the final version I checked:

- What happens with an empty list?
- What happens with one element?
- What happens with duplicates?
- Does sorting mutate the caller's input?
- What happens with invalid input?
- Does a large input finish instead of getting stuck?

That process gave me much more confidence than simply accepting the first AI answer.

---

# 10. Learning How to Test Code with AI

## Part 1 — Test planning

I used the JavaScript priority functions because the repository contains `calculateTaskScore`, `sortTasksByImportance` and `getTopPriorityTasks`. fileciteturn170file0

### My natural AI prompt

> Don't write the tests for me yet. Ask me questions that force me to explain what this scoring function is supposed to do. After I answer, help me turn my answers into a test checklist.

### My test plan

1. Basic priority score.
2. Higher priority produces a higher starting score.
3. Overdue tasks receive the overdue adjustment.
4. Tasks due today receive the correct adjustment.
5. DONE tasks receive the completion penalty.
6. REVIEW tasks receive the review penalty.
7. Special tags increase the score.
8. Recently updated tasks receive the recent-update boost.
9. Sorting does not mutate the original task array.
10. The top-priority helper respects its limit.

### Part 2 — Improving a test

My first test would be deliberately simple: create a normal task with a known priority and verify that its score matches the expected base score when no other factors apply.

### My AI prompt

> Here is the test I wrote. Don't rewrite it. Ask me what behaviour it actually proves and what it would fail to catch. Then tell me what question I should ask myself to make the test stronger.

For the due-date test, I would use a fixed date rather than relying on the current clock if the test framework allows it. That makes the result repeatable.

### Part 3 — TDD feature: +12 for current-user tasks

The first test should fail because the feature does not exist yet. I would then add the smallest change needed to make that test pass and rerun the tests.

My prompt would be:

> I want to practise TDD. Don't give me the implementation. Help me decide what the first failing test should prove for a +12 score boost when a task belongs to the current user. Once I have the test, ask me what minimal code change should make it pass.

For the “days since update” bug, I would first create a test where the expected age is known. The test should fail against the broken calculation, which proves the bug is real before I change the implementation.

### Part 4 — Integration test

The integration scenario would create several tasks, calculate their scores, sort them, and then request the top few tasks. The point is to confirm that the three functions agree with one another when used as a small workflow.

### Reflection

The biggest surprise was that a good test is not just “does this method return the value I expected?” A useful test describes behaviour. It also needs to control things like time so that the same test does not randomly change its result tomorrow.

---

# 11. Using AI to Improve Code Quality

## Exercise 1 — Readability

For the Java readability example, the main problem is cryptic names such as `U`, `un`, `a()` and `f()`. The names make the reader reverse-engineer the purpose of every statement.

### My prompt

> I can follow the code mechanically, but the names are making it much harder than it needs to be. Suggest names that describe what each class, method and variable actually represents. For every major rename, explain what information the new name gives a reader.

### My conclusion

The best improvement is not fancy syntax. It is naming. A method called `calculateTotal` tells me much more than `f()` before I even read its body.

## Exercise 2 — One giant Python function

The `process_orders` example has too many jobs: validation, inventory checks, pricing, discounts, tax, and shipping.

### My prompt

> Break this function down by responsibility. I want a list of the different jobs it is performing and a sensible helper name for each one. Don't rewrite the whole program; help me design the split first.

### My decomposition

- `validateCustomer()`
- `checkInventory()`
- `calculateSubtotal()`
- `applyDiscount()`
- `calculateTax()`
- `calculateShipping()`
- `buildOrderResult()`

The main function should then read more like a sequence of business steps instead of one huge block.

## Exercise 3 — Repeated JavaScript calculations

The duplicated age/income/score loops are a classic sign that the same algorithm is being copied for different data fields.

### My prompt

> I can see the same loop repeated for several fields. Show me the common pattern you see and suggest a reusable helper, but also tell me when abstraction would become harder for a junior developer to understand.

### My conclusion

A small reusable helper for average and maximum values would remove duplication while keeping the code understandable. I would avoid building a generic “do everything” statistics framework for such a small project.

### Overall reflection

The main thing I learned is that refactoring is not about making code look clever. The goal is to make future changes safer and easier.

---

# 12. Function Decomposition Challenge

## Chosen function: Customer data processor in Java

### Responsibilities I would expect to find

1. Validate customer fields.
2. Check for duplicates.
3. Transform raw data into the application's format.
4. Apply business rules.
5. Save the resulting customer.
6. Handle persistence errors.

### My AI prompt

> This function feels like it has too many responsibilities. Help me list every separate job it is doing. For each job, suggest a small helper name and explain why that responsibility deserves to be separate. I want a decomposition plan, not a complete rewrite.

### Final plan

- `validateCustomer()` — checks required and correctly formatted fields.
- `findExistingCustomer()` — looks for an existing record.
- `transformCustomer()` — converts the input into the internal representation.
- `applyCustomerRules()` — applies business-specific transformations.
- `saveCustomer()` — handles persistence.
- `processCustomer()` — coordinates the steps.

### Complex conditional logic

If the original method contains deeply nested `if` statements, I would first map the paths before changing anything. I would use early returns for invalid cases where appropriate and keep the actual business decisions visible.

### Reflection

The hardest part is deciding where responsibility ends. Splitting every two lines into a function would make the code worse. A helper deserves to exist when the extracted responsibility has a clear name, can be understood independently, or can be tested/reused separately.

---

# 13. Making Code Easier to Read

## Chosen example: Java sorting/algorithm code

### Step 1 — Tests first

The rule I followed is simple: before changing behaviour, make sure the existing tests pass. That gives me a baseline.

### Step 2 — My observations

The first question I ask is whether I can understand the algorithm from the names and structure. If not, I look for missing explanations, unclear variables or long blocks that mix several ideas.

### Step 3 — My prompt

> The algorithm works, so I don't want to change what it calculates. Add only the comments and naming improvements that help someone understand the approach, including the important complexity information. Don't add comments that simply repeat the code.

### Step 4 — Implementation principle

I would apply the changes myself rather than blindly copying AI output. If the AI suggests changing an expression that is actually part of the algorithm, I would treat that as a behaviour change and check it separately.

### Step 5 — Tests again

After the readability changes, I would run the same tests. If the tests fail, I would assume I accidentally changed behaviour until I prove otherwise.

### Reflection

The biggest improvement from readable code is that I can understand it faster. Good variable names reduce the amount of mental translation I have to do.

---

# 14. Improving Code with Design Patterns

## Chosen example: Java Weather Station — Observer pattern

The repository's WeatherStation already has the basic Observer design: observers can register, unregister, and receive updates whenever measurements change. fileciteturn177file0

### Problem in plain English

Without an observer approach, the weather station would need to know every display that needs updating. Every new display would require another direct call.

### My prompt

> I see the weather station notifying several displays when the measurements change. Explain the design problem in normal developer language first, then tell me whether Observer is a sensible fit. I want to understand the reason for the pattern before thinking about code.

### Pattern

**Observer.** The weather station is the subject, while displays or other interested components are observers.

### Implementation plan

1. Define a small observer interface.
2. Let interested displays implement it.
3. Store registered observers in the station.
4. Notify them when measurements change.
5. Allow observers to unsubscribe.

The current repository implementation follows that shape with `addObserver`, `removeObserver`, `setMeasurements`, and `notifyObservers`. fileciteturn177file0

### Tests I would use

- Register an observer and confirm it receives updates.
- Remove an observer and confirm it stops receiving updates.
- Register the same observer twice and confirm it is not duplicated.

### Benefit

Adding a new display no longer requires editing the weather station's measurement logic. The display subscribes to the station instead.

### Reflection

The pattern is useful because it solves a communication problem, not because “Observer” is a magic label. I would not introduce it just because a design-pattern exercise mentions it; I would first check whether the system actually has one source and several interested listeners.

---

# 15. Writing Better Code in My Own Language — JavaScript

## Activity 1 — Making code more idiomatic

The Task Manager JavaScript code already uses useful JavaScript features such as array methods, object spread and `Set`. For example, the merge implementation uses a `Set` to collect unique task IDs and the priority sorter copies the array before sorting it. fileciteturn172file0 fileciteturn170file0

### My prompt

> Here is a function I wrote. Show me how an experienced JavaScript developer might write the same logic while keeping it understandable. Explain each change and point out which JavaScript feature I was missing rather than just replacing my code.

### Three things I learned

1. Prefer built-in collection operations when they make the intent clearer.
2. Avoid mutating input data unless mutation is part of the contract.
3. A shorter solution is not automatically better; readability still matters.

## Activity 2 — Reviewing older code

### My prompt

> Treat this as code I wrote several months ago. Review it with fresh eyes. Identify code smells and explain why each one matters for readability, performance or maintenance. At the end, turn the useful points into a checklist I can use during future reviews.

### My personal checklist

- Are names clear without reading the implementation?
- Is each function doing one main job?
- Am I duplicating the same logic?
- Am I mutating data unexpectedly?
- Are time-dependent calculations testable?
- Are errors handled at the right level?
- Do tests describe behaviour rather than implementation details?

## Activity 3 — Language feature

### Chosen feature: JavaScript `Set`

I chose `Set` because it appears in the task-list merge algorithm and is useful for removing duplicates. The merge code uses a `Set` to build the collection of unique task IDs. fileciteturn172file0

### My prompt

> Explain JavaScript Set to me as if I know arrays well but don't use Set often. Give me three practical situations where it makes sense, explain the trade-offs, and show me one small example I can modify myself.

### Three things I learned

1. A Set is useful when uniqueness is part of the problem.
2. It can make the intent clearer than manually checking whether an array already contains an item.
3. I should still choose the data structure based on the behaviour I need, not because it looks modern.

---

# Overall Reflection

Working through these exercises changed how I use AI. I originally tended to think of AI mainly as a way to get an answer or a piece of code. The exercises made me use it more like a second pair of eyes.

The most useful prompts were the ones that made me explain my own thinking before giving me an answer. Asking AI to question me helped expose gaps that I would probably have missed if I had simply asked, “Explain this code.”

I also learned that verification is just as important as generation. The Merge Sort exercise was a good example: the fix looked small, but without tests it would have been easy to accept a solution without checking edge cases. The same principle applies to refactoring. A cleaner-looking version is not automatically a correct version.

Another lesson was to be careful with AI assumptions. When I ask a question about a codebase, I should provide enough context and ask the AI to distinguish what it can see from what it is assuming. This is especially important around business rules, because a number such as a priority weight or a seven-day threshold may be a business decision rather than a technical fact.

## What I would do next time

1. Start with the project structure before reading individual functions.
2. Write down my own understanding before asking AI.
3. Use AI to challenge my understanding instead of replacing it.
4. Ask for small explanations and examples rather than huge rewrites.
5. Write or improve tests before trusting a code change.
6. Run the tests again after refactoring.
7. Keep prompts specific about what the AI should and should not change.
8. Check AI suggestions against the actual repository code.
9. Separate facts from assumptions, especially for business rules.
10. Keep a short journal of mistakes I made because those mistakes are often the most useful part of the learning process.

## Final takeaway

The biggest change for me is that I now see AI-assisted development as a conversation. I still need to understand the problem, inspect the code, make decisions and verify the result. AI is most useful when it helps me think more clearly rather than when it simply does the thinking for me.
