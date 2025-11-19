@import org.springframework.validation.FieldError
@import java.util.List

@param String error = "Bad Request"
@param List<FieldError> fieldErrors = null

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>400 - Bad Request</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .error-container {
            background: white;
            padding: 30px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        h1 {
            color: #d9534f;
            margin-top: 0;
        }
        .error-message {
            color: #333;
            margin: 20px 0;
            padding: 15px;
            background: #f8d7da;
            border: 1px solid #f5c6cb;
            border-radius: 4px;
        }
        .field-errors {
            list-style: none;
            padding: 0;
            margin: 20px 0;
        }
        .field-errors li {
            padding: 10px;
            margin: 5px 0;
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            border-radius: 4px;
        }
        .field-name {
            font-weight: bold;
            color: #856404;
        }
        a {
            display: inline-block;
            margin-top: 20px;
            color: #0275d8;
            text-decoration: none;
            padding: 10px 20px;
            background: #e7f3ff;
            border-radius: 4px;
            transition: background 0.2s;
        }
        a:hover {
            background: #cce5ff;
            text-decoration: none;
        }
    </style>
</head>
<body>
<div class="error-container">
    <h1>400 - Bad Request</h1>

    <div class="error-message">
        ${error}
    </div>

    @if(fieldErrors != null && !fieldErrors.isEmpty())
        <h3>Validation Errors:</h3>
        <ul class="field-errors">
            @for(FieldError fieldError : fieldErrors)
                <li>
                    <span class="field-name">${fieldError.getField()}</span>:
                    ${fieldError.getDefaultMessage()}
                </li>
            @endfor
        </ul>
    @endif

    <a href="/">Return to Home</a>
</div>
</body>
</html>