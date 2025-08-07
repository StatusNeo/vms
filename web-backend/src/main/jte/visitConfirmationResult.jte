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
<p style="color: green;">Visit confirmed successfully for ID: ${visitId}</p>
#else
<p style="color: red;">Failed to confirm visit: ${result.message()}</p>
#if(result.reattempt())
<p>You can try again.</p>
#endif
#endif

<a href="/api/visitors/confirm-form">Back</a>
</body>
</html>
