<%--@template.header()--%>
<div class="relative">
    <label for="employee" class="block text-sm font-medium text-gray-700">Employee to Meet *</label>
    <input type="text" id="employee" name="employee"
           hx-get="/search-employees"
           hx-trigger="keyup changed delay:200ms"
           hx-target="#employeeSuggestions"
           hx-swap="innerHTML"
           class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
    <input type="hidden" id="host" name="host">
    <ul id="employeeSuggestions" class="absolute bg-white w-full border border-gray-300 rounded-md mt-1 shadow-lg z-10 hidden"></ul>
</div>
<%--@template.footer()--%>