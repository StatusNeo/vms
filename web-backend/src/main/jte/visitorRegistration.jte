@import com.statusneo.vms.model.Employee
@import java.util.List
@param List<Employee> employees

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Visitor Registration</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
<div class="bg-white p-8 rounded-lg shadow-md w-full max-w-lg">
    <h2 class="text-2xl font-bold mb-6 text-center">Visitor Registration</h2>

    <form id="visitorForm" class="space-y-4">
        <div>
            <label class="block text-gray-700">Full Name</label>
            <input type="text" name="name" required
                   class="w-full border border-gray-300 p-2 rounded" />
        </div>

        <div>
            <label class="block text-gray-700">Email</label>
            <input type="email" name="email" required
                   class="w-full border border-gray-300 p-2 rounded" />
        </div>

        <div>
            <label class="block text-gray-700">Phone Number</label>
            <input type="text" name="phoneNumber" pattern="\\d{10}" required
                   class="w-full border border-gray-300 p-2 rounded" />
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
                @for (Employee employee : employees)
                    <option value="${employee.getId()}">${employee.getName()} (${employee.getEmail()})</option>
                @endfor
            </select>
        </div>

        <button type="submit"
                class="w-full bg-blue-600 text-white p-2 rounded hover:bg-blue-700">
            Register Visitor
        </button>
    </form>

    <div id="otpSection" class="mt-6 hidden">
        <label class="block text-gray-700 mb-2">Enter OTP sent to your email</label>
        <input type="text" id="otpInput"
               class="w-full border border-gray-300 p-2 rounded mb-4" />

        <button id="verifyOtpBtn"
                class="w-full bg-green-600 text-white p-2 rounded hover:bg-green-700">
            Verify OTP
        </button>
    </div>

    <div id="message" class="mt-4 text-center"></div>
</div>

<script>
    const form = document.getElementById("visitorForm");
    const messageDiv = document.getElementById("message");
    const otpSection = document.getElementById("otpSection");
    const verifyOtpBtn = document.getElementById("verifyOtpBtn");

    let currentVisitId = null;

    form.addEventListener("submit", function (e) {
        e.preventDefault();

        const formData = new FormData(form);
        const visitor = {
            name: formData.get("name"),
            email: formData.get("email"),
            phoneNumber: formData.get("phoneNumber"),
            address: formData.get("address"),
            host: { id: formData.get("hostId") }
        };

        fetch("/api/visitors/register", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(visitor)
        })
        .then(response => response.text())
        .then(text => {
            const match = text.match(/Visit ID: (\d+)/);
            if (match) {
                currentVisitId = match[1];
                otpSection.classList.remove("hidden");
                messageDiv.innerHTML = "<p class='text-blue-600 font-semibold'>OTP sent to your email. Please enter it below.</p>";
            } else {
                messageDiv.innerHTML = "<p class='text-red-600'>" + text + "</p>";
            }
        });
    });

  verifyOtpBtn.addEventListener("click", function () {
    const otp = document.getElementById("otpInput").value;

    fetch("/api/visitors/validate-otp?visitId=" + currentVisitId + "&otp=" + otp, {
        method: "POST"
    })
    .then(response => response.text())
    .then(html => {
        messageDiv.innerHTML = html;
    });
});
</script>
</body>
</html>
