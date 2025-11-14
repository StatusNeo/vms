<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Visitor Check-in</title>
    <link href="/main.css" rel="stylesheet">
    <script src="/webjars/htmx.org/2.0.7/dist/htmx.min.js"></script>
</head>
<body class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">

<!-- Top-level modal container so HTMX swaps can render full-screen overlays without being clipped -->
<div id="modal-container"></div>

<div class="container mx-auto px-4 py-8 max-w-4xl">
    <!-- Header -->
    <div class="text-center mb-8">
        <div class="flex justify-center mb-4">
            <img id="site-logo" src="/images/StatusNeo-logo.png" alt="StatusNeo Logo" style="height:36px !important; max-height:36px !important; width:auto !important;" class="mx-auto">
        </div>
        <h1 class="text-3xl font-bold text-gray-800 mb-2">Visitor Check-in</h1>
        <p class="text-gray-600">Please fill in your details to register your visit</p>
    </div>

    <!-- Main Form -->
    <div class="bg-white rounded-2xl shadow-xl p-6 md:p-8">
        <!-- Modal Container -->
        <div id="modal-container"></div>

        <form id="visitor-form"
              hx-post="/register"
              hx-target="#modal-container"
              hx-swap="innerHTML"
              hx-trigger="submit"
              class="space-y-6">
            <input type="hidden" name="_csrf" value="">

            <div id="form-content">
                <!-- Two Column Grid for Tablet/Desktop -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">

                    <!-- Name Field -->
                    <div class="space-y-2">
                        <label for="name" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                            </svg>
                            Full Name *
                        </label>
                        <input type="text"
                               id="name"
                               name="name"
                               required
                               class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                               placeholder="Enter your full name">
                    </div>

                    <!-- Email Field -->
                    <div class="space-y-2">
                        <label for="email" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                            </svg>
                            Email Address *
                        </label>
                        <input type="email"
                               id="email"
                               name="email"
                               required
                               class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                               placeholder="Enter your email address">
                    </div>

                    <!-- Phone Field -->
                    <div class="space-y-2">
                        <label for="phone" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"/>
                            </svg>
                            Phone Number *
                        </label>
                        <input type="tel"
                               id="phone"
                               name="phoneNumber"
                               required
                               class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                               placeholder="Enter your phone number">
                    </div>

                    <!-- Company Field -->
                    <div class="space-y-2">
                        <label for="company" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                            </svg>
                            Company *
                        </label>
                        <input type="text"
                               id="company"
                               name="company"
                               required
                               class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                               placeholder="Enter your company name">
                    </div>

                    <!-- Laptop Number Field -->
                    <div class="space-y-2">
                        <label for="laptop" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                            </svg>
                            Laptop Number
                        </label>
                        <input type="text"
                               id="laptop"
                               name="laptop"
                               class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                               placeholder="Enter your laptop number (optional)">
                    </div>

                    <!-- Host Dropdown - Full Width -->
                    <div class="md:col-span-2 space-y-2">
                        <label for="host-search" class="block text-sm font-medium text-gray-700">
                            <svg class="inline w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                            </svg>
                            Who are you visiting? *
                        </label>
                        <div class="relative">
                            <input type="text"
                                   id="host-search"
                                   name="hostSearch"
                                   autocomplete="off"
                                   hx-get="/search-employees"
                                   hx-trigger="keyup changed delay:300ms"
                                   hx-target="#host-results"
                                   hx-indicator="#search-spinner"
                                   class="w-full px-4 py-3 pr-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                                   placeholder="Search by name or email...">
                            <input type="hidden" id="host" name="host">
                            <svg class="absolute right-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
                            </svg>
                            <div id="search-spinner" class="htmx-indicator absolute right-10 top-1/2 transform -translate-y-1/2">
                                <svg class="animate-spin h-5 w-5 text-blue-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                </svg>
                            </div>

                            <!-- Dropdown Results -->
                            <div id="host-results"
                                 class="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-48 overflow-y-auto hidden">
                                <!-- Results will be populated by HTMX -->
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Submit Button -->
                <div class="flex justify-center pt-4">
                    <button type="submit"
                            id="send-otp-btn"
                            class="px-8 py-4 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors min-w-48 text-lg relative flex items-center justify-center">
                        <span class="htmx-indicator:hidden">Send OTP</span>
                        <span class="htmx-indicator absolute inset-0 flex items-center justify-center">
                            <svg class="inline animate-spin h-5 w-5 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            Sending...
                        </span>
                    </button>
                </div>
            </div>
        </form>

        <!-- Footer -->
        <div class="mt-6 text-center text-sm text-gray-500">
            By checking in, you agree to our visitor policies and terms of service.
        </div>
    </div>
</div>

<!-- Removed a small duplicate autocomplete handler here in favor of the more complete handlers below -->

