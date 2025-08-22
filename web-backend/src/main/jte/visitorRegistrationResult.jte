@param String message
@param boolean success

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Visitor Registration</title>
    <link href="/css/output.css" rel="stylesheet">
</head>
<body class="bg-gray-100 min-h-screen flex items-center justify-center">
<div class="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md text-center">
    <h1 class="text-2xl font-bold mb-6 text-gray-700">Visitor Registration</h1>

    #if(success)
    <p class="text-green-600 font-medium mb-6">${message}</p>
    <a href="/api/visitors/register"
       class="inline-block mt-4 text-blue-500 hover:underline">
        Register Another Visitor
    </a>
    #else
    <p class="text-red-600 font-medium mb-6">${message}</p>
    <a href="/api/visitors/register"
       class="inline-block mt-4 text-blue-500 hover:underline">
        Try Again
    </a>
    #endif
</div>
</body>
</html>