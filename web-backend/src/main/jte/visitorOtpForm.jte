@param Long visitId

<!DOCTYPE html>
<html>
<head>
    <title>OTP Verification</title>
    <link href="/main.css" rel="stylesheet">
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
<div class="bg-white shadow-lg rounded-2xl p-8 w-full max-w-md">
    <h1 class="text-2xl font-bold text-center mb-6 text-gray-800">OTP Verification</h1>

    <form action="/api/visitors/verify-otp" method="post" class="space-y-6">
        <!-- Hidden visitId -->
        <input type="hidden" name="visitId" value="${visitId}" />

        <!-- OTP input -->
        <div>
            <label for="otp" class="block text-sm font-medium text-gray-700 mb-2">Enter OTP</label>
            <input type="text" id="otp" name="otp"
                   class="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:outline-none"
                   placeholder="Enter the OTP" required />
        </div>

        <!-- Submit -->
        <button type="submit"
                class="w-full bg-blue-600 text-white py-3 rounded-lg hover:bg-blue-700 transition duration-200">
            Verify OTP
        </button>
    </form>

    <div class="mt-4 text-center">
        <a href="/api/visitors/confirm-form"
           class="text-blue-600 hover:underline text-sm">Back</a>
    </div>
</div>
</body>
</html>