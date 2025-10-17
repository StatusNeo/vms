@param Long visitId

<!-- OTP Verification Modal Fragment -->
<div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" id="otp-modal">
    <div class="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md">
        <h1 class="text-2xl font-bold text-center mb-6 text-gray-800">OTP Verification</h1>

        <p class="text-center text-gray-600 mb-6">Enter the 6-digit OTP sent to your phone</p>

        <form hx-post="/confirm-visit" 
              hx-target="#otp-modal" 
              hx-swap="outerHTML" 
              class="space-y-6">
            <!-- Hidden visitId -->
            <input type="hidden" name="visitId" value="${visitId}" />
            <input type="hidden" name="_csrf" value="">

            <!-- OTP input -->
            <div>
                <input type="text" 
                       id="otp" 
                       name="otpCode" 
                       maxlength="6" 
                       pattern="\d{6}"
                       required
                       class="w-full h-14 px-3 border-2 border-green-500 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-center text-2xl font-mono tracking-widest"
                       placeholder="000000" />
            </div>

            <!-- Verify Button -->
            <button type="submit"
                    class="w-full bg-green-600 text-white py-3 rounded-lg hover:bg-green-700 transition duration-200 font-bold">
                Verify & Check In
            </button>
        </form>

        <div class="mt-4 text-center">
            <button onclick="document.getElementById('otp-modal').remove()" 
                    class="text-gray-500 hover:underline text-sm">
                Cancel
            </button>
        </div>
    </div>
</div>
