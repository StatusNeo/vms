<%--@template.header()--%>
<div id="otpSection" class="hidden">
    <label for="otp" class="block text-sm font-medium text-gray-700">OTP *</label>
    <div class="flex gap-2">
        <input type="number" id="otp" name="otp" required
               oninput="this.value=this.value.slice(0,6)"
               class="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500">
        <button type="button" onclick="verifyOTP()"
                class="bg-yellow-500 text-black font-bold py-2 px-4 rounded-md hover:bg-yellow-600">
            Verify OTP
        </button>
    </div>
    <div id="otpMessage" class="mt-2 text-sm"></div>
</div>
<%--@template.footer()--%>