@import com.statusneo.vms.dto.VerificationResult
@param VerificationResult result
@param Long visitId

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Visit Confirmation Result</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
<div class="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md text-center">
    <h1 class="text-2xl font-bold mb-6 text-gray-700">Visit Confirmation</h1>

    #if(result.success())
    <p class="text-green-600 font-medium mb-6">${result.message()}</p>
    #else
    <p class="text-red-600 font-medium mb-6">${result.message()}</p>

    #if(result.reattempt())
    <form action="/api/visitors/resend-otp" method="post" class="mb-4">
        <input type="hidden" name="visitId" value="${visitId}" />
        <button type="submit"
                class="w-full bg-yellow-500 text-white py-3 rounded-lg hover:bg-yellow-600 transition">
            Resend OTP
        </button>
    </form>
    #endif
    #endif

    <a href="/api/visitors/confirm-form"
       class="inline-block mt-4 text-blue-500 hover:underline">
        Back to Confirmation
    </a>
</div>
</body>
</html>