@import com.statusneo.vms.dto.VerificationResult
@param VerificationResult result
@param Long visitId

<!-- Visit Confirmation Modal -->
<div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md text-center">
        <h1 class="text-2xl font-bold mb-6 text-gray-700">Visit Confirmation</h1>

        #if(result.success())
        <p class="text-green-600 font-medium mb-6">${result.message()}</p>
        <button onclick="location.reload()" class="w-full bg-green-600 text-white py-3 rounded-lg hover:bg-green-700 transition duration-200 font-bold">
            Done
        </button>
        #else
        <p class="text-red-600 font-medium mb-6">${result.message()}</p>
        #if(result.reattempt())
        <form hx-post="/resend-otp" hx-target="#modal-container" hx-swap="innerHTML" class="mb-4">
            <input type="hidden" name="visitId" value="${visitId}" />
            <button type="submit" class="w-full bg-yellow-500 text-white py-3 rounded-lg hover:bg-yellow-600 transition font-bold">
                Resend OTP
            </button>
        </form>
        #endif
        <button onclick="document.getElementById('modal-container').innerHTML='';" class="w-full bg-gray-500 text-white py-3 rounded-lg hover:bg-gray-600 transition duration-200 font-bold">
            Close
        </button>
        #endif
    </div>
</div>
