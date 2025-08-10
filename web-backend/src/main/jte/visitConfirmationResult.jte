@import com.statusneo.vms.dto.VerificationResult
@param VerificationResult result
@param Long visitId

<!DOCTYPE html>
<html>
<head>
    <title>Visit Confirmation</title>
</head>
<body>
<h1>Visit Confirmation Result</h1>

#if(result.success())
<p style="color: green;">${result.message()}</p>
#else
<p style="color: red;">${result.message()}</p>
#if(result.reattempt())
<form action="/api/visitors/resend-otp" method="post">
    <input type="hidden" name="visitId" value="${visitId}" />
    <button type="submit">Resend OTP</button>
</form>
#endif
#endif

<a href="/api/visitors/confirm-form">Back</a>
</body>
</html>