<script>
    // Form validation (host optional)
    function validateForm() {
        const name = document.getElementById('name').value;
        const email = document.getElementById('email').value;
        const phone = document.getElementById('phone').value;
        const company = document.getElementById('company').value;
        // host is optional, do not require hostId to enable send

        const isValid = name && email && phone && company;
        document.getElementById('send-otp-btn').disabled = !isValid;
    }

    // Attach validation to all inputs that are required
    document.querySelectorAll('input[required]').forEach(input => {
        input.addEventListener('input', validateForm);
    });
</script>

<script>
    // Add keyboard selection and HTMX guards for host autocomplete
    // Prevent HTMX requests on Enter and prevent empty searches
    document.addEventListener("htmx:configRequest", (event) => {
        const keyEvent = event.detail.triggeringEvent;
        if (keyEvent?.type === "keyup" && keyEvent.key === "Enter") {
            event.preventDefault();
        }
    });

    document.body.addEventListener('htmx:beforeRequest', function(event) {
        const source = event.detail?.elt;
        if (!source || source.id !== 'host-search') return;
        const input = document.getElementById('host-search');
        const results = document.getElementById('host-results');
        if (!input || input.value.trim() === '') {
            event.preventDefault();
            if (results) results.style.display = 'none';
        }
    });

    // When HTMX swaps in host results, show the dropdown and (re)attach handlers
    document.body.addEventListener('htmx:afterSwap', function(event) {
        // If the swap targeted host-results, ensure it's visible and attach click handlers
        try {
            if (event.detail.target.id === 'host-results') {
                const box = document.getElementById('host-results');
                if (box) {
                    box.classList.remove('hidden');
                    box.style.display = 'block';
                    const items = box.querySelectorAll('li');
                    items.forEach(item => {
                        // If server didn't attach onclick, attach a safe handler
                        if (!item.onclick) {
                            item.addEventListener('click', function() {
                                const name = this.dataset.name || this.textContent.trim();
                                const id = this.dataset.id || this.getAttribute('data-id');
                                selectEmployee(name, id);
                            });
                        }
                    });
                }
            }
        } catch (e) {
            // swallow any errors to avoid breaking other htmx swaps
            console.error(e);
        }
    });

    // Hide dropdown immediately on click swaps to avoid flicker
    document.body.addEventListener('htmx:beforeSwap', function(event) {
        const targetId = event.target.getAttribute('id');
        if (targetId === 'host-results') {
            const triggering = event.detail.requestConfig && event.detail.requestConfig.triggeringEvent;
            if (triggering && triggering.type === 'click') {
                const box = document.getElementById('host-results');
                if (box) box.style.display = 'none';
            }
        }
    });

    // Make clicking a suggestion populate the input and hidden field
    function selectEmployee(name, id) {
        const input = document.getElementById('host-search');
        const hidden = document.getElementById('host');
        if (input) input.value = name || '';
        if (hidden) hidden.value = id || '';
        const box = document.getElementById('host-results');
        if (box) {
            box.classList.add('hidden');
            box.style.display = 'none';
        }
    }

    // Keyboard navigation for host-search (arrow keys + enter + escape)
    const hostSearch = document.getElementById('host-search');
    if (hostSearch) {
        hostSearch.addEventListener('keydown', (event) => {
            const suggestionBox = document.getElementById('host-results');
            if (!suggestionBox || suggestionBox.children.length === 0) return;

            const suggestions = suggestionBox.querySelectorAll('li');
            let current = suggestionBox.querySelector('.highlight');

            switch (event.key) {
                case 'ArrowDown':
                    event.preventDefault();
                    if (!current) {
                        suggestions[0].classList.add('highlight');
                    } else {
                        current.classList.remove('highlight');
                        (current.nextElementSibling || suggestions[0]).classList.add('highlight');
                    }
                    break;

                case 'ArrowUp':
                    event.preventDefault();
                    if (!current) {
                        suggestions[suggestions.length - 1].classList.add('highlight');
                    } else {
                        current.classList.remove('highlight');
                        (current.previousElementSibling || suggestions[suggestions.length - 1]).classList.add('highlight');
                    }
                    break;

                case 'Enter':
                    event.preventDefault();
                    if (current) {
                        const name = current.dataset.name || current.textContent.trim();
                        const id = current.dataset.id || current.getAttribute('data-id');
                        selectEmployee(name, id);
                    }
                    break;

                case 'Escape':
                    event.preventDefault();
                    suggestionBox.innerHTML = '';
                    suggestionBox.style.display = 'none';
                    break;
            }
        });
    }

    // Add minimal highlight style if not present (ensures keyboard nav is visible)
    (function ensureHighlightStyle(){
        const id = 'host-highlight-style';
        if (!document.getElementById(id)) {
            const style = document.createElement('style');
            style.id = id;
            style.innerHTML = '.highlight{background-color:#f3f4f6;font-weight:600}';
            document.head.appendChild(style);
        }
    })();
</script>
</body>
</html>