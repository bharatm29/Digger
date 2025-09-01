<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Repo Tree</title>
</head>
<body>
<c:forEach var="obj" items="${objs}">
    ${obj.name}<br>
    ${obj.download_url}<br>
    ${obj.url}<br>
    ${obj.type}<br>
    <hr>
</c:forEach>
</body>
</html>