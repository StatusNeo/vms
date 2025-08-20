@import com.statusneo.vms.model.Employee
@import java.util.List
@param List<Employee> employees

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Visitor Registration</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/htmx.org@1.9.10"></script>
</head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
<div class="bg-white p-8 rounded-lg shadow-md w-full max-w-lg">
    <h2 class="text-2xl font-bold mb-6 text-center">Visitor Registration</h2>

    <!-- Registration Form -->
    <form id="visitorForm"
          hx-post="/api/visitors/register"
          hx-target="#formResponse"
          hx-swap="innerHTML"
          class="space-y-4">

        <div>
            <label class="block text-gray-700">Full Name</label>
            <input type="text" name="name" required
                   class="w-full border border-gray-300 p-2 rounded"/>
        </div>

        <div>
            <label class="block text-gray-700">Email</label>
            <input type="email" name="email" required
                   class="w-full border border-gray-300 p-2 rounded"/>
        </div>

        <div>
            <label class="block text-gray-700">Phone Number</label>
            <input type="text" name="phoneNumber" pattern="\d{10}" required
                   class="w-full border border-gray-300 p-2 rounded"/>
        </div>

        <div>
            <label class="block text-gray-700">Address</label>
            <textarea name="address" rows="2" required
                      class="w-full border border-gray-300 p-2 rounded"></textarea>
        </div>

        <div>
            <label class="block text-gray-700">Host (Employee)</label>
            <select name="hostId" required
                    class="w-full border border-gray-300 p-2 rounded">
                <option value="">Select Host</option>
                @for(Employee employee : employees)
                    <option value="${employee.getId()}">${employee.getName()} (${employee.getEmail()})</option>
                @endfor
            </select>
        </div>

        <button type="submit"
                class="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700">
            Register Visitor
        </button>
    </form>

    <div id="formResponse" class="mt-6"></div>
</div>
</body>
</html>
