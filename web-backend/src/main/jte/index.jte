<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StatusNeo Visitor Management</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
    <script src="https://unpkg.com/htmx.org@1.6.1"></script>
    <!-- Add your custom CSS here -->
    <style>
        .highlight {
            background-color: #f3f4f6; /* Light-gray background to indicate selection */
            font-weight: bold;        /* Bold text for better visibility of selection */
        }
    </style>

</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
<div id="form-container" class="bg-white p-8 rounded-lg shadow-md w-full max-w-3xl">
    <div class="flex justify-center">
    <h1 class="text-2xl font-bold mb-6 text-gray-800">Welcome to </h1>
    </div>
<%--    <div class="flex justify-center bg-yellow-500">--%>
    <div class="flex justify-center">
<%--        <img src="/images/StatusNeo-Logo-Light.png" alt="StatusNeo Logo" class="mx-auto filter grayscale brightness-0">--%>
        <img src="/images/StatusNeo-logo.png" alt="StatusNeo Logo" class="mx-auto">
    </div>

    <div class="mb-6">
        <h2 class="text-xl font-semibold mb-4 text-gray-700 border-b pb-2">Visitor Registration</h2>

        <form hx-post="/register" hx-target="#form-container" hx-swap="innerHTML" class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="space-y-4">
                <div>
                    <label for="visitorName" class="block text-sm font-medium text-gray-700 mb-1">Visitor name</label>
                    <input type="text" id="visitorName" name="visitorName" placeholder="Enter visitor name" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>

                <div>
                    <label for="visitorEmail" class="block text-sm font-medium text-gray-700 mb-1">Visitor email</label>
                    <input type="email" id="visitorEmail" name="visitorEmail" placeholder="Enter visitor email" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>

                <div>
                    <label for="visitorPhone" class="block text-sm font-medium text-gray-700 mb-1">Visitor phone</label>
                    <input type="tel" id="visitorPhone" name="visitorPhone" placeholder="Enter visitor phone" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>

                <div>
                    <label for="guestCount" class="block text-sm font-medium text-gray-700 mb-1">No. of guests</label>
                    <input type="number" id="guestCount" name="guestCount" placeholder="Enter number of guests" class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
            </div>

            <div class="space-y-4">
                <div class="relative">
                    <input type="text"
                           id="employeeSearch"
                           name="query"
                           placeholder="Search"
                           hx-get="/search-employees"
                           hx-trigger="keyup changed delay:300ms"
                           hx-target="#employeeSuggestions"
                           hx-swap="outerHTML"
                           class="w-full px-3 py-2 pl-10 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                           hx-include="#employeeSearch">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <i class="fas fa-search text-gray-400"></i>
                    </div>

                    <div id="employeeSuggestions"
                         class="absolute bg-white shadow-md w-full mt-1 rounded-md z-10"
                         hx-on="mouseleave: this.style.display='none';"
                         class="hover:bg-gray-100">
                        <!-- Dynamically generated suggestions -->
                    </div>

                </div>

                <div class="mt-6">
                    <button type="submit" class="w-full bg-yellow-500 text-black font-bold py-2 px-4 rounded-md hover:bg-yellow-600 focus:outline-none focus:ring-2 focus:ring-yellow-500 focus:ring-offset-2">
                        Register
                    </button>
                </div>
            </div>
        </form>
    </div>
</div>

<script>
    document.addEventListener("htmx:configRequest", (event) => {
    const triggeringEvent = event.detail.elt?.getAttribute("hx-trigger");
    const keyEvent = event.detail.triggeringEvent;

    // Prevent HTMX from sending the request if Enter key is pressed
    if (keyEvent?.type === "keyup" && keyEvent.key === "Enter") {
        event.preventDefault(); // Stop HTMX from triggering the request
    }
});
    document.body.addEventListener("htmx:beforeRequest", (event) => {
        const input = document.getElementById("employeeSearch");
        const suggestionBox = document.getElementById("employeeSuggestions");

        // Prevent requests if the input is empty and hide the dropdown
        if (input.value.trim() === "") {
            event.preventDefault(); // Stop the request
            if (suggestionBox) suggestionBox.style.display = "none"; // Hide dropdown
        }
    });

    document.body.addEventListener("htmx:afterSwap", (event) => {
        const targetId = event.target.getAttribute("id");
        if (targetId === "employeeSuggestions") {
            const suggestionBox = document.getElementById("employeeSuggestions");
            const suggestions = suggestionBox.querySelectorAll("li");

            // Reattach event listeners for dynamically generated suggestions
            suggestions.forEach((item) => {
                item.addEventListener("click", () => {
                    const searchBox = document.getElementById("employeeSearch");
                    searchBox.value = item.textContent.trim(); // Update input with selection

                    suggestionBox.innerHTML = ""; // Clear suggestions
                    suggestionBox.style.display = "none"; // Hide dropdown
                });
            });

            // Manage dropdown visibility based on the presence of suggestions
            if (suggestions.length === 0) {
                suggestionBox.style.display = "none"; // Hide if no suggestions exist
            } else {
                suggestionBox.style.display = "block"; // Show if suggestions exist
            }
        }
    });

    document.body.addEventListener("htmx:beforeSwap", (event) => {
        const targetId = event.target.getAttribute("id");
        if (targetId === "employeeSuggestions") {
            const triggeringEvent = event.detail.requestConfig.triggeringEvent;

            // Immediately hide the dropdown on a click event
            if (triggeringEvent && triggeringEvent.type === "click") {
                const suggestionBox = document.getElementById("employeeSuggestions");
                suggestionBox.style.display = "none";
            }
        }
    });

    const searchBox = document.getElementById("employeeSearch");
    searchBox.addEventListener("keydown", (event) => {
        const suggestionBox = document.getElementById("employeeSuggestions");
        if (!suggestionBox || suggestionBox.children.length === 0) return; // Exit if no suggestions

        const suggestions = suggestionBox.querySelectorAll("li");
        let current = suggestionBox.querySelector(".highlight");

        switch (event.key) {
            case "ArrowDown":
                event.preventDefault();
                if (!current) {
                    suggestions[0].classList.add("highlight");
                } else {
                    current.classList.remove("highlight");
                    (current.nextElementSibling || suggestions[0]).classList.add("highlight");
                }
                break;

            case "ArrowUp":
                event.preventDefault();
                if (!current) {
                    suggestions[suggestions.length - 1].classList.add("highlight");
                } else {
                    current.classList.remove("highlight");
                    (current.previousElementSibling || suggestions[suggestions.length - 1]).classList.add("highlight");
                }
                break;

            case "Enter":
                event.preventDefault(); // Stop default behavior (form submission, etc.)
                if (current) {
                    searchBox.value = current.textContent.trim(); // Update input with selection
                    suggestionBox.innerHTML = ""; // Clear suggestions
                    suggestionBox.style.display = "none"; // Hide dropdown
                }
                break;

            case "Escape":
                event.preventDefault();
                suggestionBox.innerHTML = ""; // Clear suggestions
                suggestionBox.style.display = "none"; // Hide dropdown
                break;
        }
    });
</script>


</body>
</html>
