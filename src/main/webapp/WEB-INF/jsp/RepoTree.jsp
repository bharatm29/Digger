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
    <form action="/download" method="post">
        <input type="hidden" name="download_url" value="${obj.download_url}"/>
        <input type="hidden" name="url" value="${obj.url}"/>
        <input type="hidden" name="name" value="${obj.name}"/>
        <input type="hidden" name="type" value="${obj.type}"/>
        <a href="#" class="file-link" onclick="this.closest('form').submit(); return false;">
                ${obj.name}
        </a>
    </form>
</c:forEach>

<h4 style="color: red;">${error}</h4>
</body>
</html>