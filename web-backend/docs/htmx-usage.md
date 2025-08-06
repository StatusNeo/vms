
# HTMX + Spring Boot + JTE: Developer Guidelines

## 1. What Is HTMX (Quick Reminder)

* HTMX allows you to update parts of a web page using HTML attributes like `hx-get`, `hx-post`, etc.
* Your backend returns HTML fragments (not JSON, not full pages).
* HTMX swaps the returned HTML into the DOM at a specified target.

---

## 2. Core HTMX Attributes to Know

| Attribute             | Purpose                                 |
| --------------------- | --------------------------------------- |
| `hx-get="/url"`       | GET request triggered by element action |
| `hx-post="/url"`      | POST request                            |
| `hx-target="#id"`     | Where to insert the response fragment   |
| `hx-swap="innerHTML"` | How to insert it (default is innerHTML) |
| `hx-trigger="event"`  | Customize the trigger (optional)        |

Example:

```html
<button hx-get="/tasks/fragment" hx-target="#taskList">Load Tasks</button>
<div id="taskList"></div>
```

---

## 3. How to Structure Templates

Use JTE to create partial templates that HTMX can dynamically inject.

Suggested folder structure:

```
templates/
├── pages/         # Full-page views
├── fragments/     # HTMX fragment templates
└── layouts/       # Shared structure/layouts
```

---

## 4. Controller Guidelines

Use `@Controller`, not `@RestController`. Return template paths (no `@ResponseBody`).

```java
@GetMapping("/tasks/fragment")
public String getTasks(Model model) {
    model.addAttribute("tasks", taskService.getAll());
    return "fragments/taskList";  // renders fragments/taskList.jte
}
```

---

## 5. Template Fragment Example (JTE)

```html
<!-- fragments/taskList.jte -->
<ul>
  @for(task : tasks)
    <li>${task.name}</li>
  @end
</ul>
```

---

## 6. Endpoint Naming Convention

| Use Case       | URL               | Template                  |
| -------------- | ----------------- | ------------------------- |
| Full page      | `/tasks`          | `pages/tasks.jte`         |
| HTMX fragment  | `/tasks/fragment` | `fragments/taskList.jte`  |
| Modal fragment | `/tasks/modal`    | `fragments/taskModal.jte` |

---

## 7. ❌ Things Not To Do

| Don’t Do This                            | Do This Instead                          |
| ---------------------------------------- | ---------------------------------------- |
| Use `@RestController` for HTMX           | Use `@Controller` with template return   |
| Return JSON to HTMX endpoints            | Return server-rendered HTML fragments    |
| Manually use `fetch()`/AJAX              | Use HTMX attributes like `hx-get`        |
| Return full-page templates for fragments | Return only the needed fragment          |
| Mix HTMX with client-side SPA logic      | Let the server drive rendering           |
| Expect HTMX to handle routing            | Keep routing and rendering server-driven |

Anti-patterns:

```java
// ❌ Wrong: JSON API used in HTMX context
@GetMapping("/tasks/api")
@ResponseBody
public List<Task> getTasks() {
    return taskService.getAll();
}
```

```html
<!-- ❌ Wrong: Writing custom JS to fetch fragment -->
<script>
fetch('/tasks/fragment').then(...);
</script>
```

---

## 8. Debugging Tips

* Use browser DevTools → Network tab to inspect HTMX requests.
* Add a spinner with `hx-indicator="#spinner"` on the requesting element.
* Temporarily enable `?debug=true` or inspect the HTML response directly.

---

## 9. Optional Enhancements

* Use `hx-boost="true"` in `<body>` to enhance all links and forms automatically.
* Use `hx-trigger` for throttled or custom behavior, e.g. `hx-trigger="keyup changed delay:300ms"`.
* Use `hx-headers` if custom headers are needed for special handling.

---

